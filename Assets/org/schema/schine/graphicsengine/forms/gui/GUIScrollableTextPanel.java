package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.input.InputState;

public class GUIScrollableTextPanel extends GUIScrollablePanel {

	public GUIScrollMarkedReadInterface markReadInterface;
	private GUITextInput flagScrollToCar;
	private int yBef;
	private boolean lastScrollOnBottom;

	public GUIScrollableTextPanel(float width, float height, InputState state) {
		super(width, height, state);
	}

	public GUIScrollableTextPanel(float width, float height, GUIElement dependent, InputState state) {
		super(width, height, dependent, state);
	}

	public boolean isScrollLock() {
		return false;
	}

	public void scrollToCarrier(GUITextInput guiTextInput) {
		flagScrollToCar = guiTextInput;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel#draw()
	 */
	@Override
	public void draw() {
		boolean changedHeight = false;
		int yBefBef = yBef;
		if (getContent() instanceof GUITextInput) {
			GUITextInput overlay = (GUITextInput) getContent();

			overlay.getInputBox().setClip((int) Math.floor(getScrollY()), (int) Math.ceil(getScrollY() + getHeight()));
//			overlay.getCarrier().setClip((int)Math.floor(getScrollY()), (int)Math.ceil(getScrollY()+getHeight()));
//			int size = 1;
//			if(overlay.getInputBox().getText().size() > 0){
//				int cur = -1;
//				while(( cur = overlay.getInputBox().getText().get(0).toString().indexOf("\n", cur+1)) >= 0){
//					size++;
//				}
//			}
//			overlay.getInputBox().setHeight(size * overlay.getInputBox().getFont().getLineHeight());

//			System.err.println(overlay.getInputBox().getHeight()+"; "+overlay.getHeight());
		} else {
			GUITextOverlay overlay = (GUITextOverlay) getContent();

			overlay.setClip((int) Math.floor(getScrollY()), (int) Math.ceil(getScrollY() + getHeight()));
			if (yBef != overlay.getTextHeight()) {
				yBef = overlay.getTextHeight();
				changedHeight = true;
			}
		}

		super.draw();

		if (flagScrollToCar != null) {
			float y = flagScrollToCar.getCarrier().getPos().y + 30;
			float pos = y - (getHeight());

			if (pos > 0) {
				scrollVertical(Math.max(0, pos));
			}
			flagScrollToCar = null;
		}

		{
			float y = getContent().getHeight();
			float pos = y - (getHeight());
			//tolerance of 4
			lastScrollOnBottom = Math.abs(getScrollY() - pos) < (yBef - yBefBef) + getHeight() || getContent().getHeight() <= getHeight();
		}

		boolean scrolled = false;
		if (changedHeight && isScrollLock() && lastScrollOnBottom) {
			float y = getContent().getHeight();
			float pos = y - (getHeight());
			if (pos > 0) {
				scrollVertical(Math.max(0, pos));
			}
			scrolled = true;
		}

		checkMark();

//		if(yBef > yBefBef){
//			System.err.println("LS:::: "+lastScrollOnBottom+"; "+((yBef - yBefBef) +12)+"; scrolled: "+scrolled+"; changedH: "+changedHeight+"; lk: "+isScrollLock());
//		}

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel#setContent(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void setContent(GUIElement content) {

		assert (content instanceof GUITextOverlay || content instanceof GUITextInput);
		super.setContent(content);
	}

	public void checkMark() {
		if (markReadInterface != null) {
			float y = getContent().getHeight();
			float pos = y - (getHeight());

			//tolerance of 4
			if (Math.abs(getScrollY() - pos) < 4 || getContent().getHeight() <= getHeight()) {
				markReadInterface.markRead();
			}
		}
	}

}
