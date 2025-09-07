package org.schema.game.client.view.gui;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;

public interface TopBarInterface {

	float getHeight();

	void draw();

	void onInit();

	void updateCreditsAndSpeed();

	Vector3f getPos();

	void notifyEffectHit(SimpleTransformableSendableObject obj,
	                     OffensiveEffects offensiveEffects);

	void update(Timer timer);


}
