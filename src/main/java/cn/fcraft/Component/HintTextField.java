package cn.fcraft.Component;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author HJH201314
 * Add a hint to the JTextField.
 */
public class HintTextField extends JTextField implements FocusListener {

    private String hint;

    public HintTextField(String hint) {
        super(hint);
        this.hint = hint;
        super.setForeground(Color.GRAY);
        this.addFocusListener(this);
    }

    public HintTextField setHint(String hint) {
        this.hint = hint;
        return this;
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().equals(hint)) {
            super.setText("");
            super.setForeground(Color.BLACK);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setForeground(Color.GRAY);
            super.setText(hint);
        }
    }

    public String getInputText() {
        if (super.getText().equals(hint)) {
            return "";
        }
        return super.getText();
    }

}

