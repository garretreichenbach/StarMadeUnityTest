package org.schema.common.util.linAlg;

import javax.vecmath.Vector3f;

public class Triangle {
	public Triangle(Vector3f a, Vector3f b, Vector3f c) {
		v1 = a;
		v2 = b;
		v3 = c;
	}
	public Vector3f v1;
	public Vector3f v2;
	public Vector3f v3;
	private Vector3f normal;
	@Override
	public String toString(){
		return "["+v1+", "+v2+", "+v3+"]";
	}
	public Vector3f getNormal(){
		if(normal == null){
			normal = calculateNormal(v1, v2, v3);
		}
		return normal;
	}
	public Vector3f calculateNormal(Vector3f v1, Vector3f v2, Vector3f v3){
		return calculateNormal(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z,
				v3.x, v3.y, v3.z);
	}
	
	public static Vector3f calculateNormal(float vX1, float vY1, float vZ1, float vX2, float vY2, float vZ2, float vX3,
			float vY3, float vZ3) {

		Vector3f edge1 = new Vector3f(vX1-vX2, vY1-vY2, vZ1-vZ2);
		Vector3f edge2 = new Vector3f(vX2-vX3, vY2-vY3, vZ2-vZ3);

		Vector3f crsProd = new Vector3f();
		crsProd.cross(edge1, edge2); // Cross product between edge1 and
		
											// edge2

		crsProd.normalize();; // Normalization of the vector

		return crsProd;
	}
}
