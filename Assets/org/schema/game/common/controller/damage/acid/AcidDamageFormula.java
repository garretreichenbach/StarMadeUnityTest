package org.schema.game.common.controller.damage.acid;

import org.schema.game.common.controller.damage.projectile.ProjectileHandlerSegmentController.ShotStatus;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public abstract class AcidDamageFormula {
	public enum AcidFormulaType{
		EQUAL_DIST(new AcidFormulaEqualized(), en -> {
			return Lng.str("Cylinder");
		}),
		CONE_START_WIDE(new AcidFormulaConeStartWide(), en -> {
			return Lng.str("Cone-Start-Wide");
		}),
		CONE_END_WIDE(new AcidFormulaConeEndWide(), en -> {
			return Lng.str("Cone-End-Wide");
		}),
		
		;
		
		public final AcidDamageFormula formula;
		private final Translatable t;

		private AcidFormulaType(AcidDamageFormula formula, Translatable t) {
			this.formula = formula;
			this.t = t;
		}
		
		static {
			for(AcidFormulaType t : values()) {
				assert(t == t.formula.getType()):t.name()+"; "+t.formula.getType().name();
			}
		}
		public String toString() {
			return t.getName(this);
		}
		
	}
	public String toString() {
		return getType().toString();
	}
	/**
	 * Used to define the acid shape along the line of blocks hit by a projectile
	 * 
	 * @param type, block type
	 * @param damage, damage done to the block the acid damage originates from
	 * @param restDamage, rest damage left in the projectile
	 * @param initialDamage, damage the shot initially had before it hit any block
	 * @param blockIndex, block index of blocks killed in line (starting at 0)
	 * @param totalArmorValue, total armor value of the accumulated line, is 0 if it is a normal block
	 * @param projectileWidth, width of projectile (starting at 1)
	 * @param penetrationDepth, how deep a projectile can go (starting at 1)
	 * @param shotStatus, either NORMAL or OVER_PENETRATING
	 * @param out, acid damage settings such as acidDamage and propagation distance
	 */
	public abstract void getAcidDamageSetting(short type, int damage, int restDamage, int initialDamage, float totalArmorValue, int blockIndex, float projectileWidth, int penetrationDepth, ShotStatus shotStatus, AcidSetting out);
	
	public abstract AcidFormulaType getType();
}
