package org.schema.game.client.data;

import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.element.ControlElementMapOptimizer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.tech.Technology;
import org.w3c.dom.Document;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public interface GameStateInterface {
	public SendableGameState getGameState();

	public boolean isPhysicalAsteroids();

	public Document getBlockBehaviorConfig();

	public float getSectorSize();

	public Short2ObjectOpenHashMap<Technology> getAllTechs();

	public boolean getMaterialPrice();

	public int getSegmentPieceQueueSize();

	public ControlElementMapOptimizer getControlOptimizer();

	public ChannelRouter getChannelRouter();

	public Long2ObjectOpenHashMap<PlayerState> getPlayerStatesByDbId() ;

	
}
