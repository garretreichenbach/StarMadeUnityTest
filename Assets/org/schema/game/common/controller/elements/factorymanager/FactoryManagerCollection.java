package org.schema.game.common.controller.elements.factorymanager;

import api.element.block.Blocks;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class FactoryManagerCollection extends ElementCollectionManager<FactoryManagerUnit, FactoryManagerCollection, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection>> implements PowerConsumer {
	
	private boolean needsUpdate = true;
	private final Long2ObjectOpenHashMap<FactoryManagerModule> modules = new Long2ObjectOpenHashMap<>();
	
	public FactoryManagerCollection(SegmentController segController, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection> elementManager) {
		super(Blocks.FACTORY_MANAGER.getId(), segController, elementManager);
	}

	public static FactoryManagerModule getFromSegmentPiece(SegmentPiece segmentPiece) {
		if(segmentPiece.getSegmentController() instanceof ManagedUsableSegmentController<?> managedUsableSegmentController) {
			return managedUsableSegmentController.getManagerContainer().getFactoryManager().getCollectionManager().getModule(segmentPiece.getAbsoluteIndex());
		} else return null;
	}
	
	public FactoryManagerModule getModule(long absoluteIndex) {
		if(!modules.containsKey(absoluteIndex)) {
			FactoryManagerModule module = new FactoryManagerModule(absoluteIndex);
			modules.put(absoluteIndex, module);
			flagUpdate();
			return module;
		} else return modules.get(absoluteIndex);
	}

	private void flagUpdate() {
		needsUpdate = true;
	}
	
	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<FactoryManagerUnit> getType() {
		return FactoryManagerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return needsUpdate;
	}
	
	@Override
	public void update(Timer timer) {
		super.update(timer);
		needsUpdate = false;
	}

	@Override
	public FactoryManagerUnit getInstance() {
		return new FactoryManagerUnit();
	}

	@Override
	protected void onChangedCollection() {

	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return "Factory Manager";
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return false;
	}

	@Override
	public void setPowered(float powered) {

	}

	@Override
	public float getPowered() {
		return 0;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.FACTORIES;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {

	}

	@Override
	public boolean isPowerConsumerActive() {
		return false;
	}

	@Override
	public void dischargeFully() {

	}
}
