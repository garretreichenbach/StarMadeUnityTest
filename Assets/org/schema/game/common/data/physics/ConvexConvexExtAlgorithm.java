package org.schema.game.common.data.physics;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.CollisionAlgorithmConstructionInfo;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ConvexConvexAlgorithm;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.ConvexPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.util.ObjectPool;

public class ConvexConvexExtAlgorithm extends ConvexConvexAlgorithm {

	@Override
	public void processCollision(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		if (body0.getCollisionShape() instanceof CubeShape || body1.getCollisionShape() instanceof CubeShape) {
			System.err.println("WARNING: tried imcompatible collision algorithm " + body0 + "; " + body1);
			return;
		} else {

			//			Transform worldTransformA = body0.getWorldTransform(new Transform());
			//			Transform worldTransformB = body1.getWorldTransform(new Transform());

			//			System.err.println("DOING COLLISION: "+this.getClass().getSimpleName()+": "+body0.getCollisionShape().getClass().getSimpleName()+":: "+
			//			worldTransformA.origin+"; "+body1.getCollisionShape().getClass().getSimpleName()+":: "+worldTransformB.origin);

			super.processCollision(body0, body1, dispatchInfo, resultOut);

			//			if(System.currentTimeMillis() - t > 5){
			//				System.err.println("[PHYSICS] WARNING ConvexConvex between "+body0+", "+body1+" took "+(System.currentTimeMillis() - t));
			//			}
		}
	}

	public static class CreateFunc extends CollisionAlgorithmCreateFunc {
		private final ObjectPool<ConvexConvexExtAlgorithm> pool = ObjectPool.get(ConvexConvexExtAlgorithm.class);

		public ConvexPenetrationDepthSolver pdSolver;
		public SimplexSolverInterface simplexSolver;

		public CreateFunc(SimplexSolverInterface simplexSolver, ConvexPenetrationDepthSolver pdSolver) {
			this.simplexSolver = simplexSolver;
			this.pdSolver = pdSolver;
		}

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
			ConvexConvexExtAlgorithm algo = pool.get();
			algo.init(ci.manifold, ci, body0, body1, simplexSolver, pdSolver);
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			pool.release((ConvexConvexExtAlgorithm) algo);
		}
	}

}
