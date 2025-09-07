package api.listener.fastevents;

import org.schema.game.common.controller.elements.factory.FactoryCollectionManager;
import org.schema.game.common.controller.elements.shipyard.orders.states.Constructing;


/**
 * Created by lupoCani on 2020-11-28
 * <insert description here>
 */
public interface ProductionItemPullListener {
    /**
     * @param factoryCollectionManager The factory collection manager that called the listener, if any. (nullable)
     * @param constructingState The shipyard state object that called the listener, if any. (nullable)
     * (The IDE isn't letting me use @Nullable for some reason)
     */
    void onPrePull(FactoryCollectionManager factoryCollectionManager, Constructing constructingState);
    /**
     * @param factoryCollectionManager The factory collection manager that called the listener, if any. (nullable)
     * @param constructingState The shipyard state object that called the listener, if any. (nullable)
     * (The IDE isn't letting me use @Nullable for some reason)
     */
    void onPostPull(FactoryCollectionManager factoryCollectionManager, Constructing constructingState);
}
