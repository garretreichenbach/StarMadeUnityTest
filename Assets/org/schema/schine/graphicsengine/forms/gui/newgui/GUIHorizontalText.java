package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.ColoredInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUIHorizontalText extends GUIAbstractHorizontalArea {

	

	private GUITextOverlay overlay;
	
	private Vector3i sizeHelp;
	private boolean initHelp;


	private ColoredInterface colorIface;

	private int orientation = ORIENTATION_HORIZONTAL_MIDDLE;

	private void init(Object text, FontInterface size, ColoredInterface colorIface) {
		sizeHelp = new Vector3i();
		this.colorIface = colorIface;
		overlay = new GUITextOverlay(size, getState()) {
			@Override
			public void onDirty() {
				sizeHelp.x = getFont().getWidth(getText().get(0).toString());
			}

		};
		overlay.setTextSimple(text);

		setMouseUpdateEnabled(true);

		GUIScrollablePanel lr = new GUIScrollablePanel(this.getWidth(), this.getHeight(), this, getState());
		lr.setScrollable(0);
		lr.setLeftRightClipOnly = true;
		lr.setContent(overlay);
		attach(lr);
	}

	public GUIHorizontalText(InputState state, Object text, FontInterface size, ColoredInterface colorIface) {
		super(state);
		init(text, size, colorIface);
	}

	public String getTextToString() {
		return overlay.getText().toString();
	}

	@Override
	public void cleanUp() {
		overlay.cleanUp();
	}

	@Override
	public void draw() {
		if (isInside()) {
		}
		if (actCallback == null || actCallback.isVisible(getState())) {
			if(!initHelp){
				sizeHelp.x = overlay.getFont().getWidth(overlay.getText().get(0).toString());
				initHelp = true;
			}
			if(colorIface != null){
				overlay.setColor(colorIface.getColor());
			}else{
				overlay.setColor(1,1,1,1);
			}
			if((orientation & ORIENTATION_LEFT) == ORIENTATION_LEFT){
				overlay.setPos(4, UIScale.getUIScale().inset, 0);
			}else if((orientation & ORIENTATION_RIGHT) == ORIENTATION_RIGHT){
				overlay.setPos(getWidth() - sizeHelp.x-4, UIScale.getUIScale().inset, 0);
			}else{
				overlay.setPos((int) (getWidth() / 2 - sizeHelp.x / 2), UIScale.getUIScale().inset, 0);
			}
			drawAttached();
		}
	}
	
	public void setAlign(int orientation){
		this.orientation = orientation;
	}

	@Override
	public void onInit() {
		
	}

	@Override
	protected void adjustWidth() {
		
	}

	@Override
	public void setHeight(float height) {
		
	}

	@Override
	public float getHeight() {
		return UIScale.getUIScale().h;
	}


}
