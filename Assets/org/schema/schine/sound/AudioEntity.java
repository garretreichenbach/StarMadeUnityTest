package org.schema.schine.sound;

import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.UniqueInterface;

import com.bulletphysics.linearmath.Transform;

public interface AudioEntity extends UniqueInterface, Transformable, Physical {

	public String getInsideSound();

	public float getInsideSoundPitch();

	public float getInsideSoundVolume();

	public String getOutsideSound();

	public float getOutsideSoundPitch();

	public float getOutsideSoundVolume();
	
	public float getSoundRadius();

	public Transform getWorldTransformOnClient();

	public boolean isOwnPlayerInside();
}
