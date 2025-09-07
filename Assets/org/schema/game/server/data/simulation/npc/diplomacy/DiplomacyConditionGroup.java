package org.schema.game.server.data.simulation.npc.diplomacy;

import java.util.List;
import java.util.Locale;

import org.schema.common.config.ConfigParserException;
import org.schema.common.util.LogInterface.LogLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class DiplomacyConditionGroup {
	public final List<DiplomacyConditionGroup> conditions = new ObjectArrayList<DiplomacyConditionGroup>();
	public enum ConditionMod{
		AND,
		OR,
		NOT
	}
	
	public ConditionMod mod = ConditionMod.AND;
	public boolean satisfied(NPCDiplomacyEntity ent){
		if(conditions.isEmpty()){
			ent.log("No conditions in this group -> evaluated true", LogLevel.DEBUG);
			return true;
		}
		switch(mod) {
			case AND -> {
				for(DiplomacyConditionGroup g : conditions) {
					if(!g.satisfied(ent)) {
						return false;
					}
				}
				return true;
			}
			case NOT -> {
				if(conditions.size() != 1) {
					throw new IllegalArgumentException("NOT confition invalid. must be exactly one member but are: " + conditions.size());
				}
				return !conditions.get(0).satisfied(ent);
			}
			case OR -> {
				for(DiplomacyConditionGroup g : conditions) {
					if(g.satisfied(ent)) {
						return true;
					}
				}
				return false;
			}
		}
		
		throw new IllegalArgumentException("UNKNOWN CONDITION "+mod);
	}
	private static void parse(Node node, DiplomacyConditionGroup r) throws ConfigParserException {
		NodeList childNodes = node.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++){
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("condition")){
					r.conditions.add(DiplomacyCondition.parseCondition(item));
				}
				for(ConditionMod m : ConditionMod.values()){
					if(item.getNodeType() == Node.ELEMENT_NODE && 
							item.getNodeName().toLowerCase(Locale.ENGLISH).equals(m.name().toLowerCase(Locale.ENGLISH))){
						DiplomacyConditionGroup g = new DiplomacyConditionGroup();
						g.mod = m;
						parse(item, g);
						r.conditions.add(g);
					}
				}
			}
		}
	}
	public static boolean canParse(Node node) throws ConfigParserException {
		for(ConditionMod m : ConditionMod.values()){
			if(node.getNodeType() == Node.ELEMENT_NODE && 
					node.getNodeName().toLowerCase(Locale.ENGLISH).equals(m.name().toLowerCase(Locale.ENGLISH))){
				
				return true;
			}
		}
		return false;
	}
	public static DiplomacyConditionGroup parse(Node node) throws ConfigParserException {
		for(ConditionMod m : ConditionMod.values()){
			if(node.getNodeType() == Node.ELEMENT_NODE && 
					node.getNodeName().toLowerCase(Locale.ENGLISH).equals(m.name().toLowerCase(Locale.ENGLISH))){
				DiplomacyConditionGroup g = new DiplomacyConditionGroup();
				g.mod = m;
				parse(node, g);
				return g;
			}
		}
		throw new ConfigParserException("INVALID OPERATOR "+node.getNodeName());
	}
	public void check(){
		for(DiplomacyConditionGroup c : conditions){
			if(c == this){
				throw new IllegalArgumentException("CONDITION SELF REFERENCING;");
			}
			c.check();
		}
	}
	public void appendXML(Document config, Element dpl) {
		Element nCond = config.createElement(mod.toString());
		for(DiplomacyConditionGroup c : conditions){
			if(c == this){
				throw new IllegalArgumentException("CONDITION SELF REFERENCING: "+dpl.getNodeName()+"; ");
			}
			c.appendXML(config, nCond);
		}
		dpl.appendChild(nCond);
	}
}
