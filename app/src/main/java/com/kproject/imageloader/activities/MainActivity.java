package com.kproject.imageloader.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.ImageListAdapter;
import com.kproject.imageloader.dialogs.DownloadLinksDialogFragment;
import com.kproject.imageloader.dialogs.FolderNameToDownloadDialogFragment;
import com.kproject.imageloader.dialogs.LoadPageDialogFragment;
import com.kproject.imageloader.dialogs.RequestPermissionDialogFragment;
import com.kproject.imageloader.fragments.LoadPageTaskFragment;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.services.DownloadService;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.res.ColorStateList;

public class MainActivity extends BaseActivity implements LoadPageDialogFragment.DialogListener,
         													   ImageListAdapter.ItemClickListener,
															   LoadPageTaskFragment.TaskCallbacks,
															   RequestPermissionDialogFragment.RequestPermissionListener, 
															   NavigationView.OnNavigationItemSelectedListener {
	private static final int CONFIGURATIONS_CHANGED = 10;
	private static final int LOAD_BOOKMARK_PAGE_URL = 20;
	private static final int PERMISSION_REQUEST_CODE = 1;
	private static final String RESTORE_IMAGE_LIST = "restoreImageList";
	private static final String RESTORE_ORIGINAL_IMAGE_LIST = "restoreOriginalImageList";
	private static final String RESTORE_SELECTED_ITEMS_LIST = "restoreItemsSelectedList";
	private static final String TAG_LOAD_TASK_FRAGMENT = "loadPageTaskFragment";
	private static final boolean IMAGE_LIST_VISIBLE = true;
	private static final boolean IMAGE_LIST_GONE = false;
	private static final boolean EMPTY_IMAGE_LIST_VISIBLE = true;
	private static final boolean EMPTY_IMAGE_LIST_GONE = false;
	private static final boolean LOADING_VISIBLE = true;
	private static final boolean LOADING_GONE = false;
	private static final boolean TOOLBAR_SUBTITLE_VISIBLE = true;
	private static final boolean TOOLBAR_SUBTITLE_INVISIBLE = false;
	private static final boolean FAB_VISIBLE = true;
	private static final boolean FAB_INVISIBLE = false;
	
	private Toolbar toolbar;
	private DrawerLayout dlDrawerLayout;
	private NavigationView nvNavigationView;
	private RecyclerView rvImageList;
	private ImageListAdapter adpImageListAdapter;
	private RelativeLayout rlMainLayout;
	private LinearLayout llLoadingPage;
	private LinearLayout llEmptyImageList;
	private TextView tvEmptyImageListInfo;
	private TextView tvLoading;
	private FloatingActionButton fabLoadPage;
	private ActionMode actionMode;
	private AdView adView;
	
	private ArrayList<Image> imageList = new ArrayList<>();
	private ArrayList<Image> originalImageList = new ArrayList<>();
	private ArrayList<Image> restoreSelectedItems = new ArrayList<>();
	private String pageTitle;
	private String pageUrlOrQueryInformed;
	private int filterOption;
	private long backPressed;
	private boolean isTaskRunning;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.setThemeForToolbar());
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		toolbar = findViewById(R.id.tbMain_Toolbar);
		// Muda as cores do menu caso esteja no tema dark
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			toolbar.setPopupTheme(R.style.DarkToolbarStyle);
		}
		setSupportActionBar(toolbar);
		dlDrawerLayout = findViewById(R.id.dlMain_Drawerlayout);
		nvNavigationView = findViewById(R.id.nvMain_NavigationView);
		rlMainLayout = findViewById(R.id.rlMain_MainLayout);
		rvImageList = findViewById(R.id.rvMain_ImageList);
		llLoadingPage = findViewById(R.id.llMain_LoadingPage);
		llEmptyImageList = findViewById(R.id.llMain_EmptyImageList);
		tvEmptyImageListInfo = findViewById(R.id.tvMain_EmptyImageListInfo);
		tvLoading = findViewById(R.id.tvMain_Loading);
		fabLoadPage = findViewById(R.id.fabMain_LoadPage);
		adView = findViewById(R.id.avMain_AdView);
		
		fabLoadPageClick();
		initNavigationView();
		
		if (savedInstanceState != null) {
			imageList = savedInstanceState.getParcelableArrayList(RESTORE_IMAGE_LIST);
			originalImageList = savedInstanceState.getParcelableArrayList(RESTORE_ORIGINAL_IMAGE_LIST);
			restoreSelectedItems = savedInstanceState.getParcelableArrayList(RESTORE_SELECTED_ITEMS_LIST);
			pageTitle = savedInstanceState.getString("restorePageTitle");
			pageUrlOrQueryInformed = savedInstanceState.getString("restorePageUrlOrQueryInformed");
			filterOption = savedInstanceState.getInt("restoreFilterOption");
			isTaskRunning = savedInstanceState.getBoolean("isTaskRunning");
		}
		
		if (!imageList.isEmpty()) {
			setViewVisibility(IMAGE_LIST_VISIBLE, EMPTY_IMAGE_LIST_GONE, LOADING_GONE, TOOLBAR_SUBTITLE_VISIBLE, FAB_VISIBLE);
		} else if (isTaskRunning) {
			setViewVisibility(IMAGE_LIST_GONE, EMPTY_IMAGE_LIST_GONE, LOADING_VISIBLE, TOOLBAR_SUBTITLE_INVISIBLE, FAB_INVISIBLE);
		}
		
		boolean useGridLayout = Utils.getPreferenceValue(Constants.PREF_USE_GRID_LAYOUT, true);
		int numberOfColumnsInGrid = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_NUMBER_OF_COLUMNS_IN_GRID, "2"));
		rvImageList.setLayoutManager(useGridLayout ? new GridLayoutManager(this, numberOfColumnsInGrid) : new LinearLayoutManager(this));
		adpImageListAdapter = new ImageListAdapter(this, imageList);
		adpImageListAdapter.setClickListener(this);
		rvImageList.setAdapter(adpImageListAdapter);
		
		if (savedInstanceState != null) {
			adpImageListAdapter.getSelectedItems().addAll(restoreSelectedItems);
			if (!adpImageListAdapter.getSelectedItems().isEmpty()) {
				startActionMode();
			}
		}
		
		MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));  
		AdRequest adRequest = new AdRequest.Builder().build();   
		adView.loadAd(adRequest);
		adViewListener();
		
		if (Build.VERSION.SDK_INT >= 23) {
			if (!permissionGranted()) {
				requestPermissionInfo();
			}
		}
		
		handleTextShared();
		setThemeColors();
    }
	
	@Override
	public void setThemeColors() {
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			rlMainLayout.setBackgroundColor(Color.parseColor(Constants.COLOR_DARK_LAYOUT_BACKGROUND));
			nvNavigationView.setBackgroundColor(Color.parseColor(Constants.COLOR_DARK_LAYOUT_BACKGROUND));
			nvNavigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#CDCDCD")));
			tvEmptyImageListInfo.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
			tvLoading.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (adView != null) {
			adView.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adView != null) {
			adView.resume();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(RESTORE_IMAGE_LIST, imageList);
		outState.putParcelableArrayList(RESTORE_ORIGINAL_IMAGE_LIST, originalImageList);
		outState.putParcelableArrayList(RESTORE_SELECTED_ITEMS_LIST, adpImageListAdapter.getSelectedItems());
		outState.putString("restorePageTitle", pageTitle);
		outState.putString("restorePageUrlOrQueryInformed", pageUrlOrQueryInformed);
		outState.putInt("restoreFilterOption", filterOption);
		outState.putBoolean("isTaskRunning", isTaskRunning);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Recria a Activity caso alguma configuração importante, como o tema, seja modificada
		if (requestCode == CONFIGURATIONS_CHANGED && resultCode == RESULT_OK) {
			recreate();
		} else if (requestCode == LOAD_BOOKMARK_PAGE_URL && resultCode == RESULT_OK) {
			String pageUrlExtra = data.getExtras().getString("pageUrl");
			LoadPageTaskFragment fragment = LoadPageTaskFragment.newInstance(pageUrlExtra);
			getSupportFragmentManager().beginTransaction().add(fragment, TAG_LOAD_TASK_FRAGMENT).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_main_settings:
				startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), CONFIGURATIONS_CHANGED);
				return true;
			case R.id.menu_main_filter:
				dialogFilterImages();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean imageListVisible = rvImageList.getVisibility() == View.VISIBLE;
		menu.findItem(R.id.menu_main_filter).setVisible(imageListVisible ? true : false);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		dlDrawerLayout.closeDrawer(GravityCompat.START);
		switch (item.getItemId()) {
			case R.id.menu_navigationview_bookmarks:
				startActivityForResult(new Intent(MainActivity.this, BookmarksActivity.class), LOAD_BOOKMARK_PAGE_URL);
				break;
			case R.id.menu_navigationview_feedback:
				composeEmail();
				break;
			case R.id.menu_navigationview_share:
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app_extra_text_message));
				intent.setType("text/plain");
				startActivity(intent);
				break;
		}
		return false;
	}
	
	@Override
    public void onBackPressed() {
        if (dlDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            dlDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
			int exitConfirmation = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_EXIT_CONFIRMATION, "0"));
			if (exitConfirmation == Constants.EXIT_CONFIRMATION_OFF) {
				super.onBackPressed();
			} else if (exitConfirmation == Constants.EXIT_CONFIRMATION_SNACKBAR) {
				Snackbar sbExitConfirm = Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.snackbar_exit_confirmation), Snackbar.LENGTH_LONG);
				sbExitConfirm.setAction(getResources().getString(R.string.snackbar_exit), new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						finish();
					}
				});
				sbExitConfirm.show();
			} else if (exitConfirmation == Constants.EXIT_CONFIRMATION_TWO_TAPS) {
				if (backPressed + 2000 > System.currentTimeMillis()) {
					super.onBackPressed();
				} else {
					Utils.showToast(R.string.toast_exit_confirmation);
				}
				backPressed = System.currentTimeMillis();
			}
			
        }
    }
	
	@Override
	public void onLoadPageButtonClick(DialogInterface dialogInterface, final String pageUrlOrQuery) {
		if (!pageUrlOrQuery.isEmpty()) {
			pageUrlOrQueryInformed = pageUrlOrQuery;
			LoadPageTaskFragment fragment = LoadPageTaskFragment.newInstance(pageUrlOrQuery);
			getSupportFragmentManager().beginTransaction().add(fragment, TAG_LOAD_TASK_FRAGMENT).commit();
		}
	}
	
	@Override
	public void onRecyclerViewItemClick(View view, int position) {
		Image image = imageList.get(position);
		// Está no modo de ação/seleção de itens
		if (actionMode != null) {
			if (adpImageListAdapter.containsSelectedItem(image)) {
				adpImageListAdapter.removeSelectedItem(image, position);
				if (adpImageListAdapter.getSelectedItems().isEmpty()) {
					actionMode.finish();
					return;
				}
			} else {
				adpImageListAdapter.addSelectedItem(image, position);
			}
			actionMode.setTitle(adpImageListAdapter.getSelectedItems().size() + " / " + imageList.size());
		} else {
			Intent intent = new Intent(this, ImageViewerActivity.class);
			intent.putExtra("imageListPosition", position);
			intent.putParcelableArrayListExtra("imageListData", imageList);
			startActivityForResult(intent, CONFIGURATIONS_CHANGED);
		}
	}
	
	@Override
	public void onRecyclerViewLongItemClick(View view, int position) {
		if (actionMode == null) {
			adpImageListAdapter.addSelectedItem(imageList.get(position), position);
			startActionMode();
		}
	}
	
	private void requestPermissionInfo() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			// Mensagem de informação para caso o usuário já tenha negado as permissões pelo menos uma vez
			RequestPermissionDialogFragment dialog = RequestPermissionDialogFragment.newInstance();
			dialog.show(getSupportFragmentManager(), dialog.getTag());
		} else {
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
		}
	}

	// Método chamado assim que o usuário concede ou nega uma permissão
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE:
				if (grantResults.length <= 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					requestPermissionInfo();
				}
				break;
		}
	}

	private boolean permissionGranted() {
		int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		return false;
	}
	
	@Override
	public void onClickRequestPermissionDialog(DialogInterface dialog) {
		ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
		dialog.dismiss();
	}
	
	
	private void fabLoadPageClick() {
		fabLoadPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LoadPageDialogFragment dialog = LoadPageDialogFragment.newInstance(pageUrlOrQueryInformed);
				dialog.show(getSupportFragmentManager(), dialog.getTag());
			}
		});
	}
	private void initNavigationView() {
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, dlDrawerLayout, toolbar, R.string.navigationview_open, R.string.navigationview_close);
        toggle.setDrawerIndicatorEnabled(true);
        dlDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
		nvNavigationView.setNavigationItemSelectedListener(this);
	}
	
	private void adViewListener() {
		adView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				adView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				adView.setVisibility(View.GONE);
			}
		});
	}
	
	private void handleTextShared() {
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (sharedText != null) {
					if (URLUtil.isValidUrl(sharedText)) {
						LoadPageDialogFragment dialog = LoadPageDialogFragment.newInstance(sharedText);
						dialog.show(getSupportFragmentManager(), dialog.getTag());
					} else {
						Utils.showToast(R.string.toast_load_data_url_invalid);
					}
				}
			}
		}
	}
	
	private void startActionMode() {
		actionMode = startSupportActionMode(actionModeCallback);
		actionMode.setTitle(adpImageListAdapter.getSelectedItems().size() + " / " + imageList.size());
		adpImageListAdapter.isActionMode(true);
	}
	
	private void setViewVisibility(boolean rvImageListVisible, boolean llEmptyImageListVisible, boolean llLoadingPageVisible, boolean toolbarSubtitleVisible, boolean fabVisible) {
		try {
			rvImageList.setVisibility(rvImageListVisible ? View.VISIBLE : View.GONE);
			llEmptyImageList.setVisibility(llEmptyImageListVisible ? View.VISIBLE : View.GONE);
			llLoadingPage.setVisibility(llLoadingPageVisible ? View.VISIBLE : View.GONE);
			if (toolbarSubtitleVisible) {
				boolean showPageTitle = Utils.getPreferenceValue(Constants.PREF_SHOW_PAGE_TITLE, true);
				getSupportActionBar().setTitle(showPageTitle ? pageTitle : getResources().getString(R.string.app_name));
				getSupportActionBar().setSubtitle(getResources().getString(R.string.actionbar_title) + " " + imageList.size());
			} else {
				getSupportActionBar().setTitle(R.string.app_name);
				getSupportActionBar().setSubtitle("");
			}
			fabLoadPage.setVisibility(fabVisible ? View.VISIBLE : View.GONE);
		} catch (Exception e) {}
	}
	
	private void setFABVisibility(boolean visible) {
		fabLoadPage.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	private void dialogFilterImages() {
		ArrayList<String> filterBy = new ArrayList<String>(Arrays.asList(formatsFound()));
		filterBy.add(0,  getResources().getString(R.string.dialog_no_filter_images));
		final String[] formats = filterBy.toArray(new String[0]);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.dialog_filter_images));
		dialog.setSingleChoiceItems(formats, filterOption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				filterOption = pos;
				filterImageList(formats[pos]);
				dialogInterface.dismiss();
				adpImageListAdapter.notifyDataSetChanged();
			}
		});
		dialog.show();
	}
	
	private String[] formatsFound() {
		ArrayList<String> formatsFound = new ArrayList<>();
		for (Image image : originalImageList) {
			String imageName = image.getImageName();
			String imageFormat = imageName.substring(imageName.lastIndexOf("."), imageName.length());
			if (imageFormat.contains(".bin")) {
				continue;
			}
			// Caso seja um formato inválido
			if (imageFormat.length() > 6 || imageFormat.length() < 2) {
				imageFormat = ".jpg";
			}
			if (!formatsFound.contains(imageFormat.toUpperCase())) {
				formatsFound.add(imageFormat.toUpperCase());
			}
		}
		return formatsFound.toArray(new String[0]);
	}
	
	private void filterImageList(String format) {
		ArrayList<Image> filteredImageList = new ArrayList<>();
		if (format.equals(getResources().getString(R.string.dialog_no_filter_images))) {
			this.imageList.clear();
			this.imageList.addAll(originalImageList);
		} else {
			for (Image image : originalImageList) {
				if (image.getImageName().toUpperCase().endsWith(format)) {
					filteredImageList.add(image);
				}
			}
			this.imageList.clear();
			this.imageList.addAll(filteredImageList);
		}
		getSupportActionBar().setSubtitle(getResources().getString(R.string.actionbar_title) + " " + imageList.size());
	}
	
	private void composeEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"jsericksk@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_app));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
	
	/*
	* Remove o Fragment para evitar que ele seja recriado pela
	* Activity após ela ser destruída pelo sistema Android
	*/
	private void removeFragment(){
		try {
			FragmentManager fm = getSupportFragmentManager();
			LoadPageTaskFragment fragment = (LoadPageTaskFragment) fm.findFragmentByTag(TAG_LOAD_TASK_FRAGMENT);
			if (fragment != null) {
				fm.beginTransaction().remove(fragment).commit();
			}
		} catch (Exception e) {}
	}
	
	/*
	* Callbacks da LoadPageTaskFragment
	*/
	@Override
	public void onPreExecute(String pageUrlOrQuery) {
		isTaskRunning = true;
		boolean isPageUrl = URLUtil.isValidUrl(pageUrlOrQuery);
		int operationType = isPageUrl ? Constants.OPERATION_LOAD_PAGE : Constants.OPERATION_SEARCH;
		if (operationType == Constants.OPERATION_LOAD_PAGE) {
			tvLoading.setText(getResources().getString(R.string.textview_loading_page));
		} else if (operationType == Constants.OPERATION_SEARCH) {
			tvLoading.setText(getResources().getString(R.string.textview_searching, pageUrlOrQuery));
		}
		setViewVisibility(IMAGE_LIST_GONE, EMPTY_IMAGE_LIST_GONE, LOADING_VISIBLE, TOOLBAR_SUBTITLE_INVISIBLE, FAB_INVISIBLE);
		// Chamado para imediatamente ocultar o ícone de filtro por formatos
		supportInvalidateOptionsMenu();
	}
	
	@Override
	public void onPostExecute(List<Image> result) {
		isTaskRunning = false;
		filterOption = 0;
		imageList.clear();
		originalImageList.clear();
		if (!result.isEmpty()) {
			imageList.addAll(result);
			originalImageList.addAll(result);
			adpImageListAdapter.notifyDataSetChanged();
			rvImageList.scrollToPosition(0);
			setViewVisibility(IMAGE_LIST_VISIBLE, EMPTY_IMAGE_LIST_GONE, LOADING_GONE, TOOLBAR_SUBTITLE_VISIBLE, FAB_VISIBLE);
		} else {
			// Às vezes nenhuma imagem é encontrada na página
			Utils.showToast(R.string.toast_page_without_images);
			setViewVisibility(IMAGE_LIST_GONE, EMPTY_IMAGE_LIST_VISIBLE, LOADING_GONE, TOOLBAR_SUBTITLE_INVISIBLE, FAB_VISIBLE);
		}
		supportInvalidateOptionsMenu();
		removeFragment();
	}
	
	@Override
	public void onPageTitleObtained(String pageTitle) {
		// O título é obtido no método onPostExecute()
		this.pageTitle = pageTitle;
	}

	@Override
	public void onCancelled(boolean isSocketTimeoutException) {
		isTaskRunning = false;
		filterOption = 0;
		if (isSocketTimeoutException) {
			Utils.showToast(R.string.toast_load_data_page_timeout_error);
		} else {
			Utils.showToast(R.string.toast_load_data_page_error);
		}
		imageList.clear();
		originalImageList.clear();
		setViewVisibility(IMAGE_LIST_GONE, EMPTY_IMAGE_LIST_VISIBLE, LOADING_GONE, TOOLBAR_SUBTITLE_INVISIBLE, FAB_VISIBLE);
		supportInvalidateOptionsMenu();
		removeFragment();
	}
	
	/*
	* Callbacks no modo de seleção de imagens/ActionMode
	*/
	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

		@Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_action_main, menu);
            setFABVisibility(FAB_INVISIBLE);
			return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_action_main_download:
					if (DownloadService.isServiceRunning) {
						Utils.showLongToast(R.string.toast_download_service_is_running);
					} else {
						// É necessário isso porque a lista vai ser limpada no onDestroyActionMode() antes de passar os dados
						restoreSelectedItems.clear();
						restoreSelectedItems.addAll(adpImageListAdapter.getSelectedItems());
						FolderNameToDownloadDialogFragment dialog = FolderNameToDownloadDialogFragment.newInstance(restoreSelectedItems, originalImageList);
						dialog.show(getSupportFragmentManager(), dialog.getTag());
						actionMode.finish();
					}
					return true;
					
				case R.id.menu_action_main_downlod_links:
					restoreSelectedItems.clear();
					restoreSelectedItems.addAll(adpImageListAdapter.getSelectedItems());
					DownloadLinksDialogFragment dialog = DownloadLinksDialogFragment.newInstance(restoreSelectedItems);
					dialog.show(getSupportFragmentManager(), dialog.getTag());
					actionMode.finish();
					return true;
					
				case R.id.menu_action_main_select_all:
					if (adpImageListAdapter.getSelectedItems().size() == imageList.size()) {
						adpImageListAdapter.clearSelectedItems();
						actionMode.setTitle("0/" + imageList.size());
					} else {
						adpImageListAdapter.selectAllItems();
						actionMode.setTitle(adpImageListAdapter.getSelectedItems().size() + "/" + imageList.size());
					}
					return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			adpImageListAdapter.clearSelectedItems();
			adpImageListAdapter.isActionMode(false);
			setFABVisibility(FAB_VISIBLE);
        }
    };
	
}
