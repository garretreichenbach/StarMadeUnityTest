package org.schema.game.client.data.terrain.geimipmap;

import java.nio.IntBuffer;

import org.schema.schine.graphicsengine.forms.Mesh;

/**
 * Stores a terrain patch's details so the LOD background thread can update
 * the actual terrain patch back on the ogl thread.
 *
 * @author Brent Owens
 */
public class UpdatedTerrainPatch {

	private TerrainPatch updatedPatch;
	private int newLod;
	private int previousLod;
	private int rightLod, topLod, leftLod, bottomLod;
	private IntBuffer newIndexBuffer;
	private boolean reIndexNeeded = false;
	private boolean fixEdges = false;

	public UpdatedTerrainPatch(TerrainPatch updatedPatch, int newLod) {
		this.updatedPatch = updatedPatch;
		this.newLod = newLod;
	}

	public UpdatedTerrainPatch(TerrainPatch updatedPatch, int newLod, int prevLOD, boolean reIndexNeeded) {
		this.updatedPatch = updatedPatch;
		this.newLod = newLod;
		this.previousLod = prevLOD;
		this.reIndexNeeded = reIndexNeeded;

	}

	protected int getBottomLod() {
		return bottomLod;
	}

	protected void setBottomLod(int bottomLod) {
		this.bottomLod = bottomLod;
	}

	protected int getLeftLod() {
		return leftLod;
	}

	protected void setLeftLod(int leftLod) {
		this.leftLod = leftLod;
	}

	public String getName() {
		return updatedPatch.getName();
	}

	protected IntBuffer getNewIndexBuffer() {
		return newIndexBuffer;
	}

	protected void setNewIndexBuffer(IntBuffer newIndexBuffer) {
		this.newIndexBuffer = newIndexBuffer;
	}

	protected int getNewLod() {
		return newLod;
	}

	protected void setNewLod(int newLod) {
		this.newLod = newLod;
	}

	public int getPreviousLod() {
		return previousLod;
	}

	public void setPreviousLod(int previousLod) {
		this.previousLod = previousLod;
	}

	protected int getRightLod() {
		return rightLod;
	}

	protected void setRightLod(int rightLod) {
		this.rightLod = rightLod;
	}

	protected int getTopLod() {
		return topLod;
	}

	protected void setTopLod(int topLod) {
		this.topLod = topLod;
	}

	protected TerrainPatch getUpdatedPatch() {
		return updatedPatch;
	}

	protected void setUpdatedPatch(TerrainPatch updatedPatch) {
		this.updatedPatch = updatedPatch;
	}

	public boolean isFixEdges() {
		return fixEdges;
	}

	public void setFixEdges(boolean fixEdges) {
		this.fixEdges = fixEdges;
	}

	public boolean isReIndexNeeded() {
		return reIndexNeeded;
	}

	public void setReIndexNeeded(boolean reIndexNeeded) {
		this.reIndexNeeded = reIndexNeeded;
	}

	protected boolean lodChanged() {
		if (reIndexNeeded && previousLod != newLod) {
			return true;
		} else {
			return false;
		}
	}

	public void updateAll() {
		if (!updatedPatch.getMesh().isLoaded()) {
			System.err.println("patch not loaded yet to update");
			return;
		}
		updatedPatch.setLod(newLod);
		updatedPatch.setLodRight(rightLod);
		updatedPatch.setLodTop(topLod);
		updatedPatch.setLodLeft(leftLod);
		updatedPatch.setLodBottom(bottomLod);
		if (reIndexNeeded || fixEdges) {
			updatedPatch.setPreviousLod(previousLod);
			updatedPatch.getMesh().clearBuffer(Mesh.BUFFERTYPE_Index);
			updatedPatch.getMesh().setBuffer(Mesh.BUFFERTYPE_Index, 3, newIndexBuffer);
		}
	}

}
