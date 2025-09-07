package org.schema.game.common.staremote.gui.settings;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

public abstract class StarmoteSettingTextLabel extends JLabel implements StarmoteSettingElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final String valueName;
	private Object lastValue;

	public StarmoteSettingTextLabel(String name) {
		this.valueName = name;

		lastValue = getValue();
	}

	@Override
	public JComponent getComponent() {
		//		this.setText(new Object(){
		//			public String toString(){
		//				return getText();
		//			}
		//		});
		this.setText(getValue().toString());
		this.setOpaque(true);

		//	    if(isSelected){
		//	      this.setForeground(UIManager.getColor("List.selectionForeground"));
		//	      this.setBackground(UIManager.getColor("List.selectionBackground"));
		//	    }
		//	    else{
		//	      this.setForeground(person.getSchriftfarbe());
		this.setBackground(UIManager.getColor("List.background"));
		//	    }

		return this;
	}

	@Override
	public String getValueName() {
		return valueName;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean update() {
		Object newVal = getValue();
		//		System.err.println("CHECKING "+lastValue+" ?= "+newVal);
		if (!newVal.equals(lastValue)) {
			this.setText(newVal.toString());
			lastValue = newVal;
			//			System.err.println("TEXT UPDATE "+getText());
			return true;
		}
		return false;
	}

	public abstract Object getValue();

}
