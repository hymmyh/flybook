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

package org.geometerplus.fbreader.formats.pdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.fbreader.formats.txt.JustEncoding;

public class PdbHeader {
	public final String DocName;
	public final int Flags;
	public final String Id;
	public final int[] Offsets;
	public final String encodingName;
	public String compressed;//是否压缩过
	public PdbHeader(InputStream stream) throws IOException {
		final byte[] buffer = new byte[32];
		if (stream.read(buffer, 0, 32) != 32) {
			throw new IOException("PdbHeader: cannot reader document name");
		}
		int enc = new JustEncoding().detectEncoding(buffer);
		encodingName=JustEncoding.nicename[enc];
		DocName = new String(buffer,encodingName).trim();
		Flags = PdbUtil.readShort(stream);

		PdbUtil.skip(stream, 26);
		
		if (stream.read(buffer, 0, 8) != 8) {
			throw new IOException("PdbHeader: cannot reader palm id");
		}
		Id = new String(buffer, 0, 8);

		PdbUtil.skip(stream, 8);

		int numRecords = PdbUtil.readShort(stream);
		if (numRecords <= 0) {
			throw new IOException("PdbHeader: record number = " + numRecords);
		}
//		System.out.println("-pdb hym--"+DocName+"|"+Id);
		Offsets = new int[numRecords];

		for (int i = 0; i < numRecords; ++i) {
			Offsets[i] = (int)PdbUtil.readInt(stream);
			PdbUtil.skip(stream, 4);
//			System.out.println("-pdb hym--"+PdbUtil.readInt(stream));
		}
//		System.out.println("-pdb hym--"+DocName+"|"+numRecords+"|"+Offsets[1]);
	}

	public final int length() {
		return 78 + Offsets.length * 8;
	}
}
