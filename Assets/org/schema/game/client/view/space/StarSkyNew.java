package org.schema.game.client.view.space;

import api.listener.events.world.ProceduralSkyboxColorEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.common.util.BlendTool;
import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class StarSkyNew implements Drawable {

	private final GameClientState state;
	Vector3i currentSystem = new Vector3i();
	private int vBufferName;
	private int cBufferName;
	//	int starCount = 1024;//16384*2;
	private boolean firstDraw = true;
	private StarFieldShaderNew starFieldShader;
	private StarFieldFlareShader starFieldFlareShader;
	private StarSkyBackgroundShader backgroundShader;
	
	private FieldBackgroundShader fieldBackgroundShader;
	private BlendTool blendTool;
	private Integer starCount;
	private boolean drawFromPlanet;
	private float year;
	private Vector3i lastSystemPos = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	private FloatBuffer vBuffer;
	private FloatBuffer cBuffer;
	private int fieldTex;
	private int fieldTex2;

	public StarSkyNew(GameClientState state) {
		this.state = state;

	}

	public static Vector3f randomVector(float length) {
		float theta = (FastMath.TWO_PI * (float) Math.random());//random range is 0.0 to 1.0
		float phi = FastMath.acos(2.0f * (float) Math.random() - 1.0f) * FastMath.acos(2.0f * (float) Math.random() - 1.0f);
		float rho = length;
		Vector3f store = new Vector3f();
		store = FastMath.sphericalToCartesian(new Vector3f(rho, theta, phi), store);
		return store;
	}

	public static void randomVector2(int starCount, ObjectArrayList<Vector3f> poses, FloatArrayList colors) {

		float incFac = FastMath.PI * (3.0f - FastMath.sqrt(5));

		float off = 2.0f / starCount;

		float inc = 0;

		for (int k = 0; k < starCount; k++) {
			double len = ((Math.random() + 1.0) / 2.0) * 10.0;
			if (Math.random() < 0.5) {
				len = -len;
			}

			Vector3f pos = new Vector3f();
			float y = k * off - 1.0f + (off / 2.0f);

			float r = FastMath.sqrt((1.0f - y * y));

			float phi = inc;

			inc += (0.5f + Math.random()) * incFac;
			//		System.err.println("SPHERE PIC: "+FastMath.cos(phi) * r+", "+ y+", "+ FastMath.sin(phi) * r);
			pos.set(FastMath.cos(phi) * r, Math.random() > 0.5 ? y / 5.5f : y, FastMath.sinFast(phi) * r);

			pos.normalize();
			pos.scale((float) len);

			poses.add(pos);

			colors.add(0);//white in color map
		}
	}

	public static void galaxyVector(Galaxy galaxy, Vector3f ownPosition, ObjectArrayList<Vector3f> poses, FloatArrayList colors) {

//		Vector3f[] rays = new Vector3f[galaxy.getNumberOfStars()];

		galaxy.getPositions(poses, colors);

		Random r = new Random();
		r.setSeed(galaxy.getSeed());
		for (int k = 0; k < poses.size(); k++) {

			poses.get(k).x += ((r.nextFloat() * 2f) - 1f) * 0.2f;
			poses.get(k).y += ((r.nextFloat() * 2f) - 1f) * 0.2f;
			poses.get(k).z += ((r.nextFloat() * 2f) - 1f) * 0.2f;

			poses.get(k).sub(ownPosition);
		}
	}

	// ignores the higher 16 bits
	public static float toFloat(int hbits) {
		int mant = hbits & 0x03ff;            // 10 bits mantissa
		int exp = hbits & 0x7c00;            // 5 bits exponent
		if (exp == 0x7c00) {
			exp = 0x3fc00;                    // -> NaN/Inf
		} else if (exp != 0)                   // normalized value
		{
			exp += 0x1c000;                   // exp - 15 + 127
			if (mant == 0 && exp > 0x1c400) {
				return Float.intBitsToFloat((hbits & 0x8000) << 16
						| exp << 13 | 0x3ff);
			}
		} else if (mant != 0)                  // && exp==0 -> subnormal
		{
			exp = 0x1c400;                    // make it normal
			do {
				mant <<= 1;                   // mantissa * 2
				exp -= 0x400;                 // decrease exp by 1
			} while ((mant & 0x400) == 0); // while not normal
			mant &= 0x3ff;                    // discard subnormal bit
		}                                     // else +/-0 -> +/-0
		return Float.intBitsToFloat(          // combine all parts
				(hbits & 0x8000) << 16          // sign  << ( 31 - 15 )
						| (exp | mant) << 13);         // value << ( 23 - 10 )
	}

	@Override
	public void cleanUp() {
		GL11.glDeleteTextures(fieldTex);
		GL11.glDeleteTextures(fieldTex2);
	}

	@Override
	public void draw() {
		//		if(!EngineSettings.G_DRAW_BACKGOUND.isOn()){
		//			return;
		//		}
		if (firstDraw ) {
			onInit();
		}

		//		planetDrawer.draw();
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPushMatrix();

		float aspect = (float) GLFrame.getWidth() / (float) GLFrame.getHeight(); //1.333333333333333333333333f
		//do not zoom for background
		if (!EngineSettings.O_OCULUS_RENDERING.isOn()) {
			GlUtil.gluPerspective(Controller.projectionMatrix, EngineSettings.G_FOV.getFloat(), aspect, 1f, 10000, true);
		} else {
			Matrix4f gluPerspective = GlUtil.gluPerspective(Controller.projectionMatrix,
					EngineSettings.G_FOV.getFloat(),
					OculusVrHelper.getAspectRatio(),
					1f, 10000, true);

			Matrix4fTools.mul(Controller.occulusProjMatrix, gluPerspective, gluPerspective);

			GlUtil.glLoadMatrix(gluPerspective);
		}
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPushMatrix();

		try {
			drawBackground();
		} catch (GLException e) {
			e.printStackTrace();
		}

		drawStars(false);
		drawStars(true);

		GlUtil.glPopMatrix();

		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
	}

	@Override
	public boolean isInvisible() {

		return false;
	}

	@Override
	public void onInit() {
		
		starCount = EngineSettings.G_STAR_COUNT.getInt();
		starFieldShader = new StarFieldShaderNew();
		starFieldFlareShader = new StarFieldFlareShader();
		backgroundShader = new StarSkyBackgroundShader();
		fieldBackgroundShader = new FieldBackgroundShader();

		if (starCount <= 0) {
			EngineSettings.G_DRAW_STARS.setOn(false);
			firstDraw = false;
			return;
		}
		//		this.blendTool = new BlendTool();

		reloadStars();

		firstDraw = false;
	}

	public void createField() throws GLException {

		if (EngineSettings.G_PROD_BG.isOn() && state.getCurrentRemoteSector() != null) {
			Vector3i current = new Vector3i(state.getCurrentRemoteSector().clientPos());
			Vector3i currentSystem = VoidSystem.getContainingSystem(current, new Vector3i());

			if (fieldBackgroundShader.isRecompiled() || !currentSystem.equals(lastSystemPos)) {

				fieldBackgroundShader.needsFieldUpdate = true;
			}

			if (fieldBackgroundShader.needsFieldUpdate) {
				long t = System.currentTimeMillis();
				GlUtil.glPushMatrix();

				GlUtil.glLoadIdentity();
				assert (GlUtil.loadedShader == null);
				System.err.println("[BG] updating field");
				lastSystemPos.set(currentSystem);
				Random r = new Random(state.getGameState().getUniverseSeed() + currentSystem.code());
				fieldBackgroundShader.seed = r.nextFloat() * 10000f;

				fieldBackgroundShader.color.x = r.nextFloat();
				fieldBackgroundShader.color.y = r.nextFloat();
				fieldBackgroundShader.color.z = r.nextFloat();
				fieldBackgroundShader.color.w = r.nextFloat();

				fieldBackgroundShader.color2.x = r.nextFloat() * 2.0f;
				fieldBackgroundShader.color2.y = r.nextFloat() * 2.0f;
				fieldBackgroundShader.color2.z = r.nextFloat() * 2.0f;
				fieldBackgroundShader.color2.w = r.nextFloat();

				fieldBackgroundShader.rotSecA = r.nextFloat();
				fieldBackgroundShader.rotSecB = r.nextFloat();
				fieldBackgroundShader.rotSecC = r.nextFloat();

				//INSERTED CODE
				ProceduralSkyboxColorEvent proceduralSkyboxColorEvent = new ProceduralSkyboxColorEvent(
						this.lastSystemPos,
						this.state.getCurrentGalaxy().getSystemTypeAt(this.lastSystemPos),
						this.fieldBackgroundShader.color,this.fieldBackgroundShader.color2
				);
				StarLoader.fireEvent(ProceduralSkyboxColorEvent.class, proceduralSkyboxColorEvent,false);

				this.fieldBackgroundShader.color = proceduralSkyboxColorEvent.getColor1();
				this.fieldBackgroundShader.color2 = proceduralSkyboxColorEvent.getColor2();
				///

				fieldBackgroundShader.resolution = EngineSettings.G_PROD_BG_QUALITY.getInt();
				FrameBufferObjects fieldFbo;
				FrameBufferObjects fieldFbo2;
				try {
					if(fieldTex != 0){
						GL11.glDeleteTextures(fieldTex);
						GL11.glDeleteTextures(fieldTex2);
					}
					System.err.println("[STARFIELD] RECRATING WITH RESOLUTION: "+fieldBackgroundShader.resolution);
					fieldFbo = new FrameBufferObjects("FieldFBO0", fieldBackgroundShader.resolution, fieldBackgroundShader.resolution);
					fieldFbo.dstPixelFormat = GL11.GL_RGB;
					fieldFbo.initialize();
						
						
					fieldFbo2 = new FrameBufferObjects("FieldFBO1", fieldBackgroundShader.resolution, fieldBackgroundShader.resolution);
					fieldFbo2.dstPixelFormat = GL11.GL_RGBA;
					fieldFbo2.initialize();
				} catch (Exception e) {
					EngineSettings.G_PROD_BG.setOn(false);
					e.printStackTrace();
					return;
				}

				
				createFor(fieldFbo);
				fieldBackgroundShader.seed = fieldBackgroundShader.seed + 1000;
				createFor(fieldFbo2);

				GlUtil.glPopMatrix();
				System.err.println("[CLIENT][BACKGROUND] generating background took: " + (System.currentTimeMillis() - t) + "ms");
				
				fieldTex = fieldFbo.getTextureID();
				fieldTex2 = fieldFbo2.getTextureID();
				fieldFbo.cleanUp(true, false);
				fieldFbo = null;
				
				fieldFbo2.cleanUp(true, false);
				fieldFbo2 = null;
				
				
			}

		}

	}

	private void createFor(FrameBufferObjects fbo) {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		fbo.enable();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_CULL_FACE);

		Mesh sphere = ((Mesh) Controller.getResLoader().getMesh("Sphere").getChilds().get(0));
		sphere.loadVBO(true);

		assert (GlUtil.loadedShader == null);
		ShaderLibrary.fieldShader.setShaderInterface(fieldBackgroundShader);
		ShaderLibrary.fieldShader.load();
		sphere.renderVBO();
		ShaderLibrary.fieldShader.unload();

		fieldBackgroundShader.needsFieldUpdate = false;
		sphere.unloadVBO(true);

		fbo.disable();
	}

	public void drawBackground() throws GLException {
		if (!EngineSettings.G_DRAW_BACKGROUND.isOn()) {
			return;
		}
		if (firstDraw) {
			onInit();
		}
		Vector3f cPos = Controller.getCamera().getPos();
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_CULL_FACE);

		GlUtil.glPushMatrix();
		Mesh mesh = (Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0);

		GlUtil.translateModelview(cPos.x, cPos.y, cPos.z);

		GlUtil.scaleModelview(10, 10, 10);

		if (drawFromPlanet) {
			Transform t = new Transform();
			t.setIdentity();
			t.origin.set(0, 0, 0);
			Matrix3f rot = new Matrix3f();
			rot.rotX((FastMath.PI * 2) * year);
			rot.invert();

			TransformTools.rotateAroundPoint(new Vector3f(), rot, t, new Transform());
			GlUtil.glMultMatrix(t);
		}

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		if (!EngineSettings.G_PROD_BG.isOn()) {

			GlUtil.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, backgroundShader.getCubeMapTexture().getTextureId());
			ShaderLibrary.cubemapShader.setShaderInterface(backgroundShader);
			ShaderLibrary.cubemapShader.load();

			mesh.drawVBO();

			ShaderLibrary.cubemapShader.unload();
			GlUtil.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);
		}

		assert (fieldBackgroundShader != null);
		assert (ShaderLibrary.fieldShader != null);

		if (state.getCurrentRemoteSector() != null) {
			Vector3i current = new Vector3i(state.getCurrentRemoteSector().clientPos());
			Vector3i center = new Vector3i(lastSystemPos);
			center.scale(VoidSystem.SYSTEM_SIZE);
			center.add(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);

			center.sub(current);
			Vector3f realCenterPos = new Vector3f(
					center.x * ((GameStateInterface) state).getSectorSize(),
					center.y * ((GameStateInterface) state).getSectorSize(),
					center.z * ((GameStateInterface) state).getSectorSize());

			Vector3f min = new Vector3f();
			Vector3f max = new Vector3f();
			Vector3f size = new Vector3f(7 * ((GameStateInterface) state).getSectorSize(), 7 * ((GameStateInterface) state).getSectorSize(), 7 * ((GameStateInterface) state).getSectorSize());
			min.sub(size);
			max.add(size);
			if (current.x < 0) {
				min.x += ((GameStateInterface) state).getSectorSize();
				max.x += ((GameStateInterface) state).getSectorSize();
			}
			if (current.y < 0) {
				min.y += ((GameStateInterface) state).getSectorSize();
				max.y += ((GameStateInterface) state).getSectorSize();
			}
			if (current.z < 0) {
				min.z += ((GameStateInterface) state).getSectorSize();
				max.z += ((GameStateInterface) state).getSectorSize();
			}

			BoundingBox bb = new BoundingBox(min, max);

			realCenterPos.sub(Controller.getCamera().getWorldTransform().origin);

			Vector3f closestPoint = bb.getClosestPoint(realCenterPos, new Vector3f());

			//			System.err.println("Cloasest "+closestPoint+" :::: "+center+";;;; "+min+"; "+max);

			closestPoint.sub(realCenterPos);

			float alpha = 1.0f;
			float alphaBef = fieldBackgroundShader.color.w;

			if (!bb.isInside(realCenterPos)) {
				if (closestPoint.length() < 2000f) {
					alpha = 1.0f - closestPoint.length() / 2000f;
				} else {
					alpha = 0;
				}
			}
			//		System.err.println("ALPHA: "+alpha);
			//		System.err.println("CLOSEST "+realCenterPos+"; "+closestPoint.length()+"; "+bb.isInside(realCenterPos)+"; BB "+bb);

			if (fieldBackgroundShader.color.w > 0 && EngineSettings.G_PROD_BG.isOn()) {
				//this will only have an effect when fbo draw is off
				//otherwise this will have been called before
				createField();

				GlUtil.scaleModelview(2, 2, 2);

				ShaderLibrary.bgShader.loadWithoutUpdate();
				GlUtil.updateShaderInt(ShaderLibrary.bgShader, "tex", 0);
				GlUtil.updateShaderFloat(ShaderLibrary.bgShader, "alpha", alpha);

				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//				if(Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)){
				GlUtil.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//				}else{
				//					GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//				}
				Mesh sphere = ((Mesh) Controller.getResLoader().getMesh("Sphere").getChilds().get(0));

				sphere.loadVBO(true);
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fieldTex);

				GlUtil.glPushMatrix();
				GlUtil.rotateModelview(fieldBackgroundShader.rotSecA * 360, 0, 1, 0);
				sphere.renderVBO();

				GlUtil.glPushMatrix();
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fieldTex2);
				GlUtil.rotateModelview(90 + fieldBackgroundShader.rotSecB * 180, 1, 0, 0);
				GlUtil.rotateModelview(70 + fieldBackgroundShader.rotSecB * 180, 0, 0, 1);
				sphere.renderVBO();
				GlUtil.glPopMatrix();
				GlUtil.glPopMatrix();
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

				sphere.unloadVBO(true);

				ShaderLibrary.bgShader.unloadWithoutExit();
			}
			fieldBackgroundShader.color.w = alphaBef;
		}

		GlUtil.glEnable(GL11.GL_CULL_FACE);

		GlUtil.glPopMatrix();
	}

	public void drawStars(boolean rotated) {
		if (rotated) {
			return;
		}
		if (!EngineSettings.G_DRAW_STARS.isOn()) {
			return;
		}
		if (firstDraw) {
			onInit();
		}
		if (vBufferName == 0) {
			return;
		}

		if (!currentSystem.equals(state.getPlayer().getCurrentSystem())) {
			System.err.println("[CLIENT] stars recreating from changed system; last " + currentSystem + "; new " + state.getPlayer().getCurrentSystem());
			reloadStars();
		}

		GlUtil.glPushMatrix();
		Vector3f cPos = Controller.getCamera().getPos();
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glTranslatef(cPos.x, cPos.y, cPos.z);

		//		GlUtil.glEnable(GL11.GL_TEXTURE_1D);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		//		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		// Enable Pointers
		// Enable Vertex Arrays
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_1D, starFieldShader.getTexture().getTextureId());
		//		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		//		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		//		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL12.GL_TEXTURE_WRAP_R, GL11.GL_CLAMP);
		GlUtil.glEnable(GL11.GL_BLEND);
		//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		if (EngineSettings.F_FRAME_BUFFER.isOn() || EngineSettings.F_BLOOM.isOn()) {
			GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			//			GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		// Enable Pointers
		// Enable Vertex Arrays
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vBufferName);
		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

		// Enable Normal Arrays
		GlUtil.glEnableClientState(GL11.GL_COLOR_ARRAY);
		// Bind Buffer to the Normal Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, cBufferName);
		// Set The Normal Pointer To The TexCoord Buffer
		GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0);

		if (drawFromPlanet) {
			Transform t = new Transform();
			t.setIdentity();
			t.origin.set(0, 0, 0);
			Matrix3f rot = new Matrix3f();
			rot.rotX((FastMath.PI * 2) * year);
			rot.invert();

			TransformTools.rotateAroundPoint(new Vector3f(), rot, t, new Transform());
			GlUtil.glMultMatrix(t);
		}
		if (rotated) {
			GL11.glRotatef(90, 0, 0, 1);
		}

		// Render
		GlUtil.glDisable(GL11.GL_CULL_FACE);

		ShaderLibrary.starShader.setShaderInterface(starFieldShader);
		ShaderLibrary.starShader.load();
		GL11.glDrawArrays(GL11.GL_QUADS, 0, starCount * 4);
		ShaderLibrary.starShader.unload();

//		ShaderLibrary.starFlareShader.setShaderInterface(starFieldFlareShader);
//		ShaderLibrary.starFlareShader.load();
//		GL11.glDrawArrays(GL11.GL_QUADS, 0, starCount*4);
//		ShaderLibrary.starFlareShader.unload();

		GlUtil.glEnable(GL11.GL_CULL_FACE);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_COLOR_ARRAY);

		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glPopMatrix();
		GlUtil.glBindTexture(GL11.GL_TEXTURE_1D, 0);
		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_BLEND);

	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

	private void loadBillboardStars() {

		float[] tCoords = new float[]{0, 0.50f, 0.75f, 0.25f};

//		assert(useGalaxy);

		currentSystem.set(state.getPlayer().getCurrentSystem());
		System.err.println("[CLIENT] Stars: NEW SYSTEM: " + state.getPlayer().getCurrentSystem());
		Vector3i relativeSystemPos = Galaxy.getLocalCoordinatesFromSystem(state.getPlayer().getCurrentSystem(), new Vector3i());

		Vector3f systemF = new Vector3f(relativeSystemPos.x, relativeSystemPos.y, relativeSystemPos.z);
		ObjectArrayList<Vector3f> poses;
		FloatArrayList colors;
			poses = new ObjectArrayList<Vector3f>(state.getCurrentGalaxy().getNumberOfStars());
			colors = new FloatArrayList(state.getCurrentGalaxy().getNumberOfStars());
			galaxyVector(state.getCurrentGalaxy(), systemF, poses, colors);
			starCount = poses.size();

		ByteBuffer vD = GlUtil.getDynamicByteBuffer(starCount * 3 * 4 * ByteUtil.SIZEOF_FLOAT, 0);
		ByteBuffer cD = GlUtil.getDynamicByteBuffer(starCount * 3 * 4 * ByteUtil.SIZEOF_FLOAT, 1);
		vBuffer = vD.asFloatBuffer();
		cBuffer = cD.asFloatBuffer();
		Random r = new Random();
		r.setSeed(state.getCurrentGalaxy().getSeed());
		for (int i = 0; i < poses.size(); i++) {

			Vector3f pos = poses.get(i);
			for (int p = 0; p < 4; p++) {
				vBuffer.put(pos.x + 0.5f); //normalize for middle of system
				vBuffer.put(pos.y + 0.5f);
				vBuffer.put(pos.z + 0.5f);

				// Color index, vertex id, Luminosity
//				cBuffer.put(colors.getFloat(i));
				cBuffer.put(colors.getFloat(i));
				cBuffer.put(tCoords[p]);
				cBuffer.put(0.4f + r.nextFloat() * 1.0f);//(float) Math.random() *
			}
		}

	}

	public void reloadStars() {

		System.err.println("[CLIENT][STARSKY] reloading stars");

		starCount = state.getCurrentGalaxy().getNumberOfStars();

		loadBillboardStars();

		if (vBuffer.position() <= 0) {
			EngineSettings.G_DRAW_STARS.setOn(false);
			firstDraw = false;
			return;
		}
		if (vBufferName != 0) {
			GL15.glDeleteBuffers(vBufferName);
		}
		vBufferName = GL15.glGenBuffers(); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vBufferName); // Bind
		vBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuffer, GL15.GL_STATIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
		Controller.loadedVBOBuffers.add(vBufferName);

		if (cBufferName != 0) {
			GL15.glDeleteBuffers(cBufferName);
		}
		cBufferName = GL15.glGenBuffers(); // Get A Valid Name

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, cBufferName); // Bind
		cBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, cBuffer, GL15.GL_STATIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		Controller.loadedVBOBuffers.add(cBufferName);

	}

	public void update(Timer timer) {
		if (blendTool != null) {
			blendTool.checkKeyboard();
		}
		fieldBackgroundShader.time += timer.getDelta();
		if (starFieldShader != null) {
			starFieldShader.update(timer);
		}
	}

	/**
	 * @return the drawFromPlanet
	 */
	public boolean isDrawFromPlanet() {
		return drawFromPlanet;
	}

	/**
	 * @param drawFromPlanet the drawFromPlanet to set
	 */
	public void setDrawFromPlanet(boolean drawFromPlanet) {
		this.drawFromPlanet = drawFromPlanet;
	}

	/**
	 * @return the year
	 */
	public float getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(float year) {
		this.year = year;
	}

	/**
	 * @return the fieldBackgroundShader
	 */
	public FieldBackgroundShader getFieldBackgroundShader() {
		return fieldBackgroundShader;
	}

	public void reset() {
		onInit();
		if(fieldBackgroundShader != null){
			fieldBackgroundShader.needsFieldUpdate = true;
		}
	}

	

}
