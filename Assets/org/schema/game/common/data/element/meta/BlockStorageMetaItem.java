package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.PlayerBlockStorageMetaDialog;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class BlockStorageMetaItem extends MetaObject {



	public ElementCountMap storage = new ElementCountMap();

	public BlockStorageMetaItem(int id) {
		super(id);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		storage.resetAll();
		storage.deserialize(stream);
	}

	@Override
	public void fromTag(Tag tag) {
		storage.resetAll();
		Tag[] t = (Tag[]) tag.getValue();
		storage.readByteArray((byte[]) t[0].getValue());

	}

	@Override
	public Tag getBytesTag() {

		byte[] progressArray = storage.getByteArray();

		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE_ARRAY, null, progressArray), FinishTag.INST});
	}

	@Override
	public DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		return new PlayerBlockStorageMetaDialog(state, this, openedFrom);
	}
	@Override
	protected GUIHorizontalButton[] getButtons(GameClientState state, Inventory inventory){
		return new GUIHorizontalButton[]{
				getCustomEditButton(state, inventory, Lng.str("GET BLOCKS")),
				getDeleteButton(state, inventory)
		};
	}
	@Override
	public String getName() {
		return Lng.str("Block Storage Item");
	}
	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.BLOCK_STORAGE;
	}
	@Override
	public int getPermission() {
		return MODIFIABLE_CLIENT;
	}

	@Override
	public boolean isValidObject() {
		return true;
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		storage.serialize(stream);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Lng.str("Block Storage Item\n(right click to view)");
	}

	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) &&  storage.equals(((BlockStorageMetaItem)other).storage);
	}

}
