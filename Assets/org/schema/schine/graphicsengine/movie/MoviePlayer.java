package org.schema.schine.graphicsengine.movie;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.movie.AudioRenderer.State;
import org.schema.schine.graphicsengine.movie.craterstudio.util.RunningAvg;
import org.schema.schine.graphicsengine.movie.subtitles.AbstractSubtitleManager;
import org.schema.schine.graphicsengine.movie.subtitles.SubtitleFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB;
import static org.lwjgl.opengl.ARBVertexBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class MoviePlayer {

	public AbstractSubtitleManager subtitles;
	
	public File movieFile;
	public Movie movie;
//	public OpenALAudioRenderer audioRenderer;
	public AudioRenderer audioRenderer;
	public boolean videoOnly = false;

	public MoviePlayer(File movieFile) throws IOException {
		this.movieFile = movieFile;


//		try{
//		 InputStream wav16Stream = FFmpeg.extractAudioAsWAV(movieFile, 0);
//		 RIFFReader r = new RIFFReader(wav16Stream);
//		}catch(Exception e){
//			e.printStackTrace();
//		 if(true){
//			 try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
//		 }
//		}
//		assert(testIO());
		movie = Movie.open(movieFile);

		
		subtitles = SubtitleFactory.getManagerFromVideoFile(movieFile);
		
		
		boolean usePBOs = true;
		if (usePBOs) {
			pboHandle = 0;
		} else {
			pboHandle = -1;
		}

		this.init();
	}

	public int textureHandle;
	private int pboHandle;

	private boolean usePBO() {
		return pboHandle >= 0;
	}

	private void init() {
		audioRenderer = new OpenALAudioRenderer();// SoundSystemAudioRenderer(movieFile);
		audioRenderer.init(movie.audioStream(), movie.framerate());

		// create texture holding video frame
		textureHandle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureHandle);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glTexImage2D(GL_TEXTURE_2D, 0/* level */, GL_RGB, movie.width(), movie.height(), 0/* border */, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);

		if (usePBO()) {
			pboHandle = glGenBuffersARB();
			glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, pboHandle);
			glBufferDataARB(GL_PIXEL_UNPACK_BUFFER_ARB, movie.width() * movie.height() * 3, GL_STREAM_DRAW_ARB);
			glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, 0);
		}
	}

	private int offsetInSeconds;
	private boolean closed;
	private boolean paused;
	private boolean seekDone;

	public int getSeconds() {
		return offsetInSeconds;
	}

	public void relativeSeek(int seconds) throws IOException {
		seconds = offsetInSeconds + (int) movie.getPlayingTime() + seconds;
		this.absoluteSeek(Math.max(0, seconds));
	}

	public void absoluteSeek(int seconds) throws IOException {
		this.offsetInSeconds = seconds;

		// audioRenderer.await();
		audioRenderer.close();
		audioRenderer = null;

		movie.close();
		movie = null;
		movie = Movie.open(movieFile, seconds);

		totalFrameTime = seconds;
		frameTime = 0;
		
		this.init();
		seekDone = true;
		if(videoOnly) audio().setVolume(0.0f);
	}

	public void tick() {
		if(isSound()){
			audioRenderer.tick(movie);
		}
		if(seekDone){
			if(isPaused()){
				pause();
			}
			seekDone = false;
		}
	}
	public float passed(){
		return (float) totalFrameTime;
	}
	public float duration(){
		return movie.durationSecs();
	}
	public float getPercentDone(){
		return passed() / duration();
	}
	public AudioRenderer audio() {
		return audioRenderer;
	}

	public boolean isPlaying() {
		return audioRenderer != null && audioRenderer.getState() == State.PLAYING;
	}

	public boolean isPaused() {
		return (audioRenderer != null && audioRenderer.getState() == State.PAUSED) || this.paused ;
	}

	public void pause() {
		audioRenderer.pause();
		this.paused = true;
	}

	public void resume() {
		if(ended || closed){
			try {
				absoluteSeek(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			ended = false;
			closed = false;
		}else{
			audioRenderer.resume();
		}
		this.paused = false;
	}

	public void stop() {
		audioRenderer.stop();
	}

	public void close() throws IOException {
		glDeleteTextures(textureHandle);
		textureHandle = 0;

		audioRenderer.close();
		movie.close();
		
		if(pboHandle > 0){
			glDeleteBuffersARB(pboHandle);
		}
		this.closed = true;
	}

	public RunningAvg textureUpdateTook = new RunningAvg(20);
	private double frameTime;
	private double totalFrameTime;
	private boolean ended;
	private boolean looping;

	public static boolean isSound(){
		return true;
	}
	
	public boolean syncTexture(int maxFramesBacklog, Timer timer) {
		glBindTexture(GL_TEXTURE_2D, textureHandle);

		ByteBuffer texBuffer = null;

		boolean timeForNextFrame = false;
		if(!isPaused() && !ended && !closed){
			frameTime += timer.getDelta();
			totalFrameTime += timer.getDelta();
		}
		
		boolean sound = isSound();
		if(sound && movie.isTimeForNextFrame()){
			timeForNextFrame = true;
		}
		double frames = 0;
		double frameT = (1d/movie.framerate());
		if(!sound && (frames = Math.floor(frameTime / frameT)) > 0d){
			timeForNextFrame = true;
			frameTime -= (frames*frameT);
			frames--;
		}
		if(isPaused()){
			timeForNextFrame = false;
		}
//		System.err.println("FRAME ::: LOOP: "+looping+"; "+timeForNextFrame+"; "+frameTime+"; "+Math.floor((double)frameTime / frameT)+"; "+frameT);
		if (timeForNextFrame) {
			int framesRead = 0;
			do {
				if (framesRead > 0) {
					movie.videoStream().freeFrameData(texBuffer);
					texBuffer = null;

					// signal the AV-sync that we processed a frame
					movie.onUpdatedVideoFrame();
				}

				// grab the next frame from the video stream
				texBuffer = movie.videoStream().pollFrameData();
				if (texBuffer == VideoStream.EOF) {
					if(looping){
						try {
							absoluteSeek(0);
							resume();
							if(videoOnly) audio().setVolume(0.0f);
							looping = true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else{
						setEnded(true);
					}
					return false;
				}
				if (texBuffer == null) {
					return true;
				}

				framesRead++;
			} while ((sound && movie.hasVideoBacklogOver(maxFramesBacklog)) || (!sound && frames-- > 0d));
			if (framesRead > 1) {
				System.out.println("video frames skipped: " + (framesRead - 1));
			}

			{
				long tStart = System.nanoTime();

				if (usePBO()) {
					glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, pboHandle);

					ByteBuffer mapped = glMapBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, GL_WRITE_ONLY_ARB, movie.width() * movie.height() * 3, null);
					mapped.put(texBuffer);
					glUnmapBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB);

					glTexSubImage2D(GL_TEXTURE_2D, 0/* level */, 0, 0, movie.width(), movie.height(), GL_RGB, GL_UNSIGNED_BYTE, 0);

					glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, 0);
				} else {
					glTexSubImage2D(GL_TEXTURE_2D, 0/* level */, 0, 0, movie.width(), movie.height(), GL_RGB, GL_UNSIGNED_BYTE, texBuffer);
				}

				textureUpdateTook.add(System.nanoTime() - tStart);

				movie.videoStream().freeFrameData(texBuffer);
				texBuffer = null;
			}
			// signal the AV-sync that we processed a frame
			movie.onUpdatedVideoFrame();
		}
		
//		System.err.println("FPS: "+1.0 / (totalFrameTime/(double)totalRead));
		return true;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isClosed() {
		return closed;
	}

	public void setLooping(boolean b) {
		this.looping = b;
	}

}
