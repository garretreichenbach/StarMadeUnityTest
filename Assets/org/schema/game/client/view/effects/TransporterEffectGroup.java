package org.schema.game.client.view.effects;

import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterUnit;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import com.bulletphysics.linearmath.Transform;

public class TransporterEffectGroup {
	public SegmentController segmentController;
	public List<TransporterCollectionManager> transporters;
	public TransporterEffectGroup(SegmentController segmentController,
			List<TransporterCollectionManager> transporters) {
		super();
		this.segmentController = segmentController;
		this.transporters = transporters;
		tmpTrans.setIdentity();
	}
	public void updateLocal(Timer timer) {
				
	}
	Vector3i posiTmp = new Vector3i();
	Vector3f posfTmp = new Vector3f(); 
	SegmentPiece p = new SegmentPiece();
	Transform tmpTrans = new Transform();
	private Vector3f colorFrom = new Vector3f(0.2f, 0.3f, 0.4f);
	private Vector3f colorTo = new Vector3f(0.2f, 0.4f, 0.3f);
	public void draw(Mesh mesh) {
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(segmentController.getWorldTransformOnClient());
		
		for(TransporterCollectionManager t : transporters){
//			System.err.println("TTT "+t+": "+t.isTransporterActive()+" "+t.isTransporterReceivingActive());
			if(t.isTransporterActive() || t.isTransporterReceivingActive()){
				
				GlUtil.updateShaderVector3f(ShaderLibrary.transporterShader, "colorMain", t.isTransporterActive() ? colorFrom : colorTo);
				
				GlUtil.updateShaderFloat(ShaderLibrary.transporterShader, "intensity", t.getEffectIntesity());

				for(TransporterUnit u : t.getElementCollections()){
					for(long pos : u.getNeighboringCollection()){
					
				
						ElementCollection.getPosFromIndex(pos, posiTmp);
						
						SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(pos, p);
						
						if(pointUnsave != null){
							GlUtil.glPushMatrix();
							
							posiTmp.add(Element.DIRECTIONSi[Element.switchLeftRight(pointUnsave.getOrientation())]);
							
							posiTmp.sub(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
							
							posfTmp.set(posiTmp.x, posiTmp.y, posiTmp.z);
							
							
							GlUtil.translateModelview(posfTmp);
							tmpTrans.basis.set(Element.getRotationPerSideTopBase(pointUnsave.getOrientation()));
							GlUtil.glMultMatrix(tmpTrans);
							
							GlUtil.translateModelview(0, -0.5f, 0);
							mesh.drawVBO();
							
							GlUtil.glPopMatrix();
						}
						
						
						
					}
				}
			}
		}
		
		
		GlUtil.glPopMatrix();
	}
	
	
}
