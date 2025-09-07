package org.schema.game.client.view.gui.faction.newfaction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.config.FactionPointIncomeConfig;
import org.schema.game.common.data.player.faction.config.FactionPointSpendingConfig;
import org.schema.game.common.data.player.faction.config.FactionPointsGeneralConfig;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FactionPointRevenueScrollableListNew extends ScrollableTableList<FactionPointStat> implements GUIChangeListener  {

	private final List<FactionPointStat> stats = new ObjectArrayList<FactionPointStat>();
	private Faction faction;

	public FactionPointRevenueScrollableListNew(InputState state, GUIElement p, Faction f) {
		super(state, 10, 10, p);
		this.faction = f;
		((GameClientState) getState()).getFactionManager().obs.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getFactionManager().obs.deleteObserver(this);
		super.cleanUp();

	}

	@Override
	public void initColumns() {


		addColumn("Status", 7, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
		addColumn("Value", 3, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

	}

	@Override
	protected Collection<FactionPointStat> getElementList() {
		createStats();
		return stats;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<FactionPointStat> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();

		for (FactionPointStat f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(f.name);

			nameText.getPos().y = UIScale.getUIScale().inset;

			GUITextOverlayTable valueText = new GUITextOverlayTable(getState());
			valueText.setTextSimple(f);
			valueText.getPos().y = UIScale.getUIScale().inset;

			FactionRow r = new FactionRow(getState(), f, nameText, valueText);

			r.expanded = new GUIElementList(getState());

			GUIAnchor c;
			if (f.name.equals("Owned Systems")) {
				GUIElementList pList = new GUIElementList(getState());
				for (int j = 0; j < faction.lastSystemSectors.size(); j++) {
					final Vector3i sectorPos = faction.lastSystemSectors.get(j);

					c = new GUIAnchor(getState(), 200, UIScale.getUIScale().h);

					GUITextOverlayTableInnerDescription l = new GUITextOverlayTableInnerDescription(10, 10, getState());

					l.setTextSimple(new Object() {
						                @Override
										public String toString() {
							                VoidSystem systemOnClient = ((GameClientState) getState()).getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(sectorPos);
							                if (systemOnClient == null) {
								                return sectorPos + "System calculating ...";
							                }
							                return "System " + systemOnClient.getPos() + "; Name: " + systemOnClient.getName() + "; Base: " + sectorPos;
						                }
					                }
					);
					c.attach(l);
					l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
					pList.addWithoutUpdate(new GUIListElement(c, c, getState()));
				}
				pList.updateDim();
				c = new GUIAnchor(getState(), 100, pList.height);
				c.attach(pList);
			} else {

				GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
				description.setTextSimple(f.description);
				c = new GUIAnchor(getState(), 100, 100);
				description.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(description);
			}

			r.expanded.add(new GUIListElement(c, c, getState()));

			r.onInit();
			mainList.addWithoutUpdate(r);
		}
		mainList.updateDim();
	}

	public void createStats() {
		stats.clear();
		final GameClientState state = (GameClientState) getState();
		final Faction f = faction;
		stats.add(new FactionPointStat("Next Faction Point Turn ", "Time until the next faction point turn.\nEach turn factions will gain points\nfrom active members, but also lose points\nfrom owned territory. If the faction points go negative,\nthe faction will lose territory, and after that its homebase\nwill become vulnerable!") {
			@Override
			public String getValue() {
				long lastTurn = state.getGameState().getNetworkObject().lastFactionPointTurn.getLong();

				long nextTurn = (long) (lastTurn + (FactionPointsGeneralConfig.INCOME_EXPENSE_PERIOD_MINUTES * 60L * 1000L));

				long ttoNext = nextTurn - System.currentTimeMillis();

				long seconds = ttoNext / 1000L;
				long minutes = seconds / 60L;
				long hours = minutes / 60L;
				minutes %= 60;
				seconds %= 60;
				return hours + " h, " + minutes + " min, " + seconds + " sec";
			}
		});

		stats.add(new FactionPointStat("Current Faction Points ", "Current amount of faction points") {
			@Override
			public String getValue() {
				return String.valueOf(f.factionPoints);
			}
		});
		stats.add(new FactionPointStat("Total Members ", "Amount of members at at the time of the last turn.") {
			@Override
			public String getValue() {
				return String.valueOf(f.getMembersUID().size());
			}
		});

		stats.add(new FactionPointStat("Online Members ", "Amount of online members at the turn") {
			@Override
			public String getValue() {
				return String.valueOf((int) (f.lastPointsFromOnline / FactionPointIncomeConfig.FACTION_POINTS_PER_ONLINE_MEMBER));
			}
		});
		stats.add(new FactionPointStat("Offline Active Members", "Amount of members who are offline but still active.\nAn offline player is considered active a set amount of time after logging off") {
			@Override
			public String getValue() {
				return String.valueOf((int) (f.lastPointsFromOffline / FactionPointIncomeConfig.FACTION_POINTS_PER_MEMBER));
			}
		});
		stats.add(new FactionPointStat("Inactive Members ", "Amount of inactive members in the faction.\nInactive players do not produce any faction points.") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastinactivePlayer);
			}
		});
		stats.add(new FactionPointStat("Last Points Gained From Offline", "Amount of points gained by this faction\nfrom players who are offline but still active.\nAn offline player is considered active a set amount of time after logging off") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastPointsFromOffline);
			}
		});
		stats.add(new FactionPointStat("Last Points Gained From Online", "Amount of points gained by this faction\nfrom players who are online and active") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastPointsFromOnline);
			}
		});
		stats.add(new FactionPointStat("Last Count Deaths", "Amount of deaths of members of this faction since the last turn") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastCountDeaths);
			}
		});

		stats.add(new FactionPointStat("Last Points lost from Player Deaths", "Each time a member of this faction is killed\nan amount of points will be deducted based on\nthe faction size.\nThis value is deducted right at the moment of death of a player.\nAfter dying, a player has a set time of protection against losing\npoints again") {
			@Override
			public String getValue() {
				return String.valueOf((int) (f.lastLostPointAtDeaths));
			}
		});
		stats.add(new FactionPointStat("Flat cost per turn", "A flat point cost which is deducted per turn") {
			@Override
			public String getValue() {
				return String.valueOf((int) (FactionPointSpendingConfig.BASIC_FLAT_COST));
			}
		});
		stats.add(new FactionPointStat("Controlled systems last turn", "Amount of systems owned by this faction") {
			@Override
			public String getValue() {
				return String.valueOf((f.clientLastTurnSytemsCount));
			}
		});
		stats.add(new FactionPointStat("Currently controlled systems list", "no description") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastSystemSectors.size());
			}
		});

		stats.add(new FactionPointStat("Last Spent On Territory (flat)", "This is a flat cost of point per owned territory") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastPointsSpendOnBaseRate);
			}
		});
		stats.add(new FactionPointStat("Last Spent On Territory (distances)", "A terrioty costs points depending on its\ndistance to the home base.\nIf the faction has no hombase, a random territory of teh faction\nis used for reference.") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastPointsSpendOnDistanceToHome);
			}
		});
		stats.add(new FactionPointStat("Last Spent On Territory (galaxy center)", "A territory costs points depending on its\ndistance to the center of the galaxy it is in.\nThis cost is less the more populated\nthat galaxy is.") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastPointsSpendOnCenterDistance);
			}
		});
		stats.add(new FactionPointStat("Last Populated Galaxy Radius", "The more total territory is taken,\nthe more this value will be.\nThis higher this value, the less\nterritory costs in this galaxy!") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastGalaxyRadius);
			}
		});
		stats.add(new FactionPointStat("Net Faction Income (without player deaths)", "Income of a faction in faction points not counting the loss of\nfaction points when players died") {
			@Override
			public String getValue() {
				return String.valueOf(f.lastGalaxyRadius);
			}
		});

	}

	private class FactionRow extends Row {


		public FactionRow(InputState state, FactionPointStat f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}


	}

}
