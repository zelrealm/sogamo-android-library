package com.SogamoTest;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.Sogamo.SogamoAPI;
import com.Sogamo.SogamoException;

public class SogamoTest extends Activity implements OnClickListener,
		OnItemClickListener {

	Button _startButton = null;
	ListView _listView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		_listView = (ListView) findViewById(R.id.listViewId);
		TestEventsAdapter adapter = new TestEventsAdapter(this);
		adapter.getListOfEvents().add("session (new)");
		adapter.getListOfEvents().add("session (update)");
		adapter.getListOfEvents().add("inviteSent");
		adapter.getListOfEvents().add("inviteResponse");
		adapter.getListOfEvents().add("levelUp");
		adapter.getListOfEvents().add("itemChange");
		adapter.getListOfEvents().add("miscExpenditures");
		adapter.getListOfEvents().add("playerTopUp");
		adapter.getListOfEvents().add("payment");
		adapter.getListOfEvents().add("Get Suggestion");

		_listView.setAdapter(adapter);
		_listView.setOnItemClickListener(this);
		_listView.setEnabled(false);

		_startButton = (Button) findViewById(R.id.startButtonId);
		_startButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		SogamoAPI sogamoAPI = SogamoAPI.getInstance(getApplicationContext());
		try {
			sogamoAPI.startSessionWithAPIKey("e45f72965e0042f79b3d8ff24ab96826", "player@facebook.com", null);
			//sogamoAPI.startSessionWithAPIKey("e45f72965e0042f79b3d8ff24ab96826", SogamoAPI.DEVICE_UUID, null);
		} catch (SogamoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_startButton.setEnabled(false);
		_listView.setEnabled(true);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		String eventName = "";
		HashMap<String, Object> params = null;
		
		switch (position) {
		case 0:
			eventName = "session";
			params = new HashMap<String, Object>();
			params.put("gameId", "1001");
			params.put("player_id", "5001");
			params.put("username", "alex");
			params.put("firstname", "Alex");
			params.put("lastname", "Titlyanov");
			params.put("dob", "1-1-1980");
			params.put("email", "5001@gmail.com");
			params.put("gender", "male");
			params.put("relationship_status", "");
			params.put("number_of_friends", "2");
			params.put("status", "New");
			params.put("credit", "0");
			params.put("currency", "");
			break;
		case 1:
			eventName = "session";
			params = new HashMap<String, Object>();
			params.put("gameId", "1001");
			params.put("player_id", "5001");
			break;
		case 2:
			eventName = "inviteSent";
			params = new HashMap<String, Object>();
			params.put("inviteId", "738867");
			params.put("inviteType", "JOIN_GAME");
			params.put("recipientIds", "205,206,207");
			params.put("screenName", "alex");
			params.put("playerId", "5001");
			params.put("attributes", "");
			params.put("credit", "5001");
			params.put("level", "5001");
			params.put("experience", "5001");
			params.put("virtualCurrency", "5001");
			break;
		case 3:
			eventName = "inviteResponse";
			params = new HashMap<String, Object>();
			params.put("inviteId", "73887");
			params.put("respondedPlayerId", "205");
			params.put("responseDatetime", "12/12/2012");
			params.put("respondedPlayerStatus", "1");
			break;
		case 4:
			eventName = "levelUp";
			params = new HashMap<String, Object>();
			params.put("playerId", "100");
			params.put("attributes", "");
			params.put("credit", "0");
			params.put("level", "2");
			params.put("experience", "0");
			params.put("virtualCurrency", "G=2");
			params.put("presentLevel", "2");
			params.put("levelupDatetime", "12/12/2012");
			params.put("itemsUnlocked", "AEK971,C4_EXPLOSIVES");
			break;
		case 5:
			eventName = "itemChange";
			params = new HashMap<String, Object>();
			params.put("playerId", "5001");
			params.put("attributes", "");
			params.put("itemsInUse", "");
			params.put("itemsInInventory", "DORY,CHICKEN_CUTLET,CHICKEN_CUTLET");
			params.put("credit", "0");
			params.put("level", "1");
			params.put("experience", "0");
			params.put("virtualCurrency", "G=1");
			params.put("logAction", "1");
			params.put("itemsRemaining", "");
			params.put("itemsRemainingQuantity", "");
			params.put("itemsBought", "CHICKEN_CUTLET");
			params.put("itemsBoughtQuantity", "1");
			params.put("itemsBoughtPrice", "Credit=3");
			break;
		case 6:
			eventName = "miscExpenditures";
			params = new HashMap<String, Object>();
			params.put("attributes", "");
			params.put("credit", "0");
			params.put("level", "1");
			params.put("experience", "");
			params.put("currencySpent", "Credit=100,Coins=200");
			params.put("logAction", "1");
			break;
		case 7:
			eventName = "playerTopUp";
			params = new HashMap<String, Object>();
			params.put("gameId", "1001");
			params.put("playerId", "5001");
			params.put("currencyEarned", "300");
			params.put("currencyBalance", "500");
			params.put("remarks", "");
			break;
		case 8:
			eventName = "payment";
			params = new HashMap<String, Object>();
			params.put("gameId", "1001");
			params.put("playerId", "5001");
			params.put("level", "2");
			params.put("creditSpent", "10");
			params.put("resourceBought", "G=1000");
			params.put("exchangeRate", "0.124");
			params.put("realCurrency", "SGD");
			break;
		case 9:
			
			new Thread() {
				
				public void run() {
					try {
						SogamoAPI sogamoAPI = SogamoAPI.getInstance(getApplicationContext());
						String suggestion = sogamoAPI.getSuggestion("buy");
					} catch (SogamoException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				};
			}.start();
			
			break;

		}
		if(params != null) {
			SogamoAPI sogamoAPI = SogamoAPI.getInstance(getApplicationContext());
			try {
				sogamoAPI.trackEventWithName(eventName, params);
			} catch (SogamoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		

	}
}