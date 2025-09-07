package org.schema.game.client.controller.manager.ingame;

import java.util.Set;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.SelectionShader;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class BuildSelection {
	public Vector3i selectionBoxA = null;
	public Vector3i selectionBoxB = null;
	public float cameraDistance = 3.0f;
	
	
	public void setSelectionBoxOrigin(Vector3i origin){
//		this.selectionBlock = new VoidUniqueSegmentPiece();
//		this.selectionBlock.voidPos.add(origin);
		this.selectionBoxA = origin;
	}
	
	public void resetSelectionBox(){
//		this.selectionBlock = null;
		this.selectionBoxA = null;
		this.selectionBoxB = null;

	}
	protected abstract boolean canExecute(PlayerInteractionControlManager pim);
	public void handleKeyEvent(PlayerInteractionControlManager pim, KeyEventInterface e) {
		
		BuildToolsManager buildToolsManager = pim.getBuildToolsManager();
		SegmentControlManager segmentControlManager = pim.getSegmentControlManager();
		
		Vector3f posSelect = new Vector3f(Controller.getCamera().getViewable().getPos());
		Vector3f forw = Controller.getCamera().getForward(new Vector3f());
		forw.scale(cameraDistance);

		posSelect.add(forw);
		segmentControlManager.getSegmentController().getWorldTransformInverse().transform(posSelect);

		posSelect.x = FastMath.round(posSelect.x) + SegmentData.SEG_HALF;
		posSelect.y = FastMath.round(posSelect.y) + SegmentData.SEG_HALF;
		posSelect.z = FastMath.round(posSelect.z) + SegmentData.SEG_HALF;
		
		
		
		if(e.isTriggered(KeyboardMappings.BUILD_BLOCK_BUILD_MODE)){
			if (selectionBoxA == null) {
				selectionBoxA = new Vector3i(posSelect);
				System.out.println("COPY Setting boxA " + posSelect);
				if(isSingleSelect()){
					callback(pim, e);
					resetSelectionBox();
					
					buildToolsManager.selectionPlaced = true;
					buildToolsManager.setSelectMode(null);
					segmentControlManager.getSegmentBuildController().getSymmetryPlanes().setPlaceMode(0);
					pim.getPlayerCharacterManager().getSymmetryPlanes().setPlaceMode(0);
					pim.getInShipControlManager().getShipControlManager().getSegmentBuildController().getSymmetryPlanes().setPlaceMode(0);
				}
				return;
			}
			
			if(canExecute(pim)){
				selectionBoxB = new Vector3i(posSelect);
				System.out.println("COPY Setting boxB " + posSelect);
				
				
				callback(pim, e);
				resetSelectionBox();
				
				buildToolsManager.setSelectMode(null);
				segmentControlManager.getSegmentBuildController().getSymmetryPlanes().setPlaceMode(0);
				pim.getPlayerCharacterManager().getSymmetryPlanes().setPlaceMode(0);
				buildToolsManager.selectionPlaced = true;
				pim.getInShipControlManager().getShipControlManager().getSegmentBuildController().getSymmetryPlanes().setPlaceMode(0);
				System.out.println("COPY handling event 1");
				
			} else {
				//use camera to span an area
				selectionBoxB = new Vector3i(posSelect);

				Vector3i size = new Vector3i(selectionBoxB);
				size.sub(selectionBoxA);
				size.x += Math.signum(size.x);
				size.y += Math.signum(size.y);
				size.z += Math.signum(size.z);
				size.absolute();

				buildToolsManager.width.set(size.x);
				buildToolsManager.height.set(size.y);
				buildToolsManager.depth.set(size.z);
				
				resetSelectionBox();
				buildToolsManager.setSelectMode(null);
				buildToolsManager.selectionPlaced = true;
				System.out.println("COPY handling event 2");
				
			}
			
			 
		}
		if(e.isTriggered(KeyboardMappings.REMOVE_BLOCK_BUILD_MODE)){
			resetSelectionBox();
		}		
	}

	protected abstract boolean isSingleSelect();

	protected abstract void callback(PlayerInteractionControlManager pim, KeyEventInterface e);
	
	public enum DrawStyle{
		BOX,
		LINE
	}
	
	protected abstract DrawStyle getDrawStyle();

	public void draw(GameClientState state, SegmentController segCon, Mesh mesh, SelectionShader selectionShader) {
		switch(getDrawStyle()) {
			case BOX -> drawBox(state, segCon, mesh, selectionShader);
			case LINE -> drawLine(state, segCon, mesh, selectionShader);
			default -> throw new RuntimeException("Unknown Draw Style " + getDrawStyle());
		}
		
	}
	private void drawLine(GameClientState state, SegmentController segCon, Mesh mesh, SelectionShader selectionShader) {
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Vector3f scale = new Vector3f(1f, 1f, 1f);
		Vector3f forw = Controller.getCamera().getForward(new Vector3f());
		forw.scale(cameraDistance);

		Transform dT = new Transform(segCon.getWorldTransform());

		Set<Vector3f> linePoints = new ObjectOpenHashSet<Vector3f>();
		
		if (selectionBoxA != null) {

			
			Vector3f mP = new Vector3f(selectionBoxA.x, selectionBoxA.y, selectionBoxA.z);
			mP.x -= SegmentData.SEG_HALF;
			mP.y -= SegmentData.SEG_HALF;
			mP.z -= SegmentData.SEG_HALF;
			
			Vector3f mPTo = new Vector3f(Controller.getCamera().getViewable().getPos());
			mPTo.add(forw);
			segCon.getWorldTransformInverse().transform(mPTo);
			mPTo.x = FastMath.round(mPTo.x);
			mPTo.y = FastMath.round(mPTo.y);
			mPTo.z = FastMath.round(mPTo.z);
			
			
			
			
			Vector3f d = new Vector3f();
			d.sub(mPTo, mP);
			
			float len = Math.max(0.5f, d.length());
			
			d.normalize();
			for(float f = 0; f < len; f += 0.5f){
				
				Vector3f p = new Vector3f(mP);
				p.x += FastMath.round(f * d.x);
				p.y += FastMath.round(f * d.y);
				p.z += FastMath.round(f * d.z);
				
				linePoints.add(p);
			}
			
			

		} else {

			//draw box in front of camera
			dT = new Transform(segCon.getWorldTransform());
			Vector3f mP = new Vector3f(Controller.getCamera().getViewable().getPos());
			mP.add(forw);
			segCon.getWorldTransformInverse().transform(mP);
			mP.x = FastMath.round(mP.x);
			mP.y = FastMath.round(mP.y);
			mP.z = FastMath.round(mP.z);

			linePoints.add(mP);
			
			

		}
		//prevent Z-fighting
		scale.add(new Vector3f(0.01f, 0.01f, 0.01f));
		
		for(Vector3f p : linePoints){
//			p.x += SegmentData.SEG_HALF;
//			p.y += SegmentData.SEG_HALF;
//			p.z += SegmentData.SEG_HALF;
			dT.set(segCon.getWorldTransform());
			dT.basis.transform(p);
			dT.origin.add(p);
			
			GlUtil.glPushMatrix();
			GlUtil.glMultMatrix(dT);
			
			GlUtil.scaleModelview(scale.x, scale.y, scale.z);
	
			ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
			ShaderLibrary.selectionShader.load();
	
			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.1f, 0.9f, 0.6f, 0.65f);
			mesh.renderVBO();
	
			GlUtil.glPopMatrix();
		}
		GlUtil.glColor4f(1, 1, 1, 1f);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
					
	}

	private void drawBox(GameClientState state, SegmentController segCon, Mesh mesh, SelectionShader selectionShader) {
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Vector3f scale = new Vector3f(1f, 1f, 1f);
		Vector3f forw = Controller.getCamera().getForward(new Vector3f());
		forw.scale(cameraDistance);

		Transform dT = null;
		if (selectionBoxA != null) {

			dT = new Transform(segCon.getWorldTransform());
			Vector3f mP = new Vector3f(selectionBoxA.x, selectionBoxA.y, selectionBoxA.z);
			scale = new Vector3f(Controller.getCamera().getViewable().getPos());

			scale.add(forw);


			mP.x = FastMath.round(mP.x);
			mP.y = FastMath.round(mP.y);
			mP.z = FastMath.round(mP.z);

			segCon.getWorldTransformInverse().transform(scale);	

			scale.x = FastMath.round(scale.x) + SegmentData.SEG_HALF;
			scale.y = FastMath.round(scale.y) + SegmentData.SEG_HALF;
			scale.z = FastMath.round(scale.z) + SegmentData.SEG_HALF;

			scale.sub(mP);

			mP.x -= SegmentData.SEG_HALF;
			mP.y -= SegmentData.SEG_HALF;
			mP.z -= SegmentData.SEG_HALF;
			
			//make the box fit your camera point
			scale.x += Math.signum(scale.x);
			scale.y += Math.signum(scale.y);
			scale.z += Math.signum(scale.z);

			//don't make it bigger than the max allowed size
			float maxSize = state.getMaxBuildArea();
			scale.x = Math.signum(scale.x) > 0 ? Math.min(scale.x, Math.signum(scale.x) * maxSize) : Math.max(scale.x, Math.signum(scale.x) * maxSize);
			scale.y = Math.signum(scale.y) > 0 ? Math.min(scale.y, Math.signum(scale.y) * maxSize) : Math.max(scale.y, Math.signum(scale.y) * maxSize);
			scale.z = Math.signum(scale.z) > 0 ? Math.min(scale.z, Math.signum(scale.z) * maxSize) : Math.max(scale.z, Math.signum(scale.z) * maxSize);
			
			//put the starting point nicely where it belongs
			mP.x += (scale.x / 2) - (0.5f * Math.signum(scale.x));
			mP.y += (scale.y / 2) - (0.5f * Math.signum(scale.y));
			mP.z += (scale.z / 2) - (0.5f * Math.signum(scale.z));

			//get a visible box from the start
			if (scale.x == 0.0f) {
				scale.x = 1.0f;
			}
			if (scale.y == 0.0f) {
				scale.y = 1.0f;
			}

			if (scale.z == 0.0f) {
				scale.z = 1.0f;
			}

			dT.basis.transform(mP);
			dT.origin.add(mP);

		} else {

			//draw box in front of camera
			dT = new Transform(segCon.getWorldTransform());
			Vector3f mP = new Vector3f(Controller.getCamera().getViewable().getPos());
			mP.add(forw);
			segCon.getWorldTransformInverse().transform(mP);
			mP.x = FastMath.round(mP.x);
			mP.y = FastMath.round(mP.y);
			mP.z = FastMath.round(mP.z);

			dT.basis.transform(mP);
			dT.origin.add(mP);

		}

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(dT);

		//prevent Z-fighting
		scale.add(new Vector3f(Math.signum(scale.x) * 0.02f, Math.signum(scale.y) * 0.02f, Math.signum(scale.z) * 0.02f));

		
		GlUtil.scaleModelview(scale.x, scale.y, scale.z);

		ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
		ShaderLibrary.selectionShader.load();

		GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.1f, 0.9f, 0.6f, 0.65f);
		mesh.renderVBO();

		GlUtil.glColor4f(1, 1, 1, 1f);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glPopMatrix();		
	}

	
}
