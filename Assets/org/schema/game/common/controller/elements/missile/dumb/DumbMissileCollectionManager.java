package org.schema.game.common.controller.elements.missile.dumb;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealer;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.EffectChangeHanlder;
import org.schema.game.common.controller.elements.combination.MissileCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.MissileUnitModifier;
import org.schema.game.common.controller.elements.missile.MissileCollectionManager;
import org.schema.game.common.controller.elements.cannon.ZoomableUsableModule;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class DumbMissileCollectionManager extends MissileCollectionManager<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> implements PlayerUsableInterface, DamageDealer, EffectChangeHanlder, ZoomableUsableModule{

	private float speedMax;
	private float distMax;

	public DumbMissileCollectionManager(SegmentPiece element,
	                                    SegmentController segController, DumbMissileElementManager em) {
		super(element, ElementKeyMap.MISSILE_DUMB_ID, segController, em);
	}

	@Override
	public int getMargin() {
				return 0;
	}

	@Override
	protected Class<DumbMissileUnit> getType() {
		return DumbMissileUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public DumbMissileUnit getInstance() {
		return new DumbMissileUnit();
	}

	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		updateInterEffects(DumbMissileElementManager.basicEffectConfiguration, this.effectConfiguration);
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
			
			this.speedMax = 0;
			this.distMax = 0;
			ControlBlockElementCollectionManager<?, ?, ?> supp = getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> eff = getEffectCollectionManager();
			for(DumbMissileUnit u : getElementCollections()) {
				if(supp != null) {
					MissileUnitModifier<?> mod = (MissileUnitModifier<?>) getElementManager().getAddOn().getGUI(this, u, supp, eff);
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
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Missile System");
	}
	
	@Override
	public DamageDealerType getDamageDealerType() {
		return DamageDealerType.MISSILE;
	}

	private InterEffectSet effectConfiguration = new InterEffectSet(DumbMissileElementManager.basicEffectConfiguration);

	@Override
	public InterEffectSet getAttackEffectSet() {
		return this.effectConfiguration;
	}
	public MissileCombiSettings getWeaponChargeParams() {
		getElementManager().getCombiSettings().lockOnTime = getLockOnTimeRaw();
		getElementManager().getCombiSettings().possibleZoom = getPossibleZoomRaw();
		
		ControlBlockElementCollectionManager<?, ?, ?> cp = getSupportCollectionManager();
		if(cp != null) {
			getElementManager().getAddOn().calcCombiSettings(getElementManager().getCombiSettings(), this, cp, getEffectCollectionManager());
		}
		return getElementManager().getCombiSettings();
	}
	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect() {
				return null;
	}
	public float getPossibleZoomRaw() {
		return DumbMissileElementManager.POSSIBLE_ZOOM;
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, "Fire Missile", hos, ContextFilter.IMPORTANT);
		if(getPossibleZoom() > 1f) {
			h.addHelper(KeyboardMappings.SHIP_ZOOM, Lng.str("Zoom"), hos, ContextFilter.IMPORTANT);
		}
		h.addHelper(KeyboardMappings.SWITCH_FIRE_MODE, Lng.str("Switch Firing Mode [%s]", getFireMode().getName()), hos, ContextFilter.CRUCIAL);
	}

	public float getLockOnTime() {
		return getWeaponChargeParams().lockOnTime;
	}
	public float getLockOnTimeRaw() {
		return DumbMissileElementManager.LOCK_ON_TIME_SEC;
	}
	@Override
	public float getPossibleZoom() {
		return getWeaponChargeParams().possibleZoom;
	}
}
