package com.example.pujiahhsign;

public class Cookie {
	public String key = "";
	public String value = "";
	public String domain = "";
	
	public Cookie(){}
	
	public Cookie(String key, String value, String domain){
		this.key = key;
		this.value = value;
		this.domain = domain;
	}
	public String toString(){
		return key + "=" + value + ";";
	}
}
