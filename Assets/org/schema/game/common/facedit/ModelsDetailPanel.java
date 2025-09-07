package org.schema.game.common.facedit;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.schema.common.util.data.DataUtil;
import org.schema.game.common.facedit.model.Model;
import org.schema.game.common.facedit.model.SceneFile;
import org.schema.game.common.facedit.model.SceneNode;
import org.schema.game.common.facedit.model.SceneNode.MaterialEntity;
import org.schema.game.common.facedit.model.SceneNode.MeshEntity;
import org.schema.schine.common.util.FileUtil;

public class ModelsDetailPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5398434420996512730L;
	private JTextField nameField;
	private JTextField relPathField;
	private JTextField fileNameField;
	private JTextField meshFileField;
	private JTextField posXField;
	private JTextField posYField;
	private JTextField posZField;
	private JTextField scaleXField;
	private JTextField scaleYField;
	private JTextField scaleZField;
	private JTextField rotXField;
	private JTextField rotYField;
	private JTextField rotZField;
	private JTextField rotWField;
	private JTextField diffuseField;
	private JTextField normalField;
	private JTextField emissionField;
	private Model model;
	private JTextField nodeNameField;
	private JTextField entityNameField;
	private JTextField materialNameField;
	private JTextField materialFileName;
	protected boolean changed;

	/**
	 * Create the panel.
	 */
	public ModelsDetailPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		JPanel scenePanel = new JPanel();
		scenePanel.setBorder(new TitledBorder(null, "Scene", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scenePanel = new GridBagConstraints();
		gbc_scenePanel.insets = new Insets(0, 0, 5, 0);
		gbc_scenePanel.fill = GridBagConstraints.BOTH;
		gbc_scenePanel.gridx = 0;
		gbc_scenePanel.gridy = 0;
		add(scenePanel, gbc_scenePanel);
		GridBagLayout gbl_scenePanel = new GridBagLayout();
		gbl_scenePanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_scenePanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_scenePanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_scenePanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		scenePanel.setLayout(gbl_scenePanel);
		
		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		scenePanel.add(lblName, gbc_lblName);
		
		nameField = new JTextField();
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.insets = new Insets(0, 0, 5, 5);
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.gridx = 1;
		gbc_nameField.gridy = 0;
		scenePanel.add(nameField, gbc_nameField);
		nameField.setColumns(10);
		
		JLabel lblRelativePath = new JLabel("Relative Path");
		GridBagConstraints gbc_lblRelativePath = new GridBagConstraints();
		gbc_lblRelativePath.anchor = GridBagConstraints.EAST;
		gbc_lblRelativePath.insets = new Insets(0, 0, 5, 5);
		gbc_lblRelativePath.gridx = 0;
		gbc_lblRelativePath.gridy = 1;
		scenePanel.add(lblRelativePath, gbc_lblRelativePath);
		
		relPathField = new JTextField();
		relPathField.setEditable(false);
		GridBagConstraints gbc_relPathField = new GridBagConstraints();
		gbc_relPathField.insets = new Insets(0, 0, 5, 5);
		gbc_relPathField.fill = GridBagConstraints.HORIZONTAL;
		gbc_relPathField.gridx = 1;
		gbc_relPathField.gridy = 1;
		scenePanel.add(relPathField, gbc_relPathField);
		relPathField.setColumns(10);
		
		JLabel lblFileName = new JLabel("File Name");
		GridBagConstraints gbc_lblFileName = new GridBagConstraints();
		gbc_lblFileName.anchor = GridBagConstraints.EAST;
		gbc_lblFileName.insets = new Insets(0, 0, 0, 5);
		gbc_lblFileName.gridx = 0;
		gbc_lblFileName.gridy = 2;
		scenePanel.add(lblFileName, gbc_lblFileName);
		
		fileNameField = new JTextField();
		fileNameField.setEditable(false);
		GridBagConstraints gbc_fileNameField = new GridBagConstraints();
		gbc_fileNameField.insets = new Insets(0, 0, 0, 5);
		gbc_fileNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileNameField.gridx = 1;
		gbc_fileNameField.gridy = 2;
		scenePanel.add(fileNameField, gbc_fileNameField);
		fileNameField.setColumns(10);
		
		JButton btnChangeSceneFile = new JButton("Browse");
		btnChangeSceneFile.addActionListener(arg0 -> {
			ModelsDetailPanel.this.changed = true;
			JFileChooser fileChooser = new JFileChooser("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return ".scene";
				}

				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".scene");
				}
			});
			int option = fileChooser.showOpenDialog(ModelsDetailPanel.this);
			if(option == JFileChooser.APPROVE_OPTION) {
				File f = fileChooser.getSelectedFile();
				File reqParent = new File("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator);

				File fTest = f;
				boolean ok = false;
				while(fTest != null ) {
					try {
						if(fTest.getCanonicalPath().equals(reqParent.getCanonicalPath())) {
							ok = true;
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					fTest = fTest.getParentFile();
				}
				if(!ok) {
					JOptionPane.showMessageDialog(ModelsDetailPanel.this,
						    "Scene file must be located in a subdirectory of:\n\n"+reqParent.getAbsolutePath()+"\n\nBut was:\n"+f.getAbsolutePath(),
						    "Path Error",
						    JOptionPane.ERROR_MESSAGE);
				}else {
					String prevFileName = model.filename;
					String prevRelPath = model.relpath;
					model.filename = f.getName();
					model.relpath = f.getAbsolutePath().substring(reqParent.getAbsolutePath().length()-1, f.getAbsolutePath().length() - f.getName().length()).trim();
					try {
						model.initialize(null);
						fill(model);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(ModelsDetailPanel.this,
							    "An error happened:\n\n"+e.getClass().getSimpleName()+"\n"+e.getMessage(),
							    "Parse Error",
							    JOptionPane.ERROR_MESSAGE);
						model.filename = prevFileName;
						model.relpath = prevRelPath;
					}

				}
			}
		});
		GridBagConstraints gbc_btnChangeSceneFile = new GridBagConstraints();
		gbc_btnChangeSceneFile.gridx = 2;
		gbc_btnChangeSceneFile.gridy = 2;
		scenePanel.add(btnChangeSceneFile, gbc_btnChangeSceneFile);
		
		JPanel meshPanel = new JPanel();
		meshPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Node", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_meshPanel = new GridBagConstraints();
		gbc_meshPanel.insets = new Insets(0, 0, 5, 0);
		gbc_meshPanel.fill = GridBagConstraints.BOTH;
		gbc_meshPanel.gridx = 0;
		gbc_meshPanel.gridy = 1;
		add(meshPanel, gbc_meshPanel);
		GridBagLayout gbl_meshPanel = new GridBagLayout();
		gbl_meshPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_meshPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_meshPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_meshPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		meshPanel.setLayout(gbl_meshPanel);
		
		JLabel lblMeshNodeName = new JLabel("Node Name");
		GridBagConstraints gbc_lblMeshNodeName = new GridBagConstraints();
		gbc_lblMeshNodeName.anchor = GridBagConstraints.EAST;
		gbc_lblMeshNodeName.insets = new Insets(0, 0, 5, 5);
		gbc_lblMeshNodeName.gridx = 0;
		gbc_lblMeshNodeName.gridy = 0;
		meshPanel.add(lblMeshNodeName, gbc_lblMeshNodeName);
		
		nodeNameField = new JTextField();
		nodeNameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = nodeNameField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).nameNew = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_nodeNameField = new GridBagConstraints();
		gbc_nodeNameField.gridwidth = 4;
		gbc_nodeNameField.insets = new Insets(0, 0, 5, 5);
		gbc_nodeNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nodeNameField.gridx = 1;
		gbc_nodeNameField.gridy = 0;
		meshPanel.add(nodeNameField, gbc_nodeNameField);
		nodeNameField.setColumns(10);
		
		JLabel lblPosition = new JLabel("Position");
		GridBagConstraints gbc_lblPosition = new GridBagConstraints();
		gbc_lblPosition.anchor = GridBagConstraints.EAST;
		gbc_lblPosition.insets = new Insets(0, 0, 5, 5);
		gbc_lblPosition.gridx = 0;
		gbc_lblPosition.gridy = 1;
		meshPanel.add(lblPosition, gbc_lblPosition);
		
		posXField = new JTextField();
		posXField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = posXField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).positionNewX = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_posXField = new GridBagConstraints();
		gbc_posXField.insets = new Insets(0, 0, 5, 5);
		gbc_posXField.fill = GridBagConstraints.HORIZONTAL;
		gbc_posXField.gridx = 1;
		gbc_posXField.gridy = 1;
		meshPanel.add(posXField, gbc_posXField);
		posXField.setColumns(10);
		
		posYField = new JTextField();
		posYField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = posYField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).positionNewY = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_posYField = new GridBagConstraints();
		gbc_posYField.insets = new Insets(0, 0, 5, 5);
		gbc_posYField.fill = GridBagConstraints.HORIZONTAL;
		gbc_posYField.gridx = 2;
		gbc_posYField.gridy = 1;
		meshPanel.add(posYField, gbc_posYField);
		posYField.setColumns(10);
		
		posZField = new JTextField();
		posZField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = posZField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).positionNewZ = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_posZField = new GridBagConstraints();
		gbc_posZField.insets = new Insets(0, 0, 5, 5);
		gbc_posZField.fill = GridBagConstraints.HORIZONTAL;
		gbc_posZField.gridx = 3;
		gbc_posZField.gridy = 1;
		meshPanel.add(posZField, gbc_posZField);
		posZField.setColumns(10);
		
		JLabel lblScale = new JLabel("Scale");
		GridBagConstraints gbc_lblScale = new GridBagConstraints();
		gbc_lblScale.anchor = GridBagConstraints.EAST;
		gbc_lblScale.insets = new Insets(0, 0, 5, 5);
		gbc_lblScale.gridx = 0;
		gbc_lblScale.gridy = 2;
		meshPanel.add(lblScale, gbc_lblScale);
		
		scaleXField = new JTextField();
		scaleXField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = scaleXField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).scaleNewX = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_scaleXField = new GridBagConstraints();
		gbc_scaleXField.insets = new Insets(0, 0, 5, 5);
		gbc_scaleXField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleXField.gridx = 1;
		gbc_scaleXField.gridy = 2;
		meshPanel.add(scaleXField, gbc_scaleXField);
		scaleXField.setColumns(10);
		
		scaleYField = new JTextField();
		scaleYField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = scaleYField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).scaleNewY = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_scaleYField = new GridBagConstraints();
		gbc_scaleYField.insets = new Insets(0, 0, 5, 5);
		gbc_scaleYField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleYField.gridx = 2;
		gbc_scaleYField.gridy = 2;
		meshPanel.add(scaleYField, gbc_scaleYField);
		scaleYField.setColumns(10);
		
		scaleZField = new JTextField();
		scaleZField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = scaleZField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).scaleNewZ = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_scaleZField = new GridBagConstraints();
		gbc_scaleZField.insets = new Insets(0, 0, 5, 5);
		gbc_scaleZField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleZField.gridx = 3;
		gbc_scaleZField.gridy = 2;
		meshPanel.add(scaleZField, gbc_scaleZField);
		scaleZField.setColumns(10);
		
		JLabel lblRotation = new JLabel("Rotation");
		GridBagConstraints gbc_lblRotation = new GridBagConstraints();
		gbc_lblRotation.anchor = GridBagConstraints.EAST;
		gbc_lblRotation.insets = new Insets(0, 0, 5, 5);
		gbc_lblRotation.gridx = 0;
		gbc_lblRotation.gridy = 3;
		meshPanel.add(lblRotation, gbc_lblRotation);
		
		rotXField = new JTextField();
		rotXField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = rotXField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).rotationNewX = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_rotXField = new GridBagConstraints();
		gbc_rotXField.insets = new Insets(0, 0, 5, 5);
		gbc_rotXField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rotXField.gridx = 1;
		gbc_rotXField.gridy = 3;
		meshPanel.add(rotXField, gbc_rotXField);
		rotXField.setColumns(10);
		
		rotYField = new JTextField();
		rotYField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = rotYField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).rotationNewY = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_rotYField = new GridBagConstraints();
		gbc_rotYField.insets = new Insets(0, 0, 5, 5);
		gbc_rotYField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rotYField.gridx = 2;
		gbc_rotYField.gridy = 3;
		meshPanel.add(rotYField, gbc_rotYField);
		rotYField.setColumns(10);
		
		rotZField = new JTextField();
		rotZField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = rotZField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).rotationNewZ = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_rotZField = new GridBagConstraints();
		gbc_rotZField.insets = new Insets(0, 0, 5, 5);
		gbc_rotZField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rotZField.gridx = 3;
		gbc_rotZField.gridy = 3;
		meshPanel.add(rotZField, gbc_rotZField);
		rotZField.setColumns(10);
		
		rotWField = new JTextField();
		rotWField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				ModelsDetailPanel.this.changed = true;
				final JTextField input = rotWField;
				if(input.getText().trim().length() > 0) {
					model.scene.sceneNodes.get(0).rotationNewW = input.getText().trim();
				}
			}
		});
		GridBagConstraints gbc_rotWField = new GridBagConstraints();
		gbc_rotWField.insets = new Insets(0, 0, 5, 5);
		gbc_rotWField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rotWField.gridx = 4;
		gbc_rotWField.gridy = 3;
		meshPanel.add(rotWField, gbc_rotWField);
		rotWField.setColumns(10);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 6;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 4;
		meshPanel.add(separator, gbc_separator);
		
		JLabel lblEntityName = new JLabel("Entity Name");
		GridBagConstraints gbc_lblEntityName = new GridBagConstraints();
		gbc_lblEntityName.anchor = GridBagConstraints.EAST;
		gbc_lblEntityName.insets = new Insets(0, 0, 5, 5);
		gbc_lblEntityName.gridx = 0;
		gbc_lblEntityName.gridy = 6;
		meshPanel.add(lblEntityName, gbc_lblEntityName);
		
		entityNameField = new JTextField();
		entityNameField.setEditable(false);
		GridBagConstraints gbc_entityNameField = new GridBagConstraints();
		gbc_entityNameField.gridwidth = 4;
		gbc_entityNameField.insets = new Insets(0, 0, 5, 5);
		gbc_entityNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_entityNameField.gridx = 1;
		gbc_entityNameField.gridy = 6;
		meshPanel.add(entityNameField, gbc_entityNameField);
		entityNameField.setColumns(10);
		
		JLabel lblName_1 = new JLabel("Mesh File");
		GridBagConstraints gbc_lblName_1 = new GridBagConstraints();
		gbc_lblName_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblName_1.anchor = GridBagConstraints.EAST;
		gbc_lblName_1.gridx = 0;
		gbc_lblName_1.gridy = 7;
		meshPanel.add(lblName_1, gbc_lblName_1);
		
		meshFileField = new JTextField();
		meshFileField.setEditable(false);
		GridBagConstraints gbc_meshFileField = new GridBagConstraints();
		gbc_meshFileField.gridwidth = 4;
		gbc_meshFileField.insets = new Insets(0, 0, 5, 5);
		gbc_meshFileField.fill = GridBagConstraints.HORIZONTAL;
		gbc_meshFileField.gridx = 1;
		gbc_meshFileField.gridy = 7;
		meshPanel.add(meshFileField, gbc_meshFileField);
		meshFileField.setColumns(10);
		
		JButton btnMeshFileBrowse = new JButton("Browse");
		btnMeshFileBrowse.setEnabled(false);
		GridBagConstraints gbc_btnMeshFileBrowse = new GridBagConstraints();
		gbc_btnMeshFileBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_btnMeshFileBrowse.gridx = 5;
		gbc_btnMeshFileBrowse.gridy = 7;
		meshPanel.add(btnMeshFileBrowse, gbc_btnMeshFileBrowse);
		
		JLabel lblMaterialName = new JLabel("Material Name");
		GridBagConstraints gbc_lblMaterialName = new GridBagConstraints();
		gbc_lblMaterialName.anchor = GridBagConstraints.EAST;
		gbc_lblMaterialName.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaterialName.gridx = 0;
		gbc_lblMaterialName.gridy = 8;
		meshPanel.add(lblMaterialName, gbc_lblMaterialName);
		
		materialNameField = new JTextField();
		materialNameField.setEditable(false);
		GridBagConstraints gbc_materialNameField = new GridBagConstraints();
		gbc_materialNameField.gridwidth = 4;
		gbc_materialNameField.insets = new Insets(0, 0, 5, 5);
		gbc_materialNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_materialNameField.gridx = 1;
		gbc_materialNameField.gridy = 8;
		meshPanel.add(materialNameField, gbc_materialNameField);
		materialNameField.setColumns(10);
		
		JLabel lblMaterialFile = new JLabel("Material File");
		GridBagConstraints gbc_lblMaterialFile = new GridBagConstraints();
		gbc_lblMaterialFile.anchor = GridBagConstraints.EAST;
		gbc_lblMaterialFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaterialFile.gridx = 0;
		gbc_lblMaterialFile.gridy = 9;
		meshPanel.add(lblMaterialFile, gbc_lblMaterialFile);
		
		materialFileName = new JTextField();
		materialFileName.setEditable(false);
		GridBagConstraints gbc_materialFileName = new GridBagConstraints();
		gbc_materialFileName.gridwidth = 4;
		gbc_materialFileName.insets = new Insets(0, 0, 5, 5);
		gbc_materialFileName.fill = GridBagConstraints.HORIZONTAL;
		gbc_materialFileName.gridx = 1;
		gbc_materialFileName.gridy = 9;
		meshPanel.add(materialFileName, gbc_materialFileName);
		materialFileName.setColumns(10);
		
		JButton buttonMaterialFileBrowse = new JButton("Browse");
		buttonMaterialFileBrowse.setEnabled(false);
		GridBagConstraints gbc_buttonMaterialFileBrowse = new GridBagConstraints();
		gbc_buttonMaterialFileBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_buttonMaterialFileBrowse.gridx = 5;
		gbc_buttonMaterialFileBrowse.gridy = 9;
		meshPanel.add(buttonMaterialFileBrowse, gbc_buttonMaterialFileBrowse);
		
		JPanel texture = new JPanel();
		GridBagConstraints gbc_texture = new GridBagConstraints();
		gbc_texture.fill = GridBagConstraints.BOTH;
		gbc_texture.gridwidth = 6;
		gbc_texture.gridx = 0;
		gbc_texture.gridy = 10;
		meshPanel.add(texture, gbc_texture);
		texture.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Material", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagLayout gbl_texture = new GridBagLayout();
		gbl_texture.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_texture.rowHeights = new int[]{0, 0, 0, 0};
		gbl_texture.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_texture.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		texture.setLayout(gbl_texture);
		
		JLabel lblDiffuse = new JLabel("Diffuse Texture");
		GridBagConstraints gbc_lblDiffuse = new GridBagConstraints();
		gbc_lblDiffuse.insets = new Insets(0, 0, 5, 5);
		gbc_lblDiffuse.anchor = GridBagConstraints.EAST;
		gbc_lblDiffuse.gridx = 0;
		gbc_lblDiffuse.gridy = 0;
		texture.add(lblDiffuse, gbc_lblDiffuse);
		
		diffuseField = new JTextField();
		diffuseField.setEditable(false);
		GridBagConstraints gbc_diffuseField = new GridBagConstraints();
		gbc_diffuseField.insets = new Insets(0, 0, 5, 5);
		gbc_diffuseField.fill = GridBagConstraints.HORIZONTAL;
		gbc_diffuseField.gridx = 1;
		gbc_diffuseField.gridy = 0;
		texture.add(diffuseField, gbc_diffuseField);
		diffuseField.setColumns(10);
		
		JButton diffBrowse = new JButton("Browse");
		diffBrowse.addActionListener(arg0 -> {
			browseForImage(diffuseField, null);
			model.scene.sceneNodes.get(0).entity.material.diffuseTextureNew = diffuseField.getText();
		});
		GridBagConstraints gbc_diffBrowse = new GridBagConstraints();
		gbc_diffBrowse.insets = new Insets(0, 0, 5, 5);
		gbc_diffBrowse.gridx = 2;
		gbc_diffBrowse.gridy = 0;
		texture.add(diffBrowse, gbc_diffBrowse);
		
		JButton diffRemove = new JButton("Remove");
		diffRemove.addActionListener(e -> {
			model.scene.sceneNodes.get(0).entity.material.diffuseTextureNew = "";
			diffuseField.setText("<none>");
		});
		GridBagConstraints gbc_diffRemove = new GridBagConstraints();
		gbc_diffRemove.insets = new Insets(0, 0, 5, 0);
		gbc_diffRemove.gridx = 3;
		gbc_diffRemove.gridy = 0;
		texture.add(diffRemove, gbc_diffRemove);
		
		JLabel lblNormal = new JLabel("Normal Texture");
		GridBagConstraints gbc_lblNormal = new GridBagConstraints();
		gbc_lblNormal.anchor = GridBagConstraints.EAST;
		gbc_lblNormal.insets = new Insets(0, 0, 5, 5);
		gbc_lblNormal.gridx = 0;
		gbc_lblNormal.gridy = 1;
		texture.add(lblNormal, gbc_lblNormal);
		
		normalField = new JTextField();
		normalField.setEditable(false);
		GridBagConstraints gbc_normalField = new GridBagConstraints();
		gbc_normalField.insets = new Insets(0, 0, 5, 5);
		gbc_normalField.fill = GridBagConstraints.HORIZONTAL;
		gbc_normalField.gridx = 1;
		gbc_normalField.gridy = 1;
		texture.add(normalField, gbc_normalField);
		normalField.setColumns(10);
		
		JButton normalBrowse = new JButton("Browse");
		normalBrowse.addActionListener(e -> {
			browseForImage(normalField, "_nrm");
			model.scene.sceneNodes.get(0).entity.material.normalTextureNew = normalField.getText();
		});
		GridBagConstraints gbc_normalBrowse = new GridBagConstraints();
		gbc_normalBrowse.insets = new Insets(0, 0, 5, 5);
		gbc_normalBrowse.gridx = 2;
		gbc_normalBrowse.gridy = 1;
		texture.add(normalBrowse, gbc_normalBrowse);
		
		JButton normalRemove = new JButton("Remove");
		normalRemove.addActionListener(e -> {
			model.scene.sceneNodes.get(0).entity.material.normalTextureNew = "";
			normalField.setText("<none>");
		});
		GridBagConstraints gbc_normalRemove = new GridBagConstraints();
		gbc_normalRemove.insets = new Insets(0, 0, 5, 0);
		gbc_normalRemove.gridx = 3;
		gbc_normalRemove.gridy = 1;
		texture.add(normalRemove, gbc_normalRemove);
		
		JLabel lblEmission = new JLabel("Emission Texture");
		GridBagConstraints gbc_lblEmission = new GridBagConstraints();
		gbc_lblEmission.anchor = GridBagConstraints.EAST;
		gbc_lblEmission.insets = new Insets(0, 0, 0, 5);
		gbc_lblEmission.gridx = 0;
		gbc_lblEmission.gridy = 2;
		texture.add(lblEmission, gbc_lblEmission);
		
		emissionField = new JTextField();
		emissionField.setEditable(false);
		GridBagConstraints gbc_emissionField = new GridBagConstraints();
		gbc_emissionField.insets = new Insets(0, 0, 0, 5);
		gbc_emissionField.fill = GridBagConstraints.HORIZONTAL;
		gbc_emissionField.gridx = 1;
		gbc_emissionField.gridy = 2;
		texture.add(emissionField, gbc_emissionField);
		emissionField.setColumns(10);
		
		JButton emissionBrowse = new JButton("Browse");
		emissionBrowse.addActionListener(e -> {
			browseForImage(emissionField, "_em");
			model.scene.sceneNodes.get(0).entity.material.emissionTextureNew = emissionField.getText();
		});
		GridBagConstraints gbc_emissionBrowse = new GridBagConstraints();
		gbc_emissionBrowse.insets = new Insets(0, 0, 0, 5);
		gbc_emissionBrowse.gridx = 2;
		gbc_emissionBrowse.gridy = 2;
		texture.add(emissionBrowse, gbc_emissionBrowse);
		
		JButton emissionRemove = new JButton("Remove");
		emissionRemove.addActionListener(e -> {
			model.scene.sceneNodes.get(0).entity.material.emissionTextureNew = "";
			emissionField.setText("<none>");
		});
		GridBagConstraints gbc_emissionRemove = new GridBagConstraints();
		gbc_emissionRemove.gridx = 3;
		gbc_emissionRemove.gridy = 2;
		texture.add(emissionRemove, gbc_emissionRemove);
	}
	private boolean browseForImage(JTextField textField, String reqEnding) {
		File rPath = new File("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator+model.relpath+File.separator);
		JFileChooser fileChooser = new JFileChooser("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator+model.relpath+File.separator);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "ImageFile";
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".jpg") || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
			}
		});
		int option = fileChooser.showOpenDialog(ModelsDetailPanel.this);
		if(option == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			if(reqEnding != null && reqEnding.length() > 0 && !file.getName().substring(0, file.getName().lastIndexOf(".")).toLowerCase(Locale.ENGLISH).endsWith(reqEnding.toLowerCase(Locale.ENGLISH))) {
				JOptionPane.showMessageDialog(ModelsDetailPanel.this,
					    "Selected file is not suitable. file name must end with '"+reqEnding+"'",
					    "Copy Error",
					    JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!file.getParentFile().equals(rPath)) {
				File supposedFile = new File("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator+model.relpath+File.separator+file.getName());
				if(!supposedFile.exists()) {
					Object[] options = {"Yes",
		                    "Cancel"};
					int n = JOptionPane.showOptionDialog(ModelsDetailPanel.this,
					    "File "+file.getName()+" doesn't yet exist in the scene folder. Copy?",
					    "File Copy",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[1]);
					
					if(n == 0) {
						try {
							FileUtil.copyFile(file, supposedFile);
							textField.setText(file.getName());
							return true;
						} catch (IOException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(ModelsDetailPanel.this,
								    "Something went wrong when copying the file:\n\n"+e.getClass().getSimpleName()+": "+e.getMessage(),
								    "Copy Error",
								    JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}else {
						return false;
					}
				}else {
					Object[] options = {"Use file in scene folder",
							"Copy with different name",
                    "Cancel"};
					int n = JOptionPane.showOptionDialog(ModelsDetailPanel.this,
						    "File "+file.getName()+" already exists in the scene folder,\n"
						    		+ "but you selected another file with the same name.\n"
						    		+ "You cannot overwrite the existing file since it may be in use.\n"
						    		+ "You can however use the already existing file or copy a renamed version",
						    "File Copy",
						    JOptionPane.YES_NO_CANCEL_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[2]);
						
						if(n == 0) {
							textField.setText(file.getName());
							return true;
						}else if(n == 1) {
							String s = "";
							
							
							String ending = file.getName().toLowerCase(Locale.ENGLISH).substring(file.getName().lastIndexOf("."));
							while(s.length() == 0 || !s.equals(file.getName()) || 
									(new File("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator+model.relpath+File.separator+s).exists())) {
								s = (String)JOptionPane.showInputDialog(
					                  ModelsDetailPanel.this,
					                    "Please enter a name for the duplicate (must be unique)\nNote, this will create a new scene file when saved.",
					                    "Duplicate",
					                    JOptionPane.PLAIN_MESSAGE,
					                    null,
					                    null,
					                    file.getName());
								if(s.length() > 0 && (new File("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator+model.relpath+File.separator+s).exists())){
									JOptionPane.showMessageDialog(ModelsDetailPanel.this,
										    "File already exists, please chose a different name",
										    "Copy Error",
										    JOptionPane.ERROR_MESSAGE);
									s = "";
								}
								if(!s.endsWith(ending)) {
									JOptionPane.showMessageDialog(ModelsDetailPanel.this,
										    "File name must have equal format (ending with '"+ending+"')",
										    "Copy Error",
										    JOptionPane.ERROR_MESSAGE);
									s = "";
								}
							}
							
							try {
								File mFile = new File("."+File.separator+DataUtil.dataPath+model.cat.path+File.separator+model.relpath+File.separator+s);
								
								FileUtil.copyFile(file, mFile);
								textField.setText(mFile.getName());
								return true;
							} catch (IOException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(ModelsDetailPanel.this,
									    "Something went wrong when copying the file:\n\n"+e.getClass().getSimpleName()+": "+e.getMessage(),
									    "Copy Error",
									    JOptionPane.ERROR_MESSAGE);
								return false;
							}
						}else {
							return false;
						}
				}
				
			}else {
				textField.setText(file.getName());
				return true;
			}
		}
		return false;
	}
	
	
	public void fill(Model model) {
		this.model = model;
		setTextureFild(nameField, this.model.name, this.model.nameNew);
		relPathField.setText(this.model.duplicated ? "<created on save>" : this.model.relpath);
		fileNameField.setText(this.model.duplicated ? "<created on save>" : this.model.filename);
		
		fill(this.model.scene, this.model.duplicated);
		
	}
	private void fill(SceneFile scene, boolean duplicated) {
	
		fill(scene.sceneNodes.get(0), duplicated);
	}
	private void fill(SceneNode node, boolean duplicated) {
		setTextureFild(nodeNameField, node.name, node.nameNew);
		
		setTextureFild(posXField, String.valueOf(node.position.x), node.positionNewX);
		setTextureFild(posYField, String.valueOf(node.position.y), node.positionNewY);
		setTextureFild(posZField, String.valueOf(node.position.z), node.positionNewZ);
		
		
		setTextureFild(scaleXField, String.valueOf(node.scale.x), node.scaleNewX);
		setTextureFild(scaleYField, String.valueOf(node.scale.y), node.scaleNewY);
		setTextureFild(scaleZField, String.valueOf(node.scale.z), node.scaleNewZ);
		
		setTextureFild(rotXField, String.valueOf(node.rotation.x), node.rotationNewX);
		setTextureFild(rotYField, String.valueOf(node.rotation.y), node.rotationNewY);
		setTextureFild(rotZField, String.valueOf(node.rotation.z), node.rotationNewZ);
		setTextureFild(rotWField, String.valueOf(node.rotation.w), node.rotationNewW);
		
		fill(node.entity, duplicated);
	}
	private void setTextureFild(JTextField field, String cur, String nw) {
		if(nw != null) {
			field.setText(nw.trim().length() > 0 ? nw : "<none>");
		}else {
			field.setText(cur != null  ? cur : "<none>");
		}
	}
	private void fill(MeshEntity e, boolean duplicated) {
		entityNameField.setText(e.name);
		meshFileField.setText(e.meshFile);
		fill(e.material, duplicated);
		
		
		
		
		
	}
	private void fill(MaterialEntity m, boolean duplicated) {
		materialNameField.setText(m.materialName);
		materialFileName.setText(duplicated ? "<created on save>" : m.fileName);	
		
		setTextureFild(diffuseField, m.diffuseTexture, m.diffuseTextureNew);
		setTextureFild(normalField, m.normalTexture, m.normalTextureNew);
		setTextureFild(emissionField, m.emissionTexture, m.emissionTextureNew);
	}
	
	
	public void refill() {
		fill(model);
	}
}
