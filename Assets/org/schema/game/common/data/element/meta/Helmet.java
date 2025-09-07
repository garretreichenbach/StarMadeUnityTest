package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class Helmet extends MetaObject {

	public static final byte STANDARD = 0;

	private byte type = STANDARD;


	public Helmet(int id) {
		super(id);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		type = stream.readByte();
	}

	@Override
	public void fromTag(Tag tag) {
		type = ((Byte) tag.getValue());
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.BYTE, null, type);
	}

	@Override
	public DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		return new PlayerGameOkCancelInput("Helmet_editDialog", state, Lng.str("Helmet"), toDetailedString()) {
			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {

			}

			@Override
			public void pressedOK() {
				deactivate();
			}
		};
	}
	@Override
	public String getName() {
		return Lng.str("Helmet");
	}
	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.HELMET;
	}
	@Override
	public int getPermission() {
		return NO_EDIT_PERMISSION;
	}

	@Override
	public boolean isValidObject() {
		return true;
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeByte(type);
	}

	@Override
	public void handleKeyEvent(AbstractCharacter<?> playerCharacter, ControllerStateUnit unit, KeyboardMappings mapping) {
		if (mapping == KeyboardMappings.SHIP_PRIMARY_FIRE) {
			unit.playerState.sendSimpleCommand(SimplePlayerCommands.PUT_ON_HELMET);
		}
	}

	private String toDetailedString() {
		return Lng.str("A fashionable helmet\nto protect against the\ndangers in space.\n\nTo put helmet on/off, place it in the action bar \nand activate it with LMB (with closed inventory)");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Lng.str("Helmet\n(right click to view)");
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other);
	}
}
