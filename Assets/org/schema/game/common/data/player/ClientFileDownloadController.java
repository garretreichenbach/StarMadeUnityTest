package org.schema.game.common.data.player;

import java.io.File;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.network.objects.remote.RemoteControlledFileStream;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.resource.FileExt;

public class ClientFileDownloadController extends UploadController {

	private boolean needsUpdate;

	private String currentDownloadPlayerName;

	public ClientFileDownloadController(ClientChannel channel) {
		super(channel);
	}

	public ClientChannel getChannel() {
		return (ClientChannel) sendable;
	}

	/**
	 * @return the currentDownloadName
	 */
	public String getCurrentDownloadName() {
		return currentDownloadPlayerName;
	}

	/**
	 * @param currentDownloadName the currentDownloadName to set
	 */
	public void setCurrentDownloadName(String currentDownloadName) {
		this.currentDownloadPlayerName = currentDownloadName;
	}

	@Override
	protected File getFileToUpload(String from) throws IOException,
			CatalogEntryNotFoundException {
		return new FileExt(from);
	}

	@Override
	protected String getNewFileName() {
		return currentDownloadPlayerName;
	}

	@Override
	protected RemoteBuffer<RemoteControlledFileStream> getUploadBuffer() {
		return getChannel().getNetworkObject().downloadBuffer;
	}

	@Override
	protected void handleUploadedFile(File file) {
		//client handle file

		System.err.println("[CLIENT] successfully received file from server: " + file.getAbsolutePath());
		((GameClientState) getChannel().getState()).getController().getTextureSynchronizer().handleDownloaded(file);
		this.needsUpdate = false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.UploadController#upload(java.lang.String)
	 */
	@Override
	public void upload(String catalogName) throws IOException,
			UploadInProgressException {
		super.upload(catalogName);

		this.needsUpdate = true;
	}

	/**
	 * @return the needsUpdate
	 */
	public boolean isNeedsUpdate() {
		return needsUpdate;
	}

	/**
	 * @param needsUpdate the needsUpdate to set
	 */
	public void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}

}
