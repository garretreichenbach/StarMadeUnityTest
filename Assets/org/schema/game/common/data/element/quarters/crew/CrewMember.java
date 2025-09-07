package org.schema.game.common.data.element.quarters.crew;

import api.common.GameServer;
import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.*;
import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.QuarterManager;
import org.schema.game.common.data.player.CrewFleetRequest;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.CreatureSpawn;
import org.schema.game.server.data.CreatureType;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

public class CrewMember extends UnloadedAiContainer implements TagSerializable {

	public static CrewMember fromContainer(AiInterfaceContainer container) throws UnloadedAiEntityException {
		return new CrewMember(container.getUID(), container.getRealName(), container.getAi().getState()); //Todo: Figure out how to properly load the unloaded ai container
	}

	private String name;
	private CrewPositionalInterface positional;
	private int combatSkill;
	private int engineeringSkill;
	private int physicsSkill;
	private int biologySkill;
	private AICharacter character;

	public CrewMember(String uid, String name, StateInterface state) {
		super(uid, state, CrewFleetRequest.TYPE_CREW);
		this.name = name;
	}

	public void update(Timer timer) {
		//Todo: update local
	}

	public CrewPositionalInterface getPositional() {
		return positional;
	}

	public void setPositional(CrewPositionalInterface positional) {
		this.positional = positional;
	}

	public int getCombatSkill() {
		return combatSkill;
	}

	public void setCombatSkill(int combatSkill) {
		this.combatSkill = combatSkill;
	}

	public int getEngineeringSkill() {
		return engineeringSkill;
	}

	public void setEngineeringSkill(int engineeringSkill) {
		this.engineeringSkill = engineeringSkill;
	}

	public int getPhysicsSkill() {
		return physicsSkill;
	}

	public void setPhysicsSkill(int physicsSkill) {
		this.physicsSkill = physicsSkill;
	}

	public int getBiologySkill() {
		return biologySkill;
	}

	public void setBiologySkill(int biologySkill) {
		this.biologySkill = biologySkill;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void levelUp() {
		//Todo: Some sort of menu to invest xp into skills
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		byte version = (Byte) t[0].getValue();
		byte typeByte = (Byte) t[1].getValue();
		uid = (String) t[2].getValue();
		name = (String) t[3].getValue();
		combatSkill = (Integer) t[4].getValue();
		engineeringSkill = (Integer) t[5].getValue();
		physicsSkill = (Integer) t[6].getValue();
		biologySkill = (Integer) t[7].getValue();
	}

	@Override
	public Tag toTagStructure() {
		Tag version = new Tag(Tag.Type.BYTE, null, 0);
		Tag type = new Tag(Tag.Type.BYTE, null, getType());
		Tag uid = new Tag(Tag.Type.STRING, null, getUID());
		Tag name = new Tag(Tag.Type.STRING, null, this.name);
		Tag combatSkill = new Tag(Tag.Type.INT, null, this.combatSkill);
		Tag engineeringSkill = new Tag(Tag.Type.INT, null, this.engineeringSkill);
		Tag physicsSkill = new Tag(Tag.Type.INT, null, this.physicsSkill);
		Tag biologySkill = new Tag(Tag.Type.INT, null, this.biologySkill);
		return new Tag(Tag.Type.STRUCT, this.uid, new Tag[] {version, type, uid, name, combatSkill, engineeringSkill, physicsSkill, biologySkill});
	}

	public void randomizeStats() {
		combatSkill = (int) (Math.random() * 10) + 1;
		engineeringSkill = (int) (Math.random() * 10) + 1;
		physicsSkill = (int) (Math.random() * 10) + 1;
		biologySkill = (int) (Math.random() * 10) + 1;
	}

	public Quarter getAssignedQuarter(QuarterManager manager) {
		for(Quarter q : manager.getQuartersById().values()) {
			if(q.getCrew().contains(this)) return q;
		}
		return null;
	}

	public AICharacter getCharacter() {
		return character;
	}

	public boolean isAlreadySpawned() {
		return positional != null && positional.getSegmentController() != null;
	}

	public AICharacter spawn(PlayerState owner) {
		AIGameCreatureConfiguration<?, ?>[] configuration = {null};
		if(isAlreadySpawned()) return recall();
		else {
			if(positional == null && PlayerUtils.getCurrentControl(owner) instanceof SegmentController) positional = () -> (SegmentController) PlayerUtils.getCurrentControl(owner);
			if(positional == null) return null;
			Quarter quarter = getAssignedQuarter(positional.getSegmentController().getQuarterManager());
			if(quarter == null) {
				Transform transform = new Transform();
				transform.setIdentity();
				owner.getWordTransform(transform);
				CreatureSpawn spawn = new CreatureSpawn(positional.getSegmentController().getSector(new Vector3i()), transform, name, CreatureType.CHARACTER) {
					@Override
					public void initAI(AIGameCreatureConfiguration<?, ?> aiConfiguration) {
						try {
							assert (aiConfiguration != null);
							aiConfiguration.get(Types.ORIGIN_X).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
							aiConfiguration.get(Types.ORIGIN_Y).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
							aiConfiguration.get(Types.ORIGIN_Z).switchSetting(String.valueOf(Integer.MIN_VALUE), false);

							aiConfiguration.get(Types.ROAM_X).switchSetting("16", false);
							aiConfiguration.get(Types.ROAM_Y).switchSetting("3", false);
							aiConfiguration.get(Types.ROAM_Z).switchSetting("16", false);

							aiConfiguration.get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, false);

							aiConfiguration.get(Types.OWNER).switchSetting(getUID(), false);
						} catch(StateParameterNotFoundException e) {
							e.printStackTrace();
						}
						aiConfiguration.getAiEntityState().getEntity().setFactionId(getPositional().getSegmentController().getFactionId());
						configuration[0] = aiConfiguration;
					}
				};
				GameServer.getServerState().getController().queueCreatureSpawn(spawn);
			} else {
				Transform transform = new Transform();
				transform.setIdentity();
				quarter.getFirstGround().getTransform(transform);
				CreatureSpawn spawn = new CreatureSpawn(positional.getSegmentController().getSector(new Vector3i()), transform, name, CreatureType.CHARACTER) {
					@Override
					public void initAI(AIGameCreatureConfiguration<?, ?> aiConfiguration) {
						try {
							assert (aiConfiguration != null);
							aiConfiguration.get(Types.ORIGIN_X).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
							aiConfiguration.get(Types.ORIGIN_Y).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
							aiConfiguration.get(Types.ORIGIN_Z).switchSetting(String.valueOf(Integer.MIN_VALUE), false);

							aiConfiguration.get(Types.ROAM_X).switchSetting("16", false);
							aiConfiguration.get(Types.ROAM_Y).switchSetting("3", false);
							aiConfiguration.get(Types.ROAM_Z).switchSetting("16", false);

							aiConfiguration.get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, false);

							aiConfiguration.get(Types.OWNER).switchSetting(getUID(), false);
						} catch(StateParameterNotFoundException e) {
							e.printStackTrace();
						}
						aiConfiguration.getAiEntityState().getEntity().setFactionId(getPositional().getSegmentController().getFactionId());
						configuration[0] = aiConfiguration;
					}
				};
				GameServer.getServerState().getController().queueCreatureSpawn(spawn);
			}
		}
		try {
			(new StarRunnable() {

				@Override
				public void run() {
					if(configuration[0] != null) character = (AICharacter) configuration[0].getAiEntityState().getEntity();
				}
			}).runLater(1000);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return character;
	}

	public AICharacter recall() {
		if(isAlreadySpawned()) {
			try {
				Quarter quarter = getAssignedQuarter(positional.getSegmentController().getQuarterManager());
				if(character == null) throw new NullPointerException("Character is null!");
				if(quarter == null) character.getWorldTransform().set(positional.getSegmentController().getWorldTransform());
				else quarter.getFirstGround().getTransform(character.getWorldTransform());
			} catch(NullPointerException exception) {
				exception.printStackTrace();
			}
		}
		return character;
	}

	public Transform getTransform() {
		if(isAlreadySpawned()) return character.getWorldTransform();
		else return null;
	}
}

