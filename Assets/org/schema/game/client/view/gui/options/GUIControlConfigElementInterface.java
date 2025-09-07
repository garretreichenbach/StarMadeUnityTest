package org.schema.game.client.view.gui.options;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;

public interface GUIControlConfigElementInterface extends GUICallback, Drawable {

	public Vector3f getPos();

	public float getHeight();

	public float getWidth();

	public void setCallback(GUICallback callback);

	public boolean isHighlighted();

}
