package org.schema.game.common.facedit.model;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.schine.graphicsengine.core.ResourceException;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ModelCategory{
	public String name;
	public String path;
	public List<Model> models = new ObjectArrayList<Model>();
	public Node node;
	public DefaultMutableTreeNode treeNode;
	
	public String toString(){
		return name+"("+path+")";
	}

	
	public class ModelComp implements Comparator<Model>{
		DefaultMutableTreeNode parent;
		
		public ModelComp(DefaultMutableTreeNode parent) {
			super();
			this.parent = parent;
		}

		@Override
		public int compare(Model a, Model b) {
			return a.getIndexOf(parent) - b.getIndexOf(parent);
		}
	}


	public void save() throws IOException, ResourceException, ParserConfigurationException, TransformerException {
		System.err.println("Saving "+name+"; "+path);
		for(Model m : models) {
			m.save();
		}
		List<Node> cc = new ObjectArrayList<Node>();
		for(int i = 0; i < node.getChildNodes().getLength(); i++) {
			cc.add(node.getChildNodes().item(i));
		}
		//remove all childs
		for(Node n : cc) {
			node.removeChild(n);
		}
		
		for(Model m : models) {
			Node newNode = m.createNode(node.getOwnerDocument());
			node.appendChild(newNode);
			assert(newNode.getParentNode() == node);
			
		}
	}
	
}
