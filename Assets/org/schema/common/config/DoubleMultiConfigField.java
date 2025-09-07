package org.schema.common.config;

public interface DoubleMultiConfigField extends MultiConfigField{
	public double get(int index);
	public void set(int i, double val);
}
