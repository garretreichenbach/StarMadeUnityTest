package org.schema.schine.graphicsengine.forms.gui;


import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

public class DraggableAnchor extends GUIAnchor {

	public static boolean globalDragging;
	private boolean dragging;
	private Vector3i draggingStartPos = new Vector3i();
	private GUIElement affected;
	private boolean mouseDown;

	public DraggableAnchor(InputState state, float width, float height, GUIElement affected) {
		super(state, width, height);

		this.affected = affected;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		setMouseUpdateEnabled(true);
		super.draw();

		if (isInside() && Mouse.isPrimaryMouseDownUtility()) {
			if (!mouseDown) {
				if (!dragging) {
					dragging = true;

					globalDragging = true;
					draggingStartPos.set(Mouse.getX(), Mouse.getY(), 0);
				}
			}
		}
		mouseDown = Mouse.isPrimaryMouseDownUtility();

		if (dragging) {
			Vector3i d = new Vector3i(Mouse.getX(), Mouse.getY(), 0);
			d.sub(draggingStartPos);
			affected.getPos().x += d.x;
			affected.getPos().y += d.y;

			//			System.err.println("DRAGGING__: "+affected.getPos());

			draggingStartPos.set(Mouse.getX(), Mouse.getY(), 0);
			if (!Mouse.isPrimaryMouseDownUtility()) {
				dragging = false;
				globalDragging = false;
			}
		}
		affected.orientateInsideFrame();
	}

	/**
	 * @return the affected
	 */
	public GUIElement getAffected() {
		return affected;
	}

	/**
	 * @param affected the affected to set
	 */
	public void setAffected(GUIElement affected) {
		this.affected = affected;
	}

}
