package com.fuzzybee7.nmrt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options_about, menu);
		
		//menu.findItem(R.id.about_menu_item).setIntent(new Intent(this, AboutActivity.class));
		menu.findItem(R.id.home_menu_item).setIntent(new Intent(this, MainActivity.class));//basically reload home
		//menu.findItem(R.id.refresh_menu_item).setIntent(new Intent(this, NearestMRTActivity.class));//basically reload home
				
		return true;
	}
	
}
