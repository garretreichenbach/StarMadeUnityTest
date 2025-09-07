package org.schema.game.common.controller;

import org.schema.game.common.data.element.ElementInformation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ArmorValue{
	public ObjectArrayList<ElementInformation> typesHit = new ObjectArrayList<ElementInformation>();
	public float armorValueAccumulatedRaw;
	public float armorIntegrity;
	public float totalArmorValue;
	
	public void reset() {
		typesHit.clear();
		armorValueAccumulatedRaw = 0;
		armorIntegrity = 0;
		totalArmorValue = 0;
	}
	
	public void calculate() {
		assert(!typesHit.isEmpty());
		armorIntegrity = armorIntegrity / (float)typesHit.size();
		totalArmorValue = armorValueAccumulatedRaw*armorIntegrity;
	}

	public void set(ArmorValue o) {
		reset();
		typesHit.addAll(o.typesHit);
		armorValueAccumulatedRaw = o.armorValueAccumulatedRaw;
		armorIntegrity = o.armorIntegrity;
		totalArmorValue = o.totalArmorValue;
		
	}
}