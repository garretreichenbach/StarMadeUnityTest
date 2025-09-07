package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;

public enum GameRequestAnswerFactories implements GameRequestAnswerFactory{
	

	
	
	
	REQUEST_BLOCK_PROPERTIES((byte)1, BlockPropertiesRequest::new, BlockPropertiesAnswer::new, true),
	KILL_OWN_CHARACTER((byte)2, KillCharacterRequest::new, () -> {
		throw new RuntimeException("request has no answer");
	}, false),
	GAME_MODE((byte)3, GameModeRequest::new, GameModeAnswer::new, true),
	ENTITY((byte)4, EntityRequest::new, () -> {
		throw new RuntimeException("request has no answer");
	}, false),
	SERVER_STATS((byte)5, ServerStatsRequest::new, ServerStatsAnswer::new, true),
	PLAYER_STATS((byte)6, PlayerStatsRequest::new, PlayerStatsAnswer::new, true),
	ENTITY_INVENTORIES((byte)7, EntityInventoriesRequest::new, () -> {
		throw new RuntimeException("request has no answer");
	}, false),
	FACTION_CONFIG((byte)8, FactionRequest::new, FactionAnswer::new, true),
	BLOCK_BEHAVIOR((byte)9, BlockBehaviorRequest::new, BlockBehaviorAnswer::new, true),
	BLOCK_CONFIG((byte)10, BlockConfigRequest::new, BlockConfigAnswer::new, true),
	BLOCK_PROPERTIES((byte)11, BlockPropertiesRequest::new, BlockPropertiesAnswer::new, true),
	CUSTOM_BLOCK_TEXTURE((byte)12, ServerStatsRequest::new, ServerStatsAnswer::new, true),
	
	
	
	
	
	
	;
	
	
	static {
		initialize();
	}

	public static void initialize(){
		for(GameRequestAnswerFactories c : GameRequestAnswerFactories.values()) {
			assert(!GameRequestAnswerFactory.factories.containsKey(c.id)):"Already contains key "+c+"; existing: "+GameRequestAnswerFactory.factories.get(c.id);
			GameRequestAnswerFactory.factories.put(c.id, c);
		}
	}

	public String toString() {
		return name()+"("+id+")";
	}
	public static interface ReqFac{
		public GameRequestInterface getRequestInstance(); 
	}
	public static interface AnsFac{
		public GameAnswerInterface getAnswerInstance(); 
	}
	private final byte id;
	private final ReqFac rFac;
	private final AnsFac aFac;
	private final boolean blocking;
	
	private GameRequestAnswerFactories(byte id, ReqFac rFac, AnsFac aFac, boolean blocking) {
		this.id = id;
		this.rFac = rFac;
		this.aFac = aFac;
		this.blocking = blocking;
	}
	
	@Override
	public byte getGameRequestid() {
		return id;
	}

	@Override
	public GameRequestInterface getRequestInstance() {
		GameRequestInterface c = rFac.getRequestInstance();
		assert(c.getFactory() == this):c.getFactory()+"; "+this;
		return c;
	}

	@Override
	public GameAnswerInterface getAnswerInstance() {
		GameAnswerInterface c = aFac.getAnswerInstance();
		assert(c.getFactory() == this):c.getFactory()+"; "+this;
		return c;
	}

	@Override
	public boolean isBlocking() {
		return blocking;
	}

}
