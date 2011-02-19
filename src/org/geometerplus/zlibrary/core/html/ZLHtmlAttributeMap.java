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

import java.nio.charset.CharsetDecoder;

// optimized partially implemented map ZLByteBuffer -> ZLByteBuffer
// there is no remove() in this implementation
// put with the same key does not remove old entry

public final class ZLHtmlAttributeMap {
	private ZLCharBuffer[] myKeys;
	private ZLCharBuffer[] myValues;
	private int mySize;

	public ZLHtmlAttributeMap() {
		myKeys = new ZLCharBuffer[8];
		myValues = new ZLCharBuffer[8];
	}

	public void put(ZLCharBuffer key, ZLCharBuffer value) {
		final int size = mySize++;
		ZLCharBuffer[] keys = myKeys;
		if (keys.length == size) {
			keys = new ZLCharBuffer[size << 1];
			System.arraycopy(myKeys, 0, keys, 0, size);
			myKeys = keys;

			final ZLCharBuffer[] values = new ZLCharBuffer[size << 1];
			System.arraycopy(myValues, 0, values, 0, size);
			myValues = values;
		}
		keys[size] = key;
		myValues[size] = value;
	}

	public ZLCharBuffer getValue(String lcPattern) {
		int index = mySize;
		if (index > 0) {
			final ZLCharBuffer[] keys = myKeys;
			while (--index >= 0) {
				if (keys[index].equalsToLCString(lcPattern)) {
					return myValues[index];
				}
			}
		}
		return null;
	}

	public String getStringValue(String lcPattern, CharsetDecoder decoder) {
		final ZLCharBuffer buffer = getValue(lcPattern);
		//return (buffer != null) ? buffer.toString(decoder) : null;
		// hym ---
		return (buffer != null) ? buffer.toString() : null;
	}

	public int getSize() {
		return mySize;
	}

	public ZLCharBuffer getKey(int index) {
		return myKeys[index];
	}

	public void clear() {
		mySize = 0;
	}
}
