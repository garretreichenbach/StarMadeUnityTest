package org.schema.game.common.controller.elements.factorymanager;

import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.schema.common.SerializationInterface;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class FactoryManagerModule implements SerializationInterface, TagSerializable {
	
	private long index;
	private final ObjectArrayPriorityQueue<ProcessingJob> processingQueue = new ObjectArrayPriorityQueue<>();
	
	public FactoryManagerModule(long index) {
		this.index = index;
	}
	
	public FactoryManagerModule(Tag tag) {
		fromTagStructure(tag);
	}
	
	public FactoryManagerModule(DataInput data, int updateSenderStateId, boolean onServer) throws IOException {
		deserialize(data, updateSenderStateId, onServer);
	}
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {

	}

	@Override
	public void fromTagStructure(Tag tag) {

	}

	@Override
	public Tag toTagStructure() {
		return null;
	}
}
