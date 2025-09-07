package org.schema.schine.sound.controller;

import org.schema.common.XMLSerializationInterface;

public interface MusicTag extends AudioTag, XMLSerializationInterface{
	public float getPrio();
	public long getMilliActive();
	public long getMilliMaxActive();
	
	
}
