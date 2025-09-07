package org.schema.schine.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;

import com.bulletphysics.linearmath.Transform;

public class WarpTransformation implements SerializationInterface{

	public Transform t;
	public Vector3f lin;
	public Vector3f ang;
	public LocalSectorTransition local;

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		TransformTools.serializeFully(b, t);
		Vector3fTools.serialize(lin, b);
		Vector3fTools.serialize(ang, b);
		b.writeBoolean(local != null);
		if(local != null){
			local.serialize(b, isOnServer);
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		t = TransformTools.deserializeFully(b, new Transform());
		lin = Vector3fTools.deserialize(b);
		ang = Vector3fTools.deserialize(b);
		boolean loc = b.readBoolean();
		if(loc){
			local = new LocalSectorTransition();
			local.deserialize(b, updateSenderStateId, isOnServer);
		}
	}

}
