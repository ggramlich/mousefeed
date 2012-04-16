package com.mousefeed.eclipse.popup;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class FontHelper {

    /**
     * Number of times to increase font size in.
     */
    public static final int FONT_INCREASE_MULT = 2;

    /**
     * Current dialog display.
     */
    private final Display display;

    public FontHelper(Display display) {
        this.display = display;
    }

    /**
     * Configures big font for the control.
     * 
     * @param c the control to increase font for. Not <code>null</code>.
     */
    public void configureBigFont(final Control c) {
        final FontData[] fontData = c.getFont().getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setHeight(fontData[i].getHeight() * FONT_INCREASE_MULT);
        }
        final Font newFont = new Font(display, fontData);
        c.setFont(newFont);
        c.addDisposeListener(new DestroyFontDisposeListener(newFont));
    }

}
