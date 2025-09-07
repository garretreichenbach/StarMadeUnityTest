package org.schema.game.client.view.gui.buildtools;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class BuildConstructionManager {
	
	private final GameClientState state;
	private final ObjectArrayFIFOQueue<BuildConstructionCommand> queue = new ObjectArrayFIFOQueue<BuildConstructionCommand>();
	private BuildConstructionCommand current;
	
	
	public BuildConstructionManager(GameClientState state) {
		super();
		this.state = state;
	}



	public boolean isCommandQueued() {
		return current != null || !queue.isEmpty();
	}



	public void update(Timer timer) {
		if(!KeyboardMappings.BUILD_MODE_FIX_CAM.isDown()) {
			BuildConstructionCommand.issued = false;
		}
		if(current != null && current.isFinished()) {
			current.onEnd(state);
			current = null;
		}
		if(current == null && !queue.isEmpty()) {
			
			current = queue.dequeue();
			System.err.println("[BUILDCOMMANDQUREUE] starting build command: "+current);
			current.onStart(state);
		}
		if(current != null) {
			current.update(timer, state);
		}
	}
	public void onBuiltBlock(Vector3i posBuilt, Vector3i posNextToBuild, short type) {
		if(current != null) {
			current.onBuiltBlock(type);
		}
	}
	public void onRemovedBlock(long pos, short type) {
		if(current != null) {
			current.onRemovedBlock(type);
		}
	}
	
	public void updateOnNotInBuildmode(Timer timer) {
		BuildConstructionCommand.issued = false;
		reset();
	}



	private void reset() {
		current = null;
		queue.clear();
	}



	public void enqueue(BuildConstructionCommand c) {
		queue.enqueue(c);
	}



	public void onCanceled(BuildConstructionCommand com) {
		System.err.println("[BUILDCOMMANDQUREUE] command: "+com+" canceled. Clearing queue");
		//remove all other queued commands
		queue.clear();
	}



	public BuildConstructionCommand getCurrent() {
		return current;
	}



	public boolean canQueue(BuildConstructionCommand c) {
		return c.isExecutable(state);
	}



	public void resetQueue() {
		queue.clear();
		current = null;
	}
}
