package api.smd;

import api.DebugFile;
import api.SMModLoader;
import api.mod.*;
import api.mod.annotations.DoesNotWork;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.ImportFailedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 XF refers to "XenForo" the forum software SMD uses
 SMD refers to "StarMade Dock"

 * A utility class for getting mods from SMD
 */
public class SMDUtils {
    /**
     * Gets the JSON array of all mods ever made on SMD
     * @return
     */
    public static JsonArray getSMDMods() {
        //6 = XF id for category "mods"
        /* SMD Cache request*/
        return SMDCacheUtils.getJsonFromCategory(6);
        /* Legacy code for direct request
        try {
            HttpURLConnection get = GET("resource-categories/6/resources");
            return getJsonArray(IOUtils.toString(get.getInputStream(), "UTF-8"), "resources");
        } catch (IOException e) {
            e.printStackTrace();
            DebugFile.log("Could not get resource-categories/6/resources from server");
        }*/
    }

    public static JsonArray getJsonArray(String raw, String name) {
//        JsonParser parser = new JsonParser(); deprecated
//        JsonElement parse = parser.parse(raw);
        JsonObject jObject = JsonParser.parseString(raw).getAsJsonObject();
        return jObject.getAsJsonArray(name);
    }
    public static JsonArray getJsonArray(String raw){
//        return new JsonParser().parse(raw).getAsJsonArray();
        return JsonParser.parseString(raw).getAsJsonArray();
    }

    /**
     * Downloads a mod from SMD. Does not load it, simply puts it into a jar file.
     * @param modId The modidentifier to download
     */
    public static File downloadMod(ModIdentifier modId) throws IOException, NullPointerException {
        //Get download URL
        DebugFile.log("Attempting to download mod: " + modId);
        SMDModData instance = SMDModData.getInstance();
        SMDModInfo data = instance.getModData(modId.id);
        String downloadURL = data.getDownloadURL(modId);
        if(downloadURL == null)
            throw new RuntimeException("Mod download URL for " + modId + " was null.\n Likely the version is incorrect, check https://starmadedock.net/content/x."+modId.id+"/history to see if a version matches: " + modId.version);
        downloadURL = downloadURL.substring("https://starmadedock.net/api/".length());
        //Download to file
        InputStream stream = GETFromSMD(downloadURL).getInputStream();
        File destination = new File(SMModLoader.modFolder, data.getName() + "v" + modId.version + ".jar");
        FileUtils.copyInputStreamToFile(stream, destination);
        //Put in SMDModData
        SinglePlayerModData.getInstance().onDownloadedMod(modId, data.getReleaseDate(modId));
        return destination;
    }

    /**
     * Some entries have illegal filenames, such as " "Fair and Balanced" ", quotation marks are not valid
     * filenames on windows. This function makes it alphanumeric
     */
    public static String sanitizeName(String s){
        return s.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    /**
     * Downloads an SMENT from starmade dock. Not fully working.
     */
    @DoesNotWork
    public static void downloadAndLoadSMDBlueprint(int resId) throws IOException, ImportFailedException {
        SMDEntryData entry = SMDEntryData.fromResourceId(resId);

        InputStream stream = SMDUtils.GETRaw(entry.getExtraData().downloadURL).getInputStream();
        String fName = entry.getExtraData().getFileName();
        if(fName.endsWith(".sment")){
            File downloadsFolder = new File("moddata/StarLoader/downloadsTmp");
            downloadsFolder.mkdir();
            File downloadFile = new File(downloadsFolder.getAbsolutePath() + "/" + sanitizeName(entry.title) + ".sment");
            FileUtils.copyInputStreamToFile(stream, downloadFile);
            downloadFile.delete();
            downloadsFolder.delete();
            //Import the SMENT
            BluePrintController.active.importFile(downloadFile, null);
        }else if(fName.endsWith(".zip")){

        }else{

        }
    }


    //resource-categories/{id}/

    /**
     * Perform a GET request on starmade dock. Refer to the XenForo API documentation for more information.
     */
    public static HttpURLConnection GETFromSMD(String request) throws IOException {
        return GETRaw("https://starmadedock.net/api/" + request);
    }

    /**
     * Performs a raw GET request. Includes the XF API key and correct user agent for making requests to SMD.
     */
    public static HttpURLConnection GETRaw(String request) throws IOException {
        URL url = new URL(request);
        HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
        openConnection.setRequestMethod("GET");
        openConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        openConnection.setRequestProperty("XF-Api-Key", "RSVcV-pNXnzaZgHTths0Qd11WsNJ_EK7");
        openConnection.setRequestProperty("User-Agent", "StarMade-Client");
        return openConnection;
    }

    public static void main(String[] args) throws IOException {
        ModUpdater.checkUpdateAll();
    }

    /**
     * Gets a StarMade file, for example the StarMade.jar of the latest version.
     * Not currently used.
     */
    @Deprecated
    public static HttpURLConnection getSMFile(String request) throws IOException {
        URL url = new URL("https://files.star-made.org/" + request);
        HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
        openConnection.setRequestMethod("GET");
        openConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        openConnection.setRequestProperty("User-Agent", "StarMade-Client");
//        System.out.println("RCode: " + openConnection.getResponseCode());
//        System.out.println(openConnection.getResponseMessage());
        return openConnection;
//        System.out.println(text);
    }
}
