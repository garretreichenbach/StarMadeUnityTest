package org.schema.schine.tools.gradient;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

//#RM1958 remove ListModel generic argument
public class ColorListModel implements ListModel {

	public ArrayList<ColorAdd> colors = new ArrayList<ColorAdd>();

	@Override
	public int getSize() {
		return colors.size();
	}

	@Override
	public ColorAdd getElementAt(int index) {
		return colors.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {

	}

	@Override
	public void removeListDataListener(ListDataListener l) {

	}

}
