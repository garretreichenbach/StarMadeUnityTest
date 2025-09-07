package org.schema.schine.sound.controller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Locale;

public enum MusicTags implements MusicTag{
	EXPLORATION(3, 30, 300),
	EXPLORATION_VOID(3, 30, 300),
	BUILDING(2, 30, 300),
	BATTLE_ANY(3, 30, 300),
	BATTLE_SMALL(3, 30, 300),
	BATTLE_MEDIUM(3, 30, 300),
	BATTLE_BIG(3, 30, 300),
	PIRATE(3, 30, 300),
	ENEMY(3, 30, 300),
	VOID_ENEMY(3, 30, 300),
	STATION(1, 30, 300),
	SHOP(1, 30, 300),
	PLANET(1, 30, 300),
	MAIN_MENU(1, 30, 300), 
	HOME(1, 30, 300), 
	PIRATES(3, 30, 300), 
	TRADING_GUILD(1, 30, 300), 
	NPC_FACTION(1, 30, 300), 
	MINING(1, 30, 300),
	SALVAGING(1, 30, 300),
	;
	
	
	public static final String TAG_NAME = "MusicTag";
	public final float prio;
	public final int secsActive;
	public final int secsMaxActive;

	private MusicTags(float prio, int secsActive, int secsMaxActive) {
		this.prio = prio;
		this.secsActive = secsActive;
		this.secsMaxActive = secsMaxActive;
	}
	@Override
	public AudioTag getParent() {
		return null;
	}

	@Override
	public String getTagName() {
		return name();
	}
	@Override
	public float getPrio() {
		return prio;
	}
	@Override
	public long getMilliActive() {
		return secsActive * 1000L;
	}
	@Override
	public long getMilliMaxActive() {
		return secsMaxActive * 1000L;
	}
	@Override
	public short getTagId() {
		return (short)ordinal();
	}
	@Override
	public void parseXML(Node node) {
		assert(false):"has to use static method";
		
		
	}
	public static MusicTags parseXMLStatic(Node node) {
		assert(node.getNodeName().toLowerCase(Locale.ENGLISH).equals(TAG_NAME.toLowerCase(Locale.ENGLISH))):node.getTextContent();
		try {
			return valueOf(node.getTextContent().toUpperCase(Locale.ENGLISH));
		} catch(Exception ignored) { //Some audio configs don't have music tags
			return null;
		}
	}
	@Override
	public Node writeXML(Document doc, Node parent) {
		Element elem = doc.createElement(TAG_NAME);
		elem.setTextContent(name());
		parent.appendChild(elem);
		return parent;
	}

}
