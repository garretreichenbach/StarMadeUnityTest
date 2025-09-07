package org.schema.game.client.view;

public class heightfield {
	Stats stats;
	int m_size;
	int m_log_size;    // size == (1 << log_size) + 1
	int root_level;    // level of the root chunk (TODO: reverse the meaning of 'level' to be more intuitive).
	float sample_spacing;
	float vertical_scale;    // scales the units stored in heightfield_elem's.  meters == stored_Sint16 * vertical_scale
	float input_vertical_scale;    // scale factor to apply to input data.
	//	mmap_array<Sint16>*	m_height;
	hf m_bt;
	hl m_level;
	heightfield(float initial_vertical_scale, float input_scale) {
		stats = new Stats();
		m_size = 0;
		m_log_size = 0;
		sample_spacing = 1.0f;
		vertical_scale = initial_vertical_scale;
		input_vertical_scale = input_scale;
		m_bt = null;
		m_level = null;
	}

	static int lowest_one(int x)
	// Returns the bit position of the lowest 1 bit in the given value.
	// If x == 0, returns the number of bits in an integer.
	//
	// E.g. lowest_one(1) == 0; lowest_one(16) == 4; lowest_one(5) == 0;
	{
		int intbits = Integer.SIZE; //sizeof(x) * 8
		int i;
		for (i = 0; i < intbits; i++, x = x >> 1) {
			if ((x & 1) != 0) {
				break;
			}
		}
		return i;
	}

	void activate(int x, int z, int lev)
	// Sets the activation_level to the given level, if it's greater than
	// the vert's current activation level.
	{
		assert (lev < 15);    // 15 is our flag value.
		int current_level = get_level(x, z);
		if (lev > current_level) {
			if (current_level == -1) {
				stats.output_vertices++;
			}
			set_level(x, z, lev);
		}
	}

	void clear()
	// Frees any allocated data and resets our members.
	{
		if (m_level != null) {
			//			delete m_level;
			m_level = null;
		}

		m_size = 0;
		m_log_size = 0;
	}

	int get_level(int x, int z)
	// Return the activation level at (x, z)
	{
		//		assert(m_level);
		int val = m_level.get(z, x >> 1); // swap indices for VM performance -- .bt is column major
		if ((x & 1) != 0) {
			val = val >> 4;
		}
		val &= 0x0F;
		if (val == 0x0F) {
			return -1;
		} else {
			return val;
		}
	}

	int height(int x, int z)
	// Return a (writable) reference to the height element at (x, z).
	{
		//		assert(m_bt);
		return (int) (m_bt.get_sample(x, z) * input_vertical_scale / vertical_scale);
	}

	int iclamp(int i, int min, int max) {
		return Math.max(min, Math.min(i, max));
	}

	int minimum_edge_lod(int coord)
	// Given an x or z coordinate, along which an edge runs, this
	// function returns the lowest LOD level that the edge divides.
	//
	// (This is useful for determining how conservative we need to be
	// with edge skirts.)
	{
		int l1 = lowest_one(coord);
		int depth = (m_log_size - l1 - 1);

		return iclamp(root_level - depth, 0, root_level);    // TODO: reverse direction of level

		//		depth = iclamp(depth, 0, root_level);
		//		if (depth < 0) depth = 0;
		//
		//		return depth;
	}

	int node_index(int x, int z)
	// Given the coordinates of the center of a quadtree node, this
	// function returns its node index.  The node index is essentially
	// the node's rank in a breadth-first quadtree traversal.  Assumes
	// a [nw, ne, sw, se] traversal order.
	//
	// If the coordinates don'transformationArray specify a valid node (e.g. if the coords
	// are outside the heightfield) then returns -1.
	{
		if (x < 0 || x >= m_size || z < 0 || z >= m_size) {
			return -1;
		}

		int l1 = lowest_one(x | z);
		int depth = m_log_size - l1 - 1;

		int base = 0x55555555 & ((1 << depth * 2) - 1);    // total node count in all levels above ours.
		int shift = l1 + 1;

		// Effective coords within this node's level.
		int col = x >> shift;
		int row = z >> shift;

		return base + (row << depth) + col;
	}

	void set_level(int x, int z, int lev) {
		assert (lev >= -1 && lev < 15);
		lev &= 0x0F;
		int current = m_level.get(z, x >> 1); // swap indices for VM performance -- .bt is column major
		if ((x & 1) != 0) {
			current = (current & 0x0F) | (lev << 4);
		} else {
			current = (current & 0xF0) | (lev);
		}
		m_level.set(z, x >> 1, current);
	}

	public class hf {

		public float get_sample(int x, int z) {

			return 0;
		}

	}

	public class hl {

		public int get(int x, int z) {

			return 0;
		}

		public void set(int z, int i, int current) {

		}

	}

	public class Stats {
		int input_vertices;
		int output_vertices;
		int output_real_triangles;
		int output_degenerate_triangles;
		int output_chunks;
		int output_size;
	}

	//	boolean	init_bt(final char bt_filename)
	//	// Use the specified .BT format heightfield data file as our height input.
	//	//
	//	// Return true on success, false on failure.
	//	{
	//		clear();
	//
	//		m_bt = bt_array::create(bt_filename);
	//		if (m_bt == NULL) {
	//			// failure.
	//			return false;
	//		}
	//
	//
	//
	//		m_size = imax(m_bt.get_width(), m_bt.get_height());
	//
	//		// Compute the log_size and make sure the size is 2^N + 1
	//		m_log_size = (int) (log2((float) m_size - 1) + 0.5);
	//		if (m_size != (1 << m_log_size) + 1) {
	//			if (m_size < 0 || m_size > (1 << 20)) {
	//				printf("invalid heightfield dimensions -- size from file = %d\n", m_size);
	//				return false;
	//			}
	//
	//			printf("Warning: data is not (2^N + 1) x (2^N + 1); will extend to make it the correct size.\n");
	//
	//			// Expand log_size until it contains size.
	//			while (m_size > (1 << m_log_size) + 1) {
	//				m_log_size++;
	//			}
	//			m_size = (1 << m_log_size) + 1;
	//		}
	//
	//		sample_spacing = (float) (fabs(m_bt.get_right() - m_bt.get_left()) / (double) (m_size - 1));
	//		printf("sample_spacing = %f\n", sample_spacing);//xxxxxxx
	//
	//		// Allocate storage for vertex activation levels.
	////		m_height = new mmap_array<Sint16>(size, size, true);
	//		m_level = new mmap_array<Uint8>(m_size, (m_size + 1) >> 1, true);	// swap height/width; .bt is column-major
	////		assert(m_height);
	//		assert(m_level);
	//
	//		// Initialize level array.
	//		for (int i = 0; i < m_size; i++) {
	//			for (int j = 0; j < m_size; j++) {
	//				m_level.get(m_size - 1 - j, i >> 1) = 255;	// swap height/width -- (to match .bt order; good idea??)
	//			}
	//		}
	//
	//
	//
	//		return true;
	//	}

	//}
}
