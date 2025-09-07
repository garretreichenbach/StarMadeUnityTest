package org.schema.schine.graphicsengine.forms.quadtree;

import javax.vecmath.Vector3f;

public class QuadSquare {

	public static boolean Static;
	static Vector3f SunVector = new Vector3f(0.0705f, -0.9875f, -0.1411f);    // For demo lighting.  Pick some unit vector pointing roughly downward.
	static float DetailThreshold = 100;
	//const float	VERTICAL_SCALE = 1.0 / 8.0;
	final float VERTICAL_SCALE = 1.0f;
	public QuadSquare[] Child = new QuadSquare[4];
	public VertInfo[] Vertex = new VertInfo[5]; // center, e, n, w, s
	public float[] Error = new float[6]; // e, s, children: ne, nw, sw, se
	public int MinY; // Bounds for frustum culling and error testing.
	public int MaxY;
	public byte EnabledFlags; // bits 0-7: e, n, w, s, ne, nw, sw, se
	public byte[] SubEnabledCount = new byte[2]; // e, s enabled reference counts.
	public boolean Dirty; // Set when vertex data has changed, but error/enabled data has not been recalculated.
	float[] VertexArray = new float[9 * 3];
	int[] ColorArray = new int[9];
	int[] VertList = new int[24];
	int TriCount = 0;
	int MaxCreateDepth = 0;
	int BlockDeleteCount = 0;    //xxxxx
	int BlockUpdateCount = 0;    //xxxxx
	private int vcount;

	public QuadSquare(quadcornerdata pcd)
	// finalructor.
	{
		pcd.Square = this;

		// Set static to true if/when this node contains real data, and
		// not just interpolated values.  When static == false, a node
		// can be deleted by the Update() function if none of its
		// vertices or children are enabled.
		Static = false;

		int i;
		for (i = 0; i < 4; i++) {
			Child[i] = null;
		}

		EnabledFlags = 0;

		for (i = 0; i < 2; i++) {
			SubEnabledCount[i] = 0;
		}

		// Set default vertex positions by interpolating from given corners.
		// Just bilinear interpolation.
		Vertex[0].Y = (int) (0.25 * (pcd.Verts[0].Y + pcd.Verts[1].Y + pcd.Verts[2].Y + pcd.Verts[3].Y));
		Vertex[1].Y = (int) (0.5 * (pcd.Verts[3].Y + pcd.Verts[0].Y));
		Vertex[2].Y = (int) (0.5 * (pcd.Verts[0].Y + pcd.Verts[1].Y));
		Vertex[3].Y = (int) (0.5 * (pcd.Verts[1].Y + pcd.Verts[2].Y));
		Vertex[4].Y = (int) (0.5 * (pcd.Verts[2].Y + pcd.Verts[3].Y));

		for (i = 0; i < 2; i++) {
			Error[i] = 0;
		}
		for (i = 0; i < 4; i++) {
			Error[i + 2] = Math.abs((Vertex[0].Y + pcd.Verts[i].Y) - (Vertex[i + 1].Y + Vertex[((i + 1) & 3) + 1].Y)) * 0.25f;
		}

		// Compute MinY/MaxY based on corner verts.
		MinY = MaxY = pcd.Verts[0].Y;
		for (i = 1; i < 4; i++) {
			float y = pcd.Verts[i].Y;
			if (y < MinY) {
				MinY = (int) y;
			}
			if (y > MaxY) {
				MaxY = (int) y;
			}
		}

		// Initialize colors by interpolating from corners.
		Vertex[0].Lightness = (pcd.Verts[0].Lightness + pcd.Verts[1].Lightness + pcd.Verts[2].Lightness + pcd.Verts[3].Lightness) >> 2;
		Vertex[1].Lightness = (pcd.Verts[3].Lightness + pcd.Verts[0].Lightness) >> 1;
		Vertex[2].Lightness = (pcd.Verts[0].Lightness + pcd.Verts[1].Lightness) >> 1;
		Vertex[3].Lightness = (pcd.Verts[1].Lightness + pcd.Verts[2].Lightness) >> 1;
		Vertex[4].Lightness = (pcd.Verts[2].Lightness + pcd.Verts[3].Lightness) >> 1;
	}

	void AddHeightMap(final quadcornerdata cd, final HeightMapInfo hm)
	// Sets the height of all samples within the specified rectangular
	// region using the given array of floats.  Extends the tree to the
	// level of detail defined by (1 << hm.Scale) as necessary.
	{
		// If block is outside rectangle, then don'transformationArray bother.
		int BlockSize = 2 << cd.Level;
		if (cd.xorg > hm.XOrigin + ((hm.XSize + 2) << hm.Scale) ||
				cd.xorg + BlockSize < hm.XOrigin - (1 << hm.Scale) ||
				cd.zorg > hm.ZOrigin + ((hm.ZSize + 2) << hm.Scale) ||
				cd.zorg + BlockSize < hm.ZOrigin - (1 << hm.Scale)) {
			// This square does not touch the given height array area; no need to modify this square or descendants.
			return;
		}

		if (cd.Parent != null && cd.Parent.Square != null) {
			cd.Parent.Square.EnableChild(cd.ChildIndex, cd.Parent);    // causes parent edge verts to be enabled, possibly causing neighbor blocks to be created.
		}

		int i;

		int half = 1 << cd.Level;

		// Create and update child nodes.
		for (i = 0; i < 4; i++) {
			quadcornerdata q = null;
			SetupCornerData(q, cd, i);

			if (Child[i] == null && cd.Level > hm.Scale) {
				// Create child node w/ current (unmodified) values for corner verts.
				Child[i] = new QuadSquare(q);
			}

			// Recurse.
			if (Child[i] != null) {
				Child[i].AddHeightMap(q, hm);
			}
		}

		// Deviate vertex heights based on data sampled from heightmap.
		float s[] = new float[5];
		s[0] = hm.Sample(cd.xorg + half, cd.zorg + half);
		s[1] = hm.Sample(cd.xorg + half * 2, cd.zorg + half);
		s[2] = hm.Sample(cd.xorg + half, cd.zorg);
		s[3] = hm.Sample(cd.xorg, cd.zorg + half);
		s[4] = hm.Sample(cd.xorg + half, cd.zorg + half * 2);

		// Modify the vertex heights if necessary, and set the dirty
		// flag if any modifications occur, so that we know we need to
		// recompute error data later.
		for (i = 0; i < 5; i++) {
			if (s[i] != 0) {
				Dirty = true;
				Vertex[i].Y += s[i];
			}
		}

		if (!Dirty) {
			// Check to see if any child nodes are dirty, and set the dirty flag if so.
			for (i = 0; i < 4; i++) {
				if (Child[i] != null && Child[i].Dirty) {
					Dirty = true;
					break;
				}
			}
		}

		if (Dirty) {
			SetStatic(cd);
		}
	}

	boolean BoxTest(float x, float z, float size, float miny, float maxy, float error, final float Viewer[])
	// Returns true if any vertex within the specified box (origin at x,z,
	// edges of length size) with the given error value could be enabled
	// based on the given viewer location.
	{
		// Find the minimum distance to the box.
		float half = size * 0.5f;
		float dx = Math.abs(x + half - Viewer[0]) - half;
		float dy = Math.abs((miny + maxy) * 0.5f - Viewer[1]) - (maxy - miny) * 0.5f;
		float dz = Math.abs(z + half - Viewer[2]) - half;
		float d = dx;
		if (dy > d) {
			d = dy;
		}
		if (dz > d) {
			d = dz;
		}

		return (error * DetailThreshold) > d;
	}

	int CountNodes()
	// Debugging function.  Counts the number of nodes in this subtree.
	{
		int count = 1;    // Count ourself.

		// Count descendants.
		for (int i = 0; i < 4; i++) {
			if (Child[i] != null) {
				count += Child[i].CountNodes();
			}
		}

		return count;
	}

	void CreateChild(int index, final quadcornerdata cd)
	// Creates a child square at the specified index.
	{
		if (Child[index] == null) {
			quadcornerdata q = null;
			SetupCornerData(q, cd, index);

			Child[index] = new QuadSquare(q);
		}
	}

	void EnableChild(int index, final quadcornerdata cd)
	// Enable the indexed child node.  { ne, nw, sw, se }
	// Causes dependent edge vertices to be enabled.
	{
		//		if (Enabled[index + 4] == false) {
		if ((EnabledFlags & (16 << index)) == 0) {
			//			Enabled[index + 4] = true;
			EnabledFlags |= (16 << index);
			EnableEdgeVertex(index, true, cd);
			EnableEdgeVertex((index + 1) & 3, true, cd);

			if (Child[index] == null) {
				CreateChild(index, cd);
			}
		}
	}

	QuadSquare EnableDescendant(int count, int path[], final quadcornerdata cd)
	// This function enables the descendant node 'count' generations below
	// us, located by following the list of child indices in path[].
	// Creates the node if necessary, and returns a pointer to it.
	{
		count--;
		int ChildIndex = path[count];

		if ((EnabledFlags & (16 << ChildIndex)) == 0) {
			EnableChild(ChildIndex, cd);
		}

		if (count > 0) {
			quadcornerdata q = null;
			SetupCornerData(q, cd, ChildIndex);
			return Child[ChildIndex].EnableDescendant(count, path, q);
		} else {
			return Child[ChildIndex];
		}
	}

	// Enable the specified edge vertex.  Indices go { e, n, w, s }.
	// Increments the appropriate reference-count if IncrementCount is true.
	void EnableEdgeVertex(int index, boolean IncrementCount, final quadcornerdata cd) {
		if (((EnabledFlags & (1 << index)) != 0) && IncrementCount == false) {
			return;
		}

		final int Inc[] = new int[]{1, 0, 0, 8};

		// Turn on flag and deal with reference count.
		EnabledFlags |= 1 << index;
		if (IncrementCount == true && (index == 0 || index == 3)) {
			SubEnabledCount[index & 1]++;
		}

		// Now we need to enable the opposite edge vertex of the adjacent square (i.e. the alias vertex).

		// This is a little tricky, since the desired neighbor node may not exist, in which
		// case we have to create it, in order to prevent cracks.  Creating it may in turn cause
		// further edge vertices to be enabled, propagating updates through the tree.

		// The sticking point is the quadcornerdata list, which
		// conceptually is just a linked list of activation structures.
		// In this function, however, we will introduce branching into
		// the "list", making it in actuality a tree.  This is all kind
		// of obscure and hard to explain in words, but basically what
		// it means is that our implementation has to be properly
		// recursive.

		// Travel upwards through the tree, looking for the parent in common with our desired neighbor.
		// Remember the path through the tree, so we can travel down the complementary path to get to the neighbor.
		QuadSquare p = this;
		quadcornerdata pcd = cd;
		int ct = 0;
		int stack[] = new int[32];
		for (; ; ) {
			int ci = pcd.ChildIndex;

			if (pcd.Parent == null || pcd.Parent.Square == null) {
				// Neighbor doesn'transformationArray exist (it's outside the tree), so there's no alias vertex to enable.
				return;
			}
			p = pcd.Parent.Square;
			pcd = pcd.Parent;

			boolean SameParent = ((index - ci) & 2) != 0;

			ci = ci ^ 1 ^ ((index & 1) << 1);    // Child index of neighbor node.

			stack[ct] = ci;
			ct++;

			if (SameParent) {
				break;
			}
		}

		// Get a pointer to our neighbor (create if necessary), by walking down
		// the quadtree from our shared ancestor.
		p = p.EnableDescendant(ct, stack, pcd);

		/*
	    // Travel down the tree towards our neighbor, enabling and creating nodes as necessary.  We'll
		// follow the complement of the path we used on the way up the tree.
		quadcornerdata	d[16];
		int	i;
		for (i = 0; i < ct; i++) {
			int	ci = modelstack[ct-i-1];

			if (multiTexturePathPattern.Child[ci] == NULL && CreateDepth == 0) CreateDepth = ct-i;	//xxxxxxx

			if ((multiTexturePathPattern.EnabledFlags & (16 << ci)) == 0) {
				multiTexturePathPattern.EnableChild(ci, *pcd);
			}
			multiTexturePathPattern.SetupCornerData(&d[i], *pcd, ci);
			multiTexturePathPattern = multiTexturePathPattern.Child[ci];
			pcd = &d[i];
		}
		 */

		// Finally: enable the vertex on the opposite edge of our neighbor, the alias of the original vertex.
		index ^= 2;
		p.EnabledFlags |= (1 << index);
		if (IncrementCount == true && (index == 0 || index == 3)) {
			p.SubEnabledCount[index & 1]++;
		}
	}

	public float GetHeight(quadcornerdata cd, float x, float z)
	// Returns the height of the heightfield at the specified x,z coordinates.
	{
		int half = 1 << cd.Level;

		float lx = (x - cd.xorg) / (half);
		float lz = (z - cd.zorg) / (half);

		int ix = (int) Math.floor(lx);
		int iz = (int) Math.floor(lz);

		// Clamp.
		if (ix < 0) {
			ix = 0;
		}
		if (ix > 1) {
			ix = 1;
		}
		if (iz < 0) {
			iz = 0;
		}
		if (iz > 1) {
			iz = 1;
		}

		int index = ix ^ (iz ^ 1) + (iz << 1);
		if (Child[index] != null && QuadSquare.Static) {
			// Pass the query down to the child which contains it.
			quadcornerdata q = null;
			SetupCornerData(q, cd, index);
			return Child[index].GetHeight(q, x, z);
		}

		// Bilinear interpolation.
		lx -= ix;
		if (lx < 0) {
			lx = 0;
		}
		if (lx > 1) {
			lx = 1;
		}

		lz -= iz;
		if (lx < 0) {
			lz = 0;
		}
		if (lz > 1) {
			lz = 1;
		}

		float s00 = 0, s01 = 0, s10 = 0, s11 = 0;
		switch(index) {
			case 0 -> {
				s00 = Vertex[2].Y;
				s01 = cd.Verts[0].Y;
				s10 = Vertex[0].Y;
				s11 = Vertex[1].Y;
			}
			case 1 -> {
				s00 = cd.Verts[1].Y;
				s01 = Vertex[2].Y;
				s10 = Vertex[3].Y;
				s11 = Vertex[0].Y;
			}
			case 2 -> {
				s00 = Vertex[3].Y;
				s01 = Vertex[0].Y;
				s10 = cd.Verts[2].Y;
				s11 = Vertex[4].Y;
			}
			case 3 -> {
				s00 = Vertex[0].Y;
				s01 = Vertex[1].Y;
				s10 = Vertex[4].Y;
				s11 = cd.Verts[3].Y;
			}
		}

		return (s00 * (1 - lx) + s01 * lx) * (1 - lz) + (s10 * (1 - lx) + s11 * lx) * lz;
	}

	public QuadSquare GetNeighbor(int dir, final quadcornerdata cd)
	// Traverses the tree in search of the QuadSquare neighboring this square to the
	// specified direction.  0-3 -. { E, N, W, S }.
	// Returns NULL if the neighbor is outside the bounds of the tree.
	{
		// If we don'transformationArray have a parent, then we don'transformationArray have a neighbor.
		// (Actually, we could have inter-tree connectivity at this level
		// for connecting separate trees together.)
		if (cd.Parent == null) {
			return null;
		}

		// Find the parent and the child-index of the square we want to locate or create.
		QuadSquare p = null;

		int index = cd.ChildIndex ^ 1 ^ ((dir & 1) << 1);
		boolean SameParent = ((dir - cd.ChildIndex) & 2) != 0 ? true : false;

		if (SameParent) {
			p = cd.Parent.Square;
		} else {
			p = cd.Parent.Square.GetNeighbor(dir, cd.Parent);

			if (p == null) {
				return null;
			}
		}

		QuadSquare n = p.Child[index];

		return n;
	}

	void InitVert(int index, float x, float y, float z)
	// Initializes the indexed vertex of VertexArray[] with the
	// given values.
	{
		int i = index * 3;
		VertexArray[i] = x;
		VertexArray[i + 1] = y;
		VertexArray[i + 2] = z;
	}

	int MakeColor(int Lightness)
	// Makes an ARGB color, given an 8-bit lightness value.
	// Just replicates the components and uses FF for alpha.
	{
		return 0xFF000000 | (Lightness << 16) | (Lightness << 8) | Lightness;
	}

	int MakeLightness(float xslope, float zslope)
	// Generates an 8-bit lightness value, given a surface slope.
	{
		Vector3f norm = new Vector3f(-xslope, -1, -zslope);    // Actually the negative of the surface slope.
		norm.normalize();

		float dot = norm.dot(SunVector);

		int c = (int) (255 - (1 - dot) * 300);
		if (c < 0) {
			c = 0;
		}
		if (c > 255) {
			c = 255;
		}
		c *= 0x010101;
		c |= 0xFF000000;

		return c;
	}

	void NotifyChildDisable(final quadcornerdata cd, int index)
	// Marks the indexed child quadrant as disabled.  Deletes the child node
	// if it isn'transformationArray static.
	{
		// Clear enabled flag for the child.
		EnabledFlags &= ~(16 << index);

		// Update child enabled counts for the affected edge verts.
		QuadSquare s;

		if ((index & 2) != 0) {
			s = this;
		} else {
			s = GetNeighbor(1, cd);
		}
		if (s != null) {
			s.SubEnabledCount[1]--;
		}

		if (index == 1 || index == 2) {
			s = GetNeighbor(2, cd);
		} else {
			s = this;
		}
		if (s != null) {
			s.SubEnabledCount[0]--;
		}

		if (QuadSquare.Static == false) {
			Child[index] = null;

			BlockDeleteCount++;//xxxxx
		}
	}

	float RecomputeErrorAndLighting(final quadcornerdata cd)
	// Recomputes the error values for this tree.  Returns the
	// max error.
	// Also updates MinY & MaxY.
	// Also computes quick & dirty vertex lighting for the demo.
	{
		int i;

		// Measure error of center and edge vertices.
		float maxerror = 0;

		// Compute error of center vert.
		float e;
		if ((cd.ChildIndex & 1) != 0) {
			e = Math.abs(Vertex[0].Y - (cd.Verts[1].Y + cd.Verts[3].Y) * 0.5f);
		} else {
			e = Math.abs(Vertex[0].Y - (cd.Verts[0].Y + cd.Verts[2].Y) * 0.5f);
		}
		if (e > maxerror) {
			maxerror = e;
		}

		// Initial min/max.
		MaxY = Vertex[0].Y;
		MinY = Vertex[0].Y;

		// Check min/max of corners.
		for (i = 0; i < 4; i++) {
			float y = cd.Verts[i].Y;
			if (y < MinY) {
				MinY = (int) y;
			}
			if (y > MaxY) {
				MaxY = (int) y;
			}
		}

		// Edge verts.
		e = Math.abs(Vertex[1].Y - (cd.Verts[0].Y + cd.Verts[3].Y) * 0.5f);
		if (e > maxerror) {
			maxerror = e;
		}
		Error[0] = e;

		e = Math.abs(Vertex[4].Y - (cd.Verts[2].Y + cd.Verts[3].Y) * 0.5f);
		if (e > maxerror) {
			maxerror = e;
		}
		Error[1] = e;

		// Min/max of edge verts.
		for (i = 0; i < 4; i++) {
			float y = Vertex[1 + i].Y;
			if (y < MinY) {
				MinY = (int) y;
			}
			if (y > MaxY) {
				MaxY = (int) y;
			}
		}

		// Check child squares.
		for (i = 0; i < 4; i++) {
			quadcornerdata q = null;
			if (Child[i] != null) {
				SetupCornerData(q, cd, i);
				Error[i + 2] = Child[i].RecomputeErrorAndLighting(q);

				if (Child[i].MinY < MinY) {
					MinY = Child[i].MinY;
				}
				if (Child[i].MaxY > MaxY) {
					MaxY = Child[i].MaxY;
				}
			} else {
				// Compute difference between bilinear average at child center, and diagonal edge approximation.
				Error[i + 2] = Math.abs((Vertex[0].Y + cd.Verts[i].Y) - (Vertex[i + 1].Y + Vertex[((i + 1) & 3) + 1].Y)) * 0.25f;
			}
			if (Error[i + 2] > maxerror) {
				maxerror = Error[i + 2];
			}
		}

		//
		// Compute quickie demo lighting.
		//

		float OneOverSize = 1.0f / (2 << cd.Level);
		Vertex[0].Lightness = MakeLightness((Vertex[1].Y - Vertex[3].Y) * OneOverSize,
				(Vertex[4].Y - Vertex[2].Y) * OneOverSize);

		float v;
		QuadSquare s = GetNeighbor(0, cd);
		if (s != null) {
			v = s.Vertex[0].Y;
		} else {
			v = Vertex[1].Y;
		}
		Vertex[1].Lightness = MakeLightness((v - Vertex[0].Y) * OneOverSize,
				(cd.Verts[3].Y - cd.Verts[0].Y) * OneOverSize);

		s = GetNeighbor(1, cd);
		if (s != null) {
			v = s.Vertex[0].Y;
		} else {
			v = Vertex[2].Y;
		}
		Vertex[2].Lightness = MakeLightness((cd.Verts[0].Y - cd.Verts[1].Y) * OneOverSize,
				(Vertex[0].Y - v) * OneOverSize);

		s = GetNeighbor(2, cd);
		if (s != null) {
			v = s.Vertex[0].Y;
		} else {
			v = Vertex[3].Y;
		}
		Vertex[3].Lightness = MakeLightness((Vertex[0].Y - v) * OneOverSize,
				(cd.Verts[2].Y - cd.Verts[1].Y) * OneOverSize);

		s = GetNeighbor(3, cd);
		if (s != null) {
			v = s.Vertex[0].Y;
		} else {
			v = Vertex[4].Y;
		}
		Vertex[4].Lightness = MakeLightness((cd.Verts[3].Y - cd.Verts[2].Y) * OneOverSize,
				(v - Vertex[0].Y) * OneOverSize);

		// The error, MinY/MaxY, and lighting values for this node and descendants are correct now.
		Dirty = false;

		return maxerror;
	}

	//	QuadSquare::~QuadSquare()
	//	// Destructor.
	//	{
	//		// Recursively delete sub-trees.
	//		int	i;
	//		for (i = 0; i < 4; i++) {
	//			if (Child[i]) delete Child[i];
	//			Child[i] = NULL;
	//		}
	//	}

	int Render(final quadcornerdata cd, boolean Textured)
	// Draws the heightfield represented by this tree.
	// Returns the number of triangles rendered.
	{
		TriCount = 0;
		//
		//		// Do some initial setup, then do all the work in RenderAux().
		////		GL11....();
		//		glEnableClientState(GL11.GL_VERTEX_ARRAY);
		//		glVertexPointer(3, GL11.GL_FLOAT, 0, VertexArray);
		//
		//		//xxxx
		//		glMatrixMode(GL_MODELVIEW);
		//		glPushMatrix();
		//		glScalef(1.0, VERTICAL_SCALE, 1.0);
		//
		//		if (!Textured) {
		//			// No texture; use crummy vertex lighting.
		//			glEnableClientState(GL_COLOR_ARRAY);
		//			glColorPointer(4, GL_UNSIGNED_BYTE, 0, ColorArray);
		//		} else {
		//			// Set up automatic texture-coordinate generation.
		//			// Basically we're just stretching the current texture over the entire 64K x 64K terrain.
		//			glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
		//			float	multiTexturePathPattern[4] = { 1.0 / 65536, 0, 0, 0 };
		//			glTexGenfv(GL_S, GL_OBJECT_PLANE, multiTexturePathPattern);
		//
		//			glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
		//			multiTexturePathPattern[0] = 0;	multiTexturePathPattern[2] = 1.0 / 65536;
		//			glTexGenfv(GL_T, GL_OBJECT_PLANE, multiTexturePathPattern);
		//
		//			glEnable(GL_TEXTURE_GEN_S);
		//			glEnable(GL_TEXTURE_GEN_T);
		//		}
		//
		//		RenderAux(cd, Textured, Clip::SOME_CLIP);
		//
		//		glDisable(GL_TEXTURE_GEN_S);
		//		glDisable(GL_TEXTURE_GEN_T);
		//		glDisableClientState(GL_VERTEX_ARRAY);
		//		glDisableClientState(GL_COLOR_ARRAY);
		////		GL11...();
		//
		//		glPopMatrix();
		//
		return TriCount;
	}

	void RenderAux(final quadcornerdata cd, boolean Textured, Visibility vis)
	// Does the work of rendering this square.  Uses the enabled vertices only.
	// Recurses as necessary.
	{
		int half = 1 << cd.Level;
		int whole = 2 << cd.Level;

		// If this square is outside the frustum, then don'transformationArray render it.
		if (vis != Clip.NO_CLIP) {
			float min[] = new float[3];
			float max[] = new float[3];
			min[0] = cd.xorg;
			min[1] = MinY * VERTICAL_SCALE;
			min[2] = cd.zorg;
			max[0] = cd.xorg + whole;
			max[1] = MaxY * VERTICAL_SCALE;
			max[2] = cd.zorg + whole;
			vis = Clip.ComputeBoxVisibility(min, max);
			if (vis == Clip.NOT_VISIBLE) {
				// This square is completely outside the view frustum.
				return;
			}
			// else vis is either NO_CLIP or SOME_CLIP.  If it's NO_CLIP, then child
			// squares won'transformationArray have to bother with the frustum check.
		}

		int i;

		int flags = 0;
		int mask = 1;
		quadcornerdata q = null;
		for (i = 0; i < 4; i++, mask <<= 1) {
			if ((EnabledFlags & (16 << i)) != 0) {
				SetupCornerData(q, cd, i);
				Child[i].RenderAux(q, Textured, vis);
			} else {
				flags |= mask;
			}
		}

		if (flags == 0) {
			return;
		}

		//		// xxx debug color.
		//		glColor3f(cd.Level * 10 / 255.0, ((cd.Level & 3) * 60 + ((cd.zorg >> cd.Level) & 255)) / 255.0, ((cd.Level & 7) * 30 + ((cd.xorg >> cd.Level) & 255)) / 255.0);

		// Init vertex data.
		InitVert(0, cd.xorg + half, Vertex[0].Y, cd.zorg + half);
		InitVert(1, cd.xorg + whole, Vertex[1].Y, cd.zorg + half);
		InitVert(2, cd.xorg + whole, cd.Verts[0].Y, cd.zorg);
		InitVert(3, cd.xorg + half, Vertex[2].Y, cd.zorg);
		InitVert(4, cd.xorg, cd.Verts[1].Y, cd.zorg);
		InitVert(5, cd.xorg, Vertex[3].Y, cd.zorg + half);
		InitVert(6, cd.xorg, cd.Verts[2].Y, cd.zorg + whole);
		InitVert(7, cd.xorg + half, Vertex[4].Y, cd.zorg + whole);
		InitVert(8, cd.xorg + whole, cd.Verts[3].Y, cd.zorg + whole);

		if (!Textured) {
			ColorArray[0] = MakeColor(Vertex[0].Lightness);
			ColorArray[1] = MakeColor(Vertex[1].Lightness);
			ColorArray[2] = MakeColor(cd.Verts[0].Lightness);
			ColorArray[3] = MakeColor(Vertex[2].Lightness);
			ColorArray[4] = MakeColor(cd.Verts[1].Lightness);
			ColorArray[5] = MakeColor(Vertex[3].Lightness);
			ColorArray[6] = MakeColor(cd.Verts[2].Lightness);
			ColorArray[7] = MakeColor(Vertex[4].Lightness);
			ColorArray[8] = MakeColor(cd.Verts[3].Lightness);
		}

		vcount = 0;

		// Local macro to make the triangle logic shorter & hopefully clearer.
		//#define tri(a,b,c) ( VertList[vcount++] = a, VertList[vcount++] = b, VertList[vcount++] = c )

		// Make the list of triangles to draw.
		if ((EnabledFlags & 1) == 0) {
			tri(0, 8, 2);
		} else {
			if ((flags & 8) != 0) {
				tri(0, 8, 1);
			}
			if ((flags & 1) != 0) {
				tri(0, 1, 2);
			}
		}
		if ((EnabledFlags & 2) == 0) {
			tri(0, 2, 4);
		} else {
			if ((flags & 1) != 0) {
				tri(0, 2, 3);
			}
			if ((flags & 2) != 0) {
				tri(0, 3, 4);
			}
		}
		if ((EnabledFlags & 4) == 0) {
			tri(0, 4, 6);
		} else {
			if ((flags & 2) != 0) {
				tri(0, 4, 5);
			}
			if ((flags & 4) != 0) {
				tri(0, 5, 6);
			}
		}
		if ((EnabledFlags & 8) == 0) {
			tri(0, 6, 8);
		} else {
			if ((flags & 4) != 0) {
				tri(0, 6, 7);
			}
			if ((flags & 8) != 0) {
				tri(0, 7, 8);
			}
		}

		// Draw 'em.
		//		glDrawElements(GL_TRIANGLES, vcount, GL_UNSIGNED_BYTE, VertList);

		// Count 'em.
		TriCount += vcount / 3;
	}

	void ResetTree()
	// Clear all enabled flags, and delete all non-static child nodes.
	{
		int i;
		for (i = 0; i < 4; i++) {
			if (Child[i] != null) {
				Child[i].ResetTree();
				if (QuadSquare.Static == false) {
					//delete Child[i];
					Child[i] = null;
				}
			}
		}
		EnabledFlags = 0;
		SubEnabledCount[0] = 0;
		SubEnabledCount[1] = 0;
		Dirty = true;
	}

	void SetStatic(final quadcornerdata cd)
	// Sets this node's static flag to true.  If static == true, then the
	// node or its children is considered to contain significant height data
	// and shouldn'transformationArray be deleted.
	{
		if (Static == false) {
			Static = true;

			// Propagate static status to ancestor nodes.
			if (cd.Parent != null && cd.Parent.Square != null) {
				cd.Parent.Square.SetStatic(cd.Parent);
			}
		}
	}

	void SetupCornerData(quadcornerdata q, final quadcornerdata cd, int ChildIndex)
	// Fills the given structure with the appropriate corner values for the
	// specified child block, given our own vertex data and our corner
	// vertex data from cd.
	//
	// ChildIndex mapping:
	// +-+-+
	// |1|0|
	// +-+-+
	// |2|3|
	// +-+-+
	//
	// Verts mapping:
	// 1-0
	// | |
	// 2-3
	//
	// Vertex mapping:
	// +-2-+
	// | | |
	// 3-0-1
	// | | |
	// +-4-+
	{
		int half = 1 << cd.Level;

		q.Parent = cd;
		q.Square = Child[ChildIndex];
		q.Level = cd.Level - 1;
		q.ChildIndex = ChildIndex;
		switch(ChildIndex) {
			case 0 -> {
				q.xorg = cd.xorg + half;
				q.zorg = cd.zorg;
				q.Verts[0] = cd.Verts[0];
				q.Verts[1] = Vertex[2];
				q.Verts[2] = Vertex[0];
				q.Verts[3] = Vertex[1];
			}
			case 1 -> {
				q.xorg = cd.xorg;
				q.zorg = cd.zorg;
				q.Verts[0] = Vertex[2];
				q.Verts[1] = cd.Verts[1];
				q.Verts[2] = Vertex[3];
				q.Verts[3] = Vertex[0];
			}
			case 2 -> {
				q.xorg = cd.xorg;
				q.zorg = cd.zorg + half;
				q.Verts[0] = Vertex[0];
				q.Verts[1] = Vertex[3];
				q.Verts[2] = cd.Verts[2];
				q.Verts[3] = Vertex[4];
			}
			case 3 -> {
				q.xorg = cd.xorg + half;
				q.zorg = cd.zorg + half;
				q.Verts[0] = Vertex[1];
				q.Verts[1] = Vertex[0];
				q.Verts[2] = Vertex[4];
				q.Verts[3] = cd.Verts[3];
			}
		}
	}

	void StaticCullAux(final quadcornerdata cd, float ThresholdDetail, int TargetLevel)
	// Check this node and its descendents, and remove nodes which don'transformationArray contain
	// necessary detail.
	{
		int i, j;
		quadcornerdata q = null;

		if (cd.Level > TargetLevel) {
			// Just recurse to child nodes.
			for (j = 0; j < 4; j++) {
				if (j < 2) {
					i = 1 - j;
				} else {
					i = j;
				}

				if (Child[i] != null) {
					SetupCornerData(q, cd, i);
					Child[i].StaticCullAux(q, ThresholdDetail, TargetLevel);
				}
			}
			return;
		}

		// We're at the target level.  Check this node to see if it's OK to delete it.

		// Check edge vertices to see if they're necessary.
		float size = 2 << cd.Level;    // Edge length.
		if (Child[0] == null && Child[3] == null && Error[0] * ThresholdDetail < size) {
			QuadSquare s = GetNeighbor(0, cd);
			if (s == null || (s.Child[1] == null && s.Child[2] == null)) {

				// Force vertex height to the edge value.
				float y = (cd.Verts[0].Y + cd.Verts[3].Y) * 0.5f;
				Vertex[1].Y = (int) y;
				Error[0] = 0;

				// Force alias vertex to match.
				if (s != null) {
					s.Vertex[3].Y = (int) y;
				}

				Dirty = true;
			}
		}

		if (Child[2] == null && Child[3] == null && Error[1] * ThresholdDetail < size) {
			QuadSquare s = GetNeighbor(3, cd);
			if (s == null || (s.Child[0] == null && s.Child[1] == null)) {
				float y = (cd.Verts[2].Y + cd.Verts[3].Y) * 0.5f;
				Vertex[4].Y = (int) y;
				Error[1] = 0;

				if (s != null) {
					s.Vertex[2].Y = (int) y;
				}

				Dirty = true;
			}
		}

		// See if we have child nodes.
		boolean StaticChildren = false;
		for (i = 0; i < 4; i++) {
			if (Child[i] != null) {
				StaticChildren = true;
				if (Child[i].Dirty) {
					Dirty = true;
				}
			}
		}

		// If we have no children and no necessary edges, then see if we can delete ourself.
		if (StaticChildren == false && cd.Parent != null) {
			boolean NecessaryEdges = false;
			for (i = 0; i < 4; i++) {
				// See if vertex deviates from edge between corners.
				float diff = Math.abs(Vertex[i + 1].Y - (cd.Verts[i].Y + cd.Verts[(i + 3) & 3].Y) * 0.5f);
				if (diff > 0.00001) {
					NecessaryEdges = true;
				}
			}

			if (!NecessaryEdges) {
				size *= 1.414213562;    // sqrt(2), because diagonal is longer than side.
				if (cd.Parent.Square.Error[2 + cd.ChildIndex] * ThresholdDetail < size) {
					//					delete cd.Parent.Square.Child[cd.ChildIndex];	// Delete this.
					cd.Parent.Square.Child[cd.ChildIndex] = null;    // Clear the pointer.
				}
			}
		}
	}

	void StaticCullData(final quadcornerdata cd, float ThresholdDetail)
	// Examine the tree and remove nodes which don'transformationArray contain necessary
	// detail.  Necessary detail is defined as vertex data with a
	// edge-length to height ratio less than ThresholdDetail.
	{
		// First, clean non-static nodes out of the tree.
		ResetTree();

		// Make sure error values are up-to-date.
		if (Dirty) {
			RecomputeErrorAndLighting(cd);
		}

		// Recursively check all the nodes and do necessary removal.
		// We must start at the bottom of the tree, and do one level of
		// the tree at a time, to ensure the dependencies are accounted
		// for properly.
		int level;
		for (level = 0; level < 15; level++) {
			StaticCullAux(cd, ThresholdDetail, level);
		}
	}

	public void tri(int a, int b, int c) {
		VertList[vcount++] = a;
		VertList[vcount++] = b;
		VertList[vcount++] = c;
	}

	void UpdateAux(final quadcornerdata cd, final float ViewerLocation[], float CenterError)
	// Does the actual work of updating enabled states and tree growing/shrinking.
	{
		BlockUpdateCount++;    //xxxxx

		// Make sure error values are current.
		if (Dirty) {
			RecomputeErrorAndLighting(cd);
		}

		int half = 1 << cd.Level;
		int whole = half << 1;

		// See about enabling child verts.
		if ((EnabledFlags & 1) == 0 &&
				VertexTest(cd.xorg + whole, Vertex[1].Y, cd.zorg + half, Error[0], ViewerLocation) == true) {
			EnableEdgeVertex(0, false, cd);    // East vert.
		}
		if ((EnabledFlags & 8) == 0 &&
				VertexTest(cd.xorg + half, Vertex[4].Y, cd.zorg + whole, Error[1], ViewerLocation) == true) {
			EnableEdgeVertex(3, false, cd);    // South vert.
		}
		if (cd.Level > 0) {
			if ((EnabledFlags & 32) == 0) {
				if (BoxTest(cd.xorg, cd.zorg, half, MinY, MaxY, Error[3], ViewerLocation) == true) {
					EnableChild(1, cd);    // nw child.er
				}
			}
			if ((EnabledFlags & 16) == 0) {
				if (BoxTest(cd.xorg + half, cd.zorg, half, MinY, MaxY, Error[2], ViewerLocation) == true) {
					EnableChild(0, cd);    // ne child.
				}
			}
			if ((EnabledFlags & 64) == 0) {
				if (BoxTest(cd.xorg, cd.zorg + half, half, MinY, MaxY, Error[4], ViewerLocation) == true) {
					EnableChild(2, cd);    // sw child.
				}
			}
			if ((EnabledFlags & 128) == 0) {
				if (BoxTest(cd.xorg + half, cd.zorg + half, half, MinY, MaxY, Error[5], ViewerLocation) == true) {
					EnableChild(3, cd);    // se child.
				}
			}

			// Recurse into child quadrants as necessary.
			quadcornerdata q = null;

			if ((EnabledFlags & 32) != 0) {
				SetupCornerData(q, cd, 1);
				Child[1].UpdateAux(q, ViewerLocation, Error[3]);
			}
			if ((EnabledFlags & 16) != 0) {
				SetupCornerData(q, cd, 0);
				Child[0].UpdateAux(q, ViewerLocation, Error[2]);
			}
			if ((EnabledFlags & 64) != 0) {
				SetupCornerData(q, cd, 2);
				Child[2].UpdateAux(q, ViewerLocation, Error[4]);
			}
			if ((EnabledFlags & 128) != 0) {
				SetupCornerData(q, cd, 3);
				Child[3].UpdateAux(q, ViewerLocation, Error[5]);
			}
		}

		// Test for disabling.  East, South, and center.
		if ((EnabledFlags & 1) != 0 && SubEnabledCount[0] == 0 &&
				VertexTest(cd.xorg + whole, Vertex[1].Y, cd.zorg + half, Error[0], ViewerLocation) == false) {
			EnabledFlags &= ~1;
			QuadSquare s = GetNeighbor(0, cd);
			if (s != null) {
				s.EnabledFlags &= -4; //~4
			}
		}
		if ((EnabledFlags & 8) != 0 && SubEnabledCount[1] == 0 &&
				VertexTest(cd.xorg + half, Vertex[4].Y, cd.zorg + whole, Error[1], ViewerLocation) == false) {
			EnabledFlags &= ~8;
			QuadSquare s = GetNeighbor(3, cd);
			if (s != null) {
				s.EnabledFlags &= -2; //~2
			}
		}
		if (EnabledFlags == 0 &&
				cd.Parent != null &&
				BoxTest(cd.xorg, cd.zorg, whole, MinY, MaxY, CenterError, ViewerLocation) == false) {
			// Disable ourself.
			cd.Parent.Square.NotifyChildDisable(cd.Parent, cd.ChildIndex);    // nb: possibly deletes 'this'.
		}
	}

	boolean VertexTest(float x, float y, float z, float error, final float Viewer[])
	// Returns true if the vertex at (x,z) with the given world-space error between
	// its interpolated location and its true location, should be enabled, given that
	// the viewpoint is located at PositionableViewer[].
	{
		float dx = Math.abs(x - Viewer[0]);
		float dy = Math.abs(y - Viewer[1]);
		float dz = Math.abs(z - Viewer[2]);
		float d = dx;
		if (dy > d) {
			d = dy;
		}
		if (dz > d) {
			d = dz;
		}

		return (error * DetailThreshold) > d;
	}

	public class ContourParams {
		int PENCOUNT = 6;
		float OneOverInterval;

		boolean PensCreated;
		//		CPen	pen = new CPen[PENCOUNT];

		ContourParams() {
			PensCreated = false;
		}
	} //cp;

	public class quadcornerdata {
		public quadcornerdata Parent;
		public QuadSquare Square;
		public int ChildIndex;
		public int Level;
		public int xorg;
		public int zorg;
		public VertInfo[] Verts = new VertInfo[4]; // ne, nw, sw, se
	}

	public class VertInfo {
		int Y;
		int Lightness;    // For simple precomputed vertex lighting for purposes of the demo.  It's a waste of 2 bytes if we're texturing.
	}
}
