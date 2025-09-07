package api.smd;

import api.DebugFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for various SMD data
 */
public class SMDEntryUtils {
	private static boolean called = false;

	public static void fetchDataOnThread() {
		if(!called) {
			called = true;
			new Thread(() -> {
				try {
					downloadBlueprintInfos();
				} catch(Exception e) {
					e.printStackTrace();
				}
				//Set finished if it throws an exception
				while(true) {
					boolean b = finishedDownloading.compareAndSet(false, true);
					if(b) break;
				}
			}).start();
			new Thread(SMDEntryUtils::downloadTemplateInfos).start();
		}
	}

	private final static AtomicBoolean finishedDownloading = new AtomicBoolean(false);

	public static boolean isFinishedDownloading() {
		return finishedDownloading.get();
	}

	private final static HashMap<String, SMDEntryData> nameData = new HashMap<>();
	private final static HashMap<String, SMDEntryData> templateData = new HashMap<>();

	private static JsonArray getCategory(int id) {
		try {
//            get = SMDUtils.GET("");
			HttpURLConnection get = SMDUtils.GETFromSMD("resource-categories/" + id + "/resources");//
			System.out.println("Response code from server: " + get.getResponseCode());
			return SMDUtils.getJsonArray(IOUtils.toString(get.getInputStream(), "UTF-8"), "resources");
		} catch(IOException e) {
			e.printStackTrace();
			DebugFile.log("Could not get resource-categories/" + id + "/resources from server");
		}
		return null;
	}

	public static final int[] BLUEPRINT_IDS = new int[] {2, 3, 7, 8}; // 2 = 11+12+15+more
	public static final int TEMPLATE_CATEGORY_ID = 9;

	public static void downloadBlueprintInfos() {
		for(int blueprintId : BLUEPRINT_IDS) {
			for(JsonElement resourceElement : SMDCacheUtils.getJsonFromCategory(blueprintId)) {
				SMDEntryData data = new SMDEntryData(resourceElement.getAsJsonObject());
				nameData.put(data.title, data);
			}
		}
	}

	public static void downloadTemplateInfos() {
		for(JsonElement resourceElement : SMDCacheUtils.getJsonFromCategory(TEMPLATE_CATEGORY_ID)) {
			SMDEntryData data = new SMDEntryData(resourceElement.getAsJsonObject());
			nameData.put(data.title, data);
		}
	}

	public static final HashMap<String, SMDEntryData> nullMap = new HashMap<>();

	static {
		SMDEntryData blankInstance = SMDEntryData.getBlankInstance();
		blankInstance.title = "Downloading Entries from StarmadeDock, please wait!";
		blankInstance.tag_line = "...";
		nullMap.put("Downloading Entries from StarmadeDock, please wait!", blankInstance);
	}

	public static HashMap<String, SMDEntryData> getAllBlueprints() {
		if(isFinishedDownloading()) {
			return nameData;
		}
		return nullMap;
	}

	public static HashMap<String, SMDEntryData> getAllTemplates() {
		if(isFinishedDownloading()) {
			return templateData;
		}
		return nullMap;
	}

	public static void main(String[] args) throws IOException {
		for(JsonElement resourceElement : getCategory(11)) {
			System.out.println(resourceElement.getAsJsonObject().toString());
			break;
//            SMDEntryData data = new SMDEntryData(resourceElement.getAsJsonObject());
//                System.err.println("[StarLoader SMDBlueprintData] Fetched resource from SMD: " + data);
//            nameData.put(data.title, data);
		}
	}
}
