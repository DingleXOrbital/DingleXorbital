package com.example.orbital;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HomeActivity extends Activity {
	
	Button exitButton, sendButton;
	TextView levelText, subjectText, numberOfContactsText;
	EditText messageBox;

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
		
		this.sendButton = (Button) findViewById(R.id.exitButton);
		this.sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//send message
			}
		});
		
		this.messageBox = (EditText) findViewById(R.id.messageBox);
		this.levelText = (TextView) findViewById(R.id.levelText);
		this.subjectText = (TextView) findViewById(R.id.subjectText);
		this.numberOfContactsText = (TextView) findViewById(R.id.numberOfContactsText);
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
}
