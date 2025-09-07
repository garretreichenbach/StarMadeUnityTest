package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.narrowphase.ConvexPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.Transform;

public class GjkEpaPenetrationDepthSolverExt extends ConvexPenetrationDepthSolver {
	private GjkEpaSolverExt gjkEpaSolver = new GjkEpaSolverExt();

	private GjkEpaSolverExt.Results results = new GjkEpaSolverExt.Results();
	
	@Override
	public boolean calcPenDepth(SimplexSolverInterface simplexSolver,
												  ConvexShape pConvexA, ConvexShape pConvexB,
												  Transform transformA, Transform transformB,
												  Vector3f v, Vector3f wWitnessOnA, Vector3f wWitnessOnB,
												  IDebugDraw debugDraw/*, btStackAlloc* stackAlloc*/)
	{
		float radialmargin = 0f;

		// JAVA NOTE: 2.70b1: update when GjkEpaSolver2 is ported
		results.reset();
		
		if (gjkEpaSolver.collide(pConvexA, transformA,
				pConvexB, transformB,
				radialmargin/*,stackAlloc*/, results)) {
			//debugDraw->drawLine(results.witnesses[1],results.witnesses[1]+results.normal,btVector3(255,0,0));
			//resultOut->addContactPoint(results.normal,results.witnesses[1],-results.depth);
			wWitnessOnA.set(results.witnesses[0]);
			wWitnessOnB.set(results.witnesses[1]);
			return true;
		}

		return false;
	}
}
