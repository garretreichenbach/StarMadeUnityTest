package org.schema.game.common.data.player;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.schema.common.util.image.ImageAnalysisResult;
import org.schema.common.util.image.ImageUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.objects.remote.RemoteLongArray;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;

public class SkinManager {
	public static final String defaultFile = "defaultMale";
	public static final String serverDB = "./server-skins/";
	private final static String extractTmpPath = "." + File.separator + "texTmp" + File.separator;
	private final PlayerState player;
	public String clientDB;
	private String fileName = "";
	private PlayerSkin textureId;
	private boolean textureIdchanged;
	private boolean openRequest;
	private ArrayList<File> filesReceivedClient = new ArrayList<File>();
	private ArrayList<String> sendQueue = new ArrayList<String>();
	private ArrayList<File> filesReceivedServer = new ArrayList<File>();
	private boolean updateTexture;

	public SkinManager(PlayerState player) {
		this.player = player;
		if (!player.isOnServer()) {

		} else {
			clientDB = null;
			File f = new FileExt(serverDB);
			f.mkdirs();
		}
	}

	private void checkReceivedFilesOnClient() throws IOException {

		if (!filesReceivedClient.isEmpty()) {
			synchronized (filesReceivedClient) {
				File receivedFile = filesReceivedClient.get(filesReceivedClient.size() - 1);

				long bytes = FileUtil.getExtractedFilesSize(receivedFile);
				
				long gbInBytes = 1024L*1024L*1024;
				if(bytes > gbInBytes){
					throw new IOException("Extracted files too big (possible zip bomb through sparse files) for "+receivedFile.getAbsolutePath()+": "+((bytes/1024L)/1024L)+" MB");
				}
				
				
				FileUtil.extract(receivedFile, clientDB + "/tmp/");

				//remove old entry
				File old = new FileExt(clientDB + player.getName() + PlayerSkin.EXTENSION);
				if (old.exists()) {
					old.delete();
				}

				FileUtil.copyFile(receivedFile, new FileExt(clientDB + player.getName() + PlayerSkin.EXTENSION));

				FileUtil.deleteDir(new FileExt(clientDB + "/tmp/"));
				filesReceivedClient.clear();
			}

		}

	}

	public void flagUpdateTexture() {
		updateTexture = true;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the textureId
	 */
	public PlayerSkin getTextureId() {
		return textureId;
	}

	public long getTimeStamp() {
		return -1;
	}

	public void handleReceivedFileOnClient(File f) {
		synchronized (filesReceivedClient) {
			filesReceivedClient.add(f);
		}
	}

	public void handleReceivedFileOnServer(File file) {
		synchronized (filesReceivedServer) {
			filesReceivedServer.add(file);
		}
	}

	public void handleServerNT() {

		for (int i = 0; i < player.getNetworkObject().skinRequestBuffer.getReceiveBuffer().size(); i++) {
			String request = player.getNetworkObject().skinRequestBuffer.getReceiveBuffer().get(i).get();
			synchronized (sendQueue) {
				sendQueue.add(serverDB + player.getName() + PlayerSkin.EXTENSION);
			}
		}

	}

	public void initFromNetworkObject() {
		if (!player.isClientOwnPlayer()) {
			System.err.println("[SKINMANAGER] " + player + " " + player.getState() + " received skin filename " + player.getNetworkObject().skinName.get());
			fileName = player.getNetworkObject().skinName.get();
		}
	}

	private void refreshOrLoadTexture() throws IOException {
		if (!player.getState().isPassive()) {
			File tmp = new FileExt(extractTmpPath);
			if (tmp.exists()) {
				FileUtil.deleteRecursive(tmp);
			}
			tmp.mkdir();
			if (!fileName.equals(SkinManager.defaultFile)) {

				if (player.isClientOwnPlayer()) {

					File check = new FileExt(fileName);
					if (!check.exists() && check.getName().endsWith(PlayerSkin.EXTENSION)) {
						throw new RuntimeException("Can't find skin image: " + fileName + "\n Please change your skin option to a valid file");
					}

					FileUtil.extract(check, extractTmpPath);

					textureId = PlayerSkin.create(tmp, player.getName());
					textureIdchanged = true;
					System.err.println("[SKIN] OWN REFRESH LOADING " + fileName + " AS " + player.getName());

				} else {
					System.err.println("[SKIN] REFRESH LOADING " + clientDB + player.getName() + PlayerSkin.EXTENSION + " AS " + player.getName());

					FileUtil.extract(new FileExt(clientDB + player.getName() + PlayerSkin.EXTENSION), extractTmpPath);
					textureId = PlayerSkin.create(new FileExt(extractTmpPath), player.getName());
					textureIdchanged = true;
				}
			} else {

				System.err.println("[SKIN] USING DEFAULT FOR PLAYER " + player);
				textureId = null;
				textureIdchanged = true;
				//				try{
				//					textureId = (Controller.getResLoader().getSprite(defaultFile).getMaterial().getTexture().getTextureId());
				//				}catch (Exception e) {
				//					e.printStackTrace();
				//				}
			}
			System.err.println("[SKIN] REMOVING TMP PATH: " + tmp.getAbsolutePath());
			FileUtil.deleteRecursive(tmp);
		}
	}

	private void transferFromServerToClient(File test) {

	}

	public void updateFromNetworkObject() {
		if (player.isOnServer() || !player.isClientOwnPlayer()) {
			fileName = player.getNetworkObject().skinName.get();
		}
		handleServerNT();
	}

	public void updateOnClient() {
		//		if(!openRequest && !player.isClientOwnPlayer()){
		//			if(!requestedFileName.equals(getFileName())){
		//				requestOnClient();
		//			}
		//		}

		if (clientDB == null && !player.isOnServer()) {
			String host = (((GameClientState) player.getState()).getController().getConnection().getHost());
			//			host = host.replaceAll("\\.+$", "");

			clientDB = "." + File.separator + "client-skins" + File.separator + ((GameClientState) player.getState()).getPlayer().getName() + File.separator + host + File.separator;
			File f = new FileExt(clientDB);
			f.mkdirs();
		}
		if (openRequest) {
			try {
				checkReceivedFilesOnClient();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (updateTexture) {
			try {
				refreshOrLoadTexture();
			} catch (Exception e) {
				e.printStackTrace();
				fileName = defaultFile;
				try {
					refreshOrLoadTexture();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			updateTexture = false;
		}

		//our player
		//		if(textureId == 0){
		//			textureId = Controller.getResLoader().getSprite(getFileName()).getMaterial().getTexture().getTextureId();
		//		}
	}

	public void updateServer() {
		if (sendQueue.size() > 0) {
			synchronized (sendQueue) {
				while (sendQueue.size() > 0) {
					String en = sendQueue.remove(0);

					File test = new FileExt(en);
					if (test.exists()) {

						if (ServerConfig.SKIN_ALLOW_UPLOAD.isOn()) {
							System.err.println("[SERVER][SkinManager] " + player + " requested: " + en);
							transferFromServerToClient(test);
						}
						//transfer file to client
					}
				}
			}
		}

		if (!filesReceivedServer.isEmpty()) {
			synchronized (filesReceivedServer) {
				try {
					File file = filesReceivedServer.get(filesReceivedServer.size() - 1);

					assert (file.exists()) : file.getAbsolutePath();

					
					long bytes = FileUtil.getExtractedFilesSize(file);
					
					long gbInBytes = 1024L*1024L*(long)(ServerConfig.ALLOWED_UNPACKED_FILE_UPLOAD_IN_MB.getInt());
					if(bytes > gbInBytes){
						throw new IOException("Extracted files too big (possible zip bomb through sparse files) for "+file.getAbsolutePath()+": "+((bytes/1024L)/1024L)+" MB");
					}
					
					//extract
					FileUtil.extract(file, "." + File.separator + "tmp" + player.getName() + File.separator);

					file.delete();

					//read info
					File texture = (new FileExt("." + File.separator + "tmp" + player.getName() + File.separator + "tmpUpload" + File.separator)).listFiles()[0];

					String testPath = serverDB + File.separator + player.getName() + File.separator;
					File test = new FileExt(testPath);
					if (test.exists() && test.isDirectory()) {
						FileUtil.deleteRecursive(test);
					} else if (test.exists() && !test.isDirectory()) {
						test.delete();
					}
					boolean valid = true;
					FileUtil.extract(texture, testPath);
					test = new FileExt(testPath);
					File[] listFiles = test.listFiles();
					for (int i = 0; i < listFiles.length; i++) {
						File f = listFiles[i];
						if (f.isDirectory()) {
							valid = false;
						} else {
							if (!f.getName().equals("skin_main_em.png") && !f.getName().equals("skin_main_diff.png") && !f.getName().equals("skin_helmet_em.png") && !f.getName().equals("skin_helmet_diff.png")) {
								valid = false;
							} else {
								try {
									ImageAnalysisResult r = ImageUtil.analyzeImage(f);
									if (!r.isImage()) {
										System.err.println("[SERVER][SKIN] file " + f.getName() + " is not a valid image file! Uploaded by " + player.getName());
										valid = false;
									} else if (r.isTruncated()) {
										System.err.println("[SERVER][SKIN] file " + f.getName() + " is not a valid PNG file! Uploaded by " + player.getName());
										valid = false;
									} else if (!r.isPowerOfTwo()) {
										System.err.println("[SERVER][SKIN] file " + f.getName() + " is not a valid PNG file (dimensions are not power of 2)! Uploaded by " + player.getName());
										player.sendServerMessagePlayerWarning(Lng.astr("ERROR: can only use skin \ntextures with height and\nwidth in power of two\n(256,512,1024)"));
										valid = false;
									}
								} catch (NoSuchAlgorithmException e) {
									System.err.println("[SERVER][SKIN] file " + f.getName() + " is not a valid PNG file! Uploaded by " + player.getName());
									e.printStackTrace();
									valid = false;
								} catch (IOException e) {
									System.err.println("[SERVER][SKIN] file " + f.getName() + " is not a valid PNG file! Uploaded by " + player.getName());
									e.printStackTrace();
									valid = false;
								} catch (UnsatisfiedLinkError e) {

									e.printStackTrace();
									((GameServerState) player.getState()).getController()
											.broadcastMessageAdmin(Lng.astr("ADMIN WARNING\nCannot analyze skin file!\n(In case the server uses OpenJDK8\nit may have a bug, OpenJDK7 works)"), ServerMessage.MESSAGE_TYPE_ERROR);
								}
							}

						}
					}

					//					DataInputStream s = new DataInputStream(new FileInputStream(info));
					//					String name = s.readUTF();
					//					long timeChanged = s.readLong();
					//					s.close();
					if (valid) {
						//replace with new
						File old = new FileExt(serverDB + File.separator + player.getName() + PlayerSkin.EXTENSION);

						if (old.exists()) {
							old.delete();
						}
						old = new FileExt(serverDB + File.separator + player.getName() + PlayerSkin.EXTENSION);
						FileUtil.copyFile(texture, old);

						old.setLastModified(System.currentTimeMillis());
						//broadcast file change to clients
						fileName = texture.getName();
						//					lastChangedFile = timeChanged;

						FileUtil.deleteDir(new FileExt("." + File.separator + "tmp" + player.getName() + File.separator));

						RemoteLongArray l = new RemoteLongArray(2, player.getNetworkObject());
						l.set(0, old.lastModified());
						l.set(1, old.length());

						player.getNetworkObject().textureChangedBroadcastBuffer.add(l);
					} else {
						((GameServerState) player.getState()).getController()
								.broadcastMessageAdmin(Lng.astr("ADMIN WARNING\nPlayer %s\ntried to upload corrupted\nskin file",  player.getName()), ServerMessage.MESSAGE_TYPE_ERROR);
						texture.delete();
						FileUtil.deleteDir(new FileExt("." + File.separator + "tmp" + player.getName() + File.separator));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				filesReceivedServer.clear();
			}

		}
	}

	public void updateToNetworkObject() {
		if (player.isOnServer()) {

			player.getNetworkObject().skinName.set(fileName);
		} else {
			if (player.isClientOwnPlayer()) {
				//				System.err.println("SETTING OWN SKIN NAME TO "+getFileName());
				player.getNetworkObject().skinName.forceClientUpdates();
				player.getNetworkObject().skinName.set(fileName);
			}
		}
	}

	public void uploadFromClient(String filePath) throws IOException, UploadInProgressException {
		this.fileName = filePath;

		File old = new FileExt(clientDB + player.getName() + PlayerSkin.EXTENSION);
		long tChanged = 0;
		if (old.exists()) {
			old.delete();
		}
		File file = new FileExt(filePath);
		FileUtil.copyFile(file, old);

		old.setLastModified(file.lastModified());

		player.getSkinUploadController().upload(fileName);
	}

	/**
	 * @return the textureIdchanged
	 */
	public boolean isTextureIdchanged() {
		return textureIdchanged;
	}

	/**
	 * @param textureIdchanged the textureIdchanged to set
	 */
	public void setTextureIdchanged(boolean textureIdchanged) {
		this.textureIdchanged = textureIdchanged;
	}

}
