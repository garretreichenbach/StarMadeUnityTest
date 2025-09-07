package org.schema.schine.sound.loaders;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.AssetInfo;
import org.schema.common.util.LittleEndien;
import org.schema.schine.sound.controller.asset.AudioAsset.AudioFileType;
import org.schema.schine.sound.manager.engine.AudioBuffer;
import org.schema.schine.sound.manager.engine.AudioData;
import org.schema.schine.sound.manager.engine.AudioId;
import org.schema.schine.sound.manager.engine.AudioStream;
import org.schema.schine.sound.manager.engine.SeekableStream;

public class WAVLoader implements AudioLoader {

    private static final Logger logger = Logger.getLogger(WAVLoader.class.getName());

    // all these are in big endian
    private static final int i_RIFF = 0x46464952;
    private static final int i_WAVE = 0x45564157;
    private static final int i_fmt  = 0x20746D66;
    private static final int i_data = 0x61746164;

    private boolean readStream = false;

    private AudioBuffer audioBuffer;
    private AudioStream audioStream;
    private AudioData audioData;
    private int bytesPerSec;
    private float duration;

    private ResettableInputStream in;
    private int inOffset = 0;
    
    private static class ResettableInputStream extends LittleEndien implements SeekableStream {
        
        private AssetInfo info;
        private int resetOffset = 0;
        
        public ResettableInputStream(AssetInfo info, InputStream in) {
            super(in);
            this.info = info;
        }
        
        public void setResetOffset(int resetOffset) {
            this.resetOffset = resetOffset;
        }

        @Override
		public void setTime(float time) {
            if (time != 0f) {
                throw new UnsupportedOperationException("Seeking WAV files not supported");
            }
            InputStream newStream = null;
            try {
            	newStream = info.openStream();
                newStream.skip(resetOffset);
                this.in = new BufferedInputStream(newStream);
            } catch (IOException ex) {
                // Resource could have gotten lost, etc.
                try {
                	if(newStream != null) {
                		newStream.close();
                	}
                } catch (IOException ex2) {
                }
                throw new RuntimeException(ex);
            }
        }
    }

    private void readFormatChunk(int size) throws IOException{
        // if other compressions are supported, size doesn't have to be 16
//        if (size != 16)
//            logger.warning("Expected size of format chunk to be 16");

        int compression = in.readShort();
        if (compression != 1){
            throw new IOException("WAV Loader only supports PCM wave files");
        }

        int channels = in.readShort();
        int sampleRate = in.readInt();

        bytesPerSec = in.readInt(); // used to calculate duration

        int bytesPerSample = in.readShort();
        int bitsPerSample = in.readShort();

        int expectedBytesPerSec = (bitsPerSample * channels * sampleRate) / 8;
        if (expectedBytesPerSec != bytesPerSec){
            logger.log(Level.WARNING, "Expected {0} bytes per second, got {1}",
                    new Object[]{expectedBytesPerSec, bytesPerSec});
        }
        
        if (bitsPerSample != 8 && bitsPerSample != 16)
            throw new IOException("Only 8 and 16 bits per sample are supported!");

        if ( (bitsPerSample / 8) * channels != bytesPerSample)
            throw new IOException("Invalid bytes per sample value");

        if (bytesPerSample * sampleRate != bytesPerSec)
            throw new IOException("Invalid bytes per second value");

        audioData.setupFormat(channels, bitsPerSample, sampleRate);

        int remaining = size - 16;
        if (remaining > 0){
            in.skipBytes(remaining);
        }
    }

    private void readDataChunkForBuffer(int len) throws IOException {
        ByteBuffer data = MemoryUtil.memAlloc(len);
        byte[] buf = new byte[512];
        int read = 0;
        while ( (read = in.read(buf)) > 0){
            data.put(buf, 0, Math.min(read, data.remaining()) );
        }
        data.flip();
        audioBuffer.updateData(data);
        in.close();
    }

    private void readDataChunkForStream(int offset, int len) throws IOException {
        in.setResetOffset(offset);
        audioStream.updateData(in, duration);
    }

    private AudioData load(AssetInfo info, InputStream inputStream, boolean stream) throws IOException{
        this.in = new ResettableInputStream(info, inputStream);
        inOffset = 0;
        
        int sig = in.readInt();
        if (sig != i_RIFF)
            throw new IOException("File is not a WAVE file");
        
        // skip size
        in.readInt();
        if (in.readInt() != i_WAVE)
            throw new IOException("WAVE File does not contain audio");

        inOffset += 4 * 3;
        
        readStream = stream;
        if (readStream){
            audioStream = new AudioStream();
            audioData = audioStream;
        }else{
            audioBuffer = new AudioBuffer();
            audioData = audioBuffer;
        }

        while (true) {
            int type = in.readInt();
            int len = in.readInt();
            
            inOffset += 4 * 2;
	        switch(type) {
		        case i_fmt -> {
			        readFormatChunk(len);
			        inOffset += len;
		        }
		        case i_data -> {
			        // Compute duration based on data chunk size
			        duration = len / bytesPerSec;
			        if(readStream) {
				        readDataChunkForStream(inOffset, len);
			        } else {
				        readDataChunkForBuffer(len);
			        }
			        return audioData;
		        }
		        default -> {
			        int skipped = in.skipBytes(len);
			        if(skipped <= 0) {
				        return null;
			        }
			        inOffset += skipped;
		        }
	        }
        }
    }
    
    public AudioData load(AudioLoadEntry info) throws IOException {
        AudioData data;
        InputStream inputStream = null;
        try {
            inputStream = info.openStream();
            data = load(info, inputStream, ((AudioId)info.getId()).isStream());
            if (data instanceof AudioStream){
                inputStream = null;
            }
            return data;
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
        }
    }
    @Override
	public AudioFileType getFileType() {
		return AudioFileType.WAV;
	}
}
