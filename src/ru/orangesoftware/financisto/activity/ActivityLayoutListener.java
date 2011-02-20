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
package ru.orangesoftware.financisto.activity;

import java.util.ArrayList;

import ru.orangesoftware.financisto.model.MultiChoiceItem;
import android.view.View.OnClickListener;

public interface ActivityLayoutListener extends OnClickListener {

	void onSelectedPos(int id, int selectedPos);
	
	void onSelectedId(int id, long selectedId);

	void onSelected(int id, ArrayList<? extends MultiChoiceItem> items);
	
}
