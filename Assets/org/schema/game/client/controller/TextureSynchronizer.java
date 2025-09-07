package org.schema.game.client.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SkinManager;
import org.schema.game.network.objects.StringLongLongPair;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.FileExt;

public class TextureSynchronizer implements SendableAddedRemovedListener {

	private final ArrayList<FileChangeBroadcast> receivedFileBroadcasts = new ArrayList<FileChangeBroadcast>();
	private final ArrayList<File> receivedFiles = new ArrayList<File>();
	private final ArrayList<PlayerState> playersToCheckQueue = new ArrayList<PlayerState>();
	private final ArrayList<StringLongLongPair> timeStampResponses = new ArrayList<StringLongLongPair>();
	private final Map<PlayerState, TextureSynchronizationState> playerRequestMap = new HashMap<PlayerState, TextureSynchronizationState>();
	private GameClientState state;
	private String ownModelPath = SkinManager.defaultFile;
	private boolean mapChanged = false;
	private boolean ownPlayerSatisfied = false;
	private TextureSynchronizationState fileRequest;
	private boolean ownSkinClientTimeStampSet;

	public TextureSynchronizer(GameClientState state) {
		this.state = state;
		state.getController().addSendableAddedRemovedListener(this);

	}

	private void availableServerTimeStampOtherPlayer(
			TextureSynchronizationState s) {
		if (s.getPlayer().getSkinManager().getFileName().length() == 0) {
		} else {
			if (s.getPlayer().getSkinManager().getFileName()
					.equals(SkinManager.defaultFile)) {
				// default texture, so no need for request
				s.setReceived(true);
				s.getPlayer().getSkinManager().flagUpdateTexture();
			} else {

				if (s.getClientTimeStamp() < s.getServerTimeStamp() || s.getClientSize() != s.getServerSize()) {
					//local file is older, so request
					this.fileRequest = s;
				} else {
					//local file is newer, so do update without request
					s.setReceived(true);
					s.getPlayer().getSkinManager().flagUpdateTexture();
				}
			}
		}
	}

	private void availableServerTimeStampOwnPlayer(TextureSynchronizationState s) {

		if (s.getClientTimeStamp() > s.getServerTimeStamp() || s.getClientSize() != s.getServerSize()) {
			if (ownModelPath.equals(SkinManager.defaultFile)) {

				//				System.err.println("[CLIENT][TEXTURE-SYNCH] setting default file name "+ownModelPath);
				state.getPlayer().getSkinManager().setFileName(ownModelPath);
			} else {
				if (s.getClientTimeStamp() < s.getServerTimeStamp()) {
					//					System.err.println("[CLIENT][TEXTURE-SYNCH][OWN] our version is newer by "+(s.getClientTimeStamp()-s.getServerTimeStamp())+" ms upload required");
				}
				if (s.getClientSize() != s.getServerSize()) {
					//					System.err.println("[CLIENT][TEXTURE-SYNCH][OWN] our version has different size: c "+s.getClientSize()+ " / s "+s.getServerSize()+" upload required");
				}
				//				System.err.println("[CLIENT][TEXTURE-SYNCH][OWN] Server Has Different Version: UPLOAD REQUIRED: "+s.getPlayer()+": timeStamps c/s "+s.getClientTimeStamp()+"/"+s.getServerTimeStamp());
				try {
					//					File f = new FileExt(ownModelPath);
					//					f.setLastModified(System.currentTimeMillis());
					state.getPlayer().getSkinManager().uploadFromClient(ownModelPath);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (UploadInProgressException e) {
					e.printStackTrace();
				}
			}
			s.setReceived(true);
			s.getPlayer().getSkinManager().flagUpdateTexture();
		} else {
			s.setReceived(true);
			s.getPlayer().getSkinManager().setFileName(ownModelPath);
			//			System.err.println("[CLIENT][TEXTURE-SYNCH] OWN PLAYER: client file is not newer as the on on the server");
			//			System.err.println("[CLIENT][TEXTURE-SYNCH] OWN PLAYER: USING AS TEXTURE: "+s.getPlayer().getSkinManager().getFileName());
			s.getPlayer().getSkinManager().flagUpdateTexture();
		}
		ownPlayerSatisfied = true;
	}

	private void checkForNewOrChangedPlayers() {
		if (!playersToCheckQueue.isEmpty()) {
			synchronized (playersToCheckQueue) {
				while (!playersToCheckQueue.isEmpty()) {
					PlayerState p = playersToCheckQueue.remove(0);
					playerRequestMap.put(p, new TextureSynchronizationState(p));
					mapChanged = true;
				}
			}
		}
	}

	public void fileChangeBroadcaseted(PlayerState player, long fileChanged, long fileSize) {
		synchronized (receivedFileBroadcasts) {
			receivedFileBroadcasts.add(new FileChangeBroadcast(player, fileChanged, fileSize));
		}
	}

	/**
	 * @return the modelPath
	 */
	public String getModelPath() {
		return ownModelPath;
	}

	/**
	 * @param modelPath the modelPath to set
	 */
	public void setModelPath(String modelPath) {
		System.err.println("[CLIENT] set own model path to " + modelPath);
		this.ownModelPath = modelPath;
	}

	public void handleDownloaded(File file) {
		synchronized (receivedFiles) {
			receivedFiles.add(file);
		}
	}

	private void handleFileRequest(TextureSynchronizationState fileRequest) {
		if (!fileRequest.requested) {
			String req = fileRequest.getPlayer().getName() + PlayerSkin.EXTENSION;
			System.err.println("[CLIENT] REQUESTING FILE FOR DOWNLOAD: " + req);
			fileRequest.requested = true;
			state.getController().getClientChannel().requestFile(req);
		} else {
			if (!receivedFiles.isEmpty()) {
				synchronized (receivedFiles) {
					System.err.println("[TEXTURE SYNCH] RECEIVED FILES: " + receivedFiles);
					PlayerState player = fileRequest.getPlayer();
					for (int i = 0; i < receivedFiles.size(); i++) {

						File file = receivedFiles.get(i);
						if (file.getName().equals(player.getName() + PlayerSkin.EXTENSION)) {

							String clientDB = player.getSkinManager().clientDB;
							File to = new FileExt(clientDB + player.getName() + PlayerSkin.EXTENSION);

							to.delete();
							try {
								to.createNewFile();
								FileUtil.copyFile(file, to);
								file.delete();
								to.setLastModified(fileRequest.getServerTimeStamp());
							} catch (IOException e) {
								e.printStackTrace();
							}

							receivedFiles.remove(i);
							fileRequest.setReceived(true);
							player.getSkinManager().flagUpdateTexture();

							fileRequest = null;
							break;

						}
					}
				}
				this.fileRequest = null;
			}
		}
	}

	public void handleTimeStampResponse(StringLongLongPair rA) {
		synchronized (timeStampResponses) {

			timeStampResponses.add(new StringLongLongPair(rA));
		}

	}

	private void onNoServerTimeStampReceived(TextureSynchronizationState s) {
		//no server timestamp available yet. request and receive timestamp

		if (!s.isTimeStampRequested()) {
			System.err.println("[CLIENT][TEXTURE-SYNCH] requesting tex synch of " + s.getPlayer());

			File f = new FileExt(state.getPlayer().getSkinManager().clientDB + s.getPlayer().getName() + PlayerSkin.EXTENSION);
			if (f.exists()) {
				s.setClientTimeStamp(f.lastModified());
				s.setClientSize(f.length());
			} else {
				//				System.err.println("[CLIENT][TEXTURE-SYNCH] local cache for "+s.getPlayer()+" not found. client timestamp is "+s.getClientTimeStamp());
			}

			state.getController().getClientChannel().requestFileTimestamp(s.getPlayer().getName());
			s.setTimeStampRequested(true);
		} else {
			synchronized (timeStampResponses) {
				for (int i = 0; i < timeStampResponses.size(); i++) {
					StringLongLongPair timestampResponse = timeStampResponses.get(i);
					if (timestampResponse.playerName.equals(s.getPlayer().getName())) {
						s.setServerTimeStamp(timestampResponse.timeStamp);
						s.setServerSize(timestampResponse.size);
						//						System.err.println("[CLIENT][TEXTURE-SYNCH]  received tex synch of "+s.getPlayer()+": SERVER "+(new Date(s.getServerTimeStamp()))+"; CLIENT: "+(new Date(s.getClientTimeStamp()))+" -> "+(s.getServerTimeStamp() > s.getClientTimeStamp() ? "SERVER NEWER" : "CLIENT NEWER"));

					}
				}
			}
		}
	}

	private boolean receiveFileChaneBroadcast(FileChangeBroadcast bc) {
		if (!bc.player.isClientOwnPlayer()) {
			TextureSynchronizationState ts = new TextureSynchronizationState(bc.player);
			//			System.err.println("[CLIENT][TEXTURE-SYNCH] "+state.getPlayer()+" Received Broadcast for "+bc.player);
			playerRequestMap.put(bc.player, ts);

			/*
			 * step of requesting the timestamp can be skipped since the
			 * timestamp is already available from the broadcast
			 */

			File f = new FileExt(ts.getPlayer().getSkinManager().clientDB + ts.getPlayer().getName() + PlayerSkin.EXTENSION);
			if (f.exists()) {
				ts.setClientTimeStamp(f.lastModified());
				ts.setClientSize(f.length());
			}
			ts.setServerTimeStamp(bc.fileChanged);
			ts.setServerSize(bc.fileSize);
			return true;

		} else {
			//nothing to do when receiving a broadcast from ourself
		}
		return false;
	}

	public void synchronize() {

		checkForNewOrChangedPlayers();

		if (fileRequest == null) {
			updateSynchStates();
		} else {
			handleFileRequest(fileRequest);
		}
	}


	private void updateSynchStates() {

		/*
		 * do broadcasts only if there is no file
		 * request in progress
		 */
		if (!receivedFileBroadcasts.isEmpty()) {
			synchronized (receivedFileBroadcasts) {
				while (!receivedFileBroadcasts.isEmpty()) {
					FileChangeBroadcast bc = receivedFileBroadcasts.remove(0);
					mapChanged = receiveFileChaneBroadcast(bc);
				}
			}
		}

		boolean allSynched = true;

		/*
		 * update as long as not all Synchronization steps are done
		 */
		if (mapChanged) {
			for (TextureSynchronizationState s : playerRequestMap.values()) {

				if (!ownSkinClientTimeStampSet && s.getPlayer().isClientOwnPlayer() && !ownModelPath.equals(SkinManager.defaultFile)) {
					File f = new FileExt(ownModelPath);
					s.setClientTimeStamp(f.lastModified());
					s.setClientSize(f.length());
					System.err.println("[CLIENT][TEXTURE-SYNCH] not using default skin. time of last changed skin: " + new Date(s.getClientTimeStamp()));
					ownSkinClientTimeStampSet = true;
				}

				if (!ownPlayerSatisfied && !s.getPlayer().isClientOwnPlayer()) {
					/*
					 * first handle own file requests so we can set the path without
					 * getting blocked by another request
					 */
					continue;
				}

				if (fileRequest != null) {
					break;
				}

				if (!s.isReceived()) {

					if (s.getServerTimeStamp() < 0) {
						onNoServerTimeStampReceived(s);
					} else {
						if (s.getPlayer().isClientOwnPlayer()) {
							availableServerTimeStampOwnPlayer(s);
						} else {
							availableServerTimeStampOtherPlayer(s);
						}
					}

					allSynched = false;
				}
			}
		}
		this.mapChanged = !allSynched;
	}

	private class FileChangeBroadcast {
		PlayerState player;
		long fileChanged;
		long fileSize;

		public FileChangeBroadcast(PlayerState player, long fileChanged, long fileSize) {
			super();
			this.player = player;
			this.fileChanged = fileChanged;
			this.fileSize = fileSize;
		}

	}

	@Override
	public void onAddedSendable(Sendable s) {
		if (s instanceof PlayerState) {
			if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(((PlayerState) s).getId())) {
				synchronized (playersToCheckQueue) {
					System.err.println("[CLIENT][TEXTURE][SYNCHRONIZER] ADDED TO UPDATE QUEUE: " + s + " for " + state.getPlayer());
					playersToCheckQueue.add((PlayerState) s);
				}
			}
		}		
	}

	@Override
	public void onRemovedSendable(Sendable s) {
	}

}
