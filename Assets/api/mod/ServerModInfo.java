package api.mod;

import api.DebugFile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A hashmap that stores all mods that are on any particular server
 */
public class ServerModInfo {
    public static String getServerUID(String name, int port){
        return name + ":" + port;
    }
    private static HashMap<String, ArrayList<ModIdentifier>> serverModInfo = new HashMap<>();
    public static void registerModInfo(String serverUid, ModIdentifier modId){
        DebugFile.log("[Client] Registering server mod: " + modId + ", for: " + serverUid);
        ArrayList<ModIdentifier> infos = serverModInfo.get(serverUid);
        if(infos == null){
            ArrayList<ModIdentifier> emptyModList = new ArrayList<>();
            serverModInfo.put(serverUid, emptyModList);
            infos = emptyModList;
        }
        for (ModIdentifier allInfo : infos){
            if(allInfo.equals(modId)){
                DebugFile.log("Already registered, likely the user clicked refresh");
                return;
            }
        }
        infos.add(modId);
    }
    public static void dumpModInfos(){
        DebugFile.log("====== Dumping server mod info ======");
        for (String server : serverModInfo.keySet()){
            DebugFile.log("Server: " + server + ":");
            for (ModIdentifier info : serverModInfo.get(server)){
                DebugFile.log(" has mod: " + info);
            }
        }
        DebugFile.log("====== End ======");
    }

    public static ArrayList<ModIdentifier> getServerInfo(String serverUid) {
        return serverModInfo.get(serverUid);
    }
    public static void wipeServerInfo(String serverUid){
        serverModInfo.remove(serverUid);
    }
}
