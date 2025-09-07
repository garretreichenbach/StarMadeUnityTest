package api.mod;

import api.smd.SMDUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class SMDModData {
    //Index all mods from SMD with Name -> [resource id, resource date]
    private static SMDModData instance;
    private final HashMap<Integer, SMDModInfo> allModData = new HashMap<Integer, SMDModInfo>();
    public static SMDModData getInstance() {
        if(instance == null){
            instance = new SMDModData();

        }
        return instance;
    }
    public SMDModInfo getModData(int resId){
        return allModData.get(resId);
    }

    private SMDModData(){
        JsonArray mods = SMDUtils.getSMDMods();
        if(mods == null) {
            return;
        }
        for (JsonElement jsonModElement : mods) {
            JsonObject jsonModObject = jsonModElement.getAsJsonObject();
            SMDModInfo info = SMDModInfo.fromJson(jsonModObject);

            //Do not load DefaultMod from SMD, load the internal one
            if(!info.getName().equals("StarLoader") && info.getTags().contains("starloader")) {
                allModData.put(info.getResourceId(), info);
                System.out.println("[StarLoader SMDModData] Fetched mod from SMD: " + info);
            }
        }
    }


    public int getLastUpdateDate(int modResId){
        return allModData.get(modResId).getResourceDate();
    }

    public static void main(String[] args) throws IOException {
        SMDUtils.downloadMod(new ModIdentifier(8209, "0.2.001"));
    }


    public Collection<Integer> getModDataMap() {
        return allModData.keySet();
    }
    public Collection<SMDModInfo> getModDataValues(){
        return allModData.values();
    }
}
