package org.schema.schine.sound.controller.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.FiredAudioEvent;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.assignment.AudioAssignmentID.AudioAssignmentType;
import org.schema.schine.sound.controller.mixer.AudioMixer;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AudioEventDetailPanel extends JPanel implements Runnable {

	
	FiredAudioEvent selected;
	private JTextField textFieldPrimaryRangeMult;
	private JTextField textFieldSecondaryRangeMult;
	private JLabel lblIdval;
	private JLabel lblTimeval;
	private JLabel lblCallval;
	private JLabel lblTagsval;
	private JLabel lblRemoteval;
	private JLabel lblAssignmenttypeval;
	private JLabel lblPrimassetval;
	private JLabel lblSecassetval;
	private JLabel lblRemoteidval;
	public JLabel lblTransval;
	public JLabel lblSubidval;
	public JLabel lblPositionval;
	public JLabel lblRangeval;
	
	private List<JLabel> detailsLabels = new ObjectArrayList<>();
	private JLabel lblPrimassetval_1;
	private JLabel lblSecassetval_1;
	private JButton btnAssignPrimaryAsset;
	private JButton btnAssignSecondaryAsset;
	private JComboBox<AudioMixer> mixerComboBox;
	/**
	 * Create the panel.
	 */
	public AudioEventDetailPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{404};
		gridBagLayout.rowHeights = new int[]{10, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel detailsPanel = new JPanel();
		detailsPanel.setBorder(new TitledBorder(null, "Details", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_detailsPanel = new GridBagConstraints();
		gbc_detailsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_detailsPanel.fill = GridBagConstraints.BOTH;
		gbc_detailsPanel.weightx = 1.0;
		gbc_detailsPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_detailsPanel.gridx = 0;
		gbc_detailsPanel.gridy = 0;
		add(detailsPanel, gbc_detailsPanel);
		GridBagLayout gbl_detailsPanel = new GridBagLayout();
		gbl_detailsPanel.columnWidths = new int[]{0, 0};
		gbl_detailsPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_detailsPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_detailsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		detailsPanel.setLayout(gbl_detailsPanel);
		
		JLabel lblId = new JLabel("ID");
		lblId.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.WEST;
		gbc_lblId.insets = new Insets(0, 5, 5, 5);
		gbc_lblId.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 1;
		detailsPanel.add(lblId, gbc_lblId);
		
		detailsLabels.add(lblIdval = new JLabel("IDVal"));
		lblIdval.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblIdval = new GridBagConstraints();
		gbc_lblIdval.insets = new Insets(0, 5, 5, 0);
		gbc_lblIdval.gridx = 1;
		gbc_lblIdval.gridy = 1;
		gbc_lblIdval.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblIdval, gbc_lblIdval);
		
		JLabel lblTime = new JLabel("Time");
		GridBagConstraints gbc_lblTime = new GridBagConstraints();
		gbc_lblTime.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblTime.insets = new Insets(0, 5, 5, 5);
		gbc_lblTime.gridx = 0;
		gbc_lblTime.gridy = 2;
		detailsPanel.add(lblTime, gbc_lblTime);
		
		detailsLabels.add(lblTimeval = new JLabel("TimeVal"));
		GridBagConstraints gbc_lblTimeval = new GridBagConstraints();
		gbc_lblTimeval.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblTimeval.insets = new Insets(0, 5, 5, 0);
		gbc_lblTimeval.gridx = 1;
		gbc_lblTimeval.gridy = 2;
		detailsPanel.add(lblTimeval, gbc_lblTimeval);
		
		JLabel lblCall = new JLabel("Call");
		lblCall.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblCall = new GridBagConstraints();
		gbc_lblCall.insets = new Insets(0, 5, 5, 5);
		gbc_lblCall.gridx = 0;
		gbc_lblCall.gridy = 3;
		gbc_lblCall.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblCall, gbc_lblCall);
		
		detailsLabels.add(lblCallval = new JLabel("CallVal"));
		lblCallval.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblCallval = new GridBagConstraints();
		gbc_lblCallval.insets = new Insets(0, 5, 5, 0);
		gbc_lblCallval.gridx = 1;
		gbc_lblCallval.gridy = 3;
		gbc_lblCallval.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblCallval, gbc_lblCallval);
		
		JLabel lblTags = new JLabel("Tags");
		lblTags.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTags = new GridBagConstraints();
		gbc_lblTags.insets = new Insets(0, 5, 5, 5);
		gbc_lblTags.gridx = 0;
		gbc_lblTags.gridy = 4;
		gbc_lblTags.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblTags, gbc_lblTags);
		
		detailsLabels.add(lblTagsval = new JLabel("TagsVal"));
		lblTagsval.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTagsval = new GridBagConstraints();
		gbc_lblTagsval.insets = new Insets(0, 5, 5, 0);
		gbc_lblTagsval.gridx = 1;
		gbc_lblTagsval.gridy = 4;
		gbc_lblTagsval.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblTagsval, gbc_lblTagsval);
		
		JLabel lblRemote = new JLabel("Remote");
		lblRemote.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblRemote = new GridBagConstraints();
		gbc_lblRemote.insets = new Insets(0, 5, 5, 5);
		gbc_lblRemote.gridx = 0;
		gbc_lblRemote.gridy = 5;
		gbc_lblRemote.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblRemote, gbc_lblRemote);
		
		detailsLabels.add(lblRemoteval = new JLabel("RemoteVal"));
		lblRemoteval.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblRemoteval = new GridBagConstraints();
		gbc_lblRemoteval.insets = new Insets(0, 5, 5, 0);
		gbc_lblRemoteval.gridx = 1;
		gbc_lblRemoteval.gridy = 5;
		gbc_lblRemoteval.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblRemoteval, gbc_lblRemoteval);
		
		JLabel lblAssignmenttype = new JLabel("AssignmentType");
		lblAssignmenttype.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblAssignmenttype = new GridBagConstraints();
		gbc_lblAssignmenttype.insets = new Insets(0, 5, 5, 5);
		gbc_lblAssignmenttype.gridx = 0;
		gbc_lblAssignmenttype.gridy = 6;
		gbc_lblAssignmenttype.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblAssignmenttype, gbc_lblAssignmenttype);
		
		detailsLabels.add(lblAssignmenttypeval = new JLabel("AssignmentTypeVal"));
		lblAssignmenttypeval.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblAssignmenttypeval = new GridBagConstraints();
		gbc_lblAssignmenttypeval.insets = new Insets(0, 5, 5, 0);
		gbc_lblAssignmenttypeval.gridx = 1;
		gbc_lblAssignmenttypeval.gridy = 6;
		gbc_lblAssignmenttypeval.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblAssignmenttypeval, gbc_lblAssignmenttypeval);
		
		JLabel lblPrimAsset = new JLabel("Prim. Asset");
		GridBagConstraints gbc_lblPrimAsset = new GridBagConstraints();
		gbc_lblPrimAsset.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblPrimAsset.insets = new Insets(0, 5, 5, 5);
		gbc_lblPrimAsset.gridx = 0;
		gbc_lblPrimAsset.gridy = 7;
		detailsPanel.add(lblPrimAsset, gbc_lblPrimAsset);
		
		detailsLabels.add(lblPrimassetval = new JLabel("PrimAssetVal"));
		GridBagConstraints gbc_lblPrimassetval = new GridBagConstraints();
		gbc_lblPrimassetval.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblPrimassetval.insets = new Insets(0, 5, 5, 0);
		gbc_lblPrimassetval.gridx = 1;
		gbc_lblPrimassetval.gridy = 7;
		detailsPanel.add(lblPrimassetval, gbc_lblPrimassetval);
		
		JLabel lblSecAsset = new JLabel("Sec. Asset");
		GridBagConstraints gbc_lblSecAsset = new GridBagConstraints();
		gbc_lblSecAsset.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblSecAsset.insets = new Insets(0, 5, 5, 5);
		gbc_lblSecAsset.gridx = 0;
		gbc_lblSecAsset.gridy = 8;
		detailsPanel.add(lblSecAsset, gbc_lblSecAsset);
		
		detailsLabels.add(lblSecassetval = new JLabel("SecAssetVal"));
		GridBagConstraints gbc_lblSecassetval = new GridBagConstraints();
		gbc_lblSecassetval.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblSecassetval.insets = new Insets(0, 5, 5, 0);
		gbc_lblSecassetval.gridx = 1;
		gbc_lblSecassetval.gridy = 8;
		detailsPanel.add(lblSecassetval, gbc_lblSecassetval);
		
		JLabel lblRemoteid = new JLabel("RemoteID");
		lblRemoteid.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblRemoteid = new GridBagConstraints();
		gbc_lblRemoteid.insets = new Insets(0, 5, 5, 5);
		gbc_lblRemoteid.gridx = 0;
		gbc_lblRemoteid.gridy = 9;
		gbc_lblRemoteid.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblRemoteid, gbc_lblRemoteid);
		
		detailsLabels.add(lblRemoteidval = new JLabel("RemoteIDVal"));
		lblRemoteidval.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblRemoteidval = new GridBagConstraints();
		gbc_lblRemoteidval.insets = new Insets(0, 5, 5, 0);
		gbc_lblRemoteidval.gridx = 1;
		gbc_lblRemoteidval.gridy = 9;
		gbc_lblRemoteidval.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblRemoteidval, gbc_lblRemoteidval);
		
		JLabel lblArgument = new JLabel("Argument");
		lblArgument.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblArgument = new GridBagConstraints();
		gbc_lblArgument.insets = new Insets(0, 5, 0, 5);
		gbc_lblArgument.gridx = 0;
		gbc_lblArgument.gridy = 10;
		gbc_lblArgument.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(lblArgument, gbc_lblArgument);
		
		JPanel argumentPanel = new JPanel();
		argumentPanel.setBorder(new TitledBorder(null, "Argument", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_argumentPanel = new GridBagConstraints();
		gbc_argumentPanel.insets = new Insets(0, 5, 0, 0);
		gbc_argumentPanel.fill = GridBagConstraints.BOTH;
		gbc_argumentPanel.gridx = 1;
		gbc_argumentPanel.gridy = 10;
		gbc_argumentPanel.anchor = GridBagConstraints.NORTHWEST;
		detailsPanel.add(argumentPanel, gbc_argumentPanel);
		GridBagLayout gbl_argumentPanel = new GridBagLayout();
		gbl_argumentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_argumentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_argumentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_argumentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		argumentPanel.setLayout(gbl_argumentPanel);
		
		JLabel lblTransformable = new JLabel("Transformable");
		GridBagConstraints gbc_lblTransformable = new GridBagConstraints();
		gbc_lblTransformable.insets = new Insets(0, 0, 5, 5);
		gbc_lblTransformable.gridx = 0;
		gbc_lblTransformable.gridy = 0;
		argumentPanel.add(lblTransformable, gbc_lblTransformable);
		
		detailsLabels.add(lblTransval = new JLabel("TransVal"));
		GridBagConstraints gbc_lblTransval = new GridBagConstraints();
		gbc_lblTransval.insets = new Insets(0, 0, 5, 0);
		gbc_lblTransval.gridx = 1;
		gbc_lblTransval.gridy = 0;
		argumentPanel.add(lblTransval, gbc_lblTransval);
		
		JLabel lblSubid = new JLabel("SubId");
		GridBagConstraints gbc_lblSubid = new GridBagConstraints();
		gbc_lblSubid.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubid.gridx = 0;
		gbc_lblSubid.gridy = 1;
		argumentPanel.add(lblSubid, gbc_lblSubid);
		
		detailsLabels.add(lblSubidval = new JLabel("SubIdVal"));
		GridBagConstraints gbc_lblSubidval = new GridBagConstraints();
		gbc_lblSubidval.insets = new Insets(0, 0, 5, 0);
		gbc_lblSubidval.gridx = 1;
		gbc_lblSubidval.gridy = 1;
		argumentPanel.add(lblSubidval, gbc_lblSubidval);
		
		JLabel lblPosition = new JLabel("Position");
		GridBagConstraints gbc_lblPosition = new GridBagConstraints();
		gbc_lblPosition.insets = new Insets(0, 0, 5, 5);
		gbc_lblPosition.gridx = 0;
		gbc_lblPosition.gridy = 2;
		argumentPanel.add(lblPosition, gbc_lblPosition);
		
		detailsLabels.add(lblPositionval = new JLabel("PositionVal"));
		GridBagConstraints gbc_lblPositionval = new GridBagConstraints();
		gbc_lblPositionval.insets = new Insets(0, 0, 5, 0);
		gbc_lblPositionval.gridx = 1;
		gbc_lblPositionval.gridy = 2;
		argumentPanel.add(lblPositionval, gbc_lblPositionval);
		
		JLabel lblRange = new JLabel("Range");
		GridBagConstraints gbc_lblRange = new GridBagConstraints();
		gbc_lblRange.insets = new Insets(0, 0, 0, 5);
		gbc_lblRange.gridx = 0;
		gbc_lblRange.gridy = 3;
		argumentPanel.add(lblRange, gbc_lblRange);
		
		detailsLabels.add(lblRangeval = new JLabel("RangeVal"));
		GridBagConstraints gbc_lblRangeval = new GridBagConstraints();
		gbc_lblRangeval.gridx = 1;
		gbc_lblRangeval.gridy = 3;
		argumentPanel.add(lblRangeval, gbc_lblRangeval);
		
		JPanel settingsPanel = new JPanel();
		settingsPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_settingsPanel = new GridBagConstraints();
		gbc_settingsPanel.weighty = 1.0;
		gbc_settingsPanel.weightx = 1.0;
		gbc_settingsPanel.fill = GridBagConstraints.BOTH;
		gbc_settingsPanel.gridx = 0;
		gbc_settingsPanel.gridy = 1;
		add(settingsPanel, gbc_settingsPanel);
		GridBagLayout gbl_settingsPanel = new GridBagLayout();
		gbl_settingsPanel.columnWidths = new int[]{0};
		gbl_settingsPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_settingsPanel.columnWeights = new double[]{1.0};
		gbl_settingsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
		settingsPanel.setLayout(gbl_settingsPanel);
		
		JPanel assignmentSwitchPanel = new JPanel();
		assignmentSwitchPanel.setBorder(new TitledBorder(null, "Switch Assignment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_assignmentSwitchPanel = new GridBagConstraints();
		gbc_assignmentSwitchPanel.insets = new Insets(0, 0, 5, 0);
		gbc_assignmentSwitchPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_assignmentSwitchPanel.gridx = 0;
		gbc_assignmentSwitchPanel.gridy = 0;
		settingsPanel.add(assignmentSwitchPanel, gbc_assignmentSwitchPanel);
		GridBagLayout gbl_assignmentSwitchPanel = new GridBagLayout();
		gbl_assignmentSwitchPanel.columnWidths = new int[]{0,0,0};
		gbl_assignmentSwitchPanel.rowHeights = new int[]{0};
		gbl_assignmentSwitchPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_assignmentSwitchPanel.rowWeights = new double[]{0.0};
		assignmentSwitchPanel.setLayout(gbl_assignmentSwitchPanel);
		
		JButton btnSwitchToTag = new JButton("To Tags");
		btnSwitchToTag.addActionListener(e -> {
			if(selected != null) {
				selected.entry.assignmnetID.type = AudioAssignmentType.TAG;
				selected.entry.assignmnetID.resolveAssignment(selected.entry.id, selected.entry.tags, AudioController.instance);
			}
		});
		GridBagConstraints gbc_btnSwitchToTag = new GridBagConstraints();
		gbc_btnSwitchToTag.insets = new Insets(0, 0, 0, 5);
		gbc_btnSwitchToTag.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnSwitchToTag.gridx = 0;
		gbc_btnSwitchToTag.gridy = 0;
		assignmentSwitchPanel.add(btnSwitchToTag, gbc_btnSwitchToTag);
		
		JButton btnToIndividual = new JButton("To Individual");
		btnToIndividual.addActionListener(e -> {
			if(selected != null) {
				selected.entry.assignmnetID.type = AudioAssignmentType.MANUAL;
				selected.entry.assignmnetID.resolveAssignment(selected.entry.id, selected.entry.tags, AudioController.instance);
			}
		});
		GridBagConstraints gbc_btnToIndividual = new GridBagConstraints();
		gbc_btnToIndividual.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnToIndividual.insets = new Insets(0, 0, 0, 5);
		gbc_btnToIndividual.gridx = 1;
		gbc_btnToIndividual.gridy = 0;
		assignmentSwitchPanel.add(btnToIndividual, gbc_btnToIndividual);
		
		JButton btnOff = new JButton("Off");
		btnOff.addActionListener(e -> {
			if(selected != null) {
				selected.entry.assignmnetID.type = AudioAssignmentType.NONE;
			}
		});
		GridBagConstraints gbc_btnOff = new GridBagConstraints();
		gbc_btnOff.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnOff.gridx = 2;
		gbc_btnOff.gridy = 0;
		assignmentSwitchPanel.add(btnOff, gbc_btnOff);
		
		JButton btnManageTags = new JButton("Manage Tags");
		GridBagConstraints gbc_btnManageTags = new GridBagConstraints();
		gbc_btnManageTags.insets = new Insets(0, 0, 5, 0);
		gbc_btnManageTags.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnManageTags.gridx = 0;
		gbc_btnManageTags.gridy = 1;
		settingsPanel.add(btnManageTags, gbc_btnManageTags);
		btnManageTags.addActionListener(e -> (new AudioTagManagerDialog()).setVisible(true));

		JPanel assetAssignPanel = new JPanel();
		GridBagConstraints gbc_assetAssignPanel = new GridBagConstraints();
		gbc_assetAssignPanel.weightx = 1.0;
		gbc_assetAssignPanel.insets = new Insets(0, 0, 5, 0);
		gbc_assetAssignPanel.fill = GridBagConstraints.BOTH;
		gbc_assetAssignPanel.gridx = 0;
		gbc_assetAssignPanel.gridy = 2;
		settingsPanel.add(assetAssignPanel, gbc_assetAssignPanel);
		GridBagLayout gbl_assetAssignPanel = new GridBagLayout();
		gbl_assetAssignPanel.columnWidths = new int[]{0, 0, 0};
		gbl_assetAssignPanel.rowHeights = new int[]{0, 0, 0};
		gbl_assetAssignPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_assetAssignPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
		assetAssignPanel.setLayout(gbl_assetAssignPanel);
		
		this.lblPrimassetval_1 = new JLabel("PrimAssetVal");
		GridBagConstraints gbc_lblPrimassetval_1 = new GridBagConstraints();
		gbc_lblPrimassetval_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrimassetval_1.gridx = 0;
		gbc_lblPrimassetval_1.gridy = 0;
		assetAssignPanel.add(lblPrimassetval_1, gbc_lblPrimassetval_1);
		
		this.lblSecassetval_1 = new JLabel("SecAssetVal");
		GridBagConstraints gbc_lblSecassetval_1 = new GridBagConstraints();
		gbc_lblSecassetval_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblSecassetval_1.gridx = 0;
		gbc_lblSecassetval_1.gridy = 1;
		assetAssignPanel.add(lblSecassetval_1, gbc_lblSecassetval_1);
		
		this.btnAssignPrimaryAsset = new AudioAssetDroppableButton(this, "Drop to Assign Primary Asset");
		this.btnAssignPrimaryAsset.setTransferHandler(new AudioAssetTreeTransferHandler());
		GridBagConstraints gbc_btnAssignPrimaryAsset = new GridBagConstraints();
		gbc_btnAssignPrimaryAsset.weightx = 1.0;
		gbc_btnAssignPrimaryAsset.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnAssignPrimaryAsset.insets = new Insets(5, 0, 0, 0);
		gbc_btnAssignPrimaryAsset.gridx = 1;
		gbc_btnAssignPrimaryAsset.gridy = 0;
		assetAssignPanel.add(btnAssignPrimaryAsset, gbc_btnAssignPrimaryAsset);
		
		this.btnAssignSecondaryAsset = new AudioAssetDroppableButton(this, "Drop to Assign Secondary Asset");
		this.btnAssignSecondaryAsset.setTransferHandler(new AudioAssetTreeTransferHandler());
		GridBagConstraints gbc_btnAssignSecondaryAsset = new GridBagConstraints();
		gbc_btnAssignSecondaryAsset.insets = new Insets(5, 0, 5, 0);
		gbc_btnAssignSecondaryAsset.weightx = 1.0;
		gbc_btnAssignSecondaryAsset.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnAssignSecondaryAsset.gridx = 1;
		gbc_btnAssignSecondaryAsset.gridy = 1;
		assetAssignPanel.add(btnAssignSecondaryAsset, gbc_btnAssignSecondaryAsset);
		
		JPanel rangeMultPanel = new JPanel();
		GridBagConstraints gbc_rangeMultPanel = new GridBagConstraints();
		gbc_rangeMultPanel.weighty = 1.0;
		gbc_rangeMultPanel.weightx = 1.0;
		gbc_rangeMultPanel.insets = new Insets(0, 0, 5, 0);
		gbc_rangeMultPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_rangeMultPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_rangeMultPanel.gridx = 0;
		gbc_rangeMultPanel.gridy = 3;
		settingsPanel.add(rangeMultPanel, gbc_rangeMultPanel);
		GridBagLayout gbl_rangeMultPanel = new GridBagLayout();
		gbl_rangeMultPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_rangeMultPanel.rowHeights = new int[]{0, 0};
		gbl_rangeMultPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_rangeMultPanel.rowWeights = new double[]{0.0, 0.0};
		rangeMultPanel.setLayout(gbl_rangeMultPanel);
		
		JLabel lblPrimaryRangeMult = new JLabel("Primary Range Mult");
		GridBagConstraints gbc_lblPrimaryRangeMult = new GridBagConstraints();
		gbc_lblPrimaryRangeMult.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrimaryRangeMult.anchor = GridBagConstraints.EAST;
		gbc_lblPrimaryRangeMult.gridx = 0;
		gbc_lblPrimaryRangeMult.gridy = 0;
		rangeMultPanel.add(lblPrimaryRangeMult, gbc_lblPrimaryRangeMult);
		
		textFieldPrimaryRangeMult = new JTextField();
		GridBagConstraints gbc_textFieldPrimaryRangeMult = new GridBagConstraints();
		gbc_textFieldPrimaryRangeMult.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldPrimaryRangeMult.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPrimaryRangeMult.gridx = 1;
		gbc_textFieldPrimaryRangeMult.gridy = 0;
		rangeMultPanel.add(textFieldPrimaryRangeMult, gbc_textFieldPrimaryRangeMult);
		textFieldPrimaryRangeMult.setColumns(10);
		
		JLabel lblSecondaryRangeMult = new JLabel("Secondary Range Mult");
		GridBagConstraints gbc_lblSecondaryRangeMult = new GridBagConstraints();
		gbc_lblSecondaryRangeMult.anchor = GridBagConstraints.EAST;
		gbc_lblSecondaryRangeMult.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecondaryRangeMult.gridx = 0;
		gbc_lblSecondaryRangeMult.gridy = 1;
		rangeMultPanel.add(lblSecondaryRangeMult, gbc_lblSecondaryRangeMult);
		
		textFieldSecondaryRangeMult = new JTextField();
		GridBagConstraints gbc_textFieldSecondaryRangeMult = new GridBagConstraints();
		gbc_textFieldSecondaryRangeMult.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldSecondaryRangeMult.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSecondaryRangeMult.gridx = 1;
		gbc_textFieldSecondaryRangeMult.gridy = 1;
		rangeMultPanel.add(textFieldSecondaryRangeMult, gbc_textFieldSecondaryRangeMult);
		textFieldSecondaryRangeMult.setColumns(10);
		
		JButton btnApplyRange = new JButton("Apply");
		GridBagConstraints gbc_btnApplyRange = new GridBagConstraints();
		gbc_btnApplyRange.fill = GridBagConstraints.BOTH;
		gbc_btnApplyRange.gridheight = 2;
		gbc_btnApplyRange.insets = new Insets(0, 0, 0, 5);
		gbc_btnApplyRange.gridx = 2;
		gbc_btnApplyRange.gridy = 0;
		rangeMultPanel.add(btnApplyRange, gbc_btnApplyRange);
		
		JPanel mixerPanel = new JPanel();
		mixerPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Mixer", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_mixerPanel = new GridBagConstraints();
		gbc_mixerPanel.weighty = 1.0;
		gbc_mixerPanel.weightx = 1.0;
		gbc_mixerPanel.anchor = GridBagConstraints.NORTHEAST;
		gbc_mixerPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_mixerPanel.gridx = 0;
		gbc_mixerPanel.gridy = 4;
		settingsPanel.add(mixerPanel, gbc_mixerPanel);
		GridBagLayout gbl_mixerPanel = new GridBagLayout();
		gbl_mixerPanel.columnWidths = new int[]{0, 0};
		gbl_mixerPanel.rowHeights = new int[]{0, 0};
		gbl_mixerPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_mixerPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		mixerPanel.setLayout(gbl_mixerPanel);
		
		this.mixerComboBox = new JComboBox<AudioMixer>( AudioMixer.mixersExposed.toArray(new AudioMixer[AudioMixer.mixersExposed.size()]));
		mixerComboBox.addActionListener(e -> {
			if(selected == null || selected.entry.assignmnetID.getAssignment() == null || mixerComboBox.getSelectedItem() == null) {
				return;
			}
			selected.entry.assignmnetID.getAssignment().setAudioMixer((AudioMixer) mixerComboBox.getSelectedItem());
		});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		mixerPanel.add(mixerComboBox, gbc_comboBox);
		btnApplyRange.addActionListener(e -> {
			if(selected != null) {
				try {
					float rangePrim = Float.parseFloat(textFieldPrimaryRangeMult.getText());
					selected.entry.assignmnetID.getAssignment().getSettings().setPrimaryRange(rangePrim);
				}catch(NumberFormatException ex) {
					ex.printStackTrace();
				}
				try {
					float rangeSec = Float.parseFloat(textFieldPrimaryRangeMult.getText());
					selected.entry.assignmnetID.getAssignment().getSettings().setSecondaryRange(rangeSec);
				}catch(NumberFormatException ex) {
					ex.printStackTrace();
				}

			}
		});

		resetAll();
	}

	private void resetSelectedMixer() {
		if(selected == null  || selected.entry.assignmnetID.getAssignment() == null) {
			return;
		}
		for(int i = 0; i < AudioMixer.mixersExposed.size(); i++) {
			if(selected.entry.assignmnetID.getAssignment().getMixer().getName().equals(AudioMixer.mixersExposed.get(i).getName())) {
				mixerComboBox.setSelectedIndex(i);
				return;
			}
		}
	}
	private void resetAll() {
		for(JLabel l : detailsLabels) {
			l.setText("n/a");
		}
		resetSelectedMixer();
	}
	
	public void setSelected(FiredAudioEvent e) {
		this.selected = e;
		SwingUtilities.invokeLater(this);
	}




	@Override
	public void run() {
		resetAll();
		if(selected == null) {
			return;
		}
		resetSelectedMixer();
		
		lblIdval.setText(String.valueOf(selected.entry.id));

		lblTimeval.setText(String.valueOf(selected.time));

		lblCallval.setText(String.valueOf("["+selected.stackTraceElements[4].getLineNumber()+"] "+selected.stackTraceElements[4].getClassName()));
		
		lblTagsval.setText(String.valueOf(selected.entry.tags));

		lblRemoteval.setText(String.valueOf(selected.entry.remote));

		lblAssignmenttypeval.setText(String.valueOf(selected.entry.assignmnetID.type));

		if(selected.entry.assignmnetID.getAssignment() != null) {
			lblPrimassetval.setText(String.valueOf(selected.entry.assignmnetID.getAssignment().getAssetPrimary() != null ? selected.entry.assignmnetID.getAssignment().getAssetPrimary() : "none"));
	
			lblSecassetval.setText(String.valueOf(selected.entry.assignmnetID.getAssignment().getAssetSecondary() != null ? selected.entry.assignmnetID.getAssignment().getAssetSecondary() : "none"));
			
			lblPrimassetval_1.setText(String.valueOf(selected.entry.assignmnetID.getAssignment().getAssetPrimary() != null ? selected.entry.assignmnetID.getAssignment().getAssetPrimary() : "none"));
			
			lblSecassetval_1.setText(String.valueOf(selected.entry.assignmnetID.getAssignment().getAssetSecondary() != null ? selected.entry.assignmnetID.getAssignment().getAssetSecondary() : "none"));
		}
		lblRemoteidval.setText(String.valueOf(selected.networkId));
		
		
//		textFieldPrimaryRangeMult
//		textFieldSecondaryRangeMult

		if(selected.argument != null) {
			selected.argument.fillLabels(this);
		}
		
		
	}


	public void onDrop(JButton b, AudioAsset a) {
		
		if(selected == null) {
			assert(false);
		}
		if(b == btnAssignPrimaryAsset) {
			if(selected != null) {
				selected.entry.assignmnetID.getAssignment().setPrimaryAsset(a);
			}
		}else if(b == btnAssignSecondaryAsset) {
			if(selected != null) {
				selected.entry.assignmnetID.getAssignment().setSecondaryAsset(a);
			}
		}else {
			assert(false);
		}
		SwingUtilities.invokeLater(this);
	}
}
