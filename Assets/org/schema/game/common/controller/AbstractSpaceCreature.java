package org.schema.game.common.controller;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.Hittable;
import org.schema.game.common.controller.damage.beam.DamageBeamHittable;
import org.schema.game.common.data.physics.shape.GameShape;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.SpaceCreatureProvider;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.util.Collisionable;
import org.schema.game.network.objects.NetworkAbstractSpaceCreature;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.io.File;
import java.util.List;

public abstract class AbstractSpaceCreature extends SimpleTransformableSendableObject<SpaceCreatureProvider> implements DamageBeamHittable, Collisionable, Hittable {

	private String realName = "noname";
	private String uid;
	private boolean newlyCreated = true;
	private final ObjectArrayList<SpaceCreatureProvider> listeners = new ObjectArrayList<SpaceCreatureProvider>();
	private SpaceCreatureProvider sendableSegmentProvider;
	
	@Override
	public void addListener(SpaceCreatureProvider s) {
		listeners.add(s);
	}
	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return this;
	}
	@Override
	public SpaceCreatureProvider createNetworkListenEntity() {
		sendableSegmentProvider = new SpaceCreatureProvider(getState());
		sendableSegmentProvider.initialize();
		return sendableSegmentProvider;
	}

	@Override
	public List<SpaceCreatureProvider> getListeners() {
		return listeners;
	}

	public AbstractSpaceCreature(StateInterface state) {
		super(state);
	}

	@Override
	public final void sendHitConfirm(byte damageType) {
		//creature is not human controllable
	}

	@Override
	public final boolean isSegmentController() {
		return false;
	}

	@Override
	public String getName() {
		return realName;
	}

	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	@Override
	public void destroyPersistent() {
		assert (isOnServer());
		File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH + getUniqueIdentifier() + ".ent");
		f.delete();
	}

//	@Override
//	public ParticleHitCallback handleHit(ParticleHitCallback callback, Damager particleOwner,
//	                                     float damage, float damageBeforeShield, Vector3f startPos,
//	                                     Vector3f endPos, boolean shieldAbsorbed, long weaponId) {
//		return null;
//	}



	@Override
	public boolean isVulnerable() {
		return true;
	}

//	@Override
//	public int handleBeamDamage(
//			BeamState beam,
//			int hits,
//			BeamHandlerContainer<? extends SimpleTransformableSendableObject> owner,
//			Vector3f from, Vector3f to, CubeRayCastResult cubeResult,
//			boolean ignoreShields, Timer timer) {
//		return 0;
//	}

	@Override
	public EntityType getType() {
		return EntityType.SPACE_CREATURE;
	}

	@Override
	public boolean isClientOwnObject() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {

		newlyCreated = false;

		assert (tag.getName().equals("sc"));
		//newest version
		Tag[] subTags = (Tag[]) tag.getValue();

		super.fromTagStructure(subTags[0]);
		uid = (String) subTags[1].getValue();
		realName = ((String) subTags[2].getValue());

		setChangedForDb(false);
	}

	@Override
	public abstract NetworkAbstractSpaceCreature getNetworkObject();

	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		NetworkAbstractSpaceCreature s = (NetworkAbstractSpaceCreature) from;
		realName = (s.realName.get());
		uid = s.uniqueIdentifier.get();
	}

	@Override
	public Tag toTagStructure() {
		Tag idTag = new Tag(Type.STRING, null, this.getUniqueIdentifier());
		Tag realNameTag = new Tag(Type.STRING, null, realName);

		return new Tag(Type.STRUCT, null, new Tag[]{
				super.toTagStructure(),
				idTag,
				realNameTag,
				FinishTag.INST});
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);

		NetworkAbstractSpaceCreature s = (NetworkAbstractSpaceCreature) o;
		realName = (s.realName.get());

	}

	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();

		assert (getUniqueIdentifier() != null);
		getNetworkObject().uniqueIdentifier.set(getUniqueIdentifier());

		updateToNetworkObject();

	}

	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();

		if (isOnServer()) {
			getNetworkObject().realName.set(realName);
		}

	}

	@Override
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Override
	public boolean needsManifoldCollision() {
		return false;
	}

	@Override
	public String getUniqueIdentifier() {
		assert (uid != null);
		return uid;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	public void setUniqueIdentifier(String uid) {
		this.uid = uid;
	}

	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {
	}

	@Override
	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin,
	                               Vector3f tmpMin, Vector3f tmpMax, Transform instead) {
	}

	@Override
	public void initPhysics() {

		CollisionShape creatureShape = createCreatureCollisionShape();
		assert (creatureShape instanceof GameShape);

		RigidBody creatureBody = getPhysics().getBodyFromShape(creatureShape, getMass(), getInitialTransform());

		getPhysicsDataContainer().setObject(creatureBody);
		getPhysicsDataContainer().setShape(creatureShape);

		getPhysicsDataContainer().updatePhysical(0);

		setFlagPhysicsInit(true);
	}

	protected abstract CollisionShape createCreatureCollisionShape();

	public boolean isNewlyCreated() {
		return newlyCreated;
	}
	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.SPACE_CREATURE;
	}
	public boolean canBeDamagedBy(Damager from, DamageDealerType beam) {
		return true;
	}
}
