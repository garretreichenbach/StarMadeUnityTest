package api.smd;

import com.google.gson.JsonArray;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 * Created by Jake on 10/29/2020.
 * <insert description here>
 */
public class SMDCacheUtils {
    public static final String blueprintTarURL = "https://starmadedock.net/cached-api/resource-categories/";
    public static void downloadBlueprints(){
        //Get tarball from cache

    }

    public static void main(String[] args) throws IOException {
        System.setProperty("https.protocols", "TLSv1.2");

        JsonArray jsonFromCategory = getJsonFromCategory(8);
        System.out.println(jsonFromCategory.toString());
    }
    public static JsonArray getJsonFromCategory(int category) {
        //https://generic-username.gitlab.io/starmadedockcache/2.txt.gz
        try {
            System.err.println("[SMDCacheUtils] Getting json from cat: " + category);
            URL url = new URL(blueprintTarURL + category + ".json.gz");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("XF-Api-Key", "RSVcV-pNXnzaZgHTths0Qd11WsNJ_EK7");
            conn.setRequestProperty("User-Agent", "StarMade-Client");
            System.err.println("[SMDCacheUtils] RCode: " + conn.getResponseCode());
            GZIPInputStream in = new GZIPInputStream(conn.getInputStream());
            String fullStr = IOUtils.toString(in);
            Scanner scanner = new Scanner(fullStr);
            boolean beginWrite = false;
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNext()) {
                String s = scanner.nextLine();
                if (s.startsWith("{")) {
                    beginWrite = true;
                }
                if (beginWrite) {
                    sb.append(s);
                }
            }
            JsonArray arr = SMDUtils.getJsonArray(sb.toString(), "resources");
            in.close();
            conn.disconnect();
            System.err.println("[SMDCacheUtils] Got Json from cat");
            return arr;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
