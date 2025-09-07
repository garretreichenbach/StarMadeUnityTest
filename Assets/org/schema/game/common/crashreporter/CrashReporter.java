package org.schema.game.common.crashreporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.schema.game.common.util.FTPUtils;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.resource.FileExt;

import com.google.code.tempusfugit.concurrency.DeadlockDetector;
import com.google.code.tempusfugit.concurrency.ThreadDump;

public class CrashReporter extends Observable {
	private static final String LOG_DIR = "./logs/";
	private static final String INFOPATH = LOG_DIR + "info";
	private static final String THREADPATH = LOG_DIR + "threaddump";
	private static final String INFOPATHEXT = ".txt";
	private String email, description, os, hardware, java;

	public static void doUpload(String email, String description) {
		CrashReporter c = new CrashReporter();
		c.fillAutomaticInformation(email, description);
		c.startCreashReport();
	}

	public static boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\-]([\\.\\w\\-\\+])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}

	public static void main(String[] args) {
		doUpload("schema@star-made.org", "general error");
	}

	public static void createThreadDump() throws IOException {
		createThreadDump(THREADPATH);
	}

	public static void createThreadDump(String string) throws IOException {

		File fg = new FileExt(string + INFOPATHEXT);
		int ig = 0;
		while (fg.exists()) {
			fg = new FileExt(string + ig + INFOPATHEXT);
			ig++;
		}
		fg.createNewFile();
		PrintStream printStream = new PrintStream(fg);
		ThreadDump.dumpThreads(printStream);
		printStream.append("\n\n--------------\nDeadlock Check\n");
		DeadlockDetector.printDeadlocks(printStream);

		printStream.flush();
		printStream.close();
	}

	private void addCrashReportInfo() throws IOException {

		setChanged();
		notifyObservers("making report");
		File f = new FileExt(INFOPATH + INFOPATHEXT);
		int i = 0;
		while (f.exists()) {
			f = new FileExt(INFOPATH + i + INFOPATHEXT);
			i++;
		}
		f.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(f));

		bw.append("E-Mail: " + email + "\n\n\n");
		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.append("------------------------------------------------\n");
		bw.newLine();
		bw.append("OS: ");
		addNewLined(os, bw);
		bw.newLine();
		bw.append("JAVA: ");
		addNewLined(java, bw);
		bw.newLine();

		bw.append("StarMade-Version: ");
		addNewLined(VersionContainer.build, bw);
		bw.newLine();
		bw.append("HARDWARE: ");
		addNewLined(hardware, bw);
		bw.newLine();
		bw.append("------------------------------------------------\n\n\n");
		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.append("DESCRIPTION: ");
		addNewLined(description, bw);
		bw.newLine();

		bw.flush();
		bw.close();

		//		createThreadDump();
		//		try{
		//			File dir = new FileExt("./");
		//			if(dir.exists()){
		//				File[] listFiles = dir.listFiles();
		//				for(File file : listFiles){
		//					if(file.getName().endsWith(".log")){
		//						FileInputStream in = new FileInputStream(file);
		//						File outFile = new FileExt(LOG_DIR+file.getName());
		//						outFile.createNewFile();
		//						FileOutputStream out = new FileOutputStream(outFile);
		//						FileUtil.copyInputStream(in, out);
		//
		//						out.close();
		//						in.close();
		//					}
		//				}
		//			}
		//
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}

		setChanged();
		notifyObservers(1);

	}

	private void addNewLined(String s, BufferedWriter bw) throws IOException {
		String[] split = s.split("\n");
		for (int i = 0; i < split.length; i++) {
			bw.append(split[i]);
			bw.newLine();
		}
		if (split.length == 0) {
			bw.append(s);
		}
	}

	public void fillAutomaticInformation(String email, String description) {
		if (email == null || !isValidEmailAddress(email)) {
			throw new IllegalArgumentException("Not a valid Email Address: " + email);
		}
		this.email = email;
		this.description = description;
		this.os = getOSInfo();
		this.java = getJavaInfo();
		this.hardware = getHarwareInfo();
	}

	private String formatVals(String... vals) {
		StringBuffer f = new StringBuffer();
		f.append("\n");
		for (int i = 0; i < vals.length; i++) {
			f.append("    ");
			f.append(vals[i]);
			f.append(";\n");
		}
		return f.toString();
	}

	private String getHarwareInfo() {
		String processors = ("Available processors: " + Runtime.getRuntime().availableProcessors() + " cores");
		String freeMem = ("Free memory: " + Runtime.getRuntime().freeMemory() + " bytes");
		long maxMemory = Runtime.getRuntime().maxMemory();
		String maxMem = ("Maximum memory: " +
				(maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory) + " bytes");
		String totalMem = ("Total memory : " + Runtime.getRuntime().totalMemory() + " bytes");

		return formatVals(processors, "MEMORY: ", freeMem, maxMem, totalMem);

	}

	private String getJavaInfo() {
		String javaVersion = "java.version";
		String javaVendor = "java.vendor";
		String javaHome = "java.home";
		String jvmName = "java.vm.name";
		String jvmVersion = "java.vm.version";
		String libPath = "java.library.path";

		//		String usrName = "user.name";
		//		String usrHome = "user.home";
		String workingDir = "user.dir";

		return formatVals(System.getProperty(javaVersion),
				"VENDOR: " + System.getProperty(javaVendor),
				"HOME: " + System.getProperty(javaHome),
				"JVMNAME: " + System.getProperty(jvmName),
				"JVMVERSION: " + System.getProperty(jvmVersion),
				"LIBPATH: " + System.getProperty(libPath),
				"WORKING_DIR: " + System.getProperty(workingDir));
	}

	private String getOSInfo() {
		String nameOS = "os.name";
		String versionOS = "os.version";
		String architectureOS = "os.arch";

		return formatVals(System.getProperty(nameOS),
				"VERSION: " + System.getProperty(versionOS),
				"ARCHITECTURE: " + System.getProperty(architectureOS));
	}

	private void reportCrash() throws IOException {
		if (VersionContainer.build.equals("undefined")) {
			VersionContainer.loadVersion("./");
		}

		System.out.println("Adding Report File...");
		addCrashReportInfo();
		System.out.println("Zipping Logs & Info...");
		setChanged();
		notifyObservers("Zipping Logs And Report");
		FolderZipper.zipFolder(LOG_DIR, "logs.zip", null, null);

		setChanged();
		notifyObservers(50);
		setChanged();
		notifyObservers("Sending Report");
		File file = new FileExt("logs.zip");
		if (file.exists()) {
			System.out.println("Uploading Logs & Info...");
			FTPUtils.upload("oldsite.star-made.org", "starmadelog", "starmadeschema", "./upload/logs_" + email + "_v" + VersionContainer.VERSION + "-" + VersionContainer.build + "_" + System.currentTimeMillis() + ".zip", file);
		} else {
			throw new FileNotFoundException("The Zip File " + file.getAbsolutePath() + " does not exist");
		}

		setChanged();
		notifyObservers(100);
		setChanged();
		notifyObservers("Report Sent Successfully");
		file.delete();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers("");
		setChanged();
		notifyObservers(0);
		setChanged();
		notifyObservers("FINISHED");

	}

	public void startCreashReport() {
		new Thread(() -> {
			try {
				reportCrash();
			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processNormalErrorDialogException(e, true);
			}
		}).start();
	}

}
