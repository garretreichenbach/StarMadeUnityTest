package org.schema.game.client.view.gui.navigation.navigationnew;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NavigationScrollableListNew extends ScrollableTableList<SimpleTransformableSendableObject<?>>  {

	private static final Vector3f dir = new Vector3f();
	private static final Vector3f dir1 = new Vector3f();
	private static final Vector3f dir2 = new Vector3f();
	public NavigationScrollableListNew(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		setColumnsHeight(UIScale.getUIScale().scale(32));

		getState().getController().sectorEntitiesChangeObservable.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		getState().getController().sectorEntitiesChangeObservable.deleteObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#onInit()
	 */
	@Override
	public void onInit() {

		super.onInit();

	}

	@Override
	public void initColumns() {


		addFixedWidthColumnScaledUI(Lng.str("Target"), 40, (o1, o2) -> {
			final FactionManager factionManager = getState().getGameState().getFactionManager();
			RType relA = getState().getFactionManager().getRelation(getState().getCurrentPlayerObject(), o1);
			RType relB = getState().getFactionManager().getRelation(getState().getCurrentPlayerObject(), o2);

			return relA.sortWeight - relB.sortWeight;
		});
		addColumn("Name", 6, (o1, o2) -> (o1.getRealName() == null ? "NULL_NAME" : o1.getRealName()).compareToIgnoreCase((o2.getRealName() == null ? "NULL_NAME" : o2.getRealName())));
		addFixedWidthColumnScaledUI(Lng.str("Type"), 110, (o1, o2) -> o1.getTypeString().compareToIgnoreCase(o2.getTypeString()));
		addFixedWidthColumnScaledUI(Lng.str("Mass"), 60, (o1, o2) -> o1.getMass() > o2.getMass() ? 1 : (o1.getMass() < o2.getMass() ? -1 : 0));
		addColumn(Lng.str("Faction"), 3, (o1, o2) -> {
			final FactionManager factionManager = getState().getGameState().getFactionManager();
			if (factionManager.existsFaction(o1.getFactionId()) && !factionManager.existsFaction(o2.getFactionId())) {
				return 1;
			} else if (!factionManager.existsFaction(o1.getFactionId()) && factionManager.existsFaction(o2.getFactionId())) {
				return -1;
			} else if (!factionManager.existsFaction(o1.getFactionId()) && !factionManager.existsFaction(o2.getFactionId())) {
				return 0;
			} else {
				return factionManager.getFaction(o1.getFactionId()).getName().compareToIgnoreCase(factionManager.getFaction(o2.getFactionId()).getName());
			}
		});
		addFixedWidthColumnScaledUI(Lng.str("Distance"), 66, (o1, o2) -> {
			GameClientState state = getState();
			dir1.set(o1.getWorldTransformOnClient().origin);
			if (state.getCurrentPlayerObject() != null) {
				dir1.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
			} else {
				dir1.sub(Controller.getCamera().getPos());
			}
			float p1 = dir1.length();

			dir2.set(o2.getWorldTransformOnClient().origin);
			if (state.getCurrentPlayerObject() != null) {
				dir2.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
			} else {
				dir2.sub(Controller.getCamera().getPos());
			}
			float p2 = dir2.length();
			return p1 > p2 ? 1 : (p1 < p2 ? -1 : 0);
		}, true);
		addDropdownFilter(new GUIListFilterDropdown<SimpleTransformableSendableObject<?>, SimpleTransformableSendableObject.EntityType>(SimpleTransformableSendableObject.EntityType.getUsed()) {
			@Override
			public boolean isOk(SimpleTransformableSendableObject.EntityType input, SimpleTransformableSendableObject<?> f) {
				return f.getType() == input;
			}
		}, new CreateGUIElementInterface<SimpleTransformableSendableObject.EntityType>() {
			@Override
			public GUIElement create(SimpleTransformableSendableObject.EntityType o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o.getName());
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);

				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("Filter By Type (off)"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, FilterRowStyle.FULL);

		addTextFilter(new GUIListFilterText<SimpleTransformableSendableObject<?>>() {

			@Override
			public boolean isOk(String input,
			                    SimpleTransformableSendableObject<?> listElement) {
				return listElement.getRealName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<SimpleTransformableSendableObject<?>>() {

			@Override
			public boolean isOk(String input,
			                    SimpleTransformableSendableObject<?> listElement) {
				final FactionManager factionManager = getState().getGameState().getFactionManager();
				return factionManager.existsFaction(listElement.getFactionId()) && factionManager.getFaction(listElement.getFactionId()).getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY FACTION"), FilterRowStyle.RIGHT);

	}

	@Override
	protected Collection<SimpleTransformableSendableObject<?>> getElementList() {
		return getState().getCurrentSectorEntities().values();
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<SimpleTransformableSendableObject<?>> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;

		for (final SimpleTransformableSendableObject<?> f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable crewText = new GUITextOverlayTable(getState());
			GUITextOverlayTable typeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable distanceText = new GUITextOverlayTable(getState());
			GUITextOverlayTable factionText = new GUITextOverlayTable(getState());
			GUITextOverlayTable massText = new GUITextOverlayTable(getState());
			GUITextOverlayTable powerText = new GUITextOverlayTable(getState());
			GUITextOverlayTable shieldsText = new GUITextOverlayTable(getState());

			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameAnchorP.attach(crewText);

			GUIClippedRow factionAnchorP = new GUIClippedRow(getState());
			factionAnchorP.attach(factionText);

			final Vector4f tint = new Vector4f(1, 1, 1, 1);
			GUIOverlay indicator = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"hud_pointers-c-8x8"), getState()) {
				@Override
				public void draw() {
					setSpriteSubIndex(23);
					setPos(18, 16, 0);
					RType relation = ((GameClientState) getState()).getPlayer().getRelation(f.getFactionId());

					PlayerInteractionControlManager playerIntercationManager = ((GameClientState) getState()).
							getGlobalGameControlManager().
							getIngameControlManager().
							getPlayerGameControlManager().
							getPlayerIntercationManager();

					SimpleTransformableSendableObject selectedEntity = playerIntercationManager.getSelectedEntity();

					HudIndicatorOverlay.getColor(f, tint, f == selectedEntity, ((GameClientState) getState()));

					getSprite().setTint(tint);

					super.draw();
				}

			};
			indicator.setSpriteSubIndex(23);
			indicator.setScale(0.5f, 0.5f, 0.5f);

			indicator.onInit();
			final StringBuffer sb = new StringBuffer();
			final ObjectArrayList<PlayerState> crew = new ObjectArrayList<PlayerState>();
			nameText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getRealName() == null ? "NULL_NAME" : f.getRealName();
				}
			});

			crewText.setTextSimple(new Object() {
				long s;

				@Override
				public String toString() {

					if (System.currentTimeMillis() - s > 3000) {
						crew.clear();
						for (PlayerState s : getState().getOnlinePlayersLowerCaseMap().values()) {
							if (s.getAbstractCharacterObject() != null && !s.getAbstractCharacterObject().isHidden() && s.getAbstractCharacterObject().getGravity().source == f) {
								crew.add(s);
							}
						}
						s = System.currentTimeMillis();
					}

					if ((f instanceof SegmentController && f instanceof PlayerControllable && !((PlayerControllable) f).getAttachedPlayers().isEmpty()) || !crew.isEmpty()) {
						if (sb.length() > 0) {
							sb.delete(0, sb.length());
						}

						if ((f instanceof SegmentController && f instanceof PlayerControllable && !((PlayerControllable) f).getAttachedPlayers().isEmpty())) {
							sb.append(f instanceof Ship ? Lng.str("Pilots:") : Lng.str("Builders:"));

							for (int i = 0; i < ((PlayerControllable) f).getAttachedPlayers().size(); i++) {
								sb.append(((PlayerControllable) f).getAttachedPlayers().get(i).getName());
								if (i < ((PlayerControllable) f).getAttachedPlayers().size() - 1) {
									sb.append(", ");
								} else {
									sb.append("; ");
								}
							}
						}

						if (crew.size() > 0) {
							sb.append(f instanceof Ship ? Lng.str("Crew:") : Lng.str("Population:"));

							for (int i = 0; i < crew.size(); i++) {
								sb.append(crew.get(i).getName());
								if (i < crew.size() - 1) {
									sb.append(", ");
								} else {
									sb.append("; ");
								}
							}
						}

						return sb.toString();
					}
					return "";
				}
			});
			crewText.setColor(0.64f, 0.75f, 1.0f, 1.0f);

			distanceText.setTextSimple(new Object() {
				@Override
				public String toString() {
					GameClientState state = getState();
					dir.set(f.getWorldTransformOnClient().origin);
					if (state.getCurrentPlayerObject() != null) {
						dir.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
					} else {
						dir.sub(Controller.getCamera().getPos());
					}
					float camToPosLen = dir.length();
					return StringTools.formatDistance(camToPosLen);
				}
			});
			typeText.setTextSimple(f.getTypeString());

			factionText.setTextSimple(new Object() {
				@Override
				public String toString() {

					if (factionManager.existsFaction(f.getFactionId())) {
						Faction faction = factionManager.getFaction(f.getFactionId());
						return faction.getName();
					}
					return "";

				}
			});

			massText.setTextSimple(new Object() {
				private long lastMass;
				private String mass = "";
				@Override
				public String toString() {
					if(System.currentTimeMillis() - lastMass > 3000){
						if(f instanceof SegmentController){
							SegmentController segController = (SegmentController)f;
							float crm = segController.railController.calculateRailMassIncludingSelf();
//							String calculateRailMass = StringTools.formatSmallAndBig(crm);
//							
//							double cargo = (segController instanceof ManagedSegmentController<?> ? ((ManagedSegmentController<?>)segController).getManagerContainer().volumeTotal : 0);
//							
//							String massStr = Lng.str("Mass: %s (Blocks: %s) ", StringTools.formatSmallAndBig(segController.getTotalPhysicalMass()), StringTools.formatSmallAndBig(segController.getTotalElements()));
//							String cargoString = cargo > 0 ? Lng.str("Cargo: %s", StringTools.massFormat(cargo)) : "";
//							String total =  ? Lng.str("[total /w rail: %s]", calculateRailMass) : "";
//							
//							crm != segController.getTotalPhysicalMass() ? 
							mass = StringTools.massFormat(crm);
						}else{
							mass = StringTools.massFormat(f.getMass());
						}
						lastMass = System.currentTimeMillis();
					}
					return mass;//StringTools.massFormat(f.getMass());
				}
			});
			powerText.setTextSimple(new Object() {
				@Override
				public String toString() {
					GameClientState state = getState();
					if (f instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) f).getManagerContainer() instanceof PowerManagerInterface) {
						return StringTools.massFormat(((PowerManagerInterface) ((ManagedSegmentController<?>) f).getManagerContainer()).getPowerAddOn().getPower());
					} else {
						return "";
					}
				}
			});
			distanceText.setTextSimple(new Object() {
				@Override
				public String toString() {
					GameClientState state = getState();
					dir.set(f.getWorldTransformOnClient().origin);
					if (state.getCurrentPlayerObject() != null) {
						dir.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
					} else {
						dir.sub(Controller.getCamera().getPos());
					}
					float camToPosLen = dir.length();
					return StringTools.formatDistance(camToPosLen);
				}
			});
			shieldsText.setTextSimple(new Object() {
				@Override
				public String toString() {
					GameClientState state = getState();
					if (f instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) f).getManagerContainer() instanceof ShieldContainerInterface) {
						return StringTools.massFormat(((ShieldContainerInterface) ((ManagedSegmentController<?>) f).getManagerContainer()).getShieldAddOn().getShields());
					} else {
						return "";
					}
				}
			});

			int heightInset = 5;
			nameText.getPos().y = 2;
			crewText.getPos().y = 16;
			distanceText.getPos().y = heightInset;
			typeText.getPos().y = heightInset;
			massText.getPos().y = heightInset;
			powerText.getPos().y = heightInset;
			shieldsText.getPos().y = heightInset;
			factionText.getPos().y = heightInset;

			assert (!nameText.getText().isEmpty());
			assert (!typeText.getText().isEmpty());
			assert (!massText.getText().isEmpty());
			assert (!powerText.getText().isEmpty());
			assert (!shieldsText.getText().isEmpty());
			assert (!distanceText.getText().isEmpty());
			assert (!factionText.getText().isEmpty());

			final NavigationRow r = new NavigationRow(getState(), f, indicator, nameAnchorP, typeText, massText, factionAnchorP, distanceText);

//				r.expanded = new GUIElementList(getState());
////
//				GUITextOverlayTable description = new GUITextOverlayTable(10, 10, FontSize.SMALLEST, getState());
//				description.setText(f.descriptionList);
//				description.setPos(4, 2, 0);
//				GUIAncor c = new GUIAncor(getState(), 100, 100);
//				GUITextButton detailsButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, "DETAILS", new GUICallback() {
//					@Override
//					public boolean isOccluded() { return !isActive(); }
//					@Override
//					public void callback(GUIElement callingGuiElement, MouseEvent event) {
//						if(event.pressedLeftMouse()){
//							System.err.println("DETAILS");
//
//
//
//							PlayerOkCancelInput p = new PlayerOkCancelInput(getState(), 400, 400, "Details", "") {
//
//								@Override
//								public void pressedOK() {
//									deactivate();
//								}
//
//								@Override
//								public void onDeactivate() {
//
//								}
//							};
//							p.getInputPanel().setCancelButton(false);
//							p.getInputPanel().setOkButtonText("DONE");
//							p.getInputPanel().onInit();
//							WeaponDescriptionPanel pa = f.getDescriptionPanel(getState(), p.getInputPanel().getContent());
//							p.getInputPanel().getContent().attach(pa);
//							p.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
//						}
//					}
//				});
//
//				c.attach(detailsButton);
//				detailsButton.setPos(0, c.getHeight(), 0);
//				c.attach(description);
//				r.expanded.add(new GUIListElement(c, c, getState()));

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
	protected boolean isFiltered(SimpleTransformableSendableObject e) {
		return super.isFiltered(e) || !getNavigationControlManager().isDisplayed(e);
	}

	public boolean isPlayerAdmin() {
		return getState().getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(getState().getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	public WeaponAssignControllerManager getAssignWeaponControllerManager() {
		return getPlayerGameControlManager().getWeaponControlManager();
	}

	public InShipControlManager getInShipControlManager() {
		return getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private NavigationControllerManager getNavigationControlManager() {
		return getPlayerGameControlManager().getNavigationControlManager();
	}

	private PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	private SimpleTransformableSendableObject getSelected() {
		return getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
	}

	private void setSelectedObj(SimpleTransformableSendableObject s) {
		getPlayerGameControlManager().getPlayerIntercationManager().setSelectedEntity(s);
	}

	private class NavigationRow extends Row {


		public NavigationRow(InputState state, SimpleTransformableSendableObject f, GUIElement... elements) {
			super(state, f, elements);
			this.f = f;

			this.highlightSelect = true;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#isSimpleSelected()
		 */
		@Override
		protected boolean isSimpleSelected() {
			return getSelected() == f;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#clickedOnRow()
		 */
		@Override
		protected void clickedOnRow() {
			if (getSelected() == f) {
				setSelectedObj(null);
			} else {
				setSelectedObj(f);
			}
		}


	}

}
