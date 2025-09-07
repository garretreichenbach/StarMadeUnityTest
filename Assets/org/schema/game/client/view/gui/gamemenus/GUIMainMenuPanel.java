package org.schema.game.client.view.gui.gamemenus;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.GameResourceLoader.StandardButtons;
import org.schema.game.client.view.gui.GUIButton;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.InputState;

@Deprecated
public class GUIMainMenuPanel extends GUIElement implements GUIActiveInterface {
	private GUIButton buttonResume;
	private GUIButton buttonExit;
	private GUIButton buttonSuicide;
	private GUIButton buttonOptions;
	private GUIButton buttonExitToWindows;

	private GUITextOverlay infoText;
	private GUITextOverlay errorText;
	private GUIOverlay background;

	private long timeError;
	private long timeErrorShowed;
	private GUICallback guiCallback;
	private String info;
	private GUIElement close;
	private boolean firstDraw = true;
	private GUIButton messageLog;
	private GUITextButton buttonTutorials;

	public GUIMainMenuPanel(InputState state, GUICallback guiCallback, String info) {
		super(state);
		this.guiCallback = guiCallback;
		this.info = info;
		close = new GUIAnchor(getState(), 39, 26);
		close.setCallback(guiCallback);
		close.setUserPointer("X");
		close.setMouseUpdateEnabled(true);
	}

	@Override
	public void cleanUp() {
		if (background != null) {
			background.cleanUp();
		}
		if (infoText != null) {
			infoText.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		buttonSuicide.setInvisible(!((GameClientState) getState()).isPlayerSpawned());
		GlUtil.glPushMatrix();
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		transform();
		if (timeError < System.currentTimeMillis() - timeErrorShowed) {
			errorText.getText().clear();
		}
		background.draw();

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {

		infoText = new GUITextOverlay(FontSize.BIG_24, getState());

		errorText = new GUITextOverlay(getState());
		background = new GUIOverlay(Controller.getResLoader().getSprite("menu-panel-gui-"), getState());

		buttonTutorials = new GUITextButton(getState(), 155, 30, FontSize.BIG_20, new Object() {

			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				if (((GameClientState) getState()).getPlayer() != null && ((GameClientState) getState()).getPlayer().isInTestSector()) {
					return "LEAVE TEST SECTOR";
				} if (((GameClientState) getState()).getPlayer() != null && ((GameClientState) getState()).getPlayer().isInTutorial()) {
					return "EXIT TUTORIAL";
				} else {
					return "  View Tutorials";
				}
			}

		}, guiCallback);
		buttonTutorials.setUserPointer("TUTORIAL");

		buttonResume = new GUIButton(
				Controller.getResLoader().getSprite("buttons-8x8-gui-"),
				getState(),
				StandardButtons.RESUME_BUTTON, "RESUME", guiCallback);

		buttonExit = new GUIButton(
				Controller.getResLoader().getSprite("buttons-8x8-gui-"),
				getState(),
				StandardButtons.EXIT_BUTTON, "EXIT", guiCallback);

		buttonExitToWindows = new GUIButton(
				Controller.getResLoader().getSprite("buttons-8x8-gui-"),
				getState(),
				StandardButtons.EXIT_TO_WINDOWS_BUTTON, "EXIT_TO_WINDOWS", guiCallback);

		buttonOptions = new GUIButton(
				Controller.getResLoader().getSprite("buttons-8x8-gui-"),
				getState(),
				StandardButtons.OPTIONS_BUTTON, "OPTIONS", guiCallback);

		buttonSuicide = new GUIButton(
				Controller.getResLoader().getSprite("buttons-8x8-gui-"),
				getState(),
				StandardButtons.SUICIDE_BUTTON, "RESPAWN", guiCallback);

		messageLog = new GUIButton(
				Controller.getResLoader().getSprite("buttons-8x8-gui-"),
				getState(),
				StandardButtons.MESSAGE_LOG_BUTTOM, "MESSAGELOG", guiCallback);

		ArrayList<Object> t = new ArrayList<Object>();
		t.add(info);
		infoText.setText(t);

		ArrayList<Object> te = new ArrayList<Object>();
		errorText.setText(te);
		background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);

		background.onInit();
		infoText.onInit();
		buttonResume.onInit();
		buttonExit.onInit();
		messageLog.onInit();

		this.attach(background);

		background.attach(buttonResume);
		//		TODO reactivate if there is an external main menu
		//		background.attach(buttonExit);
		background.attach(buttonSuicide);
		buttonSuicide.setInvisible(true);
		background.attach(buttonOptions);
		background.attach(messageLog);
		background.attach(buttonTutorials);
		background.attach(buttonExitToWindows);

		background.attach(errorText);
		background.attach(infoText);
		close.setPos(216, 0, 0);
		background.attach(close);

		infoText.setPos(50, 10, 0);
		errorText.setPos(40, 30, 0);

		int pos = 1;
		int sep = 12;
		int height = 64;
		height += sep;
		int x = 55;

		buttonResume.setPos(x, (pos++) * height, 0);
		buttonSuicide.setPos(x, (pos++) * height, 0);
		buttonOptions.setPos(x, (pos++) * height, 0);
		//		buttonExit.setPos(x, (pos++)*height, 0);
		messageLog.setPos(x, (pos++) * height, 0);
		buttonExitToWindows.setPos(x, (pos++) * height, 0);

		buttonTutorials.setPos(40, buttonExitToWindows.getPos().y + 77, 0);

		firstDraw = false;
	}

	@Override
	public float getHeight() {
		return 256;
	}

	@Override
	public float getWidth() {
		return 256;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public void setErrorMessage(String msg, long timeShowed) {
		errorText.getText().add(msg);
		timeError = System.currentTimeMillis();
		timeErrorShowed = timeShowed;
	}

}
