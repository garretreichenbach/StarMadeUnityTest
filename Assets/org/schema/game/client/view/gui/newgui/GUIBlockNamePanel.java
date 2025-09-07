package org.schema.game.client.view.gui.newgui;

import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUINormalForeground;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;

public class GUIBlockNamePanel extends GUIElement {

	private GUINormalForeground bg;
	private GUITextOverlay text;
	private GUIScrollablePanel textScroll;
	private float speed = 1440;
	private float targetWidth;
	private float currentWidth;

	public GUIBlockNamePanel(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
		if (bg != null) {
			bg.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (currentWidth > 0) {
			orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_BOTTOM);

			getPos().y -= 94;

			GlUtil.glPushMatrix();

			setInside(false);

			transform();

			bg.setWidth((int) currentWidth);
			text.getPos().x = UIScale.getUIScale().inset;
			text.getPos().y = UIScale.getUIScale().inset;

//			bg.getPos().x = (int)(-currentWidth/2f);
			textScroll.getPos().x = (int) bg.getPos().x;

			bg.draw();
			textScroll.draw();

			if (isMouseUpdateEnabled()) {
				checkMouseInside();
			}

			for (AbstractSceneNode f : getChilds()) {
				f.draw();
			}
			GlUtil.glPopMatrix();
		}
	}

	@Override
	public void onInit() {
		bg = new GUINormalForeground(getState(), 100, UIScale.getUIScale().h);

		text = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		text.setTextSimple(new Object() {
			@Override
			public String toString() {
				if (BuildModeDrawer.currentInfo != null) {
					if (BuildModeDrawer.selectedInfo != null) {
						if (
								(ElementInformation.canBeControlled(BuildModeDrawer.selectedInfo.getId(), BuildModeDrawer.currentInfo.getId())
										|| (BuildModeDrawer.selectedInfo.getControlling() != null &&
										BuildModeDrawer.selectedInfo.getControlling().contains(BuildModeDrawer.currentInfo.getId()))) &&
										BuildModeDrawer.selectedBlock.getAbsoluteIndex() != BuildModeDrawer.currentPiece.getAbsoluteIndex()) {

							if (BuildModeDrawer.selectedBlock.getSegment().getSegmentController().getControlElementMap()
									.isControlling(BuildModeDrawer.selectedBlock.getAbsoluteIndex(), BuildModeDrawer.currentPiece.getAbsoluteIndex(), BuildModeDrawer.currentInfo.getId())) {

								return "Disconnect " + BuildModeDrawer.selectedInfo.getName() + " to " + BuildModeDrawer.currentInfo.getName() + " ('" + KeyboardMappings.CONNECT_MODULE.getKeyChar() + "')";
							} else {

								return "Connect " + BuildModeDrawer.selectedInfo.getName() + " to " + BuildModeDrawer.currentInfo.getName() + " ('" + KeyboardMappings.CONNECT_MODULE.getKeyChar() + "')";
							}

						}
					}
					
					if (BuildModeDrawer.selectedBlock == null || BuildModeDrawer.selectedBlock.getAbsoluteIndex() != BuildModeDrawer.currentPiece.getAbsoluteIndex()) {
						if (BuildModeDrawer.currentInfo.isSignal() || (BuildModeDrawer.currentInfo.getControlling() != null && BuildModeDrawer.currentInfo.getControlling().size() > 0)) {
							return ElementKeyMap.getInfo(BuildModeDrawer.currentInfo.getId()).getName() + " (select with '" + KeyboardMappings.SELECT_MODULE.getKeyChar() + "')";
						} else {
							int count = BuildModeDrawer.currentPiece.getSegment().getSegmentController().getElementClassCountMap().get(BuildModeDrawer.currentInfo.getId());
							return ElementKeyMap.getInfo(BuildModeDrawer.currentInfo.getId()).getName() + " x " + count;
						}
					} else {
						return ElementKeyMap.getInfo(BuildModeDrawer.currentInfo.getId()).getName() + " (deselect with '" + KeyboardMappings.SELECT_MODULE.getKeyChar() + "')";
					}
				} else {
					return "";
				}
			}

		});
		bg.onInit();

		textScroll = new GUIScrollablePanel(10, 10, bg, getState());
		textScroll.setContent(text);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		if (BuildModeDrawer.currentInfo != null) {
			targetWidth = text.getMaxLineWidth() + UIScale.getUIScale().inset * 2;
		} else {
			targetWidth = 0;
		}
		if (currentWidth < targetWidth) {
			float newVal = currentWidth + timer.getDelta() * speed;
			currentWidth = Math.min(targetWidth, newVal);
		} else if (currentWidth > targetWidth) {
			float newVal = currentWidth - timer.getDelta() * speed;
			currentWidth = Math.max(targetWidth, newVal);
		}
	}

	@Override
	public float getHeight() {
		return bg.getHeight();
	}

	@Override
	public float getWidth() {
		return bg.getWidth();
	}

}
