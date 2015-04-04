package com.fuzzybee7.nmrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.fuzzybee7.application.MyApplication;
import com.fuzzybee7.entities.Direction;
import com.fuzzybee7.entities.MRT;
import com.fuzzybee7.parsers.DirectionJSONParser;
import com.fuzzybee7.parsers.PlaceJSONParser;
import com.fuzzybee7.tasks.DirectionsParserTask;
import com.fuzzybee7.tasks.DirectionsParserTaskCallback;
import com.fuzzybee7.tasks.DirectionsTask;
import com.fuzzybee7.tasks.JsonTask;
import com.fuzzybee7.tasks.PlacesParserTask;
import com.fuzzybee7.tasks.PlacesParserTaskCallback;
import com.fuzzybee7.tasks.PlacesTask;
import com.fuzzybee7.tasks.JsonTaskCallback;
import com.fuzzybee7.utils.LocationUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
// import com.google.android.maps.GeoPoint;
// import com.logentries.android.AndroidLogger;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
//import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		PlacesParserTaskCallback, JsonTaskCallback,
		DirectionsParserTaskCallback {
	// Debugging
	private final String TAG = "NearestMRTActivity";
	private final boolean D = true;

	/*
	 * Constants
	 */
	private final String PREFS = "NearestMRTPreferences";

	private final String PREF_MAP_TYPE = "MapType";

	final String URL_GOOGLE_PLACES_API = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	final String PARAM_LOCATION = "location=";
	final String PARAM_RADIUS = "&radius=1111";
	final String PARAM_SORT_BY = "&rankBy=" + "distance";
	final String PARAM_TYPE = "&types=" + "subway_station";
	final String PARAM_SENSOR = "&sensor=true";
	final String PARAM_KEY = "&key=AIzaSyCCPl_JtGPUQaQ9yZIyK-dvsduyWZy4ZAs";

	final String URL_GOOGLE_DIRECTIONS_API = "http://maps.google.com/maps/api/directions/json?";
	final String PARAM_ORIGIN = "origin=";
	final String PARAM_DEST = "&destination=";
	final String PARAM_MODE = "&mode=walking";

	private static final byte TASK_PLACES_DOWNLOAD = 1;
	private static final byte TASK_DIRECTIONS_DOWNLOAD = 2;

	public static final String DEF_LAT = "1.371924";
	public static final String DEF_LON = "103.896999";

	public static final double LAT_NEX = 1.350610;
	public static final double LON_NEX = 103.872263;
	
	private final String LOGENTRIES_TOKEN = "b1ccf5f9-adcb-42c3-ae0c-09d6793fbed8";

	// private final boolean MAP_TYPE_NORMAL = true;// true means map and false
	// mean satellite
	// final byte MAP_TYPE_SATELLITE = 2;

	// private static final int TWO_MINUTES = 1000 * 60 * 2;

	boolean gps_enabled;
	boolean network_enabled;

	private String locationProvider2;
	private String locationProvider1;

	// private Handler mHandler = new Handler();

	private LatLng curLoc;// = new LatLng(Double.parseDouble(DEF_LAT),
							// Double.parseDouble(DEF_LON));

	private LocationManager locationManager; // =
												// (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	private LocationListener networkLocListener;
	private LocationListener gpsLocListener;

	private GoogleMap gooleMap;

	private SharedPreferences prefs;
	private SharedPreferences.Editor prefEditor;

	/*
	 * Views
	 */
	private TextView nearestMRTLoc;
	private TextView wd;
	private TextView wt;
	private Button btnMapTypeSwitch;

	private boolean mapTypeNormal = true; // true means map view, false means
											// satellite view

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*
		 * without the following or the declaration in styles.xml, I won't have action bar
		 */
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		setContentView(R.layout.activity_main);
		
//		ActionBar actionBar = this.getActionBar();
//		actionBar.show();
		
		checkInternetConnection();
		
		/*
		 * Set up LogEntries utilities
		 */
		AndroidLogger logger = AndroidLogger.getLogger(getApplicationContext(), LOGENTRIES_TOKEN, false);
		logger.info("it's working");

		checkGooglePlayServices();

		initialisePrefs();

		// Getting Google Map
		// MapFragment fragment = (MapFragment)
		// getFragmentManager().findFragmentById(R.id.map);
		initialiseMap();

		// mGoogleMap.setOnMyLocationChangeListener(listener);

		// Initiate the views
		initialiseViews();

		initLocation();

		/**
		 * Handles application preferences which include current longitude and
		 * latitude
		 */
		// Retrieve the shared preferences

		// Register the listener with the Location Manager to receive location
		// updates
		// String locationProvider2 = LocationManager.NETWORK_PROVIDER;

		// ---------

		Location lastKnownLocation = locationManager
				.getLastKnownLocation(locationProvider2);
		// Location lastKnownLocation =
		// mlocManager.getLastKnownLocation(locationProvider1);

		if (lastKnownLocation != null) {
			double mLatitude = lastKnownLocation.getLatitude();
			double mLongitude = lastKnownLocation.getLongitude();

			curLoc = new LatLng(mLatitude, mLongitude);

			gooleMap.moveCamera(CameraUpdateFactory.newLatLng(curLoc));
			gooleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

			// Toast.makeText(getApplicationContext(), "Cached location found",
			// Toast.LENGTH_LONG).show();

			// SharedPreferences.Editor prefEditor = prefs.edit();
			prefEditor.putString("curLat", String.valueOf(mLatitude));
			prefEditor.putString("curLong", String.valueOf(mLongitude));

			if (D)
				Log.d(TAG, "location was cached, lat was " + mLatitude
						+ " and lon was " + mLongitude);
			prefEditor.commit();

			getNearestMRT();
			// Test using dependency injection???
			// getNearestMRT(LAT_NEX, LON_NEX);//NEX
		} else {
			if (D)
				Log.w(TAG, "No cached location found");
			// Toast.makeText(getApplicationContext(),
			// "No cached location found",
			// Toast.LENGTH_LONG).show();
		}

		/**
		 * Handles application preferences which include current longitude and
		 * latitude
		 */
		// Retrieve the shared preferences
		// prefs = getSharedPreferences(PREFS,
		// Context.MODE_PRIVATE);
		initialiseButtons();

		/*
		 * Google Analytics
		 */

	}

	private void checkInternetConnection() {
												// TODO Auto-generated method stub
												
											}

	private void initLocation() {
		// ---------
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		// Define a listener that responds to location updates
		networkLocListener = new MyLocationListener();
		gpsLocListener = new MyLocationListener();

		locationProvider2 = LocationManager.NETWORK_PROVIDER;
		locationProvider1 = LocationManager.GPS_PROVIDER;

		gps_enabled = false;
		network_enabled = false;
		// exceptions will be thrown if provider is not permitted.

		// try{gps_enabled=mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception
		// ex){}
		gps_enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// try{network_enabled=mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception
		// ex){}
		network_enabled = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// don't start listeners if no provider is enabled
		if (!gps_enabled && !network_enabled) {
			Toast.makeText(getApplicationContext(),
					"Either network or GPS not enabled, please check",
					Toast.LENGTH_LONG).show();
			/*
			 * Intent gpsOptionsIntent = new
			 * Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
			 * ); startActivity(gpsOptionsIntent);
			 */
		}

		// Define a listener that responds to location updates
		// LocationListener locationListener = new LocationListener() {
		// @Override
		// public void onLocationChanged(Location location) {
		// // Called when a new location is found by the network location
		// // provider.
		// if (D)
		// Log.i(TAG, "Updating current location");
		// Toast.makeText(getApplicationContext(),
		// "Updating current location", Toast.LENGTH_LONG).show();
		// }
		//
		// public void onStatusChanged(String provider, int status,
		// Bundle extras) {
		// if (D)
		// Log.i(TAG, "status changed");
		// }
		//
		// public void onProviderEnabled(String provider) {
		// if (D)
		// Log.i(TAG, "location enabled");
		// }
		//
		// public void onProviderDisabled(String provider) {
		// if (D)
		// Log.i(TAG, "Location disabled");
		// }
		// };

		if (network_enabled) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1, 1, networkLocListener);
		}
		if (gps_enabled) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1, 1, gpsLocListener);
		}
	}

	private void checkGooglePlayServices() {
		// Checking whether Google Play Services in place and working
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (D) {
			if (status == ConnectionResult.SUCCESS) {
				// Success! Do what you want
				Log.i(TAG, "Google Play Services all good");
			} else if (status == ConnectionResult.SERVICE_MISSING) {
				Log.e(TAG, "Google Play Services not in place");
			} else if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
				Log.e(TAG, "Google Play Serivices outdated");
			} else if (status == ConnectionResult.SERVICE_DISABLED) {
				Log.e(TAG, "Google Plauy Services disabled");
			} else if (status == ConnectionResult.SERVICE_INVALID) {
				Log.e(TAG,
						"Google Play Serivices invalid but wtf does that mean?");
			} else {
				Log.e(TAG, "No way this is gonna happen");
			}
		}
	}

	private void initialisePrefs() {
		this.prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

		prefEditor = prefs.edit();
	}

	private void initialiseViews() {
		this.nearestMRTLoc = (TextView) findViewById(R.id.TextNearestMRTName);
		this.wd = (TextView) findViewById(R.id.WalkingDistance);
		this.wt = (TextView) findViewById(R.id.WalkingTime);
	}

	private void initialiseButtons() {
		this.btnMapTypeSwitch = (Button) findViewById(R.id.BtnMapTypeSwitch);
		this.btnMapTypeSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mapTypeNormal) {
					gooleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					mapTypeNormal = false;

					btnMapTypeSwitch.setText(R.string.btn_map_view);
				} else {
					gooleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					mapTypeNormal = true;

					btnMapTypeSwitch.setText(R.string.btn_satellite_view);
				}
				prefEditor.putBoolean(PREF_MAP_TYPE, mapTypeNormal);
				prefEditor.commit();
			}
		});
	}

	private void initialiseMap() {
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		this.gooleMap = fragment.getMap();// returns null why?
		this.gooleMap.setMyLocationEnabled(true);// without this the blue bubble
													// will
		// not show so it's very
		// important!!!

		this.mapTypeNormal = prefs.getBoolean(PREF_MAP_TYPE, true);
		if (!mapTypeNormal) {
			this.gooleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			// this.btnMapTypeSwitch.setText(R.string.btn_map_view);
		}
	}

	/*
	 * private void makeUseOfNewLocation(Location location) { Log.d(TAG,
	 * "testing"); }
	 */

	// private void getNearestMRT(double mLatitude, double mLongitude) {
	private void getNearestMRT() {
		// get the place(s)

		StringBuilder sb = new StringBuilder(URL_GOOGLE_PLACES_API);
		sb.append(PARAM_LOCATION + String.valueOf(this.curLoc.latitude) + ","
				+ String.valueOf(this.curLoc.longitude));
		sb.append(PARAM_RADIUS);// distance less than 1.111km; assumption that
								// reasonable walking distance <= 1km
		sb.append(PARAM_SORT_BY);// does not work with custom 'radius'
									// -
									// https://developers.google.com/maps/documentation/javascript/places#place_search_requests
		sb.append(PARAM_TYPE);// type 'subway_station'
		sb.append(PARAM_SENSOR);
		sb.append(PARAM_KEY);

		if (D)
			Log.d(TAG, sb.toString());

		// Creating a new non-ui thread task to download Google place json data
		// PlacesTask placesTask = new PlacesTask();
		JsonTask jsonTask = new JsonTask(this, TASK_PLACES_DOWNLOAD);

		// Invokes the "doInBackground()" method of the class PlaceTask
		// placesTask.execute(sb.toString());
		jsonTask.execute(sb.toString());
	}

	private void getPathToMRT(LatLng origin, LatLng des) {// should I use
															// GeoPoint objects
															// instead, how
															// about LatLng?
		// final String PARAM_SENSOR = "";

		// get the place(s)
		StringBuilder sb = new StringBuilder(URL_GOOGLE_DIRECTIONS_API);
		sb.append(PARAM_ORIGIN + String.valueOf(origin.latitude) + ","
				+ String.valueOf(origin.longitude));
		sb.append(PARAM_DEST + String.valueOf(des.latitude) + ","
				+ String.valueOf(des.longitude));
		sb.append(PARAM_MODE);// distance less than 1km
		sb.append(PARAM_SENSOR);

		if (D)
			Log.d(TAG, "the request sent was " + sb.toString());
		// sb.append("&rankby=distance");
		// sb.append("&key=AIzaSyCCPl_JtGPUQaQ9yZIyK-dvsduyWZy4ZAs");

		// Creating a new non-ui thread task to download Google place json data
		// DirectionsTask dirTask = new DirectionsTask();
		JsonTask jsonTask = new JsonTask(this, TASK_DIRECTIONS_DOWNLOAD);

		// Invokes the "doInBackground()" method of the class PlaceTask
		jsonTask.execute(sb.toString());
	}

	@Override
	protected void onPause() {
		// What do I with the location update here to save battery?
		super.onPause();
		
		MyApplication.uiInBackground = true;
		/*
		 * if (mlocManager!=null) {
		 * 
		 * if (networkLocListener!=null) {
		 * mlocManager.removeUpdates(networkLocListener); networkLocListener =
		 * null; } if (gpsLocListener!=null) {
		 * mlocManager.removeUpdates(gpsLocListener); gpsLocListener = null; }
		 * mlocManager = null; }
		 */
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		MyApplication.uiInBackground = false;
		
		// mlocManager was reset to null onPause()
		if (locationManager != null) {
			if (network_enabled) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						networkLocListener);
			}
			if (gps_enabled) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, gpsLocListener);
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		this.mapTypeNormal = prefs.getBoolean(PREF_MAP_TYPE, true);

		if (!this.mapTypeNormal) {
			this.btnMapTypeSwitch.setText(R.string.btn_map_view);
		}
	}

	/* Class My Location Listener */
	public class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			Log.w(TAG, "onLocationChanged");
			// Log.i(TAG, "testing");
			// remove updates so that the listener is not called too much
			if (locationManager != null)
				locationManager.removeUpdates(this);

			double mLatitude = loc.getLatitude();
			double mLongitude = loc.getLongitude();

			Log.i(TAG, "Updating current location");
			if (!MyApplication.uiInBackground) Toast.makeText(getApplicationContext(),
					"Updating current location", Toast.LENGTH_LONG).show();
			// SharedPreferences.Editor prefEditor = nearestMRTSettings.edit();
			// prefEditor.putString("curLat", String.valueOf(mLatitude));
			// prefEditor.putString("curLong", String.valueOf(mLongitude));
			// prefEditor.commit();

			updateCurrentLocation(new LatLng(mLatitude, mLongitude));

			// Test using dependency injection???
			// getNearestMRT(LAT_NEX, LON_NEX);//NEX

		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Disabled",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Enabled",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, provider + " location is toggled");
		}

	}

	private void updateCurrentLocation(LatLng loc) {
		Log.i(TAG, "updateCurrentLocation");
		Log.w(TAG, "lat is " + loc.latitude);
		Log.w(TAG, "long is " + loc.longitude);
		this.curLoc = loc;

		gooleMap.moveCamera(CameraUpdateFactory.newLatLng(curLoc));
		gooleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

		// loc.getLatitude();
		// loc.getLongitude();

		// String Text = "My current location is: " + "Latitud = "
		// + loc.getLatitude() + "Longitud = " + loc.getLongitude();

		// Toast.makeText( getApplicationContext(), Text,
		// Toast.LENGTH_LONG).show();
		// Log.d(TAG, Text);

		getNearestMRT();
	}

	/*
	 * private Runnable onRequestLocation = new Runnable() {
	 * 
	 * @Override public void run() { Log.i(TAG, "Updating location");
	 * //Toast.makeText( getApplicationContext(), "Testing",
	 * Toast.LENGTH_SHORT).show(); // Ask for a location
	 * mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
	 * mlocListener); // Run this again in
	 * mHandler.postDelayed(onRequestLocation, 100000); } };
	 */

	/*
	 * public View onCreateView(LayoutInflater inflater, ViewGroup container,
	 * Bundle savedInstanceState) { View mainview =
	 * inflater.inflate(R.layout.activity_nearest_mrtbase, null); return
	 * mainview; }
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options, menu);

		menu.findItem(R.id.about_menu_item).setIntent(
				new Intent(this, AboutActivity.class));
		// menu.findItem(R.id.home_menu_item).setIntent(new Intent(this,
		// NearestMRTActivity.class));//basically reload home
		menu.findItem(R.id.refresh_menu_item).setIntent(
				new Intent(this, MainActivity.class));// basically reload
															// home

		return true;
	}

	@Override
	public void clearMap() {
		this.gooleMap.clear();
	}

	@Override
	public Marker addMarker() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveCamera() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getPathToMRT() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onJsonDownloaded(String jsonString, byte task) {
		switch (task) {
		case TASK_PLACES_DOWNLOAD:
			PlacesParserTask placesParserTask = new PlacesParserTask(this);
			placesParserTask.execute(jsonString);
			break;
		case TASK_DIRECTIONS_DOWNLOAD:
			DirectionsParserTask parserTask = new DirectionsParserTask(this);
			parserTask.execute(jsonString);
			break;
		}

	}

	@Override
	public void onError() {
		Log.e(TAG, "onError");

	}

	@Override
	public void onNearestMRTFound(MRT mrt) {
		Log.w(TAG, "onNearestMRTFound");

		// Show nearest MRT station in text
		this.nearestMRTLoc.setText(mrt.getName());
		this.nearestMRTLoc.setTextAppearance(getApplicationContext(),
				R.style.MyBoldText);

		// Getting the direction to the nearest MRT station
		/*
		 * nearestMRTSettings = getSharedPreferences(NEARESTMRT_PREFS,
		 * Context.MODE_PRIVATE); double curLat =
		 * Double.parseDouble(nearestMRTSettings.getString("curLat", DEF_LAT));
		 * double curLon =
		 * Double.parseDouble(nearestMRTSettings.getString("curLon", DEF_LON));
		 * LatLng curLatLng = new LatLng(curLat, curLon);
		 */
		if (this.curLoc != null)
			getPathToMRT(this.curLoc, mrt.getLatLng());
		// this.callback.getPathToMRT();

		MarkerOptions markerOptions = new MarkerOptions();
		// Setting the position for the marker
		markerOptions.position(mrt.getLatLng());

		// Setting the title for the marker.
		// This will be displayed on taping the marker
		// markerOptions.title(name + " : " + vicinity);

		// Placing a marker on the touched position
		Marker mrtMark = this.gooleMap.addMarker(markerOptions);
		// Marker mrtMark = this.callback.addMarker();
		// mrtMark.showInfoWindow();

		this.gooleMap
				.moveCamera(CameraUpdateFactory.newLatLng(mrt.getLatLng()));
		// this.callback.moveCamera();
		// mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

		// Log.i(TAG, "the stations found, furthest first, are:");
		// for (int i = 0; i < list.size(); i++) {
		// hmPlace = list.get(i);
		// name = hmPlace.get(PLACE_NAME);
		// Log.i(TAG, (i + 1) + ')' + name);
		// }

	}

	@Override
	public void onDirectionsFound(Direction direction) {
		Log.w(TAG, "onDirectionsFounds");

		Log.w(TAG, "distance is " + direction.getDistance() + " and walking time is "
				+ direction.getDuration());
		// runOnUiThread(new Runnable() {
		// public void run() {

		// stuff that updates ui
		// wd = (TextView) findViewById(R.id.WalkingDistance);
		this.wd.setText(direction.getDistance());

		// wt = (TextView) findViewById(R.id.WalkingTime);
		this.wt.setText(direction.getDuration());

		// }
		// });
		Log.i(TAG, "overview polyline is " + direction.getPolyLine());
		List<LatLng> polyPoints = LocationUtils.decodePoly(direction.getPolyLine());

		// Instantiates a new Polygon object and adds points to define a
		// rectangle
		PolylineOptions rectOptions = new PolylineOptions();

		for (int i = 0; i < polyPoints.size() - 1; i++) {
			rectOptions.add(polyPoints.get(i));

			/*
			 * LatLng src = polyPoints.get(i); LatLng dest = polyPoints.get(i +
			 * 1); Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
			 * .add(new LatLng(src.latitude, src.longitude), new
			 * LatLng(dest.latitude, dest.longitude))
			 * .width(25).color(Color.RED).geodesic(true));
			 */
		}

		// Get back the mutable Polygon
		// rectOptions.color(Color.BLUE).geodesic(true);

		Polyline line = gooleMap.addPolyline(rectOptions.color(Color.BLUE)
				.geodesic(true));
		// Log.i(TAG, "why is nothing drawn "+line.isVisible());
	}

}
