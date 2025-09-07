package org.schema.game.client.view.creaturetool.swing;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;

// #RM1958 removed ComboBoxModel generic argument
public class CreaturePartAnimationComboboxModel implements ComboBoxModel {

	private AnimationIndexElement selected;

	@Override
	public int getSize() {
		return AnimationIndex.animations.length;
	}

	@Override
	public AnimationIndexElement getElementAt(int index) {
		return AnimationIndex.animations[index];
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		
	}

	@Override
	public void setSelectedItem(Object anItem) {
		selected = (AnimationIndexElement) anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

}
