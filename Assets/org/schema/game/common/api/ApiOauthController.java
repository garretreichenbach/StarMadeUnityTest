package org.schema.game.common.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.schema.schine.auth.exceptions.BadUplinkTokenException;
import org.schema.schine.auth.exceptions.WrongUserNameOrPasswordException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

//import com.sun.net.ssl.SSLContext;
//import com.sun.net.ssl.TrustManager;
//import com.sun.net.ssl.X509TrustManager;

// #RM1958 remove import sun.net.www.protocol.https.HttpsURLConnectionImpl;

public class ApiOauthController {
	//		public static final String REDIRECT = "urn:ietf:wg:oauth:2.0:oob";
	public static final String REQUEST_SELF_URL = "https://registry.star-made.org/api/v1/users/me.json";//"registry.star-made.org";
	public static final String AUTHTOKEN_REQ_URL = "https://registry.star-made.org/api/v1/users/login_request.json";//"registry.star-made.org";
	public static final String AUTHTOKEN_VERIFY_URL = "https://registry.star-made.org/api/v1/servers/login_request";//"registry.star-made.org";
	//public static final String APIURL_AUTH = "https://registry.star-made.org/api/v1/servers/authenticateUser";//"registry.star-made.org";
	//public static final String APIURL_PERSONAL = "https://registry.star-made.org/api/v1/users/me/personalInfo";//"registry.star-made.org";
	public static final String APIURL_BLUEPRINT = "https://registry.star-made.org/api/v1/blueprints.json";//"registry.star-made.org";
	public static final String URL = "https://registry.star-made.org";//"registry.star-made.org";
	//public static final String URL_AUTH = "https://registry.star-made.org";//"registry.star-made.org";
	public static final String TOKEN_SERVER_URL = "https://registry.star-made.org/oauth/token";
	public static final String DEDICATED_SERVER_TOKEN_SERVER = "https://registry.star-made.org/tbd";
	//public static final String AUTHORIZATION_SERVER_URL = "https://registry.star-made.org/oauth/authorize";
	public static String testUser = "test";
	public static String testPW = "thisisatestaccount";

	static TrustManager[] trustAllCerts = new TrustManager[]{
	        new X509TrustManager() {

	            public java.security.cert.X509Certificate[] getAcceptedIssuers()
	            {
	                return null;
	            }
	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
	            {
	                //No need to implement.
	            }
	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
	            {
	                //No need to implement.
	            }
//				@Override
//				public boolean isClientTrusted(X509Certificate[] arg0) {
//					return true;
//				}
//				@Override
//				public boolean isServerTrusted(X509Certificate[] arg0) {
//					return true;
//				}
	        }
	};
	static {
		// Install the all-trusting trust manager
		try 
		{
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} 
		catch (Exception e) 
		{
		    System.out.println(e);
		}
	}
	
	public static String uplinkDedicatedServer(String uplinkToken) throws IOException, BadUplinkTokenException {
		String data = "";
		// Add data here, once API is created
		URL url = new URL(DEDICATED_SERVER_TOKEN_SERVER);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setReadTimeout(20000);
		conn.setRequestMethod("POST");

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		writer.write(data);
		writer.flush();

		int response = conn.getResponseCode();

		if (response == 401) {
			throw new BadUplinkTokenException("An incorrect secure uplink token has been entered in the server.cfg file. Please check 'SECURE_UPLINK_TOKEN' is correct.");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;

		StringBuilder builder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject json = new JSONObject(builder.toString());

		reader.close();
		return json.get("access_token").toString();
	}
	public static String login(String userName, String passwd) throws IOException, WrongUserNameOrPasswordException {
		String data = "";
		data += URLEncoder.encode("grant_type", "UTF-8") + "=" + URLEncoder.encode("password", "UTF-8");
		data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8");
		data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(passwd, "UTF-8");
		data += "&" + URLEncoder.encode("scope", "UTF-8") + "=" + URLEncoder.encode("public read_citizen_info client", "UTF-8");
		URL url = new URL(TOKEN_SERVER_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setReadTimeout(20000);

		conn.setRequestMethod("POST");

		long t = System.currentTimeMillis();

		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		int responseCode = conn.getResponseCode();

		if (responseCode == 401) {
			throw new WrongUserNameOrPasswordException("Your credentials are invalid. If you need to reset them, visit registry.star-made.org.");
		}
		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;

		StringBuffer b = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			//				System.err.println(line);
			b.append(line);
		}
		long took = System.currentTimeMillis() - t;
		JSONObject json = new JSONObject(b.toString());

		rd.close();
		return json.get("access_token").toString();

	}

	public static HttpURLConnection getTokenConnection(String url) throws IOException {
		// #RM1958 replace Sun-implementation-specific URLStreamHandler with non-specific call 
		java.net.URL wsURL = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) wsURL.openConnection();

		//			java.net.URL wsURL = new URL(url);
		//	        HttpURLConnection  conn = (HttpURLConnection )wsURL.openConnection();
		conn.setDoOutput(true);
		conn.setReadTimeout(20000);
		return conn;

	}

	public static JSONObject getResponse(HttpURLConnection conn) throws IOException {
		int responseCode = conn.getResponseCode();
		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuffer b = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			b.append(line);
		}
		rd.close();
		JSONObject json = new JSONObject(b.toString());
		return json;
	}

	public static JSONObject requestSelf(String token) throws IOException {
		HttpURLConnection conn = getTokenConnection(REQUEST_SELF_URL);
		conn.setRequestProperty("Authorization", "Bearer " + token);
		conn.setRequestMethod("GET");
		JSONObject p = getResponse(conn);
		return p;
	}

	/*
		 t.string   "name"
		    t.integer  "blueprint_type"
		    t.string   "file"
		    t.text     "description"
		    t.string   "license"
		    t.datetime "created_at"
		    t.datetime "updated_at"
		The first 5 are the ones you have to send
		In a POST request to http://registry.star-made.org/blueprints
		Blueprint type is synonmous with your integer based
		class Blueprint < ActiveRecord::Base

		  TYPES = { 0 => "Ship", 1 => "Shop", 2 => "Station", 4 => "Asteroid", 5 => "Planet" }

	 */
	public static JSONObject getAllUploads(String token, JFrame jFrame) throws IOException {
		HttpURLConnection conn = getTokenConnection(APIURL_BLUEPRINT);
		conn.setRequestProperty("Authorization", "Bearer " + token);
		conn.setRequestMethod("GET");
		JSONObject p = getResponse(conn);
		return p;
	}

	public static void upload(String token, File f, String bbName, int bbType, String description, String licence, JDialog jFrame) throws IOException {

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(APIURL_BLUEPRINT);

		httpPost.addHeader("Authorization", "Bearer " + token);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		/* example for setting a HttpMultipartMode */
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		/* example for adding an image part */
		FileBody fileBody = new FileBody(f);
		builder.addPart("file", fileBody);
		builder.addPart("blueprint_type", new StringBody(String.valueOf(bbType), ContentType.DEFAULT_TEXT));
		builder.addPart("name", new StringBody(bbName, ContentType.DEFAULT_TEXT));
		builder.addPart("description", new StringBody(description, ContentType.DEFAULT_TEXT));

		HttpEntity build = builder.build();

		httpPost.setEntity(build);

		HttpResponse response = client.execute(httpPost);

		String responseString = new BasicResponseHandler().handleResponse(response);

		System.err.println("UPLOAD RESPONSE: " + responseString);
		JSONObject json = new JSONObject(responseString);
		//		Reader reader = new InputStreamReader(response.getEntity().getContent());
		//
		//
		//
		//			HttpURLConnection  conn = getTokenConnection(APIURL_BLUEPRINT);
		//			conn.setRequestMethod("POST");
		//
		//			System.err.println("UPLOADING with token "+token);
		//
		//
		//
		//
		//			MIMEMultipart mmp = new MIMEMultipart();
		//			mmp.putStandardParam( "name", bbName,  "UTF-8" );
		//			mmp.putStandardParam( "blueprint_type", String.valueOf(bbType), "UTF-8" );
		//			mmp.putBinaryFileParam( "file",f,  "application/binary","UTF-8" );
		//			mmp.putStandardParam( "description", description, "UTF-8" );
		//			mmp.finish();
		//
		//			System.err.println("POST PARAMS: "+mmp.toString());
		//			conn.setDoOutput(true);
		//			conn.setUseCaches(false);
		//
		//			conn.setRequestProperty("Authorization", "Bearer "+token);
		//			conn.setRequestMethod("POST");
		//			conn.setRequestProperty("Accept-Charset", "UTF-8");
		//			conn.setRequestProperty("Content-Type", "multipart/form-data, boundary=" +mmp.getBoundary());
		//			conn.setRequestProperty("Content-Length",
		//			    Integer.toString(mmp.getLength()));
		//
		//			OutputStream output = conn.getOutputStream();
		//			output.write( mmp.getContent().getBytes() );
		//			output.flush();
		//			output.close();
		//
		//			JSONObject p = getResponse(conn);
		//			p.toString();
	}

	public static String requestAuthToken(String token) throws IOException {
		System.err.println("[OAuth2] Requesting Auth Token " + AUTHTOKEN_REQ_URL);
		HttpURLConnection conn = getTokenConnection(AUTHTOKEN_REQ_URL);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "Bearer " + token);
		JSONObject p = getResponse(conn);

		if (p.has("error")) {
			System.err.println("[OAuth2] Error has been detected in response " + p.toString());
		} else {
			System.err.println("[OAuth2] server_auth token acquired");
		}
		return p.get("token").toString();
	}

	public static JSONObject verifyAuthToken(String authToken, String serverName) throws IOException {
		System.err.println("[OAuth2] Verifying Auth Token '" + authToken + "'");
		HttpURLConnection conn = getTokenConnection(AUTHTOKEN_VERIFY_URL);
		conn.setRequestMethod("POST");

		String data = "";
		data += URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(authToken, "UTF-8");
		data += "&" + URLEncoder.encode("address", "UTF-8") + "=" + URLEncoder.encode(serverName, "UTF-8");

		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		JSONObject p = getResponse(conn);
		p.toString();
		wr.close();

		return p;
	}

	public static void main(String[] aoa) throws IOException {
		if(aoa != null && aoa.length == 2){
			testUser = aoa[0];
			testPW = aoa[1];
		}
		String token;
		{
			//			String data = URLEncoder.encode("client_id", "UTF-8") + "=" + URLEncoder.encode(APP_ID, "UTF-8");
			//			data += "&" + URLEncoder.encode("client_secret", "UTF-8") + "=" + URLEncoder.encode(APP_SECRET, "UTF-8");
			String data = URLEncoder.encode("grant_type", "UTF-8") + "=" + URLEncoder.encode("password", "UTF-8");
			data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(testUser, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(testPW, "UTF-8");
			data += "&" + URLEncoder.encode("scope", "UTF-8") + "=" + URLEncoder.encode("public read_citizen_info client", "UTF-8");

			System.err.println(data);
			// Send data
			URL url = new URL(TOKEN_SERVER_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setReadTimeout(20000);

			conn.setRequestMethod("POST");

			long t = System.currentTimeMillis();

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			int responseCode = conn.getResponseCode();
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			StringBuffer b = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				System.err.println(line);
				b.append(line);
			}
			long took = System.currentTimeMillis() - t;
			JSONObject json = new JSONObject(b.toString());

			token = json.get("access_token").toString();
			System.err.println("TOKEN: " + token + " (TIME: " + took + "ms)");
			rd.close();
		}
		{
			// Send data
			URL url = new URL(REQUEST_SELF_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setReadTimeout(20000);

			conn.setRequestProperty("Authorization", "Bearer " + token);

			int responseCode = conn.getResponseCode();
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			while ((line = rd.readLine()) != null) {
				System.err.println(line);
			}

			rd.close();
		}
	}
}
