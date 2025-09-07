package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedBuildModeBrushSize extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeBrushSize(AdvancedGUIElement e) {
		super(e);
	}
	
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		GUITextOverlay l2 = new GUITextOverlay(getState());
		
		//pane.setTint(0, 1f, 0f, 0f, 1f);//index, color rgba
		
		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Build Size X, Y, Z");
			}
		});
		addSlider(pane.getContent(0), 0, 1, new SizeSliderResult(getBuildToolsManager().width){
			@Override
			public String getToolTipText() {
				return Lng.str("changes width of building area\nTIP: use scroll wheel on number\nTIP: right click to reset");
			}
			@Override
			public long getToolTipDelayMs() {
				return 800;
			}
		});
		addSlider(pane.getContent(0), 0, 2, new SizeSliderResult(getBuildToolsManager().height){
			@Override
			public String getToolTipText() {
				return Lng.str("changes height of building area\nTIP: use scroll wheel on number\nTIP: right click to reset");
			}
			@Override
			public long getToolTipDelayMs() {
				return 800;
			}
		});
		addSlider(pane.getContent(0), 0, 3, new SizeSliderResult(getBuildToolsManager().depth){
			@Override
			public String getToolTipText() {
				return Lng.str("changes depth of building area\nTIP: use scroll wheel on number\nTIP: right click to reset");
			}

			@Override
			public long getToolTipDelayMs() {
				return 800;
			}
			
		});
		
		
	}

	
	@Override
	public String getId() {
		return "BSIZE";
	}

	@Override
	public String getTitle() {
		return Lng.str("Brush Size");
	}




}
