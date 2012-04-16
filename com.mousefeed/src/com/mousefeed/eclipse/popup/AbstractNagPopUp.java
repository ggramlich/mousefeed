package com.mousefeed.eclipse.popup;

import static com.mousefeed.eclipse.Layout.WHOLE_SIZE;
import static com.mousefeed.eclipse.Layout.WINDOW_MARGIN;
import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang.time.DateUtils.MILLIS_PER_SECOND;

import com.mousefeed.client.Messages;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AbstractNagPopUp extends PopupDialog {

    /**
     * How close to cursor along X axis the popup will be shown.
     */
    private static final int DISTANCE_TO_CURSOR = 50;
    /**
     * Time after which the pop up will automatically close itself.
     */
    private static final int CLOSE_TIMEOUT = 4 * (int) MILLIS_PER_SECOND;
    /**
     * Timeout, after which listener closes the dialog on any user action.
     * Is necessary to skip events caused by the current user action.
     */
    private static final int CLOSE_LISTENER_TIMEOUT = 250;
    /**
     * Provides messages text.
     */
    protected static final Messages MESSAGES = new Messages(NagPopUp.class);
    /**
     * The last action invocation reminder text factory.
     */
    private static final LastActionInvocationRemiderFactory REMINDER_FACTORY = new LastActionInvocationRemiderFactory();
    /**
     * @see NagPopUp#NagPopUp(String, String, boolean)
     */
    protected String actionName;
    /**
     * @see NagPopUp#NagPopUp(String, String, boolean)
     */
    protected String accelerator;
    /**
     * Indicates whether MouseFeed canceled the action the popup notifies about.
     */
    protected boolean actionCancelled;
    /**
     * Is <code>true</code> when the dialog is already open, but not closed yet.
     */
    private boolean open;
    /**
     * The notification text.
     */
    protected StyledText actionDescriptionText;
    /**
     * Closes the dialog on any outside action, such as click, key press, etc.
     */
    private Listener closeOnActionListener = new Listener() {
            public void handleEvent(final Event event) {
                close();
            }
        };
    /**
     * FontHelper to provide font-related methods
     */
    protected FontHelper fontHelper = new FontHelper(getDisplay());

    /**
     * Current dialog display. Never <code>null</code>.
     */
    private static Display getDisplay() {
        return PlatformUI.getWorkbench().getDisplay();
    }

    /**
     * Creates the text control to show action description.
     * @param parent the parent control. Not <code>null</code>. 
     * @return the text control. Not <code>null</code>.
     */
    protected StyledText createActionDescriptionText(final Composite parent) {
        notNull(parent);
        final StyledText text = new StyledText(parent, SWT.READ_ONLY);
        configureFormData(text);
        text.setText(accelerator + " (" + actionName + ")");
        if (actionCancelled) {
            final StyleRange style = new StyleRange();
            style.start = 0;
            style.length = text.getText().length();
            style.strikeout = true;
            text.setStyleRange(style);
        }
    
        fontHelper.configureBigFont(text);
    
        // since SWT.NO_FOCUS is only a hint...
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent event) {
                close();
            }
        });
        return text;
    }

    /**
     * Configures sizes and margins for this. 
     * @param c the control to set the form data for. Not <code>null</code>.
     */
    void configureFormData(final Control c) {
        final FormData formData = new FormData();
        formData.left = new FormAttachment(WINDOW_MARGIN);
        formData.right = new FormAttachment(WHOLE_SIZE, -WINDOW_MARGIN);
        formData.top = new FormAttachment(WINDOW_MARGIN);
        formData.bottom = new FormAttachment(WHOLE_SIZE, -WINDOW_MARGIN);
        c.setLayoutData(formData);
    }

    /**
     * Set the text color here, not in {@link #createDialogArea(Composite)},
     * because it is redefined after that method is called.
     * @param parent the control parent. Not <code>null</code>.
     * @return the super value.
     */
    @Override
    protected Control createContents(final Composite parent) {
        final Control control = super.createContents(parent);
        if (actionCancelled) {
            actionDescriptionText.setForeground(
                    getDisplay().getSystemColor(SWT.COLOR_RED));
        }
        return control;
    }

    /**
     * {@inheritDoc}
     * Places the dialog close to a mouse pointer.
     */
    @Override
    public int open() {
        open = true;
        setParentShell(getDisplay().getActiveShell());
        if (actionCancelled) {
            getDisplay().beep();
        }
        getDisplay().timerExec(CLOSE_TIMEOUT,
                new Runnable() {
                    public void run() {
                        close();
                    }
                });
        addCloseOnActionListeners();
        return super.open();
    }

    /** {@inheritDoc} */
    @Override
    public boolean close() {
        open = false;
        removeCloseOnActionListeners();
        return super.close();
    }

    /**
     * Adds listeners to close the dialog on any user action.
     * @see #closeOnActionListener
     * @see #CLOSE_LISTENER_TIMEOUT
     */
    private void addCloseOnActionListeners() {
        getDisplay().timerExec(CLOSE_LISTENER_TIMEOUT,
                new Runnable() {
                    public void run() {
                        if (!open) {
                            return;
                        }
                        final Listener l = closeOnActionListener;
                        getDisplay().addFilter(SWT.MouseDown, l);
                        getDisplay().addFilter(SWT.Selection, l);
                        getDisplay().addFilter(SWT.KeyDown, l);
                    }
                });
    }

    /**
     * Removes listeners to close the dialog on any user action.
     * @see #closeOnActionListener
     */
    private void removeCloseOnActionListeners() {
        // use workbench display, because can be called more than once,
        // including when this shell and the parent shell are already discarded
        final Display d = PlatformUI.getWorkbench().getDisplay();
        d.removeFilter(SWT.MouseDown, closeOnActionListener);
        d.removeFilter(SWT.Selection, closeOnActionListener);
        d.removeFilter(SWT.KeyDown, closeOnActionListener);
    }

    /**
     * Text to show in the title.
     * Is static to make sure it does not rely on the instance members because
     * is called before calling "super" constructor.
     * @param canceled whether the action was canceled.
     * @return the title text. Can be <code>null</code>.
     */
    protected static String getTitleText(final boolean canceled) {
        return canceled
                ? MESSAGES.get("title.canceled")
                : MESSAGES.get("title.reminder");
    }

    /** {@inheritDoc} */
    @Override
    protected Point getInitialLocation(final Point initialSize) {
        final Point p = getDisplay().getCursorLocation();
        p.x += DISTANCE_TO_CURSOR;
        p.y = Math.max(p.y - initialSize.y / 2, 0); 
        return p;
    }

    /**
     * Generates the text shown at the bottom of the popup.
     * Is static to make sure it does not rely on the instance members because
     * is called before calling "super" constructor.
     * @return the text. Never <code>null</code>.
     */
    protected static String getActionConfigurationReminder() {
        return REMINDER_FACTORY.getText();
    }

    public AbstractNagPopUp(Shell parent, int shellStyle,
            boolean takeFocusOnOpen, boolean persistSize,
            boolean persistLocation, boolean showDialogMenu,
            boolean showPersistActions, String titleText, String infoText) {
        super(parent, shellStyle, takeFocusOnOpen, persistSize,
                persistLocation, showDialogMenu, showPersistActions, titleText,
                infoText);
    }

}