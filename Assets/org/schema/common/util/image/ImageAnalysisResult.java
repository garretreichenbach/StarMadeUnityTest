package org.schema.common.util.image;

public class ImageAnalysisResult {

	private boolean image;
	private boolean truncated;
	private boolean powerOfTwo;

	/**
	 * @return the image
	 */
	public boolean isImage() {
		return image;
	}

	public void setImage(boolean b) {
		this.image = b;
	}

	/**
	 * @return the truncated
	 */
	public boolean isTruncated() {
		return truncated;
	}

	public void setTruncated(boolean b) {
		this.truncated = b;
	}

	/**
	 * @return the powerOfTwo
	 */
	public boolean isPowerOfTwo() {
		return powerOfTwo;
	}

	/**
	 * @param powerOfTwo the powerOfTwo to set
	 */
	public void setPowerOfTwo(boolean powerOfTwo) {
		this.powerOfTwo = powerOfTwo;
	}

}
