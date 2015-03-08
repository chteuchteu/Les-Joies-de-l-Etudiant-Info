package com.chteuchteu.lesjoiesdeletudiantinfo.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;
import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.RSSReader;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;
import com.chteuchteu.lesjoiesdeletudiantinfo.ui.Activity_Main;
import com.chteuchteu.lesjoiesdeletudiantinfo.ui.IActivity_Main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedParser extends AsyncTask<Void, Integer, Void> {
	private boolean needsUpdate;
	private Activity activity;
	private IActivity_Main iActivity;
	private ProgressBar progressBar;

	public FeedParser(Activity_Main activity) {
		this.needsUpdate = false;
		this.activity = activity;
		this.iActivity = activity;
		this.progressBar = (ProgressBar) activity.findViewById(R.id.pb);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressBar.setIndeterminate(true);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);

		if (activity.findViewById(R.id.first_disclaimer).getVisibility() == View.VISIBLE)
			((TextView) activity.findViewById(R.id.ascii_loading)).setText(progress[0] + "%");
		else {
			if (progressBar.getVisibility() == View.GONE)
				progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(progress[0]);
			progressBar.setIndeterminate(false);
		}
	}

	public void manualPublishProgress(int n) {
		publishProgress(n);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		List<Gif> newGifs = RSSReader.parse(GifFoo.RSS_URL, this);

		if (newGifs.size() == 0)
			return null;

		List<Gif> currentGifs = GifFoo.getInstance().getGifs();
		if (currentGifs.size() == 0 || currentGifs.size() != newGifs.size() || currentGifs.size() > 0 && !currentGifs.get(0).equals(newGifs.get(0))) {
			needsUpdate = true;

			List<Gif> gifs = new ArrayList<>();
			for (int i=newGifs.size()-1; i>=0; i--)
				gifs.add(0, newGifs.get(i));

			GifFoo.getInstance().setGifs(gifs);

			Util.saveGifs(activity, gifs);
		}

		return null;
	}
	@SuppressLint("NewApi")
	@Override
	protected void onPostExecute(Void result) {
		progressBar.setVisibility(View.GONE);

		if (needsUpdate) {
			iActivity.refreshListView();

			Util.saveLastViewed(activity);
		}
		Util.setPref(activity, "lastGifsListUpdate", Util.dateToString(new Date()));
	}
}
