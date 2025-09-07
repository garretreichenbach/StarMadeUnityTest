package org.schema.game.common.controller.elements.combination.modifier;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.config.ConfigParserException;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.combination.CombinationSettings;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.BasicModifier;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.FloatBuffFormula;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.FloatNervFomula;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.SetFomula;
import org.schema.game.common.data.element.ElementCollection;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Set;

public abstract class Modifier<E extends ElementCollection, S extends CombinationSettings> {

	public static final int OLD_POWER_INDEX = 1;
	public static final int NEW_POWER_REACTOR_INDEX = 0;
	public static final String OLD_POWER_VERSION = "noreactor";
	public boolean initialized;
	public boolean loadedDual;

	public abstract void handle(E input, ControlBlockElementCollectionManager<?, ?, ?> combi, float ratio);
	
	public abstract void calcCombiSettings(S out, ControlBlockElementCollectionManager<?, ?, ?> col, ControlBlockElementCollectionManager<?, ?, ?> combi, float ratio);

	public void load(Node node, int dualIndex) throws ConfigParserException, IllegalArgumentException, IllegalAccessException {

		
		
		NodeList childNodes = node.getChildNodes();

		final Field[] fields = getClass().getDeclaredFields();

		items:
		for (int i = 0; i < childNodes.getLength(); i++) {
			boolean multiVersion = false;
			
			final Node item = childNodes.item(i);
			for (int j = 0; j < childNodes.getLength(); j++) {
				final Node otherItem = childNodes.item(j);
				if(item.getNodeType() == Node.ELEMENT_NODE && i != j && item.getNodeName().toLowerCase(Locale.ENGLISH).equals(otherItem.getNodeName().toLowerCase(Locale.ENGLISH))){
					multiVersion = true;
					loadedDual = true;
					break;
				}
			}
			boolean searching = false;
			boolean found = false;
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				searching = true;
				for (int j = 0; j < fields.length; j++) {
					fields[j].setAccessible(true);
					Annotation[] annotations = fields[j].getAnnotations();
					ConfigurationElement annotation = fields[j].getAnnotation(ConfigurationElement.class);
					if (annotation != null) {
//						System.err.println("FINDING MATCH FOR "+item.getNodeName().toLowerCase(Locale.ENGLISH)+" -> "+annotation.name().toLowerCase(Locale.ENGLISH)+" MATCH: "+item.getNodeName().toLowerCase(Locale.ENGLISH).equals(annotation.name().toLowerCase(Locale.ENGLISH))+"; multiversion: "+multiVersion+"; dual: "+(dualIndex == OLD_POWER_INDEX));
						if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals(annotation.name().toLowerCase(Locale.ENGLISH))) {
							assert (item.getAttributes().getNamedItem("style") != null) : node.getAttributes();
							String style = item.getAttributes().getNamedItem("style").getNodeValue().toLowerCase(Locale.ENGLISH);

							float value = 0;
							boolean valueSet = false;
							boolean inverse = false;
							boolean linear = false;
							Node valueItem;
							
							
							if(multiVersion){
								if ((valueItem = item.getAttributes().getNamedItem("version")) != null) {
									
									String version = valueItem.getNodeValue().toLowerCase(Locale.ENGLISH);
									if(version.equals(OLD_POWER_VERSION.toLowerCase(Locale.ENGLISH)) && dualIndex != OLD_POWER_INDEX){
										//don't overwrite new with old
										continue items;
									}
								}else if(dualIndex == OLD_POWER_INDEX){
									//dont overwrite old with new
									continue items;
								}
							}
							
							if ((valueItem = item.getAttributes().getNamedItem("value")) != null) {
								try {
									value = Float.parseFloat(valueItem.getNodeValue().toLowerCase(Locale.ENGLISH));
									valueSet = true;
								} catch (NumberFormatException e) {
									throw new ConfigParserException(node.getParentNode().getParentNode().getNodeName() + "->" + node.getParentNode().getNodeName() + "->" + node.getNodeName() + " value=... item has to be float " + node.getNodeName() + " -> " + item.getNodeName() + ", but was '" + valueItem.getNodeValue() + "'", e);
								}
							}
							if ((valueItem = item.getAttributes().getNamedItem("inverse")) != null) {
								try {
									inverse = Boolean.parseBoolean(valueItem.getNodeValue().toLowerCase(Locale.ENGLISH));
								} catch (NumberFormatException e) {
									throw new ConfigParserException(node.getParentNode().getParentNode().getNodeName() + "->" + node.getParentNode().getNodeName() + "->" + node.getNodeName() + " inverse=... item has to be boolean " + node.getNodeName() + " -> " + item.getNodeName() + ", but was '" + valueItem.getNodeValue() + "'", e);
								}
							}
							if ((valueItem = item.getAttributes().getNamedItem("linear")) != null) {
								try {
									linear = Boolean.parseBoolean(valueItem.getNodeValue().toLowerCase(Locale.ENGLISH));
								} catch (NumberFormatException e) {
									throw new ConfigParserException(node.getParentNode().getParentNode().getNodeName() + "->" + node.getParentNode().getNodeName() + "->" + node.getNodeName() + " linear=... item has to be boolean " + node.getNodeName() + " -> " + item.getNodeName() + ", but was '" + valueItem.getNodeValue() + "'", e);
								}
							}
							if (style.equals("buff")) {
								if (!valueSet) {
									throw new ConfigParserException("for style '" + style + "', a 'value' attribute has not been found (not existing or misspelled) " + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + " -> " + item.getNodeName());
								}
								fields[j].set(this, new BasicModifier(inverse, value, linear, new FloatBuffFormula()));
								found = true;
							} else if (style.equals("nerf")) {
								if (!valueSet) {
									throw new ConfigParserException("for style '" + style + "', a 'value' attribute has not been found (not existing or misspelled) " + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + " -> " + item.getNodeName());
								}
								fields[j].set(this, new BasicModifier(inverse, value, linear, new FloatNervFomula()));
								found = true;
							} else if (style.equals("set")) {
								if (!valueSet) {
									throw new ConfigParserException("for style '" + style + "', a 'value' attribute has not been found (not existing or misspelled) " + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + " -> " + item.getNodeName());
								}
								fields[j].set(this, new BasicModifier(inverse, value, linear, new SetFomula()));
								found = true;
							} else if (style.equals("skip")) {
								fields[j].set(this, new BasicModifier(false, 0, linear, null));
								found = true;
							} else {
								throw new ConfigParserException("style=... style-value is unknown: '" + style + "' (has to be either buff/nerf/set/skip) in " + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + " -> " + item.getNodeName());
							}
							assert (fields[j].get(this) != null) : fields[j].getName() + "; " + style + "; " + value;
						}
					}
				}
			}
			if (searching && !found) {

				for (Field field : fields) {
					field.setAccessible(true);
					Annotation[] annotations = field.getAnnotations();
					ConfigurationElement annotation = field.getAnnotation(ConfigurationElement.class);
					if (annotation != null) {

						System.err.println("FIELD: " + getClass().getSimpleName() + ": " + field.getName() + ": annotation: " + annotation.name());
					}
				}
				//ToDo: Remove this once additive damage is fixed
				//throw new ConfigParserException("no modifier found in game for config-tag: " + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + " -> " + item.getNodeName()+" (multi index: "+(i == OLD_POWER_INDEX ? "OLD" : "NEW")+")");
			}
		}
		Set<String> set = new ObjectOpenHashSet<String>(childNodes.getLength());
		for (int j = 0; j < fields.length; j++) {
			fields[j].setAccessible(true);
			ConfigurationElement annotations = fields[j].getAnnotation(ConfigurationElement.class);
			if (annotations != null) {
				if (fields[j].get(this) == null) {
					set.clear();
					for (int i = 0; i < childNodes.getLength(); i++) {
						final Node item = childNodes.item(i);
						if (item.getNodeType() == Node.ELEMENT_NODE) {
							if(!set.add(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
								if(item.getAttributes().getNamedItem("version") != null) {
									throw new ConfigParserException("Duplicate XML node '"+item.getNodeName()+"': " + annotations.name() + " (" + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + ")");
								}
							}
							if(item.getAttributes().getNamedItem("version") != null) {
								set.remove(item.getNodeName().toLowerCase(Locale.ENGLISH));
							}
							System.err.println("XML NODE READ: "+item.getNodeName());
						}
					}
					throw new ConfigParserException("Modifier not loaded: " + annotations.name() + " (not found in " + node.getParentNode().getParentNode().getNodeName() + " -> " + node.getParentNode().getNodeName() + " -> " + node.getNodeName() + ")");
				}
			}
		}
		initialized = true;
	}
	@Override
	public String toString(){
		StringBuffer b = new StringBuffer();
		final Field[] fields = getClass().getDeclaredFields();
		for (int j = 0; j < fields.length; j++) {
			fields[j].setAccessible(true);
			ConfigurationElement annotation = fields[j].getAnnotation(ConfigurationElement.class);
			if (annotation != null) {
				try {
					b.append(fields[j].getName()+": "+fields[j].get(this).toString()+"; \n");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return b.toString();
	}
}
