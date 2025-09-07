package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIEnterableListBlockedInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIEnterableListOnExtendedCallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerTextbox;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIEnterableList extends GUIAnchor implements GUICallback {

	protected final GUIElementList list;

	public Vector4f expandedBackgroundColor;

	private int expandedHiglightBottomDist = 32;

	public float extendedHighlightBottomDist() {
		return UIScale.getUIScale().scale(expandedHiglightBottomDist);
	}

	public void extendedHighlightBottomDistSet(int i) {
		expandedHiglightBottomDist = i;
	}

	public GUIScrollablePanel scrollPanel;

	public GUIEnterableListOnExtendedCallback extendedCallback;

	public GUIEnterableListOnExtendedCallback extendedCallbackSelector;

	public GUIEnterableListBlockedInterface extendableBlockedInterface;

	protected GUIElement collapsedButton;

	protected GUIElement backButton;

	private boolean collapsed = true;

	private boolean flagSwitch = true;

	private boolean init = false;

	private int indention;

	private GUIColoredRectangle extendedBg;

	private GUIInnerTextbox innerTextbox;

	public GUIEnterableList(InputState state) {
		this(state, new GUIElementList(state));
	}

	@Override
	public void notifyObservers() {
		super.notifyObservers(true);
	}

	public GUIEnterableList(InputState state, GUIElement collapsedButton, GUIElement backButton) {
		this(state, new GUIElementList(state), collapsedButton, backButton);
	}

	public GUIEnterableList(InputState state, GUIElementList list) {
		this(state, list, null, null);
	}

	public GUIEnterableList(InputState state, GUIElementList list, GUIElement collapsedButton, GUIElement backButton) {
		super(state);
		this.list = list;
		this.collapsedButton = collapsedButton;
		this.backButton = backButton;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(scrollPanel != null && (!scrollPanel.isActive() || !scrollPanel.isInside())) {
			// not active or not inside, so nothing to do;
			return;
		}
		if(event.pressedLeftMouse() && canClick()) {
			if((callingGuiElement == collapsedButton || callingGuiElement.getUserPointer() == collapsedButton.getUserPointer()) && collapsed) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
				AudioController.fireAudioEventID(6);
				collapsed = false;
				if(extendedCallback != null) {
					extendedCallback.extended();
				}
				if(extendedCallbackSelector != null) {
					extendedCallbackSelector.extended();
				}
				flagSwitch = true;
			} else if((callingGuiElement == backButton || callingGuiElement.getUserPointer() == backButton.getUserPointer()) && !collapsed) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
				AudioController.fireAudioEventID(5);
				collapsed = true;
				if(extendedCallbackSelector != null) {
					extendedCallbackSelector.collapsed();
				}
				flagSwitch = true;
			} else {
				assert (false) : "caller not known: '" + callingGuiElement + "; " + callingGuiElement.getUserPointer() + "': closed: " + collapsedButton + "; open " + backButton + "; collapsed " + collapsed + "; " + (callingGuiElement == collapsedButton) + "; " + (callingGuiElement == backButton);
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return scrollPanel != null && scrollPanel.isInsideScrollBar();
	}

	protected boolean canClick() {
		return extendableBlockedInterface == null || !extendableBlockedInterface.isBlocked();
	}

	@Override
	public void cleanUp() {
		if(collapsedButton != null) {
			collapsedButton.cleanUp();
		}
		if(backButton != null) {
			backButton.cleanUp();
		}
		if(list != null) {
			list.cleanUp();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		doDraw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElementList#onInit()
	 */
	@Override
	public void onInit() {
		if(collapsedButton == null) {
			GUITextOverlay col = new GUITextOverlay(FontSize.SMALL_15, getState());
			col.setTextSimple(Lng.str("ENTER"));
			collapsedButton = col;
			collapsedButton.setName("COLBUTTON");
		}
		if(backButton == null) {
			GUITextOverlay exp = new GUITextOverlay(FontSize.SMALL_15, getState());
			exp.setTextSimple(Lng.str("ENTER"));
			backButton = exp;
			backButton.setName("BACKBUTTON");
		}
		if(expandedBackgroundColor != null) {
			extendedBg = new GUIColoredRectangleLeftRightShadow(getState(), (int) list.getWidth(), (int) list.getHeight(), expandedBackgroundColor);
			// extendedBg.renderMode = RENDER_MODE_SHADOW;
			extendedBg.onInit();
			innerTextbox = new GUIInnerTextbox(getState());
			// innerTextbox.renderMode = RENDER_MODE_SHADOW;
			innerTextbox.onInit();
		}
		collapsedButton.setCallback(this);
		collapsedButton.setMouseUpdateEnabled(true);
		backButton.setCallback(this);
		backButton.setMouseUpdateEnabled(true);
		this.attach(collapsedButton);
		this.setMouseUpdateEnabled(true);
		list.setMouseUpdateEnabled(true);
		list.onInit();
		init = true;
	}

	@Override
	public float getHeight() {
		if(collapsed) {
			return collapsedButton.getHeight();
		} else {
			if(expandedBackgroundColor != null) {
				return backButton.getHeight() + extendedBg.getHeight();
			} else {
				return backButton.getHeight() + list.getHeight();
			}
		}
	}

	@Override
	public float getWidth() {
		if(collapsed) {
			return collapsedButton.getWidth();
		} else {
			return list.getWidth();
		}
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	private void adjustBg() {
		if(collapsed) {
			extendedBg.getPos().y = collapsedButton.getHeight();
		} else {
			extendedBg.getPos().y = backButton.getHeight();
		}
		if(expandedBackgroundColor != null) {
			extendedBg.setColor(expandedBackgroundColor);
		}
		extendedBg.getPos().x = indention;
		extendedBg.setWidth(collapsedButton.getWidth());
		extendedBg.setHeight(list.getHeight() + extendedHighlightBottomDist());
		innerTextbox.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		innerTextbox.setWidth(collapsedButton.getWidth() - UIScale.getUIScale().scale(20));
		innerTextbox.setHeight(list.getHeight());
		list.getPos().x = 0;
		list.getPos().y = 0;
	}

	private void doDraw() {
		if(expandedBackgroundColor != null) {
			adjustBg();
		} else {
			if(collapsed) {
				list.getPos().y = collapsedButton.getHeight();
			} else {
				list.getPos().y = backButton.getHeight();
			}
			list.getPos().x = indention;
		}
		switchCollapsed(false);
		assert (!isExpended() || (getChilds().contains(backButton) && !getChilds().contains(collapsedButton))) : getChilds();
		assert (isExpended() || (getChilds().contains(collapsedButton) && !getChilds().contains(backButton))) : getChilds() + "; " + collapsedButton + "; " + backButton;
		// if(isExpended()){
		// System.err.println("BACK: "+backButton+"; "+((GUIColoredAncor)backButton).getColor()+"; "+backButton.getWidth()+"; "+backButton.getHeight()+"; "+backButton.getPos()+"; "+getChilds());
		// }
		super.draw();
		// System.err.println("DRAW ENTERABLE "+getChilds());
		// if(isMouseUpdateEnabled()){
		// checkMouseInside();
		// if(isInside()){
		// System.err.println("ENTERABLE LIST INSODE");
		// }
		// }
	}

	/**
	 * @return the list
	 */
	public GUIElementList getList() {
		return list;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public boolean isExpended() {
		return !collapsed;
	}

	public void setExpanded(boolean b) {
		if(b) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
			AudioController.fireAudioEventID(8);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
			AudioController.fireAudioEventID(7);
		}
		collapsed = !b;
		flagSwitch = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#attach(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void attach(GUIElement o) {
		assert (!getChilds().contains(o));
		super.attach(o);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#setCallback(org.schema.schine.graphicsengine.forms.gui.GUICallback)
	 */
	@Override
	public void setCallback(GUICallback callback) {
		assert (false);
		super.setCallback(callback);
	}

	@Override
	public boolean isInside() {
		if(getParent() != null) {
			// System.err.println(this+" -> PARENT "+getParent()+" --- "+((GUIElement)getParent()).isInside());
			return ((GUIElement) getParent()).isInside() && super.isInside();
		} else {
			// System.err.println("HAS NO PARENT "+this);
		}
		return super.isInside();
	}

	public void switchCollapsed(boolean notify) {
		if(flagSwitch) {
			this.detachAll();
			if(collapsed) {
				this.attach(collapsedButton);
				// System.err.println("GUI EXPANDABLE SET TO COLLAPSED");
			} else {
				list.updateDim();
				if(extendedBg != null) {
					this.attach(extendedBg);
					extendedBg.detachAll();
					extendedBg.attach(innerTextbox);
					innerTextbox.getContent().detachAll();
					innerTextbox.getContent().attach(list);
					// adjustBg();
				} else {
					this.attach(list);
				}
				this.attach(backButton);
			}
			flagSwitch = false;
			if(notify) {
				// ShopCategoryList is listening
				notifyObservers();
			}
		}
	}

	/**
	 * @return the indention
	 */
	public int getIndention() {
		return indention;
	}

	/**
	 * @param indention the indention to set
	 */
	public void setIndention(int indention) {
		this.indention = indention;
	}

	public GUIInnerTextbox getInnerTextbox() {
		return innerTextbox;
	}
}
