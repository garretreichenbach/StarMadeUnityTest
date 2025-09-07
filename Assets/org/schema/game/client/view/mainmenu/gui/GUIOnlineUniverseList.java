package org.schema.game.client.view.mainmenu.gui;

import api.mod.ModIdentifier;
import api.mod.ServerModInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.ErrorMessage;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ListColorPalette;
import org.schema.schine.input.InputState;
import org.schema.schine.network.AbstractServerInfo;
import org.schema.schine.network.ServerInfo;
import org.schema.schine.network.ServerListRetriever.ServerListListener;
import org.schema.schine.network.server.ServerEntry;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

public class GUIOnlineUniverseList extends ScrollableTableList<AbstractServerInfo> implements ServerListListener {

	private boolean firstCall = true;

	private OnlineServerFilter oFilter;

	private final Collection<AbstractServerInfo> infos = new ObjectArrayList<AbstractServerInfo>();

	private boolean noneSelected = true;

	private GUIHideableTextOverlay errorMessage;

	public GUIOnlineUniverseList(InputState state, float width, float height, OnlineServerFilter oFilter, GUIElement p) {
		// super(state, 100, 100, p);
		super(state, width, height, p);
		this.oFilter = oFilter;
		((GameMainMenuController) state).getServerListRetriever().deleteObservers();
		((GameMainMenuController) state).getServerListRetriever().addObserver(this);
		((GameMainMenuController) state).getServerListRetriever().listener.clear();
		((GameMainMenuController) state).getServerListRetriever().listener.add(this);
		oFilter.deleteObservers();
		oFilter.addObserver(this);
		FontInterface errorFont = FontSize.BIG_30;
		// Setup the error message
		errorMessage = new GUIHideableTextOverlay(10, 10, errorFont, getState()) {

			@Override
			public void draw() {
				setColor(1, 1, 1, 1);
				super.draw();
			}
		};
		errorMessage.setPos(5, getContentHeight() * 0.10f, 0);
		// errorMessage.setPos(5, 400, 0);
		p.attach(errorMessage);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameMainMenuController) getState()).getServerListRetriever().deleteObserver(this);
		oFilter.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Host"), 5f, (o1, o2) -> (o1.getHost().toLowerCase(Locale.ENGLISH)).compareTo(o2.getHost().toLowerCase(Locale.ENGLISH)));
		addColumn(Lng.str("Name"), 5f, (o1, o2) -> (o1.getName().toLowerCase(Locale.ENGLISH)).compareTo(o2.getName().toLowerCase(Locale.ENGLISH)));
		addFixedWidthColumnScaledUI(Lng.str("Version"), 120, (o1, o2) -> {
			if (o1.getVersion().isEmpty() && o2.getVersion().isEmpty()) {
				return 0;
			}
			if (o2.getVersion().isEmpty()) {
				return 1;
			}
			if (o1.getVersion().isEmpty()) {
				return -1;
			}
			try {
				int v = VersionContainer.compareVersion(o1.getVersion(), o2.getVersion());
				int pComp = o1.getPlayerCount() > o2.getPlayerCount() ? -1 : (o1.getPlayerCount() < o2.getPlayerCount() ? 1 : 0);
				return v == 0 ? pComp : v;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}, true);
		addFixedWidthColumnScaledUI(Lng.str("Players"), 70, (o1, o2) -> o1.getPlayerCount() > o2.getPlayerCount() ? -1 : (o1.getPlayerCount() < o2.getPlayerCount() ? 1 : 0));
		addFixedWidthColumnScaledUI(Lng.str("Ping"), 70, (o1, o2) -> o1.getPing() > o2.getPing() ? 1 : (o1.getPing() < o2.getPing() ? -1 : 0));
		addTextFilter(new GUIListFilterText<AbstractServerInfo>() {

			@Override
			public boolean isOk(String input, AbstractServerInfo listElement) {
				boolean host = listElement.getHost().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
				boolean name = listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
				boolean desc = listElement.getDesc().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
				return host || name || desc;
			}
		}, Lng.str("SEARCH BY HOST/NAME/DESCRIPTION"), FilterRowStyle.FULL, FilterPos.TOP);
	}

	@Override
	protected Collection<AbstractServerInfo> getElementList() {
		if (firstCall) {
			((GameMainMenuController) getState()).getServerListRetriever().startRetrieving();
			firstCall = false;
		}
		return infos;
	}

	@Override
	public void clear() {
		infos.clear();
		hideServerError();
		onChange(false);
	}

	@Override
	public void draw() {
		super.draw();
	}

	// Lng.str("Unable to retrieve server list, please check your internet connection and settings")
	private void showServerError(String msg) {
		errorMessage.setTextSimple(msg);
		errorMessage.unhide();
	}

	private void hideServerError() {
		errorMessage.hide();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<AbstractServerInfo> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;
		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final AbstractServerInfo f : collection) {
			GUITextOverlayTable pingText = new GUITextOverlayTable(getState());
			pingText.setTextSimple(f.getPing());
			pingText.getPos().y = 5;
			GUIClippedRow pingP = new GUIClippedRow(getState());
			pingP.attach(pingText);
			GUITextOverlayTable versionText = new GUITextOverlayTable(getState());
			versionText.setTextSimple(f.getVersion());
			versionText.getPos().y = 5;
			GUIClippedRow versionP = new GUIClippedRow(getState());
			versionP.attach(versionText);
			GUITextOverlayTable playersText = new GUITextOverlayTable(getState());
			playersText.setTextSimple(f.getPlayerCount() + " / " + f.getMaxPlayers());
			playersText.getPos().y = 5;
			GUIClippedRow playersP = new GUIClippedRow(getState());
			playersP.attach(playersText);
			GUITextOverlayTable portText = new GUITextOverlayTable(getState());
			portText.setTextSimple(f.getPort());
			portText.getPos().y = 5;
			GUIClippedRow portP = new GUIClippedRow(getState());
			portP.attach(portText);
			GUITextOverlayTable hostText = new GUITextOverlayTable(getState());
			hostText.setTextSimple(f.getHost());
			hostText.getPos().y = 5;
			GUIClippedRow hostP = new GUIClippedRow(getState());
			hostP.attach(hostText);
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(f.getName());
			nameText.getPos().y = 5;
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);
			final ServerInfoInterfaceRow r = new ServerInfoInterfaceRow(getState(), f, hostP, nameP, versionP, playersP, pingP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(AbstractServerInfo e) {
		return oFilter.isFiltered(e) || super.isFiltered(e);
	}

	@Override
	public void onAddedInfo(ServerInfo serverInfo) {
		infos.add((AbstractServerInfo) serverInfo);
	}

	public void onErrorMessage(ErrorMessage m) {
		showServerError(Lng.str("Unable to retrieve server list, please check your internet connection and settings"));
	}

	private class ServerInfoInterfaceRow extends Row {
		private Vector4f[] customColorsFavorite = new Vector4f[]{
				ListColorPalette.favoriteHighlightColor,
				ListColorPalette.favoriteHighlightColorAlternate,
				ListColorPalette.selectedColorFavorite,
		};
		private Vector4f[] customColorsCustom = new Vector4f[]{
				ListColorPalette.specialHighlightColor,
				ListColorPalette.specialHighlightColorAlternate,
				ListColorPalette.selectedColorSpecial,
		};
		//INSERTED CODE
		private final Vector4f[] customColorsModded = new Vector4f[]{
				new Vector4f(0.25F,0.07F,0.07F, 0.5F),
				new Vector4f(0.15F,0.07F,0.07F, 0.5F),
				new Vector4f(0.05F,0.07F,0.07F, 0.5F),
		};
		///

		public ServerInfoInterfaceRow(InputState state, AbstractServerInfo f, GUIElement... elements) {
			super(state, f, elements);
			if (f.isCustom()) {
			} else {
			}
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			setAllwaysOneSelected(true);
			this.rightClickSelectsToo = true;
			if (noneSelected) {
				setSelectedRow(this);
				noneSelected = false;
			}
		}

		private void switchFavorite() {
			if (!f.isFavorite()) {
				f.setFavorite(true);
				List<ServerEntry> read = new ObjectArrayList<ServerEntry>();
				try {
					read.addAll(ServerEntry.read("favorites.smsl"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				ServerEntry newEn = new ServerEntry(f.getHost(), f.getPort());
				if (!read.contains(newEn)) {
					read.add(newEn);
					try {
						ServerEntry.write(read, "favorites.smsl");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				f.setFavorite(false);
				List<ServerEntry> read = new ObjectArrayList<ServerEntry>();
				try {
					read.addAll(ServerEntry.read("favorites.smsl"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				ServerEntry newEn = new ServerEntry(f.getHost(), f.getPort());
				boolean remove = read.remove(newEn);
				if (remove) {
					try {
						ServerEntry.write(read, "favorites.smsl");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		protected GUIContextPane createContext() {
			GUIContextPane p = new GUIContextPane(getState(), 180, 25);
			int b = f.isCustom() ? 3 : 2;
			GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, b, p);
			buttons.onInit();
			buttons.addButton(0, 0, Lng.str("PLAY"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(778);
						getState().getController().getInputController().setCurrentContextPane(null);
						((GameMainMenuController) getState()).setSelectedOnlineUniverse((ServerInfo) f);
						((GameMainMenuController) getState()).startSelectedOnlineGame();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			buttons.addButton(0, 1, new Object() {

				@Override
				public String toString() {
					if (f.isFavorite()) {
						return Lng.str("REMOVE FROM FAVORITES");
					}
					return Lng.str("ADD TO FAVORITES");
				}
			}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(779);
						switchFavorite();
						getState().getController().getInputController().setCurrentContextPane(null);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			if (f.isCustom()) {
				buttons.addButton(0, 2, new Object() {

					@Override
					public String toString() {
						return Lng.str("REMOVE FROM LIST");
					}
				}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(780);
							List<ServerEntry> read = new ObjectArrayList<ServerEntry>();
							try {
								read.addAll(ServerEntry.read("customservers.smsl"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							ServerEntry newEn = new ServerEntry(f.getHost(), f.getPort());
							if (read.contains(newEn)) {
								read.remove(newEn);
								try {
									ServerEntry.write(read, "customservers.smsl");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							infos.remove(f);
							flagDirty();
							getState().getController().getInputController().setCurrentContextPane(null);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
			}
			p.attach(buttons);
			return p;
		}

		@Override
		protected void clickedOnRow() {
			super.clickedOnRow();
		}

		@Override
		public Vector4f[] getCustomRowColors() {
			//INSERTED CODE
			ArrayList<ModIdentifier> serverInfo = ServerModInfo.getServerInfo(ServerModInfo.getServerUID(f.getHost(), f.getPort()));
			boolean modded = serverInfo != null && serverInfo.size() > 1;
			if(modded){
				return customColorsModded;
			}
			if(f.isFavorite()){
				return customColorsFavorite;
			}
			if(f.isCustom()){
				return customColorsCustom;
			}
			return null;
			///
//			return f.isFavorite() ? customColorsFavorite : (f.isCustom() ? customColorsCustom : null);
		}

		@Override
		public void onDoubleClick() {
			super.onDoubleClick();
			System.err.println("[GUI] DOUBLE CLICK ON SERVER ENTRY: " + f);
		}
	}
}
