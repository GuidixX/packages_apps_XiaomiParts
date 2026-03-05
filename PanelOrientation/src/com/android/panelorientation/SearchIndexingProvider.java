/*
 * Copyright (C) 2026 GuidixX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.panelorientation;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexablesContract;
import android.provider.SearchIndexablesProvider;

import static android.provider.SearchIndexablesContract.COLUMN_INDEX_NON_INDEXABLE_KEYS_KEY_VALUE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_CLASS_NAME;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_ENTRIES;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_ICON_RESID;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_ACTION;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_TARGET_CLASS;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_KEY;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_KEYWORDS;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_SCREEN_TITLE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_SUMMARY_OFF;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_SUMMARY_ON;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_TITLE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_USER_ID;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_CLASS_NAME;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_ICON_RESID;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_ACTION;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RANK;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RESID;

public class SearchIndexingProvider extends SearchIndexablesProvider {

    private static final String TAG = "SearchIndexingProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor queryXmlResources(String[] projection) {
        // No XML preference resources to index  return empty cursor
        return new MatrixCursor(SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS);
    }

    @Override
    public Cursor queryRawData(String[] projection) {
        final Context context = getContext();
        final MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.INDEXABLES_RAW_COLUMNS);

        final Object[] row = new Object[SearchIndexablesContract.INDEXABLES_RAW_COLUMNS.length];
        row[COLUMN_INDEX_RAW_TITLE]                = context.getString(R.string.edge_rejection_title);
        row[COLUMN_INDEX_RAW_SUMMARY_ON]           = context.getString(R.string.edge_rejection_summary);
        row[COLUMN_INDEX_RAW_SUMMARY_OFF]          = context.getString(R.string.edge_rejection_summary);
        row[COLUMN_INDEX_RAW_KEYWORDS]             = context.getString(R.string.edge_rejection_title);
        row[COLUMN_INDEX_RAW_SCREEN_TITLE]         = context.getString(R.string.edge_rejection_title);
        row[COLUMN_INDEX_RAW_ICON_RESID]           = 0;
        row[COLUMN_INDEX_RAW_INTENT_ACTION]        = "com.android.settings.action.IA_SETTINGS";
        row[COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE] = "com.android.panelorientation";
        row[COLUMN_INDEX_RAW_INTENT_TARGET_CLASS]  = "com.android.panelorientation.touch.EdgeRejectionSettingsActivity";
        row[COLUMN_INDEX_RAW_KEY]                  = "com.android.panelorientation.search_key";
        row[COLUMN_INDEX_RAW_USER_ID]              = -1;
        row[COLUMN_INDEX_RAW_CLASS_NAME]           = null;
        row[COLUMN_INDEX_RAW_ENTRIES]              = null;

        cursor.addRow(row);
        return cursor;
    }

    @Override
    public Cursor queryNonIndexableKeys(String[] projection) {
        return new MatrixCursor(SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS);
    }
}