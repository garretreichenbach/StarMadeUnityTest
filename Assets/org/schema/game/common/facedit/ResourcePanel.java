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

import org.schema.game.common.data.element.FactoryResource;

public abstract class ResourcePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final ArrayModel<FactoryResource> arrayListModel;
	//#RM1958 remove JList generic argument
	private JList list;

	/**
	 * Create the panel.
	 */
	public ResourcePanel(final JFrame frame, final FactoryResource[] resources) {

		arrayListModel = new ArrayModel<FactoryResource>(resources) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			/**
			 *
			 */
			

			@Override
			public void changed(
					com.bulletphysics.util.ObjectArrayList<FactoryResource> l) {
				ResourcePanel.this.changed(l);
			}

		};

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0};
		setLayout(gridBagLayout);

		JButton btnAdd = new JButton("Add");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.WEST;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		add(btnAdd, gbc_btnAdd);
		btnAdd.addActionListener(arg0 -> {
			ResourceSingleEditDialog diag = new ResourceSingleEditDialog(frame, null, arrayListModel);
			diag.setVisible(true);
		});
		JButton btnRemove = new JButton("Remove");
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.anchor = GridBagConstraints.EAST;
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 0;
		add(btnRemove, gbc_btnRemove);
		btnRemove.addActionListener(e -> {
			//#RM1958 change lists to use Object and cast to FactoryResource
			List selectedValues = Arrays.asList(list.getSelectedValues());
			for (Object mm : selectedValues) {
				if (e != null) {
					arrayListModel.remove((FactoryResource) mm);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		//#RM1958 remove JList generic argument
		list = new JList(arrayListModel);
		scrollPane.setViewportView(list);
	}

	public abstract void changed(
			com.bulletphysics.util.ObjectArrayList<FactoryResource> l);

}
