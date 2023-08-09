package com.kproject.imageloader.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.kproject.imageloader.R;
import com.kproject.imageloader.activities.MainActivity;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.services.DownloadService;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class DownloadService extends Service {
	private static final int NOTIFICATION_DOWNLOADING = 10;
	private static final int NOTIFICATION_DOWNLOAD_FINISHED = 20;
	private static final String NOTIFICATION_CHANNEL_DOWNLOAD = "downloadsChannel";
	public static boolean isServiceRunning = false;
	
	private DownloadTask downloadTask;

	@Override
	public void onCreate() {
		super.onCreate();
		isServiceRunning = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String getAction = intent.getAction();
			if (getAction.equals(Constants.START_SERVICE)) {
				ArrayList<Image> imageList = intent.getExtras().getParcelableArrayList("imageList");
				String downloadPath = intent.getExtras().getString("downloadPath");
				downloadTask = new DownloadTask(imageList, downloadPath);
				downloadTask.execute();
			} else if (getAction.equals(Constants.STOP_SERVICE)) {
				if (downloadTask != null) {
					downloadTask.cancel(true);
				}
				stopForeground(true);
				stopSelf();
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isServiceRunning = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class DownloadTask extends AsyncTask<Void, Long, Long[]> {
		private NotificationManager notificationManager;
		private NotificationCompat.Builder notification;

		private ArrayList<Image> imageList;
		private String downloadPath;
		private int numberOfFiles;
		
		public DownloadTask(ArrayList<Image> imageList, String downloadPath) {
			this.imageList = imageList;
			this.downloadPath = downloadPath;
			numberOfFiles = imageList.size();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			createNotificationChannel(notificationManager);
			notification = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_DOWNLOAD);
			notification.setSmallIcon(R.drawable.ic_download);
			notification.setContentTitle(getResources().getString(R.string.notification_downloading_images));
			notification.setProgress(numberOfFiles, 0, false);           
			notification.setContentText(getResources().getString(R.string.notification_preparing_download));
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
				notification.setPriority(NotificationCompat.PRIORITY_HIGH);
			}
			notification.setAutoCancel(false);
			Intent intentActivity = new Intent(DownloadService.this, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(DownloadService.this, 0, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setContentIntent(pendingIntent);
			Intent intentService = new Intent(DownloadService.this, DownloadService.class);
			intentService.setAction(Constants.STOP_SERVICE);
			PendingIntent stopPendingIntent = PendingIntent.getService(DownloadService.this, 1, intentService, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.addAction(R.drawable.ic_cancel, getResources().getString(R.string.button_cancel), stopPendingIntent);
			startForeground(NOTIFICATION_DOWNLOADING, notification.build());
		}

		@Override
		protected Long[] doInBackground(Void[] params) {
			long downloadErrors = 0;
			long downloadedSize = 0;
			long downloadedSizeTotal = 0;
			long downloaded = 0;
			for (Image image : imageList) {
				try {
					if (isCancelled()) {
						break;
					}
					FileUtils.downloadImage(downloadPath, image.getImageUrl(), image.getImageName());
					downloadedSize += new File(downloadPath + "/" + image.getImageName()).length();
					downloadedSizeTotal = downloadedSize;
					downloaded++;
				} catch (Exception e) {
					downloadErrors++;
				}
				publishProgress(downloaded, downloadedSize);
			}
			downloadedSizeTotal = downloadedSize;
			Long[] data = {downloadErrors, downloadedSizeTotal};
			return data;
		}

		@Override
		protected void onProgressUpdate(Long[] values) {
			super.onProgressUpdate(values);
			long downloadedProgress = values[0];
			long downloadedSize = values[1];
			int downloaded = (int) downloadedProgress;
			notification.setProgress(numberOfFiles, downloaded, false);           
			notification.setContentText(getResources().getString(R.string.notification_downloading) + " (" + downloaded + "/" + numberOfFiles + ") | (" + FileUtils.formatFileSize(downloadedSize) + ")");
			notificationManager.notify(NOTIFICATION_DOWNLOADING, notification.build());
		}

		@Override
		protected void onPostExecute(Long[] result) {
			super.onPostExecute(result);
			stopForeground(true);
			stopSelf();
			notificatonDownloadFinished(result[0], result[1]);
		}

		private void notificatonDownloadFinished(long downloadErrors, long downloadedSizeTotal) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			createNotificationChannel(notificationManager);
			NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_DOWNLOAD);
			notification.setSmallIcon(R.drawable.ic_download_completed);
			notification.setContentTitle(getResources().getString(R.string.notification_download_completed));
			// A prioridade (importância) é definida no construtor do canal de notificação no Android Oreo (API 26)
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
				notification.setPriority(NotificationCompat.PRIORITY_HIGH);
			}
			notification.setProgress(0, 0, false);
			notification.setAutoCancel(true);
			
			String downloadedSize = " | (" + FileUtils.formatFileSize(downloadedSizeTotal) + ")";
			NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
			if (downloadErrors == 0) {
				bigStyle.bigText(numberOfFiles + " " + getResources().getString(R.string.notification_downloaded_images) + downloadedSize);
			} else if (downloadErrors < numberOfFiles) {
				String text = (numberOfFiles - downloadErrors) + " " + getResources().getString(R.string.notification_downloaded_images) + downloadedSize;
				bigStyle.bigText(text);
				bigStyle.setSummaryText(downloadErrors + " " + (downloadErrors == 1 ? getResources().getString(R.string.notification_image_download_failed) : getResources().getString(R.string.notification_images_download_failed)));
			} else {
				bigStyle.bigText(getResources().getString(R.string.notification_all_downloads_failed));
			}

			notification.setStyle(bigStyle);
			Intent intentActivity = new Intent(DownloadService.this, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(DownloadService.this, 0, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setContentIntent(pendingIntent);
			notificationManager.notify(NOTIFICATION_DOWNLOAD_FINISHED, notification.build());
		}

		private void createNotificationChannel(NotificationManager notificationManager) {
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				int importance = NotificationManager.IMPORTANCE_HIGH;
				NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_DOWNLOAD, getResources().getString(R.string.notification_channel_image_download), importance);
				channel.setDescription(getResources().getString(R.string.notification_channel_image_download_description));
				notificationManager.createNotificationChannel(channel);
			}
		}

	}

}
