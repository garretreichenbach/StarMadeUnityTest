package org.schema.schine.graphicsengine.movie.subtitles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.vecmath.Vector4f;

import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class SvbSubtitleManager extends AbstractSubtitleManager{

	
	public SvbSubtitleManager(File file) throws SubtitleParseException {
		super(file);
	}

	private class TimeDuration{
		long startTime, endTime;
		
		public TimeDuration(String from) throws SubtitleParseException{
			String[] split = from.split(",");
			
			if(split.length != 2){
				throw new SubtitleParseException("Time code wrong format: "+from);
			}
			startTime = parseTimeCode(split[0]);
			endTime = parseTimeCode(split[1]);
		}
	}
	
	private Subtitle parseCaption(List<String> in) throws SubtitleParseException{
		if(in.isEmpty()){
			throw new SubtitleParseException("No input");
		}
		String timeCode = in.remove(0);
		
		TimeDuration d = new TimeDuration(timeCode);
		
		StringBuffer text = new StringBuffer();
		while(!in.isEmpty() && in.get(0).trim().length() > 0){
			String str = in.remove(0);
			String tt = KeyboardMappings.formatText(str.trim());
			text.append(tt+"\n");
		}
		
		Subtitle t = new Subtitle(text.toString(), new Vector4f(1f,1f,1f,1f), d.startTime, d.endTime);
		
		return t;
	}
	
	@Override
	protected Long2ObjectRBTreeMap<Subtitle> parseSubtitles(File file) throws SubtitleParseException {
		BufferedReader br = null;
		StringBuffer full = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			
			List<String> strings = new ObjectArrayList<String>();
			
			String l;
			while((l = br.readLine()) != null){
				full.append(l+"\n");
				strings.add(l);
			}
			System.err.println("PARSED SUBTITLES: "+file.getAbsolutePath()+":\n"+full);
			List<Subtitle> sts = parseSubtitles(strings);
			
			Long2ObjectRBTreeMap<Subtitle> map = new Long2ObjectRBTreeMap<Subtitle>();
			for(Subtitle t : sts){
				while(map.containsKey(t.startTime)){
					t = new Subtitle(t.text, t.color, t.startTime+1, t.endTime+1);
				}
				map.put(t.startTime, t);
			}
			
			return map;
			
		} catch (FileNotFoundException e) {
			System.err.println("PARSED IN FILE: "+full);
			throw new SubtitleParseException(file.getAbsolutePath(), e);
		} catch (IOException e) {
			System.err.println("PARSED IN FILE: "+full);
			throw new SubtitleParseException(file.getAbsolutePath(), e);
		} catch (Exception e) {
			System.err.println("PARSED IN FILE: "+full);
			throw new SubtitleParseException(file.getAbsolutePath(), e);
		} finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	private List<Subtitle> parseSubtitles(List<String> in) throws SubtitleParseException{
		List<Subtitle> l = new ObjectArrayList<Subtitle>();
		while(!in.isEmpty()){
			if(in.get(0).trim().isEmpty()){
				in.remove(0);
			}
			if(!in.isEmpty()){
				l.add(parseCaption(in));
			}
		}
		if(l.isEmpty()){
			throw new SubtitleParseException("Nothing parsed");
		}
		return l;
	}
	
	public static long parseTimeCode(String input) throws SubtitleParseException{
		try{
			String[] s = input.split(":");
			
			long hours = Long.parseLong(s[0]);
			long minutes = Long.parseLong(s[1]);
			
			
			String[] mm = s[2].split("\\.");
			long seconds = Long.parseLong(mm[0]);
			long milli = Long.parseLong(mm[1]);
			
			long totalTime = ((hours * 60L * 60L + minutes * 60L + seconds)*1000L)+milli;
			
			return totalTime;
		}catch(Exception e){
			throw new SubtitleParseException(e);
		}
	}



	
	
	
}
