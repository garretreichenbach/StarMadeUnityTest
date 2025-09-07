package org.schema.game.client.view.effects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.SegmentOcclusion;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Skin;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.ShadowParams;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Shadow {
	public static final int MAX_SPLITS = 3;
	public static final int NUM_OBJECTS = 0;
	public static final float LIGHT_FOV = 45.0f;
//	private static final boolean PCF = true;
	private static final boolean USE_FIXED_FPS = false;

	public static boolean USE_VSM = true;
	public static SegmentOcclusion[] occlusions;
	static FloatBuffer buff = MemoryUtil.memAllocFloat(16);
	public static boolean creatingMap;
	public static int creatingMapIndex;

	//	int width = 1024;
	//	int height = 768;
	//	int getDepthSize() = 2048;
	//	int getCurNumberSplits() = 4;
	int show_depth_tex = 0;
	int shadow_type = 0;
	int depth_tex_ar;
	int color_tex_ar;
	int depth_fb, depth_rb;
	float split_weight = 0.88f;
	BoundingSphere obj_BSphere[] = new BoundingSphere[NUM_OBJECTS];
	Frustum f[] = new Frustum[MAX_SPLITS];
	float shad_cpm[][] = new float[MAX_SPLITS][16];
	float tmp[] = new float[16];
	float tmpFrustum[][] = new float[6][4];

	;
	private GameClientState state;
	private boolean firstCheck = true;
	private boolean debug = false;
	public Shadow(GameClientState state) {
		this.state = state;

		for (int i = 0; i < f.length; i++) {
			f[i] = new Frustum();
		}
		for (int i = 0; i < obj_BSphere.length; i++) {
			obj_BSphere[i] = new BoundingSphere();
		}
	}

	public boolean usePCF() {
		return (Boolean) EngineSettings.G_SHADOW_USE_PCF.isOn();
	}

	public int getDepthSize() {
		ShadowQuality a = (ShadowQuality) EngineSettings.G_SHADOW_QUALITY.getObject();
		return switch(a) {
			case OFF -> 0;
			case BAREBONE -> 512;
			case SIMPLE -> 1024;
			case BEST -> 2048;
			case ULTRA -> 4096;
		};
//		throw new RuntimeException("Unknown Shadow Quality");
	}

	public int getCurNumberSplits() {
//		ShadowQuality a = (ShadowQuality) EngineSettings.G_SHADOW_QUALITY.getObject();
		return 1;
		/*
		return switch(a) {
			case OFF -> 0;
			case BAREBONE -> 1;
			case SIMPLE -> 2;
			case BEST -> 3;
			case ULTRA -> 4;
		};

		 */
//		throw new RuntimeException("Unknown Shadow Quality");
	}

	// Compute the 8 corner points of the current view frustum
	void updateFrustumPoints(Frustum f, Vector3f center, Vector3f view_dir, Vector3f up) {
		Vector3f right = new Vector3f();
		right.cross(view_dir, up);

		Vector3f fc = new Vector3f();
		Vector3f sViewDir = new Vector3f(view_dir);
		sViewDir.scale(f.fard);
		fc.add(center, sViewDir);

		Vector3f nc = new Vector3f();
		Vector3f gViewDir = new Vector3f(view_dir);
		gViewDir.scale(f.neard);
		nc.add(center, gViewDir);

		right.normalize();

		up.cross(right, view_dir);
		up.normalize();

		// these heights and widths are half the heights and widths of
		// the near and far plane rectangles
		float near_height = FastMath.tan(f.fov * 0.5f) * f.neard;
		float near_width = near_height * f.ratio;
		float far_height = FastMath.tan(f.fov * 0.5f) * f.fard;
		float far_width = far_height * f.ratio;

		f.point[0].x = nc.x - up.x * near_height - right.x * near_width;
		f.point[1].x = nc.x + up.x * near_height - right.x * near_width;
		f.point[2].x = nc.x + up.x * near_height + right.x * near_width;
		f.point[3].x = nc.x - up.x * near_height + right.x * near_width;

		f.point[4].x = fc.x - up.x * far_height - right.x * far_width;
		f.point[5].x = fc.x + up.x * far_height - right.x * far_width;
		f.point[6].x = fc.x + up.x * far_height + right.x * far_width;
		f.point[7].x = fc.x - up.x * far_height + right.x * far_width;

		f.point[0].y = nc.y - up.y * near_height - right.y * near_width;
		f.point[1].y = nc.y + up.y * near_height - right.y * near_width;
		f.point[2].y = nc.y + up.y * near_height + right.y * near_width;
		f.point[3].y = nc.y - up.y * near_height + right.y * near_width;

		f.point[4].y = fc.y - up.y * far_height - right.y * far_width;
		f.point[5].y = fc.y + up.y * far_height - right.y * far_width;
		f.point[6].y = fc.y + up.y * far_height + right.y * far_width;
		f.point[7].y = fc.y - up.y * far_height + right.y * far_width;

		f.point[0].z = nc.z - up.z * near_height - right.z * near_width;
		f.point[1].z = nc.z + up.z * near_height - right.z * near_width;
		f.point[2].z = nc.z + up.z * near_height + right.z * near_width;
		f.point[3].z = nc.z - up.z * near_height + right.z * near_width;

		f.point[4].z = fc.z - up.z * far_height - right.z * far_width;
		f.point[5].z = fc.z + up.z * far_height - right.z * far_width;
		f.point[6].z = fc.z + up.z * far_height + right.z * far_width;
		f.point[7].z = fc.z - up.z * far_height + right.z * far_width;

	}

	// updateSplitDist computes the near and far distances for every frustum slice
	// in camera eye space - that is, at what distance does a slice start and end
	void updateSplitDist(Frustum f[], float nd, float fd) {
		float lambda = split_weight;
		float ratio = fd / nd;
		f[0].neard = nd;

		for (int i = 1; i < getCurNumberSplits(); i++) {
			float si = i /  getCurNumberSplits();

			f[i].neard = lambda * (nd * FastMath.pow(ratio, si)) + (1 - lambda) * (nd + (fd - nd) * si);
			f[i - 1].fard = f[i].neard * EngineSettings.G_SHADOW_SPLIT_MULT.getFloat();//1.005f;
		}
		
		f[0].fard *= EngineSettings.G_SHADOW_SPLIT_FAR_0.getFloat();// 0.325f;
		f[1].neard *= EngineSettings.G_SHADOW_SPLIT_NEAR_1.getFloat();// 0.325;
		f[1].fard *= EngineSettings.G_SHADOW_SPLIT_FAR_1.getFloat();//0.325;

		f[2].neard *= EngineSettings.G_SHADOW_SPLIT_NEAR_2.getFloat();//0.325;
		f[getCurNumberSplits() - 1].fard = fd;
	}

	public void setMatrixFromArray(Matrix4f m, float[] array) {
		buff.rewind();
		buff.put(array);
		buff.rewind();
		Matrix4fTools.load(buff, m);
	}

	public void setArrayFromMatrix(Matrix4f m, float[] array) {

		buff.rewind();
		Matrix4fTools.store(m, buff);
		buff.rewind();
		buff.get(array);
	}


	public BoundingSphere createFromPoints(Vector3f[] points) {

		float radius = 0;
		Vector3f center = new Vector3f();
		// First, we'll find the center of gravity for the point 'cloud'.
		int num_points = 0; // The number of points (there MUST be a better way to get this instead of counting the number of points one by one?)
		for (Vector3f v : points) {
			center.add(v);    // If we actually knew the number of points, we'd get better accuracy by adding v / num_points.
			++num_points;
		}

		center.x /= num_points;
		center.y /= num_points;
		center.z /= num_points;

		// Calculate the radius of the needed sphere (it equals the distance between the center and the point further away).
		for (Vector3f v : points) {
			Vector3f d = new Vector3f();
			d.sub(v, center);
			float distance = d.length();

			if (distance > radius)
				radius = distance;
		}

		return new BoundingSphere(center, radius);
	}

	// this function builds a projection matrix for rendering from the shadow's POV.
	// First, it computes the appropriate z-range and sets an orthogonal projection.
	// Then, it translates and scales it, so that it exactly captures the bounding box
	// of the current frustum slice
	float applyCropMatrix(Frustum f, int index) {
		float shad_modelview[] = new float[16];
		float shad_proj[] = new float[16];
		float shad_crop[] = new float[16];
		float shad_mvp[] = new float[16];
		float maxX = EngineSettings.G_SHADOW_CROP_MATRIX_MAX.getFloat();//-1000.0f;
		float maxY = EngineSettings.G_SHADOW_CROP_MATRIX_MAX.getFloat();//-1000.0f;
		float maxZ;
		float minX = EngineSettings.G_SHADOW_CROP_MATRIX_MIN.getFloat();//1000.0f;
		float minY = EngineSettings.G_SHADOW_CROP_MATRIX_MIN.getFloat();//1000.0f;
		float minZ;

		Matrix4f nv_mvp = new Matrix4f();
		Vector4f transf = new Vector4f();

		// find the z-range of the current frustum as seen from the light
		// in order to increase precision
		//		glGetFloatv(GL_MODELVIEW_MATRIX, shad_modelview);
		Controller.getMat(Controller.modelviewMatrix, shad_modelview);
		nv_mvp.set(Controller.modelviewMatrix);

		// note that only the z-component is need and thus
		// the multiplication can be simplified
		// transf.z = shad_modelview[2] * f.point[0].x + shad_modelview[6] * f.point[0].y + shad_modelview[10] * f.point[0].z + shad_modelview[14];
		transf.set(f.point[0].x, f.point[0].y, f.point[0].z, 1.0f);
		Matrix4fTools.transform(nv_mvp, transf, transf);
		//		transf = nv_mvp*vec4f(f.point[0], 1.0f);
		minZ = transf.z;
		maxZ = transf.z;
		for (int i = 1; i < 8; i++) {
			//			transf = nv_mvp*vec4f(f.point[i], 1.0f);
			transf.set(f.point[i].x, f.point[i].y, f.point[i].z, 1.0f);
			Matrix4fTools.transform(nv_mvp, transf, transf);
			if (transf.z > maxZ) maxZ = transf.z;
			if (transf.z < minZ) minZ = transf.z;
		}


		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glLoadIdentity();
		// set the projection matrix with the new z-bounds
		// note the inversion because the light looks at the neg. z axis
		//		GlUtil. gluPerspective(LIGHT_FOV, 1.0f, maxZ, minZ); // for point lights
		GlUtil.glOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -maxZ, -minZ);
		//		glGetFloatv(GL_PROJECTION_MATRIX, shad_proj);

		Controller.getMat(Controller.projectionMatrix, shad_proj);

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(shad_modelview);
		//		glGetFloatv(GL_PROJECTION_MATRIX, shad_mvp);
		Controller.getMat(Controller.projectionMatrix, shad_mvp);
		GlUtil.glPopMatrix();

		// find the extends of the frustum slice as projected in light's homogeneous coordinates
		setMatrixFromArray(nv_mvp, shad_mvp);

		for (int i = 0; i < 8; i++) {
			transf.set(f.point[i].x, f.point[i].y, f.point[i].z, 1.0f);
			Matrix4fTools.transform(nv_mvp, transf, transf);

			transf.x /= transf.w;
			transf.y /= transf.w;

			if (transf.x > maxX) maxX = transf.x;
			if (transf.x < minX) minX = transf.x;
			if (transf.y > maxY) maxY = transf.y;
			if (transf.y < minY) minY = transf.y;
		}

		Vector3f vWorldUnitsPerTexel = new Vector3f((maxX - minX) / getDepthSize(), (maxY - minY) / getDepthSize(), 0);

		minX /= vWorldUnitsPerTexel.x;
		minX = FastMath.round(minX);
		minX *= vWorldUnitsPerTexel.x;

		maxX /= vWorldUnitsPerTexel.x;
		maxX = FastMath.round(maxX);
		maxX *= vWorldUnitsPerTexel.x;

		minY /= vWorldUnitsPerTexel.y;
		minY = FastMath.round(minY);
		minY *= vWorldUnitsPerTexel.y;

		maxY /= vWorldUnitsPerTexel.y;
		maxY = FastMath.round(maxY);
		maxY *= vWorldUnitsPerTexel.y;

		float scaleX = 2.0f / (maxX - minX);
		float scaleY = 2.0f / (maxY - minY);

		float offsetX = -0.5f * (maxX + minX) * scaleX;
		float offsetY = -0.5f * (maxY + minY) * scaleY;

		// apply a crop matrix to modify the projection matrix we got from glOrtho.
		nv_mvp.setIdentity();
		nv_mvp.m00 = scaleX;
		nv_mvp.m11 = scaleY;
		nv_mvp.m03 = offsetX;
		nv_mvp.m13 = offsetY;

		nv_mvp.transpose();

		setArrayFromMatrix(nv_mvp, shad_crop);
		//		nv_mvp.get_value(shad_crop);
		GlUtil.glLoadMatrix(shad_crop);

		//		GlUtil.translateModelview(-dx, -dy, 0);

		GlUtil.glMultMatrix(shad_proj);

		

		return minZ;
	}

	public static Shader getShadowShader(boolean blend){
		if(blend){
			return ShaderLibrary.shadowShaderCubesBlend;
		}else{
			return ShaderLibrary.shadowShaderCubes;
		}
	}
	private void debug(String what){
		if(debug){
			System.err.println(what);
			GlUtil.printGlErrorCritical(what);
		}
	}
	long time = 0;
	// here all shadow map textures and their corresponding matrices are created
	public void makeShadowMap(Timer timer) {
		if(!GraphicsContext.isCurrentFocused()){
			//something in here crashes when tabbing out from full screen, so dont do this function when window not visible
			return;
		}
		debug("Start");
		if(USE_FIXED_FPS){
			long delay = 16; //60 fps;
			if(timer.fps < 30){
				delay = 66;
			}else if(timer.fps < 60){
				delay = 33;
			}
		
			if(System.currentTimeMillis() - time < delay){
				return;
			}
			time = System.currentTimeMillis();
		}
		
		GL11.glDepthRange(EngineSettings.G_SHADOW_DEPTH_RANGE_NEAR.getFloat(), EngineSettings.G_SHADOW_DEPTH_RANGE_FAR.getFloat());
		debug("DepthRange");
		
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}

		float shad_modelview[] = new float[16];
		//		glDisable(GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		// since the shadow maps have only a depth channel, we don't need color computation
		// glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);

		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPushMatrix();

		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();

		debug("Matrix");
		Vector3f light_dir = new Vector3f(
				AbstractScene.mainLight.getPos().x, 
				AbstractScene.mainLight.getPos().y, 
				AbstractScene.mainLight.getPos().z);
		if (light_dir.x == 0 && light_dir.y == 0 && light_dir.z == 0) {
			//never normalize z length vector
			light_dir.set(0, 1, 0);
		}
		light_dir.normalize();
		light_dir.negate();
		GlUtil.lookAt(
				0, 0, 0,
				light_dir.x, light_dir.y, light_dir.z,
				-1.0f, 0.0f, 0.0f);
		debug("Look At");
		Controller.getMat(Controller.modelviewMatrix, shad_modelview);
		//		glGetFloatv(GL11.GL_MODELVIEW_MATRIX, shad_modelview);
		debug("Mat");
		// redirect rendering to the depth texture
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, depth_fb);
		// store the screen viewport
		debug("Bind");
		int vp[] = new int[16];
		Controller.viewport.rewind();
		Controller.viewport.get(vp);
		Controller.viewport.rewind();
		debug("Mat2");
		// and render only to the shadowmap
		GL11.glViewport(0, 0, getDepthSize(), getDepthSize());
		debug("ViewPort");
		boolean offSet = !Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_1);

		if (offSet) {
			// offset the geometry slightly to prevent z-fighting
			// note that this introduces some light-leakage artifacts
			GL11.glPolygonOffset(1.0f, 8192.0f);
			GlUtil.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		}
		debug("Offset");
		// draw all faces since our terrain is not closed.
		//		GlUtil.glDisable(GL11.GL_CULL_FACE);

		// compute the z-distances for each split as seen in camera space
		updateSplitDist(f, 
				EngineSettings.G_SHADOW_NEAR_DIST.getFloat(),
				EngineSettings.G_SHADOW_FAR_DIST.getFloat());
		debug("Split dist");
		// for all shadow maps:
		for (int i = 0; i < getCurNumberSplits(); i++) {
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
				GlUtil.printGlErrorCritical();
			}
			// compute the camera frustum slice boundary points in world space
			updateFrustumPoints(f[i], new Vector3f(
					Controller.getCamera().getPos().x,
					Controller.getCamera().getPos().y,
					Controller.getCamera().getPos().z
			), new Vector3f(
					Controller.getCamera().getForward().x,
					Controller.getCamera().getForward().y,
					Controller.getCamera().getForward().z
			)
					, new Vector3f(
					Controller.getCamera().getUp().x,
					Controller.getCamera().getUp().y,
					Controller.getCamera().getUp().z
			));
			debug("Frustum "+i);
			// adjust the view frustum of the light, so that it encloses the camera frustum slice fully.
			// note that this function sets the projection matrix as it sees best fit
			// minZ is just for optimization to cull trees that do not affect the shadows
			//			float minZ = applyCropMatrix(f[i], i);

			GlUtil.glMatrixMode(GL11.GL_PROJECTION);
			BoundingSphere sphere = createFromPoints(f[i].point);

			//			System.err.println(i+" BB: "+sphere.center+" -> "+sphere.radius);
			float ExtraBackup = EngineSettings.G_SHADOW_EXTRA_BACKUP.getFloat();//20.0f;
			float nearClip = EngineSettings.G_SHADOW_NEAR_CLIP.getFloat();//.05f;

			float backupDist = ExtraBackup + nearClip + sphere.radius;

			Vector3f direction = new Vector3f(
					AbstractScene.mainLight.getPos().x, 
					AbstractScene.mainLight.getPos().y, 
					AbstractScene.mainLight.getPos().z);
			if (direction.x == 0 && direction.y == 0 && direction.z == 0) {
				//never normalize z length vector
				direction.set(0, 1, 0);
			}
			direction.normalize();
			//			direction.negate();
			Vector3f shadowCamPos = new Vector3f(sphere.center);
			direction.scale(backupDist);

			shadowCamPos.add(shadowCamPos, direction);

			GlUtil.glPushMatrix();
			GlUtil.lookAt(shadowCamPos.x, shadowCamPos.y, shadowCamPos.z, sphere.center.x, sphere.center.y, sphere.center.z, 0, 1, 0);
			debug("Look At 2");
			Matrix4f shadowViewMatrix = new Matrix4f(Controller.projectionMatrix);
			GlUtil.glPopMatrix();
			debug("Pop");
			
			
			if (i == 0) {
				sphere.radius += EngineSettings.G_SHADOW_SPLIT_MAT_RADIUS_ADD_0.getFloat();//1 * 10f * i;
			}
			if (i == 1) {
				sphere.radius += EngineSettings.G_SHADOW_SPLIT_MAT_RADIUS_ADD_1.getFloat();//1 * 10f * i;
			}
			if (i == 2) {
				sphere.radius += EngineSettings.G_SHADOW_SPLIT_MAT_RADIUS_ADD_2.getFloat();//DebugControlManager.y * 10; //Change with tab + Up/Down
			}
			if (i == 3) {
				sphere.radius += EngineSettings.G_SHADOW_SPLIT_MAT_RADIUS_ADD_3.getFloat();//1 * 10f * i;
			}
			//			Matrix shadowViewMatrix = Matrix.CreateLookAt(shadowCamPos, sphere.center, new Vector3f(0,1,0));

			float bounds = sphere.radius * 2.0f;
			float farClip = backupDist + sphere.radius;

			f[i].farSpheric = (farClip);
//			if (i == 1) {
//				farClip += 10 * 10f * i;
//			}
			if (i == 0) {
				farClip += EngineSettings.G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_0.getFloat();//1 * 10f * i;
			}
			if (i == 1) {
				farClip += EngineSettings.G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_1.getFloat();//1 * 10f * i;
			}
			if (i == 2) {
				farClip += EngineSettings.G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_2.getFloat();//DebugControlManager.y * 10; //Change with tab + Up/Down
			}
			if (i == 3) {
				farClip += EngineSettings.G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_3.getFloat();//1 * 10f * i;
			}
			GlUtil.glPushMatrix();
			GlUtil.glOrtho(
					-sphere.radius, 
					sphere.radius, 
					-sphere.radius, 
					sphere.radius, 
					nearClip + EngineSettings.G_SHADOW_SPLIT_ORTHO_NEAR_ADDED.getFloat(), 
					farClip);
			Matrix4f shadowProjMatrix = new Matrix4f(Controller.projectionMatrix);
			GlUtil.glPopMatrix();
			debug("Push n stuff");
			//			modMat(shadowProjMatrix);

			Matrix4f shadowMatrix = new Matrix4f();
			Matrix4fTools.mul(shadowProjMatrix, shadowViewMatrix, shadowMatrix);
			debug("Mul");
			modMat(shadowMatrix, shadowProjMatrix);
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glLoadMatrix(shadowProjMatrix);
			debug("Load");
			// make the current depth map a rendering target
			GL30.glFramebufferTextureLayer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depth_tex_ar, 0, i);
			if (USE_VSM) {
				GL30.glFramebufferTextureLayer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, color_tex_ar, 0, i);
			}
			debug("FB TEX LAYER");
			if (firstCheck) {
				GL11.glReadBuffer(GL11.GL_NONE);
				//FrameBufferObjects.checkFrameBuffer();
				firstCheck = false;
				GlUtil.printGlErrorCritical();
			}
			// clear the depth texture from last time
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
			debug("CLEAR "+i);
			//			modMat(Controller.projectionMatrix);
			//			GlUtil.glLoadMatrix(Controller.projectionMatrix);

			GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
			GlUtil.glPushMatrix();
			GlUtil.glLoadMatrix(shadowViewMatrix);

			//			modMat(Controller.modelviewMatrix);
			//			GlUtil.glLoadMatrix(Controller.modelviewMatrix);
			assert(ShaderLibrary.shadowShaderCubes != null);
			debug("BEF DRAW SCENE "+i);
			/*
			 * DRAW THE SCENE
			 */
			creatingMap = true;
			creatingMapIndex = i;
			drawScene(false, i);
			creatingMap = false;
			
			debug("AFT DRAW SCENE "+i);
			
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glPopMatrix();

			//was reset from applyCropMatrix
			GlUtil.glMatrixMode(GL11.GL_PROJECTION);
			GlUtil.glLoadMatrix(shadowProjMatrix);
			// store the product of all shadow matries for later
			//			GlUtil.glMultMatrix(shad_modelview);
			//			modMat(Controller.projectionMatrix);
			//			GlUtil.glLoadMatrix(Controller.projectionMatrix);
			GlUtil.glMultMatrix(shadowViewMatrix);

			setArrayFromMatrix(Controller.projectionMatrix, shad_cpm[i]);
			debug("END "+i);
			//			setArrayFromMatrix(shadowMatrix, shad_cpm[i]);
			//			glGetFloatv(GL11.GL_PROJECTION_MATRIX, shad_cpm[i]);

		}

		// revert to normal back face culling as used for rendering
		//		GlUtil.glEnable(GL11.GL_CULL_FACE);
		if (offSet) {
			GlUtil.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		}
		GL11.glViewport(vp[0], vp[1], vp[2], vp[3]);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		GlUtil.glEnable(GL11.GL_TEXTURE_2D);

		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPopMatrix();
		debug("FINISH");

	}

	private void modMat(Matrix4f biasMat, Matrix4f out) {
		Vector4f ptOriginShadow = new Vector4f(0, 0, 0, 1);
		Matrix4fTools.transform(biasMat, ptOriginShadow, ptOriginShadow);
		// xShadowMatrix is the light view projection matrix
		//		D3DXVECTOR3 ptOriginShadow(0,0,0);
		//		D3DXVec3TransformCoord(&ptOriginShadow, &ptOriginShadow, &xShadowMatrix);

		// Find nearest shadow map texel. The 0.5f is because x,y are in the
		// range -1 .. 1 and we need them in the range 0 .. 1
		float texCoordX = ptOriginShadow.x * getDepthSize() * 0.5f;
		float texCoordY = ptOriginShadow.y * getDepthSize() * 0.5f;

		// Round to the nearest 'whole' texel
		float texCoordRoundedX = FastMath.round(texCoordX);
		float texCoordRoundedY = FastMath.round(texCoordY);

		// The difference between the rounded and actual tex coordinate is the
		// amount by which we need to translate the shadow matrix in order to
		// cancel sub-texel movement
		float dx = texCoordRoundedX - texCoordX;
		float dy = texCoordRoundedY - texCoordY;

		// Transform dx, dy back to homogenous light space
		dx /= (getDepthSize() * 0.5f);
		dy /= (getDepthSize() * 0.5f);

		out.m30 += dx;
		out.m31 += dy;
		//		biasMat.m32 += dz;
	}

	public void renderScene() {
		
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		float cam_proj[] = new float[16];
		float cam_modelview[] = new float[16];
		float cam_inverse_modelview[] = new float[16];
		final float far_bound[] = new float[MAX_SPLITS];
		float bias[] = new float[]{0.5f, 0.0f, 0.0f, 0.0f,
				0.0f, 0.5f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.5f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f};

		//	   GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		Controller.getMat(Controller.modelviewMatrix, cam_modelview);

		Matrix4f mInv = new Matrix4f(Controller.modelviewMatrix);
		mInv.invert();
		Controller.getMat(mInv, cam_inverse_modelview);

		Controller.getMat(Controller.projectionMatrix, cam_proj);
		// update the camera, so that the user can have a free look
		//	    GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		//	    GlUtil.glLoadIdentity();
		//		camLook();
		//
		//		// store the inverse of the resulting modelview matrix for the shadow computation
		//		glGetFloatv(GL_MODELVIEW_MATRIX, cam_modelview);
		//		// since gluLookAt gives us an orthogonal matrix, we speed up the inverse computation
		//		cameraInverse(cam_inverse_modelview, cam_modelview);
		//
		//		GlUtil.glMatrixMode(GL_PROJECTION);
		//		GlUtil.glLoadIdentity();
		//		GlUtil.gluPerspective(45.0, (double)width/(double)height, f[0].neard, f[getCurNumberSplits()-1].fard);
		//		glGetFloatv(GL_PROJECTION_MATRIX, cam_proj);

		// bind all depth maps
		GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, depth_tex_ar);
		if (usePCF()) {
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
		} else {
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
		}

		//	    GET_GLERROR()

		for (int i = getCurNumberSplits(); i < MAX_SPLITS; i++) {
			far_bound[i] = 0;
		}
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		// for every active split
		for (int i = 0; i < getCurNumberSplits(); i++) {
			//			float light_m[] = new float[16];
			// f[i].fard is originally in eye space - tell's us how far we can see.
			// Here we compute it in camera homogeneous coordinates. Basically, we calculate
			// cam_proj * (0, 0, f[i].fard, 1)^t and then normalize to [0; 1]
			//			far_bound[i] = 0.5f*(-f[i].fard*cam_proj[10]+cam_proj[14])/f[i].fard + 0.5f;
			float norm = 0.5f * (-f[i].fard * cam_proj[10] + cam_proj[14]) / f[i].fard + 0.5f;

			far_bound[i] = 0.5f * (-f[i].farSpheric * cam_proj[10] + cam_proj[14]) / f[i].farSpheric + 0.5f;

			//			System.err.println("ON "+i+": "+norm+" -> "+far_bound[i]+" ..... "+f[i].fard+" -> "+f[i].farSpheric);

			// compute a matrix that transforms from camera eye space to light clip space
			// and pass it to the shader through the OpenGL texture matrices, since we
			// don't use them now
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0 + i);

			GL11.glMatrixMode(GL11.GL_TEXTURE);

			Matrix4f biasMat = new Matrix4f();
			setMatrixFromArray(biasMat, bias);

			Matrix4f scmp = new Matrix4f();
			setMatrixFromArray(scmp, shad_cpm[i]);

			Matrix4f inc = new Matrix4f();
			setMatrixFromArray(inc, cam_inverse_modelview);

			Matrix4fTools.mul(biasMat, scmp, biasMat);
			Matrix4fTools.mul(biasMat, inc, biasMat);

			buff.rewind();
			Matrix4fTools.store(biasMat, buff);
			buff.rewind();
			GL11.glLoadMatrixf(buff);
			//			GlUtil.glMultMatrix(shad_cpm[i]);
			//			// multiply the light's (bias*crop*proj*modelview) by the inverse camera modelview
			//			// so that we can transform a pixel as seen from the camera
			//			GlUtil.glMultMatrix(cam_inverse_modelview);
			//
			//			// compute a normal matrix for the same thing (to transform the normals)
			//			// Basically, N = ((L)^-1)^-t
			//			glGetFloatv(GL_TEXTURE_MATRIX, light_m);

			//			Transform tL = new Transform(biasMat);
			//			tL.getOpenGLMatrix(m);
			//			tL.set(mat);sd
			//
			Matrix4f nm = new Matrix4f();
			nm.set(biasMat);
//			nm.invert();
			nm.transpose();

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0 + (i + 4));
			GL11.glMatrixMode(GL11.GL_TEXTURE);

			buff.rewind();
			Matrix4fTools.store(nm, buff);
			buff.rewind();
			GL11.glLoadMatrixf(buff);

		}
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		if (EngineSettings.G_SHADOW_QUALITY.getObject() == ShadowQuality.ULTRA) {
			far_bound[2] += EngineSettings.G_SHADOW_ULTRA_FAR_BOUNDS_ADDED_2.getFloat();//0.002;
			far_bound[1] += EngineSettings.G_SHADOW_ULTRA_FAR_BOUNDS_ADDED_1.getFloat();//0.002f;
			//correct near
			far_bound[0] += EngineSettings.G_SHADOW_ULTRA_FAR_BOUNDS_ADDED_0.getFloat();//0.023 + DebugControlManager.y * 0.001;
		} else {
			
			far_bound[2] += EngineSettings.G_SHADOW_OTHER_QUALITY_FAR_BOUND_ADDED_2.getFloat();//0.002;
			far_bound[1] += EngineSettings.G_SHADOW_OTHER_QUALITY_FAR_BOUND_ADDED_1.getFloat();//0.002f;
			//correct near
			far_bound[0] += EngineSettings.G_SHADOW_OTHER_QUALITY_FAR_BOUND_ADDED_0.getFloat();//0.023 + DebugControlManager.y * 0.001;
			
//			far_bound[1] -= 0.002f;
//			//correct near
//			far_bound[0] -= 0.023;
		}
		ShadowParams shadowParamsCube = prog -> {
			GlUtil.updateShaderBoolean(prog, "shadow", true);
			GlUtil.updateShaderInt(prog, "splits", getCurNumberSplits());
			GlUtil.glActiveTexture(GL13.GL_TEXTURE10);
			if (USE_VSM) {
				GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, color_tex_ar);
			} else {
				GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, depth_tex_ar);
			}
			GlUtil.updateShaderInt(prog, "stex", 10);

			GlUtil.updateShaderVector4f(prog, "far_d", far_bound[0], far_bound[1], far_bound[2], 1);
			GlUtil.updateShaderVector2f(prog, "texSize", getDepthSize(), 1.0f / getDepthSize());

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		};
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		ShadowParams shadowParamsCharacter = prog -> {
			GlUtil.updateShaderBoolean(prog, "shadow", true);
			GlUtil.updateShaderInt(prog, "splits", getCurNumberSplits());
			GlUtil.glActiveTexture(GL13.GL_TEXTURE10);
			if (USE_VSM) {
				GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, color_tex_ar);
			} else {
				GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, depth_tex_ar);
			}
			GlUtil.updateShaderInt(prog, "stex", 10);

			GlUtil.updateShaderVector4f(prog, "far_d", far_bound[0], far_bound[1], far_bound[2], 1);
			GlUtil.updateShaderVector2f(prog, "texSize", getDepthSize(), 1.0f / getDepthSize());

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		};
		ShadowParams shadowParamsShards = prog -> {
			GlUtil.updateShaderBoolean(prog, "shadow", true);
			GlUtil.updateShaderInt(prog, "splits", getCurNumberSplits());
			GlUtil.glActiveTexture(GL13.GL_TEXTURE10);
			if (USE_VSM) {
				GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, color_tex_ar);
			} else {
				GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, depth_tex_ar);
			}
			GlUtil.updateShaderInt(prog, "stex", 10);

			GlUtil.updateShaderVector4f(prog, "far_d", far_bound[0], far_bound[1], far_bound[2], 1);
			GlUtil.updateShaderVector2f(prog, "texSize", getDepthSize(), 1.0f / getDepthSize());

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		};
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		SegmentDrawer.shader.setShadow(shadowParamsCube);

		state.getWorldDrawer().getShards().setShadow(shadowParamsShards);

		Skin.setShadow(shadowParamsCharacter);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		//		GL20.glUseProgram(prog);
		//		GL20.glUniform1i(GL20.glGetUniformLocation(prog, "stex"), 0); // depth-maps
		//		GL20.glUniform1i(GL20.glGetUniformLocation(prog, "tex"), 1); // terrain tex
		//		// the shader needs to know the split distances, so that it can choose in which
		//		// texture to to the look up. Note that we pass them in homogeneous coordinates -
		//		// this the same space as GL20.gl_FragCoord is in. In this way the shader is more efficient
		//		GL20.glUniform4f(GL20.glGetUniformLocation(prog, "far_d"), 1, far_bound);
		//		 GL20.glUniform2f(GL20.glGetUniformLocation(shad_pcf_gaussian_prog, "texSize"), getDepthSize(), 1.0f/getDepthSize());

		// finally, draw the scene
		drawScene(true, -1);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		//		GET_GLERROR()

		for (int i = 0; i < getCurNumberSplits(); i++) {

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0 + i);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glMatrixMode(GL11.GL_TEXTURE);

			GL11.glLoadIdentity();

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0 + (i + 4));
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glMatrixMode(GL11.GL_TEXTURE);

			GL11.glLoadIdentity();

		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE6);
		GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
	}

	private void drawScene(boolean real, int occIndex) {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		if (real) {
			state.getWorldDrawer().getSegmentDrawer().setSegmentRenderPass(SegmentDrawer.SegmentRenderPass.OPAQUE);
			state.getWorldDrawer().getSegmentDrawer().draw();
			state.getWorldDrawer().getShards().draw();
			state.getWorldDrawer().getCharacterDrawer().draw();
			if (state.getWorldDrawer().getCreatureTool() != null) {
				state.getWorldDrawer().getCreatureTool().draw();
			}

		} else {
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
				GlUtil.printGlErrorCritical();
			}
			assert (occIndex >= 0);
			float[][] origFrustum = Controller.getCamera().getFrustum();
			//switch to shadow frustum and update
			Controller.getCamera().setFrustum(tmpFrustum);
			Controller.getCamera().updateFrustum();

			if (USE_VSM) {
				state.getWorldDrawer().getSegmentDrawer().setCullFace(false);
				GlUtil.glDisable(GL11.GL_CULL_FACE);
				GL11.glCullFace(GL11.GL_FRONT);
			}
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
				GlUtil.printGlErrorCritical();
			}
			//				CubeOptOptMesh.shader.setShadow(new ShadowParams(){
			//
			//					@Override
			//					public void execute(Shader prog) {
			//						GlUtil.updateShaderBoolean(prog, "shadow", false);
			//					}
			//
			//				});
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//						state.getWorldDrawer().getSegmentDrawer().enableCulling(false);

			state.getWorldDrawer().getSegmentDrawer().setSegmentRenderPass(SegmentDrawer.SegmentRenderPass.ALL);
			state.getWorldDrawer().getSegmentDrawer().draw(SegmentDrawer.shader, ShaderLibrary.shadowShaderCubes, false, false, occlusions[occIndex], (short) (state.getNumberOfUpdate() - 10 + occIndex));
			state.getWorldDrawer().getSegmentDrawer().drawCubeLod(true);
			state.getWorldDrawer().getSegmentDrawer().enableCulling(true);
			state.getWorldDrawer().getCharacterDrawer().shadow = true;
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
				GlUtil.printGlErrorCritical();
			}
			GL20.glUseProgram(0);
			state.getWorldDrawer().getCharacterDrawer().draw();

			state.getWorldDrawer().getShards().draw();

			if (state.getWorldDrawer().getCreatureTool() != null) {
				state.getWorldDrawer().getCreatureTool().draw();
			}
			state.getWorldDrawer().getCharacterDrawer().shadow = false;

			GlUtil.glEnable(GL11.GL_CULL_FACE);
			if (USE_VSM) {
				state.getWorldDrawer().getSegmentDrawer().setCullFace(true);
				GL11.glCullFace(GL11.GL_BACK);
			}
			//switch back to original frustum
			Controller.getCamera().setFrustum(origFrustum);
		}

	}

	public void cleanUp() {
		if(depth_fb != 0){
			GL30.glDeleteFramebuffers(depth_fb);
			depth_fb = 0;
		}
		
		
		GlUtil.printGlErrorCritical();
		
		if(depth_tex_ar != 0){
			GL11.glDeleteTextures(depth_tex_ar);
			for(int i = 0; i < Controller.loadedTextures.size(); i++){
				if(Controller.loadedTextures.getInt(i) == depth_tex_ar){
					Controller.loadedTextures.removeInt(i);
					break;
				}
			}
			depth_tex_ar = 0;
		}
		
		GlUtil.printGlErrorCritical();
		
		if(color_tex_ar != 0){
			GL11.glDeleteTextures(color_tex_ar);
			Controller.loadedTextures.removeInt(color_tex_ar);
			color_tex_ar = 0;
		}
		
		GlUtil.printGlErrorCritical();
		
		
		
		if(occlusions != null){
			for (int i = 0; i < occlusions.length; i++) {
				occlusions[i].cleanUp();
			}
		}
		GlUtil.printGlErrorCritical();
		
	}
	public void init() {
		GlUtil.printGlErrorCritical();
		//		makeScene();
		GlUtil.getIntBuffer1().rewind();
		GL30.glGenFramebuffers(GlUtil.getIntBuffer1());

		depth_fb = GlUtil.getIntBuffer1().get(0);
		Controller.loadedFrameBuffers.add(depth_fb);
		GlUtil.printGlErrorCritical();

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, depth_fb);
		GlUtil.printGlErrorCritical();
		if (!USE_VSM) {
			GL11.glDrawBuffer(GL11.GL_NONE);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GlUtil.printGlErrorCritical();
		GlUtil.getIntBuffer1().rewind();
		GL11.glGenTextures(GlUtil.getIntBuffer1());
		depth_tex_ar = GlUtil.getIntBuffer1().get(0);
		GlUtil.printGlErrorCritical();

		//		ByteBuffer data = GlUtil.getDynamicByteBuffer(getDepthSize() * getDepthSize() * ByteUtil.SIZEOF_INT, 0);
		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
			System.err.println("GL_MEMORY AFTER NORMAL MAPS BEFORE SHADOW");
			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");
		}
		Controller.loadedTextures.add(depth_tex_ar);
		GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, depth_tex_ar);

		int dstPixelFormat = GL11.GL_DEPTH_COMPONENT;
		//		if(TextureLoader.COMPRESSED_SHADOW){
		//			if(dstPixelFormat == GL11.GL_RGB){
		////				System.err.println("Using texture compression on "+path);
		//				dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGB_ARB;
		//			}
		//			if(dstPixelFormat == GL11.GL_RGBA){
		////				System.err.println("Using texture compression on "+path);
		//				dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGBA_ARB;
		//			}
		//		}

		occlusions = new SegmentOcclusion[MAX_SPLITS];
		for (int i = 0; i < occlusions.length; i++) {
			occlusions[i] = new SegmentOcclusion();
			occlusions[i].reinitialize(EngineSettings.G_MAX_SEGMENTSDRAWN.getInt() * 2);
		}

		GL12.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, dstPixelFormat, getDepthSize(), getDepthSize(), MAX_SPLITS, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (ByteBuffer) null);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_INTENSITY);
		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
			System.err.println("GL_MEMORY AFTER NORMAL MAPS AFTER SHADOW");
			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");
		}
		GlUtil.printGlErrorCritical();
		if (USE_VSM) {
			GL11.glGenTextures(GlUtil.getIntBuffer1());
			color_tex_ar = GlUtil.getIntBuffer1().get(0);
			GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, color_tex_ar);
			GL12.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL30.GL_RGB32F, getDepthSize(), getDepthSize(), MAX_SPLITS, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_INT, (ByteBuffer) null);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		}
		GlUtil.printGlErrorCritical();

		for (int i = 0; i < MAX_SPLITS; i++) {
			// note that fov is in radians here and in OpenGL it is in degrees.
			// the 0.2f factor is important because we might get artifacts at
			// the screen borders.
			f[i].fov = FastMath.DEG_TO_RAD *  EngineSettings.G_FOV.getFloat() + EngineSettings.G_SHADOW_FOV_ADDED_RAD.getFloat();//0.2f;//57.2957795f+ 0.2f;
			f[i].ratio =  GLFrame.getWidth() /  GLFrame.getHeight();
		}

				glFogf(GL_FOG_DENSITY, 0.4f);
				glFogf(GL_FOG_START, 16.0f);
				glFogf(GL_FOG_END, EngineSettings.G_SHADOW_FAR_DIST.getFloat());
//
		//	    GET_GLERROR()

		GlUtil.printGlErrorCritical();
	}

	// here we show all depth maps that have been generated for the current frame
	// note that a special shader is required to display the depth-component-only textures
	public void showDepthTex() {
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		int loc;
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_CULL_FACE);

		ShaderLibrary.shad_view.loadWithoutUpdate();
		GlUtil.updateShaderInt(ShaderLibrary.shad_view, "tex", 0);
		int size = 256;
		for (int i = 0; i < getCurNumberSplits(); i++) {
			GL11.glViewport((size + 5) * i, 0, size, size);
			GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, depth_tex_ar);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
			GlUtil.updateShaderFloat(ShaderLibrary.shad_view, "layer", i);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3f(-1.0f, -1.0f, 0.0f);
			GL11.glVertex3f(1.0f, -1.0f, 0.0f);
			GL11.glVertex3f(1.0f, 1.0f, 0.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 0.0f);
			GL11.glEnd();

		}
		for (int i = 0; i < getCurNumberSplits(); i++) {
			GL11.glViewport((size + 5) * i, (size + 5), size, size);
			GlUtil.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, color_tex_ar);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
			GlUtil.updateShaderFloat(ShaderLibrary.shad_view, "layer", i);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3f(-1.0f, -1.0f, 0.0f);
			GL11.glVertex3f(1.0f, -1.0f, 0.0f);
			GL11.glVertex3f(1.0f, 1.0f, 0.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 0.0f);
			GL11.glEnd();

		}
		ShaderLibrary.shad_view.unloadWithoutExit();

		GL11.glViewport(GLFrame.getWidth() - 129, 0, 128, 128);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		//		overviewCam();

		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private class BoundingSphere {
		private Vector3f center = new Vector3f();
		private float radius = 1;

		public BoundingSphere(Vector3f center, float radius) {
			this.center.set(center);
			this.radius = radius;
		}
		public BoundingSphere() {
			// TODO Auto-generated constructor stub
		}
	}

	private class Frustum {
		public float farSpheric;
		float neard;
		float fard;
		float fov;
		float ratio;
		Vector3f point[] = new Vector3f[8];

		public Frustum() {
			for (int i = 0; i < point.length; i++) {
				point[i] = new Vector3f();
			}
		}
	}
}
