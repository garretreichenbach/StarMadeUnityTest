package org.schema.game.common.gui.worldmanager;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.Timer;

public class WorldManagerTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timer disposeTimer;
	private Point hintCell;
	private MyPopup popup; // Inherites from JPopupMenu

	public WorldManagerTable(WorldManagerTableModel dm, Collection<WorldInfo> infos) {
		super(dm);
		dm.addAll(infos);
	}

	public WorldManagerTable(WorldManagerTableModel dm) {
		this(dm, new Vector<WorldInfo>());

//		    showTimer = new Timer(1500, new ShowPopupActionHandler());
//	        showTimer.setRepeats(false);
//	        showTimer.setCoalesce(true);
//
//	        disposeTimer = new Timer(5000, new DisposePopupActionHandler());
//	        disposeTimer.setRepeats(false);
//	        disposeTimer.setCoalesce(true);
//
//	        this.addMouseMotionListener(new MouseMotionAdapter() {
//
//	            @Override
//	            public void mouseMoved(MouseEvent e) {
//
//	                Point p = e.getPoint();
//	                int row = ServerListTable.this.rowAtPoint(p);
//	                int col = ServerListTable.this.columnAtPoint(p);
//
//	                if ((row > -1 && row < ServerListTable.this.getRowCount()) && (col > -1 && col < ServerListTable.this.getColumnCount())) {
//
//	                    if (hintCell == null || (hintCell.x != col || hintCell.y != row)) {
//
//	                        hintCell = new Point(col, row);
//	                        Object value = ServerListTable.this.getValueAt(row, col);
//	                        // Depending on how the data is stored, you may need to load more data
//	                        // here...
//	                        // You will probably want to maintain a reference to the object hint data
//
//	                        showTimer.restart();
//
//	                    }
//
//	                }
//
//	            }
//	        });

	}

	protected MyPopup getHintPopup(int row, int column) {

//        if (popup == null) {
		JLabel label = new JLabel(WorldManagerTable.this.getValueAt(row, column).toString());
		popup = new MyPopup();

//        }

		return popup;

	}

	public class MyPopup extends JPopupMenu {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 *
		 */
		

		public MyPopup() {
			super();
		}

		public MyPopup(String label) {
			super(label);
		}

	}

	public class ShowPopupActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (hintCell != null) {

				disposeTimer.stop(); // don't want it going off while we're setting up

				MyPopup popup = getHintPopup(hintCell.y, hintCell.x);
				popup.setVisible(false);

				// You might want to check that the object hint data is update and valid...
				Rectangle bounds = WorldManagerTable.this.getCellRect(hintCell.y, hintCell.x, true);
				int x = bounds.x;
				int y = bounds.y + bounds.height;
				popup.setVisible(true);
				popup.show(WorldManagerTable.this, x, y);

				disposeTimer.start();

			}

		}
	}

	public class DisposePopupActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

//            MyPopup popup = getHintPopup();
			if (popup != null) {
				popup.setVisible(false);
			}

		}
	}
}
