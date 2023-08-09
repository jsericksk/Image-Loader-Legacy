package com.kproject.imageloader.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.application.MyApplication;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.FileUtils;
import com.kproject.imageloader.utils.Utils;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageViewerActivity extends AppCompatActivity {
	private static final int CONFIGURATIONS_CHANGED = 10;
	
	private Toolbar toolbar;
	private ViewPager viewPager;
	private FrameLayout flMainLayout;
	private LinearLayout llBottomLayout;
	private ImageButton ibShare;
	private ImageButton ibDownload;
	private ImageButton ibInfo;
	private ImageButton ibAccessPage;
	
	private ArrayList<Image> imageList = new ArrayList<>();
	private float zoomLevel;
	private boolean configurationsChanged;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.setThemeForToolbar());
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
		toolbar = findViewById(R.id.tbImageViewer_Toolbar);
		viewPager = findViewById(R.id.vpImageViewer_ViewPager);
		flMainLayout = findViewById(R.id.flImageViewer_MainLayout);
		flMainLayout.setBackgroundColor(Color.parseColor(Utils.getBackgroundColor()));
		llBottomLayout = findViewById(R.id.llImageViewer_BottomLayout);
		ibShare = findViewById(R.id.ibImageViewer_Share);
		ibDownload = findViewById(R.id.ibImageViewer_Download);
		ibInfo = findViewById(R.id.ibImageViewer_Info);
		ibAccessPage = findViewById(R.id.ibImageViewer_AccessPage);
		
		if (Utils.getPreferenceValue(Constants.PREF_HIDE_STATUSBAR, true)) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			toolbar.setPopupTheme(R.style.DarkToolbarStyle);
		}
		
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		toolbar.setBackgroundColor(Color.parseColor(Utils.colorPrimary()));
		llBottomLayout.setBackgroundColor(Color.parseColor(Utils.colorPrimary()));
		
		zoomLevel = Utils.getZoomLevel();
		
		Bundle data = getIntent().getExtras();
		imageList = data.getParcelableArrayList("imageListData");
		int imagePosition = data.getInt("imageListPosition");
		
		CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(this);
		viewPager.setAdapter(customPagerAdapter);
		viewPager.setCurrentItem(imagePosition);
		
		if (savedInstanceState != null) {
			configurationsChanged = savedInstanceState.getBoolean("restoreConfigurationsChanged");
			/*
			* Esta variável é usada para enviar a confirmação de que
			* houve mudança nas configurações, feitas através dessa tela
			* e que podem afetar a MainActivity
			*/
			if (configurationsChanged) {
				setResult(RESULT_OK);
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("restoreConfigurationsChanged", configurationsChanged);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CONFIGURATIONS_CHANGED && resultCode == RESULT_OK) {
			configurationsChanged = true;
			recreate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_image_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_image_viewer_settings:
				startActivityForResult(new Intent(this, SettingsActivity.class), CONFIGURATIONS_CHANGED);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setTitleAndSubtitleToolbar(int position) {
		String imageName = imageList.get(position).getImageName();
		String imageFormat = imageName.substring(imageName.lastIndexOf("."), imageName.length()).toUpperCase();
		// Caso seja um formato inválido
		if (imageFormat.length() > 6 || imageFormat.length() < 2) {
			imageFormat = ".JPG";
		}
		getSupportActionBar().setTitle(imageName);
		getSupportActionBar().setSubtitle(getResources().getString(R.string.toolbar_subtitle_image) + " " + imageFormat + " (" + (position + 1) + "/" + imageList.size() + ")");
	}
	
	private void dialogImageInfo(String imageName, String imageSize, String imageResolution, final String imageUrl, final String imagePageUrl) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.dialog_image_info));
		View view = getLayoutInflater().inflate(R.layout.dialog_image_info, null);
		TextView tvImageNameInfo = view.findViewById(R.id.tvImageInfo_NameInfo);
		TextView tvImageSizeInfo = view.findViewById(R.id.tvImageInfo_SizeInfo);
		TextView tvImageResolutionInfo = view.findViewById(R.id.tvImageInfo_ResolutionInfo);
		TextView tvImageUrlInfo = view.findViewById(R.id.tvImageInfo_UrlInfo);
		TextView tvImagePageInfo = view.findViewById(R.id.tvImageInfo_ImagePageInfo);
		
		TextView tvImageName = view.findViewById(R.id.tvImageInfo_Name);
		TextView tvImageSize = view.findViewById(R.id.tvImageInfo_Size);
		TextView tvImageResolution = view.findViewById(R.id.tvImageInfo_Resolution);
		TextView tvImageUrl = view.findViewById(R.id.tvImageInfo_Url);
		TextView tvImagePageUrl = view.findViewById(R.id.tvImageInfo_ImagePageUrl);
		tvImageName.setText(imageName);
		tvImageSize.setText(imageSize);
		tvImageResolution.setText(imageResolution);
		tvImageUrl.setText(imageUrl);
		tvImagePageUrl.setText(imagePageUrl);
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			tvImageNameInfo.setTextColor(Color.WHITE);
			tvImageSizeInfo.setTextColor(Color.WHITE);
			tvImageResolutionInfo.setTextColor(Color.WHITE);
			tvImageUrlInfo.setTextColor(Color.WHITE);
			tvImagePageInfo.setTextColor(Color.WHITE);
			
			tvImageName.setTextColor(Color.WHITE);
			tvImageSize.setTextColor(Color.WHITE);
			tvImageResolution.setTextColor(Color.WHITE);
			tvImageUrl.setTextColor(Color.WHITE);
			tvImagePageUrl.setTextColor(Color.WHITE);
		}
		
		ImageButton ibCopyUrl = view.findViewById(R.id.ibImageInfo_CopyUrl);
		ibCopyUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ClipboardManager clipboard = (ClipboardManager) MyApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("text", imageUrl);
				clipboard.setPrimaryClip(clip);
				Utils.showToast(R.string.toast_image_url_copied);
			}
		});
		dialog.setView(view);
		
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
	private class CustomPagerAdapter extends PagerAdapter {
		private Context context;
		private LayoutInflater layoutInflater;

		public CustomPagerAdapter(Context context) {
			this.context = context;
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return imageList.size();
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			final View view = layoutInflater.inflate(R.layout.pager_adapter_image_viewer, container, false);
			final TouchImageView ivImageView = view.findViewById(R.id.ivPagerAdapter_Image);
			ivImageView.setMaxZoom(zoomLevel);
			ivImageView.setTag("Image " + position);
			setTitleAndSubtitleToolbar(viewPager.getCurrentItem());
			
			Picasso.with(context).load(imageList.get(position).getImageUrl())
			.placeholder(R.drawable.loading_image)
			.error(R.drawable.error_loading_image)
			.into(ivImageView);
					
			ivImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (toolbar.getVisibility() == View.GONE) {
						toolbar.setVisibility(View.VISIBLE);
						llBottomLayout.setVisibility(View.VISIBLE);
					} else {
						toolbar.setVisibility(View.GONE);
						llBottomLayout.setVisibility(View.GONE);
					}
				}
			});
			
			ibShare.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					shareImage(viewPager.findViewWithTag("Image " + viewPager.getCurrentItem()));
				}
			});
			
			ibDownload.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Image image = imageList.get(viewPager.getCurrentItem());
					new DownloadImageTask(context, image).execute();
				}
			});
			
			ibInfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						String imageUrl = imageList.get(viewPager.getCurrentItem()).getImageUrl();
						String imageName = FileUtils.fileName(imageUrl);
						String imageSize = FileUtils.formatFileSize(new File(getImagePathFromCache(imageUrl)).length());
						String imageResolution = imageResolution(viewPager.findViewWithTag("Image " + viewPager.getCurrentItem()));
						String imagePageUrl = imageList.get(viewPager.getCurrentItem()).getImagePage();
						dialogImageInfo(imageName, imageSize, imageResolution, imageUrl, imagePageUrl);
					} catch (Exception e) {
						String imageUrl = imageList.get(viewPager.getCurrentItem()).getImageUrl();
						String imageName = FileUtils.fileName(imageUrl);
						String imageSize = getResources().getString(R.string.toast_image_info_error);
						String imageResolution = getResources().getString(R.string.toast_image_info_error);
						String imagePageUrl = imageList.get(viewPager.getCurrentItem()).getImagePage();
						dialogImageInfo(imageName, imageSize, imageResolution, imageUrl, imagePageUrl);
					}
				}
			});
			
			ibAccessPage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String imagePage = imageList.get(viewPager.getCurrentItem()).getImagePage();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(imagePage)));
				}
			});
			
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
		private void shareImage(Object object) {
			try {
				TouchImageView imageView = (TouchImageView) object;
				BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
				Bitmap bitmap = bitmapDrawable .getBitmap();
				String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", null);
				Uri bitmapUri = Uri.parse(bitmapPath);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
				startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_image_message)));
			} catch (Exception e) {
				Utils.showToast(R.string.share_image_error);
			}
		}
		
		private String imageResolution(Object object) {
			TouchImageView imageView = (TouchImageView) object;
			BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
			Bitmap bitmap = bitmapDrawable.getBitmap();
			int height = bitmap.getHeight();
			int width = bitmap.getWidth();
			return width + "x" + height;
		}
		
		private String getImagePathFromCache(String imageUrl) {
			final String CACHE_PATH = ImageViewerActivity.this.getCacheDir().getAbsolutePath() + "/picasso-cache/";
			File[] files = new File(CACHE_PATH).listFiles();
			for (File file : files) {
				String fileName = file.getName();
				if (fileName.contains(".") && fileName.substring(fileName.lastIndexOf(".")).equals(".0")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(file));
						if (br.readLine().equals(imageUrl)) {
							File imageFile = new File(CACHE_PATH + fileName.replace(".0", ".1"));		
							if (imageFile.exists()) {
								return imageFile.getPath();
							}
						}
					} catch (FileNotFoundException | IOException e) {}
				}
			}
			return null;
		}

	}
	
	private class DownloadImageTask extends AsyncTask<Void, Long, Long[]> {
		private Context context;
		private Image image;
		private AlertDialog dialog;
		private String saveAndGetImagePath;
		private boolean isDownloadCancelled;
		
		public DownloadImageTask(Context context, Image image) {
			this.context = context;
			this.image = image;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			AlertDialog.Builder builder = new AlertDialog.Builder(context, Utils.setThemeForDialog());
			View view = getLayoutInflater().inflate(R.layout.dialog_downloading_image, null);
			TextView tvDownloading = view.findViewById(R.id.tvDownloadingImage_Downloading);
			if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
				tvDownloading.setTextColor(Color.parseColor("#CCCCCC"));
			}
			builder.setTitle(getResources().getString(R.string.notification_downloading));
			builder.setCancelable(false);
			builder.setPositiveButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int pos) {
					if (dialog != null) {
						dialog.dismiss();
					}
					Utils.showToast(R.string.toast_download_image_cancelled);
					cancel(true);
					isDownloadCancelled = true;
				}
			});
			builder.setView(view);
			dialog = builder.create();
			dialog.show();
		}

		@Override
		protected Long[] doInBackground(Void[] params) {
			String downloadPath = Utils.getDownloadPath();
			String imageUrl = image.getImageUrl();
			String imageName = image.getImageName();
			try {
				saveAndGetImagePath = FileUtils.downloadImage(downloadPath, imageUrl, imageName);
			} catch (Exception e) {
				cancel(true);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Long[] result) {
			super.onPostExecute(result);
			if (dialog != null) {
				dialog.dismiss();
			}
			Utils.showLongToast(getResources().getString(R.string.toast_image_downloaded_successfully, saveAndGetImagePath));
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (dialog != null) {
				dialog.dismiss();
			}
			if (!isDownloadCancelled) {
				Utils.showToast(R.string.toast_download_image_error);
			}
		}
		
	}

}
