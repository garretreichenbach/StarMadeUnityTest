package org.schema.game.common.data.player.dialog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.script.ScriptException;

import org.luaj.vm2.LuaError;
import org.schema.game.client.controller.PlayerDialogInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.sound.controller.AudioController;

import javax.script.ScriptException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class PlayerConversation {

	private PlayerState playerState;

	private AbstractOwnerState converationPartner;

	private PlayerConversationManager manager;

	private State currentState;

	private String title;

	private Object[] message;

	private AICreatureDialogAI dialogAI;

	private AwnserContainer[] answers;

	private PlayerDialogInput clientPlayerInput;

	private boolean changed;

	public PlayerConversation(PlayerState playerState, AbstractOwnerState converationPartner, PlayerConversationManager manager) {
		super();
		this.playerState = playerState;
		this.converationPartner = converationPartner;
		this.manager = manager;
		dialogAI = new AICreatureDialogAI("aiState", playerState, converationPartner);
	}

	public PlayerConversation() {
	// no dialogAI on client
	}

	public void update(Timer timer) {
		if (playerState.isOnServer()) {
			updateServer(timer);
		} else {
			updateClient(timer);
		}
	}

	private void updateClient(Timer timer) {
	}

	private void updateServer(Timer timer) {
		try {
			dialogAI.updateOnActive(timer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (currentState != dialogAI.getStateCurrent()) {
			if (dialogAI.getStateCurrent() instanceof DialogTextState) {
				DialogTextState s = (DialogTextState) dialogAI.getStateCurrent();
				this.title = Lng.str("Conversation with %s", converationPartner.getName());
				this.message = s.getMessage();
				this.answers = s.getAwnsers();
				changed = true;
			}
			currentState = dialogAI.getStateCurrent();
			if (currentState != null && currentState instanceof DialogCancelState) {
				System.err.println("[SERVER] Cancel state reached. Ending Conversation here");
				manager.cancelCurrentConversationOnServer();
				changed = false;
			}
		}
	}

	/**
	 * @return the playerState
	 */
	public PlayerState getPlayerState() {
		return playerState;
	}

	/**
	 * @param playerState the playerState to set
	 */
	public void setPlayerState(PlayerState playerState) {
		this.playerState = playerState;
	}

	/**
	 * @return the converationPartner
	 */
	public AbstractOwnerState getConverationPartner() {
		return converationPartner;
	}

	/**
	 * @param converationPartner the converationPartner to set
	 */
	public void setConverationPartner(AbstractOwnerState converationPartner) {
		this.converationPartner = converationPartner;
	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeUTF(title);
		buffer.writeByte(message.length);
		for (int i = 0; i < message.length; i++) {
			buffer.writeUTF(message[i].toString());
		}
		buffer.writeByte((byte) answers.length);
		for (int x = 0; x < answers.length; x++) {
			buffer.writeByte(answers[x].awnser.length);
			for (int i = 0; i < answers[x].awnser.length; i++) {
				buffer.writeUTF(answers[x].awnser[i].toString());
			}
		}
	}

	public void handleClientSelect(int selection) {
		assert (getState() instanceof ServerStateInterface);
		System.err.println("[SERVER] Received selection " + selection + " for Current: " + currentState);
		if (currentState != null) {
			if (currentState instanceof DialogTextState) {
				DialogTextState s = (DialogTextState) currentState;
				if (selection == answers.length - 1) {
					System.err.println("[SERVER] " + playerState + " canceling conversation with " + converationPartner);
					manager.cancelCurrentConversationOnServer();
				// manager.removeConversation();
				} else {
					try {
						s.stateTransition(Transition.DIALOG_AWNSER, selection);
					} catch (FSMException e) {
						e.printStackTrace();
					}
				}
			} else if (currentState instanceof DialogCancelState) {
				manager.cancelCurrentConversationOnServer();
			}
		}
	}

	public void deserialize(DataInput buffer) throws IOException {
		title = buffer.readUTF();
		int msgLen = buffer.readByte();
		message = new String[msgLen];
		for (int i = 0; i < msgLen; i++) {
			message[i] = buffer.readUTF();
		}
		answers = new AwnserContainer[buffer.readByte()];
		for (int x = 0; x < answers.length; x++) {
			int len = buffer.readByte();
			answers[x] = new AwnserContainer(new Object[len]);
			for (int i = 0; i < len; i++) {
				answers[x].awnser[i] = buffer.readUTF();
			}
		}
	}

	public void deactivateClient() {
		if (playerState.isClientOwnPlayer()) {
			if (clientPlayerInput != null) {
				clientPlayerInput.deactivate();
				((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
				((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
			}
		}
	}

	public Object[] getFormatedAwnsers() {
		Object[] a = new Object[answers.length];
		for (int i = 0; i < answers.length; i++) {
			a[i] = getFormatedAwnser(i);
		}
		return a;
	}

	public String getFormatedAwnser(int index) {
		return getFormatedMessage(answers[index].awnser);
	}

	public static String getFormatedMessage(Object[] message) {
		String translation = PlayerConversationManager.translations.get(message[0].toString().trim());
		if (translation != null) {
			message[0] = translation;
		} else {
		// System.err.println("MSG::: \n"+message[0].toString().trim());
		// for(String a : PlayerConversationManager.translations.keySet()){
		// if(a.startsWith("\"Gree")){
		// System.err.println(a);
		// }
		// }
		// assert(false):"\""+message[0].toString()+"\"\n"+PlayerConversationManager.translations;
		}
		if (message.length == 1) {
			return message[0].toString();
		} else {
			final int len = message.length;
			return switch(len) {
				case (2) -> String.format(message[0].toString(), message[1]);
				case (3) -> String.format(message[0].toString(), message[1], message[2]);
				case (4) -> String.format(message[0].toString(), message[1], message[2], message[3]);
				case (5) -> String.format(message[0].toString(), message[1], message[2], message[3], message[4]);
				case (6) -> String.format(message[0].toString(), message[1], message[2], message[3], message[4], message[5]);
				case (7) -> String.format(message[0].toString(), message[1], message[2], message[3], message[4], message[5]);
				default -> String.format(message[0].toString(), message[1], message[2], message[3], message[4], message[5], message[6]);
			};
		}
	}

	public String getFormatedMessage() {
		return getFormatedMessage(message);
	}

	public void activateClient() {
		if (playerState.isClientOwnPlayer()) {
			GameClientState state = (GameClientState) getState();
			// #RM1958 avoid java 6 compiler warning
			clientPlayerInput = new PlayerDialogInput(state, title, getFormatedMessage(), getFormatedAwnsers()) {

				/* (non-Javadoc)
				 * @see org.schema.game.client.controller.PlayerInput#cancel()
				 */
				@Override
				public void cancel() {
					manager.pressedSelection(answers.length - 1);
				}

				@Override
				public void pressedAwnser(int intValue) {
					manager.pressedSelection(intValue);
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
				}
			};
			clientPlayerInput.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(958);
			System.err.println("ACTIVATING CLIENT CONVERSATION");
		}
	}

	protected void okPressed() {
		System.err.println("PRESSED OK ");
	// manager.pressedSelection(0);
	}

	public StateInterface getState() {
		return playerState.getState();
	}

	/**
	 * @return the manager
	 */
	public PlayerConversationManager getManager() {
		return manager;
	}

	/**
	 * @param manager the manager to set
	 */
	public void setManager(PlayerConversationManager manager) {
		this.manager = manager;
	}

	public boolean createOnServer() {
		if (converationPartner instanceof AIPlayer) {
			AIPlayer p = (AIPlayer) converationPartner;
			try {
				DialogSystem load = DialogSystem.load(dialogAI);
				HashMap<String, FiniteStateMachine<?>> machines = new HashMap<String, FiniteStateMachine<?>>();
				DialogProgram dialogProgram = new DialogProgram(manager, false, machines);
				DialogStateMachine dialogStateMachine = new DialogStateMachine(dialogAI, dialogProgram, load.getFromState());
				dialogStateMachine.setDirectStates(load.getDirectStates());
				machines.put("default", dialogStateMachine);
				dialogProgram.reinit(machines);
				dialogAI.setCurrentProgram(dialogProgram);
				if (converationPartner instanceof AIPlayer) {
					System.err.println("[CONVERSATION] CURRENT CONVERSATION STATE: parter " + converationPartner + "; player " + playerState + ": conversationState: " + ((AIPlayer) converationPartner).getConversationState(playerState) + "; " + load.getDirectStates());
					String conversationState = ((AIPlayer) converationPartner).getConversationState(playerState);
					if (load.getDirectStates().containsKey(conversationState)) {
						dialogStateMachine.setStartingState(load.getDirectStates().get(conversationState));
					} else {
						System.err.println("[CONVERSATION] Concersation State: " + conversationState + " not found. using root state");
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (LuaError e) {
				e.printStackTrace();
				((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("Warning: Script failed to execute!\n(see log)"), ServerMessage.MESSAGE_TYPE_ERROR);
				return false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.err.println("[CONVERSATION][LUA] " + playerState + " -> " + converationPartner + ": SCRIPT FINISHED CREATING: " + converationPartner.getConversationScript());
		}
		return true;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
}
