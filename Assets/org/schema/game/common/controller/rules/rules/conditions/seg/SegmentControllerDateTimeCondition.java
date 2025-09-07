package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleEntityManager;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.TimedCondition;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SegmentControllerDateTimeCondition extends SegmentControllerAbstractDateTimeCondition{

	@RuleValue(tag = "Day")
	public int day;
	
	@RuleValue(tag = "Month", intMap = {0,1,2,3,4,5,6,7,8,9,10,11}, int2StringMap = {"Jan", "Feb", "Mar", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"})
	public int month;
	
	@RuleValue(tag = "Year")
	public int year;
	
	@RuleValue(tag = "Hour")
	public int hour; 
	
	@RuleValue(tag = "Minute")
	public int minute; 
	
	@RuleValue(tag = "Second")
	public int second; 
	
	@Override
	public Date getDate() {
		GregorianCalendar c = new GregorianCalendar(year, month, Math.min(31, Math.max(1, day)), Math.abs(hour%24), Math.abs(minute%60), Math.abs(second%60));
		return c.getTime();
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_DATE_TIME;
	}
	@Override
	public String getDescriptionShort() {
		DateFormat dateFormat = StringTools.getSimpleDateFormat(Lng.str("yyyy/MM/dd HH:mm:ss"), "yyyy/MM/dd HH:mm:ss");
		
		return Lng.str("will be %s before %s, %s after (fires check on time)",String.valueOf(!after), dateFormat.format(getDate()), String.valueOf(after));
	}

	
	
}
