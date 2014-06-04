package com.fuzzybee7.tasks;

import com.fuzzybee7.entities.Direction;

public interface DirectionsParserTaskCallback {
	public void onDirectionsFound(Direction direction);
	public void onError();
}
