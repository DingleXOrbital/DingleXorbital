package com.example.orbital;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MqttInitService extends Service {
	
	MqttConnect m;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		m = new MqttConnect();
		m.Connect("Init", telephonyManager.getDeviceId(),this.getBaseContext());
		Log.i("Info", "Service Successfully Started");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		m.close();
	}
}
