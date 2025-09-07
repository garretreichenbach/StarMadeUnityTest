package org.schema.game.common.controller.elements.combination.modifier;

import org.schema.common.config.ConfigParserException;
import org.schema.common.config.MultiConfigField;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.combination.CombinationSettings;
import org.schema.game.common.controller.elements.config.ReactorDualConfigElement;
import org.schema.game.common.data.element.ElementCollection;
import org.w3c.dom.Node;

public abstract class MultiConfigModifier<E extends Modifier<C, S>, C extends ElementCollection, S extends CombinationSettings> extends ReactorDualConfigElement implements MultiConfigField{
	private E[] field;
	public boolean initialized;
	
	public MultiConfigModifier() {
		super();
		set(instance(), instance());
	}
	public E get(int index){
		return field[index];
	}
	public void set(E ... val){
		this.field = val;
	}
	public abstract E instance();
	public E get(boolean reactor){
		return field[getIndex(reactor)];
	}
	public E get(ElementCollectionManager<?,?,?> c) {
		return get(c.getSegmentController().isUsingPowerReactors());
	}
	public E get(UsableElementManager<?,?,?> c) {
		return get(c.getSegmentController().isUsingPowerReactors());
	}
	public void load(Node item) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		for(int i = 0; i < field.length; i++){
			field[i].loadedDual = false;
			field[i].load(item, i);
		}			
		
		//use for debugging dual config setting
//		for(int i = 0; i < field.length; i++){
//			if(field[i].loadedDual){
//				System.err.println("DUAL CONFIG COMBO LOADED: "+item.getParentNode().getParentNode().getNodeName()+"->"+item.getParentNode().getNodeName()+"->"+item.getNodeName()+": INDEX: "+i+"("+(i == Modifier.OLD_POWER_INDEX ? "NO_REACTOR" : "REACTOR")+"): \n"+field[i]);
//			}
//		}			
	}
	public boolean checkInitialized() {
		if(!initialized){
			return false;
		}
		for(int i = 0; i < field.length; i++){
			if(!field[i].initialized){
				return false;
			}
		}
		return true;
	}
	public void setInitialized(boolean b, boolean setAlsoFields) {
		initialized = b;
		if(setAlsoFields){
			for(int i = 0; i < field.length; i++){
				field[i].initialized = b;
			}
		}
	}
}
