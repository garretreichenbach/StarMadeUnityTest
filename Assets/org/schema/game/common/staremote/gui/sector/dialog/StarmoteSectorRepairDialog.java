package org.schema.game.common.staremote.gui.sector.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.gui.StarmoteFrame;
import org.schema.game.common.staremote.gui.sector.StarmoteSectorSelectionPanel;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmoteSectorRepairDialog extends JDialog {

	/**
	 *
	 */
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the dialog.
	 */
	public StarmoteSectorRepairDialog(final GameClientState state) {
		super(StarmoteFrame.self, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Repair Sector");
		setBounds(100, 100, 449, 144);
		getContentPane().setLayout(new BorderLayout());

		final StarmoteSectorSelectionPanel starmodeSectorSelectionPanel = new StarmoteSectorSelectionPanel();
		getContentPane().add(starmodeSectorSelectionPanel, BorderLayout.CENTER);
		GridBagLayout gbl_starmodeSectorSelectionPanel = new GridBagLayout();
		gbl_starmodeSectorSelectionPanel.columnWidths = new int[]{0};
		gbl_starmodeSectorSelectionPanel.rowHeights = new int[]{0};
		gbl_starmodeSectorSelectionPanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_starmodeSectorSelectionPanel.rowWeights = new double[]{Double.MIN_VALUE};
		starmodeSectorSelectionPanel.setLayout(gbl_starmodeSectorSelectionPanel);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					Vector3i coord = starmodeSectorSelectionPanel.getCoord();
					state.getController().sendAdminCommand(AdminCommands.REPAIR_SECTOR, coord.x, coord.y, coord.z);
					dispose();
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

	}

}
