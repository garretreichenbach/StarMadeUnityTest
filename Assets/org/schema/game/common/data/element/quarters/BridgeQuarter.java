package org.schema.game.common.data.element.quarters;

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.AreaDefineDrawer;
import org.schema.game.client.view.gui.crew.quarters.CrewAssignmentPanel;
import org.schema.game.client.view.gui.crew.quarters.DefineAreaDialog;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.EffectConfigElement;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.blockeffects.config.elements.ModifierStackType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectFloatValue;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.tag.Tag;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class BridgeQuarter extends Quarter {
	public BridgeQuarter(SegmentController s) {
		super(s);
	}

	@Override
	public QuarterType getType() {
		return QuarterType.BRIDGE;
	}

	@Override
	public int getMaxDim() {
		return 50;
	}

	@Override
	public int getMinCrew() {
		return 2;
	}

	@Override
	public int getMaxCrew() {
		return 10;
	}

	@Override
	public void update(Timer timer) {
		applyEffects();
	}

	@Override
	public void forceUpdate() {
		applyEffects();
	}

	@Override
	public ConfigGroup createConfigGroup() {
		ConfigGroup configGroup = new ConfigGroup("crew - bridge");
		{ //AI Turret Accuracy
			EffectConfigElement effectConfigElement = new EffectConfigElement();
			effectConfigElement.init(StatusEffectType.AI_ACCURACY_TURRET);
			effectConfigElement.priority = 3;
			effectConfigElement.weaponType = null;
			effectConfigElement.stackType = ModifierStackType.ADD;
			StatusEffectFloatValue value = new StatusEffectFloatValue();
			value.value.set(getEngineeringEfficiency());
			effectConfigElement.value = value;
			configGroup.elements.add(effectConfigElement);
		}
		{ //AI Drone Accuracy
			EffectConfigElement effectConfigElement = new EffectConfigElement();
			effectConfigElement.init(StatusEffectType.AI_ACCURACY_DRONE);
			effectConfigElement.priority = 3;
			effectConfigElement.weaponType = null;
			effectConfigElement.stackType = ModifierStackType.ADD;
			StatusEffectFloatValue value = new StatusEffectFloatValue();
			value.value.set(getEngineeringEfficiency());
			effectConfigElement.value = value;
			configGroup.elements.add(effectConfigElement);
		}
		{ //AI PD Accuracy
			EffectConfigElement effectConfigElement = new EffectConfigElement();
			effectConfigElement.init(StatusEffectType.AI_ACCURACY_POINT_DEFENSE);
			effectConfigElement.priority = 3;
			effectConfigElement.weaponType = null;
			effectConfigElement.stackType = ModifierStackType.ADD;
			StatusEffectFloatValue value = new StatusEffectFloatValue();
			value.value.set(getEngineeringEfficiency());
			effectConfigElement.value = value;
			configGroup.elements.add(effectConfigElement);
		}
		return configGroup;
	}

	@Override
	public ConfigGroup createDamagedConfigGroup() {
		ConfigGroup configGroup = new ConfigGroup("crew - bridge damaged");
		{ //AI Turret Accuracy
			EffectConfigElement effectConfigElement = new EffectConfigElement();
			effectConfigElement.init(StatusEffectType.AI_ACCURACY_TURRET);
			effectConfigElement.priority = 3;
			effectConfigElement.weaponType = null;
			effectConfigElement.stackType = ModifierStackType.ADD;
			StatusEffectFloatValue value = new StatusEffectFloatValue();
			value.value.set(getEngineeringEfficiency() * 0.5f);
			effectConfigElement.value = value;
			configGroup.elements.add(effectConfigElement);
		}
		{ //AI Drone Accuracy
			EffectConfigElement effectConfigElement = new EffectConfigElement();
			effectConfigElement.init(StatusEffectType.AI_ACCURACY_DRONE);
			effectConfigElement.priority = 3;
			effectConfigElement.weaponType = null;
			effectConfigElement.stackType = ModifierStackType.ADD;
			StatusEffectFloatValue value = new StatusEffectFloatValue();
			value.value.set(getEngineeringEfficiency() * 0.5f);
			effectConfigElement.value = value;
			configGroup.elements.add(effectConfigElement);
		}
		{ //AI PD Accuracy
			EffectConfigElement effectConfigElement = new EffectConfigElement();
			effectConfigElement.init(StatusEffectType.AI_ACCURACY_POINT_DEFENSE);
			effectConfigElement.priority = 3;
			effectConfigElement.weaponType = null;
			effectConfigElement.stackType = ModifierStackType.ADD;
			StatusEffectFloatValue value = new StatusEffectFloatValue();
			value.value.set(getEngineeringEfficiency() * 0.5f);
			effectConfigElement.value = value;
			configGroup.elements.add(effectConfigElement);
		}
		return configGroup;
	}

	@Override
	public GUIMainWindow createGUI(SegmentPiece segmentPiece, PlayerState playerState, DialogInput dialogInput) {
		GUIMainWindow panel = new GUIMainWindow(GameClient.getClientState(), 750, 500, "BRIDGE") {
			@Override
			public void onInit() {
				super.onInit();
				GUIContentPane contentPane = addTab(Lng.str("BRIDGE"));
				//Button Pane
				GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 1, contentPane.getContent(0));
				buttonPane.onInit();
				buttonPane.addButton(0, 0, Lng.str("ASSIGN CREW"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse() && hasAnyCrewAvailable()) {
							(new CrewAssignmentPanel(getState(), BridgeQuarter.this)).activate();
						}
					}

					@Override
					public boolean isOccluded() {
						return !hasAnyCrewAvailable();
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return hasAnyCrewAvailable();
					}
				});
				buttonPane.addButton(1, 0, Lng.str("DEFINE AREA"), GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							AreaDefineDrawer.startAreaDefine(BridgeQuarter.this, segmentPiece);
							(new DefineAreaDialog(getState())).activate();
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				buttonPane.addButton(2, 0, Lng.str("ENTER"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							GameClient.getClientPlayerState().getControllerState().forcePlayerOutOfSegmentControllers();
							GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().setEntered(segmentPiece);
							GameClient.getClientState().getController().requestControlChange(GameClient.getClientPlayerState().getAssingedPlayerCharacter(), (PlayerControllable) getSegmentController(), new Vector3i(), segmentPiece.getAbsolutePos(new Vector3i()), true);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				contentPane.setTextBoxHeightLast((int) buttonPane.getHeight());
				contentPane.getContent(0).attach(buttonPane);
				//Settings Pane
				contentPane.addNewTextBox((int) (getHeight() - (int) buttonPane.getHeight()));
				//Todo
			}
		};
		panel.setCallback(dialogInput);
		panel.onInit();
		return panel;
	}

	@Override
	public Tag toTagExtra() {
		return null;
	}

	@Override
	public void fromTagExtra(Tag tag) {
	}

	private SegmentPiece getAvailableBuildBlock() {
		ManagerContainer<?> managerContainer = null;
		if(getSegmentController().getType().equals(SimpleTransformableSendableObject.EntityType.SHIP)) managerContainer = ((Ship) getSegmentController()).getManagerContainer();
		else if(getSegmentController().getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION)) managerContainer = ((SpaceStation) getSegmentController()).getManagerContainer();
		if(managerContainer != null && managerContainer.getBuildBlocks().size() > 0) return getSegmentController().getSegmentBuffer().getPointUnsave(managerContainer.getBuildBlocks().toLongArray()[0]);
		else return null;
	}
}
