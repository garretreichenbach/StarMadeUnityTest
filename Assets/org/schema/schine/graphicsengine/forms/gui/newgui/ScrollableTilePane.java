package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class ScrollableTilePane<E> extends GUIElement implements Observer {

	private final FilterController<E> filterController;

	protected Set<E> toAddTmp = new ObjectOpenHashSet<E>();

	private boolean dirty = true;

	private GUIScrollablePanel scrollPanel;

	private GUIElement dependend;

	private GUITilePane<E> tiles;

	boolean sortedYet;

	private boolean init;

	protected final int tileWidth;

	protected final int tileHeight;

	private boolean usedClose;

	public ScrollableTilePane(InputState state, GUIElement dependent, int tileWidth, int tileHeight) {
		super(state);
		this.dependend = dependent;
		filterController = new FilterController<E>(this);
		scrollPanel = new GUIScrollablePanel(100f, 100f, dependent, state) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
			 */
			@Override
			public boolean isActive() {
				return ScrollableTilePane.this.isActive();
			}
		};
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		tiles = new GUITilePane<E>(state, dependent, tileWidth, tileHeight);
		tiles.scrollPanel = scrollPanel;
	}

	protected void addTile(final E e, GUIElement element, boolean addCloseCross) {
		final GUITileParam<E> tile = new GUITileParam<E>(getState(), tileWidth, tileHeight, e);
		tile.activationInterface = ScrollableTilePane.this::isActive;
		element.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
		tile.attach(element);
		if (addCloseCross) {
			usedClose = true;
			GUIOverlay cross = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-"), getState());
			cross.setPos(tileWidth - (cross.getWidth() + 1), 1, 0);
			cross.setCallback(new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !ScrollableTilePane.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
						AudioController.fireAudioEventID(29);
						onClosePressed(tile, e);
					}
				}
			});
			tile.attach(cross);
		}
		tiles.addTile(tile, e);
	}

	public void onClosePressed(GUITileParam<E> tile, E e) {
		assert (!usedClose) : "onClosePressed() has to be overwritten if close crosses are used";
	}

	public abstract void updateListEntries(GUITilePane<E> mainList, Set<E> collection);

	public void flagDirty() {
		dirty = true;
	}

	public void clear() {
		tiles.clear();
		dirty = true;
	}

	@Override
	public void update(Observable o, Object arg) {
		this.dirty = true;
	}

	@Override
	public void cleanUp() {
		clear();
	}

	public void addBottomButton(final GUICallback callback, String name, FilterRowStyle mode, FilterPos pos) {
		filterController.addButton(callback, name, mode, pos);
	}

	public <O> void addDropdownFilter(final GUIListFilterDropdown<E, O> guiListFilterDropdown, CreateGUIElementInterface<O> factory, FilterRowStyle mode) {
		filterController.addDropdownFilter(guiListFilterDropdown, factory, mode);
	}

	public <O> void addDropdownFilter(final GUIListFilterDropdown<E, O> guiListFilterDropdown, CreateGUIElementInterface<O> factory, FilterRowStyle mode, FilterPos pos) {
		filterController.addDropdownFilter(guiListFilterDropdown, factory, mode, pos);
	}

	public void addTextFilter(final GUIListFilterText<E> filter, String inactiveText, FilterRowStyle mode) {
		filterController.addTextFilter(filter, inactiveText, mode);
	}

	public void addTextFilter(final GUIListFilterText<E> filter, String inactiveText, FilterRowStyle mode, FilterPos pos) {
		filterController.addTextFilter(filter, inactiveText, mode, pos);
	}

	public void addTextFilter(final GUIListFilterText<E> filter, FilterRowStyle mode) {
		filterController.addTextFilter(filter, mode);
	}

	protected boolean isFiltered(E e) {
		return filterController.isFiltered(e);
	}

	private void handleDirty() {
		if (dirty) {
			toAddTmp.clear();
			Collection<E> elementList = getElementList();
			toAddTmp.addAll(elementList);
			for (int i = 0; i < tiles.getTiles().size(); i++) {
				GUITileParam<E> elem = tiles.getTiles().get(i);
				assert (elem.getUserData() != null);
				E e = elem.getUserData();
				boolean containsThisElement = toAddTmp.remove(e);
				if (isFiltered(e) || !containsThisElement) {
					tiles.getTiles().remove(i);
					i--;
				}
			}
			Iterator<E> it = toAddTmp.iterator();
			while (it.hasNext()) {
				if (isFiltered(it.next())) {
					it.remove();
				}
			}
			if (!sortedYet && filterController.currentSorter != null) {
				filterController.currentSorter.sort(tiles.getTiles());
				sortedYet = true;
			}
			updateListEntries(tiles, toAddTmp);
			// if (activeSortColumnIndex >= 0) {
			// columns.get(activeSortColumnIndex).sort();
			// }
			if (filterController.currentSorter != null) {
				filterController.currentSorter.sort(tiles.getTiles());
			}
			dirty = false;
		}
	}

	protected abstract Collection<E> getElementList();

	@Override
	public void onInit() {
		scrollPanel.setContent(tiles);
		filterController.calcInit();
		init = true;
	}

	@Override
	public float getHeight() {
		return filterController.filterHeightBottom + scrollPanel.getHeight();
	}

	@Override
	public void draw() {
		assert (init);
		handleDirty();
		GlUtil.glPushMatrix();
		transform();
		int columnHeight = 0;
		filterController.drawTop(0);
		filterController.drawContent(scrollPanel, columnHeight);
		filterController.drawBottom(scrollPanel, columnHeight);
		for (AbstractSceneNode e : getChilds()) {
			e.draw();
		}
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public float getWidth() {
		return scrollPanel.getWidth();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return dependend.isActive();
	}
}
