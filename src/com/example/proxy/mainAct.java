package com.example.proxy;

import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class mainAct extends ActionBarActivity {

	Button send,receive;
	TextView text;
	EditText texter;
	String errorLog;
	static String s = " ";
	String[] numList;
	EditText usernameText, passwordText;
	String instance_id;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_act);
		send = (Button) findViewById(R.id.button_1);
		receive = (Button) findViewById(R.id.button2);
		send.setOnClickListener(listen);
		receive.setOnClickListener(listen);
		text = (TextView) findViewById(R.id.textView1);
		text.setText("");
		texter = (EditText) findViewById(R.id.texter);
		numList = new String[0];
		usernameText = (EditText) findViewById(R.id.userText);
		passwordText = (EditText) findViewById(R.id.pwText);
		
		
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
	
	private View.OnClickListener listen = new View.OnClickListener(){
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_1:
                	sender();
                    break;
                case R.id.button2:
					try {
						new asyncMail().execute("").get();
						//texter.setText(s);
						if(s.isEmpty()){
							texter.setText("Empty");
						}else{
							numList = s.split(" ");
							texter.setText(s);
						}
					} catch(Exception e){
						e.printStackTrace();
						texter.setText(e.toString());
					}
                	
                	text.setText("Received");
                	break;
                default:
                	text.setText("Something is very wrong");
                    break;
            }
        }
    };
    
    private class asyncMail extends AsyncTask<String,String,String>{
    	protected String doInBackground(String... params){
    		try{
        		EmailAccount account = new EmailAccount();
    			EmailAuthenticator authenticator = new EmailAuthenticator(account);
    			String mailServer = "imap.gmail.com";
    			Session imapSession = Session.getDefaultInstance(new Properties(), authenticator);
				Store store = imapSession.getStore("imaps");
				store.connect(mailServer, account.username, account.password);
				Folder inbox = store.getFolder("Inbox");
				inbox.open(Folder.READ_WRITE);
				Message[] result = inbox.getMessages();
				s="Empty";
				for(int i=1;i<=result.length;i++){
					Message m = result[result.length-i];
					if(m.getFlags().contains(Flags.Flag.SEEN)){
						s = "No Unread";
						break;
					}else if(m.getSubject().equals("Composer")){
						Multipart mp = (Multipart) m.getContent();
			            BodyPart bp = mp.getBodyPart(0);
			            s = bp.getContent().toString();
			            m.setFlag(Flags.Flag.DELETED, true);
			            break;
					}else{
						m.setFlag(Flags.Flag.DELETED, true);
					}
				}
				inbox.close(true);
				store.close();
			}catch(Exception e){
				//texter.setText(e.toString());
				e.printStackTrace();
				errorLog = e.toString();
				return e.toString();
			}
    		errorLog = "Done";
    		return "Done";
    	}
    }
	
	public void sender(){
		SmsManager sms = SmsManager.getDefault();
		String msg = texter.getText().toString();
		if(numList!=null){
			int l = numList.length;
			for(int i=0;i<l;i++){
				sms.sendTextMessage(numList[i], null, msg, null, null);
				try {
					Thread.sleep(750);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			text.setText("Sent");
		}else{
			text.setText("No Sender");
		}
	}
	
	public class EmailAuthenticator extends Authenticator{
		private EmailAccount account;
		public EmailAuthenticator(EmailAccount account) {
			super();
			this.account = account;
		}
		protected PasswordAuthentication getPasswordAuthentication(){  
		    return new PasswordAuthentication(account.emailAddress, account.password);  
		}
	}
	
	public class EmailAccount {
		public String urlServer = usernameText.getText().toString().split("@")[1];
		public String username = usernameText.getText().toString().split("@")[0];
		public String password = passwordText.getText().toString();
		public String emailAddress;
		public EmailAccount() {
			this.emailAddress = username + "@" + urlServer;
		}
	}
	
}
