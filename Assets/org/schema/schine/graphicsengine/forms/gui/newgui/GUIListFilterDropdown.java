package org.schema.schine.graphicsengine.forms.gui.newgui;

public abstract class GUIListFilterDropdown<E, O> implements GuiListFilter<O, E> {
	public final O[] values;
	protected O value;

	public GUIListFilterDropdown(O... values) {
		assert (values != null);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GuiListFilter#getFilter()
	 */
	@Override
	public O getFilter() {
		return value;
	}

	public void setFilter(O string) {
		this.value = string;
	}

	

}
