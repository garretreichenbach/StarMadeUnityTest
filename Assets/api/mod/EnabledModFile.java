package api.mod;

import api.DebugFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EnabledModFile {
    private static EnabledModFile instance;
    public static EnabledModFile getInstance() {
        if(instance == null){
            instance = new EnabledModFile();
        }
        return instance;
    }
    public boolean isClientEnabled(ModInfo info){
        return enabledMods.contains(info.serialize());
    }
    public void setClientEnabled(ModInfo info, boolean value){
        if(value){
            enabledMods.add(info.serialize());
        }else{
            enabledMods.remove(info.serialize());
        }
        write();
    }

    public static void main(String[] args) {
        EnabledModFile file = EnabledModFile.getInstance();
        System.out.println(file.isClientEnabled(new ModInfo("bruhstar", "0.01")));
        file.setClientEnabled(new ModInfo("bruhstar", "0.01"), true);
        System.out.println(file.isClientEnabled(new ModInfo("bruhstar", "0.01")));
        file.setClientEnabled(new ModInfo("bruhstar", "0.01"), false);
        System.out.println(file.isClientEnabled(new ModInfo("bruhstar", "0.01")));
    }
    private ArrayList<String> enabledMods = new ArrayList<String>();
    private EnabledModFile(){
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
                enabledMods.add(scanner.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void write(){
        try {
            FileWriter writer = new FileWriter("clientmods.txt");
            for(String mod : enabledMods) {
                writer.write(mod);
                writer.write("\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
