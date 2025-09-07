package org.schema.game.common.data.world.space;

import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.player.FixedSpaceEntityProvider;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.physics.Physical;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public abstract class FixedSpaceEntity extends SimpleTransformableSendableObject<FixedSpaceEntityProvider> {

	private String uniqueIdentifier = "SpaceEntityUniqueIdentifier";
	private final ObjectArrayList<FixedSpaceEntityProvider> listeners = new ObjectArrayList<FixedSpaceEntityProvider>();
	private FixedSpaceEntityProvider sendableSegmentProvider;
	
	@Override
	public void addListener(FixedSpaceEntityProvider s) {
		listeners.add(s);
	}

	@Override
	public List<FixedSpaceEntityProvider> getListeners() {
		return listeners;
	}
	@Override
	public FixedSpaceEntityProvider createNetworkListenEntity() {
		sendableSegmentProvider = new FixedSpaceEntityProvider(getState());
		sendableSegmentProvider.initialize();
		return sendableSegmentProvider;
	}
	public FixedSpaceEntity(StateInterface state) {
		super(state);
	}
	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity(){
		return this;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#isClientOwnObject()
	 */
	@Override
	public boolean isClientOwnObject() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		setMass(0);
	}

	@Override
	public String toNiceString() {
		return "SpaceEntity(" + getId() + ")";
	}

	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#getTransformedAABB(javax.vecmath.Vector3f, javax.vecmath.Vector3f, float, javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	@Override
	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin,
	                               Vector3f tmpMin, Vector3f tmpMax, Transform instead) {

	}

	@Override
	public void initPhysics() {

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getMass()
	 */
	@Override
	public void destroyPersistent() {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#newNetworkObject()
	 */
	@Override
	public void newNetworkObject() {
		
	}

	@Override
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	/**
	 * @param uniqueIdentifier the uniqueIdentifier to set
	 */
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.OTHER_SPACE;
	}
}
