package org.schema.game.common.api.steam;

import com.codedisaster.steamworks.*;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class SteamAPIHandler {

	public static boolean initialized;

	public SteamRemoteStorage remoteStorage;
	private SteamCatalogManager catalogManager;

	public SteamAPIHandler() {
		try {
			SteamAPI.loadLibraries();
			if(SteamAPI.isSteamRunning(true)) {
				initialized = SteamAPI.init();
				if(initialized) {
					catalogManager = new SteamCatalogManager(this);
					remoteStorage = new SteamRemoteStorage(catalogManager);
					catalogManager.loadCatalog();
					catalogManager.writeCatalog();
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
