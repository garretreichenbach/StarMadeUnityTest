package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.io.File;
import java.io.IOException;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.InputState;

public class GUIMovieWindow extends GUIDialogWindow{

	private final GUIMoviePlayer player;
	
	public GUIMovieWindow(InputState state, int initialWidth,
			int initialHeight, String windowId, File movie, int initalPosX, int initialPosY, Texture splash, GUIActiveInterface activeInterface) throws IOException {
		super(state, initialWidth, initialHeight, initalPosX, initialPosY, windowId);
		
		this.activeInterface = activeInterface;
		
		onInit();
		
		
		player = new GUIMoviePlayer(state, movie, getMainContentPane().getContent(0), activeInterface, true);
		getMainContentPane().getContent(0).attach(player);
	}
	public void setLooping(boolean b){
		player.setLooping(b);
	}
	
	@Override
	public void cleanUp() {
		player.cleanUp();
		super.cleanUp();
		getState().setActiveSubtitles(null);
	}
	
	@Override
	public void draw() {
		super.draw();
		
		getState().setActiveSubtitles(player.getActiveSubtitles());
	}
	public void setExtraPanel(AddTextBoxInterface x) {
		if(getMainContentPane().getTextboxes().size() == 1){
			getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		}
		getMainContentPane().setListDetailMode(0, getMainContentPane().getTextboxes().get(0));
		getMainContentPane().addNewTextBox(x.getHeight());
		GUIAnchor content = getMainContentPane().getContent(getMainContentPane().getTextboxes().size()-1);
		GUIElement c = x.createAndAttach(content);
		if(!content.getChilds().contains(c)){
			content.attach(c);
		}
	}
	public GUIMoviePlayer getPlayer() {
		return player;
	}
	
}
