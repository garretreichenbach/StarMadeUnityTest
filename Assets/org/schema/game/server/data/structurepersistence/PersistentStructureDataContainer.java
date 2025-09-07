package org.schema.game.server.data.structurepersistence;

import org.schema.schine.resource.tag.TagSerializable;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public interface PersistentStructureDataContainer extends TagSerializable {

	/**
	 * Gets the unique ID of the structure data.
	 * <p>The unique id of the structure data is represented by a long which is a combination of
	 * the sector id, the entity id, and the absolute index of the block that "owns" the data.</p>
	 * <p>By combining them into a single long, we can index them using a unique ID system that also
	 * allows us to fetch specific data by sector id, entity id, or block index without having to comb
	 * through each container individually and check it's fields.</p>
	 *
	 * @return The unique ID of the structure data
	 */
	long getId();
}
