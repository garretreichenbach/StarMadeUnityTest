package org.schema.game.common.facedit.craft;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.schema.game.common.data.element.ElementKeyMap;

public class BlockTable extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTable table;

	/**
	 * Create the panel.
	 */
	public BlockTable(final JFrame f) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 0.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		final BlockTableModel model = new BlockTableModel(f);
		table = new JTable(model);

		// Der TableRowSorter wird die Daten des Models sortieren
		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();

		// Der Sorter muss dem JTable bekannt sein
		table.setRowSorter(sorter);

		// ... und der Sorter muss wissen, welche Daten er sortieren muss
		sorter.setModel(model);

		// Den Comparator f�r die 2. Spalte (mit den Points) setzen.
		//        sorter.setComparator( 1, new PointComparator());

		scrollPane.setViewportView(table);

		JButton btnGraph = new JButton("Graph");
		btnGraph.addActionListener(e -> {
			if (table.getSelectedRow() >= 0) {

				short id = (Short) model.getValueAt(sorter.convertRowIndexToModel(table.getSelectedRow()), 0);

				JDialog d = new JDialog(f);
				d.setSize(1024, 500);
				// #RM1958 d.setAutoRequestFocus(true);
				d.setAlwaysOnTop(true);
				d.setTitle("Block consistence Graph");

				d.setLayout(new BorderLayout());
				d.getContentPane().add(ElementKeyMap.getInfo(id).getGraph(), BorderLayout.CENTER);
				d.setVisible(true);
			}
		});

		btnGraph.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_btnGraph = new GridBagConstraints();
		gbc_btnGraph.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnGraph.gridx = 0;
		gbc_btnGraph.gridy = 1;
		add(btnGraph, gbc_btnGraph);
	}

}
