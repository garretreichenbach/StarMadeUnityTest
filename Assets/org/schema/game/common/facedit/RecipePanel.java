package org.schema.game.common.facedit;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.data.element.FixedRecipeProduct;

import com.bulletphysics.util.ObjectArrayList;

public class RecipePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private FixedRecipeProduct selected;
	private ResourcePanel resourcePanelIn;
	private ResourcePanel resourcePanelOut;
	private CostP selPrice;
	private JPanel productSuperPanel;
	private JComboBox comboBoxProducts;

	/**
	 * Create the panel.
	 *
	 * @param fixedRecipe
	 */
	public RecipePanel(final JFrame frame, final FixedRecipe fixedRecipe) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0};
		panel.setLayout(gbl_panel);

		final JLabel lblRecipeView = new JLabel("Editing: " + fixedRecipe.name);
		lblRecipeView.setFont(new Font("Arial", Font.BOLD, 18));
		GridBagConstraints gbc_lblRecipeView = new GridBagConstraints();
		gbc_lblRecipeView.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblRecipeView.insets = new Insets(5, 5, 5, 0);
		gbc_lblRecipeView.gridx = 0;
		gbc_lblRecipeView.gridy = 0;
		panel.add(lblRecipeView, gbc_lblRecipeView);

		final ArrayList<CostP> a = new ArrayList<CostP>();
		CostP cP = new CostP((short) -1);
		a.add(cP);
		if (fixedRecipe.costType == -1) {
			selPrice = cP;
		}
		for (short s : ElementKeyMap.keySet) {
			CostP costP = new CostP(s);
			a.add(costP);
			if (fixedRecipe.costType == s) {
				selPrice = costP;
			}
		}

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JButton btnApplyName = new JButton("Change Name");
		btnApplyName.addActionListener(e -> {

			Object[] possibilities = null;
			String s = (String) JOptionPane.showInputDialog(
					frame,
					"Choose a name:",
					"Name Recipe",
					JOptionPane.PLAIN_MESSAGE,
					null,
					possibilities,
					fixedRecipe.name);

			//If a string was returned, say so.
			if ((s != null) && (s.length() > 0)) {
				fixedRecipe.name = s;
				lblRecipeView.setText("Editing: " + fixedRecipe.name);
				frame.repaint();
			}

		});
		GridBagConstraints gbc_btnApplyName = new GridBagConstraints();
		gbc_btnApplyName.insets = new Insets(0, 0, 0, 5);
		gbc_btnApplyName.gridx = 0;
		gbc_btnApplyName.gridy = 0;
		panel_1.add(btnApplyName, gbc_btnApplyName);

		JPanel costPanel = new JPanel();
		costPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Shop Cost", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_costPanel = new GridBagConstraints();
		gbc_costPanel.fill = GridBagConstraints.BOTH;
		gbc_costPanel.anchor = GridBagConstraints.NORTH;
		gbc_costPanel.insets = new Insets(0, 0, 5, 0);
		gbc_costPanel.gridx = 0;
		gbc_costPanel.gridy = 2;
		panel.add(costPanel, gbc_costPanel);
		GridBagLayout gbl_costPanel = new GridBagLayout();
		gbl_costPanel.columnWidths = new int[]{0, 0};
		gbl_costPanel.rowHeights = new int[]{0};
		gbl_costPanel.columnWeights = new double[]{0.0, 0.0};
		gbl_costPanel.rowWeights = new double[]{0.0};
		costPanel.setLayout(gbl_costPanel);

		//#RM1958 remove generic arguments
		JComboBox comboBoxCost = new JComboBox(new ComboBoxModel() {

			@Override
			public int getSize() {
				return a.size();
			}

			@Override
			public CostP getElementAt(int index) {
				return a.get(index);
			}

			@Override
			public void addListDataListener(ListDataListener l) {
			}

			@Override
			public void removeListDataListener(ListDataListener l) {
			}

			@Override
			public void setSelectedItem(Object anItem) {
				selPrice = (CostP) anItem;
			}

			@Override
			public Object getSelectedItem() {
				return selPrice;
			}

		});
		comboBoxCost.addActionListener(e -> {
			if (selPrice != null) {
				fixedRecipe.costType = selPrice.type;
			}
		});
		GridBagConstraints gbc_comboBoxCost = new GridBagConstraints();
		gbc_comboBoxCost.weightx = 10.0;
		gbc_comboBoxCost.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxCost.gridx = 0;
		gbc_comboBoxCost.gridy = 0;
		costPanel.add(comboBoxCost, gbc_comboBoxCost);

		final JSpinner spinner = new JSpinner();
		spinner.addChangeListener(e -> {
			try {
				fixedRecipe.costAmount = (Integer) spinner.getValue();
			} catch (Exception r) {
				r.printStackTrace();
			}
		});
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.weightx = 1.0;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		costPanel.add(spinner, gbc_spinner);

		spinner.setValue(fixedRecipe.costAmount);

		productSuperPanel = new JPanel();
		final TitledBorder titledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Recipe Products", TitledBorder.LEADING, TitledBorder.TOP, null, null);

		productSuperPanel.setBorder(titledBorder);
		GridBagConstraints gbc_productSuperPanel = new GridBagConstraints();
		gbc_productSuperPanel.weighty = 11.0;
		gbc_productSuperPanel.fill = GridBagConstraints.BOTH;
		gbc_productSuperPanel.gridx = 0;
		gbc_productSuperPanel.gridy = 3;
		panel.add(productSuperPanel, gbc_productSuperPanel);
		GridBagLayout gbl_productSuperPanel = new GridBagLayout();
		gbl_productSuperPanel.columnWidths = new int[]{0, 0};
		gbl_productSuperPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_productSuperPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_productSuperPanel.rowWeights = new double[]{1.0, 4.9E-324, 0.0, Double.MIN_VALUE};
		productSuperPanel.setLayout(gbl_productSuperPanel);

		JPanel productPanel = new JPanel();
		GridBagConstraints gbc_productPanel = new GridBagConstraints();
		gbc_productPanel.fill = GridBagConstraints.BOTH;
		gbc_productPanel.insets = new Insets(0, 0, 5, 0);
		gbc_productPanel.gridx = 0;
		gbc_productPanel.gridy = 0;
		productSuperPanel.add(productPanel, gbc_productPanel);
		GridBagLayout gbl_productPanel = new GridBagLayout();
		gbl_productPanel.columnWidths = new int[]{0, 0, 0};
		gbl_productPanel.rowHeights = new int[]{0, 0, 0};
		gbl_productPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_productPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		productPanel.setLayout(gbl_productPanel);

		comboBoxProducts = new JComboBox(new ComboBoxRecipe(fixedRecipe));
		if (comboBoxProducts.getModel().getSize() > 0) {
			comboBoxProducts.setSelectedIndex(0);
			change(frame);
		}
		comboBoxProducts.addActionListener(e -> change(frame));
		GridBagConstraints gbc_comboBoxProducts = new GridBagConstraints();
		gbc_comboBoxProducts.gridwidth = 3;
		gbc_comboBoxProducts.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxProducts.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxProducts.gridx = 0;
		gbc_comboBoxProducts.gridy = 0;
		productPanel.add(comboBoxProducts, gbc_comboBoxProducts);

		JButton btnNewButton = new JButton("Add Product");
		btnNewButton.addActionListener(e -> {
			FixedRecipeProduct[] recipeProducts = fixedRecipe.recipeProducts;
			fixedRecipe.recipeProducts = new FixedRecipeProduct[recipeProducts.length + 1];
			fixedRecipe.recipeProducts[0] = new FixedRecipeProduct();
			fixedRecipe.recipeProducts[0].input = new FactoryResource[0];
			fixedRecipe.recipeProducts[0].output = new FactoryResource[0];
			for (int i = 0; i < recipeProducts.length; i++) {
				fixedRecipe.recipeProducts[i + 1] = recipeProducts[i];
			}
			change(frame);
			comboBoxProducts.setSelectedIndex(0);
			titledBorder.setTitle("Recipe Products " + comboBoxProducts.getModel().getSize());
			comboBoxProducts.repaint();
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 1;
		productPanel.add(btnNewButton, gbc_btnNewButton);

		JButton btnRemoveProduct = new JButton("Remove Product");
		btnRemoveProduct.addActionListener(e -> {
			FixedRecipeProduct[] recipeProducts = fixedRecipe.recipeProducts;
			if (selected != null) {
				boolean contains = false;
				for (FixedRecipeProduct f : recipeProducts) {
					if (f == selected) {
						contains = true;
						break;
					}
				}
				if (contains) {

					fixedRecipe.recipeProducts = new FixedRecipeProduct[recipeProducts.length - 1];
					int i = 0;
					for (FixedRecipeProduct f : recipeProducts) {
						if (f != selected) {
							fixedRecipe.recipeProducts[i] = f;
							i++;
						}
					}
					if (comboBoxProducts.getModel().getSize() > 0) {
						comboBoxProducts.setSelectedIndex(0);
					} else {
						comboBoxProducts.setSelectedIndex(-1);
					}
					titledBorder.setTitle("Recipe Products " + comboBoxProducts.getModel().getSize());
					change(frame);
					comboBoxProducts.repaint();
				}
			}
		});

		GridBagConstraints gbc_btnRemoveProduct = new GridBagConstraints();
		gbc_btnRemoveProduct.anchor = GridBagConstraints.EAST;
		gbc_btnRemoveProduct.gridx = 1;
		gbc_btnRemoveProduct.gridy = 1;
		productPanel.add(btnRemoveProduct, gbc_btnRemoveProduct);
		titledBorder.setTitle("Recipe Products " + comboBoxProducts.getModel().getSize());

	}

	private void change(final JFrame frame) {
		selected = (FixedRecipeProduct) comboBoxProducts.getSelectedItem();
		System.err.println("SELECTED: " + selected + ";");
		if (selected != null) {
			resourcePanelIn = new ResourcePanel(frame, selected.input) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				/**
				 *
				 */
				

				@Override
				public void changed(ObjectArrayList<FactoryResource> l) {
					selected.input = l.toArray(new FactoryResource[0]);
				}
			};
			GridBagConstraints gbc_resourcePanelIn = new GridBagConstraints();
			gbc_resourcePanelIn.fill = GridBagConstraints.BOTH;
			gbc_resourcePanelIn.insets = new Insets(0, 0, 5, 0);
			gbc_resourcePanelIn.gridx = 0;
			gbc_resourcePanelIn.gridy = 1;
			if (resourcePanelIn != null) {
				productSuperPanel.remove(resourcePanelIn);
			}
			productSuperPanel.add(resourcePanelIn, gbc_resourcePanelIn);
			resourcePanelIn.setBorder(new TitledBorder(null, "Input", TitledBorder.LEADING, TitledBorder.TOP, null, null));

			resourcePanelOut = new ResourcePanel(frame, selected.output) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				/**
				 *
				 */
				

				@Override
				public void changed(ObjectArrayList<FactoryResource> l) {
					selected.output = l.toArray(new FactoryResource[0]);
				}

			};
			GridBagConstraints gbc_resourcePanelOut = new GridBagConstraints();
			gbc_resourcePanelOut.fill = GridBagConstraints.BOTH;
			gbc_resourcePanelOut.gridx = 0;
			gbc_resourcePanelOut.gridy = 2;
			if (resourcePanelOut != null) {
				productSuperPanel.remove(resourcePanelOut);
			}
			productSuperPanel.add(resourcePanelOut, gbc_resourcePanelOut);
			resourcePanelOut.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		}
		repaint();
	}

	private class CostP {
		private short type;

		public CostP(short s) {
			this.type = s;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return type;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return type == ((CostP) obj).type;
		}

		@Override
		public String toString() {
			if (type == -1) {
				return "Credits";
			} else {
				return ElementKeyMap.toString(type);
			}
		}

	}

	//#RM1958 remove ComboBoxModel generic argument
	private class ComboBoxRecipe implements ComboBoxModel {
		private FixedRecipe fixedRecipe;
		private FixedRecipeProduct selected;

		public ComboBoxRecipe(FixedRecipe fixedRecipe) {
			this.fixedRecipe = fixedRecipe;
		}

		@Override
		public int getSize() {
			if (fixedRecipe == null || fixedRecipe.recipeProducts == null) {
				return 0;
			}
			return fixedRecipe.recipeProducts.length;
		}

		@Override
		public FixedRecipeProduct getElementAt(int index) {
			return fixedRecipe.recipeProducts[index];
		}

		@Override
		public void addListDataListener(ListDataListener l) {

		}

		@Override
		public void removeListDataListener(ListDataListener l) {

		}

		@Override
		public void setSelectedItem(Object anItem) {
			this.selected = (FixedRecipeProduct) anItem;
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}
	}
}
