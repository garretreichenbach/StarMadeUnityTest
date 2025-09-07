package api.smd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Data container for an entry on StarMade Dock (Blueprint, etc)
 */
public class SMDEntryData {
	public boolean can_download;
	public int downloadCount;
	public String external_url;
	public String icon_url;
	public int last_update;
	public int prefix_id;
	public double price;
	public double rating_average;
	public int rating_count;
	public double rating_weighted;
	public int resource_category;
	public int resource_date;
	public int resource_id;
	public String resource_state;
	public String resource_type;
	public String tag_line;
	public ArrayList<String> tags = new ArrayList<>();
	public String gameVersion;
	public String title;
	public int user_id;
	public String username;
	public String version;
	public int view_count;

	private static String getJsonString(JsonElement element) {
		if(element == null || element.isJsonNull()) {
			return null;
		}
		return element.getAsString();
	}

	private static double getJsonDouble(JsonElement element) {
		if(element == null || element.isJsonNull()) {
			return 0;
		}
		return element.getAsDouble();
	}

	private static int getJsonInt(JsonElement element) {
		if(element == null || element.isJsonNull()) {
			return 0;
		}
		return element.getAsInt();
	}

	private static boolean getJsonBoolean(JsonElement element) {
		if(element == null || element.isJsonNull()) {
			return false;
		}
		return element.getAsBoolean();
	}

	public static SMDEntryData getBlankInstance() {
		SMDEntryData smdEntryData = new SMDEntryData();
		return smdEntryData;
	}

	private SMDEntryData() {

	}

	public static SMDEntryData fromResourceId(int resId) throws IOException {
		HttpURLConnection get = SMDUtils.GETFromSMD("resources/a." + resId);
		String raw = IOUtils.toString(get.getInputStream());
		JsonElement parse = new JsonParser().parse(raw);
		return new SMDEntryData(parse.getAsJsonObject().get("resource").getAsJsonObject(), false);
	}

	public SMDEntryData(JsonObject json) {
		this(json, true);
	}

	public SMDEntryData(JsonObject json, boolean trimmed) {
		can_download = getJsonBoolean(json.get("can_download"));
		gameVersion = getJsonString(json.get("custom_fields").getAsJsonObject().get("Gameversion"));
		downloadCount = getJsonInt(json.get("download_count"));
		external_url = getJsonString(json.get("external_url"));
		icon_url = getJsonString(json.get("icon_url"));
		last_update = getJsonInt(json.get("last_update"));
		prefix_id = getJsonInt(json.get("prefix_id"));
		price = getJsonDouble(json.get("price"));
		rating_average = getJsonDouble(json.get("rating_avg"));
		rating_count = getJsonInt(json.get("rating_count"));
		rating_weighted = getJsonDouble(json.get("rating_weighted"));
		resource_category = getJsonInt(json.get("resource_category_id"));
		resource_date = getJsonInt(json.get("resource_date"));
		resource_id = getJsonInt(json.get("resource_id"));
		resource_state = getJsonString(json.get("resource_state"));
		resource_type = getJsonString(json.get("resource_type"));
		tag_line = getJsonString(json.get("tag_line"));
		for(JsonElement tag : json.get("tags").getAsJsonArray()) {
			tags.add(tag.getAsString());
		}
		title = getJsonString(json.get("title"));
		user_id = getJsonInt(json.get("user_id"));
		username = getJsonString(json.get("username"));
		version = getJsonString(json.get("version"));
		view_count = getJsonInt(json.get("view_count"));
		//Trimmed json does not contain the download url
		if(!trimmed) {
			extraData = new SMDEntryExtraData(json.get("current_files").getAsJsonArray().get(0).getAsJsonObject());
		}
	}

	@Override
	public String toString() {
		return "SMDEntryData{" + "can_download=" + can_download + ", downloadCount=" + downloadCount + ", external_url='" + external_url + '\'' + ", icon_url='" + icon_url + '\'' + ", last_update=" + last_update + ", prefix_id=" + prefix_id + ", price=" + price + ", rating_average=" + rating_average + ", rating_count=" + rating_count + ", rating_weighted=" + rating_weighted + ", resource_category=" + resource_category + ", resource_date=" + resource_date + ", resource_id=" + resource_id + ", resource_state='" + resource_state + '\'' + ", resource_type='" + resource_type + '\'' + ", tag_line='" + tag_line + '\'' + ", tags=" + tags + ", gameVersion='" + gameVersion + '\'' + ", title='" + title + '\'' + ", user_id=" + user_id + ", username='" + username + '\'' + ", version='" + version + '\'' + ", view_count=" + view_count + '}';
	}

	private SMDEntryExtraData extraData = null;

	public SMDEntryExtraData getExtraData() throws IOException, NullPointerException {
		if(extraData == null) {
			//Fetch download url, etc.
			HttpURLConnection con = SMDUtils.GETFromSMD("resources/" + resource_id);
			JsonParser parser = new JsonParser();
			String raw = IOUtils.toString(con.getInputStream(), "UTF-8");
			JsonObject parse = parser.parse(raw).getAsJsonObject();
			JsonObject resObject = parse.get("resource").getAsJsonObject();
			JsonArray currentFileArray = resObject.get("current_files").getAsJsonArray();
			JsonObject firstDownload = currentFileArray.get(0).getAsJsonObject();
			extraData = new SMDEntryExtraData(firstDownload);
		}
		return extraData;
	}

	public static void main(String[] args) throws IOException {
		HttpURLConnection get = SMDUtils.GETFromSMD("resources/6876");
		System.out.println(IOUtils.toString(get.getInputStream()));
	}

}
