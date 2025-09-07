package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;

public class MainWindowTabAddEvent extends Event {
    private final GUIMainWindow windowf;
    private final GUIContentPane panef;
    private final Object titlef;

    public MainWindowTabAddEvent(GUIMainWindow window, GUIContentPane pane, Object title){
        windowf = window;
        panef = pane;
        titlef = title;
    }

    public GUIMainWindow getWindow() {
        return windowf;
    }

    public GUIContentPane getPane() {
        return panef;
    }

    /**
     * Lots of names/titles in StarMade are just objects with an overriden toString() method.
     * If its not a direct string, it can change based on the circumstances, so you may want to not compare titles that change
     * @return If the title is an unchanging string
     */
    public boolean isNameDirectString(){
        return titlef instanceof String;
    }

    /**
     *
     * @return The title of the tab that was created as an object, this objects toString is overriden
     */
    public Object getTitle() {
        return titlef;
    }

    /**
     * See documentation on isNameDirectString
     * @return The title objects toString
     */
    public String getTitleAsString(){
        return titlef.toString();
    }

    /**
     * Create tab after this one on the same window
     * @return The created ContentPane
     */
    public GUIContentPane createTab(String name) {
        return windowf.addTab(name);
    }
}
