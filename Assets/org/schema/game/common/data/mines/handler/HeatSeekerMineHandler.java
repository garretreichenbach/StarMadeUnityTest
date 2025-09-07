package org.schema.game.common.data.mines.handler;

import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.missile.MissileController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.container.TransformTimed;

public class HeatSeekerMineHandler extends MineHandler{

	
	public int damage;
	public InterEffectSet attackEffect;
	public long shotFreqMilli = 10000;
	public float missileSpeed = 1;
	public float missileDistance = 1000;
	public short lightType = 0;
	public final Vector4f color = new Vector4f(1,1,1,1);
	public boolean dieOnShot;
	
	private long lastShot;
	private final Vector3f targetTmp = new Vector3f();
	
	
	public HeatSeekerMineHandler(Mine mine) {
		super(mine);
	}

	@Override
	public void handleActive(Timer timer, List<SimpleGameObject> detected) {
		if(timer.currentTime - lastShot > shotFreqMilli) {
			System.err.println("MIN "+mine.getState()+": "+mine.getAmmo());
			if(mine.getAmmo() > 0) {
				if(mine.isOnServer()) {
					GameServerState state = (GameServerState)mine.getState();
					shootOnServer(state, timer, detected);
				}else {
					GameClientState state = (GameClientState)mine.getState();
					shootOnClient(state, timer, detected);
				}
			}
			lastShot = timer.currentTime;
			
			if(!dieOnShot) {
				if(mine.isOnServer()) {
					//reduce ammo if not proximity one shot mine
					mine.setAmmoServer((short) (mine.getAmmo()-1));
				}else {
					mine.setAmmo((short) (mine.getAmmo()-1));
				}
			}
		}
	}

	protected void shootOnClient(GameClientState state, Timer timer, List<SimpleGameObject> detected) {
		
		
	}

	
	
	private void shootAt(Timer timer, SimpleGameObject target, TransformTimed mineTrans, TransformTimed targetTrans, MissileController c) {
		targetTmp.set(0,0,0);
		targetTrans.transform(targetTmp);
		targetTmp.sub(mineTrans.origin);
		if(targetTmp.lengthSquared() > 0) {
			targetTmp.normalize();
			c.addHeatMissile(mine, mine.getWorldTransform(), targetTmp, missileSpeed, damage, missileDistance, PlayerUsableInterface.USABLE_ID_MINE_SHOOTER, lightType);
			
		}
		
	}

	protected void shootOnServer(GameServerState state, Timer timer, List<SimpleGameObject> detected) {
		
		for(SimpleGameObject target : detected) {
			shootAt(timer, target, mine.getWorldTransform(), target.getWorldTransform(), state.getController().getMissileController());
			if(dieOnShot) {
				mine.destroyOnServer();
			}
		}
	}

	@Override
	public void onBecomingActive(List<SimpleGameObject> detected) {
				
	}

	@Override
	public void handleInactive(Timer timer) {
				
	}

	@Override
	public void onBecomingInactive() {
				
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return attackEffect;
	}

}
