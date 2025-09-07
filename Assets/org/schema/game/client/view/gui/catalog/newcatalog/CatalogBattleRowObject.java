package org.schema.game.client.view.gui.catalog.newcatalog;

public class CatalogBattleRowObject {
	public String catId;
	public int amount;
	int faction;

	public CatalogBattleRowObject(String catId, int faction, int amount) {
		super();
		this.catId = catId;
		this.amount = amount;
		this.faction = faction;
	}

	@Override
	public int hashCode() {
		return catId.hashCode();
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof CatalogBattleRowObject && catId.equals(((CatalogBattleRowObject) o).catId);
	}
}
