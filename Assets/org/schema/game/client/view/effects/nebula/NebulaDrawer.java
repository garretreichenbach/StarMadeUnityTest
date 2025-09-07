package org.schema.game.client.view.effects.nebula;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class NebulaDrawer implements Drawable {

	private float time;
	private final ObjectArrayList<NebulaDrawable> nebulae = new ObjectArrayList<>();

	@Override
	public void onInit() {
		nebulae.add(new NebulaDrawable(GameResourceLoader.nebulaTexture, new Vector3f[] {
				new Vector3f(0.0f, 0.8f, 0.1f),
				new Vector3f(0.0f, 0.5f, 0.5f),
				new Vector3f(0.0f, 0.1f, 0.8f),
		}, new Vector3f()));
	}

	@Override
	public void draw() {
		for(NebulaDrawable nebula : nebulae) nebula.draw();
	}

	@Override
	public void cleanUp() {
		for(NebulaDrawable nebula : nebulae) nebula.cleanUp();
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	public void update(Timer timer) {
		time += timer.getDelta();// * 10.1f;
	}
}
