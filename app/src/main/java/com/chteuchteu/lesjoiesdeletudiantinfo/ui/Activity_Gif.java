package com.chteuchteu.lesjoiesdeletudiantinfo.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.async.GifDownloader;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;

import java.io.File;

public class Activity_Gif extends GifActivity {
	public int          pos;
	public Gif          gif;
	private GifDownloader gifDownloader;
	public WebView      webView;
	public boolean		textsShown;

	public float		deltaY;

	public int			actionBarColor = Color.argb(200, 6, 124, 64);
	
	public int			SWITCH_NEXT = 1;
	public int			SWITCH_PREVIOUS = 0;

	public ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_gif);

		int contentPaddingTop = 0;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle(" Les Joies de l'Etudiant Info");
		actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
		final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		contentPaddingTop += (int) styledAttributes.getDimension(0, 0);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				LinearLayout notifBarBG = (LinearLayout) findViewById(R.id.kitkat_actionbar_notifs);
				notifBarBG.setBackgroundColor(actionBarColor);
				notifBarBG.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Util.getStatusBarHeight(this)));
				notifBarBG.setVisibility(View.VISIBLE);
				contentPaddingTop += Util.getStatusBarHeight(this);
			}
		}
		if (contentPaddingTop != 0) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, contentPaddingTop, 0, 0);
			findViewById(R.id.actions_container).setLayoutParams(lp);
		}

		Intent thisIntent = getIntent();
		pos = 0;
		if (thisIntent != null && thisIntent.getExtras() != null
				&& thisIntent.getExtras().containsKey("pos"))
			pos = thisIntent.getExtras().getInt("pos");

		gif = gifFoo.getGifs().get(pos);

		textsShown = true;

		TextView header_nom = (TextView) findViewById(R.id.header_nom);
		header_nom.setText(gif.getName());
		if (header_nom.getText().toString().length() / 32 > 4) // nb lines
			header_nom.setLineSpacing(-10, 1);
		else if (header_nom.getText().toString().length() / 32 > 6)
			header_nom.setLineSpacing(-25, 1);

		webView = (WebView) findViewById(R.id.wv);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setVerticalScrollBarEnabled(false);
		webView.setVerticalFadingEdgeEnabled(false);
		webView.setHorizontalFadingEdgeEnabled(false);
		webView.setBackgroundColor(0x00000000);

		int marginTop = 0;
		marginTop += Util.getActionBarHeight(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			marginTop += Util.getActionBarHeight(this) / 2;
		marginTop += 35; // Actions
		findViewById(R.id.wv_container).setPadding(0, marginTop, 0, 0);
		
		TextView gif_precedent = (TextView) findViewById(R.id.gif_precedent);
		TextView gif_suivant = (TextView) findViewById(R.id.gif_suivant);
		if (pos == 0)
			gif_precedent.setVisibility(View.GONE);
		if (pos == gifFoo.getGifs().size()-1)
			gif_suivant.setVisibility(View.GONE);
		gif_precedent.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { switchGif(SWITCH_PREVIOUS); } });
		gif_suivant.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { switchGif(SWITCH_NEXT); } });
		
		findViewById(R.id.actions_container).post(new Runnable(){
			public void run() {
				deltaY = findViewById(R.id.actions_container).getHeight()/2;
			}
		});
		findViewById(R.id.onclick_catcher).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { toggleTexts(); } });
		
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.header_nom), Util.Fonts.CustomFont.RobotoCondensed_Light);
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.gif_precedent), Util.Fonts.CustomFont.RobotoCondensed_Light);
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.gif_suivant), Util.Fonts.CustomFont.RobotoCondensed_Light);
	}
	
	@Override
	protected void onPause() {
		// Another activity comes into the foreground
		super.onPause();
		
		stopThread();
	}
	
	private void loadGif() {
		File photo = new File(Util.getEntiereFileName(gif, false));
		stopThread();
		webView.setVisibility(View.GONE);
		if (!photo.exists()) {
			gifDownloader = new GifDownloader(this, gif);
			gifDownloader.execute();
		} else {
			if (gif.getState() != Gif.ST_COMPLETE)
				gif.setState(Gif.ST_COMPLETE);
			String imagePath = Util.getEntiereFileName(gif, true);
			webView.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html", "utf-8", "");

			if (pos == 0)	findViewById(R.id.gif_precedent).setVisibility(View.GONE);
			else			findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
			if (pos == gifFoo.getGifs().size()-1)		findViewById(R.id.gif_suivant).setVisibility(View.GONE);
			else			findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);

			((TextView) findViewById(R.id.header_nom)).setText(gif.getName());

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
		}
	}
	
	private void switchGif(int which) {
		if (!textsShown) {
			toggleTexts();
			return;
		}
		if (gif != null) {
			stopThread();
			int targetPos = which == SWITCH_NEXT ? pos + 1 : pos - 1;
			
			if (targetPos >= 0 && targetPos < gifFoo.getGifs().size()-1) {
				gif = gifFoo.getGifs().get(targetPos);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					updateSharingIntent();

				if (webView.getVisibility() == View.VISIBLE) {
					AlphaAnimation an = new AlphaAnimation(1.0f, 0.0f);
					an.setDuration(150);
					an.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override
						public void onAnimationEnd(Animation animation) {
							webView.setVisibility(View.GONE);
						}
					});
					webView.startAnimation(an);
				}
				
				loadGif();
				
				pos = targetPos;

				if (targetPos == 0)	findViewById(R.id.gif_precedent).setVisibility(View.GONE);
				else			findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
				if (targetPos == gifFoo.getGifs().size()-1)		findViewById(R.id.gif_suivant).setVisibility(View.GONE);
				else			findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.header_nom)).setText(gif.getName());
			}
		}
	}
	
	private void stopThread() {
		if (gifDownloader != null) {
			boolean isDownloading = gifDownloader.isDownloading();
			gifDownloader.cancel(true);
			if (isDownloading) {
				File photo = new File(Util.getEntiereFileName(gif, false));
				if (photo.exists())
					photo.delete();
			}
		}
	}
	
	private void toggleTexts() {
		TextView title = (TextView) findViewById(R.id.header_nom);
		RelativeLayout actions = (RelativeLayout) findViewById(R.id.actions_container);
		LinearLayout titleContainer = (LinearLayout) findViewById(R.id.header_nom_container);
		
		AlphaAnimation a;
		if (textsShown)
			a = new AlphaAnimation(1.0f, 0.0f);
		else
			a = new AlphaAnimation(0.0f, 1.0f);
		a.setDuration(250);
		a.setFillEnabled(true);
		a.setFillAfter(true);
		title.startAnimation(a);
		actions.startAnimation(a);
		titleContainer.startAnimation(a);
		
		// Put the gif a little bit higher
		deltaY = findViewById(R.id.actions_container).getHeight()/2;
		if (textsShown)
			deltaY = -deltaY;
		
		TranslateAnimation anim = new TranslateAnimation(0, 0, 0, deltaY);
		anim.setDuration(250);
		anim.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.wv_container);
				RelativeLayout act = (RelativeLayout) findViewById(R.id.actions_container);
				if (textsShown)
					ll.layout(ll.getLeft(), ll.getTop()+act.getHeight()/2, ll.getRight(), ll.getBottom());
				else
					ll.layout(ll.getLeft(), ll.getTop()-act.getHeight()/2, ll.getRight(), ll.getBottom());
			}
		});
		anim.setFillEnabled(true);
		anim.setFillAfter(false);
		anim.setFillBefore(false);
		findViewById(R.id.wv_container).startAnimation(anim);
		
		textsShown = !textsShown;
	}
	
	@Override
	public void onBackPressed() {
		//stopThread();
		finish();
		Util.setTransition(this, "leftToRight");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Util.removeUncompleteGifs(this, gifFoo.getGifs());
	}
	
	
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gifs, menu);
		
		MenuItem item = menu.findItem(R.id.menu_share);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mShareActionProvider = (ShareActionProvider) item.getActionProvider();
			updateSharingIntent();
		}
		else
			item.setVisible(false);
		
		return true;
	}
	
	@SuppressLint("NewApi")
	private void updateSharingIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Les Joies du Sysadmin");
		String shareText = gif.getName() + " : " + gif.getArticleUrl();
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
		mShareActionProvider.setShareIntent(sharingIntent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				stopThread();
				startActivity(new Intent(Activity_Gif.this, Activity_Main.class));
				Util.setTransition(this, "leftToRight");
				return true;
			case R.id.menu_refresh:
				stopThread();
				
				gifDownloader = new GifDownloader(this, gif);
				
				AlphaAnimation an = new AlphaAnimation(1.0f, 0.0f);
				an.setDuration(150);
				an.setFillEnabled(true);
				an.setFillAfter(true);
				an.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationRepeat(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						findViewById(R.id.pb).setVisibility(View.VISIBLE);
					}
				});
				webView.startAnimation(an);

				gifDownloader.execute();
				return true;
			case R.id.menu_openwebsite:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gif.getArticleUrl()));
				startActivity(browserIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
