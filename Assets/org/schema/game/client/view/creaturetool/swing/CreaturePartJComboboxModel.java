package org.schema.game.client.view.creaturetool.swing;

import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.resource.CreatureStructure.PartType;

// #RM1958 remove ComboBoxModel generic argument
public class CreaturePartJComboboxModel implements ComboBoxModel {

	private final PartType type;
	private final List<String> list;
	private String selected;

	public CreaturePartJComboboxModel(PartType type) {
		this.type = type;
		list = Controller.getResLoader().getResourceMap().getType(type);
	}

	@Override
	public int getSize() {
		return list.size() + 1;
	}

	@Override
	public String getElementAt(int index) {
		if (index >= 0) {
			return index == 0 ? "NONE" : list.get(index - 1);
		} else {
			return "-1";
		}
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		
	}

	/**
	 * @return the type
	 */
	public PartType getType() {
		return type;
	}	@Override
	public void setSelectedItem(Object anItem) {
		this.selected = anItem.toString();
	}

	@Override
	public Object getSelectedItem() {
		return this.selected;
	}



}
