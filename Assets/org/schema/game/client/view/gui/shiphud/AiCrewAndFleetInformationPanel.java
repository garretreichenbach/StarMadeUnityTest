package org.schema.game.client.view.gui.shiphud;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.controller.ai.UnloadedAiEntityException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AiCrewAndFleetInformationPanel extends GUIAnchor {
	Vector3i posFromSector = new Vector3i();
	private GUITextOverlay playerShipInfo;
	private long lastUpdate;

	public AiCrewAndFleetInformationPanel(InputState state) {
		super(state, 500, 150);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (System.currentTimeMillis() - lastUpdate > 50) {
			update();
			lastUpdate = System.currentTimeMillis();
		}
		super.draw();
	}

	@Override
	public void onInit() {

		playerShipInfo = new GUITextOverlay(FontSize.TINY_11, getState());
		playerShipInfo.setText(new ObjectArrayList<>(10));
		playerShipInfo.getPos().x = 0;
		playerShipInfo.getPos().y = 0;

		attach(playerShipInfo);

	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public void update() {
		playerShipInfo.getText().clear();
		GameClientState state = ((GameClientState) getState());
		long credits = state.getPlayer().getCredits();

		PlayerInteractionControlManager pc = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		SimpleTransformableSendableObject currentPlayerObject = state.getCurrentPlayerObject();

		if (state.getPlayer().getPlayerAiManager().hasCrew()) {

			if (KeyboardMappings.CREW_CONTROL.isDown()) {
				if (pc.getSelectedCrew() != null) {
					try {
						playerShipInfo.getText().add("selected: " + pc.getSelectedCrew().getRealName());
						playerShipInfo.getText().add("1: idle");
						playerShipInfo.getText().add("2: attack");
						playerShipInfo.getText().add("3: roam");
						playerShipInfo.getText().add("4: follow");
						playerShipInfo.getText().add("5: go to");
						playerShipInfo.getText().add("6: deselect");
					} catch (UnloadedAiEntityException e) {
						playerShipInfo.getText().add("ERROR: entity not loaded");
					}
				} else {
					List<AiInterfaceContainer> crew = state.getPlayer().getPlayerAiManager().getCrew();
					for (int i = 0; i < crew.size(); i++) {
						try {
							playerShipInfo.getText().add("select " + (i + 1) + " " + crew.get(i).getRealName());
						} catch (UnloadedAiEntityException e) {
							playerShipInfo.getText().add("select " + (i + 1) + " " + crew.get(i).getUID() + "(UNLOADED)");
						}
					}
				}

			}
		}

		//		SimpleTransformableSendableObject currentPlayerObject = state.getCurrentPlayerObject();
		//		boolean success = false;
		//		try{
		//
		//			if(currentPlayerObject != null){
		//				if(currentPlayerObject instanceof ManagedSegmentController<?>){
		//
		//					SegmentController segController = (SegmentController)currentPlayerObject;
		//					ManagerContainer<?> c = ((ManagedSegmentController<?>)currentPlayerObject).getManagerContainer();
		//
		//					String mass = "Mass: "+StringTools.formatPointZero(segController.getMass())+" (Blocks: "+segController.getTotalElements()+")";
		//
		//					Vector3f f = new Vector3f();
		//					f.sub(segController.getBoundingBox().max, segController.getBoundingBox().min);
		//
		//					String dimension = "Length: "+(int)f.z+"m, Height: "+(int)f.y+"m; Width: "+(int)f.x+"m";
		//
		//
		//					String thrust = "Thrust: none";
		//					if(c instanceof ManagerThrustInterface){
		//						thrust = "Thrust: "+StringTools.formatPointZero(((ManagerThrustInterface)c).getThrust().getCollectionManager().getTotalThrust());
		//					}
		//
		//					String shields = "Shields: none";
		//
		//					if(c instanceof ShieldContainerInterface){
		//						shields = "Shields: "+StringTools.formatPointZero(((ShieldContainerInterface)c).getShieldManager().getShields())
		//								+"/"+
		//								((ShieldContainerInterface)c).getShieldManager().getShieldCapabilityHP()
		//								+" ("+
		//								((ShieldContainerInterface)c).getShieldManager().getShieldRechargeRate()+" s/sec); Blocks: "+((ShieldContainerInterface)c).getShieldManager().getShieldBlocks()+"; Recovery: "+((ShieldContainerInterface)c).getShieldManager().getRecoveryTime();
		//					}
		//
		//					String power = "Power: none";
		//
		//					if(c instanceof PowerManagerInterface){
		//						PowerManagerInterface p = (PowerManagerInterface)c;
		//						power = "Power: "+StringTools.formatPointZero(p.getPowerAddOn().getPower())+"/"+
		//								StringTools.formatPointZero(p.getPowerAddOn().getMaxPower())+" ("+
		//								StringTools.formatPointZero(p.getPowerAddOn().getRecharge())+" e/sec)";
		//					}
		//					String turningSpeed = "TurningSpeed: static";
		//					if(segController instanceof Ship ){
		//						Vector3f oF = ((Ship)segController).getOrientationForce();
		//						turningSpeed = "TurningSpeed: X-Axis: "+StringTools.formatPointZero(1f+oF.x)+", Y-Axis: "+StringTools.formatPointZero(1f+oF.y)+", Z-Axis: "+StringTools.formatPointZero(1f+(oF.z/Ship.ROLL_EXTRA));
		//					}
		//
		//					playerShipInfo.getText().add(mass);
		//					playerShipInfo.getText().add(dimension);
		//					playerShipInfo.getText().add(power);
		//					playerShipInfo.getText().add(thrust);
		//					playerShipInfo.getText().add(turningSpeed);
		//					playerShipInfo.getText().add(shields);
		//
		//				}
		//			}
		//		}catch (Exception e) {
		//		}

	}

}
