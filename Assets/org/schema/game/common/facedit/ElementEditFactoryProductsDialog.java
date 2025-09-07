package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class ElementEditFactoryProductsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	private final JPanel contentPanel = new JPanel();

	private TreeSetListModel<TemporalProduct> model;

	private JButton btnEdit;

	private JButton btnDelete;

	private JList list;

	private JButton btnAdd;

	/**
	 * Create the dialog.
	 *
	 * @param field
	 */
	public ElementEditFactoryProductsDialog(final JFrame frame, final ElementInformation info, final ExecuteInterface execInterface) {
		super(frame, true);
		final TemporalFactory fac = new TemporalFactory();
		fac.setFromExistingInfo(info.id);
		final Set<TemporalProduct> base = new HashSet<TemporalProduct>();

		for (TemporalProduct k : fac.temporalProducts) {
			base.add(k);
		}
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{50, 0, 200};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			btnEdit = new JButton("Edit");
			GridBagConstraints gbc_btnEdit = new GridBagConstraints();
			gbc_btnEdit.anchor = GridBagConstraints.WEST;
			gbc_btnEdit.weightx = 1.0;
			gbc_btnEdit.insets = new Insets(0, 0, 5, 5);
			gbc_btnEdit.gridx = 0;
			gbc_btnEdit.gridy = 0;
			contentPanel.add(btnEdit, gbc_btnEdit);

		}
		{
			btnAdd = new JButton("Add");
			GridBagConstraints gbc_btnAdd = new GridBagConstraints();
			gbc_btnAdd.anchor = GridBagConstraints.WEST;
			gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
			gbc_btnAdd.gridx = 1;
			gbc_btnAdd.gridy = 0;
			contentPanel.add(btnAdd, gbc_btnAdd);
		}
		{
			btnDelete = new JButton("Delete");
			GridBagConstraints gbc_btnDelete = new GridBagConstraints();
			gbc_btnDelete.anchor = GridBagConstraints.EAST;
			gbc_btnDelete.weightx = 1.0;
			gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
			gbc_btnDelete.gridx = 2;
			gbc_btnDelete.gridy = 0;

			contentPanel.add(btnDelete, gbc_btnDelete);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.weighty = 1.0;
			gbc_scrollPane.weightx = 1.0;
			gbc_scrollPane.gridwidth = 3;
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				list = new JList();
				model = new TreeSetListModel<TemporalProduct>();
				list.setModel(model);
				Iterator<TemporalProduct> iterator = base.iterator();
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

					fac.temporalProducts.clear();
					fac.temporalProducts.addAll(base);
					for (int i = 0; i < fac.temporalProducts.size(); i++) {
						TemporalProduct temporalProduct = fac.temporalProducts.get(i);
						if (temporalProduct.input.isEmpty()) {
							fac.temporalProducts.remove(i);
							i--;
						}
					}
					fac.convertFromTemporal(info);
					execInterface.execute();

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
		btnEdit.addActionListener(arg0 -> {
			Object o = list.getSelectedValue();
			if (o != null && o instanceof TemporalProduct) {
				ProductEditDialog diag = new ProductEditDialog(frame, ((TemporalProduct) o).input, ((TemporalProduct) o).output, () -> {
					//nothing to do
				});
				diag.setVisible(true);
			}

		});
		btnAdd.addActionListener(arg0 -> {
			final TemporalProduct p = new TemporalProduct();

			ProductEditDialog diag = new ProductEditDialog(frame, p.input, p.output, () -> model.add(p));
			diag.setVisible(true);

		});
		btnDelete.addActionListener(e -> {
			Object[] vals = list.getSelectedValues();
			if (vals != null) {
				for (int i = 0; i < vals.length; i++) {
					Object selectedValue = vals[i];
					if (selectedValue != null) {
						model.remove((TemporalProduct) selectedValue);
					}

				}
			}
		});
	}

}
