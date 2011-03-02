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

import java.util.HashMap;
import java.io.*;
import java.nio.charset.*;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.zlibrary.core.html.*;
import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;

public class HtmlReader extends BookReader implements ZLHtmlReader {
	private final byte[] myStyleTable = new byte[HtmlTag.TAG_NUMBER];
	{
		myStyleTable[HtmlTag.H1] = FBTextKind.H1;
		myStyleTable[HtmlTag.H2] = FBTextKind.H2;
		myStyleTable[HtmlTag.H3] = FBTextKind.H3;
		myStyleTable[HtmlTag.H4] = FBTextKind.H4;
		myStyleTable[HtmlTag.H5] = FBTextKind.H5;
		myStyleTable[HtmlTag.H6] = FBTextKind.H6;
		myStyleTable[HtmlTag.B] = FBTextKind.BOLD;
		myStyleTable[HtmlTag.SUB] = FBTextKind.SUB;
		myStyleTable[HtmlTag.SUP] = FBTextKind.SUP;
		myStyleTable[HtmlTag.S] = FBTextKind.STRIKETHROUGH;
		myStyleTable[HtmlTag.PRE] = FBTextKind.PREFORMATTED;
		myStyleTable[HtmlTag.EM] = FBTextKind.EMPHASIS;
		myStyleTable[HtmlTag.DFN] = FBTextKind.DEFINITION;
		myStyleTable[HtmlTag.CITE] = FBTextKind.CITE;
		myStyleTable[HtmlTag.CODE] = FBTextKind.CODE;
		myStyleTable[HtmlTag.STRONG] = FBTextKind.STRONG;
		myStyleTable[HtmlTag.I] = FBTextKind.ITALIC;
	}

	protected final CharsetDecoder myAttributeDecoder;
	private boolean preflag= true;//如果有pre 开始就要在 body 前关闭它
	private boolean myInsideTitle = false;
	private boolean mySectionStarted = false;
	private byte myHyperlinkType;
	private final char[] SPACE = { ' ' };
	private String myHrefAttribute = "href";
	private boolean myOrderedListIsStarted = false;
	//private boolean myUnorderedListIsStarted = false;
	private int myOLCounter = 0;
	private byte[] myControls = new byte[10];
	private byte myControlsNumber = 0;
	
	public HtmlReader(BookModel model) throws UnsupportedEncodingException {
		super(model);
		try {	
			//String encoding = model.Book.getEncoding();
			myAttributeDecoder = createDecoder();
			setByteDecoder(createDecoder());
		} catch (UnsupportedCharsetException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		}
	}

	protected final CharsetDecoder createDecoder() throws UnsupportedEncodingException {
		
		
		return Charset.forName(Model.Book.getEncoding()).newDecoder()
			.onMalformedInput(CodingErrorAction.REPLACE)
			.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	public boolean readBook() throws IOException {
		System.out.println("----hym--readbook html-");
		return ZLHtmlProcessor.read(this, getInputStreamReader());
	}

	public InputStreamReader getInputStreamReader() throws IOException {
		System.out.println("---12---"+Model.Book.getEncoding());
		return new InputStreamReader(Model.Book.File.getInputStream( ),Model.Book.getEncoding());
	}

	public void startDocumentHandler() {
	}

	public void endDocumentHandler() {
		unsetCurrentTextModel();
	}
	public boolean isEnd(String tmpstr){
//    	if(tmpstr==null||tmpstr.trim().equals("")){
//    		return true;
//    	}else{
    		char laststr = tmpstr.charAt(tmpstr.length()-1);
    		if(laststr=='。'){
    			return true;
    		}
    		if(laststr=='”'){
    			return true;
    		}
    		if(laststr=='！'){
    			return true;
    		}
    		
    		if(laststr=='？'){
    			return true;
    		}
    		
    		if(laststr=='.'){
    			return true;
    		}
    		if(laststr=='!'){
    			return true;
    		}
    		if(laststr=='?'){
    			return true;
    		}

    		if(laststr=='」'){
    			return true;
    		}
    		if(laststr==':'){
    			return true;
    		}
    		if(laststr=='：'){
    			return true;
    		}
    		if(laststr=='’'){
    			return true;
    		}
    		if(laststr=='\''){
    			return true;
    		}
    		if(laststr=='"'){
    			return true;
    		}
    		if(laststr=='）'){
    			return true;
    		}
    		if(laststr==')'){
    			return true;
    		}
    		return false;
//    	}
    }
	public void charDataHandler(char[] data, int start, int length) {
		if(Model.Book.getZnFlag()){
            //智能处理文本，速度慢。
			String str= new String(data,start,length);
            String[] strarr=str.split("\n");
            
            // 处理文本
            String ttstr="";
            for(int i=0;i<strarr.length;i++){
            	String ttmpstr=strarr[i].trim();
            	ttmpstr=ttmpstr.replaceAll("　", "");
            	ttstr+=ttmpstr;           	   	
            	if(!ttstr.equals("")){
            		if(ttmpstr.length()>28){//要判断 最后是不是标点符号，来确定是否一个段落，还是txt为了看的方便自己的做的换行。
            			if(isEnd(ttstr)){
            				ttstr=ttstr+"\r\n";
            				addData(ttstr.toCharArray(), 0,ttstr.length(),false);
	                        endParagraph();
	                        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	                        ttstr="";
            			}else{
            				if(i==strarr.length-1){//到这一段的最后一行了
            					ttstr=ttstr+"\r\n";
                				addData(ttstr.toCharArray(), 0,ttstr.length(),false);
    	                        endParagraph();
    	                        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
    	                        ttstr="";
                        	}
            			}
            		}else{//字数 少 就直接是段落
                		ttstr=ttstr+"\r\n";
                		addData(ttstr.toCharArray(), 0,ttstr.length(),false);
                        endParagraph();
                        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
                        ttstr="";
            		}
            	}
            }
		}else{
			int count=length;
			int ss=0;
			for (int i = 0; i < count; i++) {
	            if (data[i+start] == '\n') {
	                if (ss != i) {
	                	addData(data, start+ss, i - ss,false);
	                    endParagraph();
	                    beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	                }
	                ss = i + 1;
	            } else if (data[i+start] == '\r') {
	                continue;
	            } else if (data[i+start] == '　' || data[i+start] == '\t') {
	                data[i+start] = ' ';
//	                if(i+start-1>=0&&data[i+start-1] == '　'){
//	                	data[i+start-1] = ' ';
//	                }
	            } else {
	            }
	        }
			if (ss != count) {
				addData(data, start+ss, count - ss,false);
	        }
		}
//		addData(data, start, length,false);
//		System.out.println("hym--html-"+length+":"+new String(data, start, length));
	}

	private HashMap<String,char[]> myEntityMap;
	public void entityDataHandler(String entity) {
		if (myEntityMap == null) {
			myEntityMap = new HashMap<String,char[]>(ZLXMLProcessor.getEntityMap(XHTMLReader.xhtmlDTDs()));
		}
		char[] data = myEntityMap.get(entity);
		if (data == null) {
			if ((entity.length() > 0) && (entity.charAt(0) == '#')) {
				try {
					int number;
					if (entity.charAt(1) == 'x') {
						number = Integer.parseInt(entity.substring(2), 16);
					} else {
						number = Integer.parseInt(entity.substring(1));
					}
					data = new char[] { (char)number };
				} catch (NumberFormatException e) {
				}
			}
			if (data == null) {
				data = new char[0];
			}
			myEntityMap.put(entity, data);
		}
		addData(data);
		System.out.println("html:"+entity+"|"+new String(data));
	}

	private void openControl(byte control) {
		addControl(control, true);
		if (myControlsNumber == myControls.length) {
			myControls = ZLArrayUtils.createCopy(myControls, myControlsNumber, 2 * myControlsNumber);
		}
		myControls[myControlsNumber++] = control;
	}
	
	private void closeControl(byte control) {
		for (int i = 0; i < myControlsNumber; i++) {
			addControl(myControls[i], false);
		}
		boolean flag = false;
		int removedControl = myControlsNumber;
		for (int i = 0; i < myControlsNumber; i++) {
			if (!flag && (myControls[i] == control)) {
				flag = true;
				removedControl = i;
				continue;
			}
			addControl(myControls[i], true);
		}
		if (removedControl == myControlsNumber) {
			return;
		}
		--myControlsNumber;
		for (int i = removedControl; i < myControlsNumber; i++) {
			myControls[i] = myControls[i + 1];
		}
	}
	
	private void startNewParagraph() {
		endParagraph();
		beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	}
	
	public final void endElementHandler(String tagName) {
//		System.out.println("hym---html:end->"+tagName);
		if(HtmlTag.getTagByName(tagName)==HtmlTag.PRE)
			this.preflag=true;
		if(HtmlTag.getTagByName(tagName)==HtmlTag.BODY&&!preflag){
			endElementHandler(HtmlTag.PRE);
//			System.out.println("hym---html:add end->"+tagName);
		}
		endElementHandler(HtmlTag.getTagByName(tagName));
	}

	public void endElementHandler(byte tag) {
		switch (tag) {
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
			case HtmlTag.STYLE:
			case HtmlTag.P:
				startNewParagraph();
				break;

			case HtmlTag.H1:
			case HtmlTag.H2:
			case HtmlTag.H3:
			case HtmlTag.H4:
			case HtmlTag.H5:
			case HtmlTag.H6:
			case HtmlTag.PRE:
				closeControl(myStyleTable[tag]);
				startNewParagraph();
				break;

			case HtmlTag.A:
				closeControl(myHyperlinkType);
				break;

			case HtmlTag.BODY:
				break;

			case HtmlTag.HTML:
				//unsetCurrentTextModel();
				break;
				
			case HtmlTag.B:
			case HtmlTag.S:
			case HtmlTag.SUB:
			case HtmlTag.SUP:
			case HtmlTag.EM:
			case HtmlTag.DFN:
			case HtmlTag.CITE:
			case HtmlTag.CODE:
			case HtmlTag.STRONG:
			case HtmlTag.I:
				closeControl(myStyleTable[tag]);
				break;

			case HtmlTag.OL:
				myOrderedListIsStarted = false;
				myOLCounter = 0;
				break;
				
			case HtmlTag.UL:
				//myUnorderedListIsStarted = false;
				break;
				
			default:
				break;
		}
	}

	public final void startElementHandler(String tagName, int offset, ZLHtmlAttributeMap attributes) {
		System.out.println("hym---html:start->"+tagName);
		if(HtmlTag.getTagByName(tagName)==HtmlTag.PRE)
			this.preflag=false;
		startElementHandler(HtmlTag.getTagByName(tagName), offset, attributes);
	}

	public void startElementHandler(byte tag, int offset, ZLHtmlAttributeMap attributes) {
		switch (tag) {
			case HtmlTag.HTML:
				break;

			case HtmlTag.BODY:
				setMainTextModel();
				pushKind(FBTextKind.REGULAR);
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;

			case HtmlTag.P:
				if (mySectionStarted) {
					mySectionStarted = false;
				} else if (myInsideTitle) {
					addContentsData(SPACE);
				}
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;

			case HtmlTag.A:{
				String ref = attributes.getStringValue(myHrefAttribute, myAttributeDecoder);
				if ((ref != null) && (ref.length() != 0)) {
					if (ref.charAt(0) == '#') {
						myHyperlinkType = FBTextKind.FOOTNOTE;
						ref = ref.substring(1);
					} else if (ref.charAt(0) == '&') {
						myHyperlinkType = FBTextKind.INTERNAL_HYPERLINK;
						ref = ref.substring(1); 
					}
					else {
						myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
					}
					addHyperlinkControl(myHyperlinkType, ref);
					myControls[myControlsNumber] = myHyperlinkType;
					myControlsNumber++;
				}
				break;
			}
			
			case HtmlTag.IMG: {
				/*
				String ref = attributes.getStringValue(mySrcAttribute, myAttributeDecoder);
				if ((ref != null) && (ref.length() != 0)) {
					addImageReference(ref, (short)0);
					String filePath = ref;
					if (!":\\".equals(ref.substring(1, 3))) {
						filePath = Model.Book.File.getPath();
						filePath = filePath.substring(0, filePath.lastIndexOf('\\') + 1) + ref;
					}
					addImage(ref, new ZLFileImage(MimeTypes.MIME_IMAGE_AUTO, ZLFile.createFileByPath(filePath)));
				}
				*/
				break;
			}
			
			case HtmlTag.B:
			case HtmlTag.S:
			case HtmlTag.SUB:
			case HtmlTag.SUP:
			case HtmlTag.PRE:
			case HtmlTag.STRONG:
			case HtmlTag.CODE:
			case HtmlTag.EM:
			case HtmlTag.CITE:
			case HtmlTag.DFN:
			case HtmlTag.I:
				openControl(myStyleTable[tag]);
				break;
				
			case HtmlTag.H1:
			case HtmlTag.H2:
			case HtmlTag.H3:
			case HtmlTag.H4:
			case HtmlTag.H5:
			case HtmlTag.H6:
				startNewParagraph();
				openControl(myStyleTable[tag]);
				break;
				
			case HtmlTag.OL:
				myOrderedListIsStarted = true;
				break;
				
			case HtmlTag.UL:
				//myUnorderedListIsStarted = true;
				break;
				
			case HtmlTag.LI:
				startNewParagraph();
				if (myOrderedListIsStarted) {
					char[] number = (new Integer(++myOLCounter)).toString().toCharArray();
					addData(number);
					addData(new char[] {'.', ' '});
				} else {
					addData(new char[] {'*', ' '});
				}
				break;
				
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
			case HtmlTag.STYLE:
				endParagraph();
				break;
				
			case HtmlTag.TR: 
			case HtmlTag.BR:
				startNewParagraph();
				break;
			default:
				break;
		}
	}
}
