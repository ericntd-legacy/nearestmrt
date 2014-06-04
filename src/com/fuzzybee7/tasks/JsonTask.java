package com.fuzzybee7.tasks;

import com.fuzzybee7.utils.HttpUtils;

import android.os.AsyncTask;
import android.util.Log;

/** A class, to download Google Places */
public class JsonTask extends AsyncTask<String, Integer, String> {
	private static final String TAG = "PlacesTask";

	// private final byte RESULT_FAILED = 1;
	// private final byte RESULT_SUCCESS = 2;

	private JsonTaskCallback callback;
	private byte task;

	// private byte status;

	public JsonTask(JsonTaskCallback callback, byte task) {
		this.callback = callback;
		this.task = task;
	}

	// Invoked by execute() method of this object
	@Override
	protected String doInBackground(String... url) {
		// make asynctask wait for debugger
		// android.os.Debug.waitForDebugger();
		Log.i(TAG, "downloading JSON from Google");
		String result = null;
		try {
			result = HttpUtils.downloadJson(url[0]);// downloadUrl(url[0]);
			// this.status = RESULT_SUCCESS;
		} catch (Exception e) {
			// this.status = RESULT_FAILED;
			Log.e(TAG, "exception happened", e);
		}
		return result;
	}

	// Executed after the complete execution of doInBackground() method
	@Override
	protected void onPostExecute(String result) {
		if (result != null && !result.isEmpty()) {
			this.callback.onJsonDownloaded(result, task);
		} else {
			this.callback.onError();
		}
	}

	public byte getTask() {
		return task;
	}

	public void setTask(byte task) {
		this.task = task;
	}

}
