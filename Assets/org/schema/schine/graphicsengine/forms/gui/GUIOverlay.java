/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>GUIOverlay</H2>
 * <H3>org.schema.schine.graphicsengine.forms.gui</H3>
 * GUIOverlay.java
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
package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientStateInterface;

/**
 * The Class GUIOverlay.
 */
public class GUIOverlay extends GUIElement implements TooltipProvider, GUIButtonInterface {

	/**
	 * The sprite.
	 */
	protected Sprite sprite;

	private int spriteSubIndex = -1;

	private float height;

	private float width;

	private boolean invisible;



	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */

	/**
	 * Instantiates a new gUI overlay.
	 *
	 * @param sprite the sprite
	 * @param canvas the canvas
	 */
	public GUIOverlay(Sprite sprite, InputState state) {
		super(state);

		this.sprite = sprite;
		//		double sqrt = Math.sqrt(sprite.getMultiSpriteMax());
		//		System.err.println("Sprite size: "+sprite.getWidth()+"x"+sprite.getHeight());
		//		this.height =  (float) (sprite.getHeight()/sqrt);
		//		this.width =  (float) (sprite.getWidth()/sqrt);
		this.height = sprite.getHeight();
		this.width = sprite.getWidth();
	}

	@Override
	public void cleanUp() {

	}

	//	@Override
	//	public int compareTo(ZSortedDrawable o) {
	//		return 1;
	//	}

	@Override
	public void draw() {
		if (invisible) {
			return;
		}
		if(translateOnlyMode && isTransformRotScaleIdentity()){
			drawRaw();
			return;
		}
		GlUtil.glPushMatrix();

		if (isRenderable()) {
			setInside(false);
		}
		if (spriteSubIndex > -1) {
			assert (spriteSubIndex >= 0) : spriteSubIndex;
			sprite.setSelectedMultiSprite(spriteSubIndex);
		}

		transform();
		if (isRenderable()) {

			sprite.draw();

			if (isMouseUpdateEnabled()) {
				checkMouseInside();
			}
		}

		for (AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();

	}



	@Override
	public void onInit() {

	}

	@Override
	protected void doOrientation() {
		
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public boolean isPositionCenter() {
		return sprite.isPositionCenter();
	}

	@Override
	public String getName() {
		return "(s" + spriteSubIndex + ")" + super.getName();
	}

	@Override
	public boolean isInvisible() {
		return invisible;
	}

	/**
	 * @param invisible the invisible to set
	 */
	@Override
	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	/**
	 * Gets the sprite.
	 *
	 * @return the sprite
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Sets the sprite.
	 *
	 * @param sprite the sprite to set
	 */
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	/**
	 * @return the spriteSubIndex
	 */
	public int getSpriteSubIndex() {
		return spriteSubIndex;
	}

	/**
	 * @param spriteSubIndex the spriteSubIndex to set
	 */
	public void setSpriteSubIndex(int spriteSubIndex) {
		assert (spriteSubIndex >= 0) : this.spriteSubIndex;
		assert (spriteSubIndex <= sprite.getMultiSpriteMax()) : spriteSubIndex + " / " + sprite.getMultiSpriteMax();
		this.spriteSubIndex = spriteSubIndex;
	}

	/**
	 * Update gui.
	 *
	 * @param gl    the gl
	 * @param glu   the glu
	 * @param state the state
	 */
	public void updateGUI(ClientStateInterface state) {
		for (AbstractSceneNode f : getChilds()) {
			((GUIOverlay) f).updateGUI(state);
		}
	}

	@Override
	public void drawToolTip() {
		if (invisible) {
			return;
		}

		for (AbstractSceneNode f : getChilds()) {
			if (f instanceof TooltipProvider) {
				((TooltipProvider) f).drawToolTip();
			}
		}

	}

	public void drawRaw() {
		if (invisible) {
			return;
		}
		if(translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			transform();
		}

		if (isRenderable()) {
			setInside(false);
		}
		if (spriteSubIndex > -1) {
			assert (spriteSubIndex >= 0) : spriteSubIndex;
			sprite.setSelectedMultiSprite(spriteSubIndex);
		}

		
		if (isRenderable()) {
			sprite.drawRaw();
			if (isMouseUpdateEnabled()) {
				checkMouseInside();
			}
		}

		for (AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		if(translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}
	}

}
