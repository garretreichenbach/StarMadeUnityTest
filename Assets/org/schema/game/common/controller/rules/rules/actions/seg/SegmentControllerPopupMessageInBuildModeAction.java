package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.actions.ActionUpdate;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class SegmentControllerPopupMessageInBuildModeAction extends SegmentControllerAction{
	
	
	@RuleValue(tag = "Message")
	public String message = "";
	@RuleValue(tag = "MsgType", intMap = {0,1,2,3,4}, int2StringMap = {"Simple", "Info", "Warning", "Error", "Dialog"})
	public int msgType;
	

	
	public SegmentControllerPopupMessageInBuildModeAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_POPUP_MESSAGE_IN_BUILD_MODE;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("Pops up lasting Message in build mode");
	}
	public class PopupBuildModeActionUpdate implements ActionUpdate{
		public final SegmentController s;
		private boolean buildMode;

		public PopupBuildModeActionUpdate(SegmentController s) {
			super();
			this.s = s;
		}

		@Override
		public void update(Timer timer) {
			assert(!s.isOnServer());
			if(s.isClientOwnObject() && ((GameClientState)s.getState()).isInAnyStructureBuildMode()) {
				if(!buildMode) {
					s.popupOwnClientMessage("Action"+message, message, msgType);
					buildMode = true;
				}
			}else {
				buildMode = false; 
			}
		}

		@Override
		public boolean onClient() {
			return true;
		}

		@Override
		public boolean onServer() {
			return false;
		}

		@Override
		public Action<?> getAction() {
			return SegmentControllerPopupMessageInBuildModeAction.this;
		}

		@Override
		public void onAdd() {
		}

		@Override
		public void onRemove() {
		}
		
	}
	@Override
	public void onTrigger(SegmentController s) {
		s.getRuleEntityManager().addUpdatableAction(new PopupBuildModeActionUpdate(s));
	}

	@Override
	public void onUntrigger(SegmentController s) {
		s.getRuleEntityManager().removeUpdatableAction(new PopupBuildModeActionUpdate(s));
	}

	
	
}
