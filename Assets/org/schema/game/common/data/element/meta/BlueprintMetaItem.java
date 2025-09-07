package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.PlayerBlueprintMetaDialog;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class BlueprintMetaItem extends MetaObject {

	public static final int MAX_LENGTH = 512;

	public String blueprintName = "ERROR_undef";

	public ElementCountMap goal = new ElementCountMap();
	public ElementCountMap progress = new ElementCountMap();

	public BlueprintMetaItem(int id) {
		super(id);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		blueprintName = stream.readUTF();
		goal.resetAll();
		progress.resetAll();
		goal.deserialize(stream);
		progress.deserialize(stream);
	}

	@Override
	public void fromTag(Tag tag) {
		goal.resetAll();
		progress.resetAll();
		Tag[] t = (Tag[]) tag.getValue();
		goal.readByteArray((byte[]) t[0].getValue());
		progress.readByteArray((byte[]) t[1].getValue());
		blueprintName = (String) t[2].getValue();

	}

	@Override
	public Tag getBytesTag() {

		byte[] goalArray = goal.getByteArray();
		byte[] progressArray = progress.getByteArray();

		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE_ARRAY, null, goalArray), new Tag(Type.BYTE_ARRAY, null, progressArray), new Tag(Type.STRING, null, blueprintName), FinishTag.INST});
	}

	@Override
	public DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		return new PlayerBlueprintMetaDialog(state, this, openedFrom);
	}

	@Override
	public String getName() {
		return Lng.str("Blueprint");
	}
	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.BLUEPRINT;
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
		stream.writeUTF(blueprintName);
		goal.serialize(stream);
		progress.serialize(stream);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Lng.str("Blueprint (%s)\n(right click to view)", blueprintName);
	}

	public boolean metGoal() {

		return progress.equals(goal);
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) &&  blueprintName.equals(((BlueprintMetaItem)other).blueprintName) &&  goal.equals(((BlueprintMetaItem)other).goal) &&  progress.equals(((BlueprintMetaItem)other).progress);
	}
}
