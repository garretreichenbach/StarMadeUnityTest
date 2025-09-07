package org.schema.schine.graphicsengine.forms.gui.newgui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Observer;
import java.util.Set;

public class FilterController<E> {

	int filterHeightTop;
	public int filterHeightBottom;
	public GuiListSorter<E> currentSorter;
	private final List<ControllerElement> bottomElements = new ObjectArrayList<ControllerElement>();
	private final List<ControllerElement> topElements = new ObjectArrayList<ControllerElement>();
	private final Set<GuiListFilter<?, ?>> generalListFilter = new ObjectOpenHashSet<GuiListFilter<?, ?>>();
	private final GUIElement g;

	public FilterController(GUIElement g) {
		this.g = g;
	}

	public void addButton(GUICallback callback, String name, ControllerElement.FilterRowStyle mode, ControllerElement.FilterPos pos) {
		addButton(callback, name, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, mode, pos);
	}

	public void addButton(GUICallback callback, String name, GUIHorizontalArea.HButtonType defaultType, ControllerElement.FilterRowStyle mode, ControllerElement.FilterPos pos) {

		GUITextOverlay overlay = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, g.getState());
		int widthText = overlay.getFont().getWidth(name);
		overlay.setTextSimple(name);

		GUIHorizontalArea but = new GUIHorizontalArea(g.getState(), defaultType, 10) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea#draw()
			 */
			@Override
			public void draw() {
				if(leftDependentHalf) {
					setWidth((int) (g.getWidth() / 2));
					getPos().x = 0;
				} else if(rightDependentHalf) {
					setWidth((int) (g.getWidth() / 2));
					getPos().x = (int) (g.getWidth() / 2);

				} else {
					setWidth(g.getWidth());
					getPos().x = 0;
				}
				boolean active = g.isActive();
				GUIHorizontalArea.HButtonType tt = GUIHorizontalArea.HButtonType.getType(defaultType, isInside(), active, false);
				setType(tt);
				overlay.setPos((int) (getWidth() / 2 - widthText / 2), UIScale.getUIScale().inset, 0);
				setMouseUpdateEnabled(active);
				super.draw();
			}

		};
		but.attach(overlay);

		but.setCallback(callback);
		ControllerElement controllerElement = new ControllerElement(but);

		controllerElement.setMode(mode);
		controllerElement.setPos(pos);

		if(pos == ControllerElement.FilterPos.BOTTOM) bottomElements.add(controllerElement);
		else topElements.add(controllerElement);
		if(mode == ControllerElement.FilterRowStyle.LEFT) but.leftDependentHalf = true;
		else if(mode == ControllerElement.FilterRowStyle.RIGHT) but.rightDependentHalf = true;
	}

	public <O> void addDropdownFilter(GUIListFilterDropdown<E, O> guiListFilterDropdown, CreateGUIElementInterface<O> factory, ControllerElement.FilterRowStyle mode) {
		addDropdownFilter(guiListFilterDropdown, factory, mode, ControllerElement.FilterPos.BOTTOM);

	}

	public <O extends Comparator<E> & Serializable> void addDropdownSorter(GUIListSorterDropdown<E, O> guiListFilterDropdown, CreateGUIElementInterface<O> factory, ControllerElement.FilterRowStyle mode, ControllerElement.FilterPos pos) {

		GUIElement[] fields;

		GUIElement neutral = factory.createNeutral();

		if(neutral != null) {
			fields = new GUIElement[guiListFilterDropdown.values.length + 1];
			fields[0] = neutral;
			for(int i = 0; i < guiListFilterDropdown.values.length; i++) {
				O o = guiListFilterDropdown.values[i];
				fields[i + 1] = factory.create(o);
			}
		} else {
			fields = new GUIElement[guiListFilterDropdown.values.length];
			for(int i = 0; i < guiListFilterDropdown.values.length; i++) {
				O o = guiListFilterDropdown.values[i];
				fields[i] = factory.create(o);
			}
		}

		GUIDropDownList l = new GUIDropDownList(g.getState(), 100, UIScale.getUIScale().h, 4 * UIScale.getUIScale().h + UIScale.getUIScale().h / 2, element -> {

			if(element.getContent().getUserPointer() == null) {
				currentSorter = guiListFilterDropdown;
				((Observer) g).update(null, null);
			} else {
				guiListFilterDropdown.setSorter((O) element.getContent().getUserPointer());
				((Observer) g).update(null, null);//flagDirty();
				currentSorter = guiListFilterDropdown;
			}

			//				System.err.println("SELECTTION CHANGEDLKJD: "+element);
		}, fields);
		l.dependend = g;

		if(neutral == null) {
			guiListFilterDropdown.setSorter((O) fields[0].getUserPointer());
		}
		l.onInit();
		ControllerElement bottomElement = new ControllerElement(l);
		bottomElement.setMode(mode);
		bottomElement.setPos(pos);

		if(pos == ControllerElement.FilterPos.BOTTOM) bottomElements.add(bottomElement);
		else topElements.add(bottomElement);
		if(mode == ControllerElement.FilterRowStyle.LEFT) l.leftDependentHalf = true;
		else if(mode == ControllerElement.FilterRowStyle.RIGHT) l.rightDependentHalf = true;
	}

	public <O> void addDropdownFilter(GUIListFilterDropdown<E, O> guiListFilterDropdown, CreateGUIElementInterface<O> factory, ControllerElement.FilterRowStyle mode, ControllerElement.FilterPos pos) {

		GUIElement[] fields;

		GUIElement neutral = factory.createNeutral();

		if(neutral != null) {
			fields = new GUIElement[guiListFilterDropdown.values.length + 1];
			fields[0] = neutral;
			for(int i = 0; i < guiListFilterDropdown.values.length; i++) {
				O o = guiListFilterDropdown.values[i];
				fields[i + 1] = factory.create(o);
			}
		} else {
			fields = new GUIElement[guiListFilterDropdown.values.length];
			for(int i = 0; i < guiListFilterDropdown.values.length; i++) {
				O o = guiListFilterDropdown.values[i];
				fields[i] = factory.create(o);
			}
		}

		GUIDropDownList l = new GUIDropDownList(g.getState(), 100, UIScale.getUIScale().h, 4 * UIScale.getUIScale().h + UIScale.getUIScale().h / 2, element -> {

			if(element.getContent().getUserPointer() == null) {
				generalListFilter.remove(guiListFilterDropdown);
				((GUIChangeListener) g).onChange(false);
			} else {
				guiListFilterDropdown.setFilter((O) element.getContent().getUserPointer());
				((GUIChangeListener) g).onChange(false);
				generalListFilter.add(guiListFilterDropdown);
			}

			//				System.err.println("SELECTTION CHANGEDLKJD: "+element);
		}, fields);
		l.dependend = g;

		if(neutral == null) {
			guiListFilterDropdown.setFilter((O) fields[0].getUserPointer());
		}
		l.onInit();
		ControllerElement bottomElement = new ControllerElement(l);
		bottomElement.setMode(mode);
		bottomElement.setPos(pos);

		if(pos == ControllerElement.FilterPos.BOTTOM) bottomElements.add(bottomElement);
		else topElements.add(bottomElement);
		if(mode == ControllerElement.FilterRowStyle.LEFT) l.leftDependentHalf = true;
		else if(mode == ControllerElement.FilterRowStyle.RIGHT) l.rightDependentHalf = true;
	}

	public void addTextFilter(GUIListFilterText<E> filter, String inactiveText, ControllerElement.FilterRowStyle mode) {
		addTextFilter(filter, inactiveText, mode, ControllerElement.FilterPos.BOTTOM);
	}

	public void addTextFilter(GUIListFilterText<E> filter, String inactiveText, ControllerElement.FilterRowStyle mode, ControllerElement.FilterPos pos) {
		GUISearchBar guiSearchBar = new GUISearchBar(g.getState(), inactiveText != null ? inactiveText : "SEARCH", g, new TextCallback() {
			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void newLine() {
			}
		}, t -> {
			if(t.trim().isEmpty()) {
				generalListFilter.remove(filter);
				((GUIChangeListener) g).onChange(false);
			} else {
				generalListFilter.add(filter);
				filter.setFilter(t);
				((GUIChangeListener) g).onChange(false);
			}
			g.update(null);
			return t;
		});

		if(mode == ControllerElement.FilterRowStyle.LEFT) guiSearchBar.leftDependentHalf = true;
		else if(mode == ControllerElement.FilterRowStyle.RIGHT) guiSearchBar.rightDependentHalf = true;
		guiSearchBar.onInit();
		ControllerElement bottomElement = new ControllerElement(guiSearchBar);
		bottomElement.setMode(mode);
		bottomElement.setPos(pos);
		if(pos == ControllerElement.FilterPos.BOTTOM) bottomElements.add(bottomElement);
		else topElements.add(bottomElement);
	}

	public void addTextFilter(GUIListFilterText<E> filter, ControllerElement.FilterRowStyle mode) {
		addTextFilter(filter, Lng.str("SEARCH"), mode);
	}

	public boolean isFiltered(E e) {
		for(GuiListFilter a : generalListFilter) {
			if(!a.isOk(a.getFilter(), e)) {
				return true;
			}
		}
		return false;

	}

	public void calcInit() {
		filterHeightBottom = 0;
		filterHeightTop = 0;
		for(ControllerElement bottomElement : bottomElements) {
			if(bottomElement.getMode() != ControllerElement.FilterRowStyle.LEFT) filterHeightBottom += bottomElement.gui.getHeight();
		}
		for(ControllerElement topElement : topElements) {
			if(topElement.getMode() != ControllerElement.FilterRowStyle.LEFT) filterHeightTop += topElement.gui.getHeight();
		}
	}

	public void drawTop(int filterPosY) {
		ControllerElement.drawFilterElements(false, filterPosY, topElements);
		ControllerElement.drawFilterElements(true, filterPosY, topElements);
		GlUtil.glTranslatef(0, filterHeightTop, 0);
	}

	public void drawContent(GUIScrollablePanel scrollPanel, int columnHeight) {

		GlUtil.glTranslatef(0, -filterHeightTop, 0);
		scrollPanel.dependendHeightDiff = -(columnHeight + filterHeightBottom + filterHeightTop);
		scrollPanel.setPos(0, columnHeight + filterHeightTop, 0);
		scrollPanel.draw();
	}

	public void drawBottom(GUIScrollablePanel scrollPanel, int columnHeight) {
		ControllerElement.drawFilterElements(false, (int) scrollPanel.getHeight() + columnHeight + filterHeightTop, bottomElements);
		ControllerElement.drawFilterElements(true, (int) scrollPanel.getHeight() + columnHeight + filterHeightTop, bottomElements);
	}
}
