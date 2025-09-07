/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.schema.schine.sound.manager.engine;

import org.schema.common.util.AssetId;

/**
 * <code>AudioKey</code> is extending AssetKey by holding stream flag.
 *
 * @author Kirill Vainer
 */
public class AudioId extends AssetId<AudioId>{

    private boolean stream;
    private boolean streamCache;
    private String name;
    /**
     * Create a new AudioKey.
     * 
     * @param name Name of the asset
     * @param stream If true, the audio will be streamed from harddrive,
     * otherwise it will be buffered entirely and then played.
     * @param streamCache If stream is true, then this specifies if
     * the stream cache is used. When enabled, the audio stream will
     * be read entirely but not decoded, allowing features such as 
     * seeking, determining duration and looping.
     */
    public AudioId(String name, boolean stream, boolean streamCache){
        this(name, stream);
        this.streamCache = streamCache;
    }
    
    /**
     * Create a new AudioKey
     *
     * @param name Name of the asset
     * @param stream If true, the audio will be streamed from harddrive,
     * otherwise it will be buffered entirely and then played.
     */
    public AudioId(String name, boolean stream){
       	this.name = name;
        this.stream = stream;
    }

    public AudioId(String name){
    	this(name, false);
    }

    public AudioId(){
    }

    @Override
    public String toString(){
        return name + (stream ?
                          (streamCache ? 
                            " (Stream/Cache)" : 
                            " (Stream)") : 
                         " (Buffer)");
    }

    /**
     * @return True if the loaded audio should be a {@link AudioStream} or
     * false if it should be a {@link AudioBuffer}.
     */
    public boolean isStream() {
        return stream;
    }
    
    /**
     * Specifies if the stream cache is used. 
     * 
     * When enabled, the audio stream will
     * be read entirely but not decoded, allowing features such as 
     * seeking, looping and determining duration.
     */
    public boolean isUseStreamCache(){
        return streamCache;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AudioId other = (AudioId) obj;
       
        return (name.equals(other.name) && this.stream == other.stream && this.streamCache == other.streamCache);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (super.hashCode());
        hash = 67 * hash + (this.stream ? 1 : 0);
        hash = 67 * hash + (this.streamCache ? 1 : 0);
        return hash;
    }

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public void setStreamCache(boolean streamCache) {
		this.streamCache = streamCache;
	}
    

}
