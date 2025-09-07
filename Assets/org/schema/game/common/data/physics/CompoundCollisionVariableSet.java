package org.schema.game.common.data.physics;

import org.schema.game.common.data.physics.sweepandpruneaabb.CompoundCollisionObjectSweeper;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;

public class CompoundCollisionVariableSet {

	public final ObjectPool<CompoundCollisionAlgorithmExt> pool = new ObjectPool<CompoundCollisionAlgorithmExt>(CompoundCollisionAlgorithmExt.class);//.get(CompoundCollisionAlgorithmExt.class);
	public Transform tmpTrans = new Transform();
	public Transform orgTrans = new Transform();
	public Transform chieldTrans = new Transform();
	public Transform interpolationTrans = new Transform();
	public Transform newChildWorldTrans = new Transform();
	public Transform tmpTransO = new Transform();
	public Transform orgTransO = new Transform();
	public Transform chieldTransO = new Transform();
	public Transform interpolationTransO = new Transform();
	public Transform newChildWorldTransO = new Transform();
	public int instances;
	public CompoundCollisionObjectSweeper sweeper = new CompoundCollisionObjectSweeper();
	public Transform tmpTrans0 = new Transform();
	public Transform tmpTrans1 = new Transform();
}
