package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class GUIIconButton extends GUIAnchor implements TooltipProvider {

	private final Vector4f foregroundColor;
	private final Vector4f backgroundColor;
	private final Vector4f selectedBackgroundColor;
	protected GUIColoredRectangle background;
	protected GUIOverlay image;
	private final Vector4f selectColor;
	private final Vector4f pressedColor;
	private boolean init;
	private Vector3f imgPos = new Vector3f();
	private Suspendable suspendable;
	private boolean active = true;

	public GUIIconButton(InputState state, int width, int height, GUIOverlay image, GUICallback callback, Suspendable s) {
		this(state, width, height,
				new Vector4f(0.3f, 0.3f, 0.6f, 0.9f),
				new Vector4f(1f, 1f, 1f, 1f), image, callback, s);
	}

	public GUIIconButton(InputState state, int width, int height, GUIOverlay image, GUICallback callback) {
		this(state, width, height,
				new Vector4f(0.3f, 0.3f, 0.6f, 0.9f),
				new Vector4f(1f, 1f, 1f, 1f), image, callback);
	}

	public GUIIconButton(InputState state, int width, int height, Vector4f backgroundColor, Vector4f foregroundColor, GUIOverlay image, GUICallback callback) {
		this(state, width, height, backgroundColor, foregroundColor, image, callback, null);
	}

	public GUIIconButton(InputState state, int width, int height, Vector4f backgroundColor, Vector4f foregroundColor, GUIOverlay image, GUICallback callback, Suspendable s) {
		super(state, width, height);
		this.image = image;
		this.backgroundColor = backgroundColor;
		this.selectedBackgroundColor = new Vector4f();
		this.selectedBackgroundColor.set(backgroundColor);
		this.foregroundColor = foregroundColor;
		this.selectColor = new Vector4f(0.8f, 0.8f, 1, 1);
		this.pressedColor = new Vector4f(1, 0.8f, 0.8f, 1);
		this.setCallback(callback);
		this.setMouseUpdateEnabled(true);
		this.suspendable = s;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		image.getSprite().getTint().set(foregroundColor);
		if (!active) {
			setMouseUpdateEnabled(false);
		} else if ((suspendable != null && (suspendable.isSuspended() || !suspendable.isActive() || suspendable.isHinderedInteraction()))) {
			setMouseUpdateEnabled(false);
		} else {

			if (isInside()) {
				background.setColor(selectedBackgroundColor);
				if (Mouse.isPrimaryMouseDownUtility()) {
					image.getSprite().getTint().set(0.7f, 0.6f, 0.7f, 1.0f);
				} else {
					image.getSprite().getTint().set(foregroundColor.x + 0.1f, foregroundColor.y + 0.1f, foregroundColor.z + 0.1f, foregroundColor.w);
				}
			} else {
				background.setColor(backgroundColor);
			}
		}
		image.setPos(imgPos);

		super.draw();

		setMouseUpdateEnabled(true);
		image.getSprite().getTint().set(1, 1, 1, 1);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		image.onInit();
		image.getSprite().setTint(new Vector4f(1, 1, 1, 1));
		background = new GUIColoredRectangle(getState(), (int)width, (int)height, backgroundColor);
		background.rounded = 3;
		this.attach(background);
		background.attach(image);
		super.onInit();

		init = true;
	}

	/**
	 * @return the active
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	public void setImagePos(int x, int y) {
		imgPos.set(x, y, 0);
	}

	/**
	 * @return the backgroundColor
	 */
	public Vector4f getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @return the foregroundColor
	 */
	public Vector4f getForegroundColor() {
		return foregroundColor;
	}

	/**
	 * @return the selectedBackgroundColor
	 */
	public Vector4f getSelectedBackgroundColor() {
		return selectedBackgroundColor;
	}

	public Vector4f getSelectColor() {
		return selectColor;
	}

	public Vector4f getPressedColor() {
		return pressedColor;
	}

	@Override
	public void drawToolTip() {

	}
}
