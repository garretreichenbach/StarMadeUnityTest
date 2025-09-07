package org.schema.game.common.data.player.inventory;

import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.network.objects.remote.RemoteInventory;
import org.schema.schine.resource.tag.Tag;

public class ShopInventory extends StashInventory {

	public ShopInventory(InventoryHolder state, long parameter) {
		super(state, parameter);
	}

	public static int getInventoryType() {
		return SHOP_INVENTORY;
	}

	@Override
	public int getActiveSlotsMax() {
		return 0;
	}

	@Override
	public void sendAll() {
		getInventoryHolder().getInventoryNetworkObject().getInventoriesChangeBuffer().add(
				new RemoteInventory(this, getInventoryHolder(), true, getInventoryHolder().getInventoryNetworkObject().isOnServer()));
	}
	
	@Override
	public int getLocalInventoryType() {
		return SHOP_INVENTORY;
	}

	@Override
	public SegmentPiece getBlockIfPossible() {
		if(cachedInvBlock != null){
			cachedInvBlock.refresh();
			return cachedInvBlock;
		}
		if(getInventoryHolder() instanceof StationaryManagerContainer<?>){
			cachedInvBlock = ((StationaryManagerContainer<?>)getInventoryHolder()).getSegmentController().getSegmentBuffer().getPointUnsave(getParameterIndex());
			return cachedInvBlock;
		}
		return null;
	}
	@Override
	public long getParameterIndex() {
		if(getInventoryHolder() instanceof StationaryManagerContainer<?>){
			return ((StationaryManagerContainer<?>)getInventoryHolder()).shopBlockIndex;
		}else{
			throw new NullPointerException(getInventoryHolder()+" can only request from stations");
		}
	}
	@Override
	public String getCustomName() {
		return "";
	}

	public int getMaxStock() {
		return 50000;
	}

	@Override
	public void fromMetaData(Tag tag) {
		super.fromMetaData(tag);
	}
	@Override
	public Tag toTagStructure() {
		return super.toTagStructure();
	}
}
