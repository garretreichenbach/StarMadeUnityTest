package org.schema.game.common.data.player;

import java.io.File;
import java.io.IOException;

import org.schema.common.LogUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.network.objects.remote.RemoteControlledFileStream;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteLongArray;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;

public class SkinUploadController extends UploadController {

	public SkinUploadController(PlayerState playerState) {
		super(playerState);
	}

	@Override
	protected void displayTitleMessage() {
		if (getPlayer().isClientOwnPlayer()) {
			super.displayTitleMessage();
		}
	}

	@Override
	protected File getFileToUpload(String from) throws IOException, CatalogEntryNotFoundException, FileUploadTooBigException {

		String zipFileName = "client_skin_upload_package.zip";

		File f = new FileExt(from);

		if (f.length() > 256 * 1024) {
			throw new FileUploadTooBigException(f);
		}

		File tmp = new FileExt("./tmpUpload/");
		tmp.mkdirs();

		File to = new FileExt("./tmpUpload/" + getPlayer().getName() + PlayerSkin.EXTENSION);

		to.delete();
		to.createNewFile();

		FileUtil.copyFile(f, to);

		FolderZipper.zipFolder("./tmpUpload/", zipFileName, null, null);
		FileUtil.deleteDir(tmp);

		return new FileExt(zipFileName);
	}

	@Override
	protected String getNewFileName() {
		return getPlayer().getName() + "_skin_upload_tmp.zip";
	}

	@Override
	protected RemoteBuffer<RemoteControlledFileStream> getUploadBuffer() {
		return getPlayer().getNetworkObject().skinUploadBuffer;
	}

	@Override
	protected void handleUploadedFile(File file) {
		try {
			if (file != null) {
				assert (file.exists());

				//distribute file to clients who don't have it
				getPlayer().getSkinManager().handleReceivedFileOnServer(file);
				LogUtil.log().fine("[UPLOAD] " + getPlayer() + " finished SKIN upload: " + getNewFileName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			((GameServerState) getPlayer().getState()).getController().broadcastMessage(Lng.astr("[UPLOAD][ERROR] IMPORT FAILED %s",  e.getClass().getSimpleName()), ServerMessage.MESSAGE_TYPE_ERROR);
		}

	}

	@Override
	public void handleUploadNT(int senderId) {
		if (ServerConfig.SKIN_ALLOW_UPLOAD.isOn()) {
			super.handleUploadNT(senderId);
			if (!getPlayer().isOnServer()) {
				for (int i = 0; i < getPlayer().getNetworkObject().textureChangedBroadcastBuffer.getReceiveBuffer().size(); i++) {
					RemoteLongArray fileChanged = getPlayer().getNetworkObject().textureChangedBroadcastBuffer.getReceiveBuffer().get(i);
					GameClientState client = (GameClientState) getPlayer().getState();
					System.err.println("[CLIENT][SKINUPDATE] received texture change broadcast from " + getPlayer() + " on Client Player " + client.getPlayer());
					client.getController().getTextureSynchronizer().fileChangeBroadcaseted(getPlayer(), fileChanged.get(0).get().longValue(), fileChanged.get(1).get().longValue());
				}
			}
		}
	}

	@Override
	protected void onUploadFinished() {
		(new FileExt(getNewFileName())).delete();

		String zipFileName = "client_skin_upload_package.zip";
		File f = new FileExt(zipFileName);

		f.delete();
	}

	public PlayerState getPlayer() {
		return (PlayerState) this.sendable;
	}
}
