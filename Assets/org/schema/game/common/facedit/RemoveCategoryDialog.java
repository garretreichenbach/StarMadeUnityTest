package org.schema.game.common.facedit;

import api.config.BlockConfig;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementKeyMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class RemoveCategoryDialog extends JDialog {
	public RemoveCategoryDialog(final ElementEditorFrame elementEditorFrame) {
		super(elementEditorFrame, "Remove Category", ModalityType.APPLICATION_MODAL);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setResizable(false);
		setSize(300, 100);
		setLocationRelativeTo(elementEditorFrame);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//Add a dropdown to select parent category
		JLabel parentCategoryLabel = new JLabel("Parent Category:");
		add(parentCategoryLabel);
		final JComboBox<String> parentCategoryDropdown = new JComboBox<>();
		HashMap<ElementCategory, String> childCategories = getChildCategoriesRecursive(ElementKeyMap.getCategoryHirarchy(), 0);
		for(ElementCategory category : childCategories.keySet()) parentCategoryDropdown.addItem(childCategories.get(category));
		add(parentCategoryDropdown);
		parentCategoryDropdown.setSelectedIndex(0); //Select the first category by default
		//Add a button to remove category
		JButton createCategoryButton = new JButton("Remove Category");
		createCategoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object obj = parentCategoryDropdown.getSelectedItem();
				if(obj instanceof ElementCategory) {
					ElementCategory category = (ElementCategory) obj;
					if(!BlockConfig.removeCategory(category)) JOptionPane.showMessageDialog(elementEditorFrame, "Category is not empty!", "Error", JOptionPane.ERROR_MESSAGE);
					else {
						elementEditorFrame.reinitializeElements();
						dispose();
					}
				}
			}
		});
		add(createCategoryButton);
	}

	/**
	 * Returns a HashMap of all the child categories of the given category.
	 * <p>The string values are the names of the categories, and are indented according to their depth in the tree.</p>
	 * @return A HashMap of all the child categories of the given category.
	 */
	private HashMap<ElementCategory, String> getChildCategoriesRecursive(ElementCategory category, int depth) {
		HashMap<ElementCategory, String> childCategories = new HashMap<>();
		childCategories.put(category, repeat("  ", depth) + category.getCategory());
		for(ElementCategory childCategory : category.getChildren()) childCategories.putAll(getChildCategoriesRecursive(childCategory, depth + 1));
		return childCategories;
	}

	private String repeat(String string, int count) {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < count; i++) stringBuilder.append(string);
		return stringBuilder.toString();
	}
}
