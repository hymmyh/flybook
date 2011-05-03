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

package org.geometerplus.fbreader.formats.pdb;

import java.util.*;
import java.io.*;
import java.nio.charset.CharsetDecoder;

import org.geometerplus.zlibrary.core.constants.MimeTypes;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.html.ZLCharBuffer;
import org.geometerplus.zlibrary.core.html.ZLHtmlAttributeMap;

import org.geometerplus.fbreader.formats.html.HtmlReader;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.fbreader.bookmodel.BookModel;

public class MobipocketHtmlBookReader extends HtmlReader {
	private final CharsetDecoder myTocDecoder;
	private MobipocketStream myMobipocketStream;
	private static boolean compressionflag = false;//要从第一条记录中的数据来判断是否用了压缩。
	private static long txtlen=0;
	
	MobipocketHtmlBookReader(BookModel model) throws UnsupportedEncodingException {
		super(model);
		myTocDecoder = createDecoder();
	}

	public InputStream getInputStream() throws IOException {
		myMobipocketStream = new MobipocketStream(Model.Book.File);
		return myMobipocketStream;
	}
	public InputStreamReader getInputStreamReader() throws IOException {
//		System.out.println("---12---"+Model.Book.getEncoding());
		return new InputStreamReader(getInputStream(),Model.Book.getEncoding());
	}
	private boolean myReadGuide;
	private int myTocStartOffset = Integer.MAX_VALUE;
	private int myTocEndOffset = Integer.MAX_VALUE;
	private final TreeMap<Integer,String> myTocEntries = new TreeMap<Integer,String>();
	private final TreeMap<Integer,Integer> myPositionToParagraph = new TreeMap<Integer,Integer>();
	private final TreeSet<Integer> myFileposReferences = new TreeSet<Integer>();
	private int myCurrentTocPosition = -1;
	private final ZLCharBuffer myTocBuffer = new ZLCharBuffer();

	private boolean tocRangeContainsPosition(int position) {
		return (myTocStartOffset <= position) && (position < myTocEndOffset);
	}
	public void getcompressionflag(int offset,ZLFile file){
		byte[] bytes = null;
		InputStream is = null;
//		FileInputStream fis = file.getInputStream();
//	    ZipInputStream zin = new ZipInputStream(is);
		try{
			is = file.getInputStream();
//			ZipInputStream zin = new ZipInputStream(is);
//			if(zin.getNextEntry()==null){
//				System.out.println("--zip dec err--");
//			}
			long skipLength= offset;
			is.skip(skipLength);
//			int length = (int) contentArr.get(index).getLength();
			bytes = new byte[2];
			is.read(bytes);
//			System.out.println("--bytes[1]---"+bytes[1]);
			if(bytes[1]==2){
				compressionflag=true;
			}else{
				compressionflag=false;
			}
			is.skip(2);//跳过2个
			byte[] tmp = new byte[4];//长度 没有压缩的文本
			is.read(tmp);
			txtlen=(((long)(tmp[0] & 0xFF)) << 24) +
			  + ((tmp[1] & 0xFF) << 16) +
			  + ((tmp[2] & 0xFF) << 8) +
			  + (tmp[3] & 0xFF);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
			}
		}
		
	}
//	@Override
//	public boolean readBook() throws IOException{
//		InputStream stream = null;
//		startDocumentHandler();
////		boolean compressionflag = false;//要从第一条记录中的数据来判断是否用了压缩。
//		
//		try {
//			stream = Model.Book.File.getInputStream();
//			final PdbHeader header = new PdbHeader(stream);
//			String tmpstr="";
//            boolean flag1 = false;
//            int pageNum = header.Offsets.length;
//            getcompressionflag(header.Offsets[0],Model.Book.File);
//            System.out.println("-----hym-mobi--"+compressionflag+"|"+txtlen);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return super.readBook();
//	}
	@Override
	public void startElementHandler(byte tag, int offset, ZLHtmlAttributeMap attributes) {
		final int paragraphIndex = Model.BookTextModel.getParagraphsNumber();
		myPositionToParagraph.put(offset, paragraphIsOpen() ? paragraphIndex - 1 : paragraphIndex);
		switch (tag) {
			case HtmlTag.IMG:
			{
				final ZLCharBuffer recIndex = attributes.getValue("recindex");
				if (recIndex != null) {
					try {
						final int index = Integer.parseInt(recIndex.toString());
						if (paragraphIsOpen()) {
							endParagraph();
							addImageReference("" + index);
							beginParagraph();
						} else {
							addImageReference("" + index);
						}
					} catch (NumberFormatException e) {
					}
				}
				break;
			}
			case HtmlTag.GUIDE:
				myReadGuide = true;
				break;
			case HtmlTag.REFERENCE:
				if (myReadGuide) {
					final ZLCharBuffer fp = attributes.getValue("filepos");
					final ZLCharBuffer title = attributes.getValue("title");
					if ((fp != null) && (title != null)) {
						try {
							int filePosition = Integer.parseInt(fp.toString());
							myTocEntries.put(filePosition, title.toString(myAttributeDecoder));
							if (tocRangeContainsPosition(filePosition)) {
								myTocEndOffset = filePosition;
							}
							if (attributes.getValue("type").equalsToLCString("toc")) {
								myTocStartOffset = filePosition;
								final SortedMap<Integer,String> subMap =
									myTocEntries.tailMap(filePosition + 1);
								if (!subMap.isEmpty()) {
									myTocEndOffset = subMap.firstKey();
								}
							}
						} catch (NumberFormatException e) {
						}
					}
				}
				break;
			case HtmlTag.A:
			{
				final ZLCharBuffer fp = attributes.getValue("filepos");
				if (fp != null) {
					try {
						int filePosition = Integer.parseInt(fp.toString());
						if (tocRangeContainsPosition(offset)) {
							myCurrentTocPosition = filePosition;
							if (tocRangeContainsPosition(filePosition)) {
								myTocEndOffset = filePosition;
							}
						}
						myFileposReferences.add(filePosition);
						attributes.put(new ZLCharBuffer("href"), new ZLCharBuffer("&filepos" + filePosition));
					} catch (NumberFormatException e) {
					}
				}
				super.startElementHandler(tag, offset, attributes);
				break;
			}
			default:
				super.startElementHandler(tag, offset, attributes);
				break;
		}
	}

	@Override
	public void endElementHandler(byte tag) {
		switch (tag) {
			case HtmlTag.IMG:
				break;
			case HtmlTag.GUIDE:
				myReadGuide = false;
				break;
			case HtmlTag.REFERENCE:
				break;
			case HtmlTag.A:
				if (myCurrentTocPosition != -1) {
					if (!myTocBuffer.isEmpty()) {
						myTocEntries.put(myCurrentTocPosition, myTocBuffer.toString(myTocDecoder));
						myTocBuffer.clear();
					}
					myCurrentTocPosition = -1;
				}
				super.endElementHandler(tag);
				break;
			default:
				super.endElementHandler(tag);
				break;
		}
	}
	//hym 改动
	@Override
	public void charDataHandler(char[] data, int start, int length) {
		if (myCurrentTocPosition != -1) {
			myTocBuffer.append(data, start, length);
		}
		super.addData(data, start, length,false);
	}

	@Override
	public void startDocumentHandler() {
		super.startDocumentHandler();
		if(myMobipocketStream==null){
			try {
				getInputStream();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}
		}
		for (int index = 0; ; ++index) {
			final int offset = myMobipocketStream.getImageOffset(index);
			if (offset < 0) {
				break;
			}
			final int length = myMobipocketStream.getImageLength(index);
			if (length <= 0) {
				break;
			}
			try
			{
			addImage("" + (index+1), new ZLFileImage(MimeTypes.MIME_IMAGE_AUTO, Model.Book.File, offset, length));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void endDocumentHandler() {
		for (Integer entry: myFileposReferences) {
			final SortedMap<Integer,Integer> subMap =
				myPositionToParagraph.tailMap(entry);
			if (subMap.isEmpty()) {
				break;
			}
			addHyperlinkLabel("filepos" + entry, subMap.get(subMap.firstKey()));
		}

		for (Map.Entry<Integer,String> entry : myTocEntries.entrySet()) {
			final SortedMap<Integer,Integer> subMap =
				myPositionToParagraph.tailMap(entry.getKey());
			if (subMap.isEmpty()) {
				break;
			}
			beginContentsParagraph(subMap.get(subMap.firstKey()));
			addContentsData(entry.getValue().toCharArray());
			endContentsParagraph();
		}
		super.endDocumentHandler();
	}
}
