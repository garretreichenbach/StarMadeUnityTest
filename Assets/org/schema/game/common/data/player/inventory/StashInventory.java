package org.schema.game.common.data.player.inventory;

import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.cargo.CargoElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.network.objects.LongBooleanPair;
import org.schema.game.network.objects.LongStringPair;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.remote.RemoteLongBoolean;
import org.schema.game.network.objects.remote.RemoteLongString;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class StashInventory extends Inventory {

	private final InventoryFilter filter;
	private short production;
	private int productionLimit;
	private String customName = "";
	private String password = "";
	private boolean unlocked; //If the inventory is unlocked, it can be used without a password
	private boolean autoLock = true;
	private long unlockTimer = -1; //The inventory will stay unlocked for up to 60 seconds

	public StashInventory(InventoryHolder state, long parameter) {
		super(state, parameter);
		filter = new InventoryFilter();
		filter.inventoryId = parameter;
	}

	public static int getInventoryType() {
		return STASH_INVENTORY;
	}


	public void updateLockBox() {
		if(unlockTimer > -1 && autoLock) {
			if(System.currentTimeMillis() - unlockTimer > CargoElementManager.LOCK_BOX_UNLOCK_TIME) {
				unlockTimer = -1;
				unlocked = false;
				lock();
			}
		}
	}

	public boolean isAutoLock() {
		return autoLock;
	}

	public boolean setAutoLock(boolean autoLock) {
		if(unlocked) {
			SegmentPiece block = getBlockIfPossible();
			if(block != null) {
				this.autoLock = autoLock;
				((NetworkInventoryInterface) block.getSegmentController().getNetworkObject()).getInventoryAutoLockModBuffer().add(new RemoteLongBoolean(new LongBooleanPair(block.getAbsoluteIndex(), autoLock), block.getSegmentController().isOnServer()));
				return true;
			} else {
				System.err.println("LockBox block not loaded, cant set auto lock!");
			}
		}
		return false;
	}

	private void unlock() {
		unlocked = true;
		unlockTimer = System.currentTimeMillis();
	}

	private void lock() {
		unlocked = false;
		unlockTimer = -1;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public boolean hasPassword() {
		return password != null && !password.isEmpty();
	}

	public boolean setPassword(String password) {
		if(unlocked) {
			SegmentPiece block = getBlockIfPossible();
			if(block != null) {
				this.password = password;
				((NetworkInventoryInterface) block.getSegmentController().getNetworkObject()).getInventoryPasswordModBuffer().add(new RemoteLongString(new LongStringPair(block.getAbsoluteIndex(), password), block.getSegmentController().isOnServer()));
				return true;
			} else {
				System.err.println("LockBox block not loaded, cant set password!");
			}
		}
		return false;
	}

	public boolean checkPassword(String password) {
		if(hasPassword()) {
			if(this.password.equals(password)) {
				unlock();
				return true;
			} else return false;
		} else {
			unlock();
			return true;
		}
	}

	public Tag toMetaData() {
		return new Tag(Tag.Type.STRUCT, null, new Tag[]{new Tag(Tag.Type.SHORT, null, production), filter.toTagStructure(), new Tag(Tag.Type.STRING, null, getCustomName()), new Tag(Tag.Type.INT, null, productionLimit), new Tag(Tag.Type.STRING, "password", password), new Tag(Tag.Type.BYTE, "autoLock", (byte) (autoLock ? 1 : 0)), FinishTag.INST});
	}

	public void fromMetaData(Tag tag) {
		Tag[] value = tag.getStruct();
		if(value[1].getType() != Tag.Type.FINISH) {
			filter.fromTagStructure(value[1]);
		}
		production = (Short) value[0].getValue();
		if(value.length > 2 && value[2].getType() != Tag.Type.FINISH) {
			customName = value[2].getString();
		}
		if(value.length > 3 && value[3].getType() != Tag.Type.FINISH) {
			productionLimit = value[3].getInt();
		}
		if(value.length > 4 && value[4].getType() != Tag.Type.FINISH) password = value[4].getString();
		else password = "";
		if(value.length > 5 && value[5].getType() != Tag.Type.FINISH) autoLock = value[5].getBoolean();
		else autoLock = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.inventory.Inventory#fromTagStructure(org.schema.schine.resource.tag.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		if("stash".equals(tag.getName())) {
			Tag[] value = (Tag[]) tag.getValue();

			fromMetaData(value[0]);
			super.fromTagStructure(value[1]);
		} else {
			//old
			super.fromTagStructure(tag);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.inventory.Inventory#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, "stash", new Tag[]{toMetaData(), super.toTagStructure(), FinishTag.INST});
	}

	@Override
	public int getActiveSlotsMax() {
		return 0;
	}


	@Override
	public int getLocalInventoryType() {
		return STASH_INVENTORY;
	}


	/**
	 * @return the production
	 */
	@Override
	public short getProduction() {
		return production;
	}

	/**
	 * @param production the production to set
	 */
	public void setProduction(short production) {
		this.production = production;
	}

	@Override
	public InventoryFilter getFilter() {
		return filter;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getCustomName() {
		return customName;
	}

	/**
	 * @param name the name to set
	 */
	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public int getProductionLimit() {
		return productionLimit;
	}

	public void setProductionLimit(int productionLimit) {
		this.productionLimit = productionLimit;
	}

	@Override
	public void requestClient(GameClientState state) {
		if(getInventoryHolder() instanceof ManagerContainer<?>) {
			NetworkSegmentProvider networkObject = ((ClientSegmentProvider) ((ManagerContainer<?>) getInventoryHolder()).getSegmentController().getSegmentProvider()).getSendableSegmentProvider().getNetworkObject();
			networkObject.inventoryDetailRequests.add(getParameter());
			System.err.println("REQUEST INVENTORY: " + this);
		}
		super.requestClient(state);
	}

}
