package org.schema.common.config;

public interface FloatMultiConfigField extends MultiConfigField{
	public float get(int index);
	public void set(int i, float val);
}
