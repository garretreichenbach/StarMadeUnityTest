package org.schema.game.common.data.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.collision.narrowphase.ConvexPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.narrowphase.GjkPairDetector;
import com.bulletphysics.collision.narrowphase.PointCollector;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;

public class ContinuousConvexCollision {
	/// This maximum should not be necessary. It allows for untested/degenerate cases in production code.
	/// You don't want your game ever to lock-up.
	static final int MAX_ITERATIONS = 64;
	protected final ObjectPool<ClosestPointInput> pointInputsPool = ObjectPool
			.get(ClosestPointInput.class);
	private SimplexSolverInterface simplexSolver;
	private ConvexPenetrationDepthSolver penetrationDepthSolver;
	private ConvexShape convexA;
	private ConvexShape convexB1;
	private StaticPlaneShape planeShape;

	public ContinuousConvexCollision(final ConvexShape convexA, final ConvexShape convexB,
	                                 SimplexSolverInterface simplexSolver, ConvexPenetrationDepthSolver penetrationDepthSolver)

	{
		this.simplexSolver = simplexSolver;
		this.penetrationDepthSolver = penetrationDepthSolver;
		this.convexA = convexA;
		this.convexB1 = convexB;
		this.planeShape = null;
	}

	public ContinuousConvexCollision(final ConvexShape convexA, final StaticPlaneShape plane) {
		this.convexA = convexA;
		this.planeShape = plane;
	}
	private Vector3f linVelA = new Vector3f();
	private Vector3f angVelA = new Vector3f();
	private Vector3f linVelB = new Vector3f();
	private Vector3f angVelB = new Vector3f();
	private Vector3f relLinVel = new Vector3f();
	private Transform interpolatedTransA = new Transform();
	private Transform interpolatedTransB = new Transform();
	public boolean calcTimeOfImpact(
			final Transform fromA,
			final Transform toA,
			final Transform fromB,
			final Transform toB,
			CastResult result, GjkPairDetectorVariables gjkv) {

		/// compute linear and angular velocity for this interval, to interpolate
		linVelA.set(0,0,0);
		angVelA.set(0,0,0);
		linVelB.set(0,0,0);
		angVelB.set(0,0,0);

		TransformTools.calculateVelocity(fromA, toA, 1.0f, linVelA, angVelA, gjkv.axis, gjkv.tmp2, gjkv.dmat, gjkv.dorn);
		TransformTools.calculateVelocity(fromB, toB, 1.0f, linVelB, angVelB, gjkv.axis, gjkv.tmp2, gjkv.dmat, gjkv.dorn);

		if (Vector3fTools.isNan(linVelA) || Vector3fTools.isNan(linVelB)) {
			System.err.println("WARNING: LINEAR VELOCITY WAS NAN: " + linVelA + "; " + linVelB);
			return false;
		}

		float boundingRadiusA = convexA.getAngularMotionDisc();
		float boundingRadiusB = convexB1 != null ? convexB1.getAngularMotionDisc() : 0.f;

		float maxAngularProjectedVelocity = angVelA.length() * boundingRadiusA + angVelB.length() * boundingRadiusB;

		relLinVel.set(0,0,0);

		relLinVel.sub(linVelB, linVelA);

		float relLinVelocLength = relLinVel.length();

		if ((relLinVelocLength + maxAngularProjectedVelocity) == 0.f) {
			//		DebugBox b1 = new DebugBox(
			//				new Vector3f(-0.12f-0.5f, -0.12f-0.5f, -0.12f-0.5f),
			//				new Vector3f(1.12f-0.5f, 1.12f-0.5f, 1.12f-0.5f),
			//				new Transform(fromB),
			//				1.0f, 0.0f, 0.0f, 1);
			//				DebugDrawer.boxes.add(b1);

			
			return false;
		}

		float lambda = 0.0f;
		Vector3f v = new Vector3f(1, 0, 0);

		int maxIter = MAX_ITERATIONS;

		Vector3f n = new Vector3f(0, 0, 0);
		boolean hasResult = false;
		Vector3f c;

		float lastLambda = lambda;
		//float epsilon = float(0.001);

		int numIter = 0;
		//first solution, using GJK

		float radius = 0.001f;
		//	result.drawCoordSystem(sphereTr);

		PointCollector pointCollector1 = new PointCollector();

		{
			
			computeClosestPoints(fromA, fromB, pointCollector1, gjkv);

			hasResult = pointCollector1.hasResult;
			c = pointCollector1.pointInWorld;
			
		}

		if (hasResult) {

			float dist;

			//		DebugPoint p = new DebugPoint(new Vector3f(c), new Vector4f(1,0,0,1), 0.6f);
			//		DebugDrawer.points.add(p);

			//		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE) && !Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)){
			//			System.err.println("HITOINT NOW "+c);
			//		}

			dist = pointCollector1.distance + result.allowedPenetration;

			n.set(pointCollector1.normalOnBInWorld);

			float projectedLinearVelocity = relLinVel.dot(n);
			//		float f = Float.MIN_VALUE;
			if ((projectedLinearVelocity + maxAngularProjectedVelocity) <= BulletGlobals.SIMD_EPSILON) {
				//		if ((projectedLinearVelocity + maxAngularProjectedVelocity) <= 0){
				//			if(pointCollector1.normalOnBInWorld.y < -0.8f){
				//				System.err.println("LINEAR VELOCITIES: "+projectedLinearVelocity+" ( "+pointCollector1.normalOnBInWorld+" "+relLinVel+")");
				//			}
				//			DebugBox b1 = new DebugBox(
				//					new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
				//					new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
				//					new Transform(fromB),
				//					1.0f, 1.0f, 0.0f, 1);
				//					DebugDrawer.boxes.add(b1);
//				System.err.println("#1 PFAL");
				return false;

			}
			//		if(pointCollector1.normalOnBInWorld.y < -0.8f){
			//			System.err.println("STD LINEAR VELOCITIES: "+projectedLinearVelocity+" ( "+pointCollector1.normalOnBInWorld+" "+relLinVel+")");
			//		}
			//not close enough
			while (dist > radius) {
				//			if (result.m_debugDrawer)
				//			{
				//				result.m_debugDrawer.drawSphere(c,0.2f,Vector3f(1,1,1));
				//			}
				float dLambda = 0;

				projectedLinearVelocity = relLinVel.dot(n);
//				System.err.println("#2 PBEF "+(projectedLinearVelocity + maxAngularProjectedVelocity)+" "+relLinVel+" ;;; "+projectedLinearVelocity+"; "+maxAngularProjectedVelocity);
				//don't report time of impact for motion away from the contact normal (or causes minor penetration)
				if ((projectedLinearVelocity + maxAngularProjectedVelocity) <= BulletGlobals.SIMD_EPSILON) {
					//				DebugBox b1 = new DebugBox(
					//						new Vector3f(-0.28f-0.5f, -0.28f-0.5f, -0.28f-0.5f),
					//						new Vector3f(1.28f-0.5f, 1.28f-0.5f, 1.28f-0.5f),
					//						new Transform(fromB),
					//						0.0f, 1.0f, 0.0f, 1);
					//						DebugDrawer.boxes.add(b1);
					//						if(pointCollector1.normalOnBInWorld.y < -0.8f){
					//							System.err.println("DENIED LINEAR VELOCITIES: "+projectedLinearVelocity+" ( "+pointCollector1.normalOnBInWorld+" "+relLinVel+")");
					//						}
					//				System.err.println("2 HAS RESULT: BUT PROJECTED VEL IS TO SMALL");
//					System.err.println("#2 PFAL "+(projectedLinearVelocity + maxAngularProjectedVelocity)+" "+relLinVel);
					return false;

				}

				dLambda = dist / (projectedLinearVelocity + maxAngularProjectedVelocity);

				lambda = lambda + dLambda;

				if (lambda > 1.0f) {
					//				DebugBox b1 = new DebugBox(
					//						new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
					//						new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
					//						new Transform(fromB),
					//						0.0f, 1.0f, 1.0f, 1);
					//						DebugDrawer.boxes.add(b1);
					//						if(pointCollector1.normalOnBInWorld.y < -0.8f){
					//							System.err.println("HAS RESULT: BUT LAMDA IS TO BIG "+lambda+": dist "+dist+"; projVelo "+(projectedLinearVelocity + maxAngularProjectedVelocity)+"; dLamda "+dLambda+"; itert "+numIter+"; penetration: "+result.allowedPenetration);
					//						}
//					System.err.println("#3 PFAL");
					return false;

				}

				if (lambda < 0.0f) {
					//				DebugBox b1 = new DebugBox(
					//						new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
					//						new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
					//						new Transform(fromB),
					//						0.5f, 1.0f, 0.3f, 1);
					//						DebugDrawer.boxes.add(b1);
					System.err.println("HAS RESULT: BUT LAMDA IS TO SMALL " + lambda);
					return false;

				}

				//todo: next check with relative epsilon
				if (lambda <= lastLambda) {
					//				DebugBox b1 = new DebugBox(
					//						new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
					//						new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
					//						new Transform(fromB),
					//						0.5f, 0.9f, 0.6f, 1);
					//						DebugDrawer.boxes.add(b1);
					System.err.println("HAS RESULT: BUT LAST LAMDA IS <= LAST LAMBDA");
					return false;
					//n.setValue(0,0,0);
					//				break;
				}
				lastLambda = lambda;

				//interpolate to next lambda
				
				interpolatedTransA.setIdentity();
				
				interpolatedTransB.setIdentity();
				Transform relativeTrans;

				//			System.err.println("TETETETETETE "+fromA+" "+linVelA+" "+angVelA+" "+lambda+": ");
				TransformTools.integrateTransform(fromA, linVelA, angVelA, lambda, interpolatedTransA, gjkv.iAxis, gjkv.iDorn, gjkv.iorn0, gjkv.iPredictOrn, gjkv.float4Temp);
				TransformTools.integrateTransform(fromB, linVelB, angVelB, lambda, interpolatedTransB, gjkv.iAxis, gjkv.iDorn, gjkv.iorn0, gjkv.iPredictOrn, gjkv.float4Temp);
				//			relativeTrans = interpolatedTransB.inverseTimes(interpolatedTransA);
				relativeTrans = new Transform();
				relativeTrans.set(interpolatedTransB);
				relativeTrans.inverse();
				relativeTrans.mul(interpolatedTransA);

				//			if (result.debugDrawer)
				//			{
				//				result.debugDrawer.drawSphere(interpolatedTransA.origin,0.2f,new Vector3f(1,0,0));
				//			}
				//
				//			result.DebugDraw( lambda );

				PointCollector pointCollector = new PointCollector();
				computeClosestPoints(interpolatedTransA, interpolatedTransB, pointCollector, gjkv);

				if (pointCollector.hasResult) {
					dist = pointCollector.distance + result.allowedPenetration;
					c = pointCollector.pointInWorld;
					n.set(pointCollector.normalOnBInWorld);
//					System.err.println("RESET N "+n+"; "+n.length());
				} else {
					//				DebugBox b1 = new DebugBox(
					//						new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
					//						new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
					//						new Transform(fromB),
					//						0.0f, 1.0f, 0.0f, 1);
					//						DebugDrawer.boxes.add(b1);
					System.err.println("POINT HAS NO RESULT: -1 " + numIter);
					//				result.reportFailure(-1, numIter);
					return false;
				}

				numIter++;
				if (numIter > maxIter) {
					//				DebugBox b1 = new DebugBox(
					//						new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
					//						new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
					//						new Transform(fromB),
					//						0.0f, 0.0f, 1.0f, 1);
					//						DebugDrawer.boxes.add(b1);
					//				System.err.println("Failiure: -2 "+numIter);
					//				result.reportFailure(-2, numIter);
//					System.err.println("#1 ITER EXCEEDED");
					return false;
				}
			}
//			System.err.println("SUCCESS!!!!!!!!!!");
			result.fraction = lambda;
			result.normal.set(n);
			result.hitPoint.set(c);
			return true;
		} else {
			//		DebugBox b1 = new DebugBox(
			//				new Vector3f(-0.11f-0.5f, -0.11f-0.5f, -0.11f-0.5f),
			//				new Vector3f(1.11f-0.5f, 1.11f-0.5f, 1.11f-0.5f),
			//				new Transform(fromB),
			//				1.0f, 0.0f, 0.0f, 1);
			//				DebugDrawer.boxes.add(b1);
		}

		return false;

	}
	private final GjkPairDetector gjk = new GjkPairDetector(); //WARNING: GjkPairDetectorExt is not good (doesnt hit over long distance)
	
	
	void computeClosestPoints(final Transform transA, final Transform transB, PointCollector pointCollector, GjkPairDetectorVariables gjkv) {
		if (convexB1 != null) {
			simplexSolver.reset();
			gjk.init(convexA, convexB1, simplexSolver, penetrationDepthSolver);
			ClosestPointInput input = pointInputsPool.get();
			input.init();
			input.transformA.set(transA);
			input.transformB.set(transB);
			//		gjk.setIgnoreMargin(true);
			gjk.getClosestPoints(input, pointCollector, null);
			pointInputsPool.release(input);
		} else {
			//convex versus plane
			final ConvexShape convexShape = convexA;
			final StaticPlaneShape planeShape = this.planeShape;

			boolean hasCollision = false;
			final Vector3f planeNormal = planeShape.getPlaneNormal(new Vector3f());
			final float planeConstant = planeShape.getPlaneConstant();

			Transform convexWorldTransform = transA;
			Transform convexInPlaneTrans = new Transform();
			convexInPlaneTrans.set(transB);
			convexInPlaneTrans.inverse();
			convexInPlaneTrans.mul(convexWorldTransform);
			Transform planeInConvex = new Transform();
			planeInConvex.set(convexWorldTransform);
			planeInConvex.inverse();
			planeInConvex.mul(transB);

			Matrix3f base = new Matrix3f();
			base.set(planeInConvex.basis);
			Vector3f planeNormalNeg = new Vector3f(planeNormal);
			planeNormalNeg.scale(-1);
			base.transform(planeNormalNeg);

			Vector3f vtx = convexShape.localGetSupportingVertex(planeNormalNeg, new Vector3f());

			Vector3f vtxInPlane = new Vector3f();
			vtxInPlane.set(vtx);
			convexInPlaneTrans.transform(vtx);
			float distance = (planeNormal.dot(vtxInPlane) - planeConstant);

			Vector3f vtxInPlaneProjected = new Vector3f();
			Vector3f planeScaled = new Vector3f(planeNormal);
			planeScaled.scale(distance);
			vtxInPlaneProjected.sub(vtxInPlane, planeScaled);

			Vector3f vtxInPlaneWorld = new Vector3f(vtxInPlaneProjected);
			transB.transform(vtxInPlaneWorld);
			Vector3f normalOnSurfaceB = new Vector3f(planeNormal);
			transB.basis.transform(normalOnSurfaceB);

			pointCollector.addContactPoint(
					normalOnSurfaceB,
					vtxInPlaneWorld,
					distance);
		}
	}

}
