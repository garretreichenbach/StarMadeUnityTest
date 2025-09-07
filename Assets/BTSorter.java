import api.utils.other.HashList;
import org.luaj.vm2.ast.Str;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BTSorter {
    public static void sortAndOutput() {

    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("data/config/BlockTypes.properties"));
        HashList<Integer, String> map = new HashList<>();
        while (sc.hasNext()) {
            String line = sc.nextLine();
            String[] split = line.split("=");
            if (split.length != 2) {
                continue;
            }
            map.add(Integer.parseInt(split[1]), split[0]);
        }
        ArrayList<Integer> ids = new ArrayList<>(map.keySet());
        Collections.sort(ids);
        for (Integer id : ids) {
            ArrayList<String> names = map.get(id);
            if (names.size() > 1) {
                names.forEach(name -> System.out.println("Duplicate: " + name + "=" + id));
            }
        }

        for (int i = 1; i < 2000; i++) {
            ArrayList<String> names = map.getList(i);
            if (names.size() > 1) {
                String toReplace = names.get(1);
                names.remove(toReplace);
                System.out.println("REmoved: " + toReplace);
                for (int j = 1; j < 2000; j++) {
                    ArrayList<String> list = map.getList(j);
                    if (list.isEmpty()) {
                        System.out.println("Reassigned: " + toReplace + " to " + j);
                        map.add(j, toReplace);
                        break;
                    }
                }

            }
        }

        System.err.println("=============");
        ids = new ArrayList<>(map.keySet());
        Collections.sort(ids);

        try {
            FileWriter writer = new FileWriter("data/config/BlockTypes.properties");
            writer.write("#\n" +
                    "#Sat Dec 15 22:50:09 CET 2018\n");
            for (Integer id : ids) {
                ArrayList<String> list = map.getList(id);
                if (list.size() > 1) {
                    throw new IllegalStateException();
                }
                if (list.isEmpty()) continue;
                String s = list.get(0);
                writer.write(s + "=" + id + "\n");
                System.err.println(s + "=" + id);
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
