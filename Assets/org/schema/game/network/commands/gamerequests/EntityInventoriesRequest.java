package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.SendableSegmentProvider;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.NetworkStateContainer;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerState;

public class EntityInventoriesRequest implements GameRequestInterface{

	public int segmentControllerID = -1;
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(segmentControllerID);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		segmentControllerID = b.readInt();
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.ENTITY_INVENTORIES;
	}

	@Override
	public void free() {
	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState state) throws IOException {
		final GameServerState gs = (GameServerState) state;

		final int segmentControllerId = segmentControllerID;

		try {
			NetworkStateContainer privateChannelObj = ((RegisteredClientOnServer)((ServerProcessorInterface)p).getClient()).getLocalAndRemoteObjectContainer();
			//the provider and the controller share the same id (one is in the private and one in the public channel)
			SendableSegmentProvider provider = (SendableSegmentProvider) privateChannelObj.getLocalObjects().get(segmentControllerId);
			SendableSegmentController c  = provider.getSegmentController();

			if (c == null) {
				throw new IllegalArgumentException("[SERVER] Could NOT find the segment controller ID " + segmentControllerId + ". This CAN happen, when the SegmentController for this SendableSegmentProvider was deleted and the PRIVATE sendable segment controller was still udpating");
			}
			provider.sendServerInventories();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
