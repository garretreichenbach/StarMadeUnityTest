package api.mod.config;

import api.mod.ModSkeleton;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Data container for converting mod FileConfigurations into something that can be displayed in a GUI.
 *
 * @author TheDerpGamer
 */
public class ModConfigData {

	private final ModSkeleton mod;
	private final String configName;
	private final String configPath;
	public JSONObject config;

	public ModConfigData(ModSkeleton mod, String configName) {
		this.mod = mod;
		this.configName = configName;
		if(!configName.endsWith(".json")) configName += ".json";
		configPath = "moddata" + File.separator + configName;
		reloadConfig();
	}

	public String getName() {
		return configName;
	}

	public ModSkeleton getMod() {
		return mod;
	}

	public void reloadConfig() {
		config = new JSONObject();
		read();
	}

	private void read() {
		try {
			String configContent = convertFileToString(configPath, Charset.defaultCharset());
			configContent = configContent.trim().replaceAll("/\\*(?:[^*]|\\*+[^*/])*\\*+/|//.*", ""); //Remove comments
			Scanner scanner = new Scanner(configContent);
			while(scanner.hasNext()) {
				String next = scanner.nextLine();
				//Format: key: value #description
				if(!next.isEmpty() && next.charAt(0) != '#') {
					String[] split = next.split(Pattern.quote(": "));
					if(split.length > 2) {
						StringBuilder key = new StringBuilder();
						for(int i = 0; i < split.length - 1; i++) {
							key.append(split[i]);
							if(i != split.length - 2) key.append(": ");
						}
						String value = split[split.length - 1];
						String[] description = new String[0];
						if(value.contains("#")) {
							description = value.split(Pattern.quote(" #"));
							value = description[0];
							description = new String[] {description[1]};
						}
						config.put(key.toString(), new String[] {value});
						if(description.length > 0) config.put(key + "_description", description);
					} else {
						String key = split[0];
						String value = split[1];
						String[] description = new String[0];
						if(value.contains("#")) {
							description = value.split(Pattern.quote(" #"));
							value = description[0];
							description = new String[] {description[1]};
						}
						config.put(key, new String[] {value});
						if(description.length > 0) config.put(key + "_description", description);
					}
				}
			}
		} catch(NoSuchFileException exception) {
			exception.printStackTrace();
			saveConfig();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	private String convertFileToString(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public void saveConfig() {
		try {
			File file = new File(configPath);
			file.getParentFile().mkdirs();
			if(!file.exists()) file.createNewFile();
			FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);
			writer.write(config.toString());
			writer.close();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
}
