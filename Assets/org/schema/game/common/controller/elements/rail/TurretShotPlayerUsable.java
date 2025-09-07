package org.schema.game.common.controller.elements.rail;


import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.client.view.gui.weapon.WeaponSegmentControllerUsableElement;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerReloadInterface;
import org.schema.game.common.controller.elements.SegmentControllerUsable;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class TurretShotPlayerUsable extends SegmentControllerUsable{

	public TurretShotPlayerUsable(ManagerContainer<?> o){
		super(o);
	}
	public KeyboardMappings shootFlag = null;
	@Override
	public WeaponRowElementInterface getWeaponRow() {
		WeaponRowElementInterface row = new WeaponSegmentControllerUsableElement(this);
		return row;
	}

	@Override
	public boolean isControllerConnectedTo(long index, short type) {
		return true;
	}

	@Override
	public boolean isPlayerUsable() {
		return segmentController.railController.hasTurret();
	}

	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_SHOOT_TURRETS;
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
		shootFlag = null;
		if(unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) && unit.isFlightControllerActive() ){
			if(segmentController.isOnServer()){
				shootFlag = KeyboardMappings.SHIP_PRIMARY_FIRE;
			}
		}
	}
	@Override
	public ManagerReloadInterface getReloadInterface() {
		return null;
	}

	@Override
	public ManagerActivityInterface getActivityInterface() {
		return null;
	}

	@Override
	public String getWeaponRowName() {
		return Lng.str("Turret Control");
	}

	@Override
	public short getWeaponRowIcon() {
		return ElementKeyMap.RAIL_BLOCK_TURRET_Y_AXIS;
	}

	@Override
	public String getName() {
		return Lng.str("Turret Control");
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Shoot Turrets"), hos, ContextFilter.IMPORTANT);
	}
}
