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

package org.geometerplus.fbreader.formats.html;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.txt.SinoDetect;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class HtmlPlugin extends FormatPlugin {
	
	@Override
	public boolean acceptsFile(ZLFile file) {
		return "htm".equals(file.getExtension()) 
			|| "html".equals(file.getExtension());
	}

	@Override
	public boolean readMetaInfo(Book book) {
		//活动 文档的 标题，作者，这类的东西
		System.out.println("---++hym====readMetaInfo");
		int enc = new SinoDetect().detectEncoding(new File(book.File.getPath()));
        System.out.println("encoding is ======"+enc);
	    InputStream stream = null;
	    try {
            stream = book.File.getInputStream();
            if (stream.available() <= 0) {
                return false;
            }
            book.setEncoding(SinoDetect.nicename[enc]);
//            detectEncodingAndLanguage(book, stream);
            System.out.println("----hym--html-:"+book.getEncoding());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
        book.addAuthor("Html");
        //开始获取 标题
        InputStream inputstream = null;
        try {
            inputstream = book.File.getInputStream();
        } catch (IOException e) {
        }
        if (inputstream == null) {
            return false;
        }
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(inputstream, book.getEncoding());
            char[] buffer = new char[1000];
            int count = streamReader.read(buffer);
            System.out.println(count);
            String tmp = new String(buffer,0,count);
            tmp = tmp.toUpperCase();//转成 大写
            if(tmp.indexOf("<TITLE>")!=-1)
            	tmp = tmp.substring(tmp.indexOf("<TITLE>")+7);
            if(tmp.indexOf("</TITLE>")!=-1)
            	tmp = tmp.substring(0,tmp.indexOf("</TITLE>"));
            if(tmp.trim()!=""){
            	book.setTitle(tmp+"-"+book.File.getShortName().substring(0,book.File.getShortName().indexOf(".")));
            	System.out.println("----html title---"+tmp);
            }
        }catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            try {
                inputstream.close();
            } catch (IOException e) {
            }

        }
		//return new HtmlMetaInfoReader(book).readMetaInfo();
		return true;
	}

	@Override
	public boolean readModel(BookModel model) {
		try {
			return new HtmlReader(model).readBook();
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		return null;
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return "flybook-htm";
	}
}
