package org.schema.game.client.controller.manager.ingame;


import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.util.FastCopyLongOpenHashSet;

import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class BuildInstruction {

	private final ObjectArrayList<Remove> removes = new ObjectArrayList<Remove>();
	private final ObjectArrayList<Add> adds = new ObjectArrayList<Add>();
	private final ObjectArrayList<Replace> replaces = new ObjectArrayList<Replace>();

	private final SegmentController controller;
	private float volumeAdded;
	public FillTool fillTool;
	public boolean enabled;
	


	public BuildInstruction(SegmentController controller) {
		super();
		this.controller = controller;
		this.enabled = controller.isOnServer();
	}

	public void recordRemove(SegmentPiece piece) {
		long index = piece.getAbsoluteIndex();
		short type = piece.getType();
		long controller = Long.MIN_VALUE;
		long index4 = ElementCollection.getIndex4(index, type);
		if(ElementInformation.canBeControlledByAny(type)) {
			for ( Long2ObjectMap.Entry<FastCopyLongOpenHashSet> e : this.controller.getControlElementMap().getControllingMap().getAll().long2ObjectEntrySet()) {
				if (e.getValue().contains(index4)) {
					controller = e.getLongKey();
					break;
				}
			}
		}
		LongOpenHashSet connectedFromThis = this.controller.getControlElementMap().getControllingMap().getAll().get(index);
        if (type != 0) {
            this.volumeAdded -= ElementKeyMap.getInfo(type).getVolume();
        }
        
        SegmentPiece p = new SegmentPiece();
        p.setByValue(piece);
		removes.add(new Remove(p, controller, connectedFromThis));
	}

	public void recordAdd(short type, long toBuildPiece,
	                      int elementOrientation, boolean activateBlock, long selectedController) {
		adds.add(new Add(type, toBuildPiece, elementOrientation, activateBlock, selectedController));
        if (type != 0) {
            this.volumeAdded += ElementKeyMap.getInfo(type).getVolume();
        }
	}

	public void recordReplace(long where, short from, short to, byte prevOrientation, boolean prevIsActive) {
		Replace replace = new Replace(where, from, to, prevOrientation, prevIsActive);
		replaces.add(replace);
//		System.err.println("[CLIENT] Recorded replace: "+replace);
        if (from != 0) {
            volumeAdded -= ElementKeyMap.getInfo(from).getVolume();
        }
        if (to != 0) {
            volumeAdded += ElementKeyMap.getInfo(to).getVolume();
        }
	}

	/**
	 * @return the removes
	 */
	public ObjectArrayList<Remove> getRemoves() {
		return removes;
	}

	/**
	 * @return the adds
	 */
	public ObjectArrayList<Add> getAdds() {
		return adds;
	}

	/**
	 * @return the replaces
	 */
	public ObjectArrayList<Replace> getReplaces() {
		return replaces;
	}

	/**
	 * @return the controller
	 */
	public SegmentController getController() {
		return controller;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "(;ADD: " + adds.size() + "; REMOVES: " + removes.size() + ")";
	}

	public static class Add {
		public short type;
		public long where;
		public int elementOrientation;
		public boolean activateBlock;
		public long selectedController = Long.MIN_VALUE;

		public Add(short type, long where, int elementOrientation,
		           boolean activateBlock, long selectedController) {
			super();
			this.type = type;
			this.where = where;
			this.elementOrientation = elementOrientation;
			this.activateBlock = activateBlock;
			this.selectedController = selectedController;
		}
	}

	public static class Remove {
		public SegmentPiece where;
		public long controller;
		public LongOpenHashSet connectedFromThis;

		public Remove(SegmentPiece where, long controller, LongOpenHashSet connectedFromThis) {
			super();
			this.where = where;
			this.controller = controller;
			this.connectedFromThis = connectedFromThis;
		}

		public Remove() {
		}
	}

	public static class Replace {
		public long where;
		public short from;
		public short to;
		public byte prevOrientation;
		public boolean prevIsActive;

		public Replace(long where, short from, short to, byte prevOrientation, boolean prevIsActive) {
			super();
			this.where = where;
			this.from = from;
			this.to = to;
			this.prevOrientation = prevOrientation;
			this.prevIsActive = prevIsActive;
		}

		@Override
		public String toString() {
			return "Replace [where=" + where + ", from=" + from + ", to=" + to
					+ ", prevOrientation=" + prevOrientation
					+ ", prevIsActive=" + prevIsActive + "]";
		}
		
		
	}

	public boolean fitsUndo(Inventory inventory) {
		return volumeAdded <= 0 || inventory.isOverCapacity(volumeAdded);
	}

	public void clear() {
		removes.clear();
		adds.clear();
		replaces.clear();
		volumeAdded = 0;
		fillTool = null;
	}

}
