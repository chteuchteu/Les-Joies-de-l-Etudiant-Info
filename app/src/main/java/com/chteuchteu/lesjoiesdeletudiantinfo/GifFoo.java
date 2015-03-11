package com.chteuchteu.lesjoiesdeletudiantinfo;

import android.content.Context;

import com.chteuchteu.gifapplicationlibrary.i.IDataSourceParser;
import com.chteuchteu.gifapplicationlibrary.obj.Gif;
import com.chteuchteu.gifapplicationlibrary.obj.GifApplicationBundle;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.RSSReader;

import java.util.List;

public class GifFoo {
	public static GifApplicationBundle getApplicationBundle(Context context) {
		return new GifApplicationBundle(
				"http://lesjoiesdeletudiantinfo.com/feed/",
				new IDataSourceParser() {
					@Override
					public List<Gif> parseDataSource(String dataSourceUrl) {
						return RSSReader.parse(dataSourceUrl, null);
					}
				},
				"lesJoiesdelEtudiantInfo",
				context.getString(R.string.about)
		);
	}
}
