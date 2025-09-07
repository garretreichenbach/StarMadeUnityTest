package org.schema.game.common.controller.elements;

/**
     Denotes an element manager (or other thing) containing/referencing no more than one single element collection manager.
 */
public interface SingleElementCollectionContainerInterface<T extends ElementCollectionManager<?,?,?>>{
    boolean hasCollection();
    /**
    Get the collection contained within. May return <code>null</code> if there is no collection.
     */
    T getCollection();
}
