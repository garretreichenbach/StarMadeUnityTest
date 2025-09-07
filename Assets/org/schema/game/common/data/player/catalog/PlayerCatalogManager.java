package org.schema.game.common.data.player.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.schema.game.client.controller.CatalogChangeListener;
import org.schema.game.client.controller.FactionChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.CatalogState;
import org.schema.game.server.data.FactionState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlayerCatalogManager implements CatalogChangeListener, FactionChangeListener {

	private final PlayerState player;
	private final List<CatalogPermission> availableCatalog = new ArrayList<CatalogPermission>();
	private final List<CatalogPermission> personalCatalog = new ArrayList<CatalogPermission>();
	private final List<CatalogPermission> allCatalog = new ArrayList<CatalogPermission>();
	private boolean flagCatalogUpdated = true;
	private CatalogManager catalogManager;
	private boolean addedFactionObserver;
	public final List<CatalogChangeListener> listeners = new ObjectArrayList<CatalogChangeListener>();

	public PlayerCatalogManager(PlayerState player) {
		this.player = player;

		//add observer so the list is updated accoring to faction permissions

	}

	public void cleanUp() {
		if (catalogManager != null) {
			catalogManager.listeners.remove(this);
		}
	}

	/**
	 * @return the allCatalog
	 */
	public List<CatalogPermission> getAllCatalog() {
		return allCatalog;
	}

	//	private void cleanOlder(CatalogEntry c){
	//		for(int i = 0; i < catalog.size(); i++){
	//			if(catalog.get(i).timestamp < c.timestamp){
	//				catalog.remove(i);
	//				i--;
	//			}
	//		}
	//	}
	public List<CatalogPermission> getAvailableCatalog() {
		return availableCatalog;
	}

	/**
	 * @return the personalCatalog
	 */
	public List<CatalogPermission> getPersonalCatalog() {
		return personalCatalog;
	}

	private void reorganizeCatalog() {

		long time = System.currentTimeMillis();

		Collection<CatalogPermission> cat = catalogManager.getCatalog();

		availableCatalog.clear();
		personalCatalog.clear();
		allCatalog.clear();

		for (CatalogPermission perm : cat) {

			assert(perm != null);
			boolean addToList = false;
			boolean addToPersonalList = false;
			boolean addToAllList = true;
			if (perm.ownerUID.equals(player.getName())) {
				addToList = true;
				addToPersonalList = true;
			} else if (perm.others()) {
				addToList = true;
			} else if (perm.faction()) {
				FactionManager m = ((FactionState) player.getState()).getFactionManager();
				Faction faction = m.getFaction(player.getFactionId());
				if (faction != null && faction.getMembersUID().keySet().contains(perm.ownerUID)) {
					addToList = true;
				}
			}
			if (addToList) {
				availableCatalog.add(perm);
			}
			if (addToPersonalList) {
				personalCatalog.add(perm);
			}

			if (addToAllList) {
				allCatalog.add(perm);
			}

		}
		long updateTime = System.currentTimeMillis() - time;
		if (player.isClientOwnPlayer()) {
			((GameClientState) player.getState()).notifyOfCatalogChange();
		}

		for(CatalogChangeListener c : listeners) {
			c.onCatalogChanged();
		}

		long totalTime = System.currentTimeMillis() - time;

		if (totalTime > 5) {
			System.err.println(player.getState() + " Updating catalog for " + this.player + " took " + totalTime + " ms; update: " + updateTime);
		}
	}

	public void update() {
		if (!addedFactionObserver && ((FactionState) player.getState()).getFactionManager() != null) {
			((FactionState) player.getState()).getFactionManager().listeners.add(this);
			addedFactionObserver = true;
		}
		if (catalogManager == null && ((CatalogState) player.getState()).getCatalogManager() != null) {
			catalogManager = ((CatalogState) player.getState()).getCatalogManager();
			catalogManager.listeners.add(this);
		}
		if (catalogManager == null) {
			return;
		}

		if (flagCatalogUpdated) {
			reorganizeCatalog();
			flagCatalogUpdated = false;
		}

	}


	public void onCatalogChanged() {
		flagCatalogUpdated = true;		
	}

	@Override
	public void onFactionChanged() {
		onCatalogChanged();		
	}

	@Override
	public void onRelationShipOfferChanged() {
		onCatalogChanged();		
	}

	@Override
	public void onFactionNewsDeleted() {
	}

	@Override
	public void onInvitationsChanged() {
	}

}
