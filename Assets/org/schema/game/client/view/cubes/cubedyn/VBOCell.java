package org.schema.game.client.view.cubes.cubedyn;


public class VBOCell implements Comparable<VBOCell> {
	final int initialStart;
	final int initialEnd;
	final VBOSeg vboSeg;
	public int blendedFloatStartPos;
	boolean free = true;
	int startPositionByte;
	int endPositionByte;
	int lengthInBytes;
	
	public int[][] opaqueRanges = new int[7][2];
	public int[][] blendedRanges = new int[7][2];
	public int[][] lodRanges = new int[1][2]; //LodMesh
	
	public VBOCell(int start, int end, VBOSeg vboSeg) {
		this.initialStart = start;
		this.initialEnd = end;
		this.vboSeg = vboSeg;
	}

	public boolean stillFitsInTaken(int sizeNeeded) {
		if (sizeNeeded <= sizeInitial()) {
			startPositionByte = initialStart;
			endPositionByte = initialStart + sizeNeeded;
			lengthInBytes = sizeNeeded;
			return true;
		} else {
			return false;
		}
	}

	public boolean fitIn(int sizeNeeded) {
		if (free && sizeNeeded <= sizeInitial()) {
			startPositionByte = initialStart;
			endPositionByte = initialStart + sizeNeeded;
			lengthInBytes = sizeNeeded;
			assert (endPositionByte <= initialEnd);
			free = false;
			vboSeg.takenCount++;
			return true;
		}
		return false;
	}

	private int sizeInitial() {
		return initialEnd - initialStart;
	}

	@Override
	public int compareTo(VBOCell o) {
		return initialStart - o.initialStart;
	}

	public int getBufferId() {
		return vboSeg.bufferId;
	}		/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VBOCell [initialStart=" + initialStart + ", initialEnd="
				+ initialEnd + ", bufferId=" + vboSeg.bufferId + ", free=" + free
				+ ", startPositionByte=" + startPositionByte
				+ ", endPositionByte=" + endPositionByte + "]";
	}

	public void released() {
		free = true;
		vboSeg.takenCount--;
		vboSeg.checkAllFree();
	}



}
