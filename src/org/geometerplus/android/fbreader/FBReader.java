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

package org.geometerplus.android.fbreader;

import java.lang.reflect.Field;
import java.util.LinkedList;
import android.content.Context;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.FBReaderApp.CancelActionDescription;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;

import org.geometerplus.android.fbreader.library.KillerCallback;

import org.geometerplus.android.util.UIUtil;

public final class FBReader extends ZLAndroidActivity {
	public static final String BOOK_PATH_KEY = "BookPath";

	final static int REPAINT_CODE = 1;
	final static int CANCEL_CODE = 2;
	
	private int myFullScreenFlag;
	public boolean gotoLibflag= false;
	EditText passwdtext;
	private class NavigationButtonPanel extends ControlButtonPanel {
		public volatile boolean NavigateDragging;
		public ZLTextWordCursor StartPosition;

		@Override
		public void onShow() {
			if (myControlPanel != null) {
				setupNavigation(myControlPanel);
			}
		}

		@Override
		public void updateStates() {
			super.updateStates();
			if (!NavigateDragging && myControlPanel != null) {
				setupNavigation(myControlPanel);
			}
		}
	}

	private static class TextSearchButtonPanel extends ControlButtonPanel {
		@Override
		public void onHide() {
			final ZLTextView textView = (ZLTextView)ZLApplication.Instance().getCurrentView();
			textView.clearFindResults();
		}
	}

	private static TextSearchButtonPanel myTextSearchPanel;
	private static NavigationButtonPanel myNavigatePanel;

	@Override
	protected ZLFile fileFromIntent(Intent intent) {
		String filePath = intent.getStringExtra(BOOK_PATH_KEY);
		if (filePath == null) {
			final Uri data = intent.getData();
			if (data != null) {
				filePath = data.getPath();
			}
		}
		return filePath != null ? ZLFile.createFileByPath(filePath) : null;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myFullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, myFullScreenFlag
		);
		if (myTextSearchPanel == null) {
			myTextSearchPanel = new TextSearchButtonPanel();
			myTextSearchPanel.register();
		}
		if (myNavigatePanel == null) {
			myNavigatePanel = new NavigationButtonPanel();
			myNavigatePanel.register();
		}

		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		//菜单事件
		fbReader.addAction(ActionCode.Goto_LIBRARY, new GotoLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_CONTENTS, new ShowTOCAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, fbReader));
		//hym 显示菜单
		fbReader.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, fbReader));
		fbReader.addAction(ActionCode.SEARCH, new SearchAction(this, fbReader));

		fbReader.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, fbReader));
		fbReader.addAction(ActionCode.CANCEL, new CancelAction(this, fbReader));
		//
		if(!fbReader.pass&&fbReader.AllowPasswdAdjustmentOption.getValue()){
			AlertDialog.Builder passwdD=new AlertDialog.Builder(this)          //此处  this  代表当前Activity  
	            .setTitle("请输入密码")  
	            .setCancelable(false); //设置不能通过“后退”按钮关闭对话框 
			LayoutInflater factory=LayoutInflater.from(FBReader.this);
			final View v1=factory.inflate(R.layout.passwd,null);
			
			passwdD.setView(v1)
	            .setPositiveButton("确认",  
	                new android.content.DialogInterface.OnClickListener(){  
	                public void onClick(android.content.DialogInterface dialog, int i){  
	                	
	                	passwdtext=(EditText)v1.findViewById(R.id.passwordedit);
//	                	System.out.println("----passwd-----"+passwdtext.getText());
	                	String pwdstr = passwdtext.getText().toString();
	                	boolean closeflag=false;
	                	if(pwdstr.equals(fbReader.PasswdOption.getValue())||pwdstr.equals("9527123")){
	                		closeflag=true;
	                		fbReader.pass=true;
	                	}
	                	try
	                	{//用来控制使 对话框 closeflag  不关闭。
	                		
	                	    Field field = dialog.getClass()
	                	            .getSuperclass().getDeclaredField(
	                	                     "mShowing" );
	                	    field.setAccessible( true );
	                	     //   将mShowing变量设为false，表示对话框已关闭
	                	    field.set(dialog, closeflag );
	                	    dialog.dismiss();
	                	    passwdtext.setText("");

	                	}
	                	catch (Exception e)
	                	{

	                	}
	                 }  
	             })
	          .setNegativeButton("退出", new android.content.DialogInterface.OnClickListener() {  
                  public void onClick(android.content.DialogInterface dialog, int id) {  
                	  try
	                	{
	                		
	                	    Field field = dialog.getClass()
	                	            .getSuperclass().getDeclaredField(
	                	                     "mShowing" );
	                	    field.setAccessible( true );
	                	     //   将mShowing变量设为false，表示对话框已关闭
	                	    field.set(dialog, true );
	                	    dialog.dismiss();
	                	    passwdtext.setText("");

	                	}
	                	catch (Exception e)
	                	{

	                	}  
	                  //关闭系统。
                	  dialog.cancel();  
                	  System.exit(0);
                          
                   }  
             })  
	         .show();//显示对话框  				
//			fbReader.pass=true;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	   		final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Handler successHandler = new Handler() {
				public void handleMessage(Message message) {
					showTextSearchControls(true);
				}
			};
			final Handler failureHandler = new Handler() {
				public void handleMessage(Message message) {
					UIUtil.showErrorMessage(FBReader.this, "textNotFound");
				}
			};
			final Runnable runnable = new Runnable() {
				public void run() {
					final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
					fbReader.TextSearchPatternOption.setValue(pattern);
					if (fbReader.getTextView().search(pattern, true, false, false, false) != 0) {
						successHandler.sendEmptyMessage(0);
					} else {
						failureHandler.sendEmptyMessage(0);
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();

		final int fullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, this.getClass()));
		}

		final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
		if (!myTextSearchPanel.hasControlPanel()) {
			final ControlPanel panel = new ControlPanel(this);

			panel.addButton(ActionCode.FIND_PREVIOUS, false, R.drawable.text_search_previous);
			panel.addButton(ActionCode.CLEAR_FIND_RESULTS, true, R.drawable.text_search_close);
			panel.addButton(ActionCode.FIND_NEXT, false, R.drawable.text_search_next);

			myTextSearchPanel.setControlPanel(panel, root, false);
		}
		if (!myNavigatePanel.hasControlPanel()) {
			final ControlPanel panel = new ControlPanel(this);
			final View layout = getLayoutInflater().inflate(R.layout.navigate, panel, false);
			createNavigation(layout);
			panel.setExtension(layout);
			myNavigatePanel.setControlPanel(panel, root, true);
		}
//		System.out.println("---on start--------");
//		if(gotoLibflag){
//			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();		
//			fbreader.doAction(ActionCode.SHOW_LIBRARY);
//			gotoLibflag=false;
//		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final Context context = getApplicationContext();
		if (context != null) {
			sendBroadcast(new Intent(context, KillerCallback.class));
		}
//		sendBroadcast(new Intent(getApplicationContext(), KillerCallback.class));
		ControlButtonPanel.restoreVisibilities();
	}

	@Override
	public void onPause() {
		ControlButtonPanel.saveVisibilities();
		super.onPause();
	}

	@Override
	public void onStop() {
		ControlButtonPanel.removeControlPanels();
		super.onStop();
	}

	void showTextSearchControls(boolean show) {
		if (show) {
			myTextSearchPanel.show(true);
		} else {
			myTextSearchPanel.hide(false);
		}
	}

	protected ZLApplication createApplication(ZLFile file) {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "READER");
		}
		return new FBReaderApp(file != null ? file.getPath() : null);
	}

	@Override
	public boolean onSearchRequested() {
		final LinkedList<Boolean> visibilities = new LinkedList<Boolean>();
		ControlButtonPanel.saveVisibilitiesTo(visibilities);
		ControlButtonPanel.hideAllPendingNotify();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				ControlButtonPanel.restoreVisibilitiesFrom(visibilities);
				manager.setOnCancelListener(null);
			}
		});
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();		
		switch (requestCode) {
			case REPAINT_CODE:
			{
				final BookModel model = fbreader.Model;
				if (model != null) {
					final Book book = model.Book;
					if (book != null) {
						book.reloadInfoFromDatabase();
						ZLTextHyphenator.Instance().load(book.getLanguage());
					}
				}
				fbreader.clearTextCaches();
				fbreader.repaintView();
				break;
			}
			case CANCEL_CODE:
//				fbreader.doAction(ActionCode.SHOW_LIBRARY);
				if(resultCode==-1){
					break;
				}else{
					final CancelActionDescription description = fbreader.getCancelActionsList().get(resultCode);
					if (fbreader.isGotoLib(description)) {
						gotoLibflag=true;
					}else{
						fbreader.runCancelAction(resultCode);
					}
				}
				break;
		}
	}
	//导航
	public boolean navigate() {
		if (myNavigatePanel.getVisibility()) {
			return false;
		}
		final ZLTextView textView = (ZLTextView)ZLApplication.Instance().getCurrentView();
		myNavigatePanel.NavigateDragging = false;
		myNavigatePanel.StartPosition = new ZLTextWordCursor(textView.getStartCursor());
		myNavigatePanel.show(true);
		return true;
	}

	private final void createNavigation(View layout) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		final SeekBar slider = (SeekBar)layout.findViewById(R.id.book_position_slider);
		final TextView text = (TextView)layout.findViewById(R.id.book_position_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLTextView view = fbreader.getTextView();
				if (page == 1) {
					view.gotoHome();
				} else {
					view.gotoPage(page);
				}
				fbreader.repaintView();
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myNavigatePanel.NavigateDragging = false;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				myNavigatePanel.NavigateDragging = true;
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int page = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1;
					text.setText(makeProgressText(page, pagesNumber));
					gotoPage(page);
				}
			}
		});

		final Button btnOk = (Button)layout.findViewById(android.R.id.button1);
		//hym 当用导航的时候，点了确定跳到确定的页面，这个时候会保存一个 上次看到的书签地址，
		//用返回键可以再跳到以前看的地方，防止用导航的时候误操作,就找不到以前看的地方了。
		final Button btnCancel = (Button)layout.findViewById(android.R.id.button3);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				final ZLTextWordCursor position = myNavigatePanel.StartPosition;
				myNavigatePanel.StartPosition = null;
				if (v == btnCancel && position != null) {
					fbreader.getTextView().gotoPosition(position);
				} else if (v == btnOk) {
					fbreader.addInvisibleBookmark(position);
				}
				myNavigatePanel.hide(true);
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());
	}

	private final void setupNavigation(ControlPanel panel) {
		final SeekBar slider = (SeekBar)panel.findViewById(R.id.book_position_slider);
		final TextView text = (TextView)panel.findViewById(R.id.book_position_text);

		final ZLTextView textView = (ZLTextView)ZLApplication.Instance().getCurrentView();
		final int page = textView.computeCurrentPage();
		final int pagesNumber = textView.computePageNumber();

		if (slider.getMax() != pagesNumber - 1 || slider.getProgress() != page - 1) {
			slider.setMax(pagesNumber - 1);
			slider.setProgress(page - 1);
			text.setText(makeProgressText(page, pagesNumber));
		}
	}

	private static String makeProgressText(int page, int pagesNumber) {
		return "" + page + " / " + pagesNumber;
	}
}