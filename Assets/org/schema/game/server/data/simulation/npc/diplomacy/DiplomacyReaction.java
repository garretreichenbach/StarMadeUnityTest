package org.schema.game.server.data.simulation.npc.diplomacy;

import java.util.Locale;

import org.schema.common.config.ConfigParserException;
import org.schema.common.util.LogInterface.LogLevel;
import org.schema.common.util.StringTools;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DiplomacyReaction {
	
	public enum ConditionReaction{
		DECLARE_WAR(""),
		OFFER_PEACE_DEAL(""),
		REMOVE_PEACE_DEAL_OFFER(""),
		OFFER_ALLIANCE(""),
		REMOVE_ALLIANCE_OFFER(""),
		ACCEPT_ALLIANCE_OFFER(""),
		ACCEPT_PEACE_OFFER(""),
		REJECT_ALLIANCE_OFFER(""),
		REJECT_PEACE_OFFER(""),
		SEND_POPUP_MESSAGE(""),
		;
		
		public final String desc;

		private ConditionReaction(String desc){
			this.desc = desc;
		}
		public static String list() {
			return StringTools.listEnum(ConditionReaction.values());
		}
	}
	public final int index;
	public DiplomacyReaction(int index){
		this.index = index;
	}
	public DiplomacyConditionGroup condition;
	public ConditionReaction reaction;
	public String name;
	public String message;
	public boolean isSatisfied(NPCDiplomacyEntity ent){
		if(condition == null){
			ent.log("DiplReaction: "+toString()+": Condition true (no condition set)", LogLevel.DEBUG);
		}
		return condition == null || condition.satisfied(ent);
	}
	
	@Override
	public String toString() {
		return name+" -> "+reaction.name();
	}

	@Override
	public int hashCode() {
		return index;
	}
	@Override
	public boolean equals(Object obj) {
		DiplomacyReaction other = (DiplomacyReaction) obj;
		if (index != other.index) {
			return false;
		}
		return true;
	}
	public static DiplomacyReaction parse(Node node, int index) throws ConfigParserException {
		DiplomacyReaction r = new DiplomacyReaction(index);
		NodeList childNodes = node.getChildNodes();
		
		for(int i = 0; i < childNodes.getLength(); i++){
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("name")){
					r.name = item.getTextContent();
				}
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("message")){
					r.message = item.getTextContent();
				}
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("action")){
					try{
						r.reaction = ConditionReaction.valueOf(item.getTextContent().toUpperCase(Locale.ENGLISH));
					}catch(Exception e){
						e.printStackTrace();
						r.message = "UNKNOWN REACTION TYPE IN CONFIG: "+item.getTextContent().toUpperCase(Locale.ENGLISH);
						r.reaction = ConditionReaction.SEND_POPUP_MESSAGE;
					}
				}
				if(DiplomacyConditionGroup.canParse(item)){
					r.condition = DiplomacyConditionGroup.parse(item);
				}
			}
		}
		r.check();
		return r;
	}

	public void appendXML(Document config, Element pp) {
		Comment comment = config.createComment(
				"Diplomacy Reaction");
		pp.appendChild(comment);
		
		Element dpl = config.createElement("Reaction");
		
		Element nElem = config.createElement("Name");
		nElem.setTextContent(name);
		
		Element nMessage = config.createElement("Message");
		nMessage.setTextContent(message != null ? message : "");
		
		Element nReact = config.createElement("Action");
		nReact.setTextContent(reaction.name());
		Comment cc = config.createComment(reaction.desc);
		nReact.appendChild(cc);
		
		dpl.appendChild(nElem);
		dpl.appendChild(nMessage);
		dpl.appendChild(nReact);
		condition.appendXML(config, dpl);
		
		pp.appendChild(dpl);
		
		
		check();
	}
	
	public void check(){
		condition.check();
	}
	
}
