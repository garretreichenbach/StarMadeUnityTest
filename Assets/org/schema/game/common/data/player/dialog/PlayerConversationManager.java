package org.schema.game.common.data.player.dialog;

import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.controller.tutorial.states.SatisfyingCondition;
import org.schema.game.client.controller.tutorial.states.TalkToNPCTestState;
import org.schema.game.client.controller.tutorial.states.TutorialEnded;
import org.schema.game.client.controller.tutorial.states.TutorialEndedTextState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.dialog.conversation.ConverationUpdate;
import org.schema.game.common.data.player.dialog.conversation.ConversationUpdateClientHail;
import org.schema.game.common.data.player.dialog.conversation.ConversationUpdateClientSelect;
import org.schema.game.common.data.player.dialog.conversation.ConversationUpdateServerEnd;
import org.schema.game.common.data.player.dialog.conversation.ConversationUpdateServerUpdate;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.remote.RemoteConversation;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlayerConversationManager extends AiEntityState {

	public final static Object2ObjectOpenHashMap<String, String> translations = new Object2ObjectOpenHashMap<String, String>();
	private final StateInterface state;
	private final PlayerState playerState;
	AbstractOwnerState lastPartner = null;
	private PlayerConversation conversation;
	private ObjectArrayFIFOQueue<ConverationUpdate> updates = new ObjectArrayFIFOQueue<ConverationUpdate>();

	public PlayerConversationManager(PlayerState playerState) {
		super("converation", playerState.getState());
		this.playerState = playerState;
		this.state = playerState.getState();

	}

	public void clientInteracted(AbstractOwnerState aiCharacterPlayer) {
		TutorialMode m = ((GameClientState) getState()).getController().getTutorialMode();
		if (playerState.isInTutorial() && m != null &&
				(!(m.getMachine().getFsm().getCurrentState() instanceof TalkToNPCTestState) ||
						!((TalkToNPCTestState) m.getMachine().getFsm().getCurrentState()).getNpcName().equals(aiCharacterPlayer.getAbstractCharacterObject().getRealName()))) {
			if (!(m.getMachine().getFsm().getCurrentState() instanceof TutorialEndedTextState) && !(m.getMachine().getFsm().getCurrentState() instanceof TutorialEnded)) {

				System.err.println("[CLIENT][TUTORIAL] cant talk because of state: " + m.getMachine().getFsm().getCurrentState());
				String markers = "";
				if (m.getMachine().getFsm().getCurrentState() instanceof SatisfyingCondition && ((SatisfyingCondition) m.getMachine().getFsm().getCurrentState()).getMarkers() != null) {
					markers = "(follow the flashing indicator)";
				}

				if (m.getMachine().getFsm().getCurrentState() != m.getMachine().getStartState()) {
					if ((!(m.getMachine().getFsm().getCurrentState() instanceof TalkToNPCTestState))) {
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't talk now.\nPlease complete your\nobjective first!\n",  markers), 0);
						return;
					} else {
						String npcName = ((TalkToNPCTestState) m.getMachine().getFsm().getCurrentState()).getNpcName();
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't talk to this NPC.\nPlease talk to\n%s instead!\n%s",  npcName,  markers), 0);
						return;
					}
				}
			}
		}

		if (conversation == null || conversation.getConverationPartner() != aiCharacterPlayer) {
			playerState.getNetworkObject().converationBuffer.add(new RemoteConversation(
					new ConversationUpdateClientHail(aiCharacterPlayer.getId()), playerState.isOnServer()));
		}
	}

	@Override
	public void updateOnActive(Timer timer) {
		if (!updates.isEmpty()) {
			synchronized (updates) {
				while (!updates.isEmpty()) {
					handleUpdate(updates.dequeue());
				}
			}
		}

		if (conversation != null) {
			lastPartner = conversation.getConverationPartner();
			if (lastPartner != null) {
				lastPartner.conversationPartner = playerState;
			}
			conversation.update(timer);
			if (!isOnServer() && conversation.getConverationPartner() instanceof AIPlayer) {
				AIPlayer p = (AIPlayer) conversation.getConverationPartner();
				SimpleTransformableSendableObject affinity = p.getCreature().getAffinity();
				if (affinity instanceof EditableSendableSegmentController && ((GameClientState) getState()).getController().getTutorialMode() != null) {
					((GameClientState) getState()).getController().getTutorialMode().currentContext = (EditableSendableSegmentController) affinity;
				}
			}

			//conversation can become null on cancel state
			if (conversation == null) {
				System.err.println("[CONVERSATION] " + state + ": " + playerState + " Connversation was canceled (CancelState)");
			}

			if (conversation != null && isOnServer() && conversation.isChanged()) {
				ConversationUpdateServerUpdate c = new ConversationUpdateServerUpdate(conversation);
				playerState.getNetworkObject().converationBuffer.add(new RemoteConversation(c, playerState.isOnServer()));
				conversation.setChanged(false);
			}
		} else {
			if (lastPartner != null) {
				lastPartner.conversationPartner = null;
				lastPartner = null;
			}
		}
	}

	private void handleUpdate(ConverationUpdate dequeue) {
		if (dequeue instanceof ConversationUpdateClientHail) {
			handleClientHail(((ConversationUpdateClientHail) dequeue));
		}
		if (dequeue instanceof ConversationUpdateServerUpdate) {
			handleServerUpdate(((ConversationUpdateServerUpdate) dequeue));
		}
		if (dequeue instanceof ConversationUpdateClientSelect) {
			handleClientSelectOnServer(((ConversationUpdateClientSelect) dequeue));
		}
		if (dequeue instanceof ConversationUpdateServerEnd) {
			handleServerEndOnClient(((ConversationUpdateServerEnd) dequeue));
		}
	}

	public void removeConversation() {
		conversation = null;
	}

	private void handleServerEndOnClient(
			ConversationUpdateServerEnd conversationUpdateServerEnd) {
		System.err.println("[CLIENT] CLIENT RECEIVED CONVERSATION END " + conversation);
		if (conversation != null) {
			conversation.deactivateClient();
			conversation = null;
		} else {
			System.err.println("WARNING: cannot end conversation, because it doesnt exist");
		}
	}

	private void handleClientSelectOnServer(
			ConversationUpdateClientSelect c) {
		if (isOnServer()) {
			if (conversation != null && conversation.getConverationPartner().getId() == c.getConversationParterId()) {
				conversation.handleClientSelect(c.getSelection());
			} else {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("Error Select: conversation partner\nnot found on server: %s (%s)",  c.getConversationParterId(),  conversation), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
			}
		}

	}

	private void handleServerUpdate(
			ConversationUpdateServerUpdate c) {
		if (!isOnServer()) {
			PlayerConversation conversation = c.getConversation();
			conversation.setPlayerState(playerState);
			conversation.setManager(this);
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(c.getConversationParterId());
			if (sendable != null && sendable instanceof AICreature<?>) {
				conversation.setConverationPartner(((AICreature<?>) sendable).getOwnerState());
				if (this.conversation != null) {
					this.conversation.deactivateClient();
				}
				this.conversation = conversation;
				this.conversation.activateClient();
			} else {
				GameClientState state = (GameClientState) this.state;
				state.getController().popupAlertTextMessage(Lng.str("Error: conversation partner\nnot found on client:",  c.getConversationParterId()), 0);
			}
		}
	}

	private void handleClientHail(
			ConversationUpdateClientHail c) {
		if (isOnServer()) {

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(c.getConversationParterId());
			System.err.println("[DIALOG] CONVERSATION PARTNER: " + sendable);
			if (sendable instanceof AICreature<?>) {
				PlayerConversation pc = new PlayerConversation(playerState, ((AICreature<?>) sendable).getOwnerState(), this);
				boolean success = pc.createOnServer();
				if(success) {
					conversation = pc;
					ConversationUpdateServerUpdate update = new ConversationUpdateServerUpdate(conversation);
				}
			} else {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("Error: conversation partner\nnot found on server: %s -> %s",  c.getConversationParterId(), sendable), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
			}

		}
	}

	public void updateFromNetworkObject() {
		ObjectArrayList<RemoteConversation> rb = playerState.getNetworkObject().converationBuffer.getReceiveBuffer();

		for (int i = 0; i < rb.size(); i++) {
			ConverationUpdate converationUpdate = rb.get(i).get();
			synchronized (updates) {
				updates.enqueue(converationUpdate);
			}
		}
	}

	public void pressedSelection(int selection) {
		if (conversation != null) {
			ConversationUpdateClientSelect c = new ConversationUpdateClientSelect(conversation.getConverationPartner(), selection);
			playerState.getNetworkObject().converationBuffer.add(new RemoteConversation(c, playerState.isOnServer()));
		} else {
			System.err.println("ERROR: pressedSelection() no conversation ");
		}
	}

	public void cancelCurrentConversationOnServer() {
		if (conversation != null) {
			ConversationUpdateServerEnd c = new ConversationUpdateServerEnd(conversation.getConverationPartner().getId());
			playerState.getNetworkObject().converationBuffer.add(new RemoteConversation(c, playerState.isOnServer()));
		} else {
			System.err.println("ERROR: calcelCurrentConversationServer() no conversation ");
		}
	}

	public boolean isConverationPartner(AbstractOwnerState abstractOwnerState) {
		if (conversation == null) {
			return false;
		}
		return conversation.getConverationPartner() == abstractOwnerState;
	}
}
