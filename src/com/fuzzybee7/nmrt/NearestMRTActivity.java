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
import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NearestMRTActivity extends FragmentActivity {
	// Debugging
	private final String DEBUG = "NearestMRT";
	private final boolean D = true;

	/*
	 * Constants
	 */
	private final String PREFS = "NearestMRTPreferences";
	
	private final String PREF_MAP_TYPE = "MapType";

	public static final String DEF_LAT = "1.371924";
	public static final String DEF_LON = "103.896999";

	public static final double LAT_NEX = 1.350610;
	public static final double LON_NEX = 103.872263;

	// private final boolean MAP_TYPE_NORMAL = true;// true means map and false mean satellite
	//  final byte MAP_TYPE_SATELLITE = 2;

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

	private boolean mapTypeNormal = true; // true means map view, false means satellite view
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearestmrt);
		
		checkPrerequisites();
		
		initialisePrefs();

		// Getting Google Map
		// MapFragment fragment = (MapFragment)
		// getFragmentManager().findFragmentById(R.id.map);
		initialiseMap();
		
		// mGoogleMap.setOnMyLocationChangeListener(listener);

		// Initiate the views
		initialiseViews();

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
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				if (D)
					Log.i(DEBUG, "Updating current location");
				Toast.makeText(getApplicationContext(),
						"Updating current location", Toast.LENGTH_LONG).show();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				if (D)
					Log.i(DEBUG, "status changed");
			}

			public void onProviderEnabled(String provider) {
				if (D)
					Log.i(DEBUG, "location enabled");
			}

			public void onProviderDisabled(String provider) {
				if (D)
					Log.i(DEBUG, "Location disabled");
			}
		};

		if (network_enabled) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1, 1, networkLocListener);
		}
		if (gps_enabled) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1, 1, gpsLocListener);
		}

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

			Toast.makeText(getApplicationContext(), "Cached location found",
					Toast.LENGTH_LONG).show();

			// SharedPreferences.Editor prefEditor = prefs.edit();
			prefEditor.putString("curLat", String.valueOf(mLatitude));
			prefEditor.putString("curLong", String.valueOf(mLongitude));

			if (D)
				Log.d(DEBUG, "location was cached, lat was " + mLatitude
						+ " and lon was " + mLongitude);
			prefEditor.commit();

			getNearestMRT(mLatitude, mLongitude);
			// Test using dependency injection???
			// getNearestMRT(LAT_NEX, LON_NEX);//NEX
		} else {
			if (D)
				Log.d(DEBUG, "No cached location found");
			Toast.makeText(getApplicationContext(), "No cached location found",
					Toast.LENGTH_LONG).show();
		}

		/**
		 * Handles application preferences which include current longitude and
		 * latitude
		 */
		// Retrieve the shared preferences
//		prefs = getSharedPreferences(PREFS,
//				Context.MODE_PRIVATE);
		initialiseButtons();
		
		/*
		 * Google Analytics
		 */
		
	}
	
	private void checkPrerequisites() {
		// Checking whether Google Play Services in place and working
				int status = GooglePlayServicesUtil
						.isGooglePlayServicesAvailable(getApplicationContext());
				if (D) {
					if (status == ConnectionResult.SUCCESS) {
						// Success! Do what you want
						Log.i(DEBUG, "Google Play Services all good");
					} else if (status == ConnectionResult.SERVICE_MISSING) {
						Log.e(DEBUG, "Google Play Services not in place");
					} else if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
						Log.e(DEBUG, "Google Play Serivices outdated");
					} else if (status == ConnectionResult.SERVICE_DISABLED) {
						Log.e(DEBUG, "Google Plauy Services disabled");
					} else if (status == ConnectionResult.SERVICE_INVALID) {
						Log.e(DEBUG,
								"Google Play Serivices invalid but wtf does that mean?");
					} else {
						Log.e(DEBUG, "No way this is gonna happen");
					}
				}
	}
	
	private void initialisePrefs() {
		this.prefs = getSharedPreferences(PREFS,
		Context.MODE_PRIVATE);
		
		prefEditor = prefs.edit();
	}
	
	private void initialiseViews() {
		this.nearestMRTLoc = (TextView) findViewById(R.id.NearestMRTLocation);
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
		this.gooleMap.setMyLocationEnabled(true);// without this the blue bubble will
											// not show so it's very
											// important!!!
		
		this.mapTypeNormal = prefs.getBoolean(PREF_MAP_TYPE, true);
		if (!mapTypeNormal){
			this.gooleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			// this.btnMapTypeSwitch.setText(R.string.btn_map_view);
		}
	}

	/*
	 * private void makeUseOfNewLocation(Location location) { Log.d(DEBUG,
	 * "testing"); }
	 */

	private void getNearestMRT(double mLatitude, double mLongitude) {
		// get the place(s)
		StringBuilder sb = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		sb.append("location=" + String.valueOf(mLatitude) + ","
				+ String.valueOf(mLongitude));
		sb.append("&radius=1111");// distance less than 1.111km; assumption that
									// reasonable walking distance <= 1km
		sb.append("&rankBy=" + "distance");// does not work with custom 'radius'
											// -
											// https://developers.google.com/maps/documentation/javascript/places#place_search_requests
		sb.append("&types=" + "subway_station");// type 'subway_station'
		sb.append("&sensor=true");
		sb.append("&key=AIzaSyCCPl_JtGPUQaQ9yZIyK-dvsduyWZy4ZAs");

		if (D)
			Log.d(DEBUG, sb.toString());

		// Creating a new non-ui thread task to download Google place json data
		PlacesTask placesTask = new PlacesTask();

		// Invokes the "doInBackground()" method of the class PlaceTask
		placesTask.execute(sb.toString());
	}

	private void getPathToMRT(LatLng origin, LatLng des) {// should I use
															// GeoPoint objects
															// instead, how
															// about LatLng?
		// get the place(s)
		StringBuilder sb = new StringBuilder(
				"http://maps.google.com/maps/api/directions/json?");
		sb.append("origin=" + String.valueOf(origin.latitude) + ","
				+ String.valueOf(origin.longitude));
		sb.append("&destination=" + String.valueOf(des.latitude) + ","
				+ String.valueOf(des.longitude));
		sb.append("&mode=walking");// distance less than 1km
		sb.append("&sensor=true");

		if (D)
			Log.d(DEBUG, "the request sent was " + sb.toString());
		// sb.append("&rankby=distance");
		// sb.append("&key=AIzaSyCCPl_JtGPUQaQ9yZIyK-dvsduyWZy4ZAs");

		// Creating a new non-ui thread task to download Google place json data
		DirectionsTask dirTask = new DirectionsTask();

		// Invokes the "doInBackground()" method of the class PlaceTask
		dirTask.execute(sb.toString());
	}

	@Override
	protected void onPause() {
		// What do I with the location update here to save battery?
		super.onPause();

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
			Log.i(DEBUG, "testing");
			// remove updates so that the listener is not called too much
			if (locationManager != null)
				locationManager.removeUpdates(this);

			double mLatitude = loc.getLatitude();
			double mLongitude = loc.getLongitude();

			Log.i(DEBUG, "Updating current location");
			Toast.makeText(getApplicationContext(),
					"Updating current location", Toast.LENGTH_LONG).show();
			// SharedPreferences.Editor prefEditor = nearestMRTSettings.edit();
			// prefEditor.putString("curLat", String.valueOf(mLatitude));
			// prefEditor.putString("curLong", String.valueOf(mLongitude));
			// prefEditor.commit();

			curLoc = new LatLng(mLatitude, mLongitude);

			gooleMap.moveCamera(CameraUpdateFactory.newLatLng(curLoc));
			gooleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

			// loc.getLatitude();
			// loc.getLongitude();

			String Text = "My current location is: " + "Latitud = "
					+ loc.getLatitude() + "Longitud = " + loc.getLongitude();

			// Toast.makeText( getApplicationContext(), Text,
			// Toast.LENGTH_LONG).show();
			Log.d(DEBUG, Text);

			getNearestMRT(mLatitude, mLongitude);
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
			Log.i(DEBUG, provider + " location is toggled");
		}

	}

	/*
	 * private Runnable onRequestLocation = new Runnable() {
	 * 
	 * @Override public void run() { Log.i(DEBUG, "Updating location");
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
				new Intent(this, NearestMRTActivity.class));// basically reload
															// home

		return true;
	}

	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			// make asynctask wait for debugger
			// android.os.Debug.waitForDebugger();
			Log.i(DEBUG, "tring to get nearby mrt stations");
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d(DEBUG, e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			ParserTask parserTask = new ParserTask();

			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}

	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {
			// android.os.Debug.waitForDebugger();
			List<HashMap<String, String>> places = null;
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a List construct */
				places = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d(DEBUG, e.toString());
			}
			return places;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {
			Log.i(DEBUG, "displaying the nearest mrt station");

			// Clears all the existing markers
			gooleMap.clear();

			// add a balloon showing the current location or the start point of
			// the walking path
			Marker startMark = gooleMap.addMarker(new MarkerOptions().position(
					curLoc).title("You are here"));
			startMark.showInfoWindow();

			if (list.size() > 0) {

				// Creating a marker
				MarkerOptions markerOptions = new MarkerOptions();

				// Getting a place from the places list
				HashMap<String, String> hmPlace = list.get(list.size() - 1);// only
																			// getting
																			// the
																			// last
																			// one

				// Getting name
				String name = hmPlace.get("place_name");

				// Getting vicinity
				String vicinity = hmPlace.get("vicinity");

				String nearestMRT = name + " MRT Station";// +vicinity;

				// Show nearest MRT station in text
				nearestMRTLoc.setText(nearestMRT);
				nearestMRTLoc.setTextAppearance(getApplicationContext(),
						R.style.MyBoldText);

				// Getting latitude of the place
				double lat = Double.parseDouble(hmPlace.get("lat"));

				// Getting longitude of the place
				double lng = Double.parseDouble(hmPlace.get("lng"));

				LatLng latLng = new LatLng(lat, lng);

				// Getting the direction to the nearest MRT station
				/*
				 * nearestMRTSettings = getSharedPreferences(NEARESTMRT_PREFS,
				 * Context.MODE_PRIVATE); double curLat =
				 * Double.parseDouble(nearestMRTSettings.getString("curLat",
				 * DEF_LAT)); double curLon =
				 * Double.parseDouble(nearestMRTSettings.getString("curLon",
				 * DEF_LON)); LatLng curLatLng = new LatLng(curLat, curLon);
				 */
				getPathToMRT(curLoc, latLng);

				// Setting the position for the marker
				markerOptions.position(latLng);

				// Setting the title for the marker.
				// This will be displayed on taping the marker
				markerOptions.title(name + " : " + vicinity);

				// Placing a marker on the touched position
				Marker mrtMark = gooleMap.addMarker(markerOptions);
				// mrtMark.showInfoWindow();

				gooleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
				// mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

				Log.i(DEBUG, "the stations found, furthest first, are:");
				for (int i = 0; i < list.size(); i++) {
					hmPlace = list.get(i);
					name = hmPlace.get("place_name");
					Log.i(DEBUG, (i + 1) + ')' + name);
				}
			} else {
				Log.i(DEBUG, "couldn't find any MRT stations");
			}

		}

	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		Log.i(DEBUG, "downloading JSON data of the nearest MRT station");

		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d(DEBUG, e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}

		return data;
	}

	/** A class, to download Google Directions */
	private class DirectionsTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			// android.os.Debug.waitForDebugger();
			Log.i(DEBUG, "getting the walking route to nearest mrt stations");
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d(DEBUG, e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			Log.i(DEBUG,
					"data is downloaded from Google Directions API successfully");
			DirParserTask parserTask = new DirParserTask();

			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}

	}

	/** A class to parse the Google Places in JSON format */
	private class DirParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {
			// android.os.Debug.waitForDebugger();
			List<HashMap<String, String>> routes = null;
			DirectionJSONParser dirJsonParser = new DirectionJSONParser();
			Log.i(DEBUG,
					"trying to parse the response from Google Directions API");
			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a List construct */
				routes = dirJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d(DEBUG, e.toString());
			}
			return routes;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> routes) {

			// Clears all the existing markers
			// mGoogleMap.clear();

			// for(int i=0;i<list.size();i++){

			// Getting latitude of the place
			// double lat = Double.parseDouble(hmPlace.get("lat"));

			// Getting longitude of the place
			// double lng = Double.parseDouble(hmPlace.get("lng"));

			// Getting name
			// String name = hmPlace.get("place_name");

			// Getting vicinity
			// String vicinity = hmPlace.get("vicinity");

			// String nearestMRT = name + " MRT Station";//+vicinity;

			// LatLng latLng = new LatLng(lat, lng);

			// Setting the position for the marker
			// markerOptions.position(latLng);

			// Setting the title for the marker.
			// This will be displayed on taping the marker
			// markerOptions.title(name + " : " + vicinity);

			// Placing a marker on the touched position
			// mGoogleMap.addMarker(markerOptions);

			// mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			// mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

			// TextView nearestMRTLoc = (TextView)
			// findViewById(R.id.NearestMRTLocation);
			// nearestMRTLoc.setText(nearestMRT);
			// }
			String distance = "testing";
			String duration = "testing";
			String polyline = "-NA-";

			// Getting a place from the places list
			if (routes != null && !routes.isEmpty()) {
				HashMap<String, String> hmRoute = routes.get(0);// only getting
																// the first one
				// Getting distance to MRT station/ length of route
				distance = hmRoute.get("distance");
				duration = hmRoute.get("duration");
				polyline = hmRoute.get("polyline");

				Log.i(DEBUG, "distance is " + distance
						+ " and walking time is " + duration);
				// runOnUiThread(new Runnable() {
				// public void run() {

				// stuff that updates ui
				// wd = (TextView) findViewById(R.id.WalkingDistance);
				wd.setText(distance);

				// wt = (TextView) findViewById(R.id.WalkingTime);
				wt.setText(duration);

				// }
				// });
				Log.i(DEBUG, "overview polyline is " + polyline);
				List<LatLng> polyPoints = decodePoly(polyline);

				// Instantiates a new Polygon object and adds points to define a
				// rectangle
				PolylineOptions rectOptions = new PolylineOptions();

				for (int i = 0; i < polyPoints.size() - 1; i++) {
					rectOptions.add(polyPoints.get(i));

					/*
					 * LatLng src = polyPoints.get(i); LatLng dest =
					 * polyPoints.get(i + 1); Polyline line =
					 * mGoogleMap.addPolyline(new PolylineOptions() .add(new
					 * LatLng(src.latitude, src.longitude), new
					 * LatLng(dest.latitude, dest.longitude))
					 * .width(25).color(Color.RED).geodesic(true));
					 */
				}

				// Get back the mutable Polygon
				// rectOptions.color(Color.BLUE).geodesic(true);

				Polyline line = gooleMap.addPolyline(rectOptions.color(
						Color.BLUE).geodesic(true));
				// Log.i(DEBUG, "why is nothing drawn "+line.isVisible());

			} else
				Log.i(DEBUG, "not getting anything");

		}

	}

	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			// LatLng p = new LatLng((int) (((double) lat / 1E5) * 1E6), (int)
			// (((double) lng / 1E5) * 1E6));
			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));

			poly.add(p);
		}

		return poly;
	}

}
