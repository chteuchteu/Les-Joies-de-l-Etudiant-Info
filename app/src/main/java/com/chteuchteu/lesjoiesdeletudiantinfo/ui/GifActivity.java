package com.chteuchteu.lesjoiesdeletudiantinfo.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;
import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.crashlytics.android.Crashlytics;

public class GifActivity extends ActionBarActivity {
	protected GifFoo gifFoo;
	protected Activity activity;
	protected Context context;
	protected Toolbar toolbar;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		gifFoo = GifFoo.getInstance(this);
		activity = this;
		context = this;
	}

	protected void onContentViewSet() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}
}
