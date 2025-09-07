package api.utils.other;

import java.util.Locale;

/**
 * Created by Jake on 6/27/2021.
 * Helper functions for language and strings
 */
public class LangUtil {
    public static boolean stringsEqualIgnoreCase(String strA, String strB){
        return strA.toLowerCase(Locale.ROOT).equals(strB.toLowerCase(Locale.ROOT));
    }
    public static boolean stringsEqualIgnoreCaseAny(String strA, String... strB){
        for (String s : strB) {
            if(stringsEqualIgnoreCase(strA, s)){
                return true;
            }
        }
        return false;
    }

    /**
     * Compare two strings, case-insensitive
     */
    public static int stringsCompareTo(String a, String b){
        return a.toLowerCase(Locale.ROOT).compareTo(b.toLowerCase(Locale.ROOT));
    }

    /**
     * String.contains but case-insensitive
     */
    public static boolean stringsContain(String a, String b){
        return a.toLowerCase(Locale.ROOT).contains(b.toLowerCase(Locale.ROOT));
    }
}
