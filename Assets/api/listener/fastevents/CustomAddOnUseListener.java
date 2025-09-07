package api.listener.fastevents;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Fast listener to allow AI's to use custom mod module add-ons.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public interface CustomAddOnUseListener {

	/**
	 * Called when an AI attempts to use a custom add-on.
	 * @param entity The entity attempting to use the add-on.
	 * @param managerContainer The manager container of the entity.
	 * @param timer The server timer.
	 */
	void use(Ship entity, ShipManagerContainer managerContainer, Timer timer);
}
