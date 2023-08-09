package com.kproject.imageloader.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.webkit.URLUtil;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.FileUtils;
import com.kproject.imageloader.utils.Utils;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LoadPageTaskFragment extends Fragment {
	private LoadPageTask task;
	private TaskCallbacks taskCallbacks;
	private LinkedHashSet<Image> imageLinkedHashSet = new LinkedHashSet<>();
	
	public LoadPageTaskFragment() {}

	public static LoadPageTaskFragment newInstance(String pageUrlOrQuery) {
		Bundle bundle = new Bundle();
		bundle.putString("pageUrlOrQuery", pageUrlOrQuery);
		LoadPageTaskFragment fragment = new LoadPageTaskFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public interface TaskCallbacks {
		void onPreExecute(String pageUrlOrQuery);
		void onPostExecute(List<Image> imageList);
		void onPageTitleObtained(String pageTitle);
		void onCancelled(boolean isSocketTimeoutException);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		taskCallbacks = (TaskCallbacks) context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		String pageUrlOrQuery = getArguments().getString("pageUrlOrQuery");
		task = new LoadPageTask(pageUrlOrQuery);
		task.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		taskCallbacks = null;
	}
	
	private class LoadPageTask extends AsyncTask<String, Integer, List<Image>> {
		private String pageUrlOrQuery;
		private String pageTitle;
		private boolean isPageUrl;
		private boolean isSocketTimeoutException = false;

		public LoadPageTask(String pageUrlOrQuery) {
			this.pageUrlOrQuery = pageUrlOrQuery;
			this.isPageUrl = URLUtil.isValidUrl(pageUrlOrQuery);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (taskCallbacks != null) {
				taskCallbacks.onPreExecute(pageUrlOrQuery);
			}
		}

		@Override
		protected List<Image> doInBackground(String[] params) {
			try {
				int requestTimeout = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_REQUEST_TIMEOUT, "15000"));
				String userAgent = Utils.getUserAgent();
				if (isPageUrl) {
					if (pageUrlOrQuery.startsWith("https://instagram.com") || pageUrlOrQuery.startsWith("https://www.instagram.com")) {
						return loadInstagramImage(pageUrlOrQuery, userAgent, requestTimeout);
					} else {
						return loadPage(pageUrlOrQuery, userAgent, requestTimeout);
					}
				} else {
					return searchImages(pageUrlOrQuery, userAgent, requestTimeout);
				}
			} catch (SocketTimeoutException e) {
				isSocketTimeoutException = true;
				cancel(true);
			} catch (final Exception e) {
				cancel(true);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Image> result) {
			super.onPostExecute(result);
			if (taskCallbacks != null) {
				taskCallbacks.onPageTitleObtained(pageTitle);
				taskCallbacks.onPostExecute(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (taskCallbacks != null) {
				taskCallbacks.onCancelled(isSocketTimeoutException);
			}
		}

		private List<Image> loadPage(String pageUrl, String userAgent, int requestTimeout) throws IOException {
			Document doc = null;
			if (userAgent.equals("OFF")) {
				doc = Jsoup.connect(pageUrl).timeout(requestTimeout).get();
			} else {
				doc = Jsoup.connect(pageUrl).userAgent(userAgent).timeout(requestTimeout).get();
			}
			Elements media = doc.getElementsByTag("img");
			for (Element src : media) {
				String imageUrl = src.absUrl("src");
				String imageName = URLUtil.guessFileName(imageUrl, null, null);
				if (!imageUrl.isEmpty()) {
					imageLinkedHashSet.add(new Image(0, imageUrl, imageName, pageUrl));
				}
				if (src.absUrl("data-src") != null && !src.absUrl("data-src").isEmpty()) {
					imageUrl = src.absUrl("data-src");
					imageName = URLUtil.guessFileName(imageUrl, null, null);
					if (!imageUrl.isEmpty()) {
						imageLinkedHashSet.add(new Image(0, imageUrl, imageName, pageUrl));
					}
				}
			}
			List<Image> imageList = new ArrayList<>();
			imageList.addAll(imageLinkedHashSet);
			pageTitle = doc.title();
			return imageList;
		}

		private List<Image> searchImages(String query, String userAgent, int requestTimeout) throws IOException {
			String url = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8") + "&source=lnms&tbm=isch";
			Document doc = null;
			if (userAgent.equals("OFF")) {
				doc = Jsoup.connect(url).timeout(requestTimeout).get();
			} else {
				doc = Jsoup.connect(url).userAgent(userAgent).timeout(requestTimeout).get();
			}
			String[] content = doc.toString().split("\\[1,\\[0,\"");
			for (String str : content) {
				try {
					String imageUrl = "http" + str.split("http")[2].split("\"")[0];
					if (imageUrl.equals("https://www.google.com/")) {
						continue;
					}
					String imagePage = "http" + str.split("http")[3].split("\"")[0];
					if (imageUrl.contains("\\")) {
						// Corrige alguns links que vêm bugados
						Properties properties = new Properties();
						properties.load(new StringReader("key = " + imageUrl));
						imageUrl = properties.getProperty("key");
					}
					String imageName = URLUtil.guessFileName(imageUrl, null, null);
					String imageFormat = imageName.substring(imageName.lastIndexOf("."), imageName.length());
					// Ignora imagens com formato .BIN
					if (imageFormat.contains(".bin")) {
						continue;
					}
					imageLinkedHashSet.add(new Image(0, imageUrl, imageName, imagePage));
				} catch (Exception e) {}
			}
			List<Image> imageList = new ArrayList<>();
			imageList.addAll(imageLinkedHashSet);
			pageTitle = query;
			return imageList;
		}
		
		private List<Image> loadInstagramImage(String pageUrl, String userAgent, int requestTimeout) throws IOException {
			Document doc = null;
			if (userAgent.equals("OFF")) {
				doc = Jsoup.connect(pageUrl).timeout(requestTimeout).get();
			} else {
				doc = Jsoup.connect(pageUrl).userAgent(userAgent).timeout(requestTimeout).get();
			}
			String[] pageContent = doc.toString().split("src\":");
			for (String line : pageContent) {
				if (line.startsWith("\"https://")) {
					String imageUrl = line.substring(1, line.indexOf("\",")).replace("\\u0026", "&");
					String imageName = URLUtil.guessFileName(imageUrl, null, null);
					imageLinkedHashSet.add(new Image(0, imageUrl, imageName, pageUrl));
					
				}
			}
			List<Image> imageList = new ArrayList<>();
			imageList.addAll(imageLinkedHashSet);
			pageTitle = doc.title();
			return imageList;
		}
		
	}

}
