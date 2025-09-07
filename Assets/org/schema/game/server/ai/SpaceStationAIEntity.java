package org.schema.game.server.ai;

import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.ai.HittableAIEntityState;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public class SpaceStationAIEntity extends SegmentControllerAIEntity<SpaceStation> implements HittableAIEntityState {

	/**
	 *
	 */
	
	private long lastFleetSent;

	public SpaceStationAIEntity(String name, SpaceStation s) {
		super(name, s);
	}

	@Override
	public void updateAIClient(Timer timer) {
	}

	@Override
	public void updateAIServer(Timer timer) {
	}

	@Override
	public void handleHitBy(float actualDamage, Damager from) {
		if (from != null && from instanceof SimpleTransformableSendableObject
				) {
			if(FactionManager.isNPCFaction(getEntity().getFactionId())){
				//do npc stuff
			}else if(getEntity().getFactionId() < 0){
				if (getState().getUpdateTime() - lastFleetSent > 60000) {
					lastFleetSent = System.currentTimeMillis();
					((SimpleTransformableSendableObject) from).sendControllingPlayersServerMessage(Lng.astr("Radio transmission intercepted:\n'We are being attacked!\nRequesting immediate backup!'"), ServerMessage.MESSAGE_TYPE_WARNING);
					//prepare to die
					((GameServerState) getState()).getSimulationManager().sendToAttackSpecific(
							(SimpleTransformableSendableObject) from, getEntity().getFactionId(), (int) (3 + Math.random() * 6));
				}
			}
		}
	}

}
