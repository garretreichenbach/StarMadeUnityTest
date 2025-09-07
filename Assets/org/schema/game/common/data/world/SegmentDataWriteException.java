package org.schema.game.common.data.world;

public class SegmentDataWriteException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -5101607442120378824L;
	public final SegmentData data;

	public SegmentDataWriteException(SegmentData data) {
		super(data == null ? "NULLDATA" : "SEGDATA CLASS: "+data.getClass().toString());
		this.data = data;
	}

	public static SegmentData replaceData(Segment segment) {

		assert (!(segment.getSegmentData() instanceof SegmentData4Byte));

		SegmentData oldData = segment.getSegmentData();
		SegmentData newData = segment.getSegmentController().getSegmentProvider().getSegmentDataManager().getFreeSegmentData();

		try {
			for (int i = 0; i < SegmentData.BLOCK_COUNT; i++) {
				newData.setInfoElementForcedAddUnsynched(i, oldData.getDataAt(i));
			}
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}
		newData.replace(oldData);
		

//		try {
//			throw new Exception(newData + " replaced " + oldData);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return newData;

		/*if(segmentData.getSegmentController().isOnServer()){
			return replaceDataOnServer(segmentData);
		}else{
			return replaceDataOnClient(segmentData);
		}*/
	}

	public static SegmentData replaceDataOnClient(SegmentData segmentData) {
		assert (!segmentData.getSegmentController().isOnServer());
		return replaceData(segmentData.getSegment());
	}

	public static SegmentData replaceDataOnServer(SegmentData segmentData) {
		assert (segmentData.getSegmentController().isOnServer());
		return replaceData(segmentData.getSegment());
	}

}
