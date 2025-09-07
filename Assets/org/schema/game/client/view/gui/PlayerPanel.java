package org.schema.game.client.view.gui;

import api.listener.events.gui.PlayerGUICreateEvent;
import api.listener.events.gui.PlayerGUIDrawEvent;
import api.mod.StarLoader;
import api.utils.gui.GUIControlManager;
import api.utils.gui.ModGUIHandler;
import org.schema.common.FastMath;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.ChatControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.controller.manager.ingame.SegmentControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipControllerManager;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.AdvancedBuldModeLeftContainer;
import org.schema.game.client.view.gui.advancedbuildmode.AdvancedBuildMode;
import org.schema.game.client.view.gui.ai.newai.AIPanelNew;
import org.schema.game.client.view.gui.buildtools.BuildToolsPanel;
import org.schema.game.client.view.gui.catalog.newcatalog.CatalogPanelNew;
import org.schema.game.client.view.gui.chat.ChatPanel;
import org.schema.game.client.view.gui.contracts.ContractsPanel;
import org.schema.game.client.view.gui.faction.newfaction.FactionPanelNew;
import org.schema.game.client.view.gui.fleet.FleetPanel;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.client.view.gui.inventory.inventorynew.InventoryPanelNew;
import org.schema.game.client.view.gui.mapgui.MapToolsPanel;
import org.schema.game.client.view.gui.navigation.navigationnew.NavigationPanelNew;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.game.client.view.gui.shiphud.AiCrewAndFleetInformationPanel;
import org.schema.game.client.view.gui.shiphud.newhud.BottomBarBuild;
import org.schema.game.client.view.gui.shiphud.newhud.TopBarNew;
import org.schema.game.client.view.gui.shop.shopnew.ShopPanelNew;
import org.schema.game.client.view.gui.structurecontrol.structurenew.StructurePanelNew;
import org.schema.game.client.view.gui.weapon.WeaponBottomBar;
import org.schema.game.client.view.gui.weapon.WeaponControllerPanelInterface;
import org.schema.game.client.view.gui.weapon.WeaponPanelNew;
import org.schema.game.client.view.gui.weapon.WeaponSlotOverlayElement;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.element.Element;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerPanel extends GUIElement {

	public static String infoText;

	private GUIScrollablePanel infoScroll;

	private GUITintScreenElement tutorialTint;

	private FleetPanel fleetPanel;

	private ContractsPanel contractsPanel;

	private FactionPanelNew factionPanelNew;

	private CatalogPanelNew catalogPanelNew;

	private ShopPanelNew shopPanelNew;

	private InventoryPanelNew inventoryPanelNew;

	private AIPanelNew aiPanelNew;

	private NavigationPanelNew navigationPanelNew;

	private WeaponPanelNew weaponManagerPanelNew;

	private HotbarInterface buildSideBar;

	private WeaponBottomBar[] weaponSideBars;

	private GUIAnchor inventoryTab;

	private GUIAnchor weaponTab;

	private GUIAnchor shopTab;

	private GUIAnchor close;

	private GUIAnchor naviTab;

	private GUIHelpPanelManager helpPanel;

	private TopBarInterface topBar;

	private GUIAnchor aiMenuTab;

	private GUIAnchor factionMenuTab;

	private GUIAnchor catalogTab;

	private StructurePanelNew structurePanelNew;

	private GUIElement structureMenuTab;

	private AiCrewAndFleetInformationPanel aiCrewAndFleetInformationPanel;

	private BuildToolsPanel buildToolsAstronaut;

	private MapToolsPanel mapPanel;

	private long lastTutorialTint;

	private boolean panelActive;

	private GUITopBar topTaskBar;

	private ChatPanel chat;

	private boolean chatDryRun;

	private long firstDrawChat;

	private byte selectedWeaponBar;

	private float timeSpent;

	private boolean tabInfo;

	private GUITextOverlay infoTextOverlay;

	public final AdvancedBuildMode advancedBuildMode;

	public final AdvancedBuldModeLeftContainer advancedBuildModeContainer;

	public static boolean mouseInInfoScroll;

	public PlayerPanel(InputState state) {
		super(state);
		advancedBuildMode = new AdvancedBuildMode((GameClientState) state);
		advancedBuildModeContainer = new AdvancedBuldModeLeftContainer((GameClientState) state);
	}

	public FleetPanel getFleetPanel() {
		return fleetPanel;
	}

	private void activateAIManager() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().aiConfigurationAction(null);
		// if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().isActive()){
		// getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().aiConfigurationAction(null);
		// }else{
		// getState().getController().popupAlertTextMessage("No AI context available\n" +
		// "Either use this in a ship\n" +
		// "with an AI Module, or\n" +
		// "activate an AI Module externally ("+KeyboardMappings.ACTIVATE.getKeyChar()+")", 0);
		// }
	}

	private void activateCatalogManager() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().setActive(true);
	}

	private void activateStructureControllerManager() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().setActive(true);
	}

	private void activateFactionManager() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().setActive(true);
	}

	private void activateInventory() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().inventoryAction(null);
	}

	private void activateNavigation() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().setActive(true);
	}

	private void activateShop() {
		if(getState().isInShopDistance()) {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(true);
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("No shop in range!"), 0);
		}
	}

	private void activateWeaponAssignManager() {
		if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().isActive()) {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().setActive(true);
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: Only available inside a ship!"), 0);
		}
	}

	public void afterGUIDraw() {
		weaponManagerPanelNew.checkForUpdate();
		GUIElement.enableOrthogonal();
		GlUtil.glPushMatrix();
		drawToolTips();
		GlUtil.glPopMatrix();
		GUIElement.disableOrthogonal();
	}

	private boolean aiManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().isTreeActive();
	}

	private boolean buildModeActive() {
		return getActiveBuildController() != null;
	}

	private boolean catalogControllerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().isTreeActive();
	}

	private boolean fleetControllerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFleetControlManager().isTreeActive();
	}

	private boolean contractsControllerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getContractsControlManager().isTreeActive();
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if(needsReOrientation()) {
			doOrientation();
		}
		GUIElement.enableOrthogonal();
		GlUtil.glPushMatrix();
		topBar.draw();
		if(inventoryActive() || shopActive()) buildSideBar.activateDragging(true);
		else buildSideBar.activateDragging(false);
		boolean panelActiveTest = false;
		aiCrewAndFleetInformationPanel.draw();
		if(inventoryActive()) {
			panelActiveTest = true;
			inventoryPanelNew.draw();
		} else {
			if(isNewHud()) {
				inventoryPanelNew.reset();
				inventoryPanelNew.resetOthers();
			}
		}
		if(EngineSettings.CONTROL_HELP.isOn()) {
			GlUtil.glPushMatrix();
			helpPanel.draw();
			GlUtil.glPopMatrix();
		}
		if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().musicManager.isActive()) getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().musicManager.draw();
		else getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().musicManager.cleanUp();

		if(aiManagerActive()) {
			panelActiveTest = true;
			aiPanelNew.draw();
		} else {
			aiPanelNew.reset();
		}
		if(shopActive()) {
			panelActiveTest = true;
			shopPanelNew.draw();
		} else {
			shopPanelNew.reset();
		}
		if(navigationActive()) {
			panelActiveTest = true;
			navigationPanelNew.draw();
		} else {
			navigationPanelNew.reset();
		}
		if(factionControllerManagerActive()) {
			panelActiveTest = true;
			factionPanelNew.draw();
		} else {
			factionPanelNew.reset();
		}
		if(fleetControllerManagerActive()) {
			panelActiveTest = true;
			fleetPanel.draw();
		} else {
			fleetPanel.reset();
		}
		if(contractsControllerManagerActive()) {
			panelActiveTest = true;
			contractsPanel.draw();
		} else {
			contractsPanel.reset();
		}
		if(catalogControllerManagerActive()) {
			panelActiveTest = true;
			catalogPanelNew.draw();
		} else {
			catalogPanelNew.reset();
		}
		if(shipControllerManagerActive()) {
			panelActiveTest = true;
			weaponManagerPanelNew.draw();
		} else {
		}
		if(thrustManagerManagerActive()) {
			panelActiveTest = true;
		}
		if(structureControllerManagerActive()) {
			panelActiveTest = true;
			structurePanelNew.draw();
		} else {
			structurePanelNew.reset();
		}
		if(inGameActive() && !mapActive()) {
			if(isDrawShipSideBar()) {
				weaponSideBars[selectedWeaponBar].draw();
			} else {
				buildSideBar.draw();
				if(!shopActive() && !inventoryActive() && !structureControllerManagerActive() && buildModeActive()) {
					// buildToolsShip.draw();
					if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().structureInfo) {
						advancedBuildModeContainer.draw();
					}
					advancedBuildMode.draw();
				} else if(isInAstronautMode() && !shopActive() && !inventoryActive()) {
					buildToolsAstronaut.draw();
				}
			}
		} else {
			if(mapActive()) {
				panelActiveTest = false;
				mapPanel.draw();
				if(infoText != null) {
					infoTextOverlay.setTextSimple(infoText);
					// infoTextOverlay.setHeight(infoTextOverlay.getTextHeight());
					mouseInInfoScroll = false;
					infoScroll.setPos(GLFrame.getWidth() - infoScroll.getWidth(), 50, 0);
					infoScroll.draw();
					infoText = null;
				}
			}
		}
		// drawChat();
		if(getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging() instanceof InventorySlotOverlayElement) {
			// System.err.println("DRAGGING "+state.getDragging());
			InventorySlotOverlayElement e = (InventorySlotOverlayElement) getState().getController().getInputController().getDragging();
			if(e.getType() == Element.TYPE_NONE || e.getCount(true) <= 0) {
				// assert(false):e.getType()+"; "+e.getCount(true);
				getState().getController().getInputController().setDragging(null);
			} else {
				inventoryPanelNew.drawDragging(e);
			}
		}
		if(getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging() instanceof WeaponSlotOverlayElement) {
			// System.err.println("DRAGGING "+state.getDragging());
			WeaponSlotOverlayElement e = (WeaponSlotOverlayElement) getState().getController().getInputController().getDragging();
			if(e.getType() == Element.TYPE_NONE) {
				getState().getController().getInputController().setDragging(null);
			} else {
				weaponSideBars[selectedWeaponBar].drawDragging(e);
			}
		}
		if(getState().getController().getTutorialMode() != null) {
			TutorialMode tutorialMode = getState().getController().getTutorialMode();
			if(getState().getPlayer().isInTutorial() && (!getState().getController().getPlayerInputs().isEmpty() || getState().getUpdateTime() - lastTutorialTint < 300)) {
				if(!getState().getController().getPlayerInputs().isEmpty()) {
					lastTutorialTint = getState().getUpdateTime();
				}
				tutorialTint.draw();
			}
		}
		//INSERTED CODE
		ArrayList<GUIControlManager> modControlManagerList = ModGUIHandler.getAllModControlManagers();
		for(GUIControlManager modControlManager : modControlManagerList) {
			if(modControlManager.isActive()) {
				modControlManager.draw();
			} else {
				modControlManager.cleanUp();
			}
		}
		//
		this.panelActive = panelActiveTest;
		GlUtil.glPopMatrix();
		if(panelActive) {
			topTaskBar.draw();
			GUIResizableGrabbableWindow.topHeightSubtract = (int) topTaskBar.getHeight();
		} else {
			GUIResizableGrabbableWindow.topHeightSubtract = 0;
		}

		//INSERTED CODE @413
		PlayerGUIDrawEvent event = new PlayerGUIDrawEvent(this);
		StarLoader.fireEvent(event, false);
		///
		drawChat();
		GUIElement.disableOrthogonal();
	}

	@Override
	public void onInit() {
		topBar = new TopBarNew(getState(), this);
		topBar.onInit();
		topTaskBar = new GUITopBar(getState());
		topTaskBar.onInit();
		tutorialTint = new GUITintScreenElement(getState());
		tutorialTint.getColor().set(0.1f, 0.1f, 0.1f, 0.8f);
		tutorialTint.onInit();
		chat = new ChatPanel(getState());
		chat.onInit();
		TabCallback tabCallback = new TabCallback();
		close = new GUIAnchor(getState(), 39, 26);
		close.setUserPointer("X");
		close.setMouseUpdateEnabled(true);
		close.setCallback(tabCallback);
		close.getPos().set(804, 4, 0);
		close.onInit();
		inventoryTab = new GUIAnchor(getState(), 147, 40);
		inventoryTab.setUserPointer("INVENTORY");
		inventoryTab.setMouseUpdateEnabled(true);
		inventoryTab.setCallback(tabCallback);
		inventoryTab.getPos().set(216, 26, 0);
		inventoryTab.onInit();
		aiCrewAndFleetInformationPanel = new AiCrewAndFleetInformationPanel(getState());
		aiCrewAndFleetInformationPanel.onInit();
		aiCrewAndFleetInformationPanel.orientate(ORIENTATION_LEFT | ORIENTATION_VERTICAL_MIDDLE);
		aiMenuTab = new GUIAnchor(getState(), 147, 40);
		aiMenuTab.setUserPointer("AI");
		aiMenuTab.setMouseUpdateEnabled(true);
		aiMenuTab.setCallback(tabCallback);
		aiMenuTab.getPos().set(662, 472, 0);
		aiMenuTab.onInit();
		factionMenuTab = new GUIAnchor(getState(), 147, 40);
		factionMenuTab.setUserPointer("FACTION");
		factionMenuTab.setMouseUpdateEnabled(true);
		factionMenuTab.setCallback(tabCallback);
		factionMenuTab.getPos().set(517, 472, 0);
		factionMenuTab.onInit();
		structureMenuTab = new GUIAnchor(getState(), 147, 40);
		structureMenuTab.setUserPointer("STRUCTURE");
		structureMenuTab.setMouseUpdateEnabled(true);
		structureMenuTab.setCallback(tabCallback);
		structureMenuTab.getPos().set(216, 472, 0);
		structureMenuTab.onInit();
		weaponTab = new GUIAnchor(getState(), 147, 40);
		weaponTab.setUserPointer("WEAPON");
		weaponTab.setMouseUpdateEnabled(true);
		weaponTab.setCallback(tabCallback);
		weaponTab.getPos().set(366, 26, 0);
		weaponTab.onInit();
		catalogTab = new GUIAnchor(getState(), 147, 40);
		catalogTab.setUserPointer("CATALOG");
		catalogTab.setMouseUpdateEnabled(true);
		catalogTab.setCallback(tabCallback);
		catalogTab.getPos().set(366, 472, 0);
		catalogTab.onInit();
		shopTab = new GUIAnchor(getState(), 147, 40);
		shopTab.setUserPointer("SHOP");
		shopTab.setMouseUpdateEnabled(true);
		shopTab.setCallback(tabCallback);
		shopTab.getPos().set(514, 26, 0);
		shopTab.onInit();
		naviTab = new GUIAnchor(getState(), 147, 40);
		naviTab.setUserPointer("NAVIGATION");
		naviTab.setMouseUpdateEnabled(true);
		naviTab.setCallback(tabCallback);
		naviTab.getPos().set(662, 26, 0);
		naviTab.onInit();
		fleetPanel = new FleetPanel(getState());
		fleetPanel.onInit();
		(contractsPanel = new ContractsPanel(getState())).onInit();
		buildToolsAstronaut = new BuildToolsPanel(getState(), true);
		buildToolsAstronaut.onInit();
		aiPanelNew = new AIPanelNew(getState());
		aiPanelNew.onInit();
		inventoryPanelNew = new InventoryPanelNew(getState());
		inventoryPanelNew.onInit();
		weaponManagerPanelNew = new WeaponPanelNew(getState());
		weaponManagerPanelNew.onInit();
		navigationPanelNew = new NavigationPanelNew(getState());
		navigationPanelNew.onInit();
		structurePanelNew = new StructurePanelNew(getState());
		structurePanelNew.onInit();
		infoScroll = new GUIScrollablePanel(430, 550, getState());
		infoTextOverlay = new GUITextOverlay(getState());
		infoTextOverlay.autoWrapOn = infoScroll;
		infoScroll.setContent(infoTextOverlay);
		infoScroll.onInit();
		shopPanelNew = new ShopPanelNew(getState());
		factionPanelNew = new FactionPanelNew(getState());
		catalogPanelNew = new CatalogPanelNew(getState());
		assert (inventoryPanelNew != null);
		buildSideBar = new BottomBarBuild(getState(), inventoryPanelNew);
		buildSideBar.onInit();
		try {
			helpPanel = new GUIHelpPanelManager(getState(), "." + File.separator + "data" + File.separator + "tutorial" + File.separator + "ControlHelpers.xml");
			helpPanel.onInit();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(SAXException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		weaponSideBars = new WeaponBottomBar[10];
		for(int i = 0; i < weaponSideBars.length; i++) {
			weaponSideBars[i] = new WeaponBottomBar(getState(), i);
			weaponSideBars[i].onInit();
		}
		mapPanel = new MapToolsPanel(getState());
		mapPanel.onInit();
		advancedBuildMode.onInit();
		advancedBuildModeContainer.onInit();
		//INSERTED CODE
		PlayerGUICreateEvent event = new PlayerGUICreateEvent(this);
		StarLoader.fireEvent(event, false);
		///
		doOrientation();
	}

	private boolean structureControllerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getStructureControlManager().isTreeActive();
	}

	private boolean thrustManagerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getThrustManager().isTreeActive();
	}

	public void deactivateAll() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().deactivateAll();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#doOrientation()
	 */
	@Override
	protected void doOrientation() {
		buildSideBar.orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_BOTTOM);
		helpPanel.orientate(GUIElement.ORIENTATION_LEFT | GUIElement.ORIENTATION_BOTTOM);
		helpPanel.getPos().y -= (weaponSideBars[0].getHeight() / 2 - 30);
		helpPanel.getPos().x += 30;
		for(int i = 0; i < weaponSideBars.length; i++) {
			weaponSideBars[i].orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_BOTTOM);
		}
		mapPanel.orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_BOTTOM);
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	@Override
	public float getWidth() {
		return 0;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public void drawChat() {
		if(!getState().getPlayer().getPlayerChannelManager().getAvailableChannels().isEmpty()) {
			GUIElement.enableOrthogonal();
			// System.err.println("DRLKDLKJDLKJ");
			if(!chatDryRun) {
				if(firstDrawChat == 0) {
					firstDrawChat = getState().getUpdateTime();
				}
				GlUtil.setColorMask(false);
				chat.draw();
				GlUtil.setColorMask(true);
				if(getState().getUpdateTime() > firstDrawChat + 15000) {
					chatDryRun = true;
				}
			}
			if(chatManagerActive()) {
				chat.draw();
			} else {
				chat.drawAsHud();
			}
			GUIElement.disableOrthogonal();
		}
	}

	private boolean chatManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive();
	}

	public boolean isDrawShipSideBar() {
		return (shipExternalActive() || shipControllerManagerActive()) && !shopActive() && !inventoryActive();
	}

	public void drawToolTips() {
		if(!EngineSettings.DRAW_TOOL_TIPS.isOn()) {
			return;
		}
		getState().getController().getInputController().getGuiCallbackController().drawToolTips();
		if(inGameActive()) {
			GUIElement.enableOrthogonal();
			((TooltipProvider) buildSideBar).drawToolTip();
			GUIElement.disableOrthogonal();
			GUIElement.enableOrthogonal();
			advancedBuildMode.drawToolTip(getState().getUpdateTime());
			GUIElement.disableOrthogonal();
			GUIElement.enableOrthogonal();
			advancedBuildModeContainer.drawToolTip(getState().getUpdateTime());
			GUIElement.disableOrthogonal();
		}
		if(inventoryActive()) {
			GUIElement.enableOrthogonal();
			inventoryPanelNew.drawToolTip();
			GUIElement.disableOrthogonal();
		}
		if(shipControllerManagerActive()) {
			GUIElement.enableOrthogonal();
			weaponManagerPanelNew.drawToolTip();
			GUIElement.disableOrthogonal();
			GUIElement.enableOrthogonal();
			weaponSideBars[selectedWeaponBar].drawToolTip();
			GUIElement.disableOrthogonal();
		}
		InventorySlotOverlayElement.drawFixedToolTips();
	}

	private boolean factionControllerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().isTreeActive();
	}

	public boolean isInAstronautMode() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().isTreeActive();
	}

	public SegmentBuildController getActiveBuildController() {
		if(getSegmentControlManager().getSegmentBuildController().isTreeActive()) {
			return getSegmentControlManager().getSegmentBuildController();
		} else if(getShipControllerManager().getSegmentBuildController().isTreeActive()) {
			return getShipControllerManager().getSegmentBuildController();
		}
		return null;
	}

	/**
	 * @return the buildSideBar
	 */
	public HotbarInterface getBuildSideBar() {
		return buildSideBar;
	}

	/**
	 * @param buildSideBar the buildSideBar to set
	 */
	public void setBuildSideBar(HotbarInterface buildSideBar) {
		this.buildSideBar = buildSideBar;
	}

	public SegmentControlManager getSegmentControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager();
	}

	public ShipControllerManager getShipControllerManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager();
	}

	private boolean inGameActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().isTreeActive();
	}

	private boolean inventoryActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().isTreeActive();
	}

	public boolean isMouseOnPanel() {
		boolean onBuildPanel = PlayerInteractionControlManager.isAdvancedBuildMode(getState()) && advancedBuildMode.isInside();
		boolean onStatsPanel = advancedBuildModeContainer.isInside();
		return onBuildPanel || onStatsPanel;
	}

	public void managerChanged(ElementCollectionManager<?, ?, ?> man) {
		weaponManagerPanelNew.managerChanged(man);
	}

	private boolean mapActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive();
	}

	private boolean navigationActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().isTreeActive();
	}

	public void modSelectedWeaponBottomBar(int by) {
		int n = FastMath.cyclicModulo(selectedWeaponBar + by, weaponSideBars.length);
		selectedWeaponBar = (byte) n;
	}

	public byte getSelectedWeaponBottomBar() {
		return selectedWeaponBar;
	}

	public void setSelectedWeaponBottomBar(byte index) {
		index = (byte) Math.min(weaponSideBars.length - 1, Math.max(0, index));
		selectedWeaponBar = index;
	}

	private boolean shipControllerManagerActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().isActive();
	}

	private boolean shipExternalActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActive();
	}

	private boolean shopActive() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().isTreeActive();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		topBar.updateCreditsAndSpeed();
		topBar.update(timer);
		weaponManagerPanelNew.update(timer);
		buildSideBar.update(timer);
		advancedBuildMode.update(timer);
		advancedBuildModeContainer.update(timer);
		timeSpent += timer.getDelta();
		if(!tabInfo && timeSpent > 15) {
			tabInfo = true;
			getState().getController().showBigMessage("LLALLSSKKS", Lng.str("RADIAL MENU AVAILABLE"), Lng.str("hold %s to access", KeyboardMappings.RADIAL_MENU.getKeyChar()), 0);
		}
		for(int i = 0; i < weaponSideBars.length; i++) {
			weaponSideBars[i].update(timer);
		}
		aiPanelNew.update(timer);
		buildToolsAstronaut.update(timer);
		mapPanel.update(timer);
		helpPanel.update(timer);
		if(factionPanelNew != null) {
			factionPanelNew.update(timer);
		}
		if(fleetPanel != null) {
			fleetPanel.update(timer);
		}
		chat.update(timer);
		super.update(timer);
	}

	/**
	 * @return the structurePanel
	 */
	public StructurePanelNew getStructurePanel() {
		return structurePanelNew;
	}

	/**
	 * @return the topBar
	 */
	public TopBarInterface getTopBar() {
		return topBar;
	}

	/**
	 * @param topBar the topBar to set
	 */
	public void setTopBar(TopBarInterface topBar) {
		this.topBar = topBar;
	}

	/**
	 * @return the weaponManagerPanel
	 */
	public WeaponControllerPanelInterface getWeaponManagerPanel() {
		return weaponManagerPanelNew;
	}

	/**
	 * @return the weaponSideBar
	 */
	public HotbarInterface getWeaponSideBar() {
		return weaponSideBars[selectedWeaponBar];
	}

	/**
	 * @return the panelActive
	 */
	public boolean isPanelActive() {
		return panelActive;
	}

	/**
	 * @param panelActive the panelActive to set
	 */
	public void setPanelActive(boolean panelActive) {
		this.panelActive = panelActive;
	}

	public InventoryPanelNew getInventoryPanel() {
		return inventoryPanelNew;
	}

	public void notifySwitch(AbstractControlManager o) {
		if(o instanceof ChatControlManager) {
			if(chat != null) {
				chat.onActivateChat(((ChatControlManager) o).isActive());
			}
		}
	}

	public ChatPanel getChat() {
		return chat;
	}

	private class TabCallback implements GUICallback {

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if(event.pressedLeftMouse()) {
				if(callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(610);
				} else {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(609);
				}
				if(callingGuiElement.getUserPointer().equals("INVENTORY")) {
					if(!inventoryActive()) {
						activateInventory();
					}
				} else if(callingGuiElement.getUserPointer().equals("STRUCTURE")) {
					if(!structureControllerManagerActive()) {
						activateStructureControllerManager();
					}
				} else if(callingGuiElement.getUserPointer().equals("WEAPON")) {
					if(!shipControllerManagerActive()) {
						activateWeaponAssignManager();
					}
				} else if(callingGuiElement.getUserPointer().equals("FACTION")) {
					if(!factionControllerManagerActive()) {
						activateFactionManager();
					}
				} else if(callingGuiElement.getUserPointer().equals("CATALOG")) {
					if(!catalogControllerManagerActive()) {
						activateCatalogManager();
					}
				} else if(callingGuiElement.getUserPointer().equals("AI")) {
					if(!aiManagerActive()) {
						activateAIManager();
					}
				} else if(callingGuiElement.getUserPointer().equals("SHOP")) {
					if(!shopActive()) {
						activateShop();
					}
				} else if(callingGuiElement.getUserPointer().equals("NAVIGATION")) {
					if(!navigationActive()) {
						activateNavigation();
					}
				} else if(callingGuiElement.getUserPointer().equals("X")) {
					deactivateAll();
				} else {
					assert (false) : "not known command: " + callingGuiElement.getUserPointer();
				}
			}
		}

		@Override
		public boolean isOccluded() {
			return !(getState()).getPlayerInputs().isEmpty();
		}
	}

	public GUIHelpPanelManager getHelpPanel() {
		return helpPanel;
	}

	public GUITopBar getTopTaskBar() {
		return topTaskBar;
	}

	public void setTopTaskBar(GUITopBar topTaskBar) {
		this.topTaskBar = topTaskBar;
	}
}
