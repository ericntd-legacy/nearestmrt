package com.fuzzybee7.entities;

public class Direction {
	private String distance;
	private String duration;
	private String polyLine;
	
	public Direction(String distance, String duration, String polyLine) {
		this.distance = distance;
		this.duration = duration;
		this.polyLine = polyLine;
	}
	
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getPolyLine() {
		return polyLine;
	}
	public void setPolyLine(String polyLine) {
		this.polyLine = polyLine;
	}
}
