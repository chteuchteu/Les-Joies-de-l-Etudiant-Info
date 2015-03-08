package com.chteuchteu.lesjoiesdeletudiantinfo;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;

import java.util.ArrayList;
import java.util.List;

public class GifFoo {
	public static final String RSS_URL = "http://lesjoiesdeletudiantinfo.com/feed/";
	public static final String COUNTDOWN_WS = "http://lesjoiesdeletudiantinfo.com/wp-admin/admin-ajax.php?action=getDeltaNext";

	private List<Gif> gifs;
	private static GifFoo instance;
	private Context context;

	private CountDownTimer countDownTimer;

	private GifFoo(Context context) {
		this.context = context;
		this.gifs = new ArrayList<>();
		// Create gifs directory if needed
		Util.createLJDSYDirectory();
		loadGifsFromCache();
	}

	public static synchronized GifFoo getInstance(Context context) {
		if (instance == null)
			instance = new GifFoo(context);
		return instance;
	}

	public static synchronized GifFoo getInstance() {
		return instance;
	}

	private void loadGifsFromCache() {
		if (Util.getPref(context, "gifs").equals(""))
			return;

		this.gifs.clear();
		List<Gif> gifs = Util.getGifs(context);
		if (Util.removeUncompleteGifs(context, gifs))
			gifs = Util.getGifs(context);

		Util.removeOldGifs(gifs);

		this.gifs.addAll(gifs);

	}
	public void setGifs(List<Gif> gifs) {
		this.gifs.clear();
		this.gifs.addAll(gifs);
	}

	public List<Gif> getGifs() { return this.gifs; }
	public Gif getFirstGif() { return this.gifs.size() > 0 ? this.gifs.get(0) : null; }

	public void setCountDownTimer(CountDownTimer val) { this.countDownTimer = val; }
	public CountDownTimer getCountDownTimer() { return this.countDownTimer; }

	public static void log(String s) {
		if (BuildConfig.DEBUG)
			Log.d("LJDLI", s);
	}
}
