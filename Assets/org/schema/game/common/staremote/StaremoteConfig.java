package org.schema.game.common.staremote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.resource.FileExt;

public class StaremoteConfig {
	public static final String path = "./staremote.cfg";
	private static final String passwdStr = "PASSWORD";
	private static final String enabledStr = "ENABLED";
	public static boolean enabled = false;
	public static String password = "default";

	private static void onNotExists() {
		File f = new FileExt("./staremote.cfg");
		f.delete();
		try {
			f.createNewFile();
			write();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void write() {
		try {
			Properties props = new Properties();
			props.setProperty(passwdStr, password);
			props.setProperty(enabledStr, String.valueOf(enabled));
			File f = new FileExt(path);
			OutputStream out = new FileOutputStream(f);
			props.store(out, "config file for the starmade remote server tool");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void read() {
		Properties props = new Properties();
		InputStream is = null;

		try {
			File f = new FileExt(path);
			if (!f.exists()) {
				onNotExists();
			}
			is = new FileInputStream(f);
		} catch (Exception e) {
			GuiErrorHandler.processErrorDialogException(e);
			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);

				}
			}
			return;
		}

		password = props.getProperty(passwdStr, "192.168.0.1");
		enabled = new Boolean(props.getProperty(enabledStr, "false"));
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}
}
