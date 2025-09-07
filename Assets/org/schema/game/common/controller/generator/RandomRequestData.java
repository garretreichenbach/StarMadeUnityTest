package org.schema.game.common.controller.generator;

import java.util.Random;

import org.schema.game.server.controller.RequestData;

public class RandomRequestData extends RequestData {
	public final Random random = new Random();
}
