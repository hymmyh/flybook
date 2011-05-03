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

package org.geometerplus.fbreader.formats.txt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

public final class TxtReader extends BookReader implements ZLXMLReader {
    private int myBufferSize = 65536;

    public TxtReader(BookModel model) {
        super(model);
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
    boolean readBook(ZLFile file) {
        InputStream inputstream = null;
        try {
            inputstream = file.getInputStream();
        } catch (IOException e) {
        }
        if (inputstream == null) {
            return false;
        }
        startDocumentHandler();
        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        InputStreamReader streamReader = null;
        try {
//            System.out.println("--hym--4:"+Model.Book.getEncoding());
            streamReader = new InputStreamReader(inputstream, Model.Book.getEncoding());
            char[] buffer = new char[myBufferSize];
            String tmpstr="";
            boolean flag = false;
            while (true) {
                int count = streamReader.read(buffer);
//                System.out.println(count);
                if (count <= 0) {
                    streamReader.close();
                    break;
                }
                
                if(Model.Book.getZnFlag()){
	                //智能处理文本，速度慢。
	                String str= new String(buffer,0,count);
	                String[] strarr=str.split("\n");
	                if(buffer[count-1] != '\n'){
	                	flag=true;
	                }else{
	                	flag=false;
	                }
	                // 处理文本
	                String ttstr="";
	                for(int i=0;i<strarr.length;i++){
	                	String ttmpstr=strarr[i].trim();
	                	ttmpstr=ttmpstr.replaceAll("　", "");
//	                	if(ttmpstr.startsWith("file:///")){
//	                		ttmpstr="";
//	                	}
	                	ttstr+=ttmpstr;
	                	if(i==0){
	                		ttstr=tmpstr+ttstr;
	                		tmpstr="";
	                	}
	                	if(i==strarr.length-1&&flag){
	                		tmpstr=ttstr;
	                		break;
	                	}
//	                	ttstr=ttstr.trim();           	
	                	if(!ttstr.equals("")){
	                		if(ttmpstr.length()>28){//要判断 最后是不是标点符号，来确定是否一个段落，还是txt为了看的方便自己的做的换行。
	                			if(isEnd(ttstr)){
	                				ttstr="　　"+ttstr+"\r\n";
	    	                		characterDataHandler(ttstr.toCharArray(), 0,ttstr.length());
	    	                        endParagraph();
	    	                        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	    	                        ttstr="";
	                			}else{
	                				if(i==strarr.length-1){
	                            		tmpstr=ttstr;
	                            		break;
	                            	}
	                			}
	                		}else{//字数 少 就直接是段落
		                		ttstr="　　"+ttstr+"\r\n";
		                		characterDataHandler(ttstr.toCharArray(), 0,ttstr.length());
		                        endParagraph();
		                        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
		                        ttstr="";
	                		}
	                	}
	                }
	                
                }else{//普通加载 速度快。
                	int start = 0;
	                for (int i = 0; i < count; i++) {
	                    if (buffer[i] == '\n') {
	                        if (start != i) {
	                            characterDataHandler(buffer, start, i - start);
	                           // System.out.println(new String(buffer, start, i - start));
	                            endParagraph();
	                            beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	                        }
	                        start = i + 1;
	                    } else if (buffer[i] == '\r') {
	                        continue;
	                    } else if (buffer[i] == ' ' || buffer[i] == '\t') {
	                        buffer[i] = '　';
	                        
	                        if(i-2>=0&&buffer[i-1] == '　'&&buffer[i-2] == '　'){
	                        	buffer[i-2] = ' ';
	                        }
	                    } else {
	                    }
	                }
	                if (start != count) {
	                    characterDataHandlerFinal(buffer, start, count - start);
	                }
                }
            }
            if(!tmpstr.equals("")){
            	tmpstr="　　"+tmpstr+"\r\n";
        		characterDataHandler(tmpstr.toCharArray(), 0,tmpstr.length());
            }
            endParagraph();
            endDocumentHandler();
        } catch (Exception e1) {
            // 
            e1.printStackTrace();
        } finally {
            try {
                inputstream.close();
            } catch (IOException e) {
            }

        }
        return true;
    }

    public void startDocumentHandler() {
        setMainTextModel();
    }

    public void endDocumentHandler() {
        unsetCurrentTextModel();
    }

    public boolean dontCacheAttributeValues() {
        return true;
    }

    public void characterDataHandler(char[] ch, int start, int length) {
        if (length == 0) {
            return;
        }
        addData(ch, start, length,false);
    }

    public void characterDataHandlerFinal(char[] ch, int start, int length) {
        if (length == 0) {
            return;
        }
        //addDataFinal(ch, start, length,false);
        addData(ch, start, length,false);
    }

    public boolean endElementHandler(String tagName) {
        return false;
    }

    public boolean startElementHandler(String tagName, ZLStringMap attributes) {
        return false;
    }

    private static ArrayList ourExternalDTDs = new ArrayList();

    public ArrayList externalDTDs() {
        if (ourExternalDTDs.isEmpty()) {
            ourExternalDTDs.add("data/formats/fb2/FBReaderVersion.ent");
        }
        return ourExternalDTDs;
    }

//    @Override
//    public void namespaceListChangedHandler(HashMap namespaces) {
//
//    }

    @Override
    public boolean processNamespaces() {
        return false;
    }

	@Override
	public void addExternalEntities(HashMap<String, char[]> entityMap) {
		// 
		
	}


	@Override
	public void namespaceMapChangedHandler(Map<String, String> namespaces) {
		// 
		
	}
}
