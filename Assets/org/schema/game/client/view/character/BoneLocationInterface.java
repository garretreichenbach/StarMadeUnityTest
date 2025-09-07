package org.schema.game.client.view.character;

import org.schema.schine.graphicsengine.animation.AnimationChannel;
import org.schema.schine.graphicsengine.animation.AnimationController;
import org.schema.schine.network.StateInterface;

public interface BoneLocationInterface {

	String getRootBoneName();

	String getRootTorsoBoneName();

	void initializeListeners(AnimationController controller,
	                         AnimationChannel channel, AnimationChannel channelTorso);

	public String getHeldBoneName();

	void loadClientBones(StateInterface state);

}
