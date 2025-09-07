package org.schema.schine.network.objects.remote;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;

import com.bulletphysics.linearmath.Transform;

public class LongTransformPair implements SerializationInterface{
	public long l;
	public Transform t;
	public LongTransformPair() {
		
	}
	public LongTransformPair(long l, Transform t) {
		this.l = l;
		this.t = t;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(l);
		TransformTools.serializeFully(b, t);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		l = b.readLong();
		t = TransformTools.deserializeFully(b, new Transform());
	}
}
