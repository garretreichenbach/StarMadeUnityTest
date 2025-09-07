package org.schema.game.common.facedit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.schema.game.common.data.element.FactoryResource;

public class FactoryResourceEditPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JList list;
	private ArrayListModel<FactoryResource> arrayListModel;

	/**
	 * Create the panel.
	 */
	public FactoryResourceEditPanel(final JFrame frame, String title, final List<FactoryResource> resources) {
		arrayListModel = new ArrayListModel<FactoryResource>(resources);
		list = new JList();
		setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JButton btnAdd = new JButton("Add");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.WEST;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		add(btnAdd, gbc_btnAdd);

		btnAdd.addActionListener(arg0 -> {
			FactoryResourceSingleEditDialog diag = new FactoryResourceSingleEditDialog(frame, null, arrayListModel);
			diag.setVisible(true);
			//				arrayListModel = new ArrayListModel<FactoryResource>(resources);
			//				list = new JList();
			//				list.setModel(arrayListModel);
		});

		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(e -> {
			//#RM1958 replace java7+ method
			List selectedValues = Arrays.asList(list.getSelectedValues());
			for (Object mm : selectedValues) {
				if (e != null) {
					FactoryResourceSingleEditDialog diag = new FactoryResourceSingleEditDialog(frame, (FactoryResource) mm, arrayListModel);
					diag.setVisible(true);
					break;
				}
			}

		});
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 5, 5);
		gbc_btnEdit.gridx = 1;
		gbc_btnEdit.gridy = 0;
		add(btnEdit, gbc_btnEdit);

		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.anchor = GridBagConstraints.EAST;
		gbc_btnDelete.weightx = 1.0;
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridx = 2;
		gbc_btnDelete.gridy = 0;
		add(btnDelete, gbc_btnDelete);
		btnDelete.addActionListener(e -> {
			//#RM1958 replace java7+ method
			List selectedValues = Arrays.asList(list.getSelectedValues());
			for (Object mm : selectedValues) {
				if (e != null) {
					arrayListModel.remove((FactoryResource) mm);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		scrollPane.setViewportView(list);

		list.setModel(arrayListModel);

	}

}
