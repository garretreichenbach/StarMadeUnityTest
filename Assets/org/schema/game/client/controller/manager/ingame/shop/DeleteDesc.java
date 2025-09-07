package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.common.data.element.ElementInformation;

public class DeleteDesc {
	public int wantedQuantity;
	private ElementInformation info;

	public DeleteDesc(ElementInformation info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "How many " + info.getName() + " do you want to trash" + "?\n" +
				"If you enter too many, the maximal amount you can delete\n" +
				"will be automatically displayed.\n";
	}
}