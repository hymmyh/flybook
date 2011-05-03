package org.geometerplus.fbreader.formats.umd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.zlibrary.core.constants.MimeTypes;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;


/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-1-27 下午01:16:16
 * 类说明
 */
public class UmdReader extends BookReader implements ZLXMLReader {
	private UMDFile umdFile;
	public UmdReader(BookModel model) {
		super(model);
		// 
	}
	boolean readBook(ZLFile file) {
		umdFile = new UMDFile();
		boolean flag = umdFile.read(file.getPath());
		int pageNum = umdFile.getContentSize();
		startDocumentHandler();
        
		if(UMDFile.UMD_BOOK_TYPE_TEXT == umdFile.bookInfo.type){
			//文字类型
			String tmpstr="";
            boolean flag1 = false;
			for(int j=0;j<pageNum;j++){ 
//				beginContentsParagraph(j);
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				String txttmp=umdFile.getContentText(j);				                
                    if(Model.Book.getZnFlag()){
    	                //智能处理文本，速度慢。
    	                String str= txttmp;
    	                String[] strarr=str.split("\n");
    	                if(str.charAt(str.length()-1) != '\n'){
    	                	flag1=true;
    	                }else{
    	                	flag1=false;
    	                }
    	                // 处理文本
    	                String ttstr="";
    	                for(int i=0;i<strarr.length;i++){
    	                	String ttmpstr=strarr[i].trim();
    	                	ttmpstr=ttmpstr.replaceAll("　", "");
    	                	ttstr+=ttmpstr;
    	                	if(i==0){
    	                		ttstr=tmpstr+ttstr;
    	                		tmpstr="";
    	                	}
    	                	if(i==strarr.length-1&&flag1){
    	                		tmpstr=ttstr;
    	                		break;
    	                	}
//    	                	ttstr=ttstr.trim();           	
    	                	if(!ttstr.equals("")){   	                		
    		                		ttstr="　　"+ttstr+"\r\n";
    		                		characterDataHandler(ttstr.toCharArray(), 0,ttstr.length());
    		                        endParagraph();
    		                        beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
    		                        ttstr="";
    	                		
    	                	}
    	                }
    	                
                    }else{//普通加载 速度快。
                    	char[] buffer=txttmp.toCharArray();
                        int count = buffer.length;   
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
//                int start = 0;
//                for (int i = 0; i < count; i++) {
//                    if (buffer[i] == '\n') {
//                        if (start != i) {
//                            characterDataHandler(buffer, start, i - start);
//                           // System.out.println(new String(buffer, start, i - start));
//                           //分段。
//                            endParagraph();
//                            beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
//                        }
//                        start = i + 1;
//                    } else if (buffer[i] == '\r') {
//                        continue;
//                    } else if (buffer[i] == ' ' || buffer[i] == '\t') {
//                        buffer[i] = '　';
//                    } else {
//                    }
//                }
//                if (start != count) {
//                    characterDataHandlerFinal(buffer, start, count - start);
//                }
//                endParagraph();
//                //分章节---
//                insertEndOfSectionParagraph();
////                beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
////                endContentsParagraph();
            
		}else{
			//图片类型
			for(int i=0;i<pageNum;i++){
				beginParagraph();
				final String imageName = "UMDIMG"+i;
				addHyperlinkLabel(imageName);
//				characterDataHandler(imageName.toCharArray(),0,imageName.toCharArray().length);
				addImageReference(imageName, (short)0);
				UmdFileImage umdimg= new UmdFileImage(MimeTypes.MIME_IMAGE_AUTO,umdFile);
				umdimg.setImg(umdFile.getContentBytes(i));
				addImage(imageName, umdimg);
				endParagraph();
				insertEndOfSectionParagraph();
				
				
			}
		}
		
         endDocumentHandler();	
		return true;
	}
	@Override
	public void addExternalEntities(HashMap<String, char[]> entityMap) {
		// 
		
	}

	@Override
	public void characterDataHandler(char[] ch, int start, int length) {
		if (length == 0) {
            return;
        }
        addData(ch, start, length,false);
		
	}

	@Override
	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		if (length == 0) {
            return;
        }
        addData(ch, start, length,false);
		
	}

	@Override
	public boolean dontCacheAttributeValues() {
		// 
		return false;
	}

	@Override
	public void endDocumentHandler() {
		unsetCurrentTextModel();
		
	}

	@Override
	public boolean endElementHandler(String tag) {
		// 
		return false;
	}

	@Override
	public List<String> externalDTDs() {
		// 
		return null;
	}

	@Override
	public void namespaceMapChangedHandler(Map<String, String> namespaces) {
		// 
		
	}

	@Override
	public boolean processNamespaces() {
		// 
		return false;
	}

	@Override
	public void startDocumentHandler() {
		setMainTextModel();
		
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		// 
		return false;
	}

}
