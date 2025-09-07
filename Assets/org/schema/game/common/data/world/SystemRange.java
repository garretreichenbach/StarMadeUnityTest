package org.schema.game.common.data.world;

import org.schema.common.util.linAlg.Vector3i;

public class SystemRange {
	public final Vector3i start;
	public final Vector3i end;
	
	public SystemRange(Vector3i startSec, Vector3i endSec) {
		super();
		this.start = startSec;
		this.end = endSec;
	}

	public static SystemRange get(Vector3i system){
		Vector3i startSec = new Vector3i(system);
		startSec.scale(VoidSystem.SYSTEM_SIZE);
		Vector3i endSec = new Vector3i(system);
		endSec.scale(VoidSystem.SYSTEM_SIZE);
		endSec.add(VoidSystem.SYSTEM_SIZE, VoidSystem.SYSTEM_SIZE, VoidSystem.SYSTEM_SIZE);
		
		return new SystemRange(startSec, endSec);
	}

	public static boolean isInSystem(Vector3i sector, Vector3i system) {
		int startX = system.x*VoidSystem.SYSTEM_SIZE;
		int startY = system.y*VoidSystem.SYSTEM_SIZE;
		int startZ = system.z*VoidSystem.SYSTEM_SIZE;
		int endX = startX+VoidSystem.SYSTEM_SIZE;
		int endY = startY+VoidSystem.SYSTEM_SIZE;
		int endZ = startZ+VoidSystem.SYSTEM_SIZE;
		
		return 
				sector.x >= startX && sector.x < endX &&
				sector.y >= startY && sector.y < endY &&
				sector.z >= startZ && sector.z < endZ 
				;
	}
	
}
