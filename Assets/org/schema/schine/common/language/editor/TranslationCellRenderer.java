package org.schema.schine.common.language.editor;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.schema.schine.common.language.Translation;

public class TranslationCellRenderer extends DefaultListCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Color lRed0 = new Color(255, 120, 120);
	Color lRed1 = new Color(255, 140, 140);
	Color lGreen0 = new Color(120, 255, 120);
	Color lGreen1 = new Color(140, 255, 140);
	Color lBlue0 = new Color(235, 140, 200);
	Color lBlue1 = new Color(140, 235, 200);
	DefaultListCellRenderer d = new DefaultListCellRenderer(); 
	DecimalFormat f = new DecimalFormat("0000");
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		JLabel c = (JLabel) d.getListCellRendererComponent(list, f.format(index)+"  \n"+value, index,
				isSelected, cellHasFocus);
		if (isSelected) {
			if (value != null && value instanceof Translation) {
				Translation t = (Translation) value;
				if(t.translator.equals("default")){
					c.setBackground(lBlue0);
				}else{
					c.setBackground(lBlue1);
				}
				
				return c;
			}
		} else {
			
			if (value != null && value instanceof Translation) {
				Translation t = (Translation) value;
				
				if(t.translator.equals("default")){
					c.setBackground(index%2 == 0 ? lRed0 : lRed1);
				}else{
					c.setBackground(index%2 == 0 ? lGreen0 : lGreen1);
				}
			}
			return c;
		}
		return super.getListCellRendererComponent(list, f.format(index)+"  \n"+value, index,
				isSelected, cellHasFocus);
	}
}
