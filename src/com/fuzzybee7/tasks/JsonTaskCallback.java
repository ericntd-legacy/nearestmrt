package com.fuzzybee7.tasks;

public interface JsonTaskCallback {
	public void onJsonDownloaded(String jsonString, byte task);
	public void onError();
}
