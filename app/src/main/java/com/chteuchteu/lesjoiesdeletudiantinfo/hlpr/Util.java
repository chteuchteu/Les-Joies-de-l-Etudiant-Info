package com.chteuchteu.lesjoiesdeletudiantinfo.hlpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;
import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Util {
	public static String sdFolderName = "lesJoiesdelEtudiantInfo";

	public static List<Gif> getGifs(Context context) {
		String[] sg = Util.getPref(context, "gifs").split(";;");
		List<Gif> list = new ArrayList<>();
		for (String s : sg) {
			Gif g = new Gif();
			if (s.split("::").length > 0)	g.setName(s.split("::")[0]);
			if (s.split("::").length > 1)	g.setArticleUrl(s.split("::")[1]);
			if (s.split("::").length > 2)	g.setGifUrl(s.split("::")[2]);
			if (s.split("::").length > 3)	g.setDate(s.split("::")[3]);
			list.add(g);
		}
		return list;
	}
	
	public static void saveGifs(Context c, List<Gif> gifs) {
		String str = "";
		int i=0;
		for (Gif g : gifs) {
			str += g.getName() + "::" + g.getArticleUrl() + "::" + g.getGifUrl() + "::" + g.getDate();
			if (i != gifs.size()-1)
				str += ";;";
			i++;
		}
		setPref(c, "gifs", str);
	}
	
	public static int[] getCountdownFromSeconds(int seconds) {
		int hours = seconds / 3600;
		int remainder = seconds - hours * 3600;
		int mins = remainder / 60;
		remainder = remainder - mins * 60;
		int secs = remainder;
		
		return new int[]{hours , mins , secs};
	}
	
	public static String formatNumber(int number) {
		if (number < 10)
			return "0" + number;
		return "" + number;
	}
	
	public static String getFileName(Gif g) {
		if (g == null || g.getArticleUrl() == null || g.getArticleUrl().equals(""))
			return "";
		return g.getArticleUrl().substring(g.getArticleUrl().lastIndexOf('/'));
	}
	
	public static String getEntiereFileName(Gif g, boolean withFilePrefix) {
		String path = "";
		if (withFilePrefix)
			path += "file://";
		path += Environment.getExternalStorageDirectory().getPath() + "/" + sdFolderName + Util.getFileName(g) + ".gif";
		return path;
	}
	
	public static boolean removeUncompleteGifs(Context context, List<Gif> l) {
		boolean needSave = false;
		for (Gif g : l) {
			if (g.getState() == Gif.ST_DOWNLOADING) {
				File f = new File(Util.getEntiereFileName(g, false));
				if (f.exists())
					f.delete();
				g.setState(Gif.ST_EMPTY);
				needSave = true;
			}
		}
		if (needSave)
			saveGifs(context, l);
		return needSave;
	}
	
	public static void clearCache(Context c) {
		String path = Environment.getExternalStorageDirectory().toString() + "/" + sdFolderName +"/";
		File dir = new File(path);
		File files[] = dir.listFiles();
		int crt = 0;
		if (files != null) {
			for (File f : files) {
				f.delete();
				crt++;
			}
		}
		String txt;
		if (crt == 0)
			txt = c.getText(R.string.cache_emptied_none).toString();
		else if (crt == 1)
			txt = c.getText(R.string.cache_emptied_sing).toString();
		else
			txt = c.getText(R.string.cache_emptied_plur).toString().replaceAll("#", crt + "");
		
		Toast.makeText(c, txt, Toast.LENGTH_SHORT).show();
	}
	
	public static void removeOldGifs(List<Gif> l) {
		if (l != null && l.size() > 10) {
			String path = Environment.getExternalStorageDirectory().toString() + "/" + sdFolderName + "/";
			File dir = new File(path);
			File files[] = dir.listFiles();
			if (files != null) {
				List<File> toBeDeleted = new ArrayList<File>();
				for (File f : files) {
					boolean shouldBeDeleted = true;
					int max = 15;
					if (l.size() < 15)	max = l.size();
					for (int i=0; i<max; i++) {
						String fileName = getFileName(l.get(i)).replaceAll("/", "");
						if (f.getName().contains(fileName)) {
							shouldBeDeleted = false; break;
						}
					}
					if (shouldBeDeleted)
						toBeDeleted.add(f);
				}
				for (File f : toBeDeleted)
					f.delete();
			}
		}
	}
	
	public static void createLJDSYDirectory() {
		File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + sdFolderName + "/");
		if (!dir.exists())
			dir.mkdirs();
	}
	
	public static String getHtml(String gifPath) {
		String css = "html, body, #wrapper {height:100%;width: 100%;margin: 0;padding: 0;border: 0;} #wrapper td {vertical-align: middle;text-align: center;} .container{width:100%;height:100%;background-image:url('" + gifPath +"'); background-size:contain; background-repeat:no-repeat;background-position:center;}";
		//String js = "function resizeToMax(id){myImage = new Image();var img = document.getElementById(id);myImage.src = img.src;if(myImage.width / document.body.clientWidth > myImage.height / document.body.clientHeight){img.style.width = \"100%\"; } else {img.style.height = \"100%\";}}";
		//String html = "<html><head><script>" + js + "</script><style>" + css + "</style></head><body><table id=\"wrapper\"><tr><td><img id=\"gif\" src=\""+ imagePath + "\" onload=\"resizeToMax(this.id)\" /></td></tr></table></body></html>";
		return "<html><head><style>" + css + "</style></head><body><div class=\"container\"></div></body></html>";
	}
	
	public static String getPref(Context c, String key) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getString(key, "");
	}
	
	public static void setPref(Context c, String key, String value) {
		if (value.equals(""))
			removePref(c, key);
		else {
			SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(key, value);
			editor.apply();
		}
	}
	
	public static void removePref(Context c, String key) {
		SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.apply();
	}
	
	@SuppressLint("SimpleDateFormat")
	public static Date stringToDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String dateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return formatter.format(date);
	}
	
	public static long getSecsDiff(Date first, Date latest) {
		return (latest.getTime() - first.getTime()) / 1000;
	}

	public static final class Fonts {
		/* ENUM Custom Fonts */
		public enum CustomFont {
			Roboto_Regular("Roboto-Regular.ttf"),
			Roboto_Light("Roboto-Light.ttf");

			final String file;
			private CustomFont(String fileName) { this.file = fileName; }
			public String getValue() { return this.file; }
		}

		/* Fonts */
		public static void setFont(Context c, ViewGroup g, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			setFont(g, mFont);
		}

		public static void setFont(Context c, TextView t, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			t.setTypeface(mFont);
		}

		private static void setFont(ViewGroup group, Typeface font) {
			int count = group.getChildCount();
			View v;
			for (int i = 0; i < count; i++) {
				v = group.getChildAt(i);
				if (v instanceof TextView)
					((TextView) v).setTypeface(font);
				else if (v instanceof ViewGroup)
					setFont((ViewGroup) v, font);
			}
		}
	}

	public static void setTransition(Activity activity, String level) {
		if (level.equals("rightToLeft"))
			activity.overridePendingTransition(R.anim.deeper_in, R.anim.deeper_out);
		else if (level.equals("leftToRight"))
			activity.overridePendingTransition(R.anim.shallower_in, R.anim.shallower_out);
	}

	public static void saveLastViewed(Context context) {
		if (GifFoo.getInstance().getFirstGif() != null)
			Util.setPref(context, "lastViewed", GifFoo.getInstance().getFirstGif().getArticleUrl());
	}
}
