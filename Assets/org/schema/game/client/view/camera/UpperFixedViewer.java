package org.schema.game.client.view.camera;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;

public class UpperFixedViewer extends FixedViewer {

	public UpperFixedViewer(Transformable entity) {
		super(entity);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.viewer.FixedViewer#getPos()
	 */
	@Override
	public Vector3f getPos() {
		Vector3f pos = super.getPos();
		Vector3f upVector = GlUtil.getUpVector(new Vector3f(), transform);
		upVector.scale(PlayerCharacter.headUpScale);
		pos.add(upVector);
		return pos;
	}

}
