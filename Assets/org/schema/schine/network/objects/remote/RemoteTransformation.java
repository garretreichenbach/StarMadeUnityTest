package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Quat4f;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.schine.network.objects.NetworkObject;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

public class RemoteTransformation extends RemoteField<Transform> {

	private Quat4f tmp = new Quat4f(); 
	public RemoteTransformation(Transform entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteTransformation(Transform entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_FLOAT*7;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().origin.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
		
		tmp.set(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat());
		
		MatrixUtil.setRotation(get().basis, tmp);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeFloat(get().origin.x);
		buffer.writeFloat(get().origin.y);
		buffer.writeFloat(get().origin.z);
		Quat4fTools.set(get().basis, tmp);
		buffer.writeFloat(tmp.x);
		buffer.writeFloat(tmp.y);
		buffer.writeFloat(tmp.z);
		buffer.writeFloat(tmp.w);
		
		return 1;
	}

}
