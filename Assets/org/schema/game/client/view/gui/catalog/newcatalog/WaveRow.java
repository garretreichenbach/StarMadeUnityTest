package org.schema.game.client.view.gui.catalog.newcatalog;

import java.util.List;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.catalog.CatalogWavePermission;
import org.schema.game.server.data.CatalogState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WaveRow {

	public List<CatalogPermission> permissions = new ObjectArrayList<CatalogPermission>();
	public int factionId;
	public short difficulty;
	public int amount;
	private static boolean check = false;
	
	
	
	
	public static void get(CatalogState state, List<WaveRow> out){
		CatalogManager catalogManager = state.getCatalogManager();
		
		check = true;
		for(CatalogPermission c : catalogManager.getCatalog()){
			for(CatalogWavePermission wp : c.wavePermissions){
				
				WaveRow r = new WaveRow();
				r.factionId = wp.factionId;
				r.difficulty = wp.difficulty;
				
				int indexOf = out.indexOf(r);
				
				if(indexOf >= 0){
					r = out.get(indexOf);
				}else{
					out.add(r);
				}
				r.permissions.add(c);
				r.amount += wp.amount;
			}
		}
		check = false;
	}

	public void refresh(GameClientState state) {
		CatalogManager catalogManager = state.getCatalogManager();
		permissions.clear();
		amount = 0;
		for(CatalogPermission c : catalogManager.getCatalog()){
			for(CatalogWavePermission wp : c.wavePermissions){
				if(wp.difficulty == difficulty && wp.factionId == factionId){
					permissions.add(c);
					amount += wp.amount;
				}
			}
		}
	}


	@Override
	public int hashCode() {
		return factionId * difficulty;
	}




	@Override
	public boolean equals(Object obj) {
		return factionId == ((WaveRow)obj).factionId && difficulty == ((WaveRow)obj).difficulty && check;
	}




	
	
	
	
}
