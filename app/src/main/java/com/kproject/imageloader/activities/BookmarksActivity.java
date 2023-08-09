package com.kproject.imageloader.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.BookmarkAdapter;
import com.kproject.imageloader.database.BookmarksDB;
import com.kproject.imageloader.dialogs.AddOrEditBookmarkDialogFragment;
import com.kproject.imageloader.models.Bookmark;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookmarksActivity extends BaseActivity implements BookmarkAdapter.ItemClickListener, AddOrEditBookmarkDialogFragment.DialogListener {
	private static final int PERMISSION_REQUEST_CODE = 1;
	private static final int OPERATION_ADD = 0;
	private static final int OPERATION_EDIT = 1;
	
	private RecyclerView rvBookmarkList;
	private BookmarkAdapter adpBookmarkListAdapter;
	private RelativeLayout rlMainLayout;
	private TextView tvEmptyBookmarkListInfo;
	private LinearLayout llEmptyBookmarkList;
	private BookmarksDB bookmarksDB;
	private Toolbar toolbar;
	
	private List<Bookmark> bookmarkList;
	private int bookmarkSelectedToImportation;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
		rlMainLayout = findViewById(R.id.rlBookmarks_MainLayout);
		rvBookmarkList = findViewById(R.id.rvBookmark_BookmarkList);
		tvEmptyBookmarkListInfo = findViewById(R.id.tvBookmark_EmptyBookmarkListInfo);
		llEmptyBookmarkList = findViewById(R.id.llBookmark_EmptyBookmarkList);
		toolbar = findViewById(R.id.tbBookmarks_Toolbar);
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			toolbar.setPopupTheme(R.style.DarkToolbarStyle);
		}
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		bookmarksDB = new BookmarksDB(this);
		bookmarkList = bookmarksDB.getAllBookmarks();
		rvBookmarkList.setLayoutManager(new LinearLayoutManager(this));
		adpBookmarkListAdapter = new BookmarkAdapter(this, bookmarkList);
		adpBookmarkListAdapter.setClickListener(this);
		rvBookmarkList.setAdapter(adpBookmarkListAdapter);
		
		changeListVisibility();
		
		if (Build.VERSION.SDK_INT >= 23) {
			if (!permissionGranted()) {
				requestPermissionInfo();
			}
		}
		
		setThemeColors();
    }
	
	@Override
	public void setThemeColors() {
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			rlMainLayout.setBackgroundColor(Color.parseColor(Constants.COLOR_DARK_LAYOUT_BACKGROUND));
			tvEmptyBookmarkListInfo.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_bookmarks, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_bookmarks_add_bookmark:
				AddOrEditBookmarkDialogFragment dialog = AddOrEditBookmarkDialogFragment.newInstance("", "", OPERATION_ADD, 0);
				dialog.show(getSupportFragmentManager(), dialog.getTag());
				return true;
			case R.id.menu_bookmarks_import_bookmarks:
				importBookmarks();
				return true;
			case R.id.menu_bookmarks_export_bookmarks:
				exportBookmarks();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onRecyclerViewItemClick(View view, final int position) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.dialog_load_page));
		dialog.setMessage(getResources().getString(R.string.dialog_page_url) + ": " + bookmarkList.get(position).getUrl() + "\n\n" + getResources().getString(R.string.dialog_load_page_url));
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				Intent intent = new Intent(BookmarksActivity.this, MainActivity.class);
				intent.putExtra("pageUrl", bookmarkList.get(position).getUrl());
				setResult(RESULT_OK, intent);
				dialogInterface.dismiss();
				finish();
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}

	@Override
	public void onRecyclerViewLongItemClick(View view, final int position) {
		String[] options = {getResources().getString(R.string.dialog_edit_bookmark), getResources().getString(R.string.dialog_delete_bookmark)};
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, Utils.setThemeForDialog());
		dialog.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				if (pos == 0) {
					int id = bookmarkList.get(position).getId();
					String title = bookmarkList.get(position).getTitle();
					String url = bookmarkList.get(position).getUrl();
					AddOrEditBookmarkDialogFragment dialog = AddOrEditBookmarkDialogFragment.newInstance(title, url, OPERATION_EDIT, id);
					dialog.show(getSupportFragmentManager(), dialog.getTag());
				} else if (pos == 1) {
					dialogDeleteBookmark(bookmarkList.get(position));
				}
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
	@Override
	public void onSaveBookmark(String title, String pageUrl, int operationType, int bookmarkId) {
		if (!title.isEmpty() && !pageUrl.isEmpty()) {
			if (URLUtil.isValidUrl(pageUrl)){
				Bookmark bookmark = new Bookmark();
				bookmark.setTitle(title);
				bookmark.setUrl(pageUrl);
				if (operationType == OPERATION_ADD) {
					bookmarksDB.addBookmark(bookmark);
				} else if (operationType == OPERATION_EDIT) {
					bookmark.setId(bookmarkId);
					bookmarksDB.updateBookmart(bookmark);
				}
				updateRecyclerView();
			} else {
				Utils.showToast(R.string.toast_bookmark_url_invalid);
			}
		} else {
			Utils.showToast(R.string.toast_bookmark_empty_fields_error);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE:
				boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
				if (!granted) {
					requestPermissionInfo();
				}
				break;
		}
	}

	private void requestPermissionInfo() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			// Mensagem de informação para caso o usuário já tenha negado as permissões pelo menos uma vez
			AlertDialog.Builder dialog = new AlertDialog.Builder(BookmarksActivity.this);
			dialog.setTitle(getResources().getString(R.string.dialog_request_permission));
			dialog.setMessage(getResources().getString(R.string.dialog_request_permission_to_import_and_export_bookmarks));
			dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int position) {
					ActivityCompat.requestPermissions(BookmarksActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
				}
			});
			dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int position) {
					dialogInterface.dismiss();
					finish();
				}
			});
			dialog.show();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
		}
	}

	private boolean permissionGranted() {
		int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		return false;
	}
	
	private void changeListVisibility() {
		if (bookmarkList.isEmpty()) {
			rvBookmarkList.setVisibility(View.GONE);
			llEmptyBookmarkList.setVisibility(View.VISIBLE);
		} else {
			rvBookmarkList.setVisibility(View.VISIBLE);
			llEmptyBookmarkList.setVisibility(View.GONE);
		}
		getSupportActionBar().setTitle(getResources().getString(R.string.label_bookmarks) + " (" + bookmarkList.size() + ")");
	}
	
	public void updateRecyclerView() {
		bookmarkList = bookmarksDB.getAllBookmarks();
		adpBookmarkListAdapter.getAllBookmarks().clear();
		adpBookmarkListAdapter.getAllBookmarks().addAll(bookmarkList);
		adpBookmarkListAdapter.notifyDataSetChanged();
		changeListVisibility();
	}
	
	private void dialogDeleteBookmark(final Bookmark bookmark) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.dialog_delete_bookmark));
		dialog.setMessage(getResources().getString(R.string.dialog_delete_bookmark_confirmation));
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				bookmarksDB.deleteBookmark(bookmark);
				updateRecyclerView();
				dialogInterface.dismiss();
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
	private void dialogImportBookmarks(ArrayList<File> bookmarksFound) {
		final String[] bookmarksName = new String[bookmarksFound.size()];
		final String[] bookmarksPath = new String[bookmarksFound.size()];
		for (int i = 0; i < bookmarksFound.size(); i++) {
			bookmarksName[i] = bookmarksFound.get(i).getName();
			bookmarksPath[i] = bookmarksFound.get(i).getPath();
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.menu_bookmarks_import_bookmarks));
		dialog.setSingleChoiceItems(bookmarksName, 0, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				bookmarkSelectedToImportation = pos;
			}
		});
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				bookmarksDB.importDatabase(bookmarksPath[bookmarkSelectedToImportation]);
				updateRecyclerView();
				Utils.showToast(R.string.toast_successfully_imported_bookmarks);
				dialogInterface.dismiss();
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
	private void importBookmarks() {
		try {
			File bookmarksFilePath = new File(Environment.getExternalStorageDirectory().toString(), "Image Loader");
			bookmarksFilePath.mkdirs();
			File[] fileList = bookmarksFilePath.listFiles();
			ArrayList<File> bookmarksFound = new ArrayList<>();
			for (File file : fileList) {
				if (file.getName().endsWith(".db")) {
					bookmarksFound.add(file);
				}
			}
			// Verifica se existe mais de um favorito
			if (bookmarksFound.size() > 1) {
				dialogImportBookmarks(bookmarksFound);
			} else if (bookmarksFound.size() == 1) {
				bookmarksDB.importDatabase(bookmarksFound.get(0).toString());
				updateRecyclerView();
				Utils.showToast(R.string.toast_successfully_imported_bookmarks);
			} else {
				Utils.showToast(R.string.toast_need_export_bookmarks_before);
			}
		} catch (Exception e) {
			Utils.showToast(R.string.toast_import_bookmarks_error);
		}
	}

	private void exportBookmarks() {
		try {
			File folderPath = new File(Environment.getExternalStorageDirectory().toString(), "Image Loader");
			folderPath.mkdirs();
			DateFormat dateFormat = new SimpleDateFormat("(dd-MM-yyyy_HH-mm-ss)");
			bookmarksDB.exportDatabase(folderPath.toString() + String.format("/bookmarks %s.db", dateFormat.format(new Date())));
			Utils.showToast(R.string.toast_successfully_exported_bookmarks);
		} catch (Exception e) {
			Utils.showToast(R.string.toast_export_bookmarks_error);
		}
	}
	
}
