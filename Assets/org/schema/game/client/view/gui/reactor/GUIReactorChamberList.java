package org.schema.game.client.view.gui.reactor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class GUIReactorChamberList extends ScrollableTableList<ReactorChamberUnit> implements ReactorTreeListener {

	private ManagerContainer<?> c;

	public GUIReactorChamberList(InputState state, ManagerContainer<?> c, GUIElement p) {
		super(state, 100, 100, p);
		this.c = c;
		c.getPowerInterface().addObserver(this);
	}

	private final Set<ReactorChamberUnit> selectedReactorChamberUnit = new ObjectOpenHashSet<ReactorChamberUnit>();

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
		addColumn(Lng.str("Position"), 2, (o1, o2) -> o1.getSignificator(new Vector3i()).compareTo(o2.getSignificator(new Vector3i())), true);
		addColumn(Lng.str("Type"), 3, (o1, o2) -> {
			ElementInformation info1 = ElementKeyMap.getInfo(o1.elementCollectionManager.getChamberId());
			ElementInformation info2 = ElementKeyMap.getInfo(o2.elementCollectionManager.getChamberId());
			return info1.getName().compareTo(info2.getName());
		});
		addColumn(Lng.str("Size"), 1, (o1, o2) -> o1.size() - o2.size());
		addFixedWidthColumnScaledUI(Lng.str("Select"), 50, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<ReactorChamberUnit>() {

			@Override
			public boolean isOk(String input, ReactorChamberUnit listElement) {
				ElementInformation info1 = ElementKeyMap.getInfo(listElement.elementCollectionManager.getChamberId());
				return info1.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);
	}

	@Override
	protected boolean isFiltered(ReactorChamberUnit e) {
		return super.isFiltered(e) || c.getPowerInterface().isInAnyTree(e);
	}

	@Override
	protected Collection<ReactorChamberUnit> getElementList() {
		List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> chambers = c.getPowerInterface().getChambers();
		List<ReactorChamberUnit> p = new ObjectArrayList<ReactorChamberUnit>();
		for (ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> cham : chambers) {
			for (ReactorChamberUnit e : cham.getCollectionManager().getElementCollections()) {
				p.add(e);
			}
		}
		assert (p != null);
		return p;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<ReactorChamberUnit> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager m = ((FactionState) getState()).getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		ScrollableTableList<ReactorChamberUnit>.Seperator cv = getSeperator(Lng.str("Disconnected (missing condiut connection to reactor)"), 0);
		for (final ReactorChamberUnit f : collection) {
			final ElementInformation info = ElementKeyMap.getInfo(f.elementCollectionManager.getChamberId());
			final Vector3i idPos = f.getSignificator(new Vector3i());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable typeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sizeText = new GUITextOverlayTable(getState());
			GUIAnchor optionArchor = new GUIAnchor(getState(), 10, 10);
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return idPos.toStringPure();
				}
			});
			typeText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return info.getName();
				}
			});
			sizeText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSmallAndBig(f.size());
				}
			});
			GUITextButton b = new GUITextButton(getState(), 50, this.getDefaultColumnsHeight(), Lng.str("Select"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUIReactorChamberList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(666);
						System.err.println("TODO select");
					}
				}
			});
			nameText.getPos().y = 4;
			typeText.getPos().y = 4;
			sizeText.getPos().y = 4;
			optionArchor.getPos().y = 0;
			ReactorChamberUnitRow r = new ReactorChamberUnitRow(getState(), f, nameAnchorP, typeText, sizeText, optionArchor);
			r.seperator = cv;
			r.expanded = null;
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	public Set<ReactorChamberUnit> getSelectedReactorChamberUnit() {
		return selectedReactorChamberUnit;
	}

	private class ReactorChamberUnitRow extends Row {

		public ReactorChamberUnitRow(InputState state, ReactorChamberUnit f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#isSimpleSelected()
		 */
		@Override
		protected boolean isSimpleSelected() {
			return getSelectedReactorChamberUnit().contains(f);
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#clickedOnRow()
		 */
		@Override
		protected void clickedOnRow() {
			if (isSimpleSelected()) {
				getSelectedReactorChamberUnit().remove(f);
			} else {
				getSelectedReactorChamberUnit().add(f);
			}
		}
	}
}
