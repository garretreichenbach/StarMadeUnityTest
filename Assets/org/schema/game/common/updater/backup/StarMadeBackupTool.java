package org.schema.game.common.updater.backup;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Observable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.schema.game.common.util.DataUtil;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.util.FolderZipper.ZipCallback;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.util.ZipGUICallback;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

public class StarMadeBackupTool extends Observable {

	int file = 0;
	int maxFile = 0;

	public static void backUpWithDialog(final JDialog f, final String installDir, final String databasePath, final String backupName, final String fileExtension, final boolean removeOldVersion, final boolean databaseOnly, final FileFilter filter) throws IOException {

		EventQueue.invokeLater(() -> {
			final StarMadeBackupDialog d = new StarMadeBackupDialog(f);
			d.setVisible(true);

			d.progressBar.setString("exporting...");

			EventQueue.invokeLater(() -> {
				try {
					final StarMadeBackupTool t = new StarMadeBackupTool();
					t.addObserver(d);
					t.backUp(installDir, databasePath, backupName, fileExtension, removeOldVersion, databaseOnly, filter);
					d.dispose();
					JOptionPane.showMessageDialog(f, "Successfully exported.", "Exported", JOptionPane.INFORMATION_MESSAGE);
				} catch(IOException e) {
					e.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e);
				}
			});

		});

	}

	public static void backUpWithDialog(final JFrame f, final String installDir, final String databasePath, final String backupName, final String fileExtension, final boolean removeOldVersion, final boolean databaseOnly, final FileFilter filter) throws IOException {
		final StarMadeBackupDialog d = new StarMadeBackupDialog(f);
		final StarMadeBackupTool t = new StarMadeBackupTool();
		t.addObserver(d);
		d.setVisible(true);

		EventQueue.invokeLater(() -> {

			try {
				t.backUp(installDir, databasePath, backupName, fileExtension, removeOldVersion, databaseOnly, filter);
			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}

			d.dispose();

			JOptionPane.showMessageDialog(f,
					"Successfully exported.",
					"Exported",
					JOptionPane.INFORMATION_MESSAGE);
		});

	}

	public void backUp(String installDir, String databasePath, String backupName, String fileExtension, boolean removeOldVersion, boolean databaseOnly, FileFilter filter) throws IOException {
		File dir = new FileExt(installDir);
		if (dir.exists() && dir.list().length > 0) {
			setChanged();
			notifyObservers("Backing Up");
			String backup = ("backup-StarMade-" + VersionContainer.VERSION + "-" + VersionContainer.build + "_" + backupName + (!fileExtension.startsWith(".") ? ("." + fileExtension) : fileExtension));
			System.out.println("Backing Up (archiving files)");

			file = 0;
			if (databaseOnly) {
				maxFile = FileUtil.countFilesRecusrively((new FileExt(installDir)).getAbsolutePath() + File.separator + databasePath);
			} else {
				maxFile = FileUtil.countFilesRecusrively((new FileExt(installDir)).getAbsolutePath());
			}
			final ZipGUICallback guiCallBack = new ZipGUICallback();
			ZipCallback zipCallback = f -> {
				guiCallBack.f = f;
				guiCallBack.fileMax = maxFile;
				guiCallBack.fileIndex = file;
				setChanged();
				notifyObservers(guiCallBack);
				file++;
			};

			if (databaseOnly) {

				// zip everything except backups themselves
				File f = new FileExt(installDir);
				for (File fg : f.listFiles()) {
					if (fg.isDirectory() && fg.getName().equals(databasePath)) {
						FolderZipper.zipFolder(fg.getAbsolutePath(), backup + ".tmp", "backup-StarMade-", zipCallback, "", filter);
					}
				}
			} else {
				// zip everything except backups themselves
				FolderZipper.zipFolder(installDir, backup + ".tmp", "backup-StarMade-", zipCallback, filter);
			}
			setChanged();
			notifyObservers("resetbars");
			System.out.println("Copying Backup mFile to install dir...");
			File backUpFile = new FileExt(backup + ".tmp");
			if (backUpFile.exists()) {

				File file = new FileExt(new FileExt(installDir).getAbsolutePath() + File.separator + backup);
				System.err.println("Copy to: " + file.getAbsolutePath());
				DataUtil.copy(backUpFile, file);
				backUpFile.delete();
			}

			if (removeOldVersion) {
				setChanged();
				notifyObservers("Deleting old installation");
				System.out.println("Cleaning up current installation");

				//			File oldCatalog = new FileExt(INSTALL_DIR+"/blueprints/Catalog.txt");
				//			if(oldCatalog.exists()){
				//				File backupCatalog = new FileExt(INSTALL_DIR+"/blueprints/Catalog-old-"+System.currentTimeMillis()+".txt");
				//				oldCatalog.renameTo(backupCatalog);
				//			}
				File[] list = dir.listFiles();
				for (int i = 0; i < list.length; i++) {
					File f = list[i];
					if (f.getName().equals("data") ||
							f.getName().equals("native") ||
							f.getName().startsWith("StarMade") ||
							//f.getName().startsWith("server-database") ||
							//f.getName().startsWith("client-database") ||
							f.getName().equals("MANIFEST.MF") ||
							f.getName().equals("version.txt")) {
						FileUtil.deleteDir(f);
						f.delete();
					}
				}
			}

			System.out.println("[BACKUP] DONE");
		}
	}

}
