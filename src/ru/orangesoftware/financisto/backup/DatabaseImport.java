/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto.backup;

import android.content.ContentValues;
import android.content.Context;
import api.wireless.gdata.docs.client.DocsClient;
import api.wireless.gdata.docs.data.DocumentEntry;
import api.wireless.gdata.parser.ParseException;
import api.wireless.gdata.util.ContentType;
import api.wireless.gdata.util.ServiceException;
import ru.orangesoftware.financisto.db.Database;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.DatabaseSchemaEvolution;
import ru.orangesoftware.financisto.export.Export;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static ru.orangesoftware.financisto.backup.Backup.RESTORE_SCRIPTS;

public class DatabaseImport extends FullDatabaseImport {

	private final DatabaseSchemaEvolution schemaEvolution;
    private final InputStream backupStream;

    public static DatabaseImport createFromFileBackup(Context context, DatabaseAdapter dbAdapter, String backupFile) throws FileNotFoundException {
        File file = new File(Export.EXPORT_PATH, backupFile);
        FileInputStream inputStream = new FileInputStream(file);
        return new DatabaseImport(context, dbAdapter, inputStream);
    }

    public static DatabaseImport createFromGDocsBackup(Context context, DatabaseAdapter dbAdapter, DocsClient docsClient, DocumentEntry entry)
            throws IOException, ParseException, ServiceException {
        InputStream inputStream = docsClient.getFileContent(entry, ContentType.ZIP);
        InputStream in = new GZIPInputStream(inputStream);
        return new DatabaseImport(context, dbAdapter, in);
    }

	private DatabaseImport(Context context, DatabaseAdapter dbAdapter, InputStream backupStream) {
        super(context, dbAdapter);
        this.schemaEvolution = new DatabaseSchemaEvolution(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
        this.backupStream = backupStream;
	}

    @Override
    protected String[] tablesToClean() {
        return Backup.BACKUP_TABLES;
    }

    @Override
    protected void restoreDatabase() throws IOException {
        InputStreamReader isr = new InputStreamReader(backupStream, "UTF-8");
        BufferedReader br = new BufferedReader(isr, 65535);
        try {
            recoverDatabase(br);
            runRestoreAlterscripts();
        } finally {
            br.close();
        }
    }

	private void recoverDatabase(BufferedReader br) throws IOException {
        boolean insideEntity = false;
        ContentValues values = new ContentValues();
        String line;
        String tableName = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("$")) {
                if ("$$".equals(line)) {
                    if (tableName != null && values.size() > 0) {
                        db.insert(tableName, null, values);
                        tableName = null;
                        insideEntity = false;
                    }
                } else {
                    int i = line.indexOf(":");
                    if (i > 0) {
                        tableName = line.substring(i+1);
                        insideEntity = true;
                        values.clear();
                    }
                }
            } else {
                if (insideEntity) {
                    int i = line.indexOf(":");
                    if (i > 0) {
                        String columnName = line.substring(0, i);
                        String value = line.substring(i+1);
                        values.put(columnName, value);
                    }
                }
            }
        }
	}

	private void runRestoreAlterscripts() throws IOException {
		for (String script : RESTORE_SCRIPTS) {
			schemaEvolution.runAlterScript(db, script);
		}
	}

}
