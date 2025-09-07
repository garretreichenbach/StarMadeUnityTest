package org.schema.schine.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.schema.schine.common.util.ErrorMessage;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.server.ServerEntry;

import com.bulletphysics.util.ObjectArrayList;

public class ServerListRetriever extends GUIObservable {

	public static final String serverListURL = "http://files-origin.star-made.org/serverlist";
	public static ThreadPoolExecutor theadPool;
	public boolean loading = false;
	public boolean active = true;
	public int loaded = 0;
	public int toLoad = 0;
	public int timeouts = 0;
	public int failed = 0;
	public List<ServerListListener> listener = new ObjectArrayList<ServerListRetriever.ServerListListener>();
	public interface ServerListListener{
		public void onAddedInfo(ServerInfo serverInfo);
		public void onErrorMessage(ErrorMessage msg);
	}
	
	

	public void startRetrieving() {
		
		synchronized(serverListURL){
			if (theadPool != null) {
				try {
					theadPool.shutdownNow();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			theadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(40);
		}
		
		loaded = 0;
		timeouts = 0;
		failed = 0;

		notifyObservers();
		

		System.out.println("[SERVERLIST] Retrieving Server List");

		ArrayList<ServerEntry> retrievedServerList = new ArrayList<ServerEntry>();
		
		try {
			retrievedServerList.addAll(retrieveServerList());
			System.out.println("[SERVERLIST] Successfully Retrieved Online Server List! Size: " + retrievedServerList.size());
		} catch(Exception e) {
			e.printStackTrace();
		}

		try {
			List<ServerEntry> read = ServerEntry.read("customservers.smsl");
			System.out.println("[SERVERLIST] Successfully Retrieved Locally Saved Server List! Size: " + read.size());
			retrievedServerList.addAll(read);
			
			for(ServerEntry e : retrievedServerList){
				if(read.contains(e)){
					e.custom = true;
				}
			}
		} catch (IOException e) {
		}
		System.out.println("[SERVERLIST] Total Servers to fetch: " + retrievedServerList.size());

		
		try {
			List<ServerEntry> read = ServerEntry.read("favorites.smsl");
			
			for(ServerEntry e : retrievedServerList){
				if (read.contains(e)) {
					e.favorite = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		toLoad = retrievedServerList.size();

		notifyObservers();

		for (int i = 0; i < retrievedServerList.size(); i++) {
			retrieve(retrievedServerList.get(i));
		}

	}
	
	private void retrieve(final ServerEntry serverEntry) {
		theadPool.execute(() -> {
			StarMadeNetUtil u = new StarMadeNetUtil();
			ServerInfo serverInfo;
			try {
//					System.out.println("[SERVERLIST] Checking: "+serverEntry.getLine());
				serverInfo = u.getServerInfo(serverEntry.host, serverEntry.port, 5000);
				System.out.println("[SERVERLIST] Retrieved: " + serverEntry.getLine() + ": " + serverInfo.getName() + "; Ping: " + serverInfo.getPing() + "; Type: " + serverInfo.getConnType());
				loaded++;


			} catch (UnknownHostException e) {
				e.printStackTrace();
				serverInfo = new ServerInfo(serverEntry.host, serverEntry.port);
				failed++;
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				serverInfo = new ServerInfo(serverEntry.host, serverEntry.port);
				timeouts++;
			} catch (IOException e) {
				e.printStackTrace();
				serverInfo = new ServerInfo(serverEntry.host, serverEntry.port);
				failed++;
			}
			serverInfo.setFavorite(serverEntry.favorite);
			serverInfo.setCustom(serverEntry.custom);
			if (active) {
				for(ServerListListener l : listener) {
					l.onAddedInfo(serverInfo);
				}
			}
			notifyObservers();
		});
	}

	public ArrayList<ServerEntry> retrieveServerList() {
		loading = true;
		ArrayList<ServerEntry> entries = new ArrayList<ServerEntry>();
		URL urlMirrors;
		try {
			urlMirrors = new URL(serverListURL);

			URLConnection openConnection = urlMirrors.openConnection();
			openConnection.setConnectTimeout(10000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Client");
			int version = 0;
			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream())));
			String str;
			while ((str = in.readLine()) != null) {
				try {
					ServerEntry e = new ServerEntry(str);
					entries.add(e);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			in.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			//GLFrame.processErrorDialogException(e, null);
			notifyError(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			//GLFrame.processErrorDialogException(e, null);
			notifyError(e);
		} catch (IOException e) {
			e.printStackTrace();
			//GLFrame.processErrorDialogException(e, null);
			notifyError(e);
		} finally {
			loading = false;
		}
		return entries;
	}

	private void notifyError(Exception e) {
		for(ServerListListener l : listener) {
			l.onErrorMessage(new ErrorMessage(e.getMessage()));
		}
	}
}
