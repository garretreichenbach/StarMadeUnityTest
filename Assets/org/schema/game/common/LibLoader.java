package org.schema.game.common;

import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;

import java.io.File;

//import org.schema.schine.occulusvr.OculusVrHelper;

public class LibLoader {

	public static final String NATIVE_LIB_PATH = "." + File.separator + "native" + File.separator;


	private static void addLibraryPath(String s) throws Exception {
//		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
//		usrPathsField.setAccessible(true);
//
//		final String[] paths = (String[]) usrPathsField.get(null);
//
//		for (String path : paths) {
//			if (path.equals(s)) {
//				return;
//			}
//		}
//
//		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
//		newPaths[newPaths.length - 1] = s;
//		usrPathsField.set(null, newPaths);
	}

	public static boolean is64Bit() {
		return System.getProperty("os.arch").contains("64");
	}
	public static void loadAudioLibs() throws Exception {
		System.out.println("[LIBLOADER] OS " + System.getProperty("os.name") + " - " + System.getProperty("os.version"));		
		System.out.println("[LIBLOADER] JAVA " + System.getProperty("java.vendor") + " - " + System.getProperty("java.version") + " - " + System.getProperty("java.home"));
		System.out.println("[LIBLOADER] ARCHITECTURE " + System.getProperty("os.arch"));
		String append = "";
		boolean x64 = is64Bit();
		if (x64) {
			append = File.separator + "x64" + File.separator;
		}
		if (System.getProperty("os.name").equals("Mac OS X")) {
			//mac doesent have x64/x86 libs
			System.out.println("[LIBLOADER] LOADED MacOS NATIVE LIBRARIES ");

		} else if (System.getProperty("os.name").contains("Linux")) {
			addLibraryPath(NATIVE_LIB_PATH + "linux");
			if (x64) {
				System.loadLibrary("openal64");
				System.out.println("[LIBLOADER] LOADED LINUX 64bit OPENAL LIBRARIES ");
			} else {
				System.loadLibrary("openal");
				System.out.println("[LIBLOADER] LOADED LINUX 32bit OPENAL LIBRARIES ");
			}
			
		} else if (System.getProperty("os.name").contains("FreeBSD")) {
		} else {
			addLibraryPath(NATIVE_LIB_PATH + "windows" + append);
			if (x64) {
//				System.load((new File(NATIVE_LIB_PATH + "windows" + append+"OpenAL64.dll")).getAbsolutePath());
				System.loadLibrary("OpenAL64");
				System.out.println("[LIBLOADER] LOADED WINDOWS 64bit NATIVE LIBRARIES ");
			} else {
				System.loadLibrary("OpenAL32");
				System.out.println("[LIBLOADER] LOADED WINDOWS 32bit NATIVE LIBRARIES ");
			}
		}
	}
	public static void loadNativeLibs(boolean isServer, int simdLevel, boolean audio) throws Exception {
		System.out.println("[LIBLOADER] OS " + System.getProperty("os.name") + " - " + System.getProperty("os.version"));
		System.out.println("[LIBLOADER] JAVA " + System.getProperty("java.vendor") + " - " + System.getProperty("java.version") + " - " + System.getProperty("java.home"));
		System.out.println("[LIBLOADER] ARCHITECTURE " + System.getProperty("os.arch"));
		String append = "";
		boolean x64 = is64Bit();
		if (x64) {
			append = File.separator + "x64" + File.separator;
		}
		if (System.getProperty("os.name").equals("Mac OS X")) {
			//mac doesent have x64/x86 libs
			addLibraryPath(NATIVE_LIB_PATH + "macosx");
			//System.loadLibrary("StarMadeNative");
			System.load((new File("native" + File.separator + "macosx" + File.separator + "libStarMadeNative.dylib")).getAbsolutePath());
			//            System.loadLibrary("libjinput-osx");
			//        	System.loadLibrary("liblwjgl");
			//        	System.loadLibrary("openal");
			System.out.println("[LIBLOADER] LOADED MacOS NATIVE LIBRARIES ");

		} else if (System.getProperty("os.name").contains("Linux")) {
			addLibraryPath(NATIVE_LIB_PATH + "linux");
			System.setProperty("java.library.path", System.getProperty("java.library.path") + ":native/linux");
			if (!isServer && audio) {
//				if (x64) {
//					System.loadLibrary("openal64");
//					System.out.println("[LIBLOADER] LOADED LINUX 64bit OPENAL LIBRARIES ");
//				} else {
//					System.loadLibrary("openal");
//					System.out.println("[LIBLOADER] LOADED LINUX 32bit OPENAL LIBRARIES ");
//				}
			}
			if(x64){
				try{
					System.load((new File("native" + File.separator + "linux" + File.separator + "libStarMadeNative64.so")).getAbsolutePath());
				}
				catch(UnsatisfiedLinkError e){
					e.printStackTrace();
					System.out.println("[LIBLOADER] GLibC 2.14 link error, falling back to 2.12. Warning: Possible performance loss ");
//					System.loadLibrary("StarMadeNativeGLibC2_12");
					System.load((new File("native" + File.separator + "linux" + File.separator + "libStarMadeNativeGLibC2_12.so")).getAbsolutePath());
				}
				//            	addLibraryPath(NATIVE_LIB_PATH+"linux");
				//            	System.loadLibrary("jinput-linux64");
				//            	System.loadLibrary("lwjgl64");
				//            	System.loadLibrary("openal64");
				System.out.println("[LIBLOADER] LOADED LINUX 64bit NATIVE LIBRARIES ");
			}else{
				System.load((new File("native" + File.separator + "linux" + File.separator + "libStarMadeNative.so")).getAbsolutePath());
				//            	System.loadLibrary("jinput-linux");
				//            	System.loadLibrary("lwjgl");
				//            	System.loadLibrary("openal");
				System.out.println("[LIBLOADER] LOADED LINUX 32bit NATIVE LIBRARIES ");
			}
		} else if (System.getProperty("os.name").contains("FreeBSD")) {
			addLibraryPath(NATIVE_LIB_PATH + "freebsd");
			addLibraryPath(NATIVE_LIB_PATH + "linux");
			
			//System.loadLibrary("StarMadeNative64");
			System.load((new File("native" + File.separator + "linux" + File.separator + "libStarMadeNative.so")).getAbsolutePath());
			
			System.out.println("[LIBLOADER] LOADED FreeBSD NATIVE LIBRARIES ");
		} else {
			addLibraryPath(NATIVE_LIB_PATH + "windows" + append);
			if (x64) {
				//System.loadLibrary("StarMadeNative64");
				System.load((new File("native" + File.separator + "windows" + File.separator + "x64" + File.separator + "StarMadeNative64.dll")).getAbsolutePath());
				//				if (!isServer){
//					System.loadLibrary("jinput-dx8_64");
//					System.loadLibrary("lwjgl64");
//					if (audio) {
//						System.loadLibrary("OpenAL64");
//					}
//				}
				System.out.println("[LIBLOADER] LOADED WINDOWS 64bit NATIVE LIBRARIES ");
			} else {
				//System.loadLibrary("StarMadeNative");
				System.load((new File("native" + File.separator + "windows" + File.separator + "StarMadeNative.dll")).getAbsolutePath());
				//				if (!isServer){
//					System.loadLibrary("jinput-dx8");
//					System.loadLibrary("lwjgl");
//					if (audio) {
//						System.loadLibrary("OpenAL32");
//					}
//				}
				System.out.println("[LIBLOADER] LOADED WINDOWS 32bit NATIVE LIBRARIES ");
			}
			try {
//				if (!isServer && EngineSettings.O_OCULUS_RENDERING.isOn()) {
//					OculusVrHelper.loadNatives();
//				}
			} catch (UnsatisfiedLinkError e) {
				System.err.println("[OCCULUS][ERROR] Occulus Libraries not loaded");
				e.printStackTrace();
			}
		}

		// FastNoiseSIMD test
		if (simdLevel >= 0){
			System.out.println("WARNING: Manually setting SIMD level to: " + simdLevel);
			FastNoiseSIMD.SetSIMDLevel(simdLevel);
		}
		//FastNoiseSIMD fn = new FastNoiseSIMD(101);
		//System.out.println("[LIBLOADER] FastNoiseSIMD Seed: " + fn.GetSeed());
		System.out.println("[LIBLOADER] FastNoiseSIMD Level: " + FastNoiseSIMD.GetSIMDLevel());
		System.out.println("[LIBLOADER] COMPLETE ");
	}
}
