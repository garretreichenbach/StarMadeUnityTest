package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.input.InputState;

public class GUIAdvButton extends GUIAdvTool<ButtonResult>{

	private GUIHorizontalButton button;
	
	public GUIAdvButton(InputState state, GUIElement dependent, ButtonResult r) {
		super(state, dependent, r);
		
		GUICallback callback = new GUICallback() {
			
			@Override
			public boolean isOccluded() {
				return !GUIAdvButton.this.isActive();
			}
			
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()){
					getRes().leftClick();
				}
				if(event.pressedRightMouse()){
					getRes().rightClick();
				}
			}
		};
		
		button = new GUIHorizontalButton(state, getRes().getColor(), new Object(){
			@Override
			public String toString(){
				return getRes().getName();
			}
		}, callback, GUIAdvButton.this::isActive, r.getActCallback()){

			@Override
			public void draw() {
				setColor(getRes().getColor());
				super.draw();
			}
			
		};

		
		
		attach(button);
	}
	
	
	@Override
	public void draw() {
		button.setWidth(getWidth());
		super.draw();
	}




	@Override
	public int getElementHeight() {
		return (int) button.getHeight();
	}
}
