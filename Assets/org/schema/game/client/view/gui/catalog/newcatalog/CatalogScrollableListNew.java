package org.schema.game.client.view.gui.catalog.newcatalog;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import api.common.GameClient;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.CatalogChangeListener;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerTextAreaInput;
import org.schema.game.client.controller.manager.ingame.catalog.CatalogPermissionEditDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.BlueprintPlayerHandleRequest;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.remote.RemoteBlueprintPlayerRequest;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUICheckBoxTextPair;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIPolygonStats;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiDateFormats;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CatalogScrollableListNew extends ScrollableTableList<CatalogPermission> implements CatalogChangeListener {

	public static final int AVAILABLE = 0;

	public static final int PERSONAL = 1;

	public static final int ADMIN = 2;

	private final boolean showPrice;

	public CatalogPermission selectedSingle;

	private int mode;

	private boolean selectSingle;

	private boolean spawnDocked;

	public CatalogScrollableListNew(InputState state, GUIElement p, int personalOnly, boolean showPrice, boolean selectSingle) {
		super(state, 100, 100, p);
		this.mode = personalOnly;
		((GameClientState) getState()).getCatalogManager().listeners.add(this);
		((GameClientState) getState()).getPlayer().getCatalog().listeners.add(this);
		this.selectSingle = selectSingle;
		this.showPrice = showPrice;
		useOwnFaction = ((GameClientState) getState()).getPlayer().getFactionId() > 0;
	}

	private boolean isInShipyardEdit() {
		return GameClient.getCurrentControl() != null && GameClient.getCurrentControl().isVirtualBlueprint();
	}

	private boolean canSpawnDocked() {
		return spawnDocked && (BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable());
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getCatalogManager().listeners.remove(this);
		((GameClientState) getState()).getPlayer().getCatalog().listeners.remove(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 7, (o1, o2) -> {
			assert (o1 != null);
			assert (o2 != null);
			assert (o1.getUid() != null);
			assert (o2.getUid() != null);
			return o1.getUid().compareToIgnoreCase(o2.getUid());
		}, true);
		addFixedWidthColumnScaledUI(Lng.str("Type"), 65, (o1, o2) -> o1.type.name().compareTo(o2.type.name()));
		addFixedWidthColumnScaledUI(Lng.str("Class"), 101, (o1, o2) -> o1.getClassification().ordinal() - o2.getClassification().ordinal());
		addFixedWidthColumnScaledUI(Lng.str("Date Created"), 120, (o1, o2) -> (o1.date > o2.date) ? 1 : (o1.date < o2.date ? -1 : 0));
		addFixedWidthColumnScaledUI(Lng.str("Mass"), 60, (o1, o2) -> (o1.mass > o2.mass ? 1 : (o1.mass < o2.mass ? -1 : 0)));
		if (showPrice) {
			addFixedWidthColumnScaledUI(Lng.str("Price"), 120, (o1, o2) -> (o1.price > o2.price ? 1 : (o1.price < o2.price ? -1 : 0)));
		}
		addFixedWidthColumnScaledUI(Lng.str("Rating"), 60, (o1, o2) -> (o1.rating > o2.rating ? 1 : (o1.rating < o2.rating ? -1 : 0)));
		addDropdownFilter(new GUIListFilterDropdown<CatalogPermission, Integer>(new Integer[] { 0, 1 }) {

			@Override
			public boolean isOk(Integer input, CatalogPermission f) {
				if (input == 0) {
					return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(((GameClientState) getState()).getPlayer().getName().toLowerCase(Locale.ENGLISH));
				} else if (input == 1) {
					return f.faction();
				}
				return true;
			}
		}, new CreateGUIElementInterface<Integer>() {

			@Override
			public GUIElement create(Integer o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o == 0 ? Lng.str("OWN") : Lng.str("FACTION"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("Filter By Ownership (off)"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, FilterRowStyle.LEFT);
		addDropdownFilter(new GUIListFilterDropdown<CatalogPermission, BlueprintType>(new BlueprintType[] { BlueprintType.SHIP, BlueprintType.SPACE_STATION }) {

			@Override
			public boolean isOk(BlueprintType input, CatalogPermission f) {
				return f.type == input;
			}
		}, new CreateGUIElementInterface<BlueprintType>() {

			@Override
			public GUIElement create(BlueprintType o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 20);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o.name());
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, 20);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("Filter By Type (off)"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, FilterRowStyle.RIGHT);
		addTextFilter(new GUIListFilterText<CatalogPermission>() {

			@Override
			public boolean isOk(String input, CatalogPermission listElement) {
				return listElement.getUid().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		if (mode != PERSONAL) {
			addTextFilter(new GUIListFilterText<CatalogPermission>() {

				@Override
				public boolean isOk(String input, CatalogPermission listElement) {
					return listElement.ownerUID.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
				}
			}, Lng.str("SEARCH BY OWNER"), FilterRowStyle.RIGHT);
		}
	}

	@Override
	protected Collection<CatalogPermission> getElementList() {
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		List<CatalogPermission> cat;
		if (mode == PERSONAL) {
			cat = player.getCatalog().getPersonalCatalog();
		} else if (mode == AVAILABLE) {
			cat = player.getCatalog().getAvailableCatalog();
		} else if (mode == ADMIN) {
			cat = player.getCatalog().getAllCatalog();
		} else {
			throw new IllegalArgumentException("[GUI] UNKNOWN CAT MODE: " + mode);
		}
		return cat;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<CatalogPermission> collection) {
		// mainList.clear();
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
		final CatalogManager catalogManager = ((GameClientState) getState()).getGameState().getCatalogManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final CatalogPermission f : collection) {
			assert (f.getUid() != null);
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable typeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable massText = new GUITextOverlayTable(getState());
			GUITextOverlayTable ratingText = new GUITextOverlayTable(getState());
			GUITextOverlayTable priceText = new GUITextOverlayTable(getState());
			GUITextOverlayTable classText = new GUITextOverlayTable(getState());
			GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			GUIClippedRow classAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			classAnchorP.attach(classText);
			nameText.setTextSimple(f.getUid());
			typeText.setTextSimple(f.type.type.getName());
			priceText.setTextSimple(f.price);
			classText.setTextSimple(f.getClassification().getName());
			massText.setTextSimple(StringTools.massFormat(f.mass));
			ratingText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.rating);
				}
			});
			dateText.setTextSimple(GuiDateFormats.mediumFormat.format(f.date));
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			typeText.getPos().y = heightInset;
			massText.getPos().y = heightInset;
			ratingText.getPos().y = heightInset;
			priceText.getPos().y = heightInset;
			classText.getPos().y = heightInset;
			dateText.getPos().y = heightInset;
			final CatalogRow r;
			if (showPrice) {
				r = new CatalogRow(getState(), f, nameAnchorP, typeText, classAnchorP, dateText, massText, priceText, ratingText);
			} else {
				r = new CatalogRow(getState(), f, nameAnchorP, typeText, classAnchorP, dateText, massText, ratingText);
			}
			if (!selectSingle) {
				r.expanded = new GUIElementList(getState());
				final String owner = Lng.str("Owner: %s\n", f.ownerUID);
				final String created = Lng.str("Created: %s\n", GuiDateFormats.catalogEntryCreated.format(f.date));
				final GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
				description.setTextSimple(new Object() {

					@Override
					public String toString() {
						return owner + created + Lng.str("Description:") + f.description;
					}
				});
				description.setPos(4, 2, 0);
				GUIAnchor c = new GUIAnchor(getState(), 100, Math.max(112, description.getTextHeight() + 12)) {

					@Override
					public void draw() {
						setWidth(r.l.getInnerTextbox().getWidth());
						if (description.getTextHeight() != getHeight()) {
							notifyObservers();
							setHeight(description.getTextHeight());
							System.err.println("TEXT HEIGHT UPDATED");
							r.expanded.updateDim();
						}
						super.draw();
					}
				};
				GUITextButton buyButton = new GUITextButton(getState(), 56, 24, ColorPalette.OK, Lng.str("BUY"), new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(389);
							if (((GameClientState) getState()).getGameState().isBuyBBWithCredits()) {
								buyEntry(f);
							} else {
								buyEntryAsMeta(f);
							}
						}
					}

					@Override
					public boolean isOccluded() {
						return !isActive();
					}
				});
				GUITextButton editDescriptionButton = new GUITextButton(getState(), 102, 24, ColorPalette.OK, Lng.str("DESCRIPTION"), new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							PlayerTextAreaInput t = new PlayerTextAreaInput("CatalogScrollableListNew_EDIT_DESC", (GameClientState) getState(), 140, 3, Lng.str("Edit Faction Description"), "", f.description) {

								@Override
								public String[] getCommandPrefixes() {
									return null;
								}

								@Override
								public boolean isOccluded() {
									return false;
								}

								@Override
								public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
									return null;
								}

								@Override
								public void onDeactivate() {
								}

								@Override
								public boolean onInput(String entry) {
									if (canEdit(f)) {
										f.description = entry;
										if (mode == ADMIN && getState().getPlayer().getNetworkObject().isAdminClient.get()) {
											f.changeFlagForced = true;
											catalogManager.clientRequestCatalogEdit(f);
										} else {
											f.ownerUID = getState().getPlayer().getName();
											catalogManager.clientRequestCatalogEdit(f);
										}
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
										AudioController.fireAudioEventID(390);
										return true;
									}
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
									AudioController.fireAudioEventID(391);
									return false;
								}

								@Override
								public void onFailedTextCheck(String msg) {
								}
							};
							t.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(392);
						}
					}
				});
				GUITextButton permissionEditButton = new GUITextButton(getState(), 95, 24, ColorPalette.CANCEL, Lng.str("PERMISSIONS"), new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							if (canEdit(f)) {
								CatalogPermissionEditDialog d = new CatalogPermissionEditDialog(((GameClientState) getState()), f);
								d.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(393);
							}
						}
					}
				});
				GUITextButton detailsButton = new GUITextButton(getState(), 70, 24, ColorPalette.OK, Lng.str("BLOCKS"), new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							PlayerGameOkCancelInput detailsPopup = new PlayerGameOkCancelInput("blueprintConsistence", (GameClientState) getState(), 400, 400, Lng.str("Blueprint Details"), "") {

								@Override
								public void onDeactivate() {
								}

								@Override
								public void pressedOK() {
									deactivate();
								}
							};
							((GameClientState) getState()).getPlayer().sendSimpleCommand(SimplePlayerCommands.REQUEST_BLUEPRINT_ITEM_LIST, f.getUid());
							detailsPopup.getInputPanel().onInit();
							((GUIDialogWindow) detailsPopup.getInputPanel().background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(25));
							((GUIDialogWindow) detailsPopup.getInputPanel().background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(40));
							GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, 1, ((GUIDialogWindow) detailsPopup.getInputPanel().background).getMainContentPane().getContent(0));
							buttons.onInit();
							((GUIDialogWindow) detailsPopup.getInputPanel().background).getMainContentPane().getContent(0).attach(buttons);
							final GUIBlueprintConsistenceScrollableList sc = new GUIBlueprintConsistenceScrollableList(getState(), ((GUIDialogWindow) detailsPopup.getInputPanel().background).getMainContentPane().getContent(1));
							sc.onInit();
							((GUIDialogWindow) detailsPopup.getInputPanel().background).getMainContentPane().getContent(1).attach(sc);
							detailsPopup.getInputPanel().setOkButton(false);
							detailsPopup.getInputPanel().setOkButtonText(Lng.str("DONE"));
							String buttonName = (ServerConfig.BLUEPRINTS_USE_COMPONENTS.isOn() ? Lng.str("Components/Resources") : Lng.str("Blocks/Resources"));
							buttons.addButton(0, 0, buttonName, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

								@Override
								public void callback(GUIElement callingGuiElement, MouseEvent event) {
									if (event.pressedLeftMouse()) {
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
										AudioController.fireAudioEventID(394);
										sc.setResources(!sc.isResources());
										((GameClientState) getState()).getPlayer().sendSimpleCommand(SimplePlayerCommands.REQUEST_BLUEPRINT_ITEM_LIST, f.getUid());
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
							detailsPopup.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(395);
						}
					}
				});
				GUITextButton loadAdminButton = new GUITextButton(getState(), 94, 24, ColorPalette.OK, Lng.str("ADMIN LOAD"), new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							if(isPlayerAdmin() || isInShipyardEdit()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(396);
								load(f);
							}
						}
					}
				});
				GUITextButton deleteButton = new GUITextButton(getState(), 64, 24, ColorPalette.CANCEL, Lng.str("DELETE"), new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(397);
							if (canEdit(f)) {
								deleteEntry(f);
							}
						}
					}
				});
				GUITextButton ownerButton = new GUITextButton(getState(), 120, 24, ColorPalette.CANCEL, Lng.str("CHANGE OWNER"), new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(398);
							if (isPlayerAdmin()) {
								changeOwner(f);
							}
						}
					}
				});
				GUIAnchor buttonPane0 = new GUIAnchor(getState(), 100, 32) {

					@Override
					public void draw() {
						getPos().y = r.l.getList().getHeight();
						super.draw();
					}
				};
				buttonPane0.attach(buyButton);
				buttonPane0.attach(detailsButton);
				if (canEdit(f)) {
					buttonPane0.attach(editDescriptionButton);
					buttonPane0.attach(permissionEditButton);
					buttonPane0.attach(deleteButton);
				}
				if (isPlayerAdmin()) {
					buttonPane0.attach(loadAdminButton);
					buttonPane0.attach(ownerButton);
				}
				int buttonDist = 8;
				int bHeight = 0;
				buyButton.setPos(0, bHeight, 0);
				editDescriptionButton.setPos(buyButton.getWidth() + buttonDist, bHeight, 0);
				detailsButton.setPos(buyButton.getWidth() + buttonDist + editDescriptionButton.getWidth() + buttonDist, bHeight, 0);
				permissionEditButton.setPos(detailsButton.getWidth() + buttonDist + buyButton.getWidth() + buttonDist + editDescriptionButton.getWidth() + buttonDist, bHeight, 0);
				deleteButton.setPos(detailsButton.getWidth() + buttonDist + buyButton.getWidth() + buttonDist + editDescriptionButton.getWidth() + buttonDist + permissionEditButton.getWidth() + buttonDist, bHeight, 0);
				loadAdminButton.setPos(detailsButton.getWidth() + buttonDist + buyButton.getWidth() + buttonDist + editDescriptionButton.getWidth() + buttonDist + permissionEditButton.getWidth() + buttonDist + deleteButton.getWidth() + buttonDist, bHeight, 0);
				ownerButton.setPos(detailsButton.getWidth() + buttonDist + buyButton.getWidth() + buttonDist + editDescriptionButton.getWidth() + buttonDist + permissionEditButton.getWidth() + buttonDist + deleteButton.getWidth() + buttonDist + loadAdminButton.getWidth() + buttonDist, bHeight, 0);
				c.attach(description);
				buttonPane0.getPos().y = c.getHeight();
				r.expanded.add(new GUIListElement(c, c, getState()));
				if (f.score != null) {
					final GUIAnchor stats = new GUIAnchor(getState(), 100, 128) {

						@Override
						public void draw() {
							setWidth(r.l.getInnerTextbox().getWidth());
							super.draw();
						}
					};
					GUITextOverlayTable statText = new GUITextOverlayTable(getState());
					List<Object> a = new ObjectArrayList<Object>();
					f.score.addStrings(a);
					statText.setText(a);
					statText.setPos(4, 0, 0);
					stats.attach(statText);
					GUIPolygonStats st = new GUIPolygonStats(getState(), f.score) {

						@Override
						public void draw() {
							setPos(stats.getWidth() - (getWidth() * 2), -40, 0);
							super.draw();
						}
					};
					stats.attach(st);
					r.expanded.add(new GUIListElement(stats, stats, getState()));
				}
				r.expanded.attach(buttonPane0);
			}
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	public boolean isPlayerAdmin() {
		return ((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(((GameClientState) getState()).getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	private void changeOwner(final CatalogPermission permission) {
		String description = Lng.str("Change the owner of \"%s\"", permission.getUid());
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogExtendedPanel_changeOwner", (GameClientState) getState(), 50, Lng.str("Change Owner"), description, permission.ownerUID) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public boolean isOccluded() {
				return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage(Lng.str("ONWER INVALID: %s", msg));
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getPlayer().getNetworkObject().isAdminClient.get()) {
					CatalogPermission p = new CatalogPermission(permission);
					p.ownerUID = entry;
					p.changeFlagForced = true;
					getState().getCatalogManager().clientRequestCatalogEdit(p);
				} else {
					System.err.println("ERROR: CANNOT CHANGE OWNER (PERMISSION DENIED)");
				}
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("Must only contain Letters or numbers or (_-)!"));
				return false;
			}
		});
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(399);
	}

	private void deleteEntry(final CatalogPermission permission) {
		boolean admin = ((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get();
		if (!admin && !((GameClientState) getState()).getPlayer().getName().toLowerCase(Locale.ENGLISH).equals(permission.ownerUID.toLowerCase(Locale.ENGLISH))) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("ERROR:\nCannot delete!\nYou do not own this!"), 0);
		} else {
			PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CatalogScrollableListNew_deleteEntry", (GameClientState) getState(), Lng.str("Confirm"), Lng.str("Do you really want to delete this entry?\n(a backup will be created on the server)")) {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					if (getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						CatalogPermission p = new CatalogPermission(permission);
						p.changeFlagForced = true;
						getState().getCatalogManager().clientRequestCatalogRemove(p);
					} else {
						CatalogPermission p = new CatalogPermission(permission);
						p.ownerUID = getState().getPlayer().getName();
						getState().getCatalogManager().clientRequestCatalogRemove(p);
					}
					deactivate();
				}
			};
			confirm.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(400);
		}
	}

	private boolean useOwnFaction;

	private void load(CatalogPermission permission) {
		if(isInShipyardEdit() && !canSpawnDocked() && !isPlayerAdmin()) {
			getState().getController().popupAlertTextMessage(Lng.str("You can only spawn ships on a virtual design when in shipyard edit mode!"));
			return;
		}
		String description = Lng.str("Please type in a name for your new Ship!");
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogScrollableListNew_f_load", (GameClientState) getState(), 400, 240, 50, Lng.str("New Ship"), description, permission.getUid() + "_" + System.currentTimeMillis()) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public boolean isOccluded() {
				return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage(Lng.str("SHIPNAME INVALID:") + " " + msg);
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getCharacter() == null || getState().getCharacter().getPhysicsDataContainer() == null || !getState().getCharacter().getPhysicsDataContainer().isInitialized()) {
					System.err.println("[ERROR] Character might not have been initialized");
					return false;
				}
				System.err.println("[CLIENT] BUYING CATALOG ENTRY: " + permission.getUid() + " FOR " + getState().getPlayer().getNetworkObject());
				BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
				req.catalogName = permission.getUid();
				req.entitySpawnName = entry;
				req.save = false;
				req.toSaveShip = -1;
				req.directBuy = true;
				req.setOwnFaction = useOwnFaction;
				if (spawnDocked && BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable()) {
					req.spawnOnId = BuildModeDrawer.currentPiece.getSegmentController().getId();
					req.spawnOnBlock = BuildModeDrawer.currentPiece.getAbsoluteIndex();
				}
				getState().getController().sendAdminCommand(spawnDocked ? AdminCommands.LOAD_AS_FACTION_DOCKED : AdminCommands.LOAD_AS_FACTION, req.catalogName, req.entitySpawnName, req.setOwnFaction ? getState().getPlayer().getFactionId() : 0);
				// getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, false));
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
				return false;
			}
		});
		pp.getInputPanel().onInit();
		GUICheckBoxTextPair useFact = new GUICheckBoxTextPair(getState(), Lng.str("Set as own Faction (needs faction block)"), 280, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return useOwnFaction;
			}

			@Override
			public void deactivate() {
				useOwnFaction = false;
			}

			@Override
			public void activate() {
				if (((GameClientState) getState()).getPlayer().getFactionId() > 0) {
					useOwnFaction = true;
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You are not in a faction!"), 0);
					useOwnFaction = false;
				}
			}
		};
		useFact.setPos(3, 35, 0);
		((GUIDialogWindow) pp.getInputPanel().background).getMainContentPane().getContent(0).attach(useFact);
		GUICheckBoxTextPair useSpawnDocked = new GUICheckBoxTextPair(getState(), new Object() {

			public String toString() {
				if (BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable()) {
					return Lng.str("Spawn docked");
				} else {
					return Lng.str("Spawn docked (must be aiming at a rail block)");
				}
			}
		}, 280, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				// spawnDocked = spawnDocked && (BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isDockable());
				return spawnDocked;
			}

			@Override
			public void deactivate() {
				spawnDocked = false;
			}

			@Override
			public void activate() {
				System.err.println("LOAD DOCKED: " + BuildModeDrawer.currentPiece);
				if ((BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable())) {
					spawnDocked = true;
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Must be aiming at a rail block!"), 0);
					spawnDocked = false;
				}
			}
		};
		useSpawnDocked.setPos(3, 65, 0);
		((GUIDialogWindow) pp.getInputPanel().background).getMainContentPane().getContent(0).attach(useSpawnDocked);
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(401);
	}

	private void buyEntry(final CatalogPermission permission) {
		if (!((GameClientState) getState()).isInShopDistance()) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("ERROR:\nCannot buy!\nYou are not near a shop!"), 0);
		}
		String description = Lng.str("Please type in a name for your new Ship!");
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogScrollableListNew_f_NewShip", (GameClientState) getState(), 400, 240, 50, Lng.str("New Ship"), description, permission.getUid() + "_" + System.currentTimeMillis()) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public boolean isOccluded() {
				return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getCharacter() == null || getState().getCharacter().getPhysicsDataContainer() == null || !getState().getCharacter().getPhysicsDataContainer().isInitialized()) {
					System.err.println("[ERROR] Character might not have been initialized");
					return false;
				}
				System.err.println("[CLIENT] BUYING CATALOG ENTRY: " + permission.getUid() + " FOR " + getState().getPlayer().getNetworkObject());
				BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
				req.catalogName = permission.getUid();
				req.entitySpawnName = entry;
				req.save = false;
				req.toSaveShip = -1;
				req.directBuy = true;
				req.setOwnFaction = useOwnFaction;
				getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, false));
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
				return false;
			}
		});
		pp.getInputPanel().onInit();
		GUICheckBoxTextPair useFact = new GUICheckBoxTextPair(getState(), Lng.str("Set as own Faction (needs faction block)"), 280, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return useOwnFaction;
			}

			@Override
			public void deactivate() {
				useOwnFaction = false;
			}

			@Override
			public void activate() {
				if (((GameClientState) getState()).getPlayer().getFactionId() > 0) {
					useOwnFaction = true;
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You are not in a faction!"), 0);
					useOwnFaction = false;
				}
			}
		};
		useFact.setPos(3, 35, 0);
		((GUIDialogWindow) pp.getInputPanel().background).getMainContentPane().getContent(0).attach(useFact);
		GUICheckBoxTextPair useSpawnDocked = new GUICheckBoxTextPair(getState(), new Object() {

			public String toString() {
				if (BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable()) {
					return Lng.str("Spawn docked");
				} else {
					return Lng.str("Spawn docked (must be aiming at a rail block)");
				}
			}
		}, 280, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				// spawnDocked = spawnDocked && (BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isDockable());
				return spawnDocked;
			}

			@Override
			public void deactivate() {
				spawnDocked = false;
			}

			@Override
			public void activate() {
				if ((BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable())) {
					spawnDocked = true;
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Must be aiming at a rail block!"), 0);
					spawnDocked = false;
				}
			}
		};
		useSpawnDocked.setPos(3, 65, 0);
		((GUIDialogWindow) pp.getInputPanel().background).getMainContentPane().getContent(0).attach(useSpawnDocked);
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(402);
	}

	private void buyEntryAsMeta(final CatalogPermission permission) {
		if (!((GameClientState) getState()).isInShopDistance()) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("ERROR:\nCannot buy!\nYou are not near a shop!"), 0);
		}
		String title = Lng.str("Buy Blueprint of %s", permission.getUid());
		String price = Lng.str("This blueprint is free!");
		if (permission.getEntry() == BlueprintType.SPACE_STATION) {
			price = Lng.str("A station blueprint costs %s Credits!\nYou currently have %s Credits.", StringTools.formatSeperated(((GameStateInterface) getState()).getGameState().getStationCost()), StringTools.formatSeperated(((GameClientState) getState()).getPlayer().getCredits()));
		}
		String desc = Lng.str("%s\n\nThis will put the blueprint in your inventory.\nRight click on it to provide the necessary materials.", price);
		(new PlayerGameOkCancelInput("CatalogScrollableListNew_buyEntryAsMeta", (GameClientState) getState(), title, desc) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
				BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
				req.catalogName = permission.getUid();
				req.entitySpawnName = "";
				req.save = false;
				req.toSaveShip = -1;
				req.directBuy = false;
				if (spawnDocked && BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.isValid() && BuildModeDrawer.currentPiece.getInfo().isRailDockable()) {
					req.spawnOnId = BuildModeDrawer.currentPiece.getSegmentController().getId();
					req.spawnOnBlock = BuildModeDrawer.currentPiece.getAbsoluteIndex();
				}
				getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, false));
				deactivate();
			}

			@Override
			public void onDeactivate() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(403);
	}

	private class CatalogRow extends Row {

		public CatalogRow(InputState state, CatalogPermission f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#isSimpleSelected()
		 */
		@Override
		protected boolean isSimpleSelected() {
			return selectSingle && CatalogScrollableListNew.this.selectedSingle == f;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#clickedOnRow()
		 */
		@Override
		protected void clickedOnRow() {
			CatalogScrollableListNew.this.selectedSingle = f;
			super.clickedOnRow();
		}
	}

	@Override
	public void onCatalogChanged() {
		onChange(false);
	}
}
