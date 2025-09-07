package org.schema.schine.network.commands.gamerequests;

import java.io.IOException;

import org.schema.game.network.commands.gamerequests.GameRequestAnswerFactories;
import org.schema.schine.network.commands.GameRequestAnswerCommandPackage;
import org.schema.schine.network.common.NetworkProcessor;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

public interface GameRequestAnswerFactory {
	public static Byte2ObjectOpenHashMap<GameRequestAnswerFactory> factories = new Byte2ObjectOpenHashMap<GameRequestAnswerFactory>();
	
	public byte getGameRequestid();
	public GameRequestInterface getRequestInstance();
	public GameAnswerInterface getAnswerInstance();
	
	
	public static void send(GameAnswerInterface a, NetworkProcessor p) throws IOException {
		GameRequestAnswerCommandPackage pack = new GameRequestAnswerCommandPackage();
		pack.answer = a;
		pack.send(p);
	}
	public boolean isBlocking();

	public static void initAll(){
		GameRequestAnswerFactories.initialize();
		BasicRequestAnswerFactories.initialize();
	}
}
