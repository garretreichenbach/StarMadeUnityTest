package org.schema.game.common.controller.rules.rules.actions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.common.XMLSerializationInterface;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.ActionProvider;
import org.schema.game.common.controller.rules.rules.RuleParserException;
import org.schema.schine.network.TopLevelType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class ActionList<A, E extends Action<A>> extends ObjectArrayList<E> implements SerializationInterface, XMLSerializationInterface, ActionProvider{

	private static final long serialVersionUID = 1L;

	
	@Override
	public List<Action<?>> getActions() {
		return (List<Action<?>>) this;
	}

	@Override
	public boolean isActionAvailable() {
		return true;
	}

	public static boolean isActionListNode(Node root) {
		return root.getNodeType() == Node.ELEMENT_NODE && root.getNodeName().toLowerCase(Locale.ENGLISH).equals("actions");
	}
	
	public void onTrigger(A s) {
		for(E e : this) {
			e.onTrigger(s);
		}
	}
	public void onUntrigger(A s) {
		for(E e : this) {
			e.onUntrigger(s);
		}
	}
	
	public abstract TopLevelType getEntityType();
	@SuppressWarnings("unchecked")
	@Override
	public void parseXML(Node root) {
		
		if(isActionListNode(root)) {
			NodeList cl = root.getChildNodes();
			for(int c = 0; c < cl.getLength(); c++) {
				Node m = cl.item(c);
				if(m.getNodeType() == Node.ELEMENT_NODE) {
					Node typeN = m.getAttributes().getNamedItem("type");
					if(typeN != null) {
						int type = Integer.parseInt(typeN.getNodeValue());
						Action<?> con = ActionTypes.getByUID(type).fac.instantiateAction();
						con.parseXML(m);
						if(con.getEntityType() == getEntityType()) {
							add((E)con);
						}else {
							throw new RuleParserException("ActionList in "+root.getParentNode().getNodeType()+" contains an action of a different type: Needs: "+getEntityType().getName()+"; But was: "+con.getEntityType().getName());
						}
					}else {
						throw new RuleParserException("No type on action node "+root.getNodeName()+"; "+m.getNodeName());
					}
				}
			}
		}
		
	}

	@Override
	public Node writeXML(Document doc, Node parent) {
		Element root = doc.createElement("actions");
		root.appendChild(doc.createComment("ActionList (type: "+getEntityType().getName()+")"));
		for(Action<A> a : this) {
			root.appendChild(a.writeXML(doc, root));
		}
		return root;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeShort((short)size());
		for(Action<?> a : this) {
			a.serialize(b, isOnServer);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		int size = b.readShort();
		ensureCapacity(size);
		for(int i = 0; i < size; i++) {
			final int actionType = b.readByte();
			Action<?> a = (Action<?>)ActionTypes.getByUID(actionType).fac.instantiateAction();
			a.deserialize(b, updateSenderStateId, isOnServer);
			add((E)a);
		}
	}

	public abstract void add(Action<?> c);

}
