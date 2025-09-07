package org.schema.game.common.data.physics.shape;

import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.common.data.physics.ConvexHullShapeExt;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.vecmath.Vector3f;

public class SphereShapeExt extends ConvexHullShapeExt implements GameShape {

	private final SimpleTransformableSendableObject<?> obj;
	private final float radius;

	public SphereShapeExt(float radius, float graduation, SimpleTransformableSendableObject<?> obj) {
		super(calculatePoints(radius, graduation));
		this.radius = radius;
		this.obj = obj;
	}

	public SphereShapeExt(float radius, SimpleTransformableSendableObject<?> obj) {
		this(radius, 0.25f, obj);
	}

	private static ObjectArrayList<Vector3f> calculatePoints(float radius, float graduation) {
		ObjectArrayList<Vector3f> points = new ObjectArrayList<>();
		for(float phi = 0; phi < Math.PI; phi += graduation) {
			for(float theta = 0; theta < 2 * Math.PI; theta += graduation) {
				float x = (float) (radius * Math.sin(phi) * Math.cos(theta));
				float y = (float) (radius * Math.sin(phi) * Math.sin(theta));
				float z = (float) (radius * Math.cos(phi));
				points.add(new Vector3f(x, y, z));
			}
		}
		return points;
	}

	@Override
	public SimpleTransformableSendableObject<?> getSimpleTransformableSendableObject() {
		return obj;
	}

	@Override
	public String toString() {
		return "SphereShapeExt(R" + radius + ")";
	}
}