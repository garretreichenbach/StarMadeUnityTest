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

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.SortedList;

/**
 * The Class Animation.
 */
public abstract class AnimationTrack {

	/**
	 * The frames.
	 */
	protected final SortedList<KeyFrame> frames = new SortedList<KeyFrame>();

	/**
	 * The loop.
	 */
	protected boolean loop = true;

	public void addFrame(final float time, final Vector3f translate, final AxisAngle4f rotation, Vector3f scale) {
		frames.add(new KeyFrame(time, translate, rotation, scale));
	}

	public void addFrame(final float time, final Vector3f translate, final Quat4f rotation, Vector3f scale) {
		frames.add(new KeyFrame(time, translate, rotation, scale));
	}

	public void printFrames() {
		int i = 0;
		for (KeyFrame f : frames) {
			System.err.println("FR " + i + ": " + f);
			i++;
		}
	}

	public abstract void setTime(float timeBlendFrom, float f,
	                             AnimationController control, AnimationChannel animationChannel);
}
