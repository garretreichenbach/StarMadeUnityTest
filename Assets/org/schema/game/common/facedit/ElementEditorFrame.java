package org.schema.game.common.facedit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.facedit.craft.BlockTable;
import org.schema.game.common.facedit.importer.ImportConfigDialog;
import org.schema.game.common.facedit.model.Model;
import org.schema.game.common.facedit.model.ModelCategory;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.FileExt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class ElementEditorFrame extends JFrame implements Observer, ElementTreeInterface {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static ElementInformation currentInfo;
	/**
	 *
	 */

	private final JPanel contentPane;
	private JSplitPane blocksSplitPane;
	private final JMenuBar menuBar;
	private final JMenu mnFile;
	private final JMenu mnEdit;
	private final JMenuItem mntmNew;
	private final JMenuItem mntmSave;
	private final JMenuItem mntmSaveAs;
	private final JMenuItem mntmSaveOnlyBlockConfig;
	private final JMenuItem mntmSaveOnlyModels;
	private final JMenuItem mntmLoad;
	private final JMenuItem mntmExit;
	private final JMenuItem mntmMoveEntry;
	private final JMenuItem mntmDuplicateEntry;
	private final EditorData data = new EditorData();
	private final JMenuItem mntmAddCategory;
	private final JMenuItem mntmRemoveCategory;
	private final JMenuItem mntmCreateNewEntry;
	private final JMenuItem mntmRemoveEntry;
	private final JMenuItem mntmRemoveDuplicteBuildicon;
	private final JTabbedPane tabbedPane;
	private final JSplitPane recipeSplitPane;
	private final JScrollPane scrollPane;
	private final JLabel lblNewLabel;
	private final JMenu mnBlocks;
	private final JMenu mnRecipe;
	private final JMenuItem mntmCreateNew;
	private final JMenuItem mntmRemove;
	private RecipeJList recipeJList;
	private final JPanel pricesPanel;
	private final BlockTable blockTable;
	private final JMenuItem mntmNewMenuItem;
	private final JMenuItem mntmImportFromAnother;
	private final JMenuItem mntmChamberGraph;
	private final ElementTreePanel factoryEditorElementOverviewPanel;
	/**
	 * Create the frame.
	 */
	private final JMenuItem mntmCreateCleanUp;
	private final JSplitPane modelsPane;
	private final JScrollPane modelListScroll;
	private ModelsTreePanel modelsTreePanel;
	private final JPanel modelsPanelRight;
	private final JScrollPane modelsPanelRightScrollable;
	private JFileChooser fc;
	private FixedRecipe fixedRecipe;
	private int dividerLocation = 350;

	public ElementEditorFrame() {
		setTitle("StarMade - Block Editor");

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1200, 700);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(arg0 -> {
			Object[] options = {"Ok", "Cancel"};
			int n = JOptionPane.showOptionDialog(ElementEditorFrame.this, "This will remove all current data", "New",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[1]);
			switch (n) {
				case 0:
					ElementKeyMap.clear();
					ElementTreePanel factoryEditorElementOverviewPanel = new ElementTreePanel(ElementEditorFrame.this);
					blocksSplitPane.setLeftComponent(new JScrollPane(factoryEditorElementOverviewPanel));
					blocksSplitPane.setRightComponent(new JLabel("nothing selected"));
					break;
				case 1:
					break;
			}

		});
		mnFile.add(mntmNew);

		mntmLoad = new JMenuItem("Open");
		mntmLoad.addActionListener(arg0 -> {
			File f = chooseFile(ElementEditorFrame.this, "Open");
			ElementKeyMap.clear();
			ElementKeyMap.reinitializeData(f, false, null, null, false);
			ElementTreePanel factoryEditorElementOverviewPanel = new ElementTreePanel(ElementEditorFrame.this);
			blocksSplitPane.setLeftComponent(new JScrollPane(factoryEditorElementOverviewPanel));
			blocksSplitPane.setRightComponent(new JLabel("nothing selected"));
		});
		mnFile.add(mntmLoad);

		mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(arg0 -> {
			ElementKeyMap.writeDocument(data.filePath, ElementKeyMap.getCategoryHirarchy(), ElementKeyMap.fixedRecipes);
			try {
				modelsTreePanel.save();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(ElementEditorFrame.this,
					    "An error happened:\n\n"+e.getClass().getSimpleName()+"\n"+e.getMessage(),
					    "Save Error",
					    JOptionPane.ERROR_MESSAGE);
			}
		});
		mnFile.add(mntmSave);

		mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File f = chooseFile(ElementEditorFrame.this, "Save As...");
				if(f != null) {
					ElementKeyMap.writeDocument(f, ElementKeyMap.getCategoryHirarchy(), ElementKeyMap.fixedRecipes);
				}
			}
		});
		mnFile.add(mntmSaveAs);


		mntmSaveOnlyBlockConfig = new JMenuItem("Save only BlockConfig");
		mntmSaveOnlyBlockConfig.addActionListener(arg0 -> ElementKeyMap.writeDocument(data.filePath, ElementKeyMap.getCategoryHirarchy(), ElementKeyMap.fixedRecipes));
		mnFile.add(mntmSaveOnlyBlockConfig);
		mntmSaveOnlyModels = new JMenuItem("Save only Models");
		mntmSaveOnlyModels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					modelsTreePanel.save();
				} catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(ElementEditorFrame.this, "An error happened:\n\n" + e.getClass().getSimpleName() + "\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mnFile.add(mntmSaveOnlyModels);

		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(arg0 -> dispose());
		mnFile.add(mntmExit);

		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		mntmImportFromAnother = new JMenuItem("Import from another");
		mntmImportFromAnother.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fc == null) {
					fc = new JFileChooser(new FileExt("./"));
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fc.setAcceptAllFileFilterUsed(false);
					fc.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File arg0) {
							if(arg0.isDirectory()) {
								return true;
							}
							return arg0.getName().endsWith(".xml");
						}

					@Override
					public String getDescription() {
						return "StarMade BlockConfig (.xml)";
					}
				});
				fc.setAcceptAllFileFilterUsed(false);
			}
			//Show it.
			int returnVal = fc.showDialog(ElementEditorFrame.this, "Choose import");

				//Process the results.
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					ImportConfigDialog p = new ImportConfigDialog(ElementEditorFrame.this, file);
					p.setVisible(true);
				} else {
				}
			}
		});
		mnEdit.add(mntmImportFromAnother);

		mnBlocks = new JMenu("Blocks");
		mnEdit.add(mnBlocks);


		mntmCreateNewEntry = new JMenuItem("Create New Entry");
		mnBlocks.add(mntmCreateNewEntry);

		mntmMoveEntry = new JMenuItem("Move Entry");
		mnBlocks.add(mntmMoveEntry);

		mntmDuplicateEntry = new JMenuItem("Duplicate Entry");
		mnBlocks.add(mntmDuplicateEntry);

		mntmRemoveEntry = new JMenuItem("Remove Entry");
		mnBlocks.add(mntmRemoveEntry);
		mntmRemoveEntry.setEnabled(currentInfo != null);

		mntmAddCategory = new JMenuItem("Add Category");
		mnBlocks.add(mntmAddCategory);
		mntmAddCategory.addActionListener(e -> {
			//Create a new pane to select category parent and name
			(new NewCategoryDialog(this)).setVisible(true);
		});

		mntmRemoveCategory = new JMenuItem("Remove Category");
		mnBlocks.add(mntmRemoveCategory);
		mntmRemoveCategory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				(new RemoveCategoryDialog(ElementEditorFrame.this)).setVisible(true);
			}
		});

		mntmRemoveDuplicteBuildicon = new JMenuItem("Remove Duplicte BuildIcon IDs");
		mnBlocks.add(mntmRemoveDuplicteBuildicon);

		mntmCreateCleanUp = new JMenuItem("Clean up IDs");
		mnBlocks.add(mntmCreateCleanUp);
		mntmCreateCleanUp.addActionListener(e -> {

			Object[] options = {"Ok", "Cancel"};
			int n = JOptionPane.showOptionDialog(this, "Clean up?", "Clean up block ids?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			switch(n) {
				case 0:
					try {
						ElementKeyMap.cleanUpUnusedBlockIds();
						reinitializeElements();
					} catch(IOException e1) {
						e1.printStackTrace();
					}
					break;
				case 1:
					break;
			}
		});


		mnRecipe = new JMenu("Recipe");
		mnEdit.add(mnRecipe);

		mntmCreateNew = new JMenuItem("Create New");
		mntmCreateNew.addActionListener(e -> recipeJList.addNew());
		mnRecipe.add(mntmCreateNew);

		mntmRemove = new JMenuItem("Remove");
		mntmRemove.addActionListener(e -> recipeJList.removeSelected());
		mnRecipe.add(mntmRemove);

		mntmNewMenuItem = new JMenuItem("Import mindmap");
		mntmNewMenuItem.addActionListener(arg0 -> {

				MindMapParser p = new MindMapParser();
				Object[] options = {"Ok", "Cancel"};
				p.loadWithFileChooser(ElementEditorFrame.this);
				int n = JOptionPane.showOptionDialog(ElementEditorFrame.this, "Apply MindMap?", "Apply MindMap?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				switch(n) {
					case 0:
						p.apply();
						break;
					case 1:
						break;
				}

		});
		mnRecipe.add(mntmNewMenuItem);

		mntmChamberGraph = new JMenuItem("Chamber Graph");
		mntmChamberGraph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentInfo != null) {
					JDialog d = new JDialog(ElementEditorFrame.this, "graph", true);
					d.setSize(1000, 800);
					d.setContentPane(currentInfo.getChamberGraph());
					d.setVisible(true);
				}
			}
		});
		mntmChamberGraph.setEnabled(currentInfo != null && currentInfo.isReactorChamberGeneral());
		mnEdit.add(mntmChamberGraph);
		mntmRemoveDuplicteBuildicon.addActionListener(e -> {
			Object[] options = {"Ok", "Cancel"};
			String title = "Remove";
			final JFrame jFrame = new JFrame(title);
			jFrame.setUndecorated(true); // set frame undecorated, so the frame
			// itself is invisible
			SwingUtilities.invokeLater(() -> jFrame.setVisible(true));
			// appears in the task bar
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			jFrame.setLocation(screenSize.width / 2, screenSize.height / 2);
			int n = JOptionPane.showOptionDialog(jFrame, "Are you sure you want to remove duplicate icon ids?", "Confirm Remove",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[1]);
			switch (n) {
				case 0:
					ElementKeyMap.removeDuplicateBuildIcons();
					break;
				case 1:
					break;
			}

		});
		mntmRemoveEntry.addActionListener(e -> removeEntry(currentInfo));
		mntmDuplicateEntry.addActionListener(arg0 -> {
		});
		mntmMoveEntry.addActionListener(arg0 -> {
		});
		mntmCreateNewEntry.addActionListener(arg0 -> {
			AddElementEntryDialog diag = new AddElementEntryDialog(ElementEditorFrame.this, info -> {
				ElementTreePanel factoryEditorElementOverviewPanel = new ElementTreePanel(ElementEditorFrame.this);
				blocksSplitPane.setLeftComponent(new JScrollPane(factoryEditorElementOverviewPanel));
			});
			diag.setVisible(true);
		});
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		blocksSplitPane = new JSplitPane();
		tabbedPane.addTab(Lng.str("Blocks"), null, blocksSplitPane, null);

		blocksSplitPane.setDividerLocation(dividerLocation);
		blocksSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, pce -> {
	        dividerLocation = ((Integer)pce.getNewValue()).intValue();
	        System.err.println("DIV LOC: "+dividerLocation);
        });
		factoryEditorElementOverviewPanel = new ElementTreePanel(this);
		blocksSplitPane.setLeftComponent(new JScrollPane(factoryEditorElementOverviewPanel));
		blocksSplitPane.setRightComponent(new JLabel("nothing selected"));

		recipeSplitPane = new JSplitPane();
		tabbedPane.addTab(Lng.str("Recipes"), null, recipeSplitPane, null);

		scrollPane = new JScrollPane();
		recipeSplitPane.setLeftComponent(scrollPane);
		recipeSplitPane.setDividerLocation(250);
		recipeJList = new RecipeJList(e -> {
			fixedRecipe = recipeJList.getSelected();
			if (fixedRecipe != null) {
				recipeSplitPane.setRightComponent(new RecipePanel(ElementEditorFrame.this, fixedRecipe));
				recipeSplitPane.setDividerLocation(250);
				ElementEditorFrame.this.repaint();
			}

		});
		scrollPane.setViewportView(recipeJList);

		lblNewLabel = new JLabel("nothing selected");
		recipeSplitPane.setRightComponent(lblNewLabel);

		pricesPanel = new JPanel();
		tabbedPane.addTab(Lng.str("Price Table"), null, pricesPanel, null);
		GridBagLayout gbl_pricesPanel = new GridBagLayout();
		gbl_pricesPanel.columnWidths = new int[] {0, 0};
		gbl_pricesPanel.rowHeights = new int[] {0, 0};
		gbl_pricesPanel.columnWeights = new double[] {1.0, Double.MIN_VALUE};
		gbl_pricesPanel.rowWeights = new double[] {1.0, Double.MIN_VALUE};
		pricesPanel.setLayout(gbl_pricesPanel);

		blockTable = new BlockTable(this);
		GridBagConstraints gbc_blockTable = new GridBagConstraints();
		gbc_blockTable.fill = GridBagConstraints.BOTH;
		gbc_blockTable.gridx = 0;
		gbc_blockTable.gridy = 0;
		pricesPanel.add(blockTable, gbc_blockTable);

		modelsPane = new JSplitPane();
		modelsPane.setDividerLocation(200);
		tabbedPane.addTab("Models", null, modelsPane, "3D Models etc...");

		modelListScroll = new JScrollPane();
		modelsPane.setLeftComponent(modelListScroll);

		modelsTreePanel = new ModelsTreePanel(new ModelTreeListener());
		modelListScroll.setViewportView(modelsTreePanel);

		modelsPanelRight = new JPanel();
		modelsPane.setRightComponent(modelsPanelRight);
		GridBagLayout gbl_modelsPanelRight = new GridBagLayout();
		gbl_modelsPanelRight.columnWidths = new int[] {0, 0};
		gbl_modelsPanelRight.rowHeights = new int[] {0, 0};
		gbl_modelsPanelRight.columnWeights = new double[] {1.0, Double.MIN_VALUE};
		gbl_modelsPanelRight.rowWeights = new double[] {1.0, Double.MIN_VALUE};
		modelsPanelRight.setLayout(gbl_modelsPanelRight);

		modelsPanelRightScrollable = new JScrollPane();
		GridBagConstraints gbc_modelsPanelRightScrollable = new GridBagConstraints();
		gbc_modelsPanelRightScrollable.fill = GridBagConstraints.BOTH;
		gbc_modelsPanelRightScrollable.gridx = 0;
		gbc_modelsPanelRightScrollable.gridy = 0;
		modelsPanelRight.add(modelsPanelRightScrollable, gbc_modelsPanelRightScrollable);

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				ElementEditorFrame frame = new ElementEditorFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
		});
	}

	public static String openSelectModelDialog(Component frame, String categoryFilterIgnoreCase) {

		List<String> models = new ObjectArrayList<String>();


		for(ModelCategory c : ModelsTreePanel.categories.models.values()) {
			if(categoryFilterIgnoreCase == null || categoryFilterIgnoreCase.trim().length() == 0 || c.name.toLowerCase(Locale.ENGLISH).contains(categoryFilterIgnoreCase.toLowerCase(Locale.ENGLISH))) {
				for(Model m : c.models) {
					models.add(m.name);
				}
			}
		}
		String[] possibilities = models.toArray(new String[models.size()]);

		String s = (String) JOptionPane.showInputDialog(frame, "Select a model.", "Model Select", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);

		return s;
	}

	@Override
	public void removeEntry(ElementInformation currentInfo) {
		if(currentInfo != null) {
			Object[] options = {"Ok", "Cancel"};
			String title = "Remove";
			final JFrame jFrame = new JFrame(title);
			jFrame.setUndecorated(true); // set frame undecorated, so the frame
			// itself is invisible
			SwingUtilities.invokeLater(() -> jFrame.setVisible(true));
			// appears in the task bar
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			jFrame.setLocation(screenSize.width / 2, screenSize.height / 2);
			int n = JOptionPane.showOptionDialog(jFrame, "Are you sure you want to remove " + currentInfo.getName() + "?", "Confirm Remove", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			switch(n) {
				case 0:
					ElementKeyMap.removeFromExisting(currentInfo);

					factoryEditorElementOverviewPanel.removeFromTree(currentInfo);
//					ElementTreePanel factoryEditorElementOverviewPanel = new ElementTreePanel(ElementEditorFrame.this);
//					blocksSplitPane.setLeftComponent(new JScrollPane(factoryEditorElementOverviewPanel));
					blocksSplitPane.setRightComponent(new JLabel("nothing selected"));
					break;
				case 1:
					break;
			}
		}
	}

	@Override
	public boolean hasPopupMenu() {
		return true;
	}

	@Override
	public void reinitializeElements() {
		ElementTreePanel factoryEditorElementOverviewPanel = new ElementTreePanel(this);
		blocksSplitPane.setLeftComponent(new JScrollPane(factoryEditorElementOverviewPanel));
	}

	private File chooseFile(JFrame frame, String title) {
		if(fc == null) {
			fc = new JFileChooser(new FileExt("./"));
			fc.addChoosableFileFilter(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					if(arg0.isDirectory()) {
						return true;
					}
					return arg0.getName().endsWith(".xml");
				}

				@Override
				public String getDescription() {
					return "StarMade BlockConfig (.xml)";
				}
			});
			fc.setAcceptAllFileFilterUsed(false);
		}
		//Show it.
		int returnVal = fc.showDialog(frame, title);

		//Process the results.
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		} else {
			return null;
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		ElementInformationEditPanel elementInformationEditPanel = new ElementInformationEditPanel(this, (Short) arg1);

		blocksSplitPane.setRightComponent(elementInformationEditPanel);

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if(node.getUserObject() instanceof ElementInformation) {
			System.err.println("Setting content pane: " + node.getUserObject());
			currentInfo = (ElementInformation) node.getUserObject();

			mntmRemoveEntry.setEnabled(currentInfo != null);
			ElementInformationEditPanel p = new ElementInformationEditPanel(this, currentInfo.getId());
			final JScrollPane jScrollPane = new JScrollPane(p);
			ScrollPaneLayout scrollPaneLayout = new ScrollPaneLayout();
			jScrollPane.setLayout(scrollPaneLayout);

			final int scrl;
			if(blocksSplitPane.getRightComponent() != null && blocksSplitPane.getRightComponent() instanceof JScrollPane) {
				scrl = ((JScrollPane) blocksSplitPane.getRightComponent()).getVerticalScrollBar().getValue();
			} else {
				scrl = 0;
			}

			blocksSplitPane.setRightComponent(jScrollPane);
			SwingUtilities.invokeLater(() -> {
				if(scrl != 0){
					jScrollPane.getVerticalScrollBar().setValue(scrl);
				}
			});

			blocksSplitPane.setDividerLocation(dividerLocation);
		}
		mntmChamberGraph.setEnabled(currentInfo != null && currentInfo.isReactorChamberGeneral());

	}

	public class ModelTreeListener implements ModelTreeInterface {

		private ModelsDetailPanel currentDp;

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if(node.getUserObject() instanceof Model) {
				Model m = (Model) node.getUserObject();
				ModelsDetailPanel dp = new ModelsDetailPanel();
				dp.fill(m);
				currentDp = dp;
				modelsPanelRightScrollable.setViewportView(dp);
			}
		}

		@Override
		public boolean hasPopupMenu() {
			return true;
		}

		@Override
		public void removeEntry(Model model) {
		}

		@Override
		public void addEntry(Model info) {
		}

		@Override
		public void refresh() {
			if(currentDp != null) {
				currentDp.refill();
			}
		}


	}
}
