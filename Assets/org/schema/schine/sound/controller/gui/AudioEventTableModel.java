package org.schema.schine.sound.controller.gui;

import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.schema.schine.sound.controller.FiredAudioEvent;
import org.schema.schine.sound.controller.FiredAudioEvent.FiredAudioEventHeader;
import org.schema.schine.sound.controller.config.AudioEntry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class AudioEventTableModel extends AbstractTableModel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3564114914610824670L;
	private int filter;
	private static int filterID = 1;	
	public enum AudioListFilter{
		SHOW_WITH_AUDIO,
		SHOW_WITHOUT_AUDIO,
		SHOW_TAG_AUDIO,
		SHOW_MANUAL_AUDIO,
		SHOW_REMOTE_AUDIO,
		SHOW_NON_REMOTE_AUDIO,
		
		;
		
		public final int filter;
		private AudioListFilter() {
			this.filter = filterID;
			filterID *= 2;
		}
		
		
	}
	public boolean isShow(AudioEntry e) {
		return e.isShowInList(
				isFilter(AudioListFilter.SHOW_WITH_AUDIO),
				isFilter(AudioListFilter.SHOW_WITHOUT_AUDIO),
				isFilter(AudioListFilter.SHOW_TAG_AUDIO),
				isFilter(AudioListFilter.SHOW_MANUAL_AUDIO),
				isFilter(AudioListFilter.SHOW_REMOTE_AUDIO),
				isFilter(AudioListFilter.SHOW_NON_REMOTE_AUDIO)
				);
	}
	public void updateWidths(JTable table) {
		for(int i = 0; i < FiredAudioEvent.COLUMNS.length; i ++) {
			FiredAudioEventHeader c = FiredAudioEvent.COLUMNS[i];
			table.getColumnModel().getColumn(i).setPreferredWidth(c.preferredWidth);
		}
		
	}
	public AudioEventTableModel() {
		setAllFilter();
	}
	public boolean isFilter(AudioListFilter f) {
		return (filter & f.filter) == f.filter;
	}
	public void setAllFilter() {
		setFilter(AudioListFilter.values());
	}
	public void setFilter(AudioListFilter ... fs) {
		filter = 0;
		for(AudioListFilter f : fs) {
			filter |= f.filter;
		}
	}
	public void addFilter(AudioListFilter ... fs) {
		for(AudioListFilter f : fs) {
			filter |= f.filter;
		}
	}
	public void removeFilter(AudioListFilter ... fs) {
		for(AudioListFilter f : fs) {
			filter &= ~f.filter;
		}
	}
	
	public final List<FiredAudioEvent> events = new ObjectArrayList<>();
	
	
	@Override
	public int getRowCount() {
		return events.size();
	}
	@Override
	public int getColumnCount() {
		return FiredAudioEvent.COLUMNS.length;
	}
	@Override
	public String getColumnName(int column) {
		return FiredAudioEvent.COLUMNS[column].name;
	}
	@Override
	public Object getValueAt(int row, int column) {
		return FiredAudioEvent.COLUMNS[column].r.getValue(events.get(row));
	}
	public void onUpdatedEvent(AudioEntry e) {
		SwingUtilities.invokeLater(() -> {
			for(int i = 0; i < events.size(); i++) {
				if(events.get(i).entry.id == e.id) {
					events.get(i).entry = e;
					fireTableRowsUpdated(i, i);
				}
			}
		});
	}

	public void onFiredEvent(FiredAudioEvent event) {
		if(isShow(event.entry)) {
			SwingUtilities.invokeLater(() -> {
				events.add(0, event);
				fireTableRowsInserted(0,0);
				if(events.size() > 100) {
					int del = events.size()-1;
					events.remove(del);
					fireTableRowsDeleted(del, del);
				}
			});
		}
		
	}
	public FiredAudioEvent getEventAtRow(int index) {
		return events.get(index);
	}
}
