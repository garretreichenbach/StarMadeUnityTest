package org.schema.game.common.controller.elements;

import org.schema.game.common.data.player.inventory.Inventory;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InventoryMap extends Long2ObjectOpenHashMap<Inventory> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public final ObjectArrayList<Inventory> inventoriesList = new ObjectArrayList<Inventory>();

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Inventory put(Long k, Inventory v) {
		Inventory old = super.put(k, v);
		if (old != null) {
			boolean remove = inventoriesList.remove(old);
			assert(remove);
		}
		assert(v != null);
		inventoriesList.add(v);
		return old;
	}
	@Override
	public Inventory put(long k, Inventory v) {
		Inventory old = super.put(k, v);
		
		if (old != null) {
			boolean remove = inventoriesList.remove(old);
			assert(remove);
		}
		assert(v != null);
		inventoriesList.add(v);
		return old;
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap#remove(java.lang.Object)
	 */
	@Override
	public Inventory remove(Object k) {
		if(!(k instanceof Long)){
			throw new IllegalArgumentException();
		}
		Inventory old = super.remove(k);
		if (old != null) {
			inventoriesList.remove(old);
		}
		return old;
	}
	@Override
	public Inventory remove(long k) {
		Inventory old = super.remove(k);
		if (old != null) {
			inventoriesList.remove(old);
		}
		return old;
	}
	@Override
	public boolean containsKey(Object ok) {
		if(!(ok instanceof Long)){
			throw new IllegalArgumentException();
		}
		return super.containsKey(ok);
	}
	@Override
	public Inventory get(Object ok) {
		if(!(ok instanceof Long)){
			throw new IllegalArgumentException();
		}
		return super.get(ok);
	}

}
