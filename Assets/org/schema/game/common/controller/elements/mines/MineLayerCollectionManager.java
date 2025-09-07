package org.schema.game.common.controller.elements.mines;


import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.shorts.Short2IntArrayMap;

public class MineLayerCollectionManager extends ControlBlockElementCollectionManager<MineLayerUnit, MineLayerCollectionManager, MineLayerElementManager> implements PowerConsumer, PlayerUsableInterface{

	private float powered;
	private Short2IntArrayMap consTmp = new Short2IntArrayMap(); 
	public MineLayerCollectionManager(SegmentPiece element,
	                                 SegmentController segController, MineLayerElementManager em) {
		super(element, ElementKeyMap.MINE_CORE, segController, em);

		assert (element != null);
	}
	private float timeHeldButton;
	private int lastClientArm;
	@Override
	public CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.SEPERATED;
	}
	protected void onNotShootingButtonDown(ControllerStateInterface unit, Timer timer) {
		super.onNotShootingButtonDown(unit, timer);
		if(unit.isDown(KeyboardMappings.SHIP_ZOOM)  && unit.isFlightControllerActive()) {
			if(!getSegmentController().isOnServer()) {
				if(timer.currentTime > lastClientArm+3000) {
					timeHeldButton += timer.getDelta();
					float tth = EngineSettings.SECONDARY_MOUSE_CLICK_MINE_TIMER.getFloat();
					if(timeHeldButton > tth) {
						Vector3i sector = getSegmentController().getSector(new Vector3i());
						getSegmentController().popupOwnClientMessage("mineArmMsg", Lng.str("Arming all mines in sector %s!",sector), ServerMessage.MESSAGE_TYPE_INFO);
						((GameClientState)getSegmentController().getState()).getController().getMineController()
							.requestArmedClient(unit.getPlayerState().getId(), sector);
						timeHeldButton = 0;
					}else {
						float d = tth - timeHeldButton;
						getSegmentController().popupOwnClientMessage("mineArmMsg", Lng.str("Arming sector mines in %s secs", StringTools.formatPointZero(d)), ServerMessage.MESSAGE_TYPE_INFO);
					}
				}
			}
		}else {
			timeHeldButton = 0;
		}
	}
	@Override
	protected Class<MineLayerUnit> getType() {
		return MineLayerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public MineLayerUnit getInstance() {
		return new MineLayerUnit();
	}


	@Override
	public String getModuleName() {
		return Lng.str("Mine Layer System");
	}




	@Override
	protected void onFinishedCollection() {
		super.onFinishedCollection();
		
		
	}
	private double getReactorPowerUsage(){
		double pw = MineLayerElementManager.REACTOR_POWER_CONST_NEEDED_PER_BLOCK * getTotalSize();
		pw = Math.pow(pw, MineLayerElementManager.REACTOR_POWER_POW);
		return pw;
	}
	
	@Override
	public double getPowerConsumedPerSecondResting() {
		return getReactorPowerUsage();
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getReactorPowerUsage();
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return true;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}
	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
		if(isOnServer()) {
			//no need to do it on client since it will be executed after the server already laid the mine
			//which causes a fals error message on not being able to place another mine in the same space
			handleKeyPress(getElementManager().getManagerContainer().unitSingle, timer);
		}
	}
	@Override
	public float getPowered() {
		return this.powered;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.CANNONS;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}
	@Override
	public void dischargeFully() {
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Lay Mine"), hos, ContextFilter.IMPORTANT);
		h.addHelper(KeyboardMappings.SHIP_ZOOM, Lng.str("Hold to arm mines"), hos, ContextFilter.IMPORTANT);
	}
}
