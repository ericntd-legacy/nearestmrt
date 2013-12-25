package com.fuzzybee7.nmrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionJSONParser {
	/** Receives a JSONObject and returns a list */
	public List<HashMap<String,String>> parse(JSONObject jObject){		
		
		JSONArray jRoutes = null;
		try {			
			/** Retrieves all the elements in the 'routes' array */
			jRoutes = jObject.getJSONArray("routes");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/** Invoking getRoutes with the array of json object
		 * where each json object represent a place
		 */
		return getRoutes(jRoutes);
	}
	
	
	private List<HashMap<String, String>> getRoutes(JSONArray jRoutes){
		int placesCount = jRoutes.length();
		List<HashMap<String, String>> routeList = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> route = null;	

		/** Taking each place, parses and adds to list object */
		//for(int i=0; i<placesCount;i++){
			try {
				/** Call getPlace with place JSON object to parse the place */
				route = getRoute((JSONObject)jRoutes.get(0));//just get the 1st route
				routeList.add(route);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		//}
		
		return routeList;
	}
	
	/** Parsing the Place JSON object */
	private HashMap<String, String> getRoute(JSONObject jRoute){

		HashMap<String, String> route = new HashMap<String, String>();
		String distance = "";
		String duration = "";
		String polyline = "-NA-";
		
		try {
			
			// Take all legs from the route
			JSONArray legs = jRoute.getJSONArray("legs");
			// Grab first leg
			JSONObject leg = legs.getJSONObject(0);
			
			JSONObject durationObject = leg.getJSONObject("duration");
			duration = durationObject.getString("text");
			
			JSONObject disObj = leg.getJSONObject("distance");
			distance = disObj.getString("text");
			
			route.put("distance", distance);
			route.put("duration", duration);
			
			JSONObject poly = jRoute.getJSONObject("overview_polyline");
			polyline = poly.getString("points");
			route.put("polyline", polyline);
		
		} catch (JSONException e) {			
			e.printStackTrace();
		}		
		return route;
	}
}
