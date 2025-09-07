package org.schema.game.client.view.gui.reactor;

import org.schema.game.common.data.element.ElementInformation;

public interface GUIReactorManagerInterface {

	public void onTreeNotFound(GUIReactorTree guiReactorTree);
	public void setSelectedTab(ElementInformation info);
	public ElementInformation getSelectedTab();
	public boolean isActive();

}
