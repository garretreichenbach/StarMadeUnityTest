package org.schema.game.client.controller.manager.ingame;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.tools.SingleBlockDrawer;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.PositionBlockedException;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData3Byte;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.io.*;
import java.util.Collections;
import java.util.Map.Entry;

public class CopyArea {

	public static final String path = "./templates/";
	private static final int VERSION = 6;
	private final ObjectArrayList<VoidSegmentPiece> pieces = new ObjectArrayList<VoidSegmentPiece>();
	private final Long2ObjectOpenHashMap<LongArrayList> connections = new Long2ObjectOpenHashMap<LongArrayList>();
	private final Long2ObjectOpenHashMap<String> textMap = new Long2ObjectOpenHashMap<String>();
	private final Long2ObjectOpenHashMap<Short2IntOpenHashMap> inventoryFilters = new Long2ObjectOpenHashMap<Short2IntOpenHashMap>();
	private final Long2ObjectOpenHashMap<Short2IntOpenHashMap> inventoryFillUpFilters = new Long2ObjectOpenHashMap<Short2IntOpenHashMap>();
	private final Long2ShortOpenHashMap inventoryProduction = new Long2ShortOpenHashMap();
	private final Long2IntOpenHashMap inventoryProductionLimit = new Long2IntOpenHashMap();
	public boolean locked;
	SingleBlockDrawer drawer = new SingleBlockDrawer();
	Transform t = new Transform();
	public Vector3i min;
	public Vector3i max;

	public CopyArea() {
	}

	public static int getRotOrienation(short type, int elementOrientation, int x, int y, int z, Matrix3f rot) {
		if(type != Element.TYPE_NONE) {

			ElementInformation info = ElementKeyMap.getInfo(type);

			if(info.getBlockStyle() != BlockStyle.NORMAL && info.getBlockStyle() != BlockStyle.SPRITE) {

				BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(info.getBlockStyle(), (byte) elementOrientation);

				elementOrientation = algo.findRot(BlockShapeAlgorithm.algorithms[info.getBlockStyle().id - 1], rot);

				assert (BlockShapeAlgorithm.algorithms[info.getBlockStyle().id - 1][elementOrientation] != algo);

			} else {
				try {
					if(elementOrientation >= 6) {
						try {
							throw new RuntimeException("normal block had illegal rotation (only [0,5] allowed): " + elementOrientation);
						} catch(RuntimeException e) {
							e.printStackTrace();
						}
					}
					elementOrientation %= 6;
//					assert(elementOrientation < 6):ElementKeyMap.toString(type)+": "+elementOrientation;
					if(x == 1) {
						elementOrientation = Element.getClockWiseX(elementOrientation);
					} else if(x == -1) {
						elementOrientation = Element.getCounterClockWiseX(elementOrientation);
					} else if(y == 1) {
						elementOrientation = Element.getClockWiseY(elementOrientation);
					} else if(y == -1) {
						elementOrientation = Element.getCounterClockWiseY(elementOrientation);
					} else if(z == 1) {
						elementOrientation = Element.getClockWiseZ(elementOrientation);
					} else if(z == -1) {
						elementOrientation = Element.getCounterClockWiseZ(elementOrientation);
					}
				} catch(RuntimeException e) {
					System.err.println("Exception on rotating: " + ElementKeyMap.toString(type));
					throw e;
				}
			}
		}

		return elementOrientation;
	}

	public void rotate(int x, int y, int z) {
		if(locked) return;
		Matrix3f rot = new Matrix3f();

		if(x != 0) {
			rot.rotX(FastMath.HALF_PI * x);
		} else if(y != 0) {
			rot.rotY(FastMath.HALF_PI * y);
		} else if(z != 0) {
			rot.rotZ(FastMath.HALF_PI * z);
		}

		Vector3f rotMin = new Vector3f(min.x, min.y, min.z);
		Vector3f rotMax = new Vector3f(max.x, max.y, max.z);

		rot.transform(rotMin);
		rot.transform(rotMax);

		Vector3i newMin = new Vector3i(FastMath.round(Math.min(rotMin.x, rotMax.x)), FastMath.round(Math.min(rotMin.y, rotMax.y)), FastMath.round(Math.min(rotMin.z, rotMax.z)));
		Vector3i newMax = new Vector3i(FastMath.round(Math.max(rotMin.x, rotMax.x)), FastMath.round(Math.max(rotMin.y, rotMax.y)), FastMath.round(Math.max(rotMin.z, rotMax.z)));
		Vector3f rotPos = new Vector3f();
		Long2ObjectOpenHashMap<String> newText = new Long2ObjectOpenHashMap<String>(textMap.size());

		for(int i = 0; i < pieces.size(); i++) {
			VoidSegmentPiece p = pieces.get(i);

			rotPos.set(p.voidPos.x, p.voidPos.y, p.voidPos.z);
			rot.transform(rotPos);

			String text = null;
			if(textMap.containsKey(ElementCollection.getIndex(p.voidPos))) {
				text = textMap.get(ElementCollection.getIndex(p.voidPos));
			}

			p.voidPos.set(FastMath.round(rotPos.x), FastMath.round(rotPos.y), FastMath.round(rotPos.z));

			int rotOrienation = getRotOrienation(p.getType(), p.getOrientation(), x, y, z, rot);
//			System.err.println("[CLIENT] ROTATION: "+rotOrienation);

			if(ElementKeyMap.getInfo(p.getType()).getBlockStyle() != BlockStyle.NORMAL && ElementKeyMap.getInfo(p.getType()).getBlockStyle() != BlockStyle.SPRITE) {
//				if(rotOrienation >= 16){
//					rotOrienation = (rotOrienation + 8) % 24;
//				}
//				if (rotOrienation >= SegmentData.MAX_ORIENT) {
//					p.setActive(false);
//					rotOrienation -= SegmentData.MAX_ORIENT;
//				} else {
//					p.setActive(true);
//				}
			}

			p.setOrientation((byte) rotOrienation);

			if(text != null) {
				newText.put(ElementCollection.getIndex(p.voidPos), text);
			}

		}
		min.set(newMin);
		max.set(newMax);

		Long2ObjectOpenHashMap<LongArrayList> newConnections = new Long2ObjectOpenHashMap<LongArrayList>(connections.size());

		Vector3f rotPosTo = new Vector3f();
		Vector3i posTo = new Vector3i();
		Vector3f rotPosFrom = new Vector3f();
		Vector3i posFrom = new Vector3i();

		for(Entry<Long, LongArrayList> a : connections.entrySet()) {
			ElementCollection.getPosFromIndex(a.getKey(), rotPosTo);
			rot.transform(rotPosTo);
			posTo.set(FastMath.round(rotPosTo.x), FastMath.round(rotPosTo.y), FastMath.round(rotPosTo.z));
			LongArrayList longArrayList = new LongArrayList(a.getValue().size());
			newConnections.put(ElementCollection.getIndex(posTo), longArrayList);

			for(int i = 0; i < a.getValue().size(); i++) {
				ElementCollection.getPosFromIndex(a.getValue().getLong(i), rotPosFrom);
				rot.transform(rotPosFrom);
				posFrom.set(FastMath.round(rotPosFrom.x), FastMath.round(rotPosFrom.y), FastMath.round(rotPosFrom.z));
				longArrayList.add(ElementCollection.getIndex(posFrom));
			}
		}
		connections.clear();
		connections.putAll(newConnections);
		textMap.clear();
		textMap.putAll(newText);
	}

	public void draw() {

		if(EngineSettings.G_DRAW_PASTE_PREVIEW.isOn()) {
			t.setIdentity();

			for(int i = 0; i < pieces.size(); i++) {

				VoidSegmentPiece p = pieces.get(i);
				if(ElementKeyMap.isValidType(p.getType())) {
					GlUtil.glPushMatrix();

					GlUtil.translateModelview(p.voidPos.x, p.voidPos.y, p.voidPos.z);

					ElementInformation info = ElementKeyMap.getInfo(p.getType());
					if(info.getBlockStyle() != BlockStyle.NORMAL) {

						drawer.setSidedOrientation((byte) 0);
						drawer.setShapeOrientation24(BlockShapeAlgorithm.getLocalAlgoIndex(info.getBlockStyle(), p.getOrientation()));

					} else if(info.getIndividualSides() > 3) {
						drawer.setShapeOrientation24((byte) 0);
						drawer.setSidedOrientation(p.getOrientation());
					} else if(info.orientatable) {
						drawer.setShapeOrientation24((byte) 0);
						drawer.setSidedOrientation(p.getOrientation());
					} else {
						drawer.setShapeOrientation24((byte) 0);
						drawer.setSidedOrientation((byte) 0);
					}
					drawer.alpha = 0.5f;
					drawer.setActive(p.isActive());
					drawer.useSpriteIcons = false;
					drawer.drawType(p.getType(), t);
					drawer.useSpriteIcons = true;

					GlUtil.glPopMatrix();
				}
			}
		}
	}

	public void copyArea(SegmentController segmentController, Vector3i min, Vector3i max) {
		if(locked) return;
		this.min = new Vector3i(min);
		this.max = new Vector3i(max);
		System.err.println("[CLIENT][COPYPASTE] RECORDING AREA");
		ManagerContainer<?> managerContainer = null;
		if(segmentController instanceof ManagedSegmentController<?>) {
			managerContainer = ((ManagedSegmentController<?>) segmentController).getManagerContainer();
		}
		for(int z = min.z; z <= max.z; z++) {
			for(int y = min.y; y <= max.y; y++) {
				for(int x = min.x; x <= max.x; x++) {

					SegmentPiece p = segmentController.getSegmentBuffer().getPointUnsave(ElementCollection.getIndex(x, y, z));
					if(p != null && p.getType() != Element.TYPE_NONE) {
						VoidSegmentPiece vp = new VoidSegmentPiece();
						vp.setDataByReference(p.getData());
						p.getAbsolutePos(vp.voidPos);
						vp.voidPos.sub(min);
						pieces.add(vp);
						if(p.getType() == ElementKeyMap.STASH_ELEMENT) {

						}
//						System.err.println("[CLIENT][COPYPASTE] recorded: pos: " + x + ", " + y + ", " + z + ", relativeToAreaPos: " + vp.voidPos + "; " + ElementKeyMap.toString(vp.getType()));

						long indexRelative = ElementCollection.getIndex(vp.voidPos);
						long indexInSegController = ElementCollection.getIndex4((short) x, (short) y, (short) z, vp.getOrientation());
						String text = segmentController.getTextMap().get(indexInSegController);

						if(text != null) {
							textMap.put(indexRelative, text);
						}
						if(managerContainer != null) {
							Inventory inventory = managerContainer.getInventory(indexInSegController);
							if(inventory != null) {
								if(inventory.getProduction() != 0) {
									inventoryProduction.put(indexRelative, inventory.getProduction());
								}
								if(inventory.getProductionLimit() != 0) {
									inventoryProductionLimit.put(indexRelative, inventory.getProductionLimit());
								}
								if(inventory.getFilter() != null) {
									inventoryFilters.put(indexRelative, inventory.getFilter().filter.getMapInstance());
									inventoryFillUpFilters.put(indexRelative, inventory.getFilter().fillUpTo.getMapInstance());
								}
							}
						}
						LongOpenHashSet longOpenHashSet = segmentController.getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(x, y, z));
						if(longOpenHashSet != null) {
							vp.senderId = -2;
							for(long l : longOpenHashSet) {
								int cx = ElementCollection.getPosX(l);
								int cy = ElementCollection.getPosY(l);
								int cz = ElementCollection.getPosZ(l);
								if((cx >= min.x && cx <= max.x) && (cy >= min.y && cy <= max.y) && (cz >= min.z && cz <= max.z)) {
									//									System.err.println("[CLIENT][COPYPASTE] Recorded connection: "+cx+", "+cy+", "+cz+" <- "+vp.voidPos);
									LongArrayList put = connections.get(ElementCollection.getIndex(cx - min.x, cy - min.y, cz - min.z));
									if(put == null) {
										put = new LongArrayList();
										connections.put(ElementCollection.getIndex(cx - min.x, cy - min.y, cz - min.z), put);
									}
									put.add(ElementCollection.getIndex(vp.voidPos));
									//									assert(put == 0 || put == ElementCollection.getIndex(vp.voidPos)):put+"; "+ElementCollection.getIndex(vp.voidPos);

								}
							}
						}
					}
				}
			}
		}

		Collections.sort(pieces, (o1, o2) -> o1.senderId - o2.senderId);

		if(!pieces.isEmpty()) {
			pieces.trim();
		}
		System.err.println("[CLIENT][COPYPASTE] " + pieces.size() + " blocks recorded");
	}

	/**
	 * @return the pieces
	 */
	public ObjectArrayList<VoidSegmentPiece> getPieces() {
		return pieces;
	}

	public Vector3i getSize() {
		return new Vector3i(max.x - min.x, max.y - min.y, max.z - min.z);
	}

	public Vector3f getSizef() {
		return new Vector3f(max.x - min.x, max.y - min.y, max.z - min.z);
	}

	private boolean ok(EditableSendableSegmentController c, VoidSegmentPiece p) {
		if(p.getType() == ElementKeyMap.FACTION_BLOCK) {
			if(c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0) {
				((GameClientState) c.getState()).getController().popupAlertTextMessage(
						Lng.str("ERROR\nOnly one Faction block is permitted\nper structure."), 0);
			}
			return false;
		} else if(p.getType() == ElementKeyMap.AI_ELEMENT) {
			if(c.getElementClassCountMap().get(ElementKeyMap.AI_ELEMENT) > 0) {
				((GameClientState) c.getState()).getController().popupAlertTextMessage(
						Lng.str("ERROR\nOnly one AI block is permitted\nper structure."), 0);
			}
			return false;
		} else if(p.getType() == ElementKeyMap.SHOP_BLOCK_ID) {
			if(c.getElementClassCountMap().get(ElementKeyMap.SHOP_BLOCK_ID) > 0) {
				((GameClientState) c.getState()).getController().popupAlertTextMessage(
						Lng.str("ERROR\nOnly one Shop block is permitted\nper structure."), 0);
			}
			return false;
		} else if(p.getType() == ElementKeyMap.CORE_ID) {
			((GameClientState) c.getState()).getController().popupAlertTextMessage(
					Lng.str("ERROR\nOnly one Core block is permitted\nper structure."), 0);
			return false;
		} else if(!c.allowedType(p.getType())) {
			((GameClientState) c.getState()).getController().popupAlertTextMessage(
					Lng.str("ERROR\nType not allowed here:\n%s",
							ElementKeyMap.toString(p.getType())), 0);
			return false;
		}
		return true;
	}

	private class BuildCB implements BuildCallback {
		private Short2IntOpenHashMap inventoryFilter = null;
		private short inventoryProduction = 0;
		private final SegmentController segmentController;

		private final LongOpenHashSet built = new LongOpenHashSet();
		public Short2IntOpenHashMap inventoryFillUpFilters = null;
		public int inventoryProductionLimit;

		public BuildCB(SegmentController segmentController) {
			super();
			this.segmentController = segmentController;
		}

		@Override
		public long getSelectedControllerPos() {
			return Long.MIN_VALUE;
		}

		@Override
		public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild,
		                    short type) {
			ManagerContainer<?> managerContainer = null;
			if(segmentController instanceof ManagedSegmentController<?>) {
				managerContainer = ((ManagedSegmentController<?>) segmentController).getManagerContainer();
				if(inventoryFilter != null || inventoryProduction != 0 || inventoryFillUpFilters != null || inventoryProductionLimit != 0) {
					managerContainer.addDelayedProductionAndFilterClientSet(posBuilt, inventoryFilter, inventoryFillUpFilters, inventoryProduction, inventoryProductionLimit);
				}
			}
			built.add(ElementCollection.getIndex(posBuilt));

		}

	}

	public void build(EditableSendableSegmentController c, Vector3i pos, BuildInstruction buildInstruction, SymmetryPlanes symmetryPlanes) {
		int maxArea = ((GameClientState) c.getState()).getGameState().getMaxBuildArea();
		Vector3f s = getSizef();
		if(s.x > maxArea || s.y > maxArea || s.z > maxArea) {
			((GameClientState) c.getState()).getController().popupAlertTextMessage(Lng.str("Cannot paste!\nTemplate size is too big\nfor this server."), 0);
			return;
		}

		BuildCB callback = new BuildCB(c);
		Vector3i absOnOut = new Vector3i();

		long t = System.currentTimeMillis();

		Short2IntOpenHashMap countsToBuild = new Short2IntOpenHashMap();
		Vector3i bbMin = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		Vector3i bbMax = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		for(VoidSegmentPiece p : pieces) {
			countsToBuild.addTo(p.getType(), 1);

			bbMin.min(p.voidPos.x, p.voidPos.y, p.voidPos.z);
			bbMax.max(p.voidPos.x, p.voidPos.y, p.voidPos.z);
		}

		Vector3i ns = new Vector3i(bbMax.x - bbMin.x, bbMax.y - bbMin.y, bbMax.z - bbMin.z);
		if(ns.x > maxArea || ns.y > maxArea || ns.z > maxArea) {
			System.err.println("[CLIENT][WARNING] PASTE DENIED BY ACTUAL BLOCKS PLACED. POSSIBLY MODIFIED TEMPLATE TRIED TO PLACE: size from original: " + s + "; Read size from blocks: " + ns + ";              BB " + bbMin + "; " + bbMax);
			((GameClientState) c.getState()).getController().popupAlertTextMessage(Lng.str("Cannot paste!\nTemplate size is too big\nfor this server.\nPossily modified template used."), 0);
			return;
		}

		for(it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry e : countsToBuild.short2IntEntrySet()) {
			Inventory inventory = ((GameClientState) c.getState()).getPlayer().getInventory();

			short type = ElementKeyMap.convertSourceReference(e.getShortKey());

			if(inventory.getOverallQuantity(type) < e.getIntValue()) {
				((GameClientState) c.getState()).getController().popupAlertTextMessage(Lng.str("Paste not complete:\nnot enough blocks!"), 0);
				break;
			}
		}
		int[] addedAndRest = new int[]{0, Integer.MAX_VALUE};

		c.dryBuildTest.boundingBox = new BoundingBox(); //c.getSegmentBuffer().getBoundingBox()

		try {
			for(VoidSegmentPiece p : pieces) {
				if(ElementKeyMap.getInfo(p.getType()).resourceInjection != ResourceInjectionType.OFF) {
					//set injected resource to 0
					p.setOrientation((byte) 0);
				}
				if(ElementKeyMap.getInfo(p.getType()).isReactorChamberSpecific()) {
					p.setType((short) ElementKeyMap.getInfo(p.getType()).chamberRoot);
				}
				c.dryBuildTest.build(pos.x + p.voidPos.x, pos.y + p.voidPos.y, pos.z + p.voidPos.z,
						p.getType(),
						p.getOrientation(),
						p.isActive(),
						callback,
						absOnOut,
						addedAndRest,
						null, buildInstruction);
				c.buildInSymmetry(pos.x + p.voidPos.x, pos.y + p.voidPos.y, pos.z + p.voidPos.z,
						p.getType(),
						p.getOrientation(),
						p.isActive(),
						callback,
						absOnOut,
						addedAndRest,
						buildInstruction, null, symmetryPlanes, c.dryBuildTest);
			}
		} catch(PositionBlockedException e) {
			((GameClientState) c.getState()).getController().popupAlertTextMessage(
					Lng.str("One or more blocks\ncould not be placed\nbecause they are blocked\nby another structure"), 0);
			return;
		}

		c.dryBuildTest.boundingBox.min.x -= Segment.HALF_DIM;
		c.dryBuildTest.boundingBox.min.y -= Segment.HALF_DIM;
		c.dryBuildTest.boundingBox.min.z -= Segment.HALF_DIM;

		c.dryBuildTest.boundingBox.max.x -= Segment.HALF_DIM;
		c.dryBuildTest.boundingBox.max.y -= Segment.HALF_DIM;
		c.dryBuildTest.boundingBox.max.z -= Segment.HALF_DIM;

		if(!ServerConfig.ALLOW_PASTE_AABB_OVERLAPPING.isOn()) {
			boolean boundingBoxOverlapping = c.getCollisionChecker().checkAABBCollisionWithUnrelatedStructures(c.getWorldTransformOnClient(), c.dryBuildTest.boundingBox.min, c.dryBuildTest.boundingBox.max, 0.1f);

			if(boundingBoxOverlapping) {
				System.err.println("[CLIENT] Overlapping paste BB. " + c + "; " + c.dryBuildTest.boundingBox);
				((GameClientState) c.getState()).getController().popupAlertTextMessage(
						Lng.str("To prevent buiding exploitation,\nyou cannot paste that close to any structure that is not docked to this one."), 0);
				return;
			}
		}

		for(VoidSegmentPiece p : pieces) {
//			System.err.println("[CLIENT][COPYPASTE] BUILDING PASTE: " + pos.x + p.voidPos.x + ", " + pos.y + p.voidPos.y + ", " + pos.z + p.voidPos.z + "; Type: " + ElementKeyMap.toString(p.getType()));

			if(!ok(c, p)) {
				continue;
			}

			if(ElementKeyMap.isValidType(p.getType())) {

				long normPos = ElementCollection.getIndex(p.voidPos);
				callback.inventoryFilter = inventoryFilters.get(normPos);
				callback.inventoryFillUpFilters = inventoryFillUpFilters.get(normPos);
				callback.inventoryProduction = inventoryProduction.get(normPos);
				callback.inventoryProductionLimit = inventoryProductionLimit.get(normPos);

				c.build(pos.x + p.voidPos.x, pos.y + p.voidPos.y, pos.z + p.voidPos.z,
						p.getType(),
						p.getOrientation(),
						p.isActive(),
						callback,
						absOnOut,
						addedAndRest,
						null, buildInstruction);

				c.buildInSymmetry(pos.x + p.voidPos.x, pos.y + p.voidPos.y, pos.z + p.voidPos.z,
						p.getType(),
						p.getOrientation(),
						p.isActive(),
						callback,
						absOnOut,
						addedAndRest,
						buildInstruction, null, symmetryPlanes, c);

			}
		}
		long takeBuild = System.currentTimeMillis() - t;
		System.err.println("[COPYAREA] Build done in " + takeBuild + " ms. Now connecting necessary blocks");
		Long2ObjectOpenHashMap<LongArrayList> copyOfConnectionFrom = new Long2ObjectOpenHashMap<LongArrayList>();
		boolean overlap = false;
		copyOfConnectionFrom.putAll(connections);
		Vector3i cnTmp = new Vector3i();

		int size = 0;
		for(Entry<Long, FastCopyLongOpenHashSet> e : c.getControlElementMap().getControllingMap().getAll().entrySet()) {
			size += e.getValue().size();
		}
		LongOpenHashSet cons = new LongOpenHashSet(size);
		for(Entry<Long, FastCopyLongOpenHashSet> e : c.getControlElementMap().getControllingMap().getAll().entrySet()) {
			for(long l : e.getValue()) {
				long existingControllerIndex = ElementCollection.getPosIndexFrom4(l);
				cons.add(existingControllerIndex);
			}
		}

		for(VoidSegmentPiece p : pieces) {
			if(!ok(c, p)) {
				continue;
			}
			long normPos = ElementCollection.getIndex(p.voidPos);

			if(connections.containsKey(normPos)) {
				LongArrayList longArrayList = connections.get(normPos);
				for(long index : longArrayList) {
					Vector3i controlled = cnTmp;
					controlled.add(p.voidPos, pos);
					long controlledIndex = ElementCollection.getIndex(controlled);
					if(cons.contains(controlledIndex)) {
						// If the block being connected already is connected to another controller, don't add
						// it to another.
						copyOfConnectionFrom.remove(normPos);
						overlap = true;
					}

				}
			}
		}

		if(overlap) {
			// Warn players that modules weren't connected
			String desc = Lng.str("some");
			if(copyOfConnectionFrom.isEmpty()) {
				desc = Lng.str("all");
			}
			((GameClientState) c.getState()).getController().popupGameTextMessage(Lng.str("Warning: %s module connections\nignored due to overlapping blocks.", desc), 0.0F);
		}

		for(VoidSegmentPiece p : pieces) {
			if(!ok(c, p)) {
				continue;
			}
			long normPos = ElementCollection.getIndex(p.voidPos);

			if(copyOfConnectionFrom.containsKey(normPos)) {

				LongArrayList connectionsTo = copyOfConnectionFrom.get(normPos);
				for(long index : connectionsTo) {
					Vector3i controller = ElementCollection.getPosFromIndex(index, new Vector3i());
					controller.add(pos);

					Vector3i controlled = new Vector3i(p.voidPos);
					controlled.add(pos);

					SegmentPiece piece = c.getSegmentBuffer().getPointUnsave(controller);

					if(piece == null) {
						((GameClientState) c.getState()).getController().popupAlertTextMessage(Lng.str("ERROR: \nThe contoller to a block in this paste\nis not initialized yet!\nSkipping block...\nPlease wait for the structure to fully load!"), 0);
						continue;
					}
					// Only combine 'controlled' list when type isn't a controller. Logic can have
					// multiple 'controlled' blocks, but computer should not.
					boolean isComputer = piece.getType() == ElementKeyMap.WEAPON_CONTROLLER_ID ||
							piece.getType() == ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID ||
							piece.getType() == ElementKeyMap.DAMAGE_BEAM_COMPUTER ||
							piece.getType() == ElementKeyMap.DAMAGE_PULSE_COMPUTER ||
							piece.getType() == ElementKeyMap.SALVAGE_CONTROLLER_ID ||
							piece.getType() == ElementKeyMap.REPAIR_CONTROLLER_ID ||
							piece.getType() == ElementKeyMap.SHIELD_DRAIN_CONTROLLER_ID ||
							piece.getType() == ElementKeyMap.SHIELD_SUPPLY_CONTROLLER_ID ||
							piece.getType() == ElementKeyMap.POWER_SUPPLY_BEAM_COMPUTER ||
							piece.getType() == ElementKeyMap.POWER_DRAIN_BEAM_COMPUTER ||
							piece.getType() == ElementKeyMap.PUSH_PULSE_CONTROLLER_ID;

					System.err.println("[COPYAREA] Connecting " + controller + " to " + p.voidPos + ";  Controllerpiece: " + piece);
					addControlConnection(ElementCollection.getIndex(controller), ElementCollection.getIndex(controlled), copyOfConnectionFrom, !isComputer);

					connectSymetry(controller, controlled, p.getType(), symmetryPlanes, copyOfConnectionFrom, !isComputer);

				}
			}
		}
		c.getBlockProcessor().connectionsToAddFromPaste.putAll(copyOfConnectionFrom);

		for(VoidSegmentPiece p : pieces) {
			if(!ok(c, p)) {
				continue;
			}
			long normPos = ElementCollection.getIndex(p.voidPos);
			if(textMap.containsKey(normPos)) {
				c.getBlockProcessor().textToAddFromPaste.put(ElementCollection.getIndex(pos.x + p.voidPos.x, pos.y + p.voidPos.y, pos.z + p.voidPos.z), textMap.get(normPos));
				Vector3i modulePos = new Vector3i(p.voidPos);
				modulePos.add(pos);
				addSymmetryText(modulePos, textMap.get(normPos), symmetryPlanes, c.getBlockProcessor().textToAddFromPaste);
			}
		}
	}

	public void addSymmetryText(Vector3i textModuleOrig, String text, SymmetryPlanes symmetryPlanes, Long2ObjectOpenHashMap<String> copyCon) {
		long controller;
		if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			//XY
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int distXY = (planePosXY - textModuleOrig.z) * 2 + symmetryPlanes.getXyExtraDist();

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			//XZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int distXZ = (planePosXZ - textModuleOrig.y) * 2 + symmetryPlanes.getXzExtraDist();

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y + distXZ, textModuleOrig.z);

			addSymText(controller, text, copyCon);

		} else if(!symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//YZ
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distYZ = (planePosYZ - textModuleOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y, textModuleOrig.z);

			addSymText(controller, text, copyCon);

		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			//XY XZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;

			int distXY = (planePosXY - textModuleOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - textModuleOrig.y) * 2 + symmetryPlanes.getXzExtraDist();

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y + distXZ, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y + distXZ, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

		} else if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//XY YZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distXY = (planePosXY - textModuleOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distYZ = (planePosYZ - textModuleOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//XZ YZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distXZ = (planePosXZ - textModuleOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - textModuleOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y + distXZ, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y + distXZ, textModuleOrig.z);

			addSymText(controller, text, copyCon);

		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//ALL
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distXY = (planePosXY - textModuleOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - textModuleOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - textModuleOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			//single
			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y + distXZ, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			//doubles

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y + distXZ, textModuleOrig.z);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

			controller = ElementCollection.getIndex(textModuleOrig.x, textModuleOrig.y + distXZ, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);

			//last
			controller = ElementCollection.getIndex(textModuleOrig.x + distYZ, textModuleOrig.y + distXZ, textModuleOrig.z + distXY);

			addSymText(controller, text, copyCon);
		}
	}

	public void connectSymetry(Vector3i controllerOrig, Vector3i controlledOrig, short controlledType, SymmetryPlanes symmetryPlanes, Long2ObjectOpenHashMap<LongArrayList> copyCon, boolean combine) {
		long controller;
		long controlled;
		if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			//XY
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int distXY = (planePosXY - controllerOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXYControlled = (planePosXY - controlledOrig.z) * 2 + symmetryPlanes.getXyExtraDist();

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			//XZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int distXZ = (planePosXZ - controllerOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distXZControlled = (planePosXZ - controlledOrig.y) * 2 + symmetryPlanes.getXzExtraDist();

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y + distXZ, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y + distXZControlled, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

		} else if(!symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//YZ
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distYZ = (planePosYZ - controllerOrig.x) * 2 + symmetryPlanes.getYzExtraDist();
			int distYZControlled = (planePosYZ - controlledOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			//XY XZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;

			int distXY = (planePosXY - controllerOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - controllerOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distXYControlled = (planePosXY - controlledOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZControlled = (planePosXZ - controlledOrig.y) * 2 + symmetryPlanes.getXzExtraDist();

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y + distXZ, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y + distXZControlled, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y + distXZ, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y + distXZControlled, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

		} else if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//XY YZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distXY = (planePosXY - controllerOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distYZ = (planePosYZ - controllerOrig.x) * 2 + symmetryPlanes.getYzExtraDist();
			int distXYControlled = (planePosXY - controlledOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distYZControlled = (planePosYZ - controlledOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//XZ YZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distXZ = (planePosXZ - controllerOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - controllerOrig.x) * 2 + symmetryPlanes.getYzExtraDist();
			int distXZControlled = (planePosXZ - controlledOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZControlled = (planePosYZ - controlledOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y + distXZ, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y + distXZControlled, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y + distXZ, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y + distXZControlled, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

//			build(x, y+distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, false, true, false), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//			build(x+distYZ, y, z, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, false, false, true), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//			build(x+distYZ, y+distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, false, true, true), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);

		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//ALL
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;

			int distXY = (planePosXY - controllerOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - controllerOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - controllerOrig.x) * 2 + symmetryPlanes.getYzExtraDist();
			int distXYControlled = (planePosXY - controlledOrig.z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZControlled = (planePosXZ - controlledOrig.y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZControlled = (planePosYZ - controlledOrig.x) * 2 + symmetryPlanes.getYzExtraDist();

			//single
			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y + distXZ, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y + distXZControlled, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			//doubles

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y + distXZ, controllerOrig.z);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y + distXZControlled, controlledOrig.z);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

			controller = ElementCollection.getIndex(controllerOrig.x, controllerOrig.y + distXZ, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x, controlledOrig.y + distXZControlled, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

			//last
			controller = ElementCollection.getIndex(controllerOrig.x + distYZ, controllerOrig.y + distXZ, controllerOrig.z + distXY);
			controlled = ElementCollection.getIndex(controlledOrig.x + distYZControlled, controlledOrig.y + distXZControlled, controlledOrig.z + distXYControlled);

			addControlConnection(controller, controlled, copyCon, combine);

//			build(x+distYZ, y, z, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, false, false, true), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//			build(x, y+distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, false, true, false), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//			build(x, y, z+distXY, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, true, false, false), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//
//			build(x+distYZ, y+distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, false, true, true), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//			build(x+distYZ, y, z+distXY, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, true, false, true), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//			build(x, y+distXZ, z+distXY, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, true, true, false), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);
//
//			build(x+distYZ, y+distXZ, z+distXY, type, symmetryPlanes.getMirrorOrientation(type, activateBlock, elementOrientation, true, true, true), activateBlock, callback, absOnOut, addedAndRest, buildInstruction);

		}
	}

	private void addSymText(long module, String text, Long2ObjectOpenHashMap<String> copyCon) {
		copyCon.put(module, text);
	}

	private void addControlConnection(long controller, long controlled, Long2ObjectOpenHashMap<LongArrayList> copyCon, boolean combine) {
		if(!combine && copyCon.containsKey(controlled)) {
			// If the controlled block is already in the map, it means it's already connected to a controller.
			// We don't want to have a controllable block connected to multiple controllers.
			return;
		}

		LongArrayList longArrayList = copyCon.get(controlled);
		if(longArrayList == null) {
			longArrayList = new LongArrayList();
			copyCon.put(controlled, longArrayList);
		}

		longArrayList.add(controller);
	}

	public void save(String name) throws IOException {
		File p = new FileExt(path);
		p.mkdirs();

		String fullPath = path + name + ".smtpl";

		File to = new FileExt(fullPath);

		DataOutputStream w = null;
		try {
			w = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(to)));

			w.writeByte(VERSION); //version
			w.writeInt(min.x);
			w.writeInt(min.y);
			w.writeInt(min.z);

			w.writeInt(max.x);
			w.writeInt(max.y);
			w.writeInt(max.z);

			w.writeInt(pieces.size());
			for(int i = 0; i < pieces.size(); i++) {
				pieces.get(i).serialize(w);
			}

			w.writeInt(connections.size());

			for(Entry<Long, LongArrayList> e : connections.entrySet()) {
				w.writeLong(e.getKey());
				w.writeInt(e.getValue().size());
				for(int i = 0; i < e.getValue().size(); i++) {
					w.writeLong(e.getValue().getLong(i));
				}
			}

			w.writeInt(textMap.size());

			for(Entry<Long, String> e : textMap.entrySet()) {
				w.writeLong(e.getKey());
				w.writeUTF(e.getValue());
			}

			w.writeInt(inventoryFilters.size());

			for(Entry<Long, Short2IntOpenHashMap> e : inventoryFilters.entrySet()) {
				w.writeLong(e.getKey());

				w.writeInt(e.getValue().size());
				for(Entry<Short, Integer> a : e.getValue().entrySet()) {

					w.writeShort(a.getKey());
					w.writeInt(a.getValue());
				}

			}

			w.writeInt(inventoryProduction.size());

			for(Entry<Long, Short> e : inventoryProduction.entrySet()) {
				w.writeLong(e.getKey());
				w.writeShort(e.getValue());
			}

			w.writeInt(inventoryProductionLimit.size());
			for(Entry<Long, Integer> e : inventoryProductionLimit.entrySet()) {
				w.writeLong(e.getKey());
				w.writeInt(e.getValue());
			}

			w.writeInt(inventoryFillUpFilters.size());

			for(Entry<Long, Short2IntOpenHashMap> e : inventoryFillUpFilters.entrySet()) {
				w.writeLong(e.getKey());

				w.writeInt(e.getValue().size());
				for(Entry<Short, Integer> a : e.getValue().entrySet()) {

					w.writeShort(a.getKey());
					w.writeInt(a.getValue());
				}

			}

		} finally {
			if(w != null) {
				w.close();
			}
		}
	}

	public void load(String nameWithoutSuffix) throws IOException {

		File to = new FileExt(path + nameWithoutSuffix + ".smtpl");
		load(to);
	}

	public void load(File to) throws IOException {
		try(DataInputStream w = new DataInputStream(new BufferedInputStream(new FileInputStream(to)))) {
			byte version = w.readByte(); //version
			System.out.println("Loading template version " + version);
			if(version == (byte) 1) {
				loadVersion1(w);
			} else if(version == (byte) 2) {
				loadVersion2(w);
			} else if(version == (byte) 3) {
				loadVersion3(w);
			} else if(version == (byte) 4) {
				loadVersion4(w);
			} else if(version == (byte) 5) {
				loadVersion5(w);
			} else if(version == (byte) 6) {
				loadVersion6(w);
			} else {
				throw new IOException("Unknown Template Version " + version);
			}
		}
	}

	/**
	 * Version 6 is mostly identical to version 5, but versions <= 5 deserialize using the old 3 byte system,
	 * while version 6 uses the new 4 byte system.
	 * @param w the DataInputStream to read from
	 * @throws IOException if an I/O error occurs
	 */
	private void loadVersion6(DataInputStream w) throws IOException {
		min = new Vector3i(w.readInt(), w.readInt(), w.readInt());
		max = new Vector3i(w.readInt(), w.readInt(), w.readInt());

		int piecesSize = w.readInt();
		for(int i = 0; i < piecesSize; i++) {
			VoidSegmentPiece v = new VoidSegmentPiece();
			v.deserialize(w);
			pieces.add(v);
		}

		int connectionsSize = w.readInt();
		for(int j = 0; j < connectionsSize; j++) {
			long key = w.readLong();
			int lSize = w.readInt();
			LongArrayList value = new LongArrayList(lSize);
			for(int i = 0; i < lSize; i++) value.add(w.readLong());
			connections.put(key, value);
		}

		int textSize = w.readInt();
		for(int j = 0; j < textSize; j++) {
			long key = w.readLong();
			String text = w.readUTF();
			textMap.put(key, text);
		}

		int filterSize = w.readInt();
		for(int j = 0; j < filterSize; j++) {
			long key = w.readLong();
			int lSize = w.readInt();
			Short2IntOpenHashMap m = new Short2IntOpenHashMap(lSize);
			for(int i = 0; i < lSize; i++) m.put(w.readShort(), w.readInt());
			inventoryFilters.put(key, m);
		}

		int prodSize = w.readInt();
		for(int j = 0; j < prodSize; j++) {
			long key = w.readLong();
			short text = w.readShort();
			inventoryProduction.put(key, text);
		}

		int prodLimitSize = w.readInt();
		for(int j = 0; j < prodLimitSize; j++) {
			long key = w.readLong();
			int text = w.readInt();
			inventoryProductionLimit.put(key, text);
		}

		int fillUpFilterSize = w.readInt();
		for(int j = 0; j < fillUpFilterSize; j++) {
			long key = w.readLong();
			int lSize = w.readInt();
			Short2IntOpenHashMap m = new Short2IntOpenHashMap(lSize);
			for(int i = 0; i < lSize; i++) m.put(w.readShort(), w.readInt());
			inventoryFillUpFilters.put(key, m);
		}
	}

	private void loadVersion5(DataInputStream w) throws IOException {
		loadVersion4(w);

		int prodSize = w.readInt();

		for(int j = 0; j < prodSize; j++) {
			long key = w.readLong();
			int text = w.readInt();
			inventoryProductionLimit.put(key, text);
		}

		int filterSize = w.readInt();
		for(int j = 0; j < filterSize; j++) {
			long key = w.readLong();
			int lSize = w.readInt();
			Short2IntOpenHashMap m = new Short2IntOpenHashMap(lSize);
			for(int i = 0; i < lSize; i++) {
				m.put(w.readShort(), w.readInt());
			}
			inventoryFillUpFilters.put(key, m);
		}
	}

	private void loadVersion4(DataInputStream w) throws IOException {
		min = new Vector3i(w.readInt(), w.readInt(), w.readInt());
		max = new Vector3i(w.readInt(), w.readInt(), w.readInt());
		int piecesSize = w.readInt();
		int failed = 0;
		Exception e = null;
		for(int i = 0; i < piecesSize; i++) {
			try {
				VoidSegmentPiece v = new VoidSegmentPiece();
				v.voidPos.set(w.readInt(), w.readInt(), w.readInt());
				byte[] bytes = {w.readByte(), w.readByte(), w.readByte()};
				SegmentData4Byte.migrateFrom3Byte(bytes, v);
				pieces.add(v);
			} catch(Exception exception) {
				e = exception;
				failed ++;
			}
		}
		if(e != null) {
			e.printStackTrace();
			System.out.println("Failed to migrate " + failed + " / " + piecesSize + " blocks to 4 byte segment data!");
			return;
		}

		int connectionsSize = w.readInt();

		for(int j = 0; j < connectionsSize; j++) {
			long key = w.readLong();

			int lSize = w.readInt();
			LongArrayList value = new LongArrayList(lSize);
			for(int i = 0; i < lSize; i++) {
				value.add(w.readLong());
			}

			connections.put(key, value);
		}

		int textSize = w.readInt();

		for(int j = 0; j < textSize; j++) {
			long key = w.readLong();
			String text = w.readUTF();
			textMap.put(key, text);
		}

		int filterSize = w.readInt();

		for(int j = 0; j < filterSize; j++) {
			long key = w.readLong();
			int lSize = w.readInt();
			Short2IntOpenHashMap m = new Short2IntOpenHashMap(lSize);
			for(int i = 0; i < lSize; i++) {
				m.put(w.readShort(), w.readInt());
			}
			inventoryFilters.put(key, m);
		}

		int prodSize = w.readInt();

		for(int j = 0; j < prodSize; j++) {
			long key = w.readLong();
			short text = w.readShort();
			inventoryProduction.put(key, text);
		}
	}

	private void loadVersion3(DataInputStream w) throws IOException {
		min = new Vector3i(w.readInt(), w.readInt(), w.readInt());
		max = new Vector3i(w.readInt(), w.readInt(), w.readInt());

		int piecesSize = w.readInt();
		for(int i = 0; i < piecesSize; i++) {
			VoidSegmentPiece v = new VoidSegmentPiece();
			v.voidPos.set(w.readInt(), w.readInt(), w.readInt());
			SegmentData3Byte.migrateTo(w.readByte(), w.readByte(), w.readByte(), v);
			pieces.add(v);
		}

		int connectionsSize = w.readInt();

		for(int j = 0; j < connectionsSize; j++) {
			long key = w.readLong();

			int lSize = w.readInt();
			LongArrayList value = new LongArrayList(lSize);
			for(int i = 0; i < lSize; i++) {
				value.add(w.readLong());
			}

			connections.put(key, value);
		}

		int textSize = w.readInt();

		for(int j = 0; j < textSize; j++) {
			long key = w.readLong();
			String text = w.readUTF();
			textMap.put(key, text);
		}

		int filterSize = w.readInt();

		for(int j = 0; j < filterSize; j++) {
			long key = w.readLong();
			int lSize = w.readInt();
			Short2IntOpenHashMap m = new Short2IntOpenHashMap(lSize);
			for(int i = 0; i < lSize; i++) {
				m.put(w.readShort(), w.readInt());
			}
			inventoryFilters.put(key, m);
		}

		int prodSize = w.readInt();

		for(int j = 0; j < prodSize; j++) {
			long key = w.readLong();
			short text = w.readShort();
			inventoryProduction.put(key, text);
		}

	}

	private void loadVersion2(DataInputStream w) throws IOException {
		min = new Vector3i(w.readInt(), w.readInt(), w.readInt());
		max = new Vector3i(w.readInt(), w.readInt(), w.readInt());

		int piecesSize = w.readInt();
		for(int i = 0; i < piecesSize; i++) {
			VoidSegmentPiece v = new VoidSegmentPiece();
			v.voidPos.set(w.readInt(), w.readInt(), w.readInt());
			SegmentData3Byte.migrateTo(w.readByte(), w.readByte(), w.readByte(), v);
			pieces.add(v);
		}

		int connectionsSize = w.readInt();

		for(int j = 0; j < connectionsSize; j++) {
			long key = w.readLong();

			int lSize = w.readInt();
			LongArrayList value = new LongArrayList(lSize);
			for(int i = 0; i < lSize; i++) {
				value.add(w.readLong());
			}

			connections.put(key, value);
		}

		int textSize = w.readInt();

		for(int j = 0; j < textSize; j++) {
			long key = w.readLong();
			String text = w.readUTF();
			textMap.put(key, text);
		}
	}

	private void loadVersion1(DataInputStream w) throws IOException {
		min = new Vector3i(w.readInt(), w.readInt(), w.readInt());
		max = new Vector3i(w.readInt(), w.readInt(), w.readInt());

		int piecesSize = w.readInt();
		for(int i = 0; i < piecesSize; i++) {
			VoidSegmentPiece v = new VoidSegmentPiece();
			v.voidPos.set(w.readInt(), w.readInt(), w.readInt());
			SegmentData3Byte.migrateTo(w.readByte(), w.readByte(), w.readByte(), v);
			pieces.add(v);
		}

		int connectionsSize = w.readInt();

		for(int j = 0; j < connectionsSize; j++) {
			long key = w.readLong();

			int lSize = w.readInt();
			LongArrayList value = new LongArrayList(lSize);
			for(int i = 0; i < lSize; i++) {
				value.add(w.readLong());
			}

			connections.put(key, value);
		}
	}
}
