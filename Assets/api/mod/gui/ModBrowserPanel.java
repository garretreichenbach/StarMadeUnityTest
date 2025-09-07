package api.mod.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;

import java.util.List;

public class ModBrowserPanel extends GUIElement implements GUIActiveInterface {

    private SMDModEntryScrollableTableList modList;
    public GUIMainWindow mainPanel;
    private GUIContentPane modsTab;
    private final DialogInput dialogInput;

    private final List<GUIElement> toCleanUp = new ObjectArrayList<>();
    private boolean init;

    public ModBrowserPanel(InputState state, DialogInput dialogInput) {
        super(state);
        this.dialogInput = dialogInput;
    }

    @Override
    public float getWidth() {
        return GLFrame.getWidth() - 470;
    }

    @Override
    public float getHeight() {
        return GLFrame.getHeight() - 70;
    }

    @Override
    public void cleanUp() {
        for(GUIElement e : toCleanUp) e.cleanUp();
        toCleanUp.clear();
    }

    @Override
    public void draw() {
        if(!init) onInit();
        GlUtil.glPushMatrix();
        transform();
        mainPanel.draw();
        GlUtil.glPopMatrix();

    }

    @Override
    public void onInit() {
        if(init) return;
        mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "Mod Manager");
        mainPanel.onInit();
        mainPanel.setPos(435, 35, 0);
        mainPanel.setWidth(GLFrame.getWidth() - 470);
        mainPanel.setHeight(GLFrame.getHeight() - 70);
        mainPanel.clearTabs();

        (modsTab = createInstalledTab()).onInit();
        GUIContentPane browseTab;
        (browseTab = createBrowseTab()).onInit();

        mainPanel.setSelectedTab(0);
        mainPanel.activeInterface = this;

        mainPanel.setCloseCallback(new GUICallback() {

            @Override
            public boolean isOccluded() {
                return !isActive();
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if(event.pressedLeftMouse()){
                    dialogInput.deactivate();
                }
            }
        });

        toCleanUp.add(mainPanel);
        init = true;
    }

    @Override
    public void update(Timer timer) {
        super.update(timer);
        modList.update(timer);
        modsTab.update(timer);
    }

    @Override
    public boolean isInside() {
        return mainPanel.isInside();
    }

    private GUIContentPane createBrowseTab() {
        GUIContentPane contentPane = mainPanel.addTab("BROWSE");
        contentPane.setTextBoxHeightLast(mainPanel.getInnerHeigth() - 44);
        modList = new SMDModEntryScrollableTableList(getState(), mainPanel.getWidth(), mainPanel.getHeight() - 44, contentPane.getContent(0));
        modList.onInit();
        contentPane.getContent(0).attach(modList);
        toCleanUp.add(modList);
        toCleanUp.add(contentPane);
        return contentPane;
    }

    private GUIContentPane createInstalledTab() {
        GUIContentPane installedTab = mainPanel.addTab("INSTALLED");
        installedTab.setTextBoxHeightLast(mainPanel.getInnerHeigth() - 44);
        InstalledModEntryScrollableTableList installedModList = new InstalledModEntryScrollableTableList(getState(), mainPanel.getInnerWidth(), mainPanel.getInnerHeigth() - 44, installedTab.getContent(0));
        installedModList.onInit();
        installedTab.getContent(0).attach(installedModList);
        toCleanUp.add(installedModList);
        toCleanUp.add(installedTab);
        return installedTab;
    }

    @Override
    public boolean isActive() {
        List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
        return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size()-1).getInputPanel() == this);
    }
}
