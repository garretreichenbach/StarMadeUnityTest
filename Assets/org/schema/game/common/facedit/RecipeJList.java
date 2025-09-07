package org.schema.game.common.facedit;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.data.element.FixedRecipeProduct;
import org.schema.game.common.data.element.FixedRecipes;

public class RecipeJList extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	// #RM1958 remove JList generic argument
	private JList list;

	/**
	 * Create the panel.
	 */
	public RecipeJList(ListSelectionListener e) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		// #RM1958 remove JList generic argument
		list = new JList(new RecipeModel());
		list.setCellRenderer(new RecipeCellRenderer());
		list.addListSelectionListener(e);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 0;
		add(list, gbc_list);
	}

	public FixedRecipe getSelected() {
		int i = list.getSelectedIndex();
		if (i >= 0 && i < ElementKeyMap.fixedRecipes.recipes.size()) {
			return ElementKeyMap.fixedRecipes.recipes.get(i);
		}
		return null;
	}

	public void addNew() {
		FixedRecipe fixedRecipe = new FixedRecipe();
		fixedRecipe.costType = -1;
		fixedRecipe.costAmount = 0;
		fixedRecipe.recipeProducts = new FixedRecipeProduct[0];
		ElementKeyMap.fixedRecipes.recipes.add(0, fixedRecipe);
		list.setModel(new RecipeModel());
		repaint();
	}

	public void removeSelected() {
		FixedRecipe selected = getSelected();
		if (selected != null) {
			ElementKeyMap.fixedRecipes.recipes.remove(list.getSelectedIndex());
		}
		list.setModel(new RecipeModel());
		repaint();
	}

	// #RM1958 remove ListCellRenderer generic argument
	private class RecipeCellRenderer implements ListCellRenderer {

		@Override
		public Component getListCellRendererComponent(
				JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, ((FixedRecipe) value).name, index,
					isSelected, cellHasFocus);
			//			JLabel jLabel = new JLabel(value.name);
			//			if(!isSelected){
			//				jLabel.setBackground(new Color(80, 80, 200));
			//			}else{
			//
			//			}
			return renderer;
		}

	}

	// #RM1958 remove ListModel generic argument
	private class RecipeModel implements ListModel {

		final FixedRecipes l;

		public RecipeModel() {
			super();
			FixedRecipes fixedRecipes = ElementKeyMap.fixedRecipes;
			this.l = fixedRecipes;
		}

		@Override
		public int getSize() {
			if (l == null || l.recipes == null) {
				System.err.println("AAA LIST NULL");
				return 0;
			}
			return l.recipes.size();
		}

		@Override
		public FixedRecipe getElementAt(int index) {
			return l.recipes.get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			
		}

	}

}
