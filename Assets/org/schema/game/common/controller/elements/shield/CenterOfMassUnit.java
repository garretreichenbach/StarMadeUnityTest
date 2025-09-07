package org.schema.game.common.controller.elements.shield;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.element.ElementCollection;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public abstract class CenterOfMassUnit<E extends CenterOfMassUnit<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableControllableSingleElementManager<E, CM, EM>> extends ElementCollection<E, CM, EM>{

	private final Vector3f centerOfMassUnweighted = new Vector3f();
	private final Vector3f centerOfMass = new Vector3f();
	private final Vector3i comOrigin = new Vector3i();
	
	@Override
	protected void updateBB(int x, int y, int z, long index) {
		super.updateBB(x, y, z, index);
		centerOfMassUnweighted.x += x;
		centerOfMassUnweighted.y += y;
		centerOfMassUnweighted.z += z;
		
	}
	
	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		super.calculateExtraDataAfterCreationThreaded(updateSignture, totalCollectionSet);
		centerOfMass.set(centerOfMassUnweighted);
		centerOfMass.scale(1f / size());
		comOrigin.set(Math.round(centerOfMass.x), FastMath.round(centerOfMass.y), FastMath.round(centerOfMass.z));
	}

	@Override
	public void resetAABB() {
		super.resetAABB();
		centerOfMassUnweighted.set(0,0,0);
		centerOfMass.set(0,0,0);
	}
	
	public Vector3i getCoMOrigin(){
		return comOrigin;
	}
}