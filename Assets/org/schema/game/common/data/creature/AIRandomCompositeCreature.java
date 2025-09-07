package org.schema.game.common.data.creature;

import java.util.List;
import java.util.Random;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.creature.CreaturePartNode.AttachmentType;
import org.schema.game.network.objects.TargetableDynamicAICreatureNetworkObject;
import org.schema.game.server.ai.AIControllerStateUnit;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.resource.CreatureStructure;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.ResourceLoadEntryMesh;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class AIRandomCompositeCreature extends AICompositeCreature {

	private static final int MAX_PARTS = 2;
	private float speed;
	private float scale;
	private float height;
	private float width;
	private Vector3i blockDim = new Vector3i(1, 1, 1);
	private CreaturePartNode main;
	private boolean meelee = true;

	public AIRandomCompositeCreature(StateInterface state, float speed,
	                                 float scale, float height, float width, Vector3i blockDim) {
		super(state);
	}

	public AIRandomCompositeCreature(StateInterface state) {
		super(state);
	}

	public static AIRandomCompositeCreature instantiate(StateInterface state, float speed,
	                                                    float scale, float height, float width, Vector3i blockDim, CreaturePartNode hierarchy) {
		AIRandomCompositeCreature r = new AIRandomCompositeCreature(state);
		r.speed = speed;
		r.scale = scale;
		r.height = height;
		r.width = width;
		r.blockDim = new Vector3i(blockDim);
		r.main = hierarchy;

		assert (r.main != null);

		return r;
	}

	private static String getRandomMesh(StateInterface state, Random random, PartType type) {
		List<String> creatureMeshes = state.getResourceMap().getType(type);
		if (creatureMeshes.isEmpty()) {
			throw new IllegalArgumentException("no resources for : " + type.name());
		}
		return creatureMeshes.get(random.nextInt(creatureMeshes.size()));
	}

	private static void attach(StateInterface state, Random random, int part, int maxParts, CreaturePartNode to) {
		String attMesh = getRandomMesh(state, random, PartType.values()[part]);
		CreaturePartNode pAtt;

		ResourceLoadEntryMesh r = state.getResourceMap().getMesh(attMesh);
		if (r.texture != null) {
			String texture = r.texture.getDiffuseMapNames().get(random.nextInt(r.texture.getDiffuseMapNames().size()));
			pAtt = new CreaturePartNode(to.type == PartType.BOTTOM ? PartType.MIDDLE : PartType.TOP, state, attMesh, texture);
		} else {
			pAtt = new CreaturePartNode(to.type == PartType.BOTTOM ? PartType.MIDDLE : PartType.TOP, state, attMesh, null);
		}

		to.attach(state, pAtt, AttachmentType.MAIN);
		if (part < maxParts) {
			attach(state, random, part + 1, maxParts, pAtt);
		}
	}

	public static CreaturePartNode getRandomHierarchy(StateInterface state, Random random, int partCount) {
		String mainMesh = getRandomMesh(state, random, PartType.BOTTOM);
		String texture;
		ResourceLoadEntryMesh r = state.getResourceMap().getMesh(mainMesh);
		if (r.texture != null) {
			assert (!r.texture.getDiffuseMapNames().isEmpty()) : mainMesh;
			texture = r.texture.getDiffuseMapNames().get(random.nextInt(r.texture.getDiffuseMapNames().size()));
		} else {
			texture = null;
		}

		CreaturePartNode pBot = new CreaturePartNode(PartType.MIDDLE, state, mainMesh, texture);
		attach(state, random, 1, partCount - 1, pBot);
		return pBot;
	}

	public static AIRandomCompositeCreature random(StateInterface state) {
		Random random = new Random();

		CreaturePartNode randomHierarchy = getRandomHierarchy(state, random, MAX_PARTS);

		ResourceLoadEntryMesh mainMesh = state.getResourceMap().getMesh(randomHierarchy.meshName);

		CreatureStructure creatureStructure = mainMesh.creature;

		float height = 0.2f;
		float width = 0.3f;

		float maxScale = Math.max(creatureStructure.maxScale.z, Math.max(creatureStructure.maxScale.x, creatureStructure.maxScale.y));

		float scale = (random.nextFloat() * (maxScale - 1)) + 1;

		return instantiate(state, 2 + random.nextFloat() * 4, scale, height, width, creatureStructure.dim, randomHierarchy);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICompositeCreature#getNetworkObject()
	 */
	@Override
	public TargetableDynamicAICreatureNetworkObject getNetworkObject() {
		return (TargetableDynamicAICreatureNetworkObject) super.getNetworkObject();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICompositeCreature#newNetworkObject()
	 */
	@Override
	public void newNetworkObject() {
		ntInit(new TargetableDynamicAICreatureNetworkObject(getState(), getOwnerState()));
	}

	private void fromTagStructureCompo(Tag tag) {
		Tag[] p = (Tag[]) tag.getValue();

		speed = (Float) p[0].getValue();
		scale = (Float) p[1].getValue();
		width = (Float) p[2].getValue();
		height = (Float) p[3].getValue();
		blockDim = (Vector3i) p[4].getValue();
		main = new CreaturePartNode(PartType.BOTTOM);
		main.fromTagStructure(p[5]);
	}

	private Tag toTagStructureCompo() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.FLOAT, null, speed),
				new Tag(Type.FLOAT, null, scale),
				new Tag(Type.FLOAT, null, width),
				new Tag(Type.FLOAT, null, height),
				new Tag(Type.VECTOR3i, null, blockDim),
				main.toTagStructure(),
				FinishTag.INST});
	}

	@Override
	public float getCharacterHeightOffset() {
				return 0;
	}

	@Override
	public float getCharacterHeight() {
		return height * (1f + (scale - 1f * 0.5f));
	}

	@Override
	public float getCharacterWidth() {
		return width * (1f + (scale - 1f * 0.5f));
	}

	@Override
	public Vector3i getBlockDim() {
		return blockDim;
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	@Override
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public boolean isMeleeAttacker() {
		return meelee;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICreature#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICreature#fromTagStructure(org.schema.schine.resource.tag.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		super.fromTagStructure(t[0]);
		fromTagStructureCompo(t[1]);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICreature#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{super.toTagStructure(), toTagStructureCompo(), FinishTag.INST});
	}

	@Override
	public void initialFillInventory() {
		
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICreature#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject o) {
		super.initFromNetworkObject(o);

		if (!isOnServer()) {
			this.speed = getNetworkObject().speed.getFloat();
			this.scale = getNetworkObject().scale.getFloat();
			this.height = getNetworkObject().height.getFloat();
			this.width = getNetworkObject().width.getFloat();

			this.blockDim = getNetworkObject().boxDim.getVector();
			this.main = getNetworkObject().creatureCode.get();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICreature#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject, int)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.creature.AICreature#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();

		getNetworkObject().speed.set(this.speed);
		getNetworkObject().scale.set(this.scale);
		getNetworkObject().height.set(this.height);
		getNetworkObject().width.set(this.width);
		getNetworkObject().boxDim.set(this.blockDim);
		getNetworkObject().creatureCode.set(this.main);

	}

	@Override
	public CreaturePartNode getCreatureNode() {
		return main;
	}

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public void handleControl(Timer timer,
			AIControllerStateUnit<AICreature<AICompositeCreaturePlayer>> unit) {
		
	}

	@Override
	public SendableType getSendableType() {
		return SendableTypes.AI_RANDOM_COMPOSITE_CREATURE;
	}

}
