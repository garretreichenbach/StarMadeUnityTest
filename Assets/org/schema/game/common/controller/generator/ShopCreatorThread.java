package org.schema.game.common.controller.generator;

import api.listener.events.controller.shop.ShopGenerateEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.WorldCreatorFactory;
import org.schema.game.server.controller.world.factory.WorldCreatorShopFactory;
import org.schema.game.server.controller.world.factory.WorldCreatorShopMannedFactory;

import java.util.Random;

public class ShopCreatorThread extends CreatorThread {

	private WorldCreatorFactory creator;

	public ShopCreatorThread(ShopSpaceStation shopSpaceStation) {
		super(shopSpaceStation);
		Random r = new Random(shopSpaceStation.getSeed());
		if (shopSpaceStation.getSeed() == 0 || r.nextInt(32) == 0) {
			this.creator = new WorldCreatorShopMannedFactory(shopSpaceStation.getSeed());
		} else {
			this.creator = new WorldCreatorShopFactory(shopSpaceStation.getSeed());
		}
		//INSERTED CODE
		ShopGenerateEvent event = new ShopGenerateEvent(shopSpaceStation, this, creator);
		StarLoader.fireEvent(event, shopSpaceStation.isOnServer());
		this.creator = event.getFactory();
		///

	}

	@Override
	public int isConcurrent() {
		return FULL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		return -1;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {

		creator.createWorld(getSegmentController(), ws, requestData);
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
				return false;
	}

}
