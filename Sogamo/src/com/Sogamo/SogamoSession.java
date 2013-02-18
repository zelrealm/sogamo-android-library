package com.Sogamo;

import java.util.Date;
import java.util.HashMap;

//Class contain details of the session and it's parameter.
public class SogamoSession {
	
	private String TAG = SogamoSession.class.getSimpleName(); // Identify Class from Log detail. 
	
	private String _sessionId; /* Unique Session Id */
	private String _playerId; /* End user's Unique Id */ 
	private String _logCallectorUrl; /* Url to send Log or event data */
	private int _gameId; /* Unique id to identify Current Game */ 
	private Date _startDate; /* Current Session Starting Date */ 
	private String _suggestionUrl; /* Current Session Suggestion Url */
	private boolean _isOfflineSession; /* Check whether current session is Online or Offline. True - Offline, False - Online */
	
	private SogamoSession(String aSessionId, String aPlayerId, int aGameId, String lcURL, String aSuggestionUrl, boolean isOffline) {
		// TODO Auto-generated constructor stub
		_sessionId = aSessionId;
		_playerId = aPlayerId;
		_gameId = aGameId;
		_suggestionUrl = aSuggestionUrl;
		_logCallectorUrl = lcURL;
		_isOfflineSession = isOffline;
		_startDate = new Date(System.currentTimeMillis());
	}
	
	public SogamoSession() {
	}
	
	// Constructor
	public SogamoSession init(String aSessionId, String aPlayerId, int aGameId, String lcURL, String aSuggestionUr, boolean isOffline){
		return new SogamoSession(aSessionId, aPlayerId, aGameId, lcURL, aSuggestionUr, isOffline);
	}
	
	

	/**
	 * @return the _sessionId
	 */
	public String get_sessionId() {
		return _sessionId;
	}

	/**
	 * @return the _playerId
	 */
	public String get_playerId() {
		return _playerId;
	}

	/**
	 * @return the _logCallectorUrl
	 */
	public String get_logCallectorUrl() {
		return _logCallectorUrl;
	}

	/**
	 * @return the _gameId
	 */
	public int get_gameId() {
		return _gameId;
	}

	/**
	 * @return the _startDate
	 */
	public Date get_startDate() {
		return _startDate;
	}

	/**
	 * @return the _isOfflineSession
	 */
	public boolean is_isOfflineSession() {
		return _isOfflineSession;
	}
	
	public HashMap<String, Object> convertDictionary()
	{
		HashMap<String, Object> sessionParameter = new HashMap<String, Object>();
		sessionParameter.put(SogamoConstant.SESSIONS_DATA_GAME_ID_KEY, get_gameId());
		sessionParameter.put(SogamoConstant.SESSIONS_DATA_PLAYER_ID_KEY, get_playerId());
		sessionParameter.put(SogamoConstant.SESSIONS_DATA_SESSION_ID_KEY, get_sessionId());
		sessionParameter.put(SogamoConstant.SESSIONS_DATA_SESSION_START_DATE_KEY, get_startDate());
		sessionParameter.put(SogamoConstant.SESSIONS_DATA_SUGGESTION_URL_KEY, get_suggestionUrl());
		return sessionParameter;
	}

	public String get_suggestionUrl() {
		return _suggestionUrl;
	}

	public void set_suggestionUrl(String _suggestionUrl) {
		this._suggestionUrl = _suggestionUrl;
	}
}
