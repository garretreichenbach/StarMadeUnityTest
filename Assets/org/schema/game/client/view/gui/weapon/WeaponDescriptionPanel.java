package org.schema.game.client.view.gui.weapon;

import java.util.ArrayList;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.powerbattery.PowerBatteryCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class WeaponDescriptionPanel extends GUIElement {

	private ArrayList<Object> text;
	private GUITextOverlay textOverlay;
	private GUIElementList weaponElementList;
	private GUIScrollablePanel scrollPanel;
	private boolean firstDraw = true;
	private GUIListElement elem;
	private GUIAnchor textOverlayAncor;

	public WeaponDescriptionPanel(InputState state, FontInterface font, int width, int height) {
		super(state);
		scrollPanel = new GUIScrollablePanel(width, height, state);
		weaponElementList = new GUIElementList(state);
		textOverlayAncor = new GUIAnchor(state, UIScale.getUIScale().scale(256), UIScale.getUIScale().scale(200));
		textOverlay = new GUITextOverlay(font, state);
		text = new ArrayList<Object>();
		text.add("");
		textOverlay.setColor(Color.green);
		textOverlayAncor.attach(textOverlay);
		scrollPanel.setContent(textOverlayAncor);

	}

	public WeaponDescriptionPanel(InputState state,
			FontInterface font, GUIElement dependend) {
		super(state);
		scrollPanel = new GUIScrollablePanel(UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(100), dependend, state);
		weaponElementList = new GUIElementList(state);
		textOverlayAncor = new GUIAnchor(state, UIScale.getUIScale().scale(256), UIScale.getUIScale().scale(200));
		textOverlay = new GUITextOverlay(font, state);
		text = new ArrayList<Object>();
		text.add("");
		textOverlay.setColor(Color.green);
		textOverlayAncor.attach(textOverlay);
		scrollPanel.setContent(textOverlayAncor);
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (isNewHud()) {
			textOverlay.setText(this.text);
		}
		GlUtil.glPushMatrix();
		transform();
		scrollPanel.draw();

		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {
		scrollPanel.onInit();
		firstDraw = false;
	}

	@Override
	public float getHeight() {
		return scrollPanel.getHeight();
	}

	@Override
	public float getWidth() {
		return scrollPanel.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	/**
	 * @return the text
	 */
	public ArrayList<Object> getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(ArrayList<Object> text) {
		this.text = text;
	}

	public void reset() {
		if (isNewHud()) {
			text.set(0, "");
			scrollPanel.setContent(textOverlayAncor);
		}
	}

	public void update(ElementCollectionManager<?, ?, ?> selectedManager) {
		if (selectedManager.getContainer().getSegmentController() != ((GameClientState) getState()).getCurrentPlayerObject()) {
			//do not update panel for other controllers
			return;
		}
		weaponElementList.clear();
		if (selectedManager instanceof PowerBatteryCollectionManager) {
			final GUIElementList l = new GUIElementList(getState());
			loadEntries(selectedManager, l, null, null);
			scrollPanel.setContent(l);
		}else if (selectedManager instanceof ControlBlockElementCollectionManager<?, ?, ?>) {
			weaponElementList.clear();
			final ControlBlockElementCollectionManager<?, ?, ?> w = (ControlBlockElementCollectionManager<?, ?, ?>) selectedManager;
			String support = "";
			String effect = "";

			final GUIElementList l = new GUIElementList(getState());

			StringBuffer b = new StringBuffer();

			final ControlBlockElementCollectionManager<?, ?, ?> supportCol = w.getSupportCollectionManager();
			final ControlBlockElementCollectionManager<?, ?, ?> effectCol = w.getEffectCollectionManager();

			b.append(Lng.str("Type: %s\nGroups: %d\n", w.getModuleName(),  w.getElementCollections().size()));

			if (supportCol != null) {
				float ratio = CombinationAddOn.getRatio(w, supportCol);
				b.append(Lng.str("Support:\n   %s (%f%%)\n", supportCol.getModuleName(), ratio* 100f));
			}
			if (effectCol != null) {
				float ratio = CombinationAddOn.getRatio(w, effectCol);
				b.append(Lng.str("Effect:\n   %s (%f%%)\n", effectCol.getModuleName(), ratio * 100f));
			}

			l.setScrollPane(scrollPanel);
			if (w.getElementCollections().size() > 20) {
				GUITextButton loadButton = new GUITextButton(getState(), 400, 20, Lng.str("Load %d entries (may be slow at >500)", w.getElementCollections().size()), new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						l.remove(elem);
						loadEntries(w, l, supportCol, effectCol);

					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				});
				elem = new GUIListElement(loadButton, loadButton, getState());
				l.add(elem);
			} else {
				loadEntries(w, l, supportCol, effectCol);
			}

			scrollPanel.setContent(l);

		} else {
			System.err.println("EXCEPTION: UNKNOWN MANAGER: " + selectedManager);
		}

	}

	private void loadEntries(ElementCollectionManager<?, ?, ?> w, GUIElementList l, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		GUIKeyValueEntry[] guiCollectionStats = w.getGUICollectionStats();
		if (guiCollectionStats != null) {
			for (GUIKeyValueEntry e : guiCollectionStats) {
				GUIAnchor g = e.get((GameClientState) getState());

				GUIListElement le = new GUIListElement(g, g, getState());
				l.add(le);
			}
		}

		for (int i = 0; i < w.getElementCollections().size(); i++) {
			ElementCollection u = w.getElementCollections().get(i);
			ControllerManagerGUI e = u.createUnitGUI((GameClientState) getState(), supportCol, effectCol);
			if (e != null) {
				GUIListElement listEntry = e.getListEntry((GameClientState) getState(), l);
				l.add(listEntry);
			} else {
				System.err.println("Not creating weapon panel entry for " + u);
			}
		}
	}

	public void update(SegmentController selectedManager) {
		if (isNewHud()) {
			return;
		}
		if (selectedManager instanceof SegmentController) {
			StringBuffer b = new StringBuffer();
			b.append("Undock " + selectedManager.toNiceString() + "\n" +
							"from you by executing this!\n"
			);
			text.set(0, b.toString());
			scrollPanel.setContent(textOverlayAncor);
		}
	}

	public void update(String selectedManager) {
		if (isNewHud()) {
			return;
		}
		if (selectedManager.equals("CORE")) {
			StringBuffer b = new StringBuffer();
			b.append("Type: 		Docking Beam\n" +
							"Location:		" + Ship.core + "\n"
			);
			text.set(0, b.toString());
			scrollPanel.setContent(textOverlayAncor);
		} else if (selectedManager.equals("DOCK")) {
			StringBuffer b = new StringBuffer();
			b.append("Undock yourself by executing this!\n"
			);
			text.set(0, b.toString());
			scrollPanel.setContent(textOverlayAncor);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		if (scrollPanel.getContent() == weaponElementList) {
			weaponElementList.update(timer);
		}
		super.update(timer);
	}

}
