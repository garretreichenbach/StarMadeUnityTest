package org.schema.game.common.staremote.gui.catalog.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.staremote.gui.catalog.StarmoteCatalogTableModel;

public class StarmoteCatalogEditDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final CatalogPermission p;
	private final GameClientState gState;
	private JTextField nameField;
	private JTextField ownerField;
	private JSpinner priceSpinner;
	private JTextField descField;
	private JCheckBox chckbxSpawnableFromHombases;
	private JCheckBox chckbxSpawnableByFaction;
	private JCheckBox chckbxSpawnableByEnemies;
	private JCheckBox chckbxSpawnableByOthers;

	/**
	 * Create the dialog.
	 *
	 * @param table
	 */
	public StarmoteCatalogEditDialog(JFrame f, final CatalogPermission p, GameClientState state, final StarmoteCatalogTableModel model, final JTable table) {
		super(f, true);
		setAlwaysOnTop(true);
		this.gState = state;
		this.p = p;
		setTitle("Edit Catalog Entry");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(400, 400, 550, 500);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Name", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				nameField = new JTextField();
				GridBagConstraints gbc_textField = new GridBagConstraints();
				gbc_textField.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField.gridx = 0;
				gbc_textField.gridy = 0;
				panel.add(nameField, gbc_textField);
				nameField.setColumns(10);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Owner", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 1;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				ownerField = new JTextField();
				GridBagConstraints gbc_textField_1 = new GridBagConstraints();
				gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField_1.gridx = 0;
				gbc_textField_1.gridy = 0;
				panel.add(ownerField, gbc_textField_1);
				ownerField.setColumns(10);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Price", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 2;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				priceSpinner = new JSpinner();
				GridBagConstraints gbc_textField_2 = new GridBagConstraints();
				gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField_2.gridx = 0;
				gbc_textField_2.gridy = 0;
				panel.add(priceSpinner, gbc_textField_2);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Description", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 3;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				descField = new JTextField();
				GridBagConstraints gbc_textField_3 = new GridBagConstraints();
				gbc_textField_3.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField_3.gridx = 0;
				gbc_textField_3.gridy = 0;
				panel.add(descField, gbc_textField_3);
				descField.setColumns(10);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Permissions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 4;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
			gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				chckbxSpawnableFromHombases = new JCheckBox("Spawnable from hombases");
				GridBagConstraints gbc_chckbxSpawnableFromHombases = new GridBagConstraints();
				gbc_chckbxSpawnableFromHombases.anchor = GridBagConstraints.WEST;
				gbc_chckbxSpawnableFromHombases.insets = new Insets(0, 0, 5, 0);
				gbc_chckbxSpawnableFromHombases.gridx = 0;
				gbc_chckbxSpawnableFromHombases.gridy = 0;
				panel.add(chckbxSpawnableFromHombases, gbc_chckbxSpawnableFromHombases);
			}
			{
				chckbxSpawnableByFaction = new JCheckBox("Spawnable by faction ");
				GridBagConstraints gbc_chckbxSpawnableByFaction = new GridBagConstraints();
				gbc_chckbxSpawnableByFaction.insets = new Insets(0, 0, 5, 0);
				gbc_chckbxSpawnableByFaction.anchor = GridBagConstraints.WEST;
				gbc_chckbxSpawnableByFaction.gridx = 0;
				gbc_chckbxSpawnableByFaction.gridy = 1;
				panel.add(chckbxSpawnableByFaction, gbc_chckbxSpawnableByFaction);
			}
			{
				chckbxSpawnableByEnemies = new JCheckBox("Spawnable by enemies");
				GridBagConstraints gbc_chckbxSpawnableByEnemies = new GridBagConstraints();
				gbc_chckbxSpawnableByEnemies.insets = new Insets(0, 0, 5, 0);
				gbc_chckbxSpawnableByEnemies.anchor = GridBagConstraints.WEST;
				gbc_chckbxSpawnableByEnemies.gridx = 0;
				gbc_chckbxSpawnableByEnemies.gridy = 2;
				panel.add(chckbxSpawnableByEnemies, gbc_chckbxSpawnableByEnemies);
			}
			{
				chckbxSpawnableByOthers = new JCheckBox("Spawnable by others");
				GridBagConstraints gbc_chckbxSpawnableByOthers = new GridBagConstraints();
				gbc_chckbxSpawnableByOthers.anchor = GridBagConstraints.WEST;
				gbc_chckbxSpawnableByOthers.gridx = 0;
				gbc_chckbxSpawnableByOthers.gridy = 3;
				panel.add(chckbxSpawnableByOthers, gbc_chckbxSpawnableByOthers);
			}
		}
		{
			JButton btnDeleteEntry = new JButton("Delete Entry");
			btnDeleteEntry.addActionListener(arg0 -> gState.getCatalogManager().clientRequestCatalogRemove(p));
			GridBagConstraints gbc_btnDeleteEntry = new GridBagConstraints();
			gbc_btnDeleteEntry.gridx = 0;
			gbc_btnDeleteEntry.gridy = 5;
			contentPanel.add(btnDeleteEntry, gbc_btnDeleteEntry);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					apply();
					model.rebuild(gState);
					dispose();
					table.removeColumnSelectionInterval(0, table.getColumnCount());
					table.invalidate();
					table.validate();
					table.repaint();

				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(e -> dispose());
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setOriginalSettings();
	}

	public void apply() {
		CatalogPermission p = new CatalogPermission(this.p);
		p.setUid(nameField.getText().trim());
		p.ownerUID = ownerField.getText().trim();
		p.price = (Integer) priceSpinner.getValue();
		p.description = descField.getText();

		p.setPermission(chckbxSpawnableByEnemies.isSelected(), CatalogPermission.P_ENEMY_USABLE);
		p.setPermission(chckbxSpawnableByFaction.isSelected(), CatalogPermission.P_BUY_FACTION);
		p.setPermission(chckbxSpawnableByOthers.isSelected(), CatalogPermission.P_BUY_OTHERS);
		p.setPermission(chckbxSpawnableFromHombases.isSelected(), CatalogPermission.P_BUY_HOME_ONLY);

		gState.getCatalogManager().clientRequestCatalogEdit(p);
	}

	private void setOriginalSettings() {
		nameField.setText(p.getUid());
		ownerField.setText(p.ownerUID);
		priceSpinner.setValue(p.price);
		descField.setText(p.description);

		chckbxSpawnableByEnemies.setSelected(p.enemyUsable());
		chckbxSpawnableByFaction.setSelected(p.faction());
		chckbxSpawnableByOthers.setSelected(p.others());
		chckbxSpawnableFromHombases.setSelected(p.homeOnly());
	}

}
