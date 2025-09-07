package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerMailInputNew;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.manager.ingame.faction.FactionRelationDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class FactionScrollableListNew extends ScrollableTableList<Faction> {

	private GUIElement p;

	public FactionScrollableListNew(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		this.p = p;
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
			RType relA = ((GameClientState) getState()).getFactionManager().getRelation(((GameClientState) getState()).getPlayer().getName(), ((GameClientState) getState()).getPlayer().getFactionId(), o1.getIdFaction());
			RType relB = ((GameClientState) getState()).getFactionManager().getRelation(((GameClientState) getState()).getPlayer().getName(), ((GameClientState) getState()).getPlayer().getFactionId(), o2.getIdFaction());
			return relA.sortWeight - relB.sortWeight;
		});
		addDropdownFilter(new GUIListFilterDropdown<Faction, RType>(RType.values()) {

			@Override
			public boolean isOk(RType input, Faction f) {
				final PlayerState player = ((GameClientState) getState()).getPlayer();
				final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
				RType relation = factionManager.getRelation(player.getName(), player.getFactionId(), f.getIdFaction());
				return relation == input;
			}
		}, new CreateGUIElementInterface<RType>() {

			@Override
			public GUIElement create(RType o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o.name());
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
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

	@Override
	public void updateListEntries(GUIElementList mainList, Set<Faction> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final Faction f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sizeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable relationText = new GUITextOverlayTable(getState()) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					RType relation = factionManager.getRelation(player.getName(), player.getFactionId(), f.getIdFaction());
					setColor(org.schema.game.client.view.gui.shiphud.newhud.ColorPalette.getColorDefault(relation, f.getIdFaction() == player.getFactionId()));
					super.draw();
				}
			};
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getName() + (f.isOpenToJoin() ? " " + Lng.str("(public)") : "");
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
					return f.getIdFaction() == player.getFactionId() ? Lng.str("OWN") : factionManager.getRelation(player.getName(), player.getFactionId(), f.getIdFaction()).getName();
				}
			});
			nameText.getPos().y = UIScale.getUIScale().inset;
			sizeText.getPos().y = UIScale.getUIScale().inset;
			relationText.getPos().y = UIScale.getUIScale().inset;
			final FactionRow r = new FactionRow(getState(), f, nameAnchorP, sizeText, relationText);
			r.expanded = new GUIElementList(getState());
			final GUIAnchor c = new GUIAnchor(getState(), 100, 100) {

				@Override
				public void draw() {
					this.setWidth(p.getWidth());
					super.draw();
				}
			};
			final GUIScrollablePanel scrollDescription = new GUIScrollablePanel(UIScale.getUIScale().scale(80), UIScale.getUIScale().scale(80), c, getState()) {

				@Override
				public void draw() {
					super.draw();
				}
			};
			GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
			scrollDescription.setContent(description);
			GUIResizableElement wrap = new GUIResizableElement(getState()) {

				@Override
				public float getWidth() {
					return scrollDescription.getWidth() - 20;
				}

				@Override
				public float getHeight() {
					return scrollDescription.getHeight() - 20;
				}

				@Override
				public void cleanUp() {
				}

				@Override
				public void draw() {
				}

				@Override
				public void onInit() {
				}

				@Override
				public void setWidth(float width) {
				}

				@Override
				public void setHeight(float height) {
				}
			};
			description.autoWrapOn = wrap;
			description.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getDescription();
				}
			});
			description.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().smallinset, 0);
			GUITextButton mailButton = new GUITextButton(getState(), UIScale.getUIScale().scale(80), UIScale.getUIScale().h, ColorPalette.OK, Lng.str("MAIL"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerMailInputNew mailInput = new PlayerMailInputNew((GameClientState) getState(), Lng.str("faction[%s]", f.getName()), "");
						mailInput.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(481);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton viewRelation = new GUITextButton(getState(), UIScale.getUIScale().scale(130), UIScale.getUIScale().h, ColorPalette.OK, Lng.str("VIEW RELATIONS"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						final PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("FactionScrollableListNew_VIEW_REL", (GameClientState) getState(), 540, 400, Lng.str("Relations for %s", f.getName()), "") {

							@Override
							public boolean isOccluded() {
								return !(getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1) == this);
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
							}
						};
						c.getInputPanel().setCancelButton(false);
						c.getInputPanel().onInit();
						FactionScrollableListRelation factionScrollableListRelation = new FactionScrollableListRelation(getState(), c.getInputPanel().getContent(), f) {

							@Override
							public boolean isActive() {
								return super.isActive() && (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1) == c);
							}
						};
						factionScrollableListRelation.onInit();
						c.getInputPanel().getContent().attach(factionScrollableListRelation);
						c.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(482);
					}
				}
			});
			GUITextButton joinButton = new GUITextButton(getState(), UIScale.getUIScale().scale(80), UIScale.getUIScale().h, ColorPalette.OK, Lng.str("JOIN"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						if (((GameClientState) getState()).getPlayer().getFactionId() != 0) {
							PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("FactionScrollableListNew_VIEW_JOIN", (GameClientState) getState(), Lng.str("Confirm"), Lng.str("Do you really want to join this faction\nand leave your current faction?")) {

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
							AudioController.fireAudioEventID(483);
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
					if (f.isOpenToJoin() && f.getIdFaction() > 0 && f.getIdFaction() != player.getFactionId()) {
						super.draw();
					}
				}
			};
			GUITextButton relationButton = new GUITextButton(getState(), UIScale.getUIScale().scale(130), UIScale.getUIScale().h, ColorPalette.CANCEL, Lng.str("CHANGE RELATION"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						FactionRelationDialog d = new FactionRelationDialog((GameClientState) getState(), ((GameClientState) getState()).getFaction(), f);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(484);
					}
				}
			}) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
				 */
				@Override
				public void draw() {
					if (((GameClientState) getState()).getFaction() != null) {
						FactionPermission factionPermission = ((GameClientState) getState()).getFaction().getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
						if (factionPermission != null && factionPermission.hasRelationshipPermission(((GameClientState) getState()).getFaction()) && (f.getIdFaction() > 0 || FactionManager.isNPCFaction(f.getIdFaction())) && f.getIdFaction() != player.getFactionId()) {
							super.draw();
						}
					}
				}
			};
			GUITextButton shareFogOfWarButton = new GUITextButton(getState(), UIScale.getUIScale().scale(110), UIScale.getUIScale().h, ColorPalette.CANCEL, Lng.str("SHARE FOW"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), UIScale.getUIScale().scale(400), UIScale.getUIScale().scale(230), Lng.str("Share Fog of War"), Lng.str("Really send Faction %s your faction's fog of war information?", f.getName())) {

							@Override
							public void pressedOK() {
								((GameClientState) getState()).getPlayer().getFactionController().shareFow(f.getIdFaction());
								deactivate();
							}

							@Override
							public void onDeactivate() {
							}
						};
						c.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(485);
					}
				}
			}) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
				 */
				@Override
				public void draw() {
					if (((GameClientState) getState()).getFaction() != null) {
						FactionPermission factionPermission = ((GameClientState) getState()).getFaction().getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
						if (factionPermission != null && factionPermission.hasPermissionEditPermission(((GameClientState) getState()).getFaction()) && f.getIdFaction() > 0 && f.getIdFaction() != player.getFactionId()) {
							super.draw();
						}
					}
				}
			};
			GUITextOverlayTableInnerDescription idText = new GUITextOverlayTableInnerDescription(10, 10, getState()) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					super.draw();
				}
			};
			idText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String homebase = f.getHomebaseUID().length() > 0 ? (Lng.str("Homebase: ") + f.getHomeSector().toStringPure()) : Lng.str("No Homebase");
					if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {
						return homebase + Lng.str("; Faction ID (Admin):") + f.getIdFaction();
					} else {
						return homebase;
					}
				}
			});
			c.attach(mailButton);
			c.attach(viewRelation);
			c.attach(relationButton);
			c.attach(shareFogOfWarButton);
			c.attach(idText);
			c.attach(joinButton);
			idText.setPos(UIScale.getUIScale().inset, c.getHeight() - UIScale.getUIScale().scale(16), 0);
			mailButton.setPos(0, c.getHeight(), 0);
			viewRelation.setPos(90, c.getHeight(), 0);
			relationButton.setPos(mailButton.getWidth() + UIScale.getUIScale().scale(10) + viewRelation.getWidth() + UIScale.getUIScale().scale(10), c.getHeight(), 0);
			shareFogOfWarButton.setPos(relationButton.getPos().x + relationButton.getWidth() + UIScale.getUIScale().scale(20), c.getHeight(), 0);
			joinButton.setPos(mailButton.getWidth() + UIScale.getUIScale().scale(10) + viewRelation.getWidth() + UIScale.getUIScale().scale(10) + relationButton.getWidth() + UIScale.getUIScale().scale(10) + shareFogOfWarButton.getWidth() + UIScale.getUIScale().scale(10), c.getHeight(), 0);
			c.attach(scrollDescription);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private class FactionRow extends Row {

		public FactionRow(InputState state, Faction f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(Faction e) {
		return !e.isShowInHub() || super.isFiltered(e);
	}
}
