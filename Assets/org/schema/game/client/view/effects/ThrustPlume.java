package org.schema.game.client.view.effects;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Controller;

import com.bulletphysics.linearmath.Transform;

public class ThrustPlume implements Comparable<ThrustPlume> {

	private static final Vector3f tV = new Vector3f();
	private static final Vector3f tO = new Vector3f();

	//	private static final Vector3f tV2 = new Vector3f();
	//	private static final Vector3f tO2 = new Vector3f();
	private static final Vector3f helper = new Vector3f();
	private static final Transform t = new Transform();
	private static final Vector3f localTranslation = new Vector3f(-50, 0, 0);
	private SegmentController controller;
	private Vector3i position;
	private int orientation;

	public ThrustPlume() {
	}

	@Override
	public int compareTo(ThrustPlume o) {
		if(o == null) return 1;
		if(o.hashCode() == hashCode()) return 0;
		
		getWorldTransform(t, localTranslation);
		tV.set(t.origin);

		o.getWorldTransform(t, localTranslation);
		tO.set(t.origin);

		//these are absolute position. thats why we dont need local cam vectors
		tV.sub(Controller.getCamera().getPos());
		tO.sub(Controller.getCamera().getPos());

		int val = (int) ((tV.length() * 10000.0f) - (tO.length() * 10000.0f));
		return val == 0 ? 1 : val; // to prevent equalness
	}

	public void getWorldTransform(Transform out, Vector3f translate) {
		t.set(controller.getWorldTransformOnClient());
		
		//Get the orientation
		Matrix3f rotation = Element.getRotationPerSide(orientation, Element.BACK);
		t.basis.mul(rotation);
		
		//Get the position
		helper.set((position.x - SegmentData.SEG_HALF) + translate.x, (position.y - SegmentData.SEG_HALF) + translate.y, (position.z - SegmentData.SEG_HALF) + translate.z);
		t.basis.transform(helper);
		t.origin.add(helper);
		out.set(t);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return position.hashCode() + controller.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(getClass()) && position.equals(((ThrustPlume) obj).position) && controller == ((ThrustPlume) obj).controller;
	}

	public void reset() {
		controller = null;
		position = null;
		orientation = 0;
	}

	public void set(SegmentController controller, Vector3i position, int orientation) {
		this.controller = controller;
		this.position = position;
		this.orientation = orientation;
	}
}
