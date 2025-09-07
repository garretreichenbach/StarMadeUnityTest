package org.schema.game.client.view.beam;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

//import org.schema.graphicsengine.util.timer.LinearTimerUtil;

public class BeamDrawer implements Drawable {

	static final Vector3f tmpPosC = new Vector3f();
	static final Vector3f tmpPosCReg = new Vector3f();
	static final Vector3f tmpPosD = new Vector3f();
	static final Vector3f tmpPosF = new Vector3f();
	static final Vector3f dA = new Vector3f();
	static final Vector3f dB = new Vector3f();
	private final static Vector4f color0 = new Vector4f(1f, 1f, 1f, 1.0f);
	private final static Vector4f color0t = new Vector4f();
	private final static Vector4f color1t = new Vector4f();
	private final static Vector3f right = new Vector3f(0, 0, 1);
	private final static Vector3f beamOrigUp = new Vector3f(0, 0, 1);
	private static final int centerShift = SegmentData.SEG_HALF;
	public static final Vector3f c = new Vector3f(-centerShift, -centerShift, -centerShift);
	private final static Vector3f a = new Vector3f();
	private final static Vector3f b = new Vector3f();
	private final static Vector3f crossAB = new Vector3f();
	private static final float TEX_COORD_MULT = 17;
	private static Vector3f start = new Vector3f();
	private static Vector3f end = new Vector3f();
	private static Vector3f endReg = new Vector3f();
	private static Vector3f dir = new Vector3f();
	private static Vector3f up = new Vector3f();
	private static float lastDist;
	private static float size0t;
	private static float lastLenDiff;
	private static long timert;
	private static boolean lastHandheld;
	private final LinearTimerUtil linTimer = new LinearTimerUtil();
	private final Transform tmp = new Transform();
	private final Vector3i tmpPos = new Vector3i();
	private BeamDrawerManager manager;
	private List<? extends BeamHandlerContainer<?>> beamHandlers;
	private Vector4f color1;
	private ObjectOpenHashSet<AbstractBeamHandler<?>> toDraw = new ObjectOpenHashSet<AbstractBeamHandler<?>>();
	private boolean drawNeeded;
	private Vector3f p = new Vector3f();
	private Transform tmpTrans = new Transform();
	private int exceptionCount;

	public BeamDrawer(BeamDrawerManager thisMan, List<? extends BeamHandlerContainer<?>> managers) {
		set(managers, thisMan);

	}
	public static void prepareDraw() {
	}
	private static Matrix3f rotTmp = new Matrix3f();
	public static boolean resetShader;
	public static void drawConnection(BeamState state, Transform tmp, long time, Vector3f camPos) {
		
		if (state.hitPoint != null) {
			end.set(state.hitPoint);
			endReg.set(state.to);
		} else {
//			System.err.println("NON HP");
			end.set(state.to);
			endReg.set(state.to);
		}
		
		
		
		
		tmpPosC.sub(end, state.from);
		tmpPosCReg.sub(endReg, state.from);

		state.from.set(state.relativePos);
		state.getHandler().transform(state);
		
		start.set(state.from);

		
		
		end.add(start, tmpPosC);
		endReg.add(start, tmpPosCReg);

		float yAxisAngle;
		if(!state.drawVarsCamPos.epsilonEquals(camPos, 0.01f) || !state.drawVarsStart.epsilonEquals(start, 0.01f) || !state.drawVarsEnd.epsilonEquals(end, 0.01f)){
		
			state.drawVarsCamPos.set(camPos);
			state.drawVarsStart.set(start);
			state.drawVarsEnd.set(end);
		
			dA.sub(end, start);
			dB.sub(endReg, start);
	
			float lenDiff = FastMath.carmackLength(dA) / FastMath.carmackLength(dB);
	
			if (!GlUtil.isLineInView(start, end, Controller.vis.getVisLen())) {
				return;
			}
	
			dir.sub(start, end);
			float dist = FastMath.carmackLength(dir);
			right.set(0, 0, 1);
			tmp.origin.set(start);
	
			up.cross(dir, right);
			if (up.lengthSquared() == 0) {
				up.set(0, 1, 0);
			}
			up.normalize();
			GlUtil.setForwardVector(dir, tmp);
			GlUtil.setUpVector(up, tmp);
			right.cross(dir, up);
			if (right.lengthSquared() == 0) {
				right.set(0, 0, 1);
			}
			FastMath.normalizeCarmack(right);
			GlUtil.setRightVector(right, tmp);
			
			
			state.drawVarsAxisAngle = getAngleAroundZAxis(start, end, camPos, tmp.basis);
			state.drawVarsDist = dist;
			state.drawVarsLenDiff = lenDiff;
			state.drawVarsDrawTransform.set(tmp);
			
			rotTmp.setIdentity();
			rotTmp.rotZ(state.drawVarsAxisAngle + FastMath.HALF_PI);
			state.drawVarsDrawTransform.basis.mul(rotTmp);
		}else{
		}
		updateShader(state.drawVarsDist, state.drawVarsLenDiff, state.color, state.size, time, state.handheld);
		
		GlUtil.glPushMatrix();

		//transform to face END from START
		GlUtil.glMultMatrix(state.drawVarsDrawTransform);

		//add FastMath.HALF_PI so cam points at the 'plane' of the ray
//		GlUtil.rotateModelview(FastMath.RAD_TO_DEG * (state.drawVarsAxisAngle + FastMath.HALF_PI), 0, 0, 1);


		BeamDrawerManager.mesh.renderVBO();

		GlUtil.glPopMatrix();

	}

	private static float getAngleAroundZAxis(Vector3f start, Vector3f end, Vector3f P, Matrix3f startEndRotation) {


		/*
		 * determine axis by
		 * computing normalize(cross( CAM-START, END-START ))
		 */
		a.sub(P, start);
		b.sub(end, start);

		crossAB.cross(a, b);
		crossAB.normalize();

		/*
		 * get Angle between axis
		 * and a rotated (0,1,0)
		 */

		beamOrigUp.set(0, 1, 0);

		startEndRotation.transform(beamOrigUp);

		float dot = beamOrigUp.dot(crossAB);

		float camAngleYAxis = (float) FastMath.acosFast(dot);

		/*
		 * an orthogonal reference point is needed to determine
		 * counter-clockwise rotation and therefore a full angle range between 0
		 * and 2PI.
		 * In this case the orthogonal referencepoint
		 * (1,0,0) is taken and rotated.
		 *
		 * Then the angle between a rotated (1,0,0) and
		 * axis shows by checking if its smaller then PI/2,
		 * which side of the plane the first rotation was.
		 * extending the range of the angle by PI
		 *
		 */

		beamOrigUp.set(1, 0, 0);

		startEndRotation.transform(beamOrigUp);

		float dot2 = beamOrigUp.dot(crossAB);

		float camAngleXAxis = (float) FastMath.acosFast(dot2);

		if (FastMath.HALF_PI > camAngleXAxis) {
			camAngleYAxis = FastMath.TWO_PI - camAngleYAxis;
		}

		return camAngleYAxis;
	}

	public static void updateShader(float targetDistance, float lenDiff, Vector4f color, float size, long time, boolean handheld) {

		//		float acc = 1;
		if (ShaderLibrary.simpleBeamShader.recompiled || resetShader) {
			color0t.set(0, 0, 0, 0);
			color1t.set(0, 0, 0, 0);
			timert = System.currentTimeMillis();
			size0t = -1;
			lastDist = -1;
			lastHandheld = !handheld;
			resetShader = false;
		}

		//default white colour
		if (!color0t.equals(color0)) {
			color0t.set(color0);
			GlUtil.updateShaderVector4f(ShaderLibrary.simpleBeamShader, "thrustColor0", color0t);
		}

		//sets all uniforms
		if (!color1t.equals(color)) {
			color1t.set(color);
			GlUtil.updateShaderVector4f(ShaderLibrary.simpleBeamShader, "thrustColor1", color1t);
		}
		float dist = targetDistance / TEX_COORD_MULT;
		if (Math.abs(dist - lastDist) > 0.03f) {
			GlUtil.updateShaderFloat(ShaderLibrary.simpleBeamShader, "texCoordMult", dist);
			lastDist = dist;
		}
		if (Math.abs(lenDiff - lastLenDiff) > 0.03f) {
			GlUtil.updateShaderFloat(ShaderLibrary.simpleBeamShader, "lenDiff", lenDiff);
			lastLenDiff = lenDiff;
		}
		if (timert != time){
			timert = time;
			float timePassed = (time - timert)/1000f;
			GlUtil.updateShaderFloat(ShaderLibrary.simpleBeamShader, "time", timePassed);
		}
		if (Math.abs(size - size0t) > 0.03f) {
			size0t = size;
			GlUtil.updateShaderFloat(ShaderLibrary.simpleBeamShader, "size", size0t);
			GlUtil.updateShaderFloat(ShaderLibrary.simpleBeamShader, "sizeInv", Math.max(0.0000001f, 1f / size0t));
		}
		if (lastHandheld != handheld){
			lastHandheld = handheld;
			GlUtil.updateShaderBoolean(ShaderLibrary.simpleBeamShader, "handheld", handheld);
		}
		ShaderLibrary.simpleBeamShader.recompiled = false;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (beamHandlers == null) {
			if(exceptionCount % 100 == 0) {
				try {
					throw new Exception("[CLIENT][ERROR] ########## beam handlers null of beam drawer " + this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			exceptionCount ++;
			return;
		}
		//		if(observersAdded != beamHandlers.size()){
		//			updateObservers();
		//		}
		if (drawNeeded && EngineSettings.G_DRAW_BEAMS.isOn()) {
			for (AbstractBeamHandler<?> c : toDraw) {
				Long2ObjectOpenHashMap<BeamState> beamStates = c.getBeamStates();
				for(BeamState s : beamStates.values()) {
					BeamDrawer.drawConnection(s, tmpTrans, manager.getTime(), Controller.getCamera().getPos());
					
					if(manager.getZoomFac() != 0) {
						float sizefrom = s.handheld ? 1f : 5f;
						float sizeTo = s.handheld ? 1f : Math.min(5, Math.max(100, s.getPower()/100f));
						
						s.fromInset.set(s.from);
						
						
						s.dirTmp.sub(s.to, s.from);
						FastMath.normalizeCarmack(s.dirTmp);
						
						if(!s.handheld) {
							s.dirTmp.scale(0.5f);
							s.fromInset.add(s.dirTmp);
						}
						
						
						manager.addHitpoint(s.fromInset, sizefrom*0.5f, s.color);
						if(s.hitPoint != null) {
							s.toInset.set(s.hitPoint);
							
							if(!s.handheld) {
								s.toInset.sub(s.dirTmp);
							}
							manager.addHitpoint(s.toInset, sizeTo, s.color);
						}
					}
					
				}
//				draw(c, salvageMarkers, time, camPos);
			}
		}

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	public void clearObservers() {
		for (BeamHandlerContainer<?> c : beamHandlers) {
			//			System.err.println("[BEAM-HANDLER] CLEARING OBSERVER FOR BEAM CONTAINER "+c);
			c.getHandler().setDrawer(null);
		}
		toDraw.clear();
	}

	private void draw(AbstractBeamHandler<?> beamHandler, boolean drawSalvageMarkers, long time, Vector3f camPos) {
		if(!EngineSettings.G_DRAW_BEAMS.isOn()) {
			return;
		}
		int i = 0;
		for (BeamState c : beamHandler.getBeamStates().values()) {
			assert (c != null);
			c.color.set(color1);
			drawConnection(c, tmp, time, camPos);

			if (drawSalvageMarkers && beamHandler.drawBlockSalvage()) {
				drawBlockSalvage(c, tmp);
			}

			BeamDrawerManager.drawCalls++;
			i++;

		}

	}

	private void drawBlockSalvage(BeamState c2, Transform t2) {
		
	}

	public void drawBlock(Transform tmp) {
		

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(tmp);
		float v = 1.01f;
		GlUtil.scaleModelview(v, v, v);

		BeamDrawerManager.singlecubemesh.renderVBO();

		GlUtil.glPopMatrix();

	}
	private boolean isValid(SegmentPiece p){
		p.refresh();
		return p.getType() != Element.TYPE_NONE;
	}
	private void drawBoxes(AbstractBeamHandler<?> beamHandler) {
		Vector3f pos = Controller.getCamera().getPos();
		for (BeamState c : beamHandler.getBeamStates().values()) {
			SegmentPiece currentHit = c.currentHit;
			
			if (currentHit != null && isValid(currentHit)) {
				currentHit.getAbsolutePos(tmpPos);
				SegmentController controller = currentHit.getSegmentController();
				if(!c.lastHitPos.equals(tmpPos) || !controller.getWorldTransformOnClient().equals(c.lastSegConTrans)){
					
					c.lastSegConTrans.set(controller.getWorldTransformOnClient());
					
					c.lastHitTrans.set(controller.getWorldTransformOnClient());
					c.lastHitPos.set(tmpPos);
					p.set(c.lastHitPos.x - SegmentData.SEG_HALF, c.lastHitPos.y - SegmentData.SEG_HALF, c.lastHitPos.z - SegmentData.SEG_HALF);

					c.lastHitTrans.basis.transform(p);
					c.lastHitTrans.origin.add(p);
				}
				
				float dist = Vector3fTools.distance(pos.x, pos.y, pos.z, c.lastHitTrans.origin.x, c.lastHitTrans.origin.y, c.lastHitTrans.origin.z);
						
				if(dist < 15){
					GlUtil.updateShaderFloat(ShaderLibrary.beamBoxShader, "texMult", 1f + FastMath.pow(c.hitBlockTime * 2.5f, 0.8f) * 5.0f);
				}
				if(dist < 80){
					drawBlock(c.lastHitTrans);
				}
			}

		}
	}

	void drawSelectionBoxes() {
		GlUtil.updateShaderVector4f(ShaderLibrary.beamBoxShader, "selectionColor", 0.2f, 0.4f, 0.9f, 0.7f);
		for (AbstractBeamHandler<?> c : toDraw) {
			drawBoxes(c);
		}

	}

	public List<? extends BeamHandlerContainer<?>> getBeamHandlers() {
		return beamHandlers;
	}

	public void setBeamHandlers(ArrayList<? extends BeamHandlerContainer<?>> beamHandlers) {
		this.beamHandlers = beamHandlers;
	}

	public void insertEnd(ObjectHeapPriorityQueue<BeamState> sortedStates) {
		if (drawNeeded) {
			for (AbstractBeamHandler<?> beamHandler : toDraw) {
				for (BeamState c : beamHandler.getBeamStates().values()) {
					//					c.color.set(color1);
					if (c.hitPoint != null) {
						tmpPosD.sub(Controller.getCamera().getPos(), c.hitPoint);
					} else {
						tmpPosD.sub(Controller.getCamera().getPos(), c.to);
					}
					//					tmpPosF.sub(c.to, c.from);
					//					tmpPosF.scale(0.3f);

					c.camDistEnd = tmpPosD.lengthSquared();

					sortedStates.enqueue(c);
				}
			}
		}

	}

	public void insertStart(ObjectHeapPriorityQueue<BeamState> sortedStates) {
		if (drawNeeded) {
			for (AbstractBeamHandler<?> beamHandler : toDraw) {
				for (BeamState c : beamHandler.getBeamStates().values()) {

					tmpPosD.sub(Controller.getCamera().getPos(), c.from);
					c.camDistStart = tmpPosD.lengthSquared();

					int sizeBef = sortedStates.size();
					sortedStates.enqueue(c);
					assert (sortedStates.size() == (sizeBef + 1)) : sortedStates.size();

//					System.err.println("ADDING SORTED: "+c.getHandler().getBeamShooter()+";"+"::: "+sortedStates.size());
				}
			}
		}

	}

	public void notifyDraw(AbstractBeamHandler<?> beamHandler, boolean anyBeamActiveActive) {
		if (anyBeamActiveActive) {
			toDraw.add(beamHandler);
		} else {
			toDraw.remove(beamHandler);
		}

		if (drawNeeded != (!toDraw.isEmpty())) {
			manager.notifyOfBeam(this, !toDraw.isEmpty());
		}
		drawNeeded = !toDraw.isEmpty();
	}

	public void reset() {
		if (this.beamHandlers != null) {
			clearObservers();
		}
		this.beamHandlers = null;
		exceptionCount = 0;
	}

	public void set(List<? extends BeamHandlerContainer<?>> managers, BeamDrawerManager thisMan) {
		this.beamHandlers = managers;
		this.manager = thisMan;
		if (beamHandlers != null) {
			updateObservers();
		} else {
			clearObservers();
		}
	}

	public void update(Timer timer) {

		linTimer.update(timer);
	}

	public void updateObservers() {
		clearObservers();
		for (BeamHandlerContainer c : beamHandlers) {
			c.getHandler().setDrawer(this);
		}
		//		observersAdded = beamHandlers.size();
	}
	public boolean isValid() {
		return this.beamHandlers != null;
	}

	

}
