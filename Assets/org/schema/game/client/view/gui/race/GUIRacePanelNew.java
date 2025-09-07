package org.schema.game.client.view.gui.race;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.activities.RaceManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

public class GUIRacePanelNew extends GUIInputPanel {

	private RaceManager messageController;

	private SegmentPiece openedOn;

	public GUIRacePanelNew(InputState state, int width, int height, GUICallback guiCallback) {
		super("GUIRacePanelNew", state, width, height, guiCallback, Lng.str("Race"), "");
		messageController = ((GameClientState) getState()).getRaceManager();
		setOkButton(false);
	}

	GUIActivatableTextBar buyInBar;

	private int laps = 1;

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 2, ((GUIDialogWindow) background).getMainContentPane().getContent(0));
		p.onInit();
		p.activeInterface = GUIRacePanelNew.this::isActive;
		p.addButton(0, 0, Lng.str("CREATE NEW RACE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameTextInput t = new PlayerGameTextInput("TT_RACE_NA", (GameClientState) getState(), 400, 330, 32, "Race Name", "Enter name for the race!\nThe Race will start at this gate!\nTo start the race, an activation module must send 'true' to this computer.", Lng.str("%s's Race", ((ClientState) getState()).getPlayerName())) {

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
								getState().getController().popupAlertTextMessage(Lng.str("Race name must be more than 2 letters."), 0);
								return false;
							}
							int buyIn = 0;
							try {
								buyIn = Math.abs(Integer.parseInt(buyInBar.getText()));
							} catch (Exception e) {
								if (buyInBar.getText().length() > 0) {
									getState().getController().popupAlertTextMessage(Lng.str("Invalid value for buy-in!\nMust be positive number.\n"), 0);
								}
							}
							messageController.startNewRaceOnClient(openedOn, laps, buyIn, entry);
							return true;
						}

						@Override
						public void onDeactivate() {
						}
					};
					t.getInputPanel().onInit();
					GUITextOverlayTable lapText = new GUITextOverlayTable(getState());
					lapText.setTextSimple("Laps: ");
					GUIDropDownList dropDown = new GUIDropDownList(getState(), UIScale.getUIScale().scale(44), UIScale.getUIScale().h, UIScale.getUIScale().scale(300), element -> laps = (Integer) element.getContent().getUserPointer());
					for (int i = 1; i <= 90; i++) {
						GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(22), UIScale.getUIScale().scale(22));
						GUITextOverlayTable o = new GUITextOverlayTable(getState());
						o.setTextSimple(String.valueOf(i));
						o.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
						c.attach(o);
						c.setUserPointer(i);
						dropDown.add(new GUIListElement(c, c, getState()));
					}
					buyInBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, "BUY IN (DEFAULT 0)", t.getInputPanel().getContent(), new TextCallback() {

						@Override
						public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
						}

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public void newLine() {
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}
					}, t1 -> t1);
					lapText.setPos(UIScale.getUIScale().scale(5), UIScale.getUIScale().scale(83), 0);
					dropDown.setPos(UIScale.getUIScale().scale(35), UIScale.getUIScale().scale(80), 0);
					t.getInputPanel().getContent().attach(dropDown);
					t.getInputPanel().getContent().attach(lapText);
					buyInBar.setPos(UIScale.getUIScale().scale(5), UIScale.getUIScale().scale(110), 0);
					buyInBar.onInit();
					t.getInputPanel().getContent().attach(buyInBar);
					t.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(624);
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
				return openedOn != null;
			}
		});
		p.addButton(1, 0, Lng.str("JOIN RACE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (messageController.getSelectedRaceClient() != null && !messageController.getSelectedRaceClient().isParticipant(((GameClientState) getState()).getPlayer())) {
						if (messageController.getSelectedRaceClient().getBuyIn() > 0) {
							if (((GameClientState) getState()).getPlayer().getCredits() >= messageController.getSelectedRaceClient().getBuyIn()) {
								PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("GUIRCCONFAD", (GameClientState) getState(), UIScale.getUIScale().scale(330), UIScale.getUIScale().scale(200), Lng.str("Confirm"), Lng.str("This race has a buy-in of %s credits.\nWinnings will be split 60/30/10 for the first 3 places!\nIf there are only 2 racers it's split 80/20.", messageController.getSelectedRaceClient().getBuyIn())) {

									@Override
									public boolean isOccluded() {
										return false;
									}

									@Override
									public void onDeactivate() {
									}

									@Override
									public void pressedOK() {
										if (messageController.getSelectedRaceClient() != null && !messageController.getSelectedRaceClient().isParticipant(getState().getPlayer())) {
											if (getState().getPlayer().getCredits() >= messageController.getSelectedRaceClient().getBuyIn()) {
												messageController.requestJoinOnServer(getState().getPlayer(), messageController.getSelectedRaceClient());
												deactivate();
											} else {
												getState().getController().popupAlertTextMessage(Lng.str("Can't join!\nThis race has a buy-in of %s credits.", messageController.getSelectedRaceClient().getBuyIn()), 0);
											}
										}
									}
								};
								confirm.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(625);
							} else {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't join!\nThis race has a buy-in of %s credits.", messageController.getSelectedRaceClient().getBuyIn()), 0);
							}
						} else {
							messageController.requestJoinOnServer(((GameClientState) getState()).getPlayer(), messageController.getSelectedRaceClient());
						}
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRacePanelNew.this.isActive() && messageController.getSelectedRaceClient() != null && !messageController.getSelectedRaceClient().isParticipant(((GameClientState) getState()).getPlayer());
			}
		});
		p.addButton(0, 1, Lng.str("LEAVE/FOREFEIT RACE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("GUIRCCONF", (GameClientState) getState(), 300, 100, Lng.str("Confirm"), Lng.str("Do you really want to leave/forefeit?")) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							if (messageController.getSelectedRaceClient() != null && messageController.getSelectedRaceClient().isParticipant(getState().getPlayer())) {
								if (messageController.getSelectedRaceClient().isStarted()) {
									messageController.requestForefit(getState().getPlayer(), messageController.getSelectedRaceClient());
								} else {
									messageController.requestLeave(getState().getPlayer(), messageController.getSelectedRaceClient());
								}
								deactivate();
							}
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(626);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return messageController.getSelectedRaceClient() != null && messageController.getSelectedRaceClient().isParticipant(((GameClientState) getState()).getPlayer());
			}
		});
		p.addButton(1, 1, Lng.str("REMOVE RACE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("GUIRCCC", (GameClientState) getState(), 300, 100, Lng.str("Confirm"), Lng.str("Do you really want to remove this race?")) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							if (messageController.getSelectedRaceClient() != null && messageController.getSelectedRaceClient().canEdit(getState().getPlayer())) {
								messageController.requestFinishedOnClient(messageController.getSelectedRaceClient());
							}
							deactivate();
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(627);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return messageController.getSelectedRaceClient() != null && messageController.getSelectedRaceClient().canEdit(((GameClientState) getState()).getPlayer());
			}
		});
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(p);
		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(56));
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIRaceScrollableList l = new GUIRaceScrollableList(getState(), ((GUIDialogWindow) background).getMainContentPane().getContent(1));
		l.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(l);
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1));
		GUIRaceEntrantsScrollableList l2 = new GUIRaceEntrantsScrollableList(getState(), ((GUIDialogWindow) background).getMainContentPane().getContent(2));
		l2.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(2).attach(l2);
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
