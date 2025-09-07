package org.schema.game.common.staremote.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.Staremote;
import org.schema.game.common.staremote.gui.catalog.StarmoteCatalogPanel;
import org.schema.game.common.staremote.gui.entity.StarmoteEntityPanel;
import org.schema.game.common.staremote.gui.faction.StarmoteFactionPanel;
import org.schema.game.common.staremote.gui.player.StarmotePlayerPanel;
import org.schema.game.common.staremote.gui.sector.StarmoteSectorPanel;
import org.schema.schine.common.language.Lng;

public class StarmoteMainTabsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Create the panel.
	 */
	public StarmoteMainTabsPanel(GameClientState state, Staremote starmote) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);

		StarmotePlayerPanel staremotePlayerPanel = new StarmotePlayerPanel(state, starmote);
		tabbedPane.addTab(Lng.str("Players"), null, staremotePlayerPanel, null);

		StarmoteCatalogPanel staremoteCatalogPanel = new StarmoteCatalogPanel(state);
		tabbedPane.addTab(Lng.str("Catalog"), null, staremoteCatalogPanel, null);

		StarmoteEntityPanel staremoteEntityPanel = new StarmoteEntityPanel(state);
		tabbedPane.addTab(Lng.str("Entity"), null, staremoteEntityPanel, null);

		StarmoteSectorPanel staremoteSectorPanel = new StarmoteSectorPanel(state);
		tabbedPane.addTab(Lng.str("Sector"), null, staremoteSectorPanel, null);

		StarmoteFactionPanel staremoteFactionPanel = new StarmoteFactionPanel(state);
		tabbedPane.addTab(Lng.str("Faction"), null, staremoteFactionPanel, null);

	}

}
