package com.example.pujiahhsign;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


@SuppressLint("DefaultLocale")
public class HttpConnHelper {
	private HttpURLConnection conn;
	
	private HttpResponse response;
	
	private String charset = HTTP.UTF_8;
	
	public String getCharSet(){
		return charset;
	}
	
	public void setCharset(String charset){
		this.charset = charset;
	}
	
	public int ClientConnect(String urlStr,String method,HttpEntity entity,HttpConnectionProperty property) throws ClientProtocolException, IOException{
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, property.Allow_Auto_Reirect);  
		httpClient.getParams().setParameter("http.protocol.content-charset", charset);
		httpClient.getParams().setParameter("http.useragent", property.User_Agent);
		if(method.equalsIgnoreCase("GET")){
			HttpGet httpGet = new HttpGet(urlStr);
			httpGet.setHeader("User-Agent", property.User_Agent);
			httpGet.setHeader("Accept", property.Accept);
			httpGet.setHeader("Accept-Encoding", property.Accept_Encoding);
			httpGet.setHeader("Accept-Language", property.Accept_Language);
			httpGet.setHeader("Connection", property.Connection);
			httpGet.setHeader("HOST", getHost(urlStr));
			httpGet.setHeader("Referer", property.Referer);
			httpGet.setHeader("Origin", property.Origin);
			httpGet.setHeader("Cookie", CookieInfo.getCookie(getDomain(urlStr)));
			response = httpClient.execute(httpGet);
		}
		if(method.equalsIgnoreCase("POST")){
			HttpPost httpPost = new HttpPost(urlStr);
			httpPost.setHeader("User-Agent", property.User_Agent);
			httpPost.setHeader("Accept", property.Accept);
			httpPost.setHeader("Accept-Encoding", property.Accept_Encoding);
			httpPost.setHeader("Accept-Language", property.Accept_Language);
			httpPost.setHeader("Connection", property.Connection);
			httpPost.setHeader("HOST", getHost(urlStr));
			httpPost.setHeader("Referer", property.Referer);
			httpPost.setHeader("Origin", property.Origin);
			httpPost.setHeader("Cookie", CookieInfo.getCookie(getDomain(urlStr)));
			httpPost.setEntity(entity);
			response = httpClient.execute(httpPost);
		}
		org.apache.http.Header[] header = response.getHeaders("Set-Cookie");
		List<String> cookies = new ArrayList<String>();
		for(org.apache.http.Header cook : header){
			cookies.add(cook.getValue());
		}
		CookieInfo.setCookie(cookies, getHost(urlStr));
		return response.getStatusLine().getStatusCode();
	}
	
	public int ClientMultiPost(String urlStr,HashMap<String, String> postdata,HttpConnectionProperty property) throws ClientProtocolException, IOException{
		MultipartEntity mutiEntity = new MultipartEntity();
		for(Map.Entry<String, String> entry : postdata.entrySet()){
			mutiEntity.addPart(entry.getKey(),new StringBody(entry.getValue(),Charset.forName(charset)));
		}
		return ClientConnect(urlStr,"POST",mutiEntity,property);
	}
	
	public int ClientPost(String urlStr, HashMap<String, String> postdata, HttpConnectionProperty property) throws ClientProtocolException, IOException{
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		for(Map.Entry<String, String> entry : postdata.entrySet()){
			pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, charset);
		return ClientConnect(urlStr,"POST",entity,property);
	}
	
	public int ClientGet(String urlStr,  HttpConnectionProperty property) throws ClientProtocolException, IOException{
		return ClientConnect(urlStr,"GET",null,property);
	}
	
	public String ClientGetContent() throws ParseException, IOException{
		if(response != null){
			HttpEntity entity = response.getEntity();
			if(entity.getContentType().getValue().startsWith("text/html")&&entity.getContentEncoding().getValue().equalsIgnoreCase("gzip")){
				GZIPInputStream gzipStream = new GZIPInputStream(entity.getContent());
				InputStreamReader reader = new InputStreamReader(gzipStream,charset);
				BufferedReader bufReader = new BufferedReader(reader);
				StringBuffer sb = new StringBuffer();
				String line = "";
				while((line = bufReader.readLine())!=null){
					sb.append(line);
				}
				return sb.toString();
			}
			return EntityUtils.toString(entity, charset);
		}
		return "";
	}
	
	public byte[] ClientGetImage() throws IllegalStateException, IOException{
		if(response != null){
			HttpEntity entity = response.getEntity();
			byte[] buffer = new byte[1024]; 
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream(); 
			InputStream inputStream = entity.getContent(); 
			while ((inputStream.read(buffer)) != -1) { 
				arrayOutputStream.write(buffer, 0, buffer.length); 
			}
			return arrayOutputStream.toByteArray(); 
		}
		return null;
	}
	
	public int Connect(String urlStr,String method,String postdata,HttpConnectionProperty property) throws UnknownHostException{
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method.toUpperCase());
			
			
			conn.setRequestProperty("User-Agent", property.User_Agent);
			conn.setRequestProperty("Accept", property.Accept);
			conn.setRequestProperty("Accept-Encoding", property.Accept_Encoding);
			conn.setRequestProperty("Accept-Language", property.Accept_Language);
			conn.setRequestProperty("Connection", property.Connection);
			conn.setRequestProperty("HOST", getHost(urlStr));
			conn.setRequestProperty("Referer", property.Referer);
			conn.setRequestProperty("Origin", property.Origin);
			conn.setRequestProperty("Content-Type", property.Content_Type);
			conn.setRequestProperty("Cache-Control", property.Cache_Control);
			conn.addRequestProperty("Cookie", CookieInfo.getCookie(getDomain(urlStr)));
			if(method.equalsIgnoreCase("POST")){
				//conn.setRequestProperty("Content-Length", String.valueOf(postdata.getBytes(charset).length));
				conn.setDoOutput(true); 
				conn.setDoInput(true);
				conn.setUseCaches(false);
				OutputStream stream = conn.getOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(stream, charset);
				writer.write(postdata);
				writer.flush();
				writer.close();
			}
			else{
				conn.connect();
			}
			Map<String, List<String>> headers = conn.getHeaderFields();
			List<String> cookieStr = headers.get("Set-Cookie");
			CookieInfo.setCookie(cookieStr,getHost(urlStr));
			return conn.getResponseCode();
		}
		catch(MalformedURLException e){
			e.printStackTrace();
		}
		catch(ProtocolException e){
			e.printStackTrace();
		}
		catch (UnknownHostException e){
			throw e;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return -1;
	}
	
	public int Connect(String urlStr,String method,HashMap<String, String> postdata,HttpConnectionProperty property) throws UnknownHostException{
		String data = getPostdata(postdata);
		return Connect(urlStr, method, data, property);
	}
	
	public int Connect(String urlStr) throws UnknownHostException{
		HttpConnectionProperty property = new HttpConnectionProperty();
		return Connect(urlStr, "GET", "", property);
	}
	
	//”√≤ª≥…
	/*public int MultipartPost(String urlStr,HashMap<String,String> postdata,HttpConnectionProperty property) throws UnknownHostException{
		String data = "";
		property.Content_Type = "multipart/form-data; boundary=----WebKitFormBoundarycujhe0tR9CEJHcVB";
		property.Cache_Control = "max-age=0";
		if(postdata!=null && postdata.size() > 0){
			String boundary = "----WebKitFormBoundarycujhe0tR9CEJHcVB";
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, String> item : postdata.entrySet()){
				sb.append(boundary);
				sb.append("\r\n");
				sb.append(String.format("Content-Disposition: form-data; name=\"%s\"", item.getKey()));
				sb.append("\r\n\r\n");
				sb.append(item.getValue());
				sb.append("\r\n");
			}
			sb.append(boundary);
			sb.append("--");
			data = sb.toString();
		}
		return Connect(urlStr,"POST",data,property);
	}*/
	
	public String getContent() throws IOException{
		if(conn != null){
			String content_encoding = conn.getHeaderField("Content-Encoding");
			InputStreamReader reader;
			if(content_encoding.equalsIgnoreCase("gzip")){
				GZIPInputStream gzip = new GZIPInputStream(conn.getInputStream());
				reader = new InputStreamReader(gzip,charset);
			}
			else{
				InputStream stream = conn.getInputStream();
				reader = new InputStreamReader(stream,charset);
			}
			BufferedReader bufReader = new BufferedReader(reader);
			StringBuilder response = new StringBuilder();
			String line = "";
			while((line = bufReader.readLine()) != null){
				response.append(line);
			}
			return response.toString();
		}
		return "";
	}
	
	public void close(){
		conn.disconnect();
	}
	
	private String getPostdata(HashMap<String, String> data){
		StringBuffer value = new StringBuffer();
		if(data!=null&&data.size()>0){
			for(Entry<String, String> item : data.entrySet()){
				value.append(item.getKey());
				value.append("=");
				value.append(item.getValue());
				value.append("&");
			}
			return value.substring(0, value.length() - 1);
		}
		return "";
	}
	
	private String getHost(String urlStr){
		String regEx = "http(s)?://([^/]+)/";
		Pattern pat = Pattern.compile(regEx);  
		Matcher mat = pat.matcher(urlStr);
		if(mat.find()&&mat.groupCount()>1&&mat.group(2)!=null){
			return mat.group(2);
		}
		return "";
	}
	
	private String getDomain(String urlStr){
		String host = getHost(urlStr);
		String regEx = "[^\\./]+\\.[^\\./]+$";
		Pattern pat = Pattern.compile(regEx);  
		Matcher mat = pat.matcher(host);
		if(mat.find()){
			return mat.group();
		}
		return "";
	}
		
}
