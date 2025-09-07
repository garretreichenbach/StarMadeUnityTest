package org.schema.game.client.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Manages misc. user preferences.
 *
 * @author TheDerpGamer
 */
public class UserPreferencesManager {

	private static final String PREFERENCES_FILE_NAME = "./client-preferences.json";

	public static int[] getColorPalette() {
		if(getPreferences() == null || !getPreferences().has("color-palette")) loadDefaults();
		JSONArray colorPalette = Objects.requireNonNull(getPreferences()).getJSONArray("color-palette");
		int[] colors = new int[colorPalette.length()];
		for(int i = 0; i < colorPalette.length(); i++) colors[i] = colorPalette.getInt(i);
		return colors;
	}

	public static void setColorPalette(int[] colors) {
		JSONObject preferences = Objects.requireNonNull(getPreferences());
		JSONArray colorPalette = new JSONArray();
		for(int color : colors) colorPalette.put(color);
		preferences.put("color-palette", colorPalette);
		savePreferences(preferences);
	}

	private static JSONObject getPreferences() {
		try {
			File preferencesFile = new File(PREFERENCES_FILE_NAME);
			if(!preferencesFile.exists()) {
				preferencesFile.createNewFile();
				return loadDefaults();
			} else {
				FileReader reader = new FileReader(preferencesFile, StandardCharsets.UTF_8);
				JSONObject preferences = new JSONObject(new JSONTokener(reader));
				reader.close();
				return preferences;
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	private static JSONObject loadDefaults() {
		JSONObject defaultPreferences = new JSONObject();
		try {
			JSONArray colorPalette = new JSONArray();
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			colorPalette.put(0xFFFFFFFF);
			defaultPreferences.put("color-palette", colorPalette);
			savePreferences(defaultPreferences);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return defaultPreferences;
	}

	private static void savePreferences(JSONObject preferences) {
		try {
			File preferencesFile = new File(PREFERENCES_FILE_NAME);
			preferencesFile.createNewFile();
			FileWriter writer = new FileWriter(preferencesFile, StandardCharsets.UTF_8);
			writer.write(preferences.toString());
			writer.flush();
			writer.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
