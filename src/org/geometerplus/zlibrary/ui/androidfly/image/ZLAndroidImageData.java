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

package org.geometerplus.zlibrary.ui.androidfly.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

public abstract class ZLAndroidImageData implements ZLImageData {
	private Bitmap myBitmap;
	private int myRealWidth;
	private int myRealHeight;
	private int myLastRequestedWidth = -1;
	private int myLastRequestedHeight = -1;

	protected ZLAndroidImageData() {
	}

	protected abstract Bitmap decodeWithOptions(BitmapFactory.Options options);
	public Bitmap getFullSizeBitmap() {
		return getBitmap(0, 0, true);
	}

	public Bitmap getBitmap(int maxWidth, int maxHeight) {
		return getBitmap(maxWidth, maxHeight, false);
	}

	private synchronized Bitmap getBitmap(int maxWidth, int maxHeight, boolean ignoreSize) {
		if (!ignoreSize && (maxWidth == 0 || maxHeight == 0)) {
			return null;
		}
		if (maxWidth != myLastRequestedWidth || maxHeight != myLastRequestedHeight) {
			if (myBitmap != null) {
				myBitmap.recycle();
				myBitmap = null;
			}
			//hym 注释 2011-02-26 由于采用缩放方式，开始都要直接导入大的图片，导致内存需求加大，有些机器会出现问题。
			// +maxWidth+"|"+maxHeight 都为 88的时候 是网络在线书库显示封面。这个时候用以前原版的处理,188 的高 为本地书显示封面
			if(maxWidth<=88&&maxHeight<=188){//主要用来显示目录。
				try {
					final BitmapFactory.Options options = new BitmapFactory.Options();
					if (myRealWidth <= 0) {
						options.inJustDecodeBounds = true;
						decodeWithOptions(options);
						myRealWidth = options.outWidth;
						myRealHeight = options.outHeight;
					}
					options.inJustDecodeBounds = false;
					int coefficient = 1;
					if (!ignoreSize) {
						while ((myRealHeight > maxHeight * coefficient) ||
							   (myRealWidth > maxWidth *coefficient)) {
							coefficient *= 2;
						}
					}
					options.inSampleSize = coefficient;
					myBitmap = decodeWithOptions(options);
					if (myBitmap != null) {
						myLastRequestedWidth = maxWidth;
						myLastRequestedHeight = maxHeight;
					}
				} catch (OutOfMemoryError e) {
				}
			}else{//尽量大的 显示方式。这样内存消耗比较大，不适合用来显示目录，因为目录一次显示的图片太多。
				try {
					final BitmapFactory.Options options = new BitmapFactory.Options();
					
					if (myRealWidth <= 0) {
						options.inJustDecodeBounds = true;
						myBitmap=decodeWithOptions(options);
						myRealWidth = options.outWidth;
						myRealHeight = options.outHeight;
					}
					options.inJustDecodeBounds = false;
					int coefficient = 1;
					options.inSampleSize = coefficient;
					
					
					myBitmap=decodeWithOptions(options);
					
	//				System.out.println("---hym create bmp c size :"+myRealHeight+"--"+myRealWidth);
					//hym 创建是的 缩放，原来以 2的倍数进行， 这样做有些图片会压缩的很小，导致看不清楚。
					//需要 修改 采用 Matrix matrix = new Matrix();  来缩放
					
	//				while ((myRealHeight > maxHeight * coefficient) ||
	//					   (myRealWidth > maxWidth *coefficient)) {
	//					coefficient *= 2;
	//				}
	//				options.inSampleSize = coefficient;
	//				myBitmap = decodeWithOptions(options);
	//				if((myRealHeight > maxHeight * coefficient) ||
	//						   (myRealWidth > maxWidth *coefficient)) 
//					System.out.println("------bmp create  hym log--"+myRealWidth+"|"+myRealHeight+"-"+maxWidth+"|"+maxHeight);//
					if(!ignoreSize&&myBitmap != null){//hym 2010 -01-27 图片都转成 适合的大小 放大一点在手机上看 还是可以的。
						//hym 2011-02-23 处理  如果实际的图会超出屏幕才 进行缩小处理。如果图不会超出屏幕 不进行放大处理，
	//					不然会破坏一些文档的美观。如果图片占长宽的一半以上 进行放大。
						if((myRealHeight > maxHeight/2 ) || (myRealWidth > maxWidth/2 )) {
							float scaleWidth  = (float) (maxWidth-4) / myRealWidth;  
							float scaleHeight = (float) (maxHeight-4) / myRealHeight;  
							Matrix matrix = new Matrix(); 
							if(scaleWidth<scaleHeight){
								scaleHeight=scaleWidth;
							}else{
								scaleWidth=scaleHeight;
							}
							matrix.postScale(scaleWidth, scaleHeight); 
							Bitmap resizeBitmap = Bitmap.createBitmap(  
									myBitmap, 0, 0, myRealWidth, myRealHeight, matrix, false);  
							myBitmap.recycle();  
		//					System.out.println("---hym  bmp r size :"+resizeBitmap.getHeight()+"--"+resizeBitmap.getWidth());
							myBitmap= resizeBitmap ;  
						}
					}else{//实际大小
	//					options.inSampleSize = coefficient;
	//					myBitmap = decodeWithOptions(options);
					}
					
					if (myBitmap != null) {
						myLastRequestedWidth = maxWidth;
						myLastRequestedHeight = maxHeight;
					}
				} catch (OutOfMemoryError e) {
				}
			}
		}
		return myBitmap;
	}
}
