package api.listener.events.register;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableSingleModule;
import org.schema.game.common.data.ManagedSegmentController;

import java.util.ArrayList;

public class RegisterAddonsEvent extends Event {
    private ManagerContainer<?> container;
    public RegisterAddonsEvent(ManagerContainer<?> container){
        this.container = container;
    }
    public ManagerContainer<?> getContainer() {
        return container;
    }
    public SegmentController getSegmentController(){
        return container.getSegmentController();
    }

    public ArrayList<RecharchableSingleModule> addons = new ArrayList<RecharchableSingleModule>();
    public void addModule(RecharchableSingleModule addOn){
        addons.add(addOn);
    }
}
