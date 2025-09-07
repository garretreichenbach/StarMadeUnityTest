package org.schema.game.client.view.tools;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public record IconRotationData(short typeID, IconRotationFunction function) implements Comparable<IconRotationData> {

	public void apply() {
		function.apply();
	}

	@Override
	public int compareTo(IconRotationData other) {
		if(!ElementKeyMap.isValidType(typeID)) return 0;
		else {
			ElementInformation thisInfo = getInfo();
			ElementInformation otherInfo = other.getInfo();
			if(thisInfo == null || otherInfo == null) return 0;
			else {
				if(thisInfo.getBuildIconNum() == otherInfo.getBuildIconNum()) return 0;
				else return thisInfo.getBuildIconNum() < otherInfo.getBuildIconNum() ? -1 : 1;
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IconRotationData other) return typeID == other.typeID;
		else return false;
	}

	public ElementInformation getInfo() {
		if(ElementKeyMap.isValidType(typeID)) return ElementKeyMap.getInfo(typeID);
		else return null;
	}
}
