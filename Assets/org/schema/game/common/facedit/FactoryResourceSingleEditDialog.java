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
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;

public class FactoryResourceSingleEditDialog extends JDialog {

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
	private JSpinner slider;
	private JLabel lblLevel;

	/**
	 * Create the dialog.
	 */
	public FactoryResourceSingleEditDialog(final JFrame frame, final FactoryResource facResource, final ArrayListModel<FactoryResource> arrayListModel) {
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
		//		if(facResource != null){
		//			arrayListModel.remove(facResource);
		//		}
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
			currentId = facResource != null ? facResource.type : -1;

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
				ElementChoserDialog diag = new ElementChoserDialog(frame, info -> {
					currentId = info.getId();
					lblUndefined.setText(currentId > 0 ? ElementKeyMap.getInfo(currentId).toString() : "undefined");
				});
				diag.setVisible(true);
			});
		}
		{
			System.err.println("FAC: " + facResource);
			lblLevel = new JLabel("Count " + (facResource != null ? String.valueOf(facResource.count) : "0"));
			GridBagConstraints gbc_lblLevel = new GridBagConstraints();
			gbc_lblLevel.insets = new Insets(0, 0, 0, 5);
			gbc_lblLevel.gridx = 0;
			gbc_lblLevel.gridy = 1;
			contentPanel.add(lblLevel, gbc_lblLevel);
		}
		{
			slider = new JSpinner();
			//			slider.setMajorTickSpacing(10);
			//			slider.setPaintTicks(true);
			//			slider.setPaintLabels(true);
			//			slider.setMinimum(0);
			//			slider.setMaximum(50);

			slider.setValue(facResource != null ? facResource.count : 1);
			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.weightx = 11.0;
			gbc_slider.gridwidth = 2;
			gbc_slider.insets = new Insets(0, 0, 0, 5);
			gbc_slider.gridx = 1;
			gbc_slider.gridy = 1;
			contentPanel.add(slider, gbc_slider);

			GridBagConstraints hh = new GridBagConstraints();
			hh.fill = GridBagConstraints.HORIZONTAL;
			hh.weightx = 11.0;
			hh.gridwidth = 2;
			hh.insets = new Insets(0, 0, 0, 5);
			hh.gridx = 2;
			hh.gridy = 1;
			contentPanel.add(slider, hh);

			slider.addChangeListener(arg0 -> {
				assert (lblLevel != null);
				lblLevel.setText("Count " + String.valueOf(slider.getValue()));
			});
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

				okButton.addActionListener(e -> {
					if (currentId > 0) {
						if ((Integer) slider.getValue() > 0) {
							if (facResource != null) {
								facResource.type = currentId;
								facResource.count = (Integer) slider.getValue();
								arrayListModel.dataChanged(facResource);
							} else {
								arrayListModel.add(new FactoryResource((Integer) slider.getValue(), currentId));
							}

							repaint();
						}
					} else {

					}
					//						text.setText(info.getLevel() != null ? info.getLevel().toString() : "   -   ");
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

}
