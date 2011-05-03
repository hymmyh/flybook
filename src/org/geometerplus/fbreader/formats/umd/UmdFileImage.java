package org.geometerplus.fbreader.formats.umd;

import org.geometerplus.zlibrary.core.image.ZLSingleImage;

/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-1-27 下午02:17:50
 * 类说明
 */
public class UmdFileImage extends ZLSingleImage {
	private final UMDFile umdfile;
	private byte[] img;
	public UmdFileImage(String mimeType,UMDFile umdfile1) {
		super(mimeType);
		umdfile=umdfile1;
		img=umdfile.bookInfo.cover;
	}
	public void setImg(byte[] img1){
		img=img1;
	}
	@Override
	public byte[] byteData() {
		// 
		return img;
	}
	public String getURI() {
		// TODO: implement
		return null;
	}
}
