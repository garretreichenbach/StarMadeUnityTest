package org.schema.game.common.controller.elements.cannon;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.damage.projectile.ProjectileDamageDealer;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.EffectChangeHanlder;
import org.schema.game.common.controller.elements.FocusableUsableModule;
import org.schema.game.common.controller.elements.combination.CannonCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.CannonUnitModifier;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.server.ai.AIFireState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class CannonCollectionManager extends ControlBlockElementCollectionManager<CannonUnit, CannonCollectionManager, CannonElementManager> implements PlayerUsableInterface, ProjectileDamageDealer, EffectChangeHanlder, ZoomableUsableModule, FocusableUsableModule{

	public CannonCollectionManager(SegmentPiece element, SegmentController segController, CannonElementManager em) {
		super(element, ElementKeyMap.WEAPON_ID, segController, em);
	}

	
	public float damageCharge;
	public float currentDamageMult = 1;
	private float speedMax;
	private float distMax;
	@Override
	public int getMargin() {
		return 0;
	}
	public float getDamageChargeMax() {
		return CannonElementManager.DAMAGE_CHARGE_MAX;
	}
	public float getDamageChargeSpeed() {
		return CannonElementManager.DAMAGE_CHARGE_SPEED;
	}
	@Override
	protected Class<CannonUnit> getType() {
		return CannonUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public CannonUnit getInstance() {
		return new CannonUnit();
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
	public boolean isAllowedVolley() {
		return true;
	}
	public boolean isVolleyShot() {
		if(getSegmentController().isAIControlled() && getSegmentController() instanceof  SegmentControllerAIInterface &&
				((SegmentControllerAIInterface)getSegmentController()).getAiConfiguration().isActiveAI() && 
				((AIGameConfiguration<?, ?>)((SegmentControllerAIInterface)getSegmentController()).getAiConfiguration()).get(Types.FIRE_MODE).getCurrentState().equals("Volley")) {
			return true;
		}
			
		return mode == FireMode.VOLLEY;
	}
	@Override
	public void onSwitched(boolean on) {
		super.onSwitched(on);
		damageCharge = 0;
	}
	@Override
	public boolean canUseCollection(ControllerStateInterface unit, Timer timer) {
		
		CannonCombiSettings prms = getWeaponChargeParams();
		if(unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) && prms.damageChargeMax > 0) {
			boolean anyReloading = false;
			for(CannonUnit u : getElementCollections()) {
				if(!u.canUse(timer.currentTime, false)) {
					anyReloading = true;
					break;
				}
			}
			
			if(!anyReloading && damageCharge < prms.damageChargeMax) {
				damageCharge = Math.min(prms.damageChargeMax, damageCharge + timer.getDelta() * prms.damageChargeSpeed);
			}
			
			//don't shoot when button is down
			return false;
		}
		
		return super.canUseCollection(unit, timer);
	}
	@Override
	protected void onNotShootingButtonDown(ControllerStateInterface unit, Timer timer) {
		super.onNotShootingButtonDown(unit, timer);
		
		
		if(damageCharge > 0) {
			this.currentDamageMult = damageCharge;
			handleControlShot(unit, timer);
			damageCharge = 0;
			this.currentDamageMult = 1;
		}
	}
	
	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		updateInterEffects(CannonElementManager.basicEffectConfiguration, this.effectConfiguration);
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
		
		this.speedMax = 0;
		this.distMax = 0;
		ControlBlockElementCollectionManager<?, ?, ?> supp = getSupportCollectionManager();
		ControlBlockElementCollectionManager<?, ?, ?> eff = getEffectCollectionManager();
		for(CannonUnit u : getElementCollections()) {
			if(supp != null) {
				CannonUnitModifier mod = (CannonUnitModifier) getElementManager().getAddOn().getGUI(this, u, supp, eff);
				this.speedMax = Math.max(speedMax, mod.outputSpeed);
				this.distMax = Math.max(distMax, mod.outputDistance);
			}else {
				this.speedMax = Math.max(speedMax, u.getSpeed());
				this.distMax = Math.max(distMax, u.getDistance());
			}
		}
		
	}
	@Override
	public float getWeaponSpeed() {
		return speedMax;
	}
	@Override
	public float getWeaponDistance() {
		return distMax;
	}

	@Override
	public String getModuleName() {
		return Lng.str("Cannon System");
	}

	@Override
	public DamageDealerType getDamageDealerType() {
		return DamageDealerType.PROJECTILE;
	}

	private InterEffectSet effectConfiguration = new InterEffectSet(CannonElementManager.basicEffectConfiguration);
	public float damageProduced;

	@Override
	public InterEffectSet getAttackEffectSet() {
		return this.effectConfiguration;
	}


	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE && getSegmentController().isOnServer()) {
			handleControlShot(unit, timer);
		}
	}
	
	@Override
	public void handleControlShot(ControllerStateInterface unit, Timer timer){
		damageProduced = 0;
		super.handleControlShot(unit, timer);
		if(damageProduced > 0 ) {
			CannonCombiSettings weaponChargeParams = getWeaponChargeParams();
			if(weaponChargeParams.cursorRecoilX > 0 || weaponChargeParams.cursorRecoilY > 0) {
				getElementManager().handleCursorRecoil(this, damageProduced, weaponChargeParams);
			}
		}
	}


	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect() {
		return null;
	}

	public CannonCombiSettings getWeaponChargeParams() {
		getElementManager().getCombiSettings().acidType = getAcidFormula();
		getElementManager().getCombiSettings().possibleZoom = getPossibleZoomRaw();
		getElementManager().getCombiSettings().damageChargeMax = getDamageChargeMax();
		getElementManager().getCombiSettings().damageChargeSpeed = getDamageChargeSpeed();
		
		
		getElementManager().getCombiSettings().cursorRecoilX = getCursorRecoilX();
		getElementManager().getCombiSettings().cursorRecoilMinX = getCursorRecoilMinX();
		getElementManager().getCombiSettings().cursorRecoilMaxX = getCursorRecoilMaxX();
		getElementManager().getCombiSettings().cursorRecoilDirX = getCursorRecoilDirX();
		
		getElementManager().getCombiSettings().cursorRecoilY = getCursorRecoilY();
		getElementManager().getCombiSettings().cursorRecoilMinY = getCursorRecoilMinY();
		getElementManager().getCombiSettings().cursorRecoilMaxY = getCursorRecoilMaxY();
		getElementManager().getCombiSettings().cursorRecoilDirY = getCursorRecoilDirY();
		
		ControlBlockElementCollectionManager<?, ?, ?> cp = getSupportCollectionManager();
		if(cp != null) {
			getElementManager().getAddOn().calcCombiSettings(getElementManager().getCombiSettings(), this, cp, getEffectCollectionManager());
		}
		return getElementManager().getCombiSettings();
	}
	public float getPossibleZoomRaw() {
		return CannonElementManager.POSSIBLE_ZOOM;
	}
	public AcidFormulaType getAcidFormula() {
		AcidFormulaType[] v = AcidFormulaType.values();
		ControlBlockElementCollectionManager<?, ?, ?> cp = getSupportCollectionManager();
		int index = CannonElementManager.ACID_FORMULA_DEFAULT;
		if(cp != null) {
			getElementManager().getAddOn().calcCombiSettings(getElementManager().getCombiSettings(), this, cp, getEffectCollectionManager());
			return getElementManager().getCombiSettings().acidType;
		}
		assert(!(index >= v.length || index < 0)):"Invalid Acid formula index: "+index;
		return v[index];
	}
	
	@Override
	public AIFireState getAiFireState(ControllerStateInterface unit) {
		
		float timeToFire = 0;
		CannonCombiSettings prms = getWeaponChargeParams();
		if(prms.damageChargeMax > 0 && prms.damageChargeSpeed > 0) {
			timeToFire += prms.damageChargeMax / prms.damageChargeSpeed ;
		}
		
		if(timeToFire > 0) {
			final CannonCollectionManager source = this;
			AIFireState fs = new AIFireState(){
				@Override
				public boolean needsShotReleased() {
					return source.damageCharge >= source.getWeaponChargeParams().damageChargeMax;
				}

				@Override
				protected boolean canFinish() {
					return source.damageCharge == 0;
				}
			};
			fs.secondsToExecute = timeToFire;
			fs.timeStarted = getState().getUpdateTime();
			return fs;
		}
		return null;
	}
	
	public float getCursorRecoilX() {
		return CannonElementManager.CURSOR_RECOIL_X;
	}
	public float getCursorRecoilMinX() {
		return CannonElementManager.CURSOR_RECOIL_MIN_X;
	}
	public float getCursorRecoilMaxX() {
		return CannonElementManager.CURSOR_RECOIL_MAX_X;
	}
	public float getCursorRecoilDirX() {
		return CannonElementManager.CURSOR_RECOIL_DIR_X;
	}
	public float getCursorRecoilY() {
		return CannonElementManager.CURSOR_RECOIL_Y;
	}
	public float getCursorRecoilMinY() {
		return CannonElementManager.CURSOR_RECOIL_MIN_Y;
	}
	public float getCursorRecoilMaxY() {
		return CannonElementManager.CURSOR_RECOIL_MAX_Y;
	}
	
	public float getCursorRecoilDirY() {
		return CannonElementManager.CURSOR_RECOIL_DIR_Y;
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Fire cannon"), hos, ContextFilter.IMPORTANT);
		if(getPossibleZoom() > 1f) {
			h.addHelper(KeyboardMappings.SHIP_ZOOM, Lng.str("Zoom"), hos, ContextFilter.IMPORTANT);
		}
		h.addHelper(KeyboardMappings.SWITCH_FIRE_MODE, Lng.str("Switch Firing Mode [%s]", mode.getName()), hos, ContextFilter.CRUCIAL);
	}
	@Override
	public float getPossibleZoom() {
		return getWeaponChargeParams().possibleZoom;
	}
	
	
}



	
