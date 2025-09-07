package org.schema.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipFile;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class ZipUtils {

	public static void unzip(String zipFilePath, String destDir) {
		destDir = destDir.replaceAll("\\\\", "/"); // Replace Windows backslashes with forward slashes
		destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
		System.out.println("Unzipping " + zipFilePath + " to " + destDir);
		File dir = new File(destDir);
		if(!dir.exists()) dir.mkdirs();
		try {
			ZipFile zipFile = new ZipFile(zipFilePath);
			String finalDestDir = destDir;
			zipFile.stream().forEach(entry -> {
				File file = new File(finalDestDir + entry.getName());
				if(entry.isDirectory()) file.mkdirs();
				else {
					try {
						file.getParentFile().mkdirs();
						zipFile.getInputStream(entry).transferTo(new FileOutputStream(file));
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			});
			zipFile.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
