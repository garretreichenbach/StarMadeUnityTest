package org.schema.game.client.view.gui.crew;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.ai.*;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.element.quarters.crew.CrewMember;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class AICrewScrollableListNew extends ScrollableTableList<AiInterfaceContainer> {

	public static final int AVAILABLE = 0;

	public static final int PERSONAL = 1;

	public static final int ADMIN = 2;

	public AICrewScrollableListNew(InputState state, GUIElement p, float width, float height) {
		super(state, width, height, p);
		getState().getPlayer().getPlayerAiManager().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		getState().getPlayer().getPlayerAiManager().deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 7, (o1, o2) -> {
			String a;
			String b;
			try {
				if(o1 instanceof CrewMember) a = ((CrewMember) o1).getName();
				else a = o1.getRealName();
			} catch (UnloadedAiEntityException e) {
				a = Lng.str("Unknown");
			}
			try {
				if(o2 instanceof CrewMember) b = ((CrewMember) o2).getName();
				else b = o2.getRealName();
			} catch (UnloadedAiEntityException e) {
				b = Lng.str("Unknown");
			}
			return a.compareToIgnoreCase(b);
		});
		addColumn(Lng.str("Last Known Sector"), 3, (o1, o2) -> {
			Vector3i a;
			try {
				a = o1.getLastKnownSector();
			} catch (UnloadedAiEntityException e) {
				a = new Vector3i(0, 0, 0);
			}
			Vector3i b;
			try {
				b = o2.getLastKnownSector();
			} catch (UnloadedAiEntityException e) {
				b = new Vector3i(0, 0, 0);
			}
			Vector3i ownPos = new Vector3i(getState().getPlayer().getCurrentSector());
			double dist1 = a != null ? Vector3i.getDisatance(ownPos, a) : Integer.MAX_VALUE - 100;
			double dist2 = b != null ? Vector3i.getDisatance(ownPos, b) : Integer.MAX_VALUE - 100;
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		});
		addTextFilter(new GUIListFilterText<AiInterfaceContainer>() {

			@Override
			public boolean isOk(String input, AiInterfaceContainer listElement) {
				try {
					return listElement.getRealName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
				} catch (UnloadedAiEntityException e) {
					return false;
				}
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<AiInterfaceContainer> getElementList() {
		return getState().getPlayer().getPlayerAiManager().getCrew();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<AiInterfaceContainer> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final AiInterfaceContainer f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable posText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					try {
						return f.getRealName();
					} catch (UnloadedAiEntityException e) {
						return f.getUID() + Lng.str("(UNLOADED)");
					}
				}
			});
			posText.setTextSimple(new Object() {

				@Override
				public String toString() {
					try {
						if (f.getLastKnownSector() != null) {
							return String.valueOf(f.getLastKnownSector().toStringPure());
						} else {
							return Lng.str("unknown");
						}
					} catch (UnloadedAiEntityException e) {
						return Lng.str("unknown (unloaded)");
					}
				}
			});
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			posText.getPos().y = heightInset;
			FactionRow r = new FactionRow(getState(), f, nameText, posText);
			r.expanded = new GUIElementList(getState());
			GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
			description.setTextSimple(new Object() {

				@Override
				public String toString() {
					try {
						return Lng.str("Crew member: %s", f.getRealName());
					} catch (UnloadedAiEntityException e) {
						return Lng.str("Crew member: %s (UNLOADED)", f.getUID());
					}
				}
			});
			description.setPos(4, 2, 0);
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);
			GUITextButton renameButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("RENAME"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(437);
						rename(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton deleteButton = new GUITextButton(getState(), 130, 24, ColorPalette.CANCEL, Lng.str("DELETE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(438);
						delete(f);
					}
				}
			});
			GUITextButton followMeButton = new GUITextButton(getState(), 130, 24, ColorPalette.OK, Lng.str("FOLLOW ME"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(439);
						follow(f);
					}
				}
			});
			GUITextButton idleButton = new GUITextButton(getState(), 100, 24, ColorPalette.OK, Lng.str("IDLE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(440);
						idle(f);
					}
				}
			});
			c.attach(renameButton);
			c.attach(deleteButton);
			c.attach(followMeButton);
			c.attach(idleButton);
			renameButton.setPos(0, c.getHeight(), 0);
			deleteButton.setPos(90, c.getHeight(), 0);
			followMeButton.setPos(renameButton.getWidth() + 10 + deleteButton.getWidth() + 10, c.getHeight(), 0);
			idleButton.setPos(renameButton.getWidth() + 10 + deleteButton.getWidth() + 10 + followMeButton.getWidth() + 10, c.getHeight(), 0);
			c.attach(description);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getState()
	 */
	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	public boolean isPlayerAdmin() {
		return getState().getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(getState().getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	private void rename(final AiInterfaceContainer ai) {
		final String realName;
		try {
			realName = ai.getRealName();
		} catch (UnloadedAiEntityException e) {
			getState().getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
			return;
		}
		PlayerGameTextInput playerTextInput = new PlayerGameTextInput("AICrewScrollableListNew_RENAME", getState(), 32, Lng.str("Rename"), Lng.str("Enter a new name (must be at least 3 letters)"), realName) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean onInput(String entry) {
				String enteredName = entry.trim();
				if (enteredName.length() < 3) {
					setErrorMessage(Lng.str("Name to short"));
					return false;
				}
				System.err.println("[DIALOG] APPLYING AI NAME CHANGE: " + entry + ": from " + realName + " to " + enteredName + "; changed? " + realName.equals(enteredName));
				try {
					if (!realName.equals(enteredName)) {
						if (ai.getAi() instanceof SendableSegmentController) {
							SendableSegmentController c = (SendableSegmentController) ai.getAi();
							System.err.println("[CLIENT] sending name for object: " + c + ": " + enteredName);
							c.getNetworkObject().realName.set(enteredName, true);
							assert (c.getNetworkObject().realName.hasChanged());
							assert (c.getNetworkObject().isChanged());
						} else if (ai.getAi() instanceof AICreature<?>) {
							AICreature<?> c = (AICreature<?>) ai.getAi();
							System.err.println("[CLIENT] sending name for object: " + c + ": " + enteredName);
							c.getNetworkObject().realName.set(enteredName, true);
							assert (c.getNetworkObject().realName.hasChanged());
							assert (c.getNetworkObject().isChanged());
						}
					}
				} catch (UnloadedAiEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		};
		playerTextInput.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(441);
	}

	private void delete(AiInterfaceContainer ai) {
		System.err.println(ai + " delete ");
		getState().getPlayer().getPlayerAiManager().removeAI(ai);
	}

	private void idle(AiInterfaceContainer ai) {
		System.err.println(ai + " idle ");
		try {
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		} catch (UnloadedAiEntityException e) {
			getState().getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
			e.printStackTrace();
		}
	}

	private void follow(AiInterfaceContainer ai) {
		System.err.println(ai + " follow ");
		try {
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.FOLLOW_TARGET).switchSetting(getState().getPlayer().getFirstControlledTransformable().getUniqueIdentifier(), true);
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_FOLLOWING, true);
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		} catch (PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		} catch (UnloadedAiEntityException e) {
			e.printStackTrace();
			getState().getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
		}
	}

	private class FactionRow extends Row {

		public FactionRow(InputState state, AiInterfaceContainer f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
