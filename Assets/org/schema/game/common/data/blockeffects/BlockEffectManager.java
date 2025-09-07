package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.blockeffects.updates.BlockEffectDeadUpdate;
import org.schema.game.common.data.blockeffects.updates.BlockEffectSpawnUpdate;
import org.schema.game.common.data.blockeffects.updates.BlockEffectUpdate;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.network.objects.remote.RemoteBlockEffectUpdate;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class BlockEffectManager {
	public final FastSegmentControllerStatus status = new FastSegmentControllerStatus();
	private final Short2ObjectOpenHashMap<BlockEffect> effects = new Short2ObjectOpenHashMap<BlockEffect>();
	private final ObjectOpenHashSet<BlockEffectTypes> activeEffectTypes = new ObjectOpenHashSet<BlockEffectTypes>();
	private final ObjectOpenHashSet<BlockEffect> activeEffectsSet = new ObjectOpenHashSet<BlockEffect>();
	//	private final Object2ObjectOpenHashMap<BlockEffectTypes, ObjectArrayList<BlockEffect>> blockActiveEffects = new Object2ObjectOpenHashMap<BlockEffectTypes, ObjectArrayList<BlockEffect>>();
	private final SendableSegmentController segmentController;
	private final ObjectArrayFIFOQueue<BlockEffectUpdate> receivedUpdates = new ObjectArrayFIFOQueue<BlockEffectUpdate>();
	private short idGen;

	public BlockEffectManager(SendableSegmentController controller) {
		this.segmentController = controller;
	}

	public void addEffect(BlockEffect effect) {
		if(effect.affectsMother() && segmentController.railController.getRoot() != segmentController){
//			if(!segmentController.isUsingOldPower()){
//				System.err.println("[EFFECT] PUTTING EFFECT ON MOTHER "+segmentController.railController.getRoot());
//			}
			((SendableSegmentController)segmentController.railController.getRoot()).getBlockEffectManager().addEffect(effect);
		}else{
			effect.setId(idGen++);
			
			effects.put(effect.getId(), effect);
			refreshActiveEffects();
			effect.pendingBroadcastUpdates.add(getSpawnUpdate(effect));
//			if(!segmentController.isUsingOldPower()){
//				System.err.println("[EFFECT] PUTTING EFFECT ON "+segmentController+"; ID: "+effect.getId()+"; "+effect+"; Active Set: "+getActiveEffectsSet());
//			}
		}
	}

	/**
	 * @return the activeEffects
	 */
	public ObjectOpenHashSet<BlockEffectTypes> getActiveEffectTypes() {
		return activeEffectTypes;
	}

	public BlockEffect getEffect(BlockEffectTypes effect) {
		for (BlockEffect e : effects.values()) {
			if (e.getType() == effect) {
				return e;
			}
		}
		return null;
	}

	public BlockEffect getEffectByBlockIdentifyer(long blockIndex) {
		for (BlockEffect e : effects.values()) {
			if (ElementCollection.getPosIndexFrom4(e.getBlockAndTypeId4()) == blockIndex) {
				return e;
			}
		}
		return null;
	}

	public <E extends BlockEffect> BlockEffectSpawnUpdate<E> getSpawnUpdate(E effect) {

		BlockEffectSpawnUpdate<E> effectSpawnUpdate = new BlockEffectSpawnUpdate<E>(effect.getId(), effect.getBlockAndTypeId4());
		effectSpawnUpdate.effectType = (byte) effect.getTypeOrd();
		effectSpawnUpdate.sendBlockEffect = effect;
		//		effect.getSp(effectSpawnUpdate);
		assert (effectSpawnUpdate.effectType == effect.getTypeOrd()) : effectSpawnUpdate.effectType + "; " + effect.getTypeOrd() + ": " + effect;

		return effectSpawnUpdate;
	}

	public boolean hasEffect(BlockEffectTypes type) {
		return activeEffectTypes.contains(type);
	}

	private void refreshActiveEffects() {
		activeEffectTypes.clear();
		activeEffectsSet.clear();

		LongOpenHashSet h = segmentController.getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(Ship.core));

		for (BlockEffect e : effects.values()) {
			if (e.isAlive()) {
				if (segmentController.isOnServer()) {
					if (activeEffectTypes.contains(e.getType())) {
						e.end();
					}
					if (e.getBlockAndTypeId4() != Long.MIN_VALUE && segmentController instanceof Ship) {

//						if(h != null ){
//							System.err.println(segmentController.getState()+" EFFECT "+e.getType().name()+" ACTIVBE::: CONENCTED: "+h.contains(e.getBlockAndTypeId4()));
//						}

						if (h == null || !h.contains(e.getBlockAndTypeId4())) {
//							if(!segmentController.isUsingOldPower()){
//								System.err.println("[EFFECT] EFFECT DIED FROM BLOCK CONNECTION: "+e);
//							}
							e.end();
						}
					}
				}
//				if(!segmentController.isOnServer()){
//					System.err.println("ACIVE::: "+e.getType());
//				}
				activeEffectsSet.add(e);
				activeEffectTypes.add(e.getType());

			}else{
//				if(!segmentController.isUsingOldPower()){
//					System.err.println("[EFFECT] EFFECT DIED: "+e);
//				}
			}
		}
	}

	private void sendEffectUpdates() {
		if (effects.size() > 0) {
			for (BlockEffect m : effects.values()) {
				//broadcast first (add/remove)
				if (m.hasPendingBroadcastUpdates()) {
					m.sendPendingBroadcastUpdates(segmentController);
				}

				if (m.hasPendingUpdates() && m.isAlive()) {
					m.sendPendingUpdates(segmentController);
				}

				m.clearAllUpdates();
			}
		}
	}

	public void updateClient(Timer timer) {

		//		if(segmentController instanceof Ship){
		//			System.err.println(segmentController+" UPDATING CLIENT EFFECTS: "+effects);
		//		}
		status.reset();
		ObjectIterator<BlockEffect> iterator = effects.values().iterator();
		while (iterator.hasNext()) {
			BlockEffect e = iterator.next();
			e.update(timer, status);
			if (e.isAlive()) {
			} else {
				//System.err.println(segmentController.getState() + " " + segmentController + " REMOVING EFFECT " + e);
				iterator.remove();
			}
		}

		refreshActiveEffects();
		while (receivedUpdates.size() > 0) {
			BlockEffectUpdate u = receivedUpdates.dequeue();
			u.handleClientUpdate(effects, segmentController);
		}

	}

	public void updateFromNetworkObject(NetworkSegmentController s) {
		for (int i = 0; i < s.effectUpdateBuffer.getReceiveBuffer().size(); i++) {
			BlockEffectUpdate effectUpdate = s.effectUpdateBuffer.getReceiveBuffer().get(i).get();
//			System.err.println("[CLIENT] Received effect update: "+effectUpdate);
			receivedUpdates.enqueue(effectUpdate);
		}
	}

	public void updateToFullNetworkObject(NetworkSegmentController networkObject) {

		//send all current effects

		ObjectIterator<BlockEffect> iterator = effects.values().iterator();
		while (iterator.hasNext()) {
			BlockEffect m = iterator.next();
			BlockEffectSpawnUpdate<BlockEffect> spawnUpdate = getSpawnUpdate(m);
			networkObject.effectUpdateBuffer.add(new RemoteBlockEffectUpdate(spawnUpdate, segmentController.isOnServer()));
		}
	}

	public void updateServer(Timer timer) {
		ObjectIterator<BlockEffect> iterator = effects.values().iterator();
		status.reset();

		//active effects must be refreshed before the updates are sent
		refreshActiveEffects();
		while (iterator.hasNext()) {
			BlockEffect m = iterator.next();

//			if(!segmentController.isUsingOldPower()){
//				System.err.println("[EFFECT] UPDATING EFFECT "+m);
//			}
			m.update(timer, status);

			if (m.isAlive()) {
//				System.err.println(segmentController.getState()+" "+segmentController+" ALIVE EFFECT "+m+" update in case of death: "+m.needsDeadUpdate());
			} else {
//				System.err.println(segmentController.getState()+" "+segmentController+" REMOVING EFFECT "+m);
				if (m.needsDeadUpdate()) {
					BlockEffectDeadUpdate du = new BlockEffectDeadUpdate(m.getId(), m.getBlockAndTypeId4());
					m.pendingBroadcastUpdates.add(du);
				}
			}
		}
		sendEffectUpdates();

		iterator = effects.values().iterator();
		while (iterator.hasNext()) {
			BlockEffect m = iterator.next();
			if (!m.isAlive()) {
				iterator.remove();
			}
		}
	}

	/**
	 * @return the activeEffectsSet
	 */
	public ObjectOpenHashSet<BlockEffect> getActiveEffectsSet() {
		return activeEffectsSet;
	}

}
