/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>SpeedSystem</H2>
 * <H3>org.schema.common.util.linAlg</H3>
 * SpeedSystem.java
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
package org.schema.common.util.linAlg;

import org.schema.common.FastMath;

/**
 * this class is for Mathematical computing of BulletTrajectorys
 * <p/>
 * The 2 Possibilities of Systems are: - Atilery Bullets - Guided Missiles
 * <p/>
 * It works by constructing the class with the positions of attacker and target
 * <p/>
 * then, a caller can update his path by calling the following Methods:
 * <p/>
 * getTrack() for missiles getCurveTrack() for Atilery.
 *
 * @author schema
 */

public class SpeedSystem {

	/**
	 * The tar vec.
	 */
	private Vector posVec, tarVec;

	/**
	 * The dir vec.
	 */
	private Vector v, baseVec, dirVec;

	/**
	 * The height.
	 */
	private Vector height; // only for debug

	/**
	 * Instantiates a new speed system.
	 *
	 * @param posVec the pos vec
	 * @param tarVec the tar vec
	 */
	public SpeedSystem(Vector posVec, Vector tarVec) {
		this.posVec = posVec;
		this.tarVec = tarVec;
		height = new Vector(0, 0);
		baseVec = getDirVec().getNormalized();
		// Logger.println(this,"pos: "+posVec.getX()/24+", "+posVec.getY()/24+
		// "  dir: "+baseVec.getX()/24+", "+baseVec.getY()/24);
	}

	/**
	 * the Base Vector. also known in the Parameterized View of a Vector
	 * consisting of a base Vector and a directional Vector
	 *
	 * @return the base Vector of a acceleryted System (a System with a
	 * directional Vector)
	 */
	public Vector getBaseVec() {
		return baseVec;
	}

	/**
	 * Computes the next Vector of an Atilery Bullet Path. AttileryBullets
	 * change their height while flying to the target. The Curve is a Sinus.
	 * Only the y Coordinate is affected by the Height.
	 *
	 * @param velocity (the speed of the bullet)
	 * @param lifeTime (the distance (length of vector between attacker and target))
	 * @param i        (the step of the animation)
	 * @return a Vector for an Atilery Bullet
	 */
	public Vector getCurveTrack(float velocity, float lifeTime, float i) {
		float heightFak = FastMath.sin((2 * FastMath.PI / lifeTime) * i) * 3;
		Vector heightPart = new Vector(0, -heightFak);

		height = height.add(new Vector(0, heightFak));// only for debugging info
		baseVec = baseVec.add(dirVec.mult(velocity).add(heightPart));
		v = baseVec;
		return v;
	}

	/**
	 * the Directional Vector also known in the Parameterized View of a Vector
	 * consisting of a base Vector and a directional Vector.
	 *
	 * @return a normalized Vector from object to target
	 */
	public Vector getDirVec() {
		if (dirVec == null) {
			dirVec = tarVec.sub(posVec).getNormalized();
		}
		return dirVec;
	}

	/**
	 * Use to get a Directional Vector between to Vectors. the Length of the
	 * Vector is the distance between the to Vectors
	 *
	 * @param posVec the pos vec
	 * @param tarVec the tar vec
	 * @return Directional Vector (not normalized)
	 */
	public Vector getDirVec(Vector posVec, Vector tarVec) {
		return (tarVec.sub(posVec));
	}

	/**
	 * only for debugging.
	 *
	 * @return the height
	 */
	public Vector getHeight() {
		return height;
	}

	/**
	 * Gets the pos vec.
	 *
	 * @return the Vector to the first Object
	 */
	public Vector getPosVec() {
		return posVec;
	}

	/**
	 * Sets the pos vec.
	 *
	 * @param posVec the new pos vec
	 */
	public void setPosVec(Vector posVec) {
		this.posVec = posVec;
	}

	/**
	 * Computes the next Vector of an Straight Bullet Path.
	 *
	 * @param velocity (the speed of the bullet)
	 * @return a Vector for a Bullet
	 */
	public Vector getStraightTrack(float velocity) {

		baseVec = baseVec.add(dirVec.mult(velocity));
		v = baseVec;
		return v;
	}

	/**
	 * Gets the tar vec.
	 *
	 * @return the Vector to the second Object
	 */
	public Vector getTarVec() {
		return tarVec;
	}

	/**
	 * Sets the tar vec.
	 *
	 * @param tarVec the new tar vec
	 */
	public void setTarVec(Vector tarVec) {
		this.tarVec = tarVec;
	}

	/**
	 * Used to compute the next Vector (step) of a guided missile trajectory.
	 *
	 * @param velocity (the distance, the missile advances)
	 * @param c        (the amount of degree, the missile changes its course)
	 * @return a Vector the missile goes to
	 */
	public Vector getTrack(float velocity, float c) {
		dirVec = getDirVec().rotate(c);
		baseVec = baseVec.add(dirVec.mult(velocity));
		v = baseVec;
		return v;
	}

}
