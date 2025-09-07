package org.schema.game.common.staremote.gui.entity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.schema.game.common.staremote.gui.settings.StarmoteSettingElement;
import org.schema.game.common.staremote.gui.settings.StarmoteSettingTableModel;
import org.schema.game.common.staremote.gui.settings.StarmoteSettingTextLabel;
import org.schema.game.common.staremote.gui.settings.StarmoteSettingsTableCellEditor;
import org.schema.game.common.staremote.gui.settings.StarmoteSettingsTableCellRenderer;
import org.schema.schine.network.objects.Sendable;

public class StarmoteEntitySettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final Sendable sendable;
	private StarmoteSettingTableModel listModel;
	private JTable list;

	/**
	 * Create the panel.
	 */
	public StarmoteEntitySettingsPanel(Sendable s) {
		this.sendable = s;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		listModel = new StarmoteSettingTableModel();

		list = new JTable(listModel);
		list.setDefaultRenderer(StarmoteSettingElement.class, new StarmoteSettingsTableCellRenderer());
		list.setDefaultRenderer(String.class, new StarmoteSettingsTableCellRenderer());
		list.setDefaultEditor(StarmoteSettingElement.class, new StarmoteSettingsTableCellEditor());

		initializeSettings();
		scrollPane.setViewportView(list);
	}

	public void addSetting(StarmoteSettingElement e) {
		listModel.getList().add(e);
		list.invalidate();
		list.validate();
	}

	/**
	 * @return the sendable
	 */
	public Sendable getSendable() {
		return sendable;
	}

	public void initializeSettings() {
		addSetting(new StarmoteSettingTextLabel("ID") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return sendable.getId();
			}
		});
		addSetting(new StarmoteSettingTextLabel("CLASS") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return sendable.getClass().getSimpleName();
			}
		});
	}

	public void updateSettings() {
		listModel.updateElements();
	}
}
