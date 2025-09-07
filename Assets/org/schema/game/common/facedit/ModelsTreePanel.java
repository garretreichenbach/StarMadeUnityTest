package org.schema.game.common.facedit;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.common.util.data.DataUtil;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.facedit.model.Model;
import org.schema.game.common.facedit.model.ModelCategories;
import org.schema.game.common.facedit.model.ModelCategory;
import org.schema.game.common.facedit.model.SceneFile;
import org.schema.game.common.facedit.model.SceneNode;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModelsTreePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private JTree tree;
	private ModelTreeInterface frame;
	private DefaultTreeModel defaultTreeModel;
	/**
	 * Create the panel.
	 */
	public ModelsTreePanel(ModelTreeInterface frame) {
		
		this.frame = frame;
		
		String path = DataUtil.dataPath + File.separator + DataUtil.configPath;
		File xmlFile = new File(path);
		if(!xmlFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(xmlFile.getAbsolutePath()));
		}
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		try {
			createNodes(top, xmlFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} 
		defaultTreeModel = new DefaultTreeModel(top);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowHeights = new int[]{0,0};
		gridBagLayout.columnWeights = new double[]{0.0};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel optionsPanel = new JPanel();
		GridBagConstraints gbc_optionsPanel = new GridBagConstraints();
		gbc_optionsPanel.anchor = GridBagConstraints.WEST;
		gbc_optionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_optionsPanel.gridx = 0;
		gbc_optionsPanel.gridy = 0;
		add(optionsPanel, gbc_optionsPanel);
		GridBagLayout gbl_optionsPanel = new GridBagLayout();
		gbl_optionsPanel.columnWidths = new int[]{0, 0};
		gbl_optionsPanel.rowHeights = new int[]{0, 0};
		gbl_optionsPanel.columnWeights = new double[]{0.0, 10.0};
		gbl_optionsPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		optionsPanel.setLayout(gbl_optionsPanel);
		
		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(ae -> {
			try {
				doImport();
			}catch(Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(ModelsTreePanel.this,
					    "Import Error: "+e.getClass().getSimpleName() + ": " + e.getMessage(),
					    "Import error",
					    JOptionPane.ERROR_MESSAGE);
			}
		});
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.weighty = 1.0;
		gbc_btnImport.weightx = 1.0;
		gbc_btnImport.anchor = GridBagConstraints.WEST;
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 0;
		optionsPanel.add(btnImport, gbc_btnImport);
		tree = new JTree(defaultTreeModel);
		tree.setCellRenderer(new ElementTreeRenderer());
		GridBagConstraints gbc_tree = new GridBagConstraints();
		gbc_tree.weightx = 1.0;
		gbc_tree.weighty = 10.0;
		gbc_tree.fill = GridBagConstraints.BOTH;
		gbc_tree.gridx = 0;
		gbc_tree.gridy = 1;
		add(tree, gbc_tree);
		
				tree.getSelectionModel().setSelectionMode(
						TreeSelectionModel.SINGLE_TREE_SELECTION);
				
						tree.setDragEnabled(true);
						tree.setTransferHandler(new TreeTransferHandler());
						tree.setDropMode(DropMode.INSERT);
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
		
		
		
	}
	File currentChooserPath;
	private void doImport() throws SAXException, IOException, ParserConfigurationException, ResourceException {
		JFileChooser fileChooser = new JFileChooser(currentChooserPath != null ? currentChooserPath : new File("."+File.separator+DataUtil.dataPath));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return ".scene";
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".scene");
			}
		});
		
		int option = fileChooser.showOpenDialog(ModelsTreePanel.this);
		if(option == JFileChooser.APPROVE_OPTION) {
			File origSceneFile = fileChooser.getSelectedFile();
			
			currentChooserPath = origSceneFile.getParentFile();
			
			ModelCategory[] cats = new ModelCategory[categories.models.size()];
			
			int i = 0;
			
			for(ModelCategory m : categories.models.values()) {
				cats[i++] = m;
			}
			
			ModelCategory cat = (ModelCategory)JOptionPane.showInputDialog(
					ModelsTreePanel.this,
                    "Select a model category to add to.",
                    "Import",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    cats,
                    cats[0]);
			
			
			if(cat == null) {
				return;
			}
			
			String modelName = getUniqueModelName("", cat, "Model Name", "Select a model name (no spaces, must be unique).\nThis will be the name of the model ingame and in all game configs");

			String relPath = getUniqueModelName("", cat, "Subfolder", "Select a subfolder (press cancel to skip and use base directory)");
			
			SceneFile scene = new SceneFile(origSceneFile, origSceneFile.getName().substring(0, origSceneFile.getName().lastIndexOf(".")));
			
			String targetDir = "."+File.separator+DataUtil.dataPath+cat.path+File.separator+relPath+File.separator;
			File subDir = new File(targetDir);
			subDir.mkdirs();
			
			File sceneFile = new File(targetDir+origSceneFile.getName());
			
			File originalMaterialFile = new File(origSceneFile.getAbsolutePath().substring(0, origSceneFile.getAbsolutePath().lastIndexOf("."))+".material");
			
			for(SceneNode n : scene.sceneNodes) {
				if(n.entity != null && n.entity.meshFile != null) {
					File originalMeshFile = new File(origSceneFile.getParentFile().getAbsolutePath()+File.separator+n.entity.meshFile+".xml");
					FileUtil.copyFileIfDifferentPath(originalMeshFile, new File(targetDir+originalMeshFile.getName()));
					
					if(n.entity.material.diffuseTexture != null && n.entity.material.diffuseTexture.trim().length() > 0) {
						File orig = new File(origSceneFile.getParentFile().getAbsolutePath()+File.separator+n.entity.material.diffuseTexture);
						FileUtil.copyFileIfDifferentPath(orig, new File(targetDir+orig.getName()));
					}
					if(n.entity.material.normalTexture != null && n.entity.material.normalTexture.trim().length() > 0) {
						File orig = new File(origSceneFile.getParentFile().getAbsolutePath()+File.separator+n.entity.material.normalTexture);
						FileUtil.copyFileIfDifferentPath(orig, new File(targetDir+orig.getName()));
					}
					if(n.entity.material.emissionTexture != null && n.entity.material.emissionTexture.trim().length() > 0) {
						File orig = new File(origSceneFile.getParentFile().getAbsolutePath()+File.separator+n.entity.material.emissionTexture);
						FileUtil.copyFileIfDifferentPath(orig, new File(targetDir+orig.getName()));
					}
				}
			}
			FileUtil.copyFileIfDifferentPath(originalMaterialFile, new File(targetDir+originalMaterialFile.getName()));
			FileUtil.copyFileIfDifferentPath(origSceneFile, new File(targetDir+origSceneFile.getName()));
			
			Model m = new Model(modelName);
			m.relpath = relPath;
			m.filename = origSceneFile.getName();
			m.cat = cat;
			m.initialize(null);
			cat.models.add(m);
			
			defaultTreeModel.insertNodeInto(new DefaultMutableTreeNode(m), cat.treeNode, cat.treeNode.getChildCount());
			frame.addEntry(m);
		}
	}
	
	
	public String getRelativePath(String def, ModelCategory cat, String title, String desc) {
		String s = null;
		
		
		while(s == null) {
			s = (String)JOptionPane.showInputDialog(
                  ModelsTreePanel.this,
                  desc,
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    def);
			if(s == null) {
				return ".";
			}
			if(s.trim().length() == 0){
				System.err.println("ERROR: EMPTY NAME");
				s = null;
				continue;
			}
			if(!s.matches("(\\w|_|:)(\\w|\\d|\\.|-|_|:)*")) {
				System.err.println("ERROR: NOT A FILE PATTERN: "+s);
				s = null;
				continue;
			}
		}
		return s;
	}
	public String getUniqueModelName(String def, ModelCategory cat, String title, String desc) {
		String s = null;
		
		
		while(s == null) {
			s = (String)JOptionPane.showInputDialog(
                  ModelsTreePanel.this,
                  desc,
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    def);
			if(s.trim().length() == 0){
				System.err.println("ERROR: EMPTY NAME");
				s = null;
				continue;
			}
			if(!s.matches("(\\w|_|:)(\\w|\\d|\\.|-|_|:)*")) {
				System.err.println("ERROR: NOT A NODE PATTERN: "+s);
				s = null;
				continue;
			}
			for(Model m : cat.models) {
				if(m.name.toLowerCase(Locale.ENGLISH).equals(s.toLowerCase(Locale.ENGLISH))) {
					s = null;
					break;
				}
			}
		}
		return s;
	}
	
	class TreeTransferHandler extends TransferHandler {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 56784844527399581L;
		DataFlavor nodesFlavor;
	    DataFlavor[] flavors = new DataFlavor[1];
	    DefaultMutableTreeNode[] nodesToRemove;

	    public TreeTransferHandler() {
	        try {
	            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
	                              ";class=\"" +
	                javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
	                              "\"";
	            nodesFlavor = new DataFlavor(mimeType);
	            flavors[0] = nodesFlavor;
	        } catch(ClassNotFoundException e) {
	            System.out.println("ClassNotFound: " + e.getMessage());
	        }
	    }

	    public boolean canImport(TransferHandler.TransferSupport support) {
	        if(!support.isDrop()) {
	            return false;
	        }
	        support.setShowDropLocation(true);
	        if(!support.isDataFlavorSupported(nodesFlavor)) {
	            return false;
	        }
	        // Do not allow a drop on the drag source selections.
	        JTree.DropLocation dl =
	                (JTree.DropLocation)support.getDropLocation();
	        JTree tree = (JTree)support.getComponent();
	        int dropRow = tree.getRowForPath(dl.getPath());
	        int[] selRows = tree.getSelectionRows();
	        for(int i = 0; i < selRows.length; i++) {
	            if(selRows[i] == dropRow) {
	                return false;
	            }
	        }
	        // Do not allow MOVE-action drops if a non-leaf node is
	        // selected unless all of its children are also selected.
	        int action = support.getDropAction();
	        if(action == MOVE) {
	            return haveCompleteNode(tree);
	        }
	        // Do not allow a non-leaf node to be copied to a level
	        // which is less than its source level.
	        TreePath dest = dl.getPath();
	        DefaultMutableTreeNode target =
	            (DefaultMutableTreeNode)dest.getLastPathComponent();
	        TreePath path = tree.getPathForRow(selRows[0]);
	        DefaultMutableTreeNode firstNode =
	            (DefaultMutableTreeNode)path.getLastPathComponent();
	        if(firstNode.getChildCount() > 0 &&
	               target.getLevel() < firstNode.getLevel()) {
	            return false;
	        }
	        return true;
	    }

	    private boolean haveCompleteNode(JTree tree) {
	        int[] selRows = tree.getSelectionRows();
	        TreePath path = tree.getPathForRow(selRows[0]);
	        DefaultMutableTreeNode first =
	            (DefaultMutableTreeNode)path.getLastPathComponent();
	        int childCount = first.getChildCount();
	        // first has children and no children are selected.
	        if(childCount > 0 && selRows.length == 1)
	            return false;
	        // first may have children.
	        for(int i = 1; i < selRows.length; i++) {
	            path = tree.getPathForRow(selRows[i]);
	            DefaultMutableTreeNode next =
	                (DefaultMutableTreeNode)path.getLastPathComponent();
	            if(first.isNodeChild(next)) {
	                // Found a child of first.
	                if(childCount > selRows.length-1) {
	                    // Not all children of first are selected.
	                    return false;
	                }
	            }
	        }
	        return true;
	    }

	    protected Transferable createTransferable(JComponent c) {
	        JTree tree = (JTree)c;
	        TreePath[] paths = tree.getSelectionPaths();
	        if(paths != null) {
	            // Make up a node array of copies for transfer and
	            // another for/of the nodes that will be removed in
	            // exportDone after a successful drop.
	            List<DefaultMutableTreeNode> copies =
	                new ArrayList<DefaultMutableTreeNode>();
	            List<DefaultMutableTreeNode> toRemove =
	                new ArrayList<DefaultMutableTreeNode>();
	            DefaultMutableTreeNode node =
	                (DefaultMutableTreeNode)paths[0].getLastPathComponent();
	            DefaultMutableTreeNode copy = copy(node);
	            copies.add(copy);
	            toRemove.add(node);
	            for(int i = 1; i < paths.length; i++) {
	                DefaultMutableTreeNode next =
	                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
	                // Do not allow higher level nodes to be added to list.
	                if(next.getLevel() < node.getLevel()) {
	                    break;
	                } else if(next.getLevel() > node.getLevel()) {  // child node
	                    copy.add(copy(next));
	                    // node already contains child
	                } else {                                        // sibling
	                    copies.add(copy(next));
	                    toRemove.add(next);
	                }
	            }
	            DefaultMutableTreeNode[] nodes =
	                copies.toArray(new DefaultMutableTreeNode[copies.size()]);
	            nodesToRemove =
	                toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
	            return new NodesTransferable(nodes);
	        }
	        return null;
	    }

	    /** Defensive copy used in createTransferable. */
	    private DefaultMutableTreeNode copy(TreeNode node) {
	        return new DefaultMutableTreeNode(node);
	    }

	    protected void exportDone(JComponent source, Transferable data, int action) {
	        if((action & MOVE) == MOVE) {
	            JTree tree = (JTree)source;
	            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	            // Remove nodes saved in nodesToRemove in createTransferable.
	            for(int i = 0; i < nodesToRemove.length; i++) {
	                model.removeNodeFromParent(nodesToRemove[i]);
	            }
	        }
	    }

	    public int getSourceActions(JComponent c) {
	        return COPY_OR_MOVE;
	    }

	    public boolean importData(TransferHandler.TransferSupport support) {
	        if(!canImport(support)) {
	            return false;
	        }
	        // Extract transfer data.
	        DefaultMutableTreeNode[] nodes = null;
	        try {
	            Transferable t = support.getTransferable();
	            nodes = (DefaultMutableTreeNode[])t.getTransferData(nodesFlavor);
	        } catch(UnsupportedFlavorException ufe) {
	            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
	        } catch(java.io.IOException ioe) {
	            System.out.println("I/O error: " + ioe.getMessage());
	        }
	        // Get drop location info.
	        JTree.DropLocation dl =
	                (JTree.DropLocation)support.getDropLocation();
	        int childIndex = dl.getChildIndex();
	        TreePath dest = dl.getPath();
	        DefaultMutableTreeNode parent =
	            (DefaultMutableTreeNode)dest.getLastPathComponent();
	        JTree tree = (JTree)support.getComponent();
	        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	        // Configure for drop mode.
	        int index = childIndex;    // DropMode.INSERT
	        if(childIndex == -1) {     // DropMode.ON
	            index = parent.getChildCount(); 
	        } 
	        try { 
		        // Add data to model.
		        for(int i = 0; i < nodes.length; i++) {
		        	nodes[i].setUserObject(((DefaultMutableTreeNode)nodes[i].getUserObject()).getUserObject());
		        	System.err.println("TRANSFER "+nodes[i].getUserObject());
		        	Model m = ((Model)nodes[i].getUserObject());
		        	
		        	if(m.cat != ((ModelCategory)parent.getUserObject())) {
		        		System.err.println("CANNOT DROP INTO DIFFERENT CAT");
		        		return false;
		        	}
		        	
		        	m.cat.models.remove(m);
		        	
		        	((ModelCategory)parent.getUserObject()).models.add(index, m);
		            model.insertNodeInto(nodes[i], parent, index++);
		            
		        }
	        }catch(RuntimeException e) {
	        	e.printStackTrace();
	        	throw e;
	        }
//	        if(parent.getUserObject() instanceof ModelCategory) {
//	        	((ModelCategory)parent.getUserObject()).sortOn(parent);
//	        }
	        return true;
	    }

	    public String toString() {
	        return getClass().getName();
	    }

	    public class NodesTransferable implements Transferable {
	        DefaultMutableTreeNode[] nodes;

	        public NodesTransferable(DefaultMutableTreeNode[] nodes) {
	            this.nodes = nodes;
	         }

	        public Object getTransferData(DataFlavor flavor)
	                                 throws UnsupportedFlavorException {
	            if(!isDataFlavorSupported(flavor))
	                throw new UnsupportedFlavorException(flavor);
	            return nodes;
	        }

	        public DataFlavor[] getTransferDataFlavors() {
	            return flavors;
	        }

	        public boolean isDataFlavorSupported(DataFlavor flavor) {
	            return nodesFlavor.equals(flavor);
	        }
	    }
	}
	private void createPopup(final DefaultMutableTreeNode n, MouseEvent e) {
		if (frame.hasPopupMenu() && n != null && n.getUserObject() != null) {
			final JPopupMenu popup = new JPopupMenu();

			Object userObject = n.getUserObject();

			if (userObject instanceof Model) {
				final Model info = (Model) userObject;

				{
					JMenuItem jMenuItem = new JMenuItem("Remove");
					popup.add(jMenuItem);
					jMenuItem.addActionListener(arg0 -> {
						TreePath[] selectionPaths = tree.getSelectionPaths();
						for(TreePath p : selectionPaths){
							System.err.println(p.getLastPathComponent());
						}
						defaultTreeModel.removeNodeFromParent(n);
						info.cat.models.remove(info);
						frame.removeEntry(info);
					});
				}
				{
					JMenuItem jMenuItem = new JMenuItem("Duplicate");
					popup.add(jMenuItem);
					jMenuItem.addActionListener(arg0 -> {
						TreePath[] selectionPaths = tree.getSelectionPaths();
						for(TreePath p : selectionPaths){
							System.err.println(p.getLastPathComponent());
						}
						String s = getUniqueModelName(info.name, info.cat, "Duplicate", "Please enter a name for the duplicate (must be unique)\nNote, this will create a new scene file when saved.");

						Model m = new Model(info, s);
						try {
							//parse with material of the old scene (will write to new)
							m.initialize(info.sceneFile.getName().substring(0, info.sceneFile.getName().lastIndexOf(".")));

							info.cat.models.add(m);
							defaultTreeModel.insertNodeInto(new DefaultMutableTreeNode(m), (DefaultMutableTreeNode)n.getParent(), n.getParent().getIndex(n)+1);
							frame.addEntry(m);
						}catch(Exception e1) {
							e1.printStackTrace();
							 Object[] options = {"Skip Entry"};
							JOptionPane.showOptionDialog( ModelsTreePanel.this, "Couldn't load scene for model "+m+"\n\n"+ e1.getClass().getSimpleName() + ": " + e1.getMessage(), "ERROR",
			                         JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
			                         null, options, options[0]);
						}
					});
				}
				popup.show(e.getComponent(), e.getX(), e.getY());

			} 

		}
	}

	private DefaultMutableTreeNode createNodes(DefaultMutableTreeNode top,
	                                           File xmlFile) throws IOException, ParserConfigurationException, SAXException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(xmlFile));
		Document root = db.parse(bufferedInputStream);
		bufferedInputStream.close();
		
		categories.clear();
		categories.doc = root;
		categories.file = xmlFile;
		parseRec(this, root.getDocumentElement(), categories, null);
		
		createTree(top, categories);
		
		return top;
	}
	private void createTree(DefaultMutableTreeNode top, ModelCategories categories) {
		for(ModelCategory c : categories.models.values()) {
			DefaultMutableTreeNode topChild = new DefaultMutableTreeNode(c);
			c.treeNode = topChild;
			top.add(topChild);
			
			for(Model m : c.models) {
				DefaultMutableTreeNode modelChild = new DefaultMutableTreeNode(m);
				topChild.add(modelChild);
				
			}
		}
	}
	public static ModelCategories categories  = new ModelCategories();

	
	
	
	public static void parseRec(Component parentForError, Node root, ModelCategories categories, ModelCategory cur) {
		if(root.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		
		if(root.getNodeName().toLowerCase(Locale.ENGLISH).equals("character")) {
			return;
		}
		NamedNodeMap at = root.getAttributes();
		if(at.getNamedItem("path") != null) {
			ModelCategory c = new ModelCategory();
			c.node = root;
			c.name = root.getNodeName();
			c.path = at.getNamedItem("path").getNodeValue();
			categories.models.put(c.name, c);
			cur = c;
		}
		
		if(cur != null && at.getNamedItem("filename") != null) {
			Model m = new Model(root.getNodeName());
			m.cat = cur;
			m.filename = at.getNamedItem("filename").getNodeValue();
			if(!m.filename.toLowerCase(Locale.ENGLISH).endsWith(".scene")) {
				m.filename = m.filename+".scene";
			}
				
			if(at.getNamedItem("relpath") != null) {
				m.relpath = at.getNamedItem("relpath").getNodeValue();
			}
			if(at.getNamedItem("physicsmesh") != null) {
				m.physicsmesh = at.getNamedItem("physicsmesh").getNodeValue();
			}
			try {
				m.initialize(null);
				m.cat.models.add(m);
			}catch(Exception e) {
				e.printStackTrace();
				 Object[] options = {"Skip Entry"};
				JOptionPane.showOptionDialog(parentForError, "Couldn't load scene for model "+m+"\n\n"+e.getClass().getSimpleName() + ": " + e.getMessage(), "ERROR",
                         JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
                         null, options, options[0]);
			}
		}
		
		NodeList childNodes = root.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			parseRec(parentForError, node, categories, cur);
		}
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

	public void refresh() {
		
	}

	public void save() throws IOException, ResourceException, ParserConfigurationException, TransformerException {
		categories.save();
		frame.refresh();
	}

}
