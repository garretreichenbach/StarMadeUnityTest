package org.schema.game.client.view.gui;


import javax.vecmath.Vector4f;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class RadialMenuItemBlock extends RadialMenuItem<GUIBlockSprite>{

	private short type;

	public RadialMenuItemBlock(InputState state, RadialMenu m, int index, short type, final GUIActivationCallback activationCallback, final GUICallback callback) {
		super(state, m, index, activationCallback, callback);
		this.type = type;
		
		
	}

	@Override
	protected void setColorAndPos(GUIBlockSprite label, float x, float y, Vector4f colorCurrent) {
		
		assert(colorCurrent != null);
		assert(label != null);
		assert(label.getColor() != null);
		
		label.getColor().w = colorCurrent.w;
		
		label.setPos(
				(int)(getCenterX() + x - label.getWidth() / 2), 
				(int)(getCenterY() + y - label.getHeight()/2), 0); 
//		label.setColor(m.textColor);		
	}

	@Override
	public GUIBlockSprite getLabel() {
		GUIBlockSprite label = new GUIBlockSprite(getState(), type);
		GUITextOverlay l = new GUITextOverlay(FontSize.MEDIUM_15, getState()){

			@Override
			public void draw() {
				if(RadialMenuItemBlock.this.isInside()){
					super.draw();
				}
			}
			
		};
		l.setTextSimple(ElementKeyMap.getInfo(type).getName());
		l.setPos(UIScale.getUIScale().scale(-30), (int)label.getHeight()+UIScale.getUIScale().scale(3), 0);
		
		label.attach(l);
		return label;
	}
	
}
