package org.schema.game.client.view.gui.catalog.newcatalog;

import org.schema.game.common.data.element.ElementInformation;

public class TypeRowConsistanceItem {
	public final ElementInformation info;
	private int amount;

	public TypeRowConsistanceItem(ElementInformation info, int amount) {
		super();
		this.info = info;
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public int hashCode() {
		return info.hashCode();
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof TypeRowConsistanceItem && info.equals(((TypeRowConsistanceItem) o).info);
	}
}
