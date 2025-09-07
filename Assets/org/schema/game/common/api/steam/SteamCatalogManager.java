package org.schema.game.common.api.steam;

import com.codedisaster.steamworks.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.schema.game.client.data.terrain.BufferUtils;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class SteamCatalogManager implements SteamRemoteStorageCallback {

	private final SteamAPIHandler steamAPIHandler;
	private final HashMap<SteamAPICall, String> callStack = new HashMap<>();
	private final int BLUEPRINT_MAX_SIZE = 1024 * 1024 * 10; //10MB
	private final int TEMPLATE_MAX_SIZE = 1024 * 1024; //1MB
	private long lastUpdated = System.currentTimeMillis();

	public SteamCatalogManager(SteamAPIHandler steamAPIHandler) {
		this.steamAPIHandler = steamAPIHandler;
	}

	@Override
	public void onFileShareResult(SteamUGCHandle steamUGCHandle, String s, SteamResult steamResult) {

	}

	@Override
	public void onDownloadUGCResult(SteamUGCHandle steamUGCHandle, SteamResult steamResult) {

	}

	@Override
	public void onPublishFileResult(SteamPublishedFileID steamPublishedFileID, boolean b, SteamResult steamResult) {

	}

	@Override
	public void onUpdatePublishedFileResult(SteamPublishedFileID steamPublishedFileID, boolean b, SteamResult steamResult) {

	}

	@Override
	public void onPublishedFileSubscribed(SteamPublishedFileID steamPublishedFileID, int i) {

	}

	@Override
	public void onPublishedFileUnsubscribed(SteamPublishedFileID steamPublishedFileID, int i) {

	}

	@Override
	public void onPublishedFileDeleted(SteamPublishedFileID steamPublishedFileID, int i) {

	}

	@Override
	public void onFileWriteAsyncComplete(SteamResult steamResult) {

	}

	@Override
	public void onFileReadAsyncComplete(SteamAPICall steamAPICall, SteamResult steamResult, int i, int i1) {
		if(steamResult == SteamResult.OK) {
			if(callStack.containsKey(steamAPICall)) {
				String fileName = callStack.get(steamAPICall);
				try {
					if(fileName.equals("catalog.json")) {
						ByteBuffer buffer = BufferUtils.createByteBuffer(i1);
						steamAPIHandler.remoteStorage.fileReadAsync("catalog.json", buffer.position(), buffer.remaining());
						JSONObject catalog = new JSONObject(new String(buffer.array(), StandardCharsets.UTF_8));
						readCatalog(catalog);
					} else if(fileName.endsWith(".sment")) {
						ByteBuffer buffer = BufferUtils.createByteBuffer(i1);
						steamAPIHandler.remoteStorage.fileReadAsync(fileName, buffer.position(), buffer.remaining());
						File file = new File(fileName);
						String blueprintName = fileName.substring(0, fileName.length() - 6);
						FileUtils.writeByteArrayToFile(file, buffer.array());
						if(BluePrintController.active.getBlueprint(blueprintName) != null) {
							BluePrintController.active.export(blueprintName); //Export backup
						}
						BluePrintController.active.importFile(file, null);
					} else if(fileName.endsWith(".smtpl")) {
						ByteBuffer buffer = BufferUtils.createByteBuffer(i1);
						steamAPIHandler.remoteStorage.fileReadAsync(fileName, buffer.position(), buffer.remaining());
						File file = new File("./templates/" + fileName);
						FileUtils.writeByteArrayToFile(file, buffer.array());
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	private void readCatalog(JSONObject catalog) {
		lastUpdated = catalog.getLong("lastUpdated");
		List<BlueprintEntry> entryList = BluePrintController.active.readBluePrints();
		for(BlueprintEntry entry : entryList) {
			long lastModified = entry.getLastModified();
			if(lastModified < lastUpdated) {
				ByteBuffer buffer = BufferUtils.createByteBuffer(BLUEPRINT_MAX_SIZE);
				String name = entry.getName() + ".sment";
				if(steamAPIHandler.remoteStorage.fileExists(name)) {
					callStack.put(steamAPIHandler.remoteStorage.fileReadAsync(name, buffer.position(), buffer.remaining()), name);
				}
			}
		}

		File templateFolder = new File("./templates");
		if(!templateFolder.exists()) templateFolder.mkdirs();
		for(File file : Objects.requireNonNull(templateFolder.listFiles())) {
			if(file.getName().endsWith(".smtpl")) {
				long lastModified = file.lastModified();
				if(lastModified < lastUpdated) {
					ByteBuffer buffer = BufferUtils.createByteBuffer(TEMPLATE_MAX_SIZE);
					String name = file.getName();
					if(steamAPIHandler.remoteStorage.fileExists(name)) {
						callStack.put(steamAPIHandler.remoteStorage.fileReadAsync(name, buffer.position(), buffer.remaining()), name);
					}
				}
			}
		}
	}

	public void loadCatalog() {
		File catalogFile = new File("catalog.json");
		JSONObject catalog;
		ByteBuffer buffer;
		try {
			if(!steamAPIHandler.remoteStorage.fileExists("catalog.json")) {
				catalog = new JSONObject();
				buffer = BufferUtils.createByteBuffer(catalog.toString().getBytes().length);
				buffer.put(catalog.toString().getBytes());
				steamAPIHandler.remoteStorage.fileWriteAsync("catalog.json", buffer);
			}
			buffer = BufferUtils.createByteBuffer((int) catalogFile.length());
			steamAPIHandler.remoteStorage.fileReadAsync("catalog.json", buffer.position(), buffer.remaining());
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public void writeCatalog() {
		JSONObject catalog;
		ByteBuffer buffer;
		boolean write = false;
		try {
			for(BlueprintEntry entry : BluePrintController.active.readBluePrints()) {
				if(entry.getLastModified() > lastUpdated) {
					ByteBuffer blueprintBuffer = BufferUtils.createByteBuffer(BLUEPRINT_MAX_SIZE);
					BluePrintController.active.export(entry.getName());
					File file = new File(entry.getName() + ".sment");
					byte[] data = FileUtils.readFileToByteArray(file);
					blueprintBuffer.put(data);
					steamAPIHandler.remoteStorage.fileWriteAsync(entry.getName() + ".sment", blueprintBuffer);
					write = true;
				}
			}

			File templateFolder = new File("./templates");
			if(!templateFolder.exists()) templateFolder.mkdirs();
			for(File file : Objects.requireNonNull(templateFolder.listFiles())) {
				if(file.getName().endsWith(".smtpl")) {
					long lastModified = file.lastModified();
					if(lastModified > lastUpdated) {
						ByteBuffer templateBuffer = BufferUtils.createByteBuffer(TEMPLATE_MAX_SIZE);
						byte[] data = FileUtils.readFileToByteArray(file);
						templateBuffer.put(data);
						steamAPIHandler.remoteStorage.fileWriteAsync(file.getName(), templateBuffer);
						write = true;
					}
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}

		if(write) {
			try {
				catalog = new JSONObject();
				catalog.put("lastUpdated", lastUpdated);
				buffer = BufferUtils.createByteBuffer(catalog.toString().getBytes().length);
				buffer.put(catalog.toString().getBytes());
				steamAPIHandler.remoteStorage.fileWriteAsync("catalog.json", buffer);
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}
