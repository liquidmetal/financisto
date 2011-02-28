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
package ru.orangesoftware.financisto.report;

import android.content.Context;
import ru.orangesoftware.financisto.blotter.BlotterFilter;
import ru.orangesoftware.financisto.blotter.WhereFilter;
import ru.orangesoftware.financisto.blotter.WhereFilter.Criteria;
import ru.orangesoftware.financisto.db.DatabaseAdapter;

import static ru.orangesoftware.financisto.db.DatabaseHelper.V_REPORT_PAYEES;

public class PayeesReport extends AbstractReport {

	public PayeesReport(Context context) {
		super(context);		
	}

	@Override
	public ReportData getReport(DatabaseAdapter db, WhereFilter filter) {
		return queryReport(db, V_REPORT_PAYEES, filter);
	}

	@Override
	public Criteria getCriteriaForId(DatabaseAdapter db, long id) {
		return Criteria.eq(BlotterFilter.PAYEE_ID, String.valueOf(id));
	}		
	
}
