package org.schema.schine.common.language.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class LanguageEditorMainPanel extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 * @param laguageEditor 
	 * @param f 
	 */
	public LanguageEditorMainPanel(JFrame f, LanguageEditor l) {
		l.addObserver(this);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		StatsAndToolsPanel statsAndToolsPanel = new StatsAndToolsPanel(f, l);
		GridBagConstraints gbc_statsAndToolsPanel = new GridBagConstraints();
		gbc_statsAndToolsPanel.weightx = 1.0;
		gbc_statsAndToolsPanel.fill = GridBagConstraints.BOTH;
		gbc_statsAndToolsPanel.gridx = 0;
		gbc_statsAndToolsPanel.gridy = 0;
		panel.add(statsAndToolsPanel, gbc_statsAndToolsPanel);
		GridBagLayout gbl_statsAndToolsPanel = new GridBagLayout();
		gbl_statsAndToolsPanel.columnWidths = new int[]{0};
		gbl_statsAndToolsPanel.rowHeights = new int[]{0};
		gbl_statsAndToolsPanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_statsAndToolsPanel.rowWeights = new double[]{Double.MIN_VALUE};
//		statsAndToolsPanel.setLayout(gbl_statsAndToolsPanel);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weighty = 1.0;
		gbc_panel_1.weightx = 1.0;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.weighty = 1.0;
		gbc_splitPane.weightx = 1.0;
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		panel_1.add(splitPane, gbc_splitPane);
		
		JPanel panel_2 = new JPanel();
		splitPane.setLeftComponent(panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		TranslationListPanel translationListPanel = new TranslationListPanel(f, l);
		GridBagConstraints gbc_translationListPanel = new GridBagConstraints();
		gbc_translationListPanel.weighty = 1.0;
		gbc_translationListPanel.weightx = 1.0;
		gbc_translationListPanel.fill = GridBagConstraints.BOTH;
		gbc_translationListPanel.gridx = 0;
		gbc_translationListPanel.gridy = 0;
		panel_2.add(translationListPanel, gbc_translationListPanel);
		
		
		JPanel panel_3 = new JPanel();
		splitPane.setRightComponent(panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0};
		gbl_panel_3.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		TranslationDetailPanel translationDetailPanel = new TranslationDetailPanel(f, l);
		GridBagConstraints gbc_translationDetailPanel = new GridBagConstraints();
		gbc_translationDetailPanel.fill = GridBagConstraints.BOTH;
		gbc_translationDetailPanel.gridx = 0;
		gbc_translationDetailPanel.gridy = 0;
		panel_3.add(translationDetailPanel, gbc_translationDetailPanel);
	}

	@Override
	public void update(Observable o, Object arg) {
				
	}

}
