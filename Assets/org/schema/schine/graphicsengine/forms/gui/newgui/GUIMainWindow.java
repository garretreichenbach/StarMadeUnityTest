package org.schema.schine.graphicsengine.forms.gui.newgui;

import api.listener.events.gui.MainWindowTabAddEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.MainWindowFramePalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.WindowPaletteInterface;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;

public class GUIMainWindow extends GUIResizableGrabbableWindow implements GUITabInterface {

	private final ObjectArrayList<GUIContentPane> tabs = new ObjectArrayList<GUIContentPane>();

	GUIInnerForeground innerForeground;
	boolean init = false;
	GUIInnerBackground innerBackground;
	private int selectedTab = 0;
	private GUIOverlay elements;
	private GUITexDrawableArea top;
	private GUITexDrawableArea bottom;
	private GUITexDrawableArea left;
	private GUITexDrawableArea right;
	private GUITexDrawableArea bg;
	private GUIGradientVerticalStrip lStrip;
	private GUITabPane tabPane;

	public GUIMainWindow(InputState state, int initialWidth, int initialHeight, int initialPosX, int initalPosY, String windowId) {
		super(state, initialWidth, initialHeight, initialPosX, initalPosY, windowId);
	}
	public GUIMainWindow(InputState state, int initialWidth, int initialHeight, String windowId) {
		super(state, initialWidth, initialHeight, windowId);
	}

	private static WindowPaletteInterface w = new WindowPaletteInterface() {
		
		@Override
		public Vector2f getTopModifierOffset() {
			return MainWindowFramePalette.topSizeModifierOffset;
		}
		
		@Override
		public Vector2f getRightModifierOffset() {
			return MainWindowFramePalette.rightSizeModifierOffset;
		}
		
		@Override
		public Vector2f getLeftModifierOffset() {
			return MainWindowFramePalette.leftSizeModifierOffset;
		}
		
		@Override
		public Vector2f getBottomModifierOffset() {
			return MainWindowFramePalette.bottomSizeModifierOffset;
		}
		

		@Override
		public Vector2f getMoveModifierOffset() {
			return MainWindowFramePalette.moveModifierOffset;
		}
	};
	
	
	@Override
	public WindowPaletteInterface getWindowPalette() {
		return w;
	}
	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();

		checkMouseInside();

		checkGrabbedResize();
		GlUtil.glPopMatrix();

		GlUtil.glPushMatrix();
		transform();
		drawWindow();

		//no need to transform, as we already should be at this point

		innerBackground.draw();
		assert (tabs.size() > 0);
		
		
		if (tabs.size() == 1) {
			innerForeground.cornerDistanceTopY = UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET;
			innerForeground.cornerDistanceBottomY = UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET;
			innerForeground.upperCap = true;
			innerForeground.draw();
			tabs.get(0).draw();
		} else {
			innerForeground.cornerDistanceTopY = UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET;
			innerForeground.cornerDistanceBottomY = UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET;
			innerForeground.cornerUpperOffsetY = UIScale.getUIScale().MAIN_WINDOW_TABS_HEIGHT;
			innerForeground.upperCap = false;
			innerForeground.draw();

			tabPane.setPos(getInset(), getInset(), 0);
			tabPane.draw();
			tabs.get(selectedTab).draw();
		}

		int size = getChilds().size();
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		for (int i = 0; i < size; i++) {
			getChilds().get(i).draw();
		}

		drawMouseResizeIndicators();
		GlUtil.glPopMatrix();

	}

	private void drawWindow() {
		
		elements.setPos(0, 0, 0);
		elements.setSpriteSubIndex(0);
		elements.draw();

		elements.setPos(getWidth() - elements.getWidth(), 0, 0);
		elements.setSpriteSubIndex(1);
		elements.draw();

		elements.setPos(0, getHeight() - elements.getHeight(), 0);
		elements.setSpriteSubIndex(2);
		elements.draw();

		elements.setPos(getWidth() - elements.getWidth(), getHeight() - elements.getHeight(), 0);
		elements.setSpriteSubIndex(3);
		elements.draw();

		
		startStandardDraw();
		
		
		left.setPos(0, elements.getHeight(), 0);
		left.setWidth((int) elements.getWidth());
		left.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		left.drawRaw();

		right.setPos(getWidth() - elements.getWidth(), elements.getHeight(), 0);
		right.setWidth((int) elements.getWidth());
		right.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		right.drawRaw();

		top.setPos(elements.getWidth(), 0, 0);
		top.setHeight((int) elements.getHeight());
		top.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		top.drawRaw();

		bottom.setPos(elements.getWidth(), getHeight() - elements.getHeight(), 0);
		bottom.setHeight((int) elements.getHeight());
		bottom.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		bottom.drawRaw();

		bg.setPos(elements.getWidth(), elements.getHeight(), 0);
		bg.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		bg.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		bg.drawRaw();

		endStandardDraw();
		lStrip.setPos(right.getPos().x + UIScale.getUIScale().MAIN_WINDOW_STRIP_0_X, right.getPos().y, 0);
		lStrip.draw();
		
		lStrip.setHeight((int) left.getHeight());
		lStrip.setPos(left.getPos().x + UIScale.getUIScale().MAIN_WINDOW_STRIP_1_X, left.getPos().y, 0);
		lStrip.draw();
		
		drawCross(UIScale.getUIScale().MAIN_WINDOW_CROSS_X, UIScale.getUIScale().MAIN_WINDOW_CROSS_Y);

		GlUtil.glColor4fForced(1, 1, 1, 1);
	}

	@Override
	public void onInit() {
		super.onInit();

		elements = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"UI_MainTheme-2x2-gui-"), getState());
		elements.onInit();

		top = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI_MainThemeTopBottom-2x1-gui-").getMaterial().getTexture(), 0.0f, 0.0f);
		bottom = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI_MainThemeTopBottom-2x1-gui-").getMaterial().getTexture(), 0.0f, 0.0f);

		left = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI_MainThemeLeftRight-1x2-gui-").getMaterial().getTexture(), 0.0f, 0.0f);
		right = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI_MainThemeLeftRight-1x2-gui-").getMaterial().getTexture(), 0.0f, 0.0f);

		bg = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI_MainThemeBg-gui-").getMaterial().getTexture(), 0.0f, 0.0f);

		top.onInit();
		bottom.onInit();
		left.onInit();
		right.onInit();

		
		
		lStrip = new GUIGradientVerticalStrip(getState(), UIScale.getUIScale().MAIN_WINDOW_STRIP_W, 10,
				MainWindowFramePalette.windowGradientStripeStart,
				MainWindowFramePalette.windowGradientStripeMid,
				MainWindowFramePalette.windowGradientStripeEnd);
		lStrip.onInit();

		innerBackground = new GUIInnerBackground(getState(), this, UIScale.getUIScale().MAIN_WINDOW_INNER_BG_INSET);
		innerBackground.onInit();

		innerForeground = new GUIInnerForeground(getState(), this, UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET);
		innerForeground.onInit();

//		addTab(Lng.str("DEFAULT");

		this.tabPane = new GUITabPane(getState(), this);
		this.tabPane.onInit();

		this.init = true;
	}

	@Override
	protected int getMinWidth() {
		return (int) (elements.getWidth() * 2);
	}

	@Override
	protected int getMinHeight() {
		return (int) (elements.getHeight() * 2);
	}

	@Override
	public int getTopDist() {
		if (tabs.size() > 1) {
			//leave space for tabs pane
			return UIScale.getUIScale().MAIN_WINDOW_TABS_HEIGHT;
		} else {
			return 0;
		}
	}

	public GUIContentPane addTab(Object name) {
		GUIContentPane defaultPane = new GUIContentPane(getState(), this, name);
		defaultPane.onInit();
		tabs.add(defaultPane);
		//INSERTED CODE
		MainWindowTabAddEvent event = new MainWindowTabAddEvent(this, defaultPane, name);
		StarLoader.fireEvent(event, false);
		if(event.isCanceled()) this.tabs.remove(defaultPane);
		///
		return defaultPane;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#attach(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void attach(GUIElement o) {
		assert (tabs.size() > 0);
		assert (tabs.get(0).getTextboxes().size() > 0);
		tabs.get(0).getTextboxes().get(0).getContent().attach(o);
//		super.attach(o);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#detach(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void detach(GUIElement o) {
		tabs.get(0).getTextboxes().get(0).getContent().detach(o);
//		super.detach(o);
	}

	/**
	 * @return the tabs
	 */
	@Override
	public ObjectArrayList<GUIContentPane> getTabs() {
		return tabs;
	}

	/**
	 * @return the selectedTab
	 */
	@Override
	public int getSelectedTab() {
		return selectedTab;
	}

	/**
	 * @param selectedTab the selectedTab to set
	 */
	@Override
	public void setSelectedTab(int selectedTab) {
		this.selectedTab = selectedTab;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITabInterface#getInnerWidthTab()
	 */
	@Override
	public int getInnerWidthTab() {
		return getInnerWidth();
	}

	@Override
	public int getInnerCornerDistX() {
		return innerForeground.cornerDistanceX;
	}

	@Override
	public int getInnerCornerTopDistY() {
		return innerForeground.cornerDistanceTopY;
	}
	@Override
	public int getInnerCornerBottomDistY() {
		return innerForeground.cornerDistanceBottomY;
	}
	@Override
	public int getInnerHeigth() {
		return (int) innerForeground.getHeight();
	}

	@Override
	public int getInnerWidth() {
		return (int) innerForeground.getWidth();
	}

	@Override
	public int getInnerOffsetX() {
		return UIScale.getUIScale().MAIN_WINDOW_xInnerOffset;
	}

	@Override
	public int getInnerOffsetY() {
		return UIScale.getUIScale().MAIN_WINDOW_yInnerOffset;
	}

	@Override
	public int getInset() {
		return UIScale.getUIScale().MAIN_WINDOW_INNER_FB_INSET;
	}

	public void clearTabs() {
		selectedTab = 0;
		for (int i = 0; i < tabs.size(); i++) {
			tabs.get(i).cleanUp();
		}
		tabs.clear();
	}


}
