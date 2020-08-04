package de.unijena.bioinf.ms.gui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class WarningDialog extends DoNotShowAgainDialog {


    public WarningDialog(Window owner, String warning) {
        this(owner, warning, null);
    }

    /**
     * @param owner       see JDialog
     * @param warning     Warning of this dialog
     * @param propertyKey name of the property with which the 'don't ask' flag is saved persistently
     */
    public WarningDialog(Window owner, String warning, String propertyKey) {
        this(owner, "", warning, propertyKey);
    }

    public WarningDialog(Window owner, String title, String warning, String propertyKey) {
        this(owner, title, () -> warning, propertyKey);
    }

    public WarningDialog(Window owner, String title, Supplier<String> messageProvider, String propertyKey) {
        super(owner, title, messageProvider, propertyKey);
        this.setVisible(true);
    }

    @Override
    protected void decorateButtonPanel(JPanel boxedButtonPanel) {
        boxedButtonPanel.add(Box.createHorizontalGlue());
        addOKButton(boxedButtonPanel);
    }

    protected void addOKButton(JPanel boxedButtonPanel) {
        final JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            saveDoNotAskMeAgain();
            dispose();
        });

        boxedButtonPanel.add(ok);
    }

    @Override
    protected Icon makeDialogIcon() {
        return UIManager.getIcon("OptionPane.warningIcon");
    }
}
