package org.schema.game.common.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.schema.game.common.Starter;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.game.server.controller.ImportFailedException;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.resource.FileExt;

public class CatalogPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTable table;
	private CatalogTableModel tableModel;
	private JFileChooser fc;
	private CatalogManagerEditorController jFrame;

	/**
	 * Create the panel.
	 */
	public CatalogPanel( final CatalogManagerEditorController man) {
		this.jFrame = man;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{50, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0};
		setLayout(gridBagLayout);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.weighty = 10.0;
		gbc_scrollPane_1.weightx = 1.0;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		add(scrollPane_1, gbc_scrollPane_1);
		this.tableModel = new CatalogTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setMaximumSize(new Dimension(2147483647, 2147483647));
		table.setSize(new Dimension(0, 300));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setFillsViewportHeight(true);
		table.setPreferredScrollableViewportSize(new Dimension(0, 170));
		table.setMinimumSize(new Dimension(100, 100));
		table.setAutoCreateRowSorter(true);
		scrollPane_1.setViewportView(table);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 2.0;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(e -> remove());

		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(e -> importFile());
		panel.add(btnImport);

		JButton btnExport = new JButton("Export");
		btnExport.addActionListener(arg0 -> export());
		panel.add(btnExport);
		panel.add(btnRemove);

		JButton btnUpload = new JButton("Upload");
		btnUpload.addActionListener(arg0 -> upload());
		panel.add(btnUpload);
		if (Starter.currentSession == null) {
			btnUpload.setEnabled(false);
			btnUpload.setText("Upload (Login needed)");
		}

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		panel_1.setLayout(gbl_panel_1);

		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(arg0 -> man.dispose());
		btnExit.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.weightx = 10.0;
		gbc_btnExit.insets = new Insets(0, 0, 0, 5);
		gbc_btnExit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnExit.gridx = 0;
		gbc_btnExit.gridy = 0;
		panel_1.add(btnExit, gbc_btnExit);

	}

	private void export() {
		BlueprintEntry segmentControllerBluePrintEntry = tableModel.getReadBluePrints().get(table.convertRowIndexToModel(table.getSelectedRow()));
		try {
			File export = BluePrintController.active.export(segmentControllerBluePrintEntry.getName());
			Object[] options = {"Ok"};
			int n = JOptionPane.showOptionDialog(jFrame, "Entry has been exported to\n" +
							export.getAbsolutePath(), "Exported",
					JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
					null, options, options[0]);
			switch (n) {
				case 0:
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch (CatalogEntryNotFoundException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	private void importFile() {
		if (fc == null) {
			fc = new JFileChooser(new FileExt("./"));
			fc.addChoosableFileFilter(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					}
					if (arg0.getName().endsWith(".sment")) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "StarMade Entitiy (.sment)";
				}
			});
			fc.setAcceptAllFileFilterUsed(false);
		}
		//Show it.
		int returnVal = fc.showDialog(jFrame, "Import");

		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.err.println("Accepted File " + file.getAbsolutePath());
			try {
				BluePrintController.active.importFile(file, null);
				Starter.doMigration(jFrame, true);
			} catch (ImportFailedException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
			tableModel.refreshBluePrints();
		} else {
		}

	}

	private void remove() {

		try {
			BlueprintEntry entry = (BlueprintEntry)tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), -1);
			
			Object[] options = {"Yes, delete this entry", "Cancel"};
			int n = JOptionPane.showOptionDialog(jFrame, "WARNING: you are about to delete the entry\n"+entry.getName()+"\nfrom your ship catalog.\n" +
							"this action cannot be undone!", "Delete Ship Entry",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[1]);
			switch (n) {
				case 0:
					BluePrintController.active.removeBluePrint(entry);
					tableModel.refreshBluePrints();
					break;
				case 1:
					break;
			}

		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void upload() {
		try {
			BlueprintEntry segmentControllerBluePrintEntry = tableModel.getReadBluePrints().get(table.getSelectedRow());
			File export = BluePrintController.active.export(segmentControllerBluePrintEntry.getName());
			if (Starter.currentSession != null) {
				Starter.currentSession.upload(
						export,
						segmentControllerBluePrintEntry.getName(),
						segmentControllerBluePrintEntry.getEntityType().ordinal(),
						"test desc",
						"publicLicence",
						jFrame);
			}

		} catch (Exception e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}

	}
}
