package org.schema.game.common.data.mines.handler;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Mesh;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CannonMineHandler extends MineHandler{

	
	public int damage;
	public InterEffectSet attackEffect;
	public long shotFreqMilli = 500;
	public float projectileSpeed = 1;
	public boolean pointDefense;
	public final Vector4f color = new Vector4f(1,1,1,1);
	public int shootAtTargetCount = -1;
	private long lastShot;
	private final Vector3f targetTmp = new Vector3f();
	
	
	
	public CannonMineHandler(Mine mine) {
		super(mine);
	}

	@Override
	public void handleActive(Timer timer, List<SimpleGameObject> detected) {
		if(timer.currentTime - lastShot > shotFreqMilli) {
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
			if(mine.isOnServer()) {
				mine.setAmmoServer((short) (mine.getAmmo()-1));
			}else {
				mine.setAmmo((short) (mine.getAmmo()-1));
			}
		}
	}

	protected void shootOnClient(GameClientState state, Timer timer, List<SimpleGameObject> detected) {
		int shot = 0;
		for(SimpleGameObject target : detected) {
			shootAt(timer, target, mine.getWorldTransformOnClient(), target.getWorldTransformOnClient(), state.getParticleController());
			
			shot++;
			if(shootAtTargetCount > 0 && shot >= shootAtTargetCount) {
				break;
			}
		}
		
	}

	
	private final List<AbstractSceneNode> outputs = new ObjectArrayList<AbstractSceneNode>();
	private int shotNumber;
	private Transform fromTrans = new Transform();
	private Vector3f outputPos = new Vector3f();
	private void shootAt(Timer timer, SimpleGameObject target, Transform mineTrans, Transform targetTrans, ProjectileController c) {
		targetTmp.set(0,0,0);
		if(target instanceof SegmentController && ((SegmentController)target).hasActiveReactors() ) {
			List<MainReactorUnit> mainReactors = ((ManagedSegmentController<?>)target).getManagerContainer().getPowerInterface().getMainReactors();
			long sm = Long.MIN_VALUE;
			for(MainReactorUnit m : mainReactors) {
				if(m.getSignificator() > sm) {
					sm = m.getSignificator();
				}
			}
			
			ElementCollection.getPosFromIndex(sm, targetTmp);
			targetTmp.x -= Segment.HALF_DIM;
			targetTmp.y -= Segment.HALF_DIM;
			targetTmp.z -= Segment.HALF_DIM;
		}
		
		fromTrans.set(mineTrans);
		
		targetTrans.transform(targetTmp);
		
		targetTmp.sub(fromTrans.origin);
		
		
		if(targetTmp.lengthSquared() > 0) {
			targetTmp.normalize();
			
			targetTmp.scale(projectileSpeed);
			
			int penetrationDepth = 10;
			float projectileWidth = 5;
			float impactForce = 0.001f;
			assert(mine != null):target+"; "+c;
			assert(c != null):mine;
			assert(mineTrans != null):mine;

			if(!c.isOnServer()) {
				if(outputs.isEmpty()) {
					AbstractSceneNode mesh = Controller.getResLoader().getMesh("MineTypeA-Active").getChilds().get(0);
					
					for(AbstractSceneNode s : mesh.getChilds()) {
						if(!(s instanceof Mesh)) {
							outputs.add(s);
						}
					}
					
					Collections.sort(outputs, (o1, o2) -> o1.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getName().toLowerCase(Locale.ENGLISH)));
				}else {
					
					
					AbstractSceneNode output = outputs.get(shotNumber%outputs.size());
					
//					System.err.println("OUTPUTS: "+output+" -> "+output.getInitionPos());
					
					
					outputPos.set(output.getInitionPos());
					fromTrans.basis.transform(outputPos);
					fromTrans.origin.add(outputPos);
				}
			}
			
			c.addProjectile(mine, fromTrans.origin, targetTmp, damage, 
					((GameStateInterface)mine.getState()).getGameState().getWeaponRangeReference()/2f, 
					0,
					projectileWidth,
					penetrationDepth,
					10,
					PlayerUsableInterface.USABLE_ID_MINE_SHOOTER, color, 0);
			
			
			shotNumber++;
		}
	}

	protected void shootOnServer(GameServerState state, Timer timer, List<SimpleGameObject> detected) {
		
		for(SimpleGameObject target : detected) {
			ProjectileController particleController = mine.serverInfo.sector.getParticleController();
			if(mine.serverInfo.sector.isActive() && particleController != null) {
				shootAt(timer, target, mine.getWorldTransform(), target.getWorldTransform(), particleController);
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
	public boolean isPointDefense() {
		return pointDefense;
	}
}
