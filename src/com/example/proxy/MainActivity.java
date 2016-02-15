package com.example.proxy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * import com.loopj.android.http.AsyncHttpClient; import
 * com.loopj.android.http.AsyncHttpResponseHandler; import
 * com.loopj.android.http.RequestParams;
 **/
public class MainActivity extends Activity {
	ProgressDialog prgDialog;
	// RequestParams params = new RequestParams();
	GoogleCloudMessaging gcmObj;
	Context applicationContext;
	String regId = "";

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	AsyncTask<Void, Void, String> createRegIdTask;

	public static final String REG_ID = "regId";
	public static final String EMAIL_ID = "eMailId";
	public static final String PASS_WORD = "password";
	EditText emailET, passWord;
	private String instance_id;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		applicationContext = getApplicationContext();
		emailET = (EditText) findViewById(R.id.myemail);
		passWord = (EditText) findViewById(R.id.pass_word);
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Please wait...");
		prgDialog.setCancelable(false);
		TextView title = (TextView) findViewById(R.id.title);
		Typeface type = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-Regular.ttf");
		title.setTypeface(type);
		emailET.setTypeface(type);
		passWord.setTypeface(type);
		Button button = (Button) findViewById(R.id.button1);
		button.setTypeface(type);
		instance_id = InstanceID.getInstance(getApplicationContext()).getId();
		Log.v("instance id", instance_id);

		SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
		String registrationId = prefs.getString(REG_ID, "");
		Log.v("RegistrationID", registrationId);
		TextView text = (TextView) findViewById(R.id.title);
		if (!TextUtils.isEmpty(registrationId)) {
			/**
			 * Intent i = new Intent(applicationContext, HomeActivity.class);
			 * i.putExtra("regId", registrationId); i.putExtra("emailET",
			 * emailET.getText().toString()); i.putExtra("password",
			 * passWord.getText().toString()); i.putExtra("regid", regId);
			 * startActivity(i); text.setText("done"); finish();
			 **/
			String email = prefs.getString(EMAIL_ID,"");
			String password = prefs.getString(PASS_WORD, "");
			Log.v("Retrieved", email);
			Log.v("Retrieved", password);
			emailET.setText(email);
			passWord.setText(password);
		}
	}

	// When Register Me button is clicked
	public void RegisterUser(View view) {
		String emailID = emailET.getText().toString();
		if (!TextUtils.isEmpty(emailID) && Utility.validate(emailID)) {
			registerInBackground(emailID);
		} else {
			Toast.makeText(applicationContext, "Please enter valid email", Toast.LENGTH_LONG).show();
		}
	}

	// AsyncTask to register Device in GCM Server
	private void registerInBackground(final String emailID) {
		new AsyncTask<Void, Void, String>() {
			int responseCode = 200;

			@Override
			protected String doInBackground(Void... param) {

				String msg = "";
				///////////////////////////
				try {
					if (gcmObj == null) {
						gcmObj = GoogleCloudMessaging.getInstance(applicationContext);
					}
					regId = gcmObj.register(ApplicationConstants.Google_Proj_Number);
					msg = "Registration ID :" + regId;
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				Log.v("GCM_Message", msg);
				///////////////////////////
				return msg;
			}

			protected void onPostExecute(String msg) {

				if (!TextUtils.isEmpty(regId)) {
					Toast.makeText(applicationContext, "Registered with GCM Server successfully.nn" + msg,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(applicationContext,
							"Reg ID Creation Failed.nnEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."
									+ msg,
							Toast.LENGTH_LONG).show();
				}
				///////////////////////////////////////////////
				if (responseCode == 200) {
					Toast.makeText(applicationContext, "Login Success", Toast.LENGTH_SHORT).show();
					storeRegIdinSharedPref(applicationContext, regId, emailID);
				} else {
					Toast.makeText(applicationContext, "Login failed", Toast.LENGTH_SHORT).show();
				}
			}
		}.execute(null, null, null);
	}

	// Store RegId and Email entered by User in SharedPref
	private void storeRegIdinSharedPref(Context context, String regId, String emailID) {
		SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(REG_ID, regId);
		editor.putString(EMAIL_ID, emailID);
		editor.putString(PASS_WORD, passWord.getText().toString());
		editor.commit();
		tempStoreRegIdinServer();
	}

	// Share RegID with GCM Server Application (Php)

	private void tempStoreRegIdinServer() {
		prgDialog.show();
		if (prgDialog != null) {
			prgDialog.dismiss();
		}

		Toast.makeText(applicationContext, "Reg Id shared successfully with Web App ", Toast.LENGTH_LONG).show();
		Intent i = new Intent(applicationContext, HomeActivity.class);
		i.putExtra("regId", regId);
		i.putExtra("emailET", emailET.getText().toString());
		i.putExtra("password", passWord.getText().toString());
		i.putExtra("regid", regId);
		startActivity(i);
	}

	/**
	 * private void storeRegIdinServer() { prgDialog.show(); params.put("regId",
	 * regId); // Make RESTful webservice call using AsyncHttpClient object
	 * AsyncHttpClient client = new AsyncHttpClient();
	 * client.post(ApplicationConstants.APP_SERVER_URL, params, new
	 * AsyncHttpResponseHandler() { // When the response returned by REST has
	 * Http // response code '200'
	 * 
	 * @Override public void onSuccess(String response) { // Hide Progress
	 *           Dialog prgDialog.hide(); if (prgDialog != null) {
	 *           prgDialog.dismiss(); } Toast.makeText(applicationContext,
	 *           "Reg Id shared successfully with Web App ",
	 *           Toast.LENGTH_LONG).show(); Intent i = new
	 *           Intent(applicationContext, HomeActivity.class);
	 *           i.putExtra("regId", regId); startActivity(i); finish(); }
	 * 
	 *           // When the response returned by REST has Http // response code
	 *           other than '200' such as '404', // '500' or '403' etc
	 * @Override public void onFailure(int statusCode, Throwable error,String
	 *           content) { // Hide Progress Dialog prgDialog.hide(); if
	 *           (prgDialog != null) { prgDialog.dismiss(); } // When Http
	 *           response code is '404' if (statusCode == 404) {
	 *           Toast.makeText(applicationContext,
	 *           "Requested resource not found", Toast.LENGTH_LONG).show(); } //
	 *           When Http response code is '500' else if (statusCode == 500) {
	 *           Toast.makeText(applicationContext,
	 *           "Something went wrong at server end",
	 *           Toast.LENGTH_LONG).show(); } // When Http response code other
	 *           than 404, 500 else { Toast.makeText( applicationContext,
	 *           "Unexpected Error occcured! [Most common Error: Device might "
	 *           +
	 *           "not be connected to Internet or remote server is not up and running], check for other errors as well"
	 *           , Toast.LENGTH_LONG).show(); } } }); }
	 **/
	protected void onResume() {
		super.onResume();
	}
}