/*
 * Copyright (C) Heavy Lifting Software 2007, Robert Wloch 2012.
 *
 * This file is part of MouseFeed.
 *
 * MouseFeed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MouseFeed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MouseFeed.  If not, see <http://www.gnu.org/licenses/>.
 */
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

//COUPLING:OFF - just uses a lot of other classes. It's Ok.
/**
 * Pop-up dialog, which notifies a user about wrong mouse/accelerator usage. 
 *
 * @author Andriy Palamarchuk
 * @author Robert Wloch
 */
public class NagPopUp extends AbstractNagPopUp {
    /**
     * @see NagPopUp#NagPopUp(String, String)
     */
    private final String actionId;
    
    /**
     * The notification link.
     */
    @SuppressWarnings("unused") Link actionLink;
    
    /**
     * Creates a pop-up with notification for the specified accelerator
     * and action.
     *
     * @param actionName the action label. Not blank.
     * @param accelerator the string describing the accelerator. Not blank.
     * @param actionCancelled indicates whether MouseFeed canceled the action
     * the popup notifies about. 
     */
    public NagPopUp(
            final String actionName, final String accelerator, final boolean actionCancelled) {
        super((Shell) null, PopupDialog.HOVER_SHELLSTYLE,
                false, false, false, false, false,
                getTitleText(actionCancelled),
                getActionConfigurationReminder());
        isTrue(StringUtils.isNotBlank(actionName));
        isTrue(StringUtils.isNotBlank(accelerator));

        this.actionName = actionName;
        this.accelerator = accelerator;
        this.actionCancelled = actionCancelled;
        this.actionId = null;
    }

    /**
     * Creates a pop-up with suggestion to open the Keys preference page to
     * configure a keyboard shortcut for an action.
     *
     * @param actionName the action label. Not blank.
     * @param actionId the contribution id. Not blank.
     */
    public NagPopUp(
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
        
        if (isLinkPopup()) {
            final String linkText = MESSAGES.get("message.configureShortcut", actionName);
            actionLink = createLink(composite, linkText);
        } else {
            actionDescriptionText = createActionDescriptionText(composite);
        }

        composite.pack(true);

        return composite;
    }

    /**
     * Reusable check for actionId not null and accelerator being null.
     * @return true if actionId != null && accelerator == null
     */
    protected boolean isLinkPopup() {
        return actionId != null && accelerator == null;
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
//COUPLING:ON
