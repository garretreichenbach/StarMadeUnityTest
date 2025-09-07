package org.schema.schine.graphicsengine.forms.gui;

public interface Suspendable {
	public boolean isActive();

	public boolean isHinderedInteraction();

	public boolean isSuspended();

	public boolean isTreeActive();

	public void suspend(boolean s);
}
