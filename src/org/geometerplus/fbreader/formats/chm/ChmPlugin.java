/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.chm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.geometerplus.fbreader.bookmodel.BookModel;
//import org.geometerplus.fbreader.collection.BookDescription;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class ChmPlugin extends FormatPlugin {
	public boolean acceptsFile(ZLFile file) {
		return "chm".equalsIgnoreCase(file.getExtension());
	}
	
//	public boolean readDescription(ZLFile file, BookDescription description) {
//	    int enc = new SinoDetect().detectEncoding(new File(file.getPath()));
//        System.out.println("encoding is ======"+enc);
//	    InputStream stream = null;
//	    try {
//            stream = file.getInputStream();
//            if (stream.available() <= 0) {
//                return false;
//            }
//            description.setEncoding(SinoDetect.nicename[enc]);
//            detectEncodingAndLanguage(description, stream);
//            System.out.println(description.getEncoding());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                stream.close();
//            } catch (IOException e) {
//                // ignore
//            }
//        }
//	    if (description.getEncoding() == null || description.getEncoding().equals("")) {
//	        return false;
//	    }
//
//	    return true;
//	}
	
//	public boolean readModel(BookDescription description, BookModel model) {
//		return new TxtReader(model).readBook(description.File);
//	}

	@Override
	public String readAnnotation(ZLFile file) {
		// 
		return "flybook-chm";
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		// 
		return null;
	}

	@Override
	public boolean readMetaInfo(Book book) {
		if(book.File.size()>3500000){
			return false;
		}else{
			System.out.println("----chm-size-"+book.File.size());
		}
		// 用了确认一下 编码
		CHMFile chm=null;
		try {
			chm = new CHMFile(book.File.getPath());
		} catch (DataFormatException e) {
			e.printStackTrace();
			if(chm!=null){
				try {
					chm.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			if(chm!=null){
				try {
					chm.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return false;
		}
		String encoding="utf-8";
		if(chm.lang==0x0404){
			encoding="big5";
		}else if(chm.lang==0x0804){
			encoding="gbk";
		}
		 book.setEncoding(encoding);
		if(chm!=null){
			try {
				chm.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    if (book.getEncoding() == null || book.getEncoding().equals("")) {
	        return false;
	    }
	    book.addAuthor("Chm");
	    return true;
		
	}

	@Override
	public boolean readModel(BookModel model) {
		// 
		try {
			return new ChmReader(model).readBook();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
