package org.schema.game.client.view.gui.weapon;

import java.util.List;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WeaponRowElement implements WeaponRowElementInterface {
	
	private static Vector3i absPosTmp = new Vector3i();
	private final SegmentPiece piece;
	public final ElementInformation info;
	public final SegmentController segmentController;
	public final GameClientState state;
	public final List<Object> descriptionList = new ObjectArrayList<Object>();
	public SlotAssignment shipConfiguration;
	public GUIAnchor weaponColumn;
	public GUIAnchor mainSizeColumn;
	public GUIAnchor secondaryColumn;
	public GUIAnchor sizeColumn;
	public GUIAnchor keyColumn;
	public GUIAnchor tertiaryColumn;
	private int key = -1;
	private short supportType;
	private long supportPosL;
	private float supportRatio;
	private int effectSize;
	private float effectRatio;
	private short effectType;
	private long effectPosL;
	private int supportSize;
	private int weaponBlockSize;
	private WeaponSlotOverlayElement weaponIcon;
	private WeaponSlotOverlayElement supportIcon;
	private WeaponSlotOverlayElement effectIcon;

	public WeaponRowElement(SegmentPiece piece) {
		super();
		assert (ElementKeyMap.isValidType(piece.getType()));
		this.piece = piece;
		this.info = ElementKeyMap.getInfo(piece.getType());
		this.state = (GameClientState) piece.getSegment().getSegmentController().getState();
		this.segmentController = piece.getSegment().getSegmentController();
		shipConfiguration = piece.getSegmentController().getSlotAssignment();
		if (segmentController instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) piece.getSegment().getSegmentController()).getManagerContainer();
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = managerContainer.getModulesControllerMap().get(piece.getType());

			if (managerModuleCollection != null) {
				ControlBlockElementCollectionManager<?, ?, ?> collection = ((UsableControllableElementManager<?, ?, ?>) managerModuleCollection.getElementManager()).getCollectionManagersMap().get(piece.getAbsoluteIndex());
				this.weaponBlockSize = collection.getTotalSize();
				if (collection.getSlaveConnectedElement() != Long.MIN_VALUE) {

					short supportType = (short) ElementCollection.getType(collection.getSlaveConnectedElement());
					long supportPosL = ElementCollection.getPosIndexFrom4(collection.getSlaveConnectedElement());

					this.supportType = supportType;
					this.supportPosL = supportPosL;

					ManagerModuleCollection<?, ?, ?> mmc = managerContainer.getModulesControllerMap().get(supportType);
					if (mmc != null) {
						ControlBlockElementCollectionManager<?, ?, ?> addOn = mmc.getCollectionManagersMap().get(supportPosL);
						if (addOn != null) {
							this.supportRatio = CombinationAddOn.getRatio(collection, addOn);
							this.supportSize = addOn.getTotalSize();
						}
					} else {
						System.err.println("WARN: Something is wrong with combi");
					}

				} else {
					//nothing in support
				}

				if (collection.getEffectConnectedElement() != Long.MIN_VALUE) {
					short effectType = (short) ElementCollection.getType(collection.getEffectConnectedElement());
					long effectPosL = ElementCollection.getPosIndexFrom4(collection.getEffectConnectedElement());

					this.effectType = effectType;
					this.effectPosL = effectPosL;

					ManagerModuleCollection<?, ?, ?> mmc = managerContainer.getModulesControllerMap().get(effectType);
					if (mmc != null) {
						ControlBlockElementCollectionManager<?, ?, ?> addOn = mmc.getCollectionManagersMap().get(effectPosL);
						if (addOn != null) {
							float ratio = CombinationAddOn.getRatio(collection, addOn);

							this.effectSize = addOn.getTotalSize();
							this.effectRatio = ratio;

						} else {
							System.err.println("WARN: Something is wrong with combi");
						}
					}
				} else {
				}
			}
		}
		initOverlays();
		updateInfo(piece, descriptionList);
	}

	private void initOverlays() {
		float scale = 0.75f;
		float scaleSlave = 0.65f;
		weaponIcon = new WeaponSlotOverlayElement(state);
		weaponIcon.setScale(scale, scale, scale);
		weaponIcon.setType(piece.getType(), piece.getAbsoluteIndex());
		weaponIcon.setSpriteSubIndex(ElementKeyMap.getInfo(piece.getType()).getBuildIconNum());

		GUITextOverlay nameOverlay = new GUITextOverlay(state);
		if (info.id == ElementKeyMap.LOGIC_REMOTE_INNER) {

			nameOverlay.setTextSimple(new Object() {

				/* (non-Javadoc)
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					long index = piece.getAbsoluteIndexWithType4();
					String tx = piece.getSegmentController().getTextMap().get(index);

					if (tx == null) {
						((ClientSegmentProvider) piece.getSegmentController().getSegmentProvider()).getSendableSegmentProvider().clientTextBlockRequest(index);
						tx = "";
					}
					return tx;
				}

			});
		} else {
			nameOverlay.setTextSimple(info.getName());
		}

		supportIcon = new WeaponSlotOverlayElement(state);
		supportIcon.setScale(scaleSlave, scaleSlave, scaleSlave);
		supportIcon.setSlotStyle((WeaponSlotOverlayElement.SLOT_SUPPORT));
		supportIcon.setTiedToType(piece.getType());
		supportIcon.setTiedToPosIndex(piece.getAbsoluteIndex());
		if (supportType > 0) {
			supportIcon.setType(supportType, supportPosL);
			supportIcon.setSpriteSubIndex(ElementKeyMap.getInfo(supportType).getBuildIconNum());
		} else {
		}

		effectIcon = new WeaponSlotOverlayElement(state);
		effectIcon.setScale(scaleSlave, scaleSlave, scaleSlave);
		effectIcon.setSlotStyle((WeaponSlotOverlayElement.SLOT_EFFECT));
		effectIcon.setTiedToType(piece.getType());
		effectIcon.setTiedToPosIndex(piece.getAbsoluteIndex());
		if (effectType > 0) {
			effectIcon.setType(effectType, effectPosL);
			effectIcon.setSpriteSubIndex(ElementKeyMap.getInfo(effectType).getBuildIconNum());
		}

		if (info.isSupportCombinationControllerB()) {
			supportIcon.setUnloadedHighlightSupport(true);
		}

		if (info.isMainCombinationControllerB()) {
			supportIcon.setUnloadedHighlightSupport(true);
			effectIcon.setUnloadedHighlightEffect(true);
		}

		GUITextOverlay keyTextOverlay = new GUITextOverlay(FontSize.MEDIUM_19, state);
		int key;
		if ((key = shipConfiguration.getByPos(piece.getAbsolutePos(absPosTmp))) >= 0) {
			this.key = key;
			keyTextOverlay.setTextSimple(((key + 1) % 10) + " [" + (((key) / 10) + 1) + "]");
		} else {
			this.key = -1;
			keyTextOverlay.setTextSimple("");
		}
		GUITextOverlay mainSizeText = new GUITextOverlay(FontSize.SMALL_14, state);
		mainSizeText.setTextSimple(String.valueOf(weaponBlockSize));

		GUITextOverlay supportRatioText = new GUITextOverlay(FontSize.SMALL_14, state);
		supportRatioText.setTextSimple(supportRatio > 0 ? Lng.str("%s%% from %d blocks",StringTools.formatPointZero(supportRatio * 100f), supportSize) : "");

		GUITextOverlay effectRatioText = new GUITextOverlay(FontSize.SMALL_14, state);
		effectRatioText.setTextSimple(effectRatio > 0 ? Lng.str("%s%% from %d blocks",StringTools.formatPointZero(effectRatio * 100f), effectSize) : "");

		GUITextOverlay totalSizeText = new GUITextOverlay(FontSize.MEDIUM_18, state);
		totalSizeText.setTextSimple(weaponBlockSize + supportSize + effectSize);

		this.keyColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		keyTextOverlay.setPos(2, 8, 0);
		this.keyColumn.attach(keyTextOverlay);

		mainSizeColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		mainSizeText.setPos(4, 8, 0);
		mainSizeColumn.attach(mainSizeText);

		this.weaponColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		weaponColumn.attach(weaponIcon);
		nameOverlay.setPos(16, 25, 0);
		weaponColumn.attach(nameOverlay);

		this.sizeColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		sizeColumn.attach(totalSizeText);

		this.secondaryColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		supportIcon.setPos(4, 2, 0);
		secondaryColumn.attach(supportIcon);
		supportRatioText.setPos(4, 28, 0);
		secondaryColumn.attach(supportRatioText);

		this.tertiaryColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		effectIcon.setPos(4, 2, 0);
		tertiaryColumn.attach(effectIcon);
		effectRatioText.setPos(4, 28, 0);
		tertiaryColumn.attach(effectRatioText);

	}

	@Override
	public boolean isBlocked() {
		return weaponIcon.isInside() || supportIcon.isInside() || effectIcon.isInside();
	}

	@Override
	public WeaponDescriptionPanel getDescriptionPanel(InputState state, GUIElement dependend) {
		WeaponDescriptionPanel pa = new WeaponDescriptionPanel(state, FontSize.SMALL_14, dependend);

		if (piece != null && piece.getType() != Element.TYPE_NONE) {
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) segmentController).getManagerContainer().getModulesControllerMap().get(piece.getType());
			if (managerModuleCollection != null) {
				ControlBlockElementCollectionManager<?, ?, ?> object = managerModuleCollection.getCollectionManagersMap().get(piece.getAbsoluteIndex());

				if (object != null) {
					pa.update(object);

				}
			}
		}

		return pa;
	}

	private void updateInfo(SegmentPiece updateFor, List<Object> text) {
		text.clear();
		if (updateFor == null) {
			//reset
			return;
		}
		if (piece != null) {
			SegmentController s = segmentController;
			if (s == null) {
				return;
			}
			System.err.println("[WEAPONROW] UPDATE FOR: " + updateFor);
			if (updateFor != null && updateFor.getType() != Element.TYPE_NONE) {
				ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) s).getManagerContainer().getModulesControllerMap().get(updateFor.getType());
				if (managerModuleCollection != null) {
					ControlBlockElementCollectionManager<?, ?, ?> object = managerModuleCollection.getCollectionManagersMap().get(updateFor.getAbsoluteIndex());

					if (object != null) {
						update(object, text);
					}
				}
				if (updateFor.getType() == ElementKeyMap.CORE_ID &&
						updateFor.getSegmentController().getDockingController().getDockedOn() != null) {
					//update docked to
					update("DOCK", text);
				} else if (updateFor.getType() == ElementKeyMap.CORE_ID) {
					//update core beam
					update("CORE", text);
				} else if (updateFor.getType() == ElementKeyMap.LOGIC_REMOTE_INNER) {
					update("INNER_LOGIC", text);
				} else if (updateFor.getType() == ElementKeyMap.RAIL_BLOCK_DOCKER) {
					if (updateFor.getSegmentController().railController.isDocked()) {
						update("UNDOCK", text);
					} else {
						update("RAILBEAM", text);
					}

				}

				if (ElementKeyMap.getInfo(updateFor.getType()).isOldDockable()) {
					for (ElementDocking d : updateFor.getSegment().getSegmentController().getDockingController().getDockedOnThis()) {
						if (d.to.equals(updateFor)) {
							//UPDATE docked to
							update(d.from.getSegment().getSegmentController(), text);
						}
					}

				}
			}
		}
	}

	public void update(ElementCollectionManager<?, ?, ?> selectedManager, List<Object> text) {
		if (selectedManager.getContainer().getSegmentController() != ((GameClientState) selectedManager.getSegmentController().getState()).getCurrentPlayerObject()) {
			//do not update panel for other controllers
			return;
		}
		if (selectedManager instanceof ControlBlockElementCollectionManager<?, ?, ?>) {
			final ControlBlockElementCollectionManager<?, ?, ?> w = (ControlBlockElementCollectionManager<?, ?, ?>) selectedManager;

			StringBuffer b = new StringBuffer();

			final ControlBlockElementCollectionManager<?, ?, ?> supportCol;
			final ControlBlockElementCollectionManager<?, ?, ?> effectCol;

			b.append("Type: " + w.getModuleName() + "\nGroups:		" + w.getElementCollections().size() + "\n");
			if (w.getSlaveConnectedElement() != Long.MIN_VALUE) {
				ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) selectedManager
						.getContainer().getSegmentController()).getManagerContainer().getModulesControllerMap().get((short) ElementCollection.getType(w.getSlaveConnectedElement()));
				ControlBlockElementCollectionManager<?, ?, ?> cb;
				if (managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(w.getSlaveConnectedElement()))) != null) {
					float ratio = CombinationAddOn.getRatio(w, cb);
					b.append("support: \n  " + cb.getModuleName() + " (" + ratio * 100f + "%)\n");
					supportCol = cb;
				} else {
					supportCol = null;
				}
			} else {
				supportCol = null;
			}
			if (w.getEffectConnectedElement() != Long.MIN_VALUE) {
				ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) selectedManager
						.getContainer().getSegmentController()).getManagerContainer().getModulesControllerMap().get((short) ElementCollection.getType(w.getEffectConnectedElement()));
				ControlBlockElementCollectionManager<?, ?, ?> cb;
				if (managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(w.getEffectConnectedElement()))) != null) {
					float ratio = CombinationAddOn.getRatio(w, cb);
					b.append("effect: \n  " + cb.getModuleName() + " (" + ratio * 100f + "%)\n");
					effectCol = cb;
				} else {
					effectCol = null;
				}
			} else {
				effectCol = null;
			}
			text.add(b.toString());

		} else {
			System.err.println("EXCEPTION: UNKNOWN MANAGER: " + selectedManager);
		}

	}

	public void update(SegmentController selectedManager, List<Object> text) {
		if (selectedManager instanceof SegmentController) {
			StringBuffer b = new StringBuffer();
			b.append("Undock " + selectedManager.toNiceString() + "\n" +
							"from you by executing this!\n"
			);
			text.set(0, b.toString());
		}
	}

	public void update(String selectedManager, List<Object> text) {
		if (selectedManager.equals("RAIL")) {
			StringBuffer b = new StringBuffer();
			b.append("Type: 		Rail docking Beam\n"
			);
			text.add(b.toString());
		} else if (selectedManager.equals("CORE")) {
			StringBuffer b = new StringBuffer();
			b.append("Type: 		Docking Beam\n" +
							"Location:		" + Ship.core + "\n"
			);
			text.add(b.toString());
		} else if (selectedManager.equals("DOCK")) {
			StringBuffer b = new StringBuffer();
			b.append("Undock yourself by executing this!\n"
			);
			text.add(b.toString());
		} else if (selectedManager.equals("UNDOCK")) {
			StringBuffer b = new StringBuffer();
			b.append("Undock yourself by executing this!\n"
			);
			text.add(b.toString());
		}
	}
	@Override
	public int getTotalSize(){
		return (weaponBlockSize + supportSize + effectSize);
	}
	@Override
	public int compareTo(WeaponRowElementInterface s) {
			return (s.getTotalSize()) - getTotalSize();
		
	}

	@Override
	public GUIAnchor getWeaponColumn() {
		return weaponColumn;
	}


	@Override
	public GUIAnchor getMainSizeColumn() {
		return mainSizeColumn;
	}


	@Override
	public GUIAnchor getSecondaryColumn() {
		return secondaryColumn;
	}


	@Override
	public GUIAnchor getSizeColumn() {
		return sizeColumn;
	}


	@Override
	public GUIAnchor getKeyColumn() {
		return keyColumn;
	}


	@Override
	public GUIAnchor getTertiaryColumn() {
		return tertiaryColumn;
	}


	@Override
	public List<Object> getDescriptionList() {
		return descriptionList;
	}

	@Override
	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public SegmentPiece getPiece() {
		return piece;
	}
	@Override
	public long getUsableId() {
		return piece.getAbsoluteIndex();
	}

	@Override
	public int getMaxCharges() {
		return 0;
	}

	@Override
	public int getCurrentCharges() {
		return 0;
	}
	
}
