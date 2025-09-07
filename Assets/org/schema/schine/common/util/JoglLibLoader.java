package org.schema.schine.common.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.schema.common.util.data.DataUtil;
import org.schema.schine.resource.FileExt;

public class JoglLibLoader {

	private static void loadJoglLibs(JFrame f) {
		String curDir = System.getProperty("user.dir");
		System.out.println("current working dir: " + curDir);

		System.loadLibrary("nativewindow_awt");
		System.out.println("[JoglLibLoader] nativeWindow_AWT OK");
		//		System.loadLibrary("nativewindow_jvm");
		//		System.out.println("[JoglLibLoader] nativeWindow_JVM OK");

		System.loadLibrary("gluegen-rt");
		System.out.println("[JoglLibLoader] gluegen          OK");

		System.loadLibrary("newt");
		System.out.println("[JoglLibLoader] newt             OK");

		System.loadLibrary("jogl_desktop");
		System.out.println("[JoglLibLoader] jogl_desktop     OK");

	}

	public static void loadLibs() {

		String os = System.getProperty("os.name");
		String architecture = System.getProperties().getProperty("os.arch");

		String libPath = System.getProperties().getProperty("java.library.path");
		System.out.println("[JoglLibLoader] java library path: " + libPath);
		JFrame loadingFrame = new JFrame();
		try {

			String path = "";
			String sep = File.separator;
			String archTech = null;
			if (architecture.contains("64")) {
				archTech = "x64";
			} else {
				archTech = "x86";
			}

			if (os.toLowerCase(Locale.ENGLISH).contains("win")) {
				path = "lib" + sep + "win-" + archTech + sep;
			} else if (os.toLowerCase(Locale.ENGLISH).contains("nux") || os.contains("nix")) {
				path = "lib" + sep + "linux-" + archTech + sep;
			} else if (os.toLowerCase(Locale.ENGLISH).contains("mac")) {
				path = "lib" + sep + "mac-" + archTech + sep;
			} else {
				throw new UnsupportedOperationException("[ClientStarter] OS '" + os
						+ "' not yet supported on architecture " + architecture);
			}

			System.out
					.println("[JoglLibLoader][SYSTEM] loading openGL libraries for "
							+ os + "  " + architecture);

			try {
				loadJoglLibs(loadingFrame);
			} catch (UnsatisfiedLinkError e) {
				e.printStackTrace();

				File f = new FileExt("");

				System.out.println("[JoglLibLoader][SYSTEM] PLATFORM NOT YET INSTALLED.");
				System.out.println("[JoglLibLoader][SYSTEM] NOW INSTALLING REQUIRED LIBRARIES");
				File libDir = new FileExt(f.getAbsolutePath() + sep + path);
				if (libDir.exists() && libDir.isDirectory()) {
					System.out.println("Lib directory found: " + libDir.getAbsolutePath());
					File[] listFiles = libDir.listFiles((dir, name) -> {
						if (name.endsWith(".dll") || name.endsWith(".so")) {
							return true;
						}
						return false;
					});

					for (File lib : listFiles) {
						System.out.print("[JoglLibLoader] installing " + lib.getName() + " ... ");
						File dest = new FileExt(f.getAbsolutePath() + sep + lib.getName());
						DataUtil.copy(lib, dest);
						System.out.println(" installed to " + dest.getAbsolutePath());
					}
				} else {
					throw new IllegalAccessError("library dir " + libDir.getAbsolutePath() + " not found");
				}
				loadJoglLibs(loadingFrame);
			}
		} catch (Exception e) {
			System.err.println("EXEPTION WHILE LOADING LIBS");
			Object[] options = {"terminate"};
			int n = JOptionPane.showOptionDialog(loadingFrame, e
							.getMessage(), "Critical ERROR",
					JOptionPane.CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE, null, options,
					options[0]);
			switch(n) {
				default -> {
					e.printStackTrace();
					System.err.println("Exiting because of log lib exception");
					try {throw new Exception("System.exit() called");} catch(Exception ex) {ex.printStackTrace();}
					System.exit(-1);
				}
			}
		}
	}
}
