package api.utils.gui;

import api.DebugFile;
import api.mod.ModSkeleton;
import api.utils.other.HashList;
import api.utils.other.LangUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * ModGUIHandler.java
 * Registers and handles custom mod control managers.
 *
 * @since 3/17/2021
 * @author TheDerpGamer
 */
public class ModGUIHandler {

    private static HashList<ModSkeleton, GUIControlManager> modControlManagers = new HashList<>();
    private static HashList<ModSkeleton, GUIInputDialog> modInputDialogs = new HashList<>();

    /**
     * Registers a new control manager.
     * @see api.listener.events.gui.ControlManagerActivateEvent to test if the control manager is activated.
     * @param mod The mod calling this method.
     * @param controlManager The control manager (should extend AbstractControlManager).
     */
    public static void registerNewControlManager(ModSkeleton mod, GUIControlManager controlManager) {
        modControlManagers.add(mod, controlManager);
        DebugFile.log("Registered new control manager " + controlManager.getClass().getName() + " from mod " + mod.getName() + ".");
    }

    public static void deactivateAll() {
        try{
            for(GUIControlManager controlManager : getAllModControlManagers()) controlManager.setActive(false);
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<GUIControlManager> getModControlManagers(ModSkeleton modSkeleton) {
        return modControlManagers.getList(modSkeleton);
    }

    public static GUIControlManager getGUIControlManager(String windowName) {
        for(GUIControlManager controlManager : getAllModControlManagers()) {
            if(controlManager.getMenuPanel() != null && LangUtil.stringsEqualIgnoreCase(controlManager.getMenuPanel().getName(), windowName.replace(" ", "_"))) {
                return controlManager;
            }
        }
        return null;
    }

    public static ArrayList<GUIControlManager> getAllModControlManagers() {
        ArrayList<GUIControlManager> controlManagers = new ArrayList<>();
        for(ModSkeleton modSkeleton : modControlManagers.keySet()) {
            controlManagers.addAll(getModControlManagers(modSkeleton));
        }
        return controlManagers;
    }

    public static void registerNewInputDialog(ModSkeleton mod, GUIInputDialog inputDialog) {
        modInputDialogs.add(mod, inputDialog);
        DebugFile.log("Registered new input dialog " + inputDialog.getClass().getName() + " from mod " + mod.getName() + ".");
    }

    public static void activateInputDialog(ModSkeleton mod, String name) {
        for(GUIInputDialog inputDialog : modInputDialogs.getList(mod)) {
            if(LangUtil.stringsEqualIgnoreCase(inputDialog.getInputPanel().getName(), name)) {
                inputDialog.activate();
                return;
            }
        }
    }

    public static void deactivateInputDialog(ModSkeleton mod, String name) {
        for(GUIInputDialog inputDialog : modInputDialogs.getList(mod)) {
            if(LangUtil.stringsEqualIgnoreCase(inputDialog.getInputPanel().getName(), name)) {
                inputDialog.deactivate();
                return;
            }
        }
    }

    public static ArrayList<GUIInputDialog> getAllInputDialogs() {
        ArrayList<GUIInputDialog> inputDialogs = new ArrayList<>();
        for(Map.Entry<ModSkeleton, ArrayList<GUIInputDialog>> entry : modInputDialogs.entrySet()) {
            inputDialogs.addAll(entry.getValue());
        }
        return inputDialogs;
    }

    public static GUIInputDialog getInputDialog(String name) {
        for(GUIInputDialog inputDialog : getAllInputDialogs()) {
            if(LangUtil.stringsEqualIgnoreCase(inputDialog.getInputPanel().getName(), name)) return inputDialog;
        }
        return null;
    }
}
