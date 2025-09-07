package org.schema.game.client.view.gui;

import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class GUIHelpPanelManager extends GUIElement {

	private Object2ObjectOpenHashMap<String, GUIHelpPanel> entries = new Object2ObjectOpenHashMap<String, GUIHelpPanel>();

	public GUIHelpPanelManager(InputState state, String path) throws ParserConfigurationException, SAXException, IOException {
		super(state);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new FileExt(path));

		Transition t_Back = Transition.BACK;
		Transition t_restart = Transition.TUTORIAL_RESTART;
		Transition t_satisfied = Transition.CONDITION_SATISFIED;
		Transition t_end = Transition.TUTORIAL_END;

		Element root = doc.getDocumentElement();

		NodeList childNodes = root.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				GUIHelpPanel p = new GUIHelpPanel(state, childNodes.item(i));
				entries.put(p.getOriginalTitle(), p);
			}
		}
	}

	public void updateAll(InputState state) {
		for (GUIHelpPanel panel : entries.values()) {
			panel.update(state);
		}
	}

	public GUIHelpPanel getGeneral() {
		return entries.get("General");
	}

	public GUIHelpPanel getSelected() {
		GameClientState state = (GameClientState) getState();
		PlayerGameControlManager p = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
		if (p.getPlayerIntercationManager().getPlayerCharacterManager().isTreeActive() && !p.getPlayerIntercationManager().getPlayerCharacterManager().isSuspended()) {
			if (state.getCharacter().getGravity().isGravityOn()) {
				return entries.get("AstronautModeGravity");
			} else {
				return entries.get("AstronautModeZeroG");
			}

		}
		if (p.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActive() &&
				!p.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isSuspended()) {
			return entries.get("ShipFlightMode");
		}

		if ((p.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive() &&
				!p.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isSuspended()) ||
				(p.getPlayerIntercationManager().getSegmentControlManager().getSegmentBuildController().isTreeActive() &&
						!p.getPlayerIntercationManager().getSegmentControlManager().getSegmentBuildController().isSuspended())) {
			return entries.get("BuildMode");
		}

		if (p.getShopControlManager().isTreeActive()) {
			return entries.get("Shop");
		}
		if (p.getInventoryControlManager().isTreeActive()) {
			return entries.get("Inventory");
		}

		return null;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		Vector3f pos = new Vector3f(getPos());
		getPos().y -= getHeight();

		transform();
//		System.err.println("DRAWING AT POS: "+getPos());
		getGeneral().draw();

		GUIHelpPanel selected = getSelected();
		if (selected != null) {
			selected.setPos(getGeneral().getWidth(), 0, 0);
			selected.draw();
		}

		setPos(pos);
	}

	@Override
	public void onInit() {
		
	}

	@Override
	public float getHeight() {

		GUIHelpPanel selected = getSelected();
		if (selected != null) {
			return Math.max(selected.getHeight(), getGeneral().getHeight());
		}

		return getGeneral().getHeight();
	}

	@Override
	public float getWidth() {
		GUIHelpPanel selected = getSelected();
		if (selected != null) {
			return selected.getWidth() + getGeneral().getWidth();
		}

		return getGeneral().getWidth();

	}

}
