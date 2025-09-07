package org.schema.game.common.controller.elements.shipyard;

import javax.vecmath.Vector3f;

import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class ShipyardUnit extends ElementCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> {

	final Vector3i min = new Vector3i();
	final Vector3i max = new Vector3i();
	public int xDelta;
	public int yDelta;
	public int zDelta;
	public boolean xDim;
	public boolean yDim;
	public boolean zDim;
	private String invalidReason;
	public int normalPos;
	public long endA;
	public long endB;



	@Override
	public boolean isValid() {
		//super valid checks the neighbor count to be exactly 2
		return (xDim || yDim || zDim) && super.isValid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ShipYardUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	public String getValidInfo() {


		boolean dimOk = (xDim || yDim || zDim);

		return "DimOK: " + dimOk + " (" + xDelta + ", " + yDelta + ", " + zDelta + "); 2Neighbors: " + super.isValid();
	}


	public void debugDraw(Vector3i block) {
		debugDraw(block.x, block.y, block.z);
	}

	public void debugDraw(int x, int y, int z) {
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			float scale = 0.51f;
			Transform t = new Transform(getSegmentController().getWorldTransform());
			Vector3f p = new Vector3f();
			p.set(x, y, z);
			p.x -= SegmentData.SEG_HALF;
			p.y -= SegmentData.SEG_HALF;
			p.z -= SegmentData.SEG_HALF;
			t.basis.transform(p);
			t.origin.add(p);
			DebugBox bo = new DebugBox(new Vector3f(-scale, -scale, -scale), new Vector3f(scale, scale, scale), t, 1, 0, 0, 1);
			bo.LIFETIME = 200;
			DebugDrawer.boxes.add(bo);
		}
	}


	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		
		getMin(min);
		getMax(max);

		xDelta = max.x - min.x;
		yDelta = max.y - min.y;
		zDelta = max.z - min.z;

		xDim = xDelta == 1 && yDelta > 1 && zDelta > 1;
		yDim = xDelta > 1 && yDelta == 1 && zDelta > 1;
		zDim = xDelta > 1 && yDelta > 1 && zDelta == 1;

		boolean dimOk = (xDim || yDim || zDim);
		if(!dimOk){
			invalidReason = Lng.str("Unit Group Invalid! Must be a flat 'C'-shape.");
			LogUtil.sy().fine(getSegmentController()+"; "+this+" Invalid shipyard shape");
		}else {
			LogUtil.sy().fine(getSegmentController()+"; "+this+" Ok shipyard shape");
		}
	}

	@Override
	public String getInvalidReason() {
		return invalidReason;
	}

	@Override
	public void setInvalidReason(String invalidReason) {
		this.invalidReason = invalidReason;
	}

	

}