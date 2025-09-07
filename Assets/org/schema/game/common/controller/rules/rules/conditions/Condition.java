package org.schema.game.common.controller.rules.rules.conditions;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.XMLSerializationInterface;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleParserException;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.util.FieldUtils;
import org.schema.schine.network.TopLevelType;
import org.w3c.dom.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class Condition<A> implements SerializationInterface, XMLSerializationInterface, RuleFieldValueInterface{


	public static final long TRIGGER_ON_RULE_CHANGE = 1L;
	public static final long TRIGGER_ON_AI_ACTIVE_CHANGE = 2L;
	public static final long TRIGGER_ON_TIMED_CONDITION = 4;
	public static final long TRIGGER_ON_ATTACK = 8L;
	public static final long TRIGGER_ON_FACTION_CHANGE = 16L;
	public static final long TRIGGER_ON_SECTOR_ENTITIES_CHANGED = 32L;
	public static final long TRIGGER_ON_RULE_STATE_CHANGE = 64L;

	public static final long TRIGGER_ON_ALL = 0xffffffff;
	public boolean forceTrue;
	private boolean satisfied;
	private static byte VERSION = 0; 
	/**
	 * this is null unless this condition is part of a condition group
	 */
	public Condition<?> parent;
	public Condition() {
		super();
	}

	public final boolean isSatisfied() {
		return satisfied;
	}
	public void createStateChange(short conditionIndex, RuleStateChange stateChange) {
		stateChange.changeLogCond.add(satisfied ? (short)(conditionIndex +1) : (short)-(conditionIndex +1));
		
	}
	public final boolean checkSatisfied(short conditionIndex, RuleStateChange stateChange, A a, long trigger, boolean forceTrue) {
		if(isTriggeredOn(trigger)) {
			int currentLogIndex = stateChange.changeLogCond.size();
			boolean s = processCondition(conditionIndex, stateChange, a, trigger, forceTrue);
			
			/*
			 * this becomes true if any subcondition changed and log entries have been made.
			 * for it in this case we need to also insert this conditionGroup's index into
			 * the changelog. if none of the subconditions changed, there will be no log
			 * entry
			 */
			boolean conditionGroupChanged = (stateChange.changeLogCond.size()) > currentLogIndex;
			
			if(s != satisfied || conditionGroupChanged) {
				satisfied = s;
				//index is increased by 1 to avoid having 0, since they also carry their trigger status in being pos/neg 
				stateChange.changeLogCond.add(currentLogIndex, satisfied ? (short)(conditionIndex+1) : (short)-(conditionIndex+1));
				
			}
		}
		return satisfied;
	}
		
	public boolean isTriggeredOn(long what) {
		return (getTrigger() & what) != 0; //== getTrigger(); //trigger on at least one bit matching
	}
	
	public abstract long getTrigger();
	protected abstract boolean processCondition(short conditionIndex, RuleStateChange stateChange, A a, long trigger, boolean forceTrue);

	public abstract ConditionTypes getType();
	
	private Node createConditionElement(Document doc, Node parent) {
		Element root = doc.createElement("Condition");
		{
			Attr attr = doc.createAttribute("type");
			attr.setValue(String.valueOf(getType().UID));
			root.getAttributes().setNamedItem(attr);
		}
		{
			Attr attr = doc.createAttribute("version");
			attr.setValue(String.valueOf(VERSION));
			root.getAttributes().setNamedItem(attr);
		}
		
		Comment createComment = doc.createComment(getType().name());
		root.appendChild(createComment);
		return root;
	}
	
	@Override
	public List<RuleFieldValue> createFieldValues() {
		return RuleFieldValue.create(this);
	}

	@Override
	public Node writeXML(Document doc, Node parent) {
		Node root = createConditionElement(doc, parent);
		
		List<Field> fs =FieldUtils.getAllFields(new ObjectArrayList<Field>(), getClass());
		
		for(Field f : fs) {
			RuleValue a = f.getAnnotation(RuleValue.class);
			if( a != null) {
				Element node = doc.createElement(a.tag());
				try {
					if(f.getType() == Boolean.TYPE) {
						node.setTextContent(String.valueOf(f.getBoolean(this)));
					}else if(f.getType() == Float.TYPE) {
						node.setTextContent(String.valueOf(f.getFloat(this)));
					}else if(f.getType() == Long.TYPE) {
						node.setTextContent(String.valueOf(f.getLong(this)));
					}else if(f.getType() == Short.TYPE) {
						node.setTextContent(String.valueOf(f.getShort(this)));
					}else if(f.getType() == Integer.TYPE) {
						node.setTextContent(String.valueOf(f.getInt(this)));
					}else if(f.getType() == Byte.TYPE) {
						node.setTextContent(String.valueOf(f.getByte(this)));
					}else if(f.getType() == Double.TYPE) {
						node.setTextContent(String.valueOf(f.getDouble(this)));
					}else if(isEnum(f)) {
						Enum<?> e = (Enum<?>) f.get(this);
						node.setTextContent(String.valueOf(e.ordinal()));
					}else if(f.getType() == FactionRange.class){
						FactionRange s = (FactionRange)f.get(this);
						s.writeXML(doc, node); //writes text directly on node text content
					}else if(f.getType() == ConditionList.class){
						ConditionList s = (ConditionList) f.get(this);
						for(int i = 0; i < s.size(); i++) {
							Node lNode = s.get(i).writeXML(doc, node);
							node.appendChild(lNode);
						}
					}else{
						node.setTextContent(String.valueOf(f.get(this).toString()));
					}
				} catch (Exception e) {
					throw new RuleParserException(e);
				}
				root.appendChild(node);
			}
		}
		
		return root;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte((byte)getType().UID);//write type
		
		
		List<Field> fs =FieldUtils.getAllFields(new ObjectArrayList<Field>(), getClass());
		Collections.sort(fs, (o1, o2) -> {
			RuleValue a1 = o1.getAnnotation(RuleValue.class);
			RuleValue a2 = o2.getAnnotation(RuleValue.class);
			if(a1 == null && a2 == null) {
				return 0;
			}
			if(a1 == null && a2 != null) {
				return -1;
			}
			if(a1 != null && a2 == null) {
				return 1;
			}
			return a1.tag().compareTo(a1.tag());

		});
		
		for(Field f : fs) {
			RuleValue a = f.getAnnotation(RuleValue.class);
			if( a != null) {
				try {
					if(f.getType() == Boolean.TYPE) {
						b.writeBoolean(f.getBoolean(this));
					}else if(f.getType() == Float.TYPE) {
						b.writeFloat(f.getFloat(this));
					}else if(f.getType() == Long.TYPE) {
						b.writeLong(f.getLong(this));
					}else if(f.getType() == Short.TYPE) {
						b.writeShort(f.getShort(this));
					}else if(f.getType() == Integer.TYPE) {
						b.writeInt(f.getInt(this));
					}else if(f.getType() == Byte.TYPE) {
						b.writeByte(f.getByte(this));
					}else if(f.getType() == Double.TYPE) {
						b.writeDouble(f.getDouble(this));
					}else if(isEnum(f)) {
						Enum<?> e = (Enum<?>) f.get(this);
						b.writeShort(e.ordinal());
					}else if(f.getType() == FactionRange.class){
						FactionRange s = (FactionRange)f.get(this);
						b.writeInt(s.from);
						b.writeInt(s.to);
					}else if(f.getType() == ConditionList.class){
						ConditionList s = (ConditionList)f.get(this);
						
						b.writeInt(s.size());
						for(int i = 0; i < s.size(); i++) {
							s.get(i).serialize(b, isOnServer);
						}
					}else{
						b.writeUTF(f.get(this).toString());
					}
				} catch (Exception e) {
					throw new RuleParserException(e);
				}
			}
		}
	}
	
	public static boolean isEnum(Field fld) {
		Class<?> type = fld.getType();
		return type instanceof Class && ((Class<?>)type).isEnum();
	}
	@Override
	public void parseXML(Node root) {
		assert(root.getAttributes().getNamedItem("type") != null); //confirm we are on the root node still
		NodeList childNodes = root.getChildNodes();
		List<Field> fs =FieldUtils.getAllFields(new ObjectArrayList<Field>(), getClass());
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			
			for(Field f : fs) {
				RuleValue a = f.getAnnotation(RuleValue.class);
				if( a != null && a.tag().toLowerCase(Locale.ENGLISH).equals(node.getNodeName().toLowerCase(Locale.ENGLISH))) {
					try {
						if(f.getType() == Boolean.TYPE) {
							f.setBoolean(this, Boolean.parseBoolean(node.getTextContent()));
						}else if(f.getType() == Float.TYPE) {
							f.setFloat(this, Float.parseFloat(node.getTextContent()));
						}else if(f.getType() == Long.TYPE) {
							f.setLong(this, Long.parseLong(node.getTextContent()));
						}else if(f.getType() == Short.TYPE) {
							f.setShort(this, Short.parseShort(node.getTextContent()));
						}else if(f.getType() == Integer.TYPE) {
							f.setInt(this, Integer.parseInt(node.getTextContent()));
						}else if(f.getType() == Byte.TYPE) {
							f.setByte(this, Byte.parseByte(node.getTextContent()));
						}else if(f.getType() == Double.TYPE) {
							f.setDouble(this, Double.parseDouble(node.getTextContent()));
						}else if(isEnum(f)) {
							Enum[] possile = (Enum[]) f.getType().getMethod("values").invoke(null);
							f.set(this, possile[Integer.parseInt(node.getTextContent())]);
						}else if(f.getType() == FactionRange.class){
							FactionRange s = (FactionRange)f.get(this);
							s.parseXML(node);
						}else if(f.getType() == ConditionList.class){
							
							NodeList cm = node.getChildNodes();
							ConditionList s = (ConditionList)f.get(this); //is already instantiated
							s.clear();
							for(int c = 0; c < cm.getLength(); c++) {
								Node n = cm.item(c);
								
								if(n.getNodeType() == Node.ELEMENT_NODE) {
									Node tAtt = n.getAttributes().getNamedItem("type");
									
									if(tAtt != null) {
										byte type = Byte.parseByte(tAtt.getNodeValue());
										
										Condition<A> instantiateCondition = (Condition<A>)ConditionTypes.getByUID(type).fac.instantiateCondition();
										instantiateCondition.parseXML(n);
										s.add((Condition<A>) instantiateCondition);
									}else {
										throw new RuleParserException("No type attribute on Condition Node (ConditionList). Node Name: "+n.getNodeName());
									}
								}
							}
							f.set(this, s);
						}else{
							f.set(this, node.getTextContent());
						}
					} catch (Exception e) {
						throw new RuleParserException(e);
					} 
					break;
				}
			}
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		
		//type has already been read at this point to instantiate this class
		
		List<Field> fs = FieldUtils.getAllFields(new ObjectArrayList<Field>(), getClass());
		Collections.sort(fs, (o1, o2) -> {
			RuleValue a1 = o1.getAnnotation(RuleValue.class);
			RuleValue a2 = o2.getAnnotation(RuleValue.class);
			if(a1 == null && a2 == null) {
				return 0;
			}
			if(a1 == null && a2 != null) {
				return -1;
			}
			if(a1 != null && a2 == null) {
				return 1;
			}
			return a1.tag().compareTo(a1.tag());

		});
		
		for(Field f : fs) {
			RuleValue a = f.getAnnotation(RuleValue.class);
			if( a != null) {
				try {
					if(f.getType() == Boolean.TYPE) {
						f.setBoolean(this, b.readBoolean());
					}else if(f.getType() == Float.TYPE) {
						f.setFloat(this, b.readFloat());
					}else if(f.getType() == Long.TYPE) {
						f.setLong(this, b.readLong());
					}else if(f.getType() == Short.TYPE) {
						f.setShort(this, b.readShort());
					}else if(f.getType() == Integer.TYPE) {
						f.setInt(this, b.readInt());
					}else if(f.getType() == Byte.TYPE) {
						f.setByte(this, b.readByte());
					}else if(f.getType() == Double.TYPE) {
						f.setDouble(this, b.readDouble());
					}else if(isEnum(f)) {
						Enum[] possile = (Enum[]) f.getType().getMethod("values").invoke(null);
						f.set(this, possile[b.readShort()]);
					}else if(f.getType() == FactionRange.class){
						FactionRange s = (FactionRange)f.get(this);
						s.from = b.readInt();
						s.to = b.readInt();
					}else if(f.getType() == ConditionList.class){
						ConditionList s = (ConditionList)f.get(this); //is already instantiated
						s.clear();
						int size = b.readInt();
						for(int i = 0; i < size; i++) {
							byte type = b.readByte();
							Condition<A> instantiateCondition = (Condition<A>)ConditionTypes.getByUID(type).fac.instantiateCondition();
							instantiateCondition.deserialize(b, updateSenderStateId, isOnServer);
							s.add((Condition<A>) instantiateCondition);
						}
						f.set(this, s);
					}else{
						f.set(this, b.readUTF());
					}
				} catch (Exception e) {
					throw new RuleParserException(e);
				}
			}
		}
		
	}

	public abstract String getDescriptionShort();

	public Condition<A> duplicate() throws IOException {
		
		Condition<A> c = (Condition<A>) getType().fac.instantiateCondition();
		
		FastByteArrayOutputStream fbo = new FastByteArrayOutputStream(10*1024);
		DataOutputStream sb = new DataOutputStream(fbo);
		serialize(sb, true);
		
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(fbo.array, 0, (int)fbo.position()));
		c.deserialize(in, 0, true);
		
		return c;
	}

	public long calculateGroupTriggerRec(long t) {
		return getTrigger();
	}

	public TopLevelType getEntityType() {
		return getType().getType();
	}

	public int processReceivedState(int cIndex, ShortArrayList changeLogCond, short cIndexValue) {
		boolean sat = cIndexValue > 0;
		if(sat != satisfied) {
			satisfied = sat;
		}
		//only condition groups process additional indices
		return cIndex;
	}

	public void resetCondition(boolean b) {
		this.satisfied = b;
	}

	public void addToList(ConditionList all) {
		all.add(this);
	}

	
}
