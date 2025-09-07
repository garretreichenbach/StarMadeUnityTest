package org.schema.game.server.data.simulation.npc;

import java.lang.reflect.Field;
import java.util.Locale;

import org.schema.common.config.ConfigParserException;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class NPCFactionSpawn {
		
		@ConfigurationElement(name = "RandomSpawn", description="Faction will spawn in a random star system")
		public boolean randomSpawn;
		
		@ConfigurationElement(name = "FixedSpawnSystem", description="If random spawn is off, set the location. Will switch to random if system is already taken")
		public Vector3i fixedSpawnSystem;
		
		@ConfigurationElement(name = "Name", description="Name of the faction")
		public String name;
		
		@ConfigurationElement(name = "Description", description="Description of the faction")
		public String description;
		
		@ConfigurationElement(name = "PossiblePresets", description="Possible Faction Config presets. Seperate with comma")
		public String possiblePresets;
		
		@ConfigurationElement(name = "initialGrowth", description="How many systems are initially taken")
		public int initialGrowth;

		
		
		
		public void append(Document config, Node configNode) throws IllegalArgumentException, IllegalAccessException{
			Field[] fields = getClass().getDeclaredFields();
			for (Field f : fields) {
				f.setAccessible(true);
				
				ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);

				if (annotation != null) {
					
					Element configElementNode = config.createElement(annotation.name());
					
					if(f.get(this) != null){
						if(f.getType().equals(Vector3i.class)){
							configElementNode.setTextContent(((Vector3i)f.get(this)).toStringPure());
						}else{
							configElementNode.setTextContent(f.get(this).toString());
						}
					}
					
					if(annotation.description() != null && annotation.description().trim().length() > 0){
						Comment desc = config
								.createComment(annotation.description());
						configElementNode.appendChild(desc);
					}
					configNode.appendChild(configElementNode);
				}
			}
		}
		public void parse(Node itemTop) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {

			Field[] fields = getClass().getDeclaredFields();

			boolean foundTop = false;
			ObjectOpenHashSet<Field> loaded = new ObjectOpenHashSet<Field>();
			byte version = 0;

			NodeList childNodesIn = itemTop.getChildNodes();
			foundTop = true;
			for (int k = 0; k < childNodesIn.getLength(); k++) {
				
				Node item = childNodesIn.item(k);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					boolean searching = false;
					boolean found = false;
					for (Field f : fields) {
						f.setAccessible(true);
						ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);

						if (annotation != null) {
							searching = true;
							if (annotation.name().toLowerCase(Locale.ENGLISH).equals(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
								try {
									if (f.getType() == Boolean.TYPE) {
										f.setBoolean(this, Boolean.parseBoolean(item.getTextContent().trim()));
										found = true;
									} else if (f.getType() == Integer.TYPE) {
										f.setInt(this, Integer.parseInt(item.getTextContent().trim()));
										found = true;
									} else if (f.getType() == Short.TYPE) {
										f.setShort(this, Short.parseShort(item.getTextContent().trim()));
										found = true;
									} else if (f.getType() == Byte.TYPE) {
										f.setByte(this, Byte.parseByte(item.getTextContent().trim()));
										found = true;
									} else if (f.getType() == Float.TYPE) {
										f.setFloat(this, Float.parseFloat(item.getTextContent().trim()));
										found = true;
									} else if (f.getType() == Double.TYPE) {
										f.setDouble(this, Double.parseDouble(item.getTextContent().trim()));
										found = true;
									} else if (f.getType() == Long.TYPE) {
										f.setLong(this, Long.parseLong(item.getTextContent().trim()));
										found = true;
									} else if (f.getType().equals(Vector3i.class)) {
										if(item.getTextContent().trim().length() > 0){
											f.set(this, Vector3i.parseVector3i(item.getTextContent().trim()));
										}
										found = true;
									} else if (f.getType().equals(String.class)) {
										String desc = item.getTextContent();
										desc = desc.replaceAll("\\r\\n|\\r|\\n", "");
										desc = desc.replaceAll("\\\\n", "\n");
										desc = desc.replaceAll("\\\\r", "\r");
										desc = desc.replaceAll("\\\\t", "\t");
										f.set(this, desc);
										found = true;
									} else {
										throw new ConfigParserException("Cannot parse field: " + f.getName() + "; " + f.getType());
									}
								} catch (NumberFormatException e) {
									throw new ConfigParserException("Cannot parse field: " + f.getName() + "; " + f.getType() + "; with " + item.getTextContent(), e);
								}
							}
							if (found) {
								loaded.add(f);
								break;
							}
						}
					}
					if (searching && !found) {
						assert(false):itemTop.getNodeName() + " -> " + item.getNodeName()  + ": No appropriate field found for tag: " + item.getNodeName();
						throw new ConfigParserException(itemTop.getNodeName() + " -> " + item.getNodeName()  + ": No appropriate field found for tag: " + item.getNodeName());
					}
							
				}

			}
		}
	}
