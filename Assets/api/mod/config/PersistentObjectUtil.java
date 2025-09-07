package api.mod.config;

import api.DebugFile;
import api.common.GameCommon;
import api.mod.ModSkeleton;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;

public class PersistentObjectUtil {

    /**
     * A map of mods -> All registered classes -> Lists of said objects
     */
    private static final HashMap<ModSkeleton, HashMap<Class<?>, ArrayList<Object>>> data = new HashMap<>();
    private static final HashMap<ModSkeleton, StringBuilder> logMap = new HashMap<>();
    private static final int MAX_LOGS = 10;
    private static final int MAX_CHAR_STORAGE = 100000;

    private static void initLoggerForMod(ModSkeleton skeleton) {
        File logsFolder = new File(skeleton.getResourcesFolder() + "/logs");
        if(!logsFolder.exists()) logsFolder.mkdirs();
        else if(logsFolder.listFiles() != null && Objects.requireNonNull(logsFolder.listFiles()).length > 0) {
            File[] logFiles = new File[Objects.requireNonNull(logsFolder.listFiles()).length];
            int j = logFiles.length - 1;
            for(int i = 0; i < logFiles.length && j >= 0; i++) {
                logFiles[i] = Objects.requireNonNull(logsFolder.listFiles())[j];
                j--;
            }
            for(File logFile : logFiles) {
                String fileName = logFile.getName().replace(".txt", "");
                int logNumber = Integer.parseInt(fileName.split("\\.")[1]) + 1;
                String newName = skeleton.getResourcesFolder() + "/logs/persistent_objects." + logNumber + ".log";
                if(logNumber < MAX_LOGS) logFile.renameTo(new File(newName));
                else logFile.delete();
            }
        }
        File logFile = new File(skeleton.getResourcesFolder() + "/logs/persistent_objects.0.log");
        try {
            logFile.delete();
            logFile.createNewFile();
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }
        logMap.put(skeleton, new StringBuilder());
    }

    //On mod enable:
    //Register classes
    //get classes/objects from file

    /**
     * Add an object to the persistent pool
     * @param context The mod where the data will be stored and assigned to
     * @param obj The object to add to the pool
     */
    public static void addObject(ModSkeleton context, Object obj){
        ArrayList<Object> objects = getObjects(context, obj.getClass());
        objects.add(obj);
    }
    public static boolean removeObject(ModSkeleton context, Object obj){
        ArrayList<Object> objects = getObjects(context, obj.getClass());
        return objects.remove(obj);
    }

    /**
     * Removes all objects of a specified type
     * @param context The mod skeleton
     * @param type The object's type (class)
     */
    public static <T> void removeAllObjects(ModSkeleton context, Class<T> type) {
        ArrayList<Object> objects = getObjects(context, type);
        objects.clear();
    }

    @NotNull
    public static <T> ArrayList<T> getCopyOfObjects(ModSkeleton context, Class<T> type){
        ArrayList<T> ts = new ArrayList<>();
        for (Object object : getObjects(context, type)) {
            ts.add((T) object);
        }
        return ts;
    }

    /**
     * Retrieves a list of objects of type from memory
     * @param clazz The class type
     * @return A reference to the array, they store objects so just cast the objects LOL
     *         DO NOT TRY AND CAST IT TO AN ArrayList[type]
     */
    @NotNull
    public static ArrayList<Object> getObjects(ModSkeleton context, Class<?> clazz){
        HashMap<Class<?>, ArrayList<Object>> classMap = data.get(context);
        if(classMap == null){
            HashMap<Class<?>, ArrayList<Object>> map = new HashMap<>();
            data.put(context, map);
            classMap = map;
        }
        ArrayList<Object> objects = classMap.get(clazz);
        if(objects == null){
            ArrayList<Object> value = new ArrayList<>();
            classMap.put(clazz, value);
            objects = value;
        }
        return objects;
    }
    public static void onDisableMod(ModSkeleton mod){
        save(mod);
        HashMap<Class<?>, ArrayList<Object>> m = data.get(mod);
        if(m != null) {
            m.clear();
        }
    }
    private static Gson newGson(){
        return new GsonBuilder().enableComplexMapKeySerialization().create();
    }
    public static void onModEnable(ModSkeleton mod){
        File file = getFileFor(mod);
        DebugFile.log("[PoU] Getting file for: " + mod.getName());
        if(file.exists()){
            DebugFile.log("[PoU] File exists");
            try {
                Scanner scanner = new Scanner(file);
                Gson gson = newGson();
                while (scanner.hasNext()) {
                    String fullClassName = scanner.nextLine();
                    if(!logMap.containsKey(mod)) initLoggerForMod(mod);
                    logMap.get(mod).append("[PoU] FQN: ").append(fullClassName).append("\n");
//                    DebugFile.log("[PoU] FQN: " + fullClassName);

                    Class<?> aClass = Class.forName(fullClassName);
//                    DebugFile.log("[PoU] Class: " + aClass.getName());
                    logMap.get(mod).append("[PoU] Class: ").append(aClass.getName()).append("\n");
                    ArrayList<Object> objects = getObjects(mod, aClass);
                    while (scanner.hasNext()) {
                        String rawJson = scanner.nextLine();
//                        DebugFile.log("[PoU] Read object: " + rawJson);
                        if(rawJson.startsWith("_end_")) break;
                        Object o = gson.fromJson(rawJson, aClass);
                        // If we deserialize a SimpleSerWrapper, call the onDeserialize method
                        if(o instanceof SimpleSerializerWrapper){
                            PacketReadBuffer byteBuffer = PacketReadBuffer.fromByteArray(((SimpleSerializerWrapper) o).rawData);
                            ((SimpleSerializerWrapper) o).onDeserialize(byteBuffer);
                        }
                        objects.add(o);
                        logMap.get(mod).append("[PoU] Added object: ").append(o.toString()).append("\n");
//                        DebugFile.log("[PoU] Added object: " + o.toString());
                    }

                }
            } catch (FileNotFoundException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        flushLogs(false);
    }

    /**
     * Writes all objects from mod to file
     * @param mod The mod attached to the data
     */
    //TODO Write on seperate thread and prevent overwrite if needed
    public static void save(ModSkeleton mod) {
        if(!logMap.containsKey(mod)) initLoggerForMod(mod);
        DebugFile.log("Saving PSD for: " + mod.getName());
        Gson gson = newGson();
        File file = getFileFor(mod);
        DebugFile.log("Got file: " + file.getName());
        try {
            FileWriter writer = new FileWriter(file);
            HashMap<Class<?>, ArrayList<Object>> classMap = data.get(mod);
            if (classMap != null) {
                DebugFile.log("Map not null");
                for (Map.Entry<Class<?>, ArrayList<Object>> classArrayListEntry : classMap.entrySet()) {
                    Class<?> type = classArrayListEntry.getKey();
                    ArrayList<Object> objects = classArrayListEntry.getValue();
                    writer.write(type.getName() + "\n");
                    for (Object object : objects) {
                        if(object instanceof SimpleSerializerWrapper){
                            ByteArrayOutputStream bArray = new ByteArrayOutputStream();
                            PacketWriteBuffer buf = new PacketWriteBuffer(new DataOutputStream(bArray));
                            ((SimpleSerializerWrapper) object).onSerialize(buf);
                            ((SimpleSerializerWrapper) object).rawData = bArray.toByteArray();
                        }
                        String serialized = gson.toJson(object);
                        writer.write(serialized + "\n");
//                        DebugFile.log("Wrote serialized: " + serialized);
                        logMap.get(mod).append("[PoU] Wrote serialized: ").append(serialized).append("\n");
                    }
                    writer.write("_end_\n");
                }
            }

            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        flushLogs(false);

    }
    private static File getFileFor(ModSkeleton mod){
        String contextId = GameCommon.getUniqueContextId();
        File file = new File("moddata/" + mod.getName() + "/persistent/" + contextId + ".smdat");
        if(!file.exists()){
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void flushLogs(boolean force) {
        for(Map.Entry<ModSkeleton, StringBuilder> builder : logMap.entrySet()) {
            if(builder.getValue().length() > MAX_CHAR_STORAGE || force) {
                File logFile = new File(builder.getKey().getResourcesFolder() + "/logs/persistent_objects.0.log");
                try {
                    if(!logFile.exists()) logFile.createNewFile();
                    FileWriter writer = new FileWriter(logFile, true);
                    writer.write(builder.getValue().toString());
                    writer.close();
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                }
                builder.getValue().setLength(0);
            }
        }
    }
    /* FOOT NOTE: File structure of Persistent Objects (.smdat)
    Full qualified name of class: me.modcreator.modname.TheClass
    Json Object
    Json Object
    Json Object
    ...
    terminator: _end_
    Repeat for amount of objects
     */
}

