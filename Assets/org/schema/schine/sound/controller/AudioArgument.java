package org.schema.schine.sound.controller;

import java.io.DataInput;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.SerializationInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.sound.controller.gui.AudioEventDetailPanel;
import org.schema.schine.sound.manager.engine.Filter;

public interface AudioArgument extends SerializationInterface{

	
	public interface AudioArgumentType{
		public byte getTypeByte();
		public AudioArgument getInstance();
	}
	public static AudioArgument getInstance(byte b) {
		return AudioArgumentTypes.values()[b].getInstance();
	}
	public interface AudioArgumentFactory{
		public AudioArgument getInstance();
	}
	public enum AudioArgumentTypes implements AudioArgumentType{
		ENTITY(() -> new AudioArgumentEntity()),;

		private final AudioArgumentFactory fac;
		private AudioArgumentTypes(AudioArgumentFactory fac){
			this.fac = fac;
		}
		@Override
		public byte getTypeByte() {
			return (byte)ordinal();
		}
		@Override
		public AudioArgument getInstance() {
			return fac.getInstance();
		}
		
	}
	
	public static AudioArgument deserializeStatic(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		AudioArgument instance = getInstance(b.readByte());
		instance.deserialize(b, updateSenderStateId, isOnServer);
		return instance;
	}
	public AudioArgumentType getType() ;
		
	public void fillLabels(AudioEventDetailPanel audioEventDetailPanel);

	public boolean isPositional();

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
	public float getRefDistance();

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
	public float getMaxDistance();

	public boolean isReverbEnabled();

	public Filter getReverbFilter();


	public Vector3f getPos();

	public TransformTimed getWorldTransform();

	public int getPrimaryId();

	public long getSubId();

	public void update(Timer timer);

	public void init();

}
