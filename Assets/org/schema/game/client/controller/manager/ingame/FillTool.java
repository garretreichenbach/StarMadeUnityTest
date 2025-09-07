package org.schema.game.client.controller.manager.ingame;

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.PositionBlockedException;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FillTool {

	public final LongOpenHashSet filling = new LongOpenHashSet();
	public final LongArrayList toPlaced = new LongArrayList();
	public final LongOpenHashSet open = new LongOpenHashSet();
	public final LongArrayList openList = new LongArrayList();
	private final short type;
	private final EditableSendableSegmentController c;
	private final GameClientState state;
	private boolean init;
	public static boolean useSymmetry = true;

	public FillTool(EditableSendableSegmentController c, Vector3i selectedBlock, short type) {
		this.c = c;
		this.state = (GameClientState) c.getState();
//		filling.add(ElementCollection.getIndex(selectedBlock));
//		toPlaced.add(ElementCollection.getIndex(selectedBlock));
		open.add(ElementCollection.getIndex(selectedBlock));
		openList.add(ElementCollection.getIndex(selectedBlock));

//		Vector3i m = new Vector3i();
//		for(int i = 0; i < 6; i++){
//			m.set(selectedBlock);
//			m.add(Element.DIRECTIONSi[i]);
//			long index = ElementCollection.getIndex(m);
//			openList.add(index);
//			open.add(index);
//		}

		this.type = type;

	}

	public void doFill(BuildToolsManager buildToolsManager, short selectedType, int amount) {
		if(!init) {
			if(buildToolsManager.getBuildHelper() != null) {
				buildToolsManager.getBuildHelper().recreateIterator();
			}
			init = true;
		}
		if(type != 0 && selectedType != 0 && !isTypeCompatibleForSelected(selectedType)) {
			buildToolsManager.getState().getController().popupAlertTextMessage(Lng.str("Can't replace this block with that type!"));
			return;
		}

		PlayerInteractionControlManager ppi = buildToolsManager.getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();

		selectedType = ppi.checkCanBuild(c, buildToolsManager.getSymmetryPlanes(), selectedType);
		if(selectedType <= 0) {
			return;
		}

		amount = c.checkAllPlace(selectedType, amount, buildToolsManager.getSymmetryPlanes());
		if(amount <= 0) {
			return;
		}

		if(buildToolsManager.getSymmetryPlanes().getPlaceMode() == 0 && !c.allowedType(selectedType)) {
			return;
		}

		int done = 0;
		Vector3i m = new Vector3i();
		SegmentPiece p = new SegmentPiece();
		while(!openList.isEmpty() && done < amount) {
			long next = openList.removeLong(0);
			SegmentPiece point = c.getSegmentBuffer().getPointUnsave(next, p);

			if(canBePlaced(buildToolsManager, point, next)) {
				if(isTypeCompatibleConsider(point.getType())) {
					filling.add(next);
					toPlaced.add(next);
					done++;
					for(int i = 0; i < 6; i++) {
						ElementCollection.getPosFromIndex(next, m);
						m.add(Element.DIRECTIONSi[i]);
						long index = ElementCollection.getIndex(m);
						if(!open.contains(index) && !filling.contains(index)) {
							openList.add(index);
							open.add(index);
						}
					}
				} else {
//					openList.add(next);
				}
			} else {
				//done++; //dont inc on non-actions
			}
			if(openList.isEmpty() && buildToolsManager.getBuildHelper() != null && buildToolsManager.buildHelperReplace) {
				buildToolsManager.getBuildHelper().iterate(open, openList);

			}
		}

		System.err.println("NOW ON PLACED: " + toPlaced.size() + "; filled: " + filling.size());

	}

	private boolean isTypeCompatibleForSelected(short blockType) {
		if(type == ElementKeyMap.CORE_ID) {
			return false;
		}
		if(!ElementKeyMap.isValidType(type)) {
			return blockType == type;
		} else if(ElementKeyMap.isValidType(blockType)) {
			//true if the type is equal or if they have a common source ID
			ElementInformation block = ElementKeyMap.getInfoFast(blockType);
			ElementInformation selType = ElementKeyMap.getInfoFast(type);
			boolean equalType = blockType == type;
			boolean referenceEqual = (block.getSourceReference() != 0 && block.getSourceReference() == type);
			boolean ownReferenceEqual = (selType.getSourceReference() != 0 && selType.getSourceReference() == blockType);
			boolean remoteReferenceEqual = (selType.getSourceReference() != 0 && selType.getSourceReference() == block.getSourceReference());
//			System.err.println("SEL "+ElementKeyMap.toString(type)+"; cur: "+ElementKeyMap.toString(blockType)+"; SOURCE: "+block.sourceReference+"; "+selType.sourceReference
//					+"; e "+equalType+"; r "+referenceEqual+"; o "+ownReferenceEqual+"; re "+remoteReferenceEqual);
			return equalType ||
					block.blockStyle == selType.blockStyle ||
					block.blockStyle.cube == selType.blockStyle.cube ||
//					referenceEqual || 
//					ownReferenceEqual ||
					remoteReferenceEqual
					;

		} else {
			//type != 0 and what == 0 
			return false;
		}
	}

	private boolean isTypeCompatibleConsider(short blockType) {
		if(!ElementKeyMap.isValidType(type)) {
			return blockType == type;
		} else if(ElementKeyMap.isValidType(blockType)) {
			//true if the type is equal or if they have a common source ID
			ElementInformation block = ElementKeyMap.getInfoFast(blockType);
			ElementInformation selType = ElementKeyMap.getInfoFast(type);
			boolean equalType = blockType == type;
			boolean referenceEqual = (block.getSourceReference() != 0 && block.getSourceReference() == type);
			boolean ownReferenceEqual = (selType.getSourceReference() != 0 && selType.getSourceReference() == blockType);
			boolean remoteReferenceEqual = (selType.getSourceReference() != 0 && selType.getSourceReference() == block.getSourceReference());
//			System.err.println("SEL "+ElementKeyMap.toString(type)+"; cur: "+ElementKeyMap.toString(blockType)+"; SOURCE: "+block.sourceReference+"; "+selType.sourceReference
//					+"; e "+equalType+"; r "+referenceEqual+"; o "+ownReferenceEqual+"; re "+remoteReferenceEqual);
			return equalType ||

					referenceEqual ||
					ownReferenceEqual ||
					remoteReferenceEqual
					;

		} else {
			//type != 0 and what == 0 
			return false;
		}
	}

	private boolean canBePlaced(BuildToolsManager buildToolsManager, SegmentPiece point, long next) {
		return point != null && !filling.contains(next) &&
				(buildToolsManager.getBuildHelper() == null || !buildToolsManager.buildHelperReplace
						|| buildToolsManager.getBuildHelper().contains(next));
	}

	public void place(PlayerInteractionControlManager pim, short selectedType, int orientation, boolean active, BuildInstruction buildInstruction) {
		c.dryBuildTest.boundingBox = new BoundingBox(); //c.getSegmentBuffer().getBoundingBox()

		BuildCallback callback = new BuildCallback() {

			@Override
			public long getSelectedControllerPos() {
				return Long.MIN_VALUE;
			}

			@Override
			public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type) {
			}
		};
		BuildRemoveCallback buildRemoveCallback = new BuildRemoveCallback() {

			@Override
			public long getSelectedControllerPos() {
				return Long.MIN_VALUE;
			}

			@Override
			public void onRemove(long pos, short type) {

			}

			@Override
			public boolean canRemove(short type) {
				if(FillTool.this.type != 0) {
					boolean canPutIn = state.getPlayer().getInventory().canPutIn(type, 1);

					if(!canPutIn) {
						state.getController().popupAlertTextMessage(Lng.str("Can't remove block!\nInventory full"), 0);
					}

					return canPutIn;
				} else {
					return true;
				}
			}
		};
		try {
			SegmentPiece p = new SegmentPiece();
			Vector3i absOnOut = new Vector3i();

			int overallQuantity = state.getPlayer().getInventory().getOverallQuantity(selectedType);
			int[] addedAndRest = new int[]{0, overallQuantity};

			ObjectOpenHashSet<Segment> oo = new ObjectOpenHashSet<Segment>(16);
			for(long pIndex : toPlaced) {

				int toOrientation = orientation;
				boolean toActive = active;

				int x = ElementCollection.getPosX(pIndex);
				int y = ElementCollection.getPosY(pIndex);
				int z = ElementCollection.getPosZ(pIndex);
				if(type == 0) {
					c.dryBuildTest.build(x, y, z,
							selectedType,
							toOrientation,
							toActive,
							callback,
							absOnOut,
							addedAndRest,
							null, buildInstruction);
					c.build(x, y, z,
							selectedType,
							toOrientation,
							toActive,
							callback,
							absOnOut,
							addedAndRest,
							null,
							buildInstruction);
					if(useSymmetry) {
						c.buildInSymmetry(x, y, z,
								selectedType,
								toOrientation,
								toActive,
								callback,
								absOnOut,
								addedAndRest,
								buildInstruction, null, getSymmetryPlanes(), c);
					}
				} else {
					SegmentPiece pointUnsave = c.getSegmentBuffer().getPointUnsave(pIndex, p);
					if(pointUnsave != null && isTypeCompatibleConsider(pointUnsave.getType())) {

						ElementInformation ownInfo = ElementKeyMap.getInfoFast(type);
						ElementInformation info = ElementKeyMap.getInfoFast(pointUnsave.getType());
						ElementInformation selInfo = ElementKeyMap.getInfoFast(selectedType);
						short replaceWith = selectedType;

						if(pointUnsave.getType() != type) {
							if(info.getSourceReference() == ownInfo.getId() && selInfo.blocktypeIds != null) {
								for(short t : selInfo.blocktypeIds) {
									if(ElementKeyMap.getInfoFast(t).blockStyle == info.blockStyle && ElementKeyMap.getInfoFast(t).slab == info.slab) {
										replaceWith = t;
										break;
									}
									
									//If the block we are replacing with has the same source reference as the block we are replacing, that also counts
									if(ElementKeyMap.getInfoFast(t).getSourceReference() == info.getSourceReference()) {
										replaceWith = t;
										break;
									}
								}
							} else {
								continue;
							}
						}

						if(ownInfo.getBlockStyle() == info.getBlockStyle() || ownInfo.getSlab() == info.getSlab()) {
							toOrientation = pointUnsave.getFullOrientation();
						}

						c.remove(x, y, z, buildRemoveCallback, true, oo, pointUnsave.getType(), replaceWith, toOrientation, null, buildInstruction);
						if(useSymmetry) c.removeInSymmetry(x, y, z, buildRemoveCallback, true, oo, pointUnsave.getType(), replaceWith, toOrientation, null, buildInstruction, getSymmetryPlanes());
					}
				}

			}
			if(overallQuantity > 0) {
				System.err.println("[CLIENT] BUILD INSTRUCTION: " + buildInstruction);
				buildInstruction.fillTool = this;
				pim.getUndo().add(0, buildInstruction);
			}

		} catch(PositionBlockedException e) {
			((GameClientState) c.getState()).getController().popupAlertTextMessage(
					Lng.str("One or more blocks\ncould not be placed\nbecause they are blocked\nby another structure"), 0);
		}
		toPlaced.clear();
	}

	public void undo(long index) {
		boolean r = filling.remove(index);
		open.remove(index);
		openList.add(0, index);
		assert (r);
		PlayerInteractionControlManager pi = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		BuildToolsManager buildToolsManager = pi.getBuildToolsManager();

		if(buildToolsManager.getBuildHelper() != null) {
			buildToolsManager.getBuildHelper().recreateIterator();
		}
	}

	public void redo(long index) {
		filling.add(index);
		open.add(index);
		openList.remove(index);
		PlayerInteractionControlManager pi = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		BuildToolsManager buildToolsManager = pi.getBuildToolsManager();
		if(buildToolsManager.getBuildHelper() != null) {
			buildToolsManager.getBuildHelper().recreateIterator();
		}
	}

	public SymmetryPlanes getSymmetryPlanes() {
		PlayerInteractionControlManager pp = GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		if(pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive()) {
			return pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().getSymmetryPlanes();
		} else {
			return pp.getSegmentControlManager().getSegmentBuildController().getSymmetryPlanes();
		}
	}
}
