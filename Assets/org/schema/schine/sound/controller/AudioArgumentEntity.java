package org.schema.schine.sound.controller;


import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.sound.controller.gui.AudioEventDetailPanel;
import org.schema.schine.sound.manager.engine.Filter;

import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AudioArgumentEntity implements AudioArgument{
	public Transformable transformable;
	public Transform position = new Transform();
	public TransformTimed worldPosition = new TransformTimed();
	public BoundingBox range = new BoundingBox();
	public int primaryId;
	public long secondaryId;
	public long absIndex;
	public short blockID;

	@Override
	public void fillLabels(AudioEventDetailPanel a) {
		
		a.lblTransval.setText(transformable.toString());
		a.lblPositionval.setText(position.origin.toString());
		a.lblRangeval.setText(range.toString());
		
	}
	private void updateTransform() {
		worldPosition.mul(transformable.getWorldTransform(), position);
	}
	@Override
	public boolean isPositional() {
		return true;
	}
	
	@Override
	public float getRefDistance() {
		return range.getSize()/2f;
	}
	@Override
	public float getMaxDistance() {
		return range.getSize();
	}
	@Override
	public boolean isReverbEnabled() {
		return false;
	}
	@Override
	public Filter getReverbFilter() {
		return null;
	}
	@Override
	public Vector3f getPos() {
		return worldPosition.origin;
	}
	@Override
	public TransformTimed getWorldTransform() {
		return worldPosition;
		
	}
	@Override
	public int getPrimaryId() {
		return primaryId;
	}
	@Override
	public long getSubId() {
		return secondaryId;
	}
	@Override
	public void update(Timer timer) {
		updateTransform();
	}
	@Override
	public void init() {
		updateTransform();		
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(getType().getTypeByte());
		b.writeInt(primaryId);
		b.writeLong(secondaryId);
		b.writeLong(absIndex);
		b.writeShort(blockID);
		range.serialize(b);
		TransformTools.serializeFully(b, position);
		
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type byte already read
		primaryId = b.readInt();
		secondaryId = b.readLong();
		absIndex = b.readLong();
		blockID = b.readShort();
		range.deserialize(b);
		TransformTools.deserializeFully(b, position);
	}
	@Override
	public AudioArgumentType getType() {
		return AudioArgumentTypes.ENTITY;
	}
}
