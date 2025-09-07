package org.schema.common.config;

public interface IntMultiConfigField extends MultiConfigField{
	public int get(int index);
	public void set(int i, int val);
}
