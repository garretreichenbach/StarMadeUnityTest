package org.schema.game.common.controller.elements.jumpprohibiter;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.structurepersistence.PersistentStructureDataContainer;
import org.schema.game.server.data.structurepersistence.PersistentStructureDataManager;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class JumpInhibitorPersistentDataContainer implements PersistentStructureDataContainer {

	private static final byte VERSION = 0;
	private final long id;
	private boolean active;
	private float strength;
	private float range;
	private int factionId;

	public JumpInhibitorPersistentDataContainer(JumpInhibitorCollectionManager collectionManager) {
		SegmentController segmentController = collectionManager.getSegmentController();
		id = PersistentStructureDataManager.calculateId(segmentController.getSectorId(), segmentController.dbId, collectionManager.getControllerIndex());
		active = collectionManager.isActive();
		strength = collectionManager.getChargeRemovedPerSec(); //Todo: Is this a good metric to use for strength?
		range = collectionManager.getRange();
	}

	public JumpInhibitorPersistentDataContainer(long id) {
		this.id = id;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public float getStrength() {
		return strength;
	}
	
	public void setStrength(float strength) {
		this.strength = strength;
	}
	
	public float getRange() {
		return range;
	}
	
	public void setRange(float range) {
		this.range = range;
	}
	
	public int getFactionId() {
		return factionId;
	}
	
	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] subTags = tag.getStruct();
		byte version = subTags[0].getByte();
		active = subTags[1].getBoolean();
		strength = subTags[2].getFloat();
		range = subTags[3].getFloat();
		factionId = subTags[4].getInt();
	}

	@Override
	public Tag toTagStructure() {
		Tag[] subTags = new Tag[6];
		subTags[0] = new Tag(Tag.Type.BYTE, "version", VERSION);
		subTags[1] = new Tag(Tag.Type.BYTE, "active", active);
		subTags[2] = new Tag(Tag.Type.FLOAT, "strength", strength);
		subTags[3] = new Tag(Tag.Type.FLOAT, "range", range);
		subTags[4] = new Tag(Tag.Type.INT, "faction_id", factionId);
		subTags[5] = FinishTag.INST;
		return new Tag(Tag.Type.STRUCT, "jump_inhibitor_persistent_data_container", subTags);
	}
}
