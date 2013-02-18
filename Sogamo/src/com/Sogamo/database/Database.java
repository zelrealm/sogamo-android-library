package com.Sogamo.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
	private static final String TAG = Database.class.getSimpleName();
	private Context _context;
	private SQLiteDatabase _db;
	private DatabaseHelper _dbHelper;

	public Database(Context context) {
		_context = context;
	}

	private void openConnnection() {
		_dbHelper = new DatabaseHelper(this._context);
		_db = _dbHelper.getWritableDatabase();
	}

	private void closeConnnection() {
		_dbHelper.close();
	}

	public boolean createTable(String sql) {
		openConnnection();
		try {
			_db.execSQL(sql);
			closeConnnection();
			return true;

		} catch (Exception e) {
			// String s="";
		}
		closeConnnection();
		return false;

	}

	public boolean insert(ContentValues values, String TABLE_NAME) {
		openConnnection();

		long a = _db.insert(TABLE_NAME, null, values);
		closeConnnection();
		return a > 0 ? true : false;
	}

	public boolean update(ContentValues values, String TABLE_NAME,
			String whereClause) {
		openConnnection();

		long a = _db.update(TABLE_NAME, values, whereClause, null);
		closeConnnection();
		return a > 0 ? true : false;
	}

	public boolean delete(String TABLE_NAME, String whereClause) {
		openConnnection();

		long a = _db.delete(TABLE_NAME, whereClause, null);
		closeConnnection();
		return a > 0 ? true : false;
	}

	public ArrayList<ContentValues> SelectData(String query) {
		Log.i(TAG, query);
		openConnnection();
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		Cursor cursor = _db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			do {
				ContentValues values = new ContentValues();
				for (int i = 0; i < cursor.getColumnCount(); ++i) {
					values.put(cursor.getColumnName(i), cursor.getString(i));
				}
				list.add(values);
			} while (cursor.moveToNext());

		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		closeConnnection();
		return list;
	}

	public ArrayList<ContentValues> SelectData(String query, int limit) {
		openConnnection();
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		Cursor cursor = _db.rawQuery(query, null);

		if (limit > cursor.getCount())
			limit = cursor.getCount();
		int counter = 1;
		if (cursor.moveToFirst()) {
			do {
				ContentValues values = new ContentValues();
				for (int i = 0; i < cursor.getColumnCount(); ++i) {
					values.put(cursor.getColumnName(i), cursor.getString(i));
				}
				list.add(values);
				if (counter == limit)
					break;
			} while (cursor.moveToNext());

		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		closeConnnection();
		return list;
	}

	public int count(String qry) {
		openConnnection();
		Cursor cursor = _db.rawQuery(qry, null);
		int a = cursor.getCount();
		cursor.close();
		closeConnnection();
		return a;

	}

	public void saveSettings(ContentValues setting) {
		String settingsKey = setting.getAsString(SettingsData.SETTINGS_KEY);
		if (count("SELECT * FROM " + SettingsData.TABLE_NAME + " WHERE "
				+ SettingsData.SETTINGS_KEY + "=\'" + settingsKey + "\'") == 0) {
			insert(setting, SettingsData.TABLE_NAME);
		} else {
			update(setting, SettingsData.TABLE_NAME, null);
		}
	}
}
