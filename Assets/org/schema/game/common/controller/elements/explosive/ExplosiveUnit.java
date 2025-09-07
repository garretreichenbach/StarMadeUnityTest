package org.schema.game.common.controller.elements.explosive;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

public class ExplosiveUnit extends ElementCollection<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> {

	//	float explosive;

	//	public void refreshExplosiveCapabilities() {
	//		explosive = getBBTotalSize();
	//		float tot = (float) Math.pow(size(), ExplosiveElementManager.BONUS_PER_UNIT);
	//		//		System.err.println("COLLECTION: "+tot+" ("+size()+")");
	//		explosive += tot;
	//		explosive = Math.max(1f, explosive);
	//	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#significatorUpdate(int, int, int, int, int, int, int, int, int, long)
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin,
	                                  int zMin, int xMax, int yMax, int zMax, long index) {
		super.significatorUpdateMin(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Explosive Module"), this, new ModuleValueEntry(Lng.str("Damage"), elementCollectionManager.getElementManager().getDamage()));
	}

}
