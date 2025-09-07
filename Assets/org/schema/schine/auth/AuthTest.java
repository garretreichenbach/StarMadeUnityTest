package org.schema.schine.auth;

import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class AuthTest {
	public static final String REDIRECT = "urn:ietf:wg:oauth:2.0:oob";
	public static final String APIURL = "https://registry.star-made.org/api/v1/users/me.json";//"registry.star-made.org";
	public static final String APIURL_AUTH = "https://registry.star-made.org/api/v1/servers/authenticateUser";//"registry.star-made.org";
	public static final String APIURL_PERSONAL = "https://registry.star-made.org/api/v1/users/me/personalInfo";//"registry.star-made.org";
	public static final String URL = "https://registry.star-made.org";//"registry.star-made.org";
	public static final String URL_AUTH = "https://registry.star-made.org";//"registry.star-made.org";
	public static final String APP_ID = "ab461341b8e3e4ba398425eb412d562135ad544b5b9cb1f627cbe44dfe7d2e77";
	public static final String TOKEN_SERVER_URL = "https://registry.star-made.org/oauth/token";
	public static final String AUTHORIZATION_SERVER_URL = "https://registry.star-made.org/oauth/authorize";
	public static String testUser = "test";
	public static String testPW = "thisisatestaccount";

	public static void main(String[] aoa) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		String token;
		{
			String data = URLEncoder.encode("client_id", "UTF-8") + "=" + URLEncoder.encode(APP_ID, "UTF-8");
			data += "&" + URLEncoder.encode("grant_type", "UTF-8") + "=" + URLEncoder.encode("password", "UTF-8");
			data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(testUser, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(testPW, "UTF-8");

			System.err.println(data);
			
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

	        } };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = (hostname, session) -> true;
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			
			// Send data
			URL url = new URL(TOKEN_SERVER_URL);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//			conn.setHostnameVerifier(new TrustAllHostNameVerifier());
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
			URL url = new URL(APIURL);
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
