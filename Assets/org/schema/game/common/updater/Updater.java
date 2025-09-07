package org.schema.game.common.updater;

import org.schema.common.util.security.OperatingSystem;
import org.schema.game.common.updater.backup.StarMadeBackupTool;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.version.OldVersionException;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.common.util.DownloadCallback;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater extends Observable {
	public static final int BACK_NONE = 0;
	public static final int BACK_DB = 1;
	public static final int BACK_ALL = 2;
	public static String FILES_URL = "http://files.star-made.org/";
	public static String UPDATE_SITE_OLD = "http://files.star-made.org/build/";
	//	public static String UPDATE_SITE = "http://files.star-made.org/checksum";
	public static String LAUNCHER_VERSION_SITE = "http://files.star-made.org/version";
	//	public static final String INSTALL_DIR = "./StarMade/";
	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns false.
	public static String MIRROR_SITE = "http://files.star-made.org/mirrors";
	public static String selectedMirror;
	public final ArrayList<IndexFileEntry> versions = new ArrayList<IndexFileEntry>();
	private final ArrayList<String> mirrorURLs = new ArrayList<String>();
	boolean loading = false;
	boolean versionsLoaded = false;
	private StarMadeBackupTool backup = new StarMadeBackupTool();
	private boolean updating;
	public Updater(String installDir) {
		reloadVersion(installDir);
	}

	public static void withoutGUI(boolean force, String installDir, VersionFile f, int backUp, boolean selectVersion) {
		Updater u = new Updater(installDir);
		try {
			u.startLoadVersionList(f);
			while (u.loading) {
				Thread.sleep(100);
			}

			if (selectVersion) {
				selectVersion(true, u, force, installDir, f, backUp, selectVersion);
			} else {

				if (u.isNewerVersionAvailable()) {
					System.err.println("A New Version Is Available!");

					u.startUpdateNew(installDir, u.versions.get(u.versions.size() - 1), false, backUp);
				} else {
					System.err.println("You Are Already on the Newest Version: use -force to force an update");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void selectVersion(boolean display, Updater u, boolean force, String installDir, VersionFile f, int backUp, boolean selectVersion) {
		if (display) {
			for (int i = 0; i < u.versions.size(); i++) {
				System.out.println("[" + i + "] v" + u.versions.get(i).version + "; " + u.versions.get(i).build);
			}
		}

		int k;
		try {
			System.out.println("Select the build you want to install (type in number in brackets and press Enter)");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			k = Integer.parseInt(br.readLine());
		} catch (NumberFormatException e) {
			System.out.println("Error: Input must be number");
			selectVersion(false, u, force, installDir, f, backUp, selectVersion);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			selectVersion(false, u, force, installDir, f, backUp, selectVersion);
			return;
		}
		if (k < 0 || k >= u.versions.size() - 1) {
			System.out.println("Error: Version does not exist");
			selectVersion(false, u, force, installDir, f, backUp, selectVersion);
			return;
		}

		u.startUpdateNew(installDir, u.versions.get(k), false, backUp);
	}

	public static String en(String e) throws UnsupportedEncodingException {
		return URLEncoder.encode(e, "ISO-8859-1");
	}

	public static int getRemoteLauncherVersion() throws IOException {
		URL urlVersion = new URL(LAUNCHER_VERSION_SITE);

		URLConnection openConnection = urlVersion.openConnection();
		openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + Launcher.version);
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		int version = 0;
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream())));

		version = Integer.parseInt(in.readLine());

		in.close();

		return version;
	}

	public static int askBackup(final JFrame f) {

		String[] options = new String[]{"Yes (Only Database)", "Yes (Everything)", "No"};
		int n = JOptionPane.showOptionDialog(f, "Create Backup of current game data? (recommended)"
				, "Backup?",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
		return switch(n) {
			case 0 -> BACK_DB;
			case 1 -> BACK_ALL;
			case 2 -> BACK_NONE;
			default -> BACK_NONE;
		};
	}

	private static String getJavaExec() {
		if (System.getProperty("os.name").equals("Mac OS X") || System.getProperty("os.name").contains("Linux")) {
			return "java";
		} else {
			return "javaw";
		}
	}

	public static void selfUpdate(final String[] args) throws IOException {

		URL resource = Launcher.class.getResource("/smselfupdate.jar");
		if (resource != null) {
			System.err.println("Extracting self updating jar");
			InputStream resourceAsStream = Updater.class.getResourceAsStream("/smselfupdate.jar");
			final File starmadeFile = new FileExt("smselfupdate.jar");
			FileUtil.copyInputStreamToFile(resourceAsStream, starmadeFile, (size, diff) -> {

			});
			resourceAsStream.close();

			System.err.println("Extracting self updating jar DONE: " + starmadeFile.getAbsolutePath());

			SwingUtilities.invokeLater(() -> {
				try {
					String a = "";
					for (int i = 0; i < args.length; i++) {
						a += " " + args[i];
					}

					String[] command = null;
					//-XX:+ShowMessageBoxOnError
					command = new String[]{getJavaExec(), "-Djava.net.preferIPv4Stack=true", "-jar", starmadeFile.getAbsolutePath(), a};

					System.err.println("RUNNING COMMAND: " + command);
					ProcessBuilder pb = new ProcessBuilder(command);
					Map<String, String> env = pb.environment();
					File file = new FileExt("./");
					pb.directory(file.getAbsoluteFile());
					Process p = pb.start();
					System.err.println("Exiting because launcher starting selfupdater");
					try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);

				} catch (IOException e) {
					e.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e);
				}

			});

		} else {
			throw new FileNotFoundException("Couldnt find selfupdate in jar");
		}

	}

	/* (non-Javadoc)
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
		backup.addObserver(o);
	}

	public boolean checkMirrow(FileEntry entry) {
		String fileUrl = selectedMirror + entry.name;
		try {
			URL url = new URL(fileUrl);

			URLConnection openConnection = url.openConnection();
			openConnection.setReadTimeout(15000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + Launcher.version);
			openConnection.connect();
			openConnection.getInputStream();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Mirror not available " + selectedMirror);
		}
		return false;
	}

	public void extract(FileEntry fentry, String installPath) throws IOException {
		if (!installPath.endsWith("/")) {
			installPath += "/";
		}
		System.err.println("Extracting " + fentry);
		ZipFile zipFile = new ZipFile(fentry.name);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		File f = new FileExt(installPath);
		f.mkdirs();

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory()) {
				// Assume directories are stored parents first then children.

				System.err.println("Extracting directory: " + entry.getName());
				// This is not robust, just for demonstration purposes.
				(new FileExt(installPath + entry.getName())).mkdir();
				continue;
			}
			setChanged();
			notifyObservers("Extracting " + entry.getName());
			System.err.println("Extracting file: " + entry.getName());
			FileUtil.copyInputStream(
					zipFile.getInputStream(entry),
					new BufferedOutputStream(new FileOutputStream(installPath + entry
							.getName())));
		}
		zipFile.close();
		System.err.println("Deleting archive " + fentry);
		File delFile = new FileExt(fentry.name);
		delFile.delete();
	}

	public String getStarMadeStartPath(String installDir) {
		return installDir + File.separator + "StarMade.jar";
	}

	public boolean isNewerVersionAvailable() {
		if (!versionsLoaded) {
			return false;
		}
		if (versions.isEmpty()) {
			System.err.println("versions empty");
			return false;
		}
		if (VersionContainer.build == null || VersionContainer.build.equals("undefined")) {
			System.err.println("Version build null or undefined");
			return true;
		}
		if (VersionContainer.build.equals("latest")) {
			System.err.println("newer version always available for develop version!");
			return true;
		}
		System.out.println("checking your version " + VersionContainer.build + " against latest " + versions.get(versions.size() - 1).build + " = " + VersionContainer.build.compareTo(versions.get(versions.size() - 1).build));
		return versions.size() > 0 && VersionContainer.build.compareTo(versions.get(versions.size() - 1).build) < 0;
	}

	private void loadVersionList(VersionFile v) throws IOException {

		setChanged();
		notifyObservers("Retrieving Launcher Version");

		loading = true;
		;
		try {
			versions.clear();
			int version = getRemoteLauncherVersion();

			if (version > Launcher.version) {
				throw new OldVersionException("You have an old Launcher Version.\n" +
						"Please download the latest Launcher Version at http://www.star-made.org/\n('retry' will let you ignore this message [not recommended!])");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch (OldVersionException e) {
			e.printStackTrace();
			if (!GraphicsEnvironment.isHeadless()) {
				Launcher.askForLauncherUpdate();
			}
		} finally {
			loading = false;
		}

		setChanged();
		notifyObservers("Retrieving Mirrors");
		loading = true;
		URL urlMirrors;
		try {
			versions.clear();
			urlMirrors = new URL(MIRROR_SITE);

			URLConnection openConnection = urlMirrors.openConnection();
			openConnection.setConnectTimeout(10000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + Launcher.version);
			int version = 0;
			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream())));
			String str;
			while ((str = in.readLine()) != null) {
				mirrorURLs.add(str);
			}

			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} finally {
			loading = false;
		}

		setChanged();
		notifyObservers("Retrieving Versions");
		loading = true;
		URL url;
		try {
			versions.clear();
			url = new URL(v.location);

			URLConnection openConnection = url.openConnection();
			openConnection.setConnectTimeout(10000);
			openConnection.setReadTimeout(10000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + Launcher.version);

			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream())));

			String str;

			while ((str = in.readLine()) != null) {

				//				0.1612#20140803_202439 ./build/dev/starmade-build_20140803_202439;

				String[] vPath = str.split(" ", 2);

				String[] vBuild = vPath[0].split("#", 2);

				String version = vBuild[0];
				String build = vBuild[1];

				String path = vPath[1];

				versions.add(new IndexFileEntry(path, version, build, v));
			}

			Collections.sort(versions);
			System.err.println("loaded files (sorted) " + versions);
			in.close();
			versionsLoaded = true;
			setChanged();
			notifyObservers("versions loaded");
			openConnection.getInputStream().close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} finally {
			loading = false;
		}

		// Create a URL for the desired page

	}

	public boolean lookForGame(String instalDir) {
		return (new FileExt(getStarMadeStartPath(instalDir))).exists();
	}

	public void reloadVersion(String installDir) {
		try {
			VersionContainer.loadVersion(installDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveUrl(final FileEntry entry) throws MalformedURLException, IOException {

		if (selectedMirror == null) {
			Random r = new Random();
			selectedMirror = mirrorURLs.get(r.nextInt(mirrorURLs.size()));
		}
		if (!selectedMirror.endsWith("/")) {
			selectedMirror += "/";
		}

		setChanged();
		notifyObservers("connecting...");

		String fileUrl = selectedMirror + entry.name;

		File file = new FileExt(entry.name + ".filepart");
		//remove file part
		file.delete();
		//		final long[] update = new long[2];
		final FileDownloadUpdate e = new FileDownloadUpdate();
		FileUtil.copyURLToFile(new URL(fileUrl), file, 50000, 50000, (size, diff) -> {
			e.downloaded = size;
			e.size = entry.size;
			e.fileName = e.fileName;
			setChanged();
			notifyObservers(e);
		});

		file.renameTo(new FileExt(entry.name));

	}

	public void startLoadVersionList(final VersionFile v) {
		loading = true;
		new Thread(() -> {
			try {
				loadVersionList(v);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void startUpdateNew(final String installDirStr, IndexFileEntry newest, boolean forced, int backupFromMain) {
		if (updating) {
			return;
		}

		try {
			Eula eula = getEula();
			if (eula == null) {
				//eula already accepted
			} else {
				if (UpdatePanel.frame != null) {

					JTextArea textArea = new JTextArea(eula.text);
					JScrollPane scrollPane = new JScrollPane(textArea);
					textArea.setLineWrap(true);
					textArea.setWrapStyleWord(true);
					scrollPane.setPreferredSize(new Dimension(500, 500));

					Object[] options = {"I have read the EULA and accept",
							"I don't accept"};
					int n = JOptionPane.showOptionDialog(UpdatePanel.frame,
							scrollPane,
							"StarMade EULA",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,     //do not use a custom Icon
							options,  //the titles of buttons
							options[0]); //default button title
					if (n == 0) {
						File file = new FileExt(OperatingSystem.getAppDir(), "eula.properties");
						Properties p = new Properties();

						p.put("EULA", eula.title);

						file.createNewFile();
						FileOutputStream fo = new FileOutputStream(file);
						p.store(fo, "StarMade Eula Acceptance");
						fo.flush();
						fo.close();
					} else {
						//not accept EULA -> NO DOWNLOAD FOR YOU
						return;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}

		File installDir = new FileExt(installDirStr);
		if (!installDir.exists()) {
			installDir.mkdirs();
		}
		if (!installDir.isDirectory()) {
			try {
				throw new IOException(
						"Installation dir is not a directory");
			} catch (IOException e1) {
				GuiErrorHandler.processErrorDialogException(e1);
			}
		}

		if (!installDir.canWrite()) {
			try {
				throw new IOException(
						"Your operating System denies access \n" +
								"to where you are trying to install StarMade (for good reasons)\n" +
								(new FileExt(installDirStr).getAbsolutePath()) + "\n\n" +
								"To solve this Problem,\n" +
								"Please change the install destination to another directory,\n" +
								"Or Force the install by executing this file as administrator");
			} catch (IOException e1) {
				GuiErrorHandler.processErrorDialogException(e1);
			}
		}

		setChanged();
		notifyObservers("updating");

		File instalDir = new FileExt(installDirStr);
		int backUp = backupFromMain;

		if (UpdatePanel.frame != null) {
			if (instalDir.exists() && instalDir.isDirectory() && instalDir.list().length > 0) {
				backUp = askBackup(UpdatePanel.frame);
			}
		}

		downloadDiff(instalDir, installDirStr, newest, backUp, forced);

	}

	private void downloadDiff(final File installDir, final String installDirStr, final IndexFileEntry f, final int backup, final boolean forced) {
		updating = true;

		new Thread(() -> {

			try {

				if (backup != BACK_NONE) {
					setChanged();
					notifyObservers("Creating backup!");
					boolean removeOld = false;
					boolean dbOnly = (backup & BACK_DB) == BACK_DB;
					System.err.println("BACKING UP: " + installDirStr);
					Updater.this.backup.backUp(installDirStr, "server-database", String.valueOf(System.currentTimeMillis()), ".zip", removeOld, dbOnly, null);
				}

				setChanged();
				notifyObservers("Retrieving checksums for v" + f.version + "(build " + f.build + ")");

				ChecksumFile checksums = getChecksums(f.path);

				System.err.println("Downloaded checksums: \n" + checksums.toString());

				String buildDir = FILES_URL + f.path + "/";
				checksums.download(forced, buildDir, installDir, installDirStr, new FileDowloadCallback() {
					@Override
					public void update(FileDownloadUpdate u) {
						setChanged();
						notifyObservers(u);
					}

					@Override
					public void update(String u) {
						setChanged();
						notifyObservers(u);
					}
				});

				setChanged();
				notifyObservers("Update Successfull!");

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				setChanged();
				notifyObservers("reset");
			} catch (IOException e1) {
				e1.printStackTrace();
				setChanged();
				notifyObservers("failed IO");
				GuiErrorHandler.processErrorDialogException(e1);
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
				setChanged();
				notifyObservers("failed Sha");
				GuiErrorHandler.processErrorDialogException(e1);
			} finally {
				updating = false;
				setChanged();
				notifyObservers("finished");
			}
		}).start();
	}

	public Eula getEula() throws IOException {
		System.err.println("URL::: " + FILES_URL + "/smeula.txt");
		URL urlVersion = new URL(FILES_URL + "/smeula.txt");

		URLConnection openConnection = urlVersion.openConnection();
		openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + Launcher.version);
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		// #RM1958 swap out Base64 encoder
//		String encoding = Base64.encode("dev:dev".getBytes());
//		openConnection.setRequestProperty ("Authorization", "Basic " + encoding);

		Eula e = new Eula();

		int version = 0;
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream())));

		StringBuffer b = new StringBuffer();
		String line;
		while ((line = in.readLine()) != null) {
			if (e.title == null) {
				e.title = line;

				File file = new FileExt(OperatingSystem.getAppDir(), "eula.properties");
				Properties p = new Properties();

				if (file.exists()) {
					FileInputStream fs = new FileInputStream(file);
					p.load(fs);

					fs.close();
					if (p.getProperty("EULA") != null && p.getProperty("EULA").equals(e.title)) {
						return null;
					}
				}

			}
			b.append(line + "\n");
		}

		in.close();

		e.text = b.toString();

		return e;
	}

	public static ChecksumFile getChecksums(String relPath) throws IOException {
		URL urlVersion = new URL(relPath + "/checksums");

		URLConnection openConnection = urlVersion.openConnection();
		openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + Launcher.version);
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		// #RM1958 swap out Base64 encoder
//		String encoding = Base64.encode("dev:dev".getBytes());
//		openConnection.setRequestProperty ("Authorization", "Basic " + encoding);

		int version = 0;
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream())));

		ChecksumFile f = new ChecksumFile();
		f.parse(in);

		in.close();

		return f;
	}

	public enum VersionFile {
		PRE("http://files.star-made.org/prebuildindex"),
		DEV("http://files.star-made.org/devbuildindex"),
		RELEASE("http://files.star-made.org/releasebuildindex"),
		ARCHIVE("http://files.star-made.org/archivebuildindex");

		public final String location;

		private VersionFile(String location) {
			this.location = location;
		}
	}

}
