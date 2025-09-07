package org.schema.schine.graphicsengine.movie;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.schema.schine.graphicsengine.movie.craterstudio.math.EasyMath;
import org.schema.schine.graphicsengine.movie.craterstudio.util.concur.SimpleBlockingQueue;
import org.schema.schine.sound.controller.AudioController;

import paulscode.sound.SoundSystemConfig;

public class SoundSystemAudioRenderer extends AudioRenderer{
	private int lastBuffersProcessed = 0;
	private boolean hasMoreSamples = true;

	private static enum ActionType {
		ADJUST_VOLUME, PAUSE_AUDIO, RESUME_AUDIO, STOP_AUDIO
	}

	private static class Action {
		final ActionType type;
		public Action(ActionType type, Object value) {
			this.type = type;
		}
	}

	final SimpleBlockingQueue<Action> pendingActions = new SimpleBlockingQueue<Action>();

	//

	private float volume = 1.0f;
	public SoundSystemAudioRenderer(File movieFile) {
	}

	@Override
	public void setVolume(float volume) {
		if (!EasyMath.isBetween(volume, 0.0f, 1.0f)) {
			throw new IllegalArgumentException();
		}
		this.volume = volume;
		pendingActions.put(new Action(ActionType.ADJUST_VOLUME, Float.valueOf(volume)));
	}

	@Override
	public float getVolume() {
		return this.volume;
	}

	//

	@Override
	public void pause() {
		pendingActions.put(new Action(ActionType.PAUSE_AUDIO, null));
	}

	@Override
	public void resume() {
		pendingActions.put(new Action(ActionType.RESUME_AUDIO, null));
	}

	@Override
	public void stop() {
		pendingActions.put(new Action(ActionType.STOP_AUDIO, null));
	}

	private State state = State.INIT;

	@Override
	public State getState() {
		return state;
	}

	private ByteBuffer samples;
	private byte[] buff;

	private void init(Movie sync) throws UnsupportedAudioFileException, IOException {
//		this.alSource = alGenSources();
//		if (this.alSource == 0) {
//			throw new IllegalStateException();
//		}
		final int bytecount = SoundSystemConfig.getStreamingBufferSize();
        byte[] b;
       buff = new byte[bytecount];
       
//		 AudioInputStream in = AudioSystem.getAudioInputStream(new RIFFInputStreamHack(wav16Stream));
//       
//	        AudioFormat baseFormat = audioStream.audioFormat; //in.getFormat();
//       AudioFormat decodedFormat = new AudioFormat( //PCM_SIGNED,
//               audioStream.sampleRate, //baseFormat.getSampleRate(),
//               audioStream.bytesPerSample * 8,
//               audioStream.numChannels, //baseFormat.getChannels(),
//               true,
//               false);
       System.err.println("FPD:::: "+frameRate+"; "+audioStream.byteRate);
	        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, //PCM_SIGNED,
	                audioStream.sampleRate, //baseFormat.getSampleRate(),
	                audioStream.bytesPerSample * 8,
	                audioStream.numChannels, //baseFormat.getChannels(),
	                audioStream.bytesPerSample * audioStream.numChannels, //baseFormat.getChannels() * 2,
	                frameRate, //audioStream.sampleRate, // baseFormat.getSampleRate(),
	                false);
//	        AudioSystem
//	        din = AudioSystem.getAudioInputStream(decodedFormat, in);
//		 AudioFormat baseFormat = in.getFormat();
//         AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                 baseFormat.getSampleRate(),
//                 16,
//                 baseFormat.getChannels(),
//                 baseFormat.getChannels() * 2,
//                 baseFormat.getSampleRate(),
//                 false);
		 	
//        final AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
	        AudioController.rawDataStream(decodedFormat, true, "movie",0, 0, 0, 1, 20.0f);
//	        Controller.getAudioManager().play("movie");
//	        (new Thread(new Runnable(){
//
//				@Override
//				public void run() {
//					int bytes;
//					try {
//						System.err.println("ON THREAD");
//						while( ( bytes = din.read( buff, 0, bytecount ) ) != -1 )
//						{
//							System.err.println("RUNNING THREAD");
//							 Controller.getAudioManager().feedRaw("movie", buff );
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//	        	
//	        })).start();;
	}

	private void buffer() {
		// buffer 1sec of audio
		for (int i = 0; i < 5 || i < frameRate * 0.1f; i++) {
			this.enqueueNextSamples();
		}

//		alSourcePlay(alSource);
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public boolean tick(Movie sync) {

		switch (this.state) {
			case INIT:
			try {
				this.init(sync);
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
				this.state = State.BUFFERING;
				return true;

			case BUFFERING:
				this.buffer();
				this.state = State.PLAYING;
				return true;

			case CLOSED:
				return false;
		}


		/*
		 * Handle pending actions
		 */

		for (Action action; (action = pendingActions.poll()) != null;) {
			switch (action.type) {
				case ADJUST_VOLUME:
//					alSourcef(alSource, AL_GAIN, action.floatValue());
					break;

				case PAUSE_AUDIO:
//					alSourcePause(alSource);
					state = State.PAUSED;
					return true;

				case RESUME_AUDIO:
//					alSourcePlay(alSource);
					state = State.PLAYING;
					break;

				case STOP_AUDIO:
//					alSourceStop(alSource);
					state = State.CLOSED;
					break;

				default:
					throw new IllegalStateException();
			}
		}

		switch (this.state) {
			case PLAYING:
				if(samples == null){
					try {
						close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return false;
				}
				while(samples.remaining() > 0){
					
					samples.get(buff, 0, Math.min(samples.remaining(), buff.length));
					System.err.println("PLAY ");
					AudioController.feedRaw("movie", buff);
				}
				System.err.println("PLAY ##################");
				if(samples.remaining() == 0){
					enqueueNextSamples();
				}
				sync.onRenderedAudioBuffer();
			case CLOSED:
				break;

			case PAUSED:
				return true;

			default:
				throw new IllegalStateException();
		}

		int currentBuffersProcessed = 1;//alGetSourcei(alSource, AL_BUFFERS_PROCESSED);
		int toDiscard = currentBuffersProcessed - lastBuffersProcessed;
		lastBuffersProcessed = currentBuffersProcessed;

		if (toDiscard == 0) {
			return true;
		}
		if (toDiscard < 0) {
			throw new IllegalStateException();
		}

//		for (int i = 0; i < toDiscard; i++) {
//			int buffer = alSourceUnqueueBuffers(alSource);
//			alDeleteBuffers(buffer);
//
//			this.lastBuffersProcessed--;
//			sync.onRenderedAudioBuffer();
//
//			this.enqueueNextSamples();
//		}

//		switch (alGetSourcei(alSource, AL_SOURCE_STATE)) {
//			case AL_PLAYING:
//				// ok
//				break;
//
//			case AL_STOPPED:
//				if (this.state == State.CLOSED) {
//					while (true) {
//						int buffer = alSourceUnqueueBuffers(alSource);
//						if (buffer < 0) {
//							break;
//						}
//						alDeleteBuffers(buffer);
//					}
//					this.state = State.CLOSED;
//				}
//
//				if (this.lastBuffersProcessed != 0) {
//					throw new IllegalStateException("should never happen");
//				}
//
//				if (this.state != State.CLOSED && this.hasMoreSamples) {
//					this.state = State.BUFFERING;
//				} else {
//					sync.onEndOfAudio();
//					Streams.safeClose(this);
//					return false;
//				}
//				break;
//
//			default:
//				throw new IllegalStateException("unexpected state");
//		}

		return true;
	}

	private void enqueueNextSamples() {
		if (!this.hasMoreSamples) {
			return;
		}

		samples = super.loadNextSamples();
		if (samples == null) {
			this.hasMoreSamples = false;
			return;
		}

//		int buffer = alGenBuffers();
//		alBufferData(buffer, AL_FORMAT_STEREO16, samples, audioStream.sampleRate);
//		alSourceQueueBuffers(this.alSource, buffer);
	}

	public void await() throws IOException {
//		while (alGetSourcei(alSource, AL_SOURCE_STATE) == AL_PLAYING) {
//			HighLevel.sleep(1);
//		}
	}

	@Override
	public void close() throws IOException {
//		if (this.alSource != 0) {
//			alSourceStop(this.alSource);
//			alDeleteSources(this.alSource);
			this.state = State.CLOSED;
			AudioController.closeStream("movie");
//		}

		super.close();
	}

}
