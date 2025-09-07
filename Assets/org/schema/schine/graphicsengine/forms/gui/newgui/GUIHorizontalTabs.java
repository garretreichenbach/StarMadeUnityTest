package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUIHorizontalTabs extends GUIElement implements GUICallback {

	public GUIHorizontalTabs(InputState state) {
		super(state);
	}

	private static final int leftActive = 21;

	private static final int rightActive = 24;

	private static final int leftInactive = 25;

	private static final int rightInactive = 27;

	private static final int inactiveInactive = 26;

	private static final int inactiveActive = 22;

	private static final int activeInactive = 23;

	private static final float inactiveHorizontal = 11;

	private static final float activeHorizontal = 10;

	private GUIOverlay corners;

	private GUITexDrawableArea horizontalActive;

	private GUITexDrawableArea horizontalInactive;

	@Override
	public void cleanUp() {
		if (corners != null) {
			corners.cleanUp();
		}
		if (horizontalActive != null) {
			horizontalActive.cleanUp();
		}
		if (horizontalInactive != null) {
			horizontalInactive.cleanUp();
		}
	}

	@Override
	public void onInit() {
		corners = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 32px Corners-8x8-gui-"), getState());
		Texture texture = Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 32px-horizontals-1x32-gui-").getMaterial().getTexture();
		horizontalActive = new GUITexDrawableArea(getState(), texture, 0, activeHorizontal * (1f / 32f));
		horizontalInactive = new GUITexDrawableArea(getState(), texture, 0, inactiveHorizontal * (1f / 32f));
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			int index = (Integer) callingGuiElement.getUserPointer();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(24);
			onLeftMouse(index);
		}
	}

	protected abstract void onLeftMouse(int index);

	protected abstract int getSelectedTab();

	protected abstract int getTabCount();

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		int tabSize = FastMath.round(((getTabContentWidth() - UIScale.getUIScale().W_TAB_MIN_WIDTH) / getTabCount()));
		int size = 0;
		for (int i = 0; i < getTabCount(); i++) {
			int bef = size;
			// draw start
			size += drawStart(i, tabSize, size);
			int end = drawEnd(i, tabSize, size);
			size += end;
			int after = size;
			int ancSize = after - bef;
			if (i == 0) {
				ancSize += corners.getWidth() / 2;
			} else {
				bef += corners.getWidth() / 2;
				if (i == getTabCount() - 1) {
					ancSize -= corners.getWidth() / 2;
				}
			}
			GUIAnchor tabAnchor = getTabAnchor(i);
			tabAnchor.setMouseUpdateEnabled(true);
			tabAnchor.setCallback(this);
			tabAnchor.setUserPointer(i);
			tabAnchor.setPos(bef, 0, 0);
			tabAnchor.setWidth(ancSize);
			tabAnchor.setHeight(corners.getHeight());
			tabAnchor.draw();
		}
		for (int i = 0; i < getTabCount(); i++) {
			drawTabText(i);
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public float getHeight() {
		return UIScale.getUIScale().MAIN_WINDOW_TABS_HEIGHT;
	}

	@Override
	public float getWidth() {
		return getTabContentWidth() - UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET;
	}

	protected abstract int getTabContentWidth();

	protected abstract void drawTabText(int i);

	protected abstract GUIAnchor getTabAnchor(int i);

	protected int drawStart(int i, int tabSize, int size) {
		if (i == getSelectedTab()) {
			setTabTextColor(i, true);
			if (i == 0) {
				corners.setSpriteSubIndex(leftActive);
			} else if (i == getTabCount() - 1) {
				corners.setSpriteSubIndex(inactiveActive);
			} else {
				// start is always from inactive to active here
				corners.setSpriteSubIndex(inactiveActive);
			}
		} else {
			setTabTextColor(i, false);
			if (i == 0) {
				corners.setSpriteSubIndex(leftInactive);
			} else {
				if (i - 1 == getSelectedTab()) {
					corners.setSpriteSubIndex(activeInactive);
				} else {
					corners.setSpriteSubIndex(inactiveInactive);
				}
			}
		}
		corners.setPos(size, 0, 0);
		corners.draw();
		return (int) corners.getWidth();
	}

	public abstract void setTabTextColor(int tab, boolean selected);

	protected int drawEnd(int i, int tabSize, int sizeTotal) {
		int size = 0;
		if (tabSize > corners.getWidth()) {
			size = (int) (tabSize - corners.getWidth());
			if (getTabCount() > 2) {
				if (i == 0 || i == getTabCount() - 1) {
					size -= (getTabTextWidth(i) / 2);
				} else {
					size += (getTabTextWidth(i) / 2) / (getTabCount());
				}
				size += (getTabTextWidth(i) / 2) / (getTabCount());
			}
			if (i == getTabCount() - 1) {
				// System.err.println("IF: "+p.innerForeground.getWidth()+"; "+(sizeTotal+size+corners.getWidth()));
				if ((sizeTotal + size + corners.getWidth()) > getInnerWidthTab()) {
					size -= Math.abs((sizeTotal + size + corners.getWidth()) - getInnerWidthTab());
				} else if ((sizeTotal + size + corners.getWidth()) < getInnerWidthTab()) {
					size += Math.abs((sizeTotal + size + corners.getWidth()) - getInnerWidthTab());
				}
			}
			if (i == getSelectedTab()) {
				horizontalActive.setWidth(size);
				horizontalActive.setHeight(UIScale.getUIScale().W_TAB_HEIGHT);
				horizontalActive.setPos((sizeTotal), 0, 0);
				horizontalActive.draw();
			} else {
				horizontalInactive.setWidth(size);
				horizontalInactive.setHeight(UIScale.getUIScale().W_TAB_HEIGHT);
				horizontalInactive.setPos((sizeTotal), 0, 0);
				horizontalInactive.draw();
			}
		}
		int xPos = (sizeTotal + size / 2) - getTabTextWidth(i) / 2;
		if (i == 0) {
			xPos = (int) ((sizeTotal + (size + corners.getWidth() / 2) / 2) - getTabTextWidth(i));
		} else if (i == getTabCount() - 1) {
			// xPos = (int) ((sizeTotal+(size+corners.getWidth()*2)/2)-p.getTabs().get(i).getTextWidth());
			if (getTabCount() > 2) {
				xPos += (corners.getWidth() / (getTabCount() - 1));
			}
		}
		setTabTextPos(i, xPos, 4);
		if (i == getTabCount() - 1) {
			if (i == getSelectedTab()) {
				corners.setSpriteSubIndex(rightActive);
			} else {
				corners.setSpriteSubIndex(rightInactive);
			}
			corners.setPos(sizeTotal + size, 0, 0);
			corners.draw();
			return size + (int) corners.getWidth();
		} else {
			return size;
		}
	}

	protected abstract void setTabTextPos(int i, int x, int y);

	protected abstract int getInnerWidthTab();

	protected abstract int getTabTextWidth(int i);
}
