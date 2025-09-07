package org.schema.game.client.view.gui.faction;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.input.InputState;

public abstract class FactionInvitationsAbstractScrollList extends GUIScrollablePanel implements GUIChangeListener {

	private boolean needsUpdate = true;
	private GUIElementList list;

	public FactionInvitationsAbstractScrollList(float width, float height,
			InputState state) {
		super(width, height, state);
		((GameClientState) getState()).getPlayer().getFactionController().deleteObserver(this);
		((GameClientState) getState()).getPlayer().getFactionController().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel#draw()
	 */
	@Override
	public void draw() {
		if (needsUpdate) {
			updateInvitationList(list);
			needsUpdate = false;
		}
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel#onInit()
	 */
	@Override
	public void onInit() {
		list = new GUIElementList(getState());
		setContent(list);
		super.onInit();

	}

	@Override
	public void onChange(boolean updateListDim) {
		needsUpdate = true;		
	}

	protected abstract void updateInvitationList(GUIElementList list);

}
