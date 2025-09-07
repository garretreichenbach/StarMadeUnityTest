package org.schema.game.common.controller.rules;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.common.SerializationInterface;
import org.schema.common.XMLSerializationInterface;
import org.schema.common.XMLTools;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.RuleParserException;
import org.schema.schine.network.TopLevelType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RuleSet extends ObjectArrayList<Rule> implements SerializationInterface, XMLSerializationInterface{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static byte VERSION = 0;
	public String uniqueIdentifier;
	
	public RuleSet(){
	}

	public RuleSet(RuleSet cloneFrom, String name) throws IOException {
		FastByteArrayOutputStream fbo = new FastByteArrayOutputStream(1024*1024);
		DataOutputStream sb = new DataOutputStream(fbo);
		cloneFrom.serialize(sb, true);
		
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(fbo.array, 0, (int)fbo.position()));
		deserialize(in, 0, true);
		
		this.uniqueIdentifier = name;
		assert(name != null);
		
	}
	public void assignNewIds() {
		for(Rule r : this) {
			r.assignNewId();
		}
	}
	public void checkUIDRules(RuleSetManager man) {
		for(Rule r : this) {
			r.checkUID(man);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uniqueIdentifier == null) ? 0 : uniqueIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof RuleSet)) {
			return false;
		}
		RuleSet other = (RuleSet) obj;
		if (uniqueIdentifier == null) {
			if (other.uniqueIdentifier != null) {
				return false;
			}
		} else if (!uniqueIdentifier.equals(other.uniqueIdentifier)) {
			return false;
		}
		return true;
	}

	@Override
	public void parseXML(Node node) {
		
		if(node.getAttributes().getNamedItem("version") == null) {
			throw new RuleParserException("missing version attribute on ruleset");
		}
		if(node.getAttributes().getNamedItem("id") == null) {
			throw new RuleParserException("missing id attribute on ruleset");
		}
		
		final byte version = Byte.parseByte(node.getAttributes().getNamedItem("version").getNodeValue());

		uniqueIdentifier = node.getAttributes().getNamedItem("id").getNodeValue();
		assert(uniqueIdentifier != null);
		clear();
		
		NodeList childNodes = node.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("rule")) {
					Rule r = new Rule(true);
					r.parseXML(item);
					add(r);
				}else {
					throw new RuleParserException("Node name must be 'Rule'");
				}
			}
		}
	}

	@Override
	public Node writeXML(Document doc, Node parent) {
		Node root = doc.createElement("RuleSet");
		
		Attr vAtt = doc.createAttribute("version");
		vAtt.setValue(String.valueOf(VERSION));
		root.getAttributes().setNamedItem(vAtt);
		
		Attr idAtt = doc.createAttribute("id");
		idAtt.setValue(uniqueIdentifier);
		root.getAttributes().setNamedItem(idAtt);
		for(Rule c : this) {
			Node n = c.writeXML(doc, root);
			root.appendChild(n);
		}
		
		return root;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		
	
		
		b.writeUTF(uniqueIdentifier);
		b.writeInt(size());
		for(int i = 0; i < size(); i++) {
			get(i).serialize(b, isOnServer);
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		uniqueIdentifier = b.readUTF();
		final int size = b.readInt();
		for(int i = 0; i < size; i++) {
			Rule r = new Rule(false);
			r.deserialize(b, updateSenderStateId, isOnServer);
			r.setRuleSet(this);
			add(r);
		}
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public TopLevelType getEntityType() {
		for(Rule r : this) {
			return r.getEntityType();
		}
		return TopLevelType.GENERAL;
	}

	public void export(File f) throws IOException, ParserConfigurationException, TransformerException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		Document doc = db.newDocument();
		
		List<RuleSet> l = new ObjectArrayList<RuleSet>(1);
		l.add(this);
		doc.appendChild(RuleSetManager.writeXML(doc, l));
		XMLTools.writeDocument(f, doc);
	}

	
}
