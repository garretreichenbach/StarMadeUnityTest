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

public class SegmentControllerRepeatingWeeklyDurationCondition extends SegmentControllerAbstractDateTimeCondition{

	@RuleValue(tag = "Day", intMap = {1,2,3,4,5,6,7}, int2StringMap = {"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"})
	public int day;
	
	@RuleValue(tag = "Hour")
	public int hour; 
	
	@RuleValue(tag = "Minute")
	public int minute; 
	
	@RuleValue(tag = "Second")
	public int second; 
	
	
	@RuleValue(tag = "DurationActiveSecs")
	public int secondsActive;

	
	@Override
	public Date getDate() {
		GregorianCalendar c = new GregorianCalendar();
		c.setFirstDayOfWeek(GregorianCalendar.MONDAY);
		c.set(GregorianCalendar.DAY_OF_WEEK, day);
		
		c.set(GregorianCalendar.HOUR_OF_DAY, Math.abs(hour%24));
		c.set(GregorianCalendar.MINUTE, Math.abs(minute%60));
		c.set(GregorianCalendar.SECOND, Math.abs(second%60));
		
		
		return c.getTime();
	}
	public Date getDateEnd() {
		GregorianCalendar c = new GregorianCalendar();
		c.setFirstDayOfWeek(GregorianCalendar.MONDAY);
		c.set(GregorianCalendar.DAY_OF_WEEK, day);
		
		c.set(GregorianCalendar.HOUR_OF_DAY, Math.abs(hour%24));
		c.set(GregorianCalendar.MINUTE, Math.abs(minute%60));
		c.set(GregorianCalendar.SECOND, Math.abs(second%60));
		
		c.add(GregorianCalendar.SECOND, secondsActive);
		
		return c.getTime();
	}


	@Override
	public boolean isTimeToFire(long time) {
		long timeToFire = getDate().getTime();
		long timeToEnd = getDateEnd().getTime();
		return time >= timeToFire && time < timeToEnd;
	}
	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_WEEKLY_DURATION;
	}
	@Override
	public String getDescriptionShort() {
		DateFormat dateFormat = StringTools.getSimpleDateFormat(Lng.str("HH:mm:ss"), "HH:mm:ss");
		
		return Lng.str("will be %s before %s, then %s for %s seconds",String.valueOf(!after), dateFormat.format(getDate()), String.valueOf(after), String.valueOf(secondsActive));
	}

	
	
}
