package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUITabbedContent extends GUIAnchor implements GUIWindowInterface, GUITabInterface {


	private final ObjectArrayList<GUIContentPane> tabs = new ObjectArrayList<GUIContentPane>();

	private int selectedTab;
	private GUIElement dependend;
	private GUITabPane tabPane;
	private GUIInnerForeground innerForeground;
	private boolean init;

	public GUITabbedContent(InputState state, GUIElement dependend) {
		super(state);
		this.dependend = dependend;
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

	@Override
	public int getInnerWidthTab() {
		return (int) dependend.getWidth();
	}

	public void clearTabs() {
		selectedTab = 0;
		for (int i = 0; i < tabs.size(); i++) {
			tabs.get(i).cleanUp();
		}
		tabs.clear();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		assert (init);

		GlUtil.glPushMatrix();
		transform();
		setWidth(dependend.getWidth());
		setHeight(dependend.getHeight());
		assert (tabs.size() > 0);
		if (tabs.size() == 1) {
			innerForeground.cornerDistanceTopY = 0;//INNER_FB_INSET;
			innerForeground.cornerDistanceBottomY = UIScale.getUIScale().TABBED_WINDOW_cornerDistanceBottomY;
			innerForeground.upperCap = true;
			innerForeground.draw();
			tabs.get(0).draw();
		} else {
			innerForeground.cornerDistanceTopY = 0;//INNER_FB_INSET;
			innerForeground.cornerDistanceBottomY = UIScale.getUIScale().TABBED_WINDOW_cornerDistanceBottomY;
//			innerForeground.extraHeight = TABS_HEIGHT;
			innerForeground.upperCap = false;
			innerForeground.draw();

			tabPane.setPos(getInset(), getInset(), 0);
			tabPane.draw();
			tabs.get(selectedTab).draw();
		}

		int size = getChilds().size();

		for (int i = 0; i < size; i++) {
			getChilds().get(i).draw();
		}
		GlUtil.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		setWidth(dependend.getWidth());
		setHeight(dependend.getHeight());

		this.innerForeground = new GUIInnerForeground(getState(), this, 0);
		this.innerForeground.onInit();

		this.tabPane = new GUITabPane(getState(), this);
		this.tabPane.onInit();

		this.init = true;
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

	public GUIContentPane addTab(String name) {
		GUIContentPane defaultPane = new GUIContentPane(getState(), this, name);
		defaultPane.onInit();
		tabs.add(defaultPane);

		return defaultPane;
	}

	@Override
	public int getInnerCornerDistX() {
		return 0;
	}

	@Override
	public int getInnerCornerTopDistY() {
		return 0;
	}

	@Override
	public int getInnerHeigth() {
		return (int) dependend.getHeight() - UIScale.getUIScale().MAIN_WINDOW_TABBED_INNER_HEIGHT_DIST;
	}

	@Override
	public int getInnerWidth() {
		return (int) dependend.getWidth() - UIScale.getUIScale().MAIN_WINDOW_TABBED_INNER_WIDTH_DIST;
	}

	@Override
	public int getInnerOffsetX() {
		return UIScale.getUIScale().MAIN_WINDOW_TABBED_getInnerOffsetX;
	}

	@Override
	public int getInnerOffsetY() {
		return UIScale.getUIScale().MAIN_WINDOW_TABBED_getInnerOffsetY;
	}

	@Override
	public int getInset() {
		return 0;
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

	@Override
	public int getInnerCornerBottomDistY() {
		return 0;
	}
}
