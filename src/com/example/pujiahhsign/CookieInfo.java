package com.example.pujiahhsign;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookieInfo {
	
	private static List<Cookie> list = new ArrayList<Cookie>();
	
	public static synchronized void setCookie(List<String> clist,String host){
		if(clist != null){
			for(String item : clist){
				int index = item.indexOf(";");
				String cook = item.substring(0, index);
				int idx = cook.indexOf("=");
				String key = cook.substring(0, idx);
				String val = cook.substring(idx + 1, cook.length());
				String regEx = "Domain=([^;]+)";
				String domain = host;
				Pattern pattern = Pattern.compile(regEx);
				Matcher matcher = pattern.matcher(item);
				if(matcher.find()&&matcher.groupCount()>1){
					domain = matcher.group(1);
				}
				addCookie(key,val,domain);
			}
		}
	}
	
	public static synchronized void addCookie(String key,String val,String domain){
		for(Cookie cookie : list){
			if(cookie.key.equalsIgnoreCase(key) && cookie.domain.equalsIgnoreCase(domain)){
				cookie.value = val;
				return;
			}
		}
		list.add(new Cookie(key,val,domain));
	}
	
	@SuppressLint("DefaultLocale")
	public static synchronized String getCookie(String domain){
		StringBuffer sb = new StringBuffer();
		if(list.size() > 0){
			for(Cookie cookie : list){
				if(cookie.domain.toLowerCase().endsWith(domain.toLowerCase())){
					sb.append(cookie.toString());
				}
			}
			return sb.substring(0,sb.length()-1);
		}
		return "";
	}
	
	@SuppressLint("DefaultLocale")
	public static synchronized String getValue(String key, String domain){
		for(Cookie cookie : list){
			if(cookie.domain.toLowerCase().endsWith(domain.toLowerCase())&&cookie.key.equalsIgnoreCase(key)){
				return cookie.value;
			}
		}
		return "";
	}
}
