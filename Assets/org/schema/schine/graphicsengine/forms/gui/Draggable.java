package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.MouseEvent;

public interface Draggable extends GUICallback {

	long MIN_DRAG_TIME = 200;

	public boolean checkDragReleasedMouseEvent(MouseEvent e);

	public int getDragPosX();

	public void setDragPosX(int dragPosX);

	public int getDragPosY();

	public void setDragPosY(int dragPosY);

	public Object getPlayload();

	public long getTimeDragStarted();

	public boolean isStickyDrag();

	public void setStickyDrag(boolean b);

	public void setTimeDraggingStart(long currentTimeMillis);

	public void reset();

	public short getType();

}
