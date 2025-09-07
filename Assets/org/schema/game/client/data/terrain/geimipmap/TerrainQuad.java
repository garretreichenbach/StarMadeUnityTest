package org.schema.game.client.data.terrain.geimipmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.client.data.terrain.LODLoadableInterface;
import org.schema.game.client.data.terrain.geimipmap.lodcalc.LodCalculatorFactory;
import org.schema.game.client.data.terrain.geimipmap.lodcalc.LodDistanceCalculatorFactory;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.SceneNode;

/**
 * A terrain quad is a node in the quad tree of the terrain system.
 * The root terrain quad will be the only one that receives the update() call every frame
 * and it will determine if there has been any LOD change.
 * <p/>
 * The leaves of the terrain quad tree are OldTerrain Patches. These have the real geometry cubeMeshes.
 *
 * @author Brent Owens
 */
public class TerrainQuad extends SceneNode {//implements OldTerrain {

	protected Vector2f offset;
	protected int totalSize;
	protected int size;
	protected Vector3f stepScale;
	protected float offsetAmount;
	protected short quadrant = 1;
	protected LodCalculatorFactory lodCalculatorFactory;
	protected List<Vector3f> lastCameraLocations; // used for LOD calc
	private boolean lodCalcRunning = false;
	private boolean usingLOD = true;
	private ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		Thread th = new Thread(r);
		th.setDaemon(true);
		return th;
	});
	private HashMap<String, UpdatedTerrainPatch> updatedPatches;
	private Object updatePatchesLock = new Object();
	private int maxLod = -1;
	private LODLoadableInterface terrain;

	public TerrainQuad() {
		this.setName("OldTerrain");
	}

	protected TerrainQuad(String name, int blockSize, int size,
	                      Vector3f stepScale, float[] heightMap, int totalSize,
	                      Vector2f offset, float offsetAmount,
	                      LodCalculatorFactory lodCalculatorFactory, LODLoadableInterface terrain) {
		this(name, size, stepScale, totalSize, offset, offsetAmount, lodCalculatorFactory, terrain);
		if (heightMap == null) {
			heightMap = generateDefaultHeightMap(size);
		}
		split(blockSize, heightMap);

		//		Vector3f minThis = new Vector3f();
		//		minThis.x =	0;//offset.x*stepScale.x;
		//		minThis.y = 0;
		//		minThis.z = 0;//offset.y*stepScale.z;
		//
		//
		//		Vector3f maxThis = new Vector3f();
		//
		//		maxThis.x = minThis.x + blockSize*size*stepScale.x;
		//		maxThis.y = minThis.x + blockSize*size*stepScale.y;
		//		maxThis.z = minThis.x + blockSize*size*stepScale.z;
		//		super.setBoundingBox(new BoundingBox(minThis, maxThis));
		//		System.err.println("bb is "+getBoundingBox());

		//fixNormals();
	}

	public TerrainQuad(String name, int blockSize, int size, Vector3f stepScale, float[] heightMap, LodCalculatorFactory lodCalculatorFactory, LODLoadableInterface terrain) {
		this(name, blockSize, size, stepScale, heightMap, size, new Vector2f(), 0, lodCalculatorFactory, terrain);
	}

	public TerrainQuad(String name, int blockSize, int size, Vector3f stepScale, float[] heightMap, LODLoadableInterface terrain) {
		this(name, blockSize, size, stepScale, heightMap, size, new Vector2f(), 0, null, terrain);
	}

	protected TerrainQuad(String name, int size,
	                      Vector3f stepScale, int totalSize,
	                      Vector2f offset, float offsetAmount,
	                      LodCalculatorFactory lodCalculatorFactory,
	                      LODLoadableInterface terrain) {
		this.setName(name);
		this.terrain = terrain;
		if (!FastMath.isPowerOfTwo(size - 1)) {
			throw new RuntimeException("size given: " + size + "  OldTerrain quad sizes may only be (2^N + 1)");
		}

		this.offset = offset;
		this.offsetAmount = offsetAmount;
		this.totalSize = totalSize;
		this.size = size;
		this.stepScale = stepScale;
		this.lodCalculatorFactory = lodCalculatorFactory;

	}

	public static final float[] createHeightSubBlock(float[] heightMap, int x,
	                                                 int y, int side) {
		float[] rVal = new float[side * side];
		int bsize = (int) FastMath.sqrt(heightMap.length);
		int count = 0;
		for (int i = y; i < side + y; i++) {
			for (int j = x; j < side + x; j++) {
				if (j < bsize && i < bsize) {
					rVal[count] = heightMap[j + (i * bsize)];
				}
				count++;
			}
		}
		return rVal;
	}

	protected boolean calculateLod(List<Vector3f> location, HashMap<String, UpdatedTerrainPatch> updates) {

		boolean lodChanged = false;

		if (getChilds() != null) {
			for (int i = getChilds().size(); --i >= 0; ) {
				AbstractSceneNode child = getChilds().get(i);
				if (child instanceof TerrainQuad) {
					boolean b = ((TerrainQuad) child).calculateLod(location, updates);
					if (b) {
						lodChanged = true;
					}
				} else if (child instanceof TerrainPatch) {
					boolean b = ((TerrainPatch) child).calculateLod(location, updates);
					if (b) {
						lodChanged = true;
					}
				}
			}
		}

		return lodChanged;
	}

	private List<Vector3f> cloneVectorList(List<Vector3f> locations) {
		List<Vector3f> cloned = new ArrayList<Vector3f>();
		for (Vector3f l : locations) {
			cloned.add(new Vector3f(l));
		}
		return cloned;
	}

	/**
	 * <code>createQuadPage</code> generates four new pages from this page.
	 */
	protected void createQuad(int blockSize, float[] heightMap) {
		// create 4 terrain pages
		int quarterSize = size >> 2;

		int split = (size + 1) >> 1;

		Vector2f tempOffset = new Vector2f();
		offsetAmount += quarterSize;

		if (lodCalculatorFactory == null) {
			lodCalculatorFactory = new LodDistanceCalculatorFactory(); // set a default one
		}

		// 1 upper left
		float[] heightBlock1 = createHeightSubBlock(heightMap, 0, 0, split);

		Vector3f origin1 = new Vector3f(-quarterSize * stepScale.x, 0,
				-quarterSize * stepScale.z);

		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin1.x;
		tempOffset.y += origin1.z;

		TerrainQuad page1 = new TerrainQuad(getName() + "Quad1", blockSize,
				split, stepScale, heightBlock1, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory, terrain);
		page1.setPos(origin1);
		page1.quadrant = 1;
		this.attach(page1);

		// 2 lower left
		float[] heightBlock2 = createHeightSubBlock(heightMap, 0, split - 1,
				split);

		Vector3f origin2 = new Vector3f(-quarterSize * stepScale.x, 0,
				quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin2.x;
		tempOffset.y += origin2.z;

		TerrainQuad page2 = new TerrainQuad(getName() + "Quad2", blockSize,
				split, stepScale, heightBlock2, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory, terrain);
		page2.setPos(origin2);
		page2.quadrant = 2;
		this.attach(page2);

		// 3 upper right
		float[] heightBlock3 = createHeightSubBlock(heightMap, split - 1, 0,
				split);

		Vector3f origin3 = new Vector3f(quarterSize * stepScale.x, 0,
				-quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin3.x;
		tempOffset.y += origin3.z;

		TerrainQuad page3 = new TerrainQuad(getName() + "Quad3", blockSize,
				split, stepScale, heightBlock3, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory, terrain);
		page3.setPos(origin3);
		page3.quadrant = 3;
		this.attach(page3);
		// //
		// 4 lower right
		float[] heightBlock4 = createHeightSubBlock(heightMap, split - 1,
				split - 1, split);

		Vector3f origin4 = new Vector3f(quarterSize * stepScale.x, 0,
				quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin4.x;
		tempOffset.y += origin4.z;

		TerrainQuad page4 = new TerrainQuad(getName() + "Quad4", blockSize,
				split, stepScale, heightBlock4, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory, terrain);
		page4.setPos(origin4);
		page4.quadrant = 4;
		this.attach(page4);

	}

	/**
	 * <code>createQuadBlock</code> creates four child blocks from this page.
	 */
	protected void createQuadPatch(float[] heightMap) {
		// create 4 terrain blocks
		int quarterSize = size >> 2;
		int halfSize = size >> 1;
		int split = (size + 1) >> 1;

		if (lodCalculatorFactory == null) {
			lodCalculatorFactory = new LodDistanceCalculatorFactory(); // set a default one
		}

		offsetAmount += quarterSize;

		// 1 upper left
		float[] heightBlock1 = createHeightSubBlock(heightMap, 0, 0, split);

		Vector3f origin1 = new Vector3f(-halfSize * stepScale.x, 0, -halfSize
				* stepScale.z);

		Vector2f tempOffset1 = new Vector2f();
		tempOffset1.x = offset.x;
		tempOffset1.y = offset.y;
		tempOffset1.x += origin1.x / 2;
		tempOffset1.y += origin1.z / 2;

		TerrainPatch patch1 = new TerrainPatch(getName() + "Patch1", split,
				stepScale, heightBlock1, origin1, totalSize, tempOffset1,
				offsetAmount, terrain);
		patch1.setQuadrant((short) 1);
		this.attach(patch1);
		patch1.setBoundingBox(new BoundingBox());
		patch1.updateModelBound();
		patch1.setLodCalculator(lodCalculatorFactory.createCalculator(patch1));

		// 2 lower left
		float[] heightBlock2 = createHeightSubBlock(heightMap, 0, split - 1,
				split);

		Vector3f origin2 = new Vector3f(-halfSize * stepScale.x, 0, 0);

		Vector2f tempOffset2 = new Vector2f();
		tempOffset2.x = offset.x;
		tempOffset2.y = offset.y;
		tempOffset2.x += origin1.x / 2;
		tempOffset2.y += quarterSize * stepScale.z;

		TerrainPatch patch2 = new TerrainPatch(getName() + "Patch2", split,
				stepScale, heightBlock2, origin2, totalSize, tempOffset2,
				offsetAmount, terrain);
		patch2.setQuadrant((short) 2);
		this.attach(patch2);
		patch2.setBoundingBox(new BoundingBox());
		patch2.updateModelBound();
		patch2.setLodCalculator(lodCalculatorFactory.createCalculator(patch2));

		// 3 upper right
		float[] heightBlock3 = createHeightSubBlock(heightMap, split - 1, 0,
				split);

		Vector3f origin3 = new Vector3f(0, 0, -halfSize * stepScale.z);

		Vector2f tempOffset3 = new Vector2f();
		tempOffset3.x = offset.x;
		tempOffset3.y = offset.y;
		tempOffset3.x += quarterSize * stepScale.x;
		tempOffset3.y += origin3.z / 2;

		TerrainPatch patch3 = new TerrainPatch(getName() + "Patch3", split,
				stepScale, heightBlock3, origin3, totalSize, tempOffset3,
				offsetAmount, terrain);
		patch3.setQuadrant((short) 3);
		this.attach(patch3);
		patch3.setBoundingBox(new BoundingBox());
		patch3.updateModelBound();
		patch3.setLodCalculator(lodCalculatorFactory.createCalculator(patch3));

		// 4 lower right
		float[] heightBlock4 = createHeightSubBlock(heightMap, split - 1,
				split - 1, split);

		Vector3f origin4 = new Vector3f(0, 0, 0);

		Vector2f tempOffset4 = new Vector2f();
		tempOffset4.x = offset.x;
		tempOffset4.y = offset.y;
		tempOffset4.x += quarterSize * stepScale.x;
		tempOffset4.y += quarterSize * stepScale.z;

		TerrainPatch patch4 = new TerrainPatch(getName() + "Patch4", split,
				stepScale, heightBlock4, origin4, totalSize, tempOffset4,
				offsetAmount, terrain);
		patch4.setQuadrant((short) 4);
		this.attach(patch4);
		patch4.setBoundingBox(new BoundingBox());
		patch4.updateModelBound();
		patch4.setLodCalculator(lodCalculatorFactory.createCalculator(patch4));
	}

	@Override
	public void draw() {
		//		System.err.println("pos "+getPos());
		//		if(isVisibleInFrustum()){
		//		System.err.println("drawing quad");
		GlUtil.glPushMatrix();
		transform();
		for (AbstractSceneNode f : this.getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();
		//		}
	}

	@Override
	public TerrainQuad clone() {
		System.err.println("cloning TerrainQuad " + getChilds().size());
		TerrainQuad quad = new TerrainQuad(new String(getName()),
				size, new Vector3f(stepScale),
				totalSize,
				new Vector2f(offset),
				offsetAmount,
				new LodDistanceCalculatorFactory(), terrain);
		quad.setPos(new Vector3f(getPos()));
		quad.setBoundingBox(quad.getBoundingBox());
		for (int i = 0; i < getChilds().size(); i++) {
			AbstractSceneNode clone = getChilds().get(i).clone();
			System.err.println("attaching " + clone.getClass().getSimpleName());
			quad.attach(clone);
		}
		return quad;
	}

	protected TerrainPatch findDownPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 1) {
			return getPatch(2);
		} else if (tp.getQuadrant() == 3) {
			return getPatch(4);
		} else if (tp.getQuadrant() == 2) {
			// find the page below and ask it for child 1.
			TerrainQuad quad = findDownQuad();
			if (quad != null) {
				return quad.getPatch(1);
			}
		} else if (tp.getQuadrant() == 4) {
			TerrainQuad quad = findDownQuad();
			if (quad != null) {
				return quad.getPatch(3);
			}
		}

		return null;
	}

	protected TerrainQuad findDownQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad)) {
			return null;
		}

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 1) {
			return pQuad.getQuad(2);
		} else if (quadrant == 3) {
			return pQuad.getQuad(4);
		} else if (quadrant == 2) {
			TerrainQuad quad = pQuad.findDownQuad();
			if (quad != null) {
				return quad.getQuad(1);
			}
		} else if (quadrant == 4) {
			TerrainQuad quad = pQuad.findDownQuad();
			if (quad != null) {
				return quad.getQuad(3);
			}
		}

		return null;
	}

	protected TerrainPatch findLeftPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 3) {
			return getPatch(1);
		} else if (tp.getQuadrant() == 4) {
			return getPatch(2);
		} else if (tp.getQuadrant() == 1) {
			// find the page above and ask it for child 2.
			TerrainQuad quad = findLeftQuad();
			if (quad != null) {
				return quad.getPatch(3);
			}
		} else if (tp.getQuadrant() == 2) {
			TerrainQuad quad = findLeftQuad();
			if (quad != null) {
				return quad.getPatch(4);
			}
		}

		return null;
	}

	protected TerrainQuad findLeftQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad)) {
			return null;
		}

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 3) {
			return pQuad.getQuad(1);
		} else if (quadrant == 4) {
			return pQuad.getQuad(2);
		} else if (quadrant == 1) {
			TerrainQuad quad = pQuad.findLeftQuad();
			if (quad != null) {
				return quad.getQuad(3);
			}
		} else if (quadrant == 2) {
			TerrainQuad quad = pQuad.findLeftQuad();
			if (quad != null) {
				return quad.getQuad(4);
			}
		}

		return null;
	}

	protected synchronized void findNeighboursLod(HashMap<String, UpdatedTerrainPatch> updated) {
		if (getChilds() != null) {
			for (int x = getChilds().size(); --x >= 0; ) {
				AbstractSceneNode child = getChilds().get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).findNeighboursLod(updated);
				} else if (child instanceof TerrainPatch) {

					TerrainPatch patch = (TerrainPatch) child;
					if (!patch.searchedForNeighboursAlready) {
						// set the references to the neighbours
						patch.rightNeighbour = findRightPatch(patch);
						patch.bottomNeighbour = findDownPatch(patch);
						patch.leftNeighbour = findLeftPatch(patch);
						patch.topNeighbour = findTopPatch(patch);
						patch.searchedForNeighboursAlready = true;
					}
					TerrainPatch right = patch.rightNeighbour;
					TerrainPatch down = patch.bottomNeighbour;

					UpdatedTerrainPatch utp = updated.get(patch.getName());
					if (utp == null) {
						utp = new UpdatedTerrainPatch(patch, patch.lod);
						updated.put(utp.getName(), utp);
					}

					if (right != null) {
						UpdatedTerrainPatch utpR = updated.get(right.getName());
						if (utpR == null) {
							utpR = new UpdatedTerrainPatch(right, right.lod);
							updated.put(utpR.getName(), utpR);
						}

						utp.setRightLod(utpR.getNewLod());
						utpR.setLeftLod(utp.getNewLod());
					}
					if (down != null) {
						UpdatedTerrainPatch utpD = updated.get(down.getName());
						if (utpD == null) {
							utpD = new UpdatedTerrainPatch(down, down.lod);
							updated.put(utpD.getName(), utpD);
						}

						utp.setBottomLod(utpD.getNewLod());
						utpD.setTopLod(utp.getNewLod());
					}

				}
			}
		}
	}

	protected TerrainPatch findRightPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 1) {
			return getPatch(3);
		} else if (tp.getQuadrant() == 2) {
			return getPatch(4);
		} else if (tp.getQuadrant() == 3) {
			// find the page to the right and ask it for child 1.
			TerrainQuad quad = findRightQuad();
			if (quad != null) {
				return quad.getPatch(1);
			}
		} else if (tp.getQuadrant() == 4) {
			// find the page to the right and ask it for child 2.
			TerrainQuad quad = findRightQuad();
			if (quad != null) {
				return quad.getPatch(2);
			}
		}

		return null;
	}

	protected TerrainQuad findRightQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad)) {
			return null;
		}

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 1) {
			return pQuad.getQuad(3);
		} else if (quadrant == 2) {
			return pQuad.getQuad(4);
		} else if (quadrant == 3) {
			TerrainQuad quad = pQuad.findRightQuad();
			if (quad != null) {
				return quad.getQuad(1);
			}
		} else if (quadrant == 4) {
			TerrainQuad quad = pQuad.findRightQuad();
			if (quad != null) {
				return quad.getQuad(2);
			}
		}

		return null;
	}

	protected TerrainPatch findTopPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 2) {
			return getPatch(1);
		} else if (tp.getQuadrant() == 4) {
			return getPatch(3);
		} else if (tp.getQuadrant() == 1) {
			// find the page above and ask it for child 2.
			TerrainQuad quad = findTopQuad();
			if (quad != null) {
				return quad.getPatch(2);
			}
		} else if (tp.getQuadrant() == 3) {
			TerrainQuad quad = findTopQuad();
			if (quad != null) {
				return quad.getPatch(4);
			}
		}

		return null;
	}

	protected TerrainQuad findTopQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad)) {
			return null;
		}

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 2) {
			return pQuad.getQuad(1);
		} else if (quadrant == 4) {
			return pQuad.getQuad(3);
		} else if (quadrant == 1) {
			TerrainQuad quad = pQuad.findTopQuad();
			if (quad != null) {
				return quad.getQuad(2);
			}
		} else if (quadrant == 3) {
			TerrainQuad quad = pQuad.findTopQuad();
			if (quad != null) {
				return quad.getQuad(4);
			}
		}

		return null;
	}

	/**
	 * Find any neighbours that should have their edges seamed because another neighbour
	 * changed its LOD to a greater value (less detailed)
	 */
	protected synchronized void fixEdges(HashMap<String, UpdatedTerrainPatch> updated) {
		if (getChilds() != null) {
			for (int x = getChilds().size(); --x >= 0; ) {
				AbstractSceneNode child = getChilds().get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).fixEdges(updated);
				} else if (child instanceof TerrainPatch) {
					TerrainPatch patch = (TerrainPatch) child;
					UpdatedTerrainPatch utp = updated.get(patch.getName());

					if (utp.lodChanged()) {
						if (!patch.searchedForNeighboursAlready) {
							// set the references to the neighbours
							patch.rightNeighbour = findRightPatch(patch);
							patch.bottomNeighbour = findDownPatch(patch);
							patch.leftNeighbour = findLeftPatch(patch);
							patch.topNeighbour = findTopPatch(patch);
							patch.searchedForNeighboursAlready = true;
						}
						TerrainPatch right = patch.rightNeighbour;
						TerrainPatch down = patch.bottomNeighbour;
						TerrainPatch top = patch.topNeighbour;
						TerrainPatch left = patch.leftNeighbour;
						if (right != null) {
							UpdatedTerrainPatch utpR = updated.get(right.getName());
							if (utpR == null) {
								utpR = new UpdatedTerrainPatch(right, right.lod);
								updated.put(utpR.getName(), utpR);
							}
							utpR.setFixEdges(true);
						}
						if (down != null) {
							UpdatedTerrainPatch utpD = updated.get(down.getName());
							if (utpD == null) {
								utpD = new UpdatedTerrainPatch(down, down.lod);
								updated.put(utpD.getName(), utpD);
							}
							utpD.setFixEdges(true);
						}
						if (top != null) {
							UpdatedTerrainPatch utpT = updated.get(top.getName());
							if (utpT == null) {
								utpT = new UpdatedTerrainPatch(top, top.lod);
								updated.put(utpT.getName(), utpT);
							}
							utpT.setFixEdges(true);
						}
						if (left != null) {
							UpdatedTerrainPatch utpL = updated.get(left.getName());
							if (utpL == null) {
								utpL = new UpdatedTerrainPatch(left, left.lod);
								updated.put(utpL.getName(), utpL);
							}
							utpL.setFixEdges(true);
						}
					}
				}
			}
		}
	}

	/**
	 * Ignoring the normals for now. The lighting just makes the terrain "pop" noticeably.
	 * Use a lightmap instead.
	 */
	public void fixNormals() {
		/*	if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).fixNormals();
				} else if (child instanceof TerrainPatch) {
					TerrainPatch tb = (TerrainPatch) child;
					TerrainPatch right = findRightPatch(tb);
					TerrainPatch down = findDownPatch(tb);
					int tbSize = tb.getSize();
					if (right != null) {
						float[] normData = new float[3];
						for (int y = 0; y < tbSize; y++) {
							int index1 = ((y + 1) * tbSize) - 1;
							int index2 = (y * tbSize);
							right.getNormalBuffer().position(index2 * 3);
							right.getNormalBuffer().get(normData);
							tb.getNormalBuffer().position(index1 * 3);
							tb.getNormalBuffer().put(normData);
						}
						deleteNormalVBO(right);

					}
					if (down != null) {
						int rowStart = ((tbSize - 1) * tbSize);
						float[] normData = new float[3];
						for (int z = 0; z < tbSize; z++) {
							int index1 = rowStart + z;
							int index2 = z;
							down.getNormalBuffer().position(index2 * 3);
							down.getNormalBuffer().get(normData);
							tb.getNormalBuffer().position(index1 * 3);
							tb.getNormalBuffer().put(normData);
						}
						deleteNormalVBO(down);
					}
					deleteNormalVBO(tb);
				}
			}
		}
		 */
	}

	/**
	 * Create just a flat heightmap
	 */
	private float[] generateDefaultHeightMap(int size) {
		float[] heightMap = new float[size * size];

		return heightMap;
	}

	public float getHeight(float x, float z) {
		// determine which quadrant this is in.
		AbstractSceneNode child = null;
		int split = (size - 1) >> 1;
		float halfmapx = split * stepScale.x, halfmapz = split * stepScale.z;
		float newX = 0, newZ = 0;
		if (x == 0) {
			x += .001f;
		}
		if (z == 0) {
			z += .001f;
		}
		if (x > 0) {
			if (z > 0) {
				// upper right
				child = getChilds().get(3);
				newX = x;
				newZ = z;
			} else {
				// lower right
				child = getChilds().get(2);
				newX = x;
				newZ = z + halfmapz;
			}
		} else {
			if (z > 0) {
				// upper left
				child = getChilds().get(1);
				newX = x + halfmapx;
				newZ = z;
			} else {
				// lower left...
				child = getChilds().get(0);
				if (x == 0) {
					x -= .1f;
				}
				if (z == 0) {
					z -= .1f;
				}
				newX = x + halfmapx;
				newZ = z + halfmapz;
			}
		}
		if (child instanceof TerrainPatch) {
			return ((TerrainPatch) child).getHeight(newX, newZ);
		} else if (child instanceof TerrainQuad) {
			return ((TerrainQuad) child).getHeight(x
					- ((TerrainQuad) child).getPos().x, z
					- ((TerrainQuad) child).getPos().z);
		}
		return Float.NaN;
	}

	public float getHeight(Vector2f xz) {

		return 0;
	}

	public int getMaxLod() {
		if (maxLod < 0) {
			maxLod = Math.max(1, (int) (FastMath.log(size - 1) / FastMath.log(2)) - 1); // -1 forces our minimum of 4 triangles wide
		}

		return maxLod;
	}

	protected TerrainPatch getPatch(int quad) {
		if (getChilds() != null) {
			for (int x = getChilds().size(); --x >= 0; ) {
				AbstractSceneNode child = getChilds().get(x);
				if (child instanceof TerrainPatch) {
					TerrainPatch tb = (TerrainPatch) child;
					if (tb.getQuadrant() == quad) {
						return tb;
					}
				}
			}
		}
		return null;
	}

	protected TerrainQuad getQuad(int quad) {
		if (getChilds() != null) {
			for (int x = getChilds().size(); --x >= 0; ) {
				AbstractSceneNode child = getChilds().get(x);
				if (child instanceof TerrainQuad) {
					TerrainQuad tq = (TerrainQuad) child;
					if (tq.quadrant == quad) {
						return tq;
					}
				}
			}
		}
		return null;
	}

	public short getQuadrant() {
		return quadrant;
	}

	public void setQuadrant(short quadrant) {
		this.quadrant = quadrant;
	}

	private synchronized boolean isLodCalcRunning() {
		return lodCalcRunning;
	}

	private synchronized void setLodCalcRunning(boolean running) {
		lodCalcRunning = running;
	}

	public boolean isUsingLOD() {
		return usingLOD;
	}

	private boolean lastCameraLocationsTheSame(List<Vector3f> locations) {
		boolean theSame = true;
		for (Vector3f l : locations) {
			for (Vector3f v : lastCameraLocations) {
				if (!v.equals(l)) {
					theSame = false;
					return false;
				}
			}
		}
		return theSame;
	}

	protected synchronized void reIndexPages(HashMap<String, UpdatedTerrainPatch> updated) {
		if (getChilds() != null) {
			for (int i = getChilds().size(); --i >= 0; ) {
				AbstractSceneNode child = getChilds().get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).reIndexPages(updated);
				} else if (child instanceof TerrainPatch) {
					((TerrainPatch) child).reIndexGeometry(updated);
				}
			}
		}
	}

	@Override
	public void setBoundingBox(BoundingBox v) {
		for (int i = 0; i < this.getChilds().size(); i++) {
			if (this.getChilds().get(i) instanceof TerrainQuad) {
				((TerrainQuad) getChilds().get(i)).setBoundingBox(v.clone(null));
			} else if (this.getChilds().get(i) instanceof TerrainPatch) {
				((TerrainPatch) getChilds().get(i)).setBoundingBox(v.clone(null));

			}
		}
	}

	public void setHeight(Vector2f xzCoordinate, float height) {

	}

	public void setLodCalculatorFactory(LodCalculatorFactory lodCalculatorFactory) {
		if (getChilds() != null) {
			for (int i = getChilds().size(); --i >= 0; ) {
				AbstractSceneNode child = getChilds().get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).setLodCalculatorFactory(lodCalculatorFactory);
				} else if (child instanceof TerrainPatch) {
					((TerrainPatch) child).setLodCalculator(lodCalculatorFactory.createCalculator((TerrainPatch) child));
				}
			}
		}
	}

	private void setUpdateQuadLODs(HashMap<String, UpdatedTerrainPatch> updated) {
		synchronized (updatePatchesLock) {
			updatedPatches = updated;
		}
	}

	//	@Override
	//	public void read(JmeImporter e) throws IOException {
	//		super.read(e);
	//		InputCapsule c = e.getCapsule(this);
	//		size = c.readInt("size", 0);
	//		stepScale = (Vector3f) c.readSavable("stepScale", null);
	//		offset = (Vector2f) c.readSavable("offset", new Vector2f(0,0));
	//		offsetAmount = c.readInt("offsetAmount", 0);
	//		quadrant = c.readShort("quadrant", (short) 0);
	//	}
	//
	//	@Override
	//	public void write(JmeExporter e) throws IOException {
	//		super.write(e);
	//		OutputCapsule c = e.getCapsule(this);
	//		c.write(size, "size", 0);
	//		c.write(stepScale, "stepScale", null);
	//		c.write(offset, "offset", new Vector2f(0,0));
	//		c.write(offsetAmount, "offsetAmount", 0);
	//		c.write(quadrant, "quadrant", 0);
	//	}

	/**
	 * <code>split</code> divides the heightmap data for four children. The
	 * children are either pages or blocks. This is dependent on the size of the
	 * children. If the child's size is less than or equal to the set block
	 * size, then blocks are created, otherwise, pages are created.
	 *
	 * @param blockSize the blocks size to test against.
	 * @param heightMap the height data.
	 */
	protected void split(int blockSize, float[] heightMap) {
		if ((size >> 1) + 1 <= blockSize) {
			createQuadPatch(heightMap);
		} else {
			createQuad(blockSize, heightMap);
		}

	}

	/**
	 * Call from the update() method of your gamestate to update
	 * the LOD values of each patch.
	 * This will perform the geometry calculation in a background thread and
	 * do the actual update on the opengl thread.
	 *
	 * @param meshPos
	 */
	public void update(List<Vector3f> locations) {

		// update any existing ones that need updating
		updateQuadLODs();

		if (lastCameraLocations != null) {
			if (lastCameraLocationsTheSame(locations)) {
				return; // don'transformationArray update if in same spot
			} else {
				lastCameraLocations = cloneVectorList(locations);
			}
		} else {
			lastCameraLocations = cloneVectorList(locations);
			return;
		}

		if (lodCalcRunning) {
			return;
		}

		if (getParent() instanceof TerrainQuad) {
			return; // we just want the root quad to perform this.
		}

		UpdateLOD updateLodThread = new UpdateLOD(locations);
		executor.execute(updateLodThread);

	}

	public void updateModelBound() {
		for (int i = 0; i < this.getChilds().size(); i++) {
			if (this.getChilds().get(i) instanceof TerrainQuad) {
				((TerrainQuad) getChilds().get(i)).updateModelBound();
			} else if (this.getChilds().get(i) instanceof TerrainPatch) {
				((TerrainPatch) getChilds().get(i)).updateModelBound();

			}
		}
	}

	/**
	 * Back on the ogl thread: update the terrain patch geometries
	 *
	 * @param updatedPatches to be updated
	 */
	private void updateQuadLODs() {
		synchronized (updatePatchesLock) {
			//if (true)
			//	return;
			if (updatedPatches == null || updatedPatches.size() == 0) {
				return;
			}

			//TODO do the actual geometry update here
			for (UpdatedTerrainPatch utp : updatedPatches.values()) {
				utp.updateAll();
			}

			updatedPatches.clear();
		}
	}

	public void useLOD(boolean useLod) {
		usingLOD = useLod;
	}

	/**
	 * Calculates the LOD of all child terrain patches.
	 */
	private class UpdateLOD implements Runnable {
		private List<Vector3f> camLocations;

		UpdateLOD(List<Vector3f> location) {
			camLocations = location;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			if (isLodCalcRunning()) {
				//System.out.println("thread already running");
				return;
			}
			//System.out.println("spawned thread "+toString());
			setLodCalcRunning(true);

			// go through each patch and calculate its LOD based on camera distance
			HashMap<String, UpdatedTerrainPatch> updated = new HashMap<String, UpdatedTerrainPatch>();
			boolean lodChanged = calculateLod(camLocations, updated); // 'updated' gets populated here

			if (!lodChanged) {
				// not worth updating anything else since no one's LOD changed
				setLodCalcRunning(false);
				return;
			}
			// then calculate its neighbour LOD values for seaming in the shader
			findNeighboursLod(updated);

			fixEdges(updated); // 'updated' can get added to here

			reIndexPages(updated);

			setUpdateQuadLODs(updated); // set back to main ogl thread

			setLodCalcRunning(false);
			//double duration = (System.currentTimeMillis()-start);
			//System.out.println("terminated in "+duration);
		}

	}
}

