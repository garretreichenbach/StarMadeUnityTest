package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUIHorizontalButton extends GUIHorizontalArea {

	
	private GUITextOverlay overlay;
	
	private Vector3i sizeHelp;
	private HButtonType defaultType;
	private boolean initHelp;

	private void init(Object text, FontInterface size, GUICallback callback, GUIActiveInterface activeInterface,
			GUIActivationCallback actCallback) {
		this.defaultType = type;
		setCallback(callback);
		this.actCallback = actCallback;
		this.activeInterface = activeInterface;
		sizeHelp = new Vector3i();
		overlay = new GUITextOverlay(size, getState()) {
			@Override
			public void onDirty() {
				sizeHelp.x = getFont().getWidth(getText().get(0).toString());
			}

		};
		overlay.setTextSimple(text);

		setMouseUpdateEnabled(true);

		GUIScrollablePanel lr = new GUIScrollablePanel(this.getWidth(), this.getHeight(), this, getState());
		lr.setScrollable(0);
		lr.setLeftRightClipOnly = true;
		lr.setContent(overlay);
		attach(lr);
	}

	public GUIHorizontalButton(InputState state, HButtonColor color, Object text, GUICallback callback,
			GUIActiveInterface activeInterface, GUIActivationCallback actCallback) {
		this(state, color, FontSize.MEDIUM_15, text, callback, activeInterface, actCallback);
	}
	public GUIHorizontalButton(InputState state, HButtonColor color, FontInterface size, Object text, GUICallback callback,
			GUIActiveInterface activeInterface, GUIActivationCallback actCallback) {
		super(state, color, 10);
		init(text, size, callback, activeInterface, actCallback);
	}

	public GUIHorizontalButton(InputState state, HButtonType type, Object text, GUICallback callback,
			GUIActiveInterface activeInterface, GUIActivationCallback actCallback) {
		this(state, type, FontSize.MEDIUM_15, text, callback, activeInterface, actCallback);
	}
	public GUIHorizontalButton(InputState state, HButtonType type, FontInterface size, Object text, GUICallback callback,
			GUIActiveInterface activeInterface, GUIActivationCallback actCallback) {
		super(state, type, 10);
		init(text, size, callback, activeInterface, actCallback);
	}

	public String getTextToString() {
		return overlay.getText().toString();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		overlay.cleanUp();
	}

	@Override
	public void draw() {
		if (isInside()) {
		}
		if (actCallback == null || actCallback.isVisible(getState())) {
			if(!initHelp){
				sizeHelp.x = overlay.getFont().getWidth(overlay.getText().get(0).toString());
				initHelp = true;
			}
			
			boolean active = actCallback == null || actCallback.isActive(getState());
			boolean highlight = actCallback != null && actCallback instanceof GUIActivationHighlightCallback
					&& ((GUIActivationHighlightCallback) actCallback).isHighlighted(getState());
			if (!active) {
				overlay.setColor(0.78f, 0.78f, 0.78f, 1f);
			} else {
				overlay.setColor(1f, 1f, 1f, 1f);
			}
			setMouseUpdateEnabled(active && (activeInterface == null || activeInterface.isActive()));
				
			setType(HButtonType.getType(defaultType, active && isInside()
					&& getState().getController().getInputController().getCurrentActiveDropdown() == null
					&& (getState().getController().getInputController().getCurrentContextPane() == getState()
							.getController().getInputController().getCurrentContextPaneDrawing()),
					active, highlight));

			overlay.setPos((int) (getWidth() / 2 - sizeHelp.x / 2), 4, 0);
			super.draw();
		}
	}

	public HButtonType getDefaultType() {
		return defaultType;
	}

	public void setDefaultType(HButtonType defaultType) {
		this.defaultType = defaultType;
	}

	public void setFont(FontInterface font) {
		overlay.setFont(font);
	}

	public void setText(String s) {
		overlay.setTextSimple(s);
		overlay.onInit();
	}
}
