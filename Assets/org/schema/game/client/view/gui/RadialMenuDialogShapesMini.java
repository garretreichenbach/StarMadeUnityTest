package org.schema.game.client.view.gui;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;

public class RadialMenuDialogShapesMini extends RadialMenuDialog implements GUIActivationCallback{

	private final ElementInformation info;
	private short currentSelectedType;
	public RadialMenuDialogShapesMini(GameClientState state, ElementInformation info) {
		super(state);
		this.info = info;
		assert(this.info.blocktypeIds != null):this.info;
	}

	@Override
	public RadialMenu createMenu(RadialMenuDialog radialMenuDialog) {
		currentSelectedType = getState().getGlobalGameControlManager()
				.getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager().getSelectedTypeWithSub();
		final GameClientState s = getState();
		
		RadialMenu m = new RadialMenu(s, "ShapesRadialMini", radialMenuDialog, UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(10), FontSize.SMALL_13){

			@Override
			public void draw() {
				super.draw();
				if(!Mouse.isPrimaryMouseDownUtility()){
					((GameClientState)getState()).getGlobalGameControlManager()
					.getIngameControlManager().getPlayerGameControlManager()
					.getPlayerIntercationManager().selectTypeForced(currentSelectedType);
					deactivate();
				}
//				System.err.println("Mouse: "+Mouse.getX()+", "+Mouse.getY());
			}
			
		};
		m.setForceBackButton(false);
		m.posElem = new GUIElement(s) {
			
			@Override
			public void onInit() {
			}
			
			@Override
			public void draw() {
			}
			
			@Override
			public void cleanUp() {
			}
			
			@Override
			public float getWidth() {
				return 0;
			}
			
			@Override
			public float getHeight() {
				return 0;
			}
		};
		m.posElem.setPos(Mouse.getX(), Mouse.getY(), 0);
		assert(this.info.blocktypeIds != null):this.info;
		
		
		for(int i = -1; i < this.info.blocktypeIds.length; i++){
			final short type;
			if(i < 0){
				type = info.id;
			}else{
				type = this.info.blocktypeIds[i];
			}
			ElementInformation sin = ElementKeyMap.getInfo(type);
			
			m.addItemBlock(type, new GUICallback() {
				
				
				@Override
				public boolean isOccluded() {
					return !RadialMenuDialogShapesMini.this.isActive(s);
				}
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					currentSelectedType = type;
						
				}
			}, Lng.str(""), new GUIActivationHighlightCallback() {
				
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}
				
				@Override
				public boolean isActive(InputState state) {
					return RadialMenuDialogShapesMini.this.isActive();
				}
				
				@Override
				public boolean isHighlighted(InputState state) {
					return true;
				}
			});
			
		}
		
		
		return m;
	}
		
	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if ((isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE))) {
			deactivate();
		}
		if(!Mouse.isPrimaryMouseDownUtility()){
			deactivate();
		}
	}
	

	public PlayerGameControlManager getPGCM(){
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}
	@Override
	public boolean isVisible(InputState state) {
		return true;
	}

	@Override
	public boolean isActive(InputState state) {
		return super.isActive();
	}

	

}
