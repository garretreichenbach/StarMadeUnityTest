package org.schema.game.common.controller;

import java.util.Collections;
import java.util.Locale;

import org.schema.common.config.ConfigParserException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class HpConditionList  {

	private ObjectArrayList<HpCondition> list;
	private ObjectArrayList<HpCondition> listOld;
	
	public void parse(Node item) throws ConfigParserException {
		Node valueItem = item.getAttributes().getNamedItem("version");
		boolean oldPower = false;
		if(valueItem != null && valueItem.getNodeValue().toLowerCase(Locale.ENGLISH).equals("noreactor")){
			oldPower = true;
		}
		
		NodeList childNodes = item.getChildNodes();

		ObjectArrayList<HpCondition> h = new ObjectArrayList<HpCondition>();

		for (int i = 0; i < childNodes.getLength(); i++) {

			Node child = childNodes.item(i);

			if (child.getNodeType() == Node.ELEMENT_NODE) {
				HpCondition c = HpCondition.parse(child);
				h.add(c);
			}
		}
		if (h.size() > 0) {
			h.trim();
		}

		Collections.sort(h);

		if(oldPower){
			listOld = h;
		}else{
			list = h;
		}
	}
	
	public ObjectArrayList<HpCondition> get(boolean reactors){
		if(reactors || listOld == null){
			assert(list != null);
			if(list == null){
				throw new RuntimeException("HP Triggers(Reactor) missing");
			}
			return list;
		}else{
			if(listOld == null){
				throw new RuntimeException("HP Triggers(old) missing");
			}
			return listOld;
		}
	}

	public String checkString() {
		return "Reactor: "+list+";    Old: "+listOld;
	}

}
