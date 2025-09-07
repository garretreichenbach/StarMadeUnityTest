package org.schema.game.server.controller.pathfinding;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.pathfinding.AbstractAStarCalculator;
import org.schema.game.common.controller.pathfinding.CalculationTookTooLongException;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public abstract class AbstractPathFindingHandler<E extends AbstractAStarCalculator<A>, A extends AbstractPathRequest> extends Thread {

	public static int path_in_calc;
	public static int currentIt;
	protected final GameServerState state;
	private final E ic;
	protected SegmentController segmentController;
	private ObjectArrayFIFOQueue<A> queue = new ObjectArrayFIFOQueue<A>();
	private boolean shutdown;

	public AbstractPathFindingHandler(GameServerState state, String name, E calc) {
		super(name);
		setDaemon(true);
		setPriority(MIN_PRIORITY);
		this.ic = calc;
		this.state = state;
		init();

	}

	protected abstract void init();

	public void enqueue(A cr) {
		//		System.err.println("trying to enqueue path request: queue: "+queue.size());
		synchronized (queue) {

			queue.enqueue(cr);

			//			System.err.println("Enqueued path request: queue: "+queue.size());
			queue.notify();
		}
	}

	protected void enqueueSynchedResponse() {
		synchronized (state.pathFindingCallbacks) {
			state.pathFindingCallbacks.enqueue(this);
			while (!state.pathFindingCallbacks.isEmpty()) {
				try {
					state.pathFindingCallbacks.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void shutdown(){
		shutdown = true;
		synchronized (queue) {
			queue.notifyAll();
		}
	}
	@Override
	public void run() {
		while (!shutdown) {
			A dequeue;
			synchronized (queue) {
				while (queue.isEmpty()) {
					try {
						queue.wait();
						if(shutdown){
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				dequeue = queue.dequeue();
			}
			try {
				process(dequeue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void process(A cr) {
		path_in_calc++;
		cr.refresh();

		if (!canProcess(cr)) {
			return;
		}
		segmentController = cr.getSegmentController();
		ic.init(cr);

		Vector3i absoluteElemPos = cr.getFrom(new Vector3i());

		boolean foundCore;

		if (cr.random()) {
//			System.err.println("CALCULATING RANDOM PATH!!!!!!!!!!!! "+absoluteElemPos+"; "+cr.randomOrigin()+"; "+cr.randomRoamBB()+"; "+cr.randomPathPrefferedDir());
			foundCore = ic.calculateDir(absoluteElemPos.x, absoluteElemPos.y, absoluteElemPos.z, cr.randomOrigin(), cr.randomRoamBB(), cr.randomPathPrefferedDir());
//			System.err.println("CALCULATING RANDOM PATH FINISHED!!!!!!!!!!!!");
		} else {
			Vector3i toPos = cr.getTo(new Vector3i());
			try {
				foundCore = ic.calculate(absoluteElemPos.x, absoluteElemPos.y, absoluteElemPos.z, toPos.x, toPos.y, toPos.z);
			} catch (CalculationTookTooLongException e) {
				e.printStackTrace();
				foundCore = false;
				String sec;
				if (segmentController != null) {
					Sector sector = state.getUniverse().getSector(segmentController.getSectorId());
					sec = sector != null ? sector.pos.toStringPure() : "unknown";
				} else {
					sec = "freeSpace[Unknown]";
				}
				state.getController().broadcastMessageAdmin(Lng.astr("AdminMessage: Path calculation failed:\nCalculation took over %s secs\n(sector %s)",  AbstractAStarCalculator.MAX_CALCULATION_TIME / 1000 ,  sec), ServerMessage.MESSAGE_TYPE_ERROR);
			}
		}

		afterCalculate(foundCore, cr);
		path_in_calc--;

		currentIt = 0;

	}

	protected abstract boolean canProcess(A cr);

	protected abstract void afterCalculate(boolean foundCore, A cr);

	public abstract void handleReturn();

	/**
	 * @return the ic
	 */
	public E getIc() {
		return ic;
	}

}
