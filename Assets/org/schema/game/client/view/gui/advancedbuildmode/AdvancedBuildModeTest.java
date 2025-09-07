package org.schema.game.client.view.gui.advancedbuildmode;

import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.SliderCallback;
import org.schema.game.client.view.gui.advanced.tools.SliderResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;


public class AdvancedBuildModeTest extends AdvancedGUIElement{

	public AdvancedBuildModeTest(InputState state) {
		super(state);
	}

	@Override
	protected Vector2f getInitialPos() {
		return new Vector2f(600, 32);
	}

	@Override
	public void draw() {
		super.draw();
	}
	@Override
	protected int getScrollerHeight() {
		return GLFrame.getHeight()-128;
	}
	@Override
	protected int getScrollerWidth() {
		return 128*2+64;
	}
	@Override
	protected void addGroups(List<AdvancedGUIGroup> g) {
		g.add(new AdvancedGUIGroup(this) {
			
			@Override
			public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
				pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
				GUITextOverlay l2 = new GUITextOverlay(getState());
				addLabel(pane.getContent(0), 0, 0, new LabelResult() {
					@Override
					public String getName() {
						return Lng.str("Build Size X, Y, Z");
					}
				});
				addSlider(pane.getContent(0), 0, 1, new SliderResult() {
					
					@Override
					public SliderCallback initCallback() {
						return null;
					}
					
					@Override
					public String getName() {
						return "BLABLA";
					}
					
					@Override
					public float getMin() {
						return 0;
					}
					
					@Override
					public float getMax() {
												return 10;
					}
					
					@Override
					public float getDefault() {
												return 5;
					}
					@Override
					public String getToolTipText() {
						return "LDLDLDLDLDLDL\nksajfdhksafhjksahfkjh";
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


			@Override
			public void setInitialBackgroundColor(Vector4f bgColor) {
				bgColor.set(1,1,1,1);
			}

		});
	}

	@Override
	public boolean isSelected() {
		return false;
	}



	


}
