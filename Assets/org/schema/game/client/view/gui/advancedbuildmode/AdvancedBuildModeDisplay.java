package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.CheckboxCallback;
import org.schema.game.client.view.gui.advanced.tools.CheckboxResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedBuildModeDisplay extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeDisplay(AdvancedGUIElement e) {
		super(e);
	}
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
		int y = 0;
		addCheckbox(pane.getContent(0), 0, y, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return Lng.str("Buildlight");
			}
			@Override
			public void setCurrentValue(boolean b) {
				getState().getPlayer().getBuildModePosition().setFlashlightOnClient(b);				
			}
			@Override
			public boolean getDefault() {
				return getState().getPlayer().getBuildModePosition().isFlashlightOn();
			}
			@Override
			public boolean getCurrentValue() {
				return getState().getPlayer().getBuildModePosition().isFlashlightOn();
			}
			
			@Override
			public String getToolTipText() {
				return Lng.str("Flashlight for easier\nbuilding in dark areas");
			}
			
		});
		addCheckbox(pane.getContent(0), 1, y++, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Displ. Self";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getState().getPlayer().getBuildModePosition().setOwnVisible(b);				
			}
			@Override
			public boolean getDefault() {
				return getState().getPlayer().getBuildModePosition().isOwnVisible();
			}
			@Override
			public boolean getCurrentValue() {
				return getState().getPlayer().getBuildModePosition().isOwnVisible();
			}
			
			@Override
			public String getToolTipText() {
				return Lng.str("Display own camera drone");
			}
			
		});
		addCheckbox(pane.getContent(0), 0, y, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Lighten Mode";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().lighten = b;				
			}
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().lighten;
			}
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().lighten;
			}
			
			@Override
			public String getToolTipText() {
				return Lng.str("Lighten up blocks for easier\nbuilding in dark areas");
			}
			
		});
		addCheckbox(pane.getContent(0), 1, y++, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Remove Mode";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().add = !b;				
			}
			@Override
			public boolean getDefault() {
				return !getBuildToolsManager().add;
			}
			@Override
			public boolean getCurrentValue() {
				return !getBuildToolsManager().add;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Changes view of build box to display\nselected block instead of the area next to it");
			}
		});
		addCheckbox(pane.getContent(0), 0, y, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Structure Info";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().structureInfo = b;				
			}
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().structureInfo;
			}
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().structureInfo;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Displays structure information at the left side");
			}
		});
		addCheckbox(pane.getContent(0), 1, y++, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Block Info";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().buildInfo = b;				
			}
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().buildInfo;
			}
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().buildInfo;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Displays additional block information");
			}
		});
		addCheckbox(pane.getContent(0), 0, y, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Reactor Hull Info";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().reactorHull = b;				
			}
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().reactorHull;
			}
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().reactorHull;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Displays reactor convex hull");
			}
		});
		addCheckbox(pane.getContent(0), 1, y++, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Center of Mass";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().showCenterOfMass = b;				
			}
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().showCenterOfMass;
			}
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().showCenterOfMass;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Displays indicator where\nthe center of mass is");
			}
		});
		addCheckbox(pane.getContent(0), 0, y, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Connection Box";
			}
			@Override
			public void setCurrentValue(boolean b) {
				EngineSettings.G_DRAW_SELECTED_BLOCK_WOBBLE_ALWAYS.setOn(b);				
			}
			@Override
			public boolean getDefault() {
				return EngineSettings.G_DRAW_SELECTED_BLOCK_WOBBLE_ALWAYS.isOn();
			}
			@Override
			public boolean getCurrentValue() {
				return EngineSettings.G_DRAW_SELECTED_BLOCK_WOBBLE_ALWAYS.isOn();
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Displays a box around connected blocks (old style)");
			}
		});
		addCheckbox(pane.getContent(0), 1, y++, new CheckboxResult() {
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			@Override
			public String getName() {
				return "Drone Names";
			}
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().setCameraDroneDisplayName(b);
			}
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().isCameraDroneDisplayName();
			}
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().isCameraDroneDisplayName();
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Displays names for drones");
			}
		});
		
	}

	
	@Override
	public String getId() {
		return "BDISPLAY";
	}

	@Override
	public String getTitle() {
		return Lng.str("Display");
	}




}
