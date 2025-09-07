package api.mod;

import api.smd.SMDUtils;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

//Scraped info put into a class
public class SMDModInfo {
    private int resourceId;
    private int resourceDate;
    private String gameVersion;
    private int downloadCount;
    private float ratingAverage;
    private String tagLine;
    private ArrayList<String> tags;
    private String name;
    private String iconURL;
    private String username;
    private ArrayList<SMDModVersionInfo> versions = new ArrayList<>();

    public static SMDModInfo fromJson(JsonObject jsonModObject) {
        SMDModInfo inst = new SMDModInfo();
        inst.name = jsonModObject.get("title").getAsString();
        inst.resourceId = jsonModObject.get("resource_id").getAsInt();
        inst.resourceDate = jsonModObject.get("last_update").getAsInt();
        JsonElement element = jsonModObject.get("custom_fields").getAsJsonObject().get("Gameversion");
        if (element == null) {
            inst.gameVersion = "?";
        } else {
            inst.gameVersion = element.getAsString();
        }
        inst.downloadCount = jsonModObject.get("download_count").getAsInt();
        inst.ratingAverage = jsonModObject.get("rating_avg").getAsFloat();
        inst.tagLine = jsonModObject.get("tag_line").getAsString();
        inst.username = jsonModObject.get("username").getAsString();
        JsonArray tags = jsonModObject.get("tags").getAsJsonArray();
        ArrayList<String> tagArray = new ArrayList<String>(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            tagArray.add(tags.get(i).getAsString());
        }
        inst.tags = tagArray;
        JsonElement url = jsonModObject.get("icon_url");
        if (!(url instanceof JsonNull)) {
            inst.iconURL = url.getAsString();
        }

        return inst;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getResourceDate() {
        return resourceDate;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public float getRatingAverage() {
        return ratingAverage;
    }

    public String getTagLine() {
        return tagLine;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public String getIconURL() {
        return iconURL;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "SMDModInfo{" +
                "resourceId=" + resourceId +
                ", resourceDate=" + resourceDate +
                ", gameVersion='" + gameVersion + '\'' +
                ", downloadCount=" + downloadCount +
                ", ratingAverage=" + ratingAverage +
                ", tagLine='" + tagLine + '\'' +
                ", tags=" + tags +
                ", name='" + name + '\'' +
                ", iconURL='" + iconURL + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
    public void fetchVersionInfo() throws IOException {
        HttpURLConnection con = SMDUtils.GETFromSMD("resources/" + this.resourceId + "/versions");
        JsonParser parser = new JsonParser();
        String raw = IOUtils.toString(con.getInputStream(), "UTF-8");
        JsonObject parse = parser.parse(raw).getAsJsonObject();

        for (JsonElement elem : parse.get("versions").getAsJsonArray()) {
            try {
                System.err.println("Raw: " + elem.toString());
                JsonObject obj = elem.getAsJsonObject();
                String ver = obj.get("version_string").getAsString();
                int date = obj.get("release_date").getAsInt();
                String url = obj.get("files").getAsJsonArray().get(0).getAsJsonObject().get("download_url").getAsString();
                versions.add(new SMDModVersionInfo(ver, url, date));
            }catch (NullPointerException e){
                System.err.println("[SMDModInfo] Could not parse mod history info. Shouldn't be a problem.");
            }
        }
//        JsonObject resObject = parse.get("resource").getAsJsonObject();
//        JsonArray currentFileArray = resObject.get("current_files").getAsJsonArray();
//        for (JsonElement elem : currentFileArray) {
//            JsonObject obj = elem.getAsJsonObject();
//            //todo probably not right
//            System.err.println(obj.toString());
//            SMDModVersionInfo info = new SMDModVersionInfo(obj.get("version").getAsString(),
//                    obj.get("download_url").getAsString(),
//                    obj.get("date").getAsInt());
//            versions.add(info);
//        }
    }
    public String getLatestDownloadVersion(){
        tryFetchVersion();
        return versions.get(0).version;
    }
    public int getReleaseDate(ModIdentifier id){
        tryFetchVersion();
        for (SMDModVersionInfo version : versions) {
            if(version.version.equals(id.version)){
                return version.date;
            }
        }
        return 0;
    }
    private void tryFetchVersion(){
        if(versions.isEmpty()) {
            try {
                fetchVersionInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getDownloadURL(ModIdentifier id){
        tryFetchVersion();
        for (SMDModVersionInfo version : versions) {
            if(version.version.equals(id.version)){
                return version.downloadURL;
            }
        }
        return null;
    }
}
class SMDModVersionInfo {
    String version;
    String downloadURL;
    int date;

    public SMDModVersionInfo(String version, String downloadURL, int date) {
        this.version = version;
        this.downloadURL = downloadURL;
        this.date = date;
    }
}
