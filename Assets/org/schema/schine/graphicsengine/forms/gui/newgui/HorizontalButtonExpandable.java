package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.network.client.ClientState;

public class HorizontalButtonExpandable extends GUIHorizontalArea{

	
	private GUIActivationCallback actCallback;
	private GUITextOverlay overlay;
	public GUIActiveInterface activeInterface;
	private Vector3i sizeHelp;
	private HButtonType defaultType;




	public HorizontalButtonExpandable(ClientState state, HButtonType type, Object text, GUICallback callback, GUIActiveInterface activeInterface, GUIActivationCallback actCallback) {
		super(state, type, 10);
		this.defaultType = type;
		setCallback(callback);
		this.actCallback = actCallback;
		this.activeInterface = activeInterface;
		sizeHelp = new Vector3i();
		overlay = new GUITextOverlay(FontSize.MEDIUM_15, getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#onDirty()
			 */
			@Override
			public void onDirty() {
				sizeHelp.x = getFont().getWidth(getText().get(0).toString());
			}

		};
		sizeHelp.x = overlay.getFont().getWidth(text.toString());
		overlay.setTextSimple(text);

		setMouseUpdateEnabled(true);


		GUIScrollablePanel lr = new GUIScrollablePanel(this.getWidth(), this.getHeight(), this, getState());
		lr.setScrollable(0);
		lr.setLeftRightClipOnly = true;
		lr.setContent(overlay);
		attach(lr);
	}
	
	
	
	
	@Override
	public void draw() {
		if(isInside()){
		}
		if (actCallback == null || actCallback.isVisible(getState())) {
			boolean active = actCallback == null || actCallback.isActive(getState());
			boolean highlight = actCallback != null &&
					actCallback instanceof GUIActivationHighlightCallback &&
					((GUIActivationHighlightCallback) actCallback).isHighlighted(getState());
			if (!active) {
				overlay.setColor(0.78f, 0.78f, 0.78f, 1f);
			} else {
				overlay.setColor(1f, 1f, 1f, 1f);
			}
			setMouseUpdateEnabled(active);

			if ((activeInterface == null || activeInterface.isActive()) && (!getCallback().isOccluded() || !active)) {
				setType(HButtonType.getType(defaultType, isInside() && getState().getController().getInputController().getCurrentActiveDropdown() == null && (getState().getController().getInputController().getCurrentContextPane() == getState().getController().getInputController().getCurrentContextPaneDrawing()), active, highlight));
			}

			overlay.setPos((int) (getWidth() / 2 - sizeHelp.x / 2), 4, 0);
			super.draw();
		}
	}
}
