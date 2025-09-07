package org.schema.game.client.view.gui.leaderboard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUILeaderboardExtendedPanel extends GUIAnchor {

	public GUILeaderboardExtendedPanel(InputState state, Entry<String, ObjectArrayList<KillerEntity>> f) {
		super(state, 420, 300);

		GUITextOverlay t0 = new GUITextOverlay(state);
		GUITextOverlay t1 = new GUITextOverlay(state);

		Object2IntAVLTreeMap<String> ships = new Object2IntAVLTreeMap<String>();
		Object2IntAVLTreeMap<String> kills = new Object2IntAVLTreeMap<String>();

		for (KillerEntity k : f.getValue()) {
			kills.put(k.deadPlayerName, kills.getInt(k.deadPlayerName) + 1);
			ships.put(k.shipName, ships.getInt(k.shipName) + 1);
		}

		Comparator<Map.Entry<String, Integer>> c = (o1, o2) -> (o2.getValue()).compareTo(o1.getValue());

		List<Map.Entry<String, Integer>> shipsSorted = new ObjectArrayList<Map.Entry<String, Integer>>(ships.entrySet());
		Collections.sort(shipsSorted, c);
		List<Map.Entry<String, Integer>> killsSorted = new ObjectArrayList<Map.Entry<String, Integer>>(kills.entrySet());
		Collections.sort(killsSorted, c);

		String shipsString = "";
		String victimString = "";

		if (shipsSorted.size() > 0) {
			shipsString += "Top 3 kills by ships used\n";
			for (int i = 0; i < Math.min(shipsSorted.size(), 3); i++) {
				shipsString += " " + (i + 1) + ". [" + shipsSorted.get(i).getValue().intValue() + "] " + shipsSorted.get(i).getKey() + "\n";
			}
		}

		if (killsSorted.size() > 0) {
			victimString += "Top 3 victims\n";
			for (int i = 0; i < Math.min(killsSorted.size(), 3); i++) {
				victimString += " " + (i + 1) + ". [" + killsSorted.get(i).getValue().intValue() + "] " + killsSorted.get(i).getKey() + "\n";
			}
		}

		t0.setTextSimple(victimString);
		t1.setTextSimple(shipsString);
		t1.getPos().x = 220;
		this.attach(t0);
		this.attach(t1);
	}

}
