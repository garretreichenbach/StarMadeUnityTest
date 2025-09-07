package org.schema.schine.sound.controller.gui;

import java.util.Enumeration;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.asset.AudioAsset;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AudioAssetTreeContainer {
	
	private List<AudioAssetTreeCon> cons = new ObjectArrayList<>();

	public void register(AudioAssetTreeCon con) {
		cons.add(con);
	}
	
	JTree tree;
	
	
	
	void createTree(DefaultMutableTreeNode root) {
		this.tree = new JTree(root);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new AudioAssetTreeTransferHandler());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
tree.getLastSelectedPathComponent();
			if(node != null) {
				if(node.getUserObject() instanceof AudioAsset) {
					AudioController.instance.onSelectedAsset((AudioAsset)node.getUserObject());
				}
			}
		});
		
		expandTree(tree);
		for(AudioAssetTreeCon c : cons) {
			c.applyTree(tree);
		}
	}
	private void expandTree(JTree tree) {
        DefaultMutableTreeNode root =
            (DefaultMutableTreeNode)tree.getModel().getRoot();
        Enumeration<?> e = root.breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)e.nextElement();
            if(node.isLeaf()) continue;
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            tree.expandRow(row);
        }
    }
}
