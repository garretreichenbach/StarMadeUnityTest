package org.schema.game.client.view.gui.ai;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.controller.ai.UnloadedAiEntityException;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class AiInterfaceEnterableTop extends GUIColoredRectangle {

	private AiInterfaceContainer permission;
	private String prefix;
	private boolean init;

	public AiInterfaceEnterableTop(InputState state, AiInterfaceContainer permission, String prefix, int index) {
		super(state, 510, 30, new Vector4f());
		this.permission = permission;
		this.prefix = prefix;
		setIndex(index);

		//		System.err.println("CREATING "+prefix+": "+permission.catUID);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		if (!init) {
			super.onInit();
			GUITextOverlay prefixText = new GUITextOverlay(getState());
			prefixText.setTextSimple(prefix);

			GUITextOverlay name = new GUITextOverlay(getState());
			name.setTextSimple(new Object() {

				/* (non-Javadoc)
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					try {
						return "Name: " + permission.getRealName();
					} catch (UnloadedAiEntityException e) {
						return "Name: " + permission.getUID() + "(UNLOADED)";
					}
				}

			});

			GameClientState state = (GameClientState) getState();

			name.getPos().x += 7;

			int y = 5;

			prefixText.getPos().y = y;
			name.getPos().y = y;

			attach(prefixText);
			attach(name);
			init = true;
		}

	}

	public void setIndex(int index) {
		setColor(index % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f));
	}

}
