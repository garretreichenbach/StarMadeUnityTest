package org.schema.game.common.controller.elements;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.explosion.AfterExplosionCallback;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class ModuleExplosion implements TagSerializable{
	private static final byte VERSION = 1;

	private LongList explosionPositions;
	
	private long created;
	
	private long lastExplosion;
	
	private long explosionDelay;
	
	private long moduleId;
	
	private int radius;
	
	private int damage;

	private BoundingBox moduleBB;

	private boolean chain;

	private ExplosionCause cause;
	
	public enum ExplosionCause{
		POWER_AUX,
		INTEGRITY, 
		STABILITY
	}
	
	public ModuleExplosion(LongList explosionPositions, long delayMs, int radius, int damage, long moduleId, ExplosionCause cause, BoundingBox moduleBB){
		this.explosionPositions = explosionPositions;
		this.explosionDelay = delayMs;
		this.radius = radius;
		this.damage = damage;
		this.moduleBB = moduleBB;
		this.moduleId = moduleId;
		this.created = System.currentTimeMillis();
		this.cause = cause; 
	}
	
	
	
	public ModuleExplosion() {
	}



	public void update(Timer timer, SegmentController controller){
		assert(!isFinished());
		assert(controller.isOnServer());
		
		if(timer.currentTime - lastExplosion > explosionDelay){
			executeExplosion(controller, explosionPositions.removeLong(explosionPositions.size()-1));
			lastExplosion = timer.currentTime;
		}else{
		}
	}
	
	
	public boolean isFinished(){
		return explosionPositions.isEmpty();
	}





	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[])tag.getValue();
		byte version = (Byte)t[0].getValue();
		created = (Long)t[1].getValue();
		lastExplosion = (Long)t[2].getValue();
		explosionDelay = (Long)t[3].getValue();
		moduleId = (Long)t[4].getValue();
		radius = (Integer)t[5].getValue();
		damage = (Integer)t[6].getValue();
		moduleBB = new BoundingBox(
				(Vector3f)t[7].getValue(), 
				(Vector3f)t[8].getValue());
		
		byte[] b = (byte[])t[9].getValue();
		
		FastByteArrayInputStream inB = new FastByteArrayInputStream(b);
		DataInputStream in = new DataInputStream(inB);
		int size;
		try {
			size = in.readInt();
			explosionPositions = new LongArrayList(size);
			for(int i = 0; i < size; i++){
				explosionPositions.add(in.readLong());
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		chain = t[9].getType() == Type.BYTE ? ((Byte)t[9].getValue() > 0) : false;
		if(version > 0){
			cause = ExplosionCause.values()[t[10].getByte()];
		}
	}


	@Override
	public Tag toTagStructure() {
		
		LongArrayList l = new LongArrayList(explosionPositions);
		byte[] b = new byte[l.size()*ByteUtil.SIZEOF_LONG + ByteUtil.SIZEOF_INT];
		FastByteArrayOutputStream fb = new FastByteArrayOutputStream(b);
		DataOutputStream d = new DataOutputStream(fb);
		
		try {
			d.writeInt(l.size());
		
			for(int i = 0; i < l.size(); i++){
				d.writeLong(l.getLong(i));
			}
			d.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert(b == fb.array);
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, VERSION),
			new Tag(Type.LONG, null, created),
			new Tag(Type.LONG, null, lastExplosion),
			new Tag(Type.LONG, null, explosionDelay),
			new Tag(Type.LONG, null, moduleId),
			new Tag(Type.INT, null, radius),
			new Tag(Type.INT, null, damage),
			new Tag(Type.VECTOR3f, null, moduleBB.min),
			new Tag(Type.VECTOR3f, null, moduleBB.max),
			new Tag(Type.BYTE_ARRAY, null, b),
			new Tag(Type.BYTE, null, chain ? (byte)1 : (byte)0),
			new Tag(Type.BYTE, null, (byte)cause.ordinal()),
			FinishTag.INST
		});
	}



	public LongList getExplosionPositions() {
		return explosionPositions;
	}



	public long getCreated() {
		return created;
	}



	public long getLastExplosion() {
		return lastExplosion;
	}



	public long getExplosionDelay() {
		return explosionDelay;
	}



	public long getModuleId() {
		return moduleId;
	}



	public int getRadius() {
		return radius;
	}



	public int getDamage() {
		return damage;
	}



	public BoundingBox getModuleBB() {
		return moduleBB;
	}

	private class ExplosionDamager implements Damager{
		public final SegmentController controller;
		
		

		public ExplosionDamager(SegmentController controller) {
			super();
			this.controller = controller;
		}

		@Override
		public StateInterface getState() {
			return controller.getState();
		}

		@Override
		public void sendHitConfirm(byte damageType) {
			controller.sendHitConfirm(damageType);			
		}

		@Override
		public boolean isSegmentController() {
			return false;
		}

		@Override
		public SimpleTransformableSendableObject<?> getShootingEntity() {
			return controller.getShootingEntity();
		}

		@Override
		public int getFactionId() {
			return controller.getFactionId();
		}

		@Override
		public String getName() {
			return controller.getName();
		}

		@Override
		public AbstractOwnerState getOwnerState() {
			return controller.getOwnerState();
		}

		@Override
		public void sendClientMessage(String str, byte type) {
			controller.sendClientMessage(str, type);			
		}

		@Override
		public float getDamageGivenMultiplier() {
			return controller.getDamageGivenMultiplier();
		}

		@Override
		public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
			InterEffectSet set = new InterEffectSet();
			set.setStrength(InterEffectType.EM, 0.2f);
			set.setStrength(InterEffectType.HEAT, 0.6f);
			set.setStrength(InterEffectType.KIN, 0.2f);
			return set;
		}

		@Override
		public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
			return null;
		}

		@Override
		public int getSectorId() {
			return controller.getSectorId();
		}

		@Override
		public void sendServerMessage(Object[] astr, byte msgType) {
			controller.sendServerMessage(astr, msgType);
		}
		
		
	}

	private void executeExplosion(SegmentController controller, long pos) {
		
		Transform t = new Transform();
		t.setIdentity();
		
		t.origin.set(
				ElementCollection.getPosX(pos) - SegmentData.SEG_HALF, 
				ElementCollection.getPosY(pos) - SegmentData.SEG_HALF, 
				ElementCollection.getPosZ(pos) - SegmentData.SEG_HALF);
		
		controller.getWorldTransform().transform(t.origin);
		
		
		((EditableSendableSegmentController)controller)
		.addExplosion(new ExplosionDamager(controller), 
				DamageDealerType.EXPLOSIVE,
				HitType.INTERNAL,
				moduleId,
				t, 
				radius, 
				damage, 
				chain, () -> {
				}, ExplosionData.INNER);
		switch(cause){
		case INTEGRITY:
			if(explosionPositions.size() > 0){
				controller.sendControllingPlayersServerMessage(Lng.astr("---WARNING---\nLow integrity is caused a module to collapse on itself.!\n"), ServerMessage.MESSAGE_TYPE_ERROR);
			}else{
				controller.sendControllingPlayersServerMessage(Lng.astr("Low integrity module no longer collapsing!"), ServerMessage.MESSAGE_TYPE_INFO);
			}
			break;
		case POWER_AUX:
			if(explosionPositions.size() > 0){
				controller.sendControllingPlayersServerMessage(Lng.astr("---WARNING---\nPower Auxiliary Module damaged.\nOverload causing heavy damage!\n"), ServerMessage.MESSAGE_TYPE_ERROR);
			}else{
				controller.sendControllingPlayersServerMessage(Lng.astr("Power Auxiliary Module stabilized!"), ServerMessage.MESSAGE_TYPE_INFO);
			}
			break;
		case STABILITY:
			if(explosionPositions.size() > 0){
				controller.sendControllingPlayersServerMessage(Lng.astr("---WARNING---\nLow reactor stability is causing additional damage on the ship's chambers!\n"), ServerMessage.MESSAGE_TYPE_ERROR);
			}else{
				controller.sendControllingPlayersServerMessage(Lng.astr("Reactor is no longer taking damage from low stability!"), ServerMessage.MESSAGE_TYPE_INFO);
			}
			break;
		default:
			break;
		
		}
		
	}






	public void setChain(boolean chain) {
		this.chain = chain;
	}
	
	
}
