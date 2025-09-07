package org.schema.game.common.controller.elements.combination.modifier.tagMod;

import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.FloatFormula;

public class BasicModifier implements FloatValueModifier {

	public final FloatFormula formulas;
	private final boolean linear;
	boolean inverse = false;
	private float ratio;
	private float maxBonus;
	private int combisize;
	private float effectBonus;
	private int masterSize;
	public BasicModifier(boolean inverse, float maxBonus, boolean linear, FloatFormula formulas) {
		super();
		this.inverse = inverse;
		this.maxBonus = maxBonus;
		this.formulas = formulas;
		this.linear = linear;
	}

	public float getRatio() {
		return ratio;
	}

	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(float ratio) {
		this.ratio = ratio;
	}

	public float getMaxBonus() {
		return maxBonus;
	}

	/**
	 * @param input The input value.
	 * @param size The size of the main group.
	 * @param combisize The size of the combination relative to the main group.
	 * @param effectBonus
	 * @param ratio The combination ratio.
	 * @param preScaled Whether the total input value is already scaled for the system size, including the secondary. This is generally true with nonlinear weapons, and otherwise unnecessary.
	 * @return the output of a config modifier.
	 */
	public float getOutput(float input, int size, int combisize, float effectBonus, float ratio, boolean preScaled) {
		this.ratio = ratio;
		this.combisize = combisize;
		this.effectBonus = effectBonus;
		this.masterSize = size;
		return getOutput(input, preScaled);
	}

	public float getOutput(float input, int size, int combisize, float effectBonus, float ratio) {
		this.ratio = ratio;
		this.combisize = combisize;
		this.effectBonus = effectBonus;
		this.masterSize = size;
		return getOutput(input, false);
	}

	@Override
	public float getOutput(float input) {
		return getOutput(input,false);
	}

	public float getOutput(float input, boolean preScaled){
		float val = input;
		if (formulas != null) {
			if (ratio > 0 || !formulas.needsRatio()) {
				formulas.setCombisize(combisize);
				formulas.setEffectsize(effectBonus);
				formulas.setMasterSize(masterSize);
				formulas.setInverse(inverse);
				formulas.setRatio(ratio);
				formulas.setMaxBonus(maxBonus);
				formulas.setLinear(linear);
				formulas.setPreScaledInput(preScaled);
				val = formulas.getOutput(val);
			} else {
//				System.err.println("Ratio is zero. not modifying "+formulas.isLinear());

				if (ratio == 0 && formulas.needsRatio() && linear) {
					//0% ratio, we still have to count in linear
					val = (masterSize + effectBonus) * input;
				}
			}

		} else if (linear) {
			//"skip": we still need to do linear
			val = (masterSize + effectBonus + combisize) * input;
		}

		return val;
	}

	@Override
	public String toString() {
		return "BasicModifier [formulas=" + formulas + ", linear=" + linear + ", inverse=" + inverse + ", ratio="
				+ ratio + ", maxBonus=" + maxBonus + ", combisize=" + combisize + ", effectBonus=" + effectBonus
				+ ", masterSize=" + masterSize + "]";
	}

}
