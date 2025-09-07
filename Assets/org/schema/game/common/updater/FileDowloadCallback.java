package org.schema.game.common.updater;

public interface FileDowloadCallback {
	public void update(FileDownloadUpdate u);

	public void update(String u);
}
