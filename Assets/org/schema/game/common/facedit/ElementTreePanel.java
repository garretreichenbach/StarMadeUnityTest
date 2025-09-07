package org.schema.game.common.facedit;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public class ElementTreePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTree tree;
	private ElementTreeInterface frame;
	private DefaultTreeModel defaultTreeModel;
	private TreeNode[] selectedLastPath;
	/**
	 * Create the panel.
	 */
	public ElementTreePanel(ElementTreeInterface frame) {
		setLayout(new GridLayout(0, 1, 0, 0));
		
		this.frame = frame;
		ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());

		ElementCategory categoryHirarchy = ElementKeyMap.getCategoryHirarchy();
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		createNodes(top, categoryHirarchy);
		defaultTreeModel = new DefaultTreeModel(top);
		tree = new JTree(defaultTreeModel);
		tree.setCellRenderer(new ElementTreeRenderer());
		add(tree);

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(frame);

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (SwingUtilities.isRightMouseButton(e)) {

						int row = tree.getClosestRowForLocation(e.getX(), e.getY());
						tree.setSelectionRow(row);

						DefaultMutableTreeNode n = (DefaultMutableTreeNode) selPath.getLastPathComponent();

						createPopup(n, e);

					}
				}
			}

		};
		tree.addMouseListener(ml);
		
		if(ElementEditorFrame.currentInfo != null) {
			TreePath path = find(top, ElementEditorFrame.currentInfo.getId());
			if(path != null) {
				System.err.println("FOUND LAST PATH IN TREE");
				tree.setSelectionPath(path);
				tree.scrollPathToVisible(path);
			}else {
				System.err.println("NOT FOUND LAST PATH IN TREE");
			}
		}else {
			System.err.println("NO CURRENT INFO");
		}
	}
	private TreePath find(DefaultMutableTreeNode root, short s) {
	    Enumeration<TreeNode> e = root.depthFirstEnumeration();
	    while (e.hasMoreElements()) {
	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
	        
	        if (node.getUserObject() instanceof ElementInformation && ((ElementInformation)node.getUserObject()).getId() == s) {
	            return new TreePath(node.getPath());
	        }
	    }
	    return null;
	}
	public static void main(String[] asd) {

	}

	private void createPopup(DefaultMutableTreeNode n, MouseEvent e) {
		if (frame.hasPopupMenu() && n != null && n.getUserObject() != null) {
			final JPopupMenu popup = new JPopupMenu();

			Object userObject = n.getUserObject();

			if (userObject instanceof ElementInformation) {
				final ElementInformation info = (ElementInformation) userObject;

				JMenuItem jMenuItem = new JMenuItem("Remove");
				popup.add(jMenuItem);
				jMenuItem.addActionListener(arg0 -> {
					TreePath[] selectionPaths = tree.getSelectionPaths();
					for(TreePath p : selectionPaths){
						System.err.println(p.getLastPathComponent());
					}
					frame.removeEntry(info);
				});
				popup.show(e.getComponent(), e.getX(), e.getY());

			} else if (userObject instanceof ElementCategory) {
				ElementCategory cat = (ElementCategory) userObject;

				cat.addContextMenu(frame, popup, e.getComponent());

				popup.show(e.getComponent(), e.getX(), e.getY());
			}

		}
	}

	private DefaultMutableTreeNode createNodes(DefaultMutableTreeNode top,
	                                           ElementCategory categoryHirarchy) {

		for (ElementCategory c : categoryHirarchy.getChildren()) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(c);
			top.add(createNodes(child, c));
		}
		for (ElementInformation info : categoryHirarchy.getInfoElements()) {
			DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(info);
			top.add(defaultMutableTreeNode);
			
			if(ElementEditorFrame.currentInfo != null) {
				this.selectedLastPath = defaultMutableTreeNode.getPath(); 
			}

		}
		return top;
	}

	public ElementInformation getSelectedItem() {
		if (tree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (node.getUserObject() instanceof ElementInformation) {
				return (ElementInformation) node.getUserObject();
			}
		}
		return null;
	}

	public JTree getTree() {
		return tree;
	}

	private void removeFromTree(DefaultMutableTreeNode n, ElementInformation currentInfo) {
		Enumeration children = n.children();
		int i = 0;
		while(children.hasMoreElements()){
			DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode)children.nextElement();
			if(nextElement.getUserObject() == currentInfo){
				defaultTreeModel.removeNodeFromParent(nextElement);
				return;
			}else{
				removeFromTree(nextElement, currentInfo);
			}
			i++;
		}
	}
	public void removeFromTree(ElementInformation currentInfo) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)tree.getModel().getRoot();
		removeFromTree(n, currentInfo);
		
	}

}
