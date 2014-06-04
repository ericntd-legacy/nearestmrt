package com.fuzzybee7.tasks;

import com.fuzzybee7.utils.HttpUtils;

import android.os.AsyncTask;
import android.util.Log;

/** A class, to download Google Places */
public class PlacesTask extends AsyncTask<String, Integer, String> {
	private static final String TAG = "PlacesTask";
	
	// Invoked by execute() method of this object
	@Override
	protected String doInBackground(String... url) {
		// make asynctask wait for debugger
		// android.os.Debug.waitForDebugger();
		Log.i(TAG, "tring to get nearby mrt stations");
		String result = null;
		try {
			result = HttpUtils.downloadJson(url[0]);// downloadUrl(url[0]);
		} catch (Exception e) {
			Log.e(TAG, "exception happened",e);
		}
		return result;
	}

	// Executed after the complete execution of doInBackground() method
	@Override
	protected void onPostExecute(String result) {
		PlacesParserTask parserTask = new PlacesParserTask(null);

		// Start parsing the Google places in JSON format
		// Invokes the "doInBackground()" method of the class ParseTask
		parserTask.execute(result);
	}

}
