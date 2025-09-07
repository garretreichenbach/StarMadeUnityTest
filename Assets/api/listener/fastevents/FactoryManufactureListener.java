package api.listener.fastevents;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.game.common.controller.elements.factory.FactoryCollectionManager;
import org.schema.game.common.controller.elements.factory.FactoryProducerInterface;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.element.meta.RecipeInterface;
import org.schema.game.common.data.player.inventory.Inventory;

/**
 * Created by Jake on 11/18/2020.
 * <insert description here>
 */
public interface FactoryManufactureListener {
    /**
     * @param connectedInventories Absolute position array of connected inventories
     * @return true = proceed, false = cancel event. TRUE takes priority if multiple listeners cancel it.
     */
    boolean onPreManufacture(FactoryCollectionManager cm, Inventory ownInventory, LongOpenHashSet[] connectedInventories);
    void onProduceItem(RecipeInterface recipe, Inventory inv, FactoryProducerInterface producer, FactoryResource produced, int amt, IntCollection changeSet);

}
