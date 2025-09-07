package api;

import java.util.HashMap;

/**
 * Created by Jake on 9/27/2020.
 * <insert description here>
 */
public class CustomMetaObjectRegistry {
    private static short count = -16;
    private static HashMap<String, Short> idData = new HashMap<>();
    public static short getId(String metaObjectName){
        Short re = idData.get(metaObjectName);
        if(re != null) return re;
        count--;
        idData.put(metaObjectName, count);
        return count;
    }
}
