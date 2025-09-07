package org.schema.schine.graphicsengine.forms.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GUIElementList extends GUIElement implements List<GUIListElement>, TooltipProvider {
	public static int LIST_ORIENTATION_HORIZONTAL = 1;
	public static int LIST_ORIENTATION_VERTICAL = 0;
	public int height, width;
	private int listOrientation = LIST_ORIENTATION_VERTICAL;
	protected ObjectArrayList<GUIListElement> elements;
	private boolean init;
	private GUIScrollablePanel scrollPanel;
	private boolean cachedPosition;
	private float cachePositionX;
	private float cachePositionXP;
	private float cachePositionY;
	private float cachePositionYP;
	private float cachedScrollX = -1;
	private float cachedScrollY = -1;
	private int cachedIndex;
	public int rightInset;
	public int leftInset;



	public GUIElementList(InputState state) {
		super(state);
		this.elements = new ObjectArrayList<GUIListElement>();
	}

	public static Vector4f getRowColor(int index) {
		return index % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.2f, 0.2f, 0.2f, 0.5f);
	}
	public void addObserverRecusive(GUIChangeListener o) {
		for (GUIListElement l : elements) {
			l.getContent().addObserver(o);
			if (l.getContent() instanceof GUIElementList) {
				GUIElementList e = (GUIElementList) l.getContent();
				e.addObserverRecusive(o);
			}
			if (l.getContent() instanceof GUIEnterableList) {
				GUIEnterableList e = (GUIEnterableList) l.getContent();
				e.getList().addObserverRecusive(o);
			}
		}
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addWithoutUpdate(GUIListElement e) {
		boolean add = elements.add(e);
		return add;
	}

	@Override
	public void cleanUp() {
		for(GUIListElement e : this){
			if(e != null){
				if(e.getContent() != null){
					e.getContent().cleanUp();
				}
				if(e.getSelectContent() != null && e.getSelectContent() != e.getContent()){
					e.getSelectContent().cleanUp();
				}
			}
		}
	}

	@Override
	public void draw() {
		if(translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			transform();
		}
		if (!init) {
			onInit();
		}
		if(isRenderable()) {
			setInside(false);
			if (getParent() != null) {
				setInside(((GUIElement) getParent()).isInside());
			} else if (scrollPanel != null) {
				setInside(scrollPanel.isInside());
			}
		}
		drawList(true, false);



		final int size = childs.size();
		for (int i = 0; i < size; i++) {
			childs.get(i).draw();
		}
		if(leftInset > 0){
			GlUtil.glTranslatef(leftInset, 0, 0);
		}
		drawList(false, false);
		if(leftInset > 0){
			GlUtil.glTranslatef(-leftInset, 0, 0);
		}
		if(translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}
	}

	@Override
	public void onInit() {
		if(init){
			return;
		}
		for (GUIElement e : elements) {
			e.onInit();
		}
		init = true;

	}

	public void deselectAll() {
		for (GUIListElement e : elements) {
			e.setSelected(false);
		}
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		updateDim();
		return height;
	}

	@Override
	public float getWidth() {
		return leftInset + width + rightInset;
	}

	@Override
	public boolean isPositionCenter() {

		return false;
	}

	private void drawList(final boolean after, final boolean all) {
		GlUtil.glPushMatrix();
		if (scrollPanel != null && (cachedScrollX != scrollPanel.getScrollX() || cachedScrollY != scrollPanel.getScrollY())) {
			cachedPosition = false;
		}
		int translatedX = 0;
		int translatedY = 0;
		float x = 0;
		float y = 0;
		float xP = 0;
		float yP = 0;
		int p = 0;
		final int size = elements.size();
		boolean firstDrawn = false;

		if (cachedPosition) {
			x = cachePositionX;
			y = cachePositionY;
			xP = cachePositionXP;
			yP = cachePositionYP;
			p = cachedIndex;
		}
		int totalIt = 0;
		for (int i = p; i < size; i++) {
			GUIListElement listElement = elements.get(i);

			int w = 0;
			int h = 0;
			if (!firstDrawn) {
				final int max = Math.max(i - 1, 0);
				for (int j = i; j >= max; j--) {
					w += (int) (elements.get(j).getScale().x * elements.get(j).getWidth());
					h += (int) (elements.get(j).getScale().y * elements.get(j).getHeight());
				}
			}
			boolean drawIt = true;
			float bef = listElement.getHeight();
			if (scrollPanel != null) {

				if (firstDrawn && (
						x > scrollPanel.getScrollX() + scrollPanel.getWidth() ||
								y > scrollPanel.getScrollY() + scrollPanel.getHeight())) {
					drawIt = false;
					//we are at the end
					break;
				} else if (!firstDrawn && (
						x < scrollPanel.getScrollX() - w
								||
								y < scrollPanel.getScrollY() - h)) {
					drawIt = false;
				}
			}

			if (drawIt) {
				GlUtil.translateModelview((int)xP, (int)yP, 0);
				translatedX += (int)xP;
				translatedY += (int)yP;
				if (!firstDrawn) {
					cachePositionX = x;
					cachePositionXP = xP;
					cachePositionY = y;
					cachePositionYP = yP;
					cachedScrollX = scrollPanel != null ? scrollPanel.getScrollX() : 0;
					cachedScrollY = scrollPanel != null ? scrollPanel.getScrollY() : 0;
					cachedIndex = i;
					cachedPosition = true;
					firstDrawn = true;
				}

				xP = 0;
				yP = 0;

				if (all) {
					listElement.setFromListCallback(this.getCallback());
					listElement.currentIndex = p;
					listElement.draw();

				} else {
					if (after) {
						if (!(listElement.getContent() instanceof GUIEnterableList) || (!((GUIEnterableList) listElement.getContent()).isExpended())) {
							listElement.setFromListCallback(this.getCallback());
							listElement.currentIndex = p;
							listElement.draw();

						}
					} else {
						if ((listElement.getContent() instanceof GUIEnterableList) && (((GUIEnterableList) listElement.getContent()).isExpended())) {
							listElement.setFromListCallback(this.getCallback());
							listElement.currentIndex = p;
							listElement.draw();
						}
					}
				}

				p++;

			}
			if (listOrientation == LIST_ORIENTATION_HORIZONTAL) {
				x += listElement.getScale().x * listElement.getWidth();
				xP += listElement.getScale().x * listElement.getWidth();
			} else {
				y += listElement.getScale().y * listElement.getHeight();
				yP += listElement.getScale().y * listElement.getHeight();
			}
			totalIt++;
		}

		GlUtil.translateModelview(-translatedX, -translatedY, 0);
		GlUtil.glPopMatrix();
	}

	@Override
	public void drawToolTip() {

		for (int i = 0; i < elements.size(); i++) {
			GUIListElement guiElement = elements.get(i);
			if (guiElement instanceof TooltipProvider) {
				((TooltipProvider) guiElement).drawToolTip();
			}
		}

	}

	/**
	 * @param minCapacity
	 * @see java.util.ArrayList#ensureCapacity(int)
	 */
	public void ensureCapacity(int minCapacity) {
		elements.ensureCapacity(minCapacity);
	}

	public void selectAll() {
		for (GUIListElement e : elements) {
			e.setSelected(true);
		}
	}

	public void setScrollPane(GUIScrollablePanel scrollPanel) {
		assert (scrollPanel != null);
		this.scrollPanel = scrollPanel;
	}

	/**
	 * @return
	 * @see java.util.ArrayList#size()
	 */
	@Override
	public int size() {
		return elements.size();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return elements.contains(o);
	}

	/**
	 * @return
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public Iterator<GUIListElement> iterator() {
		return elements.iterator();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#toArray()
	 */
	@Override
	public Object[] toArray() {
		return elements.toArray();
	}

	/**
	 * @param <T>
	 * @param a
	 * @return
	 * @see java.util.ArrayList#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return elements.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(GUIListElement e) {
//		if(e.getContent() instanceof GUITextOverlay){
//			((GUITextOverlay)e.getContent()).updateSize();
//		}
//		assert(!(e.getContent() instanceof GUITextOverlay)):"WARNING: GUIText wil change size after drawing once";
		boolean add = elements.add(e);
		updateDim();
		return add;
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		boolean remove = elements.remove(o);
		updateDim();
		return remove;
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return elements.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends GUIListElement> c) {
		boolean addAll = elements.addAll(c);
		updateDim();
		return addAll;
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends GUIListElement> c) {
		boolean addAll = elements.addAll(index, c);
		updateDim();
		return addAll;
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removeAll = elements.removeAll(c);
		updateDim();
		return removeAll;
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean retainAll = elements.retainAll(c);
		updateDim();
		return retainAll;
	}

	/**
	 * @see java.util.ArrayList#clear()
	 */
	@Override
	public void clear() {
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i) != null) {
				if (elements.get(i).getContent() != null) {
					elements.get(i).getContent().cleanUp();
				}

				if (elements.get(i).getSelectContent() != null) {
					elements.get(i).getSelectContent().cleanUp();
				}
			}
		}
		elements.clear();
		updateDim();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	@Override
	public GUIListElement get(int index) {
		return elements.get(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	@Override
	public GUIListElement set(int index, GUIListElement element) {
		GUIListElement set = elements.set(index, element);
		updateDim();
		return set;
	}

	@Override
	public void notifyObservers() {
		super.notifyObservers(true);
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, GUIListElement element) {

		elements.add(index, element);
		updateDim();
	}
	public void addWithoutUpdate(int index, GUIListElement element) {
		elements.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	@Override
	public GUIListElement remove(int index) {
		GUIListElement remove = elements.remove(index);
		updateDim();
		return remove;
	}
	public GUIListElement removeWithoutUpdate(int index) {
		GUIListElement remove = elements.remove(index);
		return remove;
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		return elements.indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return elements.lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public ListIterator<GUIListElement> listIterator() {
		return elements.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.AbstractList#listIterator(int)
	 */
	@Override
	public ListIterator<GUIListElement> listIterator(int index) {
		return elements.listIterator(index);
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.AbstractList#subList(int, int)
	 */
	@Override
	public List<GUIListElement> subList(int fromIndex, int toIndex) {
		return elements.subList(fromIndex, toIndex);
	}

	/**
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trimToSize() {
		elements.trim();
		updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).update(timer);
		}
	}

	public void updateDim() {
		height = 4;
		width = 0;
		for (GUIListElement e : elements) {
			e.setParent(this);
			if (e.getContent() instanceof GUIElementList) {
				((GUIElementList) e.getContent()).updateDim();
			}
			if (e.getContent() instanceof GUIEnterableList) {
				((GUIEnterableList) e.getContent()).getList().updateDim();
			}
			if (listOrientation == LIST_ORIENTATION_VERTICAL) {
				width = (int) e.getWidth();
				height += (int) e.getHeight();
			} else {
				width += (int) e.getWidth();
				height = (int) e.getHeight();
			}
			cachedPosition = false;
		}
	}

	public ObjectArrayList<GUIListElement> getElements() {
		return elements;
	}
}
