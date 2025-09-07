package api.mod.config;

import api.DebugFile;
import api.mod.StarMod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;

public class FileConfiguration {
    private StarMod mod;
    private String name;
    private HashMap<String, String> values = new HashMap<>();
    private HashMap<String, String> comments = new HashMap<>();

    private String configPath;
    public FileConfiguration(StarMod mod, String name){
        this.mod = mod;
        this.name = name;
        this.configPath = "moddata" + File.separator + mod.getSkeleton().getName() + File.separator + name +".yml";
        reloadConfig();
    }
    public FileConfiguration(StarMod mod, String name, List<String> keys, List<String> values){
        this.mod = mod;
        this.name = name;
        this.configPath = "moddata" + File.separator + mod.getSkeleton().getName() + File.separator + name +".yml";
        for (int i = 0; i < keys.size(); i++) {
            this.values.put(keys.get(i), values.get(i));
        }
    }

    public String getName() {
        return name;
    }

    public void reloadConfig(){
        values.clear();
        read();
    }
    private void read(){
        try {
            String configContent = convertFileToString(configPath, Charset.defaultCharset());
            configContent = configContent.trim().replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", ""); //Remove comments
            Scanner scanner = new Scanner(configContent); //USe
            //Scanner scanner = new Scanner(new File(configPath));
            while(scanner.hasNext()) {
                String next = scanner.nextLine();
                if(next.length() > 0 && next.charAt(0) != '#') { //No empty lines or hash comments
                    String[] split = next.split(Pattern.quote(": "));

                    // For arguments with 2 colons, use the end result as the value only
                    if(split.length > 2) {
                        StringBuilder key = new StringBuilder();
                        for (int i = 0; i < split.length - 1; i++) {
                            key.append(split[i]);
                            if(i != split.length - 2){
                                key.append(": ");
                            }
                        }
                        values.put(key.toString(), split[split.length - 1]);
                    }else{
                        values.put(split[0], split[1]);
                    }
                }
            }
        } catch(NoSuchFileException e) {
            DebugFile.warn("Config file: " + configPath + " not found, writing...");
            saveConfig();
            //e.printStackTrace();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    private String convertFileToString(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public String getConfigurableValue(String path, String defaultVal){
        String string = getString(path);
        if(string == null){
            set(path, defaultVal);
            return defaultVal;
        }
        return string;
    }
    public int getConfigurableInt(String path, int defaultVal){
        String str = getConfigurableValue(path, String.valueOf(defaultVal));
        return Integer.parseInt(str);
    }
    public long getConfigurableLong(String path, int defaultVal){
        String str = getConfigurableValue(path, String.valueOf(defaultVal));
        return Long.parseLong(str);
    }
    public float getConfigurableFloat(String path, float defaultVal){
        String str = getConfigurableValue(path, String.valueOf(defaultVal));
        return Float.parseFloat(str);
    }
    public boolean getConfigurableBoolean(String path, boolean defaultVal){
        String str = getConfigurableValue(path, String.valueOf(defaultVal));
        return Boolean.parseBoolean(str);
    }

    public boolean getBoolean(String path) {
        return Boolean.parseBoolean(values.get(path));
    }

    public int getInt(String path){
        return Integer.parseInt(values.get(path));
    }
    public double getDouble(String path){
        return Double.parseDouble(values.get(path));
    }
    public String getString(String path){
        return values.get(path);
    }
    public void set(String path, Object value){
        if(value == null){
            values.remove(path);
            return;
        }
        values.put(path, value.toString());
    }
    //-- todo remove and migrate to TOML

    public void setComment(String path, String comment) {
        if (comment == null)
            comments.remove(path);
        else
            comments.put(path, comment);
    }
    public void setList(String path, ArrayList<String> list){
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        set(path, sb);
    }
    public ArrayList<String> getList(String path){
        ArrayList<String> r = new ArrayList<String>();
        String string = getString(path);
        if(string == null) return r;
        r.addAll(Arrays.asList(string.split(",")));
        return r;
    }
    //--
    public void saveDefault(String... def) {
        for(String s : def) {
            String[] split = s.split(Pattern.quote(": "));
            if(!values.containsKey(split[0])) values.put(split[0], split[1]);
        }
        saveConfig();
    }

     LinkedList<String> getWriteLines() {
        LinkedList<String> keyList = new LinkedList<>(values.keySet());
        LinkedList<String> outList = new LinkedList<>();
        Collections.sort(keyList, Collator.getInstance());
        for (String key : keyList) {
            String value = values.get(key);
            if (comments.containsKey(key))
                outList.add("# " + comments.get(key)+"\n");
            outList.add(key + ": " + value + "\n");
        }
        return outList;
    }

    public void saveConfig() {
        try {
            File file = new File(configPath);
            file.getParentFile().mkdirs();
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            for (String line: getWriteLines()) {
                writer.write(line);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getKeys() {
        return values.keySet();
    }

    public long getLong(String key) {
        return Long.parseLong(getString(key));
    }

    public StarMod getMod() {
        return mod;
    }
}