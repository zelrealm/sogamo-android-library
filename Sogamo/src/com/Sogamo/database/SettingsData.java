package com.Sogamo.database;

public class SettingsData {
	public static final String SETTINGS_KEY = "KEY";
	public static final String SETTINGS_VALUE = "DATA";
	public static final String SETTING_LAST_LOGIN_TIME = "LAST_LOGIN_TIME";
	public static final String TABLE_NAME = "Settings";
	
	public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + 
			"(" + 
			"_INDEX INTEGER PRIMARY KEY AUTOINCREMENT, " +
			SETTINGS_KEY + " TEXT, " + 
			SETTINGS_VALUE + " TEXT" +
			")";
}
