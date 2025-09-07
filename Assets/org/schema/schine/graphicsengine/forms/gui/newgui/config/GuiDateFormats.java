package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import java.text.DateFormat;

import org.schema.common.config.ConfigurationElement;

public class GuiDateFormats extends GuiConfig {

	public static final DateFormat longFormat = DateFormat.getDateInstance(DateFormat.LONG);
	public static final DateFormat mediumFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
	public static final DateFormat shortFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	@ConfigurationElement(name = "Custom0")
	public static DateFormat custom0;
	@ConfigurationElement(name = "NetworkStatTime")
	public static DateFormat ntStatTime;
	@ConfigurationElement(name = "MailTime")
	public static DateFormat mailTime;
	@ConfigurationElement(name = "MessageLogTime")
	public static DateFormat messageLogTime;
	@ConfigurationElement(name = "FactionNewsTime")
	public static DateFormat factionNewsTime;
	@ConfigurationElement(name = "CatalogEntryCreated")
	public static DateFormat catalogEntryCreated;
	@ConfigurationElement(name = "FactionMemberLastSeenTime")
	public static DateFormat factionMemberLastSeenTime;

	public GuiDateFormats() {
	}

	@Override
	protected String getTag() {
		return "GuiDateFormats";
	}

}
