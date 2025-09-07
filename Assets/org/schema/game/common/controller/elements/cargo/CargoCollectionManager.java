package org.schema.game.common.controller.elements.cargo;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class CargoCollectionManager extends ControlBlockElementCollectionManager<CargoUnit, CargoCollectionManager, CargoElementManager> {


	private double lastInventoryVolume;
	private long lastBlockUpdate;
	private long lastBleedUpdate;
	private boolean flagCollectionChanged;
	private final SegmentPiece tmpPce = new SegmentPiece();

	
	public CargoCollectionManager(SegmentPiece element,
	                                SegmentController segController, CargoElementManager em) {
		super(element, ElementKeyMap.CARGO_SPACE, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<CargoUnit> getType() {
		return CargoUnit.class;
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public CargoUnit getInstance() {
		return new CargoUnit();
	}

	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
		flagCollectionChanged = true;
	}
	@Override
	public void remove(long blockIndex) {
		super.remove(blockIndex);
		
		//reset block to visually transparent by setting orientation on client
		SegmentPiece block = getSegmentController().getSegmentBuffer().getPointUnsave(blockIndex, tmpPce);
		if(block != null){
			SegmentData segmentData = block.getSegment().getSegmentData();
			if(segmentData != null){
				try{
					segmentData.setOrientation(block.getInfoIndex(), (byte)4);
				} catch (SegmentDataWriteException e) {
					segmentData = SegmentDataWriteException.replaceDataOnClient(segmentData);
					try {
						segmentData.setOrientation(block.getInfoIndex(), (byte)4);
					} catch (SegmentDataWriteException e1) {
						e1.printStackTrace();
						throw new RuntimeException(e1);
					}
				}
			}
			if(!getSegmentController().isOnServer() && block.getSegment() != null){
				((DrawableRemoteSegment)block.getSegment()).dataChanged(true);
			}
		}
		
	}
	@Override
	protected void onRemovedCollection(long absPos, CargoCollectionManager instance){
		super.onRemovedCollection(absPos, instance);
		for(int i = 0; i < getElementCollections().size(); i++){
			getElementCollections().get(i).resetBlocks();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ElementCollectionManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
		
		long delay = !getSegmentController().isOnServer() ? 4000 : 9000+(long)(Math.random()*2000);
		if((Math.abs(lastInventoryVolume - getInventoryVolume()) > 5 || flagCollectionChanged) && timer.currentTime - lastBlockUpdate > delay){
			if(getSegmentController().isOnServer() || getSegmentController().isInClientRange()){
				
				
				double volume = getInventoryVolume();
				
				for(int i = 0; i < getElementCollections().size(); i++){
					volume = getElementCollections().get(i).updateBlocks(volume, i == 0 ? getElementManager().getInventoryBaseCapacity() : 0);
				}
				lastInventoryVolume = getInventoryVolume();
				lastBlockUpdate = timer.currentTime;
				flagCollectionChanged = false;
			}
		}
		
		
		
		long bleedDelay = 1000*60;
		long firstDelay = 1000*60;
		if(getSegmentController().isOnServer() && ServerConfig.CARGO_BLEED_AT_OVER_CAPACITY.isOn() && timer.currentTime - lastBleedUpdate > bleedDelay){
			Inventory inventory = getElementManager().getManagerContainer().getInventory(getControllerPos());
			
			if(inventory != null && inventory.isOverCapacity() &&
					getSegmentController().isFullyLoadedWithDock() && System.currentTimeMillis() - getSegmentController().getTimeCreated() > firstDelay){
				inventory.spawnVolumeInSpace(getSegmentController(), CargoElementManager.PERCENTAGE_BLEED_PER_MINUTE);
			}
			this.lastBleedUpdate = timer.currentTime;
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {

		return new GUIKeyValueEntry[]{};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Cargo System");
	}

	public double getInventoryVolume() {
		Inventory inventory = getElementManager().getManagerContainer().getInventory(getControllerPos());
		if(inventory != null){
			return inventory.getVolume();
		}
		return 0;
		
	}

	public double getCapacity() {
		double size = getElementManager().getInventoryBaseCapacity();
		for(int i = 0; i < getElementCollections().size(); i++){
			size += getElementCollections().get(i).getCapacity();
		}
		return getSegmentController().getConfigManager().apply(StatusEffectType.CARGO_VOLUME, size);
	}

	@Override
	public float getSensorValue(SegmentPiece connected){
		return  (float) Math.min(1f, getInventoryVolume() / Math.max(0.0001f, (getCapacity())));
	}


}
