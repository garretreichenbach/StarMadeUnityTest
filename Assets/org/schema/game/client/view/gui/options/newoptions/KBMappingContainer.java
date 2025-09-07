package org.schema.game.client.view.gui.options.newoptions;

import org.schema.schine.input.KeyboardMappings;

public class KBMappingContainer {
	public final KeyboardMappings m;

	public KBMappingContainer(KeyboardMappings m) {
		this.m = m;
	}

	

	@Override
	public boolean equals(Object obj) {
		//equals on instance
		return this == obj;
	}

	
}
