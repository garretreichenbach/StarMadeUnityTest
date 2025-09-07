/*
 * Copyright (c) 2009-2012, 2016 jMonkeyEngine
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

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.sound.manager.engine.AudioData.DataType;

import javax.vecmath.Vector3f;

/**
 * An <code>AudioNode</code> is a scene Node which can play audio assets.
 *
 * An AudioNode is either positional or ambient, with positional being the
 * default. Once a positional node is attached to the scene, its location and
 * velocity relative to the {@link AudioListener} affect how it sounds when played.
 * Positional nodes can only play monoaural (single-channel) assets, not stereo
 * ones.
 *
 * An ambient AudioNode plays in "headspace", meaning that the node's location
 * and velocity do not affect how it sounds when played. Ambient audio nodes can
 * play stereo assets.
 *
 * The "positional" property of an AudioNode can be set via
 * {@link AudioNode#setPositional(boolean) }.
 *
 * @author normenhansen
 * @author Kirill Vainer
 */
public abstract class AudioNode implements AudioSource, Transformable {

    //Version #1 : AudioId is now stored into "audio_key" instead of "key"
    public static final int SAVABLE_VERSION = 1;
    protected boolean loop = false;
    protected float volume = 1;
    protected float pitch = 1;
    protected float timeOffset = 0;
    protected Filter dryFilter;
    protected AudioId audioKey;
    protected transient AudioData data = null;
    protected transient volatile AudioSource.Status status = AudioSource.Status.STOPPED;
    protected transient volatile int channel = -1;
    protected Vector3f previousWorldTranslation = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
    protected Vector3f velocity = new Vector3f();
    protected boolean reverbEnabled = false;
    protected float maxDistance = 200; // 200 meters
    protected float refDistance = 10; // 10 meters
    protected Filter reverbFilter;
    private boolean directional = false;
    protected Vector3f direction = new Vector3f(0, 0, 1);
    protected float innerAngle = 360;
    protected float outerAngle = 360;
    protected boolean positional = true;
    protected boolean velocityFromTranslation = false;
    protected float lastTpf;
	private boolean stopRequested;
	/**
     * Creates a new <code>AudioNode</code> without any audio data set.
     */
    public AudioNode() {
    }
    public void queueStop() {
    	this.stopRequested = true;
	}
    /**
     * Creates a new <code>AudioNode</code> with the given data and key.
     *
     * @param audioData The audio data contains the audio track to play.
     * @param audioKey The audio key that was used to load the AudioData
     */
    public AudioNode(AudioData audioData, AudioId audioKey) {
        setAudioData(audioData, audioKey);
    }

    public abstract boolean isPlayInstanced(); 
    protected AudioRenderer getRenderer() {
        AudioRenderer result = AudioContext.getAudioRenderer();
        if( result == null )
            throw new IllegalStateException( "No audio renderer available, make sure call is being performed on render thread." );
        return result;
    }

    
    public boolean canPlay() {
    	return !positional || data.getChannels() < 2;
    }
    /**
     * Start playing the audio.
     */
    public void play(){
       // if (positional && data.getChannels() > 1) {
        //    //throw new IllegalStateException("Only mono audio is supported for positional audio nodes");
        //}
        getRenderer().playSource(this);
    }

    /**
     * Start playing an instance of this audio. This method can be used
     * to play the same <code>AudioNode</code> multiple times. Note
     * that changes to the parameters of this AudioNode will not effect the
     * instances already playing.
     */
    public void playInstance(){
        if (positional && data.getChannels() > 1) {
            throw new IllegalStateException("Only mono audio is supported for positional audio nodes");
        }
        getRenderer().playSourceInstance(this);
    }

    /**
     * Stop playing the audio that was started with {@link AudioNode#play() }.
     */
    public void stop(){
        getRenderer().stopSource(this);
    }

    /**
     * Pause the audio that was started with {@link AudioNode#play() }.
     */
    public void pause(){
        getRenderer().pauseSource(this);
    }

    /**
     * Do not use.
     */
    @Override
	public final void setChannel(int channel) {
        if (status != AudioSource.Status.STOPPED) {
            throw new IllegalStateException("Can only set source id when stopped");
        }

        this.channel = channel;
    }

    /**
     * Do not use.
     */
    @Override
	public int getChannel() {
        return channel;
    }

    /**
     * @return The {#link Filter dry filter} that is set.
     * @see AudioNode#setDryFilter(com.jme3.audio.Filter)
     */
    @Override
	public Filter getDryFilter() {
        return dryFilter;
    }

    /**
     * Set the dry filter to use for this audio node.
     *
     * When {@link AudioNode#setReverbEnabled(boolean) reverb} is used,
     * the dry filter will only influence the "dry" portion of the audio,
     * e.g. not the reverberated parts of the AudioNode playing.
     *
     * See the relevent documentation for the {@link Filter} to determine
     * the effect.
     *
     * @param dryFilter The filter to set, or null to disable dry filter.
     */
    public void setDryFilter(Filter dryFilter) {
        this.dryFilter = dryFilter;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.DryFilter);
    }

    /**
     * Set the audio data to use for the audio. Note that this method
     * can only be called once, if for example the audio node was initialized
     * without an {@link AudioData}.
     *
     * @param audioData The audio data contains the audio track to play.
     * @param audioKey The audio key that was used to load the AudioData
     */
    public void setAudioData(AudioData audioData, AudioId audioKey) {
        if (data != null) {
            throw new IllegalStateException("Cannot change data once its set");
        }

        data = audioData;
        this.audioKey = audioKey;
    }

    /**
     * @return The {@link AudioData} set previously with
     * {@link AudioNode#setAudioData(com.jme3.audio.AudioData, com.jme3.audio.AudioId) }
     * or any of the constructors that initialize the audio data.
     */
    @Override
	public AudioData getAudioData() {
        return data;
    }

    /**
     * @return The {@link Status} of the audio node.
     * The status will be changed when either the {@link AudioNode#play() }
     * or {@link AudioNode#stop() } methods are called.
     */
    @Override
	public AudioSource.Status getStatus() {
        return status;
    }

    /**
     * Do not use.
     */
    @Override
	public final void setStatus(AudioSource.Status status) {
        this.status = status;
    }

    /**
     * Get the Type of the underlying AudioData to see if it's streamed or buffered.
     * This is a shortcut to getAudioData().getType()
     * <b>Warning</b>: Can return null!
     * @return The {@link com.jme3.audio.AudioData.DataType} of the audio node.
     */
    public DataType getType() {
        if (data == null)
            return null;
        else
            return data.getDataType();
    }

    /**
     * @return True if the audio will keep looping after it is done playing,
     * otherwise, false.
     * @see AudioNode#setLooping(boolean)
     */
    @Override
	public boolean isLooping() {
        return loop;
    }

    /**
     * Set the looping mode for the audio node. The default is false.
     *
     * @param loop True if the audio should keep looping after it is done playing.
     */
    public void setLooping(boolean loop) {
        this.loop = loop;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.Looping);
    }

    /**
     * @return The pitch of the audio, also the speed of playback.
     *
     * @see AudioNode#setPitch(float)
     */
    @Override
	public float getPitch() {
        return pitch;
    }

    /**
     * Set the pitch of the audio, also the speed of playback.
     * The value must be between 0.5 and 2.0.
     *
     * @param pitch The pitch to set.
     * @throws IllegalArgumentException If pitch is not between 0.5 and 2.0.
     */
    public void setPitch(float pitch) {
        if (pitch < 0.5f || pitch > 2.0f) {
            throw new IllegalArgumentException("Pitch must be between 0.5 and 2.0");
        }

        this.pitch = pitch;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.Pitch);
    }

    /**
     * @return The volume of this audio node.
     *
     * @see AudioNode#setVolume(float)
     */
    @Override
	public float getVolume() {
        return volume;
    }

    /**
     * Set the volume of this audio node.
     *
     * The volume is specified as gain. 1.0 is the default.
     *
     * @param volume The volume to set.
     * @throws IllegalArgumentException If volume is negative
     */
    public void setVolume(float volume) {
        if (volume < 0f) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }

        this.volume = volume;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.Volume);
    }

    /**
     * @return the time offset in the sound sample when to start playing.
     */
    @Override
	public float getTimeOffset() {
        return timeOffset;
    }

    /**
     * Set the time offset in the sound sample when to start playing.
     *
     * @param timeOffset The time offset
     * @throws IllegalArgumentException If timeOffset is negative
     */
    public void setTimeOffset(float timeOffset) {
        if (timeOffset < 0f) {
            throw new IllegalArgumentException("Time offset cannot be negative");
        }

        this.timeOffset = timeOffset;
        if (data instanceof AudioStream) {
            ((AudioStream) data).setTime(timeOffset);
        }else if(status == AudioSource.Status.PLAYING){
            stop();
            play();
        }
    }

    @Override
    public float getPlaybackTime() {
        if (channel >= 0)
            return getRenderer().getSourcePlaybackTime(this);
        else
            return 0;
    }

    /**
     * @return The velocity of the audio node.
     *
     * @see AudioNode#setVelocity(com.jme3.math.Vector3f)
     */
    @Override
	public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * Set the velocity of the audio node. The velocity is expected
     * to be in meters. Does nothing if the audio node is not positional.
     *
     * @param velocity The velocity to set.
     * @see AudioNode#setPositional(boolean)
     */
    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.Velocity);
    }

    /**
     * @return True if reverb is enabled, otherwise false.
     *
     * @see AudioNode#setReverbEnabled(boolean)
     */
    @Override
	public boolean isReverbEnabled() {
        return reverbEnabled;
    }

    /**
     * Set to true to enable reverberation effects for this audio node.
     * Does nothing if the audio node is not positional.
     * <br/>
     * When enabled, the audio environment set with
     * {@link AudioRenderer#setEnvironment(com.jme3.audio.Environment) }
     * will apply a reverb effect to the audio playing from this audio node.
     *
     * @param reverbEnabled True to enable reverb.
     */
    public void setReverbEnabled(boolean reverbEnabled) {
        this.reverbEnabled = reverbEnabled;
        if (channel >= 0) {
            getRenderer().updateSourceParam(this, AudioNodeParam.ReverbEnabled);
        }
    }

    /**
     * @return Filter for the reverberations of this audio node.
     *
     * @see AudioNode#setReverbFilter(com.jme3.audio.Filter)
     */
    @Override
	public Filter getReverbFilter() {
        return reverbFilter;
    }

    /**
     * Set the reverb filter for this audio node.
     * <br/>
     * The reverb filter will influence the reverberations
     * of the audio node playing. This only has an effect if
     * reverb is enabled.
     *
     * @param reverbFilter The reverb filter to set.
     * @see AudioNode#setDryFilter(com.jme3.audio.Filter)
     */
    public void setReverbFilter(Filter reverbFilter) {
        this.reverbFilter = reverbFilter;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.ReverbFilter);
    }

    /**
     * @return Max distance for this audio node.
     *
     * @see AudioNode#setMaxDistance(float)
     */
    @Override
	public float getMaxDistance() {
        return maxDistance;
    }

    /**
     * Set the maximum distance for the attenuation of the audio node.
     * Does nothing if the audio node is not positional.
     * <br/>
     * The maximum distance is the distance beyond which the audio
     * node will no longer be attenuated.  Normal attenuation is logarithmic
     * from refDistance (it reduces by half when the distance doubles).
     * Max distance sets where this fall-off stops and the sound will never
     * get any quieter than at that distance.  If you want a sound to fall-off
     * very quickly then set ref distance very short and leave this distance
     * very long.
     *
     * @param maxDistance The maximum playing distance.
     * @throws IllegalArgumentException If maxDistance is negative
     */
    public void setMaxDistance(float maxDistance) {
        if (maxDistance < 0) {
            throw new IllegalArgumentException("Max distance cannot be negative");
        }

        this.maxDistance = maxDistance;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.MaxDistance);
    }

    /**
     * @return The reference playing distance for the audio node.
     *
     * @see AudioNode#setRefDistance(float)
     */
    @Override
	public float getRefDistance() {
        return refDistance;
    }

    /**
     * Set the reference playing distance for the audio node.
     * Does nothing if the audio node is not positional.
     * <br/>
     * The reference playing distance is the distance at which the
     * audio node will be exactly half of its volume.
     *
     * @param refDistance The reference playing distance.
     * @throws  IllegalArgumentException If refDistance is negative
     */
    public void setRefDistance(float refDistance) {
        if (refDistance < 0) {
            throw new IllegalArgumentException("Reference distance cannot be negative");
        }

        this.refDistance = refDistance;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.RefDistance);
    }

    /**
     * @return True if the audio node is directional
     *
     * @see AudioNode#setDirectional(boolean)
     */
    @Override
	public boolean isDirectional() {
        return directional;
    }

    /**
     * Set the audio node to be directional.
     * Does nothing if the audio node is not positional.
     * <br/>
     * After setting directional, you should call
     * {@link AudioNode#setDirection(com.jme3.math.Vector3f) }
     * to set the audio node's direction.
     *
     * @param directional If the audio node is directional
     */
    public void setDirectional(boolean directional) {
        this.directional = directional;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.IsDirectional);
    }

    /**
     * @return The direction of this audio node.
     *
     * @see AudioNode#setDirection(com.jme3.math.Vector3f)
     */
    @Override
	public Vector3f getDirection() {
        return direction;
    }

    /**
     * Set the direction of this audio node.
     * Does nothing if the audio node is not directional.
     *
     * @param direction
     * @see AudioNode#setDirectional(boolean)
     */
    public void setDirection(Vector3f direction) {
        this.direction = direction;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.Direction);
    }

    /**
     * @return The directional audio node, cone inner angle.
     *
     * @see AudioNode#setInnerAngle(float)
     */
    @Override
	public float getInnerAngle() {
        return innerAngle;
    }

    /**
     * Set the directional audio node cone inner angle.
     * Does nothing if the audio node is not directional.
     *
     * @param innerAngle The cone inner angle.
     */
    public void setInnerAngle(float innerAngle) {
        this.innerAngle = innerAngle;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.InnerAngle);
    }

    /**
     * @return The directional audio node, cone outer angle.
     *
     * @see AudioNode#setOuterAngle(float)
     */
    @Override
	public float getOuterAngle() {
        return outerAngle;
    }

    /**
     * Set the directional audio node cone outer angle.
     * Does nothing if the audio node is not directional.
     *
     * @param outerAngle The cone outer angle.
     */
    public void setOuterAngle(float outerAngle) {
        this.outerAngle = outerAngle;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioNodeParam.OuterAngle);
    }

    /**
     * @return True if the audio node is positional.
     *
     * @see AudioNode#setPositional(boolean)
     */
    @Override
	public boolean isPositional() {
        return positional;
    }
    public void updateGeometricState(float delta) {

        if (channel < 0) {
            return;
        }

        Vector3f currentWorldTranslation = getWorldTransform().origin;

        if (Float.isNaN(previousWorldTranslation.x)
                || !previousWorldTranslation.equals(currentWorldTranslation)) {

            getRenderer().updateSourceParam(this, AudioNodeParam.Position);

            if (velocityFromTranslation) {
            	velocity.set(currentWorldTranslation);
            	velocity.sub(previousWorldTranslation);
                velocity.scale(1f / delta);

                getRenderer().updateSourceParam(this, AudioNodeParam.Velocity);
            }
            previousWorldTranslation.set(currentWorldTranslation);
        }

        //Todo: The below is (probably?) a band-aid fix, as I think the audio library is supposed to do this for us, but it doesn't seem to be working.
        try {
            if(GameClient.getClientState() != null && GameClient.getClientPlayerState() != null) {
                PlayerState player = GameClient.getClientPlayerState();
                if(player.getAssingedPlayerCharacter() != null) {
                    Vector3f playerPos = player.getAssingedPlayerCharacter().getWorldTransformOnClient().origin;
                    if (player.getFirstControlledTransformable() instanceof SegmentController)
                        playerPos = player.getBuildModePosition().getWorldTransform().origin;
                    float distance = Math.abs(Vector3fTools.distance(playerPos, currentWorldTranslation));
                    //get quieter the further away
                    if (distance > maxDistance) {
                        setVolume(0);
                        return;
                    }
                    float volume = 1.0f - (distance / maxDistance);
                    if (loop) volume /= 2; //Make looping sounds quieter so they don't get annoying
                    if (volume < 0) volume = 0;
                    if (volume > 1) volume = 1;
                    setVolume(volume);
                } //else we're still spawning in; probably shouldn't play positional sounds anyway
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }
    /**
     * Set the audio node as positional.
     * The position, velocity, and distance parameters effect positional
     * audio nodes. Set to false if the audio node should play in "headspace".
     *
     * @param positional True if the audio node should be positional, otherwise
     * false if it should be headspace.
     */
    public void setPositional(boolean positional) {
        this.positional = positional;
        if (channel >= 0) {
            getRenderer().updateSourceParam(this, AudioNodeParam.IsPositional);
        }
    }

    public boolean isVelocityFromTranslation() {
        return velocityFromTranslation;
    }

    public void setVelocityFromTranslation(boolean velocityFromTranslation) {
        this.velocityFromTranslation = velocityFromTranslation;
    }

//    @Override
//    public void updateLogicalState(float tpf) {
//        super.updateLogicalState(tpf);
//        lastTpf = tpf;
//    }
//
//    @Override
//    public void updateGeometricState() {
//        super.updateGeometricState();
//
//        if (channel < 0) {
//            return;
//        }
//
//        Vector3f currentWorldTranslation = worldTransform.getTranslation();
//
//        if (Float.isNaN(previousWorldTranslation.x)
//                || !previousWorldTranslation.equals(currentWorldTranslation)) {
//
//            getRenderer().updateSourceParam(this, AudioParam.Position);
//
//            if (velocityFromTranslation) {
//                velocity.set(currentWorldTranslation).subtractLocal(previousWorldTranslation);
//                velocity.multLocal(1f / lastTpf);
//
//                getRenderer().updateSourceParam(this, AudioParam.Velocity);
//            }
//
//            previousWorldTranslation.set(currentWorldTranslation);
//        }
//    }


   

  

    @Override
    public String toString() {
        String ret = getClass().getSimpleName()
                + "[status=" + status;
        if (volume != 1f) {
            ret += ", vol=" + volume;
        }
        if (pitch != 1f) {
            ret += ", pitch=" + pitch;
        }
        return ret + "]";
    }
	public void update(Timer timer) {
		if(positional) {
			updateGeometricState(timer.getDelta());
		}
		if(stopRequested) {
			stopRequested = false;
			stop();
		}
	}
	public void onFinishedAfterStopped() {
	}

	
}
