package org.schema.game.client.view.gui.thrustmanagement;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.client.controller.PlayerThrustManagerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ThrustConfiguration;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIPolygonStats;
import org.schema.schine.graphicsengine.forms.gui.newgui.PolygonStatsInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import PolygonStatsInterface.PolygonStatsEditableInterface;

public class ThrustManagementPanelNew extends GUIMainWindow implements GUIActiveInterface, PolygonStatsInterface, PolygonStatsEditableInterface {

	private PlayerThrustManagerInput mainMenu;

	// private GUIContentPane personalTab;
	private boolean init;

	private GUIContentPane generalTab;

	private GUIPolygonStats st;

	private final Ship ship;

	private final ThrustConfiguration tc;

	public ThrustManagementPanelNew(InputState state, PlayerThrustManagerInput mainMenu, Ship ship) {
		super(state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(500), "TPANNA");
		this.mainMenu = mainMenu;
		this.ship = ship;
		if (ship != null) {
			tc = ship.getManagerContainer().thrustConfiguration;
		} else {
			tc = null;
		}
	}

	@Override
	public void cleanUp() {
		generalTab.cleanUp();
		st.cleanUp();
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		super.draw();
	}

	@Override
	public void update(Timer timer) {
		if (st != null) {
			st.update();
		}
	}

	@Override
	public void onInit() {
		super.onInit();
		recreateTabs();
		orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		init = true;
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (getSelectedTab() < getTabs().size()) {
			beforeTab = getTabs().get(getSelectedTab()).getTabName();
		}
		clearTabs();
		generalTab = addTab(Lng.str("GENERAL"));
		createGeneralPane();
		if (beforeTab != null) {
			for (int i = 0; i < getTabs().size(); i++) {
				if (getTabs().get(i).getTabName().equals(beforeTab)) {
					setSelectedTab(i);
					break;
				}
			}
		}
	}

	private void createGeneralPane() {
		generalTab.setTextBoxHeightLast(28 + 32 + 24 + 30 + 24 + 24 + 24);
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 2, 1, Lng.str("Thrust Management"), generalTab.getContent(0));
		buttons.onInit();
		generalTab.getContent(0).attach(buttons);
		buttons.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("AUTO-DAMPENERS");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(697);
					ship.requestAutomaticDampeners(!ship.isAutomaticDampeners());
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				// System.err.println("AUTO D "+ship.isAutomaticDampeners());
				return ship != null && ship.isAutomaticDampeners();
			}
		});
		buttons.addButton(1, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("REACTIVATE DAMPING ON EXIT");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(698);
					ship.requestAutomaticDampenersReactivate(!ship.isAutomaticReactivateDampeners());
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return ship != null && ship.isAutomaticReactivateDampeners();
			}
		});
		GUIHorizontalButtonTablePane buttons2 = new GUIHorizontalButtonTablePane(getState(), 4, 1, generalTab.getContent(0));
		buttons2.onInit();
		generalTab.getContent(0).attach(buttons2);
		buttons2.setPos(0, UIScale.getUIScale().scale(28 + 30), 0);
		buttons2.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("RESET AXIS");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(699);
					tc.bufferThrusterBalanceChange(new Vector3f(0.33333f, 0.33333f, 0.33333f), ship.getManagerContainer().getThrusterElementManager().rotationBalance, ship.getManagerContainer().getRepulseManager().getThrustToRepul());
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
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
		buttons2.addButton(1, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("ALL FORW/BACK");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(700);
					tc.bufferThrusterBalanceChange(new Vector3f(0, 0, 1), ship.getManagerContainer().getThrusterElementManager().rotationBalance, ship.getManagerContainer().getRepulseManager().getThrustToRepul());
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
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
		buttons2.addButton(2, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("ALL UP/DOWN");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(701);
					tc.bufferThrusterBalanceChange(new Vector3f(0, 1, 0), ship.getManagerContainer().getThrusterElementManager().rotationBalance, ship.getManagerContainer().getRepulseManager().getThrustToRepul());
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
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
		buttons2.addButton(3, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("ALL RIGHT/LEFT");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(702);
					tc.bufferThrusterBalanceChange(new Vector3f(1, 0, 0), ship.getManagerContainer().getThrusterElementManager().rotationBalance, ship.getManagerContainer().getRepulseManager().getThrustToRepul());
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
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
		GUIHorizontalButtonTablePane buttons3 = new GUIHorizontalButtonTablePane(getState(), 1, 1, generalTab.getContent(0));
		buttons3.onInit();
		generalTab.getContent(0).attach(buttons3);
		buttons3.setPos(0, UIScale.getUIScale().scale(28 + 30 + 25), 0);
		buttons3.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("INHERIT THRUSTERS FROM DOCKS");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && ship != null) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(703);
					ship.requestThrustSharing(!tc.thrustSharing);
				}
			}

			@Override
			public boolean isOccluded() {
				return !mainMenu.isActive();
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ship != null && !ship.railController.next.isEmpty();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return ship != null && !ship.railController.next.isEmpty() && tc.thrustSharing;
			}
		});
		final GUITextOverlay t = new GUITextOverlay(getState());
		t.setTextSimple(new Object() {

			@Override
			public String toString() {
				if (ship != null) {
					return Lng.str("Rotational Thrusters %s%%", String.valueOf(Math.round(tc.queuedThrustBuffer.w * 100f)));
				}
				return Lng.str("n/a");
			}
		});
		GUIScrollSettingSelector rotSlider = new GUIScrollSettingSelector(getState(), GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 180) {

			@Override
			public boolean isVerticalActive() {
				return false;
			}

			@Override
			public boolean showLabel() {
				return false;
			}

			@Override
			public void settingChanged(Object setting) {
				super.settingChanged(setting);
				if (ship != null && setting != null) {
					tc.bufferThrusterBalanceChange(new Vector3f(tc.queuedThrustBuffer.x, tc.queuedThrustBuffer.y, tc.queuedThrustBuffer.z), tc.queuedThrustBuffer.w, tc.queuedRepulsorBuffer);
				}
			}

			@Override
			protected void setSettingY(float value) {
			}

			@Override
			protected void setSettingX(float value) {
				if (ship != null) {
					tc.queuedThrustBuffer.w = value * 0.01f;
					settingChanged(Math.round(tc.queuedThrustBuffer.w * 100f));
				}
			}

			@Override
			protected void incSetting() {
				if (ship != null) {
					tc.queuedThrustBuffer.w += 0.1;
					settingChanged(Math.round(tc.queuedThrustBuffer.w * 100f));
				}
			}

			@Override
			protected float getSettingY() {
				return 0;
			}

			@Override
			protected float getSettingX() {
				if (ship != null) {
					return tc.queuedThrustBuffer.w * 100f;
				}
				return 0;
			}

			@Override
			public float getMaxY() {
				return 0;
			}

			@Override
			public float getMaxX() {
				return 100f;
			}

			@Override
			protected void decSetting() {
				if (ship != null) {
					tc.queuedThrustBuffer.w -= 0.1;
					settingChanged(Math.round(tc.queuedThrustBuffer.w * 100f));
				}
			}

			@Override
			public float getMinX() {
				return 0;
			}

			@Override
			public float getMinY() {
				return 0;
			}

			@Override
			public void draw() {
				this.setWidth((int) (generalTab.getContent(0).getWidth() - (194)));
				super.draw();
			}
		};
		t.setPos(3, 28 + 32 + 24 + 7 + 28, 0);
		rotSlider.setPos(140, 28 + 32 + 23 + 24, 0);
		final GUITextOverlay r = new GUITextOverlay(getState());
		r.setTextSimple(new Object() {

			@Override
			public String toString() {
				if (ship != null && ship.getManagerContainer().hasRepulsors()) {
					return Lng.str("Thrust to Repulsors %s%%", String.valueOf(Math.round(tc.queuedRepulsorBuffer * 100f)));
				}
				return Lng.str("Thrust to Repulsors n/a");
			}
		});
		GUIScrollSettingSelector repulSlider = new GUIScrollSettingSelector(getState(), GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 180) {

			@Override
			public boolean isVerticalActive() {
				return false;
			}

			@Override
			public boolean showLabel() {
				return false;
			}

			@Override
			public void settingChanged(Object setting) {
				super.settingChanged(setting);
				if (ship != null && setting != null) {
					tc.bufferThrusterBalanceChange(new Vector3f(tc.queuedThrustBuffer.x, tc.queuedThrustBuffer.y, tc.queuedThrustBuffer.z), tc.queuedThrustBuffer.w, tc.queuedRepulsorBuffer);
				}
			}

			@Override
			protected void setSettingY(float value) {
			}

			@Override
			protected void setSettingX(float value) {
				if (ship != null) {
					tc.queuedRepulsorBuffer = value * 0.01f;
					settingChanged(Math.round(tc.queuedRepulsorBuffer * 100f));
				}
			}

			@Override
			protected void incSetting() {
				if (ship != null) {
					tc.queuedRepulsorBuffer += 0.1;
					settingChanged(Math.round(tc.queuedRepulsorBuffer * 100f));
				}
			}

			@Override
			protected float getSettingY() {
				return 0;
			}

			@Override
			protected float getSettingX() {
				if (ship != null) {
					return tc.queuedRepulsorBuffer * 100f;
				}
				return 0;
			}

			@Override
			public float getMaxY() {
				return 0;
			}

			@Override
			public float getMaxX() {
				return 100f;
			}

			@Override
			protected void decSetting() {
				if (ship != null) {
					tc.queuedRepulsorBuffer -= 0.1;
					settingChanged(Math.round(tc.queuedRepulsorBuffer * 100f));
				}
			}

			@Override
			public float getMinX() {
				return 0;
			}

			@Override
			public float getMinY() {
				return 0;
			}

			@Override
			public void draw() {
				this.setWidth((int) (generalTab.getContent(0).getWidth() - (194)));
				super.draw();
			}
		};
		r.setPos(UIScale.getUIScale().scale(3), UIScale.getUIScale().scale(28 + 32 + 24 + 7 + 28 + 28), 0);
		repulSlider.setPos(UIScale.getUIScale().scale(140), UIScale.getUIScale().scale(28 + 32 + 23 + 24 + 28), 0);
		generalTab.getContent(0).attach(repulSlider);
		generalTab.getContent(0).attach(r);
		generalTab.getContent(0).attach(rotSlider);
		generalTab.getContent(0).attach(t);
		generalTab.addNewTextBox(UIScale.getUIScale().scale(100));
		GUIScrollablePanel p = new GUIScrollablePanel(10, 10, generalTab.getContent(1), getState());
		st = new GUIPolygonStats(getState(), this) {

			@Override
			public void draw() {
				float height = generalTab.getContent(1).getHeight();
				// must be square
				setHeight((int) height);
				setWidth((int) height);
				setPos((int) (generalTab.getContent(1).getWidth() / 2 - (getWidth() / 2)), (int) (generalTab.getContent(1).getHeight() / 2 - (getHeight() / 2)), 0);
				super.draw();
			}
		};
		p.setContent(st);
		st.setEditable(this);
		p.onInit();
		generalTab.getContent(1).attach(p);
	}

	public PlayerState getOwnPlayer() {
		return ThrustManagementPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return ThrustManagementPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public int getDataPointsNum() {
		return 3;
	}

	@Override
	public double getPercent(int i) {
		if (ship != null) {
			return i == 0 ? tc.queuedThrustBuffer.x : i == 1 ? tc.queuedThrustBuffer.y : tc.queuedThrustBuffer.z;
		}
		return 0;
	}

	@Override
	public void setPercent(int i, float perc) {
		if (ship != null) {
			if (i == 0) {
				tc.queuedThrustBuffer.x = 1.0f - perc;
			} else if (i == 1) {
				tc.queuedThrustBuffer.y = 1.0f - perc;
			} else {
				tc.queuedThrustBuffer.z = 1.0f - perc;
			}
			tc.bufferThrusterBalanceChange(new Vector3f(tc.queuedThrustBuffer.x, tc.queuedThrustBuffer.y, tc.queuedThrustBuffer.z), tc.queuedThrustBuffer.w, tc.queuedRepulsorBuffer);
		}
	}

	@Override
	public double getValue(int i) {
		if (ship != null) {
			return i == 0 ? tc.queuedThrustBuffer.x : i == 1 ? tc.queuedThrustBuffer.y : tc.queuedThrustBuffer.z;
		}
		return 0;
	}

	@Override
	public String getValueName(int i) {
		if (ship != null) {
			float rotInv = 1.0f - tc.queuedThrustBuffer.w;
			float repulInv = 1.0f - tc.queuedRepulsorBuffer;
			if (!ship.getManagerContainer().hasRepulsors()) {
				repulInv = 1.0f;
			}
			float thrustScale = Math.max(0f, Math.min(1f, (repulInv + rotInv) / 2));
			return (i == 0 ? Lng.str("Left/Right ") : i == 1 ? Lng.str("Up/Down ") : Lng.str("Forward/Back ")) + FastMath.round(getValue(i) * 100f) + "% (" + FastMath.round(getValue(i) * thrustScale * 100f) + "%)";
		} else {
			return Lng.str("n/a");
		}
	}
}
