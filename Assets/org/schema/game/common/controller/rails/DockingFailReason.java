package org.schema.game.common.controller.rails;

import org.schema.game.common.controller.SegmentController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

public class DockingFailReason {

	public String reason;
	
	public void popupClient(SegmentController from){
		if(!from.isOnServer() && reason != null){
			from.popupOwnClientMessage(Lng.str("Cannot dock here!\nReason: %s", reason), ServerMessage.MESSAGE_TYPE_ERROR);
		}
	}
}
