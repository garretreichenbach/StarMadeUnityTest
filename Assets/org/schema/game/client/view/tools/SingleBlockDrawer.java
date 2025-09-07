package org.schema.game.client.view.tools;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.cubes.CubeInfo;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.lodshapes.LodDraw;
import org.schema.game.client.view.cubes.shapes.*;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Light;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;

public class SingleBlockDrawer {

	private static final MemoryManager man = new MemoryManager();
	private static final MemoryManager.ManagedMemoryChunk fBuffer = GraphicsContext.INTEGER_VERTICES ? man.intArray(CubeMeshBufferContainer.vertexComponents * 7 * CubeMeshBufferContainer.VERTS_PER_FACE) : man.floatArray(CubeMeshBufferContainer.vertexComponents * 7 * CubeMeshBufferContainer.VERTS_PER_FACE);
	private static final IntOpenHashSet alreadyDrawn = new IntOpenHashSet();
	private static final BlockRenderInfo ri = new BlockRenderInfo();
	public static int timesR;
	private static int bufferId;
	private static boolean wasDownR;
	private static int timesL;
	private static boolean wasDownL;

	static {
		man.allocateMemory();
	}

	public float alpha = 1;
	public boolean useSpriteIcons = true;
	int t;
	Transform tmpTrns = new Transform();
	Quat4f quatTmp = new Quat4f();
	Matrix3f rotTmp = new Matrix3f();
	private byte shapeOrientation = 2;
	private byte sidedOrientation;
	private boolean blinkingOrientation;
	private final AlgorithmParameters p = new AlgorithmParameters();
	private boolean checkForError;
	private boolean active;
	private boolean lightAll = true;
	public boolean drawOutline = false;

	public static void blockLighting() {
		Light.tempBuffer.clear();
		Light.tempBuffer.put(1.0f);
		Light.tempBuffer.put(0.0f);
		Light.tempBuffer.put(0.0f);
		Light.tempBuffer.put(1.0f);
		Light.tempBuffer.rewind();
		GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, Light.tempBuffer);

		Light.tempBuffer.clear();
		Light.tempBuffer.put(1.0f);
		Light.tempBuffer.put(0.0f);
		Light.tempBuffer.put(0.0f);
		Light.tempBuffer.put(1.0f);
		Light.tempBuffer.rewind();
		GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, Light.tempBuffer);

		Light.tempBuffer.clear();
		Light.tempBuffer.put(1);
		Light.tempBuffer.put(1);
		Light.tempBuffer.put(1);
		Light.tempBuffer.put(1);
		Light.tempBuffer.rewind();
		GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, Light.tempBuffer);

		Light.tempBuffer.clear();
		Light.tempBuffer.put(1);
		Light.tempBuffer.put(1);
		Light.tempBuffer.put(-10);
		Light.tempBuffer.put(1.0f);
		Light.tempBuffer.rewind();
		GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, Light.tempBuffer);


		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_LIGHT0);

	}

	public void activateBlinkingOrientation(boolean b) {
		blinkingOrientation = b;
	}

	private void draw() {

		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		Shader cubeShader;
		if(lightAll) {
			cubeShader = ShaderLibrary.getCubeShader(ShaderLibrary.CubeShaderType.SINGLE_DRAW.bit | ShaderLibrary.CubeShaderType.BLENDED.bit | ShaderLibrary.CubeShaderType.LIGHT_ALL.bit);
		} else {
			cubeShader = ShaderLibrary.getCubeShader(ShaderLibrary.CubeShaderType.SINGLE_DRAW.bit | ShaderLibrary.CubeShaderType.BLENDED.bit); //| CubeShaderType.LIGHT_ALL.bit
		}
		cubeShader.setShaderInterface(SegmentDrawer.shader);
		cubeShader.load();
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(cubeShader, "extraAlpha", alpha);
		Vector3f viewPos = new Vector3f(Controller.getCamera().getPos());
		Vector3f lightPos = new Vector3f(AbstractScene.mainLight.getPos());

		viewPos.set(0, -1, 0);
		lightPos.set(1, 1, 0);
		GlUtil.updateShaderVector3f(cubeShader, "viewPos", viewPos);
		GlUtil.updateShaderVector3f(cubeShader, "lightPos", lightPos);

		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}


		int vertices = fBuffer.limit() / CubeMeshBufferContainer.vertexComponents;

		GL11.glDrawArrays(CubeMeshBufferContainer.DRAW_STYLE, 0, vertices);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		// Draw outline if enabled
		if(drawOutline) {
			GlUtil.glEnable(GL11.GL_LINE_SMOOTH);
			GlUtil.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
			GL11.glPolygonOffset(1.0f, 1.0f);
			GL11.glLineWidth(1.0f);
			GlUtil.glColor4f(0, 0, 0, 1);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			if(checkForError) {
				GlUtil.printGlErrorCritical();
			}
			GL11.glDrawArrays(CubeMeshBufferContainer.DRAW_STYLE, 0, vertices);
			// Restore polygon mode
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.updateShaderFloat(GlUtil.loadedShader, "extraAlpha", 0.0f);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.loadedShader.unload();
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.glDisable(GL11.GL_BLEND);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
	}

	public void drawType(short type, Transform t) {

		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			if(type > 0) {
				ElementInformation info = ElementKeyMap.getInfo(type);
				if(info.getBlockStyle().solidBlockStyle) {
					org.schema.game.common.data.physics.ConvexHullShapeExt shape = (org.schema.game.common.data.physics.ConvexHullShapeExt) BlockShapeAlgorithm.getShape(info.getBlockStyle(), shapeOrientation);
					for(int i = 0; i < shape.getNumVertices(); i++) {

						Vector3f vtx = new Vector3f();
						shape.getVertex(i, vtx);

						//						Matrix4f m = new Matrix4f();
						//						GlUtil.lwglMatrix4fToVecmathMatrix(Controller.modelviewMatrix, m);
						//						Transform t = new Transform(m);

						t.transform(vtx);

						DebugPoint p = new DebugPoint(vtx, new Vector4f(1, 1, 1, 1), 0.1f);
						DebugDrawer.points.add(p);
					}

				}
			}
		}
		drawType(type);
	}

	public void drawType(short type) {
		checkForError = false;
		int drawnCode = shapeOrientation * 10000000 + sidedOrientation * 10000 + type;
		if(!alreadyDrawn.contains(drawnCode)) {
//			System.err.println("DRAWING DEBUG: " + drawnCode + "; " + "; " + getSidedOrientation() + "; " + getShapeOrientation() + "; " + type + "; ");
			alreadyDrawn.add(drawnCode);
			checkForError = true;
		}

		ElementInformation info = ElementKeyMap.getInfoFast(type);
		if(info.hasLod() && useSpriteIcons) {

			Shader s = null;

			int lights = 4;
			FloatBuffer lightVecBuffer = GlUtil.getDynamicByteBuffer(lights * 3 * 4, 0).asFloatBuffer();
			FloatBuffer lightDiffuseBuffer = GlUtil.getDynamicByteBuffer(lights * 4 * 4, 1).asFloatBuffer();
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);

			LodDraw c = new LodDraw();
			c.lightingAndPos = new float[7 * 4];

			for(int i = 0; i < 4; i++) {
				c.lightingAndPos[i * 7] = 0;
				c.lightingAndPos[i * 7 + 1] = 0;
				c.lightingAndPos[i * 7 + 2] = 0;
				c.lightingAndPos[i * 7 + 3] = 2.99f;


				Vector3i d = Element.DIRECTIONSi[i];
				c.lightingAndPos[i * 7 + 4] = d.x;
				c.lightingAndPos[i * 7 + 5] = d.y;
				c.lightingAndPos[i * 7 + 6] = d.z;

			}


			c.pointer = 0;
			c.type = type;
			c.mesh = ElementKeyMap.getInfoFast(c.type).getModel(0, false);

			short orientation = shapeOrientation;


			Oriencube oc = (Oriencube) BlockShapeAlgorithm.algorithms[5][ElementKeyMap.getInfoFast(c.type).blockStyle == BlockStyle.SPRITE ? (orientation % 6) * 4 : orientation];
//			System.err.println("ORIENTATION:::: "+orientation+" -> "+(ElementKeyMap.getInfoFast(c.type).blockStyle == BlockStyle.SPRITE ? (orientation%6)*4 : orientation)+" -> "+Element.getSideString(oc.getOrientCubePrimaryOrientation())+"\n"+oc.getBasicTransform().basis);

//			if(info.getId() == 104 ){
//				//mushroom
//				int o = orientation%6;
//				oc = Oriencube.getOrientcube(
//						o, o > 1 ? Element.FRONT : Element.TOP);
//			}
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
				System.err.println("SHAPEALGO: " + Element.getSideString(oc.getOrientCubePrimaryOrientation()) + "; prim " + Element.getSideString(oc.getOrientCubePrimaryOrientation()) + "; sec " + Element.getSideString(oc.getOrientCubeSecondaryOrientation()));
			}


			c.transform.set(oc.getBasicTransform());
			c.transform.origin.set(0, 0, 0);


			quatTmp.set(0, 0, 0, 1);
			quatTmp.set(c.mesh.getInitialQuadRot());


			tmpTrns.basis.setIdentity();
			tmpTrns.basis.set(quatTmp);

			Matrix3f tmpTransBas = new Matrix3f(tmpTrns.basis);


//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN)) {
//				if(!wasDownL) {
//					timesL++;
//					System.err.println("TIMES L::: "+timesL);
//					wasDownL = true;
//				}
//			}else {
//				wasDownL = false;
//			}
//			rot.setIdentity();
//			rot.rotZ((float)timesL*(FastMath.PI/2f));
//			tmpTrns.basis.mul(rot);
//
//
//
//
//
//			//FIXME: REMOVE THIS ONCE ALL THE SHIT SHAPES ARE GONE
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_PAGE_UP)) {
//				if(!wasDownR) {
//					timesR++;
//					System.err.println("TIMES R::: "+timesR);
//					wasDownR = true;
//				}
//			}else {
//				wasDownR = false;
//			}
			//FIXME: REMOVE THIS ONCE ALL THE SHIT SHAPES ARE GONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			if(info.getBlockStyle() == BlockStyle.SPRITE) {
				//rotate sprite shape LOD because their inital rot is fucked up
				rotTmp.setIdentity();
				rotTmp.rotX(timesR * (FastMath.PI / 2.0f));
				tmpTrns.basis.mul(rotTmp);
			}


//			System.err.println("LLL: "+timesL+"; "+timesR);

//			System.err.println("TMP TR: "+c.mesh.getInitialQuadRot()+"\n"+tmpTrns.basis);
			tmpTrns.origin.set(c.mesh.getInitionPos());

			Matrix3f tmTbef = new Matrix3f(c.transform.basis);
			Matrix4fTools.transformMul(c.transform, tmpTrns);

//			System.err.println("BASIS::: R "+timesR+"; L "+timesL+" "+c.mesh.getInitialQuadRot()+"\n"+c.transform.basis+"\n"+tmpTrns.basis);

			Mesh mesh = c.mesh;

			if(!mesh.getMaterial().isMaterialBumpMapped()) {
				if(s != ShaderLibrary.lodCubeShaderNormalOff) {
					s = ShaderLibrary.lodCubeShaderNormalOff;
					s.loadWithoutUpdate();
				}
			} else {
				if(!mesh.hasTangents && (s == null || s == ShaderLibrary.lodCubeShaderNormalOff || s == ShaderLibrary.lodCubeShaderTangent)) {
					s = ShaderLibrary.lodCubeShader;
					s.loadWithoutUpdate();
				}
				if(mesh.hasTangents && (s == null || s == ShaderLibrary.lodCubeShaderNormalOff || s == ShaderLibrary.lodCubeShaderTangent)) {
					s = ShaderLibrary.lodCubeShaderTangent;
				 s.loadWithoutUpdate();
				}
			}

			mesh.loadVBO(true);

			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getTexture().getTextureId());


			GlUtil.updateShaderInt(s, "mainTex", 0);

			if(mesh.getMaterial().isMaterialBumpMapped()) {
				GlUtil.glActiveTexture(GL13.GL_TEXTURE1);

				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getNormalMap().getTextureId());

				GlUtil.updateShaderInt(s, "normalTex", 1);
			}

			if(mesh.getMaterial().getEmissiveTexture() != null) {

				GlUtil.glActiveTexture(GL13.GL_TEXTURE2);

				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getEmissiveTexture().getTextureId());

				GlUtil.updateShaderInt(s, "emissiveTex", 2);
				GlUtil.updateShaderBoolean(s, "emissiveOn", true);
			} else {
				GlUtil.updateShaderBoolean(s, "emissiveOn", false);
			}

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			GlUtil.glPushMatrix();

			GlUtil.glMultMatrix(c.transform);

			c.fillLightBuffers(lightVecBuffer, lightDiffuseBuffer);


			GlUtil.updateShaderFloats3(s, "lightVec", lightVecBuffer);
			GlUtil.updateShaderFloats4(s, "lightDiffuse", lightDiffuseBuffer);


			mesh.renderVBO();

			// Draw outline if enabled
			if(drawOutline) {
				GlUtil.glEnable(GL11.GL_LINE_SMOOTH);
				GlUtil.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
				GL11.glPolygonOffset(1.0f, 1.0f);
				GL11.glLineWidth(1.0f);
				GlUtil.glColor4f(0, 0, 0, 1);
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
				if(checkForError) {
					GlUtil.printGlErrorCritical();
				}
				mesh.renderVBO();
				// Restore polygon mode
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}

			GlUtil.glPopMatrix();
			mesh.unloadVBO(true);
			s.unloadWithoutExit();

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glDisable(GL11.GL_BLEND);
		} else {
			putCube(type);
			draw();
		}
		checkForError = false;
	}

	private float getMiddleIndex() {
		int x = 128;
		int y = 128;
		int z = 128;

		return z * 65536 + y * 256 + x;
	}

	private int getMiddleIndexI() {
		int x = 128;
		int y = 128;
		int z = 128;

		return z * 65536 + y * 256 + x;
	}

	/**
	 * @return the shapeOrientation
	 */
	public byte getShapeOrientation() {
		return shapeOrientation;
	}

	/**
	 * @param shapeOrientation the shapeOrientation to set
	 */
	public void setShapeOrientation24(byte shapeOrientation) {
		this.shapeOrientation = shapeOrientation;
	}

	/**
	 * @return the sidedOrientation
	 */
	public byte getSidedOrientation() {
		return sidedOrientation;
	}

	/**
	 * @param sidedOrientation the sidedOrientation to set
	 */
	public void setSidedOrientation(byte sidedOrientation) {
		this.sidedOrientation = sidedOrientation;
	}

	private void putCube(short type) {
		if(checkForError) {
//			System.err.println("[SINGLE_BLOCK_DRAWER] CHECKING FOR ERROR");
			GlUtil.printGlErrorCritical();
		}

		fBuffer.clear();
		p.fresh = true;
		for(int sideId = 0; sideId < 6; sideId++) {
			byte r = 29;
			byte g = 29;
			byte b = 29;
			byte o = 8;
			int sid = sideId;
			if(sid == Element.LEFT || sid == Element.RIGHT) {
				//FIXME ugly opposite
				sid = Element.OPPOSITE_SIDE[sid];
			}
			if(blinkingOrientation && sidedOrientation == sid) {
				r = 29;
				g = 6;
				b = 5;

			}
			if(checkForError) {
				GlUtil.printGlErrorCritical();
			}
			if(GraphicsContext.INTEGER_VERTICES) {
				putIndex(type, sidedOrientation, sideId, ElementKeyMap.getInfo(type).getMaxHitPointsFull(), r, g, b, o, SegmentData.SEG_HALF + SegmentData.SEG_HALF * SegmentData.SEG + SegmentData.SEG_HALF * SegmentData.SEG_TIMES_SEG, (MemoryManager.MemIntArray) fBuffer);
			} else {
				putIndex(type, sidedOrientation, sideId, ElementKeyMap.getInfo(type).getMaxHitPointsFull(), r, g, b, o, SegmentData.SEG_HALF + SegmentData.SEG_HALF * SegmentData.SEG + SegmentData.SEG_HALF * SegmentData.SEG_TIMES_SEG, (MemoryManager.MemFloatArray) fBuffer);
			}
			if(checkForError) {
				GlUtil.printGlErrorCritical();
			}
		}
		//		System.err.println("SLKDJSLKJDLSJDLSKJLSKJLSKJDKLSJLSJLKJSLDKJSLDKJSLKDJSKLJDLSKJ "+this);
		if(fBuffer.position() != CubeMeshBufferContainer.vertexComponents * 6 * CubeMeshBufferContainer.VERTS_PER_FACE && fBuffer.position() != CubeMeshBufferContainer.vertexComponents * 7 * CubeMeshBufferContainer.VERTS_PER_FACE) {
			throw new IllegalArgumentException("BUFFER INVALID: " + fBuffer.position());
		}
		assert (fBuffer.position() == CubeMeshBufferContainer.vertexComponents * 6 * CubeMeshBufferContainer.VERTS_PER_FACE || fBuffer.position() == CubeMeshBufferContainer.vertexComponents * 7 * CubeMeshBufferContainer.VERTS_PER_FACE) : fBuffer.position();
		fBuffer.flip();
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		if(bufferId == 0) {
			bufferId = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(bufferId);

		}
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fBuffer.getByteBackingToCurrent(), GL15.GL_STATIC_DRAW);

		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
	}

	private void putIndex(short type, byte orientation, int sideId, int hitpoints, byte r, byte b, byte g, byte o, float index, MemoryManager.MemFloatArray dataBuffer) {
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		ElementInformation info = ElementKeyMap.getInfo(type);
		BlockStyle blockStyle = info.getBlockStyle();
		int individualSides = info.getIndividualSides();
		boolean animated = info.isAnimated();
		byte orientationCode = 0;

		byte shapeOrientation = this.shapeOrientation;


		if(blockStyle == BlockStyle.NORMAL24) { //normal block with 24 orientations
			orientationCode = CubeMeshBufferContainer.getOrientationCode24(sideId, shapeOrientation / 4);
		} else if(individualSides == 6) {
			orientation = (byte) Math.max(0, Math.min(5, orientation));
			assert (orientation < 6) : "Orientation wrong: " + orientation;
			orientationCode = CubeMeshBufferContainer.getOrientationCode6(sideId, orientation);
		} else if(individualSides == 3) {
			orientation = (byte) Math.max(0, Math.min(5, orientation));
			assert (orientation < 6) : "Orientation wrong: " + orientation;

			orientationCode = CubeMeshBufferContainer.getOrientationCode3(sideId, orientation);
		}

		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		float hpFac = (float) hitpoints / info.getMaxHitPointsFull();
		byte hitPointsCode = 0;

		if(hpFac < 1.0f) {
			hpFac = 1.0f - hpFac;
			hitPointsCode = FastMath.clamp((byte) (hpFac * 7), (byte) 0, (byte) 7);
		}
		//		assert(vis == (byte)1):"FUCK!!! "+vis+" of motherfucker "+data.getSegment().pos+"; "+data.getSegment().getSegmentController()+" TYPE: "+type;

		byte animatedCode = 0;
		if(info.hasLod()) {
			//LoD blocks have animated flag and the "only drawn in buildmode" flag to identify them in shader
			animatedCode = 1;
		}
//		byte layer = (byte)(Math.abs(elementInformation.getTextureId() + orientationCode) / 256);
//		short typeCode = (short)((elementInformation.getTextureId() + orientationCode)%256);

		byte layer = info.getTextureLayer(active, orientationCode);
		short typeCode = info.getTextureIndexLocal(active, orientationCode);


		if(info.getBlockStyle().solidBlockStyle || info.getBlockStyle() == BlockStyle.NORMAL24 || (info.getBlockStyle() == BlockStyle.SPRITE && !useSpriteIcons)) {
			//special block
			BlockShapeAlgorithm blockShapeAlgorithm = BlockShapeAlgorithm.algorithms[blockStyle.id - 1][shapeOrientation];

			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && blockShapeAlgorithm instanceof Oriencube oc) {
				System.err.println("SHAPEALGO: " + Element.getSideString(oc.getOrientCubePrimaryOrientation()) + "; " + Element.getSideString(oc.getOrientCubeSecondaryOrientation()));
			}


			if(info.hasLod() && info.lodShapeStyle == 1) {
				blockShapeAlgorithm = ((Oriencube) blockShapeAlgorithm).getSpriteAlgoRepresentitive();
			}
			int resIndex = 0;


			ri.sideId = sideId;
			ri.layer = layer;
			ri.typeCode = typeCode;
			ri.hitPointsCode = hitPointsCode;
			ri.animatedCode = animatedCode;
			ri.index = (int) index;
			ri.segIndex = getMiddleIndex();
			ri.halvedFactor = info.getSlab();
			ri.blockStyle = info.getBlockStyle();
			ri.orientation = orientation;
			ri.resOverlay = resIndex;
			ri.onlyInBuildMode = info.isDrawnOnlyInBuildMode();
			ri.extendedBlockTexture = info.isExtendedTexture();


			blockShapeAlgorithm.single(ri, r, g, b, o, dataBuffer, p);

		} else {

			//normal block

			int normalMode = 0;
			byte overlay = 0;

			if(CubeMeshBufferContainer.isTriangle()) {
				for(short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {

					short i = BlockShapeAlgorithm.vertexTriangleOrder[j];
					int[] halfBlockConfig = HalfBlockArray.getHalfBlockConfig(info.blockStyle, i, sideId, orientation, info.getSlab());

					float indexCode = ByteUtil.getCodeIndexF(index, r, g, b);
					byte tex;
					if(individualSides == 3) {
						//texcoord encoding
						tex = BlockShapeAlgorithm.texOrderMapNormal[0][sideId][i];
					} else {
						if(info.sideTexturesPointToOrientation) {
							//texcoord encoding
							tex = BlockShapeAlgorithm.texOrderMapPointToOrient[orientation % 6][sideId][i];
						} else {
							//texcoord encoding
							tex = BlockShapeAlgorithm.texOrderMapNormal[orientation % 6][sideId][i];
						}
					}

					byte mirror = 0;
					float code = ByteUtil.getCodeF((byte) sideId, layer, typeCode, hitPointsCode, animatedCode, tex, (byte) halfBlockConfig[2], info.isDrawnOnlyInBuildMode());
					code += BlockShapeAlgorithm.vertexOrderMap[sideId][i];
					float codeS = ByteUtil.getCodeS(normalMode, overlay, o, halfBlockConfig[0], halfBlockConfig[1]);
					dataBuffer.put(indexCode);
					dataBuffer.put(code);
					if(CubeMeshBufferContainer.vertexComponents > 2) {
						dataBuffer.put(getMiddleIndex());
						dataBuffer.put(codeS);
					}
					assert (index + i < CubeInfo.CUBE_SIDE_STRIDE) : "vert index is bigger: " + (index + i) + "/" + CubeInfo.CUBE_SIDE_STRIDE;
				}
			} else {
				for(short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
					int[] halfBlockConfig = HalfBlockArray.getHalfBlockConfig(info.blockStyle, i, sideId, orientation, info.getSlab());

					float indexCode = ByteUtil.getCodeIndexF(index, r, g, b);
					byte ext;
					if(individualSides == 3) {
						ext = BlockShapeAlgorithm.texOrderMapNormal[0][sideId][i];
					} else {
						if(info.sideTexturesPointToOrientation) {
							ext = BlockShapeAlgorithm.texOrderMapPointToOrient[orientation][sideId][i];
						} else {
							ext = BlockShapeAlgorithm.texOrderMapNormal[orientation][sideId][i];
						}
					}
					byte mirror = 0;
					float code = ByteUtil.getCodeF((byte) sideId, layer, typeCode, hitPointsCode, animatedCode, ext, (byte) halfBlockConfig[2], info.isDrawnOnlyInBuildMode());
					code += BlockShapeAlgorithm.vertexOrderMap[sideId][i];
					float codeS = ByteUtil.getCodeS(normalMode, overlay, o, halfBlockConfig[0], halfBlockConfig[1]);
					dataBuffer.put(indexCode);
					dataBuffer.put(code);
					if(CubeMeshBufferContainer.vertexComponents > 2) {
						dataBuffer.put(getMiddleIndex());
						dataBuffer.put(codeS);
					}
					assert (index + i < CubeInfo.CUBE_SIDE_STRIDE) : "vert index is bigger: " + (index + i) + "/" + CubeInfo.CUBE_SIDE_STRIDE;
				}
			}
		}
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
	}

	private void putIndex(short type, byte orientation, int sideId, int hitpoints, byte r, byte b, byte g, byte o, int index, MemoryManager.MemIntArray dataBuffer) {
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
		ElementInformation info = ElementKeyMap.getInfo(type);
		BlockStyle blockStyle = info.getBlockStyle();
		int individualSides = info.getIndividualSides();
		boolean animated = info.isAnimated();
		byte orientationCode = 0;

		byte shapeOrientation = this.shapeOrientation;

		if(blockStyle == BlockStyle.NORMAL24) { //normal block with 24 orientations
			orientationCode = CubeMeshBufferContainer.getOrientationCode24(sideId, shapeOrientation / 4);
		} else if(individualSides == 6) {
			orientation = (byte) Math.max(0, Math.min(5, orientation));
			assert (orientation < 6) : "Orientation wrong: " + orientation;
			orientationCode = CubeMeshBufferContainer.getOrientationCode6(sideId, orientation);
		} else if(individualSides == 3) {
			orientation = (byte) Math.max(0, Math.min(5, orientation));
			assert (orientation < 6) : "Orientation wrong: " + orientation;

			orientationCode = CubeMeshBufferContainer.getOrientationCode3(sideId, orientation);
		}

		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}

		float hpFac = (float) hitpoints / info.getMaxHitPointsFull();
		byte hitPointsCode = 0;

		if(hpFac < 1.0f) {
			hpFac = 1.0f - hpFac;
			hitPointsCode = FastMath.clamp((byte) (hpFac * 7), (byte) 0, (byte) 7);
		}
		//		assert(vis == (byte)1):"FUCK!!! "+vis+" of motherfucker "+data.getSegment().pos+"; "+data.getSegment().getSegmentController()+" TYPE: "+type;

		byte animatedCode = 0;
		if(info.hasLod()) {
			//LoD blocks have animated flag and the "only drawn in buildmode" flag to identify them in shader
			animatedCode = 1;
		}
		byte lDirX = 0;
		byte lDirY = 0;
		byte lDirZ = 0;
//		byte layer = (byte)(Math.abs(elementInformation.getTextureId() + orientationCode) / 256);
//		short typeCode = (short)((elementInformation.getTextureId() + orientationCode)%256);

		byte layer = info.getTextureLayer(active, orientationCode);
		short typeCode = info.getTextureIndexLocal(active, orientationCode);

		assert (info != null);
		assert (info.blockStyle != null);
		if(info.blockStyle.solidBlockStyle || info.getBlockStyle() == BlockStyle.NORMAL24 || (info.getBlockStyle() == BlockStyle.SPRITE && !useSpriteIcons)) {
			//special block
			BlockShapeAlgorithm blockShapeAlgorithm = BlockShapeAlgorithm.algorithms[blockStyle.id - 1][shapeOrientation];

			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && blockShapeAlgorithm instanceof Oriencube oc) {
				System.err.println("SHAPEALGO: " + Element.getSideString(oc.getOrientCubePrimaryOrientation()) + "; " + Element.getSideString(oc.getOrientCubeSecondaryOrientation()));
			}


			if(info.hasLod() && info.lodShapeStyle == 1) {
				blockShapeAlgorithm = blockShapeAlgorithm.getSpriteAlgoRepresentitive();
			}
			int resIndex = 0;

			ri.sideId = sideId;
			ri.layer = layer;
			ri.typeCode = typeCode;
			ri.hitPointsCode = hitPointsCode;
			ri.animatedCode = animatedCode;
			ri.index = index;
			ri.segIndex = getMiddleIndex();
			ri.halvedFactor = info.getSlab();
			ri.blockStyle = info.getBlockStyle();
			ri.orientation = orientation;
			ri.resOverlay = resIndex;
			ri.onlyInBuildMode = info.isDrawnOnlyInBuildMode();
			ri.extendedBlockTexture = info.isExtendedTexture();

			blockShapeAlgorithm.single(ri, r, g, b, o, dataBuffer, p);

		} else {

			//normal block

			int normalMode = 0;
			byte overlay = 0;

			if(CubeMeshBufferContainer.isTriangle()) {
				for(short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {

					short i = BlockShapeAlgorithm.vertexTriangleOrder[j];
					int[] halfBlockConfig = HalfBlockArray.getHalfBlockConfig(info.blockStyle, i, sideId, orientation, info.getSlab());

					int indexCode = ByteUtil.getCodeIndexI(index, r, g, b);
					byte ext;
					if(individualSides == 3) {
						ext = BlockShapeAlgorithm.texOrderMapNormal[0][sideId][i];
					} else {
						if(info.sideTexturesPointToOrientation) {
							ext = BlockShapeAlgorithm.texOrderMapPointToOrient[orientation % 6][sideId][i];
						} else {
							ext = BlockShapeAlgorithm.texOrderMapNormal[orientation % 6][sideId][i];
						}
					}

					byte mirror = 0;
					int code = ByteUtil.getCodeI((byte) sideId, layer, typeCode, hitPointsCode, animatedCode, ext, (byte) halfBlockConfig[2], info.isDrawnOnlyInBuildMode(), info.isExtendedTexture());
					code += BlockShapeAlgorithm.vertexOrderMap[sideId][i];
					int codeS = ByteUtil.getCodeSI(normalMode, overlay, o, lDirX, lDirY, lDirZ, halfBlockConfig[0], halfBlockConfig[1]);
					dataBuffer.put(indexCode);
					dataBuffer.put(code);
					if(CubeMeshBufferContainer.vertexComponents > 2) {
						dataBuffer.put(getMiddleIndexI());
						dataBuffer.put(codeS);
					}
					assert (index + i < CubeInfo.CUBE_SIDE_STRIDE) : "vert index is bigger: " + (index + i) + "/" + CubeInfo.CUBE_SIDE_STRIDE;
				}
			} else {
				for(short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
					int[] halfBlockConfig = HalfBlockArray.getHalfBlockConfig(info.blockStyle, i, sideId, orientation, info.getSlab());

					int indexCode = ByteUtil.getCodeIndexI(index, r, g, b);
					byte ext;
					if(individualSides == 3) {
						ext = BlockShapeAlgorithm.texOrderMapNormal[0][sideId][i];
					} else {
						if(info.sideTexturesPointToOrientation) {
							ext = BlockShapeAlgorithm.texOrderMapPointToOrient[orientation][sideId][i];
						} else {
							ext = BlockShapeAlgorithm.texOrderMapNormal[orientation][sideId][i];
						}
					}
					byte mirror = 0;
					float code = ByteUtil.getCodeF((byte) sideId, layer, typeCode, hitPointsCode, animatedCode, ext, (byte) halfBlockConfig[2], info.isDrawnOnlyInBuildMode());
					code += BlockShapeAlgorithm.vertexOrderMap[sideId][i];
					float codeS = ByteUtil.getCodeSI(normalMode, overlay, o, lDirX, lDirY, lDirZ, halfBlockConfig[0], halfBlockConfig[1]);
					dataBuffer.put(indexCode);
					dataBuffer.put((int) code);
					if(CubeMeshBufferContainer.vertexComponents > 2) {
						dataBuffer.put((int) getMiddleIndex());
						dataBuffer.put((int) codeS);
					}
					assert (index + i < CubeInfo.CUBE_SIDE_STRIDE) : "vert index is bigger: " + (index + i) + "/" + CubeInfo.CUBE_SIDE_STRIDE;
				}
			}
		}
		if(checkForError) {
			GlUtil.printGlErrorCritical();
		}
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	public void setLightAll(boolean lightAll) {
		this.lightAll = lightAll;
	}

	public void cleanUp() {

	}

}

