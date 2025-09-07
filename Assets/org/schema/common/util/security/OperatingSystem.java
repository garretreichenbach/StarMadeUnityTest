package org.schema.common.util.security;

import org.schema.schine.resource.FileExt;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public enum OperatingSystem {
	LINUX("linux", "unix"),
	SOLARIS("sunos", "solaris"),
	WINDOWS("win"),
	MAC("mac"),
	UNKNOWN;

	public static OperatingSystem OS;
	public final String[] ids;
	public String serial;

	OperatingSystem(String... ids) {
		this.ids = ids;
	}

	public static File getAppDir() throws IOException {
		try {
			return getAppDir("StarMade");
		} catch(IOException exception) {
			exception.printStackTrace();
			System.err.println("Warning: Failed to locate home directory! This can be due to any number of OS-related reasons, so we must fall back to the default location.");
			//This doesn't work on some OS's for some reason.
			//Sometimes it's in /home/username/.var/app/com.valvesoftware.Steam/.StarMade, sometimes it's somewhere else, and it's impossible to find out where.
			//So, we have to store it in the games folder and mark it as hidden in this case.
			File file = new FileExt(new FileExt("../").getAbsolutePath(), ".cache");
			file.mkdirs();
			return file;
		}
	}

	/**
	 * @param appName
	 * @return the working dir for the appName in the specific OS home directory
	 * @throws IOException
	 */

	public static File getAppDir(String appName) throws IOException {
		String s = System.getProperty("user.home", ".");
		File file;
		switch(getOS()) {
			case WINDOWS:
				String s1 = System.getenv("APPDATA");
				if(s1 != null) {
					file = new FileExt(s1, "." + appName + '/');
				} else {
					file = new FileExt(s, '.' + appName + '/');
				}
				break;
			case MAC:
				file = new FileExt(s, "Library/Application Support/" + appName);
				break;
			case LINUX:
			case SOLARIS:
				file = new FileExt(s, '.' + appName + '/');
				break;
			default:
				file = new FileExt(s, appName + '/');
				break;
		}

		if(!file.exists() && !file.mkdirs()) {
			throw new IOException("Error: Failed to create working directory: " + file);
		} else if(!file.isDirectory()) {
			throw new IOException("Error: File is not a directory: " + file);
		} else {
			return file;
		}
	}

	public static OperatingSystem getOS() {
		if(OS == null) {
			String s = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
			OS = UNKNOWN;
			for(OperatingSystem os : values()) {
				for(int i = 0; i < os.ids.length; i++) {
					if(s.contains(os.ids[i])) {
						try {
							os.serial = "not retrieved";
//							System.err.println("READ SERIAL for "+os.name()+": "+os.serial);
						} catch(Exception e) {
							e.printStackTrace();
						}
						OS = os;
						return OS;
					}
				}
			}
		}
		return OS;
	}
}