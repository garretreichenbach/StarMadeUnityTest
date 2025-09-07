package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class GUIListSorterDropdown<E, O extends Comparator<E>> implements GuiListSorter<E> {
	public final O[] values;
	protected O value;
	private Comparator<GUITileParam<E>> comp;

	public GUIListSorterDropdown(O... values) {
		assert (values != null);
		this.values = values;
		this.comp = (o1, o2) -> value.compare(o1.getUserData(), o2.getUserData());
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GuiListFilter#getFilter()
	 */
	@Override
	public O getSorter() {
		return value;
	}

	public void setSorter(O string) {
		this.value = string;
		this.comp = (o1, o2) -> value.compare(o1.getUserData(), o2.getUserData());
	}

	@Override
	public void sort(List<GUITileParam<E>> tiles) {
		Collections.sort(tiles,comp); 
	}

	
	

}
