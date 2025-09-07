package org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew;

import org.schema.common.util.StringTools;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUISliderBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public abstract class GUIScrollSettingSelector extends GUISettingsElement implements GUICallback, GUIScrollableInterface {

	private final GUIActivatableTextBar textBar;
	public GUIElement dep;
	public int manualXDistance;
	public int manualXWidthMod;
	public int widthMod;
	public int posMoxX;
	public LabelPosition labelPosition = LabelPosition.TOP;
	private GUISliderBar scroller;
	private boolean init;
	private int height;
	private int width;
	private final GUITextOverlay nameLabel;
	private final float marginY = 3;
	/**
	 * @param state
	 * @param scrollMode GUIScrollablePanel.SCROLLABLE_HORIZONTAL, GUIScrollablePanel.SCROLLABLE_VERTICAL
	 * @param j
	 * @param i
	 */
	protected GUIScrollSettingSelector(InputState state, int scrollMode, int width) {
		this(state, scrollMode, width, FontLibrary.FontSize.BIG_24);
	}

	protected GUIScrollSettingSelector(InputState state, int scrollMode, int width, FontLibrary.FontInterface settingsFont) {
		super(state);


		GUIElement pp = new GUIElement(state) {

			@Override
			public void onInit() {
			}

			@Override
			public void draw() {
			}

			@Override
			public void cleanUp() {
			}

			@Override
			public float getWidth() {
				return UIScale.getUIScale().scale(38);
			}

			@Override
			public float getHeight() {
				return UIScale.getUIScale().h;
			}
		};

		textBar = new GUIActivatableTextBar(state, FontLibrary.FontSize.MEDIUM_15, "VAL", pp, new TextCallback() {
			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				if(entry.trim().length() <= 0) {
					setSettingX(getMinX());
				}
				textBar.deactivateBar();
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t -> {
			while(t.length() > 0) {
				try {
					float i = Float.parseFloat(t);
					if(i > getMaxX()) {
						i = getMaxX();
					}

					setSettingX(i);
					return String.valueOf(getSettingX());
				} catch(NumberFormatException e) {
//						e.printStackTrace();
				}
				t = t.substring(0, t.length() - 1);
			}
			return t;
		}) {

			@Override
			protected void onBecomingInactive() {
				if(getText().trim().length() <= 0) {
					setSettingX(getMinX());
				} else {
					try {
						Float.parseFloat(getText().trim());
					} catch(NumberFormatException e) {
						setSettingX((int) getMinX());
					}
				}
			}

			@Override
			public void draw() {
//				System.err.println("GETTEXT: "+getText()+"; "+getTT()+"; "+textBar);
				super.draw();
			}

		};
		textBar.setDeleteOnEnter(false);
		textBar.dependendDistanceFromRight = 0;
		scroller = new GUISliderBar(state, this, scrollMode) {
			@Override
			public GUIActivatableTextBar getTextBar() {
				return textBar;
			}
		};
		textBar.activeInterface = this::isActive;
		this.width = width;
		scroller.setScrollAmountClickInLaneUIScaled(1);
		scroller.setScrollAmountClickOnArrowUIScaled(1);
		scroller.setScrollContinuoslyWhenArrorPressed(false);
		setMouseUpdateEnabled(true);
		setCallback(this);

		nameLabel = new GUITextOverlay(settingsFont, getState());
		nameLabel.setTextSimple("DEFAULT NAME");
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(event.pressedRightMouse()) {
			resetScrollValue();
		}
		if(getRelMousePos().x < scroller.getTopPosX()) {
			scroller.scrollLock();
			if(event.dWheel > 0) {
				scrollHorizontal(scroller.getScrollAmountClickOnArrow());
			}
			if(event.dWheel < 0) {
				scrollHorizontal(-scroller.getScrollAmountClickOnArrow());
			}
		}
	}

	public void resetScrollValue() {
		//overwrite if needed
		setSettingX(getMinX());
	}

	@Override
	public boolean isActive() {
		return super.isActive() && (dep == null || dep.isActive());
	}

	@Override
	public void cleanUp() {
		scroller.cleanUp();
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		if(dep != null) {
			if(manualXDistance > 0) {
				width = (int) dep.getWidth() - manualXDistance + manualXWidthMod;
			} else {
				width = (int) dep.getWidth() + widthMod;
			}
			height = 30;
		}
		if(showLabel() && labelPosition == LabelPosition.TOP) {
			nameLabel.setPos((int) (getWidth() * 0.5f - nameLabel.getMaxLineWidth() * 0.5f), 0, 0);
			scroller.setPos(posMoxX, nameLabel.getTextHeight() + marginY, 0);
		} else {
			scroller.setPos(posMoxX, marginY, 0);
			if(showLabel() && labelPosition == LabelPosition.RIGHT) {
				width = (int) (getWidth() * 0.7f);
				nameLabel.setPos(getWidth() + UIScale.getUIScale().scale(8), scroller.getHeight() * 0.5f - nameLabel.getTextHeight() * 0.5f, 0);
			}
		}
		if(manualXDistance > 0) {
			scroller.getPos().x = manualXDistance;
		}
		GlUtil.glPushMatrix();
		transform();
		if(isActive()) {
			checkMouseInside();
		}
		scroller.draw();
		if(showLabel()) {
			nameLabel.draw();
		}
		GlUtil.glPopMatrix();
		textBar.setText(StringTools.formatPointZeroZero(getSettingX()));
	}

	public boolean showLabel() {
		return true;
	}

	@Override
	public void onInit() {

		nameLabel.onInit();

		settingChanged(null);

		init = true;

	}

	@Override
	protected void doOrientation() {
	}

	public void setNameLabel(Object name) {
		nameLabel.setTextSimple(name);
	}

	@Override
	public float getHeight() {
		return scroller.getHeight() + (showLabel() ? nameLabel.getTextHeight() : 0) + marginY;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public float getWidth() {
		return (manualXDistance > 0 ? manualXDistance : 0) + getScrollBarWidth();//+leftArrow.getWidth()+rightArrow.getWidth();
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	protected abstract void decSetting();

	protected abstract void incSetting();

	protected abstract float getSettingX();

	protected abstract void setSettingX(float value);

	protected abstract float getSettingY();

	protected abstract void setSettingY(float value);

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUISettingsElement#settingChanged(java.lang.Object)
	 */
	@Override
	public void settingChanged(Object setting) {
		super.settingChanged(setting);

		float value = (setting != null && setting instanceof Float) ? (Float) setting : getSettingX();
//		try {
//			throw new Exception("SETT "+textBar+"; "+setting+"; bar "+textBar.getText()+" -> setting "+getSettingX()+" ::::: USING: "+valInt);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(textBar != null && textBar.getText() != null && !textBar.getText().equals(String.valueOf(value))) {
			String val = String.valueOf(value);
			textBar.setTextWithoutCallback(val);

			assert (textBar.getText().equals(val));
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#getScolledPercentHorizontal()
	 */
	@Override
	public float getScolledPercentHorizontal() {
		return (getSettingX() - getMinX()) / (getMaxX() - getMinX());
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#getScolledPercentVertical()
	 */
	@Override
	public float getScolledPercentVertical() {
		return (getSettingY() - getMinY()) / (getMaxY() - getMinY());
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#scrollHorizontal(float)
	 */
	@Override
	public void scrollHorizontal(float step) {
		setSettingX(Math.min(Math.max(getMinX(), getSettingX() + step), getMaxX()));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#scrollVertical(float)
	 */
	@Override
	public void scrollVertical(float step) {
		setSettingY(Math.min(Math.max(getMinY(), getSettingY() + step), getMaxY()));

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#getScrollingListener()
	 */
	@Override
	public ScrollingListener getScrollingListener() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#scrollHorizontalPercent(float)
	 */
	@Override
	public void scrollHorizontalPercent(float percent) {
		setSettingX(Math.max(getMinX(), Math.min(getMinX() + percent * (getMaxX() - getMinX()), getMaxX())));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#scrollVerticalPercent(float)
	 */
	@Override
	public void scrollVerticalPercent(float percent) {
		setSettingY(Math.max(getMinY(), Math.min(getMinY() + percent * (getMaxY() - getMaxY()), getMaxY())));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#getScrollBarHeight()
	 */
	@Override
	public float getScrollBarHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#getScrollBarWidth()
	 */
	@Override
	public float getScrollBarWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#scrollHorizontalPercentTmp(float)
	 */
	@Override
	public void scrollHorizontalPercentTmp(float percent) {
		setSettingXTmp(Math.min(Math.max(getMinX(), percent * getMaxX()), getMaxX()));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface#scrollVerticalPercentTmp(float)
	 */
	@Override
	public void scrollVerticalPercentTmp(float percent) {
		setSettingYTmp(Math.min(Math.max(getMinY(), percent * getMaxY()), getMaxY()));
	}

	public abstract float getMaxX();

	public abstract float getMaxY();

	public abstract float getMinX();

	public abstract float getMinY();

	/**
	 * @return the scroller
	 */
	public GUISliderBar getScroller() {
		return scroller;
	}

	/**
	 * @param scroller the scroller to set
	 */
	public void setScroller(GUISliderBar scroller) {
		this.scroller = scroller;
	}

	protected void setSettingXTmp(float value) {
	}

	protected void setSettingYTmp(float value) {
	}

	@Override
	public float getContentToPanelPercentageY() {
		return 1.0F;
	}

	@Override
	public float getContentToPanelPercentageX() {
		return 1.0F;
	}

	public void set(float i) {
		setSettingX(i);
	}

	public enum LabelPosition {
		TOP, RIGHT
	}

}
