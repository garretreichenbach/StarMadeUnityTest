package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.MouseEvent;

public interface DropTarget<E extends Draggable> {

	public void checkTarget(MouseEvent e);

	public boolean isTarget(Draggable draggable);

	public void onDrop(E draggable);
}
