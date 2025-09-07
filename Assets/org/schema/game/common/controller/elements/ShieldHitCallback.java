package org.schema.game.common.controller.elements;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorNotFoundException;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import com.bulletphysics.linearmath.Transform;

public class ShieldHitCallback {

	
	public boolean hasHit;
	public float xWorld;
	public float yWorld;
	public float zWorld;
	private double damage;
	public float xLocalBlock;
	public float yLocalBlock;
	public float zLocalBlock;
	public void reset(){
		hasHit = false;
		damager = null;
		originalHitEntity = null;
		nowHitEntity = null;
		damageType = DamageDealerType.GENERAL;
		inputTransform.setIdentity();
		outputTransform.setIdentity();
		damage = 0;
	}

	public void onShieldOutage(ShieldLocal shieldLocal) {
		shieldLocal.onOutage(true);
	}
	public void onShieldDamage(ShieldLocal shieldLocal) {
		shieldLocal.onDamage(this.damage);
	}
	private Vector3f tmp = new Vector3f();
	public Damager damager;
	public DamageDealerType damageType = DamageDealerType.GENERAL;
	public long weaponId;
	public SegmentController originalHitEntity;
	public SegmentController nowHitEntity;
	public int projectileSectorId;
	
	private final Transform inputTransform = new Transform();
	private final Transform outputTransform = new Transform();
	public void convertWorldHitPoint(SegmentController c) throws SectorNotFoundException {
		tmp.set(xWorld, yWorld, zWorld);
		if(!c.isOnServer()) {
			c.getClientTransformInverse().transform(tmp);
		}else {
//			System.err.println("PROJJ: "+projectileSectorId+" -> "+c.getSectorId());
			if(projectileSectorId != c.getSectorId()) {
				
				Sector sTarget = ((GameServerState)c.getState()).getUniverse().getSector(c.getSectorId());
				Sector sProj = ((GameServerState)c.getState()).getUniverse().getSector(projectileSectorId);
				
				if(sProj != null && sTarget != null) {
					inputTransform.origin.set(tmp);
					SimpleTransformableSendableObject.calcWorldTransformRelative(
							sTarget.getId(), sTarget.pos, sProj.getSectorId(), inputTransform, sTarget.getState(), true, outputTransform, c.v);
					
//					System.err.println("CONVERTED HITPOINT "+tmp+" -> "+outputTransform.origin);
					
					tmp.set(outputTransform.origin);
					
				}else {
					if(sProj == null) {
						throw new SectorNotFoundException(projectileSectorId);
					}
					if(sTarget == null) {
						throw new SectorNotFoundException(c.getSectorId());
					}
				}
				
			}else {
//				System.err.println("HITPOINT NORMAL "+tmp);
			}
			
			c.getWorldTransformInverse().transform(tmp);
		}
		xLocalBlock = tmp.x+Segment.HALF_DIM;
		yLocalBlock = tmp.y+Segment.HALF_DIM;
		zLocalBlock = tmp.z+Segment.HALF_DIM;
	}

	public String getLocalPosString() {
		return xLocalBlock+", "+yLocalBlock+", "+zLocalBlock;
	}
	public String getWorldPosString() {
		return xWorld+", "+yWorld+", "+zWorld;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}



}
