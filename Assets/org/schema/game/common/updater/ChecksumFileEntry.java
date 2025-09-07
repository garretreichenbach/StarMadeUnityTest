package org.schema.game.common.updater;

import org.schema.schine.common.util.DownloadCallback;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class ChecksumFileEntry {
	public final long size;
	public final String checksum;
	public final String relativePath;
	protected int index;

	public ChecksumFileEntry(long size, String checksum, String relativePath) {
		super();
		this.size = size;
		this.checksum = checksum;
		this.relativePath = relativePath.replaceFirst("\\.", "");
	}

	public boolean needsDownload(String buildPath, String installDirStr) throws NoSuchAlgorithmException, IOException {
		String sourceFilePath = buildPath + relativePath;

		File dst = new FileExt(installDirStr, relativePath);

		boolean replace = false;
		if (dst.exists()) {
			String localChecksum = FileUtil.getSha1Checksum(dst.getAbsolutePath());
			replace = !localChecksum.equals(checksum);
			if (replace) {
				System.err.println("[UPDATER] Checksum Differs for " + relativePath + ": " + localChecksum + " :: " + checksum);
			} else {
				System.err.println("[UPDATER] Not downloading " + relativePath + ": remote file same as local");
			}
		} else {
			System.err.println("[UPDATER] Does Not Exist " + dst.getAbsolutePath() + ": Downloading");
			replace = true;
		}
		return replace;
	}

	public void download(boolean force, String buildPath, File installDir, String installDirStr, final FileDowloadCallback cb, final FileUpdateTotal o) throws NoSuchAlgorithmException, IOException {
		String sourceFilePath = buildPath + relativePath;
		File dst = new FileExt(installDirStr, relativePath);

//		File dst = destFilePath;//new FileExt(destFilePath);

		System.err.println("Downloading " + sourceFilePath + " -> " + dst.getAbsolutePath());

		boolean replace = needsDownload(buildPath, installDirStr) || force;

		if (dst.exists() && replace) {
			if (!dst.delete()) {
				throw new IOException("File " + dst.getAbsolutePath() + " could not be removed! Is it still in use?");
			}
		}
		if (!dst.getParentFile().exists()) {
			System.err.println("Creating path: " + dst.getParentFile().getAbsolutePath());
		}

		dst.getParentFile().mkdirs();

		final String name = dst.getName();

		File file = new FileExt(dst.getAbsolutePath() + ".filepart");
		//remove file part
		file.delete();
		//		final long[] update = new long[2];
		final FileDownloadUpdate e = new FileDownloadUpdate();
		try {
			FileUtil.copyURLToFile(FileUtil.convertToURLEscapingIllegalCharacters(sourceFilePath), file, 50000, 50000, (size, diff) -> {

				o.lastSpeedSize += diff;

				o.currentSize += diff;
				e.downloaded = size;
				e.size = ChecksumFileEntry.this.size;
				e.fileName = name;
				e.index = o.index;
				e.total = o.total;
				e.totalSize = o.totalSize;
				e.currentSize = o.currentSize;

				long diffTime = System.currentTimeMillis() - o.startTime;
				if (diffTime / 200 > 1) {
					double secs = diffTime / 200d;

					o.downloadSpeed = o.lastSpeedSize / secs;
					o.lastSpeedSize = 0;
					o.startTime = System.currentTimeMillis();

				}
				e.downloadSpeed = o.downloadSpeed;

				int s = Integer.MAX_VALUE;
				synchronized (ChecksumFile.running) {
					for (ChecksumFileEntry en : ChecksumFile.running) {
						s = Math.min(en.index, s);
					}
				}
				if (s == index) {
//						System.err.println("INDNN "+index+": "+ChecksumFile.running);
					cb.update(e);
				} else {
//						System.err.println(s+" INDNN "+index+": "+ChecksumFile.running);
				}
			}, "dev", "dev", true);

			file.renameTo(dst);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			throw new IOException(e1);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return index == ((ChecksumFileEntry) obj).index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChecksumFileEntry [size=" + size + ", checksum=" + checksum
				+ ", relativePath=" + relativePath + ", index " + index + "]";
	}

}
