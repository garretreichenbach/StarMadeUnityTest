package api.listener.events.gui;

import api.listener.events.Event;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;

import java.util.ArrayList;

public class GUITopBarCreateEvent extends Event {

    private GUITopBar guiTopBar;
    private ArrayList<GUIHorizontalArea> buttons;
    private ArrayList<GUITopBar.ExpandedButton> dropdownButtons;
    private GUIHorizontalButtonTablePane taskPane;

    public GUITopBarCreateEvent(GUITopBar guiTopBar, ArrayList<GUIHorizontalArea> buttons, ObjectArrayList<GUITopBar.ExpandedButton> dropdownButtons, GUIHorizontalButtonTablePane taskPane) {
        this.guiTopBar = guiTopBar;
        this.buttons = buttons;
        this.dropdownButtons = new ArrayList<>();
        this.dropdownButtons.addAll(dropdownButtons);
        this.taskPane = taskPane;
    }

    public GUITopBar getGuiTopBar() {
        return guiTopBar;
    }

    public GUIHorizontalButtonTablePane getTaskPane() {
        return taskPane;
    }

    public ArrayList<GUITopBar.ExpandedButton> getDropdownButtons() {
        return dropdownButtons;
    }

    public ArrayList<GUIHorizontalArea> getButtons() {
        return buttons;
    }
}
