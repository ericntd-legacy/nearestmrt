package com.fuzzybee7.tasks;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.fuzzybee7.entities.Direction;
import com.fuzzybee7.parsers.DirectionJSONParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/** A class to parse the Google Places in JSON format */
public class DirectionsParserTask extends
		AsyncTask<String, Integer, List<HashMap<String, String>>> {
	private static final String TAG = "DirectionsParserTask";

	JSONObject jObject;
	private DirectionsParserTaskCallback callback;

	public DirectionsParserTask(DirectionsParserTaskCallback callback) {
		this.callback = callback;
	}

	// Invoked by execute() method of this object
	@Override
	protected List<HashMap<String, String>> doInBackground(String... jsonData) {
		// android.os.Debug.waitForDebugger();
		List<HashMap<String, String>> routes = null;
		DirectionJSONParser dirJsonParser = new DirectionJSONParser();
		Log.i(TAG, "trying to parse the response from Google Directions API");
		try {
			jObject = new JSONObject(jsonData[0]);

			/** Getting the parsed data as a List construct */
			routes = dirJsonParser.parse(jObject);

		} catch (Exception e) {
			Log.d(TAG, e.toString());
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
			 
			 Direction direction = new Direction(distance, duration, polyline);
			
			this.callback.onDirectionsFound(direction);

		} else {
			// Log.i(TAG, "not getting anything");
			this.callback.onError();
		}

	}

}
