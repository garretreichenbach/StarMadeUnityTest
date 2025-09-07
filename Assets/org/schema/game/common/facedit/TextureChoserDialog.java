package org.schema.game.common.facedit;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.data.element.ElementInformation;

public class TextureChoserDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private final JComboBox comboBox = new JComboBox();
	private TexturePanel texturePanel;
	private JTabbedPane tabbedPane;

	/**
	 * Create the dialog.
	 *
	 * @param executeInterface
	 * @param info
	 * @param frame
	 */
	public TextureChoserDialog(JFrame frame, final ElementInformation info, final ExecuteInterface executeInterface) {
		super(frame, true);
		setTitle("Texture Sheets");
		setBounds(50, 50, 1200, 900);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowHeights = new int[]{20, 30, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.weightx = 1.0;
		gbc_comboBox.anchor = GridBagConstraints.NORTH;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;

		comboBox.addItem(1);
		comboBox.addItem(3);
		comboBox.addItem(6);
		comboBox.setSelectedItem(info.getIndividualSides());
		comboBox.addActionListener(arg0 -> {
			TextureChosePanel p = (TextureChosePanel) tabbedPane.getSelectedComponent();
			update(p, p.getSelectedIndex());
		});
		getContentPane().add(comboBox, gbc_comboBox);

		{
			texturePanel = new TexturePanel(info, true);
			GridBagConstraints gbc_texturePanel = new GridBagConstraints();
			gbc_texturePanel.weightx = 1.0;
			gbc_texturePanel.anchor = GridBagConstraints.WEST;
			gbc_texturePanel.fill = GridBagConstraints.VERTICAL;
			gbc_texturePanel.insets = new Insets(0, 0, 5, 5);
			gbc_texturePanel.gridx = 0;
			gbc_texturePanel.gridy = 1;
			getContentPane().add(texturePanel, gbc_texturePanel);
		}
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.weighty = 1.0;
		gbc_contentPanel.weightx = 1.0;
		gbc_contentPanel.anchor = GridBagConstraints.WEST;
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_contentPanel.gridx = 0;
		gbc_contentPanel.gridy = 2;
		getContentPane().add(contentPanel, gbc_contentPanel);
		contentPanel.setLayout(new GridLayout(0, 1, 0, 0));
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				TextureChosePanel textureChosePanel = new TextureChosePanel(info, 0, this, info.getIndividualSides());
				tabbedPane.addTab("t000", null, textureChosePanel, null);
			}
			{
				TextureChosePanel textureChosePanel = new TextureChosePanel(info, 1, this, info.getIndividualSides());
				tabbedPane.addTab("t001", null, textureChosePanel, null);
			}
			{
				TextureChosePanel textureChosePanel = new TextureChosePanel(info, 2, this, info.getIndividualSides());
				tabbedPane.addTab("t002", null, textureChosePanel, null);
			}
			{
				TextureChosePanel textureChosePanel = new TextureChosePanel(info, 3, this, info.getIndividualSides());
				tabbedPane.addTab("t003", null, textureChosePanel, null);
			}
			{
				TextureChosePanel textureChosePanel = new TextureChosePanel(info, 7, this, info.getIndividualSides());
				tabbedPane.addTab("custom(t007)", null, textureChosePanel, null);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			GridBagConstraints gbc_buttonPane = new GridBagConstraints();
			gbc_buttonPane.weightx = 1.0;
			gbc_buttonPane.anchor = GridBagConstraints.NORTH;
			gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
			gbc_buttonPane.gridx = 0;
			gbc_buttonPane.gridy = 3;
			getContentPane().add(buttonPane, gbc_buttonPane);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(e -> {
					executeInterface.execute();
					dispose();
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(e -> dispose());
			}
		}
	}

	public void update(TextureChosePanel textureChosePanel, int selectedIndex) {
		System.out.println("UPDATING TEX DISPLAY");
		texturePanel.update(textureChosePanel.getTIndex() * 256 + selectedIndex, comboBox.getSelectedItem() != null ? (Integer) comboBox.getSelectedItem() : 1);
		//texturePanel.repaint();

		repaint();
	}

}
