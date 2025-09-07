package org.schema.game.client.view.gui.structurecontrol;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.forms.gui.*;

import javax.vecmath.Vector4f;

public class ControllerManagerGUI {

	public GUIElementList sub;
	public GUIElement collapsedButton;
	public GUIElement backButton;
	public GUIColoredRectangle backGround;

	public ControllerManagerGUI() {
		super();
	}

	public static ControllerManagerGUI create(GameClientState state, String name, ElementCollection col, GUIKeyValueEntry... entries) {
		GUIElementList list = new GUIElementList(state);

		for(GUIKeyValueEntry e : entries) {
			GUIAnchor g = e.get(state);
			list.add(new GUIListElement(g, g, state));
		}
		ControllerManagerGUI gui = new ControllerManagerGUI();

		Collapsed: {
			GUIOverlay overlay = IconDatabase.getRightArrowInstance16(state);
			overlay.setUserPointer("collapsed");
			overlay.setMouseUpdateEnabled(true);

			GUITextOverlay text = new GUITextOverlay(state);
			text.setUserPointer("collapsed");
			text.setTextSimple(name + ": [ID: " + ElementCollection.getPosFromIndex(col.idPos, new Vector3i()) + "]");
			text.setPos(overlay.getWidth() + 4, 0);

			overlay.attach(text);
			gui.collapsedButton = overlay;
		}

		Entered: {
			GUIOverlay overlay = IconDatabase.getDownArrowInstance16(state);
			overlay.setUserPointer("entered");
			overlay.setMouseUpdateEnabled(true);

			GUITextOverlay text = new GUITextOverlay(state);
			text.setUserPointer("entered");
			text.setTextSimple(name + ": [ID: " + ElementCollection.getPosFromIndex(col.idPos, new Vector3i()) + "]");
			text.setPos(overlay.getWidth() + 4, 0);

			overlay.attach(text);
			gui.backButton = overlay;
		}

		gui.backGround = new GUIColoredRectangle(state, 200, 20, new Vector4f());
		gui.sub = list;
		return gui;
	}

	public GUIListElement getListEntry(GameClientState state, final GUIElementList list) {
		assert (collapsedButton != null);
		assert (backButton != null);
		GUIEnterableList el = new GUIEnterableList(state, sub, collapsedButton, backButton);
		el.addObserver(updateListDim -> list.updateDim());
		el.getList().addObserverRecusive(updateListDim -> list.updateDim());
		el.setIndention(10);
		GUIListElement l = new GUIListElement(el, el, state);

		return l;
	}

	public void createFrom(GameClientState state, ManagerModuleCollection<?, ?, ?> mm, GUIElementList list) {
		sub = list;

		Collapsed: {
			GUIOverlay overlay = IconDatabase.getRightArrowInstance16(state);
			overlay.setUserPointer("collapsed");
			overlay.setMouseUpdateEnabled(true);

			GUITextOverlay text = new GUITextOverlay(state);
			text.setUserPointer("collapsed");
			text.setTextSimple(mm.getManagerName());
			text.setPos(overlay.getWidth() + 4, 0);

			overlay.attach(text);
			collapsedButton = overlay;
		}

		Entered: {
			GUIOverlay overlay = IconDatabase.getDownArrowInstance16(state);
			overlay.setUserPointer("entered");
			overlay.setMouseUpdateEnabled(true);

			GUITextOverlay text = new GUITextOverlay(state);
			text.setUserPointer("entered");
			text.setTextSimple(mm.getManagerName());
			text.setPos(overlay.getWidth() + 4, 0);

			overlay.attach(text);
			backButton = overlay;
		}

		this.backGround = new GUIColoredRectangle(state, 200, 20, new Vector4f());
		assert (this.check()) : this;
	}

	public void createFromElementCollection(GameClientState state, ElementCollectionManager<?, ?, ?> mm, GUIElementList list) {
		this.sub = list;
		assert (mm.getModuleName() != null);
		Collapsed: {
			GUIOverlay overlay = IconDatabase.getRightArrowInstance16(state);
			overlay.setUserPointer("collapsed");
			overlay.setMouseUpdateEnabled(true);

			GUITextOverlay text = new GUITextOverlay(state);
			text.setUserPointer("collapsed");
			text.setTextSimple(mm.getModuleName());
			text.setPos(overlay.getWidth() + 4, 0);

			overlay.attach(text);
			collapsedButton = overlay;
		}

		Entered: {
			GUIOverlay overlay = IconDatabase.getDownArrowInstance16(state);
			overlay.setUserPointer("entered");
			overlay.setMouseUpdateEnabled(true);

			GUITextOverlay text = new GUITextOverlay(state);
			text.setUserPointer("entered");
			text.setTextSimple(mm.getModuleName());
			text.setPos(overlay.getWidth() + 4, 0);

			overlay.attach(text);
			backButton = overlay;
		}

		backGround = new GUIColoredRectangle(state, 200, 20, new Vector4f());

		assert (this.check()) : this;

	}

	public boolean check() {
		return (collapsedButton != null)
				&& (backButton != null)
				&& (sub != null)
				&& (backGround != null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ControllerManagerGUI [sub=" + sub + ", collapsedButton="
				+ collapsedButton + ", backButton=" + backButton
				+ ", backGround=" + backGround + "]";
	}

}
