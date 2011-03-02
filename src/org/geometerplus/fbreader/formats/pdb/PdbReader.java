package org.geometerplus.fbreader.formats.pdb;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.formats.txt.SinoDetect;
import org.geometerplus.zlibrary.core.constants.MimeTypes;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;


/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-2-08 下午01:16:16
 * 类说明 pdb textread 读取
 */
public class PdbReader extends BookReader implements ZLXMLReader {
	private static final int WINDOW_LENGTH = 4096;  
	public PdbReader(BookModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}
	public byte[] getContentBytes(int offset,ZLFile file,int length){
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
			bytes = new byte[length];
			is.read(bytes);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}
	private static int lzssDecompress(byte[] from, int compLen, byte[] to, int origLen) {  
        return lzssDecompress(from, compLen, to, 0, origLen);  
    }  
	 protected int lz77Decompress(byte[] to, byte[] from, int paramInt)
	  {
	    int i = 0;
	    int j = 0;
	    int k;
	    int l;
	    while (true)
	    {
	      if ((i >= paramInt) )
	        return j;
	      k = i + 1;
	      l = from[i] & 0xFF;
	      if ((l >= 1) && (l <= 8))
	      {
	        i = k;
	        for (int i1 = l; ; i1 = l)
	        {
	          l = i1 + -1;
	          if (i1 > 0);
	          int i2 = j + 1;
	          int i3 = i + 1;
	          byte i4 = from[i];
	          to[j] = i4;
	          i = i3;
	          j = i2;
	        }
	      }
	      if (l <= 127)
	      {
	        int i5 = j + 1;
	        int i6 = k - 1;
	        byte i7 = from[i6];
	        to[j] = i7;
	        i = k;
	        j = i5;
	      }
	      if (l < 192)
	        break;
	      int i8 = j + 1;
	      to[j] = 32;
	      j = i8 + 1;
	      int i9 = k - 1;
	      byte i10 = (byte)(from[i9] ^ 0x80);
	      to[i8] = i10;
	      i = k;
	    }
	    int i11 = l << 8;
	    i = k + 1;
	    int i12 = from[k] & 0xFF;
	    int i13 = (i11 | i12) & 0x3FFF;
	    int i14 = i13 >>> 3;
	    int i15 = (i13 & 0x7) + 3;
	    int i16 = j;
	    while (true)
	    {
	      if (i15 <= 0)
	        j = i16;
	      int i17 = i16 - i14;
	      byte i18 = to[i17];
	      to[i16] = i18;
	      i16 += 1;
	      i15 += -1;
	    }
	  } 
    private static int lzssDecompress(byte[] from, int compLen, byte[] to, int pos, int origLen) {  
        if (to == null) to = new byte[origLen];  
          
        byte[] window = new byte[WINDOW_LENGTH];  
        int readOffset = 0;  
        int writeOffset = pos;  
        int marker = 0; // read marker, 8-bits, 1 for raw byte, 0 for back ref  
        int windowWriteOffset = 0x0FEE;  
        int windowReadOffset = 0;  
        int backRefLength = 0;  
        int current = 0;  
                  
        while (readOffset != from.length) {  
            marker >>= 1;  
              
            if ((marker & 0x0100) == 0) {  
                current = from[readOffset++] & 0x0FF;  
                marker = 0x0FF00 | current;  
            }  
              
            if(readOffset == from.length) break;  
            if ((marker & 0x01) == 1) { // copy raw bytes  
                current = from[readOffset++] & 0x0FF;  
                to[writeOffset++] = (byte)current;  
                window[windowWriteOffset++] = (byte)current;  
                windowWriteOffset &= 0x0FFF;  
            } else { // copy from slide window  
                windowReadOffset = from[readOffset++] & 0x0FF;  
                if(readOffset == from.length) break;  
                current = from[readOffset++] & 0x0FF;  
                windowReadOffset |= (current & 0x0F0) << 4;  
                backRefLength = (current & 0x0F) + 2;  
                if (backRefLength < 0) continue;  
                  
                int addOffset = 0;  
//                System.out.println("-------hym----lzss"+readOffset+"|"+writeOffset);
                while (addOffset <= backRefLength) {  
                    int curOfs = (windowReadOffset + addOffset++) & 0x0FFF;  
                    current = window[curOfs] & 0x0FF;  
                    windowReadOffset &= 0x0FFF;  
                    to[writeOffset++] = (byte)current;  
                    window[windowWriteOffset++] = (byte)current;  
                    windowWriteOffset &= 0x0FFF;  
                } // while  
            } // if-else      
        } // while  
          
        return writeOffset;  
    }  
	boolean readBook(ZLFile file) {
		InputStream stream = null;
		startDocumentHandler();
		try {
			stream = file.getInputStream();
			final PdbHeader header = new PdbHeader(stream);
			String tmpstr="";
            boolean flag1 = false;
            int pageNum = header.Offsets.length;
			for(int j=1;j<pageNum;j++){ 
//				beginContentsParagraph(j);
				
				int length=0;
				if(j==pageNum-1){
					length=(int)(file.size()-header.Offsets[j]);
				}else{
					length=header.Offsets[j+1]-header.Offsets[j];
				}
				
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
//				String txttmp=new String(getContentBytes(header.Offsets[j],file.getPath(),length),header.encodingName);
				byte[] btmp = getContentBytes(header.Offsets[j],file,length);
//				byte[] to= new byte[btmp.length+1024];
//				int tolen=this.lz77Decompress(to, btmp, btmp.length);
//				LZ77T lz77t = new LZ77T();
//			    lz77t.lz77decompress(btmp, (btmp.length - 1) * 8);
//				byte[] to =lz77t.getPOutputBuffer();
//				int tolen=lz77t.getPulNumberOfBytes();
//				int tolen=lzssDecompress(btmp,0,to,btmp.length+1024);
//				int enc = new SinoDetect().detectEncoding(btmp);
//				String encodingName=SinoDetect.nicename[enc];
//				String txttmp=new String(to,0,tolen,header.encodingName);
				String txttmp=new String(btmp,header.encodingName);
//				System.out.println("-----hym --read pdb"+length+"|"+tolen);
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
//                    	System.out.println("-----hym --read pdb");
    	                for (int i = 0; i < count; i++) {
    	                    if (buffer[i] == '\n') {
    	                        if (start != i) {
    	                            characterDataHandler(buffer, start, i - start);
//    	                            System.out.println(new String(buffer, start, i - start));
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
               
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		
		
         
		return true;
	}
	@Override
	public void addExternalEntities(HashMap<String, char[]> entityMap) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void endDocumentHandler() {
		unsetCurrentTextModel();
		
	}

	@Override
	public boolean endElementHandler(String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> externalDTDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void namespaceMapChangedHandler(Map<String, String> namespaces) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processNamespaces() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startDocumentHandler() {
		setMainTextModel();
		
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return false;
	}

}
