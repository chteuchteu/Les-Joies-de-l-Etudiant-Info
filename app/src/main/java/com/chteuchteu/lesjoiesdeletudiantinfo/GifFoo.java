package com.chteuchteu.lesjoiesdeletudiantinfo;

import android.content.Context;

import com.chteuchteu.gifapplicationlibrary.i.IDataSourceParser;
import com.chteuchteu.gifapplicationlibrary.obj.Gif;
import com.chteuchteu.gifapplicationlibrary.obj.GifApplicationBundle;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.RSSReader;
import com.chteuchteu.lesjoiesdeletudiantinfo.serv.NotificationService;
import com.chteuchteu.lesjoiesdeletudiantinfo.ui.Activity_Main;

import java.util.List;

public class GifFoo {
	public static GifApplicationBundle getApplicationBundle(Context context) {
		return new GifApplicationBundle(
				context.getString(R.string.app_name),
				"http://lesjoiesdeletudiantinfo.com/feed/",
				new IDataSourceParser() {
					@Override
					public List<Gif> parseDataSource(String dataSourceUrl) {
						return RSSReader.parse(dataSourceUrl, null);
					}
				},
				"lesJoiesdelEtudiantInfo",
				context.getString(R.string.about),
				Activity_Main.class,
                NotificationService.class
		);
	}
}
