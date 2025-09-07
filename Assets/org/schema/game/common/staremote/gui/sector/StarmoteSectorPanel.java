package org.schema.game.common.staremote.gui.sector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.gui.sector.dialog.StarmoteEntitySearchDialog;
import org.schema.game.common.staremote.gui.sector.dialog.StarmoteSectorChangeDialog;
import org.schema.game.common.staremote.gui.sector.dialog.StarmoteSectorDespawnDialog;
import org.schema.game.common.staremote.gui.sector.dialog.StarmoteSectorPopulateDialog;
import org.schema.game.common.staremote.gui.sector.dialog.StarmoteSectorRepairDialog;

public class StarmoteSectorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public StarmoteSectorPanel(final GameClientState state) {
		super();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JButton btnRepairSector = new JButton("Repair Sector");
		btnRepairSector.addActionListener(e -> (new StarmoteSectorRepairDialog(state)).setVisible(true));
		GridBagConstraints gbc_btnRepairSector = new GridBagConstraints();
		gbc_btnRepairSector.anchor = GridBagConstraints.WEST;
		gbc_btnRepairSector.insets = new Insets(0, 0, 5, 0);
		gbc_btnRepairSector.gridx = 0;
		gbc_btnRepairSector.gridy = 0;
		add(btnRepairSector, gbc_btnRepairSector);

		JButton btnWarpPlayerTo = new JButton("Warp Player to Sector");
		btnWarpPlayerTo.addActionListener(e -> (new StarmoteSectorChangeDialog(state)).setVisible(true));
		GridBagConstraints gbc_btnWarpPlayerTo = new GridBagConstraints();
		gbc_btnWarpPlayerTo.anchor = GridBagConstraints.WEST;
		gbc_btnWarpPlayerTo.insets = new Insets(0, 0, 5, 0);
		gbc_btnWarpPlayerTo.gridx = 0;
		gbc_btnWarpPlayerTo.gridy = 1;
		add(btnWarpPlayerTo, gbc_btnWarpPlayerTo);

		JButton btnSearchEntity = new JButton("Search Entity");
		btnSearchEntity.addActionListener(e -> (new StarmoteEntitySearchDialog(state)).setVisible(true));
		GridBagConstraints gbc_btnSearchEntity = new GridBagConstraints();
		gbc_btnSearchEntity.insets = new Insets(0, 0, 5, 0);
		gbc_btnSearchEntity.anchor = GridBagConstraints.WEST;
		gbc_btnSearchEntity.gridx = 0;
		gbc_btnSearchEntity.gridy = 2;
		add(btnSearchEntity, gbc_btnSearchEntity);

		JButton btnDespawnEntities = new JButton("Despawn Entities");
		btnDespawnEntities.addActionListener(e -> (new StarmoteSectorDespawnDialog(state)).setVisible(true));
		GridBagConstraints gbc_btnDespawnEntities = new GridBagConstraints();
		gbc_btnDespawnEntities.insets = new Insets(0, 0, 5, 0);
		gbc_btnDespawnEntities.anchor = GridBagConstraints.WEST;
		gbc_btnDespawnEntities.gridx = 0;
		gbc_btnDespawnEntities.gridy = 3;
		add(btnDespawnEntities, gbc_btnDespawnEntities);

		JButton btnPopulateSector = new JButton("Populate Sector");
		btnPopulateSector.addActionListener(e -> (new StarmoteSectorPopulateDialog(state)).setVisible(true));
		GridBagConstraints gbc_btnPopulateSector = new GridBagConstraints();
		gbc_btnPopulateSector.anchor = GridBagConstraints.WEST;
		gbc_btnPopulateSector.gridx = 0;
		gbc_btnPopulateSector.gridy = 4;
		add(btnPopulateSector, gbc_btnPopulateSector);
	}

}
