package org.schema.game.common.data.player;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.ai.*;
import org.schema.game.common.data.element.quarters.crew.CrewMember;
import org.schema.game.network.objects.NetworkPlayer;
import org.schema.game.network.objects.remote.RemoteCrewFleet;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.ListObjectCallback;
import org.schema.schine.resource.tag.ListSpawnObjectCallback;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.util.List;

public class PlayerAiManager extends GUIObservable {

	private final ObjectArrayList<AiInterfaceContainer> fleetUIDs = new ObjectArrayList<>();
	private final ObjectArrayList<AiInterfaceContainer> crewUIDs = new ObjectArrayList<>();
	private final PlayerState player;
	private final StateInterface state;

	private final ObjectArrayFIFOQueue<CrewFleetRequest> crewFleetRequests = new ObjectArrayFIFOQueue<CrewFleetRequest>();

	public PlayerAiManager(PlayerState player) {
		this.state = player.getState();
		this.player = player;
	}

	private static byte getType(AiInterfaceContainer ai) {
		return ai.getType();
	}

	public List<AiInterfaceContainer> getFleet() {
		return fleetUIDs;
	}

	public List<AiInterfaceContainer> getCrew() {
		return crewUIDs;
	}

	public void update() {
		if (!crewFleetRequests.isEmpty()) {
			synchronized (crewFleetRequests) {
				while (!crewFleetRequests.isEmpty()) {

					handleRequest(crewFleetRequests.dequeue());
				}
			}
		}
	}

	private void handleRequest(CrewFleetRequest req) {
		assert (req.ai != null);
		if (req.mode == CrewFleetRequest.MODE_ADD) {
			UnloadedAiContainer unloadedAiContainer = new UnloadedAiContainer(req.ai, state, req.type);
			if (req.type == CrewFleetRequest.TYPE_CREW) {
				if (!crewUIDs.contains(unloadedAiContainer)) {
					crewUIDs.add(unloadedAiContainer);
				}
			} else if (req.type == CrewFleetRequest.TYPE_FLEET) {
				if (!fleetUIDs.contains(unloadedAiContainer)) {
					fleetUIDs.add(new UnloadedAiContainer(req.ai, state, req.type));
				}
			} else {
				throw new IllegalArgumentException();
			}
		} else if (req.mode == CrewFleetRequest.MODE_REMOVE) {
			UnloadedAiContainer unloadedAiContainer = new UnloadedAiContainer(req.ai, state, req.type);
			if (req.type == CrewFleetRequest.TYPE_CREW) {
				try {
					((AIGameConfiguration<?, ?>) unloadedAiContainer.getAi().getAiConfiguration()).get(Types.OWNER).switchSetting("none", true);
				} catch (StateParameterNotFoundException e) {
					e.printStackTrace();
				} catch (UnloadedAiEntityException e) {
					e.printStackTrace();
				}
				crewUIDs.remove(unloadedAiContainer);
			} else if (req.type == CrewFleetRequest.TYPE_FLEET) {
				fleetUIDs.remove(unloadedAiContainer);
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
		if (player.isOnServer()) {
			//send on server
			player.getNetworkObject().crewRequest.add(new RemoteCrewFleet(req, player.getNetworkObject()));
		}
		notifyObservers();
	}

	/**
	 * @return the state
	 */
	public StateInterface getState() {
		return state;
	}

	/**
	 * @return the player
	 */
	public PlayerState getPlayer() {
		return player;
	}

	public void addAI(AiInterfaceContainer ai) {
		CrewFleetRequest crewFleetRequest = new CrewFleetRequest(ai.getUID(), CrewFleetRequest.MODE_ADD, getType(ai));
		if (player.isOnServer()) {
			handleRequest(crewFleetRequest);
		} else {
			player.getNetworkObject().crewRequest.add(new RemoteCrewFleet(crewFleetRequest, player.getNetworkObject()));
		}
	}

	public void removeAI(AiInterfaceContainer ai) {
		CrewFleetRequest crewFleetRequest = new CrewFleetRequest(ai.getUID(), CrewFleetRequest.MODE_REMOVE, getType(ai));
		if (player.isOnServer()) {
			handleRequest(crewFleetRequest);
		} else {
			player.getNetworkObject().crewRequest.add(new RemoteCrewFleet(crewFleetRequest, player.getNetworkObject()));
		}
	}

	public void updateToFullNetworkObject() {
		for (AiInterfaceContainer s : fleetUIDs) {
			player.getNetworkObject().crewRequest.add(new RemoteCrewFleet(new CrewFleetRequest(s.getUID(), CrewFleetRequest.MODE_ADD, CrewFleetRequest.TYPE_FLEET), player.getNetworkObject()));
		}
		for (AiInterfaceContainer s : crewUIDs) {
			player.getNetworkObject().crewRequest.add(new RemoteCrewFleet(new CrewFleetRequest(s.getUID(), CrewFleetRequest.MODE_ADD, CrewFleetRequest.TYPE_CREW), player.getNetworkObject()));
		}
	}

	public void updateToNetworkObject() {

	}

	public void updateFromNetworkObject(NetworkPlayer p) {
		for (int i = 0; i < p.crewRequest.getReceiveBuffer().size(); i++) {
			RemoteCrewFleet remoteCrewFleet = p.crewRequest.getReceiveBuffer().get(i);
			synchronized (crewFleetRequests) {
				crewFleetRequests.enqueue(remoteCrewFleet.get());
			}
		}
	}

	public void initFromNetworkObject(NetworkPlayer p) {

		updateFromNetworkObject(p);
	}

	public Tag toTagStructure() {

		ListObjectCallback<AiInterfaceContainer> cb = e -> new Tag[]{new Tag(Type.STRING, null, e.getUID()), new Tag(Type.BYTE, null, e.getType()), FinishTag.INST};

		return new Tag(Type.STRUCT, null, new Tag[]{Tag.listToTagStructUsing(crewUIDs, Type.STRUCT, null, cb), Tag.listToTagStructUsing(fleetUIDs, Type.STRUCT, null, cb), FinishTag.INST});
	}

	public void fromTagStructure(Tag tag) {
		Tag[] s = (Tag[]) tag.getValue();

		ListSpawnObjectCallback<AiInterfaceContainer> cb = e -> {
			if (e instanceof String) {
				//old
				return new UnloadedAiContainer((String) e, state, CrewFleetRequest.TYPE_CREW);
			} else {
				Tag[] t = (Tag[]) e;
				return new UnloadedAiContainer((String) t[0].getValue(), state, (Byte) t[1].getValue());
			}
		};

		Tag.listFromTagStruct(crewUIDs, s[0], cb);

		Tag.listFromTagStruct(fleetUIDs, s[1], cb);
	}

	public boolean hasCrew() {
		return !crewUIDs.isEmpty();
	}

	public boolean hasFleet() {
		return !fleetUIDs.isEmpty();
	}

	public boolean contains(AiInterface ai) {
		return containsCrew(ai) || containsFleet(ai);

	}

	public boolean containsCrew(AiInterface ai) {
		for (AiInterfaceContainer c : crewUIDs) {
			if (c.getUID().equals(ai.getUniqueIdentifier())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsFleet(AiInterface ai) {
		for (AiInterfaceContainer c : fleetUIDs) {
			if (c.getUID().equals(ai.getUniqueIdentifier())) {
				return true;
			}
		}
		return false;

	}

	public CrewMember getCrewByName(String name) {
		for(AiInterfaceContainer c : crewUIDs) {
//			if(c instanceof CrewMember && ((CrewMember) c).getName().equals(name)) return (CrewMember) c;
		}
		return null;
	}
}
