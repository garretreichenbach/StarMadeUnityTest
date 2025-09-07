package org.schema.game.common.data.player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.schema.common.LogUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.common.data.UploadReceiveState;
import org.schema.game.common.data.UploadState;
import org.schema.game.common.util.FileStreamSegment;
import org.schema.game.network.objects.remote.RemoteControlledFileStream;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class UploadController {

	private final ArrayList<DefferedDownload> deffered = new ArrayList<DefferedDownload>();
	private final Object2IntOpenHashMap<String> defferredTimes = new Object2IntOpenHashMap<String>();
	protected Sendable sendable;
	ArrayList<String> uploadsRequests = new ArrayList<String>();
	private UploadState uploadState;
	private UploadReceiveState uploadReceiveState;
	private long downloaded;

	public UploadController(Sendable sendable) {
		this.sendable = sendable;
	}

	private void checkNewUploadRequests() throws FileUploadTooBigException {
		String remove = uploadsRequests.remove(0);
		try {
			File export = getFileToUpload(remove);
			if (export.exists()) {
				uploadState = new UploadState();
				uploadState.uploadInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(export)));
				uploadState.currentUploadLength = export.length();
				uploadState.pointer = 0;
			} else {
				int deffereds;
				if (!defferredTimes.containsKey(remove)) {
					deffereds = 1;
					defferredTimes.addTo(remove, deffereds);
				} else {
					deffereds = defferredTimes.get(remove) + 1;
					defferredTimes.put(remove, deffereds);
				}
				if (deffereds > 5) {
					System.err.println("[UploadController] File " + export.getName() + " does not yet exist and has been defferred over 5 times. NOT Deferring download for " + sendable);
				} else {
					System.err.println("[UploadController] File " + export.getName() + " does not yet exist. Deferring download by 5 seconds for " + sendable);
					DefferedDownload deffered = new DefferedDownload(remove, System.currentTimeMillis(), 5000);
					this.deffered.add(deffered);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
			if (!(sendable.getState() instanceof ServerStateInterface)) {
				GLFrame.processErrorDialogException(e, sendable.getState());
			}
		} catch (CatalogEntryNotFoundException e) {
			((GameClientState) sendable.getState()).getController().popupAlertTextMessage(Lng.str("Entry not found:\n%s",  remove), 0);
		}
		uploadsRequests.clear();
	}

	private void displayReceiveTitleMessage(short length) {

		String msg = Lng.str("Downloading skin: %s... %s kb",  getNewFileName(),  downloaded / 1000);

		((GameClientState) sendable.getState()).getController().showBigTitleMessage("upload", msg, 0);
	}

	private void displayServerMessage() {

	}

	protected void displayTitleMessage() {
		String msg = Lng.str("Upload finished!");
		if (uploadState.pointer < uploadState.currentUploadLength) {
			msg = Lng.str("uploading: %s kb of %s kb", uploadState.pointer / 1000,  uploadState.currentUploadLength / 1000);
		}
		((GameClientState) sendable.getState()).getController().showBigTitleMessage(Lng.str("download"), msg, 0);

	}

	protected abstract File getFileToUpload(String from) throws IOException, CatalogEntryNotFoundException, FileUploadTooBigException;

	protected abstract String getNewFileName();

	protected abstract RemoteBuffer<RemoteControlledFileStream> getUploadBuffer();

	protected abstract void handleUploadedFile(File file);

	public void handleUploadNT(int senderId) {

		try {
			if (!getUploadBuffer().getReceiveBuffer().isEmpty()) {
				for (RemoteControlledFileStream s : getUploadBuffer().getReceiveBuffer()) {
					if (uploadReceiveState == null) {
						uploadReceiveState = new UploadReceiveState();
						File file = new FileExt(getNewFileName());

						if (sendable.getState() instanceof ServerStateInterface) {
							try {
								PlayerState p = ((GameServerState) sendable.getState()).getPlayerFromStateId(senderId);
								LogUtil.log().fine("[UPLOAD] " + p.getName() + " started to upload: " + getNewFileName());
							} catch (PlayerNotFountException e) {
								e.printStackTrace();
							}
							//
						}

						System.err.println("[UPLOAD] NEW UPLOAD INITIATED! for " + sendable + ": " + getNewFileName());
						if (file.exists()) {
							System.err.println("[UPLOAD] DELETED EXISTING FILE! for " + sendable);
							file.delete();
						}
						downloaded = 0;
						file.createNewFile();
						assert (file.exists());
						FileOutputStream fileOutputStream = new FileOutputStream(file);
						uploadReceiveState.uploadOutputStream = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
					}

					if (uploadReceiveState.ok) {
						//						System.err.println(sendable.getState()+" [SKINUPLOAD] RECEIVED LENGTH "+s.get().length+" -> total: "+downloaded);

						short length = s.get().length;

						downloaded += length;
						if (sendable.getState() instanceof ClientStateInterface) {
							displayReceiveTitleMessage(length);
						}

						uploadReceiveState.uploadOutputStream.write(s.get().buffer, 0, length);
						uploadReceiveState.uploadOutputStream.flush();
						if (s.get().last) {
							uploadReceiveState.uploadOutputStream.close();
							File file = new FileExt(getNewFileName());
							assert (file.exists());
							uploadReceiveState.file = file;

						}
					}

					if (s.get().last) {
						if (uploadReceiveState.uploadOutputStream != null) {
							uploadReceiveState.uploadOutputStream.close();
						}
						uploadReceiveState.finished = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (uploadReceiveState != null) {
				uploadReceiveState.ok = false;
				try {
					uploadReceiveState.uploadOutputStream.close();
				} catch (Exception eaa) {
					eaa.printStackTrace();
				}
			}
		}
	}

	protected void onUploadFinished() {

	}

	public void updateLocal() throws IOException {

		if (!deffered.isEmpty()) {
			for (int i = 0; i < deffered.size(); i++) {
				if (deffered.get(i).timeUp(System.currentTimeMillis())) {
					try {
						upload(deffered.get(i).getDownloadName());
						deffered.remove(i);
						i--;
					} catch (UploadInProgressException e) {
						System.err.println("[UPLOADCONTROLLER] " + sendable.getState() + " " + sendable + " cannot deffer because of ongoing upload. trying next time");
					}
				}
			}
		}

		if (!uploadsRequests.isEmpty()) {
			try {
				checkNewUploadRequests();
			} catch (FileUploadTooBigException e) {
				e.printStackTrace();
				if (sendable.getState() instanceof ClientStateInterface) {
					GLFrame.processErrorDialogException(e, sendable.getState());
				}
			}
		}

		updateUpload();

		if (uploadReceiveState != null && uploadReceiveState.finished) {
			File file = uploadReceiveState.file;

			System.err.println("[" + sendable.getState() + "][" + sendable + "] FINISHED DOWNLOADING: " + file.getAbsolutePath());

			handleUploadedFile(file);

			uploadReceiveState = null;

		}
	}

	private void updateUpload() throws IOException {
		if (uploadState != null && System.currentTimeMillis() - uploadState.lastUploadSegmentTime > UploadState.DELAY && sendable.getState().getUploadBlockSize() > 0) {
			final int blockSize = (int) sendable.getState().getUploadBlockSize();
			FileStreamSegment streamSeg = new FileStreamSegment(blockSize);
			long rest = uploadState.currentUploadLength - uploadState.pointer;
			if (uploadState.uploadInputStream != null) {
				if (rest < blockSize) {
					streamSeg.length = (short) rest;
					uploadState.uploadInputStream.readFully(streamSeg.buffer, 0, (int) rest);
					uploadState.pointer += rest;
				} else {
					streamSeg.length = (short) blockSize;
					uploadState.uploadInputStream.readFully(streamSeg.buffer);
					uploadState.pointer += blockSize;
				}
			}
			rest = uploadState.currentUploadLength - uploadState.pointer;
			if (rest <= 0) {
				streamSeg.last = true;
			}
			RemoteControlledFileStream s = new RemoteControlledFileStream(streamSeg, sendable.getNetworkObject());

			getUploadBuffer().add(s);

			uploadState.lastUploadSegmentTime = System.currentTimeMillis();

			if (sendable.getState() instanceof ClientStateInterface) {
				displayTitleMessage();
			} else {
				displayServerMessage();
			}

			if (rest <= 0) {
				uploadState.uploadInputStream.close();
				uploadState = null;
				onUploadFinished();
			}
		}
	}

	public void upload(String name) throws IOException, UploadInProgressException {

		if (uploadsRequests.size() > 0 || uploadState != null) {
			throw new UploadInProgressException();
		}
		uploadsRequests.add(name);

	}
}
