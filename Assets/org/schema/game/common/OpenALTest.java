package org.schema.game.common;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.data.ResourceUtil;
import org.schema.schine.resource.FileExt;

public class OpenALTest {
	/**
	 * Buffers hold sound data.
	 */
	IntBuffer buffer = MemoryUtil.memAllocInt(1);
	/**
	 * Sources are points emitting sound.
	 */
	IntBuffer source = MemoryUtil.memAllocInt(1);
	/**
	 * Position of the source sound.
	 */
	FloatBuffer sourcePos = (FloatBuffer) MemoryUtil.memAllocFloat(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/**
	 * Velocity of the source sound.
	 */
	FloatBuffer sourceVel = (FloatBuffer) MemoryUtil.memAllocFloat(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/**
	 * Position of the listener.
	 */
	FloatBuffer listenerPos = (FloatBuffer) MemoryUtil.memAllocFloat(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/**
	 * Velocity of the listener.
	 */
	FloatBuffer listenerVel = (FloatBuffer) MemoryUtil.memAllocFloat(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/**
	 * Orientation of the listener. (first 3 elements are "at", second 3 are "up")
	 */
	FloatBuffer listenerOri = (FloatBuffer) MemoryUtil.memAllocFloat(6).put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).rewind();

	public static void main(String[] args) {
		try {
			LibLoader.loadNativeLibs(false, -1, true);
			new OpenALTest().execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void execute() {
		// Initialize OpenAL and clear the error bit.
//		try {
//			AL.create();
//		} catch (LWJGLException le) {
//			le.printStackTrace();
//			return;
//		}
		System.err.println("ERROR? " + AL10.alGetError());

		// Load the wav data.
		if (loadALData() == AL10.AL_FALSE) {
			System.out.println("Error loading data.");
			return;
		}

		setListenerValues();

		// Loop.
		System.out.println("OpenAL Tutorial 1 - Single Static Source");
		System.out.println("[Menu]");
		System.out.println("p - Play the sample.");
		System.out.println("s - Stop the sample.");
		System.out.println("h - Pause the sample.");
		System.out.println("q - Quit the program.");
		char c = ' ';
		AL10.alSourcePlay(source.get(0));
		@SuppressWarnings("resource")
		Scanner stdin = new Scanner(System.in);
		while (c != 'q') {
			try {
				System.out.print("Input: ");
				c = stdin.nextLine().charAt(0);
			} catch (Exception ex) {
				c = 'q';
			}
			switch(c) {
				// Pressing 'p' will begin playing the sample.
				case 'p' -> {
					System.err.println("PLAY");
					AL10.alSourcePlay(source.get(0));
				}
				// Pressing 's' will stop the sample from playing.
				case 's' -> AL10.alSourceStop(source.get(0));
				// Pressing 'h' will pause the sample.
				case 'h' -> AL10.alSourcePause(source.get(0));
			}
			;
		}
		killALData();
//		AL.destroy();
	}

	/**
	 * void killALData()
	 * <p/>
	 * We have allocated memory for our buffers and sources which needs
	 * to be returned to the system. This function frees that memory.
	 */
	void killALData() {
		AL10.alDeleteSources(source);
		AL10.alDeleteBuffers(buffer);
	}

	/**
	 * boolean LoadALData()
	 * <p/>
	 * This function will load our sample data from the disk using the Alut
	 * utility and send the data into OpenAL as a buffer. A source is then
	 * also created to play that buffer.
	 */
	int loadALData() {
		// Load wav data into a buffer.
		AL10.alGenBuffers(buffer);

		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		//Loads the wave file from your file system
		/*java.io.FileInputStream fin = null;
	try {
      fin = new java.io.FileInputStream("FancyPants.wav");
    } catch (java.io.FileNotFoundException ex) {
      ex.printStackTrace();
      return AL10.AL_FALSE;
    }
    WaveData waveFile = WaveData.create(fin);
    try {
      fin.close();
    } catch (java.io.IOException ex) {
    }*/

		//Loads the wave file from this class's package in your classpath
		//FancyPants
		//0022_explosion_2wav
		File file = new FileExt("./data/audio-resource/Explosions/FancyPants.wav");

		ResourceUtil r = new ResourceUtil();
		String p = file.getAbsolutePath();
		System.err.println("EXISTS: " + file.exists() + " -> " + p);
//		WaveData waveFile = null;
//		try {
//			waveFile = WaveData.create(new BufferedInputStream(r.getResourceAsInputStream(p)));
//		} catch (ResourceException e) {
//			e.printStackTrace();
//		}
//		System.err.println(waveFile + " " + waveFile.format + ", " + waveFile.data + ", " + waveFile.samplerate);
//
//		AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
//		waveFile.dispose();

		// Bind the buffer with the source.
		AL10.alGenSources(source);

		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		AL10.alSourcei(source.get(0), AL10.AL_BUFFER, buffer.get(0));
		AL10.alSourcef(source.get(0), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(source.get(0), AL10.AL_GAIN, 1.0f);
		AL10.alSourcefv(source.get(0), AL10.AL_POSITION, sourcePos);
		AL10.alSourcefv(source.get(0), AL10.AL_VELOCITY, sourceVel);

		// Do another error check and return.
		if (AL10.alGetError() == AL10.AL_NO_ERROR)
			return AL10.AL_TRUE;

		return AL10.AL_FALSE;
	}

	/**
	 * void setListenerValues()
	 * <p/>
	 * We already defined certain values for the Listener, but we need
	 * to tell OpenAL to use that data. This function does just that.
	 */
	void setListenerValues() {
		AL10.alListenerfv(AL10.AL_POSITION, listenerPos);
		AL10.alListenerfv(AL10.AL_VELOCITY, listenerVel);
		AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOri);
	}
}

