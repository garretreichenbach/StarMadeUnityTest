package org.schema.schine.common;

import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class DebugTimer {

	public static boolean isOn() {
		return Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && Keyboard.isKeyDown(GLFW.GLFW_KEY_PAGE_UP);
	}
	
	public static class DebugElement {
		public String meta;
		public String id;
		public long taken = Long.MIN_VALUE;
		
		public long started;
		public void start() {
			started = System.currentTimeMillis();
		}
		public void end() {
			taken = System.currentTimeMillis() - started; 
		}
		public void start(String id) {
			DebugElement e = new DebugElement();
			e.id = id;
			children.put(e.id, e);
			e.start();
		}
		public void end(String id) {
			assert(children.get(id) != null):id+"; ;;; "+children;
			children.get(id).end();
		}
		public Map<String, DebugElement> children = new Object2ObjectOpenHashMap<String, DebugElement>();
		public void printTo(StringBuffer b, int layer) {
			for(int i = 0; i < layer; i++) {
				b.append("   ");
			}
			b.append(taken+" ms ["+id+"]"+(meta != null ? " [META: "+meta+"]" : "")+"\n");
			
			for(DebugElement e : children.values()) {
				e.printTo(b, layer+1);
			}
		}
		public boolean withinThreshold() {
			return taken >= EngineSettings.PROFILER_MIN_MS.getInt();
		}
		public void start(String sub1, String sub2) {
			children.get(sub1).start(sub2);
		}
		public void end(String sub1, String sub2) {
			children.get(sub1).end(sub2);
		}
		public void setMeta(String sub1, String sub2, String sub3, String meta) {
			children.get(sub1).children.get(sub2).children.get(sub3).meta = meta;
		}
		public void start(String sub1, String sub2, String sub3) {
			children.get(sub1).children.get(sub2).start(sub3);
		}
		public void end(String sub1, String sub2, String sub3) {
			children.get(sub1).children.get(sub2).end(sub3);
		}
		public void start(String sub1, String sub2, String sub3, String sub4) {
			children.get(sub1).children.get(sub2).children.get(sub3).start(sub4);
		}
		public void end(String sub1, String sub2, String sub3, String sub4) {
			children.get(sub1).children.get(sub2).children.get(sub3).end(sub4);
		}
		public void start(String sub1, String sub2, String sub3, String sub4, String sub5) {
			children.get(sub1).children.get(sub2).children.get(sub3).children.get(sub4).start(sub5);
		}
		public void end(String sub1, String sub2, String sub3, String sub4, String sub5) {
			children.get(sub1).children.get(sub2).children.get(sub3).children.get(sub4).end(sub5);
		}
	}
	
	private static class Update {
		public long updateTime;
		
		public Map<String, DebugElement> elements = new Object2ObjectOpenHashMap<String, DebugElement>();
		public void start(String id) {
			if(isOn()) {
				DebugElement e = new DebugElement();
				e.id = id;
				elements.put(e.id, e);
				e.start();
			}
		}
		public void end(String id) {
			if(isOn()) {
				elements.get(id).end();
			}
		}
		
		public void start(Object obj) {
			start(obj.toString());
		}
		public void end(Object obj) {
			end(obj.toString());
		}
		
		
		public void start(Object obj, String sub) {
				elements.get(obj.toString()).start(sub);
		}
		public void end(Object obj, String sub) {
				elements.get(obj.toString()).end(sub);
		}
		public void printTo(StringBuffer b) {
			int i = 0;
			
			for(DebugElement e : elements.values()) {
				if(e.withinThreshold()) {
					b.append("-------ELEMENT "+i+": "+e.id+(e.meta != null ? " [META: "+e.meta+"]" : "")+"---------\n");
					e.printTo(b, 1);
					i++;
				}
			}
			if(i > 0) {
				b.append("::::::::::PRINT DEBUG INFO END::::::::::");
			}
		}
		public void start(Object obj, String sub1, String sub2) {
			elements.get(obj.toString()).start(sub1, sub2);
			
		}
		public void end(Object obj, String sub1, String sub2) {
			elements.get(obj.toString()).end(sub1, sub2);
		}
		public void setMeta(Object obj, String sub1, String sub2, String sub3, String meta) {
			elements.get(obj.toString()).setMeta(sub1, sub2, sub3, meta);
			
		}
		public void start(Object obj, String sub1, String sub2, String sub3) {
			elements.get(obj.toString()).start(sub1, sub2, sub3);
			
		}
		public void end(Object obj, String sub1, String sub2, String sub3) {
			elements.get(obj.toString()).end(sub1, sub2, sub3);
		}
		public void start(Object obj, String sub1, String sub2, String sub3, String sub4) {
			elements.get(obj.toString()).start(sub1, sub2, sub3, sub4);
			
		}
		public void end(Object obj, String sub1, String sub2, String sub3, String sub4) {
			elements.get(obj.toString()).end(sub1, sub2, sub3, sub4);
		}
		public void start(Object obj, String sub1, String sub2, String sub3, String sub4, String sub5) {
			elements.get(obj.toString()).start(sub1, sub2, sub3, sub4, sub5);
			
		}
		public void end(Object obj, String sub1, String sub2, String sub3, String sub4, String sub5) {
			elements.get(obj.toString()).end(sub1, sub2, sub3, sub4, sub5);
		}
	}
	
	private Update update;
	public void start(StateInterface state) {
		if(isOn()) {
			this.update = new Update();
			this.update.updateTime = state.getUpdateTime();
		}
	}
	
	public void end() {
		if(update != null) {
			StringBuffer b = new StringBuffer();
			
			update.printTo(b);
			
			System.err.println(b);
			update = null;
		}
	}
	public void start(String id) {
		if(update != null) {
			update.start(id);
		}
	}
	
	public void end(String id) {
		if(update != null) {
			update.end(id);
		}
	}
	
	
	
	public void start(Object obj) {
		if(update != null) {
			update.start(obj);
		}
	}
	public void end(Object obj) {
		if(update != null) {
			update.end(obj);
		}
	}
	
	
	
	
	public void start(Object obj, String sub) {
		if(update != null) {
			update.start(obj, sub);
		}
	}
	public void end(Object obj, String sub) {
		if(update != null) {
			update.end(obj, sub);
		}
	}

	public void start(Object obj, String sub1, String sub2) {
		if(update != null) {
			update.start(obj, sub1, sub2);
		}
	}
	public void end(Object obj, String sub1, String sub2) {
		if(update != null) {
			update.end(obj, sub1, sub2);
		}
	}
	public void start(Object obj, String sub1, String sub2, String sub3) {
		if(update != null) {
			update.start(obj, sub1, sub2, sub3);
		}
	}
	public void start(Object obj, String sub1, String sub2, String sub3, String sub4) {
		if(update != null) {
			update.start(obj, sub1, sub2, sub3, sub4);
		}
	}
	public void start(Object obj, String sub1, String sub2, String sub3, String sub4, String sub5) {
		if(update != null) {
			update.start(obj, sub1, sub2, sub3, sub4, sub5);
		}
	}
	public void end(Object obj, String sub1, String sub2, String sub3, String sub4) {
		if(update != null) {
			update.end(obj, sub1, sub2, sub3, sub4);
		}
	}
	public void end(Object obj, String sub1, String sub2, String sub3, String sub4, String sub5) {
		if(update != null) {
			update.end(obj, sub1, sub2, sub3, sub4, sub5);
		}
	}
	public void setMeta(Object obj, String sub1, String sub2, String sub3, String meta) {
		if(update != null) {
			update.setMeta(obj, sub1, sub2,sub3, meta);
		}
	}
	public void end(Object obj, String sub1, String sub2, String sub3) {
		if(update != null) {
			update.end(obj, sub1, sub2, sub3);
		}
	}
	
	
}
