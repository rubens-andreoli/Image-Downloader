package com.mycompany.imagedownloader.view_controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;

/** References:
 * https://stackoverflow.com/questions/3622402/how-to-intercept-keyboard-strokes-going-to-java-swing-jtextfield
 * https://stackoverflow.com/questions/4863850/disable-input-some-symbols-to-jtextfield
 * https://stackoverflow.com/questions/46343616/how-can-i-convert-a-char-to-int-in-java
 */
public final class NumberField extends JTextField{
    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_VALUE = 0;
    private int maxValue;
    
    public NumberField() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(getText().isEmpty()){
                    setText(String.valueOf(DEFAULT_VALUE));
                    return;
                }
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                    return;
                }
                if(maxValue > 0){
                    try{
                        int number = Integer.parseInt(getText());
                        if(number == DEFAULT_VALUE){
                            setText("");
                        }
                        if(Integer.parseInt(number+""+c) > maxValue){
                            e.consume();
//                            setText(String.valueOf(maxValue));
                            setText(String.valueOf(DEFAULT_VALUE));
                        }
                    }catch(NumberFormatException ex){
                        System.err.println("Can't parse field value");
                        setText(String.valueOf(DEFAULT_VALUE));
                    }
                }
            }
        });
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue>=0? maxValue:0;
    }
    
    public int getInt(){
        return Integer.parseInt(getText());
    }        
    
}
