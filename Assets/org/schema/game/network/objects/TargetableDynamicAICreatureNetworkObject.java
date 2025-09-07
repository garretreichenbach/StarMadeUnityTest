package org.schema.game.network.objects;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.creature.CreaturePartNode;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.network.objects.remote.RemoteCreaturePart;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitive;
import org.schema.schine.network.objects.remote.RemoteVector3i;
import org.schema.schine.resource.CreatureStructure.PartType;

public class TargetableDynamicAICreatureNetworkObject extends TargetableAICreatureNetworkObject {

	public RemoteFloatPrimitive scale = new RemoteFloatPrimitive(1, this);
	public RemoteFloatPrimitive height = new RemoteFloatPrimitive(1, this);
	public RemoteFloatPrimitive width = new RemoteFloatPrimitive(1, this);
	public RemoteFloatPrimitive speed = new RemoteFloatPrimitive(4, this);

	public RemoteVector3i boxDim = new RemoteVector3i(new Vector3i(1, 1, 1), this);

	public RemoteCreaturePart creatureCode = new RemoteCreaturePart(new CreaturePartNode(PartType.BOTTOM), this);

	public TargetableDynamicAICreatureNetworkObject(StateInterface state, AbstractOwnerState owner) {
		super(state, owner);
	}

}
