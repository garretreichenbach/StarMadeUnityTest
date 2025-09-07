package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

public class RepulseHandler implements PowerConsumer {
	private final Ship ship;
	private final SphereShape sphere = new SphereShape(5f);
	private float thrustToRepul;
	public static final int MAX_REPULSE_BLOCKS = 32;
	private final Long2ByteOpenHashMap repulseBlocks = new Long2ByteOpenHashMap(MAX_REPULSE_BLOCKS);
	private Vector3f p = new Vector3f();
	private Transform trans = new Transform();
	private float prefDist;
	private float force;
	private float powered;
	private long lastActive;
	private boolean inUse;
	
	public RepulseHandler(Ship ship) {
		super();
		this.ship = ship;
	}
	
	public void add(long index4, byte orientation){
		long index = ElementCollection.getPosIndexFrom4(index4);
		repulseBlocks.addTo(index, orientation);
	}
	
	public void remove(long index4){
		long index = ElementCollection.getPosIndexFrom4(index4);
		repulseBlocks.remove(index);
	}
	
	public int getRepulsorSize(){
		return repulseBlocks.size();
	}
	
	public boolean hasRepulsors(){
		return repulseBlocks.size() > 0;
	}

	public float getThrustToRepul() {
		return thrustToRepul;
	}
	
	public void setThrustToRepul(float thrustToRepul) {
		this.thrustToRepul = Math.min(1f, Math.max(0f, thrustToRepul));
	}
	
	

	public void handle(Timer timer) {
		force = ship.getManagerContainer().getThrusterElementManager().getActualThrust() * VoidElementManager.REPULSE_MULT * thrustToRepul / getRepulsorSize() * powered;
		for (Entry e : repulseBlocks.long2ByteEntrySet()) {
			handleSingle(timer, e.getLongKey(), e.getByteValue());
		}
	}
	/**
	 * Each repulsor block finds the closest block and uses it to determine where to push/pull the ship to
	 * - Force is determined by the amount of thrust on the ship, how much is dedicated to the repulsors and that is distributed over the amount of repulsors 
	 * - Different modes: hover | directional Thrust | tbc..
	 * 
	 */
	public void handleSingle(Timer timer, long pos, byte orientation){		
		//Balance: 1 mass == 10 force 	
		//System.out.println("RepulseHandler thrust: " + ship.getManagerContainer().getThrusterElementManager().getActualThrust() + " thrustToRepul " + thrustToRepul);
		
		PhysicsExt physics = ship.getPhysics();
		
		CollisionObject cObject = ship.getPhysicsDataContainer().getObject();
		if(physics != null && cObject != null && cObject instanceof RigidBodySegmentController){
			ElementCollection.getPosFromIndex(pos, p);
			p.x -= Segment.HALF_DIM;
			p.y -= Segment.HALF_DIM;
			p.z -= Segment.HALF_DIM;
			trans.set(ship.getWorldTransform());
			trans.basis.transform(p);
			trans.origin.add(p);

			ClosestConvexResultCallbackExt c = new ClosestConvexResultCallbackExt(trans.origin, trans.origin);
			c.ownerObject = cObject;
			c.sphereOverlapping = new ObjectArrayList();
			c.sphereOverlappingNormals = new ObjectArrayList();
			c.sphereDontHitOwner = true;
			physics.getDynamicsWorld().convexSweepTest(sphere, trans, trans, c);
			
			//Trans origin: vector location of the repulse handler block relative to the sector axles
			//c.sphereOverlapping: vector location of the overlapping sphere blocks relative to the sector axles			
			if(c.hasHit()){
				inUse = true;
				lastActive = timer.currentTime;
				RigidBodySegmentController body = (RigidBodySegmentController)cObject;
				
				Vector3f repulDir = new Vector3f(Element.DIRECTIONSf[orientation]);
				trans.basis.transform(repulDir);
				
				//Something to let the player customize?
				prefDist = 5f;
				float maxDist = Math.min(prefDist + 0.5f, sphere.getRadius() + 0.5f) ;
				
				Vector3f up = new Vector3f(c.sphereOverlappingNormals.get(0));
				//Sometimes seems stuck on a hit block
				Vector3f hit = c.sphereOverlapping.get(0);
				float dist = Math.min(Vector3fTools.diffLength(hit, trans.origin), maxDist);
				
				//filters out bad results from neighbouring chunks
				for(Vector3f found : c.sphereOverlapping){
					float testDist = Vector3fTools.diffLength(found, trans.origin);
					if(dist > testDist){
						dist = testDist;
						hit = found;
					}
				}		

				float forceMult = force + force * FastMath.cos(dist * FastMath.PI/ maxDist);
				float lockMult = 2 * force * FastMath.cos(dist * FastMath.PI/ maxDist);
				
				//value between -1 and 1
				float angleMult = FastMath.cos(FastMath.acos(Vector3fTools.dot(repulDir, up)));
				float linDamp = 0.2f;
				float angDamp = 0.2f;
				

				//apply rotation dampening when not in flight mode, flight mode already dampens it
				if (!ship.isConrolledByActivePlayer()) {
					angDamp *= 2;
				}
				Vector3f gravity = new Vector3f();
				body.getGravity(gravity);
				
				//In gravity, hover but don't lock
				//Outside gravity, push down if you're getting further away
				if(gravity.length() != 0){
					angleMult = Math.max(0, angleMult);
					forceMult *= angleMult;
					linDamp *= angleMult;
					angDamp *= angleMult;
				} else {
					forceMult = lockMult * angleMult;
					linDamp *= angleMult;
					angDamp *= angleMult;
				}
				
				//System.out.println("RepulseHandler forceMult " + forceMult + " gravity length " + gravity.length() + " up " + up);
				
				up.scale(forceMult);
				body.setDamping(linDamp, angDamp);
				body.applyForce(up, p);
			}
		}
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return ship.getManagerContainer().getThrusterElementManager().getPowerConsumedPerSecondResting() * thrustToRepul;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return ship.getManagerContainer().getThrusterElementManager().getPowerConsumedPerSecondCharging() * thrustToRepul;
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		if(inUse && (curTime - lastActive) > 500 ){
			lastActive = curTime;
			inUse = false;
		}
		return inUse;
		
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.THRUST;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}
	@Override
	public String getName() {
		return "Repulse";
	}
	@Override
	public void dischargeFully() {
	}
}
