package org.schema.game.client.view.gui.faction.newfaction;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerMailInputNew;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FactionScrollableListRelation extends ScrollableTableList<Faction> {

	private Faction faction;

	public FactionScrollableListRelation(InputState state, GUIElement p, Faction faction) {
		super(state, 100, 100, p);
		this.faction = faction;
		((GameClientState) getState()).getFactionManager().obs.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getFactionManager().obs.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 7, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		addColumn(Lng.str("#Players"), 0, (o1, o2) -> o1.getMembersUID().size() - o2.getMembersUID().size());
		addColumn(Lng.str("Relation"), 1, (o1, o2) -> {
			RType relA = getRelation(o1);
			RType relB = getRelation(o2);
			return relA.sortWeight - relB.sortWeight;
		});
		addDropdownFilter(new GUIListFilterDropdown<Faction, RType>(RType.values()) {

			@Override
			public boolean isOk(RType input, Faction f) {
				final PlayerState player = ((GameClientState) getState()).getPlayer();
				final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
				RType relation = getRelation(f);
				return relation == input;
			}
		}, new CreateGUIElementInterface<RType>() {

			@Override
			public GUIElement create(RType o) {
				GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(10), UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o.getName());
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				c.setUserPointer(o);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(10), UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("Filter By Relation (off)"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<Faction>() {

			@Override
			public boolean isOk(String input, Faction listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.RIGHT);
	}

	@Override
	protected Collection<Faction> getElementList() {
		final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
		return factionManager.getFactionCollection();
	}

	public RType getRelation(Faction f) {
		final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		if (player.getFactionId() == faction.getIdFaction()) {
			return factionManager.getRelation(player.getName(), player.getFactionId(), f.getIdFaction());
		} else {
			return factionManager.getRelation(faction.getIdFaction(), f.getIdFaction());
		}
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<Faction> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		int i = 0;
		for (final Faction f : collection) {
			RType relation = getRelation(f);
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sizeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable relationText = new GUITextOverlayTable(getState()) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					RType relation = getRelation(f);
					setColor(org.schema.game.client.view.gui.shiphud.newhud.ColorPalette.getColorDefault(relation, f.getIdFaction() == ((GameClientState) getState()).getPlayer().getFactionId()));
					super.draw();
				}
			};
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getName() + (f.isOpenToJoin() ? Lng.str(" (public)") : "");
				}
			});
			sizeText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getIdFaction() > 0 ? String.valueOf(f.getMembersUID().size()) : "-";
				}
			});
			relationText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getIdFaction() == faction.getIdFaction() ? Lng.str("OWN") : getRelation(f).name();
				}
			});
			nameText.getPos().y = UIScale.getUIScale().inset;
			sizeText.getPos().y = UIScale.getUIScale().inset;
			relationText.getPos().y = UIScale.getUIScale().inset;
			FactionRow r = new FactionRow(getState(), f, nameAnchorP, sizeText, relationText);
			r.expanded = new GUIElementList(getState());
			GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
			description.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getDescription();
				}
			});
			description.setPos(4, 2, 0);
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);
			GUITextButton mailButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("MAIL"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerMailInputNew mailInput = new PlayerMailInputNew((GameClientState) getState(), Lng.str("faction") + "[" + f.getName() + "]", "");
						mailInput.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(486);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton joinButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("JOIN"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						if (((GameClientState) getState()).getPlayer().getFactionId() != 0) {
							PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), Lng.str("Confirm"), Lng.str("Do you really want to join this faction\nand leave your current faction?")) {

								@Override
								public boolean isOccluded() {
									return false;
								}

								@Override
								public void onDeactivate() {
								}

								@Override
								public void pressedOK() {
									getState().getPlayer().getFactionController().joinFaction(f.getIdFaction());
									deactivate();
								}
							};
							confirm.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(487);
						} else {
							((GameClientState) getState()).getPlayer().getFactionController().joinFaction(f.getIdFaction());
						}
					}
				}
			}) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					if (f.isOpenToJoin()) {
						super.draw();
					}
				}
			};
			GUITextOverlayTableInnerDescription idText = new GUITextOverlayTableInnerDescription(10, 10, getState()) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {
						super.draw();
					}
				}
			};
			idText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String homebase = f.getHomebaseUID().length() > 0 ? (Lng.str("Homebase: ") + f.getHomeSector().toStringPure()) : Lng.str("No Homebase");
					if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {
						return homebase + "; " + Lng.str("Faction ID (Admin): %d", f.getIdFaction());
					} else {
						return homebase;
					}
				}
			});
			c.attach(mailButton);
			c.attach(joinButton);
			c.attach(idText);
			idText.setPos(4, c.getHeight() - 16, 0);
			mailButton.setPos(0, c.getHeight(), 0);
			joinButton.setPos(mailButton.getWidth() + 10, c.getHeight(), 0);
			c.attach(description);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(Faction e) {
		return !e.isShowInHub() || super.isFiltered(e);
	}

	private class FactionRow extends Row {

		public FactionRow(InputState state, Faction f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
