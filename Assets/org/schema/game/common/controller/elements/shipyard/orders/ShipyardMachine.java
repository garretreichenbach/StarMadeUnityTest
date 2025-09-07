package org.schema.game.common.controller.elements.shipyard.orders;

import org.schema.game.common.controller.elements.shipyard.orders.states.BlueprintSpawned;
import org.schema.game.common.controller.elements.shipyard.orders.states.Constructing;
import org.schema.game.common.controller.elements.shipyard.orders.states.ConvertingRealToVirtual;
import org.schema.game.common.controller.elements.shipyard.orders.states.CreateBlueprintFromDesign;
import org.schema.game.common.controller.elements.shipyard.orders.states.CreateDesignFromBlueprint;
import org.schema.game.common.controller.elements.shipyard.orders.states.CreatingDesign;
import org.schema.game.common.controller.elements.shipyard.orders.states.Deconstructing;
import org.schema.game.common.controller.elements.shipyard.orders.states.DesignLoaded;
import org.schema.game.common.controller.elements.shipyard.orders.states.LoadingDesign;
import org.schema.game.common.controller.elements.shipyard.orders.states.MovingToTestSite;
import org.schema.game.common.controller.elements.shipyard.orders.states.NormalShipLoaded;
import org.schema.game.common.controller.elements.shipyard.orders.states.RemovingDesign;
import org.schema.game.common.controller.elements.shipyard.orders.states.RemovingPhysical;
import org.schema.game.common.controller.elements.shipyard.orders.states.RevertPullingBlocksFromInventory;
import org.schema.game.common.controller.elements.shipyard.orders.states.ShipyardState;
import org.schema.game.common.controller.elements.shipyard.orders.states.SpawningBlueprint;
import org.schema.game.common.controller.elements.shipyard.orders.states.UnloadingDesign;
import org.schema.game.common.controller.elements.shipyard.orders.states.WaitingForShipyardOrder;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class ShipyardMachine extends FiniteStateMachine<String> {

	/**
	 *
	 */
	
	private WaitingForShipyardOrder waiting;
	private DesignLoaded designLoaded;
	private NormalShipLoaded normalLoaded;
	private LoadingDesign loadingDesign;
	private Constructing spawningDesign;
	private Constructing constructingRepair;
	private Deconstructing deconstructing;
	private Deconstructing deconstructingRepair;

	public ShipyardMachine(ShipyardEntityState obj, MachineProgram<?> program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public ShipyardEntityState getObj() {
		return (ShipyardEntityState)super.getObj();
	}

	@Override
	public void createFSM(String parameter) {
		ShipyardEntityState gObj = getObj();
		waiting = new WaitingForShipyardOrder(gObj);
		
		designLoaded = new DesignLoaded(gObj);
		normalLoaded = new NormalShipLoaded(gObj);
		
		//manual undock
		normalLoaded.addTransition(Transition.SY_ERROR, waiting);
		designLoaded.addTransition(Transition.SY_ERROR, waiting);
		
		//manual dock
		waiting.addTransition(Transition.SY_LOAD_NORMAL, normalLoaded);
		
		createDesign(waiting, designLoaded, gObj);
		
		loadDesign(waiting, designLoaded, gObj);

		loadNormal(waiting, normalLoaded, gObj);
		
		unloadDesign(designLoaded, gObj);
		
		deconstructionOrder(normalLoaded, designLoaded, gObj);

		saveBlueprint(designLoaded, designLoaded, gObj);
		
		createDesignFromBlueprint(waiting, designLoaded, gObj);
		
		spawnDesign(designLoaded, normalLoaded, gObj);
		
		repairDesign(normalLoaded, normalLoaded, gObj);
		
		testDesign(designLoaded, waiting, gObj);
		
		spawnBlueprint(waiting, designLoaded, normalLoaded, gObj);
		
		setStartingState(waiting); 
	}

	private DesignLoaded createDesign(WaitingForShipyardOrder start, DesignLoaded end, ShipyardEntityState gObj) {
		CreatingDesign cr = new CreatingDesign(gObj);
		
		start.addTransition(Transition.SY_CREATE_DESIGN, cr);
		
		cr.addTransition(Transition.SY_LOADING_DONE, end);
		cr.addTransition(Transition.SY_ERROR, start);

		return end;
	}
	private DesignLoaded saveBlueprint(DesignLoaded start, DesignLoaded end, ShipyardEntityState gObj) {
		
		CreateBlueprintFromDesign cr = new CreateBlueprintFromDesign(gObj);
		
		start.addTransition(Transition.SY_CONVERT_TO_BLUEPRINT, cr);
		
		cr.addTransition(Transition.SY_CONVERSION_DONE, start);
		cr.addTransition(Transition.SY_ERROR, start);
		
		return end;
	}
	private DesignLoaded createDesignFromBlueprint(WaitingForShipyardOrder start, DesignLoaded end, ShipyardEntityState gObj) {
		
		CreateDesignFromBlueprint cr = new CreateDesignFromBlueprint(gObj);
		
		start.addTransition(Transition.SY_CONVERT_BLUEPRINT_TO_DESIGN, cr);
		
		cr.addTransition(Transition.SY_CONVERSION_DONE, end);
		cr.addTransition(Transition.SY_ERROR, start);
		
		return end;
	}
	private DesignLoaded loadDesign(WaitingForShipyardOrder start, DesignLoaded end, ShipyardEntityState gObj) {
		loadingDesign = new LoadingDesign(gObj);
		RemovingDesign rm = new RemovingDesign(gObj);
		start.addTransition(Transition.SY_LOAD_DESIGN, loadingDesign);
		loadingDesign.addTransition(Transition.SY_ERROR, start);
		
		loadingDesign.addTransition(Transition.SY_LOADING_DONE, end);
		return end;
	}
	private NormalShipLoaded loadNormal(WaitingForShipyardOrder start, NormalShipLoaded end, ShipyardEntityState gObj) {
		start.addTransition(Transition.SY_LOAD_NORMAL, end);
		end.addTransition(Transition.SY_ERROR, start);
		return end;
	}
	private void unloadDesign(DesignLoaded start, ShipyardEntityState gObj) {
		UnloadingDesign ld = new UnloadingDesign(gObj);
		
		start.addTransition(Transition.SY_UNLOAD_DESIGN, ld);
		
		ld.addTransition(Transition.SY_UNLOADING_DONE, waiting);
		ld.addTransition(Transition.SY_ERROR, waiting);
	}
	private DesignLoaded deconstructionOrder(NormalShipLoaded start, DesignLoaded end, ShipyardEntityState gObj) {
		deconstructing = new Deconstructing(gObj);
		start.addTransition(Transition.SY_DECONSTRUCT, deconstructing);
		start.addTransition(Transition.SY_DECONSTRUCT_RECYCLE, deconstructing);
		
		deconstructing.addTransition(Transition.SY_ERROR, start);
		deconstructing.addTransition(Transition.SY_CANCEL, start);
		
		
		deconstructing.addTransition(Transition.SY_DECONSTRUCTION_DONE, loadingDesign);
		deconstructing.addTransition(Transition.SY_DECONSTRUCTION_DONE_NO_DESIGN, waiting);
		
		
		return end;
		
	}
	private NormalShipLoaded repairDesign(NormalShipLoaded start, NormalShipLoaded end, ShipyardEntityState gObj) {
		deconstructingRepair = new Deconstructing(gObj);
		start.addTransition(Transition.SY_REPAIR_TO_DESIGN, deconstructingRepair);
		
		deconstructingRepair.addTransition(Transition.SY_ERROR, start);
		deconstructingRepair.addTransition(Transition.SY_CANCEL, start);
		
		
		LoadingDesign l = new LoadingDesign(gObj);
		
		
		constructingRepair = new Constructing(gObj);
		deconstructingRepair.addTransition(Transition.SY_DECONSTRUCTION_DONE_NO_DESIGN, l);
		
		l.addTransition(Transition.SY_ERROR, start);
		
		l.addTransition(Transition.SY_LOADING_DONE, constructingRepair);
		
		RevertPullingBlocksFromInventory r = new RevertPullingBlocksFromInventory(gObj);
		constructingRepair.addTransition(Transition.SY_ERROR, r);
		constructingRepair.addTransition(Transition.SY_CANCEL, r);
		
		
		constructingRepair.addTransition(Transition.SY_SPAWN_DONE, end);
		
		r.addTransition(Transition.SY_BLOCK_TRANSACTION_FINISHED, start);
		
		return end;
	}
	private void testDesign(DesignLoaded start, WaitingForShipyardOrder end, ShipyardEntityState gObj) {
		Constructing constructing = new Constructing(gObj);
		constructing.testDesign = true;
		MovingToTestSite mtt = new MovingToTestSite(gObj);
		LoadingDesign load = new LoadingDesign(gObj);
		
		start.addTransition(Transition.SY_TEST_DESIGN, constructing);
		
		constructing.addTransition(Transition.SY_SPAWN_DONE, mtt);
		constructing.addTransition(Transition.SY_ERROR, start);
		
		mtt.addTransition(Transition.SY_MOVING_TO_TEST_SITE_DONE, load);
		
		load.addTransition(Transition.SY_ERROR, start);
		
		load.addTransition(Transition.SY_LOADING_DONE, end);
		
		mtt.addTransition(Transition.SY_ERROR, end);
	}
	private NormalShipLoaded spawnDesign(DesignLoaded start, NormalShipLoaded end, ShipyardEntityState gObj) {
		spawningDesign = new Constructing(gObj);
		
		start.addTransition(Transition.SY_SPAWN_DESIGN, spawningDesign);
		
		RevertPullingBlocksFromInventory r = new RevertPullingBlocksFromInventory(gObj);
		spawningDesign.addTransition(Transition.SY_ERROR, r);
		spawningDesign.addTransition(Transition.SY_CANCEL, r);
		
		
		spawningDesign.addTransition(Transition.SY_SPAWN_DONE, end);
		
		r.addTransition(Transition.SY_BLOCK_TRANSACTION_FINISHED, start);
		
		return end;
	}
	private NormalShipLoaded spawnBlueprint(WaitingForShipyardOrder start, DesignLoaded dloaded, NormalShipLoaded end, ShipyardEntityState gObj) {
		
		SpawningBlueprint spBb = new SpawningBlueprint(gObj);
		start.addTransition(Transition.SY_SPAWN_BLUEPRINT, spBb);
		BlueprintSpawned bbSpawned = new BlueprintSpawned(gObj);
		spBb.addTransition(Transition.SY_SPAWN_DONE, bbSpawned);
		
		RemovingPhysical rem = new RemovingPhysical(gObj);
		bbSpawned.addTransition(Transition.SY_ERROR, rem);
		
		ConvertingRealToVirtual convertingRealToVirtual = new ConvertingRealToVirtual(gObj);
		bbSpawned.addTransition(Transition.SY_CONVERT, convertingRealToVirtual);
		
		convertingRealToVirtual.addTransition(Transition.SY_CONVERSION_DONE, dloaded);
		
		return end;
	}


	@Override
	public void onMsg(Message message) {

	}

	public State getById(Class<? extends ShipyardState> clazz, boolean isInRepair) {
		if(clazz == designLoaded.getClass()){
			return designLoaded;
		}else if(clazz == spawningDesign.getClass()){
			if(isInRepair){
				return constructingRepair;
			}else{
				return spawningDesign;
			}
		}else if(clazz == deconstructing.getClass()){
			if(isInRepair){
				return deconstructingRepair;
			}else{
				return deconstructing;
			}
		}else{
			return null;
		}
	}

}
