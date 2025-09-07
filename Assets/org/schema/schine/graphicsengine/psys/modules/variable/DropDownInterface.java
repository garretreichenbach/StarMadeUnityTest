package org.schema.schine.graphicsengine.psys.modules.variable;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public abstract class DropDownInterface implements ComboBoxModel {
	// #RM1958 removed java generics on ComboBoxModel
	private StringPair[] values;
	private Object selected;

	public DropDownInterface(StringPair... values) {
		this.values = values;
	}

	@Override
	public int getSize() {
		return values.length;
	}

	@Override
	public StringPair getElementAt(int index) {
		return values[index];
	}

	@Override
	public void addListDataListener(ListDataListener l) {

	}

	@Override
	public void removeListDataListener(ListDataListener l) {

	}

	public abstract String getName();

	public abstract int getCurrentIndex();	@Override
	public void setSelectedItem(Object anItem) {
		selected = anItem;
	}

	public abstract void set(StringPair selectedItem);

	@Override
	public Object getSelectedItem() {
		return selected;
	}



}
