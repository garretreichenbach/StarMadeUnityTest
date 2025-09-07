package org.schema.schine.sound.manager.engine;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4fTools;

public class AudioListenerDefault implements AudioListener{
	 private final Vector3f location;
	    private final Vector3f velocity;
	    private final Quat4f rotation;
	    private float volume = 1;
	    private AudioRenderer renderer;

	    public AudioListenerDefault(){
	        location = new Vector3f();
	        velocity = new Vector3f();
	        rotation = new Quat4f();
	    }
	    
	    public AudioListenerDefault(AudioListenerDefault source){
	        location = new Vector3f(source.location);
	        velocity = new Vector3f(source.velocity);
	        rotation = new Quat4f(source.rotation);
	        volume = source.volume;
	    }

	    public void setRenderer(AudioRenderer renderer){
	        this.renderer = renderer;
	    }

	    public float getVolume() {
	        return volume;
	    }

	    public void setVolume(float volume) {
	        this.volume = volume;
	        if (renderer != null)
	            renderer.updateListenerParam(this, ListenerParam.VOL);
	    }
	    
	    public Vector3f getLocation() {
	        return location;
	    }

	    public Quat4f getRotation() {
	        return rotation;
	    }

	    public Vector3f getVelocity() {
	        return velocity;
	    }

	    public Vector3f getLeft(){
	        return Quat4fTools.getRotationColumn(rotation, 0);
	    }

	    public Vector3f getUp(){
	        return Quat4fTools.getRotationColumn(rotation, 1);
	    }

	    public Vector3f getDirection(){
	        return Quat4fTools.getRotationColumn(rotation, 2);
	    }
	    
	    public void setLocation(Vector3f location) {
	        this.location.set(location);
	        if (renderer != null)
	            renderer.updateListenerParam(this, ListenerParam.POS);
	    }

	    public void setRotation(Quat4f rotation) {
	        this.rotation.set(rotation);
	        if (renderer != null)
	            renderer.updateListenerParam(this, ListenerParam.ROT);
	    }

	    public void setVelocity(Vector3f velocity) {
	        this.velocity.set(velocity);
	        if (renderer != null)
	            renderer.updateListenerParam(this, ListenerParam.VEL);
	    }
}
