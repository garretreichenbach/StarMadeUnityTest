package org.schema.game.common.data.element.quarters;

import api.common.GameClient;
import org.schema.game.client.view.AreaDefineDrawer;
import org.schema.game.client.view.gui.crew.quarters.DefineAreaDialog;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.player.PlayerState;
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
public class CargoQuarter extends Quarter {
	public CargoQuarter(SegmentController s) {
		super(s);
	}

	@Override
	public QuarterType getType() {
		return QuarterType.CARGO;
	}

	@Override
	public int getMaxDim() {
		return 100;
	}

	@Override
	public int getMinCrew() {
		return 0;
	}

	@Override
	public int getMaxCrew() {
		return 0;
	}

	@Override
	public void update(Timer timer) {

	}

	@Override
	public void forceUpdate() {

	}

	@Override
	public ConfigGroup createConfigGroup() {
		return new ConfigGroup("crew - cargo");
	}

	@Override
	public ConfigGroup createDamagedConfigGroup() {
		return new ConfigGroup("crew - cargo damaged");
	}

	@Override
	public GUIMainWindow createGUI(final SegmentPiece segmentPiece, PlayerState playerState, DialogInput dialogInput) {
		GUIMainWindow panel = new GUIMainWindow(GameClient.getClientState(), 750, 500, "CARGO") {
			@Override
			public void onInit() {
				super.onInit();
				final GUIContentPane contentPane = addTab(Lng.str("CARGO"));

				//Button Pane
				GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 0, 1, contentPane.getContent(0));
				buttonPane.onInit();
				buttonPane.addButton(0, 0, Lng.str("DEFINE AREA"), GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							AreaDefineDrawer.startAreaDefine(CargoQuarter.this, segmentPiece);
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
				contentPane.setTextBoxHeightLast((int) buttonPane.getHeight());
				contentPane.getContent(0).attach(buttonPane);

				//Settings Pane
				contentPane.addNewTextBox((int) (getHeight() - (int) buttonPane.getHeight()));
				//Todo: Add settings pane
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
}
