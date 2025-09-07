package org.schema.game.common.util;

import org.schema.game.common.crashreporter.CrashReporter;
import org.schema.schine.resource.FileExt;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class GuiErrorHandler {
	public static void exitInfoDialog(String msg) {
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		Object[] options = {"OK"};
		String title = "Information";
		final JFrame jFrame = new JFrame(title);
		jFrame.setUndecorated(true); // set frame undecorated, so the frame
		// itself is invisible
		SwingUtilities.invokeLater(() -> jFrame.setVisible(true));
		// appears in the task bar
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		jFrame.setLocation(screenSize.width / 2, screenSize.height / 2);
		int n = JOptionPane.showOptionDialog(jFrame, msg, title,
				JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, options, options[0]);
		switch(n) {
			case 0 -> {
				System.err.println("Exiting because of exit info dialog");
				try {
					CrashReporter.createThreadDump();
				} catch(IOException e1) {
					e1.printStackTrace();
				}
				try {throw new Exception("System.exit() called");} catch(Exception ex) {ex.printStackTrace();}
				System.exit(0);
			}
		}
		jFrame.dispose();
	}

	public static void processErrorDialogException(Exception e) {
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		Object[] options = {"Continue", "Create a help ticket", "Exit"};
		e.printStackTrace();
		String title = "Critical Error";
		JFrame jFrame = new JFrame(title);
		jFrame.setUndecorated(true); // set frame undecorated, so the frame
		// itself is invisible
		jFrame.setVisible(true); // the frame must be set to visible, so it
		jFrame.setAlwaysOnTop(true);
		// appears in the task bar
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		String extraMessage = "";

		if ((e.getMessage() != null && e.getMessage().contains("Database lock acquisition"))) {
			extraMessage += "\n\nIMPORTANT NOTE: this crash happens when there is still an instance of the game running\n" +
					"check your process manager for \"javaw.exe\" and terminate it, or restart your computer to solve this problem.";

			;
		}

		if (extraMessage.length() == 0) {
			extraMessage = "To get help, please create a ticket at help.star-made.org\n\nIf this message is about a File not Found, please try to repair or reinstall with your launcher";
		}

		jFrame.setLocation(screenSize.width / 2, screenSize.height / 2);
		int n = JOptionPane.showOptionDialog(jFrame, e.getClass().getSimpleName() + ": " + e.getMessage() + "\n\n " +
						extraMessage + "\n\n"
				, title,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
				null, options, options[0]);
		switch (n) {
			case 0:
				break;
			case 1:
				try {
					openWebpage(new URL("http://help.star-made.org"));
				} catch (MalformedURLException e2) {
					e2.printStackTrace();
				}
				break;
			case 2:
				System.err.println("[GLFrame] Error Message: " + e.getMessage());
				//			try {
				//				cleanUp();
				//			} catch (ErrorDialogException e1) {
				//				e1.printStackTrace();
				//			}
				try {
					CrashReporter.createThreadDump();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}
				System.exit(0);
			case 3:
				break;

		}
		jFrame.dispose();
	}
	public static void openWebpage(URI uri) {
	    DesktopUtils.browseURL(uri);
	}

	public static void openWebpage(URL url) {
	    try {
	        openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	}
	public static void processNormalErrorDialogException(Exception e, boolean printExceptionClass) {
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		Object[] options = {"Ok", "Exit"};
		e.printStackTrace();
		String title = "Error";
		final JFrame jFrame = new JFrame(title);
		jFrame.setAlwaysOnTop(true);
		jFrame.setUndecorated(true); // set frame undecorated, so the frame
		// itself is invisible
		SwingUtilities.invokeLater(() -> jFrame.setVisible(true));
		// appears in the task bar
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		jFrame.setLocation(screenSize.width / 2, screenSize.height / 2);
		int n = JOptionPane.showOptionDialog(jFrame, (printExceptionClass ? e.getClass().getSimpleName() : "") + e.getMessage(), title,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
				null, options, options[0]);
		switch (n) {
			case 0:
				break;
			case 1:
				System.err.println("[GLFrame] Error Message: " + e.getMessage());
				//			try {
				//				cleanUp();
				//			} catch (ErrorDialogException e1) {
				//				e1.printStackTrace();
				//			}
				try {
					CrashReporter.createThreadDump();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
			case 2:
				break;

		}
		jFrame.dispose();
	}

	public static void startCrashReporterInstance(final String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {

				String a = "";
				for (int i = 0; i < args.length; i++) {
					a += " " + args[i];
				}
				String starMadePath = "./CrashAndBugReport.jar";
				File starmadeFile = new FileExt(starMadePath);
				String[] command = new String[]{"java", "-jar",
						starmadeFile.getAbsolutePath(), a};

				System.err.println("RUNNING COMMAND: " + Arrays.toString(command));
				ProcessBuilder pb = new ProcessBuilder(command);
				Map<String, String> env = pb.environment();
				//					File file = new FileExt(Updater.INSTALL_DIR);
				//					if (file.exists()) {
				//						pb.directory(file.getAbsoluteFile());
				Process p = pb.start();
				System.err.println("Exiting because of gui error handler start crash dialog");
				try {
					CrashReporter.createThreadDump();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//					try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
				//					} else {
				//						throw new FileNotFoundException(
				//								"Cannot find the Install Directory: "
				//										+ Updater.INSTALL_DIR);
				//					}
			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}

		});

	}
}
