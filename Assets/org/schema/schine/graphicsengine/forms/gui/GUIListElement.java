package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.InputState;

public class GUIListElement extends GUIElement {

	private static final long TOOLTIP_DELAY_MS = 1100;
	public int currentIndex;
	protected GUIElement content;
	protected GUIElement selectContent;
	private boolean firstDraw = true;
	private boolean selected = false;
	private boolean highlighted;
	private GUIColoredRectangle highlight;
	public int heightDiff;
	public int widthDiff;
	private GUIToolTip toolTip;
	public String toolTipText;
	private long insideTime;
	public GUIListElement(InputState state) {
		super(state);
	}

	public GUIListElement(GUIElement content, InputState state) {
		this(content, content, state);

	}

	public GUIListElement(GUIElement content, GUIElement selectedContent, InputState state) {
		super(state);
		this.content = content;
		this.selectContent = selectedContent;

		highlight = new GUIColoredRectangle(state, (int)content.getWidth(), (int)content.getHeight(), new Vector4f(1, 1, 1, 0.085f));
	}

	@Override
	public void cleanUp() {
		if(highlight != null){
			highlight.cleanUp();
		}
		if(content != null){
			content.cleanUp();
		}
		if(selectContent != null && selectContent != content){
			selectContent.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		if(isRenderable()) {
			setInside(false);
			if (getParent() != null && ((GUIElement) getParent()).isInside()) {
				this.checkMouseInside();
				
			} 
		}
		
		content.draw();
		if (selected) {
			drawSelectedContent();
		}
		if (highlighted) {
			highlight.setHeight(content.getHeight());
			highlight.setWidth(content.getWidth());
			highlight.draw();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		firstDraw = false;
	}

	@Override
	protected void doOrientation() {

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#setCallback(org.schema.schine.graphicsengine.forms.gui.GUICallback)
	 */
	@Override
	public void setCallback(GUICallback callback) {
		assert (false) : "Cannot set callback to single list element. please set callback to super list";
		throw new RuntimeException("Cannot set callback to single list element. please set callback to super list");
	}

	@Override
	public float getHeight() {
		return content.getHeight() + heightDiff;
	}

	@Override
	public float getWidth() {
		return content.getWidth() + widthDiff;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public void drawSelectedContent() {
		if (selectContent != null) {
			selectContent.draw();
		}
	}

	/**
	 * @return the content
	 */
	public GUIElement getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(GUIElement content) {
		this.content = content;
	}

	public GUIElementList getList() {
		return (GUIElementList) getParent();
	}

	@Override
	public String getName() {
		return (selected ? "*" : "") + content.getName();
	}

	/**
	 * @return the selectContent
	 */
	public GUIElement getSelectContent() {
		return selectContent;
	}

	/**
	 * @param selectContent the selectContent to set
	 */
	public void setSelectContent(GUIElement selectContent) {
		this.selectContent = selectContent;
	}

	/**
	 * @return the highlighted
	 */
	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean b) {
		this.highlighted = b;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	protected void setFromListCallback(GUICallback callback) {
		super.setCallback(callback);
	}
	@Override
	public void resetToolTip() {
		insideTime = 0L;
	}

	
	public void drawToolTip(long time) {
		if(toolTipText == null ){
			toolTipText = getContent().generateToolTip();
		}
		if(toolTipText != null && isInside()){
			toolTip = new GUIToolTip(getState(), toolTipText, this);
			if(insideTime == 0L){
				insideTime = time;
			}
			if(time - insideTime > TOOLTIP_DELAY_MS){
				toolTip.setText(toolTipText);
				toolTip.draw();
			}
		}else{
			insideTime = 0L;
		}
	}

}
