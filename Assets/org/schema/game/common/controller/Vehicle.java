package org.schema.game.common.controller;

import com.bulletphysics.dynamics.RigidBody;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.generator.EmptyCreatorThread;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.network.objects.NetworkVehicle;
import org.schema.game.server.ai.VehicleAIEntity;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;

public class Vehicle extends EditableSendableSegmentController implements SegmentControllerAIInterface {

	public Vehicle(StateInterface state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		assert (tag.getName().equals(this.getClass().getSimpleName()));
		Tag[] subTags = (Tag[]) tag.getValue();

		super.fromTagStructure(subTags[0]);
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.VEHICLE;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsAdd()
	 */
	@Override
	public void onPhysicsAdd() {
		super.onPhysicsAdd();

		RigidBody b = (RigidBody) getPhysicsDataContainer().getObject();

		b.setGravity(new Vector3f(0, -1.89f, 0));
	}
	@Override
	public void sendHitConfirm(byte damageType) {
	}
	@Override
	public void activateAI(boolean active, boolean send) {
		if(getElementClassCountMap().get(ElementKeyMap.AI_ELEMENT) > 0){
			((AIConfiguationElements<Boolean>)getAiConfiguration().get(Types.ACTIVE)).setCurrentState(active, send);
			getAiConfiguration().applyServerSettings();
		}
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {

		return new Tag(Type.STRUCT, this.getClass().getSimpleName(),
				new Tag[]{super.toTagStructure(), FinishTag.INST});

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.VEHICLE;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		//		deathStarAIEntity = new DeathStarAIEntity(this);
		setMass(0.01f);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	@Override
	public AIGameConfiguration<VehicleAIEntity, Vehicle> getAiConfiguration() {
				return null;
	}

	@Override
	protected short getCoreType() {
		return ElementKeyMap.DEATHSTAR_CORE_ID;
	}

	@Override
	public NetworkVehicle getNetworkObject() {
		return (NetworkVehicle) super.getNetworkObject();
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Vehicle";
	}

	

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkVehicle(getState(), this));
	}

	@Override
	protected void onCoreDestroyed(Damager from) {

	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {
			setCreatorThread(new EmptyCreatorThread(this));
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
	}

	@Override
	public boolean isSalvagableFor(Salvager harvester,
	                               String[] cannotHitReason, Vector3i position) {
		return true;
	}

	@Override
	public String toNiceString() {
		return "Vehicle";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();

	}
	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
				return null;
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public DamageBeamHitHandler getDamageBeamHitHandler() {
				return null;
	}
}
