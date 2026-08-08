package com.xamlo.core.engine.graphics.api.gui.font;

public interface IFont {

	/**
	 * Name of Font.
	 * Example: Arial, Times New Roman....
	 * @return
	 */
	String getFontFamily();
	
	/**
	 * Size of glyph in font sizes (Times New Roman 12)
	 * @return
	 */
	int getFontSize();
	
	/**
	 * 
	 * @return
	 */
	boolean isBold();
	
	/**
	 * 
	 * @return
	 */
	boolean isItalic();

}
