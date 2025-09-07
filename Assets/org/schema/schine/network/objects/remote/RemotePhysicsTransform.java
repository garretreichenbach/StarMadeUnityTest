package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Quat4f;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.NetworkTransformation;

import com.bulletphysics.linearmath.MatrixUtil;

public class RemotePhysicsTransform extends RemoteField<NetworkTransformation> {

	private static final byte playerAttached = 1;
	private static final byte sendVelos = 2;
	private static final byte linVeloZero = 4;
	private static final byte angVeloZero = 8;
	private static final byte primed = 16;
	public static final byte LINEA_VELO = 1;
	public static final byte ANGULAR_VELO = 2;
	
	private static final float EPSILON = 0.000001f;
	private Quat4f qReceive = new Quat4f();
	private Quat4f qSend = new Quat4f();

	public RemotePhysicsTransform(NetworkTransformation modification, boolean synchOn) {
		super(modification, synchOn);
	}

	public RemotePhysicsTransform(NetworkTransformation modification, NetworkObject synchOn) {
		super(modification, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		int mask = stream.readByte() & 0xFF;

		qReceive.set(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat());

//		get().getTransformReceive().basis.m00 = stream.readFloat();
//		get().getTransformReceive().basis.m01 = stream.readFloat();
//		get().getTransformReceive().basis.m02 = stream.readFloat();
//		get().getTransformReceive().basis.m10 = stream.readFloat();
//		get().getTransformReceive().basis.m11 = stream.readFloat();
//		get().getTransformReceive().basis.m12 = stream.readFloat();
//		get().getTransformReceive().basis.m20 = stream.readFloat();
//		get().getTransformReceive().basis.m21 = stream.readFloat();
//		get().getTransformReceive().basis.m22 = stream.readFloat();
		
		
//		get().getTransformReceive().basis.set(qReceive);

		MatrixUtil.setRotation(get().getTransformReceive().basis, qReceive);
		
		get().getTransformReceive().origin.set(stream.readFloat(), stream.readFloat(), stream.readFloat());

		get().setPlayerAttachedReceive((mask & playerAttached) == playerAttached);

		if ((mask & sendVelos) == sendVelos) {
			get().receivedVil = true;
			if ((mask & linVeloZero) == linVeloZero) {
				get().getLinReceive().set(0, 0, 0);
			} else {
				get().getLinReceive().set(stream.readFloat(), stream.readFloat(), stream.readFloat());
			}
			if ((mask & angVeloZero) == angVeloZero) {
				get().getAngReceive().set(0, 0, 0);
			} else {
				get().getAngReceive().set(stream.readFloat(), stream.readFloat(), stream.readFloat());
			}
		}
			get().setTimeStampReceive(stream.readLong());
		get().received = (mask & primed) == primed;

		//		System.err.println("SERVER READING REMOTE TRANSFORM: "+get().getTransform().origin);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		//	 right
		//		v.x = m00;
		//      v.y = m10;
		//      v.z = m20;
		//   up
		//      v.x = m01;
		//      v.y = m11;
		//      v.z = m21;
		//   forward
		//      v.x = m02;
		//      v.y = m12;
		//      v.z = m22;

		int mask = 0;

		if (get().isPlayerAttached()) {
			mask |= playerAttached;
		}

		if (get().sendVil) {
			mask |= sendVelos;
		}
		if (get().getLin().lengthSquared()  <= EPSILON ) {
			mask |= linVeloZero;
		}
		if (get().getAng().lengthSquared() <= EPSILON ) {
			mask |= angVeloZero;
		}
		if (get().prime) {
			mask |= primed;
			get().prime = false;
		}
		buffer.writeByte((byte) mask);

		Quat4fTools.set(get().getTransform().basis, qSend);

		buffer.writeFloat(qSend.x);
		buffer.writeFloat(qSend.y);
		buffer.writeFloat(qSend.z);
		buffer.writeFloat(qSend.w);
		
		
//		buffer.writeFloat(get().getTransform().basis.m00);
//		buffer.writeFloat(get().getTransform().basis.m01);
//		buffer.writeFloat(get().getTransform().basis.m02);
//		buffer.writeFloat(get().getTransform().basis.m10);
//		buffer.writeFloat(get().getTransform().basis.m11);
//		buffer.writeFloat(get().getTransform().basis.m12);
//		buffer.writeFloat(get().getTransform().basis.m20);
//		buffer.writeFloat(get().getTransform().basis.m21);
//		buffer.writeFloat(get().getTransform().basis.m22);

		buffer.writeFloat(get().getTransform().origin.x);
		buffer.writeFloat(get().getTransform().origin.y);
		buffer.writeFloat(get().getTransform().origin.z);

		if (get().sendVil) {
			if ((mask & linVeloZero) == linVeloZero) {
//				nothing to send. information in bit
			} else {
				buffer.writeFloat(get().getLin().x);
				buffer.writeFloat(get().getLin().y);
				buffer.writeFloat(get().getLin().z);
			}
			if ((mask & angVeloZero) == angVeloZero) {
//				nothing to send. information in bit
			} else {
				buffer.writeFloat(get().getAng().x);
				buffer.writeFloat(get().getAng().y);
				buffer.writeFloat(get().getAng().z);
			}
		}

			buffer.writeLong(get().getTimeStamp());

		return byteLength();
	}
}
