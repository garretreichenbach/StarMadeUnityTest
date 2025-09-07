package org.schema.schine.graphicsengine.forms.gui.newgui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface GUITabInterface extends GUIWindowInterface {

	public ObjectArrayList<GUIContentPane> getTabs();

	public int getSelectedTab();

	public void setSelectedTab(int tab);

	public int getInnerWidthTab();
}
