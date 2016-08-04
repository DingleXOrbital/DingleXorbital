package com.example.orbital;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {

	Button exitButton, sendButton;
	TextView subjectText, numberOfContactsText;
	EditText messageBox;
	String[] phoneNumbers;
	String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		this.exitButton = (Button) findViewById(R.id.exitButton);
		this.exitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				stopService(new Intent(HomeActivity.this, MqttInitService.class));
				finish();
			}
		});

		this.sendButton = (Button) findViewById(R.id.sendButton);
		this.sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				initSms();
			}
		});

		this.messageBox = (EditText) findViewById(R.id.messageBox);
		this.subjectText = (TextView) findViewById(R.id.subjectText);
		this.numberOfContactsText = (TextView) findViewById(R.id.numberOfContactsText);

		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("MsgRec"));

		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras == null) {
				assert false;
			} else {
				username = extras.getString("username");
			}
		} else {
			username = (String) savedInstanceState.getSerializable("username");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = intent.getStringExtra("message");
			Log.i("receiver", "Got message: " + message);
			String[] temp = message.split(", ");
			
			if(!temp[1].equalsIgnoreCase(username)){
				return;
			}

			phoneNumbers = Arrays.copyOfRange(temp, 4, temp.length);
			/**
			 * for (String s : phoneNumbers) { Log.i("Numbers", s); }
			 **/
			Log.i("Number Count", Integer.toString(phoneNumbers.length));
			numberOfContactsText.setText(Integer.toString(phoneNumbers.length));
			subjectText.setText(temp[2]);
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("MsgRec"));
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		super.onPause();
	}

	String[] numList;

	private void initSms() {
		Toast.makeText(getApplicationContext(), "Sending!", Toast.LENGTH_LONG).show();
		String[] x = phoneNumbers;
		for (int i = 0; i < x.length; i += 3) {
			int end;
			if (i + 3 < x.length) {
				end = i + 3;
			} else {
				end = x.length;
			}
			numList = Arrays.copyOfRange(x, i, end);
			try {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						String msg = messageBox.getText().toString();
						if (numList.length != 0) {
							int l = numList.length;
							for (int i = 0; i < l; i++) {
								if (isNumeric(numList[i])) {
									sendSMS(numList[i], msg);
								}
							}
						}
						return null;
					}
				}.execute();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Sending Error", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			Toast.makeText(getApplicationContext(), "Finished sending " + end, Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Toast.makeText(getApplicationContext(), "Finished sending everything!", Toast.LENGTH_LONG).show();
	}

	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					break;
				case Activity.RESULT_CANCELED:
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		for (int i = 0; i < parts.size(); i++) {
			sentPendingIntents.add(sentPI);
			deliveredPendingIntents.add(deliveredPI);
		}
		sms.sendMultipartTextMessage(phoneNumber, null, parts, sentPendingIntents, deliveredPendingIntents);
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
