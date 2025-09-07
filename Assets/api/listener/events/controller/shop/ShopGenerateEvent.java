package api.listener.events.controller.shop;

import api.listener.events.Event;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.controller.generator.ShopCreatorThread;
import org.schema.game.server.controller.world.factory.WorldCreatorFactory;

/**
 * Created by Jake on 2/26/2021.
 * <insert description here>
 */
public class ShopGenerateEvent extends Event {
    private ShopSpaceStation station;
    private final ShopCreatorThread shopCreatorThread;
    private WorldCreatorFactory factory;

    public ShopGenerateEvent(ShopSpaceStation station, ShopCreatorThread shopCreatorThread, WorldCreatorFactory factory) {
        this.station = station;
        this.shopCreatorThread = shopCreatorThread;
        this.factory = factory;

    }

    public ShopSpaceStation getStation() {
        return station;
    }

    public ShopCreatorThread getShopCreatorThread() {
        return shopCreatorThread;
    }

    public void setFactory(WorldCreatorFactory factory) {
        this.factory = factory;
    }

    public WorldCreatorFactory getFactory() {
        return factory;
    }
}
