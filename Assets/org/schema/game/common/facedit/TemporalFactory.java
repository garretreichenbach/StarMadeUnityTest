package org.schema.game.common.facedit;

import java.util.ArrayList;

import org.schema.game.common.data.element.BlockFactory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;

public class TemporalFactory {
	public short enhancer;
	ArrayList<TemporalProduct> temporalProducts = new ArrayList<TemporalProduct>();

	public BlockFactory convertFromTemporal(ElementInformation e) {

		BlockFactory f = new BlockFactory();

		f.enhancer = this.enhancer;
		f.output = new FactoryResource[this.temporalProducts.size()][];
		f.input = new FactoryResource[this.temporalProducts.size()][];

		for (int pid = 0; pid < this.temporalProducts.size(); pid++) {
			f.input[pid] = new FactoryResource[this.temporalProducts.get(pid).input.size()];
			for (int i = 0; i < this.temporalProducts.get(pid).input.size(); i++) {
				f.input[pid][i] = this.temporalProducts.get(pid).input.get(i);
			}

			f.output[pid] = new FactoryResource[this.temporalProducts.get(pid).output.size()];
			for (int i = 0; i < this.temporalProducts.get(pid).output.size(); i++) {
				f.output[pid][i] = this.temporalProducts.get(pid).output.get(i);
			}
		}

		e.setFactory(f);

		return f;

	}

	public void setFromExistingInfo(short id) {

		ElementInformation info = ElementKeyMap.getInfo(id);
		if (info.getFactory() != null) {
			this.enhancer = info.getFactory().enhancer;
			for (int pid = 0; pid < info.getFactory().input.length; pid++) {
				TemporalProduct prod = new TemporalProduct();
				prod.factoryId = id;

				for (int i = 0; i < info.getFactory().input[pid].length; i++) {
					prod.input.add(info.getFactory().input[pid][i]);
				}
				for (int i = 0; i < info.getFactory().output[pid].length; i++) {
					prod.output.add(info.getFactory().output[pid][i]);
				}
				this.temporalProducts.add(prod);
			}
		}

	}

	@Override
	public String toString() {
		return "Factory Products: " + temporalProducts.size();
	}
}
