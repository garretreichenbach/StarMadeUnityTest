package org.schema.common.util.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Hardware4Win {

	private static String sn = null;

	public static final String getSerialNumber() {

		if (sn != null) {
			return sn;
		}

		OutputStream os = null;
		InputStream is = null;

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[]{"wmic", "bios", "get", "serialnumber"});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		os = process.getOutputStream();
		is = process.getInputStream();

		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Scanner sc = new Scanner(is);
		try {
			boolean started = false;
			while (sc.hasNext()) {
				String next = sc.next();
				if (started) {
					sn += next.trim() + " ";
				}
				if (!started) {
					started = "SerialNumber".equals(next);
					sn = "";
				}

			}
			if (started) {
				sn = sn.trim();
			}
		} finally {
			try {
				is.close();
				sc.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (sn == null) {
			throw new RuntimeException("Cannot find computer SN");
		}

		return sn;
	}
}
