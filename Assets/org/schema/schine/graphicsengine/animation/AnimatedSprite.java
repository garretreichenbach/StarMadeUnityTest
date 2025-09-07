/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>AnimatedSprite</H2>
 * <H3>org.schema.schine.graphicsengine.animation</H3>
 * AnimatedSprite.java
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
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
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

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.Texture;

/**
 * The Class AnimatedSprite.
 */
public class AnimatedSprite extends Sprite {

	/**
	 * The looped.
	 */
	private boolean looped = false;

	/**
	 * The max sprite num.
	 */
	private int maxSpriteNum;

	/**
	 * The frame speed.
	 */
	private float frameSpeed;

	/**
	 * The speed.
	 */
	private float speed = 1f;

	private boolean alive = true;

	/**
	 * Instantiates a new animated sprite.
	 *
	 * @param tex the tex
	 */
	public AnimatedSprite(Texture tex) {
		super(tex);
	}

	/**
	 * Gets the max sprite num.
	 *
	 * @return the max sprite num
	 */
	public int getMaxSpriteNum() {
		return maxSpriteNum;
	}

	/**
	 * Sets the max sprite num.
	 *
	 * @param maxSpriteNum the new max sprite num
	 */
	public void setMaxSpriteNum(int maxSpriteNum) {
		this.maxSpriteNum = maxSpriteNum;
	}

	/**
	 * Gets the speed.
	 *
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed.
	 *
	 * @param speed the new speed
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	/**
	 * Checks if is looped.
	 *
	 * @return true, if is looped
	 */
	public boolean isLooped() {
		return looped;
	}

	/**
	 * Sets the looped.
	 *
	 * @param looped the new looped
	 */
	public void setLooped(boolean looped) {
		this.looped = looped;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Sprite#update(float)
	 */
	@Override
	public void update(Timer timer) {
		if (alive) {
			if (frameSpeed > speed) {
				animationNumber++;
				animationNumber %= maxSpriteNum;
				// System.err.println("-- AnimatedSprite "+animationNumber+"/"+(
				// maxSpriteNum));
				if (animationNumber == 0 && !looped) {
					alive = false;
				}
				frameSpeed = 0;
			} else {
				frameSpeed += timer.getDelta(); // FIXME bad solution, because all
			}
		}
		// System.err.println("-- Spritenumber now: "+getSpriteNumber());
	}

}
