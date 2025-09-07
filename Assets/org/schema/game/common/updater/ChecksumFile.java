package org.schema.game.common.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.schema.common.util.StringTools;

public class ChecksumFile {
	public static Set<ChecksumFileEntry> running = new HashSet<ChecksumFileEntry>();
	public ArrayList<ChecksumFileEntry> checksums = new ArrayList<ChecksumFileEntry>();
	int toExecute;
	private int failed;

	public void parse(BufferedReader in) throws IOException {
		String line;

		while ((line = in.readLine()) != null) {

			//			./data/audio-resource/Gameplay/0022_gameplay - cockpit warning beep.ogg 14722 90b34870b3e9df9c5892d23f94f9df710716685a

			line = line.trim();
			int hashIndex = line.lastIndexOf(" ");
			if (hashIndex < 0) {
				throw new IOException("Checksum file invalid [CHECKSUMNOTFOUND]: " + line);
			}

			String checksum = line.substring(hashIndex, line.length()).trim();

			//remove hash part
			line = line.substring(0, hashIndex).trim();

			int sizeIndex = line.lastIndexOf(" ");
			if (sizeIndex < 0) {
				throw new IOException("Checksum file invalid [SIZENOTFOUND]: " + line);
			}

			String sizeStr = line.substring(sizeIndex, line.length()).trim();

			long size;
			try {
				size = Long.parseLong(sizeStr.trim());
			} catch (NumberFormatException e) {
				throw new IOException("Checksum file invalid [SIZEINVALID]: " + sizeStr + " (line left: " + line + ")", e);
			}
			//remove size part
			line = line.substring(0, sizeIndex).trim();

			String relativePath = line.trim();

			ChecksumFileEntry e = new ChecksumFileEntry(size, checksum, relativePath);

			checksums.add(e);
		}

		//put the version file last
		for (int i = 0; i < checksums.size(); i++) {
			if (checksums.get(i).relativePath.equals("./version.txt")) {
				ChecksumFileEntry remove = checksums.remove(i);
				checksums.add(remove);
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("---------------CHECKSUMS-----------------\n");
		for (int i = 0; i < checksums.size(); i++) {
			sb.append(checksums.get(i));
			sb.append("\n");
		}
		sb.append("-----------------------------------------\n");
		return sb.toString();
	}

	public void download(final boolean force, final String buildPath, final File installDir, final String installDirStr, final FileDowloadCallback cb) throws NoSuchAlgorithmException, IOException {

		final ArrayList<ChecksumFileEntry> checksumsToDownload = new ArrayList<ChecksumFileEntry>();

		cb.update("Determining files to download... ");

		final FileUpdateTotal o = new FileUpdateTotal();
		float p = 1f / checksums.size();
		float g = 0;
		for (ChecksumFileEntry e : checksums) {
			if (force || e.needsDownload(buildPath, installDirStr)) {
				checksumsToDownload.add(e);
				o.totalSize += e.size;
			}

			cb.update("Determining files to download... " + (StringTools.formatPointZero(g * p * 100.0f)) + "%  selected " + checksumsToDownload.size() + " / " + checksums.size() + "(" + (o.totalSize / 1024) / 1024 + " MB)");
			g++;
		}
		o.startTime = System.currentTimeMillis();

		ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
		toExecute = checksumsToDownload.size();
		failed = 0;
		running.clear();
		for (int i = 0; i < checksumsToDownload.size(); i++) {
			final ChecksumFileEntry e = checksumsToDownload.get(i);

			e.index = i;

			synchronized (running) {
				boolean add = running.add(e);
//				System.err.println("STARTED: "+e+"; "+running);
				assert (add);
			}
			pool.execute(() -> {

				try {
					o.index = e.index;
					o.total = checksumsToDownload.size();
					e.download(force, buildPath, installDir, installDirStr, cb, o);
				} catch (Exception e1) {
					e1.printStackTrace();
					failed++;
				}
				synchronized (running) {
					boolean remove = running.remove(e);
//						System.err.println("FINISHED: "+e);
					assert (remove);
				}
				toExecute--;
			});

		}

		while (toExecute > 0) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		if (failed > 0) {
			throw new IOException("Download failed on " + failed + " file" + (failed > 1 ? "s" : "") + "\nplease redownload forced from the options");
		}
		pool.shutdown();
	}

}
