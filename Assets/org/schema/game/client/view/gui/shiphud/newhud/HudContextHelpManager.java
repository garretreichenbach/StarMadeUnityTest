package org.schema.game.client.view.gui.shiphud.newhud;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.ManagedShop;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealer;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.StabilizerUnit;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.graphicsengine.core.settings.ContextGroup;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputAction;
import org.schema.schine.input.InputType;
import org.schema.schine.input.KeyboardMappings;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HudContextHelpManager {

	private final GameClientState state;
	private boolean init;
	private int leftStartPosY;
	private int leftEndPosY;

	private int bottomOffsetY() {
		return UIScale.getUIScale().scale(128);
	}

	private SegmentPiece currentPiece;

	public HudContextHelpManager(GameClientState state) {
		this.state = state;
	}

	public void draw() {
		if(!init) {
			onInit();
		}
		queueMouse.addAll(queueBlock);
		//drawBlock(queueBlock);
		drawMouse(queueMouse);
		drawLeft(queueLeft);


	}

	private void drawLeft(List<HudContextHelperContainer> q) {
		if(q.isEmpty()) {
			leftStartPosY = GLFrame.getHeight();
			leftEndPosY = GLFrame.getHeight();
		} else {
			leftStartPosY = GLFrame.getHeight() - bottomOffsetY();
		}

		int x = 0;
		int y = leftStartPosY;

		for(HudContextHelperContainer h : q) {
			h.icon.setPos(x, y, 0);
			h.icon.draw();
			y -= (int) (h.icon.getHeight() + UIScale.getUIScale().scale(5));
		}

		leftEndPosY = y;
	}

	private void drawMouse(List<HudContextHelperContainer> q) {

		int x = GLFrame.getWidth() / 2 + UIScale.getUIScale().scale(5);
		int y = GLFrame.getHeight() / 2;

		for(HudContextHelperContainer h : q) {
			y += (int) (h.icon.getHeight() + UIScale.getUIScale().scale(20));
			h.icon.setPos(x, y, 0);
			h.icon.draw();
		}
	}


	private HudContextHelperContainer tmpHelper = new HudContextHelperContainer();
	private final Object2ObjectOpenHashMap<HudContextHelperContainer, HudContextHelperContainer> helperCache = new Object2ObjectOpenHashMap<HudContextHelperContainer, HudContextHelperContainer>();
	private List<HudContextHelperContainer> queueMouse = new ObjectArrayList<HudContextHelperContainer>();
	private List<HudContextHelperContainer> queueLeft = new ObjectArrayList<HudContextHelperContainer>();
	private List<HudContextHelperContainer> queueBlock = new ObjectArrayList<HudContextHelperContainer>();

	public void addBlock(SegmentPiece p, Object text) {
		HudContextHelperContainer addHelper = addHelper(InputType.BLOCK, queueBlock.size(), text, Hos.BLOCK, ContextFilter.CRUCIAL);
		addHelper.p = p;
	}

	public void addInfo(Hos position, ContextFilter filter, Object text) {
		addHelper(InputType.BLOCK, queueBlock.size(), text, position, filter);
	}

	public void addHelper(KeyboardMappings m, String text, Hos position, ContextFilter filter) {
		if(m.getMappings().length > 0) addHelper(m.getMappings()[0], text, position, filter);
	}

	public HudContextHelperContainer addHelper(InputAction action, Object text, Hos position, ContextFilter filter) {
		if(action.type == InputType.BLOCK) {
			throw new RuntimeException("Cannot at block heler this way. use addHelper(InputType type, int key, Object text, Hos position, ContextFilter filter)");
		}
		return addHelper(action.type, action.value, text, position, filter);
	}

	public HudContextHelperContainer addHelper(InputType type, int key, Object text, Hos position, ContextFilter filter) {
		try {
			tmpHelper.set(type, key, text, position, filter);
			if(!helperCache.containsKey(tmpHelper)) {
				HudContextHelperContainer h = new HudContextHelperContainer(type, key, text, position, filter);
				h.create(state);
				helperCache.put(h, h);
			}
			HudContextHelperContainer helper = helperCache.get(tmpHelper);
			if(((ContextGroup) EngineSettings.G_ICON_CONTEXT_FILTER.getObject()).containsFilter(filter)) {
				if(position == Hos.MOUSE) {
					this.queueMouse.add(helper);
				} else if(position == Hos.BLOCK) {
					this.queueBlock.add(helper);
				} else {
					this.queueLeft.add(helper);
				}
			}
			return helper;
		} catch(Exception e) {
			System.err.println("Exception: HELPER FAILED WITH TEXT: " + text);
			e.printStackTrace();
			return new HudContextHelperContainer(type, key, "ERROR IN TRANSLATION", position, filter);
		}

	}

	private void addBuildAndTake() {
		if(currentPiece != null) {
			InventorySlot slot = state.getPlayer().getInventory().getSlot(state.getPlayer().getSelectedBuildSlot());
			if(slot == null) {
				if(state.getCurrentPlayerObject() instanceof PlayerCharacter) {
					addHelper(KeyboardMappings.REMOVE_BLOCK_CHARACTER, Lng.str("Remove"), Hos.MOUSE, ContextFilter.IMPORTANT);
				} else {
					addHelper(KeyboardMappings.REMOVE_BLOCK_BUILD_MODE, Lng.str("Remove"), Hos.MOUSE, ContextFilter.IMPORTANT);
				}
			} else if(slot.isMultiSlot() || !slot.isMetaItem()) {
				if(state.getCurrentPlayerObject() instanceof PlayerCharacter) {
					addHelper(KeyboardMappings.USE_SLOT_ITEM_CHARACTER, Lng.str("Place"), Hos.MOUSE, ContextFilter.IMPORTANT);
					addHelper(KeyboardMappings.REMOVE_BLOCK_CHARACTER, Lng.str("Remove"), Hos.MOUSE, ContextFilter.IMPORTANT);
				} else {
					addHelper(KeyboardMappings.BUILD_BLOCK_BUILD_MODE, Lng.str("Place"), Hos.MOUSE, ContextFilter.IMPORTANT);
					addHelper(KeyboardMappings.REMOVE_BLOCK_BUILD_MODE, Lng.str("Remove"), Hos.MOUSE, ContextFilter.IMPORTANT);
				}
			}
		}


	}

	private void addAstronautOptions() {


		addHelper(KeyboardMappings.SPAWN_SHIP, Lng.str("Spawn Ship"), Hos.LEFT, ContextFilter.NORMAL);
		addHelper(KeyboardMappings.SPAWN_SPACE_STATION, Lng.str("Spawn Station"), Hos.LEFT, ContextFilter.NORMAL);

		if(currentPiece != null) {
			addHelper(KeyboardMappings.SIT_ASTRONAUT, Lng.str("Sit"), Hos.LEFT, ContextFilter.TRIVIAL);
		}

		addBuildAndTake();

		if(currentPiece != null && state.getCharacter() != null) {
			if(state.getCharacter().getGravity() == null || state.getCharacter().getGravity().source != currentPiece.getSegmentController()) {
				addHelper(KeyboardMappings.GRAPPLING_HOOK, Lng.str("Align to target"), Hos.MOUSE, ContextFilter.TRIVIAL);
			}
		}

		addBlockOption();

		InventorySlot slot = state.getPlayer().getInventory().getSlot(state.getPlayer().getSelectedBuildSlot());
		if(slot != null && slot.isMetaItem()) {
			//possible meta item action
		}
	}

	private boolean isSelectable(ElementInformation currentInfo) {
		return
				ElementKeyMap.getFactorykeyset().contains(currentInfo.getId()) ||
						ElementKeyMap.isInventory(currentInfo.getId()) ||
						currentInfo.getControlling().size() > 0;

	}

	private void addBlockOption() {
		if(currentPiece == null || !ElementKeyMap.isValidType(currentPiece.getType())) {
			return;
		}
		ElementInformation currentInfo = ElementKeyMap.getInfoFast(currentPiece.getType());
		InputType t = InputType.KEYBOARD;
		Hos pos = Hos.MOUSE;
		SegmentPiece sBlock = getPlayerInteractionManager().getSelectedBlockByActiveController();
		if(currentInfo.getId() == ElementKeyMap.LOGIC_REMOTE_INNER) {

			long index = currentPiece.getAbsoluteIndexWithType4();
			String tx = currentPiece.getSegmentController().getTextMap().get(index);

			if(tx == null) {
				((ClientSegmentProvider) currentPiece.getSegmentController().getSegmentProvider()).getSendableSegmentProvider().clientTextBlockRequest(index);
				tx = "";
			} else {

			}
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Edit name. '%s'", tx), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.getId() == ElementKeyMap.SHOP_BLOCK_ID) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("View Shop"), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.getId() == ElementKeyMap.RACE_GATE_CONTROLLER) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("View Races"), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.getId() == ElementKeyMap.THRUSTER_ID) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Calibrate"), pos, ContextFilter.CRUCIAL);
		} else if(currentPiece != null && currentPiece.getSegmentController() instanceof ManagedShop && currentPiece.getType() == ElementKeyMap.DECORATIVE_PANEL_1 && currentPiece.getAbsolutePosY() == 267) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Spawn Shop Keep"), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.isEnterable() && !getPlayerInteractionManager().isInAnyStructureBuildMode()) {
			addHelper(KeyboardMappings.ENTER_SHIP, Lng.str("Enter %s", currentInfo.getName()), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.isSignal()) {
			String active = currentPiece.isActive() ? Lng.str("On") : Lng.str("Off");
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Activate (%s)", active), pos, ContextFilter.CRUCIAL);
		} else if(ElementInformation.isMedical(currentInfo.getId())) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Heal"), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.isInventory()) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Open"), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.getId() == ElementKeyMap.WARP_GATE_CONTROLLER) {
			if(currentPiece.getSegmentController().getConfigManager().apply(StatusEffectType.WARP_FREE_TARGET, false)) {
				addHelper(KeyboardMappings.ACTIVATE, Lng.str("Enter Warp Target"), pos, ContextFilter.CRUCIAL);
			}

		} else if(currentInfo.canActivate() /*&& activeBuildController == null*/ && currentInfo.getControlledBy().size() > 0 && !currentInfo.getControlledBy().contains(ElementKeyMap.CORE_ID)) {
			if(currentPiece.isActive()) {
				addHelper(KeyboardMappings.ACTIVATE, Lng.str("CURRENT Output"), pos, ContextFilter.CRUCIAL);
			} else {
				addHelper(KeyboardMappings.ACTIVATE, Lng.str("Set as weapon output"), pos, ContextFilter.CRUCIAL);
			}
		} else if(currentInfo.getId() == ElementKeyMap.POWER_BATTERY || currentInfo.getId() == ElementKeyMap.POWER_ID_OLD || currentInfo.getId() == ElementKeyMap.POWER_CAP_ID ||
				currentInfo.getId() == ElementKeyMap.SHIELD_CAP_ID || currentInfo.getId() == ElementKeyMap.SHIELD_REGEN_ID) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Status"), pos, ContextFilter.CRUCIAL);
		} else if(currentInfo.canActivate()) {
			addHelper(KeyboardMappings.ACTIVATE, Lng.str("Activate"), pos, ContextFilter.CRUCIAL);
		}


		if(isSelectable(currentInfo)) {
			if(sBlock != null) {
				if(!sBlock.equals(currentPiece)) {
					addHelper(KeyboardMappings.SELECT_MODULE, Lng.str("Start Connection"), pos, ContextFilter.CRUCIAL);
				} else {
					addHelper(KeyboardMappings.SELECT_MODULE, Lng.str("Deselect"), pos, ContextFilter.CRUCIAL);
				}
			} else {
				addHelper(KeyboardMappings.SELECT_MODULE, Lng.str("Start Connection"), pos, ContextFilter.CRUCIAL);
			}
		}
		if(sBlock != null && !sBlock.equals(currentPiece) && ElementKeyMap.isValidType(sBlock.getType()) && ElementInformation.canBeControlled(sBlock.getType(), currentPiece.getType())) {
			boolean controlling = sBlock.getSegmentController().getControlElementMap().isControlling(
					sBlock.getAbsoluteIndex(),
					currentPiece.getAbsoluteIndex(), currentPiece.getType());
			if(controlling) {
				addHelper(KeyboardMappings.CONNECT_MODULE, Lng.str("Disconnect from %s", ElementKeyMap.getInfo(sBlock.getType()).getName()), pos, ContextFilter.NORMAL);
			} else {
				short fromType = sBlock.getType();
				short toType = currentPiece.getType();


				if(ElementKeyMap.getInfoFast(fromType).isLightConnect(toType)) {
					addHelper(KeyboardMappings.CONNECT_MODULE, Lng.str("Use %s as tint for %s", ElementKeyMap.getInfo(currentPiece.getType()).getName(), ElementKeyMap.getInfo(sBlock.getType()).getName()), pos, ContextFilter.CRUCIAL);
				} else if(
						(ElementKeyMap.getInfoFast(fromType).isMainCombinationControllerB() && ElementKeyMap.getInfoFast(toType).isMainCombinationControllerB()) ||
								(ElementKeyMap.getInfoFast(fromType).isSupportCombinationControllerB() && ElementKeyMap.getInfoFast(toType).isMainCombinationControllerB())) {
					addHelper(KeyboardMappings.CONNECT_MODULE, Lng.str("Slave to %s", ElementKeyMap.getInfo(sBlock.getType()).getName()), pos, ContextFilter.CRUCIAL);
				} else {
					addHelper(KeyboardMappings.CONNECT_MODULE, Lng.str("Connect to %s", ElementKeyMap.getInfo(sBlock.getType()).getName()), pos, ContextFilter.CRUCIAL);
				}
			}
		}
	}

	private void addFlightMode(Timer timer) {

		addHelper(KeyboardMappings.SELECT_LOOK_ENTITY, Lng.str("Select Entity"), Hos.LEFT, ContextFilter.NORMAL);
		addHelper(KeyboardMappings.CHANGE_SHIP_MODE, Lng.str("Build Mode"), Hos.LEFT, ContextFilter.NORMAL);
		addHelper(KeyboardMappings.ENTER_SHIP, Lng.str("Exit"), Hos.LEFT, ContextFilter.NORMAL);
		addHelper(KeyboardMappings.BRAKE, Lng.str("Stop"), Hos.LEFT, ContextFilter.NORMAL);
		addHelper(KeyboardMappings.ALIGN_SHIP, Lng.str("Align"), Hos.LEFT, ContextFilter.NORMAL);

		if(!state.getPlayer().getCockpit().isCore()) {
			addHelper(KeyboardMappings.ADJUST_COCKPIT, Lng.str("Adjust Cockpit"), Hos.LEFT, ContextFilter.NORMAL);
			addHelper(KeyboardMappings.ADJUST_COCKPIT_RESET, Lng.str("Reset Cockpit"), Hos.LEFT, ContextFilter.NORMAL);
		}

		if(state.getPlayer().getCockpit().isInCockpitAdjustment()) {
			addHelper(KeyboardMappings.ADJUST_COCKPIT, Lng.str("Save Cockpit Pos"), Hos.LEFT, ContextFilter.NORMAL);
			addHelper(KeyboardMappings.ADJUST_COCKPIT_RESET, Lng.str("Reset Adjustment"), Hos.LEFT, ContextFilter.NORMAL);
		}

		if(getPlayerInteractionManager().getSelectedEntity() != null) {
			addHelper(KeyboardMappings.PIN_AI_TARGET, Lng.str("Pin as AI Target"), Hos.LEFT, ContextFilter.NORMAL);
		}
		if(getPlayerInteractionManager().getSelectedAITarget() != null) {
			addInfo(Hos.LEFT, ContextFilter.CRUCIAL, Lng.str("AI Target: %s", getPlayerInteractionManager().getSelectedAITarget().getName()));
		}
		Ship s = state.getShip();
		if(s != null) {
			for(ControllerStateUnit u : state.getPlayer().getControllerState().getUnits()) {
				if(u.playerControllable == s) {
					s.getManagerContainer().addHudConext(u, this, timer);
					break;
				}
			}
		}


	}

	public PlayerInteractionControlManager getPlayerInteractionManager() {
		return state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager();
	}

	private void addBuildModeOptions() {

		if(getPlayerInteractionManager().getSegmentControlManager().getSegmentController() == null) {
			return;
		}
		if(getPlayerInteractionManager().getBuildCommandManager().getCurrent() != null) {
			String[] split = getPlayerInteractionManager().getBuildCommandManager().getCurrent().getInstruction().split("\n");
			for(String s : split) {
				addInfo(Hos.MOUSE, ContextFilter.NORMAL, s);
			}
			addInfo(Hos.MOUSE, ContextFilter.NORMAL, Lng.str("(move mouse wheel to reset build prompt)"));
		}
		final EditableSendableSegmentController c = getPlayerInteractionManager().getSegmentControlManager().getSegmentController();

		short slotType = getPlayerInteractionManager().getSelectedTypeWithSub();
		if(currentPiece != null && ElementKeyMap.isValidType(slotType) && ElementKeyMap.getInfo(slotType).isArmor() && BuildModeDrawer.armorValue.totalArmorValue > 0) {
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Armor Stats from this angle"));
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Block Armor: %s", StringTools.formatSeperated((int) currentPiece.getInfo().getArmorValue())));
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Thickness: %s", BuildModeDrawer.armorValue.typesHit.size()));
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Total Armor: %s", StringTools.formatSeperated((int) BuildModeDrawer.armorValue.armorValueAccumulatedRaw)));
		}
		if(c.getHpController().getHpPercent() < 1.0) {
			addHelper(KeyboardMappings.REBOOT_SYSTEMS, Lng.str("Reboot Systems"), Hos.LEFT, ContextFilter.TRIVIAL);
		}

		if(c instanceof Ship) {
			addHelper(KeyboardMappings.CHANGE_SHIP_MODE, Lng.str("Flight Mode"), Hos.LEFT, ContextFilter.NORMAL);
		}

		if(!getPlayerInteractionManager().isAdvancedBuildMode()) {
			addHelper(KeyboardMappings.BUILD_MODE_FAST_MOVEMENT, Lng.str("fast move"), Hos.LEFT, ContextFilter.NORMAL);
			addHelper(KeyboardMappings.BUILD_MODE_FIX_CAM, Lng.str("Adv. Build Mode"), Hos.LEFT, ContextFilter.NORMAL);
		}

		addBuildAndTake();

		if(c.isUsingPowerReactors()
				&& getPlayerInteractionManager().isInAnyStructureBuildMode()) {

			addReactorInfo(getPlayerInteractionManager().getSelectedTypeWithSub());
		}

		addBlockOption();
	}

	public void onInit() {
		if(init) {
			return;
		}


		init = true;
	}

	private void addReactorInfo(short selectedSlotType) {

		ManagerContainer<?> manCon = ((ManagedSegmentController<?>) getPlayerInteractionManager().getSegmentControlManager().getSegmentController()).getManagerContainer();
		PowerInterface powerInt = manCon.getPowerInterface();

		//Always show some reactor info with any block selected
		if(ElementKeyMap.isReactor(selectedSlotType)) {
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Reactor regen: %s",
					StringTools.formatPointZero(powerInt.getRechargeRatePowerPerSec())));
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Total Stabilization: %s%%",
					StringTools.formatPointZero(powerInt.getStabilizerEfficiencyTotal() * 100.0)));

		}

		if(VoidElementManager.isUsingReactorDistance() && selectedSlotType == ElementKeyMap.REACTOR_STABILIZER && currentPiece != null) {
			addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Stab. Efficiency of block to place: %s%% (%sm / %sm)",
					StringTools.formatPointZero(BuildModeDrawer.currentStabEfficiency * 100f),
					StringTools.formatPointZero(BuildModeDrawer.currentStabDist),
					StringTools.formatPointZero(BuildModeDrawer.currentOptStabDist)
			));
		}

		if(currentPiece != null && manCon.isUsingPowerReactors() && manCon instanceof ShieldContainerInterface) {
			ShieldAddOn sc = ((ShieldContainerInterface) manCon).getShieldAddOn();
			ShieldLocalAddOn sh = sc.getShieldLocalAddOn();


			if(currentPiece.getType() == ElementKeyMap.SHIELD_REGEN_ID || currentPiece.getType() == ElementKeyMap.SHIELD_CAP_ID) {
				ShieldLocal containingShield = sh.getContainingShield(((ShieldContainerInterface) manCon), currentPiece.getAbsoluteIndex());
				if(containingShield != null) {
					addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Max Shields: %s Regen: %s/sec ",
							StringTools.formatSeperated(containingShield.getShieldCapacity()),
							StringTools.formatSeperated(containingShield.rechargePerSecond - containingShield.getShieldUpkeep())
					));
					if(currentPiece.getType() == ElementKeyMap.SHIELD_REGEN_ID) {
						addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Shield Origin: %s [Radius: %sm]",
								ElementCollection.getPosX(containingShield.outputPos) + ", " + ElementCollection.getPosY(containingShield.outputPos) + ", " + ElementCollection.getPosZ(containingShield.outputPos),
								StringTools.formatPointZero(containingShield.radius)));
					} else if(currentPiece.getType() == ElementKeyMap.SHIELD_CAP_ID) {
						addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("In Radius Of Shield: %s",
								containingShield.getPosString()));
					}

					if(containingShield.rechargePerSecond < containingShield.getShieldUpkeep()) {
						addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("WARNING: Needs %s regeneration more to be stable ",
								StringTools.formatPointZero(containingShield.getShieldUpkeep() - containingShield.rechargePerSecond)
						));
					}
				}
			}
		}

		if(currentPiece != null) {
			ManagerModuleCollection<?, ?, ?> manModCol = manCon.getModulesControllerMap().get(currentPiece.getType());
			if(manModCol != null && manModCol.getElementManager() instanceof UsableCombinableControllableElementManager<?, ?, ?, ?>) {
				ControlBlockElementCollectionManager<?, ?, ?> cm = ((UsableCombinableControllableElementManager<?, ?, ?, ?>) manModCol.getElementManager()).getCollectionManagersMap().get(currentPiece.getAbsoluteIndex());
				if(cm != null && cm instanceof DamageDealer) {
					InterEffectSet attackSet = ((DamageDealer) cm).getAttackEffectSet();

					for(int i = 0; i < InterEffectSet.length; i++) {
						InterEffectType t = InterEffectType.values()[i];
						///INSERTED CODE
						//By Ithirahad (as part of effects change)
						//Original: addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, t.shortName.getName(t)+": "+Math.round((attackSet.getStrength(t)/3.0f)*100f)+"%");
						addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, t.shortName.getName(t) + ": " + Math.round(attackSet.getStrength(t) * 100f) + "%");
						///
					}
				}
			}

		}
		if(currentPiece != null && ElementKeyMap.isReactor(currentPiece.getType())) {


			for(StabilizerUnit u : powerInt.getStabilizerCollectionManager().getElementCollections()) {
				if(u.contains(currentPiece.getAbsoluteIndex())) {
					if(VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE) {

						String sm = Math.round(u.smallestAngle * FastMath.RAD_TO_DEG) + "°";
						addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("SmallestAngle: %s   Stabilization: %s   with bonus:  %s x",
								sm,
								StringTools.formatPointZero(u.getStabilization()),
								StringTools.formatPointZero(u.getBonusEfficiency())
						));

						if(!u.isBonusSlot()) {
							addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("No bonus for this group because there are 5 other stabilizers with bigger angles to the reactor!"));
						}
					} else {

						addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Slot: %s Stabilization %s with efficiency %s%%",
								Element.getSideString(u.getReactorSide()),
								StringTools.formatPointZero(u.getStabilization()),
								StringTools.formatPointZero(u.getBonusEfficiency() * 100f)
						));
						if(!u.isBonusSlot()) {
							addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("No bonus for this group because there is a bigger group on the same side of the active reactor!"));
						}
					}
				}
			}
			for(MainReactorUnit u : powerInt.getMainReactors()) {
				if(u.contains(currentPiece.getAbsoluteIndex())) {
					String type = currentPiece.getInfo().getName();
					addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("%s size: %s",
							type,
							u.getNeighboringCollection().size()));
					addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("Chamber Capacity: %s%% / %s%%",
							StringTools.formatPointZero(powerInt.getChamberCapacity() * 100f)
							, "100.0"));

				}
			}

			if(((PowerImplementation) powerInt).getActiveReactor() != null) {
				for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> cham : powerInt.getChambers()) {
					for(ReactorChamberUnit u : cham.getCollectionManager().getElementCollections()) {
						if(u.contains(currentPiece.getAbsoluteIndex())) {
							String type = currentPiece.getInfo().getName();
							int minSize = ((PowerImplementation) powerInt).getActiveReactor().getMinChamberSize();
							int size = u.getNeighboringCollection().size();
							String number = size >= minSize ?
									Lng.str("Functional (size: %s)", StringTools.formatSmallAndBig(size))
									: Lng.str("Needs %s more blocks (size: %s)", StringTools.formatSmallAndBig(minSize - size), StringTools.formatSmallAndBig(size));
							addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, Lng.str("%s: %s",
									type,
									number));
						}

					}
				}
			}

			if(currentPiece.getType() == ElementKeyMap.REACTOR_CONDUIT) {
				Set<ReactorChamberUnit> dist = powerInt.getConnectedChambersToConduit(currentPiece.getAbsoluteIndex());
				String conduitCon;
				if(dist != null) {
					conduitCon = Lng.str("Chambers Connected: ");
					Iterator<ReactorChamberUnit> it = dist.iterator();
					while(it.hasNext()) {
						SegmentPiece p = it.next().getElementCollectionId();
						p.refresh();
						if(p.isValid()) {
							conduitCon += p.getInfo().getName();
						}
						if(it.hasNext()) {
							conduitCon += " -> ";
						}
					}
				} else {
					conduitCon = Lng.str("No Chambers Connected!");
				}
				addInfo(Hos.MOUSE, ContextFilter.CRUCIAL, conduitCon);
			}
		}
	}

	private void addSelectedStats(SegmentPiece selectedBlock) {
		String connectionName = null;

		if(selectedBlock != null && selectedBlock == currentPiece && ElementKeyMap.isValidType(selectedBlock.getType())) {
			ElementInformation info = ElementKeyMap.getInfoFast(selectedBlock.getType());
			if(info.isSignal()) {
				connectionName = Lng.str("Blocks");
			} else if(info.isRailTrack()) {
				connectionName = Lng.str("Blocks");

			}
			if(connectionName != null) {
				FastCopyLongOpenHashSet m = selectedBlock.getSegmentController().getControlElementMap().getControllingMap().getAll().get(selectedBlock.getAbsoluteIndex());
				int amount = m != null ? m.size() : 0;
				addBlock(selectedBlock, Lng.str("Connected %s [%s]", connectionName, amount));
			}

			if(connectionName == null && info.getControlling().size() > 0) {
				for(short controlled : info.getControlling()) {
					if(ElementKeyMap.isValidType(controlled)) {
						ElementInformation cInfo = ElementKeyMap.getInfoFast(controlled);
						connectionName = cInfo.getName();
					}
					if(connectionName != null) {
						Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> sm = selectedBlock.getSegmentController().getControlElementMap().getControllingMap().get(selectedBlock.getAbsoluteIndex());
						if(sm != null) {
							FastCopyLongOpenHashSet m = sm.get(controlled);
							int amount = m != null ? m.size() : 0;
							addBlock(selectedBlock, Lng.str("Connected %s [%s]", connectionName, amount));
						}
					}
				}
			}
		}
//		if(currentPiece != null && currentPiece.getAbsoluteIndex() == BuildModeDrawer.currentPieceIndexIntegrity){
//			String intStr;
//			if(BuildModeDrawer.currentPieceIntegrity >= VoidElementManager.INTEGRITY_MARGIN){
//				intStr = Lng.str("OK (%s)", String.valueOf(Math.round(BuildModeDrawer.currentPieceIntegrity)));
//			}else{
//				intStr = Lng.str("INSTABLE (%s) (WARNING: might collapse on damage)", String.valueOf(Math.round(BuildModeDrawer.currentPieceIntegrity)));
//			}
//			addBlock(selectedBlock, Lng.str("Structural Integrity: %s",intStr));
//		}
	}

	public void update(Timer timer) {
		queueMouse.clear();
		queueLeft.clear();
		queueBlock.clear();

		addHelper(KeyboardMappings.RADIAL_MENU, Lng.str("Options"), Hos.LEFT, ContextFilter.IMPORTANT);
		addHelper(KeyboardMappings.MAP_PANEL, Lng.str("Map"), Hos.LEFT, ContextFilter.IMPORTANT);
		this.currentPiece = BuildModeDrawer.currentPiece;
		PlayerInteractionControlManager ppi = getPlayerInteractionManager();

		if(ppi.isInAnyBuildMode()) {
			addSelectedStats(ppi.getSelectedBlockByActiveController());
		}
		if(ppi.isInAnyStructureBuildMode()) {
			addBuildModeOptions();
		} else if(ppi.getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActive()) {
			addFlightMode(timer);
		} else if(ppi.isInAnyCharacterBuildMode()) {
			addAstronautOptions();
		}
	}


	public GameClientState getState() {
		return state;
	}

	private void setLeftStartPosY(int posY) {
		this.leftStartPosY = posY;
	}

	public int getLeftStartPosY() {
		return leftStartPosY;
	}

	private void setLeftEndPosY(int leftEndPosY) {
		this.leftEndPosY = leftEndPosY;
	}

	public int getLeftEndPosY() {
		return leftEndPosY;
	}

}
