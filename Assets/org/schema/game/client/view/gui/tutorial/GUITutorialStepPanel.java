package org.schema.game.client.view.gui.tutorial;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.GUIFadingElement;
import org.schema.game.client.controller.tutorial.TutorialDialog;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.controller.tutorial.states.TimedState;
import org.schema.game.client.controller.tutorial.states.TutorialEndedTextState;
import org.schema.game.client.controller.tutorial.states.WaitingTextState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.GameResourceLoader.StandardButtons;
import org.schema.game.client.view.gui.GUIButton;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.DraggableAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUITutorialStepPanel extends GUIFadingElement {

	private static DraggableAnchor draggableAncor;

	private GUIOverlay background;

	private GUIButton next;

	private GUIButton back;

	private GUIButton endTutorial;

	private boolean init;

	private TutorialDialog dialog;

	private GUIButton skip;

	private String message;

	private float fade = 0;

	private GUITextOverlay text;

	private GUITextOverlay title;

	private GUITextOverlay titleDrag;

	private Vector4f blend;

	private GUITextOverlay doNotShowAgain;

	private GUITutorialCheckBox doNotShowAgainBox;

	private Sprite image;

	public GUITutorialStepPanel(InputState state, TutorialDialog dialog, String message, Sprite image) {
		super(state);
		this.dialog = dialog;
		background = new GUIOverlay(Controller.getResLoader().getSprite("info-panel-gui-"), state);
		this.message = message;
		while (this.message.startsWith("\n")) {
			this.message = this.message.substring(1);
		}
		blend = new Vector4f(1, 1, 1, 1.0f - fade);
		this.image = image;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		// if(!((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isActive()){
		// setPos(20, 180, 0);
		// }else{
		// }
		GlUtil.glPushMatrix();
		transform();
		// GlUtil.glEnable(GL11.GL_BLEND);
		// GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		// GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		blend.set(1, 1, 1, 1.0f - fade);
		next.getSprite().setTint(blend);
		back.getSprite().setTint(blend);
		skip.getSprite().setTint(blend);
		endTutorial.getSprite().setTint(blend);
		background.getSprite().setTint(blend);
		text.setColor(new Color(blend.x, blend.y, blend.z, blend.w));
		background.draw();
		next.getSprite().setTint(null);
		back.getSprite().setTint(null);
		skip.getSprite().setTint(null);
		endTutorial.getSprite().setTint(null);
		background.getSprite().setTint(null);
		text.setColor(new Color(1, 1, 1, 1));
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		doNotShowAgain = new GUITextOverlay(FontSize.TINY_11, getState());
		doNotShowAgainBox = new GUITutorialCheckBox(getState());
		text = new GUITextOverlay(FontSize.SMALL_13, getState());
		text.setText(new ArrayList());
		text.getText().add(message);
		title = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		title.setText(new ArrayList());
		title.getText().add("Tutorial");
		titleDrag = new GUITextOverlay(FontSize.TINY_11, getState());
		titleDrag.setText(new ArrayList());
		titleDrag.getText().add("(click to drag)");
		next = new GUIButton(Controller.getResLoader().getSprite("buttons-8x8-gui-"), getState(), StandardButtons.NEXT_BUTTON, "NEXT", dialog);
		back = new GUIButton(Controller.getResLoader().getSprite("buttons-8x8-gui-"), getState(), StandardButtons.BACK_BUTTON, "BACK", dialog) {

			/* (non-Javadoc)
			 * @see org.schema.game.client.view.gui.GUIButton#draw()
			 */
			@Override
			public void draw() {
				GameClientState s = (GameClientState) getState();
				TutorialMode tutorialMode = s.getController().getTutorialMode();
				if (tutorialMode.hasBack()) {
					super.draw();
				}
			}
		};
		skip = new GUIButton(Controller.getResLoader().getSprite("buttons-8x8-gui-"), getState(), StandardButtons.SKIP_BUTTON, "SKIP", dialog);
		endTutorial = new GUIButton(Controller.getResLoader().getSprite("buttons-8x8-gui-"), getState(), StandardButtons.END_TUTORIAL_BUTTON, "END", dialog);
		if (draggableAncor == null) {
			draggableAncor = new DraggableAnchor(getState(), UIScale.getUIScale().scale(380), UIScale.getUIScale().scale(40), background);
			background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
			background.getPos().y -= UIScale.getUIScale().scale(100);
		} else {
			background.getPos().set(draggableAncor.getAffected().getPos());
			draggableAncor = new DraggableAnchor(getState(), UIScale.getUIScale().scale(380), UIScale.getUIScale().scale(40), background);
		}
		background.attach(title);
		background.attach(titleDrag);
		background.attach(text);
		background.attach(next);
		// if we need a skip button ever
		// background.attach(skip);
		background.attach(draggableAncor);
		if (image != null) {
			GUIOverlay imgOverlay = new GUIOverlay(image, getState());
			imgOverlay.setPos(400, 20, 0);
			background.attach(imgOverlay);
		}
		if (dialog.getCondition() instanceof TutorialEndedTextState) {
			doNotShowAgain.setTextSimple("Show tutorial next time");
			background.attach(doNotShowAgain);
			background.attach(doNotShowAgainBox);
			doNotShowAgain.setPos(133, 165, 0);
			doNotShowAgainBox.setPos(98, 156, 0);
		} else {
			background.attach(back);
			if ((((GameClientState) getState()).getPlayer().isInTutorial())) {
				endTutorial.setCallback(new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
							AudioController.fireAudioEventID(727);
							((GameClientState) getState()).getGlobalGameControlManager().openExitTutorialPanel(dialog);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				});
			} else {
				endTutorial.setCallback(dialog);
			}
			background.attach(endTutorial);
		}
		title.setPos(14, 10, 0);
		titleDrag.setPos(280, 15, 0);
		text.setPos(26, 50, 0);
		float scale = 0.5f;
		next.setScale(scale, scale, scale);
		back.setScale(scale, scale, scale);
		skip.setScale(scale, scale, scale);
		endTutorial.setScale(scale, scale, scale);
		next.setPos(330, 158, 0);
		back.setPos(250, 158, 0);
		skip.setPos(110, 158, 0);
		endTutorial.setPos(30, 158, 0);
		// this.attach(background);
		skip.setInvisible(true);
		if (dialog.getCondition() instanceof WaitingTextState || !(dialog.getCondition() instanceof TimedState)) {
			skip.setInvisible(false);
		}
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
