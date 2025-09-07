package org.schema.schine.common.language.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.schema.schine.common.language.Translation;

public class TranslationDetailPanel extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JList list;
	private LanguageEditor l;
	private JTextArea textArea;
	private JEditorPane editorPane;
	private JLabel varName;
	private boolean inital;
	private JTextPane textPanePrevious;
	class DocListener implements DocumentListener {
	    String newline = "\n";
		
	 
	    @Override
		public void insertUpdate(DocumentEvent e) {
	        updateLog(e, "inserted into");
	    }
	    @Override
		public void removeUpdate(DocumentEvent e) {
	        updateLog(e, "removed from");
	    }
	    @Override
		public void changedUpdate(DocumentEvent e) {
	        //Plain text components do not fire these events
	    }

	    public void updateLog(DocumentEvent e, String action) {
	        Document doc = e.getDocument();
	        int changeLength = e.getLength();
	        if(l.selectionIndex >= 0 && l.selectionIndex < l.list.size()){
				Translation translation = l.list.get(l.selectionIndex);
				try {
					
					String trim = doc.getText(0, doc.getLength()).replaceAll("\n", "\\\\n").trim();
					if(!inital){
						l.changesPending = true;
						translation.changed = true;
						translation.translation = trim; 
					}
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
	        }
	    }
	}
	/**
	 * Create the panel.
	 * @param l 
	 * @param f 
	 */
	public TranslationDetailPanel(JFrame f, final LanguageEditor l) {
		this.l = l;
		l.addObserver(this);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 0.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		
		JPanel panel0 = new JPanel();
		GridBagConstraints gbc_panel0 = new GridBagConstraints();
		gbc_panel0.weightx = 1.0;
		gbc_panel0.weighty = 0.0;
		gbc_panel0.insets = new Insets(0, 0, 5, 0);
		gbc_panel0.fill = GridBagConstraints.BOTH;
		gbc_panel0.gridx = 0;
		gbc_panel0.gridy = 0;
		add(panel0, gbc_panel0);
		GridBagLayout gbl_panel0 = new GridBagLayout();
		gbl_panel0.columnWidths = new int[]{0};
		gbl_panel0.rowHeights = new int[]{0};
		gbl_panel0.columnWeights = new double[]{0.0};
		gbl_panel0.rowWeights = new double[]{0.0};
		panel0.setLayout(gbl_panel0);
		
		GridBagConstraints cc = new GridBagConstraints();
		cc.fill = GridBagConstraints.VERTICAL;
		cc.anchor = GridBagConstraints.NORTHWEST;
		cc.insets = new Insets(4, 8, 0, 0);
		cc.gridx = 0;
		cc.gridy = 0;
		cc.weightx = 1;
		cc.weighty = 1;
		
		
		varName = new JLabel();
		varName.setPreferredSize(new Dimension(10000, 10));
		panel0.add(varName, cc);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weightx = 1.0;
		gbc_panel.weighty = 4.0;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane);
		panel.setBorder(new TitledBorder("Translation"));
		
		editorPane = new JEditorPane();
		editorPane.getDocument().addDocumentListener(new DocListener());
		editorPane.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER){
					if(e.isShiftDown()){
						previous();
					}else{
						next();
					}
				}
			}
		});
		scrollPane.setViewportView(editorPane);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weightx = 1.0;
		gbc_panel_1.weighty = 4.0;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		panel_1.setBorder(new TitledBorder("Original"));
		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panel_1.add(scrollPane_1, gbc_scrollPane_1);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane_1.setViewportView(textArea);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "previous translation for this entry ID", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(0, 0, 5, 0);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 3;
		add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0, 0};
		gbl_panel_4.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.gridheight = 2;
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 0;
		panel_4.add(scrollPane_3, gbc_scrollPane_3);
		
		textPanePrevious = new JTextPane();
		textPanePrevious.setEditable(false);
		scrollPane_3.setViewportView(textPanePrevious);
		
		JButton btnNewButton_1 = new JButton("Apply");
		btnNewButton_1.addActionListener(e -> {
			editorPane.setText(textPanePrevious.getText());
			editorPane.requestFocus();
			editorPane.select(0, editorPane.getText().length());
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 0;
		panel_4.add(btnNewButton_1, gbc_btnNewButton_1);
		
		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weightx = 1.0;
		gbc_panel_2.weighty = 0.5;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 4;
		add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		panel_2.setBorder(new TitledBorder("Argument names (they appear in code as %s in order. To escape %, write %%)"));
		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 0;
		panel_2.add(scrollPane_2, gbc_scrollPane_2);
		
		list = new JList();
		scrollPane_2.setViewportView(list);
		
		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.weightx = 1.0;
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 5;
		add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{};
		panel_3.setLayout(gbl_panel_3);
		
		JButton btnNewButton = new JButton("Previous (ctrl+shift+enter)");
		btnNewButton.addActionListener(e -> previous());
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		panel_3.add(btnNewButton, gbc_btnNewButton);
		
		
		JButton btnUseForAll0 = new JButton("Reset Entry");
		btnUseForAll0.addActionListener(e -> {
			if(l.selectionIndex >= 0){
				l.list.get(l.selectionIndex).translation = new String(l.list.get(l.selectionIndex).original);
				l.list.get(l.selectionIndex).translator = "default";
				update();
			}
		});
		GridBagConstraints gbc_btnUseForAll0 = new GridBagConstraints();
		gbc_btnUseForAll0.weightx = 1.0;
		gbc_btnUseForAll0.insets = new Insets(0, 0, 0, 5);
		gbc_btnUseForAll0.gridx = 1;
		gbc_btnUseForAll0.gridy = 0;
		panel_3.add(btnUseForAll0, gbc_btnUseForAll0);
		
		JButton btnUseForAll = new JButton("Fill for all duplicates");
		btnUseForAll.addActionListener(e -> {
			if(l.selectionIndex >= 0){
				l.fillDupl(l.list.get(l.selectionIndex));
			}
		});
		GridBagConstraints gbc_btnUseForAll = new GridBagConstraints();
		gbc_btnUseForAll.weightx = 1.0;
		gbc_btnUseForAll.insets = new Insets(0, 0, 0, 5);
		gbc_btnUseForAll.gridx = 2;
		gbc_btnUseForAll.gridy = 0;
		panel_3.add(btnUseForAll, gbc_btnUseForAll);
		
		final JCheckBox btnUseForAll1 = new JCheckBox("auto-fill duplicates");
		GridBagConstraints gbc_btnUseForAll1 = new GridBagConstraints();
		gbc_btnUseForAll1.weightx = 1.0;
		btnUseForAll1.setSelected(true);
		l.autofillDupe = true;
		btnUseForAll1.addActionListener(e -> l.autofillDupe = btnUseForAll1.isSelected());
		gbc_btnUseForAll1.insets = new Insets(0, 0, 0, 5);
		gbc_btnUseForAll1.gridx = 3;
		gbc_btnUseForAll1.gridy = 0;
		panel_3.add(btnUseForAll1, gbc_btnUseForAll1);
		
		JButton btnNext = new JButton("Next (ctrl+enter)");
		btnNext.addActionListener(e -> next());
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.anchor = GridBagConstraints.EAST;
		gbc_btnNext.gridx = 4;
		gbc_btnNext.gridy = 0;
		panel_3.add(btnNext, gbc_btnNext);
	}
	private void previous(){
		EventQueue.invokeLater(() -> {
			if(l.selectionIndex >= 0){
				l.onChangeSelection(l.autofillDupe);
				if(l.selectionIndex > 0){
					l.selectionIndex--;
				}else{
					l.selectionIndex = l.list.size()-1;
				}

				if(l.missing > 0){
					while(!l.list.get(l.selectionIndex).translator.equals("default")){
						if(l.selectionIndex > 0){
							l.selectionIndex--;
						}else{
							l.selectionIndex = l.list.size()-1;
						}
					}
				}
				l.translationList.setSelectedIndex(l.selectionIndex);
				l.translationList.ensureIndexIsVisible(l.translationList.getSelectedIndex());
				update();
			}
		});
		
	}
	private void next(){
		EventQueue.invokeLater(() -> {
			if(l.selectionIndex >= 0){
				l.onChangeSelection(l.autofillDupe);
				l.selectionIndex = (l.selectionIndex+1)%l.list.size();
				if(l.missing > 0){
					while(!l.list.get(l.selectionIndex).translator.equals("default")){
						l.selectionIndex = (l.selectionIndex+1)%l.list.size();
					}
				}
				l.translationList.setSelectedIndex(l.selectionIndex);
				l.translationList.ensureIndexIsVisible(l.translationList.getSelectedIndex());
				update();
			}
		});
	}
	@Override
	public void update(Observable o, Object arg) {
//			System.err.println(l.selectionEvent.getValueIsAdjusting()+"; "+l.selectionIndex);
		update();
	}
	private void update(){
		
		try{
			inital = true;
			if(l.selectionIndex >= 0 && l.selectionIndex < l.list.size()){
				Translation translation = l.list.get(l.selectionIndex);
				list.setListData(translation.args);
				
				textArea.setText(translation.original.replaceAll("\\\\n", "\n"));
				editorPane.setText(translation.translation.replaceAll("\\\\n", "\n"));
				textPanePrevious.setText(translation.oldTranslation.replaceAll("\\\\n", "\n"));
				
				varName.setText("Context Class Name: \""+translation.var+"\"");
				editorPane.requestFocus();
				editorPane.select(0, editorPane.getText().length());
			}else{
				list.setListData(new String[0]);
				textArea.setText("");
				editorPane.setText("");
				varName.setText("");
			}
		}finally{
			inital = false;
		}
	}
}
