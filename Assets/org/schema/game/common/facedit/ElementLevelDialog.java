package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.data.element.BlockLevel;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public class ElementLevelDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private short currentId;
	private JLabel lblUndefined;
	private JSlider slider;

	/**
	 * Create the dialog.
	 *
	 * @param text
	 */
	public ElementLevelDialog(final JFrame frame, BlockLevel lvl, final ElementInformation info, final JTextPane text) {
		super(frame, true);
		setTitle("Block Level Editor");
		setBounds(100, 100, 510, 184);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblBaseElement = new JLabel("Base Element");
			GridBagConstraints gbc_lblBaseElement = new GridBagConstraints();
			gbc_lblBaseElement.anchor = GridBagConstraints.WEST;
			gbc_lblBaseElement.insets = new Insets(0, 0, 5, 5);
			gbc_lblBaseElement.gridx = 0;
			gbc_lblBaseElement.gridy = 0;
			contentPanel.add(lblBaseElement, gbc_lblBaseElement);
		}
		{
			currentId = lvl != null ? lvl.getIdBase() : -1;

			lblUndefined = new JLabel(currentId > 0 ? ElementKeyMap.getInfo(currentId).toString() : "undefined");
			GridBagConstraints gbc_lblUndefined = new GridBagConstraints();
			gbc_lblUndefined.weightx = 1.0;
			gbc_lblUndefined.insets = new Insets(0, 0, 5, 5);
			gbc_lblUndefined.gridx = 1;
			gbc_lblUndefined.gridy = 0;
			contentPanel.add(lblUndefined, gbc_lblUndefined);
		}
		{
			JButton btnChoose = new JButton("Choose");
			GridBagConstraints gbc_btnChoose = new GridBagConstraints();
			gbc_btnChoose.insets = new Insets(0, 0, 5, 0);
			gbc_btnChoose.anchor = GridBagConstraints.EAST;
			gbc_btnChoose.gridx = 2;
			gbc_btnChoose.gridy = 0;
			contentPanel.add(btnChoose, gbc_btnChoose);

			btnChoose.addActionListener(arg0 -> {
				ElementChoserDialog diag = new ElementChoserDialog(frame, info1 -> {
					currentId = info1.getId();
					lblUndefined.setText(currentId > 0 ? ElementKeyMap.getInfo(currentId).toString() : "undefined");
				});
				diag.setVisible(true);
			});
		}
		{
			JLabel lblLevel = new JLabel("Level");
			GridBagConstraints gbc_lblLevel = new GridBagConstraints();
			gbc_lblLevel.insets = new Insets(0, 0, 0, 5);
			gbc_lblLevel.gridx = 0;
			gbc_lblLevel.gridy = 1;
			contentPanel.add(lblLevel, gbc_lblLevel);
		}
		{
			slider = new JSlider();
			slider.setSnapToTicks(true);
			slider.setMajorTickSpacing(1);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setMinimum(1);
			slider.setMaximum(5);
			slider.setValue(lvl != null ? lvl.getLevel() : 1);
			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.weightx = 11.0;
			gbc_slider.gridwidth = 2;
			gbc_slider.insets = new Insets(0, 0, 0, 5);
			gbc_slider.gridx = 1;
			gbc_slider.gridy = 1;
			contentPanel.add(slider, gbc_slider);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(e -> dispose());
			}
		}
	}

}
