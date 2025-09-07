package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

public class Activation implements Comparable<Activation> {

	private final ThrustPlume thrustPlume;
	long timeStarted = -1;
	float ticks = 0;
	private ElementCollection o;
	float power;
	public Vector4f color;

	public Activation(Ship ship, ElementCollection<?, ?, ?> o2, float power, Vector4f color) {
		thrustPlume = new ThrustPlume();

		set(ship, o2, power, color);
	}

	@Override
	public int compareTo(Activation o) {
		return thrustPlume.compareTo(o.thrustPlume);
	}

	public void getWorldTransform(Transform t, Vector3f localTranslation) {
		thrustPlume.getWorldTransform(t, localTranslation);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return o.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return o.equals(((Activation) obj).o);
	}

	public boolean isActive() {
		if(timeStarted < 0 || (System.currentTimeMillis() - timeStarted) > MuzzleFlash.timeDrawn) {
			return false;
		}
		return true;
	}

	public void reset() {
		o = null;
		thrustPlume.reset();
	}

	public void set(Ship ship, ElementCollection<?, ?, ?> o, float power, Vector4f color) {
		this.o = o;
		if(o instanceof CustomOutputUnit<?, ?, ?>) {
			thrustPlume.set(ship, ((CustomOutputUnit<?, ?, ?>) o).getOutput(), ship.getSegmentBuffer().getPointUnsave(((CustomOutputUnit<?, ?, ?>) o).getOutput()).getOrientation());
		} else {
			thrustPlume.set(ship, o.getSignificator(new Vector3i()), Element.BACK);
		}
		this.color = color;
		this.power = power;
		ticks = 0;
		timeStarted = -1;
	}

	public void start() {
		long running = System.currentTimeMillis() - timeStarted;
		if(running > MuzzleFlash.timeDrawn * 0.8f && running < MuzzleFlash.timeDrawn) {
			timeStarted = System.currentTimeMillis() + (long) (MuzzleFlash.timeDrawn * 0.8f);
		} else if(running >= MuzzleFlash.timeDrawn) {
			timeStarted = System.currentTimeMillis();
		}
	}

	public void update(Timer timer) {
		ticks += (float) (timer.getDelta() / 1000 * ((Math.random() + 0.0001f) / 0.1f));
		if(ticks > 1) {
			ticks = 0;
		}
	}
}
