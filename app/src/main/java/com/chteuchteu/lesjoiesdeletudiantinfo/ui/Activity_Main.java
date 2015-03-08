package com.chteuchteu.lesjoiesdeletudiantinfo.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.async.CountdownLauncher;
import com.chteuchteu.lesjoiesdeletudiantinfo.async.FeedParser;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;
import com.chteuchteu.lesjoiesdeletudiantinfo.serv.NotificationService;
import com.tjeannin.apprate.AppRate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Activity_Main extends GifActivity implements IActivity_Main {
	private ArrayList<HashMap<String, String>> list;
	private MenuItem	menu_notifs;
	private int			actionBarColor = Color.argb(200, 6, 124, 64);
	private boolean	    notifsEnabled;
	public static int 	scrollY;
	private ListView 	lv_gifs;
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_main);
		
		lv_gifs = (ListView) findViewById(R.id.list);
		list = new ArrayList<>();
		
		int contentPaddingTop = 0;
		int contentPaddingBottom = 0;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle(" Les Joies de l'Etudiant Info");
		actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
		final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		contentPaddingTop += (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();

		LinearLayout countdown_container = (LinearLayout) findViewById(R.id.countdown_container);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				contentPaddingTop += Util.getStatusBarHeight(this);
				contentPaddingBottom = 150;
			}
		}
		else
			findViewById(R.id.kitkat_actionbar_notifs).setVisibility(View.GONE);
		if (contentPaddingTop != 0) {
			((RelativeLayout.LayoutParams) countdown_container.getLayoutParams()).setMargins(0, contentPaddingTop, 0, 0);
			lv_gifs.setClipToPadding(false);
			lv_gifs.setPadding(0, contentPaddingTop, 0, contentPaddingBottom);
		}

		
		if (Util.getPref(this, "first_disclaimer").equals("")) {
			Util.setPref(this, "first_disclaimer", "true");
			final LinearLayout l = (LinearLayout) findViewById(R.id.first_disclaimer);
			l.setVisibility(View.VISIBLE);
			findViewById(R.id.disclaimer_valider).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (notifsEnabled)
						enableNotifs();
					else
						disableNotifs();
					AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
					a.setDuration(300);
					a.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) {
							l.setVisibility(View.GONE);
							l.setOnClickListener(null);
						}
					});
					l.startAnimation(a);
				}
			});
			final TextView tv1 = (TextView) findViewById(R.id.disclaimer_notifs);
			
			notifsEnabled = true;
			tv1.setText("[X] " + getText(R.string.first_disclaimer_notifs));
			
			tv1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (notifsEnabled)
						tv1.setText("[   ] " + getText(R.string.first_disclaimer_notifs));
					else
						tv1.setText("[X] " + getText(R.string.first_disclaimer_notifs));
					notifsEnabled = !notifsEnabled;
				}
			});
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(getString(R.string.vote_title))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getString(R.string.vote))
			.setPositiveButton(getString(R.string.vote_yes), null)
			.setNegativeButton(getString(R.string.vote_no), null)
			.setNeutralButton(getString(R.string.vote_notnow), null);
		new AppRate(this)
			.setCustomDialog(builder)
			.setMinDaysUntilPrompt(5)
			.setMinLaunchesUntilPrompt(5)
			.init();
		
		lv_gifs.post(new Runnable() {
			@Override
			public void run() {
				if (scrollY != 0)
					lv_gifs.setSelectionFromTop(scrollY, 0);
			}
		});
		
		launchUpdateIfNeeded();
		
		// Countdown
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.countdown_txt), Util.Fonts.CustomFont.RobotoCondensed_Regular);
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.countdown_hh), Util.Fonts.CustomFont.RobotoCondensed_Regular);
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.countdown_mm), Util.Fonts.CustomFont.RobotoCondensed_Regular);
		Util.Fonts.setFont(this, (TextView) findViewById(R.id.countdown_ss), Util.Fonts.CustomFont.RobotoCondensed_Regular);
		
		launchCountDownTimer();
	}

	private void launchCountDownTimer() {
		if (gifFoo.getCountDownTimer() == null)
			new CountdownLauncher(this).execute();
	}

	@Override
	public void refreshListView() {
		ListView l = (ListView) findViewById(R.id.list);
		list.clear();

		for (Gif g : gifFoo.getGifs()) {
			if (g.isValid()) {
				HashMap<String,String> item = new HashMap<>();
				item.put("line1", g.nom);
				item.put("line2", g.date);
				list.add(item);
			}
		}
		SimpleAdapter sa = new SimpleAdapter(Activity_Main.this, list, R.layout.list_item, new String[] { "line1","line2" }, new int[] {R.id.line_a, R.id.line_b});
		l.setAdapter(sa);

		l.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
				itemClick(position);
			}
		});
		Util.saveLastViewed(this);
	}
	
	private void enableNotifs() {
		Util.setPref(this, "notifs", "true");
		
		int minutes = 180;
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(Activity_Main.this, NotificationService.class);
		PendingIntent pi = PendingIntent.getService(Activity_Main.this, 0, i, 0);
		am.cancel(pi);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + minutes*60*1000, minutes*60*1000, pi);
		if (menu_notifs != null)
			menu_notifs.setChecked(true);
		try {
			pi.send();
		} catch (CanceledException e) {	e.printStackTrace(); }
	}
	
	private void disableNotifs() {
		Util.setPref(this, "notifs", "false");
		if (menu_notifs != null)
			menu_notifs.setChecked(false);
	}
	
	@Override
	public void onBackPressed() {
		final LinearLayout l = (LinearLayout) findViewById(R.id.about);
		if (l.getVisibility() == View.VISIBLE) {
			AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
			a.setDuration(300);
			a.setAnimationListener(new AnimationListener() {
				@Override public void onAnimationStart(Animation animation) { }
				@Override public void onAnimationRepeat(Animation animation) { }
				@Override public void onAnimationEnd(Animation animation) {
					l.setVisibility(View.GONE);
					l.setOnClickListener(null);
				}
			});
			l.startAnimation(a);
		}
		else if (findViewById(R.id.first_disclaimer).getVisibility() == View.VISIBLE) { }
		else
			super.onBackPressed();
	}
	
	private void launchUpdateIfNeeded() {
		boolean letsFetch;

		String lastUpdate = Util.getPref(this, "lastGifsListUpdate");
		if (lastUpdate.equals("doitnow") || lastUpdate.equals(""))
			letsFetch = true;
		else {
			Date last = Util.stringToDate(Util.getPref(this, "lastGifsListUpdate"));
			long nbSecs = Util.getSecsDiff(last, new Date());
			long nbHours = nbSecs / 3600;
			letsFetch = nbHours > 12;
		}

		if (letsFetch)
			new FeedParser(this).execute();
	}
	
	private void itemClick(int pos) {
		Intent intent = new Intent(Activity_Main.this, Activity_Gif.class);
		scrollY = ((ListView) findViewById(R.id.list)).getFirstVisiblePosition();
		intent.putExtra("pos", pos);
		startActivity(intent);
		Util.setTransition(this, "rightToLeft");
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				Util.setPref(this, "lastGifsListUpdate", "doitnow");
				new FeedParser(this).execute();
				return true;
			case R.id.notifications:
				item.setChecked(!item.isChecked());
				if (item.isChecked()) enableNotifs();
				else disableNotifs();
				return true;
			case R.id.menu_about:
				final LinearLayout l = (LinearLayout) findViewById(R.id.about);
				if (l.getVisibility() == View.GONE) {
					Util.Fonts.setFont(this, l, Util.Fonts.CustomFont.Futura);
					l.setVisibility(View.VISIBLE);
					AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
					a.setDuration(500);
					a.setFillAfter(true);
					a.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) { }
					});
					l.startAnimation(a);
					l.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
							a.setDuration(300);
							a.setAnimationListener(new AnimationListener() {
								@Override public void onAnimationStart(Animation animation) { }
								@Override public void onAnimationRepeat(Animation animation) { }
								@Override public void onAnimationEnd(Animation animation) {
									l.setVisibility(View.GONE);
									l.setOnClickListener(null);
								}
							});
							l.startAnimation(a);
						}
					});
				} else {
					AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
					a.setDuration(300);
					a.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) {
							l.setVisibility(View.GONE);
							l.setOnClickListener(null);
						}
					});
					l.startAnimation(a);
				}
				return true;
			case R.id.menu_clear_cache:
				Util.clearCache(this);
				return true;
			case R.id.menu_contact:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://lesjoiesdeletudiantinfo.com/contact/"));
				startActivity(browserIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu_notifs = menu.findItem(R.id.notifications);
		menu_notifs.setChecked(Util.getPref(this, "notifs").equals("true"));
		return true;
	}
}
