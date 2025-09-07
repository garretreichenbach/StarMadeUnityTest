package org.schema.game.common.controller.ai;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.player.CrewFleetRequest;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.UniqueInterface;

public class UnloadedAiContainer implements AiInterfaceContainer {
	public String uid;
	private final byte type;
	private AiInterface entity;
	private StateInterface state;

	public UnloadedAiContainer(String uid, StateInterface state, byte type) {
		super();
		this.uid = uid;
		this.state = state;
		this.type = type;
	}

	public UnloadedAiContainer(AiInterface entity) {
		this(entity.getUniqueIdentifier(), entity.getState(), entity instanceof AICreature<?> ? CrewFleetRequest.TYPE_CREW : CrewFleetRequest.TYPE_FLEET);
		this.entity = entity;
	}

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public String getRealName() throws UnloadedAiEntityException {
		checkEntity();
		return entity.getRealName();
	}

	@Override
	public AiInterface getAi() throws UnloadedAiEntityException {
		checkEntity();
		return entity;
	}

	@Override
	public byte getType() {
		return type;
	}

	@Override
	public Vector3i getLastKnownSector() throws UnloadedAiEntityException {
		//TODO implement
		checkEntity();
		return null;
	}

	private void checkEntity() throws UnloadedAiEntityException {
		if(entity == null) {
			Int2ObjectOpenHashMap<Sendable> localObjects = state.getLocalAndRemoteObjectContainer().getLocalObjects();
			synchronized(localObjects) {
				for(Sendable s : localObjects.values()) {
					if(s instanceof UniqueInterface && s instanceof AiInterface && uid.contains(((AiInterface) s).getRealName())) {
						entity = (AiInterface) s;
						return;
					}
					//					else if(s instanceof TagSerializable && s instanceof AiInterface && ((TagSerializable)s).getUniqueIdentifier() != null){
					//						System.err.println("NOT: "+((TagSerializable)s).getUniqueIdentifier()+" / "+uid);
					//					}
				}
			}
		} else {
			if(!state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().containsKey(((Sendable) entity).getId())) {
				System.err.println("[PLAYERAI] " + state + " ENTITY " + entity + " HAS BEEN UNLOADED");
				entity = null;
				checkEntity();
			}

			//entity has been loaded
			return;
		}
		if(entity == null) throw new UnloadedAiEntityException(uid);
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
		return uid.equals(((UnloadedAiContainer) obj).uid);
	}

}
