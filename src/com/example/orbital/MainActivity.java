package com.example.orbital;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button serviceButton = (Button) findViewById(R.id.ServiceButton);
		
		serviceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent serviceIntent = new Intent(MainActivity.this, MqttInitService.class);
	        	startService(serviceIntent);
	        	Intent intent = new Intent(getBaseContext(), HomeActivity.class);
	        	startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	private boolean isMyServiceRunning(String servicename) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.i("Service", service.service.getClassName());
	        if (servicename.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	String[] numList;
	EditText message;
	EditText numbers;
	
	private void initSms() {
		Toast.makeText(getApplicationContext(), "Sending!", Toast.LENGTH_LONG).show();
		String[] x = numbers.getText().toString().split(", ");
		for (int i = 0; i < x.length; i += 50) {
			int end;
			if (i + 50 < x.length) {
				end = i + 50;
			} else {
				end = x.length;
			}
			numList = Arrays.copyOfRange(x, i, end);
			try {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						String msg = message.getText().toString();
						Log.v("GCM_Message", numbers.getText().toString());
						if (numList.length != 0) {
							int l = numList.length;
							for (int i = 0; i < l; i++) {
								sendSMS(numList[i], msg);
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
}
