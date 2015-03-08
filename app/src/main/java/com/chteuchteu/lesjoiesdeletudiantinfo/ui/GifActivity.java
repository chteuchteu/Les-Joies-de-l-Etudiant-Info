package com.chteuchteu.lesjoiesdeletudiantinfo.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;

public class GifActivity extends Activity {
	protected GifFoo gifFoo;
	protected Activity activity;
	protected Context context;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gifFoo = GifFoo.getInstance(this);
		activity = this;
		context = this;
	}
}
