package com.example.proxy;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class RegistrationService extends IntentService {
	public RegistrationService(String name) {
		super(name);
		Log.v("startservice", "constructor");
	}
	public RegistrationService(){
		this("");
	}
	
	private String instance_id;

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v("start service", "onhandleintent");
		InstanceID instanceID = InstanceID.getInstance(this);
		String iid = null;
		try {
			iid = InstanceID.getInstance(getBaseContext()).getId();
			instance_id = instanceID.getToken(ApplicationConstants.Google_Proj_Number,
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			Log.v("iid", iid);
			String email = intent.getStringExtra("email");
			String password = intent.getStringExtra("password");
			final String credentials = email + ":" + password;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Intent broadcastIntent = new Intent("com.example.proxy.intent.action.PROCESS_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        Log.v("regisservice_instanceid", instance_id);
        broadcastIntent.putExtra("instance_id", instance_id);
        sendBroadcast(broadcastIntent);
        Log.v("broadcast", "Sent");
	}
}
