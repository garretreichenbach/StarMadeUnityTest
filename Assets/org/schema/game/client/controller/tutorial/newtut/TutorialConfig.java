package org.schema.game.client.controller.tutorial.newtut;

import api.common.GameClient;
import api.utils.textures.StarLoaderTexture;
import api.utils.textures.TextureSwapper;
import org.schema.common.XMLSerializationInterface;
import org.schema.common.XMLTools;
import org.schema.common.util.ZipUtils;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIScrollableStringTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class TutorialConfig implements XMLSerializationInterface {

	private static final HashMap<String, GUIAnchor> imageCache = new HashMap<>();
	private static final HashMap<TutorialConfig, String> tutorials = new HashMap<>();
	public final String displayName;
	private final List<GUIElement> elements = new ArrayList<>();
	private static boolean parsedBlockConfig;

	private TutorialConfig(File tutorialFile) throws Exception {
		displayName = getDisplayName(tutorialFile);
		File tempFile = doVariableReplacements(tutorialFile);
		parseXML(XMLTools.loadXML(tempFile).getFirstChild());
		tempFile.delete();
	}

	private record ReplacementData(String className, String identifier, boolean isMethod, String... args) {
		public boolean isField() {
			return !isMethod;
		}

		public boolean isMethod() {
			return isMethod;
		}

		public String runReplacement() {
			try {
				Class<?> clazz = Class.forName(className);
				if(isField()) {
					Field field = clazz.getField(identifier);
					return field.get(null).toString();
				} else {
					Object result;
					if(args == null || args.length == 0) result = clazz.getMethod(identifier).invoke(null);
					else {
						Class<?>[] argTypes = new Class[args.length];
						Object[] argValues = new Object[args.length];
						for(int i = 0; i < args.length; i++) {
							String arg = args[i];
							argTypes[i] = String.class;
							argValues[i] = arg;
						}
						result = clazz.getMethod(identifier, argTypes).invoke(null, argValues);
					}
					if(result != null) return String.valueOf(result);
					else return "";
				}
			} catch(Exception exception) {
				exception.printStackTrace();
				return "Error: " + exception.getMessage();
			}
		}

		@Override
		public String toString() {
			if(isMethod) return "${" + className + ":" + identifier + "(" + String.join(", ", args) + ")}";
			else return "${" + className + ":" + identifier + "}";
		}
	}

	/**
	 * Goes through each line of the document, replacing any $variables with the specified value, and creating a temporary copy of the file (with the replacements substituted) that can be parsed by the XML parser.
	 * <br/>Field Example:
	 * <br/>${org.schema.game.client.controller.tutorial.newtut.TutorialMenu:myValue} will be replaced with the value of the static field myValue in the class TutorialMenu.
	 * <br/>Method Example:
	 * <br/>${org.schema.game.client.controller.tutorial.newtut.TutorialMenu:myMethod()} will be replaced with the value returned by the static method myMethod in the class TutorialMenu.
	 * <br/>If arguments are needed, you can specify values in the parentheses or even other variables as long as they are all strings. Also, the return type of the method must be either String or void.
	 * <br/>Example:
	 * <br/>${org.schema.game.client.controller.tutorial.newtut.TutorialMenu:myMethod(5, "Hello", ${org.schema.game.client.controller.tutorial.newtut.TutorialMenu:myValue})} will be replaced with the value returned by the static method myMethod in the class TutorialMenu with the arguments 5, "Hello", and the value of the static field myValue.
	 * <br/>If the method returns void, the method will still be run but the variable will be replaced with an empty string.
	 *
	 * @param tutorialFile the file to parse
	 * @return a temporary file with the variable replacements
	 */
	public static File doVariableReplacements(File tutorialFile) {
		File tempFile = null;
		try {
			tempFile = new File(tutorialFile.getParent(), tutorialFile.getName().substring(0, tutorialFile.getName().lastIndexOf('.')) + "_temp.xml");
			tempFile.createNewFile();
			FileReader reader = new FileReader(tutorialFile, StandardCharsets.UTF_8);
			StringBuilder fileContents = new StringBuilder();
			int character;
			while((character = reader.read()) != -1) {
				fileContents.append((char) character);
			}
			reader.close();
			String[] lines = fileContents.toString().split("\n");
			StringBuilder newFileContents = new StringBuilder();
			ArrayList<ReplacementData> replacements = new ArrayList<>();
			for(String line : lines) {
				if(line.contains("${")) {
					String[] parts = line.split("\\$\\{");
					for(String part : parts) {
						if(part.contains("}")) {
							String[] replacement = part.split("}");
							String identifier = replacement[0];
							String[] args = new String[0];
							boolean isMethod = identifier.contains("(");
							if(identifier.contains("(")) {
								//If the arguments are empty, the split will return an array with one empty string, so we need to check for that
								String argsString = identifier.substring(identifier.indexOf('(') + 1, identifier.lastIndexOf(')')).trim();
								if(!argsString.isBlank()) args = argsString.split(", ");
								else args = null;
								identifier = identifier.substring(0, identifier.indexOf('('));
							}
							String className = identifier.substring(0, identifier.lastIndexOf(':'));
							identifier = identifier.substring(identifier.lastIndexOf(':') + 1);
							if(args != null && args.length > 0) replacements.add(new ReplacementData(className, identifier, isMethod, args));
							else replacements.add(new ReplacementData(className, identifier, isMethod));
						}
					}
				}
				for(ReplacementData entry : replacements) {
					String replacement = entry.runReplacement();
					line = line.replace(entry.toString(), replacement);
				}
				newFileContents.append(line).append("\n");
			}
			FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8);
			writer.write(newFileContents.toString());
			writer.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return tempFile;

	}

	public static String getDisplayName(File file) {
		String name = file.getName();
		name = name.substring(0, name.lastIndexOf('.'));
		name = name.trim().replaceAll("_", " ");
		//Capitalize first letter of each word
		String[] words = name.split(" ");
		StringBuilder displayName = new StringBuilder();
		for(String word : words) {
			displayName.append(word.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(word.substring(1)).append(" ");
		}
		return displayName.toString().trim();
	}

	public static List<TutorialConfig> getTutorials() {
		if(tutorials.isEmpty()) loadTutorials();
		List<TutorialConfig> orderedTutorials = new ArrayList<>(tutorials.keySet());
		orderedTutorials.sort(Comparator.comparingInt(tutorial -> {
			for(VanillaTutorials category : VanillaTutorials.values()) {
				if(category.getDisplayName().equals(tutorial.displayName)) {
					int categories = VanillaTutorials.values().length;
					int ordinal = category.ordinal();
					return (categories - ordinal) * -1;
				}
			}
			return 1;
		}));
		return orderedTutorials;
	}

	public static void addTutorial(TutorialConfig tutorial) {
		tutorials.put(tutorial, tutorial.displayName);
	}

	private static void parseBlockConfig() {
		try {
			(new Ship(GameClient.getClientState())).getManagerContainer().reparseBlockBehavior(true);
			parsedBlockConfig = true;
		} catch(Exception exception) {
			exception.printStackTrace();
			parsedBlockConfig = false;
		}
	}

	/**
	 * Loads the Vanilla tutorials. Mods should use {@link #addTutorial(TutorialConfig)} to add their tutorials.
	 */
	private static void loadTutorials() {
		if(!parsedBlockConfig) parseBlockConfig();
		if(parsedBlockConfig) {
			for(VanillaTutorials category : VanillaTutorials.values()) {
				TutorialConfig tutorial = category.getConfig();
				if(tutorial != null) tutorials.put(tutorial, category.displayName);
			}
			File tutorialsFolder = new FileExt("./data/tutorial/");
			if(!tutorialsFolder.exists()) { //This should be handled earlier by loading the vanilla ones, but just in case...
				throw new IllegalStateException("Tutorials folder does not exist!");
			}
			File[] tutorialFiles = tutorialsFolder.listFiles();
			for(File tutorialFolder : tutorialFiles) {
				try {
					File tutorialFile = new File(tutorialFolder, tutorialFolder.getName() + ".xml");
					if(!VanillaTutorials.isVanilla(tutorialFile)) {
						TutorialConfig tutorial = new TutorialConfig(tutorialFile);
						tutorials.put(tutorial, tutorial.displayName);
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		} else throw new IllegalStateException("Failed to parse block config!");
	}

	public TutorialConfig parse(File tutorialFile) {
		try {
			return new TutorialConfig(tutorialFile);
		} catch(Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	@Override
	public void parseXML(Node root) {
		StarLoaderTexture.runOnGraphicsThread(() -> { //Todo: Images should be loaded earlier than this, as it causes a brief (but still noticeable) freeze when opening the tutorial menu
			for(int i = 0; i < root.getChildNodes().getLength(); i++) {
				try {
					Node elementNode = root.getChildNodes().item(i);
					GUIElement element = ElementType.buildElement(elementNode);
					if(element != null) elements.add(element);
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
	}

	@Override
	public Node writeXML(Document doc, Node root) {
		return null;
	}

	public List<GUIElement> getElements() {
		return elements;
	}

	/**
	 * Vanilla tutorial categories. These are displayed first, in order, before any mod tutorials.
	 */
	public enum VanillaTutorials {
		INTRODUCTION("Introduction"),
		NAVIGATION("Navigation"),
		ADVANCED_BUILD_MODE("Advanced Build Mode"),
		REACTORS("Reactors"),
		CHAMBERS("Chambers"),
		WEAPONS("Weapons"),
		SHIELDS("Shields"),
		ARMOR("Armor"),
		COMBAT("Combat"),
		RESOURCES("Resources"),
		MANUFACTURING("Manufacturing"),
		FLEETS("Fleets"),
		STATIONS("Space Stations"),
		SHIPYARDS("Shipyards"),
		FACTIONS("Factions"),
		LOGIC("Logic");

		private final String displayName;

		VanillaTutorials(String displayName) {
			this.displayName = displayName;
		}

		public static boolean isVanilla(File tutorialFile) {
			for(VanillaTutorials category : values()) {
				if(category.getConfig() != null) {
					if(Objects.equals(category.getFile(), tutorialFile)) return true;
				}
			}
			return false;
		}

		public String getDisplayName() {
			return displayName;
		}

		public TutorialConfig getConfig() {
			File tutorialsFolder = new FileExt("./data/tutorial/");
			if(!tutorialsFolder.exists()) {
				File tutorialsZip = new FileExt("./data/tutorial.zip");
				if(tutorialsZip.exists()) ZipUtils.unzip(tutorialsZip.getAbsolutePath(), tutorialsFolder.getAbsolutePath());
				else throw new IllegalStateException("Tutorials folder does not exist and no tutorials zip found!");
			}
			File[] tutorialFiles = tutorialsFolder.listFiles();
			for(File tutorialFolder : tutorialFiles) {
				try {
					File tutorialFile = new File(tutorialFolder, tutorialFolder.getName() + ".xml");
					if(!tutorialFile.exists()) continue;
					String tutorialName = tutorialFile.getName().substring(0, tutorialFile.getName().lastIndexOf('.')).toLowerCase(Locale.ENGLISH).trim();
					if(tutorialName.contains(name().toLowerCase(Locale.ENGLISH).trim())) {
						return new TutorialConfig(tutorialFile);
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
			return null;
		}

		private File getFile() {
			File tutorialsFolder = new FileExt("./data/tutorial/");
			File[] tutorialFiles = tutorialsFolder.listFiles();
			for(File tutorialFolder : tutorialFiles) {
				try {
					File tutorialFile = new File(tutorialFolder, tutorialFolder.getName() + ".xml");
					if(!tutorialFile.exists()) continue;
					String tutorialName = tutorialFile.getName().substring(0, tutorialFile.getName().lastIndexOf('.')).toLowerCase(Locale.ENGLISH).trim();
					if(tutorialName.contains(name().toLowerCase(Locale.ENGLISH).trim())) {
						return tutorialFile;
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
			return null;
		}
	}

	public enum ElementType {
		TITLE(elementNode -> {
			GUITextOverlay title = new GUITextOverlay(FontLibrary.FontSize.getUncached(40), getState());
			String text = elementNode.getTextContent().replaceAll("#text: ", "").trim();
			title.setTextSimple(text);
			title.onInit();
			title.wrapSimple = true;
			title.autoHeight = true;
			title.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return title;

		}),
		HEADER_1(elementNode -> {
			GUITextOverlay header = new GUITextOverlay(FontLibrary.FontSize.BIG_30, getState());
			String text = elementNode.getTextContent().replaceAll("#text: ", "").trim();
			header.setTextSimple(text);
			header.onInit();
			header.wrapSimple = true;
			header.autoHeight = true;
			header.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return header;
		}),
		HEADER_2(elementNode -> {
			GUITextOverlay header = new GUITextOverlay(FontLibrary.FontSize.BIG_24, getState());
			String text = elementNode.getTextContent().replaceAll("#text: ", "").trim();
			header.setTextSimple(text);
			header.onInit();
			header.wrapSimple = true;
			header.autoHeight = true;
			header.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return header;
		}),
		HEADER_3(elementNode -> {
			GUITextOverlay header = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_18, getState());
			String text = elementNode.getTextContent().replaceAll("#text: ", "").trim();
			header.setTextSimple(text);
			header.onInit();
			header.wrapSimple = true;
			header.autoHeight = true;
			header.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return header;
		}),
		PARAGRAPH(elementNode -> {
			GUITextOverlay paragraph = new GUITextOverlay(FontLibrary.FontSize.SMALL_14, getState());
			String text = elementNode.getTextContent().replaceAll("#text: ", "").trim();
			paragraph.setTextSimple(text);
			paragraph.onInit();
			paragraph.wrapSimple = true;
			paragraph.autoHeight = true;
			paragraph.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return paragraph;
		}),
		BREAK(elementNode -> {
			int lines = 1;
			try {
				lines = Integer.parseInt(elementNode.getTextContent());
			} catch(Exception ignored) {
			}
			GUITextOverlay lineBreak = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, getState());
			lineBreak.setTextSimple("\n".repeat(Math.max(0, lines)));
			lineBreak.onInit();
			lineBreak.wrapSimple = true;
			lineBreak.autoHeight = true;
			lineBreak.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return lineBreak;
		}),
		DIVIDER(elementNode -> {
			GUITextOverlay divider = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, getState());
			divider.setTextSimple("_".repeat(105));
			divider.onInit();
			divider.wrapSimple = true;
			divider.autoHeight = true;
			divider.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			return divider;
		}),
		TABLE(elementNode -> {
			//Table format:
			//<Table>
			//	<Row>Header 1, Header 2, Header 3</Row>
			//	<Row>Data 1, Data 2, Data 3</Row>
			//</Table>

			//Get headers first
			Node headersNode = elementNode.getFirstChild().getNextSibling();
			String[] headers = headersNode.getTextContent().split(", ");
			//Remove empty headers
			headers = Arrays.stream(headers).filter(header -> !header.trim().isEmpty()).toArray(String[]::new);

			//Get the data
			ArrayList<String[]> data = new ArrayList<>();
			for(int i = 0; i < elementNode.getChildNodes().getLength(); i++) {
				Node rowNode = elementNode.getChildNodes().item(i);
				if(rowNode == headersNode) continue; //Skip headers (already processed)
				String[] row = rowNode.getTextContent().split(", ");
				//Remove empty data
				row = Arrays.stream(row).filter(dataPoint -> !dataPoint.trim().isEmpty()).toArray(String[]::new);
				//Remove empty rows
				if(row.length == 0) continue;
				data.add(row);
			}

			GUIAnchor anchor = new GUIAnchor(getState());
			GUIScrollableStringTableList table = new GUIScrollableStringTableList(getState(), anchor, new ArrayList<>(List.of(headers)), data);
			anchor.attach(table);
			table.onInit();
			table._getScrollPanel().setScrollable(GUIScrollablePanel.SCROLLABLE_NONE);
			return anchor;
		}),
		IMAGE(elementNode -> {
			Node imgNode = elementNode.getFirstChild();
			String path = imgNode.getTextContent();
			String name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')); //Get the name of the image
			if(imageCache.containsKey(name)) return imageCache.get(name);
			else {
				File file = new FileExt(path);
				if(!file.exists()) throw new Exception("File does not exist: " + path);
				BufferedImage image = ImageIO.read(file);
				int desiredWidth = 720;
				int desiredHeight = 480;
				BufferedImage scaledImage = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
				scaledImage.getGraphics().drawImage(image, 0, 0, desiredWidth, desiredHeight, null);
				image = scaledImage;
				Sprite sprite = new Sprite(TextureSwapper.getTextureFromImage(image, name, false, false));
				GUIOverlay overlay = new GUIOverlay(sprite, getState());
				GUIAnchor anchor = new GUIAnchor(getState(), desiredWidth, desiredHeight);
				anchor.attach(overlay);
				imageCache.put(name, anchor);
				return overlay;
			}
		}),
		BUTTON(elementNode -> {
			//<Button width=100 height=24 palette=palette callback=callbackMethod>Text</Button>
			int width = 100;
			int height = UIScale.getUIScale().smallButtonHeight;
			GUITextButton.ColorPalette palette = GUITextButton.ColorPalette.OK;
			GUICallback callback = new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					//Do nothing
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			};
			if(elementNode.hasAttributes()) {
				if(elementNode.getAttributes().getNamedItem("width") != null) width = Integer.parseInt(elementNode.getAttributes().getNamedItem("width").getTextContent());
				if(elementNode.getAttributes().getNamedItem("height") != null) height = Integer.parseInt(elementNode.getAttributes().getNamedItem("height").getTextContent());
				if(elementNode.getAttributes().getNamedItem("palette") != null) palette = GUITextButton.ColorPalette.valueOf(elementNode.getAttributes().getNamedItem("palette").getTextContent().toUpperCase(Locale.ENGLISH));
				//Should specify a full static method identifier that can be fetched using reflection
				//Example: "org.schema.game.client.controller.tutorial.newtut.TutorialMenu:callbackMethod"
				if(elementNode.getAttributes().getNamedItem("callback") != null) {
					String callbackMethod = elementNode.getAttributes().getNamedItem("callback").getTextContent();
					//Lookup class
					Class<?> clazz = Class.forName(callbackMethod.substring(0, callbackMethod.lastIndexOf(':')));
					//Lookup method
					String methodName = callbackMethod.substring(callbackMethod.lastIndexOf(':') + 1);
					callback = new GUICallback() {
						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							try {
								clazz.getMethod(methodName, GUIElement.class, MouseEvent.class).invoke(null, callingGuiElement, event);
							} catch(Exception exception) {
								exception.printStackTrace();
								System.err.println("Error invoking callback method from tutorial xml: " + callbackMethod + "\nThis is probably due to an invalid or non-static identifier. Format:\n fully.qualified.class.name:methodName");
							}
						}

						@Override
						public boolean isOccluded() {
							return false;
						}
					};
				}
			}
			GUITextButton button = new GUITextButton(getState(), UIScale.getUIScale().scale(width), UIScale.getUIScale().scale(height), palette, elementNode.getTextContent(), callback);
			button.onInit();
			return button;
		}),
		CHECKBOX(elementNode -> {
			FontLibrary.FontInterface font = FontLibrary.FontSize.MEDIUM_15;
			String text = "";
			CheckBoxCallback checkBoxCallback = (checkbox, toggle) -> {
			};
			if(elementNode.hasAttributes()) {
				if(elementNode.getAttributes().getNamedItem("font") != null) {
					font = FontLibrary.FontSize.getUncached(Integer.parseInt(elementNode.getAttributes().getNamedItem("font").getTextContent()));
				}
				if(elementNode.getAttributes().getNamedItem("callback") != null) {
					String callbackMethod = elementNode.getAttributes().getNamedItem("callback").getTextContent();
					Class<?> clazz = Class.forName(callbackMethod.substring(0, callbackMethod.lastIndexOf(':')));
					String methodName = callbackMethod.substring(callbackMethod.lastIndexOf(':') + 1);
					checkBoxCallback = (checkbox, toggle) -> {
						try {
							clazz.getMethod(methodName, GUICheckBoxTextPairNew.class, boolean.class).invoke(null, checkbox, toggle);
						} catch(Exception exception) {
							exception.printStackTrace();
							System.err.println("Error invoking callback method from tutorial xml: " + callbackMethod + "\nThis is probably due to an invalid or non-static identifier. Format:\n fully.qualified.class.name:methodName");
						}
					};
				}
			}
			text = elementNode.getTextContent().replaceAll("#text: ", "").trim();

			CheckBoxCallback finalCheckBoxCallback = checkBoxCallback;
			GUICheckBoxTextPairNew checkBox = new GUICheckBoxTextPairNew(getState(), text, font) {
				private boolean active;

				@Override
				public void activate() {
					active = true;
					finalCheckBoxCallback.onToggle(this, true);
				}

				@Override
				public void deactivate() {
					active = false;
					finalCheckBoxCallback.onToggle(this, false);
				}

				@Override
				public boolean isChecked() {
					return active;
				}
			};
			checkBox.onInit();
			return checkBox;
		}),
		SLIDER(elementNode -> {
			int scrollMode = GUIScrollablePanel.SCROLLABLE_HORIZONTAL;
			int width = 100;
			FontLibrary.FontInterface font = FontLibrary.FontSize.MEDIUM_15;
			float maxSetting = 10;
			float minSetting = 0;
			float defaultSetting = 0;

			if(elementNode.hasAttributes()) {
				if(elementNode.getAttributes().getNamedItem("scroll") != null) scrollMode = elementNode.getAttributes().getNamedItem("scroll").getTextContent().toLowerCase(Locale.ENGLISH).trim().contains("vertical") ? GUIScrollablePanel.SCROLLABLE_VERTICAL : GUIScrollablePanel.SCROLLABLE_HORIZONTAL;
				if(elementNode.getAttributes().getNamedItem("width") != null) width = Integer.parseInt(elementNode.getAttributes().getNamedItem("width").getTextContent());
				if(elementNode.getAttributes().getNamedItem("font") != null) font = FontLibrary.FontSize.getUncached(Integer.parseInt(elementNode.getAttributes().getNamedItem("font").getTextContent()));
				if(elementNode.getAttributes().getNamedItem("max") != null) maxSetting = Float.parseFloat(elementNode.getAttributes().getNamedItem("max").getTextContent());
				if(elementNode.getAttributes().getNamedItem("min") != null) minSetting = Float.parseFloat(elementNode.getAttributes().getNamedItem("min").getTextContent());
				if(elementNode.getAttributes().getNamedItem("default") != null) defaultSetting = Float.parseFloat(elementNode.getAttributes().getNamedItem("default").getTextContent());
				else defaultSetting = minSetting;
			}

			if(maxSetting < minSetting) throw new IllegalArgumentException("Max setting must not be less than min setting!");
			if(defaultSetting < minSetting) throw new IllegalArgumentException("Default setting must not be less than min setting!");
			if(defaultSetting > maxSetting) throw new IllegalArgumentException("Default setting must not be greater than max setting!");

			String label = elementNode.getTextContent().replaceAll("#text: ", "").trim();
			int finalScrollMode = scrollMode;
			float finalMaxSetting = maxSetting;
			float finalMinSetting = minSetting;
			GUIScrollSettingSelector slider = new GUIScrollSettingSelector(getState(), finalScrollMode, width, font) {

				private float setting;

				@Override
				public boolean isVerticalActive() {
					return finalScrollMode == GUIScrollablePanel.SCROLLABLE_VERTICAL;
				}

				@Override
				protected void decSetting() {
					setting--;
				}

				@Override
				protected void incSetting() {
					setting++;
				}

				@Override
				protected float getSettingX() {
					return setting;
				}

				@Override
				protected void setSettingX(float value) {
					setting = value;
				}

				@Override
				protected float getSettingY() {
					return 0;
				}

				@Override
				protected void setSettingY(float value) {

				}

				@Override
				public float getMaxX() {
					return finalMaxSetting;
				}

				@Override
				public float getMaxY() {
					return 0;
				}

				@Override
				public float getMinX() {
					return finalMinSetting;
				}

				@Override
				public float getMinY() {
					return 0;
				}
			};
			slider.setNameLabel(label);
			slider.onInit();
			return slider;
		}),
		CUSTOM(elementNode -> {
			//Custom defined element using classpath
			//<Custom classpath="org.schema.game.client.controller.tutorial.newtut.CustomTutorialElement(args)"/>
			if(elementNode.hasAttributes()) {
				Node classpathNode = elementNode.getAttributes().getNamedItem("classpath");
				if(classpathNode != null) {
					String classPath = classpathNode.getTextContent().substring(0, classpathNode.getTextContent().indexOf('(')).trim();
					Class<?> clazz = Class.forName(classPath);
					String[] args = new String[0];
					if(classpathNode.getTextContent().contains("(")) {
						String argsString = classpathNode.getTextContent().substring(classpathNode.getTextContent().indexOf('(') + 1, classpathNode.getTextContent().lastIndexOf(')')).trim();
						if(!argsString.isBlank()) args = argsString.split(", ");
						else args = null;
					}
					if(GUIAnchor.class.isAssignableFrom(clazz)) {
						GUIAnchor element;
						if(args == null) element = (GUIAnchor) clazz.getConstructor(InputState.class).newInstance(getState());
						else element = (GUIAnchor) clazz.getConstructor(InputState.class, String[].class).newInstance(getState(), args);
						element.onInit();
						return element;
					} else throw new Exception("Class does not extend GUIAnchor");
				}
			}
			return null;
		});

		private final GUIElementParser elementBuilder;

		ElementType(GUIElementParser elementBuilder) {
			this.elementBuilder = elementBuilder;
		}

		/**
		 * Builds a GUIElement object based on the given elementNode.
		 *
		 * @param elementNode the XML node representing the GUI element
		 * @return the GUIElement object built from the elementNode
		 */
		public static GUIElement buildElement(Node elementNode) {
			String nodeName = elementNode.getNodeName().toUpperCase(Locale.ENGLISH).trim();
			nodeName = nodeName.replaceAll("#text: ", "").trim();
			nodeName = nodeName.replaceAll("#TEXT", "").trim(); //Fix for some nodes that have #TEXT in the name (not sure why this happens, but it does)
			if(nodeName.isEmpty()) return null;
			ElementType type = valueOf(nodeName);
			try {
				GUIElement element = type.elementBuilder.parseElement(elementNode);
				if(element == null) throw new NullPointerException("Element parser returned null for " + nodeName + "!");
				else return element;
			} catch(Exception exception) {
				exception.printStackTrace();
				String message = "Error: Failed to parse element " + elementNode.getNodeName() + " likely due to invalid formatting.";
				System.err.println(message);
				GUITextOverlay error = new GUITextOverlay(FontLibrary.FontSize.SMALL_14, getState());
				error.setTextSimple(message);
				error.onInit();
				error.setColor(Color.RED);
				return error;
			}
		}

		private static InputState getState() {
			return GameClient.getClientState();
		}
	}

	private interface GUIElementParser {
		GUIElement parseElement(Node elementNode) throws Exception;
	}

	public interface CheckBoxCallback {
		void onToggle(GUICheckBoxTextPairNew checkbox, boolean toggle);
	}

}
