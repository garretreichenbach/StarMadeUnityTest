package api.mod;

import api.DebugFile;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A file that stores what mods should be enabled when connecting to a singleplayer world.
 *
 * Dedicated Server = Start all mods in folder
 * Client -> Server = Start all mods that server has.
 */
public class SinglePlayerModData {
    //name,last download date,client enabled
    //Turret Hotkey,112312,enabled
    //If a user downloads a mod manualy, it will not be in the file, so whenever we try to access it, create it
    private HashMap<ModIdentifier, Pair<Integer, Boolean>> data = new HashMap<ModIdentifier, Pair<Integer, Boolean>>();

    @NotNull
    private Pair<Integer, Boolean> getClientData(ModIdentifier name){
        Pair<Integer, Boolean> pair = data.get(name);
        if(pair != null) return pair;
        data.put(name, new ImmutablePair<Integer, Boolean>(0, false));
        return data.get(name);
    }
    private static SinglePlayerModData instance;
    public static SinglePlayerModData getInstance() {
        if(instance == null){
            instance = new SinglePlayerModData();
        }
        return instance;//
    }
    public void onDownloadedMod(ModIdentifier name, int downloadDate){
        data.put(name, new ImmutablePair<Integer, Boolean>(downloadDate, false));
        write();
    }
    //Mod data g/s
    public boolean isClientEnabled(ModIdentifier modName){
        return getClientData(modName).getRight();
    }
    public void setClientEnabled(ModIdentifier name, boolean value){
        int date = getClientData(name).getLeft();
        data.put(name, new ImmutablePair<Integer, Boolean>(date, value));
        write();
    }
    public int getDownloadDate(ModIdentifier name){
        return getClientData(name).getLeft();
    }

    public static void main(String[] args) {
        SinglePlayerModData sp = new SinglePlayerModData();
        System.err.println(sp);
    }
    private SinglePlayerModData(){
        try {
            File source = new File("clientmods.txt");
            if(!source.exists()){
                boolean newFile = source.createNewFile();
                if(!newFile){
                    DebugFile.err("Failed to create clientmods.txt");
                }
            }
            Scanner scanner = new Scanner(source);
            while (scanner.hasNext()){
                String[] split = scanner.nextLine().split(",");
                data.put(ModIdentifier.deserialize(split[0]), new ImmutablePair<Integer, Boolean>(Integer.parseInt(split[1]), Boolean.parseBoolean(split[2])));
                //enabledMods.add(scanner.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            DebugFile.err("!!! FAILED TO LOAD MOD DATA FILE !!!");
            DebugFile.logError(e, null);
            data.clear();
        }
    }
    private void write(){
        try {
            FileWriter writer = new FileWriter("clientmods.txt");
            for (Map.Entry<ModIdentifier, Pair<Integer, Boolean>> entry : data.entrySet()) {
                writer.write(entry.getKey().serialize() + ",");
                writer.write(entry.getValue().getLeft() + ",");
                writer.write(String.valueOf(entry.getValue().getRight()));
                writer.write("\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}