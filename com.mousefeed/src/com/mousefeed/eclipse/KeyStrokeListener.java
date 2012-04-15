package com.mousefeed.eclipse;

import com.mousefeed.eclipse.preferences.PreferenceAccessor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

public class KeyStrokeListener implements Listener {
    /**
     * Provides access to the plugin preferences.
     */
    private final PreferenceAccessor preferences =
            PreferenceAccessor.getInstance();

    private final IBindingService bindingService;

    public KeyStrokeListener() {
        bindingService = (IBindingService) PlatformUI.getWorkbench()
                .getAdapter(IBindingService.class);
    }

    public void handleEvent(Event event) {
        if (!preferences.isShowUsedKeyboardShortcutEnabled()) {
            return;
        }
        Collection<KeySequence> keySequences = generateKeySequences(event);
        Binding binding = determineFirstPerfectMatchForKeySequences(keySequences);
        if (binding != null) {
            NagPopUp nagPopUp = createPopUpForBinding(binding);
            nagPopUp.open();
        }
    }

    private Binding determineFirstPerfectMatchForKeySequences(
            Collection<KeySequence> keySequences) {
        for (KeySequence keySequence : keySequences) {
            if (bindingService.isPerfectMatch(keySequence)) {
                return bindingService.getPerfectMatch(keySequence);
            }
        }
        return null;
    }

    private NagPopUp createPopUpForBinding(Binding perfectMatch) {
        ParameterizedCommand command = perfectMatch.getParameterizedCommand();
        String commandName;
        try {
            commandName = command.getName();
        } catch (NotDefinedException exception) {
            commandName = "undefined command";
        }
        TriggerSequence triggerSequence = perfectMatch.getTriggerSequence();
        return new NagPopUp(commandName, triggerSequence.toString(), false);
    }

    /**
     * Generates any key sequence that are near matches to the given event. The
     * first such key sequence is always the exactly matching key sequence.
     * 
     * @param event
     *            The event from which the key sequence should be generated.
     * @return The set of nearly matching key sequence, may be empty.
     */
    private Collection<KeySequence> generateKeySequences(Event event) {
        Set<KeySequence> keySequence = new HashSet<KeySequence>();

        // Add each unique key stroke to the list for consideration.
        int firstAccelerator = SWTKeySupport
                .convertEventToUnmodifiedAccelerator(event);
        keySequence.add(convertAcceleratorToKeySequence(firstAccelerator));

        // We shouldn't allow delete to undergo shift resolution.
        if (event.character == SWT.DEL) {
            return keySequence;
        }

        int secondAccelerator = SWTKeySupport
                .convertEventToUnshiftedModifiedAccelerator(event);
        keySequence.add(convertAcceleratorToKeySequence(secondAccelerator));

        int thirdAccelerator = SWTKeySupport
                .convertEventToModifiedAccelerator(event);
        keySequence.add(convertAcceleratorToKeySequence(thirdAccelerator));

        return keySequence;
    }

    private static KeySequence convertAcceleratorToKeySequence(int accelerator) {
        KeyStroke keyStroke = SWTKeySupport
                .convertAcceleratorToKeyStroke(accelerator);
        return KeySequence.getInstance(keyStroke);
    }
}
