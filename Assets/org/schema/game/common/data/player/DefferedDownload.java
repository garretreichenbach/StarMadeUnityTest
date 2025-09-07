package org.schema.game.common.data.player;

public class DefferedDownload {
	private final String downloadName;
	private final long timeDeffered;
	private final int delay;

	public DefferedDownload(String downloadName, long timeDeffered, int delay) {
		super();
		this.downloadName = downloadName;
		this.timeDeffered = timeDeffered;
		this.delay = delay;
	}

	/**
	 * @return the downloadName
	 */
	public String getDownloadName() {
		return downloadName;
	}

	public boolean timeUp(long currentTimeMs) {
		return currentTimeMs - timeDeffered > delay;
	}
}
