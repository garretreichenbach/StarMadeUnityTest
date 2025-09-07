package org.schema.game.common.data.blockeffects.config.elements;

import org.schema.common.util.StringTools;
import org.schema.schine.common.language.Lng;

public enum ModifierStackType {
	ADD("+"),
	MULT("x"),
	SET(""),
	
	;
	public final String sign;

	private ModifierStackType(String sign){
		this.sign = sign;
	}
	
	public String getVerbFloat(double value, String what, boolean percentage, boolean time, boolean respectSetHundretPercen){
		
		if(percentage){
			switch(this){
			case ADD:
				return Lng.str("adds %s%% to %s", StringTools.formatPointZero(value * 100d), what);
			case MULT:
				if(value < 1d){
					return Lng.str("decreases %s by %s%%", what, StringTools.formatPointZero(value * 100d));
				}else{
					return Lng.str("increases %s by %s%%", what, StringTools.formatPointZero(value * 100d));	
				}
				
			case SET:
				if(value != 1d || respectSetHundretPercen){
					String perm = Lng.str("permanent");
					if(time && value < 0){
						return Lng.str("sets %s to %s", what,  perm);
					}
					return Lng.str("sets %s to %s%%", what, StringTools.formatPointZero(value * 100d));
				}else{
					return "";
				}
			default:
				throw new RuntimeException(this.name());
			}
		}else{
			return getVerb(StringTools.formatPointZeroZero(value), what);
		}
	}
	public String getVerbInt(int value, String what){
		return getVerb(String.valueOf(value), what);
	}
	public String getVerb(String value, String what){
		return switch(this) {
			case ADD -> Lng.str("adds %s to %s", value, what);
			case MULT -> Lng.str("multiplies %s with %s", what, value);
			case SET -> Lng.str("sets %s to %s", what, value);
			default -> throw new RuntimeException(this.name());
		};
	}
	public String getVerbBool(boolean value, String what){
			switch(this){
			case ADD:
				return "-illegal operation ADD-";
			case MULT:
				return "-illegal operation MULT-";
			case SET:
				if(value){
					return Lng.str("enables %s", what);
				}else{
					return Lng.str("disables %s", what);
				}
			default:
				throw new RuntimeException(this.name());
			}
	}
}
