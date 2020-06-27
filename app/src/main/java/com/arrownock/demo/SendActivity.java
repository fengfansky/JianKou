package com.arrownock.demo;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.arrownock.exception.ArrownockException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SendActivity extends Activity {
	public final static String LOG_TAG = ReceiveActivity.class.getName();
	private final static String API_BASE_URL = "http://api.arrownock.com";
	private final static String SENDPUSH_ENDPOINT = "/v1/push_notification/send.json";
	private final static String SENDPUSH_API_URL = API_BASE_URL + SENDPUSH_ENDPOINT;
	private final static String APP_KEY = "com.arrownock.push.APP_KEY";
	
	// UI stuff
	public static Spinner channelSpinner = null;
	public static CheckBox locationCheckBox = null;
	public static EditText messageEditText = null;
	public static Button sendButton = null;
	public static int spinnerPosition = 0;
	
	public static String selectedChannel = "Channel1";
	public static String appKey = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send);
		
	    if (android.os.Build.VERSION.SDK_INT > 9) {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	    }
	    
		try{
		    ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
		    Bundle bundle = ai.metaData;
		    if (bundle != null) {
		    	appKey = bundle.getString(APP_KEY);
		    }
		} catch (NameNotFoundException e) {
		    Log.w(LOG_TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
		}
			
		channelSpinner = (Spinner) findViewById(R.id.select_channel_spinner);
		locationCheckBox = (CheckBox) findViewById(R.id.location_base);
		messageEditText = (EditText) findViewById(R.id.edit_message);
		sendButton = (Button)findViewById(R.id.send_button);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.channels_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		channelSpinner.setAdapter(adapter);
		
		selectedChannel = channelSpinner.getSelectedItem().toString();
		spinnerPosition = adapter.getPosition(selectedChannel);
		
		channelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int positon, long id) {
            	selectedChannel = parent.getSelectedItem().toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
	    sendButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	                String payload = messageEditText.getText().toString();
	                JSONObject message = new JSONObject();
	                String payloadSent = null;
	                try {
		                message.put("alert", "AN Demo");
		                message.put("title", payload);
		                message.put("badge", 0);
		                payloadSent = new JSONObject().put("android", message).toString();
		                Log.d(LOG_TAG, "Channle: " + selectedChannel);
		                Log.d(LOG_TAG, "PayloadSent: " + payloadSent);
	                } catch (JSONException ex) {
	                }
	                try
	                {
	                	sendPush(appKey, selectedChannel, payloadSent);
	                } catch (ArrownockException ex) {
	        			ex.printStackTrace();
	        		}
	                messageEditText.setText("");
	            }
	    });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "-----onResume-----");
		String input = ReceiveActivity.anSharedPreferences.getString("userinput", "");
		Log.d("LOG_TAG", "input: " + input);
		messageEditText.setText(input);
		channelSpinner.setSelection(ReceiveActivity.anSharedPreferences.getInt("spinnerposition", 0));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(LOG_TAG, "-----onStart-----");
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(LOG_TAG, "-----onRestart-----");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "-----onPause-----");
		// save the user input text and selected channel
		String userinput = messageEditText.getText().toString();
		if (userinput.length() != 0) {
			ReceiveActivity.anEditor.putString("userinput", userinput);
		}
		ReceiveActivity.anEditor.putInt("spinnerposition", spinnerPosition);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "-----onStop-----");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "-----onDestroy-----");
	}
	
	public void onLocationCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
            if (checked) {
            	Log.d(LOG_TAG, "Location based push, coming soon!");
            } else {
            	// normal mode
            }    
	}
	
	private static void sendPush(String appKey, String channel, String payload) throws ArrownockException {
		HttpClient client = new DefaultHttpClient();
	    HttpPost postPush = new HttpPost(SENDPUSH_API_URL+ "?key=" + appKey.trim());
	    try {
	    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    nameValuePairs.add(new BasicNameValuePair("channel", channel));
		    nameValuePairs.add(new BasicNameValuePair("payload", payload));
		    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
	    	String date = format.format(new Date());
	    	nameValuePairs.add(new BasicNameValuePair("send_at", date));
	      
		    postPush.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    HttpResponse response = client.execute(postPush);
		    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		    String line = "";
		    StringBuffer buffer = new StringBuffer();
		    while ((line = rd.readLine()) != null) {
		    	buffer.append(line);
		    	buffer.append('\n');
		    }
		    String s = buffer.toString();
		    try {
				JSONObject json = new JSONObject(s);
				JSONObject meta = json.getJSONObject("meta");
				if(meta == null || meta.getInt("code") != 200) {
					throw new ArrownockException("Send Push failed. " + meta.getString("message"), ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
				} 
			} catch (JSONException e) {
				throw new ArrownockException("Send Push Failed. " + e.getMessage(), ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
			}
	    } catch (IOException e) {
	    	throw new ArrownockException("Send Push Failed. " + e.getMessage(), ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
	    }
	    
	}
}
