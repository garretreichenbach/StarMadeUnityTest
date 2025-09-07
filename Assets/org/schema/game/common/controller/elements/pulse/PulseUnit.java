package org.schema.game.common.controller.elements.pulse;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class PulseUnit<
		E extends PulseUnit<E, CM, EM>,
		CM extends PulseCollectionManager<E, CM, EM>,
		EM extends PulseElementManager<E, CM, EM>> extends CustomOutputUnit<E, CM, EM> {

	@Override
	public int getEffectBonus() {
		//add percentage of total blocks of tertiary amount to distribute bonus on units. also cap bonus at size
		return Math.min(size(), (int) (((double) size() / (double) elementCollectionManager.getTotalSize()) * elementCollectionManager.getEffectTotal()));
	}

	public abstract float getRadius();

	//	/* (non-Javadoc)
	//	 * @see org.schema.game.common.data.element.ElementCollection#significatorUpdate(int, int, int, int, int, int, int, int, int, long)
	//	 */
	//	@Override
	//	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin,
	//			int zMin, int xMax, int yMax, int zMax, long index) {
	//		significatorUpdateZ(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
	//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PulseUnit [significator=" + significator + "]";
	}
	@Override
	protected DamageDealerType getDamageType() {
		return DamageDealerType.PULSE;
	}
	public abstract float getPulsePower();

	public abstract float getDamageWithoutEffect();

	public abstract float getBasePulsePower();

	@Override
	public float getDistanceRaw() {
		return getRadius();
	}

	@Override
	public float getExtraConsume() {
		return 1;
	}
	@Override
	public void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer) {
		boolean focus = false;
		boolean lead = false;
		unit.getShootingDir(
				getSegmentController(),
				shootContainer,
				getDistanceFull(),
				1,
				elementCollectionManager.getControllerPos(),
				focus,
				lead);
		shootContainer.shootingDirTemp.normalize();
		EM em = elementCollectionManager.getElementManager();
		em.doShot((E) this, elementCollectionManager, shootContainer, unit.getPlayerState(), timer);		
	}
}