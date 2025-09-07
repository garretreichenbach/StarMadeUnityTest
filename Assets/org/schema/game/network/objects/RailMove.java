package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Quat4f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;

public class RailMove implements SerializationInterface {
	public short toGoX;
	public short toGoY;
	public short toGoZ;
	public float lastRailTargetX;
	public float lastRailTargetY;
	public float lastRailTargetZ;
	public float lastRailTargetW;
	public byte rotationCode;
	public byte rotationSide;
	public float speedPercent;
	public boolean hasTranslation = true;
	public boolean hasLastRailRotation = true;
	public boolean isTurretBackToDefault;

	private byte TRANSLATION_BYTE = 1;
	private byte HAS_LAST_RAILROTATION_BYTE = 2;
	private byte IS_BACK_TO_DEFAULT_TURRET = 4;
	private byte SHOOTOUT_BYTE = 8;
	public boolean shootOut;

	public RailMove() {
	}

	public RailMove(boolean backToDefaultTurret) {
		isTurretBackToDefault = backToDefaultTurret;
	}

	public RailMove(byte rotationCode, byte rotationSide, Quat4f lastRailRotTarget, float speedPercent) {
		hasTranslation = false;
		if (lastRailRotTarget != null) {
			lastRailTargetX = lastRailRotTarget.x;
			lastRailTargetY = lastRailRotTarget.y;
			lastRailTargetZ = lastRailRotTarget.z;
			lastRailTargetW = lastRailRotTarget.w;
		} else {
			hasLastRailRotation = false;
		}
		this.rotationCode = rotationCode;
		this.rotationSide = rotationSide;
		this.speedPercent = speedPercent;
	}

	public RailMove(Vector3i toGo, byte rotationCode, byte rotationSide, Quat4f lastRailRotTarget, float speedPercent) {
		if (toGo != null) {
			this.toGoX = (short) toGo.x;
			this.toGoY = (short) toGo.y;
			this.toGoZ = (short) toGo.z;
		} else {
			hasTranslation = false;
		}
		if (lastRailRotTarget != null) {
			lastRailTargetX = lastRailRotTarget.x;
			lastRailTargetY = lastRailRotTarget.y;
			lastRailTargetZ = lastRailRotTarget.z;
			lastRailTargetW = lastRailRotTarget.w;
		} else {
			hasLastRailRotation = false;
		}
		this.rotationCode = rotationCode;
		this.rotationSide = rotationSide;
		this.speedPercent = speedPercent;

	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		byte code = 0;
		if (hasTranslation) {
			code |= TRANSLATION_BYTE;
		}
		if (hasLastRailRotation) {
			code |= HAS_LAST_RAILROTATION_BYTE;
		}
		if (isTurretBackToDefault) {
			code |= IS_BACK_TO_DEFAULT_TURRET;
		}
		if (shootOut) {
			code |= SHOOTOUT_BYTE;
		}

		b.writeByte(code);

		if (!isTurretBackToDefault) {
			if (hasTranslation) {
				b.writeShort(toGoX);
				b.writeShort(toGoY);
				b.writeShort(toGoZ);
			}
			if (hasLastRailRotation) {
				b.writeFloat(lastRailTargetX);
				b.writeFloat(lastRailTargetY);
				b.writeFloat(lastRailTargetZ);
				b.writeFloat(lastRailTargetW);
			}
			b.writeByte(rotationCode);
			b.writeByte(rotationSide);
			b.writeFloat(speedPercent);
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {

		byte code = b.readByte();

		isTurretBackToDefault = (code & IS_BACK_TO_DEFAULT_TURRET) == IS_BACK_TO_DEFAULT_TURRET;
		shootOut = (code & SHOOTOUT_BYTE) == SHOOTOUT_BYTE;

		if (!isTurretBackToDefault) {

			hasTranslation = (code & TRANSLATION_BYTE) == TRANSLATION_BYTE;

			if (hasTranslation) {
				toGoX = b.readShort();
				toGoY = b.readShort();
				toGoZ = b.readShort();
			}

			hasLastRailRotation = (code & HAS_LAST_RAILROTATION_BYTE) == HAS_LAST_RAILROTATION_BYTE;
			if (hasLastRailRotation) {
				lastRailTargetX = b.readFloat();
				lastRailTargetY = b.readFloat();
				lastRailTargetZ = b.readFloat();
				lastRailTargetW = b.readFloat();
			}
			rotationCode = b.readByte();
			rotationSide = b.readByte();
			speedPercent = b.readFloat();
		}
	}

}
