package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class ElementChoserDialog extends JDialog implements ElementTreeInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final HashSet<ElementInformation> fullset = new HashSet<ElementInformation>();
	private JTextField textField;
	private JTabbedPane tabbedPane;
	private JPanel searchPanel;
	private JPanel treePanel;
	private ElementTreePanel factoryEditorElementOverviewPanel;
	private ElementChoseInterface iFace;
	private JList list;
	private ElementInformation currentlySelected;
	private JScrollPane scrollPane;

	/**
	 * Create the dialog.
	 */
	public ElementChoserDialog(JFrame frame, ElementChoseInterface iFace) {
		super(frame, true);
		this.iFace = iFace;
		for (short k : ElementKeyMap.typeList()) {
			fullset.add(ElementKeyMap.getInfo(k));
		}

		setAlwaysOnTop(true);

		setBounds(100, 100, 702, 440);
		getContentPane().setLayout(new BorderLayout());
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			{
				searchPanel = new JPanel();
				tabbedPane.addTab(Lng.str("Search"), null, searchPanel, null);
				GridBagLayout gbl_searchPanel = new GridBagLayout();
				gbl_searchPanel.columnWidths = new int[]{0, 0};
				gbl_searchPanel.rowHeights = new int[]{0, 0, 0};
				gbl_searchPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_searchPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
				searchPanel.setLayout(gbl_searchPanel);
				{
					textField = new JTextField("");
					GridBagConstraints gbc_textField = new GridBagConstraints();
					gbc_textField.insets = new Insets(0, 0, 5, 0);
					gbc_textField.fill = GridBagConstraints.HORIZONTAL;
					gbc_textField.gridx = 0;
					gbc_textField.gridy = 0;
					searchPanel.add(textField, gbc_textField);
					textField.setColumns(10);

					textField.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent arg0) {

						}

						@Override
						public void keyPressed(KeyEvent arg0) {
							if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
								apply();
							}
						}

						@Override
						public void keyReleased(KeyEvent arg0) {
							updateList();
						}
					});
					
				}
				{
					list = new JList();
					list.addMouseListener(new MouseListener() {

						@Override
						public void mouseClicked(MouseEvent arg0) {
							if (arg0.getClickCount() >= 2) {
								apply();
							}
						}

						@Override
						public void mouseEntered(MouseEvent arg0) {
						}

						@Override
						public void mouseExited(MouseEvent arg0) {
						}

						@Override
						public void mousePressed(MouseEvent arg0) {
						}

						@Override
						public void mouseReleased(MouseEvent arg0) {
						}
					});
					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					GridBagConstraints gbc_list = new GridBagConstraints();
					gbc_list.fill = GridBagConstraints.BOTH;
					gbc_list.gridx = 0;
					gbc_list.gridy = 1;
					searchPanel.add(new JScrollPane(list), gbc_list);
				}
			}
			{
				treePanel = new JPanel();
				tabbedPane.addTab(Lng.str("Tree"), null, treePanel, null);
				treePanel.setLayout(new GridLayout(0, 1, 0, 0));
				{
					{
						scrollPane = new JScrollPane();
						treePanel.add(scrollPane);
						factoryEditorElementOverviewPanel = new ElementTreePanel(this);
						scrollPane.setViewportView(factoryEditorElementOverviewPanel);
						factoryEditorElementOverviewPanel.getTree().setEditable(false);
						factoryEditorElementOverviewPanel.getTree().addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {

								if (arg0.getClickCount() >= 2) {
									apply();
								}
							}

							@Override
							public void mouseEntered(MouseEvent arg0) {
							}

							@Override
							public void mouseExited(MouseEvent arg0) {
							}

							@Override
							public void mousePressed(MouseEvent arg0) {
							}

							@Override
							public void mouseReleased(MouseEvent arg0) {
							}
						});
					}
				}

			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(arg0 -> apply());
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(arg0 -> dispose());
				buttonPane.add(cancelButton);
			}
		}

		updateList();
		textField.requestFocus();
		textField.addAncestorListener(new AncestorListener() {
		    @Override
		    public void ancestorRemoved(AncestorEvent pEvent) {
		    }

		    @Override
		    public void ancestorMoved(AncestorEvent pEvent) {
		    }

		    @Override
		    public void ancestorAdded(AncestorEvent pEvent) {
		        // TextField is added to its parent => request focus in Event Dispatch Thread
		        SwingUtilities.invokeLater(() -> textField.requestFocusInWindow());
		    }
		});

	}

	private void apply() {
		if (tabbedPane.getSelectedComponent() == searchPanel) {

			Object selectedValue = list.getSelectedValue();
			System.err.println("SELECTED FROM LIST " + selectedValue);
			if (selectedValue != null && selectedValue instanceof ElementInformation) {
				iFace.onEnter((ElementInformation) selectedValue);
				dispose();
			}

		} else if (tabbedPane.getSelectedComponent() == treePanel) {
			ElementInformation selectedItem = currentlySelected;
			if (selectedItem != null) {
				iFace.onEnter(selectedItem);
				dispose();
			}
		} else {
			System.err.println("NO TABBED PANE SELECTED " + tabbedPane.getSelectedComponent());
		}
	}

	private void updateList() {
		HashSet<ElementInformation> set = new HashSet<ElementInformation>(fullset);

		String text = textField.getText();
		if (text.trim().length() == 0) {

		} else {
			//			System.err.println("CHECKING ALL FOR "+text.trim().toLowerCase(Locale.ENGLISH));
			for (ElementInformation i : fullset) {
				if (!i.getName().toLowerCase(Locale.ENGLISH).contains(text.trim().toLowerCase(Locale.ENGLISH))) {
					set.remove(i);
					//					System.err.println("SET REMOVING "+i);
				}
			}
		}

		Comparator<ElementInformation> c = (o1, o2) -> o1.getName().compareTo(o2.getName());
		//		System.err.println("FINAL SET "+set.size());
		ElementInformation[] a = new ElementInformation[set.size()];
		set.toArray(a);
		Arrays.sort(a, c);

		list.setListData(a);

		if (list.getModel().getSize() > 0) {
			list.setSelectedIndex(0);
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getUserObject() instanceof ElementInformation) {
			ElementInformation info = (ElementInformation) node.getUserObject();
			currentlySelected = info;
			System.err.println("NOW SELECTED " + currentlySelected);
		}

	}

	@Override
	public void removeEntry(ElementInformation info) {
		
	}

	@Override
	public boolean hasPopupMenu() {
				return false;
	}

	@Override
	public void reinitializeElements() {
				
	}

}
