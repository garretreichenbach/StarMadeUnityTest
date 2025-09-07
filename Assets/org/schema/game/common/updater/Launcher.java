package org.schema.game.common.updater;

import org.schema.game.common.updater.Updater.VersionFile;
import org.schema.game.common.util.DesktopUtils;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.resource.FileExt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;

public class Launcher extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public static int version = 16;
	public static int newsFetchStyle = 1;
	static boolean steam;
	private JPanel contentPane;
	private UpdatePanel updatePanel;

	/**
	 * Create the frame.
	 */
	public Launcher(boolean useWinLookAndFeel) {
		if (useWinLookAndFeel) {
			String nativeLF = UIManager.getSystemLookAndFeelClassName();
			System.err.println("[LookAndFeel] NATIVE LF " + nativeLF);

			if ("com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals(nativeLF)) {
				// Install the look and feel
				try {
					UIManager.setLookAndFeel(nativeLF);
				} catch (InstantiationException e) {
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InstantiationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (UnsupportedLookAndFeelException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (ClassNotFoundException e) {
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (InstantiationException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					} catch (UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}
				} catch (UnsupportedLookAndFeelException e) {
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (InstantiationException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					} catch (UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}
				} catch (IllegalAccessException e) {
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (InstantiationException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					} catch (UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}
				}
			} else {
				System.err.println("[ERROR] Not applying lok and feel as not all can guarantee all decoration working, resulting in a startup crash");
			}
		}
		try {
			URL resource = Launcher.class.getResource("/icon.png");
			if (resource != null) {
				setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("StarMade [Launcher v" + Launcher.version + "]");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 849, 551);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnSe = new JMenu("Options");
		menuBar.add(mnSe);

		JMenuItem mntmJavaMemorySettings = new JMenuItem("Memory Settings");
		mntmJavaMemorySettings.addActionListener(arg0 -> {
			MemorySettings m = new MemorySettings(Launcher.this);
			m.setVisible(true);

		});

		JMenuItem mntmInstallationSettings = new JMenuItem("Installation Settings");
		mntmInstallationSettings.addActionListener(e -> {
			InstallSettings m = new InstallSettings(Launcher.this, updatePanel);
			m.setVisible(true);
		});
		mnSe.add(mntmInstallationSettings);
		mnSe.add(mntmJavaMemorySettings);

		JMenuItem mntmJavaMirrorSettings = new JMenuItem("Mirror Settings");
		mntmJavaMirrorSettings.addActionListener(arg0 -> {
			String s = (String) JOptionPane.showInputDialog(
					Launcher.this,
					"Mirror URL",
					"Mirror",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					"");

			//If a string was returned, say so.
			if ((s != null) && (s.length() > 0)) {
				Updater.selectedMirror = s;
				return;
			}

		});
		mnSe.add(mntmJavaMirrorSettings);

		JMenuItem mntmServerPort = new JMenuItem("Server Port");
		mntmServerPort.addActionListener(e -> {
			String s = (String) JOptionPane.showInputDialog(
					Launcher.this,
					"Please enter a port to start the server on",
					"Port",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					String.valueOf(UpdatePanel.port));

			//If a string was returned, say so.
			if ((s != null) && (s.length() > 0)) {
				try {
					int parsedPort = Integer.parseInt(s);
					UpdatePanel.port = parsedPort;
					// custom title, error icon
					JOptionPane.showMessageDialog(Launcher.this,
							"Saving Setting: Port set to " + UpdatePanel.port + ".", "Port",
							JOptionPane.INFORMATION_MESSAGE);

					MemorySettings.saveSettings();
				} catch (NumberFormatException e2) {
					// custom title, error icon
					JOptionPane.showMessageDialog(Launcher.this,
							"Port must be a number", "Port set error",
							JOptionPane.ERROR_MESSAGE);
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			}
		});
		mnSe.add(mntmServerPort);

		JMenuItem mntmDownloadNewestLauncher = new JMenuItem("Download Newest Launcher");
		mntmDownloadNewestLauncher.addActionListener(e -> askForLauncherUpdate());
		mnSe.add(mntmDownloadNewestLauncher);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmDownloadProblems = new JMenuItem("Download Problems");
		mntmDownloadProblems.addActionListener(e -> {

			String msg = "If the download gets stuck, launching the starter as Administrator helps in some cases.\n"
					+ "You can also download the latest Version manually from http://files.star-made.org/build/ \n"
					+ "and extract it to the directory where this launcher is located.\n" +
					"\n" +
					"Intel graphics cards are known to have buggy drivers in old versions, so be sure to update to the newest version.\n" +
					"\n" +
					"There is a CrashAndBugReporter.jar in the StarMade directory if you want to send in a crash report manually.\n" +
					"Please use 64 bit java for maximal performance.\n" +
					"If you have any questions about the game, feel free to mail me at schema@star-made.org\n" +
					"\n" +
					"Have fun playing!\n";

			JOptionPane.showMessageDialog(Launcher.this, msg, "Help", JOptionPane.INFORMATION_MESSAGE);
		});
		mnHelp.add(mntmDownloadProblems);

		JMenuItem mntmStarmadeWiki = new JMenuItem("StarMade Wiki");
		mntmStarmadeWiki.addActionListener(e -> {
			try {
				openWebpage(new URL("http://wiki.star-made.org"));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		});
		mnHelp.add(mntmStarmadeWiki);

//		JMenuItem mntmStarmadeServerList = new JMenuItem("StarMade Server List");
//		mntmStarmadeServerList.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				try {
//					openWebpage(new URL("http://serverlist.star-made.org"));
//				} catch (MalformedURLException e1) {
//					e1.printStackTrace();
//				}
//			}
//		});
//		mnHelp.add(mntmStarmadeServerList);

		JMenuItem mntmStarmadeSupportChat = new JMenuItem("StarMade Support Chat");
		mntmStarmadeSupportChat.addActionListener(e -> {

			try {
				openWebpage(new URL("http://star-made.org/chat/index"));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

		});
		mnHelp.add(mntmStarmadeSupportChat);

		JMenu mnCommunity = new JMenu("Community");
		menuBar.add(mnCommunity);

		JMenuItem mntmStarmadeForums = new JMenuItem("StarMade Forums");
		mntmStarmadeForums.addActionListener(e -> {
			try {
				openWebpage(new URL("http://starmadedock.net"));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		});

		JMenuItem mntmStarmadeNews = new JMenuItem("StarMade News");
		mntmStarmadeNews.addActionListener(e -> {
			try {
				openWebpage(new URL("http://star-made.org/news"));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

		});
		mnCommunity.add(mntmStarmadeNews);
		mnCommunity.add(mntmStarmadeForums);

		JMenuItem mntmStarmadeReddir = new JMenuItem("StarMade Reddit");
		mntmStarmadeReddir.addActionListener(e -> {
			Object[] options = {"Ok",
					"Cancel"};
			int n = JOptionPane.showOptionDialog(UpdatePanel.frame,
					"Open link to a thrid party website?\n"
							+ "Schine GmbH does not take any resposibility for any content on third party sites.",
					"Third Party Website",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[0]); //default button title
			if(n == 0) {
				try {
					openWebpage(new URL("http://www.reddit.com/r/starmade"));
				} catch(MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		});
		mnCommunity.add(mntmStarmadeReddir);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		SwingUtilities.invokeLater(() -> {
		});

		NewsPanel newsPanel = new NewsPanel();
		newsPanel.setMinimumSize(new Dimension(450, 200));
		GridBagConstraints gbc_newsPanel = new GridBagConstraints();
		gbc_newsPanel.gridheight = 2;
		gbc_newsPanel.weighty = 3.0;
		gbc_newsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_newsPanel.fill = GridBagConstraints.BOTH;
		gbc_newsPanel.gridx = 0;
		gbc_newsPanel.gridy = 0;
		contentPane.add(newsPanel, gbc_newsPanel);

		updatePanel = new UpdatePanel(this);
		GridBagConstraints gbc_updatePanel = new GridBagConstraints();
		gbc_updatePanel.weighty = 1.0;
		gbc_updatePanel.fill = GridBagConstraints.BOTH;
		gbc_updatePanel.weightx = 1.0;
		gbc_updatePanel.gridx = 0;
		gbc_updatePanel.gridy = 2;
		contentPane.add(updatePanel, gbc_updatePanel);
	}

	public static void displayHelp() {
		System.out.println("StarMade Launcher " + version + " Help:");
		System.out.println("-version version selection promt");
		System.out.println("-origin Use Origin Server (not recommended)");
		System.out.println("-nogui dont start gui (needed for linux dedicated servers)");
		System.out.println("-nobackup dont create backup (default backup is server database only)");
		System.out.println("-backupall create backup of everything (default backup is server database only)");
		System.out.println("-archive use archive branch (default is release)");
		System.out.println("-pre use pre branch (default is release)");
		System.out.println("-dev use dev branch (default is release)");
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		System.setProperty("http.agent", "");

		final HashSet<String> argsSet = new HashSet<String>();

		File fU = new FileExt("smselfupdate.jar");
		if (fU.exists()) {
			fU.delete();
		}

		for (int i = 0; i < args.length; i++) {
			argsSet.add(args[i]);

			if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("--help")) {
				displayHelp();
				return;
			}
		}
		if (GraphicsEnvironment.isHeadless() && !argsSet.contains("-nogui")) {
			displayHelp();
			System.out.println("Please use the '-nogui' parameter to run the launcher in text mode!");
		}
		if (argsSet.contains("-origin")) {
			Updater.FILES_URL = "http://files-origin.star-made.org/";
		}

		File f = new FileExt("./StarMade-Starter-origin.jar");
		if (f.exists()) {
			Updater.FILES_URL = "http://files-origin.star-made.org/";
		}
		File f2 = new FileExt("./StarMade-Starter-origin.exe");
		if (f2.exists()) {
			Updater.FILES_URL = "http://files-origin.star-made.org/";
		}

		System.err.println("FILES-URL: " + Updater.FILES_URL);
		int backUp = Updater.BACK_DB;
		if (argsSet.contains("-backupall")) {
			backUp = Updater.BACK_ALL;
		} else if (argsSet.contains("-nobackup")) {
			backUp = Updater.BACK_NONE;
		}
		steam = argsSet.contains("-steam");

		boolean selectVersion = argsSet.contains("-version");
		if (argsSet.contains("-nogui")) {

			if (argsSet.contains("-dev")) {
				UpdatePanel.buildBranch = VersionFile.DEV;
			} else if (argsSet.contains("-pre")) {
				UpdatePanel.buildBranch = VersionFile.PRE;
			} else if (argsSet.contains("-archive")) {
				UpdatePanel.buildBranch = VersionFile.ARCHIVE;
			} else {
				UpdatePanel.buildBranch = VersionFile.RELEASE;
			}

			VersionFile buildBranch = UpdatePanel.buildBranch;
			Updater.withoutGUI((args.length > 1 && args[1].equals("-force")), UpdatePanel.installDir, buildBranch, backUp, selectVersion);
		} else {
			EventQueue.invokeLater(() -> {
				try {
					final Launcher frame = new Launcher(!argsSet.contains("-nolookandfeel"));
					frame.setVisible(true);
					(new Thread(() -> {
						//for steam: keep it repaining so the damn overlays go away
						try {
							Thread.sleep(1200);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
						while(frame.isVisible()) {
							try {
								Thread.sleep(500);
							} catch(InterruptedException e) {
								e.printStackTrace();
							}
							EventQueue.invokeLater(frame::repaint);
						}
					})).start();

				} catch (Exception e) {
					e.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e);
				}
			});
		}
	}

	public static void openWebpage(URI uri) {
		DesktopUtils.browseURL(uri);
	}

	public static void askForLauncherUpdate() {
		int rVer;
		try {
			rVer = Updater.getRemoteLauncherVersion();

			String update = "New launcher version found: " + rVer + "\nYou currently are on version: " + version + "\nDo you want to update?";
			if (rVer <= version) {
				update = "You already have the newest Version: " + version + " (Newest: " + rVer + ")\n" +
						"Do you want re-download and overwrite your current launcher?";
			}

			Object[] options = {"Yes",
					"No"};
			int n = JOptionPane.showOptionDialog(UpdatePanel.frame,
					update,
					"Launcher",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[0]); //default button title
			if (n == 0) {
				Updater.selfUpdate(new String[]{});
			} else {
				return;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e1);
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
