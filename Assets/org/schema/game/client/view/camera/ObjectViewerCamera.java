/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ViewerCamera</H2>
 * <H3>org.schema.schine.graphicsengine.camera</H3>
 * ViewerCamera.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.schema.game.client.view.camera;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.linearmath.Transform;

public class ObjectViewerCamera extends Camera {

	Vector3f dir = new Vector3f();
	private Transformable obj;

	private float percent;

	private Matrix3f m;

	public Vector3f forceRotation;

	public ObjectViewerCamera(StateInterface state, FixedViewer fViewable, Transformable obj, float offset) {
		super(state, fViewable);
		this.dir = new Vector3f(dir);
		getWorldTransform().setIdentity();
		this.obj = obj;
		m = new Matrix3f();
		m.setIdentity();
		setCameraStartOffset(offset);
	}

	public ObjectViewerCamera(StateInterface state, FixedViewer fViewable, Transformable obj) {
		this(state, fViewable, obj, 3000.0f);
	}

	@Override
	public void update(Timer timer, boolean server) {
		if (timer.getDelta() <= 0) {
			return;
		}
		percent += timer.getDelta();
		//		if(flyTo != null){
		//			Vector3f projected = new Vector3f();
		//			Vector3f dd = new Vector3f();
		//			dd.sub(flyTo, getWorldTransform().origin);
		//
		//			Vector3f nDD = new Vector3f(dd);
		//			nDD.normalize();
		//
		//			nDD.sub(dir);
		//			if(nDD.length() > 0.1f){
		//				GlUtil.project(new Vector3f(dir), new Vector3f(dd), projected);
		//				projected.normalize();
		//				projected.scale((float) (timer.getDelta()*0.35));
		//				dir.add(projected);
		//				dir.normalize();
		//			}
		//		}
		//		Vector3f dirScaled = new Vector3f(dir);
		//		dirScaled.scale(timer.getDelta()*55);
		//		((FixedViewer)getViewable()).getEntity().getWorldTransform().origin.add(dirScaled);
		//
		//		//		System.err.println("POS: "+getViewable().getPos());
		//		Vector3f up = GlUtil.getUpVector(new Vector3f(), getWorldTransform());
		//		up.x += (0.05f);
		//		up.y += (0.05f);
		//		up.normalize();
		//		Vector3f right = new Vector3f();
		//
		//		right.cross(dir, up);
		//		right.normalize();
		//
		//
		//
		//		up.cross(right, dir);
		//		up.normalize();
		//
		//		GlUtil.setForwardVector(dir, getWorldTransform());
		//		GlUtil.setUpVector(up, getWorldTransform());
		//		GlUtil.setRightVector(right, getWorldTransform());

		updateMouseWheel();

		Vector3f upVector = GlUtil.getUpVector(new Vector3f(), obj.getWorldTransform());
		Vector3f pos = new Vector3f(obj.getWorldTransform().origin);
		float size = 20;
		//
		//		setCameraOffset(size);
		Matrix3f r = new Matrix3f();
		float speed = EngineSettings.ORBITAL_CAM_SPEED.getFloat();
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			speed = 1;
		}
		if(forceRotation != null) {
			r.rotX(forceRotation.x);
			r.rotY(forceRotation.y);
			r.rotZ(forceRotation.z);
		} else r.rotY(FastMath.TWO_PI * percent * speed);

		//		r.mul(t.basis);

		Vector3f forward = new Vector3f(0, 0, size);
		r.transform(forward);

		pos.add(forward);

		//		getWorldTransform().set(GlUtil.lookAtWithoutLoad(0,0,1, pos.x, pos.y, pos.z, 0, 1, 0));

		//		getWorldTransform().origin.set(pos);
		this.m.set(getWorldTransform().basis);
		getWorldTransform().basis.set(obj.getWorldTransform().basis);
		getWorldTransform().basis.mul(r);
		updateViewer(timer);
	}
}