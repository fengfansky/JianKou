package com.arrownock.demo;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.arrownock.exception.ArrownockException;
import com.arrownock.anpush.AnPush;
import com.arrownock.anpush.AnPushCallbackAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReceiveActivity extends Activity {
	public final static String LOG_TAG = ReceiveActivity.class.getName();
	
	private AnPush an = null;
	private AnPushCallbackAdapter callback = new DemoCallback();
	
	// SharedPreferences for the whole APP 
    public static SharedPreferences anSharedPreferences;
    public static SharedPreferences.Editor anEditor;
	
	// UI stuff
	public static CheckBox channel1CheckBox = null;
	public static CheckBox channel2CheckBox = null;
	public static TextView logView = null;
	public static TextView anidView = null;
	
	private int startSilentHour = -1;
	private int startSilentMinute = -1;
	private int endSilentHour = -1;
	private int endSilentMinute = -1;
	
	private int startResendSilentHour = -1;
	private int startResendSilentMinute = -1;
	private int endResendSilentHour = -1;
	private int endResendSilentMinute = -1;
	
	private int startScheduledMuteHour = -1;
	private int startScheduledMuteMinute = -1;
	private int endScheduledMuteHour = -1;
	private int endScheduledMuteMinute = -1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receive);
		Log.d(LOG_TAG, "-----onCreate-----");
		
		// Initialize AnPush Instance
		try {
			an = AnPush.getInstance(this.getBaseContext());
			an.setCallback(callback);
			an.setSecureConnection(false);
		} catch (ArrownockException e1) {
			e1.printStackTrace();
		}
		anSharedPreferences = getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
		anEditor = anSharedPreferences.edit();
		
		channel1CheckBox = (CheckBox)findViewById(R.id.checkbox_1);
		channel2CheckBox = (CheckBox)findViewById(R.id.checkbox_2);
		logView = (EditText) findViewById(R.id.push_log);
		logView.setInputType(InputType.TYPE_NULL);
		logView.setSingleLine(false);
		logView.setHorizontallyScrolling(false);
		anidView = ((TextView) findViewById(R.id.anid_text));
		
		List<String> channels = new ArrayList<String>();
		
		if (anLoad("channel1CheckBox")) {
			channels.add("Channel1");
		}
		if (anLoad("channel2CheckBox")) {
			channels.add("Channel2");
		}
		
		final Button startButton = ((Button) findViewById(R.id.enable_push));
		final Button stopButton = ((Button) findViewById(R.id.disable_push));

		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (an.getAnID() != null) {
					PushNotificationsManager.startPush(getBaseContext());
					startButton.setEnabled(false);
					stopButton.setEnabled(true);
				} else {
					// pop up warning to let user choose channel first
					Log.w(LOG_TAG, "Device not registered, please select channel first.");
				}
			}
		});
		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PushNotificationsManager.stopPush(getApplicationContext());
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
		
		final Button startSilentButton = ((Button) findViewById(R.id.silent_period_start));
		final Button endSilentButton = ((Button) findViewById(R.id.silent_period_end));
		final Button clearSilentButton = ((Button) findViewById(R.id.silent_period_clear));
		
		startSilentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar c = Calendar.getInstance();
				new TimePickerDialog(ReceiveActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
							int minute) {
							startSilentButton.setText("Start:  " + hourOfDay + " : " + minute);
							startSilentHour = hourOfDay;
							startSilentMinute = minute;
							saveToStorage("startSilentHour", startSilentHour);
							saveToStorage("startSilentMinute", startSilentMinute);
						}
					}
				//Initial Time
				, c.get(Calendar.HOUR_OF_DAY)
				, c.get(Calendar.MINUTE)
				//true means 24 hours
				, true).show();
			}
		});	
	
		endSilentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar c = Calendar.getInstance();
				new TimePickerDialog(ReceiveActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
							int minute) {
							endSilentButton.setText("End:  " + hourOfDay + " : " + minute);
							endSilentHour = hourOfDay;
							endSilentMinute = minute;
							saveToStorage("endSilentHour", endSilentHour);
							saveToStorage("endSilentMinute", endSilentMinute);
							clearSilentButton.setEnabled(true);
							// save the start time
						}
					}
				//Initial Time
				, c.get(Calendar.HOUR_OF_DAY)
				, c.get(Calendar.MINUTE)
				//true means 24 hours
				, true).show();
			}
		});	
		
		clearSilentButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (an.getAnID() != null) {
					if (clearSilentButton.getText().equals("Set")) {
						int startHour = getFromStorage("startSilentHour");
						int startMinute = getFromStorage("startSilentMinute");
						int endHour = getFromStorage("endSilentHour");
						int endMinute = getFromStorage("endSilentMinute");
						int duration = 0;
						if (startHour < endHour) {
							duration = (endHour - startHour) * 60 - startMinute + endMinute;
						} else if (startHour == endHour && startMinute < endMinute) {
							duration = endMinute - startMinute;
						} else {
							duration = (endHour * 60 + endMinute) + 24 * 60 - (startHour * 60 + endMinute);
						}
						try {
							an.setSilentPeriod(startHour, startMinute, duration, false);
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			
						clearSilentButton.setText("Clear");
					} else {
						try {
							an.clearSilentPeriod();
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						startSilentButton.setText("Start");
						endSilentButton.setText("End");
						removeFromStorage("startSilentHour");
						removeFromStorage("startSilentMinute");
						removeFromStorage("endSilentHour");
						removeFromStorage("endSilentMinute");
						clearSilentButton.setText("Set");
						clearSilentButton.setEnabled(false);
					}
				} else {
					// pop up warning to let user choose channel first
					Log.w(LOG_TAG, "Device not registered, please select channel first.");
				}
			}
		});
		
		final Button startResendSilentButton = ((Button) findViewById(R.id.resend_silent_period_start));
		final Button endResendSilentButton = ((Button) findViewById(R.id.resend_silent_period_end));
		final Button clearResendSilentButton = ((Button) findViewById(R.id.resend_silent_period_clear));
		
		startResendSilentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar c = Calendar.getInstance();
				new TimePickerDialog(ReceiveActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
							int minute) {
							startResendSilentButton.setText("Start:  " + hourOfDay + " : " + minute);
							startResendSilentHour = hourOfDay;
							startResendSilentMinute = minute;
							saveToStorage("startResendSilentHour", startResendSilentHour);
							saveToStorage("startResendSilentMinute", startResendSilentMinute);
						}
					}
				//Initial Time
				, c.get(Calendar.HOUR_OF_DAY)
				, c.get(Calendar.MINUTE)
				//true means 24 hours
				, true).show();
			}
		});	
	
		endResendSilentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar c = Calendar.getInstance();
				new TimePickerDialog(ReceiveActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
							int minute) {
							endResendSilentButton.setText("End:  " + hourOfDay + " : " + minute);
							endResendSilentHour = hourOfDay;
							endResendSilentMinute = minute;
							saveToStorage("endResendSilentHour", endResendSilentHour);
							saveToStorage("endResendSilentMinute", endResendSilentMinute);
							clearResendSilentButton.setEnabled(true);
							// save the start time
						}
					}
				//Initial Time
				, c.get(Calendar.HOUR_OF_DAY)
				, c.get(Calendar.MINUTE)
				//true means 24 hours
				, true).show();
			}
		});	
		
		clearResendSilentButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (an.getAnID() != null) {
					if (clearResendSilentButton.getText().equals("Set")) {
						int startHour = getFromStorage("startResendSilentHour");
						int startMinute = getFromStorage("startResendSilentMinute");
						int endHour = getFromStorage("endResendSilentHour");
						int endMinute = getFromStorage("endResendSilentMinute");
						int duration = 0;
						if (startHour < endHour) {
							duration = (endHour - startHour) * 60 - startMinute + endMinute;
						} else if (startHour == endHour && startMinute < endMinute) {
							duration = endMinute - startMinute;
						} else {
							duration = (endHour * 60 + endMinute) + 24 * 60 - (startHour * 60 + endMinute);
						}
						try {
							an.setSilentPeriod(startHour, startMinute, duration, true);
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			
						clearResendSilentButton.setText("Clear");
					} else {
						try {
							an.clearSilentPeriod();
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						startResendSilentButton.setText("Start");
						endResendSilentButton.setText("End");
						removeFromStorage("startResendSilentHour");
						removeFromStorage("startResendSilentMinute");
						removeFromStorage("endResendSilentHour");
						removeFromStorage("endResendSilentMinute");
						clearResendSilentButton.setText("Set");
						clearResendSilentButton.setEnabled(false);
					}
				} else {
					// pop up warning to let user choose channel first
					Log.w(LOG_TAG, "Device not registered, please select channel first.");
				}
			}
		});
		
		final Button setMuteButton = ((Button) findViewById(R.id.mute_set));
		final Button clearMuteButton = ((Button) findViewById(R.id.mute_clear));

		setMuteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (an.getAnID() != null) {
					try {
						an.setMute();
					} catch (ArrownockException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMuteButton.setEnabled(false);
					clearMuteButton.setEnabled(true);
					saveToStorage("setMute", 0);
					saveToStorage("clearMute", 1);
				} else{
					// pop up warning to let user choose channel first
					Log.w(LOG_TAG, "Device not registered, please select channel first.");
				}
			}
		});
		clearMuteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (an.getAnID() != null) {
					try {
						an.clearMute();
					} catch (ArrownockException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMuteButton.setEnabled(true);
					clearMuteButton.setEnabled(false);
					saveToStorage("setMute", 1);
					saveToStorage("clearMute", 0);
				} else {
					// pop up warning to let user choose channel first
					Log.w(LOG_TAG, "Device not registered, please select channel first.");
				}
			}
		});
		
		final Button startScheduledMuteButton = ((Button) findViewById(R.id.scheduled_mute_start));
		final Button endScheduledMuteButton = ((Button) findViewById(R.id.scheduled_mute_end));
		final Button clearScheduledMuteButton = ((Button) findViewById(R.id.scheduled_mute_clear));
		
		startScheduledMuteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar c = Calendar.getInstance();
				new TimePickerDialog(ReceiveActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
							int minute) {
							startScheduledMuteButton.setText("Start:  " + hourOfDay + " : " + minute);
							startScheduledMuteHour = hourOfDay;
							startScheduledMuteMinute = minute;
							saveToStorage("startScheduledMuteHour", startScheduledMuteHour);
							saveToStorage("startScheduledMuteMinute", startScheduledMuteMinute);
						}
					}
				//Initial Time
				, c.get(Calendar.HOUR_OF_DAY)
				, c.get(Calendar.MINUTE)
				//true means 24 hours
				, true).show();
			}
		});	
	
		endScheduledMuteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar c = Calendar.getInstance();
				new TimePickerDialog(ReceiveActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
							int minute) {
							endScheduledMuteButton.setText("End:  " + hourOfDay + " : " + minute);
							endScheduledMuteHour = hourOfDay;
							endScheduledMuteMinute = minute;
							saveToStorage("endScheduledMuteHour", endScheduledMuteHour);
							saveToStorage("endScheduledMuteMinute", endScheduledMuteMinute);
							clearScheduledMuteButton.setEnabled(true);
							// save the start time
						}
					}
				//Initial Time
				, c.get(Calendar.HOUR_OF_DAY)
				, c.get(Calendar.MINUTE)
				//true means 24 hours
				, true).show();
			}
		});	
		
		clearScheduledMuteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (an.getAnID() != null) {
					if (clearScheduledMuteButton.getText().equals("Set")) {
						int startHour = getFromStorage("startScheduledMuteHour");
						int startMinute = getFromStorage("startScheduledMuteMinute");
						int endHour = getFromStorage("endScheduledMuteHour");
						int endMinute = getFromStorage("endScheduledMuteMinute");
						int duration = 0;
						if (startHour < endHour) {
							duration = (endHour - startHour) * 60 - startMinute + endMinute;
						} else if (startHour == endHour && startMinute < endMinute) {
							duration = endMinute - startMinute;
						} else {
							duration = (endHour * 60 + endMinute) + 24 * 60 - (startHour * 60 + endMinute);
						}
						try {
							an.setScheduledMute(startHour, startMinute, duration);
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			
						clearScheduledMuteButton.setText("Clear");
					} else {
						try {
							an.clearMute();
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						startScheduledMuteButton.setText("Start");
						endScheduledMuteButton.setText("End");
						removeFromStorage("startScheduledMuteHour");
						removeFromStorage("startScheduledMuteMinute");
						removeFromStorage("endScheduledMuteHour");
						removeFromStorage("endScheduledMuteMinute");
						clearScheduledMuteButton.setText("Set");
						clearScheduledMuteButton.setEnabled(false);
					}
				} else {
					// pop up warning to let user choose channel first
					Log.w(LOG_TAG, "Device not registered, please select channel first.");
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "-----onResume-----");
		boolean enabled = an.isEnabled();
		((Button) findViewById(R.id.enable_push)).setEnabled(!enabled);
		((Button) findViewById(R.id.disable_push)).setEnabled(enabled);
		channel1CheckBox.setChecked(anLoad("channel1CheckBox"));
		channel2CheckBox.setChecked(anLoad("channel2CheckBox"));
		
		if (getFromStorage("setMute") != -1) {
			if (getFromStorage("setMute") == 1) {
				((Button) findViewById(R.id.mute_set)).setEnabled(true);
				((Button) findViewById(R.id.mute_clear)).setEnabled(false);
			} else {
				((Button) findViewById(R.id.mute_set)).setEnabled(false);
				((Button) findViewById(R.id.mute_clear)).setEnabled(true);
			}
		} else {
			((Button) findViewById(R.id.mute_set)).setEnabled(true);
			((Button) findViewById(R.id.mute_clear)).setEnabled(false);
		}
		
		if (getFromStorage("endSilentHour") == -1) { // time not set
			((Button) findViewById(R.id.silent_period_clear)).setEnabled(false);
		} else {
			int startHour = getFromStorage("startSilentHour");
			int startMinute = getFromStorage("startSilentMinute");
			int endHour = getFromStorage("endSilentHour");
			int endMinute = getFromStorage("endSilentMinute");
			((Button) findViewById(R.id.silent_period_start)).setText("Start:  " + startHour + " : " + startMinute);
			((Button) findViewById(R.id.silent_period_end)).setText("End:  " + endHour + " : " + endMinute);
			((Button) findViewById(R.id.silent_period_clear)).setText("Clear");
			((Button) findViewById(R.id.silent_period_clear)).setEnabled(true);
		}
		
		if (getFromStorage("endResendSilentHour") == -1) { // time not set
			((Button) findViewById(R.id.resend_silent_period_clear)).setEnabled(false);
		} else {
			int startHour = getFromStorage("startResendSilentHour");
			int startMinute = getFromStorage("startResendSilentMinute");
			int endHour = getFromStorage("endResendSilentHour");
			int endMinute = getFromStorage("endSResendilentMinute");
			((Button) findViewById(R.id.resend_silent_period_start)).setText("Start:  " + startHour + " : " + startMinute);
			((Button) findViewById(R.id.resend_silent_period_end)).setText("End:  " + endHour + " : " + endMinute);
			((Button) findViewById(R.id.resend_silent_period_clear)).setText("Clear");
			((Button) findViewById(R.id.resend_silent_period_clear)).setEnabled(true);
		}
		
		if (getFromStorage("endScheduledMuteHour") == -1) { // time not set
			((Button) findViewById(R.id.scheduled_mute_clear)).setEnabled(false);
		} else {
			int startHour = getFromStorage("startScheduledMuteHour");
			int startMinute = getFromStorage("startScheduledMuteMinute");
			int endHour = getFromStorage("endScheduledSilentHour");
			int endMinute = getFromStorage("endScheduledSilentMinute");
			((Button) findViewById(R.id.scheduled_mute_start)).setText("Start:  " + startHour + " : " + startMinute);
			((Button) findViewById(R.id.scheduled_mute_end)).setText("End:  " + endHour + " : " + endMinute);
			((Button) findViewById(R.id.scheduled_mute_clear)).setText("Clear");
			((Button) findViewById(R.id.scheduled_mute_clear)).setEnabled(true);
		}
		writeLogView();

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
		anSave("channel1CheckBox", channel1CheckBox.isChecked());
		anSave("channel2CheckBox", channel2CheckBox.isChecked());
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
	public void onChannelCheckboxClicked(View view) {
		Log.v(LOG_TAG, "channelcheckbox clicked");
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
		List<String> regitserChannels = new ArrayList<String>();
		List<String> unregitserChannels = new ArrayList<String>();	    
	    // Check which checkbox was clicked
	    switch(view.getId()) {
	        case R.id.checkbox_1:
	            if (checked) {
	            	regitserChannels.add("Channel1");
	            } else {
	            	unregitserChannels.add("Channel1");
	            }    
	            break;
	        case R.id.checkbox_2:
	            if (checked) {
	            	regitserChannels.add("Channel2");
	            } else {
	            	unregitserChannels.add("Channel2");
	            }
	            break;
	    }
	    
	    if (regitserChannels.size() != 0) {
	    	// register channels
			try {
				an.register(regitserChannels, false);
			} catch (ArrownockException ex) {
				ex.printStackTrace();
			}
	    }
	    
	    if (unregitserChannels.size() != 0) {
	    	// unregister channels
			try {
				an.unregister(unregitserChannels);
			} catch (ArrownockException e) {
				e.printStackTrace();
			} 
	    }

	}
	
	public void anSave(String buttonName, final boolean isChecked) {
	    anEditor.putBoolean(buttonName, isChecked);
	    anEditor.commit();
	}

	public static boolean anLoad(String buttonName) { 
	    return anSharedPreferences.getBoolean(buttonName, false);
	}
	
	public void saveToStorage(String key, int value){
		anEditor.putInt(key, value);
		anEditor.commit();
	}
	public int getFromStorage(String key) {
		return anSharedPreferences.getInt(key, -1);
	}
	public void removeFromStorage(String key){
		anEditor.remove(key).commit();
	}
	
	protected void writeLogView() {	
		String thirdlog = ReceiveActivity.anSharedPreferences.getString("thirdlog","");
		String secondlog = ReceiveActivity.anSharedPreferences.getString("secondlog","");
		String firstlog = ReceiveActivity.anSharedPreferences.getString("firstlog","");

		String thirdlogdate = ReceiveActivity.anSharedPreferences.getString("thirdlogdate","");
		String secondlogdate = ReceiveActivity.anSharedPreferences.getString("secondlogdate","");
		String firstlogdate = ReceiveActivity.anSharedPreferences.getString("firstlogdate","");
		if (ReceiveActivity.logView != null) {
			ReceiveActivity.logView.setText("");
			ReceiveActivity.logView.append(firstlog + "\n");
			ReceiveActivity.logView.append(firstlogdate + "\n");
			ReceiveActivity.logView.append(secondlog + "\n");
			ReceiveActivity.logView.append(secondlogdate + "\n");
			ReceiveActivity.logView.append(thirdlog + "\n");
			ReceiveActivity.logView.append(thirdlogdate + "\n");
		}	
	}
}

