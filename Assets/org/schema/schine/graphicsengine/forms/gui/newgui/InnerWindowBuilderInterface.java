package org.schema.schine.graphicsengine.forms.gui.newgui;

public interface InnerWindowBuilderInterface {

	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt);

	public void updateOnDraw();

	public void adaptTextBox(int t, GUIInnerTextbox tb);
}
