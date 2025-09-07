package org.schema.game.common.controller.elements.activation;

import api.element.block.Blocks;
import api.listener.events.block.SegmentPieceActivateEvent;
import api.mod.StarLoader;
import api.utils.SegmentPieceUtils;
import api.utils.game.SegmentControllerUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.weapon.MarkerBeam;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import java.util.ArrayList;

public class ActivationCollectionManager extends ControlBlockElementCollectionManager<AbstractUnit, ActivationCollectionManager, ActivationElementManager> implements PlayerUsableInterface {
	public int currentSignal;
	private MarkerBeam destination;

	// private String activatorName;
	public ActivationCollectionManager(SegmentPiece element, SegmentController segController, ActivationElementManager em) {
		super(element, Element.TYPE_ALL, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		if(!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().managerChanged(this);
		}
	}

	@Override
	public boolean hasTag() {
		return destination != null;
	}

	@Override
	public WeaponRowElementInterface getWeaponRow() {
		return super.getWeaponRow();
	}

	@Override
	public boolean isControllerConnectedTo(long index, short type) {
		return type == ElementKeyMap.CORE_ID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ControlBlockElementCollectionManager#applyMetaData(org.schema.game.common.controller.elements.BlockMetaDataDummy)
	 */
	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		assert (destination == null);
		destination = ((ActivationDestMetaDataDummy) dummy).dest;
	}

	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.STRUCT, null, new Tag[] {destination == null ? new Tag(Type.BYTE, null, (byte) 0) : destination.toTag(), FinishTag.INST});
	}

	@Override
	public boolean isPlayerUsable() {
		return getControllerElement().getType() == ElementKeyMap.LOGIC_REMOTE_INNER;
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE && getSegmentController().isOnServer()) {
			((SendableSegmentController) getSegmentController()).activateSwitchSingleServer(getControllerIndex());
			/*AudioController.fireAudioEvent("BLOCK_ACTIVATE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.ACTIVATE }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex4(), 5))*/
			AudioController.fireAudioEventID(872, AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex4(), 5));
			/*AudioController.fireAudioEvent(AudioTags.HUD, AudioTags.BLOCK, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(873);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	public int onActivate(ActivationElementManager man, SegmentPiece piece, boolean oldActive, boolean active) {
		if(!isOnServer()) {
			// remotely called through BlockProcessor SendableSegmentController.getNeedsActiveUpdateClient()
			return 0;
		}
		LongOpenHashSet longOpenHashSet = getSegmentController().getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(getControllerPos()));
		if(longOpenHashSet != null && (longOpenHashSet.size() > 0 && getTotalSize() < longOpenHashSet.size())) {
			System.err.println(getSegmentController().getState() + " " + getSegmentController() + " CANNOT ACTIVATE: totalSize: " + getTotalSize() + " / controlMap: " + (longOpenHashSet != null ? longOpenHashSet.size() : 0) + "::: HashSet: " + longOpenHashSet);
			return -1;
		}
		assert (piece.getAbsoluteIndex() == getControllerElement().getAbsoluteIndex()) : piece + "; " + getControllerElement();
		// System.err.println("-----COLLECTIONMANAGER ACTIVATE: "+piece+" ("+piece.isActive()+" -> "+active+"); "+getSegmentController().getState() + "; "
		// + getSegmentController() + ": COLLECTIONS TO DELIGATE TO: "
		// + getCollection().size());
		if(ElementKeyMap.isValidType(piece.getType())) {
			//INSERTED CODE
			SegmentPieceActivateEvent ev = new SegmentPieceActivateEvent(man, piece, this);
			StarLoader.fireEvent(ev, ev.isServer());
			if(ev.isCanceled()) {
				return 0;
			}
			///
			if(piece.getInfo() != null) {
				ArrayList<SegmentPiece> controlledList = SegmentPieceUtils.getControlledPieces(piece);
				for(SegmentPiece controlled : controlledList) {
					if(Blocks.fromId(controlled.getType()).getPlayerUsableId() != -1) {
						if(controlled.getSegmentController() instanceof ManagedUsableSegmentController<?> managedUsableSegmentController) {
							PlayerUsableInterface usableInterface = SegmentControllerUtils.getAddon(managedUsableSegmentController, Blocks.fromId(controlled.getType()).getPlayerUsableId());
							if(usableInterface instanceof RecharchableActivatableDurationSingleModule usable) {
								if(usable.canExecute()) {
									if(!usable.isActive() && active) usable.executeModule();
									else if(usable.isActive() && !active) usable.deactivateManually();
								} else if(usable.isAutoChargeToggable()) usable.setAutoChargeOn(!usable.isAutoChargeOn());
								break;
							}
						}
					}
				}
			}
			if(!getElementCollections().isEmpty()) {
				ElementCollection<?, ?, ?> ec = getElementCollections().get(0);
				for(int i = 0; i < man.getCollectionManagers().size(); i++) {
					ActivationCollectionManager c = man.getCollectionManagers().get(i);
					for(int j = 0; j < c.getElementCollections().size(); j++) {
						if(c.getElementCollections().get(j).contains(piece.getAbsoluteIndex())) {
							ec = c.getElementCollections().get(j);
							break;
						}
					}
				}
			}
			boolean simple = false;
			if(piece.getType() == ElementKeyMap.SIGNAL_AND_BLOCK_ID) {
				// System.err.println(getSegmentController().getState() +" SINGLE ACTIVATING SIGNAL 'AND' " + piece);
				long pieceIndex = piece.getAbsoluteIndex();
				for(int i = 0; i < man.getCollectionManagers().size(); i++) {
					ActivationCollectionManager c = man.getCollectionManagers().get(i);
					for(int j = 0; j < c.getElementCollections().size(); j++) {
						if(c.getElementCollections().get(j).contains(pieceIndex)) {
							c.getControllerElement().refresh();
							// another activation controller is controlling this
							// element
							active = active && c.getControllerElement().isActive();
						}
					}
				}
			} else if(piece.getType() == ElementKeyMap.SIGNAL_OR_BLOCK_ID) {
				// System.err.println(getSegmentController().getState() +" SINGLE ACTIVATING SIGNAL 'OR' " + piece);
				long pieceIndex = piece.getAbsoluteIndex();
				for(int i = 0; i < man.getCollectionManagers().size(); i++) {
					ActivationCollectionManager c = man.getCollectionManagers().get(i);
					for(int j = 0; j < c.getElementCollections().size(); j++) {
						if(c.getElementCollections().get(j).contains(pieceIndex)) {
							c.getControllerElement().refresh();
							// another activation controller is controlling this
							// element
							active = active || c.getControllerElement().isActive();
						}
					}
				}
			} else if(piece.getType() == ElementKeyMap.SIGNAL_NOT_BLOCK_ID) {
				active = !active;
			} else if(piece.getType() == ElementKeyMap.SIGNAL_RANDOM) {
				active = Math.random() > 0.5;
			} else if(ElementKeyMap.isButton(piece.getType())) {
				// nothing to do here (yet)
			} else if(piece.getType() == ElementKeyMap.LOGIC_FLIP_FLOP) {
				if(active) {
					// on 'false' -> 'true' input this state
					active = !oldActive;
				} else {
					// flip flop only activates if the input is 'true'
					active = oldActive;
				}
			} else if(piece.getType() == ElementKeyMap.LOGIC_WIRELESS) {
				// nothing to do here (yet)
			} else {
				simple = true;
			}
			if(oldActive != active) {
				for(AbstractUnit a : getElementCollections()) {
					a.onActivate(this, man, piece, active);
				}
			}
			// System.err.println("SETTING: "+active);
			currentSignal = ((SendableSegmentController) getSegmentController()).signalId;
			piece.setActive(active);
			//INSERTED CODE @???
			//ev = new SegmentPieceActivateEvent(man, piece, this, Event.Condition.POST);
			//StarLoader.fireEvent(SegmentPieceActivateEvent.class, ev, this.isOnServer());
			///
			return active ? 1 : 0;
		}
		return -1;
	}

	public MarkerBeam getDestination() {
		return destination;
	}

	public void setDestination(MarkerBeam b) {
		this.destination = b;
	}

	@Override
	public boolean isAddToPlayerUsable() {
		return isPlayerUsable();
	}

	@Override
	public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState, PlayerControllable newAttached) {
	}

	@Override
	protected Class<AbstractUnit> getType() {
		return AbstractUnit.class;
	}

	@Override
	public AbstractUnit getInstance() {
		return new AbstractUnit();
	}

	@Override
	public boolean isUsingIntegrity() {
		return false;
	}

	@Override
	public String getModuleName() {
		return Lng.str("Activation System");
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		if(getControllerElement() != null) {
			getControllerElement().refresh();
			boolean a = getControllerElement().isActive();
			h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, a ? Lng.str("Deactivate") : Lng.str("Activate"), hos, ContextFilter.IMPORTANT);
		}
	}
}
