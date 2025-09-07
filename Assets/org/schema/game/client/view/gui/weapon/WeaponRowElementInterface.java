package org.schema.game.client.view.gui.weapon;

import java.util.List;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIEnterableListBlockedInterface;
import org.schema.schine.input.InputState;

public interface WeaponRowElementInterface extends GUIEnterableListBlockedInterface, Comparable<WeaponRowElementInterface>{

	public WeaponDescriptionPanel getDescriptionPanel(InputState state, GUIElement dependend);
	public GUIAnchor getWeaponColumn();


	public GUIAnchor getMainSizeColumn();


	public GUIAnchor getSecondaryColumn();


	public GUIAnchor getSizeColumn();


	public GUIAnchor getKeyColumn() ;


	public GUIAnchor getTertiaryColumn();


	public List<Object> getDescriptionList();
	public int getKey();
	public int getTotalSize();
	public long getUsableId();

	public int getMaxCharges();
	public int getCurrentCharges();
}
