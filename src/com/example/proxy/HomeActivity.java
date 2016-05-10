package com.example.proxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	TextView msgET, usertitleET;

	Button send;
	EditText message;
	EditText numbers;
	String instance_id;
	MyWebRequestReceiver receiver;
	String credentials;
	String regId;
	int responseCode = 0;
	String device_id;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Intent intent = getIntent();
		String messages = intent.getStringExtra("message");
		send = (Button) findViewById(R.id.sendbutton);
		send.setOnClickListener(listen);
		message = (EditText) findViewById(R.id.messagecontent);
		numbers = (EditText) findViewById(R.id.message);

		try {
			device_id = InstanceID.getInstance(this).getToken(ApplicationConstants.Google_Proj_Number,
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Typeface type = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-Regular.ttf");
		message.setTypeface(type);
		numbers.setTypeface(type);
		send.setTypeface(type);
		// Get Email ID from Shared preferences
		SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
		String eMailId = prefs.getString("eMailId", "");
		usertitleET = (TextView) findViewById(R.id.usertitle);
		usertitleET.setText("Welcome\n" + eMailId.split("@")[0]);
		usertitleET.setTypeface(type);
		if (messages == null) {
			String email = intent.getStringExtra("emailET");
			String password = intent.getStringExtra("password");
			regId = intent.getStringExtra("regid");

			Intent i = new Intent(this, RegistrationService.class);
			i.putExtra("email", email);
			i.putExtra("password", password);
			startService(i);

			IntentFilter filter = new IntentFilter(MyWebRequestReceiver.PROCESS_RESPONSE);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new MyWebRequestReceiver();
			registerReceiver(receiver, filter);

			credentials = email + ":" + password;

		} else {
			Log.v("message", messages);
			// When Message sent from Broadcast Receiver is not empty
			if (messages != null) {
				// Set the message
				numbers.setText(messages);
			}
		}
	}

	private void initiate(final String credentials) {
		new AsyncTask<Void, Void, String>() {
			protected String doInBackground(Void... param) {
				try {
					Log.v("Credentials", credentials);
					URL url = new URL(ApplicationConstants.APP_SERVER_URL);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setChunkedStreamingMode(0);
					conn.setReadTimeout(10000);
					conn.setConnectTimeout(15000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setRequestProperty("Authorization",
							"Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP));

					List<NameValuePair> params = new ArrayList<NameValuePair>();
					if (instance_id == null) {
						Log.v("lastcall", "error");
					} else {
						Log.v("lastcall", instance_id);
					}
					Log.v("regid", regId);
					JSONObject a = new JSONObject();
					a.put("registration", regId);
					params.add(new BasicNameValuePair("description", a.toString()));
					params.add(new BasicNameValuePair("device", device_id));
					params.add(new BasicNameValuePair("type", "android"));

					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
					writer.write(getQuery(params));
					writer.flush();
					writer.close();
					os.close();
					responseCode = conn.getResponseCode();
					Log.v("RESPONSE CODE", Integer.toString(responseCode));
					Log.v("RESPONSE", conn.getResponseMessage());
					conn.disconnect();
					if (responseCode != 200) {
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Integer.toString(responseCode);
			}
		}.execute();
	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");
			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}
		return result.toString();
	}

	String[] numList;

	private View.OnClickListener listen = new View.OnClickListener() {
		public void onClick(View v) {
			initSms();
		}
	};

	private void initSms() {
		Toast.makeText(getApplicationContext(), "Sending!", Toast.LENGTH_LONG).show();
		try {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					numList = numbers.getText().toString().split(", ");
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
			Toast.makeText(getApplicationContext(), "Finished sending everything!", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Sending Error", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
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
				switch (getResultCode()) {
				case Activity.RESULT_OK:
//					Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//					Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
//					Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
//					Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
//					Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
//					Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
//					Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public class MyWebRequestReceiver extends BroadcastReceiver {

		public static final String PROCESS_RESPONSE = "com.example.proxy.intent.action.PROCESS_RESPONSE";

		public void onReceive(Context context, Intent intent) {
			Log.v("broadcast", "received");
			instance_id = intent.getStringExtra("instance_id");
			// usertitleET.setText(instance_id);
			Log.v("instance_id", instance_id);
			initiate(credentials);
		}
	}
}