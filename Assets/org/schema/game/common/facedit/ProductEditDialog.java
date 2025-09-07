package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.data.element.FactoryResource;

public class ProductEditDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 *
	 * @param executeInterface
	 */
	public ProductEditDialog(JFrame frame, final ArrayList<FactoryResource> input, final ArrayList<FactoryResource> output, final ExecuteInterface executeInterface) {
		super(frame, true);
		setBounds(100, 100, 450, 435);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{125};
		gbl_contentPanel.rowHeights = new int[]{25, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

			GridBagConstraints gbc_splitPane = new GridBagConstraints();
			gbc_splitPane.weighty = 1.0;
			gbc_splitPane.weightx = 1.0;
			gbc_splitPane.fill = GridBagConstraints.BOTH;
			gbc_splitPane.anchor = GridBagConstraints.NORTHWEST;
			gbc_splitPane.gridx = 0;
			gbc_splitPane.gridy = 0;
			contentPanel.add(splitPane, gbc_splitPane);
			{
				FactoryResourceEditPanel factoryResourceEditPanel = new FactoryResourceEditPanel(frame, "input", input);
				splitPane.setLeftComponent(factoryResourceEditPanel);
			}
			{
				FactoryResourceEditPanel factoryResourceEditPanel = new FactoryResourceEditPanel(frame, "output", output);
				splitPane.setRightComponent(factoryResourceEditPanel);
			}
			splitPane.setDividerLocation(200);
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
					executeInterface.execute();
					dispose();
					//						FactoryResourceEditPanel diag = new FactoryResourceEditPanel(treeSet);
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
