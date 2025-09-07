package org.schema.game.common.updater;

import org.schema.common.util.StringTools;
import org.schema.game.common.updater.Updater.VersionFile;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.util.ZipGUICallback;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.resource.FileExt;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class UpdatePanel extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public static int maxMemory = 1024;
	public static int minMemory = 512;
	public static int earlyGenMemory = 128;

	public static int maxMemory32 = 512;
	public static int minMemory32 = 256;
	public static int earlyGenMemory32 = 64;

	public static int serverMaxMemory = 1024;
	public static int serverMinMemory = 1024;
	public static int serverEarlyGenMemory = 256;

	public static int port = 4242;
	public static String installDir = "./StarMade/";
	public static VersionFile buildBranch = VersionFile.RELEASE;
	public static JFrame frame;
	final Updater updater;
	private final JButton btnUpdateToNewest;
	private final JProgressBar progressBar;
	private final JButton btnRefresh;
	private final JButton btnStartGame;
	private final JLabel label;
	private final JLabel lblNewLabel;
	private JButton btnStartDedicatedServer;
	private final JProgressBar progressBar_File;
	private final JButton btnOptions;

	/**
	 * Create the panel.
	 */
	public UpdatePanel(final JFrame f) {
		UpdatePanel.frame = f;
		try {
			MemorySettings.loadSettings();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		this.updater = new Updater(installDir);
		this.updater.addObserver(this);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(250, 30));
		progressBar.setMinimumSize(new Dimension(200, 34));
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.weightx = 1.0;
		gbc_progressBar.gridwidth = 5;
		gbc_progressBar.fill = GridBagConstraints.BOTH;
		gbc_progressBar.insets = new Insets(5, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 0;
		add(progressBar, gbc_progressBar);

		btnStartGame = new JButton("Start Game");
		btnStartGame.addActionListener(e -> startInstance(new String[0]));

		progressBar_File = new JProgressBar();
		progressBar_File.setStringPainted(true);
		progressBar_File.setPreferredSize(new Dimension(250, 20));
		progressBar_File.setMinimumSize(new Dimension(200, 14));
		GridBagConstraints gbc_progressBar_File = new GridBagConstraints();
		gbc_progressBar_File.weightx = 1.0;
		gbc_progressBar_File.fill = GridBagConstraints.BOTH;
		gbc_progressBar_File.gridwidth = 4;
		gbc_progressBar_File.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar_File.gridx = 0;
		gbc_progressBar_File.gridy = 1;
		add(progressBar_File, gbc_progressBar_File);
		btnStartGame.setAlignmentX(Component.RIGHT_ALIGNMENT);
		GridBagConstraints gbc_btnStartGame = new GridBagConstraints();
		gbc_btnStartGame.insets = new Insets(0, 0, 5, 0);
		gbc_btnStartGame.anchor = GridBagConstraints.EAST;
		gbc_btnStartGame.gridx = 4;
		gbc_btnStartGame.gridy = 1;
		btnStartGame.setEnabled(updater.lookForGame(installDir));
		add(btnStartGame, gbc_btnStartGame);

		JLabel label_1 = new JLabel("");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 2;
		add(label_1, gbc_label_1);

		btnUpdateToNewest = new JButton("Update and install latest version");
		btnUpdateToNewest.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnUpdateToNewest.addActionListener(e -> updater.startUpdateNew(installDir, updater.versions.get(updater.versions.size() - 1), false, 0));

		btnRefresh = new JButton("refresh");
		btnRefresh.addActionListener(arg0 -> {
			updater.reloadVersion(installDir);
			updater.startLoadVersionList(buildBranch);
			btnStartGame.setEnabled(updater.lookForGame(installDir));
			btnStartDedicatedServer.setEnabled(updater.lookForGame(installDir));
		});
		GridBagConstraints gbc_btnRefresh = new GridBagConstraints();
		gbc_btnRefresh.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRefresh.insets = new Insets(0, 0, 5, 5);
		gbc_btnRefresh.gridx = 0;
		gbc_btnRefresh.gridy = 3;
		add(btnRefresh, gbc_btnRefresh);

		btnOptions = new JButton("Options & Repair");
		btnOptions.addActionListener(e -> {
			InstallOptions o = new InstallOptions(UpdatePanel.this, f);
			o.setVisible(true);
		});

		JButton btnRegisterGameWith = new JButton("Register With Steam");
		btnRegisterGameWith.addActionListener(arg0 -> {

			String msg = "To keep StarMade as open as possible, the game can be played and bought outside of steam.\n"
					+ "This means however, that all purchases must be registered in our central database.\n\n"
					+ "If you bought the game in steam, please create an account on https://registry.star-made.org/,\n"
					+ "and you can upgrade your account with steam.\n\n"
					+ "We're sorry for the inconvenience. We are working on an automated system.\n";

			JOptionPane.showMessageDialog(f, msg, "Steam", JOptionPane.INFORMATION_MESSAGE);
			try {
//					Launcher.openWebpage(new URL("https://registry.star-made.org/"));
				Launcher.openWebpage(new URL("https://registry.star-made.org/steam"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});
		GridBagConstraints gbc_btnRegisterGameWith = new GridBagConstraints();
		gbc_btnRegisterGameWith.insets = new Insets(0, 0, 5, 5);
		gbc_btnRegisterGameWith.gridx = 1;
		gbc_btnRegisterGameWith.gridy = 3;
		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
		gbc_btnOptions.weightx = 1.0;
		gbc_btnOptions.anchor = GridBagConstraints.EAST;
		gbc_btnOptions.insets = new Insets(0, 0, 5, 5);
		gbc_btnOptions.gridx = 2;
		gbc_btnOptions.gridy = 3;
		add(btnOptions, gbc_btnOptions);
		btnUpdateToNewest.setAlignmentX(Component.RIGHT_ALIGNMENT);
		btnUpdateToNewest.setEnabled(this.updater.isNewerVersionAvailable());
		GridBagConstraints gbc_btnUpdateToNewest = new GridBagConstraints();
		gbc_btnUpdateToNewest.anchor = GridBagConstraints.EAST;
		gbc_btnUpdateToNewest.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateToNewest.gridx = 3;
		gbc_btnUpdateToNewest.gridy = 3;
		add(btnUpdateToNewest, gbc_btnUpdateToNewest);

		btnStartDedicatedServer = new JButton("Start Dedicated Server");
		btnStartDedicatedServer.addActionListener(arg0 -> startServerInstance(new String[0]));
		btnStartDedicatedServer.setEnabled(updater.lookForGame(installDir));
		GridBagConstraints gbc_btnStarDedicatedServer = new GridBagConstraints();
		gbc_btnStarDedicatedServer.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnStarDedicatedServer.insets = new Insets(0, 0, 5, 0);
		gbc_btnStarDedicatedServer.gridx = 4;
		gbc_btnStarDedicatedServer.gridy = 3;
		add(btnStartDedicatedServer, gbc_btnStarDedicatedServer);

		label = new JLabel("");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.gridwidth = 4;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 4;
		gbc_label.anchor = GridBagConstraints.WEST;
		add(label, gbc_label);

		lblNewLabel = new JLabel("");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 4;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 5;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		add(lblNewLabel, gbc_lblNewLabel);

		updater.startLoadVersionList(buildBranch);
		(new Thread(() -> {
			//for steam: keep it repaining so the damn overlays go away
			try {
				Thread.sleep(1200);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			while (UpdatePanel.this.isVisible()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				EventQueue.invokeLater(UpdatePanel.this::repaint);
			}
		})).start();

	}

	private static String getJavaExec() {
		if (System.getProperty("os.name").equals("Mac OS X") || System.getProperty("os.name").contains("Linux")) {
			return "java";
		} else {
			return "javaw";
		}
	}

	public static boolean is64Bit() {
		return System.getProperty("os.arch").contains("64");
	}

	private void startInstance(final String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				StringBuilder a = new StringBuilder();
				for(String arg : args) a.append(" ").append(arg);
				String starMadePath = updater.getStarMadeStartPath(installDir);
				File starmadeFile = new FileExt(starMadePath);
				String[] command;
				if(is64Bit()) command = new String[]{getJavaExec(), "-Djava.net.preferIPv4Stack=true", "-Xmn" + earlyGenMemory + "M", "-Xms" + minMemory + "M", "-Xmx" + maxMemory + "M", "-Xincgc", "-jar", starmadeFile.getAbsolutePath(), "-force", "-port:" + port, a.toString()};
				else command = new String[]{getJavaExec(), "-Djava.net.preferIPv4Stack=true", "-Xmn" + earlyGenMemory32 + "M", "-Xms" + minMemory32 + "M", "-Xmx" + maxMemory32 + "M", "-Xincgc", "-jar", starmadeFile.getAbsolutePath(), "-force", "-port:" + port, a.toString()};
				System.err.println("RUNNING COMMAND: " + command);
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.environment();
				File file = new FileExt(installDir);
				if(file.exists()) {
					pb.directory(file.getAbsoluteFile());
					pb.start();
					System.err.println("Exiting because updater starting game");
					try{
						throw new Exception("System.exit() called");
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					System.exit(0);
				} else {
					throw new FileNotFoundException("Cannot find the Install Directory: " + installDir);
				}
			} catch(IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
		});

	}

	private void startServerInstance(final String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				StringBuilder a = new StringBuilder();
				for(String arg : args) a.append(" ").append(arg);
				String starMadePath = updater.getStarMadeStartPath(installDir);
				File starmadeFile = new FileExt(starMadePath);
				String[] command = new String[] {getJavaExec(), "-Djava.net.preferIPv4Stack=true", "-Xmn" + serverEarlyGenMemory + "M", "-Xms" + serverMinMemory + "M", "-Xmx" + serverMaxMemory + "M", "-Xincgc", "-jar", starmadeFile.getAbsolutePath(), "-server", "-gui", "-port:" + port, a.toString()};
				System.err.println("RUNNING COMMAND: " + Arrays.toString(command));
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.environment();
				File file = new FileExt(installDir);
				if (file.exists()) {
					pb.directory(file.getAbsoluteFile());
					pb.start();
					try {
						throw new Exception("System.exit() called");
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					System.exit(0);
				} else {
					throw new FileNotFoundException("Cannot find the Install Directory: " + installDir);
				}
			} catch(IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
		});
	}

	@Override
	public void update(Observable arg0, Object o) {
		if(o != null) {
			if(o.equals("resetbars")) {
				progressBar.setString("");
				progressBar.setValue(0);
				progressBar_File.setValue(0);
				progressBar_File.setString("");
			} else if(o.equals("reload Versions")) {
				System.err.println("[UPDATER] trigger versions reloading");
				updater.startLoadVersionList(buildBranch);
				btnStartGame.setEnabled(updater.lookForGame(installDir));
				btnStartDedicatedServer.setEnabled(updater.lookForGame(installDir));
			} else if(o.equals("versions loaded")) {
				System.err.println("[UPDATER] trigger versions loaded " + installDir);
				updater.reloadVersion(installDir);
				btnUpdateToNewest.setEnabled(this.updater.isNewerVersionAvailable());
				progressBar.setString("");
			} else if(o.equals("updating")) {
				btnUpdateToNewest.setEnabled(false);
				btnStartGame.setEnabled(false);
				btnStartDedicatedServer.setEnabled(false);
				btnOptions.setEnabled(false);
				btnRefresh.setEnabled(false);
			} else if(o.equals("finished")) {
				System.err.println("FINISHED Update");
				btnUpdateToNewest.setEnabled(false);
				btnRefresh.setEnabled(true);
				btnOptions.setEnabled(true);
				updater.reloadVersion(installDir);
				updater.startLoadVersionList(buildBranch);
				btnStartGame.setEnabled(updater.lookForGame(installDir));
				btnStartDedicatedServer.setEnabled(updater.lookForGame(installDir));
			} else if(o.equals("reset")) {
				progressBar.setString("");
				progressBar.setValue(0);
				progressBar_File.setValue(0);
				progressBar_File.setString("");
			} else if(o instanceof ZipGUICallback) {
				ZipGUICallback p = (ZipGUICallback) o;
				int pc = (int) Math.ceil(((float) p.fileIndex / (float) p.fileMax) * 100);
				progressBar.setString("Backing files up: " + pc + "  %");
				progressBar.setValue(pc);
			} else if(o instanceof FileDownloadUpdate) {
				FileDownloadUpdate p = (FileDownloadUpdate) o;
				int downloadFile = (int) ((float) p.downloaded / (float) p.size * 100);
				int downloadTotal = (int) ((float) p.currentSize / (float) p.totalSize * 100);

				String totalSize = "" + StringTools.formatPointZero((p.totalSize / 1024d) / 1024d) + "MB";
				String currentSize = "" + StringTools.formatPointZero((p.currentSize / 1024d) / 1024d) + "MB";

				String downloadSpeed = "";
				if(p.downloadSpeed / 1000000d > 0.5d) downloadSpeed = StringTools.formatPointZeroZero(p.downloadSpeed / 1000000d) + " MB/sec";
				else downloadSpeed = StringTools.formatPointZeroZero(p.downloadSpeed / 1000d) + " kB/sec";

				progressBar.setString("Downloading files " + p.index + "/" + p.total + " (" + currentSize + "/" + totalSize + ") [" + downloadTotal + "%]");

				String fileSize = "(" + StringTools.formatPointZero((p.size / 1024d) / 1024d) + "MB) ";
				progressBar_File.setString("(Downloading 5 files) largest currently: " + p.fileName + " " + fileSize + " [" + downloadFile + "%] " + downloadSpeed);
				progressBar_File.setValue(downloadFile);
				progressBar.setValue(downloadTotal);
				return;
			} else {
				if(o instanceof String) progressBar.setString(o.toString());
			}
		}
		boolean gameFound = updater.lookForGame(installDir);
		File f = (new FileExt(installDir));
		String version;
		if(VersionContainer.build == null || VersionContainer.build.equals("undefined")) version = "Currently no valid StarMade version installed";
		else version = "Currently installed StarMade Version: v" + VersionContainer.VERSION + "; build " + VersionContainer.build;
		if(updater.isNewerVersionAvailable()) {
			if(updater.versions == null || updater.versions.isEmpty()) version += ". StarMade is Available to Install!";
			else version += ". A new Version is Available! (v" + updater.versions.get(updater.versions.size() - 1).version + "; build " + updater.versions.get(updater.versions.size() - 1).build + ")";
			label.setForeground(Color.GREEN.darker());
		} else {
			version += ". You already have the latest version";
			label.setForeground(Color.BLACK);
		}
		label.setText(version);
		String v = (gameFound ? "Installation found in " + f.getAbsolutePath() : "No installation found in " + f.getAbsolutePath());
		v = v.replace("/./", "/");
		v = v.replace("\\.\\", "\\");
		lblNewLabel.setText(v);
		if(gameFound) lblNewLabel.setForeground(Color.GREEN.darker());
		else lblNewLabel.setForeground(Color.RED.darker());
	}
}
