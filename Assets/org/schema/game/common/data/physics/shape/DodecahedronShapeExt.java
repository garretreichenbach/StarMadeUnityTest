package org.schema.game.common.data.physics.shape;

import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.vecmath.Vector3f;

public class DodecahedronShapeExt extends org.schema.game.common.data.physics.ConvexHullShapeExt implements GameShape {

	public Dodecahedron dodecahedron;
	private SimpleTransformableSendableObject obj;

	public DodecahedronShapeExt(ObjectArrayList<Vector3f> points, SimpleTransformableSendableObject obj) {
		super(points);
		this.obj = obj;
	}

	@Override
	public SimpleTransformableSendableObject getSimpleTransformableSendableObject() {
		return obj;
	}

	@Override
	public String toString() {
		return "DodecahedronShapeExt(R" + dodecahedron.radius + ")";
	}

	
}
