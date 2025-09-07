package org.schema.game.client.data.gamemap;

import java.util.ArrayList;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

public class UniverseMap implements TagSerializable {

	private final ArrayList<Vector3i> systems = new ArrayList<Vector3i>();

	@Override
	public void fromTagStructure(Tag tag) {

	}

	@Override
	public Tag toTagStructure() {
		return null;
	}

	/**
	 * @return the systems
	 */
	public ArrayList<Vector3i> getSystems() {
		return systems;
	}


}
