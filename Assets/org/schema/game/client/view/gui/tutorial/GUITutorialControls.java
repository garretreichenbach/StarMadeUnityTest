package org.schema.game.client.view.gui.tutorial;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUITutorialControls extends GUIAnchor implements GUICallback {

	private GUITextOverlay currentTutorialPart;

	private GUITextOverlay skipTutorialPart;

	private GUITextOverlay repeatTutorialPart;

	private GUITextOverlay skipTutorialStep;

	private GUITextOverlay endTutorial;

	private GUITextOverlay resetTutorial;

	private boolean init;

	public GUITutorialControls(InputState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (getTutorial() != null) {
			if (event.pressedLeftMouse()) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
				AudioController.fireAudioEventID(726);
				if (callingGuiElement == skipTutorialPart) {
					getTutorial().endStep();
				} else if (callingGuiElement == repeatTutorialPart) {
					getTutorial().repeatStep();
				} else if (callingGuiElement == skipTutorialStep) {
					getTutorial().skip();
				} else if (callingGuiElement == endTutorial) {
					getTutorial().end();
				} else if (callingGuiElement == resetTutorial) {
					getTutorial().repeat();
				}
			}
		}
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		super.draw();
	}

	@Override
	public void onInit() {
		FontInterface f = FontSize.SMALL_13;
		int width = UIScale.getUIScale().scale(120);
		int height = UIScale.getUIScale().scale(20);
		int textHeight = UIScale.getUIScale().scale(30);
		currentTutorialPart = new GUITextOverlay(FontSize.SMALL_15, getState());
		skipTutorialPart = new GUITextOverlay(f, getState());
		repeatTutorialPart = new GUITextOverlay(f, getState());
		skipTutorialStep = new GUITextOverlay(f, getState());
		endTutorial = new GUITextOverlay(f, getState());
		resetTutorial = new GUITextOverlay(f, getState());
		skipTutorialPart.setCallback(this);
		repeatTutorialPart.setCallback(this);
		skipTutorialStep.setCallback(this);
		endTutorial.setCallback(this);
		resetTutorial.setCallback(this);
		skipTutorialPart.setMouseUpdateEnabled(true);
		repeatTutorialPart.setMouseUpdateEnabled(true);
		skipTutorialStep.setMouseUpdateEnabled(true);
		endTutorial.setMouseUpdateEnabled(true);
		resetTutorial.setMouseUpdateEnabled(true);
		currentTutorialPart.setText(new ArrayList(1));
		skipTutorialPart.setText(new ArrayList(1));
		repeatTutorialPart.setText(new ArrayList(1));
		skipTutorialStep.setText(new ArrayList(1));
		endTutorial.setText(new ArrayList(1));
		resetTutorial.setText(new ArrayList(1));
		currentTutorialPart.getText().add("Tutorial Controls");
		skipTutorialPart.getText().add("Skip Part");
		repeatTutorialPart.getText().add("Repeat Part");
		skipTutorialStep.getText().add("skip current step");
		endTutorial.getText().add("end tutorial");
		resetTutorial.getText().add("reset tutorial");
		Vector4f color = new Vector4f(0, 0, 0, 0.7f);
		GUIColoredRectangle currentTutorialPartR = new GUIColoredRectangle(getState(), width, height, color);
		GUIColoredRectangle skipTutorialPartR = new GUIColoredRectangle(getState(), width, height, color);
		GUIColoredRectangle repeatTutorialPartR = new GUIColoredRectangle(getState(), width, height, color);
		GUIColoredRectangle skipTutorialStepR = new GUIColoredRectangle(getState(), width, height, color);
		GUIColoredRectangle endTutorialR = new GUIColoredRectangle(getState(), width, height, color);
		GUIColoredRectangle resetTutorialR = new GUIColoredRectangle(getState(), width, height, color);
		skipTutorialStepR.getPos().set(0, UIScale.getUIScale().scale(30), 0);
		repeatTutorialPartR.getPos().set(0, UIScale.getUIScale().scale(60), 0);
		skipTutorialPartR.getPos().set(UIScale.getUIScale().scale(120), UIScale.getUIScale().scale(60), 0);
		resetTutorialR.getPos().set(0, UIScale.getUIScale().scale(90), 0);
		endTutorialR.getPos().set(UIScale.getUIScale().scale(120), UIScale.getUIScale().scale(90), 0);
		currentTutorialPartR.attach(currentTutorialPart);
		skipTutorialPartR.attach(skipTutorialPart);
		repeatTutorialPartR.attach(repeatTutorialPart);
		skipTutorialStepR.attach(skipTutorialStep);
		endTutorialR.attach(endTutorial);
		resetTutorialR.attach(resetTutorial);
		this.attach(currentTutorialPartR);
		this.attach(skipTutorialPartR);
		this.attach(repeatTutorialPartR);
		this.attach(skipTutorialStepR);
		this.attach(endTutorialR);
		this.attach(resetTutorialR);
		init = true;
	}

	@Override
	public float getHeight() {
		return UIScale.getUIScale().scale(180);
	}

	@Override
	public float getWidth() {
		return UIScale.getUIScale().scale(260);
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public TutorialMode getTutorial() {
		return ((GameClientState) getState()).getController().getTutorialMode();
	}
}
