/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>PhysicsData</H2>
 * <H3>org.schema.schine.physics</H3>
 * PhysicsData.java
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
package org.schema.schine.physics;

import org.schema.schine.network.Identifiable;

/**
 * The Class PhysicsData.
 */
public abstract class PhysicsData  {

	/**
	 * The game entity.
	 */
	private Physical entity;

	/**
	 * The state.
	 */
	private PhysicsState state;

	/**
	 * Instantiates a new physics data.
	 *
	 * @param n     the game entity
	 * @param state the state
	 */
	public PhysicsData(Physical n, PhysicsState state) {
		this.entity = n;
		this.state = state;
	}


	public int getEntityID() {
		return ((Identifiable) entity).getId();
	}

	public PhysicsState getState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(PhysicsState state) {
		this.state = state;
	}

	/**
	 * Gets the game entity.
	 *
	 * @return the game entity
	 */
	public Physical getEntity() {
		return entity;
	}

	/**
	 * Sets the game entity.
	 *
	 * @param entity the new game entity
	 */
	public void setEntity(Physical gameEntity) {
		this.entity = gameEntity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Entity: " + entity.toString() + "\n";
	}
}
