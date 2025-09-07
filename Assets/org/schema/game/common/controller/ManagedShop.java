package org.schema.game.common.controller;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShopManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.network.objects.NetworkManagedShop;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;

public class ManagedShop extends ShopSpaceStation implements ManagedSegmentController {
	private final ShopManagerContainer managerContainer;


	public ManagedShop(StateInterface state) {
		super(state);
		managerContainer = new ShopManagerContainer(state, this);
	}

	public boolean isAdvancedShop() {
		return realName.contains("Trade Station");
	}

	@Override
	public ManagerContainer<?> getManagerContainer() {
		return managerContainer;
	}

	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		getManagerContainer().initFromNetworkObject(getNetworkObject());
	}

	@Override
	public double getCapacityFor(Inventory inventory) {
		return shopInventory.getVolume() + 100000d;
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		getManagerContainer().updateFromNetworkObject(o, senderId);
	}

	@Override
	public SendableType getSendableType() {
		return SendableTypes.MANAGED_SHOP;
	}

	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		getManagerContainer().updateToFullNetworkObject(getNetworkObject());
	}

	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		getManagerContainer().updateToNetworkObject(getNetworkObject());
	}

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkManagedShop(getState(), this));
	}

	@Override
	public NetworkManagedShop getNetworkObject() {
		return (NetworkManagedShop) super.getNetworkObject();
	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);

		getManagerContainer().updateLocal(timer);
	}
}
