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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

//COUPLING:OFF - just uses a lot of other classes. It's Ok.
/**
 * Pop-up dialog, which notifies a user about wrong mouse/accelerator usage. 
 *
 * @author Andriy Palamarchuk
 * @author Robert Wloch
 */
public class NagPopUp extends AbstractNagPopUp {
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
    }

    /**
     * Creates a control for showing the info.
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NO_FOCUS);
        composite.setLayout(new FormLayout());

        actionDescriptionText = createActionDescriptionText(composite);

        composite.pack(true);
        return composite;
    }

}
//COUPLING:ON
