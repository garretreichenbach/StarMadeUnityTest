package org.schema.game.client.view.gui.trade;

import api.common.GameCommon;
import org.schema.game.client.controller.PlayerBlockTypeDropdownInputNew;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.*;

import static org.schema.game.common.controller.trade.TradeManager.*;

public class GUITradeNodeScrollableList extends ScrollableTableList<TradeNodeStub> {

	private ShopInterface currentClosestShop;

	public GUITradeNodeScrollableList(InputState state, ShopInterface currentClosestShop, GUIElement p) {
		super(state, 100, 100, p);
		this.currentClosestShop = currentClosestShop;
		((GameClientState) state).getController().getClientChannel().getGalaxyManagerClient().tradeDataListener.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState) getState()).getController().getClientChannel().getGalaxyManagerClient().tradeDataListener.deleteObserver(this);
	}

	@Override
	protected boolean isFiltered(TradeNodeStub e) {
		return super.isFiltered(e) || (currentClosestShop != null && e.getEntityDBId() == currentClosestShop.getSegmentController().getDbId());
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("System"), 3f, (o1, o2) -> (o1.getSystem()).compareTo(o2.getSystem()));
		addColumn(Lng.str("Name"), 3f, (o1, o2) -> o1.getStationName().compareTo(o2.getStationName()));
		addColumn(Lng.str("Faction"), 3f, (o1, o2) -> {
			String a = ((FactionState) getState()).getFactionManager().getFactionName(o1.getFactionId());
			String b = ((FactionState) getState()).getFactionManager().getFactionName(o2.getFactionId());
			return a.compareTo(b);
		});
		addColumn(Lng.str("Owner"), 3f, (o1, o2) -> {
			String a = o1.getOwners().isEmpty() ? Lng.str("Nobody") : o1.getOwnerString();
			String b = o2.getOwners().isEmpty() ? Lng.str("Nobody") : o2.getOwnerString();
			return a.compareTo(b);
		});
		addFixedWidthColumnScaledUI(Lng.str("Order"), 80, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<TradeNodeStub>() {

			@Override
			public boolean isOk(String input, TradeNodeStub listElement) {
				return listElement.getStationName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<TradeNodeStub>() {

			@Override
			public boolean isOk(String input, TradeNodeStub listElement) {
				String a = ((FactionState) getState()).getFactionManager().getFactionName(listElement.getFactionId());
				return a.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY FACTION"), FilterRowStyle.RIGHT);
		activeSortColumnIndex = 1;
		addButton(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITradeNodeScrollableList.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerBlockTypeDropdownInputNew p = new PlayerBlockTypeDropdownInputNew("IP_SDFDFDWW", (GameClientState) getState(), Lng.str("SEARCH FOR OFFER"), 0, 0, false) {

						@Override
						public void onOkMeta(MetaObject object) {
						}

						@Override
						protected boolean includeInfo(ElementInformation info) {
							return info.isShoppable() && !info.isDeprecated();
						}

						@Override
						public void onOk(ElementInformation info) {
							if (currentClosestShop != null) {
								TradeNodeTypeSearchDialog sc;
								try {
									sc = new TradeNodeTypeSearchDialog(getState(), currentClosestShop, info);
									sc.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(709);
								} catch (ShopNotFoundException e) {
									e.printStackTrace();
								}
							} else {
								getState().getController().popupAlertTextMessage("Search not available here", 0);
							}
						}

						@Override
						public void onAdditionalElementOk(Object userPointer) {
						}
					};
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(710);
				}
			}
		}, Lng.str("SEARCH FOR OFFER"), FilterRowStyle.FULL, FilterPos.TOP);
	}

	@Override
	protected Collection<TradeNodeStub> getElementList() {
		Collection<TradeNodeStub> list = new ArrayList<>();
		for(TradeNodeStub tradeNode : ((GameClientState) getState()).getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().values()) {
			if(currentClosestShop == null) break;
			if(tradeNode.getTradePermission() == PERM_ALL) list.add(tradeNode);
			else if(tradeNode.getTradePermission() == PERM_ALL_BUT_ENEMY) {
				if(! (GameCommon.getGameState().getFactionManager().isEnemy(tradeNode.getFactionId(), currentClosestShop.getFactionId()))) list.add(tradeNode);
			} else if(tradeNode.getTradePermission() == PERM_ALLIES_AND_FACTION) {
				if(GameCommon.getGameState().getFactionManager().isFriend(tradeNode.getFactionId(), currentClosestShop.getFactionId())) list.add(tradeNode);
			} else if(tradeNode.getTradePermission() == PERM_FACTION) {
				if(tradeNode.getFactionId() == currentClosestShop.getFactionId()) list.add(tradeNode);
			}
		}
		return list;
	}

	// @Override
	// public void draw() {
	// super.draw();
	//
	// long t = System.currentTimeMillis();
	// if(t - lastRefresh > 1000){
	// flagDirty();
	// lastRefresh = t;
	// }
	// }
	@Override
	public void updateListEntries(GUIElementList mainList, Set<TradeNodeStub> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TradeNodeStub f : collection) {
			GUITextOverlayTable systemText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable factionText = new GUITextOverlayTable(getState());
			GUITextOverlayTable ownerText = new GUITextOverlayTable(getState());
			systemText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getSystem().toStringPure() + " sec[" + f.getSector().toStringPure() + "]";
				}
			});
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getStationName();
				}
			});
			factionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String a = ((FactionState) getState()).getFactionManager().getFactionName(f.getFactionId());
					return a;
				}
			});
			ownerText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String a = f.getOwnerString();
					return a;
				}
			});
			GUIClippedRow sysAnchorP = new GUIClippedRow(getState());
			sysAnchorP.attach(systemText);
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			GUIClippedRow factionAnchorP = new GUIClippedRow(getState());
			factionAnchorP.attach(factionText);
			GUIClippedRow ownerAnchorP = new GUIClippedRow(getState());
			ownerAnchorP.attach(ownerText);
			GUITextButton b = new GUITextButton(getState(), 50, this.getDefaultColumnsHeight(), Lng.str("Order"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUITradeNodeScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openOrderDialog(f);
					}
				}
			});
			GUIClippedRow orderAnchorP = new GUIClippedRow(getState());
			orderAnchorP.attach(b);
			systemText.getPos().y = 5;
			nameText.getPos().y = 5;
			factionText.getPos().y = 5;
			ownerText.getPos().y = 5;
			TradeNodeStubRow r = new TradeNodeStubRow(getState(), f, sysAnchorP, nameAnchorP, factionAnchorP, ownerAnchorP, orderAnchorP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private void openOrderDialog(TradeNodeStub f) {
		if (currentClosestShop == null) {
			((GameClientState) getState()).getController().popupAlertTextMessage("Ordering not available here", 0);
			return;
		}
		OrderDialog d;
		try {
			d = new OrderDialog((GameClientState) getState(), currentClosestShop, (TradeNodeClient) f);
			d.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(711);
		} catch (ShopNotFoundException e) {
			e.printStackTrace();
			((GameClientState) getState()).getController().popupAlertTextMessage(e.getMessage(), 0);
		}
	}

	private class TradeNodeStubRow extends Row {

		public TradeNodeStubRow(InputState state, TradeNodeStub f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
