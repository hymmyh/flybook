package org.geometerplus.fbreader.formats.umd;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.txt.TxtReader;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;


/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-1-27 下午01:15:55
 * 类说明
 */
public class UmdPlugin extends FormatPlugin {
	private UMDFile umdFile;
	@Override
	public boolean acceptsFile(ZLFile file) {
		return "umd".equalsIgnoreCase(file.getExtension());
		
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return "flybook-umd";
	}

	@Override
	public ZLImage readCover(ZLFile file) {
//		System.out.println("---hym file size--:"+file.size());
		umdFile = new UMDFile();
		umdFile.readcover(file.getPath(),file.size());
		return  new UmdCoverReader().readCover(file,umdFile) ;
	}

	@Override
	public boolean readMetaInfo(Book book) {
		// 作者 书名 等
		umdFile = new UMDFile();
		boolean flag = umdFile.readmeta(book.File.getPath());
		int pageNum = umdFile.getContentSize();
//		System.out.println("pageNum " + pageNum);
		book.addAuthor(umdFile.bookInfo.author);
		book.setTitle(umdFile.bookInfo.title);
		return flag;
	}

	@Override
	public boolean readModel(BookModel model) {
		// 
		
		return new UmdReader(model).readBook(model.Book.File);
	}

}
