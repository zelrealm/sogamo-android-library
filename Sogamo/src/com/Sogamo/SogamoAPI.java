package com.Sogamo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.Sogamo.database.Database;
import com.Sogamo.database.EventMaster;
import com.Sogamo.database.SessionMaster;
import com.Sogamo.database.SettingsData;
import com.Sogamo.util.APIDefinitions;
import com.Sogamo.util.AppFocusObserver;
import com.Sogamo.util.AppFocusObserver.AppFocusListener;
import com.Sogamo.util.InternetUtil;
import com.Sogamo.util.Plist;
import com.Sogamo.util.TaskQueue;
import com.Sogamo.util.Utils;
import com.Sogamo.util.XmlParseException;

public class SogamoAPI implements AppFocusListener {

	private String TAG = SogamoAPI.class.getSimpleName(); // Identify Class from

	// uniquely identify player
	private String _playerId = null;

	// player details
	private HashMap<?, ?> _playerDetail = null;

	// store API Key
	private String _apiKey;

	// store current session data
	private static SogamoSession _currentSession = null;

	// stores API Definition Version
	private static float _apiDefinitionsVersion = -1;

	// Store context object
	private Context context;

	// contains all plist data in hashMap object.
	private HashMap<?, ?> _plistData = null;

	// Static instance of the API will be used while calling public functions
	private static SogamoAPI _sogamoAPI = null;

	// Application Focus observer member instance
	private AppFocusObserver _appFocusObserver = null;

	// constant string to detect
	public static String DEVICE_UUID = "DEVICE_UUID";

	// A short task queue and executor for internal command needs.
	private ScheduledExecutorService _scheduledExecutorService = null;
	TaskQueue _taskQueue;

	public static SogamoAPI getInstance(Context context) {
		if (_sogamoAPI == null) {
			_sogamoAPI = new SogamoAPI(context);
		}
		return _sogamoAPI;

	}

	/**
     * constructor which initialize context object
     * @param mContext context of Application.
     */
	private SogamoAPI(Context mContext) {
		context = mContext;

		if (_taskQueue == null) {
			_taskQueue = new TaskQueue(context);
		}

	}
	
	/**
     * public function to set flush interval of the session events 
     * @param timeinterval time interval after which events are flushed.
     */
	public void setFlushInterval(int timeinterval) {
		if (_scheduledExecutorService == null) {
			_scheduledExecutorService = Executors
					.newSingleThreadScheduledExecutor();
			_scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					_taskQueue.add(new Runnable() {

						@Override
						public void run() {
							uploadSessionEventsInBatch();
						}
					});
				}
			}, timeinterval, timeinterval, TimeUnit.SECONDS);
		}

	}
	
	/**
     * public function to start a session 
     * @param anAPIKey API key for application
     * 		  playerId Id of player
     * 		  playerDetail player Details
     */
	
	public void startSessionWithAPIKey(String anAPIKey, String playerId, HashMap<?, ?> playerDetail) throws SogamoException {
		_apiKey = anAPIKey;

		if (playerId == null) {
			throw new SogamoException("No Player Id Found");
		} else {
			// Check if player player Id is "DEVICE_UUID" if true replace with IMEI of device
			if (playerId.compareTo(DEVICE_UUID) == 0) {
				_playerId = SogamoDeviceUtilities.macAddress(context);
			} else {
				_playerId = playerId;
			}
		}
		
		if (playerDetail != null)
			_playerDetail = playerDetail;

		// Register for notifications
		_appFocusObserver = new AppFocusObserver(context);
		_appFocusObserver.setListener(this);
		_appFocusObserver.start();

		_taskQueue.add(new Runnable() {
			@Override
			public void run() {
				internalStartSessionWithAPIKey(_apiKey, _playerId, _playerDetail);
			}
		});
	}
	/**
     * public function to get Suggestion from server 
     * @param suggestionType suggestion type
     */
	public String getSuggestion(String suggestionType) throws SogamoException {
		if (_playerId == null) {
			throw new SogamoException("No Player Id Found");
		}

		if (_currentSession == null) {
			throw new SogamoException("No Session Exists");
		}

		String suggestionString = "";
		if (InternetUtil.haveNetworkConnection(context)) {
			String response = InternetUtil.getServerData("http://"
					+ _currentSession.get_suggestionUrl()
					+ String.format("apiKey=%S&playerId=%S&suggestionType=%S",
							_apiKey, _playerId, suggestionType));
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				suggestionString = jsonObject
						.getString(SogamoConstant.DEFINITIONS_DATA_SUGGESTION_KEY);
			} catch (JSONException e) {
				Log.i(TAG, "Server Response is not in valid Format");
				throw new SogamoException(
						"Server Response is not in valid Format");
			}
		} else {
			throw new SogamoException("Server Unreachable.");
		}

		return suggestionString;
	}
	/**
     * public function to start a session 
     * @param anAPIKey API key for application
     * 		  playerId Id of player
     * 		  playerDetail player Details
     */
	public void trackEventWithName(final String eventName,
			final HashMap<String, Object> params) throws SogamoException {

		if (_playerId == null) {
			String errorMsg = "No Player Id Found";
			Log.e(TAG, errorMsg);
			throw new SogamoException(errorMsg);
		}

		if (_currentSession == null) {
			String errorMsg = "A Sogamo session must be created with startSessionWithAPIKey: before any events can be tracked!";
			Log.e(TAG, errorMsg);
			throw new SogamoException(errorMsg);
		}

		if (eventName == null) {
			String errorMsg = "Event Name cannot be null!";
			Log.e(TAG, errorMsg);
			throw new SogamoException(errorMsg);
		}

		if (params == null) {
			String errorMsg = "Event Params cannot be null!";
			Log.e(TAG, errorMsg);
			throw new SogamoException(errorMsg);
		}

		_taskQueue.add(new Runnable() {

			@Override
			public void run() {
				internalTrackEventWithName(eventName, params);
			}
		});
	}
	
	/*******************************************************
	 * 
	 * Call back function for application focus observer
	 * 
	 * *****************************************************/
	/**
     * call back function for application focus changed observer
     * @param isForeground true if application comes to foreground else false
     */
	
	@Override
	public void appFocusChanged(boolean isForeground) {
		Log.i(TAG, "Application Focus Changed " + isForeground);
		if (isForeground) {
			_taskQueue.add(new Runnable() {

				@Override
				public void run() {
					loadPersistedSessionData();
				}
			});

		} else {
			if (InternetUtil.haveNetworkConnection(context)) {
				Log.i(TAG, "Network found. Uploading Session.");
				_taskQueue.add(new Runnable() {

					@Override
					public void run() {
						uploadSessionEventsInBatch();
					}
				});
			} else {
				Log.i(TAG, "No Network found. Skipping Upload of Session.");
			}

		}
	}



	/*******************************************************
	 * 
	 * Internal Functions
	 * 
	 * *****************************************************/
	/**
     * internal function to start a session 
     * @param anAPIKey API key for application
     * 		  playerId Id of player
     * 		  playerDetail player Details
     */
	private void internalStartSessionWithAPIKey(String anAPIKey,
			String playerId, HashMap<?, ?> playerDetail) {
		try {
			_plistData = loadAPIDefinitions();
		} catch (XmlParseException e) {
			// Failed to load definition file due to Xml Parse Exception
			Log.e(TAG, "Loading API Definiation failed with Exception");
		}

		loadPersistedSessionData();
		/*
		 * Check whether session is expired or not hasCurrentSessionExpired()
		 * returns false if previous session data is not expired otherwise
		 * create new object
		 */
		if (hasSessionExpired(_currentSession) == false) {
			Log.d(TAG,
					"Current session is still valid. No new session key required");
		} else {
			// Create New Session object or when session expired
			Log.d(TAG, "No session detected. Creating a new one...");
			_currentSession = null;

			// Check whether Internet connection available or not
			if (InternetUtil.haveNetworkConnection(context)) {
				Log.d(TAG, "Network Found. Getting new session key..");
				_currentSession = authenticateWithAPIKeyAndPlayerId(_apiKey,
						_playerId);
				// Check to see if new Session is created properly
				if (_currentSession == null) {
					// if new session is note created create a offline session
					_currentSession = createOfflineSession();
				} else {
					// New Session is created save session
					saveSesstion(_currentSession);
					// and update last login time
					setLastLoginTime();
				}
			} else {
				Log.d(TAG, "No Network Found. Create Offline session..");
				_currentSession = createOfflineSession();
			}
		}

		internalTrackEventWithName("session",
				_currentSession.convertDictionary());
	}

	/**
     * Load plist file in hashmap object
     * @param
     */
	private HashMap<?, ?> loadAPIDefinitions() throws XmlParseException {
		// String _plistXML = converXMLtoString();
		String _plistXML = APIDefinitions.XMLContent;
		// If null then error generate in reading plist file.
		HashMap<?, ?> definitions = (HashMap<?, ?>) Plist
				.objectFromXml(_plistXML);
		return definitions;
	}


	/**
     * load previously saved session data
     * @param
     */
	private void loadPersistedSessionData() {
		// Crate Database to store Session Data
		Database database = new Database(context);
		database.createTable(SessionMaster.CREATE_TABLE);

		// Fetch Session data from Database
		ArrayList<ContentValues> _sessionList = database
				.SelectData("SELECT * FROM " + SessionMaster.TABLE_NAME);

		// Check if data stored in Database or not
		if (_sessionList.size() > 0) {

			// get Previous session object
			ContentValues values = _sessionList.get(0);

			// Initialize current session object from previous session object
			// data
			_currentSession = new SogamoSession();
			_currentSession = _currentSession.init(values
					.getAsString(SessionMaster.SESSION_ID), values
					.getAsString(SessionMaster.PLAYER_ID), values
					.getAsInteger(SessionMaster.GAME_ID), values
					.getAsString(SessionMaster.LOG_URL), values
					.getAsString(SessionMaster.SUGGESTION_URL), values
					.getAsString(SessionMaster.OFFLINE).equals("true"));
		} else {
			Log.d(TAG, " No stored sessions found");
		}
	}

	/**
     * store current session in database
     * @param sogamoSession session to be saved.
     */
	private void saveSesstion(SogamoSession sogamoSession) {
		ContentValues values = new ContentValues();
		values.put(SessionMaster.GAME_ID, sogamoSession.get_gameId());
		values.put(SessionMaster.LOG_URL, sogamoSession.get_logCallectorUrl());
		values.put(SessionMaster.OFFLINE, sogamoSession.is_isOfflineSession());
		values.put(SessionMaster.PLAYER_ID, sogamoSession.get_playerId());
		values.put(SessionMaster.SESSION_ID, sogamoSession.get_sessionId());
		values.put(SessionMaster.SUGGESTION_URL,
				sogamoSession.get_suggestionUrl());
		values.put(SessionMaster.STARTDATE, sogamoSession.get_startDate() + "");

		Database database = new Database(context);
		database.createTable(SessionMaster.CREATE_TABLE);
		if (database.count("Select * from " + SessionMaster.TABLE_NAME) == 0)
			database.insert(values, SessionMaster.TABLE_NAME);
		else
			database.update(values, SessionMaster.TABLE_NAME, null);
	}
	
	/**
     * create an offline session
     * @param 
     * @return new offline session
     */
	private SogamoSession createOfflineSession() {
		SogamoSession session = new SogamoSession().init(Utils.getDateTime(),
				"", -1, "", "", true);

		return session;
	}

	/**
     * check to see if session is expired or not
     * @param sogamoSession session expire need to be checked 
     * @return 'false' if session is expired else 'true' 
     */
	private boolean hasSessionExpired(SogamoSession sogamoSession) {
		boolean sessionExpired = true;
		if (sogamoSession != null) {
			long difference = sogamoSession.get_startDate().getTime()
					- new Date().getTime();
			int days = (int) (difference / (1000 * 60 * 60 * 24));
			int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
			sessionExpired = (hours <= SogamoConstant.SESSION_EXPIRED_TIME_HOURS);
			Log.i(TAG, "Session Expired ? " + sessionExpired);
		} else {
			Log.d(TAG, " No session found");
		}

		return sessionExpired;
	}
	
	/**
     * contact authentication server to get new session
     * @param apiKey Application provided API key
     * 		  playerId Id of player of application
     * @return New Session Id
     */
	
	private SogamoSession authenticateWithAPIKeyAndPlayerId(String apiKey,
			String playerId) {

		SogamoSession newSession = null;
		String urlData = _apiKey + "&playerId=" + _playerId;

		String lastLogInTime = getLastLoginTime();
		if (lastLogInTime.length() > 0) {
			urlData += "&last_active_datetime=" + lastLogInTime;
		}

		urlData += "&login_datetime="
				+ String.format("%d", Utils.getUnixTimeStamp());

		String url = SogamoConstant.AUTHENTICATION_SERVER_URL + "?apiKey="
				+ urlData;

		// get current session data
		String res = InternetUtil.getServerData(url);
		if (res.startsWith("error")) {
			Log.d(TAG, "Request Error " + res.substring(res.indexOf(" ")));
		} else {
			newSession = parseAuthenticationServerResponse(res, playerId);
		}

		return newSession;
	}


	/**
     * Return last login time from settings database
     * @param 
     * @return last login time
     */
	private String getLastLoginTime() {
		String lastLoginTime = "";
		Database database = new Database(context);
		database.createTable(SettingsData.CREATE_TABLE);
		ArrayList<ContentValues> settings = database
				.SelectData("SELECT * FROM " + SettingsData.TABLE_NAME);
		for (int index = 0; index < settings.size(); index++) {
			ContentValues setting = settings.get(index);
			if (setting.getAsString(SettingsData.SETTINGS_KEY).compareTo(
					SettingsData.SETTING_LAST_LOGIN_TIME) == 0) {
				lastLoginTime = setting
						.getAsString(SettingsData.SETTINGS_VALUE);
				break;
			}
		}
		return lastLoginTime;
	}

	/**
     * Save current time as last login time to database
     * @param 
     */
	private void setLastLoginTime() {
		ContentValues setting = new ContentValues();
		setting.put(SettingsData.SETTINGS_KEY,
				SettingsData.SETTING_LAST_LOGIN_TIME);
		setting.put(SettingsData.SETTINGS_VALUE,
				String.format("%d", Utils.getUnixTimeStamp()));

		Database database = new Database(context);
		database.createTable(SettingsData.CREATE_TABLE);
		database.saveSettings(setting);
	}

	/**
     * parse response to generate Session.
     * @param res response from authentication server
     * 		  playerId id of player
     */
	private SogamoSession parseAuthenticationServerResponse(String res,
			String playerId) {
		SogamoSession sogamoSession = null;
		try {

			JSONObject jsonObject = new JSONObject(res);
			int gameId = jsonObject
					.getInt(SogamoConstant.SESSIONS_DATA_GAME_ID_KEY);
			String sessionId = jsonObject
					.getString(SogamoConstant.SESSIONS_DATA_SESSION_ID_KEY);
			String suggestionUrl = jsonObject
					.getString(SogamoConstant.SESSIONS_DATA_SUGGESTION_URL_KEY);
			String lcUrl = jsonObject
					.getString(SogamoConstant.SESSIONS_DATA_LOG_COLLECTOR_URL_KEY);

			sogamoSession = new SogamoSession();
			sogamoSession = sogamoSession.init(sessionId, playerId, gameId,
					lcUrl, suggestionUrl, false);

		} catch (Exception e) {
			Log.d(TAG, "Parse Authentication " + e.toString());
		}

		return sogamoSession;
	}

	/**
     * internal function to track events.
     * @param eventName name of the event
     * 		  params event params
     */	
	private void internalTrackEventWithName(String eventName, HashMap<String, Object> param) {
		SogamoEvent newEvent = autoFillEventParams(eventName, param,
				_currentSession);
		if (newEvent != null) {
			if (validateEvent(newEvent) == true) {
				Log.i(TAG, "Event is valid ! " + eventName );
				saveEventForSession(_currentSession,newEvent);
			} else {
				Log.d(TAG, eventName + " event failed to be tracked! [Validation Failed]");
			}
		} else {
			Log.d(TAG, eventName + " event failed to be tracked! [Create or Autofill Failed]");
		}

	}

	/**
     * auto-fill event params if possible from current session information.
     * @param eventName name of the event
     * 		  params event params
     * 		  session current session
     * @return autofilled session event.
     */
	private SogamoEvent autoFillEventParams(String eventName,
			HashMap<String, Object> params, SogamoSession session) {

		String eventIndex = getEventIndexForName(eventName);
		if (eventIndex != null) {
			Map<?, ?> eventsMap = (Map<?, ?>) _plistData
					.get(SogamoConstant.DEFINITIONS_DATA_API_DEFINITIONS_KEY);
			Map<?, ?> event = (Map<?, ?>) eventsMap.get(eventName);
			Map<?, ?> paramsList = (Map<?, ?>) event.get("parameters");
			Iterator event_detail_key = paramsList.keySet().iterator();

			while (event_detail_key.hasNext()) {
				String param_detail_key_name = (String) event_detail_key.next();
				Map<?, ?> paramItem = (Map<?, ?>) paramsList
						.get(param_detail_key_name);
				Boolean isRequired = (Boolean) paramItem.get("required");
				if (isRequired) {
					if ((param_detail_key_name.compareTo("session_id") == 0)
							|| (param_detail_key_name.compareTo("sessionId") == 0)) {
						params.put(param_detail_key_name,
								session.get_sessionId());
					}
					if ((param_detail_key_name.compareTo("game_id") == 0)
							|| (param_detail_key_name.compareTo("gameId") == 0)) {
						params.put(param_detail_key_name,
								String.format("%d", session.get_gameId()));
					}
					if ((param_detail_key_name.compareTo("player_id") == 0)
							|| (param_detail_key_name.compareTo("playerId") == 0)) {
						params.put(param_detail_key_name,
								session.get_playerId());
					}
					if (param_detail_key_name.compareTo("login_datetime") == 0) {
						params.put(param_detail_key_name,
								session.get_startDate());
					}
					if (param_detail_key_name.compareTo("logDatetime") == 0) {
						params.put(param_detail_key_name,
								new Date(System.currentTimeMillis()));
					}
					if (param_detail_key_name.compareTo("updatedDatetime") == 0) {
						params.put(param_detail_key_name,
								new Date(System.currentTimeMillis()));
					}
					if (param_detail_key_name.compareTo("last_active_datetime") == 0) {
						params.put(param_detail_key_name,
								new Date(System.currentTimeMillis()));
					}
				}
			}
		}

		SogamoEvent sogamoEvent = SogamoEvent.init(eventName, eventIndex,
				params);
		return sogamoEvent;
	}

	/**
     * validates passed event with API definition plist data.
     * @param sogamoEvent event to be validated
     * @return true for valid event otherwise false.
     */
	private boolean validateEvent(SogamoEvent sogamoEvent) {
		if (_plistData == null)
			return false;

		// Load all event detail in "_envent"
		Map<?, ?> _event = (Map<?, ?>) _plistData
				.get(SogamoConstant.DEFINITIONS_DATA_API_DEFINITIONS_KEY);

		// Check event name is valid or not
		if (_event.containsKey(sogamoEvent.getEventName())) {

			HashMap<String, Object> params = sogamoEvent.getParams();
			String eventName = sogamoEvent.getEventName();

			String _eventIndex = (String) ((Map<?, ?>) _event.get(eventName))
					.get("event_index");
			if (sogamoEvent.getEventId().compareTo(_eventIndex) != 0) {
				Log.e(TAG, eventName + " paramKey detail not found");
				return false;
			}

			Iterator event_parameters_key = ((Map<?, ?>) ((Map<?, ?>) _event
					.get(sogamoEvent.getEventName()))
					.get(SogamoConstant.DEFINITIONS_DATA_PARAMETERS_KEY))
					.keySet().iterator();

			while (event_parameters_key.hasNext()) {

				// Get Parameter name or identifier in "type"
				String type = (String) event_parameters_key.next();

				Map<?, ?> _plist_paramters_detail = ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) _event
						.get(eventName))
						.get(SogamoConstant.DEFINITIONS_DATA_PARAMETERS_KEY))
						.get(type));

				// Check particular parameter is required or optional
				if (((Boolean) _plist_paramters_detail
						.get(SogamoConstant.DEFINITIONS_DATA_REQUIRED_KEY))
						.booleanValue()) {
					if (!params.containsKey(type)) {
						Log.e(TAG, "given event's " + type
								+ " required paramter not found");
						return false;
					}
				}

				// Check for parameter's data type
				String datatype = (String) _plist_paramters_detail
						.get(SogamoConstant.DEFINITIONS_DATA_TYPE_KEY);

				if (params != null) {
					Object obj = params
							.get(SogamoConstant.DEFINITIONS_DATA_TYPE_KEY);
					if (obj != null) {
						boolean flagDatatype = false;
						if (datatype.equals("NSString")
								&& obj instanceof String)
							flagDatatype = true;
						else if (datatype.equals("NSDate")
								&& (obj instanceof Date || obj instanceof java.sql.Date))
							flagDatatype = true;
						else if (datatype.equals("NSNumber")
								&& (obj instanceof Integer || obj instanceof Long))
							flagDatatype = true;

						if (flagDatatype == false) {
							Log.e(TAG, type + "'s data type mismatch");
							return false;
						}
					}
				}
			}
		}

		return true;
	}
	/**
     * searches a specified event in API definition and return its index.
     * @param eventName event name to be searched
     * @return index of the passed event if not found null.
     */
	private String getEventIndexForName(String eventName) {
		Map<?, ?> eventsMap = (Map<?, ?>) _plistData
				.get(SogamoConstant.DEFINITIONS_DATA_API_DEFINITIONS_KEY);

		if (eventsMap == null) {
			Log.d(TAG, "API Definitions data is missing!");
			return null;
		}

		String eventIndex = null;

		if (eventsMap.containsKey(eventName)) {
			Map<?, ?> event = (Map<?, ?>) eventsMap.get(eventName);
			eventIndex = (String) event.get("event_index");
		} else {
			Log.d(TAG, "No such event Name!");
		}

		return eventIndex;
	}
	
	/**
     * Save an Event for a specified Session to Database.
     * @param sogamoSession session to which the event will be saved
     * 		  sogamoEvent event to be saved for session
     */
	private void saveEventForSession(SogamoSession sogamoSession, SogamoEvent sogamoEvent) {
		if(sogamoEvent == null) {
			Log.e(TAG, "Event is Null. Not saving Event");
		} else {
			try {
				// Convent Session Event to Json before saving to database
				String eventJSON = sogamoEvent.getJson(sogamoSession.get_gameId());

				Database database = new Database(context);
				database.createTable(EventMaster.CREATE_TABLE);

				ContentValues values = new ContentValues();
				values.put(EventMaster.SESSION_ID, sogamoSession.get_sessionId());
				values.put(EventMaster.EVENT_ID, sogamoEvent.getEventId());
				values.put(EventMaster.EVENT_NAME, sogamoEvent.getEventName());
				values.put(EventMaster.EVENT_ACTION, eventJSON);

				database.insert(values, EventMaster.TABLE_NAME);
				Log.d(TAG, sogamoEvent.getEventName() + " event successfully tracked!");
			} catch (JSONException e) {
				Log.d(TAG, sogamoEvent.getEventName() + " event failed to be tracked! [JSON conversion Error.]");
			}			
		}

	}
	
	/**
     * Upload Session events to session collector url in batch format.
     * @param 
     */
	private void uploadSessionEventsInBatch() {
		Log.i(TAG, "Starting to Upload data in batch Format");
		Database database = new Database(context);
		ArrayList<ContentValues> sessionList = database.SelectData("SELECT * FROM " 
												+ SessionMaster.TABLE_NAME);
		Log.i(TAG, "No of Sessions Found are " + sessionList.size());
		for(int sessionIndex=0; sessionIndex < sessionList.size(); sessionIndex++) {
			ContentValues sessionContents = sessionList.get(sessionIndex);
			String sessionId = sessionContents.getAsString(SessionMaster.SESSION_ID);
			String logCollectorBaseUrl = sessionContents.getAsString(SessionMaster.LOG_URL);
			
			// Read All Session Events Corresponding to session ID.
			ArrayList<ContentValues> eventsList = database.SelectData("SELECT * FROM "
													+ EventMaster.TABLE_NAME 
													+ " WHERE " + EventMaster.SESSION_ID
													+ "=\'" + sessionId + "\'");
			Log.i(TAG, "No of Session Events Found are " + eventsList.size() + " for session id " + sessionId);
			
			// Prepare Date in format of [?index1=data1&index2=data2...]
			String data = "";
			for (int eventIndex = 0; eventIndex < eventsList.size(); eventIndex++) {
				try {
					ContentValues eventContents = eventsList.get(eventIndex);
					String eventAction = eventContents.getAsString(EventMaster.EVENT_ACTION);
					// URL encode session event before adding to data
					String eventData = eventIndex + "=" + URLEncoder.encode(eventAction, "UTF8");
					// add & only to eventData after 0th event.
					if(eventIndex != 0) {
						data += "&"+eventData;
					}
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "Session Event Url Encoding Failed. Skipping");
				}
			}
			// Create Data collector Url with data in batch upload format
			String dataCollectionUrlWithData = String.format("http://%Sbatch?%S", logCollectorBaseUrl, data);
			// issue http get call to Data collector Url
			String response = InternetUtil.getServerData(dataCollectionUrlWithData);
			Log.i(TAG, "Event Data Upload Response " + response);
			// Check for any error in data uploading
			if (response.startsWith("error")) {
				Log.e(TAG, "Data Upload Error");
			} else {
				// Delete Session Events from DB for sessionId
				database.delete(EventMaster.TABLE_NAME, EventMaster.SESSION_ID + " = \'" + sessionId + "\'");
				// Check if deleting session is current session if not delete from DB
				if(_currentSession.get_sessionId().compareTo(sessionId) != 0) {
					database.delete(SessionMaster.TABLE_NAME, EventMaster.SESSION_ID + " = \'" + sessionId + "\'");	
				}	
			}
			
		}
	}
}
