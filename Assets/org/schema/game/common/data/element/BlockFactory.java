package org.schema.game.common.data.element;

public class BlockFactory {

	public short enhancer;
	public FactoryResource[][] input;
	public FactoryResource[][] output;

	@Override
	public String toString() {
		return input != null ? "Block Factory Products: " + input.length : "INPUT";
	}
}
