package org.schema.game.client.view.gui.reactor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIReactorPowerConsumerList extends ScrollableTableList<PowerConsumer> implements ReactorTreeListener {

	private ManagerContainer<?> c;
	public GUIReactorPowerConsumerList(InputState state, ManagerContainer<?> c, GUIElement p) {
		super(state, 100, 100, p);
		this.c = c;
		c.getPowerInterface().addObserver(this);
	}
	@Override
	public void onTreeChanged(ReactorSet t) {
		onChange(false);
	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		c.getPowerInterface().deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {


		addFixedWidthColumnScaledUI(Lng.str("Index"), 45, (o1, o2) -> {
			int p1 = c.getPowerInterface().getPowerConsumerList().indexOf(o1);
			int p2 = c.getPowerInterface().getPowerConsumerList().indexOf(o2);
			return p1 - p2;
		}, true);
		addFixedWidthColumnScaledUI(Lng.str("Name"), 350, (o1, o2) -> o1.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getName().toLowerCase(Locale.ENGLISH)));

		
		
		addFixedWidthColumnScaledUI(Lng.str("Active"), 68, (o1, o2) -> CompareTools.compare(
				o1.isPowerConsumerActive(),
				o2.isPowerConsumerActive()));
		addFixedWidthColumnScaledUI(Lng.str("Powered"), 66, (o1, o2) -> CompareTools.compare(
				o1.getPowered(),
				o2.getPowered()));
		addColumn(Lng.str("Charging"), 1, (o1, o2) -> CompareTools.compare(
				o1.getPowerConsumedPerSecondCharging(),
				o2.getPowerConsumedPerSecondCharging()));
		addColumn(Lng.str("Resting"), 1, (o1, o2) -> CompareTools.compare(
				o1.getPowerConsumedPerSecondResting(),
				o2.getPowerConsumedPerSecondResting()));
		addFixedWidthColumnScaledUI(Lng.str("Mode"), 80, (o1, o2) -> {
			long currentTimeMillis = System.currentTimeMillis();
			return CompareTools.compare(
					o1.isPowerCharging(currentTimeMillis),
					o2.isPowerCharging(currentTimeMillis));
		});
		addColumn(Lng.str("Current"), 1, (o1, o2) -> {
			long currentTimeMillis = System.currentTimeMillis();
			return CompareTools.compare(
					!o1.isPowerConsumerActive() ? 0 : ( o1.isPowerCharging(currentTimeMillis) ? o1.getPowerConsumedPerSecondCharging() : o1.getPowerConsumedPerSecondResting()),
					!o2.isPowerConsumerActive() ? 0 : ( o2.isPowerCharging(currentTimeMillis) ? o2.getPowerConsumedPerSecondCharging() : o2.getPowerConsumedPerSecondResting()));
		});
		
		addTextFilter(new GUIListFilterText<PowerConsumer>() {

			@Override
			public boolean isOk(String input, PowerConsumer listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);
		
		
		activeSortColumnIndex = 0;
		continousSortColumn = 0;
	}


	@Override
	protected Collection<PowerConsumer> getElementList() {
		List<PowerConsumer> p = new ObjectArrayList<PowerConsumer>();
		p.addAll(c.getPowerInterface().getPowerConsumerList());
		assert(p != null);
		return p;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<PowerConsumer> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final FactionManager m = ((FactionState)getState()).getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		
		for (final PowerConsumer f : collection) {
			GUITextOverlayTable indexText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable actText = new GUITextOverlayTable(getState());
			GUITextOverlayTable poweredText = new GUITextOverlayTable(getState());
			GUITextOverlayTable consumptionCText = new GUITextOverlayTable(getState());
			GUITextOverlayTable consumptionRText = new GUITextOverlayTable(getState());
			GUITextOverlayTable consumptionModeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable consumptionCurrentText = new GUITextOverlayTable(getState());
			final int index = i;
			GUIClippedRow indexAnchorP = new GUIClippedRow(getState());
			indexAnchorP.attach(indexText);
			indexText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return String.valueOf(index);
				}
			});
			
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(f.getName());
			actText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.isPowerConsumerActive() ? "ON" : "OFF";
				}
			});
			
			
			GUIClippedRow poweredAnchorP = new GUIClippedRow(getState());
			poweredAnchorP.attach(poweredText);
			poweredText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return StringTools.formatPointZero(f.getPowered()*100d)+"%";
				}
			});
			
			consumptionCText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return StringTools.formatPointZeroZero(f.getPowerConsumedPerSecondCharging());
				}
			});
			consumptionRText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return StringTools.formatPointZeroZero(f.getPowerConsumedPerSecondResting());
				}
			});
			consumptionModeText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.isPowerCharging(System.currentTimeMillis()) ? "CHARGE" : "REST";
				}
			});
			consumptionCurrentText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return !f.isPowerConsumerActive() ? "-" : StringTools.formatPointZeroZero(( f.isPowerCharging(System.currentTimeMillis()) ? f.getPowerConsumedPerSecondCharging() : f.getPowerConsumedPerSecondResting()));
				}
			});
			nameText.getPos().y = 4;
			actText.getPos().y = 4;
			consumptionCText.getPos().y = 4;
			poweredAnchorP.getPos().y = 4;
			consumptionRText.getPos().y = 4;
			consumptionModeText.getPos().y = 4;
			consumptionCurrentText.getPos().y = 4;

			PowerConsumerRow r = new PowerConsumerRow(getState(), f, indexAnchorP, nameAnchorP, actText, poweredAnchorP, consumptionCText, consumptionRText, consumptionModeText, consumptionCurrentText);
			r.expanded = null;
			

			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}
	
	@Override
	public void draw() {
		super.draw();
	}

	private class PowerConsumerRow extends Row {

		public PowerConsumerRow(InputState state, PowerConsumer f, GUIElement... elements) {
			super(state, f, elements);
		}
	}




}
