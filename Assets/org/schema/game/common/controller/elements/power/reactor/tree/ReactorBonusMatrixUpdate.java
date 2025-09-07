package org.schema.game.common.controller.elements.power.reactor.tree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Matrix3f;

import org.schema.common.SerializationInterface;

public class ReactorBonusMatrixUpdate implements SerializationInterface{
	public long id;
	public Matrix3f bonusMatrix;
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(id);
		b.writeFloat(bonusMatrix.m00);
		b.writeFloat(bonusMatrix.m01);
		b.writeFloat(bonusMatrix.m02);
		b.writeFloat(bonusMatrix.m10);
		b.writeFloat(bonusMatrix.m11);
		b.writeFloat(bonusMatrix.m12);
		b.writeFloat(bonusMatrix.m20);
		b.writeFloat(bonusMatrix.m21);
		b.writeFloat(bonusMatrix.m22);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		id = b.readLong();
		bonusMatrix = new Matrix3f();
		bonusMatrix.m00 = b.readFloat();
		bonusMatrix.m01 = b.readFloat();
		bonusMatrix.m02 = b.readFloat();
		bonusMatrix.m10 = b.readFloat();
		bonusMatrix.m11 = b.readFloat();
		bonusMatrix.m12 = b.readFloat();
		bonusMatrix.m20 = b.readFloat();
		bonusMatrix.m21 = b.readFloat();
		bonusMatrix.m22 = b.readFloat();
	}
}
