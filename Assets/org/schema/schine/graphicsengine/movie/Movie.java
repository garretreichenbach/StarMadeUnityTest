package org.schema.schine.graphicsengine.movie;

/*
 * Copyright (c) 2012, Riven
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Riven nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.io.Closeable;
import java.io.File;
import java.io.IOException;


public class Movie implements Closeable {

	public static Movie open(File movieFile) throws IOException {
		return Movie.open(movieFile, 0);
	}

	public static Movie open(File movieFile, int seconds) throws IOException {
		VideoMetadata metadata = FFmpeg.extractMetadata(movieFile);

		ProcessInputStreamContainer rgb24Stream = FFmpeg.extractVideoAsRGB24(movieFile, seconds);
		ProcessInputStreamContainer wav16Stream = FFmpeg.extractAudioAsWAV(movieFile, seconds);

		
		System.err.println("[MOVIE] opening movie file: "+movieFile.getAbsolutePath());
		
		AudioStream audioStream;
		try {
			audioStream = new AudioStream(wav16Stream.stream);
		} catch (IOException exc) {
			audioStream = new AudioStream(); // no audio, feed in dummy samples
		}
		VideoStream videoStream = new VideoStream(rgb24Stream.stream, metadata);
		return new Movie(metadata, videoStream, audioStream, wav16Stream, rgb24Stream);
	}

	private ProcessInputStreamContainer pAudio;
	private ProcessInputStreamContainer pVideo;

	private Movie(VideoMetadata metadata, VideoStream videoStream, AudioStream audioStream, ProcessInputStreamContainer pVideo, ProcessInputStreamContainer pAudio) {
		this.metadata = metadata;
		this.videoStream = videoStream;
		this.audioStream = audioStream;
		this.pVideo = pVideo;
		this.pAudio = pAudio;
	}

	//

	private final VideoMetadata metadata;

	public float durationSecs() {
		return metadata.totalTimeSecs;
	}
	public int width() {
		return metadata.width;
	}

	public int height() {
		return metadata.height;
	}

	public float framerate() {
		return metadata.framerate;
	}

	//

	private final VideoStream videoStream;
	private final AudioStream audioStream;

	public VideoStream videoStream() {
		return videoStream;
	}

	public AudioStream audioStream() {
		return audioStream;
	}

	//

	private long initFrame;
	private long frameInterval;

	public void init() {
		initFrame = System.nanoTime();
		audioIndex = 0;
		videoIndex = 0;
		frameInterval = (long) (1000000000L / metadata.framerate);
	}

	//

	private static final int AUDIO_UNAVAILABLE = -1;
	private static final int AUDIO_TERMINATED = -2;

	private int audioIndex;
	private int videoIndex;

	public int getVideoFrame() {
		return videoIndex;
	}

	public float getPlayingTime() {
		return this.videoIndex / this.framerate();
	}

	public void onMissingAudio() {
		audioIndex = AUDIO_UNAVAILABLE;
	}

	public void onEndOfAudio() {
		audioIndex = AUDIO_TERMINATED;

//		try {
//			this.close();
//		} catch (IOException exc) {
//			exc.printStackTrace();
//		}
	}

	public void onRenderedAudioBuffer() {
		this.audioIndex++;
	}

	public void onUpdatedVideoFrame() {
		this.videoIndex++;
	}

	public boolean hasVideoBacklogOver(int frameCount) {
		return switch(audioIndex) {
			case AUDIO_TERMINATED -> false;
			case AUDIO_UNAVAILABLE -> (videoIndex + frameCount) * frameInterval <= System.nanoTime() - initFrame;
			default -> (videoIndex + frameCount) <= audioIndex;
		};
	}

	public boolean isTimeForNextFrame() {
		return switch(audioIndex) {
			case AUDIO_TERMINATED -> true;
			case AUDIO_UNAVAILABLE -> videoIndex * frameInterval <= System.nanoTime() - initFrame;
			default -> videoIndex <= audioIndex;
		};
	}

	@Override
	public void close() throws IOException {
		try{
			audioStream.close();
			videoStream.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			if(pVideo.process != null){
				System.err.println("[MOVIE] Destroying Video Process");
				
				pVideo.process.destroy();
				
			}
			if(pAudio.process != null){
				System.err.println("[MOVIE] Destroying Audio Process");
				pAudio.process.destroy();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
