package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.PlayerTextAreaInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class Logbook extends MetaObject {

	public static final int MAX_LENGTH = 512;
	private String txt = "undefLog";

	public Logbook(int id) {
		super(id);
	}

	public static String getRandomEntry(GameServerState state) {
		return state.logbookEntries[Universe.getRandom().nextInt(state.logbookEntries.length)];
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		txt = stream.readUTF();
	}

	@Override
	public void fromTag(Tag tag) {
		txt = (String) tag.getValue();
	}

	@Override
	public Tag getBytesTag() {
		assert (txt != null);
		return new Tag(Type.STRING, null, txt);
	}

	@Override
	public PlayerInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		PlayerTextAreaInput playerTextAreaInput = new PlayerTextAreaInput("Logbook_EnterText", state, 400, 500, MAX_LENGTH, 100, Lng.str("Edit Logbook"),
				"",
				txt) {
			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback,
			                                 String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
				if (parent != null) {
					parent.suspend(false);
				}
			}

			@Override
			public boolean onInput(String entry) {
				Logbook l = new Logbook(getId());
				l.txt = entry != null ? entry : "";
				try {
					getState().getMetaObjectManager().modifyRequest(getState().getController().getClientChannel().getNetworkObject(), l);
				} catch (MetaItemModifyPermissionException e) {
					getState().getController().popupAlertTextMessage(Lng.str("Operation not permitted:\nAccess denied!"), 0);
				}
				return true;
			}
		};
		playerTextAreaInput.getTextInput().setMinimumLength(0);
		return playerTextAreaInput;
	}

	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.LOG_BOOK;
	}
	@Override
	public int getPermission() {
		return MODIFIABLE_CLIENT;
	}

	@Override
	public boolean isValidObject() {
		return txt != null && txt.length() <= MAX_LENGTH;
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeUTF(txt);
	}
	@Override
	public String getName() {
		return Lng.str("Logbook");
	}
	/**
	 * @return the txt
	 */
	public String getTxt() {
		return txt;
	}

	/**
	 * @param txt the txt to set
	 */
	public void setTxt(String txt) {
		this.txt = txt;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Lng.str("Logbook\n(right click with mouse cursor)");
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) && txt.equals(((Logbook)other).txt);
	}
}
