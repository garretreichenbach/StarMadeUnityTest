package org.schema.game.common.data.world;

import javax.vecmath.Vector3f;

public interface StealthReconEntity {
	public boolean canSeeStructure(StealthReconEntity target, boolean checkAllySharing);
	public boolean canSeeIndicator(StealthReconEntity target, boolean checkAllySharing);
	public float getReconStrengthRaw();
	public float getReconStrength(float distance);
	public float getStealthStrength();

	public Vector3f getPosition(); //visibility/scan stat derivation necessitates a relative position
}
