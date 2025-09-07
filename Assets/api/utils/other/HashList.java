package api.utils.other;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * HashMap that maps to a list rather than a single value
 */
public class HashList<K, V> extends HashMap<K, ArrayList<V>> {
    public void add(K key, V val) {
        ArrayList<V> vs = get(key);
        if (vs == null) {
            ArrayList<V> value = new ArrayList<>();
            put(key, value);
            vs = value;
        }
        vs.add(val);
    }

    public ArrayList<V> getList(K key) {
        if(get(key) == null) put(key, new ArrayList<V>());
        return get(key);
    }
}
