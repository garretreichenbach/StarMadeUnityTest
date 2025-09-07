package org.schema.game.client.view.gui;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.GUIFadingElement;
import org.schema.game.client.controller.GUIMessageDialog;
import org.schema.game.client.view.GameResourceLoader.StandardButtons;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.DraggableAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUIMessagePanel extends GUIFadingElement {

	private static DraggableAnchor draggableAncor;
	private GUIOverlay background;
	private GUIButton ok;
	private GUIButton cancel;
	private GUIScrollablePanel scrollPanel;
	private boolean init;
	private GUIMessageDialog dialog;
	private String message;
	private float fade = 0;
	private GUITextOverlay text;
	private GUITextOverlay title;
	private GUITextOverlay titleDrag;
	private Vector4f blend;
	private String titleString;
	private boolean withCancel;

	public GUIMessagePanel(InputState state, GUIMessageDialog dialog, String message, String titleString, boolean withCancel) {
		super(state);
		this.dialog = dialog;
		this.titleString = titleString;
		this.withCancel = withCancel;
		background = new GUIOverlay(Controller.getResLoader().getSprite("info-panel-gui-"), state);
		scrollPanel = new GUIScrollablePanel(363, 110, state);
		this.message = message;
		blend = new Vector4f(1, 1, 1, 1.0f - fade);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		//		if(!((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isActive()){
		//			setPos(20, 180, 0);
		//		}else{

		//		}

		GlUtil.glPushMatrix();
		transform();
		//		GlUtil.glEnable(GL11.GL_BLEND);
		//		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		blend.set(1, 1, 1, 1.0f - fade);
		ok.getSprite().setTint(blend);
		background.getSprite().setTint(blend);
		text.setColor(new Color(blend.x, blend.y, blend.z, blend.w));

		background.draw();

		ok.getSprite().setTint(null);
		background.getSprite().setTint(null);
		text.setColor(new Color(1, 1, 1, 1));
		//		GlUtil.glDisable(GL11.GL_BLEND);
		//		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {

		text = new GUITextOverlay(FontSize.SMALL_13, getState());
		text.setText(new ArrayList());
		text.getText().add(message);

		title = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		title.setTextSimple(titleString);
		titleDrag = new GUITextOverlay(FontSize.TINY_11, getState());
		titleDrag.setTextSimple(Lng.str("(click to drag)"));

		ok = new GUIButton(Controller.getResLoader().getSprite("buttons-8x8-gui-"), getState(),
				StandardButtons.OK_BUTTON, "OK", dialog);
		cancel = new GUIButton(Controller.getResLoader().getSprite("buttons-8x8-gui-"), getState(),
				StandardButtons.CANCEL_BUTTON, "CANCEL", dialog);

		if (draggableAncor == null) {
			draggableAncor = new DraggableAnchor(getState(), 380, 40, background);
			background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		} else {
			background.getPos().set(draggableAncor.getAffected().getPos());
			draggableAncor = new DraggableAnchor(getState(), 380, 40, background);
		}

		scrollPanel.setContent(text);

		background.attach(title);
		background.attach(titleDrag);
		background.attach(scrollPanel);
		background.attach(ok);
		if (withCancel) {
			background.attach(cancel);
		}
		background.attach(draggableAncor);

		title.setPos(14, 10, 0);
		titleDrag.setPos(280, 15, 0);

		scrollPanel.setPos(23, 42, 0);

		float scale = 0.5f;
		ok.setScale(scale, scale, scale);
		cancel.setScale(scale, scale, scale);

		ok.setPos(330, 158, 0);
		cancel.setPos(220, 158, 0);

		//		this.attach(background);

		init = true;
	}

	@Override
	public float getHeight() {
		return background.getHeight();
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	@Override
	public void setFade(float val) {
		this.fade = val;
	}

}
