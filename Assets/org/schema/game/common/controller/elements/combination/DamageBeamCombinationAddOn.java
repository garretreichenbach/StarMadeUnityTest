package org.schema.game.common.controller.elements.combination;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamElementManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamUnit;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.controller.elements.combination.modifier.MultiConfigModifier;

public class DamageBeamCombinationAddOn extends BeamCombinationAddOn<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> {

	@ConfigurationElement(name = "cannon")
	private static final MultiConfigModifier<BeamUnitModifier<DamageBeamUnit>, DamageBeamUnit, BeamCombiSettings> beamCannonUnitModifier = new MultiConfigModifier<BeamUnitModifier<DamageBeamUnit>, DamageBeamUnit, BeamCombiSettings>(){
		@Override
		public BeamUnitModifier<DamageBeamUnit> instance() {
			return new BeamUnitModifier();
		}
	};
	@ConfigurationElement(name = "beam")
	private static final MultiConfigModifier<BeamUnitModifier<DamageBeamUnit>, DamageBeamUnit, BeamCombiSettings> beamBeamUnitModifier = new MultiConfigModifier<BeamUnitModifier<DamageBeamUnit>, DamageBeamUnit, BeamCombiSettings>(){
		@Override
		public BeamUnitModifier<DamageBeamUnit> instance() {
			return new BeamUnitModifier();
		}
	};
	@ConfigurationElement(name = "missile")
	private static final MultiConfigModifier<BeamUnitModifier<DamageBeamUnit>, DamageBeamUnit, BeamCombiSettings> beamMissileUnitModifier = new MultiConfigModifier<BeamUnitModifier<DamageBeamUnit>, DamageBeamUnit, BeamCombiSettings>(){
		@Override
		public BeamUnitModifier<DamageBeamUnit> instance() {
			return new BeamUnitModifier();
		}
	};

	public DamageBeamCombinationAddOn(DamageBeamElementManager elementManager,
	                                  GameStateInterface inferface) {
		super(elementManager, inferface);
	}

	@Override
	protected String getTag() {
		return "damagebeam";
	}

	@Override
	protected BeamUnitModifier getBeamCannonUnitModifier() {
		return beamCannonUnitModifier.get(elementManager);
	}

	@Override
	protected BeamUnitModifier getBeamMissileUnitModifier() {
		return beamMissileUnitModifier.get(elementManager);
	}

	@Override
	protected BeamUnitModifier getBeamBeamUnitModifier() {
		return beamBeamUnitModifier.get(elementManager);
	}

	

}
