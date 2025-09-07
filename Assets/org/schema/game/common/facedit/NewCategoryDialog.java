package org.schema.game.common.facedit;

import api.config.BlockConfig;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementKeyMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class NewCategoryDialog extends JDialog {
	public NewCategoryDialog(ElementEditorFrame elementEditorFrame) {
		super(elementEditorFrame, "New Category", ModalityType.APPLICATION_MODAL);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setResizable(false);
		setSize(300, 150);
		setLocationRelativeTo(elementEditorFrame);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//Add a dropdown to select parent category
		JLabel parentCategoryLabel = new JLabel("Parent Category:");
		add(parentCategoryLabel);
		final JComboBox<String> parentCategoryDropdown = new JComboBox<>();
		final HashMap<ElementCategory, String> childCategories = getChildCategoriesRecursive(ElementKeyMap.getCategoryHirarchy(), 0);
		for(ElementCategory category : childCategories.keySet()) parentCategoryDropdown.addItem(childCategories.get(category));
		add(parentCategoryDropdown);
		parentCategoryDropdown.setSelectedIndex(0); //Select the first category by default
		//Add an editable text bar to enter category name
		JLabel categoryNameLabel = new JLabel("Category Name:");
		add(categoryNameLabel);
		final JTextField categoryNameField = new JTextField();
		add(categoryNameField);
		//Add a button to create category
		JButton createCategoryButton = new JButton("Create Category");
		createCategoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ElementCategory parentCategory = null;
				for(ElementCategory category : childCategories.keySet()) {
					if(childCategories.get(category).equals(parentCategoryDropdown.getSelectedItem())) {
						parentCategory = category;
						break;
					}
				}
				if(parentCategory != null) {
					BlockConfig.newElementCategory(parentCategory, categoryNameField.getText());
					elementEditorFrame.reinitializeElements();
					dispose();
				}
			}
		});
		add(createCategoryButton);
	}

	private HashMap<ElementCategory, String> getChildCategoriesRecursive(ElementCategory category, int depth) {
		return getChildCategoriesRecursive(category, depth, new HashSet<ElementCategory>());
	}

	private HashMap<ElementCategory, String> getChildCategoriesRecursive(ElementCategory category, int depth, Set<ElementCategory> visited) {
		HashMap<ElementCategory, String> childCategories = new HashMap<>();
		// Prevent infinite recursion by checking if we've already visited this category
		if(category == null || visited.contains(category)) return childCategories;
		visited.add(category);
		ArrayList<ElementCategory> children = BlockConfig.getChildCategoriesRecursive(category);
		for(ElementCategory child : children) {
			// Skip if we've already processed this child to avoid duplicates
			if(visited.contains(child)) continue;
			String name = child.getCategory();
			childCategories.put(child, category.getCategory() + " > " + name);
			HashMap<ElementCategory, String> subChildCategories = getChildCategoriesRecursive(child, depth + 1, visited);
			childCategories.putAll(subChildCategories);
		}
		return childCategories;
	}
}