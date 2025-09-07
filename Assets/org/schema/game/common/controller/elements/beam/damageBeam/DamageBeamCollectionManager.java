package org.schema.game.common.controller.elements.beam.damageBeam;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealer;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.EffectChangeHanlder;
import org.schema.game.common.controller.elements.beam.BeamCollectionManager;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.server.ai.AIFireState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class DamageBeamCollectionManager extends BeamCollectionManager<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> implements BeamHandlerContainer<SegmentController>, DamageDealer, EffectChangeHanlder{

	private final DamageBeamHandler handler;
	private float speedMax;
	private float distMax;
	

	public DamageBeamCollectionManager(SegmentPiece element,
	                                   SegmentController segController, DamageBeamElementManager em) {

		super(element, ElementKeyMap.DAMAGE_BEAM_MODULE, segController, em);

		this.handler = new DamageBeamHandler(segController, this);
	}

	/**
	 * @return the handler
	 */
	@Override
	public DamageBeamHandler getHandler() {
		return handler;
	}
	protected boolean dropShieldsOnCharge() {
		return DamageBeamElementManager.DROP_SHIELDS_ON_CHARGING;
	}
	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<DamageBeamUnit> getType() {
		return DamageBeamUnit.class;
	}
	public float getPossibleZoomRaw() {
		return DamageBeamElementManager.POSSIBLE_ZOOM;
	}
	@Override
	public DamageBeamUnit getInstance() {
		return new DamageBeamUnit();
	}

	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		updateInterEffects(DamageBeamElementManager.basicEffectConfiguration, this.effectConfiguration);
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
		
		this.speedMax = 0;
		this.distMax = 0;
		ControlBlockElementCollectionManager<?, ?, ?> supp = getSupportCollectionManager();
		ControlBlockElementCollectionManager<?, ?, ?> eff = getEffectCollectionManager();
		for(DamageBeamUnit u : getElementCollections()) {
			if(supp != null) {
				BeamUnitModifier<?> mod = (BeamUnitModifier<?>) getElementManager().getAddOn().getGUI(this, u, supp, eff);
				this.distMax = Math.max(distMax, mod.outputDistance);
			}else {
				this.distMax = Math.max(distMax, u.getDistance());
			}
		}
		
	}
	@Override
	public float getChargeTime() {
		return DamageBeamElementManager.CHARGE_TIME;
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
		return Lng.str("Damage Beam System");
	}
	@Override
	public DamageDealerType getDamageDealerType() {
		return DamageDealerType.BEAM;
	}
	private InterEffectSet effectConfiguration = new InterEffectSet(DamageBeamElementManager.basicEffectConfiguration);

	@Override
	public AIFireState getAiFireState(ControllerStateInterface unit) {
		
		float timeToFire = 0;
		BeamCombiSettings prms = getWeaponChargeParams();
		timeToFire += prms.chargeTime;
		timeToFire += prms.burstTime > 0 ? prms.burstTime : unit.getBeamTimeout();
		
		if(timeToFire > 0) {
			AIFireState fs = new AIFireState(){
				@Override
				public boolean needsShotReleased() {
					return false; //beam will cycle automatically; can mark as already released
				}
			};
			fs.secondsToExecute = timeToFire;
			fs.timeStarted = getState().getUpdateTime();
			return fs;
		}
		return null;
	}

	@Override
	public InterEffectSet getAttackEffectSet() {
		return this.effectConfiguration;
	}

	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect() {
				return null;
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Fire"), hos, ContextFilter.IMPORTANT);
		if(getPossibleZoom() > 1f) {
			h.addHelper(KeyboardMappings.SHIP_ZOOM, Lng.str("Zoom"), hos, ContextFilter.IMPORTANT);
		}
		h.addHelper(KeyboardMappings.SWITCH_FIRE_MODE, Lng.str("Switch Firing Mode [%s]", getFireMode().getName()), hos, ContextFilter.CRUCIAL);
	}
	
}
