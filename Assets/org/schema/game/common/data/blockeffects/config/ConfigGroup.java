package org.schema.game.common.data.blockeffects.config;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.schema.common.SerializationInterface;
import org.schema.common.util.data.DataUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ConfigGroup implements SerializationInterface, Comparable<ConfigGroup>{
	public short ntId = -1;
	public String id;
	private static short ntIdGen = 1;
	public final List<EffectConfigElement> elements = new ObjectArrayList<EffectConfigElement>();
	private static final Object synch = new Object();
	public ConfigGroup(){
	}
	public ConfigGroup(String id){
		this.id = id;
		assignNTID();
	}
	private void assignNTID(){
		synchronized(synch){
			ntId = ntIdGen++;
		}
	}
	public Node write(Document doc){
		Element grp = doc.createElement("Group");
		
		grp.setAttribute("id", id);
		for(EffectConfigElement e : elements){
			Node elem = e.write(doc);
			grp.appendChild(elem);
		}
		return grp;
	}
	public long getHash() {
		long s = 0;
		s += id.hashCode()*(elements.size()+1);
		
		Random r = new Random(s);
		
		
		
		for(int i = 0; i < elements.size(); i++){
			EffectConfigElement e = elements.get(i);
			s *= DataUtil.primes[r.nextInt(DataUtil.primes.length)]*r.nextLong()*e.addHash();
		}
		return s;
	}
	public void parse(Node node) throws IllegalArgumentException, IllegalAccessException{
		Node idItem = node.getAttributes().getNamedItem("id");
		if(idItem == null){
			throw new EffectException("No ID in "+node.getNodeName());
		}
		id = idItem.getNodeValue();
		assignNTID();
		NodeList childs = node.getChildNodes();
		
		for(int i = 0; i < childs.getLength(); i++){
			Node item = childs.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE){
				Node typeNode = item.getAttributes().getNamedItem("type");
				if(typeNode == null){
					throw new EffectException("No type in "+node.getParentNode().getNodeName()+"->"+node.getNodeName()+"");
				}
				EffectConfigElement e = new EffectConfigElement();
				String typeString = typeNode.getNodeValue();
				StatusEffectType type = StatusEffectType.valueOf(typeString.trim().toUpperCase(Locale.ENGLISH));
				if(type == null){
					throw new EffectException("Unknown type in "+node.getParentNode().getNodeName()+"->"+node.getNodeName()+": '"+typeString+"', must be "+StatusEffectType.getAll());
				}
				
				e.init(type);
				e.readConfig(item);
				elements.add(e);
			}
		}
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		ntId = stream.readShort();
		assert(ntId >= 0):ntId;
		id = stream.readUTF();
		elements.clear();
		final int size = stream.readShort();
		int readType = -1;
		try {
			for(int i = 0; i < size; i++){
				readType = stream.readByte() & 0xFF; //read unsigned
//				System.err.println("[CONFIG] DESERIALIZED CONFIG GROUP TYPE: "+readType);
				StatusEffectType type = StatusEffectType.values()[readType];
//				System.err.println("[CONFIG] DESERIALIZED CONFIG GROUP TYPE: "+readType+" -> "+type.name());
				EffectConfigElement c = new EffectConfigElement();
				c.init(type);
				c.deserialize(stream);
				elements.add(c);
			}
//			System.err.println("[CONFIG] READ EFFECTS ("+size+")");
		}catch(RuntimeException e) {
			System.err.println("'''''''''!!!!!!!!! Exception: size: "+size+"; last byte: "+readType+"; "+ntId+"; "+id);
			e.printStackTrace();
			throw(e);
		}catch(IOException e) {
			System.err.println("'''''''''!!!!!!!!! Exception: size: "+size+"; last byte: "+readType+"; "+ntId+"; "+id);
			e.printStackTrace();
			throw(e);
		}
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		assert(ntId >= 0):ntId;
		assert(id != null):id;
		buffer.writeShort(ntId);
		buffer.writeUTF(id);
		
		
		final List<EffectConfigElement> elements = new ObjectArrayList<EffectConfigElement>(this.elements);
		buffer.writeShort((short)elements.size());
		for(EffectConfigElement e : elements){
			buffer.writeByte((byte)e.getType().ordinal()); 
			e.serialize(buffer);
		}
	}
	@Override
	public int compareTo(ConfigGroup o) {
		return id.compareTo(o.id);
	}
	@Override
	public int hashCode() {
		return id.toLowerCase(Locale.ENGLISH).hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return id.toLowerCase(Locale.ENGLISH).equals(((ConfigGroup)obj).id.toLowerCase(Locale.ENGLISH));
	}
	@Override
	public String toString() {
		return "ConfigGroup["+id+"; "+ntId+"; (size: "+elements.size()+")]";
	}
	public String getEffectDescription() {
		StringBuffer sb = new StringBuffer();
		for(EffectConfigElement s : elements){
			String effectDescription = s.getEffectDescription();
			if(effectDescription.trim().length() > 0){
				sb.append(effectDescription+"\n");
			}
		}
		return sb.toString();
	}
	


}
