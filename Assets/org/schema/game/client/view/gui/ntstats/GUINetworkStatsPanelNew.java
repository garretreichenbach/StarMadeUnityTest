package org.schema.game.client.view.gui.ntstats;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerNetworkStatsInput;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.ntstats.GUINetworkStatsScrollableList.DataDisplayMode;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIStatisticsGraph;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITabbedContent;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState;

public class GUINetworkStatsPanelNew extends GUIInputPanel implements GUIActiveInterface {

	public GUINetworkStatsPanelNew(InputState state, int width, int height,
	                               PlayerNetworkStatsInput guiCallback) {
		super("GUINetworkStatsPanelNew", state, width, height, guiCallback, Lng.str("Network Statistics"), "");
		setOkButton(false);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(25));
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(86));

		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(86));

		GUITabbedContent ps = new GUITabbedContent(getState(), ((GUIDialogWindow) background).getMainContentPane().getContent(2));
		ps.activationInterface = this;
		ps.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(2).attach(ps);

		GUIContentPane receivedTab = ps.addTab(Lng.str("RECEIVED"));
		GUIContentPane sentTab = ps.addTab(Lng.str("SENT"));

		receivedTab.getTextColorSelected().set(new Vector4f(1.0f, 0.4f, 0.4f, 0.7f));
		receivedTab.getTextColorUnselected().set(new Vector4f(0.65f, 0.1f, 0.1f, 0.7f));
		sentTab.getTextColorSelected().set(new Vector4f(0.4f, 1.0f, 0.4f, 0.7f));
		sentTab.getTextColorUnselected().set(new Vector4f(0.1f, 0.5f, 0.1f, 0.7f));

		GUIScrollablePanel sp = new GUIScrollablePanel(100, 100, ((GUIDialogWindow) background).getMainContentPane().getContent(1), getState());
		GUIStatisticsGraph p = new GUIStatisticsGraph(getState(), ps, ((GUIDialogWindow) background).getMainContentPane().getContent(1),
				((ClientState)getState()).getDataStatsManager().getReceivedData(),
				((ClientState)getState()).getDataStatsManager().getSentData()){

			@Override
			public String formatMax(long maxAmplitude, long curAplitude) {
				return "Max: " + StringTools.readableFileSize(maxAmplitude) + "; In: " + StringTools.readableFileSize(curAplitude)+ (ps == null ? " (F12)" : "");
			}
	
};
		p.onInit();
		sp.setContent(p);

		GUIDropDownList l = new GUIDropDownList(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().h, UIScale.getUIScale().scale(300), p);
		addTime(l, "10 secs", 10000);
		addTime(l, "30 secs", 30000);
		addTime(l, "1 min", 1 * 60000);
		addTime(l, "2 min", 2 * 60000);
		addTime(l, "3 min", 3 * 60000);
		addTime(l, "4 min", 4 * 60000);
		l.setSelectedIndex(3);
		l.dependend = ((GUIDialogWindow) background).getMainContentPane().getContent(0);
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(l);

		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(sp);

		GUINetworkStatsScrollableList lRec = new GUINetworkStatsScrollableList(getState(), receivedTab.getContent(0), DataDisplayMode.RECEIVED, ((StateInterface)getState()).getDataStatsManager(), p);
		lRec.onInit();
		receivedTab.getContent(0).attach(lRec);

		GUINetworkStatsScrollableList lSent = new GUINetworkStatsScrollableList(getState(), sentTab.getContent(0), DataDisplayMode.SENT, ((StateInterface)getState()).getDataStatsManager(), p);
		lSent.onInit();
		sentTab.getContent(0).attach(lSent);
	}

	private void addTime(GUIDropDownList l, String name, long time) {
		GUIAnchor c = new GUIAnchor(getState(), 10, 24);
		GUITextOverlayTable t = new GUITextOverlayTable(getState());
		t.setTextSimple(name);
		t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		c.setUserPointer(time);
		c.attach(t);
		l.add(new GUIListElement(c, getState()));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == this) && super.isActive();
	}

}
