/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the description of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.schema.game.client.data.terrain;

import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.forms.Mesh;

/**
 * implements all writeXXXXArray methods to reduce boilerplate code
 * Geomap implementations are encourged to extend this class
 */
public abstract class AbstractGeomap implements Geomap {

	@Override
	public Mesh createMesh(Vector3f scale, Vector2f tcScale, boolean center) {
		//        FloatBuffer pb = writeVertexArray(null, scale, center);
		//        FloatBuffer tb = writeTexCoordArray(null, Vector2f.ZERO, tcScale);
		//        FloatBuffer nb = writeNormalArray(null, scale);
		//        IntBuffer ib = writeIndexArray(null);
		Mesh m = new Mesh();
		//        m.setBuffer(Type.Position, 3, pb);
		//        m.setBuffer(Type.Normal, 3, nb);
		//        m.setBuffer(Type.TexCoord, 2, tb);
		//        m.setBuffer(Type.Index, 3, ib);
		//        m.setStatic();
		//        m.updateBound();
		return m;
	}

	@Override
	public Vector2f getUV(int x, int y, Vector2f store) {
		store.set((float) x / (float) getWidth(),
				(float) y / (float) getHeight());
		return store;
	}

	@Override
	public Vector2f getUV(int i, Vector2f store) {
		return store;
	}

	@Override
	public IntBuffer writeIndexArray(IntBuffer store) {
		int faceN = (getWidth() - 1) * (getHeight() - 1) * 2;

		if (store != null) {
			if (store.remaining() < faceN * 3) {
				throw new BufferUnderflowException();
			}
		} else {
			store = MemoryUtil.memAllocInt(faceN * 3);
		}

		int i = 0;
		for (int z = 0; z < getHeight() - 1; z++) {
			for (int x = 0; x < getWidth() - 1; x++) {
				store.put(i).put(i + getWidth()).put(i + getWidth() + 1);
				store.put(i + getWidth() + 1).put(i + 1).put(i);
				i++;

				// TODO: There's probably a better way to do this..
				if (x == getWidth() - 2) {
					i++;
				}
			}
		}
		store.flip();

		return store;
	}

	/*
	 * (non-Javadoc)
	 * Subclasses are encourged to provide a better implementation
	 * which directly accesses the data rather than using getNormal
	 */
	@Override
	public FloatBuffer writeNormalArray(FloatBuffer store, Vector3f scale) {
		if (!isLoaded()) {
			throw new NullPointerException();
		}

		if (store != null) {
			if (store.remaining() < getWidth() * getHeight() * 3) {
				throw new BufferUnderflowException();
			}
		} else {
			store = MemoryUtil.memAllocFloat(getWidth() * getHeight() * 3);
		}
		store.rewind();

		if (!hasNormalmap()) {
			Vector3f oppositePoint = new Vector3f();
			Vector3f adjacentPoint = new Vector3f();
			Vector3f rootPoint = new Vector3f();
			Vector3f tempNorm = new Vector3f();
			int normalIndex = 0;

			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					rootPoint.set(x, getValue(x, y), y);
					if (y == getHeight() - 1) {
						if (x == getWidth() - 1) { // case #4
							// left cross up
							//                            adj = normalIndex - getWidth();
							//                            opp = normalIndex - 1;
							adjacentPoint.set(x, getValue(x, y - 1), y - 1);
							oppositePoint.set(x - 1, getValue(x - 1, y), y);
						} else { // case #3
							// right cross up
							//                            adj = normalIndex + 1;
							//                            opp = normalIndex - getWidth();
							adjacentPoint.set(x + 1, getValue(x + 1, y), y);
							oppositePoint.set(x, getValue(x, y - 1), y - 1);
						}
					} else {
						if (x == getWidth() - 1) { // case #2
							// left cross down
							adjacentPoint.set(x - 1, getValue(x - 1, y), y);
							oppositePoint.set(x, getValue(x, y + 1), y + 1);
							//                            adj = normalIndex - 1;
							//                            opp = normalIndex + getWidth();
						} else { // case #1
							// right cross down
							adjacentPoint.set(x, getValue(x, y + 1), y + 1);
							oppositePoint.set(x + 1, getValue(x + 1, y), y);
							//                            adj = normalIndex + getWidth();
							//                            opp = normalIndex + 1;
						}
					}

					tempNorm.set(adjacentPoint);
					tempNorm.sub(rootPoint);
					oppositePoint.sub(rootPoint);
					tempNorm.cross(tempNorm, oppositePoint);

					tempNorm.x *= (scale.x);
					tempNorm.y *= (scale.y);
					tempNorm.z *= (scale.z);

					tempNorm.normalize();
					//                    store.put(tempNorm.x).put(tempNorm.y).put(tempNorm.z);
					BufferUtils.setInBuffer(tempNorm, store,
							normalIndex);
					normalIndex++;
				}
			}
		} else {
			Vector3f temp = new Vector3f();
			for (int z = 0; z < getHeight(); z++) {
				for (int x = 0; x < getWidth(); x++) {
					getNormal(x, z, temp);
					store.put(temp.x).put(temp.y).put(temp.z);
				}
			}
		}

		return store;
	}

	/*
	 * (non-Javadoc)
	 * ...
	 */
	@Override
	public FloatBuffer writeTexCoordArray(FloatBuffer store, Vector2f offset, Vector2f scale) {
		if (store != null) {
			if (store.remaining() < getWidth() * getHeight() * 2) {
				throw new BufferUnderflowException();
			}
		} else {
			store = MemoryUtil.memAllocFloat(getWidth() * getHeight() * 2);
		}

		if (offset == null) {
			offset = new Vector2f();
		}

		Vector2f tcStore = new Vector2f();
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				getUV(x, y, tcStore);
				store.put(offset.x + tcStore.x * scale.x);
				store.put(offset.y + tcStore.y * scale.y);

				//                store.put( ((float)x) / getWidth() );
				//                store.put( ((float)y) / getHeight() );
			}

		}

		return store;
	}

	/*
	 * (non-Javadoc)
	 * Subclasses are encourged to provide a better implementation
	 * which directly accesses the data rather than using getHeight
	 */
	@Override
	public FloatBuffer writeVertexArray(FloatBuffer store, Vector3f scale, boolean center) {
		// check now so we don'transformationArray have an NPE at the loop (getHeight call)
		if (!isLoaded()) {
			throw new NullPointerException();
		}

		if (store != null) {
			// you might want to write this data as a part of a larger
			// vertex array/bufferList to reduce draw calls and that is
			// why remaining() is used instead of limit()
			if (store.remaining() < getWidth() * getHeight() * 3) {
				throw new BufferUnderflowException();
			}
		} else {
			// allocate a new bufferList, this bufferList is accessible to the user
			// through the return of this method..
			store = MemoryUtil.memAllocFloat(getWidth() * getHeight() * 3);
		}

		Vector3f offset = new Vector3f(-getWidth() * scale.x, 0, -getWidth() * scale.z);
		if (!center) {
			offset = new Vector3f();//offset.zero();
		}

		// going through Y/Z first, scanlines are horizontal
		for (int z = 0; z < getHeight(); z++) {
			for (int x = 0; x < getWidth(); x++) {
				store.put(x * scale.x + offset.x);
				store.put(getValue(x, z) * scale.y);
				store.put(z * scale.z + offset.y);
			}
		}

		return store;
	}

	/**
	 * This one has indexed LOD
	 *
	 * @param store
	 * @return
	 */
	public IntBuffer writeIndexArray2(IntBuffer store) {
		int faceN = (getWidth() - 1) * (getHeight() - 1) * 2;

		final int lod2 = 32;
		final int lod = 1;

		if (store != null) {
			if (store.remaining() < faceN * 3) {
				throw new BufferUnderflowException();
			}
		} else {
			store = MemoryUtil.memAllocInt(faceN * 3);
		}

		for (int z = 0; z < getHeight() - 1; z += lod) {
			for (int x = 0; x < getWidth() - 1; x += lod) {
				int i = x + z * getWidth();
				store.put(i).put(i + getWidth() * lod).put(i + getWidth() * lod + lod);
				store.put(i + getWidth() * lod + lod).put(i + lod).put(i);
			}
		}
		store.flip();

		for (int i = 0; i < store.limit(); i++) {
			int index = store.get();
			if (index < getWidth() - 3 && index % lod2 != 0) {
				store.position(store.position() - 1);
				store.put(index - index % lod2);
			}
		}

		return store;
	}

}
