package org.schema.game.common.facedit.importer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ElementParser;
import org.schema.game.common.data.element.annotation.Element;
import org.schema.game.common.facedit.ArrayListModel;
import org.schema.game.common.util.GuiErrorHandler;

public class ImportConfigDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	// #RM1958 remove JList generic argument
	private JList rightList;
	private JList leftList;

	/**
	 * Create the dialog.
	 */
	public ImportConfigDialog(JFrame frame, final File file) {
		super(frame);
		setTitle("Importer");
		setBounds(100, 100, 559, 540);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblMoveFieldsThat = new JLabel("Move the fields to the right that should be imported to the current config");
			GridBagConstraints gbc_lblMoveFieldsThat = new GridBagConstraints();
			gbc_lblMoveFieldsThat.gridwidth = 3;
			gbc_lblMoveFieldsThat.insets = new Insets(0, 0, 5, 5);
			gbc_lblMoveFieldsThat.gridx = 0;
			gbc_lblMoveFieldsThat.gridy = 0;
			contentPanel.add(lblMoveFieldsThat, gbc_lblMoveFieldsThat);
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.weighty = 1.0;
			gbc_panel.weightx = 1.0;
			gbc_panel.insets = new Insets(0, 0, 0, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.anchor = GridBagConstraints.NORTHWEST;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 1;

			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0};
			gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};

			panel.setLayout(gbl_panel);
			{

				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
				gbc_scrollPane.weighty = 1.0;
				gbc_scrollPane.weightx = 1.0;
				gbc_scrollPane.fill = GridBagConstraints.BOTH;
				gbc_scrollPane.gridx = 0;
				gbc_scrollPane.gridy = 0;
				JScrollPane scrollPane = new JScrollPane(parse());
				scrollPane.setPreferredSize(new Dimension(1, 1));
				panel.add(scrollPane, gbc_scrollPane);

			}
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.weighty = 1.0;
			gbc_panel.insets = new Insets(0, 0, 0, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 1;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.rowHeights = new int[]{23, 0, 0};
			gbl_panel.columnWeights = new double[]{0.0};
			gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				JButton button = new JButton(">");
				button.addActionListener(e -> {
					int[] selectedIndices = leftList.getSelectedIndices();
					ArrayList<FField> removed = new ArrayList<FField>();
					for (int i : selectedIndices) {
						FField f = ((ArrayListModel<FField>) leftList.getModel()).getCollection().get(i);
						((ArrayListModel<FField>) rightList.getModel()).add(f);
						removed.add(f);
					}
					for (FField f : removed) {
						((ArrayListModel<FField>) leftList.getModel()).remove(f);
					}
					Collections.sort(((ArrayListModel<FField>) rightList.getModel()).getCollection());
					Collections.sort(((ArrayListModel<FField>) leftList.getModel()).getCollection());
					((ArrayListModel<FField>) leftList.getModel()).allChanged();
					((ArrayListModel<FField>) rightList.getModel()).allChanged();
				});
				GridBagConstraints gbc_button = new GridBagConstraints();
				gbc_button.anchor = GridBagConstraints.SOUTH;
				gbc_button.insets = new Insets(0, 0, 5, 0);
				gbc_button.weighty = 1.0;
				gbc_button.weightx = 1.0;
				gbc_button.gridx = 0;
				gbc_button.gridy = 0;
				panel.add(button, gbc_button);
			}
			{
				JButton button = new JButton("<");
				button.addActionListener(e -> {
					int[] selectedIndices = rightList.getSelectedIndices();
					ArrayList<FField> removed = new ArrayList<FField>();
					for (int i : selectedIndices) {
						FField f = ((ArrayListModel<FField>) rightList.getModel()).getCollection().get(i);
						((ArrayListModel<FField>) leftList.getModel()).add(f);
						removed.add(f);
					}
					for (FField f : removed) {
						((ArrayListModel<FField>) rightList.getModel()).remove(f);
					}
					Collections.sort(((ArrayListModel<FField>) rightList.getModel()).getCollection());
					Collections.sort(((ArrayListModel<FField>) leftList.getModel()).getCollection());
					((ArrayListModel<FField>) leftList.getModel()).allChanged();
					((ArrayListModel<FField>) rightList.getModel()).allChanged();
				});
				GridBagConstraints gbc_button = new GridBagConstraints();
				gbc_button.anchor = GridBagConstraints.NORTH;
				gbc_button.weighty = 1.0;
				gbc_button.gridx = 0;
				gbc_button.gridy = 1;
				panel.add(button, gbc_button);
			}
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.weighty = 1.0;
			gbc_panel.weightx = 1.0;
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 2;
			gbc_panel.gridy = 1;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0};
			gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setPreferredSize(new Dimension(1, 1));
				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
				gbc_scrollPane.gridx = 0;
				gbc_scrollPane.gridy = 0;
				gbc_scrollPane.weighty = 1.0;
				gbc_scrollPane.weightx = 1.0;
				gbc_scrollPane.fill = GridBagConstraints.BOTH;

				// #RM1958 remove JList generic argument
				rightList = new JList();

				rightList.setModel(new ArrayListModel(new ArrayList<FField>()));
				rightList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				scrollPane.setViewportView(rightList);

				panel.add(scrollPane, gbc_scrollPane);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					apply(file);
					dispose();
				});
				{
					JButton btnOutputHpDiff = new JButton("Output HP diff (dev tool)");
					btnOutputHpDiff.addActionListener(arg0 -> diffHPIds(file));
					{
						JButton btnOutputSelectedFields = new JButton("Output Selected Fields (dev tool)");
						btnOutputSelectedFields.addActionListener(arg0 -> outputFields(file));
						buttonPane.add(btnOutputSelectedFields);
					}
					buttonPane.add(btnOutputHpDiff);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(e -> dispose());
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	private void outputFields(File f) {
		ElementParser p = new ElementParser();
		StringBuffer b = new StringBuffer();
		try {
			p.loadAndParseCustomXML(f, false, "./data/config/BlockTypes.properties", GameResourceLoader.getConfigInputFile());

			for (ElementInformation other : p.getInfoElements()) {
				ElementInformation elementInformation = ElementKeyMap.getInfo(other.getId());
				if (elementInformation != null && elementInformation.getId() == other.getId()) {

					for (FField field : ((ArrayListModel<FField>) rightList.getModel()).getCollection()) {

						field.f.setAccessible(true);
						if (field.f.getType() == Boolean.TYPE) {
							if (field.f.getBoolean(elementInformation) != field.f.getBoolean(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getBoolean(elementInformation) + " / " + field.f.getBoolean(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getBoolean(elementInformation));
						} else if (field.f.getType() == Integer.TYPE) {
							if (field.f.getInt(elementInformation) != field.f.getInt(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getInt(elementInformation) + " / " + field.f.getInt(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getInt(elementInformation));
						} else if (field.f.getType() == Short.TYPE) {
							if (field.f.getShort(elementInformation) != field.f.getShort(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getShort(elementInformation) + " / " + field.f.getShort(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getShort(elementInformation));
						} else if (field.f.getType() == Byte.TYPE) {
							if (field.f.getByte(elementInformation) != field.f.getByte(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getByte(elementInformation) + " / " + field.f.getByte(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getByte(elementInformation));
						} else if (field.f.getType() == Float.TYPE) {
							if (field.f.getFloat(elementInformation) != field.f.getFloat(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getFloat(elementInformation) + " / " + field.f.getFloat(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getFloat(elementInformation));
						} else if (field.f.getType() == Double.TYPE) {
							if (field.f.getDouble(elementInformation) != field.f.getDouble(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getDouble(elementInformation) + " / " + field.f.getDouble(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getDouble(elementInformation));
						} else if (field.f.getType() == Long.TYPE) {
							if (field.f.getLong(elementInformation) != field.f.getLong(other))
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getLong(elementInformation) + " / " + field.f.getLong(other));
							else
								System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.getLong(elementInformation));
						} else {
							System.err.println(elementInformation + " " + field.f.getName() + ": " + field.f.get(elementInformation) + " / " + field.f.get(other));

						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	private void diffHPIds(File f) {
		ElementParser p = new ElementParser();
		StringBuffer b = new StringBuffer();
		try {
			p.loadAndParseCustomXML(f, false, "./data/config/BlockTypes.properties", GameResourceLoader.getConfigInputFile());

			b.append("[");
			for (ElementInformation other : p.getInfoElements()) {
				ElementInformation elementInformation = ElementKeyMap.getInfo(other.getId());
				if (elementInformation != null && elementInformation.getId() == other.getId()) {

					if (elementInformation.getMaxHitPointsFull() < other.getMaxHitPointsFull()) {
						b.append(elementInformation.getId() + ", ");
					}
//					for(FField field : ((ArrayListModel<FField>)rightList.getModel()).getCollection()){
//						System.err.println("APPLYING: "+field.f.getName()+" for "+elementInformation);
//						field.f.setAccessible(true);
//						if(field.f.getType() == Boolean.TYPE){
//							field.f.setBoolean(elementInformation, field.f.getBoolean(other));
//						}else if(field.f.getType() == Integer.TYPE){
//							field.f.setInt(elementInformation, field.f.getInt(other));
//						}else if(field.f.getType() == Short.TYPE){
//							field.f.setShort(elementInformation, field.f.getShort(other));
//						}else if(field.f.getType() == Byte.TYPE){
//							field.f.setByte(elementInformation, field.f.getByte(other));
//						}else if(field.f.getType() == Float.TYPE){
//							field.f.setFloat(elementInformation, field.f.getFloat(other));
//						}else if(field.f.getType() == Double.TYPE){
//							field.f.setDouble(elementInformation, field.f.getDouble(other));
//						}else if(field.f.getType() == Long.TYPE){
//							field.f.setLong(elementInformation, field.f.getLong(other));
//						}else{
//							field.f.set(elementInformation, field.f.get(other));
//						}
//					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
		System.err.println(b.toString());
	}

	private void apply(File f) {
		ElementParser p = new ElementParser();
		try {
			p.loadAndParseCustomXML(f, false, "./data/config/BlockTypes.properties", GameResourceLoader.getConfigInputFile());

			for (ElementInformation other : p.getInfoElements()) {
				ElementInformation elementInformation = ElementKeyMap.getInfo(other.getId());
				if (elementInformation != null) {

					for (FField field : ((ArrayListModel<FField>) rightList.getModel()).getCollection()) {
						System.err.println("APPLYING: " + field.f.getName() + " for " + elementInformation);
						field.f.setAccessible(true);
						if (field.f.getType() == Boolean.TYPE) {
							field.f.setBoolean(elementInformation, field.f.getBoolean(other));
						} else if (field.f.getType() == Integer.TYPE) {
							field.f.setInt(elementInformation, field.f.getInt(other));
						} else if (field.f.getType() == Short.TYPE) {
							field.f.setShort(elementInformation, field.f.getShort(other));
						} else if (field.f.getType() == Byte.TYPE) {
							field.f.setByte(elementInformation, field.f.getByte(other));
						} else if (field.f.getType() == Float.TYPE) {
							field.f.setFloat(elementInformation, field.f.getFloat(other));
						} else if (field.f.getType() == Double.TYPE) {
							field.f.setDouble(elementInformation, field.f.getDouble(other));
						} else if (field.f.getType() == Long.TYPE) {
							field.f.setLong(elementInformation, field.f.getLong(other));
						} else {
							field.f.set(elementInformation, field.f.get(other));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	// #RM1958 remove JList generic argument
	private JList parse() {
		Field[] declaredFields = ElementInformation.class.getDeclaredFields();

		ArrayList<FField> options = new ArrayList<FField>(declaredFields.length);
		for (int i = 0; i < declaredFields.length; i++) {
			if (declaredFields[i].getAnnotation(Element.class) != null) {
				Element annotation = declaredFields[i].getAnnotation(Element.class);
				options.add(new FField(declaredFields[i], annotation));
			}
		}

		// #RM1958 remove JList generic argument
		leftList = new JList();
		leftList.setModel(new ArrayListModel(options));
		leftList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		Collections.sort(((ArrayListModel<FField>) leftList.getModel()).getCollection());
		return leftList;
	}

	private class FField implements Comparable<FField> {
		private Field f;
		private Element anno;

		public FField(Field f, Element anno) {
			super();
			this.f = f;
			this.anno = anno;
		}

		@Override
		public String toString() {
			return anno.parser().tag;
		}

		@Override
		public int compareTo(FField o) {
			return toString().compareTo(o.toString());
		}

	}
}
