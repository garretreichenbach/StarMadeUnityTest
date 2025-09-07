package api.mod.config;

import api.mod.ModSkeleton;
import api.mod.StarMod;

import java.util.Arrays;

import static api.mod.ModSkeleton.getVirtualMod;

/**
 * "testing" class for FileConfiguration
 */
public class FileConfiguration_test {
    public static void main(String[] args) {
        System.out.println("config write output should order values alphabetically");
        String[] keys=new String[]{"ccc","bbbb","a_a","Aaa","AbA"},
                values = new String[]{"1","2","3","4","5"};

        StarMod mock = new StarMod();
        ModSkeleton virtualMod = getVirtualMod("name here", "author here", "desc here", "1.0", false, mock);
        mock.setSkeleton(virtualMod);
        FileConfiguration config = new FileConfiguration(mock,"testconfig", Arrays.asList(keys), Arrays.asList(values));
        StringBuilder out = new StringBuilder();
        for (String s: config.getWriteLines()) {
            out.append(s);
        }

        String expected = "a_a: 3\n" +
                "Aaa: 4\n" +
                "AbA: 5\n" +
                "bbbb: 2\n" +
                "ccc: 1\n";
        assert expected.equals(out.toString());
    }
}
