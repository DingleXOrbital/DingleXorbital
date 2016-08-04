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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class MqttConnect {

	MqttAndroidClient client;
	final String subscribe = "orbital";

	public void Connect(final String topic, final String msg, final Context context) {
		try {
			MqttConnectOptions option = new MqttConnectOptions();
			option.setUserName("iliuurbi");
			option.setPassword("L1jVq-tGOYW3".toCharArray());
			client = new MqttAndroidClient(context, "tcp://m12.cloudmqtt.com:18123", "jk");
			client.connect(option, context, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken mqttToken) {
					client.setCallback(new MqttCallback() {
						@Override
						public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
							Log.i("Message Arrived", arg1.toString());
							//Toast.makeText(context, arg1.toString(), Toast.LENGTH_SHORT);
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
						Log.i("Topic", topic);
						Log.i("Msg", msg);
						client.publish(topic, message);
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
	
	public void close(){
		try {
			client.unsubscribe(subscribe);
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
