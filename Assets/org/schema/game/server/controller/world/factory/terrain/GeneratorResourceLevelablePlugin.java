package org.schema.game.server.controller.world.factory.terrain;

@Deprecated
public class GeneratorResourceLevelablePlugin extends GeneratorResourcePlugin {

	public GeneratorResourceLevelablePlugin(int count, short type,
	                                        short convertableBlock) {
		super(count, type, convertableBlock);
	}
	//
	//	public GeneratorResourceLevelablePlugin(short type, int count,
	//			short convertableBlock) {
	//		super(type, count, convertableBlock);
	//		assert(ElementKeyMap.getInfo(type).isLeveled());
	//	}

	//	@Override
	//	public void setBlock(SegmentData par1World, int l1, int i2, int j2, int blockNumber, Random random){
	//		int level = 1;
	//		float lim = 0.1f;
	//		while(level < 5 && random.nextFloat() < lim){
	//			level++;
	//			lim *= 0.9f; //this makes higher levels even harder to spawn
	//		}
	//
	//		short type = minableBlockId;//ElementKeyMap.getLevel(minableBlockId, level);
	//
	//		par1World.setInfoElementForcedAddUnsynched((byte)Math.abs(l1%16), (byte)Math.abs(i2%16), (byte)Math.abs(j2%16), type, false);
	//	}

}
