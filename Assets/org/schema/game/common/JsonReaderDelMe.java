package org.schema.game.common;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

public class JsonReaderDelMe {
	public static void main(String[] args) {

		outputChecksums();
		outputIndex();
	}

	public static void outputIndex() {
		String fileToString;
		try {
			fileToString = FileUtil.fileToString(new FileExt("releasebuildindex.json"));
			JSONObject o = new JSONObject(fileToString);

			System.err.println("------------BUILDS; indexfileversion: " + o.getString("indexfileversion") + "; type: " + o.getString("type"));

			JSONArray jsonArray = o.getJSONArray("builds");

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				System.err.println("#" + i + ": " + jsonObject.getString("version") + "; Build " + jsonObject.getString("build") + "; Path " + jsonObject.getString("path"));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void outputChecksums() {
		String fileToString;
		try {

			fileToString = FileUtil.fileToString(new FileExt("checksums.json"));
			JSONObject o = new JSONObject(fileToString);

			System.err.println("------------CHECKSUMS; fileformatversion: " + o.getString("entryfileversion"));

			JSONArray jsonArray = o.getJSONArray("entries");

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				System.err.println("#" + i + ": " + jsonObject.getString("path") + "; " + jsonObject.getLong("size") + "; MD5 " + jsonObject.getString("MD5") + "; SHA1 " + jsonObject.getString("SHA1"));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
