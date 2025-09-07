package org.schema.schine.graphicsengine.movie.subtitles;

import java.io.File;
import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class AbstractSubtitleManager {

	public long time = -1;
	private final Long2ObjectRBTreeMap<Subtitle> map;
	
	private final List<Subtitle> activeSubtitles = new ObjectArrayList<Subtitle>();
	
	public AbstractSubtitleManager(File file) throws SubtitleParseException{
		this.map = parseSubtitles(file);
	}
	
	public void updateSubtitles(long movieTime){
		getSubtitlesStartedBetween(time, movieTime);
		time = movieTime;
		
		
		for(int i = 0; i < activeSubtitles.size(); i++){
			Subtitle st = activeSubtitles.get(i);
			if(movieTime > st.endTime  || movieTime < st.startTime){
				activeSubtitles.remove(i);
				i--;
			}
		}
	}
	
	private void getSubtitlesStartedBetween(long fromTime, long toTime) {
		for(long s : map.keySet()){
			if(s > fromTime && s <= toTime){
				activeSubtitles.add(map.get(s));
			}
		}
	}

	protected abstract Long2ObjectRBTreeMap<Subtitle> parseSubtitles(File file) throws SubtitleParseException;

	public List<Subtitle> getActiveSubtitles() {
		return activeSubtitles;
	}
	
	
}
