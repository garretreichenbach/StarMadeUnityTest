package org.schema.schine.common.language;

import java.util.List;

public class Translation{
	public String original;
	public String translation;
	public String[] args;
	public String var;
	public String translator;
	
	public List<Translation> dupl;
	public boolean changed;
	public String oldTranslation;
	public Translation(String var, String original, String translation, String oldTranslation, String translator, String[] args) {
		super();
		this.var = var;
		this.args = args;
		this.original = original;
		this.translation = translation;
		this.translator = translator;
		this.oldTranslation = oldTranslation != null ? oldTranslation : "";
	}
	public Translation(Translation t) {
		this(t.var, t.original, new String(t.translation), new String(t.oldTranslation), new String(t.translator), new String[t.args.length]);
		
		for(int i = 0; i < args.length; i++){
			args[i] = new String(t.args[i]);
		}
	}
	public Translation() {
		// TODO Auto-generated constructor stub
	}
	public void set(Translation t) {
		var = t.var;
		original = t.original;
		translation = new String(t.translation);
		translator = new String(t.translator);
		args = new String[t.args.length];
		
		for(int i = 0; i < args.length; i++){
			args[i] = new String(t.args[i]);
		}
	}
	@Override
	public String toString() {
		return original;
	}
	
	
	
}
