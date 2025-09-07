package org.schema.schine.common.language.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SearchDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	public LanguageEditor editor;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SearchDialog dialog = new SearchDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SearchDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{174, 86, 0};
		gbl_contentPanel.rowHeights = new int[]{20, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblEnterSearchString = new JLabel("Enter Search String");
			GridBagConstraints gbc_lblEnterSearchString = new GridBagConstraints();
			gbc_lblEnterSearchString.insets = new Insets(0, 0, 5, 5);
			gbc_lblEnterSearchString.gridx = 0;
			gbc_lblEnterSearchString.gridy = 0;
			contentPanel.add(lblEnterSearchString, gbc_lblEnterSearchString);
		}
		{
			textField = new JTextField();
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 0);
			gbc_textField.gridwidth = 2;
			gbc_textField.weightx = 1.0;
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.anchor = GridBagConstraints.NORTH;
			gbc_textField.gridx = 0;
			gbc_textField.gridy = 1;
			contentPanel.add(textField, gbc_textField);
			textField.setColumns(10);
		}
		
			JRadioButton rdbtnOriginal = new JRadioButton("Original");
			rdbtnOriginal.setSelected(true);
			GridBagConstraints gbc_rdbtnOriginal = new GridBagConstraints();
			gbc_rdbtnOriginal.insets = new Insets(0, 0, 0, 5);
			gbc_rdbtnOriginal.gridx = 0;
			gbc_rdbtnOriginal.gridy = 2;
			contentPanel.add(rdbtnOriginal, gbc_rdbtnOriginal);
		
		
			final JRadioButton rdbtnTranslation = new JRadioButton("Translation");
			GridBagConstraints gbc_rdbtnTranslation = new GridBagConstraints();
			gbc_rdbtnTranslation.gridx = 1;
			gbc_rdbtnTranslation.gridy = 2;
			contentPanel.add(rdbtnTranslation, gbc_rdbtnTranslation);
			
			ButtonGroup group = new ButtonGroup();
		    group.add(rdbtnTranslation);
		    group.add(rdbtnOriginal);
		
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			GridBagLayout gbl_buttonPane = new GridBagLayout();
			gbl_buttonPane.columnWidths = new int[]{226, 73, 55, 65, 0};
			gbl_buttonPane.rowHeights = new int[]{23, 0, 0};
			gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_buttonPane.rowWeights = new double[]{};
			buttonPane.setLayout(gbl_buttonPane);
			
			{
				JButton okButton = new JButton("Previous");
				okButton.addActionListener(e -> editor.search(textField.getText(), rdbtnTranslation.isSelected(), false));
				okButton.setHorizontalAlignment(SwingConstants.LEFT);
				okButton.setActionCommand("OK");
				GridBagConstraints gbc_okButton = new GridBagConstraints();
				gbc_okButton.anchor = GridBagConstraints.NORTHWEST;
				gbc_okButton.insets = new Insets(0, 0, 5, 5);
				gbc_okButton.gridx = 0;
				gbc_okButton.gridy = 0;
				buttonPane.add(okButton, gbc_okButton);
				getRootPane().setDefaultButton(okButton);
			}
			
				JButton btnNext = new JButton("Next");
				btnNext.addActionListener(e -> editor.search(textField.getText(), rdbtnTranslation.isSelected(), true));
				GridBagConstraints gbc_btnNext = new GridBagConstraints();
				gbc_btnNext.weightx = 1.0;
				gbc_btnNext.anchor = GridBagConstraints.NORTHWEST;
				gbc_btnNext.insets = new Insets(0, 0, 5, 5);
				gbc_btnNext.gridx = 1;
				gbc_btnNext.gridy = 0;
				buttonPane.add(btnNext, gbc_btnNext);
				
			
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(e -> SearchDialog.this.dispose());
				cancelButton.setActionCommand("Cancel");
				GridBagConstraints gbc_cancelButton = new GridBagConstraints();
				gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
				gbc_cancelButton.gridx = 3;
				gbc_cancelButton.gridy = 1;
				buttonPane.add(cancelButton, gbc_cancelButton);
			}
			
			this.getRootPane().setDefaultButton(btnNext);
			this.requestFocus();
		}
	}

}
