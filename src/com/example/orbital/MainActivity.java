package com.example.orbital;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button serviceButton = (Button) findViewById(R.id.ServiceButton);
		final EditText usernameText = (EditText) findViewById(R.id.editTextUsername);

		serviceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String username = usernameText.getText().toString();
				if (username.isEmpty()) {
					return;
				}

				if (!isMyServiceRunning("com.example.orbital.MqttInitService")) {
					Intent serviceIntent = new Intent(MainActivity.this, MqttInitService.class);
					serviceIntent.putExtra("username", usernameText.getText().toString());
					startService(serviceIntent);
				}
				Intent intent = new Intent(getBaseContext(), HomeActivity.class);
				intent.putExtra("username", username);
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
			// Log.i("Service", service.service.getClassName());
			if (servicename.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
