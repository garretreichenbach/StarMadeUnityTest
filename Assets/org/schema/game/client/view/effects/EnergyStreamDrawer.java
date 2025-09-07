package org.schema.game.client.view.effects;

import java.nio.FloatBuffer;
import java.util.List;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.texture.Material;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class EnergyStreamDrawer implements Drawable {

	static int maxVerts = 1024;
	

	private final Vector4f color = new Vector4f(1,1,1,1);
	
	private static Int2ObjectOpenHashMap<Verts> vMap = new Int2ObjectOpenHashMap<Verts>();
	
	public static Verts getVerts(int c){
		Verts verts = vMap.get(c);
		if(verts == null){
			verts = new Verts(c);
			vMap.put(c, verts);
		}
		return verts;
	}
	
	private static class Verts{
		public final int vertCount;
		private final Vector3f[] tubeVerticesA;
		private final Vector3f[] tubeVerticesNormal;
		private final Vector3f[] tubeVerticesB;
		private final Vector3f[] tubeTexCoord;
		private final Vector3f[] tubeVerticesAPre;
		private final Vector3f[] tubeVerticesNormalPre;
		private final Vector3f[] tubeVerticesBPre;
		private final Vector3f[] tubeTexCoordPre;
		public Verts(int vertCount){
			this.vertCount = vertCount;
			tubeVerticesA = new Vector3f[vertCount];
			tubeVerticesNormal = new Vector3f[vertCount];
			tubeVerticesB = new Vector3f[vertCount];
			tubeTexCoord = new Vector3f[vertCount];
			tubeVerticesAPre = new Vector3f[vertCount];
			tubeVerticesNormalPre = new Vector3f[vertCount];
			tubeVerticesBPre = new Vector3f[vertCount];
			tubeTexCoordPre = new Vector3f[vertCount];
			
			for (int i = 0; i < vertCount; i++) {
				tubeVerticesA[i] = new Vector3f();
				tubeVerticesB[i] = new Vector3f();
				tubeVerticesNormal[i] = new Vector3f();
				tubeTexCoord[i] = new Vector3f();

				tubeVerticesAPre[i] = new Vector3f();
				tubeVerticesBPre[i] = new Vector3f();
				tubeVerticesNormalPre[i] = new Vector3f();
				tubeTexCoordPre[i] = new Vector3f();
			}
		}
		private int putStraightSection(final float radius, Vector3f start, Vector3f end, Vector3f startDir, Vector3f endDir, float tStart, float tEnd, float distTotal, FloatBuffer buffer, FloatBuffer normalBuffer, FloatBuffer texCoordBuffer, int index, int maxIndex) {
			configMatrix(m0, startDir);
			configMatrix(m1, endDir);
			for (int i = 0; i < vertCount; i++) {
				tubeVerticesA[i].set(tubeVerticesAPre[i]);
				tubeVerticesB[i].set(tubeVerticesBPre[i]);

				m0.transform(tubeVerticesA[i]);
				m1.transform(tubeVerticesB[i]);

				tubeVerticesNormal[i].set(tubeVerticesA[i]);

				tubeTexCoord[i].set(tubeTexCoordPre[i]);

				tubeVerticesA[i].add(start);
				tubeVerticesB[i].add(end);
			}
			assert (maxIndex != 0);
			float percent = (float) index / (float) maxIndex;

			for (int i = 0; i < vertCount; i++) {
				GlUtil.putPoint4(buffer, tubeVerticesA[i], radius);
				GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[i]);
				tubeTexCoord[i].x = tStart;
				tubeTexCoord[i].z = distTotal;
				GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[i]);

				
				
				GlUtil.putPoint4(buffer, tubeVerticesA[(i + 1) % vertCount], radius);
				GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[(i + 1) % vertCount]);
				tubeTexCoord[(i + 1) % vertCount].x = tStart;
				if(i == vertCount-1){
					//last texture coordinate has to be 1 and not 0 (looparound)
					tubeTexCoord[(i + 1) % vertCount].y = 1;
				}
				tubeTexCoord[(i + 1) % vertCount].z = distTotal;
				GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[(i + 1) % vertCount]);

				
				
				
				
				GlUtil.putPoint4(buffer, tubeVerticesB[(i + 1) % vertCount], radius);
				GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[(i + 1) % vertCount]);
				tubeTexCoord[(i + 1) % vertCount].x = tEnd;
				if(i == vertCount-1){
					//last texture coordinate has to be 1 and not 0 (looparound)
					tubeTexCoord[(i + 1) % vertCount].y = 1;
				}
				tubeTexCoord[(i + 1) % vertCount].z = distTotal;
				GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[(i + 1) % vertCount]);
				
				
				
				GlUtil.putPoint4(buffer, tubeVerticesB[i], radius);
				GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[i]);
				tubeTexCoord[i].x = tEnd;
				tubeTexCoord[i].z = distTotal;
				GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[i]);

			}
			return 4*vertCount;

		}

		private void initRadiusA(float radius){
			forward.set(0, 0, 1);

			float sec = FastMath.PI / (vertCount/2.0f);
			float rot = 0;
			float texCoordY = 0;
			float p = 1.0f / vertCount;

			for (int i = 0; i < vertCount; i++) {

				aa.set(forward, rot);
				mT.set(aa);
				tubeVerticesAPre[i].set(0, radius, 0);
				mT.transform(tubeVerticesAPre[i]);
				tubeVerticesNormalPre[i].set(tubeVerticesAPre[i]);
				tubeTexCoordPre[i].set(0, texCoordY, 0);
				texCoordY += p;
				
				rot += sec;
			}
		}
		
		private void initRadiusB(float radius){
			forward.set(0, 0, 1);
			
			float sec = FastMath.PI / (vertCount/2.0f);
			float rot = 0;
			float texCoordY = 0;
			float p = 1.0f / vertCount;
			
			for (int i = 0; i < vertCount; i++) {
				
				aa.set(forward, rot);
				mT.set(aa);
				tubeVerticesBPre[i].set(0, radius, 0);
				mT.transform(tubeVerticesBPre[i]);
				tubeVerticesNormalPre[i].set(tubeVerticesBPre[i]);
				tubeTexCoordPre[i].set(0, texCoordY, 0);
				texCoordY += p;
				rot += sec;
			}
		}
	}
	
	static Vector3f forward = new Vector3f();
	static AxisAngle4f aa = new AxisAngle4f();
	static Matrix3f m0 = new Matrix3f();
	static Matrix3f m1 = new Matrix3f();
	static Matrix3f mT = new Matrix3f();
	
	private final SegmentController c;
	private int VBOIndex;
	private Vector3f start = new Vector3f();
	private Vector3f end = new Vector3f();
	private Vector3f dist = new Vector3f();
	private Vector3f sDir = new Vector3f();
	private Vector3f eDir = new Vector3f();
	private Vector3f tmpDir = new Vector3f();
	private static Vector3f up = new Vector3f(0, 1, 0);
	private static Vector3f right = new Vector3f();
	private int vertices;
	private boolean flagUpdate = true;
	private boolean noDraw;
	private int VBONormalIndex;
	private int VBOTexCoordsIndex;
	private Material material;
	private long failed = -1;
	private boolean conOn = EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn();
	private boolean conLohOn = EngineSettings.G_DRAW_ALL_CONNECTIONS.isOn();
	
	public static float radiusScale = 1.0f;
	
	private List<StabilizerPath> stabilizerPaths = new ObjectArrayList<StabilizerPath>();

	public EnergyStreamDrawer(SegmentController c) {
		super();
		this.c = c;
		
		conOn = EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn();
	}
	private static int getVertsFromRadius(float rr){
		int maxVerts = 8;
		if(EngineSettings.USE_POLY_SCALING_ENERGY_STREAM.isOn()){
			if(rr > 0.75){
				if(rr < 1.5){
					maxVerts = 16;
				}else if(rr < 6.0){
					maxVerts = 32;
				}else{
					maxVerts = 48;
				}
			}
		}
		return maxVerts;
	}
	private Vector3f d = new Vector3f();
	private boolean wasUsingPoly = EngineSettings.USE_POLY_SCALING_ENERGY_STREAM.isOn();
	public void buildMesh(){
		
		if(stabilizerPaths.size() == 0){
			return;
		}
		
		
		
		
		long t0 = System.currentTimeMillis();
		long t = System.currentTimeMillis();
		int connectionCount = 0;
		
		
		
		float minRadius = 0.01f;
		int i = 0;
		vertices = 0;
		for(StabilizerPath s : stabilizerPaths){
			long from = s.start;
			s.graphicsTotalLength = 0;
			while(s.nodes.containsKey(from)){
				long to = s.nodes.get(from);
				if(to == from){
					noDraw = true;
					return;
				}
				
				int x = ElementCollection.getPosX(from) - ElementCollection.getPosX(to);
				int y = ElementCollection.getPosY(from) - ElementCollection.getPosY(to);
				int z = ElementCollection.getPosZ(from) - ElementCollection.getPosZ(to);
				
				maxVerts = getVertsFromRadius(s.getRadius());
				float length = Vector3fTools.length(x, y, z);
				if (length > 0.1f) {
					connectionCount++;
					s.graphicsTotalLength += length;
					
					//2 circles of verts(4)/normal(3)/texcoord(3) times vert count
					vertices += 2 * (4 + 3 + 3) * maxVerts;
				}
				
				from = to;
				i++;
				if(i > 0 && i %100 == 0){
					System.err.println("STAB PATH DRAWER HIGH NODE COUNT ::: "+i);
				}
			}
		}
		
		
		
		
		
		long tookInitial = System.currentTimeMillis() - t;
		noDraw = connectionCount == 0;
		if (noDraw) {
			return;
		}
		
		
		t = System.currentTimeMillis();
		
		int bytes = vertices * 3 * ByteUtil.SIZEOF_FLOAT;
		
		while (maxVerts < bytes) {
			maxVerts *= 2;
		}
		FloatBuffer buffer = GlUtil.getDynamicByteBuffer(maxVerts, 0).asFloatBuffer();
		FloatBuffer normalBuffer = GlUtil.getDynamicByteBuffer(maxVerts, 1).asFloatBuffer();
		FloatBuffer texCoordBuffer = GlUtil.getDynamicByteBuffer(maxVerts, 2).asFloatBuffer();
		long tookFetchBuffers = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
		int max = 1;
		int index = 1;
		float pMod = 0;
		int actualVerts = 0;
		
		float startEndLen = 0.4f;
		for(StabilizerPath s : stabilizerPaths){
			
			final float radiusInit = s.getRadius();
			float radius = s.getRadius();
			int verts = 8;
			if(EngineSettings.USE_POLY_SCALING_ENERGY_STREAM.isOn()){
				if(radius > 0.75){
					if(radius < 1.5){
						verts = 16;
					}else if(radius < 6.0){
						verts = 32;
					}else{
						verts = 48;
					}
				}
			}
			Verts v = getVerts(getVertsFromRadius(s.getRadius()));
			
			long from = s.start;
			int seg = 0;
			boolean startPoint = true;
			float curPosLen = 0;
			float nextPosLen = 0;
			while(s.nodes.containsKey(from)){
				
				long to = s.nodes.get(from);
				boolean endPoint = !s.nodes.containsKey(to);
				
				int fromX = ElementCollection.getPosX(from);
				int fromY = ElementCollection.getPosY(from);
				int fromZ = ElementCollection.getPosZ(from);
				
				int toX = ElementCollection.getPosX(to);
				int toY = ElementCollection.getPosY(to);
				int toZ = ElementCollection.getPosZ(to);
				
				int xD = toX - fromX;
				int yD = toY - fromY;
				int zD = toZ - fromZ;
				
				if (xD*xD + yD*yD + zD*zD > 1 || radius*radiusScale > 0.5f) {

					d.set(xD,yD,zD);
					
					float len = d.length();
					if(startPoint || endPoint){
						len+=radius;
					}
					nextPosLen += len;
					d.normalize();
					d.scale(startEndLen);
					
					float fac = 0.2f;//d.length()+radius*2f; was causing the stretches (T2789)
					
					float curStart = curPosLen / s.graphicsTotalLength;
					float curStartPlus = (curPosLen+(fac)) / s.graphicsTotalLength;
					float curEndMinus = (nextPosLen-(fac)) / s.graphicsTotalLength;
					float curEnd = nextPosLen / s.graphicsTotalLength;
					
					if(startPoint){
						
						start.set(
								pMod - SegmentData.SEG_HALF + fromX, 
								pMod - SegmentData.SEG_HALF + fromY, 
								pMod - SegmentData.SEG_HALF + fromZ);
						
						end.set(
								pMod - SegmentData.SEG_HALF + fromX + d.x, 
								pMod - SegmentData.SEG_HALF + fromY + d.y, 
								pMod - SegmentData.SEG_HALF + fromZ + d.z);
						
						sDir.sub(end, start);
						sDir.normalize();
						eDir.sub(end, start);
						eDir.normalize();
						v.initRadiusA(minRadius*radiusScale);
						v.initRadiusB(radiusInit*radiusScale);
						
						actualVerts += v.putStraightSection(radiusInit, start, end, sDir, eDir, curStart, curStartPlus, s.graphicsTotalLength, buffer, normalBuffer, texCoordBuffer, index, max);
					}else{
						//this is a corner section
//						assert(false):s.nodes;
						
						start.set(
								pMod - SegmentData.SEG_HALF + fromX, 
								pMod - SegmentData.SEG_HALF + fromY, 
								pMod - SegmentData.SEG_HALF + fromZ);
						
						end.set(
								pMod - SegmentData.SEG_HALF + fromX + d.x, 
								pMod - SegmentData.SEG_HALF + fromY + d.y, 
								pMod - SegmentData.SEG_HALF + fromZ + d.z);
						
//						sDir.sub(end, start);
//						sDir.normalize();
						sDir.set(eDir); //use from last corner
						eDir.sub(end, start);
						eDir.normalize();
						v.initRadiusA(radiusInit*radiusScale);
						v.initRadiusB(radiusInit*radiusScale);
						
						actualVerts += v.putStraightSection(radiusInit, start, end, sDir, eDir, curStart, curStartPlus, s.graphicsTotalLength, buffer, normalBuffer, texCoordBuffer, index, max);
					}
					
					v.initRadiusA(radiusInit*radiusScale);
					v.initRadiusB(radiusInit*radiusScale);
					
					float realStart; 
					float realEnd; 
					
					if(startPoint){
						start.set(
								pMod - SegmentData.SEG_HALF + fromX + d.x, 
								pMod - SegmentData.SEG_HALF + fromY + d.y, 
								pMod - SegmentData.SEG_HALF + fromZ + d.z);
						
						//end needed here only for dir
						end.set(
								pMod - SegmentData.SEG_HALF + toX - d.x, 
								pMod - SegmentData.SEG_HALF + toY - d.y, 
								pMod - SegmentData.SEG_HALF + toZ - d.z);
						
						sDir.sub(end, start);
						sDir.normalize();
						
						realStart = curStartPlus;
					}else{
//						start.set(
//								(float)pMod - (float)SegmentData.SEG_HALF + (float)fromX, 
//								(float)pMod - (float)SegmentData.SEG_HALF + (float)fromY, 
//								(float)pMod - (float)SegmentData.SEG_HALF + (float)fromZ);
//						realStart = curStart;
						
						
						
						start.set(
								pMod - SegmentData.SEG_HALF + fromX + d.x, 
								pMod - SegmentData.SEG_HALF + fromY + d.y, 
								pMod - SegmentData.SEG_HALF + fromZ + d.z);
						
						//end needed here only for dir
						end.set(
								pMod - SegmentData.SEG_HALF + toX - d.x, 
								pMod - SegmentData.SEG_HALF + toY - d.y, 
								pMod - SegmentData.SEG_HALF + toZ - d.z);
						
						sDir.sub(end, start);
						sDir.normalize();
						
						realStart = curStartPlus;
					}
					if(endPoint){
						end.set(
								pMod - SegmentData.SEG_HALF + toX - d.x, 
								pMod - SegmentData.SEG_HALF + toY - d.y, 
								pMod - SegmentData.SEG_HALF + toZ - d.z);
						
						eDir.sub(end, start);
						eDir.normalize();
						
						realEnd = curEndMinus;
					}else{
						
						
						
						end.set(
								pMod - SegmentData.SEG_HALF + toX - d.x, 
								pMod - SegmentData.SEG_HALF + toY - d.y, 
								pMod - SegmentData.SEG_HALF + toZ - d.z);
						
						eDir.sub(end, start);
						eDir.normalize();
						
						realEnd = curEndMinus;
						
					}
					assert(sDir.equals(eDir));
					actualVerts += v.putStraightSection(radiusInit, start, end, sDir, eDir, realStart, realEnd, s.graphicsTotalLength, buffer, normalBuffer, texCoordBuffer, index, max);
					sDir.set(eDir);
					
					if(endPoint){
						start.set(
								pMod - SegmentData.SEG_HALF + toX - d.x, 
								pMod - SegmentData.SEG_HALF + toY - d.y, 
								pMod - SegmentData.SEG_HALF + toZ - d.z);
						
						end.set(
								pMod - SegmentData.SEG_HALF + toX, 
								pMod - SegmentData.SEG_HALF + toY, 
								pMod - SegmentData.SEG_HALF + toZ);
						
						
						sDir.sub(end, start);
						sDir.normalize();
						eDir.sub(end, start);
						eDir.normalize();
						
						v.initRadiusA(radiusInit*radiusScale);
						v.initRadiusB(minRadius*radiusScale);
						
						actualVerts += v.putStraightSection(radiusInit, start, end, sDir, eDir, curEndMinus, curEnd, s.graphicsTotalLength, buffer, normalBuffer, texCoordBuffer, index, max);
					}else{
						//this is a corner section
//						assert(false):from+"; "+to+"; "+s.nodes;
						
						start.set(
								pMod - SegmentData.SEG_HALF + toX - d.x, 
								pMod - SegmentData.SEG_HALF + toY - d.y, 
								pMod - SegmentData.SEG_HALF + toZ - d.z);
						
//						end.set(
//								pMod - SegmentData.SEG_HALF + toX, 
//								pMod - SegmentData.SEG_HALF + toY, 
//								pMod - SegmentData.SEG_HALF + toZ);
//						
//						
//						sDir.sub(end, start);
//						sDir.normalize();
//						eDir.sub(end, start);
//						eDir.normalize();
						
						
						long next = s.nodes.get(to);
						
						int nfromX = ElementCollection.getPosX(to);
						int nfromY = ElementCollection.getPosY(to);
						int nfromZ = ElementCollection.getPosZ(to);
						
						int ntoX = ElementCollection.getPosX(next);
						int ntoY = ElementCollection.getPosY(next);
						int ntoZ = ElementCollection.getPosZ(next);
						
						tmpDir.x = ntoX-nfromX;
						tmpDir.y = ntoY-nfromY;
						tmpDir.z = ntoZ-nfromZ;
						
						tmpDir.normalize();
						
						tmpDir.add(eDir);
						tmpDir.normalize();
						eDir.set(tmpDir);
						
						end.set(
								pMod - SegmentData.SEG_HALF + toX, 
								pMod - SegmentData.SEG_HALF + toY, 
								pMod - SegmentData.SEG_HALF + toZ);
						
						
						
						realEnd = curEnd;
						
						actualVerts += v.putStraightSection(radiusInit, start, end, sDir, eDir, curEndMinus, curEnd, s.graphicsTotalLength, buffer, normalBuffer, texCoordBuffer, index, max);
					}
					seg++;
					
					curPosLen += len;
					nextPosLen = curPosLen;
				}
				
				
				from = to;
				startPoint = false;
			}
			wasUsingPoly = EngineSettings.USE_POLY_SCALING_ENERGY_STREAM.isOn();
		}
		vertices = actualVerts;
		long tookVerInser = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
		assert (vertices == actualVerts) : vertices + "/" + actualVerts;

		if (VBOIndex == 0) {
			VBOIndex = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(VBOIndex);
			
			VBONormalIndex = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(VBONormalIndex);

			VBOTexCoordsIndex = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(VBOTexCoordsIndex);
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOIndex); // Bind
		
		buffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormalIndex); // Bind
		normalBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoordsIndex); // Bind
		texCoordBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STATIC_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		material = new Material();
		material.setShininess(64);
		material.setSpecular(new float[]{0.9f, 0.9f, 0.9f, 1});
		long tookVBO = System.currentTimeMillis() - t;

		long total = System.currentTimeMillis() - t0;

		if (total > 30) {
			System.err.println("CONNECTION BUILDING TOOK " + total + "ms : tookInitial " + tookInitial + "; tookFetchBuffers " + tookFetchBuffers + "; tookVerInser " + tookVerInser + "; tookVBO " + tookVBO);
		}
	}

	@Override
	public void cleanUp() {
		if (VBOIndex != 0) {
			GL15.glDeleteBuffers(VBOIndex);
			GL15.glDeleteBuffers(VBONormalIndex);
			GL15.glDeleteBuffers(VBOTexCoordsIndex);
		}
	}
	public void drawDebug(){
		((ManagedSegmentController<?>)c).getManagerContainer().getPowerInterface().drawDebugEnergyStream();
	}
	@Override
	public void draw() {

		if (failed > 0 && System.currentTimeMillis() - failed > 2500) {
			flagUpdate = true;
			failed = -1;
		}
		if(conOn != EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn()){
			flagUpdate = true;
			conOn = EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn();
		}
		if(conLohOn != EngineSettings.G_DRAW_ALL_CONNECTIONS.isOn()){
			flagUpdate = true;
			conLohOn = EngineSettings.G_DRAW_ALL_CONNECTIONS.isOn();
		}
		
		
		
		if (flagUpdate) {
			updateConnections();
			buildMesh();
			flagUpdate = false;
		}
		
		
		if(wasUsingPoly != EngineSettings.USE_POLY_SCALING_ENERGY_STREAM.isOn()){
			flagUpdate = true;
			wasUsingPoly = EngineSettings.USE_POLY_SCALING_ENERGY_STREAM.isOn();
			return;
		}
		if (!noDraw && stabilizerPaths.size() > 0) {
			
			
			
			
			
			GlUtil.glPushMatrix();

			GlUtil.glMultMatrix(c.getWorldTransformOnClient());

			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.printGLState();
			material.attach(0);

			
			boolean hit = false;
			for(StabilizerPath e : stabilizerPaths){
				if(e.isHit()){
					hit = true;
					break;
				}
			}
			if(hit){
				color.set(1,0,0,1);
			}else{
				color.set(1,1,1,1);
			}
			GlUtil.glColor4f(color);
			// Enable Pointers
			// Enable Vertex Arrays
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoordsIndex);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glTexCoordPointer(3, GL11.GL_FLOAT, 0, 0);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormalIndex);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOIndex);

			GL11.glVertexPointer(4, GL11.GL_FLOAT, 0, 0);

			GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

			GlUtil.glPopMatrix();
		}

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	public void flagUpdate() {
		flagUpdate = true;
	}

	/**
	 * @return the c
	 */
	public SegmentController getSegmentController() {
		return c;
	}

	private static void configMatrix(Matrix3f m, Vector3f dir){
		forward.set(0, 0, 1);
		right.set(1, 0, 0);
		up.set(0, 1, 0);

		forward.set(dir);

		forward.normalize();
		if(forward.y > 0.999999){
			forward.set(0,1,0);
			up.set(0,0,1);
			right.set(1,0,0);
		}else if(forward.y < -0.999999){
			forward.set(0,-1,0);
			up.set(0,0,-1);
			right.set(-1,0,0);
		}else{
		
			up.cross(right, forward);
			if (up.lengthSquared() == 0) {
				//special case
				up.set(0, 1, 0);
			}
			up.normalize();
			right.cross(up, forward);
			if (right.lengthSquared() == 0) {
				//special case
				right.set(1, 0, 0);
			}
			right.normalize();
		}
		GlUtil.setRightVector(right, m);
		GlUtil.setUpVector(up, m);
		GlUtil.setForwardVector(forward, m);
	}
	

	public void updateConnections() {
		long time = System.currentTimeMillis();
		failed = -1;
		int checks = 0;
		
		
		assert(c instanceof ManagedSegmentController<?>):c;
		assert(!c.isUsingOldPower()):c;
		
		ManagerContainer<?> man = ((ManagedSegmentController<?>)c).getManagerContainer();

		stabilizerPaths.clear();
		stabilizerPaths.addAll(man.getPowerInterface().getStabilizerPaths());
		
		long took = System.currentTimeMillis() - time;
		if (took > 40) {
			System.err.println("[CLIENT] update connections took " + took + "ms; paths: " + stabilizerPaths.size() + "; checks: " + checks);
		}
		
		
	}

}
