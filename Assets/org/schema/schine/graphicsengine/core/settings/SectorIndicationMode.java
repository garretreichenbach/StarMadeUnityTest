package org.schema.schine.graphicsengine.core.settings;

public enum SectorIndicationMode {
	INDICATION_ONLY,
	INDICATION_AND_ARROW,
	OFF;

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return name();
	}

}
