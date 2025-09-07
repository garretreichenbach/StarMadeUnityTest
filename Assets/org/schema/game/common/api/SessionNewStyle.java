package org.schema.game.common.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;

import org.json.JSONObject;
import org.schema.game.common.Starter;
import org.schema.schine.auth.Session;
import org.schema.schine.auth.exceptions.WrongUserNameOrPasswordException;
import org.schema.schine.tools.rss.Feed;
import org.schema.schine.tools.rss.RSSFeedParser;

public class SessionNewStyle implements Session {

	private boolean loggedIn = false;
	private String authToken;
	private String serverName;
	private String token;
	private String registryName = "";
	long registryId;
	private String skinURL;
	private long id;

	public SessionNewStyle(String serverName) {
		this.serverName = serverName;
	}

	public void loginWithExistingToken(String token) throws IOException {
		this.token = token;
		System.err.println("[API-LOGIN] REQUESTING OWN INFO");
		fillIdAndName(token);
		System.err.println("[API-LOGIN] REQUESTING AUTH TOKEN TO AUTHORIZE ON SERVER");
		this.authToken = ApiOauthController.requestAuthToken(token);

		Starter.loggedIn = true;
		Starter.currentSession = this;
		loggedIn = true;
	}
	private void fillIdAndName(String token) throws IOException{
		
		JSONObject requestSelf = ApiOauthController.requestSelf(token);
		try{
			JSONObject user = requestSelf.getJSONObject("user");
			registryName = user.getString("username");
			id = user.getLong("id");
			skinURL = user.getString("skin_url");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	public void login(String username, String passwd) throws IOException, WrongUserNameOrPasswordException {
		token = ApiOauthController.login(username, passwd);
		System.err.println("[API-LOGIN] REQUESTING OWN INFO");
		fillIdAndName(token);
		System.err.println("[API-LOGIN] REQUESTING AUTH TOKEN TO AUTHORIZE ON SERVER");
		this.authToken = ApiOauthController.requestAuthToken(token);

		Starter.loggedIn = true;
		Starter.currentSession = this;
		loggedIn = true;
	}

	@Override
	public boolean isValid() {
		return loggedIn;
	}

	@Override
	public void upload(File f, String bbName, int bbType, String description, String licence, JDialog jFrame) throws IOException {
		if (token != null) {
			ApiOauthController.upload(token, f, bbName, bbType, description, licence, jFrame);
			//			System.err.println("ALL UPLOADS: \n"+ApiOauthController.getAllUploads(token, jFrame));
		} else {
			throw new IOException("No token created to upload");
		}
	}

	@Override
	public ArrayList<String> retrieveNews(int max) {
		ArrayList<String> news = new ArrayList<String>();
		RSSFeedParser rss = new RSSFeedParser("http://star-made.org/news.rss");
		Feed readFeed = rss.readFeed(max);

		System.err.println("FEED: " + readFeed);

		for (int i = 0; i < readFeed.getMessages().size(); i++) {
			System.err.println("FEEDMSG: " + readFeed.getMessages().get(i));
		}

		for (int i = 0; i < readFeed.getMessages().size(); i++) {
			StringBuilder contentBuilder = new StringBuilder();
			URL url;
			InputStream is = null;
			BufferedReader br;
			String line;

			try {
				url = new URL(readFeed.getMessages().get(i).getLink());
				is = url.openStream();  // throws an IOException
				br = new BufferedReader(new InputStreamReader(is));

				while ((line = br.readLine()) != null) {
					contentBuilder.append(line);
				}
			} catch (MalformedURLException mue) {
				mue.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				try {
					if (is != null) is.close();
				} catch (IOException ioe) {
					// nothing to see here
				}
			}
			String content = contentBuilder.toString();

			//
			news.add(getDiv("page-header", content) + getDiv("postBody", content));

			if (news.isEmpty()) {
				System.out.println("Not found");
			}
		}
		return news;
	}

	@Override
	public String getUniqueSessionId() {
		return serverName;
	}

	@Override
	public String getAuthTokenCode() {
		return authToken;
	}

	@Override
	public void afterLogin() {
		
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	private String getDiv(String div, String from) {
		// Compile and use regular expression
		String patternStr = "<div class=\"" + div + "\">(.*?)</div>";

		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(from);

		while (matcher.find()) {
			// Get all groups for this match
			if(matcher.groupCount() > 0) {
				String groupStr = matcher.group(0);
				System.out.println("Group found:\n" + groupStr);
				return groupStr;

			}
		}
		return "cound not be retrieved";
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRegistryName() {
		return registryName;
	}

	public long getUserId() {
		return id;
	}
	
	public String getSkinURL(){
		return skinURL;
	}

}
