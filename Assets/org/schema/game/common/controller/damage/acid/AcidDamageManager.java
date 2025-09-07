package org.schema.game.common.controller.damage.acid;


import java.util.Arrays;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AcidDamageManager {
	private static final ObjectArrayList<AcidDamageContainer> pool = new ObjectArrayList<AcidDamageContainer>();
	private static final int POOL_SIZE = 512;
//	private static final int MAX_UPDATE_COUNT = 256;
//	private static final long UPDATE_FREQUENCY = 90;
	private static final int MAX_UPDATE_COUNT = 2048;
	private static final long UPDATE_FREQUENCY = 10;
	static{
		for(int i = 0; i < POOL_SIZE; i++){
			pool.add(new AcidDamageContainer());
		}
	}
	
	private final EditableSendableSegmentController segmentController;
	private short idGen;
	public static class AcidDamageContainer{
		
		public short id;
		public EditableSendableSegmentController segmentController;
		public Damager damager;
		public Vector3f firingDir;
		public Vector3f dirPrio;

		private final SegmentPiece piece = new SegmentPiece(); 
		private final SegmentPiece pieceOld = new SegmentPiece(); 
		
		public long weaponId;
		//only used for logging
		public float damageTotal;
		public int invalidCount;

		private final LongOpenHashSet executedBlock = new LongOpenHashSet(128);
		private final LongArrayList toPropagate = new LongArrayList(128); 
		private final Long2IntOpenHashMap toDamage = new Long2IntOpenHashMap(128); 
		private final Long2IntOpenHashMap toPropagationCount = new Long2IntOpenHashMap(128); 
		private final Long2ByteOpenHashMap toPropagationAirTravel = new Long2ByteOpenHashMap(128); 
		
		public final DamageDealerType damageType = DamageDealerType.PROJECTILE;
		private long lastUpdate;
		private SegmentPiece[] tmpPieceBuffer = new SegmentPiece[6];
		{
			for(int i = 0; i < tmpPieceBuffer.length; i++) {
				tmpPieceBuffer[i] = new SegmentPiece();
			}
		}
		private SegmentPiece[] sideBuffer = new SegmentPiece[6];
		//true if acid damage should propagate on air for one step
		public boolean propagateOnInitallyKilled;
		public boolean decoOnly;
		public byte airTravel;
		
		private void addPropagate(long pos, int propagate, int damage, byte air){
			toPropagate.add(pos);
			toDamage.put(pos, damage);
			toPropagationCount.put(pos, propagate);
			toPropagationAirTravel.put(pos, air);
		}
		
		private void editPropagate(long pos, int damage, byte air){
			if(toDamage.containsKey(pos)){
//				System.out.println("PROPAGATION editing propagation " + toDamage.get(found) + " -> " +  (toDamage.get(found) + damage) + " at " + found);
				toDamage.addTo(pos, damage);
				toPropagationAirTravel.put(pos, air);
			}
		}
		
		public boolean update(Timer timer, AcidDamageManager acidDamageManager){
			if(toPropagate.size() > 0){
				propagate(timer, acidDamageManager);
				return true;
			}else{
				//nothing to do. propagation finished
				return false;
			}
		}
		public void clear(){
			//propagated.clear();
			toPropagate.clear();
			toDamage.clear();
			toPropagationCount.clear();
			toPropagationAirTravel.clear();
			executedBlock.clear();
			id = -1;
			lastUpdate = 0;
			damager = null;
			segmentController = null;
			dirPrio = null;
			piece.setSegment(null);
			//only used for logging
			damageTotal = 0;
			invalidCount = 0;
			propagateOnInitallyKilled = false;
			decoOnly = false;
			Arrays.fill(sideBuffer, null);
		}
		
		
		private void propagate(Timer timer, final AcidDamageManager acidDamageManager){
			if(timer.currentTime - lastUpdate < UPDATE_FREQUENCY){
				return;
			}
			

			
			
			lastUpdate = timer.currentTime;
			final int count = acidDamageManager.updateCount >= 0 ? Math.min(toPropagate.size(), acidDamageManager.updateCount) : toPropagate.size();	
			
			//Damage/Kill current block, then spread remaining damage to all touching blocks:
			//If block is killed:
				//Propagate remaining damage to surrounding blocks if they exist, weigh damage based on firing direction and block you're looking at
				//Make sure to only propagate to the ones that aren't propagated or dead already
				
				//If the found blocks already have a propagation queued up
					//Add the remaining damage to that one
				
			//If block is only damaged
				//Nothing, just update it
			
			for(int i= 0; i < count; i++){
				
				long l = toPropagate.removeLong(0);
				executedBlock.add(l);
				int damage = toDamage.remove(l);
				assert(damage >= 0):damage;
				int propagateVal = toPropagationCount.remove(l);
				byte air = toPropagationAirTravel.remove(l);
				acidDamageManager.updateCount = Math.max(0, acidDamageManager.updateCount - 1);	
				
				final int ox = ElementCollection.getPosX(l);
				final int oy = ElementCollection.getPosY(l);
				final int oz = ElementCollection.getPosZ(l);
				final long index = l;

//				System.err.println("DAMAGE: "+damage+"; "+propagateVal);
				
				if(propagateOnInitallyKilled) {
					//propagate with the assumption that the source block is already dead
					propagateToSurroundingBlocks(damage, ox, oy, oz, propagateVal, this.airTravel);
					propagateOnInitallyKilled = false; //only do this one time
				}else {
					
					
					
					//Apply damage to block
					SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(index, piece);
					if (pointUnsave != null) {
						if(ElementKeyMap.isValidType(pointUnsave.getType())) {
							short beforeDamHitpointsByte = pointUnsave.getHitpointsByte();
							
							pieceOld.setByValue(pointUnsave);
							float damageDone = segmentController
								.damageElement(
									pointUnsave.getType(),
									pointUnsave.getInfoIndex(),
									pointUnsave.getSegment().getSegmentData(),
									damage,
									damager,
									damageType,
									weaponId);
							
							
							pointUnsave.refresh();
							damageTotal += damageDone;
		
							short afterDamHitpointsByte = pointUnsave.getHitpointsByte();
							float damageRemaining = damage - damageDone;
	//						System.err.println("DAMAGE REMAINING: "+damageRemaining+"; DONE: "+damageDone+"; left: "+afterDamHitpoints);
							
							if(beforeDamHitpointsByte > afterDamHitpointsByte) {
								if(afterDamHitpointsByte > 0) {
									assert(damageRemaining <= 0):"There should be no rest damage remaining";
									segmentController
										.sendBlockHpByte(index, afterDamHitpointsByte);
								} else {
									segmentController.onBlockKill(pieceOld, damager);
									segmentController.sendBlockKill(index);
									propagateToSurroundingBlocks(damageRemaining, ox, oy, oz, propagateVal, this.airTravel);
								}
							}
						}else if(air > 0){
							air--;
							propagateToSurroundingBlocks(damage, ox, oy, oz, propagateVal, air);
						}
					}
				}
			}
		}

		private int propagateToSurroundingBlocks(final float damageRemaining, final int ox, final int oy, final int oz, int propagateVal, byte air) {
			//Check for how many blocks are alive and surrounding it
			if (damageRemaining > 0 && propagateVal > 0) {

				float foundBlocks = 0;
				Arrays.fill(sideBuffer, null);
				for (int di = 0; di < 6; di++) {
					

					int airTries = air;
					
					int airLen = 1;
					do {
						final int dx = Element.DIRECTIONSi[di].x*airLen;
						final int dy = Element.DIRECTIONSi[di].y*airLen;
						final int dz = Element.DIRECTIONSi[di].z*airLen;
						final int x = ox + dx;
						final int y = oy + dy;
						final int z = oz + dz;
						long indexNeighbour = ElementCollection.getIndex(x, y, z);
						
						SegmentPiece pointUnsaveNeighbour = segmentController.getSegmentBuffer().getPointUnsave(indexNeighbour, tmpPieceBuffer[di]);
						if (pointUnsaveNeighbour != null) {
							if((ElementKeyMap.isValidType(pointUnsaveNeighbour.getType()))) {
								if(airLen == 1 || !executedBlock.contains(indexNeighbour)) {
									foundBlocks++;
									sideBuffer[di] = pointUnsaveNeighbour;
								}
							}else if(airTries > 0 && executedBlock.contains(indexNeighbour)) {
								//don't travel over blocks we already executed
								break;
							}
						}
						airLen++;
					} while(sideBuffer[di] == null && airTries-- > 0);

				}
				final boolean decoOnly = this.decoOnly;
				//System.out.println("PROPAGATING " + pointUnsave.getPos(new Vector3b()) + " foundBlocks " + foundBlocks);
				if (foundBlocks > 0) {
					float damagePerBlock;
					//split damage to surrounding blocks
					damagePerBlock = damageRemaining / foundBlocks;
					propagateVal--;

					for (int di = 0; di < 6; di++) {
						//float weight = 0;
						if(sideBuffer[di] != null) {
							SegmentPiece pointUnsaveNeighbour = sideBuffer[di];
							long indexNeighbour = pointUnsaveNeighbour.getAbsoluteIndex();
	
							//if this acid is deco only, only propagate if the block is decorative. otherwise always propagate on any block
							if (!decoOnly || pointUnsaveNeighbour.getInfo().isDecorative()) {
								
								if (damagePerBlock > 0) {
									if (!toDamage.containsKey(indexNeighbour)) {
										addPropagate(indexNeighbour, propagateVal, (int) (damagePerBlock), air);
									} else {
										editPropagate(indexNeighbour, (int) (damagePerBlock), air);
									}
								}
	
							}
						}

					}
				}
			}
			return propagateVal;
		}
	}
	
	
	
	private static void freeInst(AcidDamageContainer c){
		synchronized(pool){
			if(pool.size() < POOL_SIZE){
				c.clear();
				pool.add(c);
			}
		}
	}
	private static AcidDamageContainer getInst(){
		synchronized(pool){
			if(pool.isEmpty()){
				return new AcidDamageContainer();
			}else{
				return pool.remove(pool.size()-1);
			}
		}
	}

	private ObjectArrayList<AcidDamageContainer> props = new ObjectArrayList<AcidDamageContainer>(64);
	private int updateCount;
	
	public AcidDamageManager(EditableSendableSegmentController segmentController){
		this.segmentController = segmentController;
	}
	
	public void inputDamage(long pos, Vector3f firingDirection, int damage, int propagation, Damager damager, long weaponId, boolean propagateOnInitallyKilled, boolean decoOnly) {
		if(propagation > 0){
			short id = (short) ((idGen+1)%(Short.MAX_VALUE-1));
			AcidDamageContainer inst = getInst();
			assert(inst.segmentController == null):inst.segmentController;
			inst.propagateOnInitallyKilled = propagateOnInitallyKilled;
			inst.segmentController = segmentController;
			inst.firingDir = firingDirection;
			inst.decoOnly = decoOnly;
			inst.airTravel = 1;
//			System.out.println("PROPAGATING DirectionPrio: " + inst.dirPrio);
			inst.weaponId = weaponId;
			inst.damager = damager;
			inst.id = idGen++;
			inst.addPropagate(pos, propagation, damage, inst.airTravel);
			
			props.add(inst);
		}
	}
	public void clear(){
		for(int i = 0; i < props.size(); i++){
			AcidDamageContainer mmm = props.get(i);
			mmm.clear();
			freeInst(mmm);
		}
		props.clear();
	}
	public void update(Timer timer){
		this.updateCount = MAX_UPDATE_COUNT;
		for(int i = 0; i < props.size(); i++){
			AcidDamageContainer acidDamageContainer = props.get(i);
			
			
			boolean alive = acidDamageContainer.update(timer, this);
			if(!alive){
				AcidDamageContainer remove = props.remove(i);
//				System.out.println("PROPAGATING total damage done: " + remove.damageTotal + " || invalidCount: " + remove.invalidCount);
				remove.clear();
				freeInst(remove);
				i--;
			}
			if(updateCount == 0){
				break;
			}
		}
	}
	

	
}
