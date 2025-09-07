package org.schema.game.common.controller.elements.power.reactor;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.physics.PairCachingGhostObjectExt;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.GLDebugDrawer;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CylinderShapeZ;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class StabilizerPath {

	public final Long2LongOpenHashMap nodes = new Long2LongOpenHashMap();
	public final Long2ObjectOpenHashMap<PairCachingGhostObjectExt> colObjs = new Long2ObjectOpenHashMap<PairCachingGhostObjectExt>();
	public final Long2ObjectOpenHashMap<Transform> localTrans = new Long2ObjectOpenHashMap<Transform>();
	public final PowerInterface pw;
	private StabilizerUnit unit;
	private final double weight;

	public StabilizerPath(double weight, PowerInterface pw, StabilizerUnit unit){
		this.pw = pw;
		this.unit = unit;
		this.weight = weight;
	}
	
	public void update(Timer timer){
		
		
	}
	private final Transform tmp = new Transform();
	private long hitTime;
	public long start;
	public float graphicsTotalLength;
	
	public void remove(PhysicsExt physics, PhysicsDataContainer con){
		for(PairCachingGhostObjectExt e : colObjs.values()){
			physics.removeObject(e);
		}
		colObjs.clear();
		localTrans.clear();
		
	}
	public void drawDebug(SegmentController segmentController) {
		for(PairCachingGhostObjectExt l : colObjs.values()){
			GLDebugDrawer.drawOpenGL(l.getWorldTransform(new Transform()), l.getCollisionShape(), new Vector3f(1,0,0), 0); //DebugDrawModes.DRAW_WIREFRAME
		}
	}
	public void generate(PhysicsExt physics, SegmentController con){
		remove(physics, con.getPhysicsDataContainer());

		
		float radius = ((ManagedSegmentController<?>)con).getManagerContainer().getPowerInterface().getStabilzerPathRadius();
		
		for(Entry e : nodes.long2LongEntrySet()){
			long from = e.getLongKey();
			long to = e.getLongValue();
			Vector3f fromVec = ElementCollection.getPosFromIndex(from, new Vector3f());
			Vector3f toVec = ElementCollection.getPosFromIndex(to, new Vector3f());
			
			Vector3f d = new Vector3f();
			d.sub(toVec, fromVec);
			if(d.lengthSquared() == 0){
				continue;
			}
			float cylinderLenFull = d.length();
			CylinderShapeZ s = new CylinderShapeZ(new Vector3f(radius, radius, cylinderLenFull/2f));
			
			
			PairCachingGhostObjectExt c = new PairCachingGhostObjectExt(CollisionType.ENERGY_STREAM, con.getPhysicsDataContainer()){
				/* (non-Javadoc)
				 * @see com.bulletphysics.collision.dispatch.CollisionObject#checkCollideWith(com.bulletphysics.collision.dispatch.CollisionObject)
				 */
				@Override
				public boolean checkCollideWith(CollisionObject co) {
					//takes care of cube shapes collision. doesnt take care of convex sweep tests
					return false;
				}
			};
			
			c.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
			c.setCollisionShape(s);
			
			c.setUserPointer(this);
			
			Transform local = new Transform();
			local.setIdentity();
			Vector3f pos = new Vector3f(fromVec);
			pos.x -= Segment.HALF_DIM;
			pos.y -= Segment.HALF_DIM;
			pos.z -= Segment.HALF_DIM;
			
			
			Vector3f front = new Vector3f(d);
			front.normalize();
			
			Vector3f up;
			Vector3f right; 
			if(front.y > 0.99999){
				front = new Vector3f(0,1,0);
				up = new Vector3f(0,0,1);
				right = new Vector3f(1,0,0);
			}else if(front.y < -0.99999){
				front = new Vector3f(0,-1,0);
				up = new Vector3f(0,0,-1);
				right = new Vector3f(-1,0,0);
			}else{
				up = new Vector3f(0,1,0);
				right = new Vector3f(1,0,0);
				
				right.cross(front, up);
				up.cross(front, right);
				
				right.normalize();
				up.normalize();
			}
			
			
			
			
			GlUtil.setForwardVector(front, local);
			GlUtil.setUpVector(up, local);
			GlUtil.setRightVector(right, local);
			local.origin.set(pos);
			//the cylinder origin is in the middle
			d.normalize();
			d.scale(cylinderLenFull/2);
			local.origin.add(d);
			
			
			c.setWorldTransform(local);
			
			assert(!TransformTools.isNan(local)):local.getMatrix(new Matrix4f());
			colObjs.put(from, c);
			localTrans.put(from, local);
			
			short group = (CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.CHARACTER_FILTER) ^ CollisionFilterGroups.DEBRIS_FILTER; //we collide with nothing
			short filter = (CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.CHARACTER_FILTER) ^ CollisionFilterGroups.DEBRIS_FILTER; //characters dont collide with us
			
			assert(checkAABB(c));
			
			physics.addObject(c, group, filter);
		}
		if(con.isOnServer()){
			updateTransform(con.getWorldTransform());
		}else{
			updateTransform(con.getWorldTransformOnClient());
		}
	}
	public boolean checkAABB(CollisionObject c){
		Transform t = new Transform();
		t.setIdentity();
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		c.getCollisionShape().getAabb(t, min, max);
		
//		System.err.println("AABB: "+min+", "+max);
		
		assert(!Float.isNaN(min.x)):min+"; "+max;
		assert(!Float.isNaN(min.y)):min+"; "+max;
		assert(!Float.isNaN(min.z)):min+"; "+max;
		assert(!Float.isNaN(max.x)):min+"; "+max;
		assert(!Float.isNaN(max.y)):min+"; "+max;
		assert(!Float.isNaN(max.z)):min+"; "+max;
		
		return true;
	}
	public void updateTransform(Transform worldTransform){
		assert(!TransformTools.isNan(worldTransform)):worldTransform.getMatrix(new Matrix4f());
		for(long l : colObjs.keySet()){
			PairCachingGhostObjectExt c = colObjs.get(l);
			Transform local = localTrans.get(l);
			
			assert(!TransformTools.isNan(local)):local.getMatrix(new Matrix4f());
			
			tmp.set(worldTransform);
			
			Matrix4fTools.transformMul(tmp, local);
			
			assert(!TransformTools.isNan(tmp)):tmp.getMatrix(new Matrix4f());
			c.setWorldTransform(tmp);
		}
	}

	public void onPhysicsAdd(SegmentController segmentController, PhysicsExt physics) {
		generate(physics, segmentController);
	}

	public void onPhysicsRemove(SegmentController segmentController, PhysicsExt physics) {
		remove(physics, segmentController.getPhysicsDataContainer());		
	}

	@Override
	public String toString() {
		return "[StabilizerPath: "+pw.getSegmentController()+"; "+unit+"]";
	}

	public void onHit(Damager damager, float damage) {
		if(damager == null) {
			//activation beam and such
			return;
		}
		if(damager.getShootingEntity() instanceof SegmentController &&
				((SegmentController)damager.getShootingEntity()).railController.isInAnyRailRelationWith(pw.getSegmentController())){
			//dont hit own beam
			return;
		}
		if(pw.getSegmentController().getState().getUpdateTime() > this.hitTime + 200){
			this.hitTime = pw.getSegmentController().getState().getUpdateTime();
//			pw.destroyStabilizersBasedOnReactorSize(damager);
			pw.doEnergyStreamCooldownOnHit(damager, damage, this.hitTime);
		}
	}
	
	public boolean isHit(){
		return getHitDurationSec() > 0;
	}
	public float getHitDurationSec(){
		return pw.getCurrentEnergyStreamDamageCooldown();
	}

	public float getRadius() {
		return Math.max(0.4f, (float) (weight * pw.getStabilzerPathRadius()));
	}
}
