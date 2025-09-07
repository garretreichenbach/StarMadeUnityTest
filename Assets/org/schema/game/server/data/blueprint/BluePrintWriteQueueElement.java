package org.schema.game.server.data.blueprint;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;

public class BluePrintWriteQueueElement {
	public final SegmentController segmentController;
	public final boolean local;
	public String name;
	public boolean requestedAdditionalBlueprintData;
	public final BlueprintClassification classification;

	public BluePrintWriteQueueElement(SegmentController ship, String name, BlueprintClassification classification, boolean local) {
		super();
		this.segmentController = ship;
		this.name = name;
		this.local = local;
		this.classification = classification;
	}

}
