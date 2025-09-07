package org.schema.game.common.controller.elements.transporter;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class TransporterUnit extends ElementCollection<TransporterUnit, TransporterCollectionManager, TransporterElementManager> {

	Vector3i min = new Vector3i();
	Vector3i max = new Vector3i();
	Vector3f minf = new Vector3f();
	Vector3f maxf = new Vector3f();
	Vector3f minOut = new Vector3f();
	Vector3f maxOut = new Vector3f();
	Vector3f minOtherOut = new Vector3f();
	Vector3f maxOtherOut = new Vector3f();
	Vector3f minBoxOther = new Vector3f(1, 1, 1);
	Vector3f maxBoxOther = new Vector3f(1, 1, 1);
	public float getPowerNeeded(SimpleTransformableSendableObject forJumpEntity) {

		return 0;
	}

	public float getPowerConsumption() {

		return (size()) * TransporterElementManager.POWER_CONST_NEEDED_PER_BLOCK;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TransporterUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}


	public void update(Timer timer) {
		
	}


	

	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		
		getMin(min);
		getMax(max);


	}
}