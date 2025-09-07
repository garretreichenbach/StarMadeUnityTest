package org.schema.schine.sound.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.vecmath.Vector3f;

import org.schema.common.util.AssetId;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.loaders.AudioLoadEntry;
import org.schema.schine.sound.loaders.OGGLoader;
import org.schema.schine.sound.manager.engine.AudioContext;
import org.schema.schine.sound.manager.engine.AudioData;
import org.schema.schine.sound.manager.engine.AudioId;
import org.schema.schine.sound.manager.engine.AudioListener;
import org.schema.schine.sound.manager.engine.AudioListenerDefault;
import org.schema.schine.sound.manager.engine.AudioNode;
import org.schema.schine.sound.manager.engine.AudioSource.Status;
import org.schema.schine.sound.manager.engine.lwjgl.LwjglAL;
import org.schema.schine.sound.manager.engine.lwjgl.LwjglALC;
import org.schema.schine.sound.manager.engine.lwjgl.LwjglEFX;
import org.schema.schine.sound.manager.engine.openal.ALAudioRenderer;

public class SoundEngine {
	
	public void initialize(){
		initializeRenderer();
	}
	
	public static void main(String[] args) throws Exception{
		try{
			loadAllAudioLibs();
			SoundEngine engine = new SoundEngine();
			engine.initialize();
			
			
			
			AudioListener l = new AudioListenerDefault();
			AudioContext.getAudioRenderer().setListener(l);
			
			final TransformTimed t = new TransformTimed();
			t.setIdentity();
			
//			final String path = "./data/audio-resource/Menu UI/0022_menu_ui - swoosh scroll large.ogg";
			final String path = "./data/audio-resource/Spaceship User/0022_spaceship_user_-_medium_engine_thrusters_loop - mono.ogg";
			final String name = "testSound";
			final AudioId id = new AudioId(name);
			
			OGGLoader ogg = new OGGLoader();
			
			AudioData data = ogg.load(new AudioLoadEntry("") {
				@Override
				public InputStream openStream() {
					try {
						return new BufferedInputStream(new FileInputStream(path));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public AssetId<AudioId> getId() {
					return id;
				}

				@Override
				protected void loadResource(ResourceLoader resourceLoader) throws IOException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public String getFilePath() {
					// TODO Auto-generated method stub
					return null;
				}
			});
			AudioNode n = new AudioNode(data, id) {
				
				@Override
				public TransformTimed getWorldTransform() {
					return t;
				}
				
				@Override
				public Vector3f getPosition() {
					return t.origin;
				}
				
				@Override
				public AudioAsset getAsset() {
					return null;
				}
				@Override
				public boolean isPlayInstanced() {
					return false;
				}
			};
			
			n.setPositional(true);
			
			
			n.setVelocityFromTranslation(true);
			n.setReverbEnabled(true);
			n.play();
			boolean moving = true;
			
			n.setLooping(true);
			
			try {
				float d = 0.5f;
				
				float delta = 0.01f;
				float dd = 0;
				while(true){
					if(moving){
						n.getWorldTransform().origin.z += d;
					}
					if(Math.abs(n.getWorldTransform().origin.z) > 30){
						d = -d;
					}
					
					System.err.println(n.getWorldTransform().origin);
					n.updateGeometricState(delta);
					AudioContext.getAudioRenderer().update(0.05f);
					Thread.sleep((long)(delta*1000f));
					dd += delta;
					if(dd >= 5){
						System.err.println("DISABLED LOOPING");
						n.setLooping(false);
					}
					if(n.getStatus() == Status.STOPPED){
						System.err.println("STOPPED PLAYBACK");
						break;
					}
				}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			org.lwjgl.openal.AL.destroy();
		}catch(Exception r){
//			org.lwjgl.openal.AL.destroy();
			throw r;
		}
	}
	public static void playTestSound() {
		try{
			
			
			final TransformTimed t = new TransformTimed();
			t.setIdentity();
			
			final String pathSound = "./data/audio-resource/Spaceship User/0022_spaceship_user_-_medium_engine_thrusters_loop - mono.ogg";
			final String name = "testSound";
			final AudioId id = new AudioId(name);
			
			OGGLoader ogg = new OGGLoader();
			
			AudioData data = ogg.load(new AudioLoadEntry("") {
				@Override
				public InputStream openStream() {
					try {
						return new BufferedInputStream(new FileInputStream(pathSound));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public AssetId<AudioId> getId() {
					return id;
				}

				@Override
				protected void loadResource(ResourceLoader resourceLoader) throws IOException {
					
				}

				@Override
				public String getFilePath() {
					return null;
				}
			});
			AudioNode n = new AudioNode(data, id) {
				@Override
				public boolean isPlayInstanced() {
					return false;
				}
				@Override
				public TransformTimed getWorldTransform() {
					return t;
				}
				
				@Override
				public Vector3f getPosition() {
					return t.origin;
				}

				@Override
				public AudioAsset getAsset() {
					return null;
				}
				
			};
			
			n.setPositional(true);
			
			
			n.setVelocityFromTranslation(true);
			n.setReverbEnabled(true);
			n.play();
			boolean moving = true;
			
			n.setLooping(true);
			
			try {
				float d = 0.5f;
				
				float delta = 0.01f;
				float dd = 0;
				while(true){
					if(moving){
						n.getWorldTransform().origin.z += d;
					}
					if(Math.abs(n.getWorldTransform().origin.z) > 30){
						d = -d;
					}
					
					System.err.println(n.getWorldTransform().origin);
					n.updateGeometricState(delta);
					AudioContext.getAudioRenderer().update(0.05f);
					Thread.sleep((long)(delta*1000f));
					dd += delta;
					if(dd >= 5){
						System.err.println("DISABLED LOOPING");
						n.setLooping(false);
					}
					if(n.getStatus() == Status.STOPPED){
						System.err.println("STOPPED PLAYBACK");
						break;
					}
				}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			org.lwjgl.openal.AL.destroy();
		}catch(Exception r){
//			org.lwjgl.openal.AL.destroy();
			throw new RuntimeException(r);
		}
	}
	public enum Library{
		LWJGL,
	}
	private void initializeRenderer() {
//		AudioRenderer renderer = newAudioRenderer(Library.LWJGL);
//		renderer.initialize();
//		AudioContext.setAudioRenderer(renderer);
////		renderer.setEnvironment(new Environment());
//		renderer.setEnvironment(Environment.Cavern);
	}
//	public AudioRenderer newAudioRenderer(Library settings) {

//        AL al;
//        ALC alc;
//        EFX efx;
//        if (settings == Library.LWJGL) {
//            al = new LwjglAL();
//            alc = new LwjglALC();
//            efx = new LwjglEFX();
//        } else {
//            throw new UnsupportedOperationException(
//                    "Unrecognizable audio renderer specified: "
//                    + settings.name());
//        }
//
//        if (al == null || alc == null || efx == null) {
//            return null;
//        }
//
//        return new ALAudioRenderer(al, alc, efx);
//    }
	
	public static boolean is64Bit() {
		return System.getProperty("os.arch").contains("64");
	}
	private static void addLibraryPath(String s) throws Exception {
		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);

		final String[] paths = (String[]) usrPathsField.get(null);

		for (String path : paths) {
			if (path.equals(s)) {
				return;
			}
		}

		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = s;
		usrPathsField.set(null, newPaths);
	}
	public static final String NATIVE_LIB_PATH = "." + File.separator + "native" + File.separator;
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
	public static void loadAllAudioLibs(){
		ALAudioRenderer alAudioRenderer = new ALAudioRenderer(new LwjglAL(), new LwjglALC(), new LwjglEFX());
		AudioContext.setAudioRenderer(alAudioRenderer);
		
		
		
//		try {
//			loadAudioLibs();
////			org.lwjgl.openal.AL.create(); //need to create context. without this we get unsatisfied link errors
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
