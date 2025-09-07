package org.schema.game.common.staremote.gui.settings;

import javax.swing.JComponent;

public interface StarmoteSettingElement {

	public JComponent getComponent();

	public String getValueName();

	public boolean isEditable();

	public boolean update();
}
