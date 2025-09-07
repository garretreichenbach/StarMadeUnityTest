package org.schema.game.server.data;

import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.elements.EntityIndexScore;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.ControlElementMapper;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.resource.tag.Tag;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public interface BlueprintInterface {

	ControlElementMapper getControllingMap();

	ElementCountMap getElementMap();

	ElementCountMap getElementCountMapWithChilds();

	String getName();

	long getPrice();

	EntityIndexScore getScore();

	BlueprintType getType();

	Tag getAiTag();
	
	public boolean isChunk16();

	double getCapacitySingle();

	public BoundingBox getBb();

	public boolean isOldPowerFlag();

	public Long2ObjectOpenHashMap<VoidSegmentPiece> getDockerPoints();
}
