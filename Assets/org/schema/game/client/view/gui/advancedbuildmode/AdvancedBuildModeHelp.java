package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.KeyboardMappings;

public class AdvancedBuildModeHelp extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeHelp(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Press middle(wheel) mouse button\nwhile holding %s to put the selected\nblock type on the hotbar", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar());
			}
		});
		pane.addNewTextBox(UIScale.getUIScale().scale(30));
		addLabel(pane.getContent(1), 0, 0, new LabelResult() {
			
			@Override
			public String getName() {
				return Lng.str("Press middle(wheel) mouse button\nwithout holding %s to switch to the\ndocked structure you're looking",KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar());
			}
		});
		
//		pane.addNewTextBox(UIScale.getUIScale().scale(30));
//		addLabel(pane.getContent(2), 0, 0, new LabelResult() {
//			
//			@Override
//			public String getName() {
//				return Lng.str("Use the cursor Keys to cycle\nthrough docked structures");
//			}
//		});
	}

	
	@Override
	public String getId() {
		return "BHELP";
	}

	@Override
	public String getTitle() {
		return Lng.str("Help & Tips");
	}




}
