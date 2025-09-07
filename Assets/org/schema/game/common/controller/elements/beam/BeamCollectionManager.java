package org.schema.game.common.controller.elements.beam;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FocusableUsableModule;
import org.schema.game.common.controller.elements.ManagerContainer.ReceivedBeamLatch;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.cannon.ZoomableUsableModule;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;

public abstract class BeamCollectionManager<E extends BeamUnit<E, CM, EM>, CM extends BeamCollectionManager<E, CM, EM>, EM extends BeamElementManager<E, CM, EM>> extends ControlBlockElementCollectionManager<E, CM, EM>
implements BeamHandlerContainer<SegmentController>, PlayerUsableInterface, ZoomableUsableModule, FocusableUsableModule{

	float beamCharge;
	private boolean wasShootButtonDown;
	private boolean charged;
	protected long lastBeamFired;
	public BeamCollectionManager(SegmentPiece controllerElement, short clazz, SegmentController segController, EM em) {
		super(controllerElement, clazz, segController, em);
	}
	@Override
	public boolean needsUpdate() {
		return true;
	}


	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE && getSegmentController().isOnServer()) {
			handleControlShot(unit, timer);
		}
	}
	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		getHandler().clearStates();

		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
	}
	public float getChargeTime() {
		return 0;
	}
	public BeamCombiSettings getWeaponChargeParams() {
		getElementManager().getCombiSettings().chargeTime = getChargeTime();
		getElementManager().getCombiSettings().possibleZoom = getPossibleZoomRaw();
		getElementManager().getCombiSettings().burstTime = getElementManager().getBurstTime();
		
		if(getElementManager().getAddOn()  != null) {
			ControlBlockElementCollectionManager<?, ?, ?> cp = getSupportCollectionManager();
			if(cp != null) {
				getElementManager().getAddOn().calcCombiSettings(getElementManager().getCombiSettings(), (CM) this, cp, getEffectCollectionManager());
			}
		}
		return getElementManager().getCombiSettings();
	}
	public void flagBeamFiredWithoutTimeout(E e) {
		this.lastBeamFired = getState().getUpdateTime();
	}
	@Override
	public void onSwitched(boolean on) {
		super.onSwitched(on);
		charged = false;
		wasShootButtonDown = false;
		beamCharge = 0;
	}
	
	public float getPossibleZoomRaw() {
		return -1;
	}
	private FireMode mode = FireMode.getDefault(this.getClass());
	@Override
	public boolean isInFocusMode() {
		return mode == FireMode.FOCUSED;
	}

	@Override
	public void setFireMode(FireMode mode) {
		this.mode = mode; 		
	}
	@Override
	public FireMode getFireMode() {
		return mode;
	}
	@Override
	public boolean canUseCollection(ControllerStateInterface unit, Timer timer) {
		
		if(super.canUseCollection(unit, timer)) {
			try {
				if(unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) ) {
					
					
					BeamCombiSettings prms = getWeaponChargeParams();
					if(prms.chargeTime > 0) {
						if(wasShootButtonDown && charged) {
							//retain fire and ignore chrage status as long as mouse button is held
							return true;
						}
						boolean anyReloading = false;
						for(E u : getElementCollections()) {
							if(!u.canUse(timer.currentTime, false)) {
								anyReloading = true;
								break;
							}
						}
						
						if(!anyReloading && beamCharge < prms.chargeTime) {
							if(beamCharge == 0 && dropShieldsOnCharge() && getElementManager().getManagerContainer() instanceof ShieldContainerInterface) {
								ShieldAddOn shieldAddOn = ((ShieldContainerInterface)getElementManager().getManagerContainer()).getShieldAddOn();
								//discharge old shields
								shieldAddOn.onHit(0L, (short)0, (long) Math.ceil(shieldAddOn.getShields()), DamageDealerType.GENERAL);
								
								boolean onCompleteStructure = true;
								//prevent new shields from working while charging
								shieldAddOn.getShieldLocalAddOn().addShieldCondition(new ShieldConditionInterface() {
									@Override
									public boolean isActive() {
										return beamCharge > 0;
									}

									@Override
									public boolean isShieldUsable() {
										return false;
									}
									
								}, onCompleteStructure);
							}
							beamCharge = Math.min(prms.chargeTime, beamCharge + timer.getDelta() );
						}
						if(beamCharge >= prms.chargeTime) {
							charged = true;
							beamCharge = 0;
							return true;
						}
						//don't shoot when button is down
						return false;
					}
				}
				
				return true;
			}finally {
				wasShootButtonDown = unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE);
			}
		}
		return false;
	}
	
	protected boolean dropShieldsOnCharge() {
		return false;
	}
	
	@Override
	protected void onNotShootingButtonDown(ControllerStateInterface unit, Timer timer) {
		super.onNotShootingButtonDown(unit, timer);
		BeamCombiSettings prms = getWeaponChargeParams();
		
		wasShootButtonDown = false;
		if(prms.chargeTime > 0) {
			if(beamCharge >= prms.chargeTime) {
				handleControlShot(unit, timer);
				beamCharge = 0;
			}else {
				charged = false;
				if(beamCharge > 0) {
					getSegmentController().popupOwnClientMessage("notFireBeamNotCharged", Lng.str("Weapon must be charged for %s sec!",StringTools.formatPointZero(prms.chargeTime)), ServerMessage.MESSAGE_TYPE_ERROR);
					beamCharge = 0;
				}
			}
		}
	}
	@Override
	protected void onRemovedCollection(long absPos, CM instance) {
		super.onRemovedCollection(absPos, instance);
		getElementManager().notifyBeamDrawer();
	}
	@Override
	public void update(Timer timer) {
		super.update(timer);
		getHandler().update(timer);
	}
	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return getElementManager().getManagerContainer().getAttackEffectSet(weaponId, damageDealerType);
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	public boolean handleBeamLatch(ReceivedBeamLatch d) {
		return getHandler().handleBeamLatch(d);
	}
	@Override
	public float getPossibleZoom() {
		return getWeaponChargeParams().possibleZoom;
	}
	
}
