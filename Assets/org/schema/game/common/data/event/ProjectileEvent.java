package org.schema.game.common.data.event;

import org.schema.schine.event.Event;
import org.schema.schine.event.EventPayload;
import org.schema.schine.event.EventType;

public class ProjectileEvent extends Event{

	@Override
	public EventType getType() {
		return EventType.PROJECTILE;
	}

	public final EventPayload damage = new EventPayload();
	public final EventPayload position = new EventPayload();
	public final EventPayload direction = new EventPayload(); 
	public final EventPayload damageType = new EventPayload(); 
	
	public final EventPayload[] parameters = new EventPayload[]{damage, position, direction, damageType};
	
	@Override
	public EventPayload[] getParameters() {
		return parameters;
	}
}
