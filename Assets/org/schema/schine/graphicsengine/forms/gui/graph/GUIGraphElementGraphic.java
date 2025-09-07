package org.schema.schine.graphicsengine.forms.gui.graph;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangleOutline;
import org.schema.schine.graphicsengine.forms.gui.GUIResizableElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.TooltipProviderCallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIFilledArea;
import org.schema.schine.input.InputState;

public class GUIGraphElementGraphic extends GUIResizableElement implements TooltipProviderCallback{

	public final GUIGraphElementGraphicsContainer c;
	private final GUIGraphElementBackground background;
	private final GUITextOverlay textOverlay;

	private int width;
	private int height;
	private boolean init;
	private GUIColoredRectangleOutline selectionOutline;
	public GUIGraphConnection connectionToParent;
	private GUIToolTip toolTip;
	
	public GUIGraphElementGraphic(InputState state, GUIGraphElementGraphicsContainer c) {
		super(state);
		this.c = c;
		this.background = c.initiateBackground();
		this.textOverlay = new GUITextOverlay(state);
		background.attach(textOverlay);
		setCallback(c.getSelectionCallback());
		selectionOutline = new GUIColoredRectangleOutline(getState(), 10, 10, 4, new Vector4f(1,1,1,1));
		textOverlay.setTextSimple(new Object(){
			@Override
			public String toString() {
				return GUIGraphElementGraphic.this.c.getText();
			}
			
		});
		if(EngineSettings.DRAW_TOOL_TIPS.isOn()){
		toolTip = new GUIToolTip(state, new Object(){
			@Override
			public String toString() {
				return GUIGraphElementGraphic.this.c.getToolTipText();
			}
			
		}, this);
		}
		setMouseUpdateEnabled(true);
	}
	public void doFormating() {
		textOverlay.setFont(c.getFontSize());
		textOverlay.setColor(c.getTextColor());
		c.setBackgroundColor(background);
		
		
		textOverlay.updateTextSize();
		textOverlay.setPos(c.getTextOffsetX(), c.getTextOffsetY()+4, 0);
		
		width = c.getBackgroundWidth(textOverlay.getMaxLineWidth());
		height = c.getBackgroundHeight(textOverlay.getTextHeight());
		
		
		
		background.setWidth(width);
		background.setHeight(height);
		
		selectionOutline.setWidth(width);
		selectionOutline.setHeight(height);
		
		
		if(connectionToParent != null){
			c.setConnectionColor(connectionToParent);
		}
	}
	

	@Override
	public void draw() {
		if(!init){
			onInit();
		}
		
		if (isInvisible()) {
			return;
		}
		
		doFormating();
		
		GlUtil.glPushMatrix();

		setInside(false);

		transform();

		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		background.draw();
		if(c.isSelected()){
			selectionOutline.draw();
		}
		c.drawExtra(width, height);
		for (AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();
	}

	
	@Override
	public void onInit() {
		if(init){
			return;
		}
		init = true;
	}

	@Override
	public void setWidth(float width) {
		this.width = (int)width;
	}

	@Override
	public void setHeight(float height) {
		this.height = (int)height;
	}
	public GUIFilledArea getBackground(){
		return background;
	}
	@Override
	public void cleanUp() {
		background.cleanUp();
		textOverlay.cleanUp();
		selectionOutline.cleanUp();
	}
	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}
	@Override
	public GUIToolTip getToolTip() {
		return toolTip;
	}
	@Override
	public void setToolTip(GUIToolTip toolTip) {
		this.toolTip = toolTip;
	}
	

}
