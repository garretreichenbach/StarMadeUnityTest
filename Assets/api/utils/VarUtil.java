package api.utils;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Allows attachment of data to class without modifying it
 */
public class VarUtil {
    //Allow GC to collect the keys (classes) whenever it wants.
    private static WeakHashMap<Object, HashMap<String, Object>> map = new WeakHashMap<>();
    //assignField(this, "count", 3);
    public synchronized static void assignField(Object self, String field, Object data){
        HashMap<String, Object> classData = map.get(self);
        if(classData == null){
            HashMap<String, Object> dat = new HashMap<>();
            map.put(self, dat);
            classData = dat;
        }
        classData.put(field, data);
    }
    public static <T> T getField(Object self, String field, Class<T> autoCast){
        HashMap<String, Object> dat = map.get(self);
        if(dat == null) return null;
        return (T) dat.get(field);
    }
    public static int incrementAndGet(Object self, String field){
        Integer i = getField(self, field, Integer.class);
        if(i == null){
            i = 0;
        }
        i++;
        assignField(self, field, i);
        return i;
    }
}
