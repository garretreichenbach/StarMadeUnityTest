package api.utils.registry;

import api.ModPlayground;
import api.common.GameCommon;
import api.mod.ModSkeleton;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * GOALS:
 * - Mod A requests a unique string for a RegistryType
 * - The String (registry type + namespaced string), is mapped to the next available long
 * - The UR is synchronized with the client at all times.
 * - What if the client needs a new UR value immediately that doesnt exist yet on the server?
 * - Should the client be able to do this?
 * - Load all URs at enable time?
 * - Lock thread?
 * - Server should control and allocate URVs
 * - Server sends data to client before mods are enabled
 * - Client gets data and allows mods to use it
 * - If a mod on the client gets a URV that is not assigned, it throws an exception
 *
 */
public class UniversalRegistry {
    //Server Only
    public static void readData(ModPlayground defaultMod){
        System.err.println("== Loading UR data ==");
        String uniqueContextId = GameCommon.getUniqueContextId();
        //POSSIBLE: Move data to be stored with the world itself?
        for (RegistryType type : RegistryType.values()) {
            FileConfiguration config = defaultMod.getConfig("universalregistry/" + uniqueContextId + "/" + type.name().toLowerCase(Locale.ROOT));
            long max = type.getStartingValue();//If there is no values, use the starting value
            for (String key : config.getKeys()) {
                long value = config.getLong(key);
                type.dataMap.put(key, value);
                max = Math.max(max, value);
            }
            type.currentValue = max;
        }
    }
    public static void writeToFile(ModPlayground defaultMod){
        String uniqueContextId = GameCommon.getUniqueContextId();
        for (RegistryType type : RegistryType.values()) {
            FileConfiguration config = defaultMod.getConfig("universalregistry/" + uniqueContextId + "/" + type.name().toLowerCase(Locale.ROOT));
            config.getKeys().clear();
            for (Map.Entry<String, Long> entry : type.getDataMap().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            config.saveConfig();
        }

    }
    public static long registerURV(RegistryType type, ModSkeleton mod, String uid) {
        String namespacedKey = mod.getName() + "=" + uid;
        Long l = type.dataMap.get(namespacedKey);
        if(l == null) {
            type.dataMap.put(namespacedKey, ++type.currentValue);
            return type.currentValue;
        }else{
            return l;
        }
    }
    public static long registerCustomURV(RegistryType type, ModSkeleton mod, String uid, long customId) {
        String namespacedKey = mod.getName() + "=" + uid;
        Long l = type.dataMap.get(namespacedKey);
        if(l == null) {
            type.dataMap.put(namespacedKey, customId);
        }else{
            if(customId != l) {
                System.err.println("[UniversalRegistry] registerCustomURV called but existing value != new value: " + l + ", " + customId);
            }
            type.dataMap.put(namespacedKey, customId);
        }
        return customId;
    }
    public static Long getExistingURVOrNull(RegistryType type, StarMod mod, String uid) {
        String namespacedKey = mod.getName() + "=" + uid;
        return type.dataMap.get(namespacedKey);
    }

    /**
     * @throws IllegalStateException on URV not found
     */
    public static long getExistingURV(RegistryType type, StarMod mod, String uid) {
        String namespacedKey = mod.getName() + "=" + uid;
        Long l = type.dataMap.get(namespacedKey);
        if(l == null) {
            System.err.println("[UniversalRegistry] Null value, printing UR:");
            dumpRegistry();
            throw new IllegalStateException("Registry key ("+namespacedKey+") not found. Mod error or network sync issue.");
        }else{
            return l;
        }
    }
    public static void dumpRegistry(){
        System.err.println("===== UNIVERSAL REGISTRY ======");
        for (RegistryType type : RegistryType.values()) {
            System.err.println("==> " + type.name());
            for (Map.Entry<String, Long> entry : type.dataMap.entrySet()) {
                System.err.println(entry.getKey() + " > " + entry.getValue());
            }
        }
        System.err.println("===== END UNIVERSAL REGISTRY ======");
    }

    public enum RegistryType {
        //Last used value in PlayerUsableInterface
        PLAYER_USABLE_ID(-9223372036854775774L),

        META_OBJECT(Long.MIN_VALUE),
        ORE(16),
        BLOCK_ID(Long.MIN_VALUE) //Handled by BlockTypes.properties, but kept as a backup in the UR
        ;


        private final long startingValue;
        private long currentValue;
        private final HashMap<String, Long> dataMap = new HashMap<>();

        RegistryType(long startingValue) {
            this.startingValue = startingValue;
            this.currentValue = startingValue;
        }

        public long getStartingValue() {
            return startingValue;
        }
        public static void reset(){
            for (RegistryType value : values()) {
                value.currentValue = value.startingValue;
                value.dataMap.clear();
            }
        }
        public HashMap<String, Long> getDataMap() {
            return dataMap;
        }
    }
}
