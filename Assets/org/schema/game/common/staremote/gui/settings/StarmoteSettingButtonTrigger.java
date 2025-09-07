package org.schema.game.common.staremote.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

public abstract class StarmoteSettingButtonTrigger extends JButton implements StarmoteSettingElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final String valueName;
	private Object lastValue;

	public StarmoteSettingButtonTrigger(String name) {
		this.valueName = name;

		lastValue = getValue();

		addActionListener(arg0 -> trigger());
	}

	@Override
	public JComponent getComponent() {
		//		this.setText(new Object(){
		//			public String toString(){
		//				return getText();
		//			}
		//		});
		this.setText(valueName + "   " + getValue());
		this.setOpaque(true);

		//	    if(isSelected){
		//	      this.setForeground(UIManager.getColor("List.selectionForeground"));
		//	      this.setBackground(UIManager.getColor("List.selectionBackground"));
		//	    }
		//	    else{
		//	      this.setForeground(person.getSchriftfarbe());
		//	      this.setBackground(UIManager.getColor("List.background"));
		//	    }

		return this;
	}

	@Override
	public String getValueName() {
		return valueName;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.staremote.gui.entity.StaremoteSettingElement#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean update() {
		Object newVal = getValue();
		//		System.err.println("CHECKING "+lastValue+" ?= "+newVal);
		if (!newVal.equals(lastValue)) {
			this.setText(valueName + "   " + getValue());
			lastValue = newVal;
			//			System.err.println("TEXT UPDATE "+getText());
			return true;
		}
		return false;
	}

	public abstract Object getValue();

	public abstract void trigger();
}
