package com.mousefeed.eclipse.popup;

import static org.apache.commons.lang.Validate.isTrue;

import com.mousefeed.eclipse.Activator;
import java.util.HashSet;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.commands.ICommandService;

public class ConfigureShortcutPopUp extends AbstractNagPopUp {
    /**
     * @see ConfigureShortcutPopUp(String, String)
     */
    private final String actionId;

    /**
     * Creates a pop-up with suggestion to open the Keys preference page to
     * configure a keyboard shortcut for an action.
     *
     * @param actionName the action label. Not blank.
     * @param actionId the contribution id. Not blank.
     */
    public ConfigureShortcutPopUp(
            final String actionName, final String actionId) {
        super((Shell) null, PopupDialog.HOVER_SHELLSTYLE,
                false, false, false, false, false,
                getTitleText(false),
                getActionConfigurationReminder());
        isTrue(StringUtils.isNotBlank(actionName));
        isTrue(StringUtils.isNotBlank(actionId));

        this.actionName = actionName;
        this.actionId = actionId;
        this.actionCancelled = false;
        this.accelerator = null;
    }

    /**
     * Creates a control for showing the info.
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NO_FOCUS);
        composite.setLayout(new FormLayout());

        final String linkText = MESSAGES.get("message.configureShortcut", actionName);
        createLink(composite, linkText);

        composite.pack(true);
        return composite;
    }

    /**
     * Creates the link control to show a hyperlink to the Keys
     * preference page.
     * 
     * @param parent the parent control. Not <code>null</code>. 
     * @param text the text of the link
     * @return the link control. Not <code>null</code>.
     */
    protected Link createLink(final Composite parent, final String text) {
        final Link link = new Link(parent, SWT.NONE);
        link.setFont(parent.getFont());
        link.setText("<A>" + text + "</A>");  //$NON-NLS-1$//$NON-NLS-2$
        
        configureFormData(link);
        fontHelper.configureBigFont(link);

        link.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                doLinkActivated();
            }
        });

        return link;
    }
    
    /**
     * Handle link activation.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    final void doLinkActivated() {
        Object data = null;
        
        final IWorkbench workbench = Activator.getDefault().getWorkbench();
        final ICommandService commandService = (ICommandService) workbench.getService(ICommandService.class);
        
        final Command command = commandService.getCommand(actionId);
        if (command != null) {
            final HashSet allParameterizedCommands = new HashSet();
            try {
                allParameterizedCommands.addAll(ParameterizedCommand
                        .generateCombinations(command));
            } catch (final NotDefinedException e) {
                // It is safe to just ignore undefined commands.
            }
            if (!allParameterizedCommands.isEmpty()) {
                data = allParameterizedCommands.iterator().next();
                
                // only commands can be bound to keyboard shortcuts
                openWorkspacePreferences(data);
            }
        }

    }

    /**
     * Opens the preference dialog with optional data.
     * 
     * @param data an optional data object that can be handed as a parameter to the preference dialog. May be null.
     */
    protected final void openWorkspacePreferences(final Object data) {
        final Display display = Display.getCurrent();
        final PreferenceDialogLauncher runnable = new PreferenceDialogLauncher(data);
        display.asyncExec(runnable);
    }
    

}
