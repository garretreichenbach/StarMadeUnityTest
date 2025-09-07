package org.schema.game.common.updater.backup;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.updater.FileDownloadUpdate;
import org.schema.game.common.util.FolderZipper.ZipCallback;
import org.schema.game.common.util.ZipGUICallback;

public class StarMadeBackupDialog extends JDialog implements Observer, ZipCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public StarMadeBackupDialog(JFrame f) {
		super(f);

		init();
	}

	/**
	 * Create the dialog.
	 *
	 * @wbp.parser.constructor
	 */
	public StarMadeBackupDialog(JDialog f) {
		super(f);
		init();
	}

	public void init() {
		setTitle("Exporting StarMade Database");
		setBounds(100, 100, 494, 100);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{217, 0};
		gbl_contentPanel.rowHeights = new int[]{1, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
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
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				progressBar = new JProgressBar();
				progressBar.setStringPainted(true);
				progressBar.setString("0%");
				GridBagConstraints gbc_progressBar = new GridBagConstraints();
				gbc_progressBar.fill = GridBagConstraints.BOTH;
				gbc_progressBar.weighty = 1.0;
				gbc_progressBar.weightx = 1.0;
				gbc_progressBar.gridx = 0;
				gbc_progressBar.gridy = 0;
				panel.add(progressBar, gbc_progressBar);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
		}
	}

	@Override
	public void update(Observable obser, final Object o) {

		EventQueue.invokeLater(() -> {
			if (o != null) {
				if (o.equals("resetbars")) {
					progressBar.setString("");
					progressBar.setValue(0);
				} else if (o.equals("reload Versions")) {
				} else if (o.equals("versions loaded")) {

					progressBar.setString("");

				} else if (o.equals("updating")) {

				} else if (o.equals("finished")) {
					System.err.println("FINISHED Update");

				} else if (o.equals("reset")) {
					progressBar.setString("");
					progressBar.setValue(0);

				} else if (o instanceof ZipGUICallback) {
					ZipGUICallback p = (ZipGUICallback) o;
					int pc = (int) Math.ceil(((float) p.fileIndex / (float) p.fileMax) * 100);
					progressBar.setString("Exporting: " + pc + "  %");
					progressBar.setValue(pc);
				} else if (o instanceof FileDownloadUpdate) {

					return;
				} else {
					if (o instanceof String) {
						progressBar.setString(o.toString());
					}
				}

				progressBar.repaint();
			}
		});
	}

	@Override
	public void update(File f) {
		progressBar.setString("extracting: " + f.getAbsolutePath());
		progressBar.setValue(0);
	}

}
