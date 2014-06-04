package com.fuzzybee7.tasks;

import com.fuzzybee7.utils.HttpUtils;

import android.os.AsyncTask;
import android.util.Log;

/** A class, to download Google Directions */
public class DirectionsTask extends AsyncTask<String, Integer, String> {
	
	private static final String TAG = "DirectionsTask";

	String data = null;

	// Invoked by execute() method of this object
	@Override
	protected String doInBackground(String... url) {
		// android.os.Debug.waitForDebugger();
		Log.i(TAG, "getting the walking route to nearest mrt stations");
		try {
			data = HttpUtils.downloadJson(url[0]);// downloadUrl(url[0]);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
		return data;
	}

	// Executed after the complete execution of doInBackground() method
	@Override
	protected void onPostExecute(String result) {
		Log.i(TAG,
				"data is downloaded from Google Directions API successfully");
		DirectionsParserTask parserTask = new DirectionsParserTask(null);

		// Start parsing the Google places in JSON format
		// Invokes the "doInBackground()" method of the class ParseTask
		parserTask.execute(result);
	}

}
