package org.schema.schine.common.language.editor;

import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;

import org.schema.schine.common.language.Translation;

public class TranslationListModel extends AbstractListModel implements Observer{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	public LanguageEditor l;
	
	public TranslationListModel(LanguageEditor l) {
		super();
		this.l = l;
		l.addObserver(this);
	}

	@Override
	public int getSize() {
		return l.list == null ? 0 : l.list.size();
	}

	@Override
	public Translation getElementAt(int index) {
		return l.list.get(index);
	}

	@Override
	public void update(Observable o, Object arg) {
		fireContentsChanged(l, 0, l.list.size()-1);
	}

	public void changed(int selectionIndex) {
		fireContentsChanged(this, selectionIndex, selectionIndex);
	}

	public void allChanged() {
		fireContentsChanged(this, 0, l.list.size()-1);
	}

	

}
