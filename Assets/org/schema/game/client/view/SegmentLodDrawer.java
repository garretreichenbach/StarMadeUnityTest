package org.schema.game.client.view;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.generator.PlanetCreatorThread;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.WorldCreatorPlanetFactory;
import org.schema.game.server.controller.world.factory.terrain.TerrainGenerator;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.simple.Box;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SegmentLodDrawer {

	
	private SegmentDrawer segmentDrawer;
	private Vector4f color = new Vector4f(0.37f,0.37f,0.37f,0.3f);
	RequestDataPlanet pd = new RequestDataPlanet();
	private static Vector3f[][] box = Box.getVertices(
			new Vector3f(-SegmentData.SEG_HALF,-SegmentData.SEG_HALF,-SegmentData.SEG_HALF),
			new Vector3f(SegmentData.SEG_HALF,SegmentData.SEG_HALF,SegmentData.SEG_HALF), Box.init());
	private static Vector3f[][] boxSmall = Box.getVertices(
			new Vector3f(-1.5f,-1.5f,-1.5f),
			new Vector3f(2.5f,2.5f,2.5f), Box.init());
	
	private static Vector3f[][] boxBlock = Box.getVertices(
			new Vector3f(-0.5f,-0.5f,-0.5f),
			new Vector3f(0.5f,0.5f,0.5f), Box.init());

	public SegmentLodDrawer(SegmentDrawer segmentDrawer) {
		super();
		this.segmentDrawer = segmentDrawer;
		
		
		
	}
	
	public void draw(){
		ObjectArrayList<SegmentController> segmentControllers = segmentDrawer.getSegmentControllers();
		for(int i = 0; i < segmentControllers.size(); i++){
			SegmentController segmentController = segmentControllers.get(i);
			
			if(segmentController instanceof Planet){
				GL20.glUseProgram(0);
				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glEnable(GL11.GL_CULL_FACE);
				GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				GlUtil.glDisable(GL12.GL_TEXTURE_3D);
				GlUtil.glDisable(GL11.GL_TEXTURE_2D);
				GlUtil.glDisable(GL11.GL_TEXTURE_1D);
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glColor4f(color.x, color.y, color.z, color.w);
				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(segmentController.getWorldTransformOnClient());
				PlanetCreatorThread planetCreatorThread = new PlanetCreatorThread(((Planet)segmentController), ((Planet)segmentController).getPlanetType());
				((WorldCreatorPlanetFactory)planetCreatorThread.creator).initialize(segmentController);
				TerrainGenerator generator = ((WorldCreatorPlanetFactory)planetCreatorThread.creator).generator;
				assert(generator != null);
				if(pd.getR().noiseArray != null){
					Arrays.fill(pd.getR().noiseArray, 0);
				}
				generator.generateTerrainMicro(0, 0, null, pd, this);
//				generator.generateTerrainMacro(0, 0, null, pd, this);
				
//				drawLowest(segmentController.getSegmentBuffer());
				GlUtil.glPopMatrix();
				
				GlUtil.glEnable(GL11.GL_DEPTH_TEST);
				GlUtil.glEnable(GL11.GL_LIGHTING);
				GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glEnable(GL11.GL_CULL_FACE);
				GlUtil.glDisable(GL11.GL_BLEND);
				
				
				break;
			}
		}
	}

	public void draw(int x, int y, int z) {
		
		//		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		
		

		GlUtil.glPushMatrix();
		GlUtil.glTranslatef(x, y, z);
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < box.length; i++) {
			for (int k = 0; k < box[i].length; k++) {
				GL11.glVertex3f(box[i][k].x, box[i][k].y, box[i][k].z);
			}
		}
		GL11.glEnd();
		GlUtil.glPopMatrix();
		
	}
	public void drawSmall(int x, int y, int z) {
		
		//		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		
		
		
		GlUtil.glPushMatrix();
		GlUtil.glTranslatef(x, y, z);
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < boxSmall.length; i++) {
			for (int k = 0; k < boxSmall[i].length; k++) {
				GL11.glVertex3f(boxSmall[i][k].x, boxSmall[i][k].y, boxSmall[i][k].z);
			}
		}
		GL11.glEnd();
		GlUtil.glPopMatrix();
		
	}
	public void drawBlock(int x, int y, int z) {
		
		//		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		
		
		
		GlUtil.glPushMatrix();
		GlUtil.glTranslatef(x, y, z);
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < boxBlock.length; i++) {
			for (int k = 0; k < boxBlock[i].length; k++) {
				GL11.glVertex3f(boxBlock[i][k].x, boxBlock[i][k].y, boxBlock[i][k].z);
			}
		}
		GL11.glEnd();
		GlUtil.glPopMatrix();
		
	}
	
}
