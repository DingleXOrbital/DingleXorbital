package com.example.orbital;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MqttInitService extends Service {
	
	String username;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try{
			this.username = intent.getStringExtra("username");
			Log.i("Username", this.username);
		} catch(Exception e){
			
		}
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Connect(telephonyManager.getDeviceId(), this.getBaseContext());
		Log.i("Info", "Service Successfully Started");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		close();
	}

	MqttAndroidClient client;
	final String subscribe = "PhoneNumbers";

	public void Connect(final String msg, final Context context) {
		try {
			MqttConnectOptions option = new MqttConnectOptions();
			option.setUserName("iliuurbi");
			option.setPassword("L1jVq-tGOYW3".toCharArray());
			client = new MqttAndroidClient(context, "tcp://m12.cloudmqtt.com:18123", this.username);
			client.connect(option, context, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken mqttToken) {
					client.setCallback(new MqttCallback() {
						@Override
						public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
							String message = arg1.toString();
							Log.i("Message Arrived", message);

							if (message.contains("kwoky")) {
								Intent intent = new Intent("MsgRec");
								intent.putExtra("message", message);
								LocalBroadcastManager.getInstance(MqttInitService.this).sendBroadcast(intent);
							}
						}

						@Override
						public void deliveryComplete(IMqttDeliveryToken arg0) {
						}

						@Override
						public void connectionLost(Throwable arg0) {
						}
					});
					try {
						client.subscribe(subscribe, 1);
						Log.i("Info", "setCallBack and subscribe");
						MqttMessage message = new MqttMessage();
						message.setPayload(msg.getBytes());
						Log.i("Topic", subscribe);
						Log.i("Msg", msg);
						client.publish(subscribe, message);
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(IMqttToken arg0, Throwable arg1) {
					Log.i("Client connection failed", arg1.getMessage());
				}
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
		Log.i("Info", "Return");
	}

	public void close() {
		try {
			client.unsubscribe(subscribe);
			Log.i("Unsubscribe", "Success");
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
