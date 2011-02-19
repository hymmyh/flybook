/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.preferences;

import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.library.Book;

import org.geometerplus.android.fbreader.BookInfoActivity;
import org.geometerplus.android.fbreader.SQLiteBooksDatabase;

class BookTitlePreference extends ZLStringPreference {
	private final Book myBook;

	BookTitlePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		setValue(book.getTitle());
	}

	public void onAccept() {
		myBook.setTitle(getValue());
	}
}

class LanguagePreference extends ZLStringListPreference {
	private final Book myBook;

	LanguagePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		final TreeSet<String> set = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
		set.addAll(ZLLanguageUtil.languageCodes());
		set.add(ZLLanguageUtil.OTHER_LANGUAGE_CODE);

		final int size = set.size();
		String[] codes = new String[size];
		String[] names = new String[size];
		int index = 0;
		for (String code : set) {
			codes[index] = code;
			names[index] = ZLLanguageUtil.languageName(code);
			++index;
		}
		setLists(codes, names);
		String language = myBook.getLanguage();
		if (language == null) {
			language = ZLLanguageUtil.OTHER_LANGUAGE_CODE;
		}
		if (!setInitialValue(language)) {
			setInitialValue(ZLLanguageUtil.OTHER_LANGUAGE_CODE);
		}
	}

	public void onAccept() {
		final String value = getValue();
		myBook.setLanguage((value.length() != 0) ? value : null);
	}
}
// 智能 加载 txt
class ZnPreference extends ZLStringListPreference {
	private final Book myBook;

	ZnPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
//		final TreeSet<String> set = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
//		
//		set.add(ZLLanguageUtil.OTHER_LANGUAGE_CODE);

//		final int size = set.size();
		String[] codes = {"0","1"};
		String[] names = {"快速加载","智能加载"};
//		int index = 0;
//		for (String code : set) {
//			codes[index] = code;
//			names[index] = ZLLanguageUtil.languageName(code);
//			++index;
//		}
		setLists(codes, names);
		String zn = "0";
		if(myBook.getZnFlag()){
			zn = "1";
		}		
		if (!setInitialValue(zn)) {
			setInitialValue("0");
		}
	}

	public void onAccept() {
		final String value = getValue();
		myBook.setZnFlag((value.length() != 0) ? value : null);
	}
}

public class EditBookInfoActivity extends ZLPreferenceActivity {
	private Book myBook;

	public EditBookInfoActivity() {
		super("BookInfo");
	}

	@Override
	protected void init(Intent intent) {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "LIBRARY");
		}

		final String path = intent.getStringExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY);
		final ZLFile file = ZLFile.createFileByPath(path);
		myBook = Book.getByFile(file);

		addPreference(new BookTitlePreference(this, Resource, "title", myBook));
		addPreference(new LanguagePreference(this, Resource, "language", myBook));
		addPreference(new ZnPreference(this, Resource, "txtzn", myBook));
	}

	@Override
	protected void onPause() {
		super.onPause();
		myBook.save();
	}
}
