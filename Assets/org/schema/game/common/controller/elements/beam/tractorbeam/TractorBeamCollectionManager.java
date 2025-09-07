package org.schema.game.common.controller.elements.beam.tractorbeam;

import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.Salvager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.beam.BeamCollectionManager;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorBeamHandler.TractorMode;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class TractorBeamCollectionManager extends BeamCollectionManager<TractorUnit, TractorBeamCollectionManager, TractorElementManager> {

	private final TractorBeamHandler handler;
	private TractorMode mode = TractorMode.HOLD;
	public TractorBeamCollectionManager(SegmentPiece element,
	                                    SegmentController segController, TractorElementManager em) {

		super(element, ElementKeyMap.TRACTOR_BEAM, segController, em);

		this.handler = new TractorBeamHandler((Salvager) segController, this);
	}

	/**
	 * @return the handler
	 */
	@Override
	public TractorBeamHandler getHandler() {
		return handler;
	}
	
	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		final TractorMode oMode = this.mode;
		mode = ((TractorBeamMetaDataDummy) dummy).mode;
		if(mode != oMode) {
			sendModeUpdate();
		}
	}

	@Override
	protected Class<TractorUnit> getType() {
		return TractorUnit.class;
	}

	@Override
	public TractorUnit getInstance() {
		return new TractorUnit();
	}
	
	@Override
	public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
		
	}
	public void setTractorModeFromReceived(TractorMode tractorMode) {
		this.mode = tractorMode;
		if(isOnServer()) {
			//deligate if received by server
			sendModeUpdate();
		}
	}
	
	@Override
	public void handleControlShot(ControllerStateInterface unit, Timer timer) {
		if(unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE)) {
			super.handleControlShot(unit, timer);
		}
	}
	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		super.handleKeyEvent(unit, mapping, timer);
		if(!isOnServer()) {
			if(mapping == KeyboardMappings.SWITCH_FIRE_MODE) {
				final TractorMode oldMode = this.mode;
				int ord = mode.ordinal();
				if(KeyboardMappings.isControlDown()) {
					ord--;
					if(ord < 0) {
						ord = TractorMode.values().length-1;
					}
				}else {
					ord++;
				}
				mode = TractorMode.values()[ord%TractorMode.values().length];
				System.err.println("[CLIENT][TRACTORBEAM] MODE UPDATE:: "+oldMode.getName()+" -> "+mode.getName());
				sendModeUpdate();
			}
			
		}
	}
	public void sendModeUpdate() {
		TractorModeValueUpdate v = new TractorModeValueUpdate();
		v.setServer(
		((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), 
			getUsableId());
		v.val = (byte)mode.ordinal();
		assert(v.getType() == ValTypes.TRACTOR_MODE);
		((NTValueUpdateInterface) getSegmentController().getNetworkObject())
		.getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}
	@Override
	public String getModuleName() {
		return Lng.str("Tractor Beam System");
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Fire tractor beam"), hos, ContextFilter.IMPORTANT);
		h.addHelper(KeyboardMappings.SWITCH_FIRE_MODE, Lng.str("Change tractor mode (Current: %s)",mode.getName()), hos, ContextFilter.CRUCIAL);
		
	}

	public TractorMode getTractorMode() {
		return mode;
	}
	private final ConnectedLogicCon cTmp = new ConnectedLogicCon();
	public void calculateTractorModeFromLogic() {
		if(isOnServer()) {
			TractorMode old = this.mode;
			ConnectedLogicCon con = getActiveConnectedLogic(cTmp);
			
			if(con.connected > 0) {
				if(con.active == 0) {
					mode = TractorMode.HOLD;
				}else if(con.active == 1) {
					mode = TractorMode.PUSH;
				}else if(con.active >= 2) {
					mode = TractorMode.PULL;
				}
			}
			
			if(old != this.mode) {
				sendModeUpdate();
			}
		}
	}

	

	
}
