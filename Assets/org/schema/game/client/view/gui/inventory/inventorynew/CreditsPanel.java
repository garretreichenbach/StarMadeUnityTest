package org.schema.game.client.view.gui.inventory.inventorynew;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;

public class CreditsPanel extends GUIElement implements GUICallback {

	private static Vector4f cWhite = new Vector4f(1f, 1f, 1f, 1f);
	private static Vector4f cGrey = new Vector4f(0.38f, 0.38f, 0.38f, 1);
	boolean rightDependentHalf = false;
	boolean leftDependentHalf = false;
	GUIHorizontalArea creditsBg;
	GUITextOverlayTable credits;
	GUIOverlay creditsTakeButton;
	private GUIElement dependend;

	public CreditsPanel(InputState state, GUIElement dependend) {
		super(state);
		this.dependend = dependend;

	}

	@Override
	public void cleanUp() {
		if (creditsBg != null) {
			creditsBg.cleanUp();
		}
		if (credits != null) {
			credits.cleanUp();
		}
		if (creditsTakeButton != null) {
			creditsTakeButton.cleanUp();
		}
	}

	@Override
	public void draw() {

		int dWidth = (int) dependend.getWidth();
		int dPos = 0;
		if (leftDependentHalf) {
			dWidth = (int) dependend.getWidth() / 2;
		} else if (rightDependentHalf) {
			dWidth = (int) dependend.getWidth() / 2;
			dPos = ((int) dependend.getWidth()) - dWidth;
		}

		GlUtil.glPushMatrix();
		transform();

		GlUtil.translateModelview(dPos, 0, 0);

		creditsBg.setWidth(dWidth);
		creditsTakeButton.setPos(dWidth - (creditsTakeButton.getWidth() + 4), (int) (creditsBg.getHeight() / 2 - creditsTakeButton.getHeight() / 2), 0);
		if (creditsTakeButton.isInside()) {
			creditsTakeButton.setSpriteSubIndex(24);
		} else {
			creditsTakeButton.setSpriteSubIndex(23);
		}

		creditsBg.draw();

		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {
		creditsBg = new GUIHorizontalArea(getState(), HButtonType.TEXT_FILED_LIGHT, 10);

		credits = new GUITextOverlayTable(getState());

		credits.setPos(6, 5, 0);

		credits.setTextSimple(new Object() {

			@Override
			public String toString() {
				return Lng.str("Credits: %d", ((GameClientState) getState()).getPlayer().getCredits());
			}

		});
		creditsBg.attach(credits);

		creditsTakeButton = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 16px-8x8-gui-"), getState()) {
			@Override
			public void draw() {

				super.draw();
				getSprite().getTint().set(cWhite);
			}

		};
		creditsTakeButton.setSpriteSubIndex(23);

		if (creditsTakeButton.getSprite().getTint() == null) {
			creditsTakeButton.getSprite().setTint(new Vector4f(1, 1, 1, 1));
		}

		creditsTakeButton.setMouseUpdateEnabled(true);
		creditsTakeButton.setCallback(this);

		creditsBg.attach(creditsTakeButton);

	}

	@Override
	public float getHeight() {
		return creditsBg.getHeight();
	}

	@Override
	public float getWidth() {
		return creditsBg.getWidth();
	}

	public PlayerGameControlManager getPlayerGameControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	public InventoryControllerManager getInventoryControlManager() {
		return getPlayerGameControlManager().getInventoryControlManager();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			getInventoryControlManager().openDropCreditsDialog(0);
		}
	}

	@Override
	public boolean isOccluded() {
		return !dependend.isActive();
	}

}
