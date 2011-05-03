/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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
// hym  改动 为了 html 中文
package org.geometerplus.zlibrary.core.html;

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.geometerplus.fbreader.bookmodel.FBTextKind;

public abstract class ZLHtmlProcessor {
	public static boolean readchm(ZLHtmlReader reader, InputStreamReader stream,char[] buffer) {
		try {
			ZLHtmlParser parser = new ZLHtmlParser(reader, stream);
			reader.startDocumentHandler();
			parser.doIt(buffer);
			reader.endDocumentHandler();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	public static boolean read(ZLHtmlReader reader, InputStreamReader stream) {
		try {
			ZLHtmlParser parser = new ZLHtmlParser(reader, stream);
			reader.startDocumentHandler();
			parser.doIt();
			reader.endDocumentHandler();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	public static boolean readchmtxt(ZLHtmlReader reader, InputStreamReader stream) {
		try {
			ZLHtmlParser parser = new ZLHtmlParser(reader, stream);
			
			parser.doIt();
			
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	public static boolean read(ZLHtmlReader reader, InputStream stream) {
		try {
			ZLHtmlParser parser = new ZLHtmlParser(reader, stream);
			reader.startDocumentHandler();
			parser.doIt();
			reader.endDocumentHandler();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
