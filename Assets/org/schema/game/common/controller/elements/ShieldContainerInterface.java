package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.shield.capacity.ShieldCapacityCollectionManager;
import org.schema.game.common.controller.elements.shield.regen.ShieldRegenCollectionManager;
import org.schema.schine.network.StateInterface;

public interface ShieldContainerInterface extends PowerManagerInterface {
	public ShieldRegenCollectionManager getShieldRegenManager();

	public ShieldCapacityCollectionManager getShieldCapacityManager();

//	public double handleShieldHit(Damager damager, Vector3f hitPoint, DamageDealerType damageType, float damage, long weaponId);

	public ShieldAddOn getShieldAddOn();

	public SegmentController getSegmentController();

	public StateInterface getState();

	public boolean isUsingPowerReactors();
	
	@Override
	public void addUpdatable(ManagerUpdatableInterface m);

	//	public void onHit(int damage);

}
