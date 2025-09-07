package org.schema.game.common.data.player.catalog;

import org.schema.game.common.controller.elements.EntityIndexScore;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintType;

public class CatalogRawEntry {
	public final String name;
	public final long price;
	public final BlueprintType entityType;
	public final float mass;
	public final EntityIndexScore score;
	BlueprintClassification classification;

	public CatalogRawEntry(String name, long price, EntityIndexScore score, BlueprintType type, 
			BlueprintClassification classification,
			float mass) {
		super();
		this.name = name;
		this.price = price;
		this.classification = classification;
		this.entityType = type;
		this.mass = mass;
		this.score = score;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return name.equals(((CatalogRawEntry) obj).name);
	}

}
