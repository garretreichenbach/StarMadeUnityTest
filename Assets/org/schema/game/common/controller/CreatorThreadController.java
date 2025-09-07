package org.schema.game.common.controller;

import java.util.Collections;
import java.util.Comparator;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.element.world.SegmentQueueManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class CreatorThreadController extends Thread {

	private final boolean onServer;

	private final ObjectArrayList<SegmentController> segmentControllersTmp = new ObjectArrayList<SegmentController>();

	private final ObjectArrayList<SegmentController> segmentControllers = new ObjectArrayList<SegmentController>();
	private final ObjectArrayList<SegmentController> segmentControllersToAdd = new ObjectArrayList<SegmentController>();
	private final ObjectArrayList<SegmentController> segmentControllersToRemove = new ObjectArrayList<SegmentController>();

	private final Int2ObjectOpenHashMap<SegmentController> segmentControllerSet = new Int2ObjectOpenHashMap<SegmentController>();
	private final ObjectArrayList<SegmentController> inReach = new ObjectArrayList<SegmentController>();
	private final SegmentSort segmentSort = new SegmentSort();
	private StateInterface state;
	private boolean controllerSetChanged = true;
	private long lastSort;
	private boolean finished;
	public final SegmentQueueManager clientQueueManager;
	public CreatorThreadController(StateInterface state) {
		this.setDaemon(true);
		this.state = state;
		onServer = state instanceof ServerStateInterface;
		setDaemon(true);
		setName((onServer ? "[SERVER]" : "[CLIENT]") + "_CREATOR_THREAD");
		if(!onServer){
			this.clientQueueManager = new SegmentQueueManager((GameClientState) state);
		}else{
			this.clientQueueManager = null;
		}
	}

	public void addCreatorThread(SegmentController segmentController) {
		if (!onServer) {
			synchronized (segmentControllersToAdd) {
				segmentControllersToAdd.add(segmentController);
			}
		}
	}


	private void doAddAndRemove() {
		if (!segmentControllersToAdd.isEmpty()) {
			
			synchronized (segmentControllersToAdd) {
				initRegisters(segmentControllersToAdd);
				segmentControllers.addAll(segmentControllersToAdd);
				segmentControllersToAdd.clear();
			}
		}
		if (!segmentControllersToRemove.isEmpty()) {
			
			synchronized (segmentControllersToRemove) {
				clearRegisters(segmentControllersToRemove);
				segmentControllers.removeAll(segmentControllersToRemove);
				segmentControllersToRemove.clear();
			}
		}

		if (segmentControllers.size() > 0 && Controller.getCamera() != null && System.currentTimeMillis() - lastSort > 1000) {
			segmentSort.from.set(Controller.getCamera().getPos());
			try{
				Collections.sort(segmentControllers, segmentSort);
			}catch(Exception e){
				e.printStackTrace();
			}

			lastSort = System.currentTimeMillis();
		}
		
	}
	public final SegmentControllerRegister register = new SegmentControllerRegister();
	public class SegmentControllerRegister{
		short reg;
		public static final int MAX_REG = 20000;
		Short2ObjectOpenHashMap<ClientSegmentProvider> registry = new Short2ObjectOpenHashMap<ClientSegmentProvider>();
		public void register(ClientSegmentProvider c){
			if(c.registerId == -1){
				c.registerId = getFreeRegister();
				registry.put(c.registerId, c);
			}
		}
		public void unregister(ClientSegmentProvider c){
			if(c.registerId != -1){
				ClientSegmentProvider remove = registry.remove(c.registerId);
				if(remove != null){
					System.err.println("[CLIENT][RequestRegister] unregister provider "+c.registerId+": "+c.getSegmentController()+"; "+c);
				}else{
					System.err.println("[CLIENT][RequestRegister] Exception: unregister provider failed "+c.registerId+": "+c.getSegmentController()+"; "+c);
				}
				assert(remove != null):c+"; "+c.registerId;
				
				clientQueueManager.onUnregister(c, c.registerId);
				c.registerId = -1;
				
				
			}else{
				//either called twice or prematurely 
				assert(false):c.getSegmentController()+"; "+c+": ";
			}
		}
		private short getFreeRegister() {
			while(registry.containsKey(reg)){
				reg = (short) ((reg+1)%MAX_REG);
			}
			return reg;
		}
		public void getCopy(Short2ObjectOpenHashMap<ClientSegmentProvider> r) {
			synchronized(register){
				assert(r.isEmpty());
				r.putAll(registry);
			}
		}
		public void onRemovedFromQueue(short id) {
			for(SegmentController c : segmentControllers){
				if(c.getSegmentProvider() != null){
					ClientSegmentProvider cs = (ClientSegmentProvider)c.getSegmentProvider();
					if(cs.registerId == id){
						System.err.println("[CLIENT] RECOVERED REGISTER:::::: "+id+": "+cs.getSegmentController());
						cs.clearBuffers();
						cs.resetRegisterAABB();
					}
				}
			}
		}
	}
	private void initRegisters(ObjectArrayList<SegmentController> d) {
		synchronized(register){
			for(SegmentController c : d){
				register.register((ClientSegmentProvider) c.getSegmentProvider());
			}
		}
	}

	private void clearRegisters(ObjectArrayList<SegmentController> d) {
		synchronized(register){
			for(SegmentController c : d){
				register.unregister((ClientSegmentProvider) c.getSegmentProvider());
			}
		}
	}

	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	public void notifyQueueChange(boolean active, SegmentController controller) {

		if (active) {
			if (!segmentControllerSet.containsKey(controller.getId())) {
				long t = System.currentTimeMillis();
				synchronized (segmentControllerSet) {
					segmentControllerSet.put(controller.getId(), controller);
					GameClientState.clientCreatorThreadIterations = segmentControllerSet.size();
					controllerSetChanged = true;
					segmentControllerSet.notify();
				}
				long took = System.currentTimeMillis() - t;
				if (took > 5) {
					System.err.println("[CREATORTHREAD][CLIENT] WARNING: notify for " + controller + " on queue " + segmentControllerSet.size() + " took " + took + " ms");
				}
			}
		} else {
			if (segmentControllerSet.containsKey(controller.getId())) {
				synchronized (segmentControllerSet) {

					segmentControllerSet.remove(controller.getId());
					GameClientState.clientCreatorThreadIterations = segmentControllerSet.size();
					controllerSetChanged = true;
					//					System.err.println("UNREGISTERED UPDATE TRHEAD: "+controller+": "+segmentControllerSet.size());
				}
			}
		}

	}

	public void removeCreatorThread(SegmentController segmentController) {
		if (!onServer) {
			synchronized (segmentControllersToRemove) {
				segmentControllersToRemove.add(segmentController);
			}
		}
	}

	@Override
	public void run() {
		try {

			if (onServer) {
			} else {
				while (((GameClientState) state).getPlayer() == null) {
					Thread.sleep(70);
				}

				startClientRequestNewThread();

//				while (!finished) {
//					System.err.println("DONDLKNDLKNDKLN");
//					clientHandle();
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Creator Thread DIED!!!");
			GuiErrorHandler.processErrorDialogException(e);
			GLFrame.setFinished(true);
		}

	}

	private void startClientRequestNewThread() {
		Runnable r = () -> {
			try {

				while (!finished) {
					long time = System.currentTimeMillis();
					doAddAndRemove();
					float madeRequest = 0;
					float controllerCount = 0;

					for (int i = 0; i < segmentControllers.size(); i++) {

						SegmentController segmentController = segmentControllers.get(i);
						if (segmentController.getCreatorThread() != null && segmentController.isInClientRange()) {
							ClientSegmentProvider p = (ClientSegmentProvider) segmentController.getSegmentProvider();
							if(!p.registeredContent){
								p.registerContent(clientQueueManager);
							}

							segmentController.getCreatorThread().requestQueueHandle(clientQueueManager);

						}
					}

					clientQueueManager.doActualRequests(CreatorThreadController.this);

					synchronized (state) {
						state.setSynched();
						try{
							clientQueueManager.executeSynchend(CreatorThreadController.this);
							for (int i = 0; i < segmentControllers.size(); i++) {
								SegmentController segmentController = segmentControllers.get(i);
//								if (!segmentController.isInClientRange()) {
								if (!state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(segmentController.getId())) {
									segmentControllers.remove(i);
									i--;
								}

							}
						}finally{
							state.setUnsynched();
						}
					}
					Thread.sleep(30);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};

		Thread requestNewThread = new Thread(r, "[CLIENT]RequestNewSegments");
		requestNewThread.setPriority(3);
		requestNewThread.start();
	}

	public void waitForChange() throws InterruptedException {

		/*
		 * there is no thread hazard here
		 *
		 * segmentControllerSet is only accessed synchronized if for the rare
		 * occurrence that if(segmentControllerSet.isEmpty()) would cause a
		 * racing condition, the (wrongly) executed run would only cause one
		 * more update with the segmentControllersTmp list
		 */

		if (segmentControllerSet.isEmpty()) {
			synchronized (segmentControllerSet) {
				while (segmentControllerSet.isEmpty()) {
					segmentControllerSet.wait(20000);
				}

			}
		}
		if (!finished && controllerSetChanged) {
			segmentControllersTmp.clear();
			synchronized (segmentControllerSet) {
				segmentControllersTmp.addAll(segmentControllerSet.values());
				controllerSetChanged = false;
			}
		}
	}
	public void onStopClient() {
		finished = true;
		clientQueueManager.setShutdown(true);
		synchronized (segmentControllerSet) {
			segmentControllerSet.notifyAll();
		}
	}
	private class SegmentSort implements Comparator<SegmentController> {
		private Vector3f from = new Vector3f();
		private Vector3f d = new Vector3f();

		@Override
		public int compare(SegmentController o1, SegmentController o2) {
			d.sub(from, o1.getWorldTransformOnClient().origin);

			float d1 = d.lengthSquared();

			d.sub(from, o2.getWorldTransformOnClient().origin);

			float d2 = d.lengthSquared();
			int i = (int) (d1 - d2);

			//never return 0
			if (i == 0) {
				return o1.getId() - o2.getId();
			}
			return i;
		}

	}

	

}
