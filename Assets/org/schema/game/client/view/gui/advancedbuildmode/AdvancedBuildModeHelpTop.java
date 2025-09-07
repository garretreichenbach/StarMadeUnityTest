package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.KeyboardMappings;

public class AdvancedBuildModeHelpTop extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeHelpTop(AdvancedGUIElement e) {
		super(e);
	}
	
	
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		GUITextOverlay l2 = new GUITextOverlay(getState());
		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
			@Override
			public String getName() {
				if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isStickyAdvBuildMode()){
					return Lng.str("Press %s to unsticky\nadvanced build mode.", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar());
				}else{
					return Lng.str("Hold %s to access build tools.\nPress it twice to sticky.", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar());
				}
			}

			@Override
			public VerticalAlignment getVerticalAlignment() {
				return VerticalAlignment.TOP;
			}

			@Override
			public HorizontalAlignment getHorizontalAlignment() {
				return HorizontalAlignment.LEFT;
			}
			
		});
		
		
	}

	
	@Override
	public String getId() {
		return "BTOPHELP";
	}

	@Override
	public String getTitle() {
		return Lng.str("Help");
	}

	@Override
	public boolean isDefaultExpanded(){
		return true;
	}

	@Override
	public int getSubListIndex() {
		return 0;
	}
	@Override
	public boolean isExpandable() {
		return false;
	}
	@Override
	public boolean isClosable() {
		return true;
	}
	@Override
	public void onClosed() {
		GUIResizableGrabbableWindow.setHidden(getWindowId(), true);
	}
}
