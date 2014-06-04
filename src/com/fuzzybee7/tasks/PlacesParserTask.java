package com.fuzzybee7.tasks;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.fuzzybee7.entities.MRT;
import com.fuzzybee7.nmrt.R;
import com.fuzzybee7.parsers.PlaceJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/** A class to parse the Google Places in JSON format */
public class PlacesParserTask extends
		AsyncTask<String, Integer, List<HashMap<String, String>>> {
	private static final String TAG = "PlacesParserTask";
	
//	private final byte RESULT_FAILED = 1;
//	private final byte RESULT_OK = 2;
	
	JSONObject jObject;
	private PlacesParserTaskCallback callback;
	//  private byte status;
	
	public PlacesParserTask(PlacesParserTaskCallback callback) {
		this.callback = callback;
	}

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
			Log.d(TAG, e.toString());
		}
		return places;
	}

	// Executed after the complete execution of doInBackground() method
	@Override
	protected void onPostExecute(List<HashMap<String, String>> list) {
		Log.i(TAG, "displaying the nearest mrt station");

		// Clears all the existing markers
		// gooleMap.clear();
//		this.callback.clearMap();
//
//		// add a balloon showing the current location or the start point of
//		// the walking path
////		Marker startMark = gooleMap.addMarker(new MarkerOptions().position(
////				curLoc).title("You are here"));
//		Marker startMark = this.callback.addMarker();
//		
//		startMark.showInfoWindow();

		if (list!=null&&list.size() > 0) {
			MRT mrt = new MRT();
			
			/*
			 * Getting a place from the places list only the last one in the last
			 * which is the nearest for now
			 */
			HashMap<String, String> hmPlace = list.get(list.size() - 1);// only
																		// getting
																		// the
																		// last
																		// one

			final String PLACE_NAME = "place_name";
			final String PLACE_VICINITY = "vicinity";
			// Getting name
			String name = hmPlace.get(PLACE_NAME);
			Log.w(TAG, "MRT name is " + name);
			mrt.setName(name);

			// Getting vicinity
			String vicinity = hmPlace.get(PLACE_VICINITY);
			Log.w(TAG, "MRT vicinity is " + vicinity);
			
			// Getting latitude of the place
			 double lat = Double.parseDouble(hmPlace.get("lat"));
			
			 // Getting longitude of the place
			 double lng = Double.parseDouble(hmPlace.get("lng"));
			
			 LatLng latLng = new LatLng(lat, lng);
			 mrt.setLatLng(latLng);

			this.callback.onNearestMRTFound(mrt);
			
			// Creating a marker
//			MarkerOptions markerOptions = new MarkerOptions();
//
//			// Getting a place from the places list
//			HashMap<String, String> hmPlace = list.get(list.size() - 1);// only
//																		// getting
//																		// the
//																		// last
//																		// one
//
//			// Getting name
//			String name = hmPlace.get("place_name");
//
//			// Getting vicinity
//			String vicinity = hmPlace.get("vicinity");
//
//			String nearestMRT = name + " MRT Station";// +vicinity;
//
//			// Show nearest MRT station in text
////			nearestMRTLoc.setText("Stack Overflow is a question and answer site for professional and enthusiast programmers. It's 100% free, no registration required.");
////			nearestMRTLoc.setTextAppearance(getApplicationContext(),
////					R.style.MyBoldText);
//			this.callback.updateNearestMRT();
//
//			// Getting latitude of the place
//			double lat = Double.parseDouble(hmPlace.get("lat"));
//
//			// Getting longitude of the place
//			double lng = Double.parseDouble(hmPlace.get("lng"));
//
//			LatLng latLng = new LatLng(lat, lng);
//
//			// Getting the direction to the nearest MRT station
//			/*
//			 * nearestMRTSettings = getSharedPreferences(NEARESTMRT_PREFS,
//			 * Context.MODE_PRIVATE); double curLat =
//			 * Double.parseDouble(nearestMRTSettings.getString("curLat",
//			 * DEF_LAT)); double curLon =
//			 * Double.parseDouble(nearestMRTSettings.getString("curLon",
//			 * DEF_LON)); LatLng curLatLng = new LatLng(curLat, curLon);
//			 */
//			// getPathToMRT(curLoc, latLng);
//			this.callback.getPathToMRT();
//
//			// Setting the position for the marker
//			markerOptions.position(latLng);
//
//			// Setting the title for the marker.
//			// This will be displayed on taping the marker
//			markerOptions.title(name + " : " + vicinity);
//
//			// Placing a marker on the touched position
//			// Marker mrtMark = gooleMap.addMarker(markerOptions);
//			Marker mrtMark = this.callback.addMarker();
//			// mrtMark.showInfoWindow();
//
//			// gooleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//			this.callback.moveCamera();
//			// mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//
//			Log.i(TAG, "the stations found, furthest first, are:");
//			for (int i = 0; i < list.size(); i++) {
//				hmPlace = list.get(i);
//				name = hmPlace.get("place_name");
//				Log.i(TAG, (i + 1) + ')' + name);
//			}
		} else {
			Log.i(TAG, "couldn't find any MRT stations");
			this.callback.onError();
		}

	}

}
