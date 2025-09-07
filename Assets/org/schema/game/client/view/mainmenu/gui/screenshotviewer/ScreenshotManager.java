package org.schema.game.client.view.mainmenu.gui.screenshotviewer;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.common.JsonSerializable;
import org.schema.common.util.data.DataUtil;
import org.schema.game.common.util.DesktopUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

public class ScreenshotManager {

	private static final HashSet<ScreenshotData> screenshots = new HashSet<>();
	private static File lastScreen;

	public static File getRandomLoadingScreen() {
		File loadingScreensPath = new File(DataUtil.dataPath + "image-resource/loading-screens");
		if(loadingScreensPath.exists() && loadingScreensPath.isDirectory()) {
			File[] files = loadingScreensPath.listFiles();
			if(files != null && files.length > 0) {
				int randomIndex = (int) (Math.random() * files.length);
				File randomFile = files[randomIndex];
				if(randomFile.exists()) {
					return randomFile;
				}
			}
		}
		//Fallback to default loading screen
		File defaultLoadingScreen = new File(DataUtil.dataPath + "image-resource/loadingscreen-background.png");
		if(defaultLoadingScreen.exists()) {
			return defaultLoadingScreen;
		}
		//If all else fails, return null
		return null;
		//Todo: Fix screenshot manager
//		HashSet<ScreenshotData> loadingScreens = getFavorites();
//		if(loadingScreens.isEmpty()) loadingScreens = getScreenshots();
//		if(loadingScreens.isEmpty()) return loadFallbackScreen();
//		else {
//			try {
//				ScreenshotData[] loadingScreensArray = loadingScreens.toArray(new ScreenshotData[0]);
//				ScreenshotData randomScreen = loadingScreensArray[(int) (Math.random() * loadingScreensArray.length)];
//				if(randomScreen == null) return loadFallbackScreen();
//				File file = randomScreen.getFile();
//				if(file == null || !file.exists()) return loadFallbackScreen();
//				if(file.equals(lastScreen)) return getRandomLoadingScreen();
//				else {
//					lastScreen = file;
//					return file;
//				}
//			} catch(Exception exception) {
//				exception.printStackTrace();
//			}
//		}
//		return loadFallbackScreen();
	}

	private static File loadFallbackScreen() {
		return new File(DataUtil.dataPath + "image-resource/loadingscreen-background.png");
	}

	public static HashSet<ScreenshotData> getScreenshots() {
		load();
		return screenshots;
	}

	public static HashSet<ScreenshotData> getFavorites() {
		load();
		HashSet<ScreenshotData> favorites = new HashSet<>();
		for(ScreenshotData data : screenshots) if(data.isFavorite()) favorites.add(data);
		return favorites;
	}

	private static void loadDefaultScreenshots() {
		File loadingScreensPath = new File(DataUtil.dataPath + "image-resource/loading-screens");
		if(loadingScreensPath.exists() && loadingScreensPath.isDirectory()) {
			for(File file : Objects.requireNonNull(loadingScreensPath.listFiles())) {
				if(file.getName().endsWith(".png")) {
					try {
						screenshots.add(new ScreenshotData(file));
					} catch(IOException exception) {
						exception.printStackTrace();
					}
				}
			}
		}
	}

	private static void loadExistingScreenshots() {
		File screenshotsPath = new File("./screenshots");
		if(screenshotsPath.exists() && screenshotsPath.isDirectory()) {
			for(File file : Objects.requireNonNull(screenshotsPath.listFiles())) {
				if(file.getName().endsWith(".png")) {
					try {
						screenshots.add(new ScreenshotData(file));
					} catch(IOException exception) {
						exception.printStackTrace();
					}
				}
			}
		}
	}

	private static void load() {
		try {
			screenshots.clear();
			loadDefaultScreenshots();
			loadExistingScreenshots();
			save();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

//	private static void loadMissingScreenshots() {
//		try {
//			File screenshotsFolder = new File("./screenshots");
//			if(!screenshotsFolder.exists()) screenshotsFolder.mkdirs();
//			//Load any screenshots that aren't already registered
//			for(File file : Objects.requireNonNull(screenshotsFolder.listFiles())) {
//				if(file.getName().endsWith(".png")) {
//					boolean found = false;
//					for(ScreenshotData data : screenshots) {
//						if(data.getFile().getName().equals(file.getName())) {
//							found = true;
//							break;
//						}
//					}
//					if(!found) screenshots.add(new ScreenshotData(file));
//				}
//			}
//		} catch(Exception exception) {
//			exception.printStackTrace();
//		}
//	}

	private static void save() {
		try {
			File file = new File("./screenshots/screenshots.json");
			JSONArray array = new JSONArray();
			for(ScreenshotData data : screenshots) array.put(data.toJson());
			FileUtils.write(file, array.toString(4), "UTF-8");
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
	public static void addData(ScreenshotData data) {
		screenshots.add(data);
		save();
	}

	public static void removeData(ScreenshotData data) {
		screenshots.remove(data);
		if(data.getFile().exists()) data.getFile().delete();
		save();
	}

	public static class ScreenshotData implements JsonSerializable, Comparable<ScreenshotData>, ClipboardOwner {

		private File file;
		private BufferedImage image;
		private boolean favorite;

		public ScreenshotData(File file) throws IOException {
			this.file = file;
			image = ImageIO.read(file);
			favorite = false;
		}

		public ScreenshotData(File file, boolean favorite) throws IOException {
			this.file = file;
			this.favorite = favorite;
			image = ImageIO.read(file);
		}

		public ScreenshotData(JSONObject json) {
			fromJson(json);
		}

		@Override
		public JSONObject toJson() {
			JSONObject json = new JSONObject();
			json.put("file", file.getAbsolutePath());
			json.put("favorite", favorite);
			return json;
		}

		@Override
		public void fromJson(JSONObject json) {
			file = new File(json.getString("file"));
			favorite = json.getBoolean("favorite");
			try {
				image = ImageIO.read(file);
			} catch(IOException exception) {
				exception.printStackTrace();
				System.err.println("Screenshot not found: " + file.getAbsolutePath() + ", removing from list.");
				removeData(this);
			}
		}

		@Override
		public int compareTo(ScreenshotData o) {
			if(o.favorite && !favorite) return 1;
			if(favorite && !o.favorite) return -1;
			return file.getName().compareTo(o.file.getName());
		}

		@Override
		public int hashCode() {
			return file.hashCode();
		}

		public File getFile() {
			return file;
		}

		public BufferedImage getImage() {
			return image;
		}

		public boolean isFavorite() {
			return favorite;
		}

		public void setFavorite(boolean favorite) {
			this.favorite = favorite;
			save();
		}

		/**
		 * Attempts to copy the image to the clipboard.
		 */
		public void copyToClipboard() {
			try {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new Transferable() {
					@Override
					public DataFlavor[] getTransferDataFlavors() {
						return new DataFlavor[]{DataFlavor.imageFlavor};
					}

					@Override
					public boolean isDataFlavorSupported(DataFlavor flavor) {
						return flavor.equals(DataFlavor.imageFlavor);
					}

					@Override
					public Object getTransferData(DataFlavor flavor) {
						return image;
					}
				}, this);
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}

		/**
		 * Attempts to open the image in the default image viewer for the system.
		 */
		public void open() {
			try { //No idea if this works on Mac or Linux, as I only have a Windows machine to test on
				String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
				if(OS.contains("win")) Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + file.getAbsolutePath());
				else if(OS.contains("mac")) Runtime.getRuntime().exec("open " + file.getAbsolutePath());
				else if(OS.contains("nix") || OS.contains("nux")) Runtime.getRuntime().exec("xdg-open " + file.getAbsolutePath());
			} catch(IOException exception) {
				exception.printStackTrace();
				openInFileExplorer();
			}
		}

		public void openInFileExplorer() {
			try {
				String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
				if(OS.contains("win")) Runtime.getRuntime().exec("explorer.exe /select," + file.getAbsolutePath());
				else if(OS.contains("mac")) Runtime.getRuntime().exec("open -R " + file.getAbsolutePath());
				else if(OS.contains("nix") || OS.contains("nux")) Runtime.getRuntime().exec("xdg-open " + file.getParent());
			} catch(IOException exception) {
				exception.printStackTrace();
				//Just open the ./screenshots folder
				File screenshotsPath = new File("./screenshots");
				if(screenshotsPath.exists() && screenshotsPath.isDirectory()) {
					DesktopUtils.openFolder(screenshotsPath);
				}
			}
		}

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			//Nothing to do here
		}

		public String getName() {
			return file.getName().substring(0, file.getName().lastIndexOf('.'));
		}
	}
}
