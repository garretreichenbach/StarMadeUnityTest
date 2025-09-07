package org.schema.game.common.data.mission.spawner.component;

import java.util.Locale;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.creature.CreaturePartNode;
import org.schema.game.common.data.creature.CreaturePartNode.AttachmentType;
import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.data.CreatureSpawn;
import org.schema.game.server.data.CreatureType;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.linearmath.Transform;

public class SpawnComponentCreature implements SpawnComponent {

	private CreatureType creatureType;
	private String bottom = "none";
	private String middle = "none";
	private String top = "none";
	private String name = "/";
	private String behavior = AIGameCreatureConfiguration.BEHAVIOR_ROAMING;
	private int factionId = FactionManager.FAUNA_GROUP_ENEMY[0];

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		creatureType = CreatureType.values()[(Integer) t[0].getValue()];
		bottom = (String) t[1].getValue();
		middle = (String) t[2].getValue();
		top = (String) t[3].getValue();
		name = (String) t[4].getValue();
		behavior = (String) t[5].getValue();
		factionId = (Integer) t[6].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{

				new Tag(Type.INT, null, creatureType.ordinal()),
				new Tag(Type.STRING, null, bottom),
				new Tag(Type.STRING, null, middle),
				new Tag(Type.STRING, null, top),
				new Tag(Type.STRING, null, name),
				new Tag(Type.STRING, null, behavior),
				new Tag(Type.INT, null, factionId),
				FinishTag.INST
		});
	}


	@Override
	public void execute(final SpawnMarker marker) {
		Transform f = new Transform(marker.attachedTo().getWorldTransform());
		Vector3f pos = new Vector3f(marker.getPos().x - SegmentData.SEG_HALF, marker.getPos().y - SegmentData.SEG_HALF, marker.getPos().z - SegmentData.SEG_HALF);

		f.basis.transform(pos);
		f.origin.add(pos);

		GameServerState state = (GameServerState) marker.getState();
		Sector sector = state.getUniverse().getSector(marker.attachedTo().getSectorId());

		if (sector != null) {

			assert (creatureType != null);
			assert (name != null);
			CreatureSpawn s = new CreatureSpawn(
					new Vector3i(sector.pos),
					f, name, creatureType) {
				@Override
				public void initAI(
						AIGameCreatureConfiguration<?, ?> aiConfiguration) {
					try {
						assert (aiConfiguration != null);
						aiConfiguration.get(Types.ORIGIN_X).switchSetting(String.valueOf(marker.getPos().x - SegmentData.SEG_HALF), false);
						aiConfiguration.get(Types.ORIGIN_Y).switchSetting(String.valueOf(marker.getPos().y - SegmentData.SEG_HALF), false);
						aiConfiguration.get(Types.ORIGIN_Z).switchSetting(String.valueOf(marker.getPos().z - SegmentData.SEG_HALF), false);

						aiConfiguration.get(Types.ROAM_X).switchSetting("4", false);
						aiConfiguration.get(Types.ROAM_Y).switchSetting("2", false);
						aiConfiguration.get(Types.ROAM_Z).switchSetting("4", false);

						aiConfiguration.get(Types.ORDER).switchSetting(behavior, false);

						aiConfiguration.getAiEntityState().getEntity().setFactionId(getFactionId());

						aiConfiguration.getAiEntityState().getEntity().setAffinity(marker.attachedTo());

					} catch (StateParameterNotFoundException e) {
						e.printStackTrace();
					}

				}
			};

			if (creatureType == CreatureType.CREATURE_SPECIFIC) {
				CreaturePartNode pBot = new CreaturePartNode(PartType.BOTTOM, state, bottom, null);
				if (!middle.toLowerCase(Locale.ENGLISH).equals("none")) {
					CreaturePartNode pMid = new CreaturePartNode(PartType.MIDDLE, state, middle, null);
					pBot.attach(state, pMid, AttachmentType.MAIN);

					if (!top.toLowerCase(Locale.ENGLISH).equals("none")) {
						CreaturePartNode pTop = new CreaturePartNode(PartType.TOP, state, top, null);
						pMid.attach(state, pTop, AttachmentType.MAIN);
					}
				}
				s.setNode(pBot);
			} else if (creatureType == CreatureType.CHARACTER) {

			}

			state.getController().queueCreatureSpawn(s);
		} else {
			System.err.println("[SPAWNER][ERROR] cannot spawn. sector not loaded " + marker.attachedTo().getSectorId() + "; att: " + marker.attachedTo());
		}

	}

	@Override
	public SpawnComponentType getType() {
		return SpawnComponentType.CREATURE;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the top
	 */
	public String getTop() {
		return top;
	}

	/**
	 * @param top the top to set
	 */
	public void setTop(String top) {
		this.top = top;
	}

	/**
	 * @return the middle
	 */
	public String getMiddle() {
		return middle;
	}

	/**
	 * @param middle the middle to set
	 */
	public void setMiddle(String middle) {
		this.middle = middle;
	}

	/**
	 * @return the bottom
	 */
	public String getBottom() {
		return bottom;
	}

	/**
	 * @param bottom the bottom to set
	 */
	public void setBottom(String bottom) {
		this.bottom = bottom;
	}

	/**
	 * @return the creatureType
	 */
	public CreatureType getCreatureType() {
		return creatureType;
	}

	/**
	 * @param creatureType the creatureType to set
	 */
	public void setCreatureType(CreatureType creatureType) {
		this.creatureType = creatureType;
	}

	/**
	 * @return the factionId
	 */
	public int getFactionId() {
		return factionId;
	}

	/**
	 * @param factionId the factionId to set
	 */
	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}

}
