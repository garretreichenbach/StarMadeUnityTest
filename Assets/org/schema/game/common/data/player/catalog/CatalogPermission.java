package org.schema.game.common.data.player.catalog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.elements.EntityIndexScore;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CatalogPermission implements TagSerializable, SerializationInterface {

	public final static int P_BUY_FACTION = 1;
	public final static int P_BUY_OTHERS = 2;
	public final static int P_BUY_HOME_ONLY = 4;
	public final static int P_LOCKED = 8;
	public final static int P_ENEMY_USABLE = 16;
	public static int MAX_RATING = 10;
	public boolean changeFlagForced;
	private String uid;
	public String ownerUID;
	public String description = "";
	public float rating;
	public int timesSpawned;
	public long date;
	public long price;
	public int permission = getDefaultCatalogPermission() | P_LOCKED;//P_BUY_OTHERS | P_LOCKED; //default permission
	public float mass;
	
	
	
	public final List<CatalogWavePermission> wavePermissions = new ObjectArrayList<CatalogWavePermission>();
	public org.schema.game.server.data.blueprintnw.BlueprintType type;
	public EntityIndexScore score;
	private BlueprintClassification classification;

	public CatalogPermission() {
	}

	public CatalogPermission(CatalogPermission clone) {
		apply(clone);
	}

	public static int getDefaultCatalogPermission() {

		int a = ServerConfig.DEFAULT_BLUEPRINT_ENEMY_USE.isOn() ? P_ENEMY_USABLE : 0;
		int b = ServerConfig.DEFAULT_BLUEPRINT_FACTION_BUY.isOn() ? P_BUY_FACTION : 0;
		int c = ServerConfig.DEFAULT_BLUEPRINT_HOME_BASE_BUY.isOn() ? P_BUY_HOME_ONLY : 0;
		int d = ServerConfig.DEFAULT_BLUEPRINT_OTHERS_BUY.isOn() ? P_BUY_OTHERS : 0;

		return a | b | c | d;
	}

	public boolean enemyUsable() {
		return (permission & P_ENEMY_USABLE) == P_ENEMY_USABLE;
	}

	public boolean faction() {
		return (permission & P_BUY_FACTION) == P_BUY_FACTION;
	}

	public org.schema.game.server.data.blueprintnw.BlueprintType getEntry() {
		assert (type != null);
		return type;
	}
	public BlueprintClassification getClassification(){
		if(classification == null){
			classification = type.type.getDefaultClassification();
		}
		return classification;
	}
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] subs = (Tag[]) tag.getValue();
		uid = (String) subs[0].getValue();
		ownerUID = (String) subs[1].getValue();
		permission = (Integer) subs[2].getValue();
		if (subs[3].getType() == Type.INT) {
			price = (Integer) subs[3].getValue();
		} else {
			price = (Long) subs[3].getValue();
		}
		description = (String) subs[4].getValue();
		date = (Long) subs[5].getValue();
		if(date == 0){
			date = System.currentTimeMillis();
		}
		timesSpawned = (Integer) subs[6].getValue();
		if (subs[7].getType() == Type.FLOAT) {
			mass = (Float) subs[7].getValue();
		}
		if (subs.length > 8 && subs[8].getType() != Type.FINISH) {
			type = org.schema.game.server.data.blueprintnw.BlueprintType.values()[(Integer) subs[8].getValue()];
		}
		if (subs.length > 9 && subs[9].getType() != Type.FINISH) {
			Tag[] wav = (Tag[]) subs[9].getValue();
			for(Tag t : wav){
				if(t.getType() != Type.FINISH){
					CatalogWavePermission p = new CatalogWavePermission();
					p.fromTagStructure(t);
					wavePermissions.add(p);
				}
			}
		}
		if (subs.length > 10 && subs[10].getType() != Type.FINISH) {
			byte clF = subs[10].getByte();
			classification = BlueprintClassification.values()[clF];
		}
	}

	@Override
	public Tag toTagStructure() {

		Tag cTag = new Tag(Type.STRING, null, uid);
		Tag oTag = new Tag(Type.STRING, null, ownerUID);
		Tag permTag = new Tag(Type.INT, null, permission);
		Tag priceTag = new Tag(Type.LONG, null, price);
		Tag descriptionTag = new Tag(Type.STRING, null, description);
		Tag dateTag = new Tag(Type.LONG, null, date);
		Tag timeSpawnedTag = new Tag(Type.INT, null, timesSpawned);
		Tag massTag = new Tag(Type.FLOAT, null, mass);
		Tag typeTag = new Tag(Type.INT, null, type.ordinal());

		Tag[] w = new Tag[wavePermissions.size()+1];
		for(int i = 0; i < wavePermissions.size(); i++){
			w[i] = wavePermissions.get(i).toTagStructure();
		}
		w[w.length-1] = FinishTag.INST;
		Tag waveTag = new Tag(Type.STRUCT, null, w);

		Tag classTag = new Tag(Type.BYTE, null, (byte)getClassification().ordinal());
		
		return new Tag(Type.STRUCT, null, new Tag[]{cTag, oTag, permTag, priceTag, descriptionTag, dateTag, timeSpawnedTag, massTag, typeTag, waveTag, classTag, FinishTag.INST});
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uid.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return uid.equals(((CatalogPermission) obj).uid);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CatalogPermission [catUID=" + uid + ", ownerUID=" + ownerUID
				+ ", description=" + description + ", price=" + price
				+ ", permission=" + permission + "]";
	}

	public boolean homeOnly() {
		return (permission & P_BUY_HOME_ONLY) == P_BUY_HOME_ONLY;
	}

	public boolean locked() {
		return (permission & P_LOCKED) == P_LOCKED;
	}

	public boolean others() {
		return (permission & P_BUY_OTHERS) == P_BUY_OTHERS;
	}

	public void recalculateRating(Object2IntArrayMap<String> map) {
		if (map.isEmpty()) {
			rating = -1;
		} else {
			float votes = map.size();
			float totalPoints = 0;
			for (int vote : map.values()) {
				totalPoints += vote;
			}
			rating = totalPoints / votes;
		}
	}

	public void setPermission(boolean b, int permission) {
		if (b) {
			this.permission |= permission;
		} else {
			this.permission &= ~permission;
		}
		System.err.println("SET PERMISSIONS: " + permission + ": " + b + ": " + this.permission + " fac: " + faction() + "; others: " + others() + "; home " + homeOnly() + "; enemy " + enemyUsable());
	}

	public void apply(CatalogPermission clone) {
		changeFlagForced = clone.changeFlagForced;
		uid = new String(clone.uid);
		ownerUID = new String(clone.ownerUID);
		description = new String(clone.description);
		price = clone.price;
		mass = clone.mass;
		permission = clone.permission;
		classification = clone.classification;
		this.type = clone.type;
		this.wavePermissions.clear();
		this.wavePermissions.addAll(clone.wavePermissions);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setClassification(BlueprintClassification classification) {
		this.classification = classification;
	}


	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeUTF(this.uid);
		b.writeUTF(this.ownerUID);
		b.writeUTF(this.description);
		b.writeInt(this.permission);
		b.writeLong(this.price);
		b.writeBoolean(this.changeFlagForced);
		b.writeFloat(this.rating);
		b.writeFloat(this.mass);
		b.writeByte((byte) this.type.ordinal());
		b.writeLong(this.date);
		b.writeByte(this.getClassification().ordinal());
		boolean hasScoe = this.score != null;
		b.writeBoolean(hasScoe);
		if (hasScoe) {
			this.score.serialize(b, isOnServer);
		}
		
		b.writeInt(this.wavePermissions.size());
		for(int i = 0; i < this.wavePermissions.size(); i++){
			this.wavePermissions.get(i).serialize(b);
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		String name = b.readUTF();
		String owner = b.readUTF();
		String description = b.readUTF();
		int permission = b.readInt();
		long price = b.readLong();
		boolean forced = b.readBoolean();
		float rating = b.readFloat();
		float mass = b.readFloat();
		int typeOrdinal = b.readByte();
		long date = b.readLong();
		byte clF = b.readByte();

		this.uid = name;
		this.ownerUID = owner;
		this.permission = permission;
		this.description = description;
		this.price = price;
		this.date = date;
		this.changeFlagForced = forced;
		this.rating = rating;
		this.mass = mass;
		this.type = BlueprintType.values()[typeOrdinal];
		this.classification = BlueprintClassification.values()[clF];

		boolean hasScore = b.readBoolean();
		if (hasScore) {
			this.score = new EntityIndexScore();
			this.score.deserialize(b, updateSenderStateId, isOnServer);
		}

		this.wavePermissions.clear();
		
		final int wpSize = b.readInt();
		for(int i = 0; i < wpSize; i++){
			CatalogWavePermission p = new CatalogWavePermission();
			p.deserialize(b);
			
			this.wavePermissions.add(p);
		}		
	}

}
