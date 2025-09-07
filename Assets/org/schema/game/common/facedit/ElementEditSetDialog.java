package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public class ElementEditSetDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	private final JPanel contentPanel = new JPanel();

	private TreeSetListModel<ElementInformation> model;

	private JButton btnAdd;

	private JButton btnDelete;

	private JList list;

	/**
	 * Create the dialog.
	 */
	public ElementEditSetDialog(final JFrame frame, final Collection<Short> collection) {
		super(frame, true);

		final Set<ElementInformation> base = new HashSet<ElementInformation>();

		for (Short k : collection) {
			base.add(ElementKeyMap.getInfo(k));
		}
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{224, 150};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			btnAdd = new JButton("Add");
			GridBagConstraints gbc_btnAdd = new GridBagConstraints();
			gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
			gbc_btnAdd.gridx = 0;
			gbc_btnAdd.gridy = 0;
			contentPanel.add(btnAdd, gbc_btnAdd);

		}
		{
			btnDelete = new JButton("Delete");
			GridBagConstraints gbc_btnDelete = new GridBagConstraints();
			gbc_btnDelete.weightx = 1.0;
			gbc_btnDelete.insets = new Insets(0, 0, 5, 5);
			gbc_btnDelete.gridx = 1;
			gbc_btnDelete.gridy = 0;

			contentPanel.add(btnDelete, gbc_btnDelete);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.weighty = 1.0;
			gbc_scrollPane.weightx = 1.0;
			gbc_scrollPane.gridwidth = 2;
			gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				list = new JList();
				model = new TreeSetListModel<ElementInformation>();
				list.setModel(model);
				Iterator<ElementInformation> iterator = base.iterator();
				while (iterator.hasNext()) {
					model.add(iterator.next());
				}
				scrollPane.setViewportView(list);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);

				okButton.addActionListener(e -> {
					base.clear();
					base.addAll(model.getCollection());
					collection.clear();
					for (ElementInformation ei : base) {
						collection.add(ei.getId());
					}
					dispose();
				});

			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");

				cancelButton.addActionListener(arg0 -> dispose());
				buttonPane.add(cancelButton);

			}
		}
		btnAdd.addActionListener(arg0 -> {
			ElementChoserDialog diag = new ElementChoserDialog(frame, info -> model.add(info));
			diag.setVisible(true);
		});
		btnDelete.addActionListener(e -> {
			Object[] vals = list.getSelectedValues();
			if (vals != null) {
				for (int i = 0; i < vals.length; i++) {
					Object selectedValue = vals[i];
					if (selectedValue != null) {
						model.remove((ElementInformation) selectedValue);
					}

				}
			}
		});
	}


}
