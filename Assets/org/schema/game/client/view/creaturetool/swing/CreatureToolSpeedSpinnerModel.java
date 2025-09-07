package org.schema.game.client.view.creaturetool.swing;

import java.util.Vector;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.schema.game.client.view.creaturetool.CreatureTool;
import org.schema.schine.resource.CreatureStructure.PartType;

public class CreatureToolSpeedSpinnerModel implements SpinnerModel {

	private final CreatureTool tool;
	private Vector<ChangeListener> listeners = new Vector<ChangeListener>();
	private PartType[] types;

	public CreatureToolSpeedSpinnerModel(CreatureTool tool, PartType... types) {
		this.tool = tool;
		this.types = types;
	}

	@Override
	public Object getValue() {
		if (tool.getCreature() != null) {
			System.err.println("GET VALUE: " + tool.getCreature() + " value "
					+ tool.getCreature().getSpeed());
			return tool.getCreature().getSpeed();
		} else {
			System.err.println("VAL 0");
			return 0f;
		}
	}

	@Override
	public void setValue(Object value) {
		System.err.println("VALUE: " + tool.getCreature() + " value " + value);
		if (tool.getCreature() != null && value != null) {
			try {
				tool.getCreature().setSpeed(Float.parseFloat(value.toString()));
				for (PartType type : types) {
					tool.updateAnimSpeed(type, tool.getCreature().getSpeed());
					tool.updateAnimSpeed(type, tool.getCreature().getSpeed());
					tool.updateAnimSpeed(type, tool.getCreature().getSpeed());
				}
			} catch (NumberFormatException e) {
			}
			;
		}
		this.fireChangeEvent();
	}

	@Override
	public Object getNextValue() {
		if (tool.getCreature() != null) {
			return tool.getCreature().getSpeed() + 1f;

		} else {
			return 0f;
		}
	}

	@Override
	public Object getPreviousValue() {
		if (tool.getCreature() != null) {
			return tool.getCreature().getSpeed() - 1f;
		} else {
			return 0f;
		}
	}

	@Override
	public void addChangeListener(ChangeListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		this.listeners.remove(l);
	}

	public void fireChangeEvent() {
		for (ChangeListener l : this.listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}

}
