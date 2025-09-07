package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.network.client.ClientState;

public abstract class FactionInvitationsAbstractScrollListNew extends GUIScrollablePanel implements GUIChangeListener {

	private boolean needsUpdate = true;
	private GUIElementList list;
	private GUIElement p;

	public FactionInvitationsAbstractScrollListNew(
			ClientState state, GUIElement p) {
		super(10, 10, p, state);
		this.p = p;
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
		setWidth(p.getWidth());
		setHeight(p.getHeight());
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
