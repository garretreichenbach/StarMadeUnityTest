package org.schema.game.common.gui.worldmanager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.Starter;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.updater.backup.StarMadeBackupDialog;
import org.schema.game.common.updater.backup.StarMadeBackupTool;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

public class WorldManager extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String DB_PATH = "." + File.separator + "server-database" + File.separator;
	private final JPanel contentPanel = new JPanel();
	private WorldManagerTableModel worldManagerTableModel;
	private WorldManagerTable table;

	/**
	 * Create the dialog.
	 */
	public WorldManager(final JDialog f) {
		super(f);
		setBounds(100, 100, 777, 338);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{217, 0};
		gbl_contentPanel.rowHeights = new int[]{1, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent evt) {
				try {
					GameServerState.readDatabasePosition(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.weighty = 1.0;
			gbc_panel.weightx = 1.0;
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.anchor = GridBagConstraints.NORTHWEST;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				JPanel panel_1 = new JPanel();
				GridBagConstraints gbc_panel_1 = new GridBagConstraints();
				gbc_panel_1.weighty = 1.0;
				gbc_panel_1.insets = new Insets(0, 0, 5, 0);
				gbc_panel_1.fill = GridBagConstraints.BOTH;
				gbc_panel_1.gridx = 0;
				gbc_panel_1.gridy = 0;
				panel.add(panel_1, gbc_panel_1);
				GridBagLayout gbl_panel_1 = new GridBagLayout();
				gbl_panel_1.columnWidths = new int[]{0, 0};
				gbl_panel_1.rowHeights = new int[]{0, 0};
				gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
				panel_1.setLayout(gbl_panel_1);
				{
					{
						JPanel panel_2 = new JPanel();
						GridBagLayout gbl_panel_2 = new GridBagLayout();
						gbl_panel_2.columnWidths = new int[]{0, 0};
						gbl_panel_2.rowHeights = new int[]{0, 0};
						gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
						gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
						panel_2.setLayout(gbl_panel_2);
						{
							worldManagerTableModel = new WorldManagerTableModel();
							table = new WorldManagerTable(worldManagerTableModel, getWorldInfos());
							table.setFillsViewportHeight(true);
							table.setTableHeader(null);
							table.getColumnModel().getColumn(0).setWidth(240);
							table.getColumnModel().getColumn(0).setPreferredWidth(240);
							table.getColumnModel().getColumn(0).setMaxWidth(240);
							table.getColumnModel().getColumn(1).setWidth(50);
							table.getColumnModel().getColumn(1).setMaxWidth(50);
							table.getColumnModel().getColumn(1).setPreferredWidth(50);
							GridBagConstraints gbc_worldManagerTable = new GridBagConstraints();
							gbc_worldManagerTable.fill = GridBagConstraints.BOTH;
							gbc_worldManagerTable.gridx = 0;
							gbc_worldManagerTable.gridy = 0;
							panel_1.add(new JScrollPane(table), gbc_worldManagerTable);
						}
					}

				}
			}
			{
				JPanel panel_1 = new JPanel();
				GridBagConstraints gbc_panel_1 = new GridBagConstraints();
				gbc_panel_1.fill = GridBagConstraints.BOTH;
				gbc_panel_1.gridx = 0;
				gbc_panel_1.gridy = 1;
				panel.add(panel_1, gbc_panel_1);
				GridBagLayout gbl_panel_1 = new GridBagLayout();
				gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
				gbl_panel_1.rowHeights = new int[]{0, 0};
				gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				panel_1.setLayout(gbl_panel_1);
				{
					JButton btnCreateNew = new JButton("Create New");
					btnCreateNew.addActionListener(e -> {
						Object[] possibilities = null;
						String s = (String) JOptionPane.showInputDialog(
								WorldManager.this,
								"Name your world (use letters and numbers only)",
								"World Name",
								JOptionPane.PLAIN_MESSAGE,
								null,
								possibilities,
								"myWorld");

						//If a string was returned, say so.
						if ((s != null) && (s.length() > 0)) {
							createWorld(s);
							return;
						}
						//open new world dialog
					});
					GridBagConstraints gbc_btnCreateNew = new GridBagConstraints();
					gbc_btnCreateNew.insets = new Insets(0, 0, 0, 5);
					gbc_btnCreateNew.gridx = 0;
					gbc_btnCreateNew.gridy = 0;
					panel_1.add(btnCreateNew, gbc_btnCreateNew);
				}
				{
					JButton btnSetAsDefault = new JButton("Set as default");
					btnSetAsDefault.addActionListener(e -> {

						//do all the startup stuff
						int selectedRow = table.getSelectedRow();
						if (selectedRow >= 0) {
							String name = table.getValueAt(selectedRow, 0).toString();
							ServerConfig.WORLD.setString(name);
							try {
								ServerConfig.write();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							try {
								GameServerState.readDatabasePosition(false);
								Starter.doMigration(f, false);
								Starter.copyDefaultBB(true);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							worldManagerTableModel.replaceAll(getWorldInfos());
						}
					});
					GridBagConstraints gbc_btnSetAsDefault = new GridBagConstraints();
					gbc_btnSetAsDefault.insets = new Insets(0, 0, 0, 5);
					gbc_btnSetAsDefault.gridx = 1;
					gbc_btnSetAsDefault.gridy = 0;
					panel_1.add(btnSetAsDefault, gbc_btnSetAsDefault);
				}
				{
					JButton btnExport = new JButton("Export");
					btnExport.addActionListener(e -> {
						int selectedRow = table.getSelectedRow();
						if (selectedRow >= 0) {
							String name = table.getValueAt(selectedRow, 0).toString();
							backUp(name);

						}
						//do backup in zip
					});
					GridBagConstraints gbc_btnExport = new GridBagConstraints();
					gbc_btnExport.insets = new Insets(0, 0, 0, 5);
					gbc_btnExport.gridx = 2;
					gbc_btnExport.gridy = 0;
					panel_1.add(btnExport, gbc_btnExport);
				}
				{
					JButton btnImport = new JButton("Import");
					btnImport.addActionListener(e -> {

						JFileChooser fc = new JFileChooser(new FileExt("./"));
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fc.setAcceptAllFileFilterUsed(false);
						fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

							@Override
							public boolean accept(File arg0) {
								if (arg0.isDirectory()) {
									return true;
								}
								if (arg0.getName().endsWith(".smdb")) {
									return true;
								}
								return false;
							}

							@Override
							public String getDescription() {
								return "StarMade Database (.smdb)";
							}
						});
						fc.setAcceptAllFileFilterUsed(false);
						//Show it.
						int returnVal = fc.showDialog(WorldManager.this, "Choose database to import");

						//Process the results.
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();

							Object[] possibilities = null;
							String s = (String) JOptionPane.showInputDialog(
									WorldManager.this,
									"Name your world (use letters and numbers only)",
									"World Name",
									JOptionPane.PLAIN_MESSAGE,
									null,
									possibilities,
									"myWorld");

							//If a string was returned, say so.
							if ((s != null) && (s.length() > 0)) {
								if (s.toLowerCase(Locale.ENGLISH).equals("old")) {
									JOptionPane.showMessageDialog(WorldManager.this,
											"Cannot create world. This name is not permitted.",
											"Error",
											JOptionPane.ERROR_MESSAGE);
								} else {
									File f1 = new FileExt(DB_PATH + s);

									if (!f1.exists()) {
										importDB(file, s);
									} else {
										JOptionPane.showMessageDialog(WorldManager.this,
												"Cannot create world. A world with that name already exists.",
												"Error",
												JOptionPane.ERROR_MESSAGE);
									}
								}
							} else {
								JOptionPane.showMessageDialog(WorldManager.this,
										"Cannot create world. Name must not be empty.",
										"Error",
										JOptionPane.ERROR_MESSAGE);
							}

						} else {
						}

						//extract from backup

					});
					GridBagConstraints gbc_btnImport = new GridBagConstraints();
					gbc_btnImport.insets = new Insets(0, 0, 0, 5);
					gbc_btnImport.gridx = 3;
					gbc_btnImport.gridy = 0;
					panel_1.add(btnImport, gbc_btnImport);
				}
				{
					JButton btnDelete = new JButton("Delete");
					btnDelete.addActionListener(e -> {

						int selectedRow = table.getSelectedRow();
						if (selectedRow >= 0) {
							String name = table.getValueAt(selectedRow, 0).toString();
							String path = table.getValueAt(selectedRow, 2).toString();

							//Custom button text
							Object[] options = {"Backup and Delete",
									"Delete without backup (not recommended)",
									"Cancel"};
							int n = JOptionPane.showOptionDialog(WorldManager.this,
									"Do you really want to delete",
									"Delete Database Confirmation",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null,
									options,
									options[2]);

							switch (n) {
								case (0):

									delete(name, path, true);
									break;
								case (1):
									delete(name, path, false);
									break;
								case (2):
									break;
							}
						}
						//Delete world confirm
						//do special handling for 'old'
					});
					GridBagConstraints gbc_btnDelete = new GridBagConstraints();
					gbc_btnDelete.gridx = 4;
					gbc_btnDelete.gridy = 0;
					panel_1.add(btnDelete, gbc_btnDelete);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Back");
				cancelButton.addActionListener(e -> dispose());
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public static List<WorldInfo> getWorldInfos() {
		ArrayList<WorldInfo> infos = new ArrayList<WorldInfo>();

		File dir = new FileExt(DB_PATH);
		File[] listFiles = dir.listFiles(File::isDirectory);

		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].getName().toLowerCase(Locale.ENGLISH).equals("index")) {
				infos.add(new WorldInfo("old", dir.getAbsolutePath(), ServerConfig.WORLD.getString().toLowerCase(Locale.ENGLISH).equals("old")));
				continue;
			}
			if (listFiles[i].getName().toLowerCase(Locale.ENGLISH).equals("data")) {
				//belongs to old
				continue;
			}
			infos.add(new WorldInfo(listFiles[i].getName(), listFiles[i].getAbsolutePath(), ServerConfig.WORLD.getString().toLowerCase(Locale.ENGLISH).equals(listFiles[i].getName().toLowerCase(Locale.ENGLISH))));

		}

		return infos;
	}

	private void createWorld(String s) {

		if (s.toLowerCase(Locale.ENGLISH).equals("old")) {
			JOptionPane.showMessageDialog(this,
					"Cannot create world. This name is not permitted.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		} else {
			File f = new FileExt(DB_PATH + s);
			if (f.exists()) {
				JOptionPane.showMessageDialog(this,
						"Cannot create world. A world with that name already exists.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				File pp = new FileExt(DB_PATH);

				List<File> catalogFile = new ArrayList<File>();
				if (pp.exists() && pp.isDirectory()) {
					for (File d : pp.listFiles()) {
						File c = new FileExt(DB_PATH + d.getName() + File.separator + CatalogManager.CALAOG_FILE_NAME_FULL);
						if (c.exists()) {
							catalogFile.add(c);
						}
					}
				}
				Collections.sort(catalogFile, (o1, o2) -> {
					//Long.compare causes problems on some java versions
					return o1.lastModified() < o2.lastModified() ? 1 : (o1.lastModified() > o2.lastModified() ? -1 : 0);
				});
				f.mkdir();
				if (catalogFile.size() > 0) {

					FS[] m = new FS[catalogFile.size()];
					for (int i = 0; i < catalogFile.size(); i++) {
						m[i] = new FS(catalogFile.get(i));
					}

					FS ss = (FS) JOptionPane.showInputDialog(
							this,
							"Copy catalog from another world?\n\n"
									+ "Pressing cancel will not delete your blueprints,\n"
									+ "but all catalog entries will reset their permissions (owner, pirate usable, etc).",
							"Copy Catalog",
							JOptionPane.PLAIN_MESSAGE,
							null,
							m,
							null);
					if (ss != null) {

						File to = new FileExt(DB_PATH + s + File.separator + CatalogManager.CALAOG_FILE_NAME_FULL);

						try {
							FileUtil.copyFile(ss.f, to);
						} catch (IOException e) {
							e.printStackTrace();
						}

					}

				}
				worldManagerTableModel.replaceAll(getWorldInfos());
			}
		}

	}

	private void delete(String name, String path, boolean backup) {
		if (backup) {
			backUp(name);
		}

		if (name.toLowerCase(Locale.ENGLISH).equals("old")) {
			File dir = new FileExt(DB_PATH);
			File[] listFiles = dir.listFiles(pathname -> {
				if (pathname.getName().toLowerCase(Locale.ENGLISH).equals("index") || pathname.getName().toLowerCase(Locale.ENGLISH).equals("data")) {
					return true;
				}
				return !pathname.isDirectory();
			});

			for (File f : listFiles) {
				if (f.isDirectory()) {
					try {
						FileUtil.deleteRecursive(f);
					} catch (IOException e) {
						e.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e);
					}
				} else {
					f.delete();
				}
			}
		} else {
			try {
				FileUtil.deleteRecursive(new FileExt("." + File.separator, "server-database" + File.separator + name));
			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
		}
		worldManagerTableModel.replaceAll(getWorldInfos());
	}

	private void importDB(final File zipFile, final String name) {

		SwingUtilities.invokeLater(() -> {
			StarMadeBackupDialog starMadeBackupDialog = new StarMadeBackupDialog(WorldManager.this);
			starMadeBackupDialog.setVisible(true);
			try {
				FileUtil.extract(zipFile,
						DB_PATH + name + File.separator, "server-database",
						starMadeBackupDialog);

				JOptionPane.showMessageDialog(WorldManager.this,
						"Successfully imported world.",
						"Imported",
						JOptionPane.INFORMATION_MESSAGE);
				worldManagerTableModel.replaceAll(getWorldInfos());

			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
			starMadeBackupDialog.dispose();
		});

	}

	private void backUp(String name) {
		try {
			FileFilter f = pathname -> {
				if (pathname.getName().toLowerCase(Locale.ENGLISH).equals("index") || pathname.getName().toLowerCase(Locale.ENGLISH).equals("data") || pathname.getName().toLowerCase(Locale.ENGLISH).equals("server-database")) {
					return true;
				}
				return !pathname.isDirectory();
			};
			if (name.equals("old")) {
				StarMadeBackupTool.backUpWithDialog(WorldManager.this, "." + File.separator, "server-database", String.valueOf(System.currentTimeMillis()), ".smdb", false, true, f);
			} else {
				StarMadeBackupTool.backUpWithDialog(WorldManager.this, "." + File.separator, "server-database" + File.separator + name, name + String.valueOf(System.currentTimeMillis()), ".smdb", false, true, f);
			}

		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	private class FS {
		File f;

		public FS(File f) {
			super();
			this.f = f;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.toString().equals(obj.toString());
		}

		@Override
		public String toString() {
			return f.getParentFile().getName();
		}

	}

}
