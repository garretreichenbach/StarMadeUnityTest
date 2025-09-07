package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableList;

import java.util.List;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/24/2021]
 */
public class AdvancedBuildModeGUICreateEvent extends Event {

    private final GUIDockableList dockableList;
    private final List<AdvancedGUIGroup> groups;

    public AdvancedBuildModeGUICreateEvent(GUIDockableList dockableList, List<AdvancedGUIGroup> groups) {
        this.dockableList = dockableList;
        this.groups = groups;
    }

    public GUIDockableList getDockableList() {
        return dockableList;
    }

    public List<AdvancedGUIGroup> getGroups() {
        return groups;
    }
}
