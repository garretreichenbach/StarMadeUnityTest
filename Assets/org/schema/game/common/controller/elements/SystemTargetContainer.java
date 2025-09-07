package org.schema.game.common.controller.elements;

import java.util.List;
import java.util.Random;

import org.schema.game.common.data.element.ElementCollection;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SystemTargetContainer {
	
	private final List<TargetableSystemInterface> fullList = new ObjectArrayList<TargetableSystemInterface>();
	private final FloatList fullPrioList = new FloatArrayList();
	private float fullPrio;
	
	public void initialize(List<TargetableSystemInterface> l) {
		fullPrio = 0;
		fullList.clear();
		fullPrioList.clear();
		for (int i = 0; i < l.size(); i++) {
			TargetableSystemInterface t = l.get(i);
			if(t.hasAnyBlock()) {
				fullList.add(t);
				fullPrioList.add(t.getPriority());
				fullPrio += t.getPriority();
			}
		}
		for(int i = 0; i < fullPrioList.size(); i++) {
			fullPrioList.set(i, fullPrioList.get(i) / fullPrio);
		}
	}
	
	public TargetableSystemInterface getRandom(Random r) {
		if(fullList.isEmpty()) {
			return null;
		}
		float f = r.nextFloat();
		
		TargetableSystemInterface selected = fullList.get(0); 
		float total = fullPrioList.getFloat(0);
		for(int i = 1; i < fullList.size(); i++) {
			total+=fullPrioList.getFloat(i);
			if(f > total-fullPrioList.getFloat(i-1) &&  f <= total) {
				selected = fullList.get(i);
				break;
			}
		}
		return selected;
	}

	public ElementCollection<?, ?, ?> getRandomCollection(Random r) {
		TargetableSystemInterface random = getRandom(r);
		if(random != null) {
			return random.getRandomCollection(r);
		}
		return null;
	}

	public boolean isEmpty() {
		return fullList.isEmpty();
	}
}
