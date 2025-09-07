package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.UsableElementManager;

/**
 * Called when an ElementCollectionManager is created, note that all info about this may not yet be created because its only the first super call
 * Usualy worth it to put it in a StarRunnable and get it next tick after its created
 */
public class ElementCollectionManagerInstantiateEvent extends Event {

    private final ElementCollectionManager<?, ?, ? extends UsableElementManager> elementCollectionManager;
    private final short enhancerClazz;
    private final SegmentController segmentController;
    private final UsableElementManager elementManager;

    public <EM extends UsableElementManager<?, ?, EM>> ElementCollectionManagerInstantiateEvent(ElementCollectionManager<?, ?, EM > elementCollectionManager, short enhancerClazz, SegmentController segmentController, EM elementManager) {

        this.elementCollectionManager = elementCollectionManager;
        this.enhancerClazz = enhancerClazz;
        this.segmentController = segmentController;
        this.elementManager = elementManager;
    }

    public ElementCollectionManager<?, ?, ? extends UsableElementManager> getElementCollectionManager() {
        return elementCollectionManager;
    }

    public short getEnhancerClazz() {
        return enhancerClazz;
    }

    public SegmentController getSegmentController() {
        return segmentController;
    }

    public UsableElementManager getElementManager() {
        return elementManager;
    }
}
