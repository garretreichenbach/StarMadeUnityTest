package org.schema.game.common.controller.elements.pulse;

import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.IntegrityBasedInterface;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.UsableControllableFiringElementManager;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

public abstract class PulseElementManager<
		E extends PulseUnit<E, CM, EM>,
		CM extends PulseCollectionManager<E, CM, EM>,
		EM extends PulseElementManager<E, CM, EM>>
		extends UsableControllableFiringElementManager<E, CM, EM> implements
		BlockActivationListenerInterface, IntegrityBasedInterface {

	public static boolean debug = false;
	private final ShootContainer shootContainer = new ShootContainer();
	public PulseElementManager(short controller, short controlling,
	                           SegmentController segmentController) {
		super(controller, controlling, segmentController);
	}
	
	public void doShot(E c, CM m,
	                    ShootContainer shootContainer, PlayerState playerState, Timer timer) {

		ManagerModuleCollection<?, ?, ?> effectModuleCollection = null;

		m.setEffectTotal(0);
		if (m.getEffectConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getEffectConnectedElement());
			effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);

			ControlBlockElementCollectionManager<?, ?, ?> effect = CombinationAddOn.getEffect(m.getEffectConnectedElement(), effectModuleCollection, getSegmentController());
			if (effect != null) {
				m.setEffectTotal(effect.getTotalSize());
			}
		}


		if (c.canUse(System.currentTimeMillis(), false)) {
			if (isUsingPowerReactors() || consumePower(c.getPowerConsumption() )) {
				Transform t = new Transform();
				t.setIdentity();
				t.origin.set(shootContainer.weapontOutputWorldPos);

				c.setStandardShotReloading();

				Vector3f dir = new Vector3f(shootContainer.shootingDirTemp);
				dir.normalize();
				long weaponId = m.getUsableId();
				addSinglePulse(c, dir, t, weaponId, m.getColor());

				handleResponse(ShootingRespose.FIRED, c, shootContainer.weapontOutputWorldPos);
			} else {
				handleResponse(ShootingRespose.NO_POWER, c, shootContainer.weapontOutputWorldPos);
			}
		} else {
			handleResponse(ShootingRespose.RELOADING, c, shootContainer.weapontOutputWorldPos);
		}
	}

	public void addSinglePulse(PulseUnit c, Vector3f dir, Transform t, long weaponId,  Vector4f pulseColore) {
		addSinglePulse(t, dir, c.getPulsePower(), c.getRadius(), weaponId, pulseColore);
	}

	public abstract void addSinglePulse(Transform t, Vector3f dir, float pulsePower, float radius,
			long weaponId, Vector4f pulseColor);

	
	
	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		//		System.err.println("WEAPON CONTROLLER ON ACTIVATE ON "+getSegmentController()+"; ON "+getSegmentController().getState());
		long absPos = piece.getAbsoluteIndex();
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			for (PulseUnit d : getCollectionManagers().get(i).getElementCollections()) {
				if (d.contains(absPos)) {
					d.setMainPiece(piece, active);

					return active ? 1 : 0;
				}
			}
		}
		return active ? 1 : 0;
	}
	//	@Override
	//	public void handleSingleActivation(SegmentPiece controller){
	//		if(getCollectionManagers().isEmpty()){
	//			if(debug){
	//				System.err.println("NO WEAPONS");
	//			}
	//			//nothing to shoot with
	//			return;
	//		}
	//		int unpowered = 0;
	//		if(getPowerManager().getPower() <= 0.5f && clientIsOwnShip()){
	//		}
	//		if(debug){
	//			System.err.println("FIREING CONTROLLERS: "+getState()+", "+getCollectionManagers().size()+" FROM: "+controlledFrom);
	//		}
	//		long time = System.currentTimeMillis();
	//		for(int i = 0; i < getCollectionManagers().size(); i++){
	//			CM m = getCollectionManagers().get(i);
	//			boolean selected = controller.equalsPos(m.getControllerPos());
	//			if(debug){
	//				System.err.println("SELECTED:: "+selected+" "+getState());
	//			}
	//			if(selected){
	//				for(int u = 0; u < m.getCollection().size(); u++){
	//					E c =  m.getCollection().get(u);
	//					if(debug){
	//						System.err.println("CAN SHOOT: "+c.canUse(time)+"; "+getPowerManager().getPower());
	//					}
	//					if(c.canUse(time) && consumePower(c.getPowerConsumption())){
	//						if(debug){
	//							System.err.println("2CAN SHOOT: "+c.canUse(time)+"; "+getPowerManager().getPower()+": "+getState()+": "+getSegmentController().getSectorId()+";; "+getSegmentController().getPhysics());
	//						}
	//
	//						Vector3i v = c.getOutput();
	//
	//						Vector3f weapontOutputWorldPos = new Vector3f(
	//								v.x - SegmentData.SEG_HALF,
	//								v.y - SegmentData.SEG_HALF,
	//								v.z - SegmentData.SEG_HALF);
	//						if(debug){
	//							System.err.println("WEAPON OUTPUT ON "+getState()+" -> "+v+"");
	//						}
	//						if(getSegmentController().isOnServer()){
	//							getSegmentController().getWorldTransform().transform(weapontOutputWorldPos);
	//						}else{
	//							getSegmentController().getWorldTransformClient().transform(weapontOutputWorldPos);
	//						}
	//
	//
	//
	//						c.updateLastShoot();
	//
	//
	//						//							getParticleController().addProjectile(getSegmentController(), weapontOutputWorldPos, shootingDirTemp, c.getDamage(), c.getDistance());
	//
	//						Transform t = new Transform();
	//						t.setIdentity();
	//						t.origin.set(weapontOutputWorldPos);
	//
	//						short effectType = 0;
	//						float effectRatio = 0;
	//						float effectSize = 0;
	//
	//						addSinglePulse(c, t, effectType, effectRatio, effectSize);
	//
	//
	//
	//						if(!getSegmentController().isOnServer()){
	//							Controller.queueTransformableAudio("0022_spaceship user - laser gun single fire small", t, 0.99f);
	//						}
	//						handleResponse(ShootingRespose.FIRED, c, weapontOutputWorldPos);
	//					}else{
	//					}
	//				}
	//				if(m.getCollection().isEmpty() && clientIsOwnShip()){
	//					((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nNo Weapons connected \nto entry point",0);
	//				}
	//			}
	//		}
	//		if(unpowered > 0 && clientIsOwnShip()){
	//			((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nWeapon Elements unpowered: "+unpowered,0);
	//		}
	//		if(getCollectionManagers().isEmpty() && clientIsOwnShip()){
	//			((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nNo weapon controllers",0);
	//		}
	//
	//	}

	@Override
	public String getManagerName() {
		return Lng.str("Pulse System Collective");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableControllableElementManager#getCollectionManagers()
	 */
	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		//		if(!getSegmentController().isOnServer()){
		//			debug = Keyboard.isKeyDown(GLFW.GLFW_KEY_NUMPAD1);
		//		}
		if (!unit.isFlightControllerActive()) {
			if (debug) {
				System.err.println("NOT ACTIVE");
			}
			return;
		}
		if (getCollectionManagers().isEmpty()) {
			if (debug) {
				System.err.println("NO WEAPONS");
			}
			//nothing to shoot with
			return;
		}
		try {
			if (!convertDeligateControls(unit, shootContainer.controlledFromOrig, shootContainer.controlledFrom)) {
				if (debug) {
					System.err.println("NO SLOT");
				}
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		long time = System.currentTimeMillis();
		int unpowered = 0;
		getPowerManager().sendNoPowerHitEffectIfNeeded();
		if (debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + shootContainer.controlledFrom);
		}
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			CM m = getCollectionManagers().get(i);
			if (unit.isSelected(m.getControllerElement(), shootContainer.controlledFrom)) {
				boolean controlling = shootContainer.controlledFromOrig.equals(shootContainer.controlledFrom);
				controlling |= getControlElementMap().isControlling(shootContainer.controlledFromOrig, m.getControllerPos(), controllerId);
				if (debug) {
					System.err.println("Controlling " + controlling + " " + getState());
				}

				if (controlling) {
					if(!m.allowedOnServerLimit()){
						continue;
					}
					if (shootContainer.controlledFromOrig.equals(Ship.core)) {
						unit.getControlledFrom(shootContainer.controlledFromOrig);
					}
					if (debug) {
						System.err.println("Controlling " + controlling + " " + getState() + ": " + m.getElementCollections().size());
					}
					for (int u = 0; u < m.getElementCollections().size(); u++) {
						E c = m.getElementCollections().get(u);

						Vector3i v = c.getOutput();

						shootContainer.weapontOutputWorldPos.set(
								v.x - SegmentData.SEG_HALF,
								v.y - SegmentData.SEG_HALF,
								v.z - SegmentData.SEG_HALF);
						if (debug) {
							System.err.println("WEAPON OUTPUT ON " + getState() + " -> " + v + "");
						}
						if (getSegmentController().isOnServer()) {
							getSegmentController().getWorldTransform().transform(shootContainer.weapontOutputWorldPos);
						} else {
							getSegmentController().getWorldTransformOnClient().transform(shootContainer.weapontOutputWorldPos);
						}

						shootContainer.centeralizedControlledFromPos.set(shootContainer.controlledFromOrig);
						shootContainer.centeralizedControlledFromPos.sub(Ship.core);


						shootContainer.camPos.set(getSegmentController().getAbsoluteElementWorldPosition(shootContainer.centeralizedControlledFromPos, shootContainer.tmpCampPos));

						
						boolean focus = false;
						boolean lead = false;
						unit.getShootingDir(getSegmentController(), shootContainer, c.getDistanceFull(), 1, m.getControllerPos(), focus, lead);

						shootContainer.shootingDirTemp.normalize();

						doShot(c, m, shootContainer, unit.getPlayerState(), timer);

						//							getParticleController().addProjectile(getSegmentController(), weapontOutputWorldPos, shootingDirTemp, c.getDamage(), c.getDistance());

					}
					if (m.getElementCollections().isEmpty() && clientIsOwnShip()) {
						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Weapons connected \nto entry point"), 0);
					}
				}
			}
		}
		if (unpowered > 0 && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nWeapon Elements unpowered: %s",  unpowered), 0);
		}
		if (getCollectionManagers().isEmpty() && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo weapon controllers."), 0);
		}

	}
}
