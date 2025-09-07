package org.schema.game.common.data.player;

import java.io.File;
import java.io.IOException;

import org.schema.common.LogUtil;
import org.schema.game.network.objects.remote.RemoteControlledFileStream;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.game.server.data.CatalogState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.server.ServerMessage;

public class ShipUploadController extends UploadController {

	private long lastNoticeSent;

	public ShipUploadController(PlayerState playerState) {
		super(playerState);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.UploadController#displayTitleMessage()
	 */
	@Override
	protected void displayTitleMessage() {
		if (getPlayer().isClientOwnPlayer()) {
			super.displayTitleMessage();
		}
	}

	@Override
	protected File getFileToUpload(String from) throws IOException, CatalogEntryNotFoundException {
		return BluePrintController.active.export(from);
	}

	@Override
	protected String getNewFileName() {
		return getPlayer().getName() + "_upload_tmp.zip";
	}

	@Override
	protected RemoteBuffer<RemoteControlledFileStream> getUploadBuffer() {
		return getPlayer().getNetworkObject().shipUploadBuffer;
	}

	@Override
	protected void handleUploadedFile(File file) {
		try {
			if (file != null) {
				assert (file.exists());

				LogUtil.log().fine("[UPLOAD] " + getPlayer() + " finished BLUEPRINT upload: " + file.getName() + ": ");

				if (ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt() < 0 ||
						getPlayer().getCatalog().getPersonalCatalog().size() < ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt()) {
					((CatalogState) getPlayer().getState()).getCatalogManager().importEntry(file, getPlayer().getName());
				} else {
					getPlayer().sendServerMessage(new ServerMessage(Lng.astr("Cannot save blueprint:\nout of slots: %s/%s!",  getPlayer().getCatalog().getPersonalCatalog().size(),  ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt()), ServerMessage.MESSAGE_TYPE_ERROR, getPlayer().getId()));
				}

				if (file.exists()) {
					file.delete();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			((GameServerState) getPlayer().getState()).getController().broadcastMessage(Lng.astr("[UPLOAD][ERROR] IMPORT FAILED %s",  e.getClass().getSimpleName()), ServerMessage.MESSAGE_TYPE_ERROR);
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.UploadController#handleUploadNT()
	 */
	@Override
	public void handleUploadNT(int senderId) {

		if (getPlayer().isOnServer() && !ServerConfig.ALLOW_UPLOAD_FROM_LOCAL_BLUEPRINTS.isOn()) {
			if (!getUploadBuffer().getReceiveBuffer().isEmpty()) {
				if (System.currentTimeMillis() - lastNoticeSent > 60000) {
					getPlayer().sendServerMessage(new ServerMessage(Lng.astr("This server doesn't allow\nuploading blueprints!\n\nUploading data is discarded."), ServerMessage.MESSAGE_TYPE_ERROR, getPlayer().getId()));
					this.lastNoticeSent = System.currentTimeMillis();
				}
			}

			return;
		}
		super.handleUploadNT(senderId);

	}

	public PlayerState getPlayer() {
		return (PlayerState) this.sendable;
	}
}
