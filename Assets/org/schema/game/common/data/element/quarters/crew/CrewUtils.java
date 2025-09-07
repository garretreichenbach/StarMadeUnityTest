package org.schema.game.common.data.element.quarters.crew;

import api.common.GameCommon;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.QuarterManager;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.objects.Sendable;

import java.util.Locale;
import java.util.Random;

/**
 * Crew Utility class.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class CrewUtils {

	public static void addCrew(PlayerState playerState, String name) {
		QuarterManager quarterManager = getQuarterManager(playerState);
		if(quarterManager != null) {
			CrewMember crewMember = new CrewMember(SimpleTransformableSendableObject.EntityType.NPC.dbPrefix + name + "_" + System.currentTimeMillis(), name, playerState.getState());
			crewMember.randomizeStats();
			playerState.getPlayerAiManager().addAI(crewMember);
			crewMember.spawn(playerState);
			(new StarRunnable() {
				@Override
				public void run() {
					crewMember.recall();
				}
			}).runLater(1000);
		}
	}

	public static void removeCrew(PlayerState playerState, String name) {
		QuarterManager quarterManager = getQuarterManager(playerState);
		if(quarterManager != null) {
			CrewMember crewMember = playerState.getPlayerAiManager().getCrewByName(name);
			if(crewMember != null) playerState.getPlayerAiManager().removeAI(crewMember);
		}
	}

	public static void recallCrew(PlayerState playerState, String arg) {
		CrewMember crewMember = playerState.getPlayerAiManager().getCrewByName(arg);
		if(crewMember != null) crewMember.recall();
	}

	public static void recallAllCrew(PlayerState playerState) {
		for(AiInterfaceContainer crewMember : playerState.getPlayerAiManager().getCrew()) {
			if(crewMember instanceof CrewMember) ((CrewMember) crewMember).recall();
		}
	}

	public static void setArea(PlayerState playerState, Quarter quarter, String point) {
		QuarterManager quarterManager = getQuarterManager(playerState);
		if(quarterManager != null) {
			if(point.toLowerCase(Locale.ENGLISH).equals("p1")) quarter.getArea().min.set(new Vector3i(playerState.getBuildModePosition().getWorldTransform().origin.x, playerState.getBuildModePosition().getWorldTransform().origin.y, playerState.getBuildModePosition().getWorldTransform().origin.z));
			else if(point.toLowerCase(Locale.ENGLISH).equals("p2")) quarter.getArea().max.set(new Vector3i(playerState.getBuildModePosition().getWorldTransform().origin.x, playerState.getBuildModePosition().getWorldTransform().origin.y, playerState.getBuildModePosition().getWorldTransform().origin.z));
		}
	}

	public static Quarter getQuarterInfo(PlayerState playerState, String name) {
		QuarterManager quarterManager = getQuarterManager(playerState);
		if(quarterManager != null) {
			CrewMember member = playerState.getPlayerAiManager().getCrewByName(name);
			if(member != null) {
				for(Quarter quarter : quarterManager.getQuartersById().values()) {
					if(quarter.getCrew().contains(member)) return quarter;
				}
			}
		}
		return null;
	}

	public static Quarter getById(PlayerState playerState, int id) {
		QuarterManager quarterManager = getQuarterManager(playerState);
		if(quarterManager != null) return quarterManager.getQuartersById().get(id);
		return null;
	}

	public static void forceUpdate(Quarter quarter) {
		quarter.forceUpdate();
	}

	private static QuarterManager getQuarterManager(PlayerState playerState) {
		int entityId = playerState.getSelectedEntityId();
		Sendable sendable = GameCommon.getGameObject(entityId);
		if(sendable instanceof SegmentController) return ((SegmentController) sendable).getQuarterManager();
		else return null;
	}

	public static String randomName() {
		//Todo: Something better than this
		return "Crew Member #" + (new Random().nextInt(1000) + 1000);
	}
}
