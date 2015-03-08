package com.chteuchteu.lesjoiesdeletudiantinfo.async;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chteuchteu.lesjoiesdeletudiantinfo.GifFoo;
import com.chteuchteu.lesjoiesdeletudiantinfo.R;
import com.chteuchteu.lesjoiesdeletudiantinfo.hlpr.Util;
import com.chteuchteu.lesjoiesdeletudiantinfo.ui.Activity_Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class CountdownLauncher extends AsyncTask<Void, Integer, Void> {
	private Activity_Main activity;
	private int nbSeconds;

	public CountdownLauncher(Activity_Main activity) {
		this.activity = activity;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		String source = "";
		try {
			URL address = new URL(GifFoo.COUNTDOWN_WS);
			BufferedReader in = new BufferedReader(new InputStreamReader(address.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				source += inputLine;

			in.close();

			if (!source.equals(""))
				nbSeconds = Integer.parseInt(source);
		} catch (Exception e) { Log.e("", e.toString()); }

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		View countdownContainer = activity.findViewById(R.id.countdown_container);

		if (nbSeconds != 0) {
			countdownContainer.setVisibility(View.VISIBLE);

			CountDownTimer countDownTimer = GifFoo.getInstance().getCountDownTimer();
			if (countDownTimer != null)
				countDownTimer.cancel();

			countDownTimer = new CountDownTimer(nbSeconds * 1000, 1000) {
				public void onTick(long millisUntilFinished) {
					int[] formatted = Util.getCountdownFromSeconds((int) millisUntilFinished / 1000);

					((TextView) activity.findViewById(R.id.countdown_hh)).setText(formatted[0] + "");
					((TextView) activity.findViewById(R.id.countdown_mm)).setText(Util.formatNumber(formatted[1]));
					((TextView) activity.findViewById(R.id.countdown_ss)).setText(Util.formatNumber(formatted[2]));
				}

				public void onFinish() {
					Util.setPref(activity, "lastGifsListUpdate", "doitnow");
					new FeedParser(activity).execute();
				}
			};
			GifFoo.getInstance().setCountDownTimer(countDownTimer);
			countDownTimer.start();
		}
	}
}
