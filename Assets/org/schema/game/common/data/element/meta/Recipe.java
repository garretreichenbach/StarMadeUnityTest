package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.InvalidMetaItemException;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class Recipe extends MetaObject implements RecipeInterface {
	private static final float BAKE_TIEM = 10; //make dynamic
	private final long base = (ServerConfig.RECIPE_LEVEL_AMOUNT.getInt());
	public RecipeProduct[] recipeProduct;
	public long fixedPrice = -1;
	private long producedGoods = 0;
	private String inputString = "";
	private String outputString = "";
	private int level = 1;
	private int lastPercent;
	private long nextLvl;
	private long lastProducedGoods;
	private byte maxLevel = 30;

	public Recipe(int id) {
		super(id);
	}

	public static RecipeProduct getProduct(short[] types, short[] counts, short[] outtypes, short[] outcounts) throws InvalidFactoryParameterException {
		if (types.length != counts.length) {
			throw new InvalidFactoryParameterException("types array length not equals counts array length");
		}
		if (outtypes.length != outcounts.length) {
			throw new InvalidFactoryParameterException("types array length not equals counts array length");
		}
		RecipeProduct recipeProduct = new RecipeProduct();
		recipeProduct.inputResource = new FactoryResource[types.length];
		recipeProduct.outputResource = new FactoryResource[outtypes.length];

		for (int i = 0; i < types.length; i++) {
			recipeProduct.inputResource[i] = new FactoryResource(counts[i], types[i]);
		}
		for (int i = 0; i < outtypes.length; i++) {
			recipeProduct.outputResource[i] = new FactoryResource(outcounts[i], outtypes[i]);
		}
		return recipeProduct;
	}

	public void updateLevel() {
		this.level = 1;
		long curBase = base;
		nextLvl = (long) (base * (Math.pow(1.6f, level)));
		double previousLvl = 0;
		if (nextLvl > 0) {
			while (producedGoods > nextLvl && level < maxLevel) {
				level++;
				previousLvl = nextLvl;

				long n = nextLvl + base * (long) (Math.pow(1.6f, level));
				if (n > 0) {
					nextLvl = n;
				} else {
					nextLvl = Long.MAX_VALUE;
				}
			}
		}
	}

	public String getPriceString() {
		if (fixedPrice >= 0) {
			int price = (int) (fixedPrice * (ServerConfig.RECIPE_REFUND_MULT.getFloat()));
			return price + " credits";
		} else {
			int refund = ServerConfig.RECIPE_BLOCK_COST.getInt();

			refund = (int) (refund * (ServerConfig.RECIPE_REFUND_MULT.getFloat()));

			short type = recipeProduct[0].outputResource[0].type;

			ElementInformation info = ElementKeyMap.getInfo(type);
			return refund + " " + info.getName();

		}
	}

	private void checkLevel(GameServerState state) {
		/*
		 * only level up simple recipes
		 */
		if (recipeProduct.length == 1) {
			updateLevel();
			recipeProduct[0].outputResource[0].count = (short) level;
			if (state != null) {
				int percent = 0;

				if (nextLvl > 0) {
					double pc = ((double) producedGoods / (double) nextLvl) * 100;
					percent = (int) pc;
				} else {
					percent = 0;
				}
				if (this.lastPercent != percent) {
					//					System.err.println("[RECIPE] NEW % "+percent);
					if (state.getGameState() != null) {
						state.getGameState().announceMetaObject(this);
					}
					this.lastPercent = percent;
				}
			}
		}
	}

	public void compile() {
		compileInput();
		compileOutput();
	}

	private void compileInput() {

		for (int i = 0; i < recipeProduct.length; i++) {
			assert (recipeProduct[i] != null) : i;
			assert (recipeProduct[i].inputResource != null) : i;
			assert (recipeProduct[i].outputResource != null) : i;
			for (int j = 0; j < recipeProduct[i].inputResource.length; j++) {
				assert (recipeProduct[i].inputResource[j] != null) : i + "; " + j;
			}
			for (int j = 0; j < recipeProduct[i].outputResource.length; j++) {
				assert (recipeProduct[i].outputResource[j] != null) : i + "; " + j;
			}
		}

		StringBuffer a = new StringBuffer();
		if (recipeProduct.length > 1) {
			ElementInformation info0 = ElementKeyMap.getInfo(recipeProduct[0].inputResource[0].type);
			ElementInformation info1 = ElementKeyMap.getInfo(recipeProduct[1].inputResource[0].type);
			
		} else {
			for (int i = 0; i < recipeProduct[0].inputResource.length; i++) {
				a.append(recipeProduct[0].inputResource[i]);
			}
		}
		this.inputString = a.toString();
	}

	private void compileOutput() {
		StringBuffer a = new StringBuffer();
		if (recipeProduct.length > 1) {
			ElementInformation info0 = ElementKeyMap.getInfo(recipeProduct[0].inputResource[0].type);
			ElementInformation info1 = ElementKeyMap.getInfo(recipeProduct[1].inputResource[0].type);
			
		} else {
			for (int i = 0; i < recipeProduct[0].outputResource.length; i++) {
				a.append(recipeProduct[0].outputResource[i]);
			}
		}
		this.outputString = a.toString();
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
	}

	@Override
	public void fromTag(Tag tag) {
		if ("v2".equals(tag.getName())) {
			Tag[] products = (Tag[]) ((Tag[]) tag.getValue())[0].getValue();

			if (((Tag[]) tag.getValue())[1].getType() == Type.INT) {
				producedGoods = Math.max(0, (Integer) ((Tag[]) tag.getValue())[1].getValue());
			} else if (((Tag[]) tag.getValue())[1].getType() == Type.LONG) {
				producedGoods = Math.max(0, (Long) ((Tag[]) tag.getValue())[1].getValue());
			}

			recipeProduct = new RecipeProduct[products.length - 1];

			//			System.err.println("PARSING PRODUCTS: "+recipeProduct.length);

			for (int i = 0; i < recipeProduct.length; i++) {
				try {
					recipeProduct[i] = fromTagProduct(products[i]);
				} catch (InvalidFactoryParameterException e) {
					e.printStackTrace();
					throw new InvalidMetaItemException(e.getClass() + ": " + e.getMessage());
				}
			}

			fixedPrice = (Long) ((Tag[]) tag.getValue())[2].getValue();
			maxLevel = (Byte) ((Tag[]) tag.getValue())[3].getValue();

		} else if ("v1".equals(tag.getName())) {
			Tag[] products = (Tag[]) ((Tag[]) tag.getValue())[0].getValue();

			if (((Tag[]) tag.getValue())[1].getType() == Type.INT) {
				producedGoods = Math.max(0, (Integer) ((Tag[]) tag.getValue())[1].getValue());
			} else if (((Tag[]) tag.getValue())[1].getType() == Type.LONG) {
				producedGoods = Math.max(0, (Long) ((Tag[]) tag.getValue())[1].getValue());
			}

			recipeProduct = new RecipeProduct[products.length - 1];

			//			System.err.println("PARSING PRODUCTS: "+recipeProduct.length);

			for (int i = 0; i < recipeProduct.length; i++) {
				try {
					recipeProduct[i] = fromTagProduct(products[i]);
				} catch (InvalidFactoryParameterException e) {
					e.printStackTrace();
					throw new InvalidMetaItemException(e.getClass() + ": " + e.getMessage());
				}
			}

			fixedPrice = (Long) ((Tag[]) tag.getValue())[2].getValue();

		} else if ("v0".equals(tag.getName())) {
			Tag[] products = (Tag[]) ((Tag[]) tag.getValue())[0].getValue();

			if (((Tag[]) tag.getValue())[1].getType() == Type.INT) {
				producedGoods = Math.max(0, (Integer) ((Tag[]) tag.getValue())[1].getValue());
			} else if (((Tag[]) tag.getValue())[1].getType() == Type.LONG) {
				producedGoods = Math.max(0, (Long) ((Tag[]) tag.getValue())[1].getValue());
			}

			recipeProduct = new RecipeProduct[products.length - 1];

			//			System.err.println("PARSING PRODUCTS: "+recipeProduct.length);

			for (int i = 0; i < recipeProduct.length; i++) {
				try {
					recipeProduct[i] = fromTagProduct(products[i]);
				} catch (InvalidFactoryParameterException e) {
					e.printStackTrace();
					throw new InvalidMetaItemException(e.getClass() + ": " + e.getMessage());
				}
			}

		} else {

			recipeProduct = new RecipeProduct[1];
			try {
				recipeProduct[0] = fromTagProduct(tag);
			} catch (InvalidFactoryParameterException e) {
				e.printStackTrace();
				throw new InvalidMetaItemException(e.getClass() + ": " + e.getMessage());
			}

			compile();

			if (((Tag[]) tag.getValue()).length > 4) {
				if (((Tag[]) tag.getValue())[4].getType() == Type.INT) {
					producedGoods = Math.max(0, (Integer) ((Tag[]) tag.getValue())[4].getValue());
				} else if (((Tag[]) tag.getValue())[4].getType() == Type.LONG) {
					producedGoods = Math.max(0, (Long) ((Tag[]) tag.getValue())[4].getValue());
				}
			}
		}
		checkLevel(null);
	}

	@Override
	public Tag getBytesTag() {
		Tag[] productsTag = new Tag[recipeProduct.length + 1];
		productsTag[recipeProduct.length] = FinishTag.INST;

		//		System.err.println("WRITING PRODUCTS: "+recipeProduct.length);

		for (int p = 0; p < recipeProduct.length; p++) {

			RecipeProduct prod = recipeProduct[p];

			Tag[] inputTag = new Tag[prod.inputResource.length + 1];
			Tag[] outputTag = new Tag[prod.outputResource.length + 1];

			inputTag[prod.inputResource.length] = FinishTag.INST;
			for (int i = 0; i < prod.inputResource.length; i++) {
				inputTag[i] = new Tag(Type.SHORT, null, prod.inputResource[i].type);
			}

			outputTag[prod.outputResource.length] = FinishTag.INST;
			for (int i = 0; i < prod.outputResource.length; i++) {
				outputTag[i] = new Tag(Type.SHORT, null, prod.outputResource[i].type);
			}

			Tag[] inputTagCount = new Tag[prod.inputResource.length + 1];
			Tag[] outputTagCount = new Tag[prod.outputResource.length + 1];

			inputTagCount[prod.inputResource.length] = FinishTag.INST;
			for (int i = 0; i < prod.inputResource.length; i++) {
				inputTagCount[i] = new Tag(Type.SHORT, null, (short) prod.inputResource[i].count);
			}

			outputTagCount[prod.outputResource.length] = FinishTag.INST;
			for (int i = 0; i < prod.outputResource.length; i++) {
				outputTagCount[i] = new Tag(Type.SHORT, null, (short) prod.outputResource[i].count);
			}
			productsTag[p] = new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.STRUCT, null, inputTag),
					new Tag(Type.STRUCT, null, outputTag),
					new Tag(Type.STRUCT, null, inputTagCount),
					new Tag(Type.STRUCT, null, outputTagCount),
					FinishTag.INST});

		}
		return new Tag(Type.STRUCT, "v2", new Tag[]{
				new Tag(Type.STRUCT, null, productsTag),
				new Tag(Type.LONG, null, producedGoods),
				new Tag(Type.LONG, null, fixedPrice),
				new Tag(Type.BYTE, null, maxLevel),
				FinishTag.INST});
	}

	@Override
	public DialogInput getEditDialog(GameClientState state,
	                                 AbstractControlManager parent, Inventory openedFrom) {
		return new PlayerGameOkCancelInput("RECIPE", state, Lng.str("Recipe"), toDetailedString()) {

			@Override
			public void onDeactivate() {

			}

			@Override
			public void pressedOK() {
				deactivate();
			}
		};
	}

	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.RECIPE;
	}
	@Override
	public String getName() {
		return Lng.str("Recipe");
	}
	@Override
	public int getPermission() {
		return NO_EDIT_PERMISSION;
	}

	@Override
	public boolean isValidObject() {
		return recipeProduct != null;
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {

	}

	private RecipeProduct fromTagProduct(Tag tag) throws InvalidFactoryParameterException {
		Tag[] inputTags = (Tag[]) ((Tag[]) tag.getValue())[0].getValue();
		Tag[] outputTags = (Tag[]) ((Tag[]) tag.getValue())[1].getValue();

		short[] input = new short[inputTags.length - 1];
		for (int i = 0; i < inputTags.length - 1; i++) {
			input[i] = (Short) inputTags[i].getValue();
			if (input[i] >= 0 && !ElementKeyMap.exists(input[i])) {
				throw new InvalidMetaItemException("Recipe is invalid: input type: " + input[i] + " does not exist");
			}
		}

		short[] output = new short[outputTags.length - 1];
		for (int i = 0; i < outputTags.length - 1; i++) {
			output[i] = (Short) outputTags[i].getValue();
			if (output[i] >= 0 && !ElementKeyMap.exists(output[i])) {
				throw new InvalidMetaItemException("Recipe is invalid: input type: " + output[i] + " does not exist");
			}
		}

		Tag[] inputTagsCount = (Tag[]) ((Tag[]) tag.getValue())[2].getValue();
		Tag[] outputTagsCount = (Tag[]) ((Tag[]) tag.getValue())[3].getValue();

		short[] inputCount = new short[inputTagsCount.length - 1];
		for (int i = 0; i < inputTagsCount.length - 1; i++) {
			inputCount[i] = (Short) inputTagsCount[i].getValue();
		}

		short[] outputCount = new short[outputTagsCount.length - 1];
		for (int i = 0; i < outputTagsCount.length - 1; i++) {
			outputCount[i] = (Short) outputTagsCount[i].getValue();
		}

		return getProduct(input, inputCount, output, outputCount);
	}

	public void setRecipe(short[] types, short[] counts, short[] outtypes, short[] outcounts) throws InvalidFactoryParameterException {
		if (types.length != counts.length) {
			throw new InvalidFactoryParameterException("types array length not equals counts array length");
		}
		if (outtypes.length != outcounts.length) {
			throw new InvalidFactoryParameterException("types array length not equals counts array length");
		}
		recipeProduct = new RecipeProduct[1];
		recipeProduct[0] = getProduct(types, counts, outtypes, outcounts);

		compile();
	}

	public boolean isCoarse() {
		if (recipeProduct.length > 1) {
			ElementInformation info0 = ElementKeyMap.getInfo(recipeProduct[0].inputResource[0].type);
			ElementInformation info1 = ElementKeyMap.getInfo(recipeProduct[1].inputResource[0].type);
			
		}
		return false;
	}

	public boolean isRefine() {
		if (recipeProduct.length > 1) {
			ElementInformation info0 = ElementKeyMap.getInfo(recipeProduct[0].inputResource[0].type);
			ElementInformation info1 = ElementKeyMap.getInfo(recipeProduct[1].inputResource[0].type);
			
		}
		return false;
	}

	private Object toDetailedString() {
		StringBuffer a = new StringBuffer();
		boolean refine = isRefine();
		boolean coarse = isRefine();
		if (refine || coarse) {
			ElementInformation info0 = ElementKeyMap.getInfo(recipeProduct[0].inputResource[0].type);
			ElementInformation info1 = ElementKeyMap.getInfo(recipeProduct[1].inputResource[0].type);
			if (refine) {

				a.append("This recipe will refine ");
				a.append(info0.getName().replaceAll("L1", "").trim());
				a.append(" for ");
				a.append(recipeProduct[0].inputResource[0].count);
				a.append("\n");
				a.append("of one level to ");
				a.append(recipeProduct[1].outputResource[0].count);
				a.append("\nof the next higher level");
			} else if (coarse) {

				a.append("This recipe will coarse ");
				a.append(info0.getName().replaceAll("L2", "").trim());
				a.append(" for ");
				a.append(recipeProduct[0].inputResource[0].count);
				a.append("\n");
				a.append("of one level to ");
				a.append(recipeProduct[1].outputResource[0].count);
				a.append("\nof the next lower level");
			}
		} else {
			if (producedGoods != lastProducedGoods) {
				updateLevel();
				lastProducedGoods = producedGoods;
			}
			a.append("This recipe produces " + recipeProduct[0].outputResource[0] + "\n");
			a.append("Level: ");
			a.append(level);
			a.append(" / ");
			a.append(maxLevel);
			if (level < maxLevel) {
				a.append("\nNext level progress: ~");
				a.append((producedGoods));
				a.append(" of ");
				a.append(nextLvl);
				a.append(" produced (updated every %)");
			} else {
				a.append("\nMax level reached (" + maxLevel + ")");
			}

			a.append("\nResources Needed:\n");
			for (int i = 0; i < recipeProduct[0].inputResource.length; i++) {
				a.append("+ ");
				a.append(recipeProduct[0].inputResource[i] + "\n");
			}

		}
		return a.toString();
	}

	@Override
	public String toString() {
		return Lng.str("Recipe for %s\n(right click for info)", outputString);
	}

	public String getSellString() {
		return "Do you want to sell this recipe?\n\n" +
				"You will get " + getPriceString() + " back.\n"
				+ "Any level process on the recipe will be lost.";
	}

	@Override
	public RecipeProduct[] getRecipeProduct() {
		return recipeProduct;
	}

	@Override
	public void producedGood(int count, GameServerState state) {
		producedGoods += count;
		checkLevel(state);
	}

	@Override
	public float getBakeTime() {
		return BAKE_TIEM;
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other);
	}
}
