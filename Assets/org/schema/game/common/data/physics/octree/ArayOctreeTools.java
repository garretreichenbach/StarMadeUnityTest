package org.schema.game.common.data.physics.octree;

public class ArayOctreeTools {
	private static ArrayOctree fullTreeServer = new ArrayOctreeFull(true);
	private static ArrayOctree emptyTreeServer = new ArrayOctreeEmpty(true);
	
	private static ArrayOctree fullTreeClient = new ArrayOctreeFull(false);
	private static ArrayOctree emptyTreeClient = new ArrayOctreeEmpty(false);
	
	public static ArrayOctree fullTreeServer() {
		return fullTreeServer;//new ArrayOctree(true);// 
	}
	
	public static ArrayOctree emptyTreeServer() {
		return emptyTreeServer;//new ArrayOctree(true);//emptyTreeServer;
	}
	
	
	
	public static ArrayOctree fullTreeClient() {
		return fullTreeClient;//new ArrayOctree(false);//
	}
	
	public static ArrayOctree emptyTreeClient() {
		return emptyTreeClient;//new ArrayOctree(false);//emptyTreeClient;
	}
	
	
}
