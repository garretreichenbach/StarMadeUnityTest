package org.schema.game.server.ai.program.common.states;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.AIGameEntityState;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

public abstract class GameState<E extends Sendable> extends State {

	/**
	 *
	 */
	
	protected static Transform serverTmp = new Transform();
	Vector3f dist = new Vector3f();

	public GameState(AiEntityStateInterface gObj) {
		super(gObj);
	}

	public static boolean isDockedOn(SegmentController a, SegmentController b) {
		return a.getDockingController().getDockedOn() != null && a.getDockingController().getDockedOn().to.getSegment().getSegmentController() == b;
	}
	
	public static boolean checkTarget(SimpleGameObject s, ShipGameState stat) {
		Ship self = stat.getEntity();
		Sector sector = ((GameServerState) stat.getEntity().getState()).getUniverse().getSector(s.getSectorId());
		if (sector != null && (sector.isProtected()
				|| sector.isPeace())) {

			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " dismissed " + s + " as target: sector protection or peace");
			}
			//do not target objects in protected sectors
			return false;
		}
		boolean isActiveAI = s instanceof AiInterface && ((AiInterface) s).getAiConfiguration().getAiEntityState().isActive();
		boolean targetSelectedEntityMode = stat.getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Selected Target");
		boolean hasAttachedPlayers = (s instanceof PlayerControllable && !((PlayerControllable) s).getAttachedPlayers().isEmpty());

		if (hasAttachedPlayers && ((PlayerControllable) s).getAttachedPlayers().get(0).isInvisibilityMode()) {
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " dismissed " + s + " as target: invisible");
			}
			return false;
		}
		if (!targetSelectedEntityMode && !(hasAttachedPlayers || isActiveAI || s instanceof Missile || s.getFactionId() == FactionManager.PIRATES_ID)) {
			//always attack pirates even if inactive
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE OF lack of attached players or not active AI " + stat.getEntity() + "; " + s);
			}
			return false;
		}

		if (targetSelectedEntityMode) {
			AbstractOwnerState sa = stat.getEntity().railController.getRoot().getOwnerState();
			if (sa != null && sa instanceof PlayerState) {
				PlayerState p = (PlayerState) sa;
				int selectedEntityId = p.getNetworkObject().selectedAITargetId.get();
				if (s.getAsTargetId() != selectedEntityId) {
					if (GameClientState.isDebugObject(self)) {
						System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE 'SELECTED TARGET' MODE AND SELECTED IS NOT THE TARGET. " + stat.getEntity() + "; " + s+"; "+s.getAsTargetId()+"; "+selectedEntityId);
					}
					return false;
				}
				Sendable sendable = stat.getEntity().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedEntityId);
				if (sendable == null || !(sendable instanceof SimpleTransformableSendableObject)) {
					if (GameClientState.isDebugObject(self)) {
						System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE DESELECTION WITH 'SELECTED TARGET' MODE. " + stat.getEntity() + "; " + s);
					}
					return false;
				}

			}
		}

		if (s instanceof SegmentController && (isDockedOn(((SegmentController) s), stat.getEntity()) || isDockedOn(self, ((SegmentController) s)))) {
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE OF DOCKED " + stat.getEntity() + "; " + s);
			}
			return false;
		}

		if (s instanceof Ship && ((Ship) s).isCloakedFor(self)) {
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE OF CLOACKED " + stat.getEntity() + "; " + s);
			}
			return false;
		}
		if (s instanceof Ship && ((Ship) s).isCoreOverheating()) {
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE OF CORE OVERHEATING " + stat.getEntity() + "; " + s);
			}
			return false;
		}
		if (s instanceof AbstractCharacter<?> && ((AbstractCharacter<?>) s).isHidden()) {
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE OF HIDDEN CHAR " + stat.getEntity() + "; " + s);
			}
			return false;
		}

		if (!targetSelectedEntityMode && s instanceof PlayerControllable && !(s instanceof AbstractCharacter<?>)) {
			if (((PlayerControllable) s).getAttachedPlayers().isEmpty() && !((AiInterface) s).getAiConfiguration().isActiveAI() && s.getFactionId() != FactionManager.PIRATES_ID) {
				System.err.println("[AI][TURRET] Dead Entity. Getting new Target");
				return false;
			}
		}

		
		s.calcWorldTransformRelative(self.getSectorId(), ((GameServerState) self.getState()).getUniverse().getSector(self.getSectorId()).pos);
		stat.dist.sub(s.getClientTransform().origin, (((SimpleTransformableSendableObject) stat.getEntityState().getEntity()).getWorldTransform().origin));

		if (s instanceof Ship && ((Ship) s).isJammingFor(self) && stat.dist.length() > stat.getEntityState().getShootingRange() - 100) {
			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " TARGET FAILED BECAUSE OF TOO FAR AND JAMMING " + stat.getEntity() + "; " + s + "; " + stat.dist + "/" + stat.getEntityState().getShootingRange() / 2);
			}
			return false;

		}
		if (stat.dist.length() > stat.getEntityState().getShootingRange()) {

			if (GameClientState.isDebugObject(self)) {
				System.err.println("[AI][DEBUG][CHECK] " + stat.getEntity() + " dismissed " + s + " as target: out of range: dist: " + stat.dist.length() + " / range: " + stat.getEntityState().getShootingRange());
			}
//							System.err.println("[AI][ENGAGE] not in range for "+getEntity()+" -> "+((TargetProgram<?>)getEntityState().getCurrentProgram()).getTarget());
			return false;
		}
		return true;
	}

	public AIGameConfiguration<?, ?> getAIConfig() {
		return (AIGameConfiguration<?, ?>) ((AiInterface) getEntityState().getEntity()).getAiConfiguration();
	}

	@SuppressWarnings("unchecked")
	public E getEntity() {
		return ((AIGameEntityState<E>) super.getEntityState()).getEntity();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.State#getGObj()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AIGameEntityState<E> getEntityState() {
		return (AIGameEntityState<E>) super.getEntityState();
	}
}
