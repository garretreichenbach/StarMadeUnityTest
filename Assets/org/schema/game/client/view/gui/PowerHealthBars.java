package org.schema.game.client.view.gui;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;

public class PowerHealthBars extends GUIElement {

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	int x = 0;
	int y = 0;
	private GUIOverlay healthBarFrame;
	private GUIOverlay healthBar;
	private GUIOverlay powerBarFrame;
	private GUIOverlay powerBar;
	private GUITextOverlay shieldNerf;
	private GUITextOverlay powerCons;
	private SinusTimerUtil sinusTime;
	private float distance = 32;
	private boolean recovery;
	private GUIOverlay shieldBar;
	private GUIOverlay shieldBarFrame;

	public PowerHealthBars(GameClientState state) {
		super(state);

		sinusTime = new SinusTimerUtil();

		String sprite = "powerbar-1x4-gui-";

		healthBar = new GUIOverlay(Controller.getResLoader().getSprite(sprite), getState());
		healthBar.setSpriteSubIndex(0);
		healthBar.setPos(66, -11, 0);

		healthBarFrame = new GUIOverlay(Controller.getResLoader().getSprite(sprite), getState());
		healthBarFrame.setSpriteSubIndex(2);
		healthBarFrame.setPos(66, -11, 0);

		shieldBar = new GUIOverlay(Controller.getResLoader().getSprite(sprite), getState());
		shieldBar.setSpriteSubIndex(2);
		shieldBar.setPos(66, -11, 0);

		shieldBarFrame = new GUIOverlay(Controller.getResLoader().getSprite(sprite), getState());
		shieldBarFrame.setSpriteSubIndex(2);
		shieldBarFrame.setPos(66, -11, 0);

		powerBar = new GUIOverlay(Controller.getResLoader().getSprite(sprite), getState());
		powerBar.setSpriteSubIndex(1);
		powerBar.setPos(549, -11, 0);

		powerBarFrame = new GUIOverlay(Controller.getResLoader().getSprite(sprite), getState());
		powerBarFrame.setSpriteSubIndex(3);
		powerBarFrame.setPos(549, -11, 0);

		if (Controller.getResLoader().getSprite(sprite).getTint() == null) {
			Controller.getResLoader().getSprite(sprite).setTint(new javax.vecmath.Vector4f(1, 1, 1, 1));
		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		GlUtil.glPushMatrix();
		transform();

		GameClientState state = (GameClientState) getState();
		healthBarFrame.getSprite().getTint().w = 0;
		powerBarFrame.getSprite().getTint().w = 0;
		healthBarFrame.draw();

		healthBar.getSprite().getTint().set(1, 1, 1, 1);
		{
			float wPercent = 1f - state.getPlayer().getHealth() / PlayerState.MAX_HEALTH;

			Vector4f clip = new Vector4f(
					healthBar.getPos().x + wPercent * 402f,
					healthBar.getPos().x + 402,

					healthBar.getPos().y,
					healthBar.getPos().y + healthBar.getHeight());

			healthBar.drawClipped(clip);

		}

		if (state.getShip() != null) {
			Ship ship = state.getShip();

			shieldBar.getSprite().getTint().set(1, 1, 1, 1);
			float sPercent = (float) (1f - ship.getManagerContainer().getShieldAddOn().getShields() / ship.getManagerContainer().getShieldAddOn().getShieldCapacity());
			Vector4f sclip = new Vector4f(
					shieldBar.getPos().x + sPercent * 402f,
					shieldBar.getPos().x + 402,

					shieldBar.getPos().y,
					shieldBar.getPos().y + shieldBar.getHeight());
			shieldBar.drawClipped(sclip);

			powerBarFrame.getSprite().getTint().w = 0;
			if (recovery != ship.getManagerContainer().getPowerAddOn().isInRecovery()) {
				recovery = ship.getManagerContainer().getPowerAddOn().isInRecovery();
				//				sinusTime.reset();
			}
			if (recovery || ship.getManagerContainer().getPowerAddOn().getPowerRailed() <= 0) {
				powerBarFrame.getSprite().getTint().x = 1;
				powerBarFrame.getSprite().getTint().y = 0;
				powerBarFrame.getSprite().getTint().z = 0;
				powerBarFrame.getSprite().getTint().w = sinusTime.getTime() * 0.3f;
			}
			powerBarFrame.draw();

			powerBar.getSprite().getTint().set(1, 1, 1, 1);
			//*0.785f is the factor the picture is shortened (402px / 512px)
			powerBar.drawClipped((float) ship.getManagerContainer().getPowerAddOn().getPower() * 0.785f, (float) ship.getManagerContainer().getPowerAddOn().getMaxPower(), 1, 1);

			if (ship.getManagerContainer().getPowerAddOn().getPowerConsumedPerSecond() > 0) {
				double pcSec = ship.getManagerContainer().getPowerAddOn().getPowerConsumedPerSecond();
				powerCons.getPos().set(powerBar.getPos());
				powerCons.getPos().y += 115;
				powerCons.getPos().x += 200;
				String s = "Cons: " + StringTools.formatPointZero(pcSec) + "/sec";
				if (ship.getManagerContainer().getPowerAddOn().isInRecovery()) {
					s += "   Outage Recovery!";
				}
				powerCons.getText().set(0, s);
				powerCons.draw();
			}

			//			System.err.println("SHIELDS: "+ship.getManagerContainer().getShieldAddOn().getShields() );
			if (ship.getManagerContainer().getShieldAddOn().getShieldCapacity() > 0) {

				double pc = (ship.getManagerContainer().getShieldAddOn().getShields() / ship.getManagerContainer().getShieldAddOn().getShieldCapacity()) * 100;
				shieldNerf.getPos().set(shieldBar.getPos());
				shieldNerf.getPos().y += 115;
				shieldNerf.getPos().x += 10;
				double dirRecovery = ship.getManagerContainer().getShieldAddOn().getRecovery();
				double outRecovery = ship.getManagerContainer().getShieldAddOn().getRecoveryOut();
				shieldNerf.getText().set(0, "Shield: " + (int) Math.ceil(pc) + "%     "
						+ "Recharge: " + (int) Math.ceil(ship.getManagerContainer().getShieldAddOn().getNerf() * 100) + "% " + (dirRecovery > 0 ? "(under fire)" : "     "
						+ (outRecovery > 0 ? "Recovery: " + StringTools.formatPointZero(outRecovery) + "sec" : "")));
				shieldNerf.draw();
			}
		}

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {

		healthBarFrame.onInit();
		healthBar.onInit();
		powerBarFrame.onInit();
		powerBar.onInit();
		shieldNerf = new GUITextOverlay(FontSize.TINY_12, getState());
		shieldNerf.onInit();
		shieldNerf.setTextSimple("ShieldSystems: 100%");

		powerCons = new GUITextOverlay(FontSize.TINY_12, getState());
		powerCons.onInit();
		powerCons.setTextSimple("Power Consumption: 0");
	}

	@Override
	public float getHeight() {
		return healthBarFrame.getHeight();
	}

	@Override
	public float getWidth() {
		return healthBarFrame.getWidth() * 2 + distance;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public boolean isExternalShipActive() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActive();
	}

	@Override
	public void update(Timer timer) {
		sinusTime.update(timer);
	}

}
