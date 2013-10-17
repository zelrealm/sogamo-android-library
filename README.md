# Sogamo Analytics API on Android #

The repository contains two folders:

1. Sogamo - The Sogamo Android Library
2. SogamoTest - A sample application that demonstrates how to use the Sogamo library.

# Requirements #

1. ADT 1.9 or later
2. Android 2.3.3 or later

# Setup #
Adding the Sogamo to your Android project is just a few easy steps:

1. Add the SogamoAPI Framework.
	* Drag and drop the **SogamoAPI.jar** folder into your project.’s lib folder in Eclipse.
2. Add the required permissions for Framework.
	* Open Manifest file of your application.
	* add following lines
		
		```
		<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
		```
		```
		<uses-permission android:name="android.permission.INTERNET"/>
		```
		```
		<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
		```
3. Add ```import com.Sogamo.SogamoAPI;``` to all classes that call SogamoAPI functions 

# Usage #
## Initialization ##
The first thing you need to do is to initialize a SogamoAPI session with your project API key. We recommend doing this in `onCreate() of your Launcher Activty`, with the following method:
	
	SogamoAPI sogamoAPI = SogamoAPI.getInstance(getApplicationContext());
	sogamoAPI.startSessionWithAPIKey(API_KEY, USERS_FACEBOOK_ID_OR_DEVICE_UUID, USERS_ADDITIONAL_DETAILS);
	
	example: sogamoAPI.startSessionWithAPIKey("e45f72965e0042f79b3d8ff24ab96826", "player@facebook.com", null);

You cannot set the facebookId: parameter to null. The function will throw an exception if you do that. If that information is unavailable, please send `sogamoAPI.DEVICE_UUID` instead. We, however, strongly recommend that you include the Facebook ID of the user when starting the session. This will allow you to gain insight into how your users behave across all other Sogamo-linked applications that they use. Obtaining the user's Facebook ID is easy with the [Facebook SDK](https://github.com/facebook/facebook-android-sdk).

## Tracking Events ##
After initializing the SogamoAPI singleton object, you are ready to track events. This can be done with the following method:

	HashMap<String, Object> params = new HashMap<String, Object>();
	params.put("gameId", 1001);
	params.put("playerId", "5001");
	params.put("currencyEarned", 300);
	params.put("currencyBalance", 500);
	params.put("remarks", "");

	SogamoAPI sogamoAPI = SogamoAPI.getInstance(getApplicationContext());
	sogamoAPI.trackEventWithName("playerTopUp", params);

## Flush Tracking Events ##
You can set the Tracking Events Flush interval. This will send all stored Session events to Sogamo Server after each expiry of timeinterval seconds.

	setFlushInterval(int timeinterval)

Where timeinterval: is in Seconds.


##Getting Suggestion##
To get a suggestion from the suggestion server, you can use the following function.

	String getSuggestion(String suggestionType)

Where  suggestionType: is the Type of Suggestion required.
       And return is a xml formated suggestion String 

Note: This function is a blocking (synchronous) call to server so you need to call this function in a thread other then application UI Thread.
Example:
```java
new Thread() {
	public void run() {
		try {
			SogamoAPI sogamoAPI = SogamoAPI.getInstance(getApplicationContext());
			String suggestion = sogamoAPI.getSuggestion("buy");
		} catch (SogamoException e1) {
			e1.printStackTrace();
		}
	};
}.start();
```
