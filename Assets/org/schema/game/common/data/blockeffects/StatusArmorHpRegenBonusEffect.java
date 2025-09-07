package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class StatusArmorHpRegenBonusEffect extends StatusBlockEffect {

	public StatusArmorHpRegenBonusEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_ARMOR_HP_REGEN_BONUS, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {

	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return 0;
	}

	@Override
	public boolean affectsMother() {
		return false;
	}
}
