package org.schema.game.common.controller;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.vecmath.Vector3f;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public interface CelestialBodyGravityHandler {

	Vector3f getGravityVector(SimpleTransformableSendableObject<?> target);
}
