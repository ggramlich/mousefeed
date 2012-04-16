package com.mousefeed.eclipse.popup;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Launcher runnable to open preference dialog.
 * 
 * @author Robert Wloch
 */
class PreferenceDialogLauncher implements Runnable {
    /**
     * ID of keys preference page.
     */
    static final String ORG_ECLIPSE_UI_PREFERENCE_PAGES_KEYS_ID = "org.eclipse.ui.preferencePages.Keys";

    /**
     * Data object used as data parameter to the keys preference page.
     */
    private final Object data;

    /**
     * Constructs a launcher to open the keys preference page with the optional data object as parameter.
     * @param data optional data object used as data parameter to the keys preference page
     */
    PreferenceDialogLauncher(final Object data) {
        this.data = data;
    }

    /**
     * Creates and opens the Keys preference page.
     */
    public void run() {
        final Display workbenchDisplay = PlatformUI.getWorkbench().getDisplay();
        final Shell activeShell = workbenchDisplay.getActiveShell();
        
        final String id = ORG_ECLIPSE_UI_PREFERENCE_PAGES_KEYS_ID;
        final String[] displayedIds = new String[] {id};
        final PreferenceDialog preferenceDialog = 
                PreferencesUtil.createPreferenceDialogOn(activeShell, id, displayedIds, data);
        preferenceDialog.open();
    }
}