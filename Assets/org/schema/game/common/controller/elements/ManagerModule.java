
package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;

public abstract class ManagerModule<E extends ElementCollection<E, EC, EM>, EC extends ElementCollectionManager<E, EC, EM>, EM extends UsableElementManager<E, EC, EM>> {

	private final EM elementManager;
	private final short elementID;

	private ManagerModule<?, ?, ?> next;

	public ManagerModule(EM usableCollectionElementManager, short elementID) {
		this.elementManager = usableCollectionElementManager;
		this.elementID = elementID;
	}

	public void addControlledBlock(Vector3i from, short fromType, Vector3i to, short toType) {
	}

	public void clear() {
	}

	/**
	 * @return the elementID
	 */
	public short getElementID() {
		return elementID;
	}

	public EM getElementManager() {
		return elementManager;
	}

	/**
	 * @return the next
	 */
	public ManagerModule<?, ?, ?> getNext() {
		return next;
	}

	/**
	 * @param next the next to set
	 */
	public void setNext(ManagerModule<?, ?, ?> next) {
		this.next = next;
	}

	public void onConnectionRemoved(Vector3i controller, Vector3i controlled,
	                                short controlledType) {
	}

	@Override
	public String toString() {
		return "(" + getElementManager().getClass().getSimpleName() + ": " + (elementID == Element.TYPE_RAIL_TRACK ? "TYPE_RAIL_TRACK" : (elementID == Element.TYPE_RAIL_INV ? "TYPE_RAIL_INV" : (elementID == Element.TYPE_ALL ? "TYPE_ALL" : ElementKeyMap.getInfo(elementID).getName()))) + ")";
	}

	public abstract void update(Timer timer, long time);

	public ControllerManagerGUI createGUI(GameClientState state) {
		if (this instanceof ManagerModuleCollection<?, ?, ?>) {
			ManagerModuleCollection<?, ?, ?> mm = (ManagerModuleCollection<?, ?, ?>) this;
			GUIElementList list = new GUIElementList(state);

			GUIKeyValueEntry[] val = mm.getGUIElementCollectionValues();
			
			for (int i = 0; i < val.length; i++) {
				GUIAnchor guiAnchor = val[i].get(state);
				list.addWithoutUpdate(new GUIListElement(guiAnchor, guiAnchor, state));
			}
			
			
			for (ElementCollectionManager<?, ?, ?> m : mm.getCollectionManagers()) {
//				System.err.println("CCC "+list.size()+"; "+m.getClass()+"; "+m.rawCollection.size());
				ControllerManagerGUI createGUI = m.createGUI(state);
				assert (createGUI != null);
				GUIListElement listEntry = createGUI.getListEntry(state, list);
				assert (listEntry != null);
				list.addWithoutUpdate(listEntry);
			}
			list.updateDim();
			ControllerManagerGUI r = new ControllerManagerGUI();
			r.createFrom(state, mm, list);
			assert (r.check()) : r;
			return r;
		} else if (this instanceof ManagerModuleSingle<?, ?, ?>) {

			ManagerModuleSingle<?, ?, ?> mm = (ManagerModuleSingle<?, ?, ?>) this;
			ElementCollectionManager<?, ?, ?> m = mm.getCollectionManager();
			return m.createGUI(state);
		}
		throw new NullPointerException();
	}

	public double calculateWeaponDamageIndex() {
		if (elementManager instanceof WeaponElementManagerInterface) {
			return ((WeaponElementManagerInterface) elementManager).calculateWeaponDamageIndex();
		}
		return 0;
	}

	public double calculateWeaponHitPropabilityIndex() {
		if (elementManager instanceof WeaponElementManagerInterface) {
			return ((WeaponElementManagerInterface) elementManager).calculateWeaponHitPropabilityIndex();
		}
		return 0;
	}

	public double calculateWeaponSpecialIndex() {
		if (elementManager instanceof WeaponElementManagerInterface) {
			return ((WeaponElementManagerInterface) elementManager).calculateWeaponSpecialIndex();
		}
		return 0;
	}

	public double calculateWeaponRangeIndex() {
		if (elementManager instanceof WeaponElementManagerInterface) {
			return ((WeaponElementManagerInterface) elementManager).calculateWeaponRangeIndex();
		}
		return 0;
	}

	public double calculateWeaponPowerConsumptionPerSecondIndex() {
		if (elementManager instanceof WeaponElementManagerInterface) {
			return ((WeaponElementManagerInterface) elementManager).calculateWeaponPowerConsumptionPerSecondIndex();
		}
		return 0;
	}

	public double calculateSupportIndex() {
		if (elementManager instanceof SupportElementManagerInterface) {
			return ((SupportElementManagerInterface) elementManager).calculateSupportIndex();
		}
		return 0;
	}

	public double calculateSupportPowerConsumptionPerSecondIndex() {
		if (elementManager instanceof SupportElementManagerInterface) {
			return ((SupportElementManagerInterface) elementManager).calculateSupportPowerConsumptionPerSecondIndex();
		}
		return 0;
	}

	public double calculateStealthIndex(double scoreForConstant) {
		if (elementManager instanceof StealthElementManagerInterface) {
			return ((StealthElementManagerInterface) elementManager).calculateStealthIndex(scoreForConstant);
		}
		return 0;
	}

	public void init(ManagerContainer container) {
		elementManager.init(container);
	}

	public abstract void onFullyLoaded();

	
	public abstract boolean needsAnyUpdate();
	
}
