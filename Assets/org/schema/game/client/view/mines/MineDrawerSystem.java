package org.schema.game.client.view.mines;

import java.util.Collection;

import org.schema.game.client.view.meshlod.LODDrawerSystem;
import org.schema.game.client.view.meshlod.LODMeshSystem;
import org.schema.game.client.view.meshlod.LODMeshSystem.LODStage;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.mines.Mine.MineType;
import org.schema.schine.graphicsengine.core.Controller;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class MineDrawerSystem extends LODDrawerSystem<MineDrawableData>{
	
	public final Int2ObjectOpenHashMap<MineDrawableData> mines = new Int2ObjectOpenHashMap<MineDrawableData> ();
	private final boolean activeMines;
	
	public MineDrawerSystem(final MineType type, final boolean active) {
		super();
		
		this.activeMines = active;
		
		LODMeshSystem<MineDrawableData> m = new LODMeshSystem<MineDrawableData>();
		if(active) {
			m.init(new LODStage(20, 0, false, type.name0_active, 0),
					new LODStage(20, 20, false, type.name1_active, 0),
					new LODStage(20, 20, false, type.name2_active, 0),
					new LODStage(100, 50, true, type.sprite, type.subSpriteIndexActive)
					);
		}else {
			m.init(new LODStage(20, 0, false, type.name0, 0),
					new LODStage(20, 20, false, type.name1, 0),
					new LODStage(20, 20, false, type.name2, 0),
					new LODStage(100, 50, true, type.sprite, type.subSpriteIndex)
					);
		}
		create(m);
		
	}

	@Override
	public Collection<MineDrawableData> getEntries() {
		return mines.values();
	}

	public void addEntry(Mine m) {
		MineDrawableData d = new MineDrawableData(m);
		this.camPos.set(Controller.getCamera().getPos());
		d.updateDistance(camPos);
		mines.put(m.getId(), d);
	}
	public boolean removeEntry(Mine m) {
		MineDrawableData removed = mines.remove(m.getId());
		onRemoved(removed);
		return removed != null;
	}

	public boolean isActiveMines() {
		return activeMines;
	}

	


	

}
