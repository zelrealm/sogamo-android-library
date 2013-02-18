package com.Sogamo;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;

import com.Sogamo.database.EventMaster;
import com.Sogamo.util.Utils;

public class SogamoEvent {

	private String _eventName; /* Specify a name of Event */ 
	private String _eventId; /* Uniquely identify Event */
	private HashMap<String, Object> _params; /* Contains List of Parameters for particular Event  */
	
	private SogamoEvent(String name, String index, HashMap<String, Object> params) {
		// TODO Auto-generated constructor stub
		setEventId(index);
		setEventName(name);
		setParams(params);
	}
	
	//Constructor
	public static SogamoEvent init(String name, String index, HashMap<String, Object> params ) {
		return new SogamoEvent(name, index, params);
	}

	public String getEventName() {
		return _eventName;
	}

	public void setEventName(String _eventName) {
		this._eventName = _eventName;
	}

	public String getEventId() {
		return _eventId;
	}

	public void setEventId(String _eventId) {
		this._eventId = _eventId;
	}

	public HashMap<String, Object> getParams() {
		return _params;
	}

	public void setParams(HashMap<String, Object> _params) {
		this._params = _params;
	}
	
	public String getJson(int gameId) throws JSONException{
		String actionValue = String.format("%d.%S.%S", gameId, this.getEventName(), this.getEventId());
		JSONObject paramsJson = new JSONObject();
		paramsJson.put("action", actionValue);
		for (String key : this._params.keySet()) {
			Object param_value = this._params.get(key);
			
			if (param_value instanceof String) {
				paramsJson.put(key, (String) param_value);
			} else if (param_value instanceof Date) {
				Date eventDate =(Date)param_value;
				paramsJson.put(key, String.format("%d", eventDate.getTime()));
			} else if (param_value instanceof integer) {
				paramsJson.put(key, String.format("%d", (Integer) param_value));
			} else if (param_value instanceof Float) {
				paramsJson.put(key, String.format("%f", (Float) param_value));
			} else {
				// ToDo more
			}
		}
		
		return paramsJson.toString();
	}
}
