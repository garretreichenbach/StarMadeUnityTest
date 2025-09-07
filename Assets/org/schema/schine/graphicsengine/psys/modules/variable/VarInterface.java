package org.schema.schine.graphicsengine.psys.modules.variable;

public interface VarInterface<E extends Object> {
	public String getName();

	public E get();

	public void set(String f);

	public E getDefault();
}
