package org.schema.game.common.facedit;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.schema.game.common.data.element.ElementInformation;

public class TextureChosePanel extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	JLabel[] lbl = new JLabel[16 * 16];
	private int selectedSide = 0;
	private int selectedIndex = -1;
	private int tIndex;

	/**
	 * Create the panel.
	 *
	 * @param j
	 */
	public TextureChosePanel(ElementInformation info, int tIndex, final TextureChoserDialog diag, int individualSides) {
		final TableCellRenderer cellRenderer = new Renderer();
		this.tIndex = tIndex;

		if (info.getTextureId(selectedSide) > tIndex * 256 && info.getTextureId(selectedSide) < tIndex * 256 + 256) {
			selectedIndex = info.getTextureId(selectedSide) % 256;
		}
		final JTable t = new JTable(16, 16) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			/**
			 *
			 */
			

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				return cellRenderer;
			}
		};

		t.setDragEnabled(false);
		t.setCellSelectionEnabled(true);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setRowSelectionAllowed(false);
		t.setColumnSelectionAllowed(false);

		for (int i = 0; i < lbl.length; i++) {
			lbl[i] = new JLabel();
		}
		t.setModel(new AbstractTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			/**
			 *
			 */
			

			@Override
			public int getRowCount() {
				return 16;
			}

			@Override
			public int getColumnCount() {
				return 16;
			}

			@Override
			public Object getValueAt(int x, int y) {
				return y * 16 + x;
			}
		});
		t.addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseReleased(MouseEvent e) {
				int row = t.getSelectedRow();
				int col = t.getSelectedColumn();
				selectedIndex = row * 16 + col;
				System.err.println("[TextureChosePanel] NOW SELECTED TEX INDEX " + selectedIndex);
				t.repaint();

				diag.update(TextureChosePanel.this, selectedIndex);
			}

		});

		t.getColumnModel().setColumnMargin(0);
		for (int i = 0; i < 16; i++) {
			t.setRowHeight(i, 65);

			t.getColumnModel().getColumn(i).setWidth(64);
			t.getColumnModel().getColumn(i).setPreferredWidth(64);
			t.getColumnModel().getColumn(i).setMaxWidth(64);
		}
		t.doLayout();
		JPanel p = new JPanel(new GridLayout());
		p.add(t);
		//		t.setPreferredSize(new Dimension(1200, 1200));
		//		JScrollPane jScrollPane = new JScrollPane(p);
		//		jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//		jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		setViewportView(p);
	}

	/**
	 * @return the selectedIndex
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * @return the tIndex
	 */
	public int getTIndex() {
		return tIndex;
	}

	/**
	 * @return the selectedSide
	 */
	public int getSelectedSide() {
		return selectedSide;
	}

	/**
	 * @param selectedSide the selectedSide to set
	 */
	public void setSelectedSide(int selectedSide) {
		this.selectedSide = selectedSide;
	}

	private class Renderer extends JLabel implements TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 *
		 */
		

		@Override
		public Component getTableCellRendererComponent(JTable table,
		                                               Object value, boolean isSelected, boolean hasFocus, int row,
		                                               int column) {
			int index = row * 16 + column;

			setIcon(EditorTextureManager.getImage(getTIndex() * 256 + index));

			setEnabled(index != selectedIndex);
			return this;
		}

	}

}
