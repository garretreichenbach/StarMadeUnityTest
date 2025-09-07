package org.schema.schine.common.language.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TranslationListPanel extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JList ls;
	/**
	 * Create the panel.
	 * @param l 
	 * @param f 
	 */
	public TranslationListPanel(JFrame f, final LanguageEditor l) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		ls = new JList(new TranslationListModel(l));
		l.addObserver(this);
		l.translationList = ls;
		ls.addListSelectionListener(e -> {
			l.onChangeSelection(l.autofillDupe);
			l.translationListSelectionChanged(ls.getSelectedIndex());
		});
		ls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ls.setCellRenderer(new TranslationCellRenderer());
		JScrollPane scrollPane = new JScrollPane(ls);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
	}

	@Override
	public void update(Observable o, Object arg) {
	}

}
