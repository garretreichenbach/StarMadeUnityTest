package org.schema.game.client.view.tools;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class JTagTreeModel implements TreeModel {

	private Tag root;

	public JTagTreeModel(Tag t) {
		super();
		this.root = t;
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		Tag t = (Tag) arg0;
		if (t.getType() == Type.STRUCT) {
			Tag[] ta = ((Tag[]) t.getValue());
			return ta[arg1];
		}
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		Tag t = (Tag) arg0;
		if (t.getType() == Type.STRUCT) {
			Tag[] ta = ((Tag[]) t.getValue());
			return ta.length;
		} else {
			return 0;
		}
	}

	@Override
	public boolean isLeaf(Object arg0) {
		Tag t = (Tag) arg0;
		return t.getType() != Type.STRUCT;
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {

	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		Tag t = (Tag) arg0;
		if (t.getType() == Type.STRUCT) {
			Tag[] ta = ((Tag[]) t.getValue());
			for (int i = 0; i < ta.length; i++) {
				if (ta[i] == arg1) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener arg0) {

	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {

	}

}
