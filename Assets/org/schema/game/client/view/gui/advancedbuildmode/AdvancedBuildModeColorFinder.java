package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.common.util.data.DataUtil;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.*;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.TexturePack;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIColorPickerRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;

import javax.imageio.ImageIO;
import javax.vecmath.Vector4f;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Advanced build mode tool for color palette.
 * </br>This allows players to select a color from a color pane, and the game will create a list of blocks that are
 * closest to the selected color.
 *
 * @author TheDerpGamer
 */
public class AdvancedBuildModeColorFinder extends AdvancedBuildModeGUISGroup {

	private static final int SCALE_DOWN = 0;
	private static final int AVERAGE = 1; //This costs more performance, but is more accurate
	private static final int MODE = SCALE_DOWN;
	private static final int RES = 64; // Resolution of the block textures to sample for color averaging

	private static final float BORDER_DIFFERENCE_THRESHOLD = 1.0f; // Threshold to determine if a color is close enough to be considered a border color
	private static final String LIGHT_GREY_BORDER = "#707070"; //If we are close to this color, we consider it a light border
	private static final String DARK_GREY_BORDER = "#272727"; //If we are close to this color, we consider it a dark border

	private static final HashMap<String, Short[]> blockCategories = new HashMap<>();
	private final HashSet<Short> blockTypes = new HashSet<>(); // Set to store all blocks we can sample from
	private final HashMap<Short, Vector4f> cachedBlockColors = new HashMap<>(); // Map to store block types and their average colors
	private final HashSet<Short> closestColors = new HashSet<>();
	private final List<GUIAdvBlockDisplay> displays = new ArrayList<>();
	private final BufferedImage[] textureSheets = new BufferedImage[4]; // Array to hold the texture sheets for the blocks
	private final String selectedCategory = "Any"; // Default category to show all blocks
	private GUIAdvTextBar textBar;
	private GUIAdvSlider saturationSlider;
	private GUIAdvSlider brightnessSlider;

	public AdvancedBuildModeColorFinder(AdvancedGUIElement e) {
		super(e);
		doSetup();
	}

	/**
	 * Initializes the texture sheets and calculates the average colors for all blocks.
	 */
	private void doSetup() {
		TexturePack pack = (TexturePack) EngineSettings.G_TEXTURE_PACK.getObject();
		try {
			textureSheets[0] = ImageIO.read(new File(DataUtil.dataPath + "./textures/block/" + pack.name + "/" + RES + "/t000.png"));
			textureSheets[1] = ImageIO.read(new File(DataUtil.dataPath + "./textures/block/" + pack.name + "/" + RES + "/t001.png"));
			textureSheets[2] = ImageIO.read(new File(DataUtil.dataPath + "./textures/block/" + pack.name + "/" + RES + "/t002.png"));
			textureSheets[3] = ImageIO.read(new File(DataUtil.dataPath + "./textures/block/" + pack.name + "/" + RES + "/t003.png"));
		} catch(Exception exception) {
			exception.printStackTrace();
		}

		blockTypes.clear();
		for(short blockType : ElementKeyMap.typeList()) {
			if(!ElementKeyMap.isValidType(blockType)) continue; // Skip invalid block types
			if(blockType == ElementKeyMap.CORE_ID || blockType == ElementKeyMap.AI_ELEMENT || blockType == ElementKeyMap.SHOP_BLOCK_ID || blockType == ElementKeyMap.FACTION_BLOCK)
				continue;
			ElementInformation info = ElementKeyMap.getInfo(blockType);
			if(info.getSourceReference() != 0 || !info.isPlacable() || info.isReactorChamberAny() ||
					info.isDrawnOnlyInBuildMode() || info.isDeprecated() || ElementKeyMap.isShard(blockType) ||
					ElementKeyMap.isOre(blockType) || !info.isInRecipe() || !info.isShoppable() || info.isPlant())
				continue;
			blockTypes.add(blockType); // Add valid block types to the set
		}

		blockCategories.put("Any", blockTypes.toArray(new Short[0])); // Add all blocks to the "ALL" category
		calculateBlockColors(); // Pre-calculate the average colors for all blocks so we can quickly find the closest ones later
	}

	@Override
	public String getId() {
		return "COLORFINDER";
	}

	@Override
	public String getTitle() {
		return Lng.str("Color Finder");
	}

	public String toHex(Vector4f color) {
		int r = (int) (color.x * 255);
		int g = (int) (color.y * 255);
		int b = (int) (color.z * 255);
		return String.format("#%02X%02X%02X", r, g, b);
	}

	@Override
	public void build(final GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(30);
		pane.addNewTextBox(30);
		pane.addNewTextBox(30);
		pane.addNewTextBox(30);
		pane.addNewTextBox(30);

		GUIColorPickerRectangle colorPicker = new GUIColorPickerRectangle(getState(), 200, 20) {
			@Override
			public void onPickedColor(Vector4f color) {
				calculateClosest(color);
				createBlockDisplays(pane);
				textBar.setText(toHex(color));
			}
		};

		GUIColoredRectangle currentColorDisplay = new GUIColoredRectangle(getState(), 200, 20, colorPicker.getSelectedColor()) {
			@Override
			public void draw() {
				setColor(colorPicker.getSelectedColor());
				if(isActive() && isInside()) { // Shit ass hack cus the mouse callback doesnt work properly
					getToolTip().setText("Color: " + toHex(getColor()) + "\nR=" + (int) (getColor().x * 255) + " G=" + (int) (getColor().y * 255) + " B=" + (int) (getColor().z * 255));
				}
				super.draw();
			}
		};
		currentColorDisplay.onInit();
		currentColorDisplay.setToolTip(new GUIToolTip(getState(), "Color: " + toHex(colorPicker.getSelectedColor()) + "\nR=" + (int) (colorPicker.getSelectedColor().x * 255) + " G=" + (int) (colorPicker.getSelectedColor().y * 255) + " B=" + (int) (colorPicker.getSelectedColor().z * 255), currentColorDisplay));
		currentColorDisplay.setMouseUpdateEnabled(true);
		currentColorDisplay.setPos(2, 2, 0);
		pane.setContent(0, 0, currentColorDisplay);

		textBar = addTextBar(pane.getContent(0, 1), 0, 0, new TextBarResult() {
			@Override
			public String onTextChanged(String text) {
				try {
					colorPicker.fromHex(text.trim());
					calculateClosest(colorPicker.getSelectedColor());
					createBlockDisplays(pane);
				} catch(IllegalArgumentException ignored) {
				}
				return text.trim();
			}

			@Override
			public TextBarCallback initCallback() {
				return new TextBarCallback() {
					@Override
					public void onValueChanged(String value) {
						try {
							colorPicker.fromHex(value.trim());
							calculateClosest(colorPicker.getSelectedColor());
							createBlockDisplays(pane);
						} catch(IllegalArgumentException ignored) {
						}
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Enter Hex Color");
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Enter Hex Color");
			}
		});
		textBar.setInactiveText(Lng.str("Enter Hex Color"));
		textBar.setMouseUpdateEnabled(true);
		colorPicker.setPos(2, 2, 0);
		pane.setContent(0, 2, colorPicker);

		addButton(pane.getContent(0, 3), 1, 0, new ButtonResult() {
			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.PINK;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedLeftMouse() {
						colorPicker.calcRandom();
						textBar.setText(colorPicker.toHex(colorPicker.getSelectedColor()));
						calculateClosest(colorPicker.getSelectedColor());
						createBlockDisplays(pane);
						brightnessSlider.setValue((int) (colorPicker.getBrightness())); // Update brightness slider
						saturationSlider.setValue((int) (colorPicker.getSaturation())); // Update saturation slider
					}

					@Override
					public void pressedRightMouse() {

					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Random Color");
			}
		});

		saturationSlider = addSlider(pane.getContent(0, 3), 0, 1, new SliderResult() {
			@Override
			public SliderCallback initCallback() {
				return value -> {
					colorPicker.setSaturation(value); // Set saturation based on slider value
					textBar.setText(colorPicker.toHex(colorPicker.getSelectedColor()));
					calculateClosest(colorPicker.getSelectedColor());
					createBlockDisplays(pane);
				};
			}

			@Override
			public String getName() {
				return Lng.str("Saturation");
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Saturation");
			}

			@Override
			public float getDefault() {
				return (int) colorPicker.getSaturation(); // Default to current saturation
			}

			@Override
			public float getMax() {
				return 100;
			}

			@Override
			public float getMin() {
				return 0;
			}
		});
		saturationSlider.setMouseUpdateEnabled(true);

		brightnessSlider = addSlider(pane.getContent(0, 3), 0, 2, new SliderResult() {
			@Override
			public SliderCallback initCallback() {
				return value -> {
					colorPicker.setBrightness(value); // Set brightness based on slider value
					textBar.setText(colorPicker.toHex(colorPicker.getSelectedColor()));
					calculateClosest(colorPicker.getSelectedColor());
					createBlockDisplays(pane);
				};
			}

			@Override
			public String getName() {
				return Lng.str("Brightness");
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Brightness");
			}

			@Override
			public float getDefault() {
				return (int) colorPicker.getBrightness();
			}

			@Override
			public float getMax() {
				return 100;
			}

			@Override
			public float getMin() {
				return 0;
			}
		});
		brightnessSlider.setMouseUpdateEnabled(true);
		calculateClosest(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		createBlockDisplays(pane); // Default to white
	}

	@Override
	public void drawToolTip(long time) {
		super.drawToolTip(time);
		for(GUIAdvBlockDisplay display : displays) {
			display.drawToolTip(time);
		}
	}

	private void calculateBlockColors() {
		List<Short> borderlessBlocks = new ArrayList<>();
		List<Short> borderedBlocks = new ArrayList<>();
		List<Short> darkBorderedBlocks = new ArrayList<>();
		List<Short> lightBorderedBlocks = new ArrayList<>();
		for(short blockType : blockTypes) {
			if(cachedBlockColors.containsKey(blockType)) continue; // Skip if already cached
			Vector4f averageColor = getAverageColor(blockType);
			String borderType = getBorderType(blockType);
			switch(borderType) {
				case "Borderless":
					borderlessBlocks.add(blockType);
					break;
				case "Dark Bordered":
					darkBorderedBlocks.add(blockType);
					borderedBlocks.add(blockType);
					break;
				case "Light Bordered":
					lightBorderedBlocks.add(blockType);
					borderedBlocks.add(blockType);
					break;
			}
			cachedBlockColors.put(blockType, averageColor);
		}
		blockCategories.put("Borderless", borderlessBlocks.toArray(new Short[0]));
		blockCategories.put("Bordered", borderedBlocks.toArray(new Short[0]));
		blockCategories.put("Dark Bordered", darkBorderedBlocks.toArray(new Short[0]));
		blockCategories.put("Light Bordered", lightBorderedBlocks.toArray(new Short[0]));
	}

	/**
	 * Calculates the closest block colors to the given color.
	 * @param color The color to compare against the block colors.
	 */
	private void calculateClosest(final Vector4f color) {
		closestColors.clear();
		for(Map.Entry<Short, Vector4f> entry : cachedBlockColors.entrySet()) {
			if(!"Any".equals(selectedCategory)) {
				// If a category is specified, only consider blocks in that category
				if(!Arrays.asList(blockCategories.get(selectedCategory)).contains(entry.getKey())) continue;
			}
			short blockType = entry.getKey();
			Vector4f averageColor = entry.getValue();
			float distance = (float) Math.sqrt(Math.pow(averageColor.x - color.x, 2) + Math.pow(averageColor.y - color.y, 2) + Math.pow(averageColor.z - color.z, 2));
			if(closestColors.size() < 12 || distance < getAverageColor(closestColors.iterator().next()).length()) {
				closestColors.add(blockType);
				if(closestColors.size() > 12) {
					// Remove the furthest color if we exceed the limit
					float maxDistance = 0;
					short furthestColor = 0;
					for(short closestColor : closestColors) {
						Vector4f closestAverageColor = getAverageColor(closestColor);
						float currentDistance = (float) Math.sqrt(Math.pow(closestAverageColor.x - color.x, 2) + Math.pow(closestAverageColor.y - color.y, 2) + Math.pow(closestAverageColor.z - color.z, 2));
						if(currentDistance > maxDistance) {
							maxDistance = currentDistance;
							furthestColor = closestColor;
						}
					}
					if(furthestColor != 0) closestColors.remove(furthestColor);
				}
			}
		}

		//Sort the list so that the closest colors are at the front
		List<Short> sortedColors = new ArrayList<>(closestColors);
		Collections.sort(sortedColors, new Comparator<Short>() {
			@Override
			public int compare(Short o1, Short o2) {
				//Use the pre-calculated average colors for comparison
				if(o1.equals(o2)) return 0;
				Vector4f color1 = cachedBlockColors.get(o1);
				Vector4f color2 = cachedBlockColors.get(o2);
				float distance1 = (float) Math.sqrt(Math.pow(color1.x - color.x, 2) + Math.pow(color1.y - color.y, 2) + Math.pow(color1.z - color.z, 2));
				float distance2 = (float) Math.sqrt(Math.pow(color2.x - color.x, 2) + Math.pow(color2.y - color.y, 2) + Math.pow(color2.z - color.z, 2));
				return Float.compare(distance1, distance2); // Sort by distance to the selected color
			}
		});
		closestColors.clear();
		closestColors.addAll(sortedColors); // Update closestColors with the sorted list
	}

	/**
	 * Returns the border type of a block based on its type.
	 * <br/>It does this by picking the top left corner of the texture and checking its color. If it matches the light or dark border color, it's considered bordered.
	 * <br/>Otherwise, it's considered borderless.
	 * @param blockType The block type to check.
	 * @return A string representing the border type: "Borderless", "Bordered", "Dark Bordered", or "Light Bordered".
	 */
	private String getBorderType(short blockType) {
		try {
			ElementInformation info = ElementKeyMap.getInfo(blockType);
			int textureId = info.getTextureId(0); // Just use the front side texture for color calculation
			int sheetIndex = textureId / (16 * 16);
			BufferedImage sprite = null;
			if(sheetIndex < textureSheets.length) {
				BufferedImage sheet = textureSheets[sheetIndex];
				if(sheet != null) {
					int x = (textureId % (16 * 16)) % 16 * RES; // Calculate x position in the sheet
					int y = (textureId % (16 * 16)) / 16 * RES; // Calculate y position in the sheet
					sprite = sheet.getSubimage(x, y, RES, RES); // Get the subimage for the block texture
				} else {
					System.err.println("Texture sheet is null for block type: " + blockType + " at index: " + sheetIndex);
				}
			} else {
				System.err.println("Invalid sheet index: " + sheetIndex + " for block type: " + blockType);
			}

			if(sprite != null) {
				//Go with the top left corner pixel color
				int rgb = sprite.getRGB(0, 0);
				Vector4f color = new Vector4f(((rgb >> 16) & 0xFF) / 255.0f, // Red
						((rgb >> 8) & 0xFF) / 255.0f, // Green
						(rgb & 0xFF) / 255.0f, // Blue
						((rgb >> 24) & 0xFF) / 255.0f // Alpha
				);
				// Check if the color is close to the light or dark border colors
				Vector4f lightBorderColor = toVector4f(LIGHT_GREY_BORDER);
				Vector4f darkBorderColor = toVector4f(DARK_GREY_BORDER);
				if(Math.abs(color.x - lightBorderColor.x) < BORDER_DIFFERENCE_THRESHOLD && Math.abs(color.y - lightBorderColor.y) < BORDER_DIFFERENCE_THRESHOLD && Math.abs(color.z - lightBorderColor.z) < BORDER_DIFFERENCE_THRESHOLD) {
					return "Light Bordered"; // Close to light border color
				} else if(Math.abs(color.x - darkBorderColor.x) < BORDER_DIFFERENCE_THRESHOLD && Math.abs(color.y - darkBorderColor.y) < BORDER_DIFFERENCE_THRESHOLD && Math.abs(color.z - darkBorderColor.z) < BORDER_DIFFERENCE_THRESHOLD) {
					return "Dark Bordered"; // Close to dark border color
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return "Borderless"; // Default to borderless if we can't determine the type
	}

	private Vector4f toVector4f(String color) {
		if(color.startsWith("#")) color = color.substring(1); // Remove the '#' character if present
		float r = Integer.parseInt(color.substring(0, 2), 16) / 255.0f; // Red component
		float g = Integer.parseInt(color.substring(2, 4), 16) / 255.0f; // Green component
		float b = Integer.parseInt(color.substring(4, 6), 16) / 255.0f; // Blue component
		return new Vector4f(r, g, b, 1.0f); // Alpha is set to 1 (opaque)
	}

	/**
	 * Calculates the average color of a block type by sampling its texture.
	 * @param blockType The block type to calculate the average color for.
	 * @return A Vector4f representing the average color (RGBA) of the block type.
	 */
	private Vector4f getAverageColor(short blockType) {
		ElementInformation info = ElementKeyMap.getInfo(blockType);
		//use the cached data if available
		if(cachedBlockColors.containsKey(blockType)) {
			return cachedBlockColors.get(blockType);
		}
		int textureId = info.getTextureId(0); // Just use the front side texture for color calculation
		int sheetIndex = textureId / (16 * 16);
		BufferedImage sprite = null;
		Vector4f averageColor = new Vector4f();
		try {
			if(sheetIndex < textureSheets.length) {
				BufferedImage sheet = textureSheets[sheetIndex];
				if(sheet != null) {
					int x = (textureId % (16 * 16)) % 16 * RES; // Calculate x position in the sheet
					int y = (textureId % (16 * 16)) / 16 * RES; // Calculate y position in the sheet
					sprite = sheet.getSubimage(x, y, RES, RES); // Get the subimage for the block texture
				} else {
					System.err.println("Texture sheet is null for block type: " + blockType + " at index: " + sheetIndex);
				}
			} else {
				System.err.println("Invalid sheet index: " + sheetIndex + " for block type: " + blockType);
			}

			if(MODE == SCALE_DOWN) {
				// Scale down the sprite to 1x1 pixel and get the average color
				BufferedImage scaledSprite = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
				if(sprite != null) {
					scaledSprite.getGraphics().drawImage(sprite, 0, 0, 1, 1, null);
					int rgb = scaledSprite.getRGB(0, 0);
					averageColor.x = ((rgb >> 16) & 0xFF) / 255.0f; // Red
					averageColor.y = ((rgb >> 8) & 0xFF) / 255.0f; // Green
					averageColor.z = (rgb & 0xFF) / 255.0f; // Blue
				}
			} else {
				// Average the colors of the pixels in the sprite
				int totalPixels = 0;
				if(sprite != null) {
					for(int i = 0; i < sprite.getWidth(); i++) {
						for(int j = 0; j < sprite.getHeight(); j++) {
							int rgb = sprite.getRGB(i, j);
							if((rgb >> 24) != 0x00) { // Check if pixel is not transparent
								averageColor.x += ((rgb >> 16) & 0xFF) / 255.0f; // Red
								averageColor.y += ((rgb >> 8) & 0xFF) / 255.0f; // Green
								averageColor.z += (rgb & 0xFF) / 255.0f; // Blue
								totalPixels++;
							}
						}
					}
					if(totalPixels > 0) {
						averageColor.x /= totalPixels;
						averageColor.y /= totalPixels;
						averageColor.z /= totalPixels;
					}
				} else {
					System.err.println("Sprite is null for block type: " + blockType);
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return averageColor;
	}

	private void createBlockDisplays(GUIContentPane pane) {
		for(GUIAdvBlockDisplay display : displays) {
			display.cleanUp();
			display.setVisibility(2);
		}
		displays.clear();
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 3; y++) {
				final int index = x + y * 4;
				displays.add(addBlockDisplay(pane.getContent(0, 4), x, y, new BlockDisplayResult() {
					@Override
					public short getCurrentValue() {
						return closestColors.toArray(new Short[0])[index];
					}

					@Override
					public short getDefault() {
						return closestColors.isEmpty() ? 0 : closestColors.iterator().next();
					}

					@Override
					public BlockSelectCallback initCallback() {
						return null;
					}

					@Override
					public String getToolTipText() {
						if(ElementKeyMap.isValidType(getCurrentValue()))
							return ElementKeyMap.getInfo(getCurrentValue()).getName();
						return "";
					}
				}));
			}
		}
		for(GUIAdvBlockDisplay display : displays) display.setMouseUpdateEnabled(true);
	}
}
