package com.example.pujiahhsign;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public abstract class Pujiahh {
	
	public static final int NETWORKPROBLEM = 0;
	private static final int LOGINSUCCESS = 1;
	public static final int LOGINFAILED = 2;
	private final String HOST = "pujiahh.com";
	private final String tokenUrl = "http://pujiahh.com/account/login/?next=/";
	private final String loginUrl = "http://pujiahh.com/account/login/?next=/";

	private HttpConnHelper conn = null;
	
	public Pujiahh(){
		conn = new HttpConnHelper();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage (Message msg) {
			if(msg.what == LOGINSUCCESS){
				LoginSuccess(String.valueOf(msg.obj));
			}
			else{
				LoginFailed(msg.what);
			}
		}
	};

	public abstract void PreLogin();
	
	public abstract void LoginFailed(int code);
	
	public abstract void LoginSuccess(String sessionid);
	
	
	private synchronized String getToken() throws ClientProtocolException, IOException{
		if(200 == conn.ClientGet(tokenUrl, new HttpConnectionProperty())){
			return CookieInfo.getValue("csrftoken", HOST);
		}
		return "";
	}
	
	public void login(final String username, final String password) {
		// TODO Auto-generated method stub
		PreLogin();
		new Thread(){
			@Override
			public void run(){
				try {
					//ªÒ»°token
					String csrftoken = getToken();
					if("".equals(csrftoken)){
						mHandler.obtainMessage(NETWORKPROBLEM).sendToTarget();
						return;
					}
					HashMap<String,String> postdata = new HashMap<String, String>();
					postdata.put("csrfmiddlewaretoken", csrftoken);
					postdata.put("username", username);
					postdata.put("password", password);
					postdata.put("next", "/");
					switch(conn.ClientMultiPost(loginUrl, postdata, new HttpConnectionProperty())){
					case 302:
						mHandler.obtainMessage(LOGINSUCCESS, CookieInfo.getValue("sessionid", HOST)).sendToTarget();
						break;
					case 200:
						mHandler.obtainMessage(LOGINFAILED).sendToTarget();
						break;
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					mHandler.obtainMessage(NETWORKPROBLEM).sendToTarget();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();
	}
	
}
