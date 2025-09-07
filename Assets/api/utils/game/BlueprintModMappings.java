package api.utils.game;

import api.DebugFile;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jake on 11/9/2020.
 * <insert description here>
 */
public class BlueprintModMappings {

    private final HashMap<Short, String> namespacedBlocks = new HashMap<>();
    private final HashMap<String, Short> reverseNamespacedBlocks = new HashMap<>();
    public short translateId(short oldId){
        //TODO: Maybe make a faster cache?
        String namespaceKey = namespacedBlocks.get(oldId);
        Short aShort = getCurrent().reverseNamespacedBlocks.get(namespaceKey);
        if(aShort == null){
//            System.err.println("ModMappings translation failure: " + oldId + ", oldKey=" + namespaceKey);
            //No mappings available. Vanilla or unknown block.
            return oldId;
        }
        return aShort;
    }
    private static final boolean debugMappingsState = false;
    public void printState(){
        if(!debugMappingsState) return;
        DebugFile.info("> BlueprintModMappings State: ");
        for (Map.Entry<Short, String> entry : namespacedBlocks.entrySet()) {
            DebugFile.info(entry.getKey() + " ::: " + entry.getValue());
        }
        DebugFile.info("===========");
    }


    //todo uncache on log onto new world
    public static void uncacheMappings(){
        currentMappings = null;
    }
    private static BlueprintModMappings currentMappings;
    public static BlueprintModMappings getCurrent(){
        if(currentMappings == null) {
            BlueprintModMappings mappings = new BlueprintModMappings();
            ElementInformation[] infoArray = ElementKeyMap.getInfoArray();
            for (ElementInformation block : infoArray) {
                if (block != null) {
                    if (block.fullName.contains("~")) {
                        mappings.namespacedBlocks.put(block.id, block.fullName);
                        mappings.reverseNamespacedBlocks.put(block.fullName, block.id);
                    }
                }
            }
            currentMappings = mappings;
        }
        return currentMappings;
    }

    public HashMap<Short, String> getNamespacedBlocks() {
        return namespacedBlocks;
    }

    public HashMap<String, Short> getReverseNamespacedBlocks() {
        return reverseNamespacedBlocks;
    }
}
