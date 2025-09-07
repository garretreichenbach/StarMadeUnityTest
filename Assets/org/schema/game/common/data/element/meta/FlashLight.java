package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class FlashLight extends MetaObject {

	public Vector4f color = new Vector4f(1, 1, 1, 1);
	public boolean active;

	public FlashLight(int id) {
		super(id);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		color.x = stream.readFloat();
		color.y = stream.readFloat();
		color.z = stream.readFloat();
		color.w = stream.readFloat();
		active = stream.readBoolean();
	}

	@Override
	public void fromTag(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();

		color = ((Vector4f) v[0].getValue());
		active = ((Byte) v[1].getValue()) != 0;
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.VECTOR4f, null, color),
				new Tag(Type.BYTE, null, active ? (byte) 1 : (byte) 0),
				FinishTag.INST});
	}

	@Override
	public DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		return new PlayerGameOkCancelInput("BP_editDialog", state, Lng.str("Flash Light"), toDetailedString()) {
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
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.FLASH_LIGHT;
	}
	@Override
	public String getName() {
		return Lng.str("Flashlight");
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
		stream.writeFloat(color.x);
		stream.writeFloat(color.y);
		stream.writeFloat(color.z);
		stream.writeFloat(color.w);
		stream.writeBoolean(active);
	}

	public void drawPossibleOverlay(GUIOverlay reload) {

		if (active) {
			int base = 2;
			float max = 8;
			float percent = 0;

			percent = 0.0f;

			int sprite = base;
			sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));

			reload.setSpriteSubIndex(sprite);
			reload.draw();
		}
	}

	@Override
	public void handleKeyEvent(AbstractCharacter<?> playerCharacter, ControllerStateUnit unit, KeyboardMappings mapping) {

		if (mapping == KeyboardMappings.USE_SLOT_ITEM_CHARACTER) {
			active = !active;
		}
	}

	@Override
	public boolean drawUsingReloadIcon() {
		return true;
	}

	private String toDetailedString() {
		return Lng.str("This is a flash light.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Lng.str("Flashlight\n(right click to view)");
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other);
	}
}
