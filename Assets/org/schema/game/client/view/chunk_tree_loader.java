package org.schema.game.client.view;

public class chunk_tree_loader {
	//	//
	//	// chunk_tree_loader -- helper for lod_chunk_tree that handles the
	//	// background loader thread.
	//	//
	//
	//
	//
	//
	////		SDL_RWops*	get_source() { return m_source_stream; }
	//
	//		// Call this to enable/disable the background loader thread.
	//
	//
	//		private class pending_load_request
	//		{
	//			lod_chunk	m_chunk;
	//			float	m_priority;
	//
	//			pending_load_request(){
	//				m_chunk=(null);
	//				m_priority=(0.0f) ;
	//				}
	//
	//			pending_load_request(lod_chunk chunk, float priority){
	//				m_chunk=(chunk);
	//				m_priority=(priority);
	//
	//			}
	//
	//
	//		}
	//		static int	compare(pending_load_request r1, pending_load_request r2)
	//		// Comparison function for qsort.  Sort based on priority.
	//		{
	//			float	p1 = ((pending_load_request) r1).m_priority;
	//			float	p2 = ((pending_load_request) r2).m_priority;
	//
	//			if (p1 < p2) { return -1; }
	//			else if (p1 > p2) { return 1; }
	//			else { return 0; }
	//		}
	//		private class retire_info
	//		// A struct that associates a chunk with its newly loaded
	//		// data.  For communicating between m_loader_thread and the
	//		// main thread.
	//		{
	//			lod_chunk	m_chunk;
	//			lod_chunk_data	m_chunk_data;
	//
	//			retire_info(){
	//				m_chunk=(null);
	//				m_chunk_data=(null) ;
	//			}
	//		};
	//
	//		private class retire_texture_info
	//		// Associates a chunk with its newly loaded texture data.
	//		{
	//			lod_chunk	m_chunk;
	//			int	m_texture_image;
	//
	//			retire_texture_info(){
	//				m_chunk=(null);
	//				m_texture_image=(0) ;
	//			}
	//		};
	//
	//		lod_chunk_tree	m_tree;
	////		SDL_RWops*	m_source_stream;
	//
	//		// These two are for the main thread's use only.  For
	//		// update()/update_texture() to communicate with
	//		// sync_loader_thread().
	//		ArrayList<lod_chunk>	m_unload_queue;
	//		ArrayList<pending_load_request>	m_load_queue;
	//
	//		ArrayList<lod_chunk>	m_unload_texture_queue;
	//		ArrayList<lod_chunk>	m_load_texture_queue;
	//
	//		// These two are for the main thread to communicate with the
	//		// loader thread & vice versa.
	//	public static final int REQUEST_BUFFER_SIZE = 4;
	//		lod_chunk[] m_request_buffer = new lod_chunk[REQUEST_BUFFER_SIZE];	// chunks waiting to be loaded; filled will NULLs otherwise.
	//		retire_info[]	m_retire_buffer = new retire_info[REQUEST_BUFFER_SIZE];	// chunks waiting to be united with their loaded data.
	//
	//		lod_chunk[]	m_request_texture_buffer = new lod_chunk[REQUEST_BUFFER_SIZE];		// chunks waiting for their textures to be loaded.
	//		retire_texture_info[] m_retire_texture_buffer = new retire_texture_info[REQUEST_BUFFER_SIZE];	// chunks waiting to be united with their loaded textures.
	//
	//		// Loader thread stuff.
	////		SDL_Thread*	m_loader_thread;
	//
	//		boolean	m_run_loader_thread;	// loader thread watches for this to go false, then exits.
	////		SDL_mutex*	m_mutex;
	//
	//
	//	chunk_tree_loader(lod_chunk_tree tree)
	//	// Constructor.  Retains internal copies of the given pointers.
	//	{
	//		m_tree = tree;
	//
	//		for (int i = 0; i < REQUEST_BUFFER_SIZE; i++)
	//		{
	//			m_request_buffer[i] = null;
	//			m_retire_buffer[i].m_chunk = null;
	//			m_request_texture_buffer[i] = null;
	//			m_retire_texture_buffer[i].m_chunk = null;
	//		}
	//
	//		// Set up thread communication stuff.
	////		m_mutex = SDL_CreateMutex();
	////		assert(m_mutex);
	//		start_loader_thread();
	//	}
	//
	//
	////	~chunk_tree_loader()
	////	// Destructor.  Make sure thread is done.
	////	{
	////		// Make sure to kill the loader thread.
	////		set_use_loader_thread(false);
	////
	////		SDL_DestroyMutex(m_mutex);
	////		m_mutex = NULL;
	////	}
	//
	//
	//	void	start_loader_thread()
	//	// Initiate the loader thread, then return.
	//	{
	//		m_run_loader_thread = true;
	//
	////		// Thunk to wrap loader_thread(), which is a member fn.
	////		struct wrapper {
	////			static int	thread_wrapper(void* loader)
	////			{
	////				return ((chunk_tree_loader*) loader).loader_thread();
	////			}
	////		};
	////		m_loader_thread = SDL_CreateThread(wrapper::thread_wrapper, this);
	//	}
	//
	//
	//	void	set_use_loader_thread(boolean use)
	//	// Call this to enable/disable the use of a background loader thread.
	//	// May take a moment of latency to return, since if the background
	//	// thread is active, then this function has to signal it and wait for
	//	// it to terminate.
	//	{
	////		if (m_run_loader_thread) {
	////			assert(m_loader_thread);
	////			if (use) {
	////				// We're already using the thread; nothing to do.
	////				return;
	////			}
	////			else {
	////				// Thread is running -- kill it.
	////				m_run_loader_thread = false;
	////				SDL_WaitThread(m_loader_thread, NULL);
	////				m_loader_thread = NULL;
	////				return;
	////			}
	////		}
	////		else {
	////			assert(m_loader_thread == NULL);
	////			if (use == false) {
	////				// We're already not using the loader thread; nothing to do.
	////				return;
	////			} else {
	////				// Thread is not running -- start it up.
	////				start_loader_thread();
	////				assert(m_loader_thread);
	////			}
	////		}
	//	}
	//
	//
	//	void	sync_loader_thread()
	//	// Call this periodically, to implement previously requested changes
	//	// to the lod_chunk_tree.  Most of the work in preparing changes is
	//	// done in a background thread, so this call is intended to be
	//	// low-latency.
	//	//
	//	// The chunk_tree_loader is not allowed to make any changes to the
	//	// lod_chunk_tree, except in this call.
	//	{
	//		// mutex section
	////		SDL_LockMutex(m_mutex);
	////		{
	////			// Unload data.
	////			for (int i = 0; i < m_unload_queue.size(); i++) {
	////				lod_chunk	c = m_unload_queue[i];
	////				// Only unload the chunk if it's not currently in use.
	////				// Sometimes a chunk will be marked for unloading, but
	////				// then is still being used due to a dependency in a
	////				// neighboring part of the hierarchy.  We want to
	////				// ignore the unload request in that case.
	////				if (c.m_parent != NULL
	////				    && c.m_parent.m_split == false)
	////				{
	////					c.unload_data();
	////				}
	////			}
	////			m_unload_queue.resize(0);
	////
	////			// Unload textures.
	////			{for (int i = 0; i < m_unload_texture_queue.size(); i++) {
	////				lod_chunk*	c = m_unload_texture_queue[i];
	////				if (c.m_parent != NULL) {
	////					assert(c.m_parent.m_texture_id != 0);
	////					assert(c.has_children() == false
	////					       || (c.m_children[0].m_texture_id == 0
	////						   && c.m_children[1].m_texture_id == 0
	////						   && c.m_children[2].m_texture_id == 0
	////						   && c.m_children[3].m_texture_id == 0));
	////
	////					c.release_texture();
	////				}
	////			}}
	////			m_unload_texture_queue.resize(0);
	////
	////			// Retire any serviced requests.
	////			{for (int i = 0; i < REQUEST_BUFFER_SIZE; i++)
	////			{
	////				retire_info&	r = const_cast<retire_info&>(m_retire_buffer[i]);	// cast away 'volatile' (we're inside the mutex section)
	////				if (r.m_chunk)
	////				{
	////					assert(r.m_chunk.m_data == NULL);
	////
	////					if (r.m_chunk.m_parent != NULL
	////					    && r.m_chunk.m_parent.m_data == NULL)
	////					{
	////						// Drat!  Our parent data was unloaded, while we were
	////						// being loaded.  Only thing to do is discard the newly loaded
	////						// data, to avoid breaking an invariant.
	////						// (No big deal; this situation is rare.)
	////						delete r.m_chunk_data;
	////					}
	////					else
	////					{
	////						// Connect the chunk with its data!
	////						r.m_chunk.m_data = r.m_chunk_data;
	////					}
	////				}
	////				// Clear out this entry.
	////				r.m_chunk = NULL;
	////				r.m_chunk_data = NULL;
	////			}}
	////
	////			// Retire any serviced texture requests.
	////			{for (int i = 0; i < REQUEST_BUFFER_SIZE; i++)
	////			{
	////				retire_texture_info&	r = const_cast<retire_texture_info&>(m_retire_texture_buffer[i]);
	////				if (r.m_chunk)
	////				{
	////					assert(r.m_chunk.m_texture_id == 0);
	////
	////					if (r.m_chunk.m_parent != NULL
	////					    && r.m_chunk.m_parent.m_texture_id == 0)
	////					{
	////						// Drat!  Our parent texture was unloaded, while we were
	////						// being loaded.  Only thing to do is to discard the
	////						// newly loaded image, to avoid breaking the invariant.
	////						// (No big deal; this situation is rare.)
	////						delete r.m_texture_image;
	////					}
	////					else
	////					{
	////						// Connect the chunk with its texture!
	////						r.m_chunk.m_texture_id = lod_tile_freelist::make_texture(r.m_texture_image);	// @@ this actually could cause some bad latency, because we build mipmaps...
	////					}
	////				}
	////				// Clear out this entry.
	////				r.m_chunk = NULL;
	////				r.m_texture_image = NULL;
	////			}}
	////
	////			//
	////			// Pass new data requests to the loader thread.  Go in
	////			// order of priority, and only take a few.
	////			//
	////
	////			// Wipe out stale requests.
	////			{for (int i = 0; i < REQUEST_BUFFER_SIZE; i++) {
	////				m_request_buffer[i] = NULL;
	////			}}
	////
	////			// Fill in new requests.
	////			int	qsize = m_load_queue.size();
	////			if (qsize > 0)
	////			{
	////				int	req_count = 0;
	////
	////				// Sort by priority.
	////				qsort(&m_load_queue[0], qsize, sizeof(m_load_queue[0]), pending_load_request::compare);
	////				{for (int i = 0; i < qsize; i++)
	////				{
	////					lod_chunk*	c = m_load_queue[qsize - 1 - i].m_chunk;	// Do the higher priority requests first.
	////					// Must make sure the chunk wasn'transformationArray just retired.
	////					if (c.m_data == NULL
	////					    && (c.m_parent == NULL || c.m_parent.m_data != NULL))
	////					{
	////						// Request this chunk.
	////						m_request_buffer[req_count++] = c;
	////						if (req_count >= REQUEST_BUFFER_SIZE) {
	////							// We've queued up enough requests.
	////							break;
	////						}
	////					}
	////				}}
	////
	////				m_load_queue.resize(0);	// forget this frame's requests; we'll generate a fresh list during the next update()
	////			}
	////
	////			//
	////			// Pass texture requests.
	////			//
	////
	////			// Wipe out stale requests.
	////			{for (int i = 0; i < REQUEST_BUFFER_SIZE; i++) {
	////				m_request_texture_buffer[i] = NULL;
	////			}}
	////
	////			int	tqsize = m_load_texture_queue.size();
	////			if (tqsize > 0)
	////			{
	////				int	req_count = 0;
	////
	////				{for (int i = 0; i < tqsize; i++)
	////				{
	////					lod_chunk*	c = m_load_texture_queue[i];
	////					// Must make sure the chunk wasn'transformationArray
	////					// just retired, and also that its
	////					// parent wasn'transformationArray just unloaded.
	////					if (c.m_texture_id == 0
	////					    && (c.m_parent == NULL || c.m_parent.m_texture_id != 0))
	////					{
	////						// Request this chunk.
	////						m_request_texture_buffer[req_count++] = c;
	////						if (req_count >= REQUEST_BUFFER_SIZE) {
	////							// We've queued up enough requests.
	////							break;
	////						}
	////					}
	////				}}
	////
	////				m_load_texture_queue.resize(0);	// forget this frame's requests; we'll generate a fresh list during the next update()
	////			}
	////		}
	////		SDL_UnlockMutex(m_mutex);
	////
	////
	////		if (m_run_loader_thread == false)
	////		{
	////			// Loader thread is not actually running (at client
	////			// request, via set_use_loader_thread()), so instead,
	////			// service any requests synchronously, right now.
	////			int	count;
	////			for (count = 0; count < 4; count++) {
	////				bool	loaded = loader_service_data();
	////				if (loaded == false) break;
	////			}
	////			for (count = 0; count < 4; count++) {
	////				bool	loaded = loader_service_texture();
	////				if (loaded == false) break;
	////			}
	////		}
	//	}
	//
	//
	//	void	request_chunk_load(lod_chunk chunk, float urgency)
	//	// Request that the specified chunk have its data loaded.  May
	//	// take a while; data doesn'transformationArray actually show up & get linked in
	//	// until some future call to sync_loader_thread().
	//	{
	//
	//		// Don'transformationArray schedule for load unless our parent already has data.
	//		if (chunk.m_parent == null
	//		    || chunk.m_parent.m_data != null)
	//		{
	//
	//
	//			m_load_queue.add(new pending_load_request(chunk, urgency));
	//
	//		}
	//	}
	//
	//
	//	void	request_chunk_unload(lod_chunk chunk)
	//	// Request that the specified chunk have its data unloaded;
	//	// happens within short latency.
	//	{
	//		m_unload_queue.add(chunk);
	//	}
	//
	//
	//	void	request_chunk_load_texture(lod_chunk chunk)
	//	// Request that the specified chunk have its texture loaded.  May
	//	// take a while; data doesn'transformationArray actually show up & get linked in
	//	// until some future call to sync_loader_thread().
	//	{
	////		assert(chunk);
	////		assert(chunk.m_texture_id == 0);
	////		assert(m_tree.m_texture_quadtree.get_depth() >= chunk.m_level);
	//
	//		// Don'transformationArray schedule for load unless our parent already has a texture.
	//		if (chunk.m_parent == null
	//		    || chunk.m_parent.m_texture_id != 0)
	//		{
	//			m_load_texture_queue.add(chunk);
	//		}
	//	}
	//
	//
	//	void	request_chunk_unload_texture(lod_chunk chunk)
	//	// Request that the specified chunk have its texture unloaded; happens
	//	// within short latency.
	//	{
	//		assert(chunk.m_texture_id != 0);
	//
	//		m_unload_texture_queue.add(chunk);
	//	}
	//
	//
	//	int	loader_thread()
	//	// Thread function for the loader thread.  Sit and load chunk data
	//	// from the request queue, until we get killed.
	//	{
	//		while (m_run_loader_thread == true)
	//		{
	//			boolean	loaded = false;
	////			loaded = loader_service_data() || loaded;
	////			loaded = loader_service_texture() || loaded;
	////
	////			if (loaded == false)
	////			{
	////				// We seem to be dormant; sleep for a while
	////				// and then check again.
	////				SDL_Delay(10);
	////			}
	//		}
	//
	//		return 0;
	//	}
	//
	//
	//	boolean	loader_service_data()
	//	// Service a request for data.  Return true if we actually serviced
	//	// anything; false if there was nothing to service.
	//	{
	//		// Grab a request.
	//		lod_chunk	chunk_to_load = null;
	////		SDL_LockMutex(m_mutex);
	//		{
	//			// Get first request that's not already in the
	//			// retire bufferList.
	//			for (int req = 0; req < REQUEST_BUFFER_SIZE; req++)
	//			{
	//				chunk_to_load = m_request_buffer[0];	// (could be NULL)
	//
	//				// shift requests down.
	//				int	i;
	//				for (i = 0; i < REQUEST_BUFFER_SIZE - 1; i++)
	//				{
	//					m_request_buffer[i] = m_request_buffer[i + 1];
	//				}
	//				m_request_buffer[i] = null;	// fill empty slot with NULL
	//
	//				if (chunk_to_load == null) break;
	//
	//				// Make sure the request is not in the retire bufferList.
	//				boolean	in_retire_buffer = false;
	//				{
	//					for (int j = 0; j < REQUEST_BUFFER_SIZE; j++) {
	//						if (m_retire_buffer[j].m_chunk == chunk_to_load) {
	//							// This request has already been serviced.  Don'transformationArray
	//							// service it again.
	//							chunk_to_load = null;
	//							in_retire_buffer = true;
	//							break;
	//						}
	//				}}
	//				if (in_retire_buffer == false) break;
	//			}
	//		}
	////		SDL_UnlockMutex(m_mutex);
	//
	//		if (chunk_to_load == null)
	//		{
	//			// There's no request to service right now.
	//			return false;
	//		}
	//
	////		assert(chunk_to_load.m_data == NULL);
	////		assert(chunk_to_load.m_parent == NULL || chunk_to_load.m_parent.m_data != NULL);
	//
	//		// Service the request by loading the chunk's data.  This
	//		// could take a while, and involves waiting on IO, so we do it
	//		// with the mutex unlocked so the main update/render thread
	//		// can hopefully get some work done.
	//		lod_chunk_data	loaded_data = null;
	//
	//		// Geometry.
	////		SDL_RWseek(m_source_stream, chunk_to_load.m_data_file_position, SEEK_SET);
	//		loaded_data = new lod_chunk_data(); //m_source_stream
	//
	//		// "Retire" the request.  Must do this with the mutex locked.
	//		// The main thread will do "chunk_to_load.m_data = loaded_data".
	////		SDL_LockMutex(m_mutex);
	//		{
	//			for (int i = 0; i < REQUEST_BUFFER_SIZE; i++)
	//			{
	//				if (m_retire_buffer[i].m_chunk == null)
	//				{
	//					// empty slot; put the info here.
	//					m_retire_buffer[i].m_chunk = chunk_to_load;
	//					m_retire_buffer[i].m_chunk_data = loaded_data;
	//					break;
	//				}
	//			}
	//			// TODO: assert if we didn'transformationArray find a retire slot!
	//			// (there should always be one, because it's as big as
	//			// the request queue)
	//		}
	////		SDL_UnlockMutex(m_mutex);
	//
	//		return true;
	//	}
	//
	//
	//	boolean	loader_service_texture()
	//	// Service a pending texture request.  Return true if we serviced
	//	// something.  Return false if there was nothing to service.
	//	{
	//		// Grab a request.
	//		lod_chunk	chunk_to_load = null;
	////		SDL_LockMutex(m_mutex);
	////		{
	////			// Get first request that's not already in the
	////			// retire bufferList.
	////			for (int req = 0; req < REQUEST_BUFFER_SIZE; req++)
	////			{
	////				chunk_to_load = m_request_texture_buffer[0];	// (could be NULL)
	////
	////				// shift requests down.
	////				int	i;
	////				for (i = 0; i < REQUEST_BUFFER_SIZE - 1; i++)
	////				{
	////					m_request_texture_buffer[i] = m_request_texture_buffer[i + 1];
	////				}
	////				m_request_texture_buffer[i] = NULL;	// fill empty slot with NULL
	////
	////				if (chunk_to_load == NULL) break;
	////				assert(chunk_to_load.m_texture_id == 0);
	////
	////				// Make sure the request is not in the retire bufferList.
	////				bool	in_retire_buffer = false;
	////				{for (int i = 0; i < REQUEST_BUFFER_SIZE; i++) {
	////					if (m_retire_texture_buffer[i].m_chunk == chunk_to_load) {
	////						// This request has already been serviced.  Don'transformationArray
	////						// service it again.
	////						chunk_to_load = NULL;
	////						in_retire_buffer = true;
	////						break;
	////					}
	////				}}
	////				if (in_retire_buffer == false) break;
	////			}
	////		}
	////		SDL_UnlockMutex(m_mutex);
	////
	////		if (chunk_to_load == NULL)
	////		{
	////			// There's no request to service right now.
	////			return false;
	////		}
	////
	////		assert(chunk_to_load.m_texture_id == 0);
	////		assert(chunk_to_load.m_parent == NULL || chunk_to_load.m_parent.m_texture_id != 0);
	////
	////		// Service the request by loading the chunk's data.
	////		// This could take a while, and involves wating on IO,
	////		// so we do it with the mutex unlocked so the main
	////		// update/render thread can hopefully get some work
	////		// done.
	////		image::rgb*	texture_image = NULL;
	////
	////		// TextureNew.
	////		const tqt*	qt = m_tree.m_texture_quadtree;
	////		assert(qt && chunk_to_load.m_level < qt.get_depth());
	////
	////		texture_image = qt.load_image(chunk_to_load.m_level,
	////					       chunk_to_load.m_x,
	////					       chunk_to_load.m_z);
	////
	////		// "Retire" the request.  Must do this with the mutex
	////		// locked.  The main thread will do
	////		// "chunk_to_load.m_data = loaded_data".
	////		SDL_LockMutex(m_mutex);
	////		{
	////			for (int i = 0; i < REQUEST_BUFFER_SIZE; i++)
	////			{
	////				if (m_retire_texture_buffer[i].m_chunk == 0)
	////				{
	////					// empty slot; put the info here.
	////					m_retire_texture_buffer[i].m_chunk = chunk_to_load;
	////					m_retire_texture_buffer[i].m_texture_image = texture_image;
	////					break;
	////				}
	////			}
	////		}
	////		SDL_UnlockMutex(m_mutex);
	//
	//		return true;
	//	}
}
