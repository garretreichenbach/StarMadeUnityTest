package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public abstract class GUIHorizontalProgressBar extends GUIElement {
	private final Vector4f color = new Vector4f(1, 1, 1, 1);
	protected String text;
	GUIProgressBarFrame background;
	GUIProgressBarFilling progress;
	private GUIElement dependend;
	private boolean displayPercent;
	private boolean init;
	private GUITextOverlay textOverlay;
	private int textWidth;
	private int progressBarInset = UIScale.getUIScale().smallinset;
	private GUIScrollablePanel lr;
	public float widthAdd;

	public GUIHorizontalProgressBar(InputState state, GUIElement dependent) {
		this(state, null, dependent);
	}

	public GUIHorizontalProgressBar(InputState state, String text, GUIElement dependent) {
		super(state);
		this.dependend = dependent;
		this.text = text;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (background != null) {
			background.cleanUp();
		}
		if (progress != null) {
			progress.cleanUp();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		GlUtil.glPushMatrix();

		transform();

		background.setWidth((int) getDepWidth());
		background.draw();

		progress.setWidth((int) (getValue() * (getDepWidth() - progressBarInset * 2)));
		progress.setColor(color);
		progress.draw();

		if (textOverlay != null) {
			textOverlay.setPos((int) (getDepWidth() / 2 - textWidth / 2), 5, 0);
//			textOverlay.draw();
			lr.draw();
		}

		if(callback != null){
			checkMouseInside();
		}
		
		GlUtil.glPopMatrix();
	}
	private float getDepWidth(){
		return (dependend.getWidth() - widthAdd);
	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#onInit()
	 */
	@Override
	public void onInit() {
		background = new GUIProgressBarFrame(getState(), 10, UIScale.getUIScale().h);
		progress = new GUIProgressBarFilling(getState(), 10, UIScale.getUIScale().h - progressBarInset);
		progress.setPos(progressBarInset, progressBarInset, 0);

		if (displayPercent || text != null) {
			
			
			
			textOverlay = new GUITextOverlay(FontSize.SMALL_15, getState()) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#onDirty()
				 */
				@Override
				public void onDirty() {
					super.onDirty();
					textWidth = FontSize.SMALL_15.getWidth(getText().get(0).toString());
				}

			};

			textOverlay.setTextSimple(new Object() {
				@Override
				public String toString() {
					return getText();
				}

			});
			textWidth = FontSize.SMALL_15.getWidth(getText());
			
			lr = new GUIScrollablePanel(this.getWidth(), this.getHeight(), this, getState());
			lr.setScrollable(0);
			lr.setLeftRightClipOnly = true;
			lr.setContent(textOverlay);
			lr.onInit();
		}

		background.onInit();
		progress.onInit();

		init = true;
	}

	public String getText() {
		return (text != null ? text + " " : "") + (displayPercent ? ((int) Math.floor(getValue() * 100f) + " %") : "");
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getHeight()
	 */
	@Override
	public float getHeight() {
		return background.getHeight();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getWidth()
	 */
	@Override
	public float getWidth() {
		return background.getWidth();
	}

	/**
	 * @return the value
	 */
	public abstract float getValue();

	/**
	 * @return the displayPercent
	 */
	public boolean isDisplayPercent() {
		return displayPercent;
	}

	/**
	 * @param displayPercent the displayPercent to set
	 */
	public void setDisplayPercent(boolean displayPercent) {
		this.displayPercent = displayPercent;
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

}
