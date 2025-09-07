package org.schema.game.common.util;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.Locale;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class DesktopUtils {

	public static void openFolder(File file) {
		try {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(file);
			} else {
				String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
				if(os.contains("win")) {
					Runtime.getRuntime().exec("explorer.exe /select," + file.getAbsolutePath());
				} else if(os.contains("mac")) {
					Runtime.getRuntime().exec("open " + file.getAbsolutePath());
				} else if(os.contains("nix") || os.contains("nux")) {
					Runtime.getRuntime().exec("xdg-open " + file.getAbsolutePath());
				} else throw new UnsupportedOperationException("Unknown OS: " + os);
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void browseURL(URI path) {
		try {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(path);
			} else {
				String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
				if(os.contains("win")) {
					Runtime.getRuntime().exec("explorer.exe " + path.toString());
				} else if(os.contains("mac")) {
					Runtime.getRuntime().exec("open " + path.toString());
				} else if(os.contains("nix") || os.contains("nux")) {
					Runtime.getRuntime().exec("xdg-open " + path.toString());
				} else throw new UnsupportedOperationException("Unknown OS: " + os);
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}