public interface SegmentAabbInterface {
	void getSegmentAabb(Segment s, Transform trans, Vector3f outOuterMin, Vector3f outOuterMax, Vector3f localMinOut,
		Vector3f localMaxOut, AABBVarSet varSet);

	void getAabb(Transform tmpAABBTrans0, Vector3f min, Vector3f max);

	public void getAabbUncached(Transform t, Vector3f aabbMin, Vector3f aabbMax, boolean cache);

	public void getAabbIdent(Vector3f min, Vector3f max);
}