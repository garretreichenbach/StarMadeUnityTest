package org.schema.game.common.data.element.beam;

import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.BeamState;

public abstract class BeamHandler extends AbstractBeamHandler<SegmentController> {
	protected static final float BEAM_TIMEOUT_IN_SECS = 0.05f;
	private final BeamLatchTransitionInterface latchTransitionInterface;

	protected BeamLatchTransitionInterface initLatchTransitionInterface() {
		return new DefaultLatchTransitionInterface();
	}
	
	public BeamHandler(SegmentController segmentController, BeamHandlerContainer owner) {
		super(owner, segmentController);
		this.latchTransitionInterface = initLatchTransitionInterface();
	}

	@Override
	public void transform(BeamState con) {
		if (!getBeamShooter().isOnServer()) {
			getBeamShooter().getWorldTransformOnClient().transform(con.from);
		} else {
			getBeamShooter().getWorldTransform().transform(con.from);
		}
	}
	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType){
		return getBeamShooter().getAttackEffectSet(weaponId, damageDealerType);
	}	
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return getBeamShooter().getMetaWeaponEffect(weaponId, damageDealerType);
	}

	@Override
	public boolean isUsingOldPower() {
		return getBeamShooter().isUsingOldPower();
	}

	@Override
	public BeamLatchTransitionInterface getBeamLatchTransitionInterface() {
		return latchTransitionInterface;
	}
	public int getSectorId() {
		return getBeamShooter().getSectorId();
	}
}
