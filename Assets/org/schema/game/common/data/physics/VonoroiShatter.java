package org.schema.game.common.data.physics;

import java.util.Collections;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.common.data.physics.qhull.DPoint3d;
import org.schema.game.common.data.physics.qhull.QuickHull3D;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VonoroiShatter {

	static Vector3fb curVoronoiPoint = new Vector3fb(); // Here for btAlignedObjectArray.quickSort pointCmp scope

	public static Vector3fb sub(Vector3fb a, Vector3fb b, Vector3fb out) {
		out.sub(a, b);
		return out;
	}

	static void getVerticesInsidePlanes(ObjectArrayList<Vector3fb> planes, ObjectArrayList<Vector3fb> verticesOut, IntOpenHashSet planeIndicesOut) {

		// Based on btGeometryUtil.cpp (Gino van den Bergen / Erwin Coumans)
		//		verticesOut.resize(0);
		verticesOut.clear();
		planeIndicesOut.clear();
		final int numPlanes = planes.size();
		System.err.println("PLANES: " + planes);
		for (int i = 0; i < numPlanes; i++) {
			final Vector3fb N1 = planes.get(i);

			for (int j = i + 1; j < numPlanes; j++) {
				final Vector3fb N2 = planes.get(j);

				Vector3fb n1n2 = new Vector3fb(N1.cross(N2));
				if (n1n2.lengthSquared() > 0.0001f) {
					for (int k = j + 1; k < numPlanes; k++) {
						final Vector3fb N3 = planes.get(k);

						final Vector3fb n2n3 = new Vector3fb(N2.cross(N3));
						final Vector3fb n3n1 = new Vector3fb(N3.cross(N1));
						if ((n2n3.lengthSquared() > 0.0001f) && (n3n1.lengthSquared() > 0.0001f) /*&& (n1n2.lengthSquared() > 0.0001f)*/) {
							float quotient = (N1.dot(n2n3));
							if (Math.abs(quotient) > 0.0001f) {
								quotient = (-1f / quotient);
								Vector3fb potentialVertex = new Vector3fb();
								//								potentialVertex.x = (n2n3.x * N1.w + n3n1.x * N2.w + n1n2.x * N3.w) * quotient;
								//								potentialVertex.y = (n2n3.y * N1.w + n3n1.y * N2.w + n1n2.y * N3.w) * quotient;
								//								potentialVertex.z = (n2n3.z * N1.w + n3n1.z * N2.w + n1n2.z * N3.w) * quotient;

								n2n3.scale(-N1.w);
								n3n1.scale(-N2.w);
								n1n2.scale(-N3.w);
								potentialVertex.set(n2n3);
								potentialVertex.add(n3n1);
								potentialVertex.add(n1n2);
								potentialVertex.scale(quotient);

								//								Vector3fb potentialVertex = (n2n3 * N1.w + n3n1 * N2.w + n1n2 * N3.w) * (-1f / quotient);
								int l = 0;
								for (l = 0; l < numPlanes; l++) {
									final Vector3fb NP = planes.get(l);
									if (NP.dot(potentialVertex) + -NP.w > 0.000001f) {
										break;
									}
								}
								System.err.println("POTENTIAL VERT: " + potentialVertex + "; " + numPlanes + " ---- " + l);
								if (l == numPlanes) {
									// vertex (three plane intersection) inside all planes
									verticesOut.add(potentialVertex);
									planeIndicesOut.add(i);
									planeIndicesOut.add(j);
									planeIndicesOut.add(k);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Computes the triple product of this and the specified other vectors, which is equal to
	 * <code>this.dot(b.cross(c))</code>.
	 */
	public static float triple(Vector3f a, Vector3f b, Vector3f c) {
		return a.x * (b.y * c.z - b.z * c.y) + a.y * (b.z * c.x - b.x * c.z) + a.z * (b.x * c.y - b.y * c.x);
	}
	//	static boolean pointCmp(const Vector3fb& p1, const Vector3fb& p2) {
	//		return ((p1-curVoronoiPoint).lengthSquared() < (p2-curVoronoiPoint).lengthSquared());
	//	}

	public static void voronoiBBShatter(PhysicsExt physics, ObjectArrayList<Vector3fb> points, final Vector3fb bbmin, final Vector3fb bbmax, final Quat4f bbq, final Vector3fb bbt, float matDensity) {
		// points define voronoi cells in world space (avoid duplicates)
		// bbmin & bbmax = bounding box min and max in local space
		// bbq & bbt = bounding box quaternion rotation and translation
		// matDensity = Material density for voronoi shard mass calculation
		Vector3fb bbvx = new Vector3fb(QuaternionUtil.quatRotate(bbq, new Vector3fb(1.0f, 0.0f, 0.0f), new Vector3fb()));
		Vector3fb bbvy = new Vector3fb(QuaternionUtil.quatRotate(bbq, new Vector3fb(0.0f, 1.0f, 0.0f), new Vector3fb()));
		Vector3fb bbvz = new Vector3fb(QuaternionUtil.quatRotate(bbq, new Vector3fb(0.0f, 0.0f, 1.0f), new Vector3fb()));
		Quat4f bbiq = new Quat4f(bbq);
		bbiq.inverse();

		//		btConvexHullComputer* convexHC = new btConvexHullComputer();

		ObjectArrayList<Vector3fb> vertices = new ObjectArrayList<Vector3fb>();
		Vector3fb rbb, nrbb;
		float nlength, maxDistance, distance;
		ObjectArrayList<Vector3fb> sortedVoronoiPoints = new ObjectArrayList<Vector3fb>(points);
		Vector3fb plane;
		Vector3fb normal;
		ObjectArrayList<Vector3fb> planes = new ObjectArrayList<Vector3fb>();
		IntOpenHashSet planeIndices = new IntOpenHashSet();
		//		IntOpenHashSet<int>::iterator planeIndicesIter;
		int numplaneIndices;
		int cellnum = 0;
		int i, j, k;

		int numpoints = points.size();
		System.err.println("ADDING: " + numpoints + " SHARDS");
		for (i = 0; i < numpoints; i++) {
			curVoronoiPoint.set(points.get(i));
			//			System.err.println("CURRWENT VERT: "+curVoronoiPoint);
			//			Vector3fb icp = new Vector3fb(QuaternionUtil.quatRotate(bbiq, sub(curVoronoiPoint, bbt, new Vector3fb()), new Vector3fb()));
			//			rbb = sub(icp, bbmax, new Vector3fb());
			//			nrbb = sub(bbmin, icp, new Vector3fb());
			rbb = new Vector3fb();
			nrbb = new Vector3fb();
			rbb.sub(curVoronoiPoint, bbmax);
			nrbb.sub(bbmin, curVoronoiPoint);
			planes.clear();
			for (int l = 0; l < 6; l++) {
				planes.add(new Vector3fb());
			}

			System.err.println("RBB: " + rbb + "; nrbb: " + nrbb);

			planes.get(0).set(bbvx);
			//			planes.get(0).negate();
			planes.get(0).w = -rbb.x;

			planes.get(1).set(bbvy);
			//			planes.get(1).negate();
			planes.get(1).w = -rbb.y;

			planes.get(2).set(bbvz);
			//			planes.get(2).negate();
			planes.get(2).w = -rbb.z;

			planes.get(3).set(bbvx);
			planes.get(3).negate();
			planes.get(3).w = -nrbb.x;

			planes.get(4).set(bbvy);
			planes.get(4).negate();
			planes.get(4).w = -nrbb.y;

			planes.get(5).set(bbvz);
			planes.get(5).negate();
			planes.get(5).w = -nrbb.z;

			System.err.println("PLANE EQUATION FROM " + curVoronoiPoint + "; " + planes);

			maxDistance = BulletGlobals.SIMD_INFINITY;
			Collections.sort(sortedVoronoiPoints, new PComp(curVoronoiPoint));

			for (Vector3fb p : planes) {
				Vector3f m = new Vector3f(p);
				m.scale(p.w);
				m.add(curVoronoiPoint);
				if (i == 0) {
					DebugPoint debugPoint = new DebugPoint(m, new Vector4f(0, 0, 1, 1), 0.12f);
					debugPoint.LIFETIME = 7000;
					DebugDrawer.points.add(debugPoint);
				}
				if (i == 1) {
					DebugPoint debugPoint = new DebugPoint(m, new Vector4f(0, 1, 0, 1), 0.12f);
					debugPoint.LIFETIME = 7000;
					DebugDrawer.points.add(debugPoint);
				}
			}
			//			sortedVoronoiPoints.quickSort(pointCmp);
			for (j = 1; j < numpoints; j++) {
				normal = sub(sortedVoronoiPoints.get(j), curVoronoiPoint, new Vector3fb());
				nlength = normal.length();
				if (nlength > maxDistance) {
					break;
				}
				plane = new Vector3fb(normal);
				plane.normalize();

				plane.w = -nlength / 2f;
				planes.add(plane);
				getVerticesInsidePlanes(planes, vertices, planeIndices);

				if (vertices.size() == 0) {
					//					System.err.println("NO VERTS HERE");
					break;
				}

				numplaneIndices = planeIndices.size();
				if (numplaneIndices != planes.size()) {
					IntIterator planeIndicesIterator = planeIndices.iterator();
					//					int next = planeIndicesIterator.nextInt();
					ObjectArrayList<Vector3fb> planesCop = new ObjectArrayList<Vector3fb>();
					for (k = 0; k < numplaneIndices; k++) {
						int next = planeIndicesIterator.nextInt();
						//						if (k != next /**planeIndicesIter**/){
						planes.get(k).set(planes.get(next/**planeIndicesIter**/));
						//							planesCop.add(planes.get(next/**planeIndicesIter**/));
						//						}

						//						planeIndicesIter++;

					}
					//					planes.clear();
					//					planes.addAll(planesCop);

					while (planes.size() > numplaneIndices) {
						planes.remove(planes.size() - 1);
					}
					//					planes.resize(numplaneIndices);
				}
				maxDistance = vertices.get(0).length();
				for (k = 1; k < vertices.size(); k++) {
					distance = vertices.get(k).length();
					if (maxDistance < distance) {
						maxDistance = distance;
					}
					//					if(j == 1){
					//						DebugPoint debugPoint = new DebugPoint(new Vector3f(vertices.get(i)), new Vector4f(1,1,1,1), 0.1f);
					//						debugPoint.LIFETIME = 7000;
					//						DebugDrawer.points.add(debugPoint);
					//					}
				}
				for (Vector3fb p : planes) {
					//					if(j == 1){
					Vector3f m = new Vector3f(p);
					m.scale(p.w);
					m.add(curVoronoiPoint);
					DebugPoint debugPoint = new DebugPoint(m, new Vector4f(1, 1, 1, 1), 0.11f);
					debugPoint.LIFETIME = 7000;
					DebugDrawer.points.add(debugPoint);
					//					}
				}

				maxDistance *= 2f;
			}

			if (vertices.size() < 4) {
				//				System.err.println("NO VERTICES");
				continue;
			}
			System.err.println("ADDING VERTS: " + vertices.size());
			// Clean-up voronoi convex shard vertices and generate edges & faces
			//			convexHC->compute(&vertices[0].getX(), sizeof(Vector3fb), vertices.size(),0.0,0.0);
			com.bulletphysics.util.ObjectArrayList<Vector3f> a = new com.bulletphysics.util.ObjectArrayList<Vector3f>(vertices.size());
			a.addAll(vertices);

			System.err.println("VERTICES: " + a);

			DPoint3d[] d = new DPoint3d[a.size()];
			for (int s = 0; s < d.length; s++) {
				d[s] = new DPoint3d(a.get(s).x, a.get(s).y, a.get(s).z);

			}
			QuickHull3D convexHC;
			try {
				convexHC = new QuickHull3D(d);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			// At this point we have a complete 3D voronoi shard mesh contained in convexHC

			// Calculate volume and center of mass (Stan Melax volume integration)
			int numFaces = convexHC.getNumFaces();//->faces.size();
			int v0, v1, v2; // Triangle vertices
			float volume = 0f;
			Vector3fb com = new Vector3fb(0f, 0f, 0f);
			for (j = 0; j < numFaces; j++) {
				int[] edges = convexHC.getFaces()[j];
				//				const btConvexHullComputer::Edge* edge = &convexHC->edges[convexHC->faces[j]];
				//				v0 = new Vector3fb((float)convexHC.getVertices()[edges[0]].x, (float)(convexHC.getVertices()[edges[0]].y, (float)convexHC.getVertices()[edges[0]].z);
				v0 = edges[0];
				v1 = edges[1];
				//				v0 = edge->getSourceVertex();
				//				v1 = edge->getTargetVertex();
				//				edge = edge->getNextEdgeOfFace();
				int nextEdge = 2;
				v2 = edges[nextEdge];//edge->getTargetVertex();
				while (v2 < edges.length/**v2 != v0**/) {
					// Counter-clockwise triangulated voronoi shard mesh faces (v0-v1-v2) and edges here...
					float vol = triple(new Vector3fb(convexHC.getVertices()[v0]), new Vector3fb(convexHC.getVertices()[v1]), new Vector3fb(convexHC.getVertices()[v2]));//convexHC->vertices[v0].triple(convexHC->vertices[v1], convexHC->vertices[v2]);
					volume += vol;

					Vector3fb ll = new Vector3fb(convexHC.getVertices()[v0]);
					ll.add(new Vector3fb(convexHC.getVertices()[v1]));
					ll.add(new Vector3fb(convexHC.getVertices()[v2]));
					ll.scale(vol);

					//					com += vol * (convexHC->vertices[v0] + convexHC->vertices[v1] + convexHC->vertices[v2]);
					com.add(ll);

					nextEdge += 2;
					v1 = nextEdge - 1;
					v2 = nextEdge;
					//					edge = edge->getNextEdgeOfFace();
					//					v1 = v2;
					//					v2 = edge->getTargetVertex();
				}
			}
			com.scale(1f / (volume * 4f));
			volume /= 6f;

			// Shift all vertices relative to center of mass
			int numVerts = a.size();
			for (j = 0; j < numVerts; j++) {
				a.get(j).sub(com);
			}
			org.schema.game.common.data.physics.ConvexHullShapeExt shardShape = new org.schema.game.common.data.physics.ConvexHullShapeExt(a);
			// Note:
			// At this point convex hulls contained in convexHC should be accurate (line up flush with other pieces, no cracks),
			// ...however Bullet Physics rigid bodies demo visualizations appear to produce some visible cracks.
			// Use the mesh in convexHC for visual display or to perform boolean operations with.

			// Create Bullet Physics rigid body shards
			//			btCollisionShape* shardShape = new btorg.schema.game.common.data.physics.ConvexHullShapeExt(&(convexHC->vertices[0].getX()), convexHC->vertices.size());
			shardShape.setMargin(0f); // for this demo; note convexHC has optional margin parameter for this
			//			m_collisionShapes.push_back(shardShape);
			Transform shardTransform = new Transform();
			shardTransform.setIdentity();
			curVoronoiPoint.add(com);
			shardTransform.origin.set(curVoronoiPoint); // Shard's adjusted location
			curVoronoiPoint.sub(com);
			RigidBody bodyFromShape = physics.getBodyFromShape(shardShape, 0/*volume*matDensity*/, shardTransform);

			bodyFromShape.getCollisionShape().setMargin(0);
			assert (!bodyFromShape.getCollisionShape().isConcave());

			//			physics.addObject(bodyFromShape);
			//
			//			GameClientState c = (GameClientState) physics.getState();
			//			c.getWorldDrawer().addShard(bodyFromShape, convexHC);

			//			btDefaultMotionState* shardMotionState = new btDefaultMotionState(shardTransform);
			//			btScalar shardMass(volume * matDensity);
			//			Vector3fb shardInertia(0.,0.,0.);
			//			shardShape->calculateLocalInertia(shardMass, shardInertia);
			//			btRigidBody::btRigidBodyConstructionInfo shardRBInfo(shardMass, shardMotionState, shardShape, shardInertia);
			//			btRigidBody* shardBody = new btRigidBody(shardRBInfo);
			//			m_dynamicsWorld->addRigidBody(shardBody);

			cellnum++;

		}
		//		printf("Generated %d voronoi btRigidBody shards\n", cellnum);
	}

}
