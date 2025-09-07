package org.schema.game.common.data;

import com.bulletphysics.collision.shapes.TriangleShape;
import com.bulletphysics.linearmath.Transform;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.TriBoundingBoxVariables;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class Dodecahedron {

	static Vector3f colors[] = new Vector3f[]{
			new Vector3f(1.0f, 0.0f, 0.0f), //red, 0
			new Vector3f(0.0f, 1.0f, 0.0f), //green, 1
			new Vector3f(0.0f, 0.0f, 1.0f), //blue, 2
			new Vector3f(1.0f, 1.0f, 0.0f), //yellow, 3
			new Vector3f(1.0f, 0.0f, 1.0f), //purple, 4
			new Vector3f(0.0f, 1.0f, 1.0f), //cyan, 5
			new Vector3f(1.0f, 1.0f, 1.0f), //white, 6
			new Vector3f(1.0f, 0.5f, 0.0f), //orange, 7
			new Vector3f(0.5f, 1.0f, 0.0f), //greenish yellow, 8
			new Vector3f(0.5f, 1.0f, 0.5f), //light green, 9
			new Vector3f(0.0f, 0.5f, 0.0f), //dark green, 10
			new Vector3f(0.0f, 0.5f, 0.5f), //dark cyan, 11
	};
	private static float[][] tc = new float[][]{{0, 0}, {0, 1}, {1, 1}, {1, 0}, {0.5f, 0.5f}};
	public static boolean debug;
	public final float radius; /* any radius in which the polyhedron is inscribed */
	public final Vector3f poly[][] = new Vector3f[12][5];
	public final Vector3f centers[] = new Vector3f[12];
	public final Vector3f normals[][] = new Vector3f[12][5];
	public final TriangleShape[][] shapes = new TriangleShape[12][5];
	Vector3f vertices[] = new Vector3f[20];/*
	 * 20 vertices with x, y, z
	 * coordinate
	 */
	float Pi = FastMath.PI;
	float phiaa = 52.62263590f; /* the two phi angles needed for generation */
	float phibb = 10.81231754f;
	TriBoundingBoxVariables v = new TriBoundingBoxVariables();
	Vector3f[] triTest = new Vector3f[3];
	Vector3f[] triTestRay = new Vector3f[3];
	{
		for(int i = 0; i < triTestRay.length; i++){
			triTestRay[i] = new Vector3f();
		}
	}
	private int dList;

	//	public Dodecahedron(){
	//
	//	}
	public Dodecahedron(float radius) {
		this.radius = radius;
	}

	public static boolean pnpoly(Vector3f[] vert, float testx, float testy) {
		int i, j;
		boolean c = false;
		for (i = 0, j = vert.length - 1; i < vert.length; j = i++) {
			if (((vert[i].z > testy) != (vert[j].z > testy))
					&& (testx < (vert[j].x - vert[i].x) * (testy - vert[i].z)
					/ (vert[j].z - vert[i].z) + vert[i].x)) {
				c = !c;
			}
		}
		return c;
	}

	public static boolean pnpoly(Vector3f[] vert, float testx, float testy, float margin) {
		int i, j;
		float ls = (testx * testx) + (testy * testy);
		if (ls == 0) {
			return true;
		}
		boolean c = false;
		for (i = 0, j = vert.length - 1; i < vert.length; j = i++) {
			if (((vert[i].z * margin > testy) != (vert[j].z * margin > testy))
					&& (testx < (vert[j].x * margin - vert[i].x * margin) * (testy - vert[i].z * margin)
					/ (vert[j].z * margin - vert[i].z * margin) + vert[i].x * margin)) {
				c = !c;
			}
		}
		return c;
	}

	public static float nearestEdge(Vector3f[] vert, float testx, float testy, float mar) {
		float min = 1000000;
		min = Math.min(min, pointToLineDistance(vert[0], vert[1], testx, testy, mar));
		min = Math.min(min, pointToLineDistance(vert[1], vert[2], testx, testy, mar));
		min = Math.min(min, pointToLineDistance(vert[2], vert[3], testx, testy, mar));
		min = Math.min(min, pointToLineDistance(vert[3], vert[4], testx, testy, mar));
		min = Math.min(min, pointToLineDistance(vert[4], vert[0], testx, testy, mar));

		return min;
	}

	public static float pointToLineDistance(Vector3f A, Vector3f B, float Px, float Pz, float mar) {
		float normalLength = FastMath.carmackSqrt((B.x * mar - A.x * mar) * (B.x * mar - A.x * mar)
				+ (B.z * mar - A.z * mar) * (B.z * mar - A.z * mar));
		return FastMath.abs((Px - A.x * mar) * (B.z * mar - A.z * mar) - (Pz - A.z * mar)
				* (B.x * mar - A.x * mar))
				/ normalLength;
	}

	public void create() {

		float phia = Pi * phiaa / 180.0f; /* 4 sets of five points each */
		float phib = Pi * phibb / 180.0f;
		float phic = Pi * (-phibb) / 180.0f;
		float phid = Pi * (-phiaa) / 180.0f;
		float the72 = Pi * 72.0f / 180f;
		float theb = the72 / 2.0f; /* pairs of layers offset 36 degrees */
		float the = 0.0f;
		for (int i = 0; i < 5; i++) {
			vertices[i] = new Vector3f();
			vertices[i].x = radius * FastMath.cos(the) * FastMath.cos(phia);
			vertices[i].y = radius * FastMath.sin(the) * FastMath.cos(phia);
			vertices[i].z = radius * FastMath.sin(phia);
			the = the + the72;
		}
		the = 0.0f;
		for (int i = 5; i < 10; i++) {
			vertices[i] = new Vector3f();
			vertices[i].x = radius * FastMath.cos(the) * FastMath.cos(phib);
			vertices[i].y = radius * FastMath.sin(the) * FastMath.cos(phib);
			vertices[i].z = radius * FastMath.sin(phib);
			the = the + the72;
		}
		the = theb;
		for (int i = 10; i < 15; i++) {
			vertices[i] = new Vector3f();
			vertices[i].x = radius * FastMath.cos(the) * FastMath.cos(phic);
			vertices[i].y = radius * FastMath.sin(the) * FastMath.cos(phic);
			vertices[i].z = radius * FastMath.sin(phic);
			the = the + the72;
		}
		the = theb;
		for (int i = 15; i < 20; i++) {
			vertices[i] = new Vector3f();
			vertices[i].x = radius * FastMath.cos(the) * FastMath.cos(phid);
			vertices[i].y = radius * FastMath.sin(the) * FastMath.cos(phid);
			vertices[i].z = radius * FastMath.sin(phid);
			the = the + the72;
		}


		/* map vertices to 12 faces */
		polygon(0, 0, 1, 2, 3, 4);

		polygon(1, 0, 1, 6, 10, 5);
		polygon(2, 1, 2, 7, 11, 6);
		polygon(3, 2, 3, 8, 12, 7);
		polygon(4, 3, 4, 9, 13, 8);
		polygon(5, 4, 0, 5, 14, 9);

		polygon(6, 15, 16, 11, 6, 10);
		polygon(7, 16, 17, 12, 7, 11);
		polygon(8, 17, 18, 13, 8, 12);
		polygon(9, 18, 19, 14, 9, 13);
		polygon(10, 19, 15, 10, 5, 14);

		polygon(11, 15, 16, 17, 18, 19);

		inverse(1); //green inverse
		inverse(2); //blue inverse
		inverse(3); //yellow inverse
		inverse(4); //purple inverse
		inverse(5); //cyan inverse
		inverse(11); //dark cyan inverse

		for (int i = 0; i < 12; i++) {
			cresteCollisionShapes(i, poly[i], centers[i]);
		}

	}

	public void draw() {
		//		System.err.println("DRAW DODECA: "+radius);
		//		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F5)){
		//			for(int i = 0; i < 12; i++){
		//				polygonDraw(i);
		//			}
		//		}else{

		if (dList == 0) {
			dList = GL11.glGenLists(1);
			GL11.glNewList(dList, GL11.GL_COMPILE);
			for (int i = 0; i < 12; i++) {
				polygonDraw(i);
			}
			GL11.glEndList();
		}
		GL11.glCallList(dList);
		//		}
		//		for(int i = 0; i < 12; i++){
		//			areaDraw(i);
		//		}
		GlUtil.glColor4f(1, 1, 1, 1);
	}

	private void inverse(int i) {
		ArrayUtils.reverse(poly[i]);
	}

	public Transform getTransform(int side, Transform out, float f, float r) {

		out.setIdentity();
		out.origin.set(centers[side]);
		Vector3f up = new Vector3f(centers[side]);
		up.normalize();
		Vector3f right = new Vector3f(poly[side][0]);
		right.sub(centers[side]);
		right.normalize();
		Vector3f forward = new Vector3f();
		forward.cross(right, up);
		forward.normalize();

		Vector3f addForw = new Vector3f(forward);
		addForw.scale(f);
		out.origin.add(addForw);

		Vector3f addright = new Vector3f(right);
		addright.scale(r);
		out.origin.add(addright);

		GlUtil.setUpVector(up, out);
		GlUtil.setRightVector(right, out);
		GlUtil.setForwardVector(forward, out);

		return out;
	}

	public void polygon(int polyV, int v0, int v1, int v2, int v3, int v4) {
		poly[polyV] = new Vector3f[]{vertices[v0], vertices[v1], vertices[v2], vertices[v3], vertices[v4]};
		normals[polyV] = new Vector3f[]{new Vector3f(vertices[v0]), new Vector3f(vertices[v1]), new Vector3f(vertices[v2]), new Vector3f(vertices[v3]), new Vector3f(vertices[v4])};
		for (int i = 0; i < normals[polyV].length; i++) {
			normals[polyV][i].normalize();
		}
		float x = (vertices[v0].x + vertices[v1].x + vertices[v2].x + vertices[v3].x + vertices[v4].x) / 5f;
		float y = (vertices[v0].y + vertices[v1].y + vertices[v2].y + vertices[v3].y + vertices[v4].y) / 5f;
		float z = (vertices[v0].z + vertices[v1].z + vertices[v2].z + vertices[v3].z + vertices[v4].z) / 5f;
		centers[polyV] = new Vector3f(x, y, z);
	}

	public void polygonDraw(int polyV) {

		Vector3f[] poly = this.poly[polyV];

		//		ShaderLibrary.perpixelShader.loadWithoutUpdate();
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GL11.glBegin(GL11.GL_POLYGON);

		GlUtil.glColor4f(colors[polyV].x, colors[polyV].y, colors[polyV].z, 1.0f);

		Vector3f a = new Vector3f(poly[0]);
		Vector3f b = new Vector3f(poly[1]);
		Vector3f c = new Vector3f(poly[2]);

		Vector3f ab = new Vector3f();
		ab.sub(b, a);
		Vector3f ac = new Vector3f();
		ac.sub(c, a);
		Vector3f normal = new Vector3f();
		normal.cross(ab, ac);
		normal.normalize();
		GL11.glNormal3f(normal.x, normal.y, normal.z);

		for (int i = 0; i < 5; i++) {
			GL11.glTexCoord2f(tc[i][0], tc[i][1]);
			GL11.glVertex3f(poly[i].x, poly[i].y, poly[i].z);
		}

		GL11.glEnd();

		Vector3f nCenter = new Vector3f(centers[polyV]);
		nCenter.normalize();
		nCenter.scale(10);
		nCenter.add(centers[polyV]);

		GlUtil.glColor4f(colors[(polyV + 6) % 12].x, colors[(polyV + 6) % 12].y, colors[(polyV + 6) % 12].z, 1.0f);

		//		GL11.glBegin(GL11.GL_LINES);
		//		GL11.glVertex3f(centers[polyV].x, centers[polyV].y, centers[polyV].z);
		//		GL11.glVertex3f(nCenter.x, nCenter.y, nCenter.z);
		//
		//		GL11.glEnd();

		GlUtil.glColor4f(1, 1, 1, 1.0f);

		//		ShaderLibrary.perpixelShader.unloadWithoutExit();
	}

	public void areaDraw(int polyV) {
		Vector3f[] poly = this.poly[polyV];
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glColor4f(1, 1, 1, 0.3f);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		float scale = 10;
		for (int i = 0; i < 5; i++) {
			GL11.glVertex3f(poly[i].x, poly[i].y, poly[i].z);
			GL11.glVertex3f(poly[i].x * scale, poly[i].y * scale, poly[i].z * scale);
		}
		GL11.glVertex3f(poly[0].x, poly[0].y, poly[0].z);
		GL11.glVertex3f(poly[0].x * scale, poly[0].y * scale, poly[0].z * scale);
		GL11.glEnd();
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
	}

	public Vector3f[] getPolygon(int side) {
		Vector3f[] poly = new Vector3f[5];
		for (int i = 0; i < 5; i++) {
			poly[i] = new Vector3f(this.poly[side][i]);
		}
		return poly;
	}

	public boolean testAABB(Vector3f min, Vector3f max) {
		for (int i = 0; i < poly.length; i++) {
			if (testTriangles(poly[i], centers[i], min, max)) {
				return true;
			}
		}

		return false;
	}
	public boolean testAABB(Vector3f min, Vector3f max, boolean[][] overlapping) {
		assert (overlapping.length == poly.length);
		assert (overlapping[0].length == 6);
		boolean overlap = false;
		for (int i = 0; i < poly.length; i++) {
			if (testTriangles(poly[i], centers[i], min, max, overlapping, i)) {
				overlap = true;
			}
		}
		return overlap;
	}

	private boolean testTriangles(Vector3f[] penta, Vector3f center, Vector3f min,
	                              Vector3f max, boolean[][] overlapping, int polyIndex) {
		boolean overlap = false;
		triTest[2] = center;
		for (int i = 0; i < 5; i++) {
			triTest[0] = penta[i];
			triTest[1] = penta[(i + 1) % 5];

			//			overlapping[polyIndex][i+1] = AabbUtil2.testTriangleAgainstAabb2(triTest, min, max);
			overlapping[polyIndex][i + 1] = BoundingBox.intersectsTriangle(triTest, min, max, v);
			overlap = overlap || overlapping[polyIndex][i + 1];
			//			if(overlapping[polyIndex][i+1] && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
			//				DebugDrawer.lines.add(new DebugLine(triTest[0], triTest[1]));
			//				DebugDrawer.lines.add(new DebugLine(triTest[1], triTest[2]));
			//				DebugDrawer.lines.add(new DebugLine(triTest[2], triTest[0]));
			//
			//				DebugDrawer.boundingBoxes.add(new DebugBoundingBox(min, max, 1, 0, 1, 1));
			//			}
		}
		overlapping[polyIndex][0] = overlap;
		return overlap;
	}

	public boolean intersectsOuterRadius(Transform dTrans, Vector3f min, Vector3f max, float margin) {
		;
		if (BoundingBox.testPointAABB(dTrans.origin, min, max)) {
			// Do special code.
			// here, for now don't do a collision, until the centre is
			// outside teh box
			//	         fDcoll = 0.0f;
			//	         xNcoll = Vector(0, 0, 0);
			return true;
		}

		// get closest point on box from sphere centre
		float x = (dTrans.origin.x < min.x) ? min.x : (dTrans.origin.x > max.x) ? max.x : dTrans.origin.x;
		float y = (dTrans.origin.y < min.y) ? min.y : (dTrans.origin.y > max.y) ? max.y : dTrans.origin.y;
		float z = (dTrans.origin.z < min.z) ? min.z : (dTrans.origin.z > max.z) ? max.z : dTrans.origin.z;

		// find the separation
		float xDiff = dTrans.origin.x - x;
		float yDiff = dTrans.origin.y - y;
		float zDiff = dTrans.origin.z - z;

		// check if points are far enough
		float fDistSquared = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;

		if (fDistSquared > radius * radius - margin) {
			return false;
		}

		//	    float fDist = sqrt(fDistSquared);
		//
		//	    // collision depth
		//	    fDcoll = xSphere.GetRadius() - fDist;
		//
		//	    // normal of collision (going towards the sphere centre)
		//	    xNcoll = xDiff  / fDist;

		return true;
	}

	private void cresteCollisionShapes(int polyIndex, Vector3f[] penta, Vector3f center) {

		triTest[2] = center;
		for (int i = 0; i < 5; i++) {
			triTest[0] = penta[i];
			triTest[1] = penta[(i + 1) % 5];

			TriangleShape shape = new TriangleShape();
			for (int j = 0; j < 3; j++) {
				shape.vertices1[j].set(triTest[j]);
			}
			shapes[polyIndex][i] = shape;
		}

	}
	Vector3f closest = new Vector3f();
	public boolean testRay(Vector3f dodecaPos, Vector3f from, Vector3f to) {
		dir.sub(to, from);
		len = FastMath.carmackLength(dir);
		FastMath.normalizeCarmack(dir);
		
//		float c = 10000000;
//		for (int i = 0; i < poly.length; i++) {
//			if(Vector3fTools.diffLength(from, Vector3fTools.add(dodecaPos, centers[i])) < c){
//				c = Vector3fTools.diffLength(from, Vector3fTools.add(dodecaPos, centers[i]));
//				closest.set(Vector3fTools.add(dodecaPos, centers[i]));
//			}
//		}
		
		for (int i = 0; i < poly.length; i++) {
			if (rayTestTriangles(dodecaPos, poly[i], centers[i], from, to, dir)) {
				if(Dodecahedron.debug){
					Vector3f f = new Vector3f(from);
					Vector3f ti = new Vector3f(v.intersection);
//					ti.sub(from);
					DebugDrawer.lines.add(new DebugLine(f, ti, new Vector4f( 1,0,1,0.7f)));
				}
				return true;
			}
		}
		
		return false;
	}

	private boolean rayTestTriangles(Vector3f dodecaPos, Vector3f[] penta, Vector3f center, Vector3f from,
			Vector3f to, Vector3f dir) {
		
		triTest[2].add(center, dodecaPos);
		
		for (int i = 0; i < 5; i++) {
			triTest[0].add(penta[i], dodecaPos);
			triTest[1].add(penta[(i + 1) % 5], dodecaPos);
			if (intersectTriangle(from, to, dir, triTest[0], triTest[1], triTest[2], v.tuv, v.intersection)) {
				return true;
			}
		}
		
		return false;
	}
	public boolean intersectTriangle(Vector3f from, Vector3f to, Vector3f dir, Vector3f vert0, Vector3f vert1,
				Vector3f vert2, Vector3f tuv, Vector3f intersection) {
			// Find vectors for two edges sharing vert0
			edge1.sub(vert1, vert0);
			edge2.sub(vert2, vert0);
	
	//		dir.sub(to, from);
			
			// Begin calculating determinant -- also used to calculate U parameter
			pvec.cross(dir, edge2);
	
			// If determinant is near zero, ray lies in plane of triangle
			float det = edge1.dot(pvec);
	
			if (det > -FastMath.FLT_EPSILON && det < FastMath.FLT_EPSILON){
				if(Dodecahedron.debug){
					Vector3f f = new Vector3f(from);
					Vector3f ti = new Vector3f(to);
					DebugDrawer.lines.add(new DebugLine(f, ti, new Vector4f( 0,1,1,0.7f)));
				}
				return false;
			}
	
			float invDet = 1.0f / det;
	
			// Calculate distance from vert0 to ray origin
			tvec.sub(from, vert0);
	
			// Calculate U parameter and test bounds
			float u = tvec.dot(pvec) * invDet;
			if (u < 0.0f || u > 1.0f){
				if(Dodecahedron.debug){
					Vector3f f = new Vector3f(from);
					Vector3f ti = new Vector3f(to);
					DebugDrawer.lines.add(new DebugLine(f, ti, new Vector4f( 0,1,0,0.7f)));
				}
				return false;
			}
	
			// Prepare to test V parameter
			qvec.cross(tvec, edge1);
	
			// Calculate V parameter and test bounds
			float v = dir.dot(qvec) * invDet;
			if (v < 0.0f || (u + v) > 1.0f){
				if(Dodecahedron.debug ){
					Vector3f f = new Vector3f(from);
					Vector3f ti = new Vector3f(to);
					DebugDrawer.lines.add(new DebugLine(f, ti, new Vector4f( 0,1,0,0.7f)));
				}
				return false;
			}
	
			// Calculate t, ray intersects triangle
			float t = edge2.dot(qvec) * invDet;
	
			tuv.set(t, u, v);
			intersection.scale(t, dir);
			intersection.add(from);
			
			boolean inter = FastMath.carmackLength(intersection) < len;
			if(Dodecahedron.debug){
				System.err.println("II: "+from+" -> "+to+"; tuv: "+tuv+" :: lenToTUV "+FastMath.carmackLength(intersection)+"; "+len+"; "+inter);
				Vector3f f = new Vector3f(from);
				Vector3f ti = new Vector3f(to);
				Vector3f z = new Vector3f(intersection);
				DebugDrawer.lines.add(new DebugLine(f, ti, inter ? new Vector4f(1,0,0,0.7f) : new Vector4f( 1,1,1,0.7f)));
//				DebugDrawer.lines.add(new DebugLine(f, z, new Vector4f(1,0,0,0.7f)));
			}
			return inter;
		}
	private final Vector3f edge1 = new Vector3f();
	private final Vector3f edge2 = new Vector3f();
	private final Vector3f pvec = new Vector3f();
	private final Vector3f tvec = new Vector3f();
	private final Vector3f qvec = new Vector3f();
	private final Vector3f dir = new Vector3f();
	private float len;
	
	private boolean testTriangles(Vector3f[] penta, Vector3f center, Vector3f min,
	                              Vector3f max) {

		triTest[2] = center;
		for (int i = 0; i < 5; i++) {
			triTest[0] = penta[i];
			triTest[1] = penta[(i + 1) % 5];

			if (BoundingBox.intersectsTriangle(triTest, min, max, v)) {
				return true;
			}
		}

		return false;
	}

	public void cleanUp() {
		if (dList != 0) {
			GL11.glDeleteLists(dList, 1);
		}
	}
}
