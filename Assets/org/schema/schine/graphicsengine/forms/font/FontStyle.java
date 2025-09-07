package org.schema.schine.graphicsengine.forms.font;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont;

public class FontStyle {
	public static final FontStyle def = new FontStyle(FontSize.MEDIUM_15);
	public static final FontStyle small = new FontStyle(FontSize.SMALL_14);
	public static final FontStyle big = new FontStyle(FontSize.BIG_20);
	public final FontInterface fontSize;
	public FontStyle(FontInterface s) {
		this.fontSize = s;
	}
	public UnicodeFont getFont() {
		return fontSize.getFont();
	}
}
