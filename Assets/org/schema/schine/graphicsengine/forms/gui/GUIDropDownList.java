package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDropdownBackground;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.DelayedDropDownSelectedChanged;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GUIDropDownList extends GUIElement implements GUICallback, GUIUniqueExpandableInterface, List<GUIListElement> {

	private final GUIElementList list;

	public GUIElement dependend;

	public int dependentWidthOffset;

	public boolean leftDependentHalf;

	public boolean rightDependentHalf;

	private GUIElement dropdownButton;

	private GUIColoredAnchor bg;

	private GUIColoredAnchor bgExp;

	private GUIScrollablePanel scrollPanel;

	private boolean init = false;

	private boolean expanded = false;

	private int width;

	private int height;

	private int expHeight;

	private DropDownCallback dropDownCallback;

	private GUIListElement selectedElement;

	private boolean listChanged;

	private GUIListElement highlight;

	private Matrix4f currentMatrix = new Matrix4f();

	public GUIDropDownList(InputState state, int width, int height, int expHeight, DropDownCallback dropDownCallback) {
		super(state);
		list = new GUIElementList(state);
		this.width = width;
		this.height = height;
		this.expHeight = expHeight;
		this.dropDownCallback = dropDownCallback;
	}

	public GUIDropDownList(InputState state, int width, int height, int expHeight, DropDownCallback dropDownCallback, Collection<? extends GUIElement> elements) {
		this(state, width, height, expHeight, dropDownCallback);
		for (GUIElement e : elements) {
			if (e instanceof GUIListElement) {
				list.add((GUIListElement) e);
			} else {
				list.add(new GUIListElement(e, e, state));
			}
		}
		onListChanged();
	}

	public GUIDropDownList(InputState state, int width, int height, int expHeight, DropDownCallback dropDownCallback, GUIElement... elements) {
		this(state, width, height, expHeight, dropDownCallback);
		for (GUIElement e : elements) {
			if (e instanceof GUIListElement) {
				list.add((GUIListElement) e);
			} else {
				list.add(new GUIListElement(e, e, state));
			}
		}
		onListChanged();
	}

	private short lastExec;

	private GUIElement mouseOver;

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (isActive()) {
			if (mouseOver != null && mouseOver != callingGuiElement) {
				mouseOver.resetToolTip();
			}
			mouseOver = callingGuiElement;
			if (event.pressedLeftMouse()) {
				if (lastExec != getState().getNumberOfUpdate()) {
					if (expanded && callingGuiElement instanceof GUIListElement) {
						selectedElement = (GUIListElement) callingGuiElement;
						dropDownCallback.onSelectionChanged((GUIListElement) callingGuiElement);
						setExpanded(false);
					}
					if ((!bg.isInside() && callingGuiElement == dropdownButton) || callingGuiElement == bg) {
						setExpanded(!expanded);
					}
					lastExec = getState().getNumberOfUpdate();
				}
			} else {
				if (callingGuiElement instanceof GUIListElement) {
					if (highlight != null) {
						highlight.setHighlighted(false);
					}
					highlight = (GUIListElement) callingGuiElement;
					if (highlight.getContent() instanceof GUIResizableElement) {
						((GUIResizableElement) highlight.getContent()).setWidth(scrollPanel.getWidth());
					}
					highlight.setHighlighted(true);
				}
			}
		}
	}

	@Override
	public boolean isOccluded() {
		if (dependend != null && !dependend.isActive()) {
			return true;
		}
		boolean scrollBarIn = isScrollBarInside();
		return expanded && scrollBarIn;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (listChanged) {
			onListChanged();
		}
		GlUtil.glPushMatrix();
		transform();
		if (dependend != null) {
			int dWidth = (int) dependend.getWidth();
			int dPos = 0;
			if (leftDependentHalf) {
				dWidth = (int) dependend.getWidth() / 2;
			} else if (rightDependentHalf) {
				dWidth = (int) dependend.getWidth() / 2;
				dPos = ((int) dependend.getWidth()) - dWidth;
			}
			GlUtil.translateModelview(dPos, 0, 0);
			boolean diff = width != (dWidth + dependentWidthOffset);
			width = dWidth + dependentWidthOffset;
			bg.setWidth(getWidth());
			bgExp.setWidth(getWidth());
			bgExp.setHeight(Math.min(list.getHeight(), expHeight));
			dropdownButton.setPos(getWidth() - dropdownButton.getWidth() - UIScale.getUIScale().scale(3), 0, 0);
			scrollPanel.setWidth(getWidth());
			scrollPanel.setHeight(bgExp.getHeight());
			if (diff) {
				for (int i = 0; i < size(); i++) {
					if (get(i).getContent() instanceof GUIAnchor) {
						((GUIAnchor) get(i).getContent()).setWidth(width);
					}
				}
			}
		}
		if (!expanded) {
			bg.draw();
		} else {
			getState().getController().getInputController().setCurrentActiveDropdown(this);
			this.currentMatrix.set(Controller.modelviewMatrix);
			float[] mw = new float[16];
			bg.draw();

		}
		if (selectedElement != null) {
			selectedElement.draw();
		}
		dropdownButton.draw();
		for (int i = 0; i < getChilds().size(); i++) {
			getChilds().get(i).draw();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		scrollPanel = new GUIScrollablePanel(width, expHeight, getState());
		scrollPanel.setContent(list);
		scrollPanel.setPos(0, height, 0);
		scrollPanel.setMouseUpdateEnabled(true);
		scrollPanel.setScrollLocking(true);
		list.setCallback(this);
		list.setMouseUpdateEnabled(true);
		list.setParent(scrollPanel);
		list.setScrollPane(scrollPanel);
		dropdownButton = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-"), getState());
		dropdownButton.setPos(getWidth() - dropdownButton.getWidth(), (int) (getHeight() / 2 - dropdownButton.getHeight() / 2), 0);
		dropdownButton.setMouseUpdateEnabled(true);
		dropdownButton.setCallback(this);
		((GUIOverlay) dropdownButton).setSpriteSubIndex(5);
		bg = new GUIDropdownBackground(getState(), width, height);
		bg.setMouseUpdateEnabled(true);
		bg.setCallback(this);
		bgExp = new GUIDropdownBackground(getState(), width, expHeight);
		bgExp.setPos(0, height, 0);
		init = true;
	}

	@Override
	public void drawExpanded() {
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(currentMatrix);
//		System.err.println("LOAD EXPANDED MATRIX "+this.currentMatrix.m30+", "+this.currentMatrix.m31+"; ");
		if (expanded) {
			// System.err.println("DDDD "+bgExp.getPos()+" :::: "+bgExp.getHeight());
			bgExp.draw();
			assert (list.getParent() == scrollPanel);
			scrollPanel.draw();
		// System.err.println("___JJSJKJS_ "+scrollPanel.isInside()+"; "+list.isInside());
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public float getHeight() {
		// expanded ? expHeight : height;
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (dependend == null || dependend.isActive()) && super.isActive();
	}

	/**
	 * @return the list
	 */
	public GUIElementList getList() {
		return list;
	}

	/**
	 * @return the selectedElement
	 */
	public GUIListElement getSelectedElement() {
		return selectedElement;
	}

	/**
	 * @param selectedElement the selectedElement to set
	 */
	public void setSelectedElement(GUIListElement selectedElement) {
		this.selectedElement = selectedElement;
	}

	/**
	 * @return the expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * @param expanded the expanded to set
	 */
	@Override
	public void setExpanded(boolean expanded) {
		if (expanded) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
			AudioController.fireAudioEventID(4);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
			AudioController.fireAudioEventID(3);
		}
		this.expanded = expanded;
	}

	@Override
	public boolean isScrollBarInside() {
		return scrollPanel != null && scrollPanel.isInsideScrollBar();
	}

	public void onListChanged() {
		if (selectedElement == null || !contains(selectedElement)) {
			if (size() > 0) {
				selectedElement = get(0);
				flagSelectedChanged(selectedElement);
			} else {
				selectedElement = null;
			}
		}
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#size()
	 */
	@Override
	public int size() {
		return list.size();
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#iterator()
	 */
	@Override
	public Iterator<GUIListElement> iterator() {
		return list.iterator();
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#toArray()
	 */
	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * @param a
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#add(org.schema.schine.graphicsengine.forms.gui.GUIListElement)
	 */
	@Override
	public boolean add(GUIListElement e) {
		listChanged = true;
		return list.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		listChanged = true;
		return list.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends GUIListElement> c) {
		listChanged = true;
		return list.addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends GUIListElement> c) {
		listChanged = true;
		return list.addAll(index, c);
	}

	/**
	 * @param c
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		listChanged = true;
		return list.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		listChanged = true;
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	/**
	 * @param index
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#get(int)
	 */
	@Override
	public GUIListElement get(int index) {
		return list.get(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#set(int, org.schema.schine.graphicsengine.forms.gui.GUIListElement)
	 */
	@Override
	public GUIListElement set(int index, GUIListElement element) {
		return list.set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#add(int, org.schema.schine.graphicsengine.forms.gui.GUIListElement)
	 */
	@Override
	public void add(int index, GUIListElement element) {
		listChanged = true;
		list.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#remove(int)
	 */
	@Override
	public GUIListElement remove(int index) {
		listChanged = true;
		return list.remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#listIterator()
	 */
	@Override
	public ListIterator<GUIListElement> listIterator() {
		return list.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#listIterator(int)
	 */
	@Override
	public ListIterator<GUIListElement> listIterator(int index) {
		return list.listIterator(index);
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#subList(int, int)
	 */
	@Override
	public List<GUIListElement> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/**
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#trimToSize()
	 */
	public void trimToSize() {
		listChanged = true;
		list.trimToSize();
	}

	public void setSelectedIndex(int i) {
		if (i >= 0 && i < list.size()) {
			selectedElement = list.get(i);
			flagSelectedChanged(list.get(i));
		}
	}

	public void setSelectedUserPointer(Object pointer) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getContent() != null && list.get(i).getContent().getUserPointer() != null && list.get(i).getContent().getUserPointer().getClass() == pointer.getClass() && list.get(i).getContent().getUserPointer().equals(pointer)) {
				setSelectedIndex(i);
				return;
			}
		}
		System.err.println("[GUIDROPDOWN] Userpointer not found " + pointer);
	}

	private void flagSelectedChanged(GUIListElement e) {
		if (dropDownCallback != null) {
			DelayedDropDownSelectedChanged d = new DelayedDropDownSelectedChanged(dropDownCallback, e);
			getState().getController().getInputController().getDelayedDropDowns().enqueue(d);
		}
	}

	public void drawToolTip(long time) {
		if (isActive() && expanded && mouseOver != null && mouseOver instanceof GUIListElement) {
			((GUIListElement) mouseOver).drawToolTip(time);
		}
	}
}
