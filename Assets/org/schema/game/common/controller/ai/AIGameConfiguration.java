package org.schema.game.common.controller.ai;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.Waiting;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.AIConfigurationInterface;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class AIGameConfiguration<E extends AiEntityState, A extends SimpleTransformableSendableObject> implements TagSerializable, AIConfigurationInterface<Types> {

	private final Int2ObjectOpenHashMap<AIConfiguationElements<?>> elements = new Int2ObjectOpenHashMap<AIConfiguationElements<?>>();

	private final A owner;

	private StateInterface state;
	private State lastAiState;

	private E aIEntity;
	private SegmentPiece controllerBlock;

	private ObjectArrayFIFOQueue<String[]> settingMods = new ObjectArrayFIFOQueue<String[]>();

	@SuppressWarnings("unused")
	private SimpleGameObject lastTarget;
	private long lastError;

	public AIGameConfiguration(StateInterface state, A owner) {
		this.state = state;
		this.owner = owner;

		E idleEntityState = getIdleEntityState();
		assert (idleEntityState != null);
		aIEntity = idleEntityState;

		initialize(elements);

		for (AIConfiguationElements<?> e : elements.values()) {
			assert (elements.get(e.getType().ordinal()) == e) : elements.get(e.getType().ordinal()) + " ----------- " + e;
		}
	}

	public abstract void initialize(Int2ObjectOpenHashMap<AIConfiguationElements<?>> elements);

	@Override
	public void applyServerSettings() {
		assert(isOnServer());
		for (AIConfiguationElements<?> e : elements.values()) {
			onServerSettingChanged(e);
		}
		if(isOnServer() && owner.getRuleEntityManager() != null) {
			owner.getRuleEntityManager().triggerOnAIActivityChange();
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		if (tag.getName().equals("AIConfig1")) {
			Tag[] tags = (Tag[]) tag.getValue();
			for (int j = 0; j < tags.length && tags[j].getType() != Type.FINISH; j++) {
				Tag[] configTag = (Tag[]) tags[j].getValue();
				AIConfiguationElements<?> aiConfiguationElements = elements.get(((Byte) configTag[0].getValue()).intValue());
				aiConfiguationElements.fromTagStructure(configTag[1]);
				onServerSettingChanged(aiConfiguationElements);
			}
		} else if (tag.getName().equals("AIConfig0")) {
			Tag[] tags = (Tag[]) tag.getValue();
			for (int j = 0; j < tags.length && tags[j].getType() != Type.FINISH; j++) {
				Tag[] configTag = (Tag[]) tags[j].getValue();
				for (AIConfiguationElements<?> e : elements.values()) {

					if (e != null && ((String) configTag[0].getValue()).equals(e.getType().name())) {
						try {
							//							System.err.println("[SERVER][AI] Tag SWTICHING SETTING "+elements[i].getDescription()+" to "+configTag[1].getValue());
							e.switchSetting(((String) configTag[1].getValue()), true);
							onServerSettingChanged(e);
						} catch (StateParameterNotFoundException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		} else {
			Tag[] tags = (Tag[]) tag.getValue();
			for (int j = 0; j < tags.length && tags[j].getType() != Type.FINISH; j++) {
				Tag[] configTag = (Tag[]) tags[j].getValue();
				for (AIConfiguationElements<?> e : elements.values()) {
					if (e != null && ((String) configTag[0].getValue()).equals(e.getType().name())) {
						try {
							//								System.err.println("[SERVER][AI] Tag SWTICHING SETTING "+elements[i].getDescription()+" to "+configTag[1].getValue());
							e.switchSetting(((String) configTag[1].getValue()), true);
							onServerSettingChanged(e);
						} catch (StateParameterNotFoundException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}

	}

	@Override
	public Tag toTagStructure() {
		//		System.err.println("[AI] WRITING AI TAG OF "+getOwner());

		ArrayList<Tag> configTags = new ArrayList<Tag>();
		Tag[] configTag = new Tag[elements.size() + 1];
		int i = 0;
		for (AIConfiguationElements<?> e : elements.values()) {
			//			System.err.println("[AI] WRITING AI TAG OF "+getOwner()+" -> "+e);
			configTag[i] = e.toTagStructure();
			i++;
		}

		configTag[elements.size()] = FinishTag.INST;
		return new Tag(Type.STRUCT, "AIConfig1", configTag);
	}

	@Override
	public AIConfiguationElements<?> get(Types t) {
		AIConfiguationElements<?> aiConfiguationElements = elements.get(t.ordinal());
		if (aiConfiguationElements == null) {
			throw new IllegalArgumentException("AI Type '" + t.name() + "'[" + t.ordinal() + "] not found for " + owner + ": " + elements);
		}
		return aiConfiguationElements;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AIConfigurationInterface#getAiState()
	 */
	@Override
	public E getAiEntityState() {
		return aIEntity;
	}

	@Override
	public boolean isActiveAI() {
		return aIEntity.isActive();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AIConfigurationInterface#isAIActiveClient()
	 */
	@Override
	public boolean isAIActiveClient() {
		return get(Types.ACTIVE).isOn();
	}

	@Override
	public void callBack(AIConfiguationElementsInterface e, boolean send) {
//		System.err.println("[AI] Setting callback " + getState() + " " + getOwner() + " Executing "+e.toString()+" send callback: " + send);
		if (send) {
			sendSettingModification((AIConfiguationElements<?>) e);
		}

	}

	@Override
	public void update(Timer timer) {
//		if(isOnServer()){
//			System.err.println("I�DAZE: "+getOwner()+": "+isActiveAI()+"; "+getAiEntityState().getCurrentProgram()+"; "+getAiEntityState().getCurrentProgram().isSuspended());
//		}
		
		aIEntity.updateGeneral(timer);
		while (!settingMods.isEmpty()) {
			String[] sA = settingMods.dequeue();

			//					System.err.println(getState()+" "+getOwner()+" RECEIVED AI STAT: "+Arrays.toString(sA));
			for (AIConfiguationElements<?> s : elements.values()) {
				boolean isElement = s.getName().equals(sA[0]);
				if (isElement && !s.getCurrentState().toString().equals(sA[1])) {
					try {
						//only send when on server
						s.switchSetting(sA[1], isOnServer());
						if (isOnServer()) {
							onServerSettingChanged(s);
						} else {
							onClientSettingChanged(s);
						}
					} catch (StateParameterNotFoundException e) {
						e.printStackTrace();
					}
				} else if (!isOnServer() && isElement) {
					//always execute on client, even if state didnt change
					//it may have changed because the mode change came
					//from this client
					onClientSettingChanged(s);
				}
			}
		}

		if (isActiveAI()) {
			if (isOnServer()) {
				try {
					if (!isActiveAI() && !(lastAiState instanceof Waiting)) {
						aIEntity.getStateCurrent().stateTransition(Transition.RESTART);
					}
//					System.err.println("UPDATING PROGRAM: "+getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState());
					aIEntity.getCurrentProgram().update(timer);
				} catch (Exception e) {
					System.err.println("Exception: Error occured updating AI (ExcMessage: " + e.getMessage() + "): Current Program: " + aIEntity.getCurrentProgram() + "; state: " + aIEntity.getStateCurrent() + "; in " + owner);

					e.printStackTrace();

					if (System.currentTimeMillis() - lastError > 1000) {
						((GameServerState) state).getController().broadcastMessage(Lng.astr("AI Error occured on Server!\nPlease send in server error report.\n%s",  e.getClass().getSimpleName()), ServerMessage.MESSAGE_TYPE_ERROR);
						lastError = System.currentTimeMillis();
					}

				}
			} else {
				try {
					aIEntity.updateOnActive(timer);
				} catch (FSMException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
				if (owner.isClientOwnObject()) {
					try {
						if(!((AIConfiguationElements<String>)get(Types.TYPE)).getCurrentState().equals("Fleet")){
							get(Types.ACTIVE).switchSetting(true);
	
							((GameClientState) state).getController().popupAlertTextMessage(
									Lng.str("WARNING\nThis vessel was AI controlled\n... switched off AI\nre-enable AI with %s", ElementKeyMap.getInfo(ElementKeyMap.AI_ELEMENT).getName()), 0);
						}
					} catch (StateParameterNotFoundException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		}
		aIEntity.afterUpdate(timer);
	}

	/**
	 * @return the controllerBlock
	 */
	public SegmentPiece getControllerBlock() {
		return controllerBlock;
	}

	/**
	 * @param controllerBlock the controllerBlock to set
	 */
	public void setControllerBlock(SegmentPiece controllerBlock) {
		this.controllerBlock = controllerBlock;
	}

	/**
	 * @return the elements
	 */
	public Int2ObjectOpenHashMap<AIConfiguationElements<?>> getElements() {
		return elements;
	}

	protected abstract E getIdleEntityState();

	/**
	 * @return the lastAiState
	 */
	public State getLastAiState() {
		return lastAiState;
	}

	/**
	 * @param lastAiState the lastAiState to set
	 */
	public void setLastAiState(State lastAiState) {
		this.lastAiState = lastAiState;
	}

	/**
	 * @return the owner
	 */
	public A getOwner() {
		return owner;
	}

	/**
	 * @return the state
	 */
	public StateInterface getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(StateInterface state) {
		this.state = state;
	}

	private boolean isOnServer() {
		return owner.isOnServer();
	}

	protected void onClientSettingChanged(AIConfiguationElements<?> s) {
		if (s.getType() == Types.ACTIVE && !s.isOn()) {
			System.err.println("[AI] SENTINEL SET TO FALSE ON " + state);
			aIEntity = getIdleEntityState();
		}
	}

	public void onCoreDestroyed(Damager from) {

		if (isOnServer()) {
			((AIConfiguationElements<Boolean>) elements.get(Types.ACTIVE.ordinal())).setCurrentState(false, owner.isOnServer());
			applyServerSettings();

		}
	}

	public void onStartOverheating(Damager from) {

		if (isOnServer()) {
			((AIConfiguationElements<Boolean>) elements.get(Types.ACTIVE.ordinal())).setCurrentState(false, owner.isOnServer());
			applyServerSettings();

		}
	}

	public void onDamageServer(float actualDamage, Damager from) {
//				System.err.println("############asda----------------- onDamageServer "+isActiveAI()+"; "+getAiEntityState()+" ("+(getAiEntityState() instanceof HittableAIEntityState)+")");
		if ((isActiveAI() || isForcedHitReaction()) && aIEntity instanceof HittableAIEntityState) {
			((HittableAIEntityState) aIEntity).handleHitBy(actualDamage, from);
		}
	}

	protected abstract boolean isForcedHitReaction();

	public void onProximity(SegmentController segmentController) {

	}

	protected void onServerSettingChanged(AIConfiguationElements<?> s) {
		if(!isOnServer()) {
			assert(false);
			return;
		}
		if (s.getType() == Types.ACTIVE) {
			if (s.isOn()) {
//				try{
//					throw new NullPointerException("ON "+owner);
//				}catch(Exception r){
//					r.printStackTrace();
//				}
//								System.err.println("[AI][SERVER] AI ENTITY ACTIVATED ON "+owner+" : "+getState());
				prepareActivation();
			} else {
//				try{
//					throw new NullPointerException("OFF "+owner);
//				}catch(Exception r){
//					r.printStackTrace();
//				}
				
				((GameServerState) state).getSimulationManager().onAIDeactivated(owner);
//								System.err.println("[AI][SERVER] AI ENTITY DEACTIVATED ON "+owner+" : "+getState());
			}
			assert (aIEntity) != null;
			if (aIEntity.getCurrentProgram() != null) {
				aIEntity.getCurrentProgram().suspend(!s.isOn());
			} else {
				//				System.err.println("Cannot");
			}
		}

		if (aIEntity.getCurrentProgram() != null) {
			try {
				aIEntity.getCurrentProgram().onAISettingChanged(s);
			} catch (FSMException e) {
				e.printStackTrace();
			}
		}

	}

	protected abstract void prepareActivation();

	public void sendSettingModification(AIConfiguationElements<?> e) {
		if (owner.getNetworkObject() == null) {
//			System.err.println("[AI] NOT SENDING MOD SINCE NETWORK OBJECT IS NULL FOR "+owner);
			return;
		}
		//		System.err.println(getState()+" "+getOwner()+" Senfing modification: "+e);
		RemoteStringArray setting = new RemoteStringArray(2, owner.getNetworkObject());
		setting.set(0, e.getName());
		setting.set(1, e.getCurrentState().toString());

		((AINetworkInterface) owner.getNetworkObject()).getAiSettingsModification().add(setting);
	}

	/**
	 * @param aIEntity the aIEntity to set
	 */
	public void setAIEntity(E aIEntity) {
		this.aIEntity = aIEntity;
	}

	@Override
	public void initFromNetworkObject(NetworkObject networkObject) {
		
	}
	@Override
	public void updateToFullNetworkObject(NetworkObject networkObject) {

		if (isOnServer()) {

			for (AIConfiguationElements<?> e : elements.values()) {
				sendSettingModification(e);
			}
		}

	}

	@Override
	public void updateFromNetworkObject(NetworkObject o) {

		ObjectArrayList<RemoteStringArray> settingsReceive = ((AINetworkInterface) owner.getNetworkObject()).getAiSettingsModification().getReceiveBuffer();
		for (int i = 0; i < settingsReceive.size(); i++) {
			RemoteStringArray remoteStringArray = settingsReceive.get(i);
			synchronized (settingMods) {
//				System.err.println("CLIENT RECEIVED: "+owner+" :: "+remoteStringArray.get(0).get()+"; "+remoteStringArray.get(1).get());
				settingMods.enqueue(new String[]{remoteStringArray.get(0).get(), remoteStringArray.get(1).get()});
			}
		}

	}

	@Override
	public void updateToNetworkObject(NetworkObject networkObject) {
		AINetworkInterface nt = (AINetworkInterface) networkObject;

		if (isOnServer()) {
			if (ServerConfig.DEBUG_FSM_STATE.isOn()) {
				//				if (isActiveAI() && (this.lastAiState != getAiEntityState().getStateCurrent() || ((TargetProgram<?>) getAiEntityState()
				//								.getCurrentProgram()).getTarget() != lastTarget)) {
				String tar = "";
				if (isActiveAI()) {
					tar = "\nTar:" + ((TargetProgram<?>) aIEntity
							.getCurrentProgram()).getTarget();
					this.lastAiState = aIEntity.getStateCurrent();
					lastTarget = ((TargetProgram<?>) aIEntity.getCurrentProgram())
							.getTarget();
				}

				SimulationManager simulationManager = ((GameServerState) state).getSimulationManager();
				String simString = simulationManager.getDebugStringFor(owner);
				String sim = "";
				if (!simString.equals("NOSIM")) {
					sim = "\n" + sim;
				}
				nt.getDebugState().set(
						"[SERVERAI](" + aIEntity.toString() + "[" + (aIEntity.isActive() ? "ON" : "OFF") + "] "
								+ ")"
								+ tar + ";" + sim + "\n " + owner.getGravity());

				//				}else{
				//					nt.getDebugState().set(
				//							"("
				//									+ getAiEntityState().toString()+"["+(getAiEntityState().isActive() ? "ON(?)" : "OFF(!)")+"]"
				//									+ ")");
				//				}
			} else {
				if (nt.getDebugState().get().length() > 0) {
					nt.getDebugState().set("");
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AIConfig(" + owner + "[" + state + "])";
	}

	public void setFrom(AIGameConfiguration<E, A> aiConfiguration) {
		for(Entry<Integer, AIConfiguationElements<?>> a : elements.entrySet()){
			AIConfiguationElements<?> aiConfiguationElements = aiConfiguration.elements.get(a.getKey());
			try {
				a.getValue().switchSetting(aiConfiguationElements.getCurrentState().toString(), false);
			} catch (StateParameterNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
