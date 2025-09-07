package org.schema.schine.ai.stateMachines;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.resource.tag.TagSerializable;

public interface AIConfigurationInterface<E> extends TagSerializable {

	public AiEntityStateInterface getAiEntityState();

	public boolean isActiveAI();

	public boolean isAIActiveClient();

	public void callBack(AIConfiguationElementsInterface aiConfiguationElements, boolean send);

	public void update(Timer timer);

	public void updateFromNetworkObject(NetworkObject o);

	public void updateToFullNetworkObject(NetworkObject networkObject);

	public void updateToNetworkObject(NetworkObject networkObject);

	public void initFromNetworkObject(NetworkObject from);

	public void applyServerSettings();

	public AIConfiguationElementsInterface get(E active);
}
