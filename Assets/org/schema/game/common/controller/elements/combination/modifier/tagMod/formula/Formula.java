package org.schema.game.common.controller.elements.combination.modifier.tagMod.formula;

public abstract class Formula {
	private float maxBonus;
	private float ratio;
	private boolean inverse;
	private boolean linear;
	private boolean preScaled = false;
	private int combisize;
	private float effectsize;
	private int masterSize;

	/**
	 * @return the maxBonus
	 */
	public float getMaxBonus() {
		return maxBonus;
	}

	/**
	 * @param maxBonus the maxBonus to set
	 */
	public void setMaxBonus(float maxBonus) {
		this.maxBonus = maxBonus;
	}

	/**
	 * @return the ratio
	 */
	public float getRatio() {
		return ratio;
	}

	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(float ratio) {
		this.ratio = ratio;
	}

	/**
	 * @return the inverse
	 */
	public boolean isInverse() {
		return inverse;
	}

	/**
	 * @param inverse the inverse to set
	 */
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	/**
	 * @return whether the value scales with total group size (primary + secondary + effect) or just the ratio between primary and secondary alone
	 */
	public boolean isLinear() {
		return linear;
	}

	/**
	 * @param linear the linear to set
	 */
	public void setLinear(boolean linear) {
		this.linear = linear;
	}

	/**
	 * @return the combisize
	 */
	public int getCombisize() {
		return combisize;
	}

	/**
	 * @param combisize the combisize to set
	 */
	public void setCombisize(int combisize) {
		this.combisize = combisize;
	}

	/**
	 * @return the effectsize
	 */
	public float getEffectsize() {
		return effectsize;
	}

	/**
	 * @param effectsize the effectsize to set
	 */
	public void setEffectsize(float effectsize) {
		this.effectsize = effectsize;
	}

	/**
	 * @return the masterSize
	 */
	public int getMasterSize() {
		return masterSize;
	}

	/**
	 * @param masterSize the masterSize to set
	 */
	public void setMasterSize(int masterSize) {
		this.masterSize = masterSize;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * @param preScaled Set whether the input is already scaled according to system size.<br/>
	 *                     Useful for cases where the input scaling is nonlinear and thus shouldn't be calculated in the formula. May only work correctly if isLinear is set true.
	 */
	public void setPreScaledInput(boolean preScaled) {
		this.preScaled = preScaled;
	}

	public boolean isPreScaled() {
		return preScaled;
	}
}
