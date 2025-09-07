package api.listener.events.register;

import api.listener.events.Event;
import api.utils.game.module.ModManagerContainerModule;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModule;
import org.schema.game.common.controller.elements.ShipManagerContainer;

import java.util.ArrayList;
import java.util.HashMap;

public class ManagerContainerRegisterEvent extends Event {
    private ManagerContainer container;

    /**
     * This is whats called when a Ship/Station, more specifically their "Manager Container" which _contains_
     * all of the ElementManagers (For more info on ElementManagers look at the StarLoader's wiki page on them
     * @param container
     */
    public ManagerContainerRegisterEvent(ManagerContainer container){

        this.container = container;
    }

    private ArrayList<ManagerModule> modules = new ArrayList<ManagerModule>();
    public void addModuleCollection(ManagerModule manager){
        modules.add(manager);
    }


    public SegmentController getSegmentController(){
        return container.getSegmentController();
    }

    public ArrayList<ManagerModule> getRegisteredModules() {
        return modules;
    }

    /**
     *
     * @return A list of all the modules that have been registered.
     */
    public ObjectArrayList<ManagerModule<?, ?, ?>> getSegmentControllerModules() {
        return container.getModules();
    }

    public ManagerContainer getContainer() {
        return container;
    }

    /**
     * TRUE if its a ShipManagerContainer, and can be safely casted to one
     * FALSE if its a StationaryManagerContainer, and can be safely casted
     * @return
     */
    public boolean isShip(){
        return container instanceof ShipManagerContainer;
    }

    public HashMap<Short, ModManagerContainerModule> moduleMap = new HashMap<>();
    public void addModMCModule(ModManagerContainerModule module){
        moduleMap.put(module.getBlockId(), module);
    }
}
