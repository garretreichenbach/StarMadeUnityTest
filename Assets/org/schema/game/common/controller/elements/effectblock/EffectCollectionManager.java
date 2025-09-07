package org.schema.game.common.controller.elements.effectblock;

import java.util.List;

import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerModule;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.BlockEffectManager;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public abstract class EffectCollectionManager<E extends EffectUnit<E, CM, EM>, CM extends EffectCollectionManager<E, CM, EM>, EM extends EffectElementManager<E, CM, EM>> extends ControlBlockElementCollectionManager<E, CM, EM> implements PlayerUsableInterface{

	public EffectCollectionManager(SegmentPiece controllerElement, short clazz,
	                               SegmentController segController, EM em) {
		super(controllerElement, clazz, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	protected void onChangedCollection() {
		
		for(ManagerModule<?, ?, ?> m : getElementManager().getManagerContainer().getModules()) {
			if(m.getElementManager() instanceof UsableControllableElementManager) {
				List<ControlBlockElementCollectionManager<?,?,?>> cml = (List<ControlBlockElementCollectionManager<?, ?, ?>>) ((UsableControllableElementManager<?, ?, ?>)m.getElementManager()).getCollectionManagers();
				for(ControlBlockElementCollectionManager<?,?,?> cm : cml) {
					if(cm.getEffectCollectionManager() == this) {
						cm.onEffectChanged();
					}
				}
			}
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	private BlockEffectManager getBlockEffectManager() {
		return ((SendableSegmentController) getSegmentController()).getBlockEffectManager();
	}

	public BlockEffect getCurrentBlockEffect() {
		return getBlockEffectManager().getEffectByBlockIdentifyer(getControllerElement().getAbsoluteIndex());
	}

	public void addEffect(BlockEffect e) {
		e.setBlockId(getControllerElement().getAbsoluteIndexWithType4());
		getBlockEffectManager().addEffect(e);
	}
	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer){
		super.handleKeyPress(unit, timer);
		
	}
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		super.handleKeyEvent(unit, mapping, timer);
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE || mapping == KeyboardMappings.SHIP_ZOOM) {
			getElementManager().handle(unit, timer);
		}
	}
	@Override
	public boolean isPlayerUsable() {
		return !isUsingPowerReactors();
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, "Activate/Deactivate", hos, ContextFilter.IMPORTANT);
	}
}
