package com.mousefeed.eclipse.popup;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ShowShortcutPopUp extends AbstractNagPopUp implements PositionablePopUp {

    private final ShowShortcutPositioner positioner;

    public ShowShortcutPopUp(String actionName, String accelerator, ShowShortcutPositioner positioner) {
        super((Shell) null, PopupDialog.HOVER_SHELLSTYLE,
                false, false, false, false, false,
                getTitleText(false),
                getActionConfigurationReminder());
        this.actionName = actionName;
        this.accelerator = accelerator;
        this.positioner = positioner;
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

    /** {@inheritDoc} */
    @Override
    protected Point getInitialLocation(final Point initialSize) {
        final Rectangle bounds = getDisplay().getBounds();
        positioner.register(this, bounds, initialSize);
        return positioner.getPosition(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean close() {
        positioner.deRegister(this);
        return super.close();
    }

    public void setLocation(Point position) {
        getShell().setLocation(position);
    }

}
