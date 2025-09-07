package org.schema.schine.graphicsengine.forms.gui;

public interface GUICallbackBlocking extends GUICallback{

	public boolean isBlocking();

	public void onBlockedCallbackExecuted();

	public void onBlockedCallbackNotExecuted(boolean anyOtherBlockedCallbacksExecuted);
}
