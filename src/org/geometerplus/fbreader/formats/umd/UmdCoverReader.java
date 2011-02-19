package org.geometerplus.fbreader.formats.umd;


import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage.SourceType;

/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-1-27 下午02:12:43
 * 类说明
 */
public class UmdCoverReader {
	private static class UMDCoverImage extends ZLImageProxy {
		private final ZLFile myFile;
		private final UMDFile umdfile;
		String MIME_IMAGE_AUTO = "image/auto";
		UMDCoverImage(ZLFile file,UMDFile umdfile1) {
			myFile = file;
			umdfile=umdfile1;
		}

		@Override
		public ZLSingleImage getRealImage() {
			return new UmdFileImage(MIME_IMAGE_AUTO,umdfile);
		}

		@Override
		public int sourceType() {
			return SourceType.DISK;
		}

		@Override
		public String getId() {
			return myFile.getPath();
		}
	}
	public ZLImageProxy readCover(ZLFile file,UMDFile umdfile1) {
		return new UMDCoverImage(file,umdfile1);
	}

}
