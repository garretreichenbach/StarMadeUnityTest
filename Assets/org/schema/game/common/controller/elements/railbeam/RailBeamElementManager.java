package org.schema.game.common.controller.elements.railbeam;

import java.io.IOException;
import java.util.Locale;

import javax.vecmath.Vector3f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.weapon.WeaponRowElement;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.CannotImmediateRequestOnClientException;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.elements.BeamHandlerDeligator;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ManagerReloadInterface;
import org.schema.game.common.controller.elements.ManagerUpdatableInterface;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.beam.BeamCommand;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.BeamReloadCallback;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.ShipConfigurationNotFoundException;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

public class RailBeamElementManager extends UsableElementManager implements BeamReloadCallback, ManagerUpdatableInterface, BeamHandlerDeligator {

	@ConfigurationElement(name = "Distance")
	public static float DOCKING_BEAM_DISTANCE = 100;

	private final Long2ObjectOpenHashMap<RailDockerUsable> railDockers = new Long2ObjectOpenHashMap<RailDockerUsable>();

	private final SegmentPiece tmpPiece = new SegmentPiece();

	private Vector3f shootingDirTemp = new Vector3f();

	private RailBeamHandler railBeamManager;

	private Vector3i controlledFromOrig = new Vector3i();

	private Vector3i controlledFrom = new Vector3i();

	public RailBeamElementManager(SegmentController segmentController) {
		super(segmentController);
		this.railBeamManager = new RailBeamHandler(segmentController, this);
	}

	/**
	 * @return the dockingBeamManager
	 */
	public RailBeamHandler getRailBeamManager() {
		return railBeamManager;
	}

	protected boolean convertDeligateControls(ControllerStateInterface unit, Vector3i controlledFromOrig, Vector3i controlledFrom) throws IOException {
		unit.getParameter(controlledFromOrig);
		unit.getParameter(controlledFrom);
		if (unit.getPlayerState() == null) {
			return true;
		}
		SlotAssignment shipConfiguration = null;
		// autorequest true previously
		SegmentPiece fromPiece = getSegmentBuffer().getPointUnsave(controlledFrom, new SegmentPiece());
		if (fromPiece == null) {
			return false;
		}
		if (fromPiece.getType() == ElementKeyMap.CORE_ID) {
			try {
				shipConfiguration = checkShipConfig(unit);
				int currentlySelectedSlot = unit.getPlayerState().getCurrentShipControllerSlot();
				if (unit.getPlayerState() != null && !shipConfiguration.hasConfigForSlot(currentlySelectedSlot)) {
					return false;
				} else {
					controlledFrom.set(shipConfiguration.get(currentlySelectedSlot));
				}
			} catch (ShipConfigurationNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void update(Timer timer) {
		railBeamManager.update(timer);
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(ElementCollection firingUnit, ElementCollectionManager col, ControlBlockElementCollectionManager supportCol, ControlBlockElementCollectionManager effectCol) {
		return null;
	}

	@Override
	protected String getTag() {
		return "railbeam";
	}

	@Override
	public ElementCollectionManager getNewCollectionManager(SegmentPiece position, Class clazz) {
		throw new IllegalAccessError("This should not be called. ever");
	}

	@Override
	public String getManagerName() {
		return Lng.str("Rail Beam System Collective");
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		try {
			if (!unit.isFlightControllerActive() || !unit.isUnitInPlayerSector()) {
				return;
			}
			try {
				if (!convertDeligateControls(unit, controlledFromOrig, controlledFrom)) {
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			// autorequest true previously
			SegmentPiece fromPiece = getSegmentBuffer().getPointUnsave(controlledFrom, tmpPiece);
			if (fromPiece == null || fromPiece.getType() != ElementKeyMap.RAIL_BLOCK_DOCKER) {
				return;
			}
			if (getSegmentController().railController.isDocked()) {
				if (getSegmentController().isClientOwnObject() && timer.currentTime - getSegmentController().railController.isDockedSince() > 1000 && timer.currentTime - getSegmentController().railController.getLastDisconnect() > 1000) {
					if (!getSegmentController().isVirtualBlueprint()) {
						String lst = getSegmentController().railController.getOneBeforeRoot().lastDockerPlayerServerLowerCase;
						if (!(getSegmentController().railController.getRoot() instanceof ShopSpaceStation) || getAttachedPlayers().isEmpty() || lst.length() == 0 || ((AbstractOwnerState) getAttachedPlayers().get(0)).getName().toLowerCase(Locale.ENGLISH).equals(lst)) {
							System.err.println("[CLIENT][RAILBEAM] Disconnecting from tail");
							getSegmentController().railController.disconnectClient();
						} else {
							((GameClientController) getState().getController()).popupAlertTextMessage(Lng.str("Can't undock from shop!\nOnly %s can undock!", getSegmentController().lastDockerPlayerServerLowerCase), 0);
						}
					} else {
						((GameClientController) getState().getController()).popupAlertTextMessage(Lng.str("Can't undock a design!"), 0);
					}
				}
				return;
			}
			if (timer.currentTime - getSegmentController().railController.getLastDisconnect() < 1000) {
				// prevent instant redock
				return;
			}
			unit.getForward(shootingDirTemp);
			shootingDirTemp.scale(DOCKING_BEAM_DISTANCE);
			Vector3f from = new Vector3f();
			getSegmentController().getAbsoluteElementWorldPosition(new Vector3i(controlledFrom.x - SegmentData.SEG_HALF, controlledFrom.y - SegmentData.SEG_HALF, controlledFrom.z - SegmentData.SEG_HALF), from);
			Vector3f to = new Vector3f();
			to.set(from);
			to.add(shootingDirTemp);
			float coolDown = 0;
			float burstTime = -1;
			float initialTicks = 1;
			float powerConsumed = 0;
			Vector3f relativePos = new Vector3f(controlledFrom.x - SegmentData.SEG_HALF, controlledFrom.y - SegmentData.SEG_HALF, controlledFrom.z - SegmentData.SEG_HALF);
			BeamCommand b = new BeamCommand();
			b.currentTime = timer.currentTime;
			b.identifier = ElementCollection.getIndex(controlledFrom);
			b.relativePos.set(relativePos);
			b.reloadCallback = this;
			b.from.set(from);
			b.to.set(to);
			b.playerState = unit.getPlayerState();
			b.beamTimeout = 0.1f;
			b.tickRate = 0.1f;
			b.beamPower = 0;
			b.controllerPos = controlledFrom;
			b.cooldownSec = coolDown;
			b.bursttime = burstTime;
			b.initialTicks = initialTicks;
			b.powerConsumedByTick = powerConsumed;
			b.powerConsumedExtraByTick = 0;
			b.weaponId = b.identifier;
			b.latchOn = false;
			b.checkLatchConnection = false;
			b.firendlyFire = true;
			b.penetrating = false;
			b.acidDamagePercent = 0;
			b.minEffectiveRange = 1;
			b.minEffectiveValue = 1;
			b.maxEffectiveRange = 1;
			b.maxEffectiveValue = 1;
			railBeamManager.addBeam(b);
			/*AudioController.fireAudioEvent("BEAM_FIRE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.BEAM, AudioTags.RAIL, AudioTags.FIRE }, AudioParam.START, AudioController.ent(getSegmentController(), fromPiece, b.identifier, 10))*/
			AudioController.fireAudioEventID(914, AudioController.ent(getSegmentController(), fromPiece, b.identifier, 10));
			getManagerContainer().onAction();
		} catch (CannotImmediateRequestOnClientException e) {
			System.err.println("[CLIENT][WARNING] " + getSegmentController() + " Cannot DOCK! segment not yet in buffer " + e.getSegIndex() + ". -> requested");
		}
	}

	@Override
	public void setShotReloading(long reload) {
	}

	@Override
	public boolean canUse(long curTime, boolean popupText) {
		return true;
	}

	@Override
	public boolean isInitializing(long curTime) {
		return false;
	}

	@Override
	public long getNextShoot() {
		return 0;
	}

	@Override
	public long getCurrentReloadTime() {
		return 0;
	}

	/**
	 * @return the railDockers
	 */
	public LongSet getRailDockers() {
		return railDockers.keySet();
	}

	public void addRailDocker(long index4) {
		long index = ElementCollection.getPosIndexFrom4(index4);
		RailDockerUsableUndocked railDockerUsable = new RailDockerUsableUndocked(index);
		railDockers.put(index, railDockerUsable);
		getManagerContainer().addPlayerUsable(railDockerUsable);
	}

	public void removeRailDockers(long index4) {
		long index = ElementCollection.getPosIndexFrom4(index4);
		RailDockerUsable remove = railDockers.remove(index);
		if (remove != null) {
			getManagerContainer().removePlayerUsable(remove);
		}
	}

	public class RailDockerUsableUndocked extends RailDockerUsable {

		public RailDockerUsableUndocked(long index) {
			super(index);
		}
	}

	public class RailDockerUsable implements PlayerUsableInterface {

		public final long index;

		public RailDockerUsable(long index) {
			super();
			this.index = index;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (index ^ (index >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof RailDockerUsable && index == ((RailDockerUsable) obj).index;
		}

		public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
		}

		private RailBeamElementManager getOuterType() {
			return RailBeamElementManager.this;
		}

		@Override
		public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState, PlayerControllable newAttached) {
		}

		public void onSwitched(boolean on) {
		}

		@Override
		public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
		}

		@Override
		public boolean isAddToPlayerUsable() {
			return true;
		}

		@Override
		public WeaponRowElementInterface getWeaponRow() {
			SegmentPiece railDockerPiece = getSegmentController().getSegmentBuffer().getPointUnsave(index);
			if (railDockerPiece != null) {
				WeaponRowElementInterface rowRailDocker = new WeaponRowElement(railDockerPiece);
				return rowRailDocker;
			}
			return null;
		}

		@Override
		public boolean isControllerConnectedTo(long index, short type) {
			return type == ElementKeyMap.CORE_ID;
		}

		public float getWeaponSpeed() {
			return 0;
		}

		public float getWeaponDistance() {
			return 0;
		}

		@Override
		public boolean isPlayerUsable() {
			return !getSegmentController().railController.isDockedAndExecuted();
		}

		@Override
		public long getUsableId() {
			return index;
		}

		@Override
		public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
			if (unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) && canUse(timer.currentTime, true)) {
				handle(unit, timer);
			}
		}

		@Override
		public ManagerReloadInterface getReloadInterface() {
			return null;
		}

		@Override
		public ManagerActivityInterface getActivityInterface() {
			return null;
		}

		@Override
		public String getName() {
			return Lng.str("Rail Docker");
		}

		@Override
		public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		}

		@Override
		public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
			h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Dock to target Rail"), hos, ContextFilter.IMPORTANT);
		}
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void onNoUpdate(Timer timer) {
	}

	@Override
	public void onElementCollectionsChanged() {
	}

	@Override
	public void flagCheckUpdatable() {
		setUpdatable(false);
	}

	@Override
	public BeamHandlerContainer<?> getBeamHandler() {
		return railBeamManager;
	}

	@Override
	public void flagBeamFiredWithoutTimeout() {
	}
}
