package org.schema.game.client.data.terrain.geimipmap;

/**
 * Tells the terrain to update its Level of Detail.
 * It needs the cameras to do this, and there could possibly
 * be several cameras in the scene, so it accepts a list
 * of cameras.
 * NOTE: right now it just uses the first camera passed in,
 * in the future it will use all of them to determine what
 * LOD to set.
 *
 * @author Brent Owens
 */
public class TerrainLodControl /*extends AbstractControl */ {

	//	private OldTerrain terrain;
	//	private List<CameraInterface> cameras;
	//
	//	/**
	//	 * Only uses the first camera right now.
	//	 * @param terrain to act upon (must me a Spatial)
	//	 * @param cameras one or more cameras to reference for LOD calc
	//	 */
	//	public TerrainLodControl(OldTerrain terrain, List<CameraInterface> cameras) {
	//		super((Spatial)terrain);
	//		this.terrain = terrain;
	//		this.cameras = cameras;
	//	}
	//
	//	@Override
	//	protected void controlRender(RenderManager rm, ViewPort vp) {
	//
	//	}
	//
	//	@Override
	//	protected void controlUpdate(float tpf) {
	//		//list of cameras for when terrain supports multiple cameras (ie split screen)
	//                List<Vector3f> cameraLocations = new ArrayList<Vector3f>();
	//                for (CameraInterface c : cameras)
	//                    cameraLocations.add(c.getLocation());
	//		terrain.update(cameraLocations);
	//	}
	//
	//	@Override
	//	public Control cloneForSpatial(Spatial spatial) {
	//		if (spatial instanceof OldTerrain)
	//			return new TerrainLodControl((OldTerrain)spatial, cameras);
	//
	//		return null;
	//	}

}
