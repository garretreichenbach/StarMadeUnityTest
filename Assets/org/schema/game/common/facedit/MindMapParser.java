package org.schema.game.common.facedit;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MindMapParser {

	private final ArrayList<MindMapCategory> categories = new ArrayList<MindMapCategory>();
	private final Short2ObjectOpenHashMap<Block> blocks = new Short2ObjectOpenHashMap<Block>();

//	ElementParser p = new ElementParser();
//	
//	public MindMapParser(){
//		p.parse();
//	}

	public static void main(String m[]) {
		MindMapParser p = new MindMapParser();
		p.parse("mindMapImport");

		System.err.println("PARSED: \n" + p.toString());
	}

	public static String getInfo(int id, String name) {
		String in = (ElementKeyMap.isValidType((short) id) ? ElementKeyMap.getInfo((short) id).getName() : "unknown");
		return "[" + (in.equals(name) ? in : ("!!! " + in)) + "]";
	}

	public void parse(String file) {

		File f = new FileExt(file);
		BufferedReader r = null;

		try {
			r = new BufferedReader(new FileReader(f));

			String line;

			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {

					System.err.println("PARSING LINE: " + line);

					if (line.startsWith("#cat")) {
						parseCat(line);
					} else if (line.startsWith("#shop")) {
						parseShopCat(line);
					} else if (line.startsWith("#block")) {
						parseBlock(line);
					} else if (line.startsWith("#item")) {
						parseItem(line);
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public int getValueInt(String name, String line) {
		String value = getValue(name, line);

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new MindMapParsingException("Not an integer: when parsing value " + name + " from: " + line, e);
		}

	}

	public boolean existsValue(String name, String line) {
		return line.toLowerCase(Locale.ENGLISH).indexOf(name.toLowerCase(Locale.ENGLISH)) >= 0;
	}

	public String getValue(String name, String line) {
		int indexOf = line.toLowerCase(Locale.ENGLISH).indexOf(name.toLowerCase(Locale.ENGLISH));
		if (indexOf >= 0) {
			int eqPos = line.indexOf("=", indexOf);
			if (eqPos >= 0) {
				int firstQuote = line.indexOf("\"", eqPos);
				int secondQuote = firstQuote >= 0 ? line.indexOf("\"", firstQuote + 1) : -1;

				if (firstQuote > 0 && secondQuote > 0) {
					return line.substring(firstQuote + 1, secondQuote);
				} else {
					throw new MindMapParsingException("invalid value in quotes: " + name + " in " + line);
				}
			} else {
				throw new MindMapParsingException("invalid value: " + name + " in " + line);
			}
		} else {
			throw new MindMapParsingException("value not found: " + name + " in " + line);
		}
	}

	public void loadWithFileChooser(Component component) {
		JFileChooser fc = new JFileChooser("./");
		fc.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				if (arg0.isDirectory()) {
					return true;
				}
				if (arg0.getName().endsWith(".txt")) {
					return true;
				}
				return false;
			}

			@Override
			public String getDescription() {
				return "MindMapRaw (txt)";
			}
		});
		fc.setAcceptAllFileFilterUsed(false);

		//Show it.
		int returnVal = fc.showDialog(component, "Choose MindMap");

		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			parse(file.getAbsolutePath());
		} else {
		}
	}

	public void apply() {
		for (short id : ElementKeyMap.keySet) {
			ElementInformation info = ElementKeyMap.getInfo(id);
			Block block = blocks.get(id);

			if (block != null) {
				info.setInventoryGroup(block.inventoryGroup);
				info.consistence.clear();
				for (int i = 0; i < block.items.size(); i++) {
					try {
						Item item = block.items.get(i);
						if (item.id > 0) {
							ElementInformation infoCons = ElementKeyMap
									.getInfo((short) item.id);

							if (infoCons == null) {
								throw new MindMapParsingException(
										"Cannot use consistence with id: "
												+ item.id
												+ "; id is unknown. used in "
												+ info.getName() + "("
												+ info.getId() + ")");
							} else {
								info.consistence.add(new FactoryResource(item.count, infoCons.getId()));

								if (block.superCat.toLowerCase(Locale.ENGLISH).contains("basic")) {
									info.setProducedInFactory(ElementInformation.FAC_COMPONENT);
								} else if (block.superCat.toLowerCase(Locale.ENGLISH).contains("standard")) {
									info.setProducedInFactory(ElementInformation.FAC_BLOCK);
								} else if (block.superCat.toLowerCase(Locale.ENGLISH).contains("advanced")) {
									info.setProducedInFactory(ElementInformation.FAC_CHEM);
								}
							}
						}
					} catch (Exception e) {
						GuiErrorHandler.processErrorDialogException(e);
					}
				}
			}
		}
	}

	private void add(Block block) {
		categories.get(categories.size() - 1).add(block);
	}

	private void add(MindMapCategory mindMapCategory) {
		categories.add(mindMapCategory);
	}

	private void add(Item item) {
		categories.get(categories.size() - 1).add(item);
	}

	private void add(ShopCategory shopCategory) {
		categories.get(categories.size() - 1).add(shopCategory);
	}

	private void parseCat(String line) {
		String name = getValue("name", line);

		add(new MindMapCategory(name));
	}

	private void parseShopCat(String line) {
		String cat = getValue("cat", line);

		add(new ShopCategory(cat));
	}

	private void parseBlock(String line) {
		String name = getValue("name", line);
		String group = "";
		if (existsValue("group", line)) {
			group = getValue("group", line);
		}
		int id = getValueInt("id", line);

		add(new Block(id, name, group));
	}

	private void parseItem(String line) {
		String cost = getValue("cost", line);
		int id = getValueInt("id", line);
		int count = getValueInt("count", line);

		add(new Item(cost, id, count));

	}

	public ArrayList<MindMapCategory> getCategories() {
		return categories;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < categories.size(); i++) {
			categories.get(i).getString(sb);
		}

		return sb.toString();
	}

	private class MindMapCategory {
		private final String name;
		private final ArrayList<ShopCategory> shopCategories = new ArrayList<ShopCategory>();

		public MindMapCategory(String name) {
			super();
			this.name = name;
			ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		}

		public void getString(StringBuffer sb) {
			sb.append(name + "\n");
			for (int i = 0; i < shopCategories.size(); i++) {
				shopCategories.get(i).getString(sb);
			}
		}

		public void add(Block block) {
			shopCategories.get(shopCategories.size() - 1).add(block, name);
		}

		public void add(Item item) {
			shopCategories.get(shopCategories.size() - 1).add(item);
		}

		public void add(ShopCategory shopCategory) {
			shopCategories.add(shopCategory);
		}

	}

	private class ShopCategory {

		private final String cat;

		private final ArrayList<Block> blocks = new ArrayList<Block>();

		public ShopCategory(String cat) {
			super();
			this.cat = cat;
		}

		public void add(Item item) {
			blocks.get(blocks.size() - 1).add(item);
		}

		public void add(Block block, String superCat) {
			if (blocks.size() == 0 || blocks.get(blocks.size() - 1).items.size() > 0) {
				block.superCat = superCat;
				MindMapParser.this.blocks.put((short) block.id, block);
				blocks.add(block);
			} else {
				throw new MindMapParsingException("Last Block didnt have a recipe!");
			}
		}

		public void getString(StringBuffer sb) {
			sb.append("   Cat: " + cat + "\n");
			for (int i = 0; i < blocks.size(); i++) {
				blocks.get(i).getString(sb);
			}
		}

	}

	private class Block {
		private final int id;
		private final ArrayList<Item> items = new ArrayList<Item>();
		private final String inventoryGroup;
		public String superCat;
		String name;

		public Block(int id, String name, String group) {
			super();
			this.id = id;
			this.name = name;
			this.inventoryGroup = group;

		}

		public void add(Item item) {
			items.add(item);
		}

		public void getString(StringBuffer sb) {
			sb.append("      " + name + " (" + id + ") " + getInfo(id, name) + "\n");
			for (int i = 0; i < items.size(); i++) {
				items.get(i).getString(sb);
			}
		}

	}

	private class Item {
		private final String cost;
		private final int id;
		private final int count;

		public Item(String cost, int id, int count) {
			super();
			this.cost = cost;
			this.id = id;
			this.count = count;
		}

		public void getString(StringBuffer sb) {
			sb.append("         -> " + cost + " (" + id + ") " + getInfo(id, cost) + "; count: " + count + "\n");
		}

	}

}
