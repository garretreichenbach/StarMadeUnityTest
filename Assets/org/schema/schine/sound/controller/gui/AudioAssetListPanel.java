package org.schema.schine.sound.controller.gui;

import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioGUIException;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.asset.AudioAssetManager;
import org.schema.schine.sound.manager.engine.AudioEnvironment;
import org.schema.schine.sound.manager.engine.AudioNode;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.IOException;
import java.util.Enumeration;

public class AudioAssetListPanel extends JPanel implements AudioChangeListener {

	private static AudioNode currentPlaying;
	JTree tree;
	private final JScrollPane scrollPane;
	public boolean onButtonDraggable;
	private final JButton btnRefresh;
	private final DefaultMutableTreeNode root;
	private final JPanel panelSoundTest;
	private final JButton btnPlay;
	private final JButton btnStop;
	private JCheckBox chckbxLoop;
	private JCheckBox chckbxSpatialTest;
	private JCheckBox chckbxReverb;
	private JComboBox<AudioEnvironment.AudioEnvironments> comboBoxEnv;

	/**
	 * Create the panel.
	 */
	public AudioAssetListPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0};
		gridBagLayout.columnWeights = new double[] {1.0, 1.0};
		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0};
		setLayout(gridBagLayout);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		root = new DefaultMutableTreeNode("Audio");
		try {
			fillTreeWithFileData(tree, root, AudioController.instance.getConfig().assetManager.buildHirachy());
		} catch(Exception e) {
			e.printStackTrace();
		}
		createTree(root);

		JButton btnNewButton = new JButton("Update From Directory");
		btnNewButton.addActionListener(e -> {
			try {
				AudioController.instance.getConfig().assetManager.readAndCombineEntriesFromDir();
				refresh();
			} catch(IOException e1) {
				e1.printStackTrace();
			}
		});

		btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(e -> refresh());

		panelSoundTest = new JPanel();
		panelSoundTest.setBorder(new TitledBorder(null, "Play Test", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelSoundTest = new GridBagConstraints();
		gbc_panelSoundTest.gridwidth = 2;
		gbc_panelSoundTest.insets = new Insets(0, 0, 5, 5);
		gbc_panelSoundTest.fill = GridBagConstraints.BOTH;
		gbc_panelSoundTest.gridx = 0;
		gbc_panelSoundTest.gridy = 1;
		add(panelSoundTest, gbc_panelSoundTest);
		GridBagLayout gbl_panelSoundTest = new GridBagLayout();
		gbl_panelSoundTest.columnWidths = new int[] {0, 0, 0, 0, 0};
		gbl_panelSoundTest.rowHeights = new int[] {0, 0, 0};
		gbl_panelSoundTest.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panelSoundTest.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
		panelSoundTest.setLayout(gbl_panelSoundTest);

		btnPlay = new JButton("Play");
		btnPlay.addActionListener(e -> {
			TreePath selectionPath = tree.getSelectionPath();
			if(selectionPath != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

				if(node.getUserObject() instanceof AudioAsset) {
					AudioAsset a = (AudioAsset) node.getUserObject();
					try {
						currentPlaying = AudioController.instance.playTestAudio(a, chckbxLoop.isSelected(), chckbxSpatialTest.isSelected(), chckbxReverb.isSelected(), ((AudioEnvironment.AudioEnvironments) comboBoxEnv.getSelectedItem()).getEnv());
					} catch(AudioGUIException e1) {
						JFrame frame = (JFrame) SwingUtilities.getRoot(this);
						JOptionPane.showMessageDialog(frame, e1.getMessage());
					}
					System.err.println("[GUI] AUDIO NOW PLAYING " + currentPlaying);
				} else {
					System.err.println("[GUI] no asset selected");
				}
			} else {
				System.err.println("[GUI] nothing selected");
			}
		});
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPlay.insets = new Insets(0, 0, 5, 5);
		gbc_btnPlay.gridx = 0;
		gbc_btnPlay.gridy = 0;
		panelSoundTest.add(btnPlay, gbc_btnPlay);

		btnStop = new JButton("Stop");
		btnStop.addActionListener(e -> AudioController.instance.stopTestAudio());
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStop.insets = new Insets(0, 0, 5, 5);
		gbc_btnStop.gridx = 1;
		gbc_btnStop.gridy = 0;
		panelSoundTest.add(btnStop, gbc_btnStop);

		chckbxLoop = new JCheckBox("Loop");
		GridBagConstraints gbc_chckbxLoop = new GridBagConstraints();
		gbc_chckbxLoop.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxLoop.gridx = 0;
		gbc_chckbxLoop.gridy = 1;
		panelSoundTest.add(chckbxLoop, gbc_chckbxLoop);

		chckbxSpatialTest = new JCheckBox("Spatial Test (Mono clips only)");
		GridBagConstraints gbc_chckbxSpatialTest = new GridBagConstraints();
		gbc_chckbxSpatialTest.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxSpatialTest.gridx = 1;
		gbc_chckbxSpatialTest.gridy = 1;
		panelSoundTest.add(chckbxSpatialTest, gbc_chckbxSpatialTest);

		chckbxReverb = new JCheckBox("Reverb");
		GridBagConstraints gbc_chckbxReverb = new GridBagConstraints();
		gbc_chckbxReverb.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxReverb.gridx = 2;
		gbc_chckbxReverb.gridy = 1;
		panelSoundTest.add(chckbxReverb, gbc_chckbxReverb);

		comboBoxEnv = new JComboBox(AudioEnvironment.AudioEnvironments.values());
		GridBagConstraints gbc_comboBoxEnv = new GridBagConstraints();
		gbc_comboBoxEnv.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxEnv.gridx = 3;
		gbc_comboBoxEnv.gridy = 1;
		panelSoundTest.add(comboBoxEnv, gbc_comboBoxEnv);

		GridBagConstraints gbc_btnRefresh = new GridBagConstraints();
		gbc_btnRefresh.fill = GridBagConstraints.BOTH;
		gbc_btnRefresh.weightx = 1.0;
		gbc_btnRefresh.insets = new Insets(0, 0, 0, 5);
		gbc_btnRefresh.gridx = 0;
		gbc_btnRefresh.gridy = 2;
		add(btnRefresh, gbc_btnRefresh);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.BOTH;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 2;
		add(btnNewButton, gbc_btnNewButton);

		AudioController.instance.addChangeListener(this);

		setPlayButtons(true);
	}

	private void refresh() {
		root.removeAllChildren();
		fillTreeWithFileData(tree, root, AudioController.instance.getConfig().assetManager.buildHirachy());
		createTree(root);

		if(onButtonDraggable) {
			AudioAssetTreeTransferHandler.draggableToButtonTree = tree;
		}
	}

	private void createTree(DefaultMutableTreeNode root) {
		tree = new JTree(root);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new AudioAssetTreeTransferHandler());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if(node != null) {
				if(node.getUserObject() instanceof AudioAsset) {
					AudioController.instance.onSelectedAsset((AudioAsset) node.getUserObject());
				}
			}
		});

		scrollPane.setViewportView(tree);
		revalidate();
		repaint();
		expandTree(tree);
	}

	public void fillTreeWithFileData(JTree tree, DefaultMutableTreeNode parent, AudioAssetManager.AudioAssetCat buildHirachy) {

		assert (parent.getAllowsChildren());
		if(buildHirachy.asset == null) {
			DefaultMutableTreeNode dir = new DefaultMutableTreeNode(buildHirachy.name);
			dir.setAllowsChildren(true);
			parent.add(dir);
			for(AudioAssetManager.AudioAssetCat f : buildHirachy.children) {
				fillTreeWithFileData(tree, dir, f);
			}
		} else {
			DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(buildHirachy.asset);
			fileNode.setAllowsChildren(false);
			parent.add(fileNode);
		}

	}

	private void expandTree(JTree tree) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		Enumeration<?> e = root.breadthFirstEnumeration();
		while(e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if(node.isLeaf()) continue;
			int row = tree.getRowForPath(new TreePath(node.getPath()));
			tree.expandRow(row);
		}
	}

	public void onBecomingActive(boolean active) {
		if(active) {
			refresh();
		}
	}

	@Override
	public void onAudioStop(AudioNode n) {
		if(n == currentPlaying) {

			currentPlaying = null;
		}
		setPlayButtons(true);
	}

	@Override
	public void onAudioPlay(AudioNode n) {
		setPlayButtons(false);
	}

	private void setPlayButtons(boolean b) {
		btnPlay.setEnabled(b);
		btnStop.setEnabled(!b);
		chckbxLoop.setEnabled(b);
		chckbxReverb.setEnabled(b);
		chckbxSpatialTest.setEnabled(b);
		comboBoxEnv.setEnabled(b);
	}

	@Override
	public void onAudioPause(AudioNode n) {

	}
}
