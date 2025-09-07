package org.schema.game.common.data.player.tech;

import org.schema.game.client.data.GameStateInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class TechnologyTree implements TagSerializable {

	private final TechnologyOwner owner;

	public TechnologyTree(TechnologyOwner owner) {
		this.owner = owner;
	}


	@Override
	public void fromTagStructure(Tag tag) {
		
	}

	@Override
	public Tag toTagStructure() {
				return null;
	}

	public StateInterface getState() {
		return owner.getState();
	}

	/**
	 * @return the owner
	 */
	public TechnologyOwner getOwner() {
		return owner;
	}

	public Short2ObjectOpenHashMap<Technology> getAllTechs() {
		return ((GameStateInterface) getState()).getAllTechs();
	}
}
