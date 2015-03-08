package com.chteuchteu.lesjoiesdeletudiantinfo.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;
import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GifDownloader extends AsyncTask<Void, Integer, Void> {
	private Activity activity;
	private Gif gif;

	private File photo;
	private ProgressBar progressBar;
	private WebView webView;

	private boolean isDownloading;

	public GifDownloader(Activity activity, Gif gif) {
		this.activity = activity;
		this.gif = gif;
		this.isDownloading = false;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressBar = (ProgressBar) activity.findViewById(R.id.pb);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setIndeterminate(true);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		webView = (WebView) activity.findViewById(R.id.wv);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		progressBar.setProgress(progress[0]);
		progressBar.setIndeterminate(false);
	}

	@SuppressWarnings("resource")
	@Override
	protected Void doInBackground(Void... arg0) {
		int fileLength;
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			isDownloading = true;
			photo = new File(Util.getEntiereFileName(gif, false));
			URL url = new URL(gif.urlGif);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;

			fileLength = connection.getContentLength();

			input = connection.getInputStream();
			output = new FileOutputStream(Util.getEntiereFileName(gif, false));

			byte data[] = new byte[4096];
			long total = 0;
			int count;

			gif.state = Gif.ST_DOWNLOADING;

			while ((count = input.read(data)) != -1) {
				if (isCancelled()) {
					input.close();
					if (photo.exists())
						photo.delete();
					return null;
				}
				total += count;
				publishProgress((int)((total*100)/fileLength));
				output.write(data, 0, count);
			}
			publishProgress(100);
			isDownloading = false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) { }

			if (connection != null)
				connection.disconnect();
		}

		return null;
	}

	public boolean isDownloading() { return this.isDownloading; }

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onPostExecute(Void result) {
		progressBar.setVisibility(View.GONE);


		if (photo != null && photo.exists()) {
			try {
				gif.state = Gif.ST_COMPLETE;

				webView.setVisibility(View.GONE);
				String imagePath = Util.getEntiereFileName(gif, true);
				webView.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html","utf-8", "");

				webView.setWebViewClient(new WebViewClient() {
					public void onPageFinished(WebView v, String u) {
						webView.setVisibility(View.VISIBLE);
						AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
						a.setStartOffset(250);
						a.setDuration(350);
						a.setFillEnabled(true);
						a.setFillAfter(true);
						webView.startAnimation(a);
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
				onDownloadError();
			}
		} else
			onDownloadError();
	}

	private void onDownloadError() {
		Toast.makeText(activity, R.string.download_error, Toast.LENGTH_SHORT).show();
		gif.state = Gif.ST_DOWNLOADING;
		Util.removeUncompleteGifs(activity, GifFoo.getInstance().getGifs());
		progressBar.setVisibility(View.GONE);
	}
}
