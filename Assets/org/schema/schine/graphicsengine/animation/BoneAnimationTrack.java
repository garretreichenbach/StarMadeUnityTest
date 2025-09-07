/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Animation</H2>
 * <H3>org.schema.schine.graphicsengine.animation</H3>
 * Animation.java
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
package org.schema.schine.graphicsengine.animation;

import java.util.BitSet;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.forms.Bone;
import org.schema.schine.graphicsengine.forms.Skeleton;

/**
 * The Class Animation.
 */
public class BoneAnimationTrack extends AnimationTrack {

	static final Quat4f IDENTITY = new Quat4f(0, 0, 0, 1);
	/**
	 * The bone description.
	 */
	private final String boneName;
	private int targetBoneIndex = -1;
	private Quat4f tempQ = new Quat4f();
	private Vector3f tempV = new Vector3f();
	private Vector3f tempS = new Vector3f();
	private Quat4f tempQ2 = new Quat4f();
	private Vector3f tempV2 = new Vector3f();
	private Vector3f tempS2 = new Vector3f();

	public BoneAnimationTrack(String boneName) {
		this.boneName = boneName;
	}

	/**
	 * @return the boneName
	 */
	public String getBoneName() {
		return boneName;
	}

	void getRotation(int frame, Quat4f v) {
		v.set(frames.get(frame).getRotation());
	}

	/**
	 * @return the targetBoneIndex
	 */
	public int getTargetBoneIndex() {
		return targetBoneIndex;
	}

	/**
	 * @param targetBoneIndex the targetBoneIndex to set
	 */
	public void setTargetBoneIndex(int targetBoneIndex) {
		this.targetBoneIndex = targetBoneIndex;
	}

	float getTime(int frame) {
		return frames.get(frame).getTime();
	}

	void getTranslation(int frame, Vector3f v) {
		v.set(frames.get(frame).getTranslate());
	}

	void getScale(int frame, Vector3f v) {
		v.set(frames.get(frame).getScale());
	}

	/**
	 * Modify the bone which this track modifies in the skeleton to contain
	 * the correct animation transforms for a given time.
	 * The transforms can be interpolated in some method from the keyframes.
	 *
	 * @param time    the current time of the animation
	 * @param weight  the weight of the animation
	 * @param control
	 * @param channel
	 * @param vars
	 */
	@Override
	public void setTime(float time, float weight, AnimationController control, AnimationChannel channel) {
		setTime(time, control.getSkeleton(), weight, channel.getAffectedBones(), channel.isOverwritePreviousAnimation());
	}

	/**
	 * Modify the bone which this track modifies in the skeleton to contain
	 * the correct animation transforms for a given time.
	 * The transforms can be interpolated in some method from the keyframes.
	 *
	 * @param b
	 */
	public void setTime(float time, Skeleton skeleton, float weight, BitSet affectedBones, boolean overwritingAnimation) {
		if (targetBoneIndex == -1) {
			targetBoneIndex = skeleton.getBoneIndex(boneName);
		}
		if (affectedBones != null && !affectedBones.get(targetBoneIndex)) {
			return;
		}
		Bone target = skeleton.getBones().get(targetBoneIndex);

		int lastFrame = frames.size() - 1;
		if (time < 0 || frames.size() == 1) {
			getRotation(0, tempQ);
			getTranslation(0, tempV);
			getScale(0, tempS);
		} else if (time >= getTime(lastFrame)) {
			getRotation(lastFrame, tempQ);
			getTranslation(lastFrame, tempV);
			getScale(lastFrame, tempS);
		} else {
			int startFrame = 0;
			int endFrame = 1;
			// use lastFrame so we never overflow the array
			int i;
			for (i = 0; i < lastFrame && getTime(i) < time; i++) {
				startFrame = i;
				endFrame = i + 1;
			}

			float blend = (time - getTime(startFrame))
					/ (getTime(endFrame) - getTime(startFrame));

			getRotation(startFrame, tempQ);
			getTranslation(startFrame, tempV);
			getScale(startFrame, tempS);

			getRotation(endFrame, tempQ2);
			getTranslation(endFrame, tempV2);
			getScale(endFrame, tempS2);

			Vector3f m = new Vector3f(tempS);
			m.interpolate(tempS2, blend);
//			if((m.x <= 0.5f || m.y <= 0.5f || m.z <= 0.5f)){
//				System.err.println("MKAMKSM: "+tempS+", "+tempS2+" -> "+m+" "+boneName);
//			}

			Quat4Util.nlerp(tempQ2, blend, tempQ);
			tempV.interpolate(tempV2, blend);
			tempS.interpolate(tempS2, blend);

		}

		if (weight != 1f) {
			IDENTITY.set(0, 0, 0, 1);
			Quat4Util.slerp(IDENTITY, 1f - weight, tempQ);
			tempV.scale(weight);
//			tempS.scale(weight);
		}
		if ((tempS.x <= 0.5f || tempS.y <= 0.5f || tempS.z <= 0.5f)) {
			getScale(lastFrame, tempS2);
//			System.err.println("SCALE NOW: "+tempS+"; "+boneName+"; ");
//			tempS.set(0.5f,0.5f,0.5f);
		}
		target.setAnimTransforms(tempV, tempQ, tempS, overwritingAnimation);
	}

}
