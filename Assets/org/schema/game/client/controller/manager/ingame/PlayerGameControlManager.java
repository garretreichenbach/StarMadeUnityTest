package org.schema.game.client.controller.manager.ingame;

import api.common.GameClient;
import api.utils.gui.GUIControlManager;
import api.utils.gui.ModGUIHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerMessageLogPlayerInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.AiConfigurationManager;
import org.schema.game.client.controller.manager.ingame.catalog.CatalogControlManager;
import org.schema.game.client.controller.manager.ingame.faction.FactionControlManager;
import org.schema.game.client.controller.manager.ingame.map.MapControllerManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.controller.manager.ingame.ship.*;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.controller.manager.ingame.structurecontrol.StructureControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.reactor.ReactorTreeDialog;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.rails.RailController;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class PlayerGameControlManager extends AbstractControlManager {

	private final ObjectArrayList<AbstractControlManager> panelElements = new ObjectArrayList<AbstractControlManager>();

	short lastUpdateInventory = -1;

	long lastUpdateTime = 0;

	private PlayerInteractionControlManager playerIntercationManager;

	private InventoryControllerManager inventoryControlManager;

	private ShopControllerManager shopControlManager;

	private WeaponAssignControllerManager weaponControlManager;

	private FleetControlManager fleetControlManager;

	private ContractsControlManager contractsControlManager;

	private NavigationControllerManager navigationControlManager;

	private MapControllerManager mapControlManager;

	private AiConfigurationManager aiConfigurationManager;

	private FactionControlManager factionControlManager;

	private CatalogControlManager catalogControlManager;

	private StructureControllerManager structureControlManager;

	private ThrustManager thrustManager;
	public MusicManager musicManager;

	public PlayerGameControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	public void activateThrustManager(Ship ship) {//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//
		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}

		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);

		boolean act = thrustManager.isActive();
		thrustManager.setSelectedShip(ship);
		thrustManager.setActive(!act);
		if(!thrustManager.isActive()) {
			for(int i = 0; i < getState().getController().getPlayerInputs().size(); i++) {
				DialogInterface p = getState().getController().getPlayerInputs().get(i);
				if(p instanceof PlayerMessageLogPlayerInput) {
					getState().getController().getPlayerInputs().get(i).deactivate();
					break;
				}
			}
		}
	}

	public void aiConfigurationAction(SegmentPiece piece) {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(getState().getShip() != null && getState().getShip().getAiConfiguration().getControllerBlock() != null) {
			aiConfigurationManager.setCanEdit(true);
			aiConfigurationManager.setAi(getState().getShip());
		} else if(piece != null && piece.getSegment().getSegmentController() instanceof AiInterface) {
			aiConfigurationManager.setCanEdit(true);
			aiConfigurationManager.setAi((AiInterface) piece.getSegment().getSegmentController());
		} else if(playerIntercationManager.getSelectedEntity() != null && playerIntercationManager.getSelectedEntity() instanceof AiInterface) {
			aiConfigurationManager.setCanEdit(getState().getPlayer().getNetworkObject().isAdminClient.get());
			aiConfigurationManager.setAi((AiInterface) playerIntercationManager.getSelectedEntity());
		} else {
			aiConfigurationManager.setCanEdit(false);
			aiConfigurationManager.setAi(null);
		}

		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);

		boolean activate = !aiConfigurationManager.isActive();
		aiConfigurationManager.setDelayedActive(activate);
	}

	public void deactivateAll() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		for(AbstractControlManager a : panelElements) {
			a.setActive(false);
		}
		playerIntercationManager.hinderInteraction(600);
	}

	/**
	 * @return the aiConfigurationManager
	 */
	public AiConfigurationManager getAiConfigurationManager() {
		return aiConfigurationManager;
	}

	/**
	 * @return the catalogControlManager
	 */
	public CatalogControlManager getCatalogControlManager() {
		return catalogControlManager;
	}

	/**
	 * @return the cubatomControlManager
	 */
	public StructureControllerManager getStructureControlManager() {
		return structureControlManager;
	}

	public FactionControlManager getFactionControlManager() {
		return factionControlManager;
	}

	/**
	 * @return the inventoryControlManager
	 */
	public InventoryControllerManager getInventoryControlManager() {
		return inventoryControlManager;
	}

	/**
	 * @return the mapControlManager
	 */
	public MapControllerManager getMapControlManager() {
		return mapControlManager;
	}

	/**
	 * @return the navigationControlManager
	 */
	public NavigationControllerManager getNavigationControlManager() {
		return navigationControlManager;
	}

	/**
	 * @return the playerIntercationManager
	 */
	public PlayerInteractionControlManager getPlayerIntercationManager() {
		return playerIntercationManager;
	}

	/**
	 * @return the shopControlManager
	 */
	public ShopControllerManager getShopControlManager() {
		return shopControlManager;
	}

	/**
	 * @return the weaponControlManager
	 */
	public WeaponAssignControllerManager getWeaponControlManager() {
		return weaponControlManager;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		/*
		 * update dialogs
		 */
		int size = getState().getController().getPlayerInputs().size();
		if(size > 0) {
			// only the last in list is active
			getState().getController().getPlayerInputs().get(size - 1).handleKeyEvent(e);
			return;
		}
		/*
		 * Update local player keys
		 */
		if(getState().getController().getPlayerInputs().isEmpty() && !getState().isDebugKeyDown()) {
			if(inventoryControlManager.isActive() && e.isTriggered(KeyboardMappings.ACTIVATE)) {
				inventoryAction(null);
			}
			if(e.isTriggered(KeyboardMappings.INVENTORY_PANEL)) {
				inventoryAction(null);
			} else if(e.isTriggered(KeyboardMappings.SHOP_PANEL)) {
				shopAction();
			} else if(e.isTriggered(KeyboardMappings.WEAPON_PANEL)) {
				weaponAction();
			} else if(e.isTriggered(KeyboardMappings.FLEET_PANEL)) {
				fleetAction();
			} else if(e.isTriggered(KeyboardMappings.NAVIGATION_PANEL)) {
				navigationAction();
			} else if(e.isTriggered(KeyboardMappings.CATALOG_PANEL)) {
				catalogAction();
			} else if(e.isTriggered(KeyboardMappings.HELP_SCREEN)) {
				EngineSettings.CONTROL_HELP.setOn(!EngineSettings.CONTROL_HELP.isOn());
			} else if(e.isTriggered(KeyboardMappings.STRUCTURE_PANEL)) {
				structureAction();
			} else if(e.isTriggered(KeyboardMappings.REACTOR_KEY)) {
				if(getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject().hasAnyReactors()) {
					ReactorTreeDialog d = new ReactorTreeDialog(getState(), (ManagedSegmentController<?>) getState().getCurrentPlayerObject());
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(154);
				}
			} else if(e.isTriggered(KeyboardMappings.MAP_PANEL)) {
				galaxyMapAction();
			} else if(e.isTriggered(KeyboardMappings.AI_CONFIG_PANEL)) {
				aiConfigurationAction(null);
			} else if(e.isTriggered(KeyboardMappings.FACTION_MENU)) {
				factionAction();
			} else if(e.isTriggered(KeyboardMappings.CREATIVE_MODE)) {
				if(getState().getPlayer().isHasCreativeMode()) {
					getState().getPlayer().setUseCreativeMode(!getState().getPlayer().isUseCreativeMode());
				}
			} else if(e.isTriggered(KeyboardMappings.SWITCH_COCKPIT_SHIP_NEXT) || e.isTriggered(KeyboardMappings.SWITCH_COCKPIT_SHIP_PREVIOUS)) {
				boolean up = e.isTriggered(KeyboardMappings.SWITCH_COCKPIT_SHIP_NEXT);
				PlayerGameControlManager playerGameControlManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
				handleDockingAndRail(up ? 1 : -1, playerGameControlManager);
			}


		}
		boolean interactionNeedsSuspension = false;
		for(AbstractControlManager a : panelElements) {
			interactionNeedsSuspension = interactionNeedsSuspension || a.isActive();
		}

		//INSERTED CODE
		for(GUIControlManager guiControlManager : ModGUIHandler.getAllModControlManagers()) {
			if(guiControlManager.isActive()) {
				interactionNeedsSuspension = true;
				break;
			}
		}
		//

		if(playerIntercationManager.isSuspended() != interactionNeedsSuspension) {
			// make sure this comes last to determine if hiding is needed
			playerIntercationManager.suspend(interactionNeedsSuspension);
		}
		if(getState().getController().getPlayerInputs().isEmpty()) {
			super.handleKeyEvent(e);
		}
	}

	private void handleDockingAndRail(int up, PlayerGameControlManager playerGameControlManager) {
		if(playerIntercationManager.getEntered() == null) {
			return;
		}

		final SegmentController current = playerIntercationManager.getEntered().getSegmentController();
		if(KeyboardMappings.SWITCH_COCKPIT_SHIP_HOLD_FOR_CHAIN.isDown()) {
			if(current.railController.isDockedAndExecuted()) {
				RailController rail = current.railController.previous.getRailRController();
				if(Math.abs(up) > rail.next.size() - 1) {
					return;
				}
				int dockIndex = -1;
				for(int i = 0; i < rail.next.size(); i++) {
					RailRelation r = rail.next.get(i);
					if(r.docked.getSegmentController() == current) {
						dockIndex = i;
					}
				}
				// switch in same chain
				dockIndex = FastMath.cyclicModulo(dockIndex + up, rail.next.size());
				if(dockIndex >= 0 && dockIndex < rail.next.size()) {
					SegmentController c = rail.next.get(dockIndex).docked.getSegmentController();
					if(c != current && InShipControlManager.checkEnter(c)) {
						InShipControlManager.switchEntered(c);
					} else {
						if(c != current) {
							getState().getController().popupInfoTextMessage(Lng.str("Skipped %s as access to it was denied", c.toNiceString()), 0);
						}
						handleDockingAndRail(up > 0 ? up + 1 : up - 1, playerGameControlManager);
					}
				}
			}
		} else {
			SegmentController c = null;
			// switch chain
			if(up > 0) {
				if(current.railController.isRail()) {
					c = current.railController.next.get(0).docked.getSegmentController();
				}
			} else {
				if(current.railController.isDockedAndExecuted()) {
					c = current.railController.previous.rail.getSegmentController();
				}
			}
			if(c != null && c != current && InShipControlManager.checkEnter(c)) {
				assert (playerIntercationManager.getEntered().getSegmentController() == current);
				assert (playerIntercationManager.getEntered().getSegmentController() != c);
				InShipControlManager.switchEntered(c);
			}
			getState().getController().popupInfoTextMessage(Lng.str("Hold '%s' to switch to other docks\non the same docking chain level", KeyboardMappings.SWITCH_COCKPIT_SHIP_HOLD_FOR_CHAIN.getKeyChar()), 0);
		}
	}

	@Override
	public void onSwitch(boolean active) {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//
		if(active) {
			structureControlManager.setActive(false);
			navigationControlManager.setActive(false);
			shopControlManager.setActive(false);
			weaponControlManager.setActive(false);
			fleetControlManager.setActive(false);
			musicManager.setActive(false);
			contractsControlManager.setActive(false);
			inventoryControlManager.setActive(false);
			mapControlManager.setActive(false);
			thrustManager.setActive(false);
			playerIntercationManager.setDelayedActive(true);
		} else {
			// close inventory
			inventoryControlManager.setActive(false);
			navigationControlManager.setActive(false);
			shopControlManager.setActive(false);
			weaponControlManager.setActive(false);
			fleetControlManager.setActive(false);
			musicManager.setActive(false);
			contractsControlManager.setActive(false);
			mapControlManager.setActive(false);
			thrustManager.setActive(false);
			structureControlManager.setActive(false);
		}
		super.onSwitch(active);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.schema.game.client.controller.manager.AbstractControlManager#update
	 * (org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		boolean interactionNeedsSuspension = inventoryControlManager.isActive() || shopControlManager.isActive() || weaponControlManager.isActive() || factionControlManager.isActive() || aiConfigurationManager.isActive() || fleetControlManager.isActive() || catalogControlManager.isActive();

		//INSERTED CODE
		for(AbstractControlManager controlManager : ModGUIHandler.getAllModControlManagers()) {
			if(controlManager.isActive()) {
				interactionNeedsSuspension = true;
				break;
			}
		}
		//
		if(playerIntercationManager.isSuspended() != interactionNeedsSuspension) {
			// make sure this comes last to determine if hiding is needed
			playerIntercationManager.suspend(interactionNeedsSuspension);
		}
		super.update(timer);
	}

	public void galaxyMapAction() {
		//INSERTED CODE
		Vector3i sector = GameClient.getClientPlayerState().getCurrentSector();
		if(sector.x >= 100000000 || sector.y >= 100000000 || sector.z >= 100000000)
			return; //Don't draw if outside universe to prevent visual glitches
		ModGUIHandler.deactivateAll();
		//

		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);

		boolean activate = !mapControlManager.isActive();
		System.err.println("ACTIVATE MAP " + activate);
		mapControlManager.setDelayedActive(activate);
	}

	public void navigationAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}

		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}

		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);

		boolean activate = !navigationControlManager.isActive();
		navigationControlManager.setDelayedActive(activate);
	}

	public void initialize() {
		playerIntercationManager = new PlayerInteractionControlManager(getState());
		inventoryControlManager = new InventoryControllerManager(getState());
		shopControlManager = new ShopControllerManager(getState());
		weaponControlManager = new WeaponAssignControllerManager(getState());
		fleetControlManager = new FleetControlManager(getState());
		navigationControlManager = new NavigationControllerManager(getState());
		mapControlManager = new MapControllerManager(getState());
		aiConfigurationManager = new AiConfigurationManager(getState());
		factionControlManager = new FactionControlManager(getState());
		catalogControlManager = new CatalogControlManager(getState());
		structureControlManager = new StructureControllerManager(getState());
		thrustManager = (new ThrustManager(getState()));
		musicManager = new MusicManager(getState());
		contractsControlManager = new ContractsControlManager(getState());
		getControlManagers().add(thrustManager);
		getControlManagers().add(playerIntercationManager);
		getControlManagers().add(inventoryControlManager);
		getControlManagers().add(shopControlManager);
		getControlManagers().add(weaponControlManager);
		getControlManagers().add(fleetControlManager);
		getControlManagers().add(navigationControlManager);
		getControlManagers().add(aiConfigurationManager);
		getControlManagers().add(mapControlManager);
		getControlManagers().add(factionControlManager);
		getControlManagers().add(catalogControlManager);
		getControlManagers().add(structureControlManager);
		getControlManagers().add(musicManager);
		getControlManagers().add(contractsControlManager);
		panelElements.add(thrustManager);
		panelElements.add(inventoryControlManager);
		panelElements.add(shopControlManager);
		panelElements.add(weaponControlManager);
		panelElements.add(fleetControlManager);
		panelElements.add(navigationControlManager);
		panelElements.add(aiConfigurationManager);
		panelElements.add(mapControlManager);
		panelElements.add(factionControlManager);
		panelElements.add(catalogControlManager);
		panelElements.add(structureControlManager);
		panelElements.add(musicManager);
		panelElements.add(contractsControlManager);
	}

	public boolean isAnyMenuActive() {
		for(AbstractControlManager c : getControlManagers()) {
			if(c.isActive() && c != playerIntercationManager) {
				return true;
			}
		}
		for(AbstractControlManager controlManager : ModGUIHandler.getAllModControlManagers()) {
			if(controlManager.isActive()) return true;
		}

		return false;
	}

	public ThrustManager getThrustManager() {
		return thrustManager;
	}

	public void inventoryAction(Inventory inventory) {
		inventoryAction(inventory, !inventoryControlManager.isActive(), false);
	}

	public void inventoryAction(Inventory inventory, boolean activate, boolean decativeBefore) {
		if(getState().getNumberOfUpdate() == lastUpdateInventory && System.currentTimeMillis() - lastUpdateTime < 1000) {
			return;
		}
		lastUpdateInventory = getState().getNumberOfUpdate();
		lastUpdateTime = System.currentTimeMillis();

		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(decativeBefore) {
			inventoryControlManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);
		if(activate) {
			inventoryControlManager.setSecondInventory(inventory);
		}
		inventoryControlManager.setActive(activate);
	}

	public void shopAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		boolean activate = !shopControlManager.isActive();
		if(!getState().isInShopDistance()) {
			getState().getController().popupInfoTextMessage(Lng.str("ERROR: You are not near any shop!"), 0);
		} else {
			if(inventoryControlManager.isActive()) {
				inventoryControlManager.setActive(false);
			}
			if(mapControlManager.isActive()) {
				mapControlManager.setActive(false);
			}
			if(factionControlManager.isActive()) {
				factionControlManager.setActive(false);
			}
			if(weaponControlManager.isActive()) {
				weaponControlManager.setActive(false);
			}
			if(fleetControlManager.isActive()) {
				fleetControlManager.setActive(false);
			}
			if(navigationControlManager.isActive()) {
				navigationControlManager.setActive(false);
			}
			if(structureControlManager.isActive()) {
				structureControlManager.setActive(false);
			}
			if(catalogControlManager.isActive()) {
				catalogControlManager.setActive(false);
			}
			if(aiConfigurationManager.isActive()) {
				aiConfigurationManager.setActive(false);
			}
			if(thrustManager.isActive()) {
				thrustManager.setActive(false);
			}
			if(musicManager.isActive()) musicManager.setActive(false);
			if(contractsControlManager.isActive()) contractsControlManager.setActive(false);
			shopControlManager.setDelayedActive(activate);
		}
	}

	public void weaponAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(!playerIntercationManager.getInShipControlManager().isActive()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: Weapon Menu only available\ninside a ship!"), 0);
			return;
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);
		boolean activate = !weaponControlManager.isActive();
		weaponControlManager.setDelayedActive(activate);
	}

	public void fleetAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);
		boolean activate = !fleetControlManager.isActive();
		fleetControlManager.setDelayedActive(activate);
	}

	public void structureAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(factionControlManager.isActive()) {
			factionControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);
		boolean activate = !structureControlManager.isActive();
		structureControlManager.setDelayedActive(activate);
	}

	public void catalogAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().structureControlManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().aiConfigurationManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().inventoryControlManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().shopControlManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().navigationControlManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().weaponControlManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().factionControlManager.setActive(false);
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().catalogControlManager.setActive(true);
	}

	public void openMusicManager() {
		//INSERTED CODE
		//ModGUIHandler.deactivateAll();
		//

		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);

		if(factionControlManager.isActive()) factionControlManager.setActive(false);
		if(musicManager.isActive()) musicManager.setActive(false);
		musicManager.setDelayedActive(!musicManager.isActive());
	}

	private MusicManager getMusicManager() {
		return musicManager;
	}

	public void factionAction() {
		//INSERTED CODE
		ModGUIHandler.deactivateAll();
		//

		if(inventoryControlManager.isActive()) {
			inventoryControlManager.setActive(false);
		}
		if(shopControlManager.isActive()) {
			shopControlManager.setActive(false);
		}
		if(navigationControlManager.isActive()) {
			navigationControlManager.setActive(false);
		}
		if(fleetControlManager.isActive()) {
			fleetControlManager.setActive(false);
		}
		if(structureControlManager.isActive()) {
			structureControlManager.setActive(false);
		}
		if(catalogControlManager.isActive()) {
			catalogControlManager.setActive(false);
		}
		if(mapControlManager.isActive()) {
			mapControlManager.setActive(false);
		}
		if(weaponControlManager.isActive()) {
			weaponControlManager.setActive(false);
		}
		if(aiConfigurationManager.isActive()) {
			aiConfigurationManager.setActive(false);
		}
		if(thrustManager.isActive()) {
			thrustManager.setActive(false);
		}
		if(musicManager.isActive()) musicManager.setActive(false);
		if(contractsControlManager.isActive()) contractsControlManager.setActive(false);
		boolean activate = !factionControlManager.isActive();
		factionControlManager.setDelayedActive(activate);
	}

	public FleetControlManager getFleetControlManager() {
		return fleetControlManager;
	}

	public ContractsControlManager getContractsControlManager() {
		return contractsControlManager;
	}

	public void contractsAction() {
		ModGUIHandler.deactivateAll();
		if(inventoryControlManager.isActive()) inventoryControlManager.setActive(false);
		if(shopControlManager.isActive()) shopControlManager.setActive(false);
		if(navigationControlManager.isActive()) navigationControlManager.setActive(false);
		if(fleetControlManager.isActive()) fleetControlManager.setActive(false);
		if(structureControlManager.isActive()) structureControlManager.setActive(false);
		if(catalogControlManager.isActive()) catalogControlManager.setActive(false);
		if(mapControlManager.isActive()) mapControlManager.setActive(false);
		if(weaponControlManager.isActive()) weaponControlManager.setActive(false);
		if(aiConfigurationManager.isActive()) aiConfigurationManager.setActive(false);
		if(thrustManager.isActive()) thrustManager.setActive(false);
		if(musicManager.isActive()) musicManager.setActive(false);
		contractsControlManager.setDelayedActive(!contractsControlManager.isActive());
	}
}