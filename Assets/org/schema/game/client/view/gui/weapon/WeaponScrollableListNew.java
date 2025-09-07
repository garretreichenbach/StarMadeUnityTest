package org.schema.game.client.view.gui.weapon;

import java.util.*;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WeaponScrollableListNew extends ScrollableTableList<WeaponRowElementInterface> implements DropTarget<WeaponSlotOverlayElement> {

	private final List<WeaponRowElementInterface> weaponRowList = new ObjectArrayList<WeaponRowElementInterface>();

	public WeaponScrollableListNew(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		setColumnsHeight(getRowHeight());
		getAssignWeaponControllerManager().addObserver(this);
	}

	public static int getRowHeight() {
		return UIScale.getUIScale().scale(52);
	}

	@Override
	public void checkTarget(MouseEvent e) {
		int inMinX = 208;
		int inMinY = 24;
		int inMaxX = 816;
		int inMaxY = 512;
		if((getRelMousePos().x < inMinX || getRelMousePos().y < inMinY || getRelMousePos().x > inMaxX || getRelMousePos().y > inMaxY) && !(getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponSideBar().isInside())) {
			Draggable dragging = getState().getController().getInputController().getDragging();
			if(dragging != null && isTarget(dragging)) {
				if(dragging.checkDragReleasedMouseEvent(e)) {
					onDrop((WeaponSlotOverlayElement) dragging);
				}
			}
		}
	}

	@Override
	public boolean isTarget(Draggable draggable) {
		return draggable instanceof WeaponSlotOverlayElement;
	}

	@Override
	public void onDrop(WeaponSlotOverlayElement draggable) {
		(new WeaponSlotOverlayElement(getState())).onDrop(draggable);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		getAssignWeaponControllerManager().deleteObserver(this);
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Hotbar"), 1, Comparator.comparingInt(WeaponRowElementInterface::getKey));
		addColumn(Lng.str("System"), 4, Comparator.comparingInt(WeaponRowElementInterface::getKey));
		addFixedWidthColumnScaledUI(Lng.str("Total Size"), 85, Comparator.comparingInt(WeaponRowElementInterface::getKey));
		addFixedWidthColumnScaledUI(Lng.str("Main Size"), 85, Comparator.comparingInt(WeaponRowElementInterface::getKey));
		addColumn(Lng.str("Secondary Slot"), 2, Comparator.comparingInt(WeaponRowElementInterface::getKey));
		addColumn(Lng.str("Tertiary Slot"), 2, Comparator.comparingInt(WeaponRowElementInterface::getKey));
	}

	@Override
	protected Collection<WeaponRowElementInterface> getElementList() {
		reconstructWeaponList();
		return weaponRowList;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<WeaponRowElementInterface> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		// collection is sometimes 1 smaller than the reconstructedWeaponList, see https://phab.starma.de/T2220 which those cases are
		// System.out.println("WeaponSlotOverlay mainList "+ mainList.size() + " collection size " + collection.size() + " weaponRowList size " + getElementList().size());
		List<WeaponRowElementInterface> list = new ArrayList<WeaponRowElementInterface>(getElementList());
		for(final WeaponRowElementInterface f : list) {
			final WeaponRow r = new WeaponRow(getState(), f, f.getKeyColumn(), f.getWeaponColumn(), f.getSizeColumn(), f.getMainSizeColumn(), f.getSecondaryColumn(), f.getTertiaryColumn());
			r.extendableBlockedInterface = f;
			r.expanded = new GUIElementList(getState());
			// 
			final GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
			description.setText(f.getDescriptionList());
			description.setPos(4, 2, 0);
//			GUITextButton detailsButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, "DETAILS", new GUICallback() {
//
//				@Override
//				public void callback(GUIElement callingGuiElement, MouseEvent event) {
//					if (event.pressedLeftMouse()) {
//						PlayerGameOkCancelInput p = new PlayerGameOkCancelInput("WeaponScrollableListNew_DETAILS", getState(), 400, 400, Lng.str("Details"), "") {
//
//							@Override
//							public void onDeactivate() {
//							}
//
//							@Override
//							public void pressedOK() {
//								deactivate();
//							}
//						};
//						p.getInputPanel().setCancelButton(false);
//						p.getInputPanel().setOkButtonText(Lng.str("DONE"));
//						p.getInputPanel().onInit();
//						WeaponDescriptionPanel pa = f.getDescriptionPanel(getState(), p.getInputPanel().getContent());
//						p.getInputPanel().getContent().attach(pa);
//						p.activate();
//						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
//						AudioController.fireAudioEventID(740);
//					}
//				}
//
//				@Override
//				public boolean isOccluded() {
//					return !isActive();
//				}
//			});
			GUIAnchor anchor = new GUIAnchor(getState(), 100.0f, 28.0f) {
				@Override
				public void draw() {
					setWidth(r.getWidth());
					super.draw();
				}
			};

			GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
			buttonPane.onInit();
			buttonPane.addButton(0, 0, Lng.str("DETAILS"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						PlayerGameOkCancelInput p = new PlayerGameOkCancelInput("WeaponScrollableListNew_DETAILS", getState(), 400, 400, Lng.str("Details"), "") {

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
							}
						};
						p.getInputPanel().setCancelButton(false);
						p.getInputPanel().setOkButtonText(Lng.str("DONE"));
						p.getInputPanel().onInit();
						WeaponDescriptionPanel pa = f.getDescriptionPanel(getState(), p.getInputPanel().getContent());
						p.getInputPanel().getContent().attach(pa);
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(740);
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
			buttonPane.addButton(1, 0, Lng.str("SELECT CONTROLLER"), GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {

				public PlayerInteractionControlManager getPlayerInteractionControlManager() {
					return getPlayerGameControlManager().getPlayerIntercationManager();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(f instanceof WeaponRowElement element) {
							SegmentPiece segmentPiece = element.getPiece();
							if(segmentPiece.getSegmentController() instanceof ManagedSegmentController<?> controller) {
								ManagerContainer<?> managerContainer = controller.getManagerContainer();
								ManagerModuleCollection<?, ?, ?> managerModuleCollection = managerContainer.getModulesControllerMap().get(segmentPiece.getType());
								if(managerModuleCollection != null) {
									ControlBlockElementCollectionManager<?, ?, ?> collection = ((UsableControllableElementManager<?, ?, ?>) managerModuleCollection.getElementManager()).getCollectionManagersMap().get(segmentPiece.getAbsoluteIndex());
									getPlayerInteractionControlManager().setSelectedBlockByActiveController(collection.getControllerElement());
								}
							}
						}
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
			anchor.attach(buttonPane);

			GUIAnchor c = new GUIAnchor(getState(), 100, 100) {

				@Override
				public void draw() {
					setWidth(r.getWidth());
					if(description.getTextHeight() == 0) setHeight(r.getHeight());
					else setHeight(description.getTextHeight());
					super.draw();
				}
			};
//			detailsButton.setPos(0, c.getHeight() + 6, 0);
			c.attach(description);
			GUIScrollablePanel scroll = new GUIScrollablePanel(100, 100, getState()) {

				@Override
				public void draw() {
					setWidth(r.bg.getWidth() - 24);
					super.draw();
				}
			};
			scroll.setContent(c);
			scroll.onInit();
			GUIListElement elem = new GUIListElement(scroll, getState());
			elem.heightDiff = 4;
			r.expanded.add(elem);
//			r.expanded.attach(detailsButton);
			r.expanded.attach(anchor);
			buttonPane.setPos(0, c.getHeight() + 4, 0);
			r.onExpanded = new GUIEnterableListOnExtendedCallback() {

				@Override
				public void extended() {
					getAssignWeaponControllerManager().setSelectedPiece(f.getUsableId());
				}

				@Override
				public void collapsed() {
					if(getAssignWeaponControllerManager().getSelectedPiece() == f.getUsableId()) {
						getAssignWeaponControllerManager().setSelectedPiece(Long.MIN_VALUE);
					}
				}
			};
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
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

	public PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private void reconstructWeaponList() {
		weaponRowList.clear();
		if(getInShipControlManager().getEntered() != null) {
			Ship ship = getState().getShip();
			if(ship == null) {
				return;
			}
			SegmentPiece entered = getInShipControlManager().getEntered();
			final long index = entered.getAbsoluteIndex();
			final short type = entered.getType();
			Collection<PlayerUsableInterface> playerUsable = ship.getManagerContainer().getPlayerUsable();
			for(PlayerUsableInterface p : playerUsable) {
				if(p.isPlayerUsable() && p.isControllerConnectedTo(index, type)) {
					WeaponRowElementInterface weaponRow = p.getWeaponRow();
					if(weaponRow != null) {
						weaponRowList.add(weaponRow);
					}
				}
			}
		}
		Collections.sort(weaponRowList);
	}

	private class WeaponRow extends Row {

		public WeaponRow(InputState state, WeaponRowElementInterface f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
		// @Override
		// public float getExtendedHighlightBottomDist() {
		// return 40;
		// }
	}
}
