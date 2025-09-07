package org.schema.game.client.view.effects;

import java.util.ArrayList;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.BidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public class MuzzleFlash implements Drawable, DrawerObserver {

	public static final float timeDrawn = 100;
	private static final ArrayList<Activation> activationPool = new ArrayList<Activation>();
	private static final float POWER_SCALE = 5000;
	private final ObjectAVLTreeSet<Activation> activations = new ObjectAVLTreeSet<Activation>();

	private final Vector4f color0 = new Vector4f(1f, 0.4f, 0.2f, 0.9f);

	private final Vector4f color1 = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
	private final Vector4f color0t = new Vector4f();
	private final Vector4f color1t = new Vector4f();
	private final Transform t = new Transform();
	private final Vector3f localTranslation = new Vector3f(0, 0, Element.BLOCK_SIZE / 2);
	private final ArrayList<Activation> toAdd = new ArrayList<Activation>();
	private Ship ship;
	private float percentage = 0;
	private boolean firstDraw = true;
	public boolean raw;
	private long cur;

	public MuzzleFlash(Ship ship) {
		this.ship = ship;
		this.ship.getManagerContainer().getWeapon().getElementManager().addObserver(this);
	}

	//	public Activation getActivation(Ship ship, ElementCollection o){
	//		synchronized(activationPool){
	//			if(!activationPool.isEmpty()){
	//				Activation act = activationPool.remove(0);
	//				act.set(ship, o);
	//				return act;
	//			}else{
	//				Activation act = new Activation(ship, o);
	//				return  act;
	//			}
	//		}
	//	}
	//	public void releaseActivation(Activation a){
	//		synchronized(activationPool){
	//			a.reset();
	//			activationPool.add(a);
	//		}
	//	}
	public void activate(ElementCollection<?,?,?> o, float power, Vector4f color) {
		Activation a = new Activation(ship, o, power, color);//getActivation(ship, o);

		if (!toAdd.contains(a)) {
			toAdd.add(a);
		} else {
			//			releaseActivation(a);
		}
	}

	@Override
	public void cleanUp() {
		if (ship != null) {
			ship.getManagerContainer().getWeapon().getElementManager().deleteObserver(this);
		}
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		//		if(!initialized ){
		//
		//			if(ship.getWorldTransform() != null){
		//				update(null, SegNotifyType.SHIP_ELEMENT_CHANGED);
		//				initialized = true;
		//			}
		//			return;
		//		}
		if (!activations.isEmpty()) {

			BidirectionalIterator<Activation> descendingIterator = activations.iterator(activations.last());

			while (descendingIterator.hasPrevious()) {
				Activation a = descendingIterator.previous();
				if (!a.isActive()) {
					continue;
				}
				if(!raw){
					updateShader(a);
				}else{
					updateBloomShader(a);
				}

				a.getWorldTransform(t, localTranslation);

				if (!GlUtil.isPointInCamRange(t.origin, Controller.vis.getVisLen()) || !GlUtil.isInViewFrustum(t, PlumeAndMuzzleDrawer.plumeMesh, 0)) {
					continue;
				}

				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(t);
				((Mesh) PlumeAndMuzzleDrawer.plumeMesh.getChilds().get(0)).drawVBOAttributed();

				GlUtil.glPopMatrix();

			}
		}

		//		GlUtil.glEnable(GL11.GL_CULL_FACE);
	}

	private void updateBloomShader(Activation a) {
		float time = (cur - a.timeStarted);

		float maxMuzzle = 0.4f;
		
		float scale = Math.max(0.0005f, Math.min(1.0f, a.power / POWER_SCALE )* maxMuzzle);
		
		Vector4f color = a.color;
		
		percentage = Math.min(scale, 0.0f + (time / timeDrawn));
		
		
		GlUtil.updateShaderVector4f(ShaderLibrary.silhouetteAlpha, "silhouetteColor", percentage*color.x, percentage*color.y, percentage*color.z, 1.0f);
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		firstDraw = false;
	}

	public Ship getShip() {
		return ship;
	}

	public void setShip(Ship ship) {
		this.ship = ship;
	}

	@Override
	public void update(DrawerObservable arg0, Object o, Object message) {
		if (o != null && o instanceof FiringUnit && "s".equals(message)) {
			
			activate((ElementCollection) o, ((FiringUnit)o).getFiringPower(), ((FiringUnit)o).getColor());
		}

	}

	public void update(Timer timer) {
		this.cur = timer.currentTime;
		while (!toAdd.isEmpty()) {
			Activation a = toAdd.remove(0);
			if (activations.contains(a)) {
				for (Activation existing : activations) {
					if (a.equals(existing)) {
						existing.start();
						break;
					}
				}
				//				releaseActivation(a);
			} else {
				a.start();
				activations.add(a);
			}
		}
		ObjectBidirectionalIterator<Activation> iterator = activations.iterator();
		while (iterator.hasNext()) {
			Activation s = iterator.next();
			if (!s.isActive()) {
				//				releaseActivation(s);
				iterator.remove();
			} else {
				s.update(timer);
			}
		}

		//		for(Activation s : toDel){
		//			releaseActivation(s);
		//		}
	}

	//	Matrix4f modelViewProjection = new Matrix4f();
	//	FloatBuffer mvpBuffer = MemoryUtil.memAllocFloat(16);
	public void updateShader(Activation a) {
		float time = (cur - a.timeStarted);

		percentage = Math.min(1, 0.7f + (time / timeDrawn) * 0.3f);

		color0t.set(color0);
		color1t.set(color1);
		color0t.scale(percentage);
		color1t.scale(percentage);

		color1t.x = 0.5f - percentage / 2;
		color1t.z = percentage;

		GlUtil.updateShaderVector4f(ShaderLibrary.exaustShader, "thrustColor0", color0t);
		GlUtil.updateShaderVector4f(ShaderLibrary.exaustShader, "thrustColor1", color1t);

		if (!ExhaustPlumes.color0tStat.equals(color0t)) {
			GlUtil.updateShaderVector4f(ShaderLibrary.exaustShader, "thrustColor0", color0t);
			ExhaustPlumes.color0tStat.set(color0t);
		}

		if (!ExhaustPlumes.color1tStat.equals(color1t)) {
			GlUtil.updateShaderVector4f(ShaderLibrary.exaustShader, "thrustColor1", color1t);
			ExhaustPlumes.color1tStat.set(color1t);
		}

		GlUtil.updateShaderFloat(ShaderLibrary.exaustShader, "ticks", a.ticks);

	}

}
