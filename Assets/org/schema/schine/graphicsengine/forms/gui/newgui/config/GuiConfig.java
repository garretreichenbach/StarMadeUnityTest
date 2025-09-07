package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.XMLTools;
import org.schema.common.config.ConfigParserException;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class GuiConfig {

	public final static String configPath = 
			"." + File.separator + "data" + File.separator + 
			"config" + File.separator + "gui" + File.separator 
			;
	
	

	public GuiConfig() {
	}

	public static void load(String name) throws SAXException, IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, ConfigParserException {
		loadPath( configPath + name);
	}

	private static void loadPath(String pathOfFactionConfig) throws SAXException, IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, ConfigParserException {
		List<GuiConfig> configs = new ObjectArrayList<GuiConfig>(10);
		configs.add(new ButtonColorPalette());
		configs.add(new ListColorPalette());
		configs.add(new ChatColorPalette());
		configs.add(new PlayerStatusColorPalette());
		configs.add(new GuiDateFormats());
		configs.add(new MainWindowFramePalette());
		assert ((new FileExt(pathOfFactionConfig)).exists());
		Document doc = XMLTools.loadXML(new FileExt(pathOfFactionConfig));
		for (int i = 0; i < configs.size(); i++) {
			configs.get(i).parse(doc);
		}
	}


	protected abstract String getTag();

	public void parse(Document config) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
			org.w3c.dom.Element root = config.getDocumentElement();
			NodeList childNodesTop = root.getChildNodes();

			Field[] fields = getClass().getDeclaredFields();
//			for(Field f : fields){
//				System.err.println("READING FIELD OF "+getClass()+": "+f.getName()+"; "+(f.getAnnotations() != null ? Arrays.toString(f.getAnnotations()) : ""));
//			}

			boolean foundTop = false;
			ObjectOpenHashSet<Field> loaded = new ObjectOpenHashSet<Field>();
			for (int j = 0; j < childNodesTop.getLength(); j++) {
				Node itemTop = childNodesTop.item(j);
				if (itemTop.getNodeType() == Node.ELEMENT_NODE && itemTop.getNodeName().toLowerCase(Locale.ENGLISH).equals(getTag().toLowerCase(Locale.ENGLISH))) {
					NodeList childNodesIn = itemTop.getChildNodes();
					foundTop = true;
					for (int k = 0; k < childNodesIn.getLength(); k++) {
						Node itemIn = childNodesIn.item(k);
						if (itemIn.getNodeType() == Node.ELEMENT_NODE) {

							if (itemIn.getNodeName().toLowerCase(Locale.ENGLISH).equals("basicvalues")) {
								NodeList childNodes = itemIn.getChildNodes();
								for (int i = 0; i < childNodes.getLength(); i++) {
									Node item = childNodes.item(i);
									if (item.getNodeType() == Node.ELEMENT_NODE) {
										boolean searching = false;
										boolean found = false;
										for (Field f : fields) {
											f.setAccessible(true);
											ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
//											System.err
//											.println("1CHECKING: "+f.getName()+" with "+item.getNodeName()+"::: ");
											if (annotation != null) {
												searching = true;
//												System.err
//														.println("2CHECKING: "+annotation.name()+" with "+item.getNodeName()+"::: "+annotation.name().toLowerCase(Locale.ENGLISH).equals(item.getNodeName().toLowerCase(Locale.ENGLISH)));
												if (annotation.name().toLowerCase(Locale.ENGLISH).equals(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
													try {
														if (f.getType() == Boolean.TYPE) {
															f.setBoolean(this, Boolean.parseBoolean(item.getTextContent()));
															found = true;
														} else if (f.getType() == Integer.TYPE) {
															f.setInt(this, Integer.parseInt(item.getTextContent()));
															found = true;
														} else if (f.getType() == Short.TYPE) {
															f.setShort(this, Short.parseShort(item.getTextContent()));
															found = true;
														} else if (f.getType() == Byte.TYPE) {
															f.setByte(this, Byte.parseByte(item.getTextContent()));
															found = true;
														} else if (f.getType() == Float.TYPE) {
															f.setFloat(this, Float.parseFloat(item.getTextContent()));
															found = true;
														} else if (f.getType() == Double.TYPE) {
															f.setDouble(this, Double.parseDouble(item.getTextContent()));
															found = true;
														} else if (f.getType() == Long.TYPE) {
															f.setLong(this, Long.parseLong(item.getTextContent()));
															found = true;
														} else {

															if (f.getType().equals(Vector2f.class)) {
																String[] split = item.getTextContent().split(",");
																if (split == null || split.length != 2) {
																	throw new ConfigParserException("Must be 2 int values seperated by comma: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; ");
																}
																try {
																	Vector2f val = new Vector2f(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()));
																	f.set(this, val);
																	found = true;

																} catch (NumberFormatException e) {
																	e.printStackTrace();
																	throw new ConfigParserException("Values must be numbers: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "");
																}

															} else if (f.getType().equals(Vector4i.class)) {
																String[] split = item.getTextContent().split(",");
																if (split == null || split.length != 4) {
																	throw new ConfigParserException("Must be 4 int values seperated by comma: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "");
																}
																try {
																	Vector4i val = new Vector4i(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()), Integer.parseInt(split[3].trim()));
																	f.set(this, val);
																	found = true;

																} catch (NumberFormatException e) {
																	e.printStackTrace();
																	throw new ConfigParserException("Values must be numbers: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "");
																}

															} else if (f.getType().equals(Vector4f.class)) {
																String[] split = item.getTextContent().split(",");
																if (split == null || split.length != 4) {
																	throw new ConfigParserException("Must be 4 int values seperated by comma: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "");
																}
																try {
																	Vector4f val = new Vector4f(Float.parseFloat(split[0].trim()) / 255f, Float.parseFloat(split[1].trim()) / 255f, Float.parseFloat(split[2].trim()) / 255f, Float.parseFloat(split[3].trim()) / 255f);
																	f.set(this, val);
																	found = true;

																} catch (NumberFormatException e) {
																	e.printStackTrace();
																	throw new ConfigParserException("Values must be numbers: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "");
																}

															} else if (f.getType().equals(DateFormat.class)) {
																f.set(this, new SimpleDateFormat(item.getTextContent()));

																found = true;
															} else if (f.getType().equals(String.class)) {
																f.set(this, item.getTextContent());

																found = true;
															} else {
																throw new ConfigParserException("Cannot parse field: " + f.getName() + "; " + f.getType());
															}
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
											throw new ConfigParserException(itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": No appropriate field found for tag: " + item.getNodeName());
										}
									}
								}
							} else {
								throw new ConfigParserException("tag \"" + itemTop.getNodeName() + " -> " + itemIn.getNodeName() + "\" unknown in this context (has to be either \"BasicValues\" or \"Combinable\")");
							}
						}
					}

				}
			}

			if (!foundTop) {
				throw new ConfigParserException("Tag \"" + getTag() + "\" not found in configuation. Please create it (case insensitive)");
			}
			Annotation[] annotations = getClass().getAnnotations();
			for (Field f : fields) {
				f.setAccessible(true);
				ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);

				if (annotation != null && !loaded.contains(f)) {
					throw new ConfigParserException("virtual field '" + f.getName() + "' ('" + annotation.name() + "') not found. Please define a tag \"" + annotation.name() + "\" inside the <BasicValues> of \"" + getTag() + "\"");
				}
			}

	}

}
