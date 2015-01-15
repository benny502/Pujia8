package com.example.pujiahhsign;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class InfoActivity extends Activity {

	private String infoUrl = "http://pujiahh.com/";
	
	private String signUrl = "http://pujiahh.com/checkin/";
	
	private TextView userinfo;
	
	private ImageView avatar;
	
	private HttpConnHelper conn;
	
	protected static final int GETINFO_COMPLETE = 0;

	protected static final int NETWORKPROBLEM = 1;

	protected static final int GETAVATAR_COMPLETE = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		Intent intent = getIntent();
		String sessionid = intent.getStringExtra("sessionid");
		if("".equals(sessionid)||null==sessionid){
			//没有登录
			Intent loginIntent = new Intent(InfoActivity.this,MainActivity.class);
			startActivity(loginIntent);
			this.finish();
		}
		else{
		    userinfo = (TextView)findViewById(R.id.userinfo);
		    avatar = (ImageView)findViewById(R.id.avatar);
		    conn = new HttpConnHelper();
			GetProfile(sessionid);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_info, menu);
		return true;
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case NETWORKPROBLEM:
				Toast.makeText(InfoActivity.this, "网络连接失败，请检查您的网络状况", Toast.LENGTH_SHORT).show();
				break;
			case GETINFO_COMPLETE:
				userinfo.setText(String.valueOf(msg.obj));
				break;
			case GETAVATAR_COMPLETE:
				byte[] image = (byte[])msg.obj;
				Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
				avatar.setImageBitmap(bitmap);
			}
		}
	};
	
	public void GetProfile(final String sessionid){
		new Thread(){
			@Override
			public void run(){
				CookieInfo.addCookie("sessionid", sessionid, "pujiahh.com");
				try {
					if(200 == conn.ClientGet(infoUrl, new HttpConnectionProperty())){
						String html = conn.ClientGetContent();
						String url = getAvatar(html);
						String nick = getNick(html);
						String regDate = getReg(html);
						mHandler.obtainMessage(GETINFO_COMPLETE, nick).sendToTarget();
						getImage(url);
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					mHandler.obtainMessage(NETWORKPROBLEM).sendToTarget();
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			private void getImage(String url) throws ClientProtocolException, IOException {
				// TODO Auto-generated method stub
				if(200 == conn.ClientGet(url, new HttpConnectionProperty())){
					byte[] image = conn.ClientGetImage();
					if(image != null){
						mHandler.obtainMessage(GETAVATAR_COMPLETE,image).sendToTarget();
					}
				}
			}

			private String getReg(String html) {
				// TODO Auto-generated method stub
				Matcher mat = Pattern.compile("注册时间:[\\W]+[\\d]{4}(-[\\d]{2}){2}").matcher(html);
				if(mat.find()){
					return mat.group();
				}
				return null;
			}
			
			private String getAvatar(String html){
				Matcher mat = Pattern.compile("<a href[^h]+(http://www.pujiahh.com/static/avatars/[\\w]+.jpg)").matcher(html);
				if(mat.find()){
					return mat.group(1);
				}
				return "";
			}
			
			private String getNick(String html) {
				// TODO Auto-generated method stub
				Matcher mat = Pattern.compile("<p>([^<^(]+)(<a[^(]+)?(\\(uid:[\\W]?[0-9]+\\))").matcher(html);
				if(mat.find()){
					return String.format("%s %s", mat.group(1).trim(), mat.group(3));
				}
				return "";
			}
		}.start();
	}
	


}
