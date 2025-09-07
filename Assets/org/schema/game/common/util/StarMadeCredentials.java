package org.schema.game.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.schema.common.util.security.OperatingSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.FileExt;

public class StarMadeCredentials {

	private final String passwd;
	private final String user;

	public StarMadeCredentials(String user, String passwd) {
		super();
		this.passwd = passwd;
		this.user = user;
	}

	public static File getPath() throws IOException {

		//		return new FileExt("L:\\git\\StarMade");

		return OperatingSystem.getAppDir();
	}

	public static boolean exists() {
		try {
			return (new FileExt(getPath(), "cred")).exists();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static StarMadeCredentials read() throws Exception {
		String passwd;
		String user;

		BufferedReader r = new BufferedReader(new FileReader(new FileExt(getPath(), "cred")));
		try {
			user = r.readLine();
			String encryptedPasswd = r.readLine();
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(getMac());
			passwd = textEncryptor.decrypt(encryptedPasswd.replaceAll("(\\r|\\n)", ""));
		} catch(EncryptionOperationNotPossibleException e) {
			removeFile();
			throw new Exception(Lng.str("Something went wrong reading your encrypted uplink credentials. This can happen due to operating system updates and/or a lot of other reasons.\n\nFIX: Please use the uplink button in 'Online Play' to re-enter your star-made.org credentials.\n"), e);
		}catch (Exception e) {
			removeFile();
			throw e;
		} finally {
			r.close();
		}

		return new StarMadeCredentials(user, passwd);
	}

	public static void removeFile() throws IOException {
		(new FileExt(getPath(), "cred")).delete();
	}

	private static String getJavaExec() {
		if (System.getProperty("os.name").equals("Mac OS X") || System.getProperty("os.name").contains("Linux")) {
			return "java";
		} else {
			return "javaw";
		}
	}

	public static String getMac() {
		//		return "a";

		//		String s = UUIDGen.getMACAddress();

		//
		//		byte[] bytes = s.getBytes();
		//		StringBuffer b = new StringBuffer();
		//
		//		for(int i = 0; i < bytes.length; i++){
		//			b.append(String.valueOf(bytes[i]));
		//		}
		//
		//		return b.toString();

		String starMadePath = "./data/mac.jar";
		File starmadeFile = new FileExt(starMadePath);
		String[] command = null;
		//-XX:+ShowMessageBoxOnError
		command = new String[]{getJavaExec(), "-jar", starmadeFile.getAbsolutePath()};

		ProcessBuilder pb = new ProcessBuilder(command);
		Map<String, String> env = pb.environment();
		pb.directory(new FileExt("./"));
		try {
			Process p = pb.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = r.readLine();
			//			System.err.println("####MAC: "+s);
			return s;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public static void main(String args[]) throws Exception {
		try {
			StarMadeCredentials c = read();
			c = read();

			System.err.println(c.passwd);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return the passwd
	 */
	public String getPasswd() {
		return passwd;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	public void write() throws IOException {
		removeFile();

		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		String mac = getMac();
		textEncryptor.setPassword(mac);
		String encryptedPasswd = textEncryptor.encrypt(passwd);

		FileWriter fw = new FileWriter(new FileExt(getPath(), "cred"));

		BufferedWriter bfw = new BufferedWriter(fw);
		bfw.append(user);
		bfw.newLine();
		bfw.append(encryptedPasswd);
		bfw.newLine();
		//		bfw.append(mac);
		//		bfw.newLine();
		//		bfw.append((new UUID()).toString());
		//		bfw.newLine();
		bfw.flush();
		bfw.close();

	}

	//	public static St
}
