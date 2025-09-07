package org.schema.game.common.controller;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

public interface BeamHandlerContainer<E extends SimpleTransformableSendableObject> extends Damager {
	/**
	 * @return the handler
	 */
	public AbstractBeamHandler<E> getHandler();

}
