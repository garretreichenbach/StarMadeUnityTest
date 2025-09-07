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

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * The Class Animation.
 */
public class Animation {

	private final HashMap<String, AnimationTrack> tracks = new HashMap<String, AnimationTrack>();
	/**
	 * The bone description.
	 */

	public float animationLength;
	/**
	 * The loop.
	 */
	protected boolean loop = true;
	private String name;

	public float getLength() {
		return animationLength;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the tracks
	 */
	public HashMap<String, AnimationTrack> getTracks() {
		return tracks;
	}

	/**
	 * Checks if is loop.
	 *
	 * @return true, if is loop
	 */
	public boolean isLoop() {
		return loop;
	}

	/**
	 * Sets the loop.
	 *
	 * @param loop the new loop
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public void setTime(float timeBlendFrom, float blendAmount,
	                    AnimationController control, AnimationChannel animationChannel) {
		if (tracks.isEmpty()) {
			System.err.println("No tracks in animation");
		}
		for (Entry<String, AnimationTrack> t : tracks.entrySet()) {
			//			System.err.println("Track '"+t.getKey()+"': '"+((BoneAnimationTrack)t.getValue()).getBoneName()+"': "+((BoneAnimationTrack)t.getValue()).getTargetBoneIndex());
			//			t.getValue().printFrames();
			t.getValue().setTime(timeBlendFrom, blendAmount, control, animationChannel);
		}

	}

	@Override
	public String toString() {
		return "Anim[" + name + "]";
	}
}
