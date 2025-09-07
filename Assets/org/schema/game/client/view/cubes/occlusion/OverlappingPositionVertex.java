package org.schema.game.client.view.cubes.occlusion;

import theleo.jstruct.Struct;


/**
 * x,y,z 1 or -1 respectively for local vertex position within block
 * 
 * @author schema
 *
 */
@Struct
public class OverlappingPositionVertex{
	public int x;
	public int y;
	public int z;
	int sideId;
}
