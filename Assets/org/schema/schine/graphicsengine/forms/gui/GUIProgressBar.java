package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.network.client.ClientState;

public class GUIProgressBar extends GUIAnchor {
	private GUITextButton progressBar;
	private GUITextButton progressBarFrame;
	private GUITextButton progressBarFrameBack;
	private float pc;
	private Object txt;
	private Vector4f transparent = new Vector4f();
	private Vector4f barColor = new Vector4f(0.7f, 0.7f, 0.7f, 1f);
	private Vector4f backColor = new Vector4f(0.1f, 0.1f, 0.1f, 1f);
	private boolean initialized;
	private boolean drawText;

	public GUIProgressBar(ClientState state, float width, float height, Object txt, boolean drawText, GUICallback clickCallback) {
		super(state, width, height);
		this.drawText = drawText;
		progressBar = new GUITextButton(state, 0, (int) height, "", new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColor() {
				return barColor;
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getSelectedBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				return barColor;
			}

		};

		progressBarFrame = new GUITextButton(state, (int) width, (int) height, txt, clickCallback) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColor() {
				return transparent;
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getSelectedBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				return transparent;
			}

		};
		progressBarFrameBack = new GUITextButton(state, (int) width, (int) height, "", new GUICallback() {
			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColor() {
				return backColor;
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getSelectedBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				return backColor;
			}

		};
		this.txt = txt;
		this.attach(progressBarFrameBack);
		this.attach(progressBar);
		this.attach(progressBarFrame);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (!initialized) {
			onInit();
			initialized = true;
		}
		progressBar.background.setWidth(Math.round(pc * getWidth()));
		if (drawText) {
			progressBarFrame.setText(txt + " " + FastMath.round(pc * 100f) + "%");
		}
		super.draw();

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		progressBarFrameBack.onInit();
		progressBar.onInit();
		progressBarFrame.onInit();
	}

	public void setPercent(float percentZeroToOne) {
		float pc = Math.max(0f, Math.min(1f, percentZeroToOne));

		this.pc = pc;
	}

	public float getPercent() {
		return pc;
	}
}
