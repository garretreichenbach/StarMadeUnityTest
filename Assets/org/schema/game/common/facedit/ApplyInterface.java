package org.schema.game.common.facedit;

import java.lang.reflect.Field;

import org.schema.game.common.data.element.ElementInformation;

public interface ApplyInterface {
	public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException;

	public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException;
}
