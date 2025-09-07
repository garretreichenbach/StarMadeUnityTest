package org.schema.game.common.data.world;

import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;


public class EntityUID implements Comparable<EntityUID>{
	public final String uid;
	public final long id;
	public boolean spawnedOnlyInDb;
	public final EntityType type;
	public long dockedTo;
	public long dockedRoot;
	public boolean tracked;
	
	
	public EntityUID(String uid, EntityType type, long id) {
		super();
		this.uid = uid;
		this.id = id;
		this.type = type;
	}
	@Override
	public int hashCode() {
		return (int)(id^(id>>>32));
	}
	@Override
	public boolean equals(Object obj) {
		return id == ((EntityUID)obj).id;
	}
	@Override
	public int compareTo(EntityUID o) {
		if(spawnedOnlyInDb == o.spawnedOnlyInDb){
			if(type == o.type){
				return uid.compareTo(o.uid);
			}
			if(type == EntityType.SHIP && o.type != EntityType.SHIP){
				//put db only ships in the back
				return 1;
			}
			if(type != EntityType.SHIP && o.type == EntityType.SHIP){
				//put db only ships in the back
				return -1;
			}
			return uid.compareTo(o.uid);
		}else{
			if(spawnedOnlyInDb){
				//put in the back
				return 1;
			}else{
				return -1;
			}
		}
		
	}
	@Override
	public String toString() {
		return "EntityUID [uid=" + uid + ", id=" + id + ", spawnedOnlyInDb="
				+ spawnedOnlyInDb + ", type=" + type.name() + "]";
	}
	
	
}