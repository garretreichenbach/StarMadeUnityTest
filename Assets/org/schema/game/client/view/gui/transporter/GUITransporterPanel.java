package org.schema.game.client.view.gui.transporter;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerTransporterInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

public class GUITransporterPanel extends GUIInputPanel {

	private TransporterCollectionManager transporter;

	private SegmentPiece openedOn;

	private final PlayerTransporterInput input;

	public GUITransporterPanel(InputState state, int width, int height, final TransporterCollectionManager cc, PlayerTransporterInput guiCallback) {
		super("GUITPPanelNew", state, width, height, guiCallback, (new Object() {

			@Override
			public String toString() {
				return Lng.str("Transporter:") + cc.getTransporterName();
			}
		}), "");
		transporter = cc;
		setOkButton(false);
		this.input = guiCallback;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 2, ((GUIDialogWindow) background).getMainContentPane().getContent(0));
		p.onInit();
		p.activeInterface = GUITransporterPanel.this::isActive;
		p.addButton(0, 0, Lng.str("CHANGE NAME"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameTextInput t = new PlayerGameTextInput("TT_RACE_NAME", (GameClientState) getState(), 32, "Transporter", "Enter name for the transporter") {

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public boolean onInput(String entry) {
							if (entry.length() < 2) {
								setErrorMessage(Lng.str("Must be longer than 2 letters!"));
								return false;
							}
							transporter.setTransporterSettings(entry, transporter.getPublicAccess());
							transporter.sendSettingsUpdate();
							return true;
						}

						@Override
						public void onDeactivate() {
						}
					};
					t.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(719);
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
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
		p.addButton(1, 0, Lng.str("ACTIVATE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(720);
					transporter.sendTransporterUsage();
					input.deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITransporterPanel.this.isActive() && transporter.canUse();
			}
		});
		p.addButton(0, 1, Lng.str("PUBLIC ACCESS"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (transporter.isPublicAccess()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(722);
						transporter.setTransporterSettings(transporter.getTransporterName(), TransporterCollectionManager.PRIVATE_ACCESS);
						transporter.sendSettingsUpdate();
					} else {
						PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("GUITPSCONF", (GameClientState) getState(), 400, 170, Lng.str("Confirm"), Lng.str("Public access means that this transporter can be\ntargeted by any nearby structure.\nYou and the target will also lose all shields on transporting.")) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								transporter.setTransporterSettings(transporter.getTransporterName(), !transporter.isPublicAccess() ? TransporterCollectionManager.PUBLIC_ACCESS : TransporterCollectionManager.PRIVATE_ACCESS);
								transporter.sendSettingsUpdate();
								deactivate();
							}
						};
						confirm.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(721);
					}
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITransporterPanel.this.isActive();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return transporter.isPublicAccess();
			}
		});
		p.addButton(1, 1, Lng.str("FACTION ACCESS"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (transporter.isFactionAccess()) {
						transporter.setTransporterSettings(transporter.getTransporterName(), TransporterCollectionManager.PRIVATE_ACCESS);
						transporter.sendSettingsUpdate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(724);
					} else {
						PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("GUITPSCONF", (GameClientState) getState(), 400, 170, Lng.str("Confirm"), Lng.str("Faction access means that this transporter can be\ntargeted by a nearby structure of the same faction.\nYou and the target will also lose all shields on transporting.")) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								transporter.setTransporterSettings(transporter.getTransporterName(), !transporter.isFactionAccess() ? TransporterCollectionManager.FACTION_ACCESS : TransporterCollectionManager.PRIVATE_ACCESS);
								transporter.sendSettingsUpdate();
								deactivate();
							}
						};
						confirm.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(723);
					}
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITransporterPanel.this.isActive();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return transporter.isFactionAccess();
			}
		});
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(p);
		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(56));
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(66));
		GUITextOverlay info = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		info.setTextSimple(new Object() {

			@Override
			public String toString() {
				StateInterface state = (StateInterface) getState();
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(transporter.getDestinationUID());
				String toName = transporter.getDestinationUID().equals("none") ? Lng.str("not set") : Lng.str("Not in range!");
				String toBlock = transporter.getDestinationUID().equals("none") ? "" : transporter.getDestinationBlock().toStringPure();
				if (sendable != null && sendable instanceof SegmentController) {
					toName = ((SegmentController) sendable).getName();
					TransporterModuleInterface r = (TransporterModuleInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer();
					TransporterCollectionManager tt = r.getTransporter().getCollectionManagersMap().get(ElementCollection.getIndex(transporter.getDestinationBlock()));
					if (tt != null) {
						toBlock = tt.getTransporterName();
					}
				}
				return Lng.str("Name: %s\nTarget Structure: %s;\nTarget Transporter: %s", transporter.getTransporterName(), toName, toBlock);
			}
		});
		info.setPos(2, 2, 0);
		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(info);
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUITrasporterDestinationsScrollableList l = new GUITrasporterDestinationsScrollableList(getState(), ((GUIDialogWindow) background).getMainContentPane().getContent(2), transporter);
		l.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(2).attach(l);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == this) && super.isActive();
	}

	public SegmentPiece getOpenedOn() {
		return openedOn;
	}

	public void setOpenedOn(SegmentPiece openedOn) {
		this.openedOn = openedOn;
	}
}
