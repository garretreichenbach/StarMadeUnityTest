package org.schema.schine.graphicsengine.forms;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public interface GUICloneableElement {

	/**
	 * Represents a unique ID shared by this element and any clones of it.
	 * <br/>Useful for telling if an element is a clone of another.
	 * @return the shared ID
	 */
	long getSharedID();

	/**
	 * Clones this element.
	 * @return the clone
	 */
	GUIElement clone();
}
