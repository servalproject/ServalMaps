/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Maps Software
 *
 * Serval Maps Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.maps.tags;

import org.servalproject.maps.provider.TagsContract;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * extend the AutoComplteTextView class to use custom code to
 * autocomplete based on a tag list
 */
public class TagsAutoComplete extends AutoCompleteTextView {
	
	/*
	 * private class level constants
	 */
	private final String sTag = "TagsAutoComplete";
	private final boolean V_LOG = true;
	
	/*
	 * private class level variables
	 */
	private String existingText = "";
	private String seperatorChar = TagsContract.TAG_DELIMITER;
	

	public TagsAutoComplete(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupAdapter();
	}
	
	public TagsAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupAdapter();
    }

	public TagsAutoComplete(Context context) {
		super(context);
		setupAdapter();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AutoCompleteTextView#performFiltering(java.lang.CharSequence, int)
	 */
	@Override
	protected void performFiltering(final CharSequence text, final int keyCode) {
		
		// find the term that we'll try to match on
		String mText = text.toString().trim();
		existingText = mText.substring(0, mText.lastIndexOf(seperatorChar) + 1);
		mText = mText.substring(mText.lastIndexOf(seperatorChar) + 1);
		
		if(V_LOG) {
			Log.v(sTag, "existing text: '" + existingText + "'");
			Log.v(sTag, "will attempt match on: '" + mText + "'");
		}
		
		// if nothing found, call the super method
		if (!TextUtils.isEmpty(mText)) {
            super.performFiltering(mText, keyCode);
        }
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AutoCompleteTextView#replaceText(java.lang.CharSequence)
	 */
	@Override
    protected void replaceText(CharSequence text) {
		// add the selected tag to the list
		if(V_LOG) {
			Log.v(sTag, "existing text: '" + existingText + "'");
			Log.v(sTag, "will attempt to add: '" + text + seperatorChar + "'");
			Log.v(sTag, "replacement text: '" + existingText + text + seperatorChar + "'");
		}
		
		// filter the text before adding it
		String mNewText = text.toString();
		mNewText = mNewText.substring(0, mNewText.indexOf(" ("));
		
		super.replaceText(existingText + mNewText + seperatorChar);
    }
	
	/*
	 *  data adapter methods and classes
	 */
	private void setupAdapter() {
		setAdapter(new TagsAdapter(getContext(), null));
	}
	
	public static class TagsAdapter extends CursorAdapter implements Filterable {
		
		/*
		 * private class level variables
		 */
		private ContentResolver contentResolver;

		public TagsAdapter(Context context, Cursor c) {
			super(context, c);
			contentResolver = context.getContentResolver();
		}

		/*
		 * (non-Javadoc)
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(convertToString(cursor));
        }

		/*
		 * (non-Javadoc)
		 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            view.setText(convertToString(getCursor()));
            return view;
        }
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.CursorAdapter#convertToString(android.database.Cursor)
		 */
		@Override
        public String convertToString(Cursor cursor) {
            return cursor.getString(1) + " (" + cursor.getString(2) +")";
        }
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.CursorAdapter#runQueryOnBackgroundThread(java.lang.CharSequence)
		 */
		@Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			
			// use an existing filter query provider if one exists
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }
            
            String mSelection = TagsContract.Table.TAG + " GLOB ?";
            String[] mSelectionArgs = new String[1];
            mSelectionArgs[0] = constraint + "*";

            // no need to specify a projection as the Uri maps to a single data set
            return contentResolver.query(
            		TagsContract.UNIQUE_CONTENT_URI,
            		null,
            		mSelection,
            		mSelectionArgs,
            		null);
        }
	}
}
