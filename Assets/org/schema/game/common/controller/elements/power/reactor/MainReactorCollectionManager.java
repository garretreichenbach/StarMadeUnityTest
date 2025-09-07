package org.schema.game.common.controller.elements.power.reactor;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class MainReactorCollectionManager extends ElementCollectionManager<MainReactorUnit, MainReactorCollectionManager, MainReactorElementManager>{


	public MainReactorCollectionManager(
			SegmentController segController, MainReactorElementManager em) {
		super(ElementKeyMap.REACTOR_MAIN, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<MainReactorUnit> getType() {
		return MainReactorUnit.class;
	}

	@Override
	protected void onFinishedCollection() {
		super.onFinishedCollection();
		//mark the stabilizer collection as dirty (if no reboot is necessary)
		getPowerInterface().flagStabilizersDirty();
		getPowerInterface().onAnyReactorModulesChanged();
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}


	@Override
	public void update(Timer timer) {
	}


	@Override
	public MainReactorUnit getInstance() {
		return new MainReactorUnit();
	}

	@Override
	protected void onChangedCollection() {
		getPowerInterface().calcBiggestAndActiveReactor();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Total Recharge"), "n/a")};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Main Reactor");
	}

	@Override
	public float getSensorValue(SegmentPiece connected){
		PowerInterface p = getPowerInterface();
		return  (float) p.getPowerConsumptionAsPercent();
	}


	
	
}
