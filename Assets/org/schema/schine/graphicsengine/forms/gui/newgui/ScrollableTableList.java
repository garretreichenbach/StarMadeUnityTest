package org.schema.schine.graphicsengine.forms.gui.newgui;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.util.CompareTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ListColorPalette;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;
import java.util.*;

public abstract class ScrollableTableList<E> extends GUIElement implements GUIChangeListener {
	protected final ObjectArrayList<Column> columns = new ObjectArrayList<Column>();

	// new Vector4f(0.2f, 0.2f, 0.2f, 1);
	protected final Vector4f colorRowHighlight = ListColorPalette.selectedColor;

	// new Vector4f(0.1f, 0.1f, 0.1f, 0);
	private final Vector4f colorRowA = ListColorPalette.mainListBackgroundColor;

	// new Vector4f(0.08f, 0.08f, 0.08f, 1);
	private final Vector4f colorRowB = ListColorPalette.mainListBackgroundColorAlternate;

	private Vector4f draggingBGColor = new Vector4f(0.3f, 0.5f, 0.123f, 1f);

	public int activeSortColumnIndex = -1;

	protected int continousSortColumn = -1;

	private int defaultColumnsHeight = UIScale.getUIScale().TABLE_defaultColumnsHeight;

	protected Set<E> toAddTmp = new ObjectOpenHashSet<E>();

	private GUIScrollablePanel scrollPanel;

	private GUIElementList mainList;

	private boolean dirty = true;

	private GUIElement dependent;

	private int columnWeightedColumnsCount;

	private float columnTotalWeightParts;

	private float columnTotalFixedParts;

	private boolean columnsWidthChanged = true;

	private int columnFixedColumnsCount;

	private final FilterController<E> filterController;

	private ScrollableTableList<E>.Row selectedRow;

	private boolean sortedYet;

	protected ScrollableTableList<E>.Column defaultSortByColumn;

	private boolean init;

	public SelectedObjectCallback selCallback;

	public ScrollableTableList<E>.Row extendedRow;

	public static FontInterface tableFontInterface = FontSize.MEDIUM_15;

	public static FontInterface dropdownFontInterface = FontSize.MEDIUM_15;

	public static FontInterface innerFontInterface = FontSize.SMALL_14;

	public ScrollableTableList<E>.GUIClippedRow getSimpleRow(Object txt, GUIActiveInterface active) {
		GUIClippedRow contP = new GUIClippedRow(getState());
		contP.activationInterface = active;
		GUITextOverlayTable contCont = new GUITextOverlayTable(getState());
		contCont.setTextSimple(txt);
		contP.attach(contCont);
		return contP;
	}

	public ScrollableTableList<E>.GUIClippedRow getSimpleRow(Object txt) {
		GUIClippedRow contP = new GUIClippedRow(getState());
		GUITextOverlayTable contCont = new GUITextOverlayTable(getState());
		contCont.setTextSimple(txt);
		contP.attach(contCont);
		return contP;
	}

	public class Seperator extends GUIListElement implements Comparable<Seperator> {

		public Seperator(InputState state, int sortIndex) {
			super(state);
			this.sortIndex = sortIndex;
		}

		public String name = "";

		public Vector4f color = new Vector4f();

		public Vector4f colorText = new Vector4f(0.1f, 0.8f, 0.8f, 1);

		private GUIColoredRectangle bg;

		public final int sortIndex;

		public boolean sortByName = false;

		private GUITextOverlay septext;

		@Override
		public void onInit() {
			this.bg = new GUIColoredRectangle(getState(), 0, getSepheight(), color);
			this.bg.renderMode = RENDER_MODE_SHADOW;
			bg.setWidth(ScrollableTableList.this.getWidth() - 4);
			bg.setPos(2, 0, 0);
			bg.setColor(color);
			this.content = this.bg;
			this.selectContent = this.bg;
			this.septext = new GUITextOverlay(FontSize.BIG_30, getState());
			septext.setTextSimple(name);
			septext.getPos().set(12, 6, 0);
			septext.setColor(colorText);
			bg.attach(septext);
		}

		public int compareTo(Seperator sep) {
			if (sortByName) {
				return name.compareTo(sep.name);
			}
			return CompareTools.compare(sortIndex, sep.sortIndex);
		}

		public void draw() {
			bg.setWidth(ScrollableTableList.this.getWidth() - 4);
			bg.setHeight(getSepheight());
			super.draw();
		}

		public int getSepheight() {
			return getDefaultColumnsHeight() + 15;
		}
	}

	private final Object2ObjectOpenHashMap<String, Seperator> seperators = new Object2ObjectOpenHashMap<String, Seperator>();

	public Seperator getSeperator(String name, int sortIndex) {
		String ind = name + sortIndex;
		Seperator s = seperators.get(ind);
		if (s == null) {
			s = new Seperator(getState(), sortIndex);
			s.name = name;
			s.onInit();
			ScrollableTableList<E>.Seperator put = seperators.put(ind, s);
			assert (put == null);
		}
		return s;
	}

	public ScrollableTableList(InputState state, float width, float height, GUIElement p) {
		super(state);
		this.dependent = p;
		filterController = new FilterController<E>(this);
		scrollPanel = new GUIScrollablePanel(width, height - filterController.filterHeightBottom, p, state) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
			 */
			@Override
			public boolean isActive() {
				return ScrollableTableList.this.isActive();
			}
		};
	}

	@Override
	public void cleanUp() {
		mainList.clear();
	}

	public void clear() {
		mainList.clear();
		flagDirty();
	}

	public void handleDirty() {
		if (dirty) {
			toAddTmp.clear();
			Collection<E> elementList = getElementList();
			assert (noneNull(elementList));
			toAddTmp.addAll(elementList);
			for (int i = 0; i < mainList.size(); i++) {
				GUIListElement elem = mainList.get(i);
				if (elem instanceof ScrollableTableList.Seperator) {
					mainList.removeWithoutUpdate(i);
					i--;
					continue;
				}
				E e = ((Row) elem).getSort();
				boolean containsThisElement = toAddTmp.remove(e);
				if (isFiltered(e) || !containsThisElement) {
					GUIListElement remove = mainList.removeWithoutUpdate(i);
					if (remove != null) {
						remove.cleanUp();
					}
					i--;
				}
			}
			Iterator<E> it = toAddTmp.iterator();
			while (it.hasNext()) {
				if (isFiltered(it.next())) {
					it.remove();
				}
			}
			if (!sortedYet && defaultSortByColumn != null) {
				defaultSortByColumn.sortBy(true);
				sortedYet = true;
			}
			seperators.clear();
			updateListEntries(mainList, toAddTmp);
			mainList.updateDim();
			if (activeSortColumnIndex >= 0) {
				columns.get(activeSortColumnIndex).sort();
				insertSeperators();
			}
			dirty = false;
		}
	}

	@Override
	public void draw() {
		assert (init);
		handleDirty();
		if (continousSortColumn >= 0 && continousSortColumn == activeSortColumnIndex) {
			columns.get(activeSortColumnIndex).sort();
		}
		GlUtil.glPushMatrix();
		transform();
		filterController.drawTop(0);
		int from = 0;
		for (int i = 0; i < columns.size(); i++) {
			int w = columns.get(i).draw(from);
			columns.get(i).seperator.setHeight((int) mainList.getHeight());
			from += w;
		}
		filterController.drawContent(scrollPanel, UIScale.getUIScale().TABLE_HEADER_HEIGHT);
		filterController.drawBottom(scrollPanel, UIScale.getUIScale().TABLE_FOOTER_HEIGHT);
		for (AbstractSceneNode e : getChilds()) {
			e.draw();
		}
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		GlUtil.glPopMatrix();
		columnsWidthChanged = false;
	}

	private boolean noneNull(Collection<E> elementList) {
		for (E e : elementList) {
			if (e == null) {
				System.err.println("ERROR: " + elementList);
				return false;
			}
		}
		return true;
	}

	@Override
	public void onInit() {
		initColumns();
		columns.trim();
		this.columnTotalFixedParts = 0;
		this.columnTotalWeightParts = 0;
		this.columnFixedColumnsCount = 0;
		this.columnWeightedColumnsCount = 0;
		for (int i = 0; i < columns.size(); i++) {
			if (columns.get(i).fixedSize >= 0) {
				this.columnTotalFixedParts += columns.get(i).fixedSize;
				this.columnFixedColumnsCount++;
			} else {
				this.columnTotalWeightParts += (1 + columns.get(i).weigth);
				this.columnWeightedColumnsCount++;
			}
		}
		for (int i = 0; i < columns.size(); i++) {
			columns.get(i).init(getState());
		}
		mainList = new GUIElementList(getState());
		scrollPanel.setContent(mainList);
		mainList.setScrollPane(scrollPanel);
		for (int i = 0; i < columns.size(); i++) {
			Sprite sprite = Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 8px Vertical-32x1-gui-");
			float p = (1f / (sprite.getMultiSpriteMaxX()));
			float px = 1f / sprite.getMaterial().getTexture().getWidth();
			GUITexDrawableArea seperator = new GUITexDrawableArea(getState(), sprite.getMaterial().getTexture(), 10f * p + 2f * px, 0.0f);
			seperator.setColor(new Vector4f(1, 1, 1, 1));
			seperator.setWidth(2);
			seperator.renderMode = RENDER_MODE_SHADOW;
			columns.get(i).seperator = seperator;
			if (i > 0) {
				mainList.attach(seperator);
			}
		}
		filterController.calcInit();
		init = true;
	}

	public abstract void initColumns();

	protected ScrollableTableList<E>.Column addColumn(String columnName, float weigth, Comparator<E> comapareOn) {
		return addColumn(columnName, weigth, comapareOn, columns.isEmpty());
	}

	protected ScrollableTableList<E>.Column addColumn(String columnName, float weigth, Comparator<E> comapareOn, boolean sortByDef) {
		Column c = new Column(columnName, weigth, columns.size(), comapareOn);
		columns.add(c);
		if (sortByDef) {
			defaultSortByColumn = c;
		}
		return c;
	}

	protected ScrollableTableList<E>.Column addFixedWidthColumnScaledUI(String columnName, int fixedWidth, Comparator<E> comapareOn) {
		return addFixedWidthColumnScaledUI(columnName, fixedWidth, comapareOn, columns.isEmpty());
	}

	protected ScrollableTableList<E>.Column addFixedWidthColumnScaledUI(String columnName, int fixedWidth, Comparator<E> comapareOn, boolean sortByDef) {
		Column c = new Column(columnName, 0, columns.size(), comapareOn);
		c.fixedSize = UIScale.getUIScale().scale(fixedWidth);
		columns.add(c);
		if (sortByDef) {
			defaultSortByColumn = c;
		}
		return c;
	}

	protected abstract Collection<E> getElementList();

	public void flagDirty() {
		dirty = true;
	}

	public abstract void updateListEntries(GUIElementList mainList, Set<E> collection);

	@Override
	public float getHeight() {
		return filterController.filterHeightBottom + scrollPanel.getHeight();
	}

	public float getContentHeight() {
		return scrollPanel.getHeight();
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
		return dependent.isActive();
	}

	@Override
	public void onChange(boolean updateListDim) {
		if(updateListDim) mainList.updateDim();
		flagDirty();
	}

	public void addButton(final GUICallback callback, String name, FilterRowStyle mode, FilterPos pos) {
		filterController.addButton(callback, name, mode, pos);
	}

	public void addButton(final GUICallback callback, String name, HButtonType type, FilterRowStyle mode, FilterPos pos) {
		filterController.addButton(callback, name, type, mode, pos);
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

	public ScrollableTableList<E>.Row getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(ScrollableTableList<E>.Row selectedRow) {
		this.selectedRow = selectedRow;
		if (selectedRow != null) {
			selectedRow.lastClick = System.currentTimeMillis();
			if (selCallback != null) {
				selCallback.onSelected(selectedRow.f);
			}
		}
	}

	public class GUIClippedRow extends GUIAnchor {

		private int column;

		public ScrollableTableList<E>.Row row;

		public GUIClippedRow(InputState state) {
			super(state, 10, 10);
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
		 */
		@Override
		public void draw() {
			setWidth(columns.get(column).bg.getWidth() - 8);
			setHeight(row.getColumnsHeight());
			super.draw();
		}

		public GUIScrollablePanel getClippedPane(int index) {
			this.column = index;
			GUIScrollablePanel guiScrollablePanel = new GUIScrollablePanel(1, 1, this, getState());
			guiScrollablePanel.setContent(this);
			guiScrollablePanel.setLeftRightClipOnly = true;
			return guiScrollablePanel;
		}
	}

	public abstract class Row extends GUIListElement implements GUIEnterableListOnExtendedCallback, Draggable, DropTarget<Row> {

		public Seperator seperator;

		protected boolean rightClickSelectsToo;

		public long lastClick;

		public GUIElementList expanded;

		protected boolean draggable;

		public GUIColoredAnchor bg;

		public GUIColoredAnchor bgHightlight;

		public GUIEnterableList l;

		public GUIEnterableListBlockedInterface extendableBlockedInterface;

		public GUIEnterableListOnExtendedCallback onExpanded;

		protected boolean highlightSelect = false;

		protected boolean highlightSelectSimple = false;

		protected int customColumnHeightExpanded = -1;

		private final GUIElement[] elements;

		private boolean init = false;

		private boolean onInitCalled;

		public E f;

		private boolean allwaysOneSelected;

		public final GUIResizableElement[] useColumnWidthElements;

		private int dPosX;

		private int dPosY;

		private long tStartDrag;

		protected boolean isDragStartOk() {
			return true;
		}

		public Row(InputState state, E userData, GUIElement... elements) {
			super(state);
			this.f = userData;
			assert (elements.length == columns.size());
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof ScrollableTableList.GUIClippedRow) {
					// #RM1958 build script fix: remove generic argument
					((ScrollableTableList.GUIClippedRow) elements[i]).row = this;
					elements[i] = ((ScrollableTableList<?>.GUIClippedRow) elements[i]).getClippedPane(i);
				}
			}
			this.elements = elements;
			this.useColumnWidthElements = new GUIResizableElement[elements.length];
			assert (checkTexts());
		}

		@Override
		public void extended() {
			if (allwaysOneSelected) {
				if (extendedRow != null && extendedRow != this) {
					extendedRow.unexpend();
				}
				extendedRow = (this);
			}
		}

		@Override
		public void collapsed() {
			if (allwaysOneSelected) {
				if (extendedRow == this) {
					extendedRow = null;
				}
			}
		}

		private boolean checkTexts() {
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof GUITextOverlay) {
					assert (!((GUITextOverlay) elements[i]).getText().isEmpty()) : "at column " + i;
					for (Object s : ((GUITextOverlay) elements[i]).getText()) {
						assert (s != null) : "at column " + i;
						assert (s.toString() != null) : "at column " + i;
					}
				}
			}
			return true;
		}

		protected boolean isSimpleSelected() {
			return ScrollableTableList.this.getSelectedRow() == this;
		}

		protected GUIContextPane createContext() {
			return null;
		}

		protected void clickedOnRow() {
			if (highlightSelectSimple) {
				if (isSimpleSelected()) {
					if (!allwaysOneSelected) {
						ScrollableTableList.this.setSelectedRow(null);
					} else {
						if (getState().getUpdateTime() - lastClick < 300) {
							if (selCallback != null) {
								selCallback.onSelected(f);
							}
							onDoubleClick();
							if (selCallback != null) {
								selCallback.onDoubleClick(f);
							}
						}
						lastClick = getState().getUpdateTime();
					}
				} else {
					ScrollableTableList.this.setSelectedRow(this);
				}
			} else {
			}
		}

		public void unexpend() {
			if (l != null) {
				l.setExpanded(false);
			}
		}

		private void adaptWidths() {
			bg.setWidth(ScrollableTableList.this.getWidth() - 4);
			bg.setPos(2, 0, 0);
			if (this.bgHightlight != null) {
				this.bgHightlight.setWidth(ScrollableTableList.this.getWidth());
			}
			int leftInset = 4;
			for (int i = 0; i < columns.size(); i++) {
				this.elements[i].getPos().x = (leftInset) + columns.get(i).getPosX();
			}
			for (int i = 0; i < columns.size(); i++) {
				if (this.useColumnWidthElements[i] != null) {
					if (i < columns.size() - 1) {
						this.useColumnWidthElements[i].setWidth(this.elements[i + 1].getPos().x - this.elements[i].getPos().x);
					} else {
						this.useColumnWidthElements[i].setWidth(getWidth() - this.elements[i].getPos().x);
					}
				}
			}
		}

		public final E getSort() {
			return f;
		}

		public Vector4f[] getCustomRowColors() {
			return null;
		}

		@Override
		public void draw() {
			assert (onInitCalled);
			bg.setHeight(getColumnsHeight());
			if (bgHightlight != null) {
				bgHightlight.setHeight(getColumnsHeight());
			}
			Vector4f[] customRowColors = getCustomRowColors();
			if (draggable && getState().getController().getInputController().getDragging() == this) {
				bg.getColor().set(draggingBGColor);
			} else if (draggable && getState().getController().getInputController().getDragging() != null && bg.isInside()) {
				bg.getColor().set(colorRowHighlight);
			} else if (isSimpleSelected()) {
				bg.getColor().set(colorRowHighlight);
			} else {
				if (customRowColors != null) {
					bg.getColor().set(currentIndex % 2 == 0 ? customRowColors[0] : customRowColors[1]);
				} else {
					bg.getColor().set(currentIndex % 2 == 0 ? colorRowA : colorRowB);
				}
			}
			if (columnsWidthChanged || !init || bg.getWidth() != ScrollableTableList.this.getWidth()) {
				adaptWidths();
				init = true;
			}
			if (customRowColors != null && l != null) {
				if (this.bgHightlight != null) {
					this.bgHightlight.setColor(customRowColors[2]);
				}
				l.expandedBackgroundColor = customRowColors[2];
			} else {
				if (this.bgHightlight != null) {
					this.bgHightlight.setColor(colorRowHighlight);
				}
				if (l != null) {
					l.expandedBackgroundColor = colorRowHighlight;
				}
			}
			super.draw();
		}

		protected int getColumnsHeight() {
			return getDefaultColumnsHeight();
		}

		@Override
		public void onInit() {
			if (expanded != null) {
				this.bg = new GUIColoredRectangle(getState(), 0, getColumnsHeight(), new Vector4f());
				this.bg.renderMode = RENDER_MODE_SHADOW;
				if (highlightSelect) {
					this.bgHightlight = new GUIColoredRectangleLeftRightShadow(getState(), 0, customColumnHeightExpanded > 0 ? customColumnHeightExpanded : getColumnsHeight(), colorRowHighlight);
					// this.bgHightlight.renderMode = RENDER_MODE_SHADOW;
					for (int i = 0; i < columns.size(); i++) {
						this.bgHightlight.attach(this.elements[i]);
					}
				} else {
					this.bgHightlight = bg;
				}
				for (int i = 0; i < columns.size(); i++) {
					this.bg.attach(this.elements[i]);
				}
				l = new GUIEnterableList(getState(), expanded, this.bg, this.bgHightlight);
				l.scrollPanel = scrollPanel;
				l.expandedBackgroundColor = colorRowHighlight;
				l.extendedCallback = onExpanded;
				l.extendedCallbackSelector = this;
				l.extendableBlockedInterface = extendableBlockedInterface;
				l.addObserver(ScrollableTableList.this);
				this.content = l;
				this.selectContent = l;
				l.onInit();
				l.switchCollapsed(false);
			} else {
				this.bg = new GUIColoredRectangle(getState(), 0, getColumnsHeight(), new Vector4f());
				this.bg.renderMode = RENDER_MODE_SHADOW;
				bg.setMouseUpdateEnabled(true);
				bg.setCallback(new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (draggable && event.pressedLeftMouse() && isDragStartOk()) {
							getState().getController().getInputController().setDragging(Row.this);
						}
						if (event.pressedLeftMouse()) {
							clickedOnRow();
						} else if (event.pressedRightMouse()) {
							if (rightClickSelectsToo) {
								clickedOnRow();
							}
							System.err.println("[CLIENT] OPENING CONTEXT MENU");
							getState().getController().getInputController().setCurrentContextPane(createContext());
						}
						checkTarget(event);
					}

					@Override
					public boolean isOccluded() {
						return !ScrollableTableList.this.isActive();
					}
				});
				for (int i = 0; i < columns.size(); i++) {
					this.bg.attach(this.elements[i]);
				}
				this.content = this.bg;
				this.selectContent = this.bg;
			}
			super.onInit();
			onInitCalled = true;
		}

		@Override
		public boolean checkDragReleasedMouseEvent(MouseEvent e) {
			return (e.releasedLeftMouse()) || (e.pressedLeftMouse() && getState().getController().getInputController().getDragging().isStickyDrag());
		}

		@Override
		public int getDragPosX() {
			return dPosX;
		}

		@Override
		public void setDragPosX(int dragPosX) {
			dPosX = dragPosX;
		}

		@Override
		public int getDragPosY() {
			return dPosY;
		}

		@Override
		public void setDragPosY(int dragPosY) {
			dPosY = dragPosY;
		}

		@Override
		public Object getPlayload() {
			return null;
		}

		@Override
		public long getTimeDragStarted() {
			return tStartDrag;
		}

		@Override
		public boolean isStickyDrag() {
			return false;
		}

		@Override
		public void setStickyDrag(boolean b) {
		}

		@Override
		public void setTimeDraggingStart(long currentTimeMillis) {
			tStartDrag = currentTimeMillis;
		}

		@Override
		public void reset() {
		// from Draggable
		}

		@Override
		public short getType() {
			return -1;
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			// from Draggable
			checkTarget(event);
		}

		@Override
		public void checkTarget(MouseEvent e) {
			if (getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging().checkDragReleasedMouseEvent(e) && (getState().getController().getInputController().getDragging() instanceof ScrollableTableList<?>.Row)) {
				onDrop((ScrollableTableList<E>.Row) getState().getController().getInputController().getDragging());
				getState().getController().getInputController().getDragging().reset();
				getState().getController().getInputController().setDragging(null);
			}
		}

		@Override
		public boolean isTarget(Draggable draggable) {
			return draggable != null;
		}

		@Override
		public void onDrop(ScrollableTableList<E>.Row draggable) {
		}

		@Override
		public boolean isOccluded() {
			return false;
		}

		public int compareTo(int index, Row row) {
			return columns.get(index).comp(this.f, row.f);
		}

		public void onDoubleClick() {
			assert (allwaysOneSelected);
		}

		public boolean isAllwaysOneSelected() {
			return allwaysOneSelected;
		}

		public void setAllwaysOneSelected(boolean allwaysOneSelected) {
			this.allwaysOneSelected = allwaysOneSelected;
		}
	}

	private void insertSeperators() {
		// remove and reinsert seperator (since they always have to be on top of the category no matter the sort)
		if (ScrollableTableList.this.seperators.size() > 0) {
			for (int i = 0; i < mainList.size(); i++) {
				GUIListElement l = mainList.get(i);
				if (l instanceof ScrollableTableList.Seperator) {
					mainList.removeWithoutUpdate(i);
					i--;
				}
			}
			for (Seperator s : seperators.values()) {
				for (int i = 0; i < mainList.size(); i++) {
					if (mainList.get(i) instanceof ScrollableTableList.Row) {
						Row l = (Row) mainList.get(i);
						if (l.seperator.sortIndex == s.sortIndex) {
							assert (!mainList.contains(s));
							mainList.addWithoutUpdate(i, s);
							break;
						}
					}
				}
			}
			mainList.updateDim();
		}
	}

	public int getDefaultColumnsHeight() {
		return this.defaultColumnsHeight;
	}

	protected void setColumnsHeight(int defaultColumnsHeight) {
		this.defaultColumnsHeight = defaultColumnsHeight;
	}

	public class Column implements GUICallback, Comparator<E> {

		public GUIHorizontalArea bg;

		public GUITexDrawableArea seperator;

		String columnName;

		float weigth;

		int fixedSize = -1;

		private int textWidth;

		private int index;

		private GUITextOverlay text;

		private Comparator<GUIListElement> comparator;

		private Comparator<E> comp;

		public Column(String columnName, float weigth, int index, Comparator<E> comp) {
			super();
			this.columnName = columnName;
			this.weigth = weigth;
			this.index = index;
			this.comp = comp;
		}

		private int comp(Object a, Object b) {
			return compare((E) a, (E) b);
		}

		@Override
		public int compare(E a, E b) {
			return comp.compare(a, b);
		}

		public void init(InputState state) {
			bg = new GUIHorizontalArea(state, HButtonType.BUTTON_GREY_DARK, 10);
			bg.onInit();
			bg.setMouseUpdateEnabled(true);
			bg.setCallback(this);
			GUIScrollablePanel lr = new GUIScrollablePanel(bg.getWidth(), bg.getHeight(), bg, state);
			lr.setScrollable(0);
			lr.setLeftRightClipOnly = true;
			text = new GUITextOverlay(FontSize.MEDIUM_15, state);
			text.setTextSimple(columnName);
			text.onInit();
			lr.setContent(text);
			this.textWidth = text.getFont().getWidth(columnName);
			bg.attach(lr);
			this.comparator = getComparator();
			// reverse order right away so when its reversed again on first click on column it will be the original
			comparator = Collections.reverseOrder(comparator);
		}

		public Comparator<GUIListElement> getComparator() {
			return (o1, o2) -> {
				if (o1 instanceof ScrollableTableList.Seperator || o2 instanceof ScrollableTableList.Seperator) {
					return 0;
				}
				return ((Row) o1).compareTo(index, ((Row) o2));
			};
		}

		private float getPercentWidth() {
			return (1 + weigth) / columnTotalWeightParts;
		}

		public int draw(int from) {
			int width;
			if (fixedSize >= 0) {
				width = (fixedSize);
			} else {
				width = (int) (getPercentWidth() * (getWidth() - columnTotalFixedParts));
			}
			if (index == columns.size() - 1) {
				int diff = (int) (getWidth() - (width + from));
				width += diff;
			}
			// System.err.println("WI: "+columnName+"; "+width);
			if (bg.getWidth() != width) {
				columnsWidthChanged = true;
			}
			bg.setWidth(width);
			bg.setPos(from, 0, 0);
			text.setPos(width / 2 - textWidth / 2, 6, 0);
			if (index > 0) {
				seperator.setPos(from - 2, 0, 0);
			}
			bg.draw();
			return width;
		}

		CComp ccomp = new CComp();

		private void sort() {
			Collections.sort(mainList, ccomp);
			insertSeperators();
		}

		private class CComp implements Comparator<GUIListElement> {

			@Override
			public int compare(GUIListElement o1, GUIListElement o2) {
				if (o1 instanceof ScrollableTableList.Row && o2 instanceof ScrollableTableList.Row) {
					Row r1 = (Row) o1;
					Row r2 = (Row) o2;
					if (r1.seperator != null && r2.seperator == null) {
						return -1;
					} else if (r1.seperator == null && r2.seperator != null) {
						return 1;
					} else if (r1.seperator != null && r2.seperator != null) {
						int sepComp = r1.seperator.compareTo(r2.seperator);
						if (sepComp != 0) {
							// when different seperator, return that difference
							return sepComp;
						}
					}
				}
				return comparator.compare(o1, o2);
			}
		}

		public int getPosX() {
			return (int) bg.getPos().x;
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if (event.pressedLeftMouse()) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
				AudioController.fireAudioEventID(28);
				sortBy(true);
			}
		}

		public void reverseOrders() {
			comparator = Collections.reverseOrder(comparator);
		}

		public void sortBy(boolean reverse) {
			if (reverse) {
				reverseOrders();
			}
			ScrollableTableList.this.activeSortColumnIndex = index;
			sort();
		}

		@Override
		public boolean isOccluded() {
			return !dependent.isActive();
		}
	}


	//INSERTED CODE
	public GUIScrollablePanel _getScrollPanel() {
		return scrollPanel;
	}
	///
	
}
