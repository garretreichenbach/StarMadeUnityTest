package org.schema.game.common.controller.elements.pulse.push;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.pulse.PulseElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class PushPulseElementManager extends PulseElementManager<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> {

	public static boolean debug = false;
	@ConfigurationElement(name = "Force")
	public static float BASE_FORCE = 100;
	@ConfigurationElement(name = "ReloadMs")
	public static float BASE_RELOAD = 10000;
	@ConfigurationElement(name = "Radius", description = "blast radius")
	public static float BASE_RADIUS = 16;
	@ConfigurationElement(name = "PowerConsumption")
	public static float BASE_POWER_CONSUMPTION = 300;
	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 10;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;

	public PushPulseElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.PUSH_PULSE_CONTROLLER_ID, ElementKeyMap.PUSH_PULSE_ID, segmentController);
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.PUSH_PULSE_ID);
	}

	@Override
	public void addSinglePulse(Transform t, Vector3f dir, float pulsePower, float radius,
			long weaponId, Vector4f pulseColor) {
		getPulseController().addPushPulse(t, dir, getSegmentController(), pulsePower, radius, weaponId, pulseColor);
	}

	@Override
	public String getManagerName() {
		return "Push Pulse System Collective";
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(PushPulseUnit firingUnit,
	                                             PushPulseCollectionManager col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		if (effectCol != null) {
			col.setEffectTotal(effectCol.getTotalSize());
		} else {
			col.setEffectTotal(0);
		}
		float damage = firingUnit.getPulsePower();
		float radius = firingUnit.getRadius();
		float reload = firingUnit.getReloadTimeMs();
		float powerConsumption = firingUnit.getPowerConsumption();
		float mode = 1;
		float stop = 0;
		float pull = 0;
		float push = 0;
		float bonusShields = 0;
		float bonusBlocks = 0;
		float explosiveRadius = 0;
		float powerConsumptionBonus = 0;
		float armorEfficiency = 0;
		float addPowerDamage = 0;
		float effectRatio = 0;

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Push Pulse Unit"), firingUnit,
				new ModuleValueEntry(Lng.str("Force"), StringTools.formatPointZero(damage)),
				new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(radius)),
				new ModuleValueEntry(Lng.str("Reload(ms)"), StringTools.formatPointZero(reload)),
				new ModuleValueEntry(Lng.str("Power Consumption"), StringTools.formatPointZero(powerConsumption)),
				new ModuleValueEntry(Lng.str("Effect Ratio(%):"), StringTools.formatPointZero(effectRatio)),
				new ModuleValueEntry(Lng.str("(Effect)ArmorEfficiency"), StringTools.formatPointZero(armorEfficiency)),
				new ModuleValueEntry(Lng.str("(Effect)PowerConsumptionModifier"), StringTools.formatPointZero(powerConsumptionBonus)),
				new ModuleValueEntry(Lng.str("(Effect)ExplosiveRadius"), StringTools.formatPointZero(explosiveRadius)),
				new ModuleValueEntry(Lng.str("(Effect)Push"), StringTools.formatPointZero(push)),
				new ModuleValueEntry(Lng.str("(Effect)Pull"), StringTools.formatPointZero(pull)),
				new ModuleValueEntry(Lng.str("(Effect)Stop"), StringTools.formatPointZero(stop)),
				new ModuleValueEntry(Lng.str("(Effect)ShieldBonus"), StringTools.formatPointZero(bonusShields))
		);
	}

	@Override
	protected String getTag() {
		return "pushpulse";
	}

	@Override
	public PushPulseCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<PushPulseCollectionManager> clazz) {
		return new PushPulseCollectionManager(position, getSegmentController(), this);
	}


}
