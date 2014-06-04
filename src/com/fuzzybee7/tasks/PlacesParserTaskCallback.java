package com.fuzzybee7.tasks;

import java.util.HashMap;
import java.util.List;

import com.fuzzybee7.entities.MRT;
import com.google.android.gms.maps.model.Marker;

public interface PlacesParserTaskCallback {
	public void clearMap();
	public Marker addMarker();
	// public void updateNearestMRT();
	public void moveCamera();
	public void getPathToMRT();
	
	public void onNearestMRTFound(MRT mrt);
	public void onError();
}
