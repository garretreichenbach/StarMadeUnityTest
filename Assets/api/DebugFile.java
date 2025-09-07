package api;

import api.mod.StarMod;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  Legacy class, deprecated in favour of StarMod.log or System.err for the global log
 */
@Deprecated
public class DebugFile {

    public static void logError(Throwable e, StarMod mod){
        e.printStackTrace();
    }
    public static void log(String s, StarMod mod){
        if(mod == null) System.err.println(s);
        else System.err.println(mod.getName() + ": " + s);
    }
    public static void debug(String s) {
        log("[DEBUG] " + s, null);
    }
    public static void err(String s){
        log("[ERROR] " + s, null);
    }
    public static void info(String s){
        log("[INFO] " + s, null);
    }
    public static void warn(String s){
        log("[WARNING] " + s, null);
    }
    public static void log(String s){
        log(s, null);
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat ("dd-MM-yyyy 'at' HH:mm:ss z");
    public static String getTime() {
        Date date = new Date(System.currentTimeMillis());
        String timeStamp = formatter.format(date);
        timeStamp += "  ";
        return timeStamp;
    }
}
