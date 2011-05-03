package org.geometerplus.fbreader.formats.pdb;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.formats.txt.JustEncoding;
import org.geometerplus.zlibrary.core.constants.MimeTypes;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;


/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-2-08 下午01:16:16
 * 类说明 pdb text read 读取,注意压缩方式
 */
public class PdbReader extends BookReader implements ZLXMLReader {
	private static final int WINDOW_LENGTH = 4096;  
	private static boolean compressionflag = false;//要从第一条记录中的数据来判断是否用了压缩。
	private static long txtlen=0;
	private static byte[] ttb=new byte[0];
	public PdbReader(BookModel model) {
		super(model);
		// 
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
					
					e.printStackTrace();
				}
			}
		}
		return bytes;
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
	public String bytesToString(byte[] startb,byte[] endb,int len,String encodingName,boolean flag){
		int allnum=0;
		if(startb!=null){
			allnum=startb.length;
		}
		int num=len-1;
		for(int k=len-1;k>=0&&flag;k--){
			if(endb[k]=='\n'){
				num=k;
				break;
			}
		}
		allnum+=num+1;
		byte[] tmpb = new byte[allnum];
		if(startb!=null){
			System.arraycopy(startb, 0, tmpb, 0, startb.length);
			System.arraycopy(endb, 0, tmpb,  startb.length,num+1);
		}else{
			System.arraycopy(endb, 0, tmpb, 0, num+1);
		}
		ttb=new byte[len-num-1];
		System.arraycopy(endb, num+1, ttb, 0,len-num-1);
//		System.out.println("-----hym --startb"+ttb.length+"|");
		String str="";
		try {
			str=new String(tmpb,encodingName);
		} catch (UnsupportedEncodingException e) {
			// 
			e.printStackTrace();
		}
		return str;
	}
	/**
	 * 解码 PalmDoc
	 * PalmDoc files are decoded as follows:

    Read a byte from the compressed stream. If the byte is
        0x00: "1 literal" copy that byte unmodified to the decompressed stream.
        0x09 to 0x7f: "1 literal" copy that byte unmodified to the decompressed stream.
        0x01 to 0x08: "literals": the byte is interpreted as a count from 1 to 8, and that many literals are copied unmodified from the compressed stream to the decompressed stream.
        0x80 to 0xbf: "length, distance" pair: the 2 leftmost bits of this byte ('10') are discarded, and the following 6 bits are combined with the 8 bits of the next byte to make a 14 bit "distance, length" item. Those 14 bits are broken into 11 bits of distance backwards from the current location in the uncompressed text, and 3 bits of length to copy from that point (copying n+3 bytes, 3 to 10 bytes).
        0xc0 to 0xff: "byte pair": this byte is decoded into 2 characters: a space character, and a letter formed from this byte XORed with 0x80.
    Repeat from the beginning until there is no more bytes in the compressed file.

	 * @param to
	 * @param from
	 * @param paramInt
	 * @return
	 */

    
    /**
     *  从 维基百科 外文版找到资料。
bytes	content	comments
2	Compression	1 == no compression, 2 = PalmDOC compression (see below)
2	Unused	Always zero
4	text length	Uncompressed length of the entire text of the book
2	record count	Number of PDB records used for the text of the book.
2	record size	Maximum size of each record containing text, always 4096
4	Current Position	Current reading position, as an offset into the uncompressed text

 PalmDOC uses LZ77 compression techniques. DOC files can contain only compressed text. 
 The format does not allow for any text formatting. This keeps files small, in keeping 
 with the Palm philosophy. However, extensions to the format can use tags, such as 
 HTML or PML, to include formatting within text. These extensions to PalmDoc are not 
 interchangeable and are the basis for most eBook Reader formats on Palm devices. 
 
     * @param file
     * @return
     */
	boolean readBook(ZLFile file) {
		InputStream stream = null;
		startDocumentHandler();
//		boolean compressionflag = false;//要从第一条记录中的数据来判断是否用了压缩。
		
		try {
			stream = file.getInputStream();
			final PdbHeader header = new PdbHeader(stream);
			String tmpstr="";
            boolean flag1 = false;
            int pageNum = header.Offsets.length;
            getcompressionflag(header.Offsets[0],file);
//            System.out.println("-----hym---"+compressionflag+"|"+txtlen);
           
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
				String txttmp="";
				if(compressionflag){//压缩
					byte[] to= new byte[btmp.length+1024];
					int tolen=DocDecompressor.decompress(btmp, to);
//					System.out.println("-----hym --read pdb"+length+"|"+tolen);
					//不能直接 string 可能会截断在 中文的中间。
//					txttmp=new String(to,0,tolen,header.encodingName);
					if(j==pageNum-1){
						txttmp=bytesToString(ttb,to,tolen,header.encodingName,false);
					}else
						txttmp=bytesToString(ttb,to,tolen,header.encodingName,true);
//					System.out.println("-----hym --ttb"+ttb.length+"|");
				}else{
					//不能直接 string 可能会截断在 中文的中间。
//					txttmp=new String(btmp,header.encodingName);
					if(j==pageNum-1){
						txttmp=bytesToString(ttb,btmp,btmp.length,header.encodingName,false);
					}else
						txttmp=bytesToString(ttb,btmp,btmp.length,header.encodingName,true);
				}
//				String txttmp=new String(btmp,header.encodingName);
				
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
