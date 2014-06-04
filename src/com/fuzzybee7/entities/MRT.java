package com.fuzzybee7.entities;

import com.google.android.gms.maps.model.LatLng;

public class MRT {
	private String name;
	private LatLng latLng;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}
}
