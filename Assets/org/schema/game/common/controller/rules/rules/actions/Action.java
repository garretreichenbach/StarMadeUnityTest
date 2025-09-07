package org.schema.game.common.controller.rules.rules.actions;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.common.XMLSerializationInterface;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.RuleFieldValue;
import org.schema.game.common.controller.rules.rules.conditions.RuleFieldValueInterface;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.game.common.util.FieldUtils;
import org.schema.schine.network.TopLevelType;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class Action<A>  implements SerializationInterface, XMLSerializationInterface, RuleFieldValueInterface{
	private static byte VERSION = 0;
	public Action() {
		super();
	}
	public TopLevelType getEntityType() {
		return getType().getType();
	}
	public abstract ActionTypes getType();
	private Node createConditionElement(Document doc, Node parent) {
		Element root = doc.createElement("Action");
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
	public abstract void onTrigger(A s);
	public abstract void onUntrigger(A s);
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
			f.setAccessible(true);
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
					}else if(SerializationInterface.class.isAssignableFrom(f.getType())) {
						((SerializationInterface)f.get(this)).deserialize(b, updateSenderStateId, isOnServer);
					}else{
						f.set(this, b.readUTF());
					}
				} catch (DOMException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	public Action<A> duplicate() throws IOException {
		
		Action<A> c = (Action<A>) getType().fac.instantiateAction();
		
		FastByteArrayOutputStream fbo = new FastByteArrayOutputStream(10*1024);
		DataOutputStream sb = new DataOutputStream(fbo);
		serialize(sb, true);
		
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(fbo.array, 0, (int)fbo.position()));
		c.deserialize(in, 0, true);
		
		return c;
	}
	@Override
	public List<RuleFieldValue> createFieldValues() {
		return RuleFieldValue.create(this);
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte((byte)getType().UID);//write type
		
		
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
			f.setAccessible(true);
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
					}else if(SerializationInterface.class.isAssignableFrom(f.getType())) {
						((SerializationInterface)f.get(this)).serialize(b, isOnServer);
					}else{
						b.writeUTF(f.get(this).toString());
					}
				} catch (DOMException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	@Override
	public void parseXML(Node node) {
		assert(node.getAttributes().getNamedItem("type") != null); //confirm we are on the root node still
		NodeList childNodes = node.getChildNodes();
		List<Field> fs =FieldUtils.getAllFields(new ObjectArrayList<Field>(), getClass());
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node b = childNodes.item(i);
			
			for(Field f : fs) {
				f.setAccessible(true);
				RuleValue a = f.getAnnotation(RuleValue.class);
				if( a != null && a.tag().toLowerCase(Locale.ENGLISH).equals(b.getNodeName().toLowerCase(Locale.ENGLISH))) {
					try {
						if(f.getType() == Boolean.TYPE) {
							f.setBoolean(this, Boolean.parseBoolean(b.getTextContent()));
						}else if(f.getType() == Float.TYPE) {
							f.setFloat(this, Float.parseFloat(b.getTextContent()));
						}else if(f.getType() == Long.TYPE) {
							f.setLong(this, Long.parseLong(b.getTextContent()));
						}else if(f.getType() == Short.TYPE) {
							f.setShort(this, Short.parseShort(b.getTextContent()));
						}else if(f.getType() == Integer.TYPE) {
							f.setInt(this, Integer.parseInt(b.getTextContent()));
						}else if(f.getType() == Byte.TYPE) {
							f.setByte(this, Byte.parseByte(b.getTextContent()));
						}else if(f.getType() == Double.TYPE) {
							f.setDouble(this, Double.parseDouble(b.getTextContent()));
						}else if(ActionList.class.isAssignableFrom(f.getType())) {
							NodeList cn = b.getChildNodes();
							for(int c = 0; c < cn.getLength(); c++) {
								Node item = cn.item(c);
								if(ActionList.isActionListNode(item)) {
									((ActionList<?, ?>)f.get(this)).parseXML(item);
									break;
								}
							}
							
						}else{
							f.set(this, b.getTextContent());
						}
					} catch (DOMException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}
	
	@Override
	public Node writeXML(Document doc, Node parent) {
		Node root = createConditionElement(doc, parent);
		
		List<Field> fs =FieldUtils.getAllFields(new ObjectArrayList<Field>(), getClass());
		
		for(Field f : fs) {
			f.setAccessible(true);
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
					}else if(ActionList.class.isAssignableFrom(f.getType())) {
						node.appendChild(((ActionList<?, ?>)f.get(this)).writeXML(doc, node));
					}else{
						node.setTextContent(String.valueOf(f.get(this).toString()));
					}
				} catch (DOMException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				root.appendChild(node);
			}
		}
		
		return root;
	}

	public abstract String getDescriptionShort();

	public abstract void onTrigger(RuleEntityContainer s, TopLevelType topLevelType);

	public abstract void onUntrigger(RuleEntityContainer s, TopLevelType topLevelType);
}