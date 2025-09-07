package org.schema.game.client.view.gui.shiphud;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.TopBarInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import com.bulletphysics.dynamics.RigidBody;

public class TopBar extends GUIElement implements TopBarInterface {
	Vector3i posFromSector = new Vector3i();
	private GUITextOverlay playerCredits;
	private GUITextOverlay playerShipInfo;
	private GUITextOverlay playerShipSpeed;
	private GUIOverlay topBar;
	private GUITextOverlay mail;

	public TopBar(InputState state) {
		super(state);
		topBar = new GUIOverlay(Controller.getResLoader().getSprite("top-bar-gui-"), state);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (needsReOrientation()) {
			doOrientation();
		}
		GlUtil.glPushMatrix();

		transform();
		topBar.draw();

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {

		mail = new GUITextOverlay(FontSize.TINY_12, getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#draw()
			 */
			@Override
			public void draw() {
				if (((GameClientState) getState()).getController().getClientChannel().getPlayerMessageController().hasUnreadMessages()) {
					super.draw();
				}
			}

		};
		mail.setText(new ArrayList(1));
		mail.getText().add("New Mail (F4)");
		mail.getPos().x = 240;
		mail.getPos().y = 70;

//		playerCredits = new GUITextOverlay(300, 40, FontLibrary.getBoldDotrice18(), getState());
//		playerCredits.setText(new ArrayList<Object>(1));
//		playerCredits.getText().add("0");
//		playerCredits.getPos().x = 240;
//		playerCredits.getPos().y = 34;

		playerShipInfo = new GUITextOverlay(FontSize.TINY_11, getState());
		playerShipInfo.setText(new ArrayList(1));
		playerShipInfo.getText().add("");
		playerShipInfo.getText().add("");
		playerShipInfo.getPos().x = 398;
		playerShipInfo.getPos().y = 30;

//		playerShipSpeed = new GUITextOverlay(300, 40, FontLibrary.getBoldDotrice18(), getState());
//		playerShipSpeed.setText(new ArrayList<Object>(1));
//		playerShipSpeed.getText().add("0");
//		playerShipSpeed.getPos().x = 676;
//		playerShipSpeed.getPos().y = 34;

		topBar.attach(playerCredits);
		topBar.attach(playerShipSpeed);
		topBar.attach(playerShipInfo);
		topBar.attach(mail);

	}

	@Override
	protected void doOrientation() {
		topBar.orientate(GUIElement.ORIENTATION_TOP | GUIElement.ORIENTATION_HORIZONTAL_MIDDLE);
	}

	@Override
	public float getHeight() {
		return 64;
	}

	@Override
	public float getWidth() {
		return 768;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public void updateCreditsAndSpeed() {
		GameClientState state = ((GameClientState) getState());
		long credits = state.getPlayer().getCredits();
		playerCredits.getText().set(0, String.valueOf(credits));
		SimpleTransformableSendableObject currentPlayerObject = state.getCurrentPlayerObject();
		boolean success = false;
		try {

			if (currentPlayerObject != null) {
				if (currentPlayerObject.getPhysicsDataContainer().isInitialized()) {
					if (currentPlayerObject.getPhysicsDataContainer().getObject() instanceof RigidBody) {
						RigidBody b = (RigidBody) currentPlayerObject.getPhysicsDataContainer().getObject();
						Vector3f vector3f = new Vector3f();
						b.getLinearVelocity(vector3f);
						success = true;
						playerShipSpeed.getText().set(0, StringTools.formatPointZero(vector3f.length()));
					}
				}
				Vector3i vector = state.getCurrentRemoteSector().getNetworkObject().pos.getVector();
				String sector = Sector.isTutorialSector(vector) ? "Tutorial" : (vector.x + ", " + vector.y + ", " + vector.z);
				if (state.getCurrentClientSystem() == null) {
					playerShipInfo.getText().set(1, "Sector: " + sector + " [Sys: scanning...]");
				} else {
					VoidSystem sys = state.getCurrentClientSystem();
					String system = Sector.isTutorialSector(vector) ? "Tutorial" : (sys.getPos().x + ", " + sys.getPos().y + ", " + sys.getPos().z);
					String f;

					if (sys.getOwnerFaction() == 0) {
						f = "";
					} else if (state.getFactionManager().existsFaction(sys.getOwnerFaction())) {
						f = " <" + state.getFactionManager().getFaction(sys.getOwnerFaction()).getName() + ">";
					} else {
						f = "<unknown(" + sys.getOwnerFaction() + ")>";
					}

					playerShipInfo.getText().set(1, "Sec: " + sector + "; Sys: " + system);
				}
				if (currentPlayerObject instanceof Ship) {
					Ship ship = (Ship) currentPlayerObject;

					if (((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
							.getPlayerGameControlManager().getPlayerIntercationManager()
							.getInShipControlManager().getShipControlManager()
							.getSegmentBuildController().isActive()) {
						playerShipInfo.getText().set(0, state.getPlayerName() + " in " + ship.getRealName());

					} else {
						playerShipInfo.getText().set(0, ship.getRealName());
					}
				} else {
					playerShipInfo.getText().set(0, state.getPlayerName());

				}

			}
		} catch (Exception e) {
		}
		if (!success) {
			playerShipSpeed.getText().set(0, "0");
		}
	}

	@Override
	public void notifyEffectHit(SimpleTransformableSendableObject obj,
	                            OffensiveEffects offensiveEffects) {
		
	}

}
