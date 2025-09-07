public abstract class Segment : SegmentAabbInterface {

	public const int DIM_BITS = 5;
	public const int DIM_BITS_X2 = DIM_BITS * 2;
	public const byte DIM = 1 << DIM_BITS;
	public const byte HALF_DIM = DIM/2;
	const int HALF_SIZE = (int) Element.BLOCK_SIZE;
	public static bool ALLOW_ADMIN_OVERRIDE = false;
	public static long debugTime = 0;
	private static int idGen;
	public const float[] cachedTransform = new float[16];
	public const Vector3i pos = new Vector3i();
	public const Vector3i absPos = new Vector3i();
	public float cacheBBMinX;
	public float cacheBBMinY;
	public float cacheBBMinZ;
	public float cacheBBMaxX;
	public float cacheBBMaxY;
	public float cacheBBMaxZ;
	public short cacheDate;
	protected int id;
	private int size = 0;
	private SegmentData segmentData;
	private SegmentController segmentController;
}
