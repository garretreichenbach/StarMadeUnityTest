package api.mod;

import api.mod.config.ModConfigData;
import api.mod.config.ModControlData;
import api.mod.exception.ModDescriptionNotFoundException;
import api.mod.exception.ModInvalidMetadataException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.jakev.starloader.LaunchClassLoader;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The description of a mod, this contains name/dependency/etc info.
 * <p>
 * starMod are null if the mod is not loaded.
 * <p>
 * The actual mod content (StarMod), may not be loaded
 */
public class ModSkeleton {
	//================== Constants (Defined by mod.json) ===================
	//Info metadata
	private String modName;
	private String modAuthor;
	private String modDescription;
	private String modVersion;
	private String modSMVersion;
	private BufferedImage iconImage;
	private String starloaderVersion = "Not_Defined";
	private boolean hardLoadAllClasses = false;

	//Mod metadata
	private String mainClass;
	//Controls if a mod is client-only, meaning it will be enabled on servers if enabled.
	private boolean clientMod = false;
	//Controls if a mod is server-only, meaning it will not be synced between client and server. Basically a plugin.
	private boolean serverMod = false;
	private int smdResourceId = -1;
	ArrayList<Integer> dependencies = new ArrayList<>();

	//Core mod settings
	private boolean coreMod = false;
	private boolean requiresClassResize = false;

	//================== State data (Changed by mod loader) ==================
	private StarMod realMod = null;
	private boolean isLoaded = false;
	private boolean isEnabled = false;
	private File jarFile;
	private boolean isLocal = false;
	private boolean outOfDate = false;
	private LaunchClassLoader classLoader;
	private final HashMap<String, ModConfigData> configMap = new HashMap<>();
	private ModControlData controlData;

	public String getDebugName() {
		return modName + " (" + modVersion + ") [" + smdResourceId + "]";
	}

	public final File getResourcesFolder() {
		return new File("moddata/" + this.modName);
	}

	private ModSkeleton() {

	}

	public ModControlData getControls() {
		if(controlData == null) controlData = new ModControlData(this);
		return controlData;
	}

	public ModConfigData getConfigData(String name) {
		ModConfigData namedConfig = configMap.get(name);
		if(namedConfig == null) {
			ModConfigData newConfig = new ModConfigData(this, name);
			configMap.put(name, newConfig);
			return newConfig;
		}
		return namedConfig;
	}

	public HashMap<String, ModConfigData> getConfigs() {
		return configMap;
	}

	public static void main(String[] args) {
		Gson gson = new Gson();
		ModSkeleton virtualMod = getVirtualMod("name here", "author here", "desc here", "1.0", false, null);
		virtualMod.dependencies.add(123);
		virtualMod.dependencies.add(3456);
		System.err.println(gson.toJson(virtualMod));
	}

	/**
	 * Virtual Mods are mods that have no file, an example of this is DefaultMod
	 */
	public static ModSkeleton getVirtualMod(String name, String author, String description, String version, boolean forceEnable, StarMod container) {
		ModSkeleton skeleton = new ModSkeleton();
		skeleton.modName = name;
		skeleton.modAuthor = author;
		skeleton.modAuthor = author;
		skeleton.modDescription = description;
		skeleton.modVersion = version;
		skeleton.clientMod = forceEnable;
		skeleton.realMod = container;
		skeleton.smdResourceId = -1;
		container.setSkeleton(skeleton);
		return skeleton;
	}

	public static ModSkeleton fromJarFile(File file, boolean isLocal) throws IOException {
		ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
		ModSkeleton skeleton = new ModSkeleton();
		String rawJson = null;
		while(true) {
			ZipEntry entry = zipInput.getNextEntry();
			if(entry == null) break;
			if(entry.getName().endsWith("mod.json")) {
				rawJson = IOUtils.toString(zipInput);
			} else if(entry.getName().endsWith("modicon.png")) {
				skeleton.iconImage = ImageIO.read(zipInput);
			}
		}
		zipInput.close();
		if(rawJson == null) {
			throw new ModDescriptionNotFoundException(file);
		}
		JsonObject json = new JsonParser().parse(rawJson).getAsJsonObject();

		skeleton.jarFile = file;
		skeleton.isLocal = isLocal;
		try {
			skeleton.modName = json.get("name").getAsString();
			skeleton.modAuthor = json.get("author").getAsString();
			skeleton.modDescription = json.get("description").getAsString();
			skeleton.modVersion = json.get("version").getAsString();
			skeleton.modSMVersion = json.get("starmade_version").getAsString();
			skeleton.clientMod = json.get("client_mod").getAsBoolean();
			skeleton.serverMod = json.get("server_mod").getAsBoolean();
			skeleton.smdResourceId = json.get("smd_resource_id").getAsInt();
			skeleton.mainClass = json.get("main_class").getAsString();
			skeleton.starloaderVersion = getJsonString(json.get("starloader_version"));

			skeleton.coreMod = json.get("core_mod").getAsBoolean();
			skeleton.requiresClassResize = json.get("requires_class_resize").getAsBoolean();
			skeleton.hardLoadAllClasses = getJsonBoolean(json.get("hard_load_all_classes"));

			for(JsonElement elem : json.get("dependencies").getAsJsonArray()) {
				skeleton.dependencies.add(elem.getAsInt());
			}
		} catch(NullPointerException e) {
			e.printStackTrace();
			throw new ModInvalidMetadataException(rawJson, file);
		}
		return skeleton;
	}

	private static String getJsonString(JsonElement elem) {
		if(elem == null || elem.isJsonNull()) {
			return "";
		} else {
			return elem.getAsString();
		}
	}

	private static boolean getJsonBoolean(JsonElement elem) {
		if(elem == null || elem.isJsonNull()) {
			return false;
		} else {
			return elem.getAsBoolean();
		}
	}

	public String getStarLoaderVersion() {
		return starloaderVersion;
	}

	public void setOutOfDate(boolean outOfDate) {
		this.outOfDate = outOfDate;
	}

	public boolean isOutOfDate() {
		return outOfDate;
	}

	public String getName() {
		return modName;
	}

	public String getModAuthor() {
		return modAuthor;
	}

	public String getModDescription() {
		return modDescription;
	}

	public String getModVersion() {
		return modVersion;
	}

	public String getModSMVersion() {
		return modSMVersion;
	}

	public boolean isClientMod() {
		return clientMod;
	}

	public boolean isServerMod() {
		return serverMod;
	}

	public int getSmdResourceId() {
		return smdResourceId;
	}

	public ArrayList<Integer> getDependencies() {
		return dependencies;
	}

	public File getJarFile() {
		return jarFile;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public boolean isCoreMod() {
		return coreMod;
	}

	public boolean requiresClassResize() {
		return requiresClassResize;
	}

	public boolean isHardLoadAllClasses() {
		return hardLoadAllClasses;
	}

	public BufferedImage getIconImage() {
		return iconImage;
	}

	public StarMod getRealMod() {
		return realMod;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setRealMod(StarMod sMod) {
		realMod = sMod;
	}

	public String getMainClass() {
		return mainClass;
	}

	public ModSkeleton setLoaded(boolean loaded) {
		isLoaded = loaded;
		return this;
	}

	public LaunchClassLoader getClassLoader() {
		return classLoader;
	}

	public ModSkeleton setClassLoader(LaunchClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	public void flagEnabled(boolean b) {
		this.isEnabled = b;
	}


}
