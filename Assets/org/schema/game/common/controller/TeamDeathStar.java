package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.generator.DeathStarCreatorThread;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.NetworkTeamDeathStar;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class TeamDeathStar extends EditableSendableSegmentController {

	public TeamDeathStar(StateInterface state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#allowedType(short)
	 */
	@Override
	public boolean allowedType(short type) {
		boolean a = type != ElementKeyMap.AI_ELEMENT && !ElementKeyMap.getInfo(type).getType().hasParent("spacestation");

		if (!a && !isOnServer()) {
			((GameClientController) getState().getController()).popupAlertTextMessage(
					Lng.str("Cannot place\n%s\non a death star!",  ElementKeyMap.getInfo(type).getName()), 0);
		}
		return super.allowedType(type) && a;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.TEAM_DEATH_STAR;
	}
	@Override
	protected short getCoreType() {
		return ElementKeyMap.DEATHSTAR_CORE_ID;
	}

	@Override
	public NetworkTeamDeathStar getNetworkObject() {
		return (NetworkTeamDeathStar) super.getNetworkObject();
	}
	@Override
	public void sendHitConfirm(byte damageType) {
	}
	@Override
	protected String getSegmentControllerTypeString() {
		return "DeathStar";
	}

	

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkTeamDeathStar(getState(), this));
	}

	@Override
	protected void onCoreDestroyed(Damager from) {
		System.err.println("DEATHSTAR HAS BEEN DESTROYED BY !!!! " + from);
		if (isOnServer()) {
			int loser = this.getFactionId();
			int winner = from instanceof SimpleTransformableSendableObject ? ((SimpleTransformableSendableObject) from).getFactionId() : 0;
			((GameServerState) getState()).getController().endRound(winner, loser, from);
		}

	}
	//	public void handleTeamChanges(NetworkTeamDeathStar p){
	//		for(RemoteInteger s : p.requestTeamChange.getReceiveBuffer()){
	//			for(Team t : ((Teamable)getState()).getTeams()){
	//				if(t.getTeamId() == s.get()){
	//					this.team = t;
	//				}
	//			}
	//		}
	//	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {
			setCreatorThread(new DeathStarCreatorThread(this));
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.DEATH_STAR;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		//		deathStarAIEntity = new DeathStarAIEntity(this);
		setMass(0);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		assert (tag.getName().equals("DeathStar"));
		Tag[] subTags = (Tag[]) tag.getValue();

		int teamId = (Integer) subTags[0].getValue();

		//		for(Team t : ((Teamable)getState()).getTeams()){
		//			if(t.getTeamId() == teamId){
		//				this.team = t;
		//			}
		//		}
		//		assert(team != null):"team id invalid: "+teamId;

		super.fromTagStructure(subTags[1]);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {

		Tag teamId = new Tag(Type.INT, "team", this.getFactionId());

		return new Tag(Type.STRUCT, "DeathStar",
				new Tag[]{teamId, super.toTagStructure(), FinishTag.INST});

	}

	@Override
	public boolean isSalvagableFor(Salvager harvester, String[] cannotHitReason, Vector3i position) {
		if (Ship.core.equals(position)) {
			cannotHitReason[0] = "Can't salvage core!";
			return false;
		}
		if (harvester instanceof Ship) {
			//			boolean team = ((Ship) harvester).getTeam().equals(this.team);
			//			if(!team){
			//				cannotHitReason[0] = "Can only salvage your\n own Team's deathstar";
			//			}
			//			return  team;
		}
		return false;
	}

	@Override
	public String toNiceString() {
		return "Death Star";
	}
	@Override
	public boolean isStatic() {
		return true;
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
