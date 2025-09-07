package org.schema.game.client.data;

import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ClientMessageLog extends GUIObservable {
	public static final Color flashingColor = new Color(0.7f, 0.3f, 8f, 1);
	public static final Color tipColor = new Color(0.3f, 0.3f, 0.0f, 1.0f);
	public static final Color infoColor = new Color(0.0f, 0.3f, 0.0f, 1.0f);
	public static final Color gameColor = new Color(0.0f, 0.0f, 0.3f, 1.0f);
	public static final Color errorColor = new Color(0.5f, 0.0f, 0.0f, 1.0f);
	public static final Color chatColor = new Color(0.0f, 0.3f, 0.3f, 1.0f);

	private final ObjectArrayList<ClientMessageLogEntry> log = new ObjectArrayList<ClientMessageLogEntry>();
	private final ObjectArrayList<ClientMessageLogEntry> factionLog = new ObjectArrayList<ClientMessageLogEntry>();
	private final Object2ObjectOpenHashMap<String, ObjectArrayList<ClientMessageLogEntry>> privateLog = new Object2ObjectOpenHashMap<String, ObjectArrayList<ClientMessageLogEntry>>();
	private boolean changed;
	private String currentFilter;

	public void log(ClientMessageLogEntry l) {
		log.add(l);
		this.setChanged(true);
	}

	public void logFaction(ClientMessageLogEntry l) {
		factionLog.add(l);
		this.setChanged(true);
	}

	public void logPrivate(String name, ClientMessageLogEntry l) {
		ObjectArrayList<ClientMessageLogEntry> list = privateLog.get(name);
		if (list == null) {
			list = new ObjectArrayList();
			privateLog.put(name, list);
		}
		list.add(l);

		this.setChanged(true);
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;

		//Observer: MessageLogScrollableListNew
		notifyObservers();
	}

	public String getCurrentChatPrefix() {
		if (currentFilter == null) {
			return "";
		}
		if (currentFilter.equals("*all")) {
			return "";
		} else if (currentFilter.equals("*faction")) {
			return "/f ";
		} else if (currentFilter.equals("*private")) {
			return "";
		} else if (currentFilter.equals("*system")) {
			return "";
		} else {
			return "/pm " + currentFilter + " ";
		}
	}

	public GUIDropDownList makeGUIList(final GUIElementList list, final boolean dateTag, final boolean typeTag) {
		int width = 200;
		int height = 16;
		GUITextOverlay all = new GUITextOverlay(list.getState());
		all.setTextSimple("*all");
		all.setUserPointer("*all");
		GUITextOverlay faction = new GUITextOverlay(list.getState());
		faction.setTextSimple("*faction");
		faction.setUserPointer("*faction");
		GUITextOverlay privateOnly = new GUITextOverlay(list.getState());
		privateOnly.setTextSimple("*private");
		privateOnly.setUserPointer("*private");
		GUITextOverlay system = new GUITextOverlay(list.getState());
		system.setTextSimple("*system");
		system.setUserPointer("*system");

		GUITextOverlay[] privates = new GUITextOverlay[privateLog.size()];
		int i = 0;
		for (String name : privateLog.keySet()) {
			privates[i] = new GUITextOverlay(list.getState());

			privates[i].setTextSimple("PM " + name);
			privates[i].setUserPointer(name);
			i++;
		}
		ObjectArrayList<GUIListElement> t = new ObjectArrayList<GUIListElement>();

		add(t, all);
		add(t, faction);
		add(t, privateOnly);
		add(t, system);
		for (GUITextOverlay a : privates) {
			add(t, a);
		}
		Int2ObjectOpenHashMap<Sendable> localObjects = ((GameClientState) list.getState()).getLocalAndRemoteObjectContainer().getLocalObjects();
		synchronized (localObjects) {
			for (Sendable s : localObjects.values()) {
				if (s instanceof PlayerState) {
					PlayerState p = (PlayerState) s;
					if (p != ((GameClientState) list.getState()).getPlayer() && !privateLog.containsKey(p.getName())) {
						GUITextOverlay gg = new GUITextOverlay(list.getState());

						gg.setTextSimple("PM " + p.getName());
						gg.setUserPointer(p.getName());
						add(t, gg);
					}
				}
			}
		}
		CB dropDownCallback = new CB(list, dateTag, typeTag);
		final GUIDropDownList dropDown = new GUIDropDownList(list.getState(), width, height, 400, dropDownCallback, t);
		dropDownCallback.first = false;
		for (int h = 0; h < dropDown.size(); h++) {
			if (dropDown.get(h).getUserPointer().equals(currentFilter)) {
				GUIListElement guiListElement = dropDown.get(h);
				dropDown.setSelectedElement(guiListElement);
			}
		}

		return dropDown;
	}

	private void add(ObjectArrayList<GUIListElement> t, GUIElement e) {
		GUIListElement guiListElement = new GUIListElement(e, e, e.getState());
		guiListElement.setUserPointer(e.getUserPointer());
		t.add(guiListElement);
	}

	/**
	 * @return the log
	 */
	public ObjectArrayList<ClientMessageLogEntry> getLog() {
		return log;
	}

	/**
	 * @return the factionLog
	 */
	public ObjectArrayList<ClientMessageLogEntry> getFactionLog() {
		return factionLog;
	}

	/**
	 * @return the privateLog
	 */
	public Object2ObjectOpenHashMap<String, ObjectArrayList<ClientMessageLogEntry>> getPrivateLog() {
		return privateLog;
	}

	private class CB implements DropDownCallback {
		boolean first = true;
		private GUIElementList list;
		private boolean dateTag;
		private boolean typeTag;

		public CB(GUIElementList list, boolean dateTag, boolean typeTag) {
			this.list = list;
			this.dateTag = dateTag;
			this.typeTag = typeTag;
		}

		@Override
		public void onSelectionChanged(GUIListElement element) {
			list.clear();

			if (currentFilter == null) {
				currentFilter = (String) element.getUserPointer();
				first = false;
			} else if (first) {
				first = false;
			} else {
				currentFilter = (String) element.getUserPointer();
			}

			if (currentFilter.equals("*all")) {
				ObjectArrayList<ClientMessageLogEntry> log = ClientMessageLog.this.getLog();
				for (int i = log.size() - 1; i >= 0; i--) {
					list.add(log.get(i).getGUIListEntry((GameClientState) list.getState(), dateTag, typeTag));
				}
			} else if (currentFilter.equals("*faction")) {
				ObjectArrayList<ClientMessageLogEntry> log = ClientMessageLog.this.getFactionLog();
				for (int i = log.size() - 1; i >= 0; i--) {
					list.add(log.get(i).getGUIListEntry((GameClientState) list.getState(), dateTag, typeTag));
				}
			} else if (currentFilter.equals("*private")) {
				for (int i = getLog().size() - 1; i >= 0; i--) {
					if (getLog().get(i).getType() == ClientMessageLogType.CHAT_PRIVATE) {
						list.add(getLog().get(i).getGUIListEntry((GameClientState) list.getState(), dateTag, typeTag));
					}
				}
			} else if (currentFilter.equals("*system")) {
				for (int i = getLog().size() - 1; i >= 0; i--) {
					if (getLog().get(i).getType() == ClientMessageLogType.ERROR ||
							getLog().get(i).getType() == ClientMessageLogType.INFO ||
							getLog().get(i).getType() == ClientMessageLogType.GAME ||
							getLog().get(i).getType() == ClientMessageLogType.TIP
							) {
						list.add(getLog().get(i).getGUIListEntry((GameClientState) list.getState(), dateTag, typeTag));
					}
				}
			} else {
				ObjectArrayList<ClientMessageLogEntry> log = getPrivateLog().get(currentFilter);
				if (log != null) {
					for (int i = log.size() - 1; i >= 0; i--) {
						list.add(log.get(i).getGUIListEntry((GameClientState) list.getState(), dateTag, typeTag));
					}
				}
			}
		}
	}

	//	/**
	//	 * @return the log
	//	 */
	//	public ObjectArrayList<ClientMessageLogEntry> getLog() {
	//		return log;
	//	}
	//	/**
	//	 * @return the factionLog
	//	 */
	//	public ObjectArrayList<ClientMessageLogEntry> getFactionLog() {
	//		return factionLog;
	//	}
	//	/**
	//	 * @return the privateLog
	//	 */
	//	public Object2ObjectOpenHashMap<String, ClientMessageLogEntry> getPrivateLog() {
	//		return privateLog;
	//	}

}
