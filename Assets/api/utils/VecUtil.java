package api.utils;

import javax.vecmath.Vector3f;

public class VecUtil {
    public static Vector3f scale(Vector3f v, float scale){
        v.scale(scale);
        return v;
    }
    public static Vector3f add(Vector3f v, Vector3f w){
        v.add(w);
        return v;
    }
}
