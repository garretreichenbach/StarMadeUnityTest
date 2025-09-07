package org.schema.schine.graphicsengine.psys;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.resource.FileExt;

public class ParticleSystemGUI extends JFrame {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ParticleSystemConfiguration currentSystem;
	private JFileChooser fc;
	private JPanel panel_1;

	/**
	 * Create the frame.
	 */
	public ParticleSystemGUI(final StateInterface state) {

		setTitle("StarMade ParticleSystem");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(GLFrame.getWidth() - 40, 50, 690, 578);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(e -> {
			currentSystem = ParticleSystemConfiguration.fromScratch();

			updateCurrentSystem();
		});
		mnFile.add(mntmNew);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(e -> openOpenFile());
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(e -> {
			if (currentSystem != null) {
				if (currentSystem.saveName == null) {
					openSaveAs();
				} else {
					currentSystem.save(new FileExt(currentSystem.saveName));
				}
			}
		});
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save As");
		mntmSaveAs.addActionListener(e -> {
			if (currentSystem != null) {
				openSaveAs();
			}
		});
		mnFile.add(mntmSaveAs);

		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(e -> {
			try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
		});
		mnFile.add(mntmClose);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));

		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JButton btnStart = new JButton("Start");
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 0;
		btnStart.addActionListener(e -> {
			if (currentSystem != null && state != null) {
				System.err.println("STARTING PARTICLE SYSTEM AT: " + ((ClientState) state).getCurrentPosition().origin);
				ClientState cs = ((ClientState) state);
				cs.getParticleSystemManager().startParticleSystemWorld(currentSystem, ((ClientState) state).getCurrentPosition());
			}
		});
		panel.add(btnStart, gbc_btnStart);

		JButton btnPause = new JButton("Pause");
		btnPause.addActionListener(e -> {
			ClientState cs = ((ClientState) state);
			cs.getParticleSystemManager().pauseParticleSystemsWorld();
		});
		GridBagConstraints gbc_btnPause = new GridBagConstraints();
		gbc_btnPause.insets = new Insets(0, 0, 0, 5);
		gbc_btnPause.gridx = 2;
		gbc_btnPause.gridy = 0;
		panel.add(btnPause, gbc_btnPause);

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(e -> {
			ClientState cs = ((ClientState) state);
			cs.getParticleSystemManager().stopParticleSystemsWorld();
		});
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.gridx = 4;
		gbc_btnStop.gridy = 0;
		panel.add(btnStop, gbc_btnStop);

		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{1};
		gbl_panel_1.rowHeights = new int[]{1};
		gbl_panel_1.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				ParticleSystemGUI frame = new ParticleSystemGUI(null);
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void openSaveAs() {
		File f = chooseFile(ParticleSystemGUI.this, "Save As...");
		if (f != null) {
			currentSystem.saveName = f.getAbsolutePath();
			currentSystem.save(f);
		}
	}

	private void openOpenFile() {
		File f = chooseFile(ParticleSystemGUI.this, "Open ParticleSystemConfig...");
		if (f != null) {
			try {
				currentSystem = ParticleSystemConfiguration.fromFile(f, true);
				updateCurrentSystem();
			} catch (Exception e) {
				e.printStackTrace();
				GLFrame.processErrorDialogExceptionWithoutReportWithContinue(e, null);
			}
		}
	}

	private File chooseFile(JFrame frame, String title) {
		File d = new FileExt("./data/effects/particles/");
		System.err.println("PATH: " + d.getAbsolutePath());
		if (d.getAbsolutePath().contains("\\schine\\")) {
			d = new FileExt("../schine-starmade/data/effects/particles/");
		}
		d.mkdirs();
		if (fc == null) {
			fc = new JFileChooser(d);
			fc.addChoosableFileFilter(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.isDirectory() || arg0.getName().endsWith(".xml");
				}

				@Override
				public String getDescription() {
					return "StarMade BlockConfig (.xml)";
				}
			});
			fc.setAcceptAllFileFilterUsed(false);
		}
		//Show it.
		int returnVal = fc.showDialog(frame, title);

		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

	private void updateCurrentSystem() {
		panel_1.removeAll();
		JPanel panel = currentSystem.getPanel();
		GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
		gbc_equationDisplay.weighty = 1.0;
		gbc_equationDisplay.weightx = 1.0;
		gbc_equationDisplay.fill = GridBagConstraints.BOTH;
		gbc_equationDisplay.gridx = 0;
		gbc_equationDisplay.gridy = 0;
		panel_1.add(panel, gbc_equationDisplay);
		panel_1.revalidate();
		panel_1.repaint();
	}
}
