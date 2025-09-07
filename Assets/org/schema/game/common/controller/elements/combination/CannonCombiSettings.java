package org.schema.game.common.controller.elements.combination;

import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;

public class CannonCombiSettings implements CombinationSettings {

	public AcidFormulaType acidType;
	public float damageChargeMax;
	public float damageChargeSpeed;

	public float cursorRecoilX;
	public float cursorRecoilMinX;
	public float cursorRecoilMaxX;
	public float cursorRecoilDirX;

	public float cursorRecoilY;
	public float cursorRecoilMinY;
	public float cursorRecoilMaxY;
	public float cursorRecoilDirY;
	public float possibleZoom;

}
