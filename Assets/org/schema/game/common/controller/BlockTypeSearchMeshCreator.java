package org.schema.game.common.controller;

import org.schema.game.client.view.ElementCollectionDrawer;
import org.schema.game.common.controller.BlockTypeSearchRunnable.BlockTypeSearchCallback;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementCollectionMesh;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.longs.LongList;

public class BlockTypeSearchMeshCreator implements BlockTypeSearchCallback{
	public final SegmentController c;
	public ElementCollectionMesh m;
	private int amountFound;

	public BlockTypeSearchMeshCreator(SegmentController c) {
		this.c = c;
	}

	@Override
	public void handleThreaded(LongList result) {
		this.amountFound = result.size();
		if(this.amountFound > 0){
			m = ElementCollection.getMeshInstance();
			m.calculate(null, 0L, result);
			m.initializeMesh();
			m.setColor(1, 0.5f, 0.5f, 0.9f);
		}
	}

	@Override
	public void executeAfterDone() {
		if(this.amountFound > 0){
			c.popupOwnClientMessage(Lng.str("Found %s blocks of this type", this.amountFound), ServerMessage.MESSAGE_TYPE_INFO);
			if(ElementCollectionDrawer.searchForTypeResult != null){
				ElementCollectionDrawer.searchForTypeResult.cleanUp();
			}
			ElementCollectionDrawer.searchForTypeResult = this;
		}else{
			c.popupOwnClientMessage(Lng.str("Could not find any blocks of this type"), ServerMessage.MESSAGE_TYPE_WARNING);
			ElementCollectionDrawer.searchForTypeResult = null;
		}
	}

	public void cleanUp() {
		if(m != null){
			m.clear();
			m.destroyBuffer();
		}
	}
}
