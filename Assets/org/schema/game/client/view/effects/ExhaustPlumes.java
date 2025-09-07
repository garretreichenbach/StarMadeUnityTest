package org.schema.game.client.view.effects;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.client.view.effects.segmentcontrollereffects.JumpStart;
import org.schema.game.client.view.effects.segmentcontrollereffects.RunningEffect;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.thrust.ThrusterUnit;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class ExhaustPlumes implements Drawable, Shaderable {

	static Shader shader;
	static Vector4f color0tStat = new Vector4f();
	static Vector4f color1tStat = new Vector4f();
//	private static Matrix3f rot = new Matrix3f();
	Vector4f color0 = new Vector4f(1f, 1f, 1f, 1.0f);
	Vector4f color1 = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);
	Vector4f color0t = new Vector4f();
	Vector4f color1t = new Vector4f();
	Vector3f helper = new Vector3f();
	Transform t = new Transform();
	Vector3f localTranslation = new Vector3f(0, 0, -Element.BLOCK_SIZE / 2f);
	//	private void resort(){
	//		plums.clear();
	//		synchronized (unsortedplumes) {
	//			for(Plum p : unsortedplumes){
	//				plums.add(p);
	//			}
	//		}
	//	}
	Vector3i test = new Vector3i();
	private Ship ship;
	//	private ObjectAVLTreeSet<Plum> plums = new ObjectAVLTreeSet<Plum>();
	private ObjectAVLTreeSet<ThrustPlume> unsortedplumes = new ObjectAVLTreeSet<ThrustPlume>();
	//	Matrix4f modelViewProjection = new Matrix4f();
	private float ticks = 0;
	private boolean initialized;
	private boolean firstDraw = true;
	private long scheduleUpdate = -1;
	private ObjectPool<ThrustPlume> plumPool = ObjectPool.get(ThrustPlume.class);
	private SegmentPiece pointUnsaveTmp = new SegmentPiece();

	public ExhaustPlumes(Ship ship) {
		this.ship = ship;
		//		getShip().getManagerContainer().getThrusterElementManager().addObserver(this);
//		rot.rotY(FastMath.PI);
	}

	@Override
	public void cleanUp() {
		if(ship != null) {
			//			getShip().getManagerContainer().getThrusterElementManager().deleteObserver(this);
		}

	}

	public boolean raw = true;

	@Override
	public void draw() {
		if(firstDraw) {
			onInit();
		}
		if(ship.isCloakedFor(((GameClientState) ship.getState()).getCurrentPlayerObject()) || ship.percentageDrawn < 1f || ship.isInAdminInvisibility()) {
			return;
		}
		if(!MainGameGraphics.drawBloomedEffects()) {
			return;
		}
		if(!initialized) {

			if(ship.getWorldTransform() != null) {
				//				update(null, SegNotifyType.SHIP_ELEMENT_CHANGED, null);
				scheduleUpdate();
				initialized = true;
			}
			return;
		}

		boolean needsUpdate = true;

		int i = 0;

		ObjectBidirectionalIterator<ThrustPlume> descendingIterator = unsortedplumes.iterator();
		while(descendingIterator.hasNext()) {
			ThrustPlume p = descendingIterator.next();

			p.getWorldTransform(t, localTranslation);
//			t.basis.mul(rot);

			if(!GlUtil.isPointInCamRange(t.origin, Controller.vis.getVisLen()) || !GlUtil.isInViewFrustum(t, PlumeAndMuzzleDrawer.plumeMesh, 0)) {
				//				System.err.println("NOT DRAW PLUM_ "+i);
				continue;
			}

			if(needsUpdate) {
				if(!raw) {
					updateShader();
				} else {
					updateBloomShader(p);
				}
				needsUpdate = false;
			}

			GlUtil.glPushMatrix();
			GlUtil.glMultMatrix(t);
			((Mesh) PlumeAndMuzzleDrawer.plumeMesh.getChilds().get(0)).drawVBOAttributed();
			GlUtil.glPopMatrix();
			i++;
		}

		//		GlUtil.glEnable(GL11.GL_CULL_FACE);
	}

	private void updateBloomShader(ThrustPlume a) {

		float acc = Math.min(0.99f, ship.lastSpeed / ship.getMaxServerSpeed());

		Vector4f coreColor = ship.getManagerContainer().getColorCore();
		GlUtil.updateShaderVector4f(ShaderLibrary.silhouetteAlpha, "silhouetteColor", acc * coreColor.x, acc * coreColor.y, acc * coreColor.z, 1.0f);
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		shader = ShaderLibrary.exaustShader;
	}

	public Ship getShip() {
		return ship;
	}

	public void setShip(Ship ship) {
		this.ship = ship;
	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {

	}

	public void onPlumesChanged() {

		synchronized(unsortedplumes) {
			for(ThrustPlume p : unsortedplumes) {
				p.reset();
				plumPool.release(p);
			}
			unsortedplumes.clear();

			for(ThrusterUnit u : ship.getManagerContainer().getThrusterElementManager().getCollection().getElementCollections()) {
				int i = 0;
				for(long pos : u.getLastElements().values()) {
					ElementCollection.getPosFromIndex(pos, test);
					byte orientation = ship.getSegmentBuffer().getPointUnsave(pos).getOrientation();
//					test.z -= 1;
					//Since thrust plumes can be any direction now, we need to account for non-backwards facing thrusters
					//So if its facing backwards, z gets decremented by one, but if its forward, we need to increment z, and the same for left, right, etc.
					switch(orientation) {
						case Element.FRONT: 
							test.z += 1;
							break;
						case Element.BACK: 
							test.z -= 1;
							break;
						case Element.LEFT: 
							test.x -= 1;
							break;
						case Element.RIGHT: 
							test.x += 1;
							break;
						case Element.TOP:
							test.y += 1;
							break;
						case Element.BOTTOM:
							test.y -= 1;
							break;
					}
					
					SegmentPiece pointUnsave;
					pointUnsave = ship.getSegmentBuffer().getPointUnsave(test, pointUnsaveTmp);
					if(pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
						ThrustPlume thrustPlume = plumPool.get();
						thrustPlume.set(ship, ElementCollection.getPosFromIndex(pos, new Vector3i()), orientation);
						unsortedplumes.add(thrustPlume);
						i++;
					}
				}
			}

			//			Set<ElementPosition> allControlledElements = ship
			//					.getControlElementMap().getAllControlledElements(
			//							ElementKeyMap.THRUSTER_ID);
			//			System.err.println("[Plum UPDATE] thruster get took "+(System.currentTimeMillis() - t));
			//			//			System.err.println("DOING UPDATE FOR "+allControlledElements.size());
			//			for (ElementPosition c : allControlledElements) {
			//				Vector3i pos = new Vector3i(c.x, c.y, c.z);
			//
			//				test.set(pos);
			//
			//				try {
			//					SegmentPiece pointUnsave = ship.getSegmentBuffer().getPointUnsave(test, true);
			//					//				System.err.println("CHECKING POS "+test+": "+pointUnsave);
			//					if(pointUnsave != null && pointUnsave.getType() == ElementKeyMap.THRUSTER_ID){
			//						test.z -= 1;
			//
			//						pointUnsave = ship.getSegmentBuffer().getPointUnsave(test, true);
			//						if(pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE){
			//
			//							Plum plum = plumPool.get();
			//							plum.set(ship, pos);
			//							unsortedplumes.add(plum);
			//						}
			//
			//					}
			//				} catch (IOException e) {
			//					e.printStackTrace();
			//				} catch (InterruptedException e) {
			//					e.printStackTrace();
			//				} catch(CannotImmediateRequestOnClientException e){
			//					scheduleUpdate = System.currentTimeMillis();
			//				}
			//
			//			}
			//			System.err.println("[Plum UPDATE] total took "+(System.currentTimeMillis() - t));
		}
	}

	public void scheduleUpdate() {
		scheduleUpdate = System.currentTimeMillis();
	}

	//	@Override
	//	public void update(DrawerObservable arg0, Object o, Object message) {
	//
	//		if (o != null && o instanceof SegNotifyType) {
	//			//			System.err.println("++++++++++++++++++++++++++++++++++++++++++NOTIFIED PLUM UPDATED");
	//			if (o == SegNotifyType.SHIP_ELEMENT_CHANGED) {
	//				scheduleUpdate = System.currentTimeMillis();
	//			}
	//		}
	//
	//	}
	public void update(Timer timer) {

		float newVelo = ship.getVelocity().length();

		float rate = 0.3f;
		RunningEffect effect = ((GameClientState) ship.getState()).getWorldDrawer().getSegmentControllerEffectDrawer()
				.getEffect(ship);

		if(effect != null && effect instanceof JumpStart) {
			newVelo = ship.getCurrentMaxVelocity();
			rate = 0.1f;
		}

		if(ship.getSegmentController().isDocked()) {
			if(ship.getRootShip().getManagerContainer().thrustConfiguration.thrustSharing && ship.getRootShip() != ship) {
				ship.lastSpeed = ship.getRootShip().lastSpeed;
			} else {
				ship.lastSpeed = Math.max(newVelo, ship.lastSpeed - timer.getDelta() * rate * ship.getMaxServerSpeed());
			}
		} else if(ship.lastSpeed < newVelo) {
			ship.lastSpeed = Math.min(newVelo, ship.lastSpeed + timer.getDelta() * rate * ship.getMaxServerSpeed());
		} else {
			ship.lastSpeed = Math.max(newVelo, ship.lastSpeed - timer.getDelta() * rate * ship.getMaxServerSpeed());
		}

		if(scheduleUpdate > 0 && System.currentTimeMillis() - scheduleUpdate > 100) {
			onPlumesChanged();
			scheduleUpdate = -1;
		}
		ticks += timer.getDelta() / 100 * ((Math.random() + 0.0001f) / 0.1f);
		if(ticks > 1) {
			ticks = 0;
		}
	}

	public void updateShader() {
		float acc = Math.min(0.99f, ship.lastSpeed / ship.getMaxServerSpeed());

		color0t.set(color0);
		color1t.set(color1);
		color0t.scale(acc);
		color1t.scale(acc);

		color1t.x = 0.5f - acc / 2;
		color1t.z = acc;

		if(!color0tStat.equals(color0t)) {
			GlUtil.updateShaderVector4f(shader, "thrustColor0", color0t);
			color0tStat.set(color0t);
		}

		if(!color1tStat.equals(color1t)) {
			GlUtil.updateShaderVector4f(shader, "thrustColor1", color1t);
			color1tStat.set(color1t);
		}

		GlUtil.updateShaderFloat(shader, "ticks", ticks);
		if(ship.isDocked()) {
			System.err.println("[PLUME] update shader");
		}
	}
}
