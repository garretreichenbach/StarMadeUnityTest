package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;

public class ListColorPalette extends GuiConfig {

	@ConfigurationElement(name = "MainListBackgroundColor")
	public static Vector4f mainListBackgroundColor;
	@ConfigurationElement(name = "MainListBackgroundColorAlternate")
	public static Vector4f mainListBackgroundColorAlternate;
	
	@ConfigurationElement(name = "FavoriteHighlightColor")
	public static Vector4f favoriteHighlightColor;
	@ConfigurationElement(name = "FavoriteHighlightColorAlternate")
	public static Vector4f favoriteHighlightColorAlternate;
	@ConfigurationElement(name = "FavortiteSelectedColor")
	public static Vector4f selectedColorFavorite;
	
	@ConfigurationElement(name = "SpecialHighlightColor")
	public static Vector4f specialHighlightColor;
	@ConfigurationElement(name = "SpecialHighlightColorAlternate")
	public static Vector4f specialHighlightColorAlternate;
	@ConfigurationElement(name = "SpecialSelectedColor")
	public static Vector4f selectedColorSpecial;
	
	
	@ConfigurationElement(name = "BuyListBackgroundColor")
	public static Vector4f buyListBackgroundColor;
	@ConfigurationElement(name = "BuyListBackgroundColorAlternate")
	public static Vector4f buyListBackgroundColorAlternate;
	@ConfigurationElement(name = "BuyListBackgroundColorSelected")
	public static Vector4f buyListBackgroundColorSelected;
	
	@ConfigurationElement(name = "SellListBackgroundColor")
	public static Vector4f sellListBackgroundColor;
	@ConfigurationElement(name = "SellListBackgroundColorAlternate")
	public static Vector4f sellListBackgroundColorAlternate;
	@ConfigurationElement(name = "SellListBackgroundColorSelected")
	public static Vector4f sellListBackgroundColorSelected;
	
	@ConfigurationElement(name = "SelectedColor")
	public static Vector4f selectedColor;
	
	

	public ListColorPalette() {
	}

	@Override
	protected String getTag() {
		return "ListColorPalette";
	}

}
