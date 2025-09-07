package org.schema.game.client.controller;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.ScanData;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlayerScanDialog extends PlayerGameOkCancelInput {
	private static final DateFormat dateFormat = StringTools.getSimpleDateFormat(Lng.str("MM/dd/yyyy HH:mm"), "MM/dd/yyyy HH:mm");

	public PlayerScanDialog(GameClientState state, List<ScanData> data) {
		super("Scan History", state, 600, 500, Lng.str("Scan History"), "");
		getInputPanel().setOkButtonText(Lng.str("DONE"));
		getInputPanel().setCancelButton(false);
		getInputPanel().onInit();
		GUIScrollablePanel scrollPane = new GUIScrollablePanel(824, 424, ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(0), state);
		int i = 0;
		GUIElementList l = new GUIElementList(state);
		for(int j = data.size() - 1; j >= 0; j--) {
			ScanData d = data.get(j);
			String time = dateFormat.format(new Date(d.time));
			add(state, l, Lng.str("Scan Time:"), new Object() {
				@Override
				public String toString() {
					return time;
				}
			}, i);
			i++;
			add(state, l, Lng.str("Scanned From:"), new Object() {
				@Override
				public String toString() {
					return d.origin.toString();
				}
			}, i);
			i++;
			add(state, l, Lng.str("Scan Range:"), new Object() {
				@Override
				public String toString() {
					return String.valueOf(d.range);
				}
			}, i);
			i++;
			add(state, l, Lng.str("System Ownership:"), new Object() {
				@Override
				public String toString() {
					VoidSystem systemOnClient = getState().getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(d.origin);
					Faction f;
					if(systemOnClient != null && systemOnClient.getOwnerUID() != null && systemOnClient.getOwnerFaction() != 0 && (f = getState().getFactionManager().getFaction(systemOnClient.getOwnerFaction())) != null) {
						return f.getName();
					} else {
						return "-";
					}
				}
			}, i);
			i++;
			String sectorRel = switch(d.systemOwnerShipType) {
				case BY_ALLY -> Lng.str("Allied Territory");
				case BY_ENEMY -> Lng.str("Enemy Territory");
				case BY_NEUTRAL -> Lng.str("Neutral Territory");
				case BY_SELF -> Lng.str("Own Territory");
				case NONE -> Lng.str("Unclaimed Territory");
			};
			add(state, l, Lng.str("Territory Relation:"), new Object() {
				@Override
				public String toString() {
					return sectorRel;
				}
			}, i);
			i++;
			add(state, l, Lng.str("Mining bonus multiplier in this System:"), new Object() {
				@Override
				public String toString() {
					return String.valueOf(d.systemOwnerShipType.getMiningBonusMult());
				}
			}, i);
			i++;
			add(state, l, Lng.str("System Owner Station Position:"), new Object() {
				@Override
				public String toString() {
					VoidSystem systemOnClient = getState().getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(d.origin);
					if(systemOnClient != null &&
						systemOnClient.getOwnerUID() != null && systemOnClient.getOwnerFaction() != 0) {
						return systemOnClient.getOwnerPos().toString();
					} else {
						return "-";
					}
				}
			}, i);
			i++;
			add(state, l, Lng.str("Scanned Activity:"), new Object() {
				@Override
				public String toString() {
					if(d.data.isEmpty()) {
						return Lng.str("NO ACTIVITY");
					}
					return "";
				}
			}, i);
			i++;
			Collections.sort(d.data, (o1, o2) -> {
				if(o1.factionId == o2.factionId) {
					return o1.name.toLowerCase(Locale.ENGLISH).compareTo(o2.name.toLowerCase(Locale.ENGLISH));
				}
				return o1.factionId - o2.factionId;
			});
			for(int h = 0; h < d.data.size(); h++) {
				ScanData.DataSet dataSet = d.data.get(h);
				add(state, l, Lng.str("#%s Name: ", h), new Object() {
					@Override
					public String toString() {
						return dataSet.name;
					}
				}, i);
				i++;
				add(state, l, Lng.str("Faction: "), new Object() {
					@Override
					public String toString() {
						if(getState().getFaction() != null && getState().getFaction().getPersonalEnemies().contains(dataSet.name)) {
							return Lng.str("Personal Faction Enemy");
						}
						if(dataSet.factionId == 0) {
							return Lng.str("Not in a Faction");
						}
						Faction faction = getState().getFactionManager().getFaction(dataSet.factionId);
						if(faction != null) {
							return faction.getName();
						} else {
							return Lng.str("Unknown Faction (%s)", dataSet.factionId);
						}
					}
				}, i);
				i++;
				add(state, l, Lng.str("Relation: "), new Object() {
					@Override
					public String toString() {
						if(getState().getFaction() != null && getState().getFaction().getPersonalEnemies().contains(dataSet.name)) {
							return Lng.str("Personal Faction Enemy");
						}
						return getState().getFactionManager().getRelation(state.getPlayer().getFactionId(), dataSet.factionId).name();
					}
				}, i);
				i++;
				add(state, l, Lng.str("Sector: "), new Object() {
					@Override
					public String toString() {
						return dataSet.sector.toString();
					}
				}, i);
				i++;
				add(state, l, Lng.str("Vessel: "), new Object() {
					@Override
					public String toString() {
						return dataSet.controllerInfo;
					}
				}, i);
				i++;
			}
			i++;
			add(state, l, "", "", i); //line break
			i++;
			if(d.systemResourceData.isEmpty()) {
				add(state, l, Lng.str("Unable To Detect Available Resources In System"), new Object() {
					@Override
					public String toString() {
						return Lng.str("(No resources in range)");
					}
				}, i);
				i++;
			} else {
				add(state, l, Lng.str("POSSIBLE SYSTEM RESOURCES:"), new Object() {
					@Override
					public String toString() {
						return "ACQUISITION:";
					}
				}, i);
				i++;
				for(int k = 0; k < d.systemResourceData.size(); k++) {
					ScanData.SystemResourceScanDataSet dataSet = d.systemResourceData.get(k);
					add(state, l, Lng.str("Resource: %s - Concentration: %s", dataSet.name, String.format("%.2f", dataSet.resourceQuantity * 100) + "%"), dataSet.resourceInfo, i);
					i++;
				}
			}
			i++;
			if(d.extractorResourceData.isEmpty()) {
				final String s1 = d.extractorScan? Lng.str("NO EXTRACTABLE RESOURCE SITES FOUND") : Lng.str("NO SCANNER PROSPECTING CAPABILITY");
				final String s2 = d.extractorScan? Lng.str("(No resource locations in range)") : Lng.str("(No prospecting chamber installed / capability blocked)");
				add(state, l, s1, new Object() {
					@Override
					public String toString() {
						return s2;
					}
				}, i);
				i++;
			} else {
				add(state, l, Lng.str("EXTRACTABLE RESOURCE SITES:"), new Object() {
					@Override
					public String toString() {
						return Lng.str("DESCRIPTION:");
					}
				}, i);
				i++;
				for(int k = 0; k < d.extractorResourceData.size(); k++) {
					ScanData.ExtractorResourceScanDataSet dataSet = d.extractorResourceData.get(k);
					add(state, l, Lng.str("Source: %s\n", dataSet.name), dataSet + "\n", i);
					i++;
				}
			}
			i++;
			add(state, l, "-----------------------------------------------------------", new Object() {
				@Override
				public String toString() {
					return "";
				}
			}, i);
			i++;
		}
		scrollPane.setContent(l);
		l.setScrollPane(scrollPane);
		getInputPanel().getContent().attach(scrollPane);
		getInputPanel().setCancelButton(false);
	}

	private void add(GameClientState state, GUIElementList l, String title, Object value, int index) {
		GUIColoredRectangle a = new GUIColoredRectangle(getState(), 800, 20, GUIElementList.getRowColor(index));
		GUITextOverlay t = new GUITextOverlay(state);
		t.setTextSimple(title);
		GUITextOverlay tv = new GUITextOverlay(state);
		tv.setTextSimple(value);
		tv.getPos().y = 2;
		t.getPos().y = 2;
		tv.getPos().x = 420;
		a.attach(t);
		a.attach(tv);
		l.add(new GUIListElement(a, a, state));
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public void pressedOK() {
		deactivate();
	}

	@Override
	public boolean allowChat() {
		return true;
	}
}
