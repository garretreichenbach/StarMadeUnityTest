package org.schema.schine.graphicsengine.camera;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.graphicsengine.camera.look.FirstPersonCameraLook;
import org.schema.schine.graphicsengine.camera.look.MouseLookAlgorithm;
import org.schema.schine.graphicsengine.camera.viewer.AbstractViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Orientatable;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.sound.manager.engine.AudioListener;
import org.schema.schine.sound.manager.engine.AudioRenderer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

public class Camera implements Transformable, Orientatable, AudioListener {

	private static FloatBuffer proj = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer modl = MemoryUtil.memAllocFloat(16);
	private final Vector3f transformedPos = new Vector3f();
	private final Vector3f cachedUp = new Vector3f();
	private final Vector3f cachedRight = new Vector3f();
	private final Vector3f cachedForward = new Vector3f();
	public boolean alwaysAllowWheelZoom;
	public boolean updateOffset = true;
	public boolean mouseRotationActive = true;
	public static final CameraMouseState mouseState = new CameraMouseState();
	protected float[] transformationOpenGl;
	/**
	 * The viewer.
	 */
	protected AbstractViewer viewer;
	protected float cameraStartOffset;
	protected StateInterface state;
	/**
	 * Update frustum.
	 *
	 * @param gl the gl
	 * @param glu the glu
	 */
	float clip[] = new float[16];
	int wheel = 0;
	Vector3f fTmp = new Vector3f();
	Vector3f uTmp = new Vector3f();
	Vector3f lTmp = new Vector3f();
	Vector3f rTmp = new Vector3f();
	Transform previous = new Transform();
	Transform current = new Transform();
	Transform currentGL = new Transform();
	Vector3f oldPos = new Vector3f();
	Vector3f oldForward = new Vector3f();
	Vector3f oldUp = new Vector3f();
	Vector3f rightTmp = new Vector3f();
	Vector3f oldRight = new Vector3f();
	Vector3f posTmp = new Vector3f();
	Vector3f forwardTmp = new Vector3f();
	private float mouseSensibilityWheel = EngineSettings.MOUSE_WHEEL_SENSIBILITY.getFloat();
	/**
	 * The frustum.
	 */
	private float frustum[][];
	private TransformTimed transform;
	private MouseLookAlgorithm lookAlgorithm;
	private Transform startTransform;
	private float cameraOffset;
	private final Vector3f velocity = new Vector3f();
	private ThreadLocal<ObjectPool<Vector3f>> localPool = new ThreadLocal<ObjectPool<Vector3f>>() {
		@Override
		protected ObjectPool<Vector3f> initialValue() {
			return new ObjectPool(Vector3f.class);
		}
	};
	private float mouseSensibilityY = 1.0f;
	private float mouseSensibilityX = 1.0f;
	private boolean stable = true;
	private boolean allowZoom;
	private AudioRenderer renderer;

	/**
	 * Instantiates a new abstract viewer camera.
	 *
	 * @param viewer the viewer
	 */
	public Camera(StateInterface state, AbstractViewer viewer) {
		this.state = state;
		init(viewer);
	}

	public Camera(StateInterface state, AbstractViewer viewer, Transform startTransformable) {
		this.startTransform = startTransformable;
		this.state = state;
		init(viewer);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.AbstractViewerCamera#update(long)
	 */

	public static void main(String[] alsdkasld) {
		float[] f = new float[]{-0.34999016f, -5.8114665E-7f, 0.93675333f, 0,
				0.51698923f, 0.8339127f, 0.19315825f, 0,
				-0.78117007f, 0.5518945f, -0.29186073f, 0,
				0.0f, 0.0f, 0.0f, 1.0f};
		Transform t = new Transform();
		t.setFromOpenGLMatrix(f);

		Vector3f p = new Vector3f(188.99323f, 77.021645f, 294.4366f);
		p.negate();
		Vector3f op = new Vector3f(p);
		Vector3f o = new Vector3f();
		t.transform(op);

		while (true) {
			o.set(p);
			t.basis.transform(o);
			if (!o.equals(op)) {
				throw new NullPointerException(o + "; " + op);
			}
		}
	}

	public Matrix3f getExtraOrientationRotation() {
		return null;
	}

	/**
	 * Attach viewable.
	 *
	 * @param viewer the viewer
	 */
	public void attachViewable(AbstractViewer viewer) {
		this.viewer = viewer;
		viewer.setCamera(this);
		reset();
	}

	/**
	 * @return the cachedForward
	 */
	public Vector3f getCachedForward() {
		return cachedForward;
	}

	/**
	 * @return the cachedRight
	 */
	public Vector3f getCachedRight() {
		return cachedRight;
	}

	/**
	 * @return the cachedUp
	 */
	public Vector3f getCachedUp() {
		return cachedUp;
	}

	public float getCameraOffset() {
		return cameraOffset;
	}

	/**
	 * @param cameraOffset the cameraOffset to set
	 */
	public void setCameraOffset(float cameraOffset) {

		this.cameraOffset = cameraOffset;

	}

	@Override
	public Vector3f getForward() {
		return GlUtil.getForwardVector(new Vector3f(), getWorldTransform());
	}

	@Override
	public void setForward(Vector3f val) {
		//		System.err.println("SET FORWARD: "+val);
		GlUtil.setForwardVector(val, getWorldTransform());
	}

	@Override
	public Vector3f getLeft() {
		return GlUtil.getLeftVector(new Vector3f(), getWorldTransform());
	}

	@Override
	public void setLeft(Vector3f val) {
		GlUtil.setLeftVector(val, getWorldTransform());
	}

	@Override
	public Vector3f getRight() {
		return GlUtil.getRightVector(new Vector3f(), getWorldTransform());
	}

	@Override
	public void setRight(Vector3f val) {
		GlUtil.setRightVector(val, getWorldTransform());
	}

	@Override
	public Vector3f getUp() {
		return GlUtil.getUpVector(new Vector3f(), getWorldTransform());
	}

	@Override
	public void setUp(Vector3f val) {
		GlUtil.setUpVector(val, getWorldTransform());
	}

	public Vector3f getForward(Vector3f out) {
		return GlUtil.getForwardVector(out, getWorldTransform());
	}

	public Vector3f getLeft(Vector3f out) {
		return GlUtil.getLeftVector(out, getWorldTransform());
	}

	/**
	 * @return the lookAlgorithm
	 */
	public MouseLookAlgorithm getLookAlgorithm() {
		return lookAlgorithm;
	}

	/**
	 * @param lookAlgorithm the lookAlgorithm to set
	 */
	public void setLookAlgorithm(MouseLookAlgorithm lookAlgorithm) {
		this.lookAlgorithm = lookAlgorithm;
	}

	/**
	 * @return the mouseSensibilityWheel
	 */
	public float getMouseSensibilityWheel() {
		return mouseSensibilityWheel;
	}

	/**
	 * @param mouseSensibilityWheel the mouseSensibilityWheel to set
	 */
	public void setMouseSensibilityWheel(float mouseSensibilityWheel) {
		this.mouseSensibilityWheel = mouseSensibilityWheel;
	}

	/**
	 * @return the mouseSensibilityX
	 */
	public float getMouseSensibilityX() {
		return mouseSensibilityX * EngineSettings.I_MOUSE_SENSITIVITY.getFloat();
	}

	/**
	 * @param mouseSensibilityX the mouseSensibilityX to set
	 */
	public void setMouseSensibilityX(float mouseSensibilityX) {
		this.mouseSensibilityX = mouseSensibilityX;
	}

	/**
	 * @return the mouseSensibilityY
	 */
	public float getMouseSensibilityY() {
		return mouseSensibilityY * EngineSettings.I_MOUSE_SENSITIVITY.getFloat();
	}

	/**
	 * @param mouseSensibilityY the mouseSensibilityY to set
	 */
	public void setMouseSensibilityY(float mouseSensibilityY) {
		this.mouseSensibilityY = mouseSensibilityY;
	}

	public CameraMouseState getMouseState() {
		return mouseState;
	}


	/**
	 * has to be synched since racing conditions
	 * could fuck up offsetPos
	 */
	public Vector3f getOffsetPos(Vector3f out) {
		Vector3f offsetPos = getPool().get();
		Vector3f offset = getPool().get();

		try {
			//			System.err.println("CAM: "+getWorldTransform().origin);
			offsetPos.set(getWorldTransform().origin);
			offset.set(getForward());
			offset.negate();
			offset.scale(cameraOffset);
			offsetPos.add(offset);
			out.set(offsetPos);
			return offsetPos;
		} finally {
			getPool().release(offsetPos);
			getPool().release(offset);
		}
	}

	private ObjectPool<Vector3f> getPool() {
		return localPool.get();
	}

	@Override
	public Vector3f getPos() {
		return transformedPos;
	}

	public Vector3f getPosWithoutOffset(Vector3f out) {
		out.set(getWorldTransform().origin);
		return out;
	}

	public Vector3f getRight(Vector3f out) {
		return GlUtil.getRightVector(out, getWorldTransform());
	}

	public Vector3f getUp(Vector3f out) {
		return GlUtil.getUpVector(out, getWorldTransform());
	}

	/**
	 * Gets the viewable.
	 *
	 * @return the viewable
	 */
	public AbstractViewer getViewable() {
		assert (viewer != null);
		return viewer;
	}

	@Override
	public TransformTimed getWorldTransform() {
		return transform;
	}

	private void init(AbstractViewer viewer) {
		transform = (new TransformTimed());
		transform.setIdentity();
		transformationOpenGl = new float[16];
		reset();
		this.lookAlgorithm = new FirstPersonCameraLook(this);

		frustum = new float[6][4];
		attachViewable(viewer);
		mouseState.reset();
	}
	public boolean isAABBFullyInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

		return (frustum[0][0] * (minX) + frustum[0][1] * (minY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (maxX) + frustum[0][1] * (minY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (minX) + frustum[0][1] * (maxY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (maxX) + frustum[0][1] * (maxY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (minX) + frustum[0][1] * (minY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (maxX) + frustum[0][1] * (minY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (minX) + frustum[0][1] * (maxY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0 &&
				frustum[0][0] * (maxX) + frustum[0][1] * (maxY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0)

				&&

				(frustum[1][0] * (minX) + frustum[1][1] * (minY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (maxX) + frustum[1][1] * (minY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (minX) + frustum[1][1] * (maxY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (maxX) + frustum[1][1] * (maxY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (minX) + frustum[1][1] * (minY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (maxX) + frustum[1][1] * (minY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (minX) + frustum[1][1] * (maxY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0 &&
						frustum[1][0] * (maxX) + frustum[1][1] * (maxY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0)

				&&

				(frustum[2][0] * (minX) + frustum[2][1] * (minY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (maxX) + frustum[2][1] * (minY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (minX) + frustum[2][1] * (maxY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (maxX) + frustum[2][1] * (maxY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (minX) + frustum[2][1] * (minY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (maxX) + frustum[2][1] * (minY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (minX) + frustum[2][1] * (maxY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0 &&
						frustum[2][0] * (maxX) + frustum[2][1] * (maxY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0)

				&&

				(frustum[3][0] * (minX) + frustum[3][1] * (minY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (maxX) + frustum[3][1] * (minY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (minX) + frustum[3][1] * (maxY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (maxX) + frustum[3][1] * (maxY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (minX) + frustum[3][1] * (minY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (maxX) + frustum[3][1] * (minY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (minX) + frustum[3][1] * (maxY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0 &&
						frustum[3][0] * (maxX) + frustum[3][1] * (maxY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0)

				&&

				(frustum[4][0] * (minX) + frustum[4][1] * (minY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (maxX) + frustum[4][1] * (minY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (minX) + frustum[4][1] * (maxY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (maxX) + frustum[4][1] * (maxY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (minX) + frustum[4][1] * (minY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (maxX) + frustum[4][1] * (minY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (minX) + frustum[4][1] * (maxY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0 &&
						frustum[4][0] * (maxX) + frustum[4][1] * (maxY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0)

				&&

				(frustum[5][0] * (minX) + frustum[5][1] * (minY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (maxX) + frustum[5][1] * (minY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (minX) + frustum[5][1] * (maxY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (maxX) + frustum[5][1] * (maxY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (minX) + frustum[5][1] * (minY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (maxX) + frustum[5][1] * (minY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (minX) + frustum[5][1] * (maxY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0 &&
						frustum[5][0] * (maxX) + frustum[5][1] * (maxY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0);

	}
	public boolean isAABBInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

		return (frustum[0][0] * (minX) + frustum[0][1] * (minY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (maxX) + frustum[0][1] * (minY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (minX) + frustum[0][1] * (maxY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (maxX) + frustum[0][1] * (maxY) + frustum[0][2] * (minZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (minX) + frustum[0][1] * (minY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (maxX) + frustum[0][1] * (minY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (minX) + frustum[0][1] * (maxY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0 ||
				frustum[0][0] * (maxX) + frustum[0][1] * (maxY) + frustum[0][2] * (maxZ) + frustum[0][3] > 0)

				&&

				(frustum[1][0] * (minX) + frustum[1][1] * (minY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (maxX) + frustum[1][1] * (minY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (minX) + frustum[1][1] * (maxY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (maxX) + frustum[1][1] * (maxY) + frustum[1][2] * (minZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (minX) + frustum[1][1] * (minY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (maxX) + frustum[1][1] * (minY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (minX) + frustum[1][1] * (maxY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0 ||
						frustum[1][0] * (maxX) + frustum[1][1] * (maxY) + frustum[1][2] * (maxZ) + frustum[1][3] > 0)

				&&

				(frustum[2][0] * (minX) + frustum[2][1] * (minY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (maxX) + frustum[2][1] * (minY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (minX) + frustum[2][1] * (maxY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (maxX) + frustum[2][1] * (maxY) + frustum[2][2] * (minZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (minX) + frustum[2][1] * (minY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (maxX) + frustum[2][1] * (minY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (minX) + frustum[2][1] * (maxY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0 ||
						frustum[2][0] * (maxX) + frustum[2][1] * (maxY) + frustum[2][2] * (maxZ) + frustum[2][3] > 0)

				&&

				(frustum[3][0] * (minX) + frustum[3][1] * (minY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (maxX) + frustum[3][1] * (minY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (minX) + frustum[3][1] * (maxY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (maxX) + frustum[3][1] * (maxY) + frustum[3][2] * (minZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (minX) + frustum[3][1] * (minY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (maxX) + frustum[3][1] * (minY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (minX) + frustum[3][1] * (maxY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0 ||
						frustum[3][0] * (maxX) + frustum[3][1] * (maxY) + frustum[3][2] * (maxZ) + frustum[3][3] > 0)

				&&

				(frustum[4][0] * (minX) + frustum[4][1] * (minY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (maxX) + frustum[4][1] * (minY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (minX) + frustum[4][1] * (maxY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (maxX) + frustum[4][1] * (maxY) + frustum[4][2] * (minZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (minX) + frustum[4][1] * (minY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (maxX) + frustum[4][1] * (minY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (minX) + frustum[4][1] * (maxY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0 ||
						frustum[4][0] * (maxX) + frustum[4][1] * (maxY) + frustum[4][2] * (maxZ) + frustum[4][3] > 0)

				&&

				(frustum[5][0] * (minX) + frustum[5][1] * (minY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (maxX) + frustum[5][1] * (minY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (minX) + frustum[5][1] * (maxY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (maxX) + frustum[5][1] * (maxY) + frustum[5][2] * (minZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (minX) + frustum[5][1] * (minY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (maxX) + frustum[5][1] * (minY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (minX) + frustum[5][1] * (maxY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0 ||
						frustum[5][0] * (maxX) + frustum[5][1] * (maxY) + frustum[5][2] * (maxZ) + frustum[5][3] > 0);

//		for( int p = 0; p < 6; p++ )
//		{
//			if(
//				) {
//				continue;
//			}
//
//			return false;
//		}
//		return true;
	}

	/**
	 * Checks if is form bounding box frustum.
	 *
	 * @param x    the x
	 * @param y    the y
	 * @param z    the z
	 * @param size the size
	 * @param f
	 * @param g
	 * @return true, if is form bounding box frustum
	 */
	public boolean isAABBInFrustum(Vector3f min, Vector3f max) {
		return isAABBInFrustum(min.x, min.y, min.z, max.x, max.y, max.z);
	}
	public boolean isAABBFullyInFrustum(Vector3f min, Vector3f max) {
		return isAABBFullyInFrustum(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	/**
	 * Checks if is form bounding sphere in frustrum.
	 *
	 * @param f the f
	 * @return the float
	 */
	public float isFormBoundingSphereInFrustrum(AbstractSceneNode f) {
		return isFormBoundingSphereInFrustrum(f.getPos(), f);
	}

	public float isFormBoundingSphereInFrustrum(Vector3f pos, AbstractSceneNode f) {

		return isBoundingSphereInFrustrum2(pos, f.getBoundingSphereRadius());
	}

	public float isBoundingSphereInFrustrum2(Vector3f pos, float radius) {

		int p;
		float d = 0;
		for (p = 0; p < 6; p++) {
			d = frustum[p][0] * pos.x + frustum[p][1] * pos.y + frustum[p][2] * pos.z + frustum[p][3];
			if (d <= -radius) {
				return 0;
			}
		}
		return d + radius;
	}

	public boolean isBoundingSphereInFrustrum(Vector3f pos, float radius) {

		int p;
		float d = 0;
		for (p = 0; p < 6; p++) {
			d = frustum[p][0] * pos.x + frustum[p][1] * pos.y + frustum[p][2] * pos.z + frustum[p][3];
			if (d <= -radius) {
				return false;
			}
		}
		return true;
	}

	public boolean isPointInFrustrum(float x, float y, float z) {

		for (int p = 0; p < 6; p++) {
			if (frustum[p][0] * (x) + frustum[p][1] * (y) + frustum[p][2] * (z) + frustum[p][3] <= 0) {
				return false;
			}

		}
		return true;
	}

	public boolean isPointInFrustrum(Vector3f pos) {
		return isPointInFrustrum(pos.x, pos.y, pos.z);
	}

	protected int limitWheel(int in) {
		return Math.max(0, in);
	}

	public void loadModelView() {
		GlUtil.glLoadMatrix(getWorldTransform());

		updateFrustum();
	}

	public Transform lookAt(boolean load) {

		getOffsetPos(transformedPos);

		getForward().normalize();
		getUp().normalize();

		rightTmp.cross(getUp(), getForward());
		rightTmp.normalize();
		rightTmp.negate();

		forwardTmp.set(getForward());
		forwardTmp.negate();

		currentGL.basis.setRow(0, rightTmp);
		currentGL.basis.setRow(1, getUp());
		currentGL.basis.setRow(2, forwardTmp);

//		Matrix3f scale = new Matrix3f();
//		scale.m00 = 1;
//		scale.m11 = 1;
//		scale.m22 = -1;
//		currentGL.basis.mul(scale);

		posTmp.set(transformedPos);

		posTmp.negate();

		currentGL.origin.set(posTmp);

		currentGL.basis.transform(currentGL.origin);

		if (load) {
			GlUtil.glLoadMatrix(currentGL);
		}

		//		Vector3f origin = new Vector3f(previous.origin);
		//		origin.sub(current.origin);

		oldPos.set(posTmp);
		oldForward.set(forwardTmp);
		oldUp.set(getUp());
		oldRight.set(rightTmp);
		previous.set(current);
		updateFrustum();
		return currentGL;
	}

	/**
	 * Reset.
	 */
	public void reset() {
		getWorldTransform().setIdentity();
		if (startTransform == null) {
			setForward(new Vector3f(0, 0, -1));
			setRight(new Vector3f(1, 0, 0));
			setUp(new Vector3f(0, 1, 0));
		} else {
			System.err.println("[CAMERA] CAMERA HAS START TRANSFORM");
			setForward(new Vector3f(GlUtil.getForwardVector(new Vector3f(), startTransform)));
			setRight(new Vector3f(GlUtil.getRightVector(new Vector3f(), startTransform)));
			setUp(new Vector3f(GlUtil.getUpVector(new Vector3f(), startTransform)));
		}
		getWorldTransform().getOpenGLMatrix(transformationOpenGl);
		cameraOffset = cameraStartOffset;

	}

	public void reset(Transform t) {
		startTransform = new Transform(t);
		getWorldTransform().set(t);
		if (startTransform == null) {
			setForward(new Vector3f(0, 0, -1));
			setRight(new Vector3f(1, 0, 0));
			setUp(new Vector3f(0, 1, 0));
		} else {
			System.err.println("[CAMERA] CAMERA HAS START TRANSFORM");
			setForward(new Vector3f(GlUtil.getForwardVector(new Vector3f(), startTransform)));
			setRight(new Vector3f(GlUtil.getRightVector(new Vector3f(), startTransform)));
			setUp(new Vector3f(GlUtil.getUpVector(new Vector3f(), startTransform)));
		}
		getWorldTransform().getOpenGLMatrix(transformationOpenGl);
		cameraOffset = cameraStartOffset;

	}

	public void setCameraStartOffset(float f) {
		this.cameraStartOffset = f;
		updateOffset = true;
		reset();
	}

	public void update(Timer timer, boolean server) {

		boolean controlled = CameraMouseState.isInMouseControl();
		if (controlled || updateOffset) {

			updateMouseWheel();

			if (mouseRotationActive) {
				if (!controlled && updateOffset) {
					//only update offset
					//needed for initial update of mouse offset
					lookAlgorithm.mouseRotate(server,
							0,
							0,
							0,
							getMouseSensibilityX(),
							getMouseSensibilityY(),
							0);
				} else {
					if (((ClientStateInterface) state).getController().isJoystickOk() && (mouseState.dx == 0 && mouseState.dy == 0)) {
						ClientStateInterface c = (ClientStateInterface) state;

						double pitch = c.getController().getJoystickAxis(JoystickAxisMapping.PITCH);
						double yaw = c.getController().getJoystickAxis(JoystickAxisMapping.YAW);
						double roll = c.getController().getJoystickAxis(JoystickAxisMapping.ROLL);

						float x = ((float) yaw * timer.getDelta() * 10);
						float y = ((float) pitch * timer.getDelta() * 10);
						lookAlgorithm.mouseRotate(server,
								x,
								y,
								0,
								//(float)roll * timer.getDelta()*10f,
								getMouseSensibilityX(),
								getMouseSensibilityY(),
								0);
					} else {

						lookAlgorithm.mouseRotate(server,
								mouseState.dx / 1000.0F,
								mouseState.dy / 1000.0F,
								0,
								getMouseSensibilityX(),
								getMouseSensibilityY(),
								0);
					}
				}
			}
		} else if (alwaysAllowWheelZoom) {
			updateMouseWheel();
		}
		updateViewer(timer);
		
		
		
		GlUtil.getUpVector(cachedUp, getWorldTransform());
		GlUtil.getRightVector(cachedRight, getWorldTransform());
		GlUtil.getForwardVector(cachedForward, getWorldTransform());
	}

	public void updateFrustum() {
		proj.rewind();
		modl.rewind();
		Matrix4fTools.store(Controller.projectionMatrix,proj);
		Matrix4fTools.store(Controller.modelviewMatrix,modl);

		float t;

		/* Combine the two matrices (multiply projection by modelview) */
		clip[0] = modl.get(0) * proj.get(0) + modl.get(1) * proj.get(4) + modl.get(2) * proj.get(8) + modl.get(3) * proj.get(12);
		clip[1] = modl.get(0) * proj.get(1) + modl.get(1) * proj.get(5) + modl.get(2) * proj.get(9) + modl.get(3) * proj.get(13);
		clip[2] = modl.get(0) * proj.get(2) + modl.get(1) * proj.get(6) + modl.get(2) * proj.get(10) + modl.get(3) * proj.get(14);
		clip[3] = modl.get(0) * proj.get(3) + modl.get(1) * proj.get(7) + modl.get(2) * proj.get(11) + modl.get(3) * proj.get(15);

		clip[4] = modl.get(4) * proj.get(0) + modl.get(5) * proj.get(4) + modl.get(6) * proj.get(8) + modl.get(7) * proj.get(12);
		clip[5] = modl.get(4) * proj.get(1) + modl.get(5) * proj.get(5) + modl.get(6) * proj.get(9) + modl.get(7) * proj.get(13);
		clip[6] = modl.get(4) * proj.get(2) + modl.get(5) * proj.get(6) + modl.get(6) * proj.get(10) + modl.get(7) * proj.get(14);
		clip[7] = modl.get(4) * proj.get(3) + modl.get(5) * proj.get(7) + modl.get(6) * proj.get(11) + modl.get(7) * proj.get(15);

		clip[8] = modl.get(8) * proj.get(0) + modl.get(9) * proj.get(4) + modl.get(10) * proj.get(8) + modl.get(11) * proj.get(12);
		clip[9] = modl.get(8) * proj.get(1) + modl.get(9) * proj.get(5) + modl.get(10) * proj.get(9) + modl.get(11) * proj.get(13);
		clip[10] = modl.get(8) * proj.get(2) + modl.get(9) * proj.get(6) + modl.get(10) * proj.get(10) + modl.get(11) * proj.get(14);
		clip[11] = modl.get(8) * proj.get(3) + modl.get(9) * proj.get(7) + modl.get(10) * proj.get(11) + modl.get(11) * proj.get(15);

		clip[12] = modl.get(12) * proj.get(0) + modl.get(13) * proj.get(4) + modl.get(14) * proj.get(8) + modl.get(15) * proj.get(12);
		clip[13] = modl.get(12) * proj.get(1) + modl.get(13) * proj.get(5) + modl.get(14) * proj.get(9) + modl.get(15) * proj.get(13);
		clip[14] = modl.get(12) * proj.get(2) + modl.get(13) * proj.get(6) + modl.get(14) * proj.get(10) + modl.get(15) * proj.get(14);
		clip[15] = modl.get(12) * proj.get(3) + modl.get(13) * proj.get(7) + modl.get(14) * proj.get(11) + modl.get(15) * proj.get(15);

		/* Extract the numbers for the RIGHT plane */
		frustum[0][0] = clip[3] - clip[0];
		frustum[0][1] = clip[7] - clip[4];
		frustum[0][2] = clip[11] - clip[8];
		frustum[0][3] = clip[15] - clip[12];

		/* Normalize the result */
		t = FastMath.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
		float invT = 1.0f / t;
		frustum[0][0] *= invT;
		frustum[0][1] *= invT;
		frustum[0][2] *= invT;
		frustum[0][3] *= invT;

		/* Extract the numbers for the FLAG_LEFT plane */
		frustum[1][0] = clip[3] + clip[0];
		frustum[1][1] = clip[7] + clip[4];
		frustum[1][2] = clip[11] + clip[8];
		frustum[1][3] = clip[15] + clip[12];

		/* Normalize the result */
		t = FastMath.sqrt(frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2]);
		frustum[1][0] *= invT;
		frustum[1][1] *= invT;
		frustum[1][2] *= invT;
		frustum[1][3] *= invT;

		/* Extract the FLAG_BOTTOM plane */
		frustum[2][0] = clip[3] + clip[1];
		frustum[2][1] = clip[7] + clip[5];
		frustum[2][2] = clip[11] + clip[9];
		frustum[2][3] = clip[15] + clip[13];

		/* Normalize the result */
		t = FastMath.sqrt(frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2]);
		frustum[2][0] *= invT;
		frustum[2][1] *= invT;
		frustum[2][2] *= invT;
		frustum[2][3] *= invT;

		/* Extract the FLAG_TOP plane */
		frustum[3][0] = clip[3] - clip[1];
		frustum[3][1] = clip[7] - clip[5];
		frustum[3][2] = clip[11] - clip[9];
		frustum[3][3] = clip[15] - clip[13];

		/* Normalize the result */
		t = FastMath.sqrt(frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2]);
		frustum[3][0] *= invT;
		frustum[3][1] *= invT;
		frustum[3][2] *= invT;
		frustum[3][3] *= invT;

		/* Extract the FAR plane */
		frustum[4][0] = clip[3] - clip[2];
		frustum[4][1] = clip[7] - clip[6];
		frustum[4][2] = clip[11] - clip[10];
		frustum[4][3] = clip[15] - clip[14];

		/* Normalize the result */
		t = FastMath.sqrt(frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2]);
		frustum[4][0] *= invT;
		frustum[4][1] *= invT;
		frustum[4][2] *= invT;
		frustum[4][3] *= invT;

		/* Extract the NEAR plane */
		frustum[5][0] = clip[3] + clip[2];
		frustum[5][1] = clip[7] + clip[6];
		frustum[5][2] = clip[11] + clip[10];
		frustum[5][3] = clip[15] + clip[14];

		/* Normalize the result */
		t = FastMath.sqrt(frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2]);
		frustum[5][0] *= invT;
		frustum[5][1] *= invT;
		frustum[5][2] *= invT;
		frustum[5][3] *= invT;

	}

	public void setAllowZoom(boolean allowZoom) {
		
		this.allowZoom = allowZoom;
	}
	protected void updateMouseWheel() {
		if (allowZoom || alwaysAllowWheelZoom) {

			int oldWheel = wheel;
			this.wheel = limitWheel(wheel + (-mouseState.dWheel) * 100);
			if (updateOffset) {
				wheel = (int) FastMath.round((Math.log(Math.log(cameraOffset) / Math.log(1.3f)) / mouseSensibilityWheel));
				updateOffset = false;
			}

			if (oldWheel != wheel) {
				float wheelFac = wheel * mouseSensibilityWheel;
				cameraOffset = (wheel > 0 ? FastMath.exp(FastMath.pow(wheelFac, 1.3f)) : 0);
			}
			allowZoom = false;
		}
	}

	public void updateViewerTransform() {
		getWorldTransform().origin.set(getViewable().getPos());
	}
	public void updateViewer(Timer timer) {
		velocity.set(viewer.getPos());
		
		viewer.update(timer);
		updateViewerTransform();
		
		velocity.sub(viewer.getPos(), velocity);
		
		velocity.scale(timer.getDelta());
	}

	/**
	 * @return the stable
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * @param stable the stable to set
	 */
	public void setStable(boolean stable) {
		this.stable = stable;
	}

	/**
	 * @return the frustum
	 */
	public float[][] getFrustum() {
		return frustum;
	}

	/**
	 * @param frustum the frustum to set
	 */
	public void setFrustum(float frustum[][]) {
		this.frustum = frustum;
	}

	@Override
	public void setRenderer(AudioRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public float getVolume() {
		return 1.0f;
	}

	@Override
	public void setVolume(float volume) {
	}

	@Override
	public Vector3f getLocation() {
		return getWorldTransform().origin;
	}

	private final Quat4f rotTmp = new Quat4f();
	@Override
	public Quat4f getRotation() {
		getWorldTransform().getRotation(rotTmp);
		return rotTmp;
	}

	@Override
	public Vector3f getVelocity() {
		return velocity;
	}

	@Override
	public Vector3f getDirection() {
		return getForward();
	}

	@Override
	public void setLocation(Vector3f location) {
		throw new RuntimeException();
	}

	@Override
	public void setRotation(Quat4f rotation) {
		throw new RuntimeException();
	}

	@Override
	public void setVelocity(Vector3f velocity) {
		this.velocity.set(velocity);
	}

}
