package com.Sogamo.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

	public static String getDateTime() {
		/*
		 * SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		 * return sdf.format(new Date(System.currentTimeMillis()));
		 */

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Date currentLocalTime = cal.getTime();
		//SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyy HH:mm:ss z");
		SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		
		date.setTimeZone(TimeZone.getTimeZone("GMT"));
		String localTime = date.format(currentLocalTime);

		return localTime;
	}
	
	public static int getUnixTimeStamp() {
	    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

	    return (int) (c.getTimeInMillis() / 1000L);
	}
	
	int componentTimeToTimestamp(int year, int month, int day, int hour, int minute) {

	    Calendar c = Calendar.getInstance();
	    c.set(Calendar.YEAR, year);
	    c.set(Calendar.MONTH, month);
	    c.set(Calendar.DAY_OF_MONTH, day);
	    c.set(Calendar.HOUR, hour);
	    c.set(Calendar.MINUTE, minute);
	    c.set(Calendar.SECOND, 0);
	    c.set(Calendar.MILLISECOND, 0);

	    return (int) (c.getTimeInMillis() / 1000L);
	}
	
	public static String getEncodedUri(String originalUri) throws UnsupportedEncodingException {
		String encodedString = URLEncoder.encode(originalUri, "UTF-8");
		if(encodedString.length() > 0) {
			encodedString = encodedString.replace("+", "");
		}
		return encodedString;
	}
}
