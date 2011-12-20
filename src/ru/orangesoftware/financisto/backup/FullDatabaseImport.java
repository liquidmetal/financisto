/*
 * Copyright (c) 2011 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */
package ru.orangesoftware.financisto.backup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.service.RecurrenceScheduler;
import ru.orangesoftware.financisto.utils.CurrencyCache;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static ru.orangesoftware.financisto.backup.Backup.tableHasSystemIds;

public abstract class FullDatabaseImport {

	protected final Context context;
	protected final DatabaseAdapter dbAdapter;
    protected final MyEntityManager em;
	protected final SQLiteDatabase db;

	public FullDatabaseImport(Context context, DatabaseAdapter dbAdapter) {
		this.context = context;
		this.dbAdapter = dbAdapter;
		this.db = dbAdapter.db();
        this.em = dbAdapter.em();
	}

	public void importDatabase() throws IOException {
        db.beginTransaction();
        try {
            cleanDatabase();
            restoreDatabase();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        CurrencyCache.initialize(em);
        long t0 = System.currentTimeMillis();
        dbAdapter.recalculateAccountsBalances();
        long t1 = System.currentTimeMillis();
        Log.i("Financisto", "Import: Recalculating balances done in " + TimeUnit.MILLISECONDS.toSeconds(t1 - t0) + "s");
        dbAdapter.rebuildRunningBalance();
        long t2 = System.currentTimeMillis();
        Log.i("Financisto", "Import: Updating running balances done in " + TimeUnit.MILLISECONDS.toSeconds(t2 - t1) + "s");
        scheduleAll();
    }

    protected abstract void restoreDatabase() throws IOException;

    private void cleanDatabase() {
        for (String tableName : tablesToClean()) {
            if (tableHasSystemIds(tableName) && shouldKeepSystemEntries()) {
                db.execSQL("delete from "+tableName+" where _id>0");
            } else {
                db.execSQL("delete from "+tableName);
            }
        }
    }

    protected boolean shouldKeepSystemEntries() {
        return false;
    }

    protected abstract String[] tablesToClean();

    private void scheduleAll() {
        RecurrenceScheduler scheduler = new RecurrenceScheduler(dbAdapter);
        scheduler.scheduleAll(context);
	}

}