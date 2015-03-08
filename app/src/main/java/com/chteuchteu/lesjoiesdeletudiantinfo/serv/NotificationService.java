package com.chteuchteu.lesjoiesdeletudiantinfo.serv;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;
import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.RSSReader;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;
import com.chteuchteu.lesjoiesdeletudiantinfo.ui.Activity_Main;

import java.util.List;

public class NotificationService extends Service {
	private WakeLock mWakeLock;
	private Context context;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private void handleIntent(Intent intent) {
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.chteuchteu.lesjoiesdeletudiantinfo");
		mWakeLock.acquire();

		context = this;
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (!cm.getBackgroundDataSetting()) {
			stopSelf();
			return;
		}
		
		// do the actual work, in a separate thread
		new PollTask().execute();
	}
	
	private class PollTask extends AsyncTask<Void, Void, Void> {
		int nbUnseenGifs = 0;
		List<Gif> l;
		
		@Override
		protected Void doInBackground(Void... params) {
			l = RSSReader.parse(GifFoo.RSS_URL, null);
			
			String lastUnseenGif = Util.getPref(context, "lastViewed");
			if (l.size() > 0) {
				for (Gif g : l) {
					if (g.getArticleUrl().equals(lastUnseenGif))
						break;
					else
						nbUnseenGifs++;
				}
			}
			
			if (nbUnseenGifs > 0)
				Util.saveGifs(getApplicationContext(), l);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// Check if there are new gifs, and if a notification for them hasn't been displayed yet
			boolean notif = (nbUnseenGifs > 0 && 
					(Util.getPref(context, "lastNotifiedGif").equals("")
							|| l.size() > 0 && !l.get(0).getGifUrl().equals(Util.getPref(context, "lastNotifiedGif"))));
			
			if (notif) {
				// Save the last gif
				if (l.size() > 0)
					Util.setPref(context, "lastNotifiedGif", l.get(0).getGifUrl());
				
				String title = "Les Joies de l'Etudiant Info";
				String text;
				if (nbUnseenGifs > 1)
					text = nbUnseenGifs + " nouveaux gifs !";
				else
					text = "1 nouveau gif !";
				
				NotificationCompat.Builder builder =
						new NotificationCompat.Builder(NotificationService.this)
				.setSmallIcon(R.drawable.ic_notifications)
				.setNumber(nbUnseenGifs)
				.setContentTitle(title)
				.setAutoCancel(true)
				.setContentText(text);
				int NOTIFICATION_ID = 1664;
				
				Intent targetIntent = new Intent(NotificationService.this, Activity_Main.class);
				PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				builder.setContentIntent(contentIntent);
				NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nManager.notify(NOTIFICATION_ID, builder.build());
			}
			stopSelf();
		}
	}
	

	@Override
	public void onStart(Intent intent, int startId) {
		handleIntent(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_NOT_STICKY;
	}
	
	public void onDestroy() {
		super.onDestroy();
		mWakeLock.release();
	}
}
