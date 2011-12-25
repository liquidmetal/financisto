package ru.orangesoftware.financisto.export.qif;

import android.app.ProgressDialog;
import android.content.Context;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.export.ImportExportAsyncTask;

public class QifExportTask extends ImportExportAsyncTask {

	private final QifExportOptions options;

	public QifExportTask(Context context, ProgressDialog dialog, QifExportOptions options) {
		super(context, dialog);
        this.options = options;
	}
	
	@Override
	protected Object work(Context context, DatabaseAdapter db, String...params) throws Exception {
        QifExport qifExport = new QifExport(db, options);
        return qifExport.export(context);
	}

	@Override
	protected String getSuccessMessage(Object result) {
		return String.valueOf(result);
	}

}
