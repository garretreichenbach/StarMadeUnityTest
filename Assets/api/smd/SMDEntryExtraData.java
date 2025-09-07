package api.smd;

import com.google.gson.JsonObject;

/**
 * Data which can only be gotten from another GET request
 */
public class SMDEntryExtraData {
    String fileName;
    String downloadURL;
    int size;
    int id;

    public SMDEntryExtraData(JsonObject json) {
        downloadURL = json.get("download_url").getAsString();
        fileName = json.get("filename").getAsString();
        size = json.get("size").getAsInt();
        id = json.get("id").getAsInt();
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public int getSize() {
        return size;
    }

    public int getId() {
        return id;
    }
}
