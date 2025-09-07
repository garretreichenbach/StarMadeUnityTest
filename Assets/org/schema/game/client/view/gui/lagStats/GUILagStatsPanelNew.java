package org.schema.game.client.view.gui.lagStats;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.PlayerLagStatsInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUICheckBoxTextPair;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIStatisticsGraph;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITabbedContent;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUILagStatsPanelNew extends GUIInputPanel implements GUIActiveInterface {

	private GUILagObjectsScrollableList lRec;

	public GUILagStatsPanelNew(InputState state, int width, int height,
	                               PlayerLagStatsInput guiCallback) {
		super("GUILagStatsPanelNew", state, width, height, guiCallback, "Lag Stats", "");
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

		final GUIContentPane receivedTab = ps.addTab(Lng.str("LAG OBJECTS"));

		
		
		receivedTab.getTextColorSelected().set(new Vector4f(1.0f, 0.4f, 0.4f, 0.7f));
		receivedTab.getTextColorUnselected().set(new Vector4f(0.65f, 0.1f, 0.1f, 0.7f));
		GUIScrollablePanel sp = new GUIScrollablePanel(UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(100), ((GUIDialogWindow) background).getMainContentPane().getContent(1), getState());
		GUIStatisticsGraph p = new GUIStatisticsGraph(getState(), ps, ((GUIDialogWindow) background).getMainContentPane().getContent(1),
			((GameClientState)getState()).lagStats){

				@Override
				public String formatMax(long maxAmplitude, long curAplitude) {
					return "Max: " + maxAmplitude+"ms" + (ps == null ? " (F7)" : "");
				}
			
		};
		p.onInit();
		sp.setContent(p);
		

		GUIDropDownList lm = new GUIDropDownList(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().h, UIScale.getUIScale().scale(300), p);
		addTime(lm, "10 secs", 10000);
		addTime(lm, "30 secs", 30000);
		addTime(lm, "1 min", 1 * 60000);
		addTime(lm, "2 min", 2 * 60000);
		addTime(lm, "3 min", 3 * 60000);
		addTime(lm, "4 min", 4 * 60000);
		lm.setSelectedIndex(3);
		lm.dependend = ((GUIDialogWindow) background).getMainContentPane().getContent(0);
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(lm);

		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(sp);

		GUICheckBoxTextPair l = new GUICheckBoxTextPair(getState(), Lng.str("Always show lag objects"), UIScale.getUIScale().scale(200), UIScale.getUIScale().h) {
			
			@Override
			public boolean isActivated() {
				return EngineSettings.G_DRAW_LAG_OBJECTS_IN_HUD.isOn();
			}
			
			@Override
			public void deactivate() {
				EngineSettings.G_DRAW_LAG_OBJECTS_IN_HUD.setOn(false);
			}
			
			@Override
			public void activate() {
				EngineSettings.G_DRAW_LAG_OBJECTS_IN_HUD.setOn(true);				
			}
		};
//		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(l);

		GUITextButton clearAll = new GUITextButton(getState(), UIScale.getUIScale().scale(95), UIScale.getUIScale().h, Lng.str("Show current"), new GUICallback() {
			
			@Override
			public boolean isOccluded() {
				return !((GameClientState)getState()).lagStats.isAnySelected();
			}
			
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()){
					((GameClientState)getState()).lagStats.deselectAll();
				}
			}
			
		}){

			@Override
			public boolean isActive() {
				return super.isActive() && ((GameClientState)getState()).lagStats.isAnySelected();
			}

			@Override
			public void draw() {
				setPos(UIScale.getUIScale().smallinset, receivedTab.getContent(0).getHeight()+UIScale.getUIScale().inset, 0);
				super.draw();
			}
			
		};
		clearAll.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset,0);
		receivedTab.getContent(0).attach(clearAll);
		lRec = new GUILagObjectsScrollableList((GameClientState) getState(), receivedTab.getContent(0), ((GameClientState)getState()).lagStats);
		lRec.onInit();
		
		((GameClientState)getState()).lagStats.scollableList = lRec;
		receivedTab.getContent(0).attach(lRec);

	}

	private void addTime(GUIDropDownList l, String name, long time) {
		GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(10), UIScale.getUIScale().h);
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

	public void flagChanged() {
		if(lRec != null){
			lRec.flagDirty();
		}
	}

}
