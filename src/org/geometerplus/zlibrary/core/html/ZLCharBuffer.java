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

package org.geometerplus.zlibrary.core.html;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
//hym 把 byte 改成 char 为了 html 中的中文
public final class ZLCharBuffer {
	char[] myData;
	int myLength;

	ZLCharBuffer(int len) {
		myData = new char[len];
	}

	public ZLCharBuffer() {
		this(20);
	}

	ZLCharBuffer(ZLCharBuffer container) {
		final int len = container.myLength;
		myData = ZLArrayUtils.createCopy(container.myData, len, len);
		myLength = len;
	}

	public boolean isEmpty() {
		return myLength == 0;
	}

	public void append(char[] buffer, int offset, int count) {
		final int len = myLength;
		char[] data = myData;
		final int newLength = len + count;
		if (data.length < newLength) {
			data = ZLArrayUtils.createCopy(data, len, newLength);
			myData = data;
		}
		System.arraycopy(buffer, offset, data, len, count);
		myLength = newLength;
		myStringValue = null;
	}

	public void clear() {
		myLength = 0;
		myStringValue = null;
	}

	public boolean equals(Object o) {
		final ZLCharBuffer container = (ZLCharBuffer)o;
		final int len = myLength;
		if (len != container.myLength) {
			return false;
		}
		final char[] data0 = myData;
		final char[] data1 = container.myData;
		for (int i = len; --i >= 0; ) {
			if (data0[i] != data1[i]) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		final int len = myLength;
		final char[] data = myData;
		int code = len * 31;
		if (len > 1) {
			code += data[0];
			code *= 31;
			code += data[1];
			if (len > 2) {
				code *= 31;
				code += data[2];
			}
		} else if (len > 0) {
			code += data[0];
		}
		return code;
	}

	public boolean equalsToLCString(String lcPattern) {
		return (myLength == lcPattern.length()) &&
				lcPattern.equals(new String(myData, 0, myLength).toLowerCase());
	} 

	private static final Object myConverterLock = new Object();
	private static char[] myConverterBuffer = new char[20];
	private String myStringValue;
	public String toString(CharsetDecoder decoder) {
//		if (myStringValue == null) {
//			synchronized (myConverterLock) {
//				if (myConverterBuffer.length < myLength) {
//					myConverterBuffer = new char[myLength];
//				}
//				ByteBuffer byteBuffer = ByteBuffer.wrap(myData, 0, myLength);
//				CharBuffer charBuffer = CharBuffer.wrap(myConverterBuffer);
//				decoder.decode(byteBuffer, charBuffer, true);
//				myStringValue = new String(myConverterBuffer, 0, charBuffer.position());
//			}
//		}
//		return myStringValue;
		//hym 
		return toString();
	}
	public String toString() {
		if (myStringValue == null) {
			myStringValue = new String(myData, 0, myLength);
		}
		return myStringValue;
	}
}
