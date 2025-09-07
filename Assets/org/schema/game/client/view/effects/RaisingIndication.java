package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import com.bulletphysics.linearmath.Transform;

public class RaisingIndication extends Indication {
	public static Vector3f tmp = new Vector3f();
	public Transform current = new Transform();
	public float speed = 1;

	public RaisingIndication(Transform start, Object text) {
		super(start, text);
		current.set(start);
	}

	public RaisingIndication(Transform t, Object text, float r, float g, float b,
	                         float a) {
		this(t, text);
		setColor(new Vector4f(r, g, b, a));
		
		this.lifetime = EngineSettings.HIT_INDICATION_NUMBERS_LIFETIME.getFloat();
	}

	@Override
	public Transform getCurrentTransform() {
		return current;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.effects.Indication#scale()
	 */
	@Override
	public float scaleIndication() {
		return EngineSettings.G_HIT_INDICATION_SIZE.getFloat();
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		tmp.set(Controller.getCamera().getUp());
		tmp.scale(timer.getDelta() * speed);
		current.origin.add(tmp);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.effects.Indication#getDist()
	 */
	@Override
	public float getDist() {
		return (EngineSettings.G_DAMAGE_DISPLAY.getInt());
	}

}
