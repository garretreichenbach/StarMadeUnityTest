package org.schema.game.common.data.player.faction.config;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.XMLTools;
import org.schema.common.config.ConfigParserException;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.elements.ReactorLevelCalcStyle;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.combination.Combinable;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class FactionConfig {

	public final static String factionConfigPath = "." + File.separator + "data" + File.separator + "config" + File.separator + "FactionConfig.xml";
	public final static String factionModConfigPath = "." + File.separator + "customFactionConfig" + File.separator + "FactionConfig.xml";
	public final static String factionConfigPathDEFAULT = "." + File.separator + "customFactionConfig" + File.separator + "FactionConfigTemplate.xml";
	public final static String factionConfigPathHOWTO = "." + File.separator + "data" + File.separator + "config" + File.separator + "customFactionConfigHOWTO.txt";
	public static ObjectOpenHashSet<Class<?>> initializedServer = new ObjectOpenHashSet<Class<?>>();
	public static ObjectOpenHashSet<Class<?>> initializedClient = new ObjectOpenHashSet<Class<?>>();
	private final boolean onServer;
	public FactionConfig(StateInterface state) {
		onServer = state instanceof ServerStateInterface;
	}

	public static void load(StateInterface state) throws SAXException, IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, ConfigParserException {
		try{
			load(state, factionConfigPath);
		}catch(IOException e){
			e.printStackTrace();
			
			File f0 = new FileExt(factionConfigPath);
			File fSave = new FileExt("." + File.separator + "customFactionConfig" + File.separator + "FactionConfigError.xml");
			FileUtil.copyFile(f0, fSave);
			
			f0.delete();
			File f1 = new FileExt(factionModConfigPath);
			f1.delete();
			
			FileUtil.copyFile(new FileExt(factionConfigPathDEFAULT), new FileExt(factionConfigPath));
			load(state, factionConfigPath);
			
			if(state instanceof GameServerState){
				//reset with default
				((GameServerState)state).setFactionConfigCheckSum(FileUtil.getSha1Checksum("./data/config/FactionConfig.xml"));
				RandomAccessFile ff = new RandomAccessFile("./data/config/FactionConfig.xml", "r");
				byte[] bcF = new byte[(int) ff.length()];
				ff.read(bcF);
				ff.close();
				((GameServerState)state).setFactionConfigFile(bcF);
			}
		}
	}

	public static void load(StateInterface state, String pathOfFactionConfig) throws IOException {
		List<FactionConfig> configs = new ObjectArrayList<FactionConfig>(10);
		configs.add(new FactionActivityConfig(state));
		configs.add(new FactionPointGalaxyConfig(state));
		configs.add(new FactionPointIncomeConfig(state));
		configs.add(new FactionPointSpendingConfig(state));
		configs.add(new FactionPointsGeneralConfig(state));
		configs.add(new FactionSystemOwnerBonusConfig(state));
		assert ((new FileExt(pathOfFactionConfig)).exists());
		Document doc = XMLTools.loadXML(new FileExt(pathOfFactionConfig));
		for (int i = 0; i < configs.size(); i++) {
			try {
				configs.get(i).parse(doc);
			} catch (IllegalArgumentException | IllegalAccessException | ConfigParserException e) {
				throw new IOException(e);
			}
		}
	}

	public boolean isOnServer() {
		return onServer;
	}

	protected abstract String getTag();

	public void parse(Document config) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		if ((onServer && !initializedServer.contains(this.getClass())) ||
				(!onServer && !initializedClient.contains(this.getClass()))) {
			org.w3c.dom.Element root = config.getDocumentElement();
			NodeList childNodesTop = root.getChildNodes();

			Field[] fields = getClass().getDeclaredFields();

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

											if (annotation != null) {
												searching = true;
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

															if (f.getType().equals(BlockEffectTypes.class)) {
																BlockEffectTypes valueOf = BlockEffectTypes.valueOf(item.getTextContent());
																if (valueOf == null) {
																	throw new ConfigParserException("[CustomFactionConfig] Cannot parse enum field: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; enum unkown (possible: " + Arrays.toString(BlockEffectTypes.values()) + ")");
																} else {
																	f.set(this, valueOf);
																	found = true;
																}

															} else if (f.getType().equals(UnitCalcStyle.class)) {
																UnitCalcStyle valueOf = UnitCalcStyle.valueOf(item.getTextContent());
																if (valueOf == null) {
																	throw new ConfigParserException("[CustomFactionConfig] Cannot parse enum field: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; enum unkown (possible: " + Arrays.toString(UnitCalcStyle.values()) + ")");
																} else {
																	f.set(this, valueOf);
																	found = true;
																}

															} else if (f.getType().equals(ReactorLevelCalcStyle.class)) {
																ReactorLevelCalcStyle valueOf = ReactorLevelCalcStyle.valueOf(item.getTextContent());
																if (valueOf == null) {
																	throw new ConfigParserException("[CustomFactionConfig] Cannot parse enum field: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; enum unkown (possible: " + Arrays.toString(UnitCalcStyle.values()) + ")");
																} else {
																	f.set(this, valueOf);
																	found = true;
																}

															} else if (f.getType().equals(String.class)) {
																String desc = item.getTextContent();
																desc = desc.replaceAll("\\r\\n|\\r|\\n", "");
																desc = desc.replaceAll("\\\\n", "\n");
																desc = desc.replaceAll("\\\\r", "\r");
																desc = desc.replaceAll("\\\\t", "\t");
																f.set(this, desc);
																found = true;
															} else {
																throw new ConfigParserException("[CustomFactionConfig] Cannot parse field: " + f.getName() + "; " + f.getType());
															}
														}
													} catch (NumberFormatException e) {
														throw new ConfigParserException("[CustomFactionConfig] Cannot parse field: " + f.getName() + "; " + f.getType() + "; with " + item.getTextContent(), e);
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
							} else if (itemIn.getNodeName().toLowerCase(Locale.ENGLISH).equals("combination")) {
								if (this instanceof Combinable) {
									((Combinable<?, ?, ?, ?>) this).getAddOn().parse(itemIn);
								} else {
									throw new ConfigParserException(itemTop.getNodeName() + " -> " + itemIn.getNodeName() + " class is not combinable " + this + ", but has combinable tag");
								}
							} else {
								throw new ConfigParserException("[CustomFactionConfig] tag \"" + itemTop.getNodeName() + " -> " + itemIn.getNodeName() + "\" unknown in this context (has to be either \"BasicValues\" or \"Combinable\")");
							}
						}
					}

				}
			}

			if (!foundTop) {
				throw new ConfigParserException("[CustomFactionConfig] Tag \"" + getTag() + "\" not found in configuation. Please create it (case insensitive)");
			}
			Annotation[] annotations = getClass().getAnnotations();
			for (Field f : fields) {
				f.setAccessible(true);
				ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);

				if (annotation != null && !loaded.contains(f)) {
					throw new ConfigParserException("[CustomFactionConfig] virtual field " + f.getName() + " (" + annotation.name() + ") not found. Please define a tag \"" + annotation.name() + "\" inside the <BasicValues> of \"" + getTag() + "\"");
				}
			}

			if (onServer) {
				initializedServer.add(getClass());
			} else {
				initializedClient.add(getClass());
			}
		} else {
			if (onServer) {
				assert (initializedServer.size() > 0);
			} else {
				assert (initializedClient.size() > 0);
			}
		}

	}

}
