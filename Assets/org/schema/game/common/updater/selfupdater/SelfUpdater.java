package org.schema.game.common.updater.selfupdater;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Map;
import java.util.Observable;

import javax.swing.SwingUtilities;

import org.schema.common.util.StringTools;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.common.util.DownloadCallback;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

public class SelfUpdater extends Observable {

	public String LAUNCHER_PATH = "http://files.star-made.org/StarMade-Starter";
	boolean running;
	private SelfUpdaterFrame updaterFrame;

	public static void main(String args[]) {
		SelfUpdater f = new SelfUpdater();
		f.runT();

	}

	private static String getJavaExec() {
		if (System.getProperty("os.name").equals("Mac OS X") || System.getProperty("os.name").contains("Linux")) {
			return "java";
		} else {
			return "javaw";
		}
	}

	public void runT() {
		running = true;
		SwingUtilities.invokeLater(() -> {
			if (GraphicsEnvironment.isHeadless()) {
				// non gui mode

			} else {
				// gui mode
				updaterFrame = new SelfUpdaterFrame();
				updaterFrame.setAlwaysOnTop(true);
				updaterFrame.setVisible(true);

			}

			running = false;
		});

		while (running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			downloadNewVersion();
		} catch (Exception e) {
			e.printStackTrace();
			if (!GraphicsEnvironment.isHeadless()) {
				GuiErrorHandler.processErrorDialogException(e);
			}
		}

		restartLauncher(new String[]{});

	}

	public void restartLauncher(final String[] args) {

		try {
			String a = "";
			for (int i = 0; i < args.length; i++) {
				a += " " + args[i];
			}

			String[] command = null;
			//-XX:+ShowMessageBoxOnError
			if (System.getProperty("os.name").equals("Mac OS X") || System.getProperty("os.name").contains("Linux")) {
				File starmadeFile = new FileExt("StarMade-Starter.jar");
				command = new String[]{getJavaExec(), "-Djava.net.preferIPv4Stack=true", "-jar", starmadeFile.getAbsolutePath(), a};

			} else {
				File starmadeFile = new FileExt("StarMade-Starter.exe");
				command = new String[]{starmadeFile.getAbsolutePath(), a};
			}

			System.err.println("RUNNING COMMAND: " + command);
			ProcessBuilder pb = new ProcessBuilder(command);
			Map<String, String> env = pb.environment();
			File file = new FileExt("./");
			pb.directory(file.getAbsoluteFile());
			Process p = pb.start();
			System.err.println("Exiting because selfupdater starting launcher");
			try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);

		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}

	}

	public void downloadNewVersion() throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
		String sourceFilePath = LAUNCHER_PATH;
		String localFile = "StarMade-Starter";
		if (System.getProperty("os.name").equals("Mac OS X") || System.getProperty("os.name").contains("Linux")) {
			sourceFilePath += ".jar";
			localFile += ".jar";
		} else {
			sourceFilePath += ".exe";
			localFile += ".exe";
		}

		URLConnection urlConnection = FileUtil.convertToURLEscapingIllegalCharacters(sourceFilePath).openConnection();
		//#RM1958 replace java7+ method urlConnection.getContentLengthLong()
		long s = urlConnection.getContentLength();
		System.err.println("File size to download: " + s);
		final long fileSize = s;
		File tmp = new FileExt(localFile + ".tmp");
		FileUtil.copyURLToFile(FileUtil.convertToURLEscapingIllegalCharacters(sourceFilePath), tmp, 50000, 50000, (size, diff) -> {
			if (updaterFrame != null) {
				updaterFrame.getProgressBar().setValue((int) (((float) size / (float) fileSize) * 100));
				updaterFrame.getProgressBar().setString("Downloading... " + StringTools.formatPointZero((size / 1000000d)) + "MB/" + StringTools.formatPointZero((fileSize / 1000000d)) + "MB    " + updaterFrame.getProgressBar().getValue() + " %");

			}
		}, "dev", "dev", true);
		File old = new FileExt(localFile);
		if (old.exists()) {
			old.delete();
		}
		tmp.renameTo(new FileExt(localFile));
	}

}
