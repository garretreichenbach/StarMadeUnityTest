package org.schema.game.client.view.camera.drone;

import org.schema.game.common.data.player.BuildModePosition;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;


public class CameraDrone{

	public final BuildModePosition bp;
	public final GUITextOverlay nameText;
	
	
	public CameraDrone(PlayerState player) {
		this.bp = player.getBuildModePosition();
		nameText = new GUITextOverlay(FontSize.BIG_30, (InputState)bp.getState());
		nameText.setTextSimple(player.getName());
	}
	

	public void draw(Mesh droneMesh) {
		if(!bp.isDisplayed()) {
			return;
		}
		
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(bp.getWorldTransform());
		GlUtil.glPushMatrix();
		GlUtil.rotateModelview(-90, 1, 0, 0);
		droneMesh.renderVBO();
		GlUtil.glPopMatrix();
		
		if(!bp.isClientOwnPlayer() && bp.isDrawNames()) {
			GlUtil.glTranslatef(-0.44f, -0.36f, 0.48f);
			GlUtil.scaleModelview(0.01f, -0.01f, -0.01f);
			nameText.doDepthTest = true;
			nameText.draw();
		}
		
		GlUtil.glPopMatrix();
	}


}
