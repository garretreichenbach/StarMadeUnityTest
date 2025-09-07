package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedBuildModeDocking extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeDocking(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		addButton(pane.getContent(0), 0, 0, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						if(!getBuildToolsManager().isInCreateDockingMode()){
							getBuildToolsManager().startCreateDockingMode();
						}else{
							getBuildToolsManager().cancelCreateDockingMode();
						}						
					}
				};
			}
			
			@Override
			public String getName() {
				return getBuildToolsManager().getCreateDockingModeMsg();
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		
	}

	
	@Override
	public String getId() {
		return "BDOCKING";
	}

	@Override
	public String getTitle() {
		return Lng.str("Docking");
	}




}
