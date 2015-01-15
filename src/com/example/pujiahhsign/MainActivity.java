package com.example.pujiahhsign;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Pujiahh pujiahh;
	EditText username;
	EditText password;
	Button login;
	LinearLayout loginFrame;
	LinearLayout signFrame;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		login = (Button)findViewById(R.id.login);
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);
		username.setOnEditorActionListener(editListener);
		password.setOnEditorActionListener(editListener);
		login.setOnClickListener(listener);
		
		pujiahh = new Pujiahh(){
			@Override
			public void PreLogin() {
				// TODO Auto-generated method stub
				login.setEnabled(false);
			}

			@Override
			public void LoginFailed(int code) {
				// TODO Auto-generated method stub
				login.setEnabled(true);
				switch(code){
					case NETWORKPROBLEM:
						Toast.makeText(MainActivity.this, "登录失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
						break;
					case LOGINFAILED:
						Toast.makeText(MainActivity.this, "登录失败,请重新检查用户名和密码", Toast.LENGTH_SHORT).show();
						break;
				}
			}

			@Override
			public void LoginSuccess(String sessionid) {
				// TODO Auto-generated method stub
				//Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MainActivity.this,InfoActivity.class);
				intent.putExtra("sessionid", sessionid);
				startActivity(intent);
				MainActivity.this.finish();
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	
	public void collapseSoftInputMethod(View v){
		 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		 imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	private OnEditorActionListener editListener = new OnEditorActionListener(){
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// TODO Auto-generated method stub
			EditText edit = (EditText)v;
			switch(edit.getId()){
			case R.id.username:
				password.requestFocus();
				break;
			}
			return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
		}

	};
	
	private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			collapseSoftInputMethod(v);
			login();
		}

	};
	
	public void login(){
		String user;
		String pass;
		if((user = username.getText().toString().trim()).equals("")){
			Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
			return;
		}
		if((pass = password.getText().toString().trim()).equals("")){
			Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
			return;
		}
		pujiahh.login(user,pass);
		//pujiahh.login(MainActivity.this,"小虫跳跳","980214");
	}

}
