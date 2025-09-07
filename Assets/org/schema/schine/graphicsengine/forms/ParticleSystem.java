/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ParticleSystem</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * ParticleSystem.java
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
package org.schema.schine.graphicsengine.forms;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.*;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.Random;

/**
 * A Particle System is a set of particles witch act concodingly within the
 * system rules.
 *
 * @author schema
 */
public class ParticleSystem implements Drawable, Positionable, ZSortedDrawable {

	/**
	 * The Constant TYPE_CONSTANTFORCE.
	 */
	public static final int TYPE_CONSTANTFORCE = 0;

	/**
	 * The Constant TYPE_EXPLOSION.
	 */
	public static final int TYPE_EXPLOSION = 1;
	/**
	 * The rand.
	 */
	private static Random rand = new Random();
	/**
	 * The type.
	 */
	protected int type;
	/**
	 * The last.
	 */
	protected int last;
	/**
	 * The ps.
	 */
	protected Particle[] ps;
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	Vector3f other = new Vector3f();
	Vector3f here = new Vector3f();
	/**
	 * The spawn max.
	 */
	private int spawnMin, spawnMax;
	/**
	 * The max particles.
	 */
	private int maxParticles = 1;
	private Vector3f pos;
	/**
	 * The start.
	 */
	private int end, start;
	private FloatBuffer bufferedTransformation = MemoryUtil.memAllocFloat(16);
	/**
	 * The sys life time.
	 */
	private float sysLifeTime;
	/**
	 * The sprite.
	 */
	private Sprite[] sprite;
	private boolean constant;
	private int spawnInitial;

	/**
	 * Creates a Particle System with given Type and a given lifetime.
	 *
	 * @param lifeTime the life time
	 * @param type     the type
	 * @param sprite   the sprite
	 */
	public ParticleSystem(int lifeTime, int type, int spawnInitial, int spawnMin, int spawnMax, Sprite... sprite) {
		this.type = type;
		this.sprite = sprite;
		this.sysLifeTime = lifeTime;
		initializeType();
		constant = lifeTime < 0;
		this.spawnMin = spawnMin;
		this.spawnMax = spawnMax;
		this.spawnInitial = spawnInitial;
	}

	/**
	 * Adds the one.
	 */
	private void addOne() {
		// Vector3f nPos = pos;
		// nPos.add(relativeOutputPosition);
		if(type == TYPE_EXPLOSION) {
			ps[last] = new Particle();
			ps[last].lifeTime = sysLifeTime;
			ps[last].speed = 0.0f;
			ps[last].dir = new Vector3f(0, 0, 0);
		} else {
			ps[last] = new Particle();
			ps[last].lifeTime = rand.nextInt(600) + 60;
			ps[last].speed = 100.0f;
			ps[last].dir = new Vector3f((rand.nextFloat() - 0.5f) * 22.5f, rand.nextInt(10) - 2.5f + rand.nextFloat(), (rand.nextFloat() - 0.5f) * 22.5f);
		}

		ps[last].setPos(new Vector3f(pos));
		last++;

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		sprite[0].setBillboard(true);

		if (last > 0) {
			GlUtil.glPushMatrix();

			GlUtil.glDisable(GL11.GL_DEPTH_TEST);

			GlUtil.glDepthMask(false);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			sprite[0].setFlip(true);
			Sprite.draw(sprite[0], last, ps);

			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDepthMask(true);

			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glPopMatrix();
		}
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.opengl
	 * .GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {

	}

	@Override
	public int compareTo(ZSortedDrawable o) {
		if (o.getBufferedTransformationPosition() == null) {
			return Integer.MIN_VALUE;
		}
		other.set(o.getBufferedTransformationPosition());
		other.sub(Controller.getCamera().getPos());
		here.set(this.getBufferedTransformationPosition());
		here.sub(Controller.getCamera().getPos());
		return FastMath.round(other.length() - here.length());
	}

	@Override
	public void drawZSorted() {
//		getBufferedTransformation().rewind();
//		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, getBufferedTransformation());
//		AbstractScene.zSortedMap.add(this);
	}

	/**
	 * @return the bufferedTransformation
	 */
	@Override
	public FloatBuffer getBufferedTransformation() {
		return bufferedTransformation;
	}

	@Override
	public Vector3f getBufferedTransformationPosition() {
		Vector3f pos = new Vector3f(bufferedTransformation.get(3), bufferedTransformation.get(7), bufferedTransformation.get(11));
		return pos;
	}

	/**
	 * @param bufferedTransformation the bufferedTransformation to set
	 */
	public void setBufferedTransformation(FloatBuffer bufferedTransformation) {
		this.bufferedTransformation = bufferedTransformation;
	}

	/**
	 * Erase one.
	 *
	 * @param which the which
	 */
	private void eraseOne(int which) {
		if (!isEmpty()) {
			ps[which] = ps[last - 1]; // switch last with the one to be deleted
			last--; // and kick it out of the range
		}
	}

	/**
	 * Gets the max particles.
	 *
	 * @return the max particles
	 */
	public int getMaxParticles() {
		return maxParticles;
	}

	/**
	 * Sets the max particles.
	 *
	 * @param maxParticles the new max particles
	 */
	public void setMaxParticles(int maxParticles) {
		this.maxParticles = maxParticles;
	}

	@Override
	public Vector3f getPos() {
		return pos;
	}

	public void setPos(Vector3f pos) {
		this.pos = pos;

	}

	/**
	 * Gets the spawn max.
	 *
	 * @return the spawn max
	 */
	public int getSpawnMax() {
		return spawnMax;
	}

	/**
	 * Sets the spawn max.
	 *
	 * @param spawnMax the new spawn max
	 */
	public void setSpawnMax(int spawnMax) {
		this.spawnMax = spawnMax;
	}

	/**
	 * Gets the spawn min.
	 *
	 * @return the spawn min
	 */
	public int getSpawnMin() {
		return spawnMin;
	}

	/**
	 * Sets the spawn min.
	 *
	 * @param spawnMin the new spawn min
	 */
	public void setSpawnMin(int spawnMin) {
		this.spawnMin = spawnMin;
	}

	/**
	 * @return the sprite
	 */
	public Sprite[] getSprite() {
		return sprite;
	}

	/**
	 * @param sprite the sprite to set
	 */
	public void setSprite(Sprite[] sprite) {
		this.sprite = sprite;
	}

	/**
	 * Gets the sys life time.
	 *
	 * @return the sys life time
	 */
	public float getSysLifeTime() {
		return sysLifeTime;
	}

	/**
	 * Sets the sys life time.
	 *
	 * @param sysLifeTime the new sys life time
	 */
	public void setSysLifeTime(float sysLifeTime) {
		this.sysLifeTime = sysLifeTime;
	}

	/**
	 * Initialize type.
	 */
	private void initializeType() {

		setSize(maxParticles);
	}

	public boolean isAlive() {
		return constant || sysLifeTime > 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	private boolean isEmpty() {
		return start == last;
	}

	public void setPos(float x, float y, float z) {
		this.pos.set(pos);

	}

	/**
	 * Sets the size.
	 *
	 * @param particles the new size
	 */
	private void setSize(int particles) {
		last = start = 0;
		// set end
		end = start + particles;
		ps = new Particle[end + 1];

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(float)
	 */
	public void update(Timer timer) {

		// relativeOutputPosition = pos;
		// create a random number of new particles every frame
		if (sysLifeTime > 0 || constant) {
			int val = spawnInitial;
			spawnInitial = 0;
			if (spawnMax <= spawnMin) {
				val += 0;
			} else {
				val += rand.nextInt(spawnMax - spawnMin);
			}
			val += spawnMin;
			// System.err.println("-! WARNING: unknown ParticleSystem type: "+type
			// );
			//			System.err.println("spawning "+val+" particles "+pos);
			for (int i = 0; i < val; ++i) {
				if (last < maxParticles) {
					addOne();
				}
			}
			sysLifeTime = Math.max(sysLifeTime - timer.getDelta() * 1000, 0);
		}

		updateParticles(timer);

	}

	/**
	 * Update particles.
	 */
	private void updateParticles(Timer timer) {

		int pointer = start;

		while (pointer < last) {
			Particle p = ps[pointer];

			if (p.dir.lengthSquared() > 0) {
				p.dir.normalize();
				p.dir.scale((float) (timer.getDelta() * 1000.0));
				p.dir.scale(p.speed);
				p.getPos().add(p.dir);
			}

			if (p.lifeTime > 0) {
				p.lifeTime -= timer.getDelta(); // frametime
				switch(type) {
					case (TYPE_CONSTANTFORCE) -> {
						p.speed /= timer.getDelta();
						break;
					}
					case (TYPE_EXPLOSION) -> {
						p.speed /= timer.getDelta();
						break;
					}
					default -> System.err.println("-! WARNING: unknown ParticleSystem type: " + type);
				}

				pointer++;
			} else {
				eraseOne(pointer);
			}

		}
		// System.err.println(pointer + " Particles in System updated");
	}

}
