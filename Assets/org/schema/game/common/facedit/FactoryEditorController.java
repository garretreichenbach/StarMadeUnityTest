package org.schema.game.common.facedit;

import java.util.Arrays;
import java.util.HashMap;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.BlockFactory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;

public class FactoryEditorController {

	private final HashMap<Short, TemporalElement> elementMap = new HashMap<Short, TemporalElement>();
	EditorData data = new EditorData();

	public TemporalElement convertExisting(short id) {
		TemporalElement e = new TemporalElement();
		e.setId(id);

		ElementInformation info = ElementKeyMap.getInfo(id);
		e.setIconId(info.getBuildIconNum());
		e.setTextureId(Arrays.copyOf(info.getTextureIds(), info.getTextureIds().length));
		e.setName(info.getName());

		TemporalFactory f = new TemporalFactory();
		f.enhancer = info.getFactory().enhancer;
		for (int pid = 0; pid < info.getFactory().input.length; pid++) {
			TemporalProduct prod = new TemporalProduct();
			prod.factoryId = id;

			for (int i = 0; i < info.getFactory().input[pid].length; i++) {
				prod.input.add(info.getFactory().input[pid][i]);
			}
			for (int i = 0; i < info.getFactory().output[pid].length; i++) {
				prod.output.add(info.getFactory().output[pid][i]);
			}
			f.temporalProducts.add(prod);
		}

		e.setFactory(f);
		elementMap.put(id, e);
		return e;

	}

	public ElementInformation convertFromTemporal(TemporalElement e) {
		ElementInformation info = new ElementInformation(e.getId(), e.getName(), ElementKeyMap.getCategoryHirarchy().find("factory"), e.getTextureId());

		BlockFactory f = new BlockFactory();

		f.enhancer = e.getFactory().enhancer;
		f.output = new FactoryResource[e.getFactory().temporalProducts.size()][];
		f.input = new FactoryResource[e.getFactory().temporalProducts.size()][];

		for (int pid = 0; pid < e.getFactory().temporalProducts.size(); pid++) {
			f.input[pid] = new FactoryResource[e.getFactory().temporalProducts.get(pid).input.size()];
			for (int i = 0; i < e.getFactory().temporalProducts.get(pid).input.size(); i++) {
				f.input[pid][i] = e.getFactory().temporalProducts.get(pid).input.get(i);
			}

			f.output[pid] = new FactoryResource[e.getFactory().temporalProducts.get(pid).output.size()];
			for (int i = 0; i < e.getFactory().temporalProducts.get(pid).output.size(); i++) {
				f.output[pid][i] = e.getFactory().temporalProducts.get(pid).output.get(i);
			}
		}

		info.setFactory(f);

		return info;

	}

	private void convertToTempoal() {
		for (short s : ElementKeyMap.getFactorykeyset()) {
			TemporalProduct p = new TemporalProduct();
		}
	}

	public TemporalProduct createTemporalFactory() {
		TemporalProduct p = new TemporalProduct();

		return p;
	}

	public void initialize() {
		if (!ElementKeyMap.initialized) {
			ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		}

		convertToTempoal();
	}
}
