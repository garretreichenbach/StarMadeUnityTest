package api.mod.config;

import api.DebugFile;
import api.mod.ModSkeleton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Data container for mod control data.
 *
 * @author TheDerpGamer
 */
public class ModControlData {

	private final ModSkeleton mod;
	public final HashMap<String, String[]> values = new HashMap<>();
	private final String configPath;

	public ModControlData(ModSkeleton mod) {
		this.mod = mod;
		configPath = "moddata" + File.separator + mod.getName() + "-controls.yml";
		reloadConfig();
	}

	public ModSkeleton getMod() {
		return mod;
	}

	public void reloadConfig() {
		values.clear();
		read();
	}

	private void read() {
		try {
			String configContent = convertFileToString(configPath, Charset.defaultCharset());
			configContent = configContent.trim().replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", ""); //Remove comments
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
						values.put(key.toString(), split[split.length - 1].split(Pattern.quote(" #")));
					} else values.put(split[0], split[1].split(Pattern.quote(" #")));
				}
			}
		} catch(NoSuchFileException exception) {
			DebugFile.warn("Config file: " + configPath + " not found, writing...");
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
			if(!file.exists()) {
				file.createNewFile();
			}
			FileWriter writer = new FileWriter(file);
			for(String line : getWriteLines()) writer.write(line);
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> getWriteLines() {
		List<String> keyList = new ArrayList<>(values.keySet());
		List<String> outList = new ArrayList<>();
		keyList.sort(Collator.getInstance());
		for(String key : keyList) {
			String[] value = values.get(key);
			StringBuilder line = new StringBuilder(key + ": ");
			for(int i = 0; i < value.length; i++) {
				line.append(value[i]);
				if(i != value.length - 1) line.append(" ");
			}
			outList.add(line.toString());
		}
		return outList;
	}
}
