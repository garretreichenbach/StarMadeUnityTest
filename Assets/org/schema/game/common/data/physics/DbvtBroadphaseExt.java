package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.DbvtAabbMm;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.DbvtProxy;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.HashedOverlappingPairCache;
import com.bulletphysics.collision.broadphase.OverlappingPairCache;

public class DbvtBroadphaseExt extends DbvtBroadphase {

	private final Vector3f delta = new Vector3f();
	private final Vector3f centerOut = new Vector3f();
	private DbvtAabbMm tmpAabb = new DbvtAabbMm();
	public DbvtBroadphaseExt() {
		//		super();
		this(null);
	}

	public DbvtBroadphaseExt(OverlappingPairCache paircache) {
		//		super(paircache);
		sets[0] = new DbvtExt();
		sets[1] = new DbvtExt();

		//Dbvt.benchmark();
		releasepaircache = (paircache != null ? false : true);
		predictedframes = 2;
		stageCurrent = 0;
		fupdates = 1;
		dupdates = 1;
		this.paircache = (paircache != null ? paircache : new HashedOverlappingPairCache());
		gid = 0;
		pid = 0;

		for (int i = 0; i <= STAGECOUNT; i++) {
			stageRoots[i] = null;
		}
		//#if DBVT_BP_PROFILE
		//clear(m_profiling);
		//#endif

	}

	private static DbvtProxy listappend(DbvtProxy item, DbvtProxy list) {
		item.links[0] = null;
		item.links[1] = list;
		if (list != null) list.links[0] = item;
		list = item;
		return list;
	}

	private static DbvtProxy listremove(DbvtProxy item, DbvtProxy list) {
		if (item.links[0] != null) {
			item.links[0].links[1] = item.links[1];
		} else {
			list = item.links[1];
		}

		if (item.links[1] != null) {
			item.links[1].links[0] = item.links[0];
		}
		return list;
	}

	@Override
	public void setAabb(BroadphaseProxy absproxy, Vector3f aabbMin, Vector3f aabbMax, Dispatcher dispatcher) {
		DbvtProxy proxy = (DbvtProxy) absproxy;

		DbvtAabbMm aabb = DbvtAabbMm.FromMM(aabbMin, aabbMax, tmpAabb);
		if (proxy.stage == STAGECOUNT) {
			// fixed -> dynamic set
			sets[1].remove(proxy.leaf);
			proxy.leaf = sets[0].insert(aabb, proxy);
		} else {
			// dynamic set:
			if (DbvtAabbMm.Intersect(proxy.leaf.volume, aabb)) {/* Moving				*/
				delta.add(aabbMin, aabbMax);
				delta.scale(0.5f);
				delta.sub(proxy.aabb.Center(centerOut));
				//#ifdef DBVT_BP_MARGIN
				delta.scale(predictedframes);
				sets[0].update(proxy.leaf, aabb, delta, DBVT_BP_MARGIN);
				//#else
				//m_sets[0].update(proxy->leaf,aabb,delta*m_predictedframes);
				//#endif
			} else {
				// teleporting:
				sets[0].update(proxy.leaf, aabb);
			}
		}

		stageRoots[proxy.stage] = listremove(proxy, stageRoots[proxy.stage]);
		proxy.aabb.set(aabb);
		proxy.stage = stageCurrent;
		stageRoots[stageCurrent] = listappend(proxy, stageRoots[stageCurrent]);
	}

	public void clean() {
		sets[0] = null;
		sets[1] = null;
	}
}
